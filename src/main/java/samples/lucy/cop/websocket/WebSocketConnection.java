/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.lucy.cop.websocket;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpClientConnection;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;

import samples.lucy.cop.websocket.protocol.WebSocketFrame;
import samples.lucy.cop.websocket.protocol.WebSocketProtocol;

/**
 * A web socket connection.
 */
public final class WebSocketConnection {

  /**
   * The character encoding used in this web socket.
   */
  public static final String UTF8 = "UTF-8";

  private static final int INITIAL = 0;
  private static final int OPEN = 1;
  private static final int CLOSING = 2;
  private static final int CLOSED = 3;

  private static final int OPCODE_TEXT = 0x1;
  private static final int OPCODE_BINARY = 0x2;
  private static final int OPCODE_CLOSE = 0x8;
  private static final int OPCODE_PING = 0x9;
  private static final int OPCODE_PONG = 0xA;

  // We need to hold on to the http client; otherwise the http client will
  // get garbage collected and our underlying connection will get closed
  // by the finalizer of the connection manager.
  @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
  private DefaultHttpClient fClient;
  private HttpClientConnection fConnection;
  private SessionInputBuffer fInBuffer;
  private SessionOutputBuffer fOutBuffer;
  private WebSocketListener fListener;
  private Thread fInputThread;
  private WebSocketProtocol fProtocol;

  private AtomicInteger fState;

  /**
   * Creates a new web socket connection.
   *
   * @param aClient     The HTTP client.
   * @param aConnection The HTTP connection to use.
   * @param aInBuffer   The input buffer.
   * @param aOutBuffer  The output buffer.
   * @param aProtocol   The protocol to use.
   * @param aListener   The web socket listener.
   */
  public WebSocketConnection(
      DefaultHttpClient aClient, HttpClientConnection aConnection,
      SessionInputBuffer aInBuffer,
      SessionOutputBuffer aOutBuffer,
      WebSocketProtocol aProtocol,
      WebSocketListener aListener
                            ) {
    fClient = aClient;
    this.fConnection = aConnection;
    this.fInBuffer = aInBuffer;
    this.fOutBuffer = aOutBuffer;
    fListener = aListener;
    fProtocol = aProtocol;
    fState = new AtomicInteger(INITIAL);
  }

  /**
   * Allow this connection to receive incoming data.
   */
  public void open() {
    if (fState.compareAndSet(INITIAL, OPEN)) {
      fInputThread = new Thread(new Runnable() {
        @Override
        public void run() {
          WebSocketFrame frame = new WebSocketFrame();
          while (!Thread.currentThread().isInterrupted()) {
            try {
              fProtocol.receiveFrame(WebSocketConnection.this.fInBuffer, frame);
              handleFrame(frame);
            } catch (IOException e) {
              shutdown(e);
              break;
            }
          }
        }
      });
      fInputThread.start();
    }
  }

  private void handleFrame(WebSocketFrame aFrame) throws IOException {
    int opcode = aFrame.getOpcode();
    if (opcode == OPCODE_TEXT) {
      if (isOpen()) {
        String string = new String(aFrame.getPayload(), UTF8);
        fListener.onText(this, string);
      }
    } else if (opcode == OPCODE_BINARY) {
      if (isOpen()) {
        fListener.onData(this, aFrame.getPayload());
      }
    } else if (opcode == OPCODE_CLOSE) {
      int code = WebSocketListener.NO_STATUS_CODE;
      String reason = "";

      if (isOpen()) {
        byte[] payload = aFrame.getPayload();

        if (payload.length >= 2) {
          code = (payload[0] & 0xFF) << 8 | (payload[1] & 0xFF);
          reason = new String(payload, 2, Math.max(0, payload.length - 2), UTF8);
        }
        close();
      }
      shutdown(code, reason);
    } else if (opcode == OPCODE_PING) {
      if (isOpen()) {
        WebSocketFrame pong = new WebSocketFrame();
        pong.update(true, OPCODE_PONG, aFrame.getPayload());
        try {
          fProtocol.sendFrame(fOutBuffer, pong);
        } catch (IOException e) {
          shutdown(e);
        }
      }
    } else if (opcode == OPCODE_PONG) {
      // Nothing to do
    }
  }

  /**
   * Send text over this web socket connection.
   *
   * @param aText The text to send.
   *
   * @throws IOException If the connection is already closed.
   */
  public void sendText(String aText) throws IOException {
    if (!isOpen()) {
      throw new IOException("Connection closed");
    }
    byte[] textBytes = aText.getBytes(UTF8);
    WebSocketFrame frame = new WebSocketFrame();
    frame.update(true, OPCODE_TEXT, textBytes);
    fProtocol.sendFrame(fOutBuffer, frame);
  }

  /**
   * Send byte data over this web socket connection.
   *
   * @param aData The byte data to send.
   *
   * @throws IOException If the connection is already closed.
   */
  public void sendData(byte[] aData) throws IOException {
    if (!isOpen()) {
      throw new IOException("Connection closed");
    }
    WebSocketFrame frame = new WebSocketFrame();
    frame.update(true, OPCODE_BINARY, aData);
    fProtocol.sendFrame(fOutBuffer, frame);
  }

  public boolean isOpen() {
    return fState.get() == OPEN;
  }

  /**
   * Close this web socket connection.
   */
  public void close() {
    if (fState.compareAndSet(OPEN, CLOSING)) {
      WebSocketFrame close = new WebSocketFrame();
      close.update(true, OPCODE_CLOSE, new byte[0]);
      try {
        fProtocol.sendFrame(fOutBuffer, close);
      } catch (IOException e) {
        shutdown(e);
      }
    }
  }

  private void shutdown(IOException aException) {
    shutdown(WebSocketListener.ABNORMAL_TERMINATION, aException.getMessage());
  }

  private void shutdown(int aCode, String aReason) {
    if (fState.get() != CLOSED) {
      try {
        fInputThread.interrupt();
        fState.set(CLOSED);
        fConnection.close();
        fListener.onClosed(this, aCode, aReason);
      } catch (IOException e) {
        // Ignored
      }
    }
  }
}
