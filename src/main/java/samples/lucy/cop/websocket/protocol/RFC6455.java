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
package samples.lucy.cop.websocket.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;

/**
 * Implementation of RFC 6455 (http://tools.ietf.org/html/rfc6455).
 */
public class RFC6455 implements WebSocketProtocol {

  private static final String ASCII = "US-ASCII";
  private Random fRandom;

  /**
   * Creates a new web socket based on HyBi.
   */
  public RFC6455() {
    fRandom = new SecureRandom();
  }

  @Override
  public synchronized String[] generateNonce() throws IOException {
    byte[] nonce = new byte[16];
    for (int i = 0; i < nonce.length; i++) {
      nonce[i] = (byte) ('A' + fRandom.nextInt(25));
    }
    byte[] nonceBase64 = Base64.encodeBase64(nonce);

    MessageDigest d;
    try {
      d = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      IOException ioException = new IOException();
      ioException.initCause(e);
      throw ioException;
    }
    d.update(nonceBase64);
    d.update("258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(ASCII));
    byte[] responseAlt = Base64.encodeBase64(d.digest());

    return new String[]{
        new String(nonceBase64, 0, nonceBase64.length, ASCII),
        new String(responseAlt, 0, responseAlt.length, ASCII),
    };
  }

  /**
   * Creates a client handshake request.
   *
   * @param aUri   The request uri.
   * @param aNonce The nonce to use.
   *
   * @return An HTTP request.
   */
  @Override
  public HttpUriRequest createClientHandshakeRequest(URI aUri, String[] aNonce) {
    String scheme = aUri.getScheme();
    String requestScheme = scheme.equals("ws") ? "http" : "https";
    int requestPort = aUri.getPort() != -1 ? aUri.getPort() : scheme.equals("ws") ? 80 : 443;
    try {
      URI requestURI = new URI(requestScheme, aUri.getHost() + ":" + requestPort, aUri.getPath(), aUri.getRawQuery(), aUri.getRawFragment());

      HttpGet clientHandshake = new HttpGet(aUri.toString());
      clientHandshake.addHeader("Request-URI", requestURI.toString());
      clientHandshake.addHeader("Host", aUri.getHost());
      clientHandshake.addHeader("Upgrade", "websocket");
      clientHandshake.addHeader("Connection", "upgrade");
      clientHandshake.addHeader("Sec-WebSocket-Key", aNonce[0]);
      clientHandshake.addHeader("Sec-WebSocket-Version", Integer.toString(13));
      return clientHandshake;
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void checkServerHandshakeResponse(HttpResponse aResponse, String[] aKey) throws IOException {
    Header upgrade = aResponse.getFirstHeader("Upgrade");
    if (upgrade == null || !upgrade.getValue().equalsIgnoreCase("websocket")) {
      throw new IOException("Missing or invalid Upgrade header");
    }

    Header connection = aResponse.getFirstHeader("Connection");
    if (connection == null || !connection.getValue().equalsIgnoreCase("Upgrade")) {
      throw new IOException("Missing or invalid Connection header");
    }

    Header accept = aResponse.getFirstHeader("Sec-WebSocket-Accept");
    if (accept == null ||
        (!accept.getValue().equalsIgnoreCase(aKey[1]) && !accept.getValue().equalsIgnoreCase(aKey[2]))) {
      throw new IOException("Missing or invalid Sec-WebSocket-Accept header");
    }
  }

  @Override
  public synchronized void sendFrame(SessionOutputBuffer aOutBuffer, WebSocketFrame aFrame) throws IOException {
    byte[] payload = aFrame.getPayload();

    int fin = (aFrame.isFinal() ? 0x1 : 0x0) << 7;
    int opcode = aFrame.getOpcode();
    int frame1 = fin | opcode;
    aOutBuffer.write(frame1);
    int mask = 0x1 << 7;
    int length = payload.length;
    if (length <= 125) {
      aOutBuffer.write(length | mask);
    } else if (length <= 65535) {
      aOutBuffer.write(126 | mask);
      aOutBuffer.write((length >> 8) & 0xFF);
      aOutBuffer.write(length & 0xFF);
    } else {
      aOutBuffer.write(127 | mask);
      aOutBuffer.write(0x00);
      aOutBuffer.write(0x00);
      aOutBuffer.write(0x00);
      aOutBuffer.write(0x00);
      aOutBuffer.write((length >> 24) & 0xFF);
      aOutBuffer.write((length >> 16) & 0xFF);
      aOutBuffer.write((length >> 8) & 0xFF);
      aOutBuffer.write(length & 0xFF);
    }

    int maskingKeyInt = fRandom.nextInt();
    int[] maskingKey = new int[]{
        (maskingKeyInt >> 24) & 0xFF,
        (maskingKeyInt >> 16) & 0xFF,
        (maskingKeyInt >> 8) & 0xFF,
        (maskingKeyInt) & 0xFF
    };
    aOutBuffer.write(maskingKey[0]);
    aOutBuffer.write(maskingKey[1]);
    aOutBuffer.write(maskingKey[2]);
    aOutBuffer.write(maskingKey[3]);

    for (int i = 0, m = 0; i < payload.length; i++, m++) {
      if (m == 4) {
        m = 0;
      }
      payload[i] ^= maskingKey[m];
    }
    aOutBuffer.write(payload);
    aOutBuffer.flush();
  }

  @Override
  public void receiveFrame(SessionInputBuffer aBuffer, WebSocketFrame aFrame) throws IOException {
    int frame1 = readByte(aBuffer);
    boolean fin = (frame1 >> 7) != 0;
    int opcode = frame1 & 0xF;

    int frame2 = readByte(aBuffer);
    boolean masked = (frame2 >> 7) != 0;
    int length = frame2 & 0x7F;
    if (length == 126) {
      length = (readByte(aBuffer) << 8) | readByte(aBuffer);
    } else if (length == 127) {
      long l = ((readByte(aBuffer) & 0xFFL) << 56) |
               ((readByte(aBuffer) & 0xFFL) << 48) |
               ((readByte(aBuffer) & 0xFFL) << 40) |
               ((readByte(aBuffer) & 0xFFL) << 32) |
               ((readByte(aBuffer) & 0xFFL) << 24) |
               ((readByte(aBuffer) & 0xFFL) << 16) |
               ((readByte(aBuffer) & 0xFFL) << 8) |
               (readByte(aBuffer) & 0xFFL);
      if (l > Integer.MAX_VALUE) {
        // Not yet supported
      } else {
        length = (int) l;
      }
    }

    byte[] data = new byte[length];
    if (masked) {
      int[] maskingKey = new int[]{
          readByte(aBuffer),
          readByte(aBuffer),
          readByte(aBuffer),
          readByte(aBuffer)
      };
      for (int i = 0, m = 0; i < data.length; i++, m++) {
        if (m == 4) {
          m = 0;
        }
        int read = readByte(aBuffer);
        if (read == -1) {
          throw new EOFException();
        }
        data[i] = (byte) (read ^ maskingKey[m]);
      }
    } else {
      for (int i = 0; i < data.length; i++) {
        int read = readByte(aBuffer);
        if (read == -1) {
          throw new EOFException();
        }
        data[i] = (byte) read;
      }
    }

    aFrame.update(fin, opcode, data);
  }

  private int readByte(SessionInputBuffer aBuffer) throws IOException {
    int value = aBuffer.read();
    if (value == -1) {
      throw new EOFException();
    }
    return value;
  }
}
