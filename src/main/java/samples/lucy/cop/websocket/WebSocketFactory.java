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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import samples.lucy.cop.websocket.protocol.RFC6455;
import samples.lucy.cop.websocket.protocol.WebSocketProtocol;

/**
 * Factory to create web socket connections.
 */
@SuppressWarnings("deprecation")
public final class WebSocketFactory {

  /**
   * Creates a new web socket factory.
   */
  public WebSocketFactory() {
  }

  /**
   * Create a new web socket connection for the given URI.
   *
   * @param aUri      The address of the remote client to connect with.
   * @param aListener The listener that handles incoming data.
   *
   * @return A web socket connection.
   *
   * @throws IOException If the connection could not be established.
   */
  public WebSocketConnection connect(URI aUri, WebSocketListener aListener) throws IOException {
    return connect(aUri, aListener, new RFC6455());
  }

  /**
   * Create a new web socket connection for the given URI and protocol.
   *
   * @param aUri      The address of the remote client to connect with.
   * @param aListener The listener that handles incoming data.
   * @param aProtocol The protocol to use for sending and receiving messages.
   *
   * @return A web socket connection.
   *
   * @throws IOException If the connection could not be established.
   */
  public WebSocketConnection connect(URI aUri, WebSocketListener aListener, WebSocketProtocol aProtocol) throws IOException {
    if (aUri == null) {
      throw new NullPointerException("URI must not be null");
    }

    if (aProtocol == null) {
      throw new NullPointerException("Protocol must not be null");
    }

    String scheme = aUri.getScheme();
    if (scheme == null || (!scheme.equals("ws") && !scheme.equals("wss"))) {
      throw new IllegalArgumentException("Invalid scheme: " + aUri);
    }

    DefaultHttpClient client = new WebSocketHttpClient();

    String[] key = aProtocol.generateNonce();

    HttpUriRequest clientHandshake = aProtocol.createClientHandshakeRequest(aUri, key);

    HttpResponse response = client.execute(clientHandshake);
    if (response instanceof WebSocketAcceptResponse) {
      aProtocol.checkServerHandshakeResponse(response, key);
      WebSocketClientConnection webSocketClientConnection = ((WebSocketAcceptResponse) response).fConnection;
      return new WebSocketConnection(
          client,
          webSocketClientConnection,
          webSocketClientConnection.fInbuffer,
          webSocketClientConnection.fOutbuffer,
          aProtocol,
          aListener
      );
    }

    return null;
  }

  private static class WebSocketHttpClient extends DefaultHttpClient {

    public WebSocketHttpClient() {
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {
      SchemeRegistry registry = new SchemeRegistry();
      registry.register(new Scheme("ws", PlainSocketFactory.getSocketFactory(), 80));
      registry.register(new Scheme("wss", SSLSocketFactory.getSocketFactory(), 443));

      return new WebSocketClientConnManager(registry);
    }

    @Override
    protected HttpRequestExecutor createRequestExecutor() {
      return new WebSocketHttpRequestExecutor();
    }
  }

  private static class WebSocketHttpRequestExecutor extends HttpRequestExecutor {

    @Override
    protected boolean canResponseHaveBody(HttpRequest aRequest, HttpResponse aResponse) {
      return super.canResponseHaveBody(aRequest, aResponse) ||
             aResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SWITCHING_PROTOCOLS;
    }

    @Override
    protected HttpResponse doReceiveResponse(HttpRequest aRequest, HttpClientConnection aConnection, HttpContext aContext) throws HttpException, IOException {
      if (aRequest == null) {
        throw new IllegalArgumentException("HTTP request must not be null");
      }
      if (aConnection == null) {
        throw new IllegalArgumentException("HTTP connection must not be null");
      }
      if (aContext == null) {
        throw new IllegalArgumentException("HTTP context must not be null");
      }

      HttpResponse response = null;
      int statuscode = 0;

      while (response == null || (statuscode != HttpStatus.SC_SWITCHING_PROTOCOLS && statuscode < HttpStatus.SC_OK)) {

        response = aConnection.receiveResponseHeader();
        if (canResponseHaveBody(aRequest, response)) {
          aConnection.receiveResponseEntity(response);
        }
        statuscode = response.getStatusLine().getStatusCode();

      } // while intermediate response

      return response;
    }
  }

  @SuppressWarnings("deprecation")
  private static class WebSocketClientConnManager extends org.apache.http.impl.conn.SingleClientConnManager {

    public WebSocketClientConnManager(SchemeRegistry aRegistry) {
      super(new BasicHttpParams(), aRegistry);
    }

    @Override
    protected ClientConnectionOperator createConnectionOperator(SchemeRegistry aSchemeRegistry) {
      return new DefaultClientConnectionOperator(aSchemeRegistry) {
        @Override
        public OperatedClientConnection createConnection() {
          return new WebSocketClientConnection();
        }
      };
    }
  }

  private static class WebSocketClientConnection extends DefaultClientConnection {

    private SessionInputBuffer fInbuffer;
    private SessionOutputBuffer fOutbuffer;

    @Override
    protected void init(SessionInputBuffer aInBuffer, SessionOutputBuffer aOutBuffer, HttpParams aParams) {
      super.init(aInBuffer, aOutBuffer, aParams);
      this.fInbuffer = aInBuffer;
      this.fOutbuffer = aOutBuffer;
    }

    @Override
    protected HttpResponseFactory createHttpResponseFactory() {
      return new DefaultHttpResponseFactory() {
        @Override
        public HttpResponse newHttpResponse(final ProtocolVersion aVersion,
                                            final int aStatus,
                                            HttpContext aContext) {
          if (aVersion == null) {
            throw new IllegalArgumentException("HTTP version must not be null");
          }
          if (aStatus == HttpStatus.SC_SWITCHING_PROTOCOLS) {
            return new WebSocketAcceptResponse(WebSocketClientConnection.this, aVersion, aStatus, "");
          } else {
            return super.newHttpResponse(aVersion, aStatus, aContext);
          }
        }

        @Override
        public HttpResponse newHttpResponse(final StatusLine aStatusLine,
                                            HttpContext aContext) {
          if (aStatusLine == null) {
            throw new IllegalArgumentException("Status line must not be null");
          }
          if (aStatusLine.getStatusCode() == HttpStatus.SC_SWITCHING_PROTOCOLS) {
            return new WebSocketAcceptResponse(WebSocketClientConnection.this, aStatusLine);
          } else {
            return super.newHttpResponse(aStatusLine, aContext);
          }
        }
      };
    }

    @Override
    public void receiveResponseEntity(HttpResponse aResponse) throws HttpException, IOException {
      if (aResponse == null) {
        throw new IllegalArgumentException("HTTP response must not be null");
      }
      assertOpen();

      if (aResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SWITCHING_PROTOCOLS) {
        aResponse.setEntity(new WebSocketEntity());
      } else {
        super.receiveResponseEntity(aResponse);
      }
    }
  }

  private static final class WebSocketEntity implements HttpEntity {

    public WebSocketEntity() {
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean isChunked() {
      return false;
    }

    @Override
    public long getContentLength() {
      return -1;
    }

    @Override
    public Header getContentType() {
      return null;
    }

    @Override
    public Header getContentEncoding() {
      return null;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
      return null;
    }

    @Override
    public void writeTo(OutputStream aOutputStream) throws IOException {
    }

    @Override
    public boolean isStreaming() {
      return true;
    }

    @Override
    public void consumeContent() throws IOException {
    }
  }

  private static final class WebSocketAcceptResponse extends BasicHttpResponse {

    private WebSocketClientConnection fConnection;

    private WebSocketAcceptResponse(WebSocketClientConnection aConnection, StatusLine aStatusLine) {
      super(aStatusLine);
      fConnection = aConnection;
    }

    private WebSocketAcceptResponse(WebSocketClientConnection aConnection, ProtocolVersion aVersion, int aCode, String aReason) {
      super(aVersion, aCode, aReason);
      this.fConnection = aConnection;
    }
  }
}
