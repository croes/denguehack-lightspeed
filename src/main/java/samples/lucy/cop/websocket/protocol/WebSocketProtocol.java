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

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;

/**
 * Interface for a web protocol.
 */
public interface WebSocketProtocol {

  /**
   * Generates a nonce.
   *
   * @return The generated nonce.
   *
   * @throws IOException If the nonce could not be generated.
   */
  String[] generateNonce() throws IOException;

  /**
   * Creates a client handshake request.
   *
   * @param aUri   The request uri.
   * @param aNonce The nonce to use.
   *
   * @return An HTTP request.
   */
  HttpUriRequest createClientHandshakeRequest(URI aUri, String[] aNonce);

  /**
   * Check the response of the server handshake.
   *
   * @param aResponse The response to check.
   * @param aKey      The nonce to use for verification.
   *
   * @throws IOException If the response is invalid.
   */
  void checkServerHandshakeResponse(HttpResponse aResponse, String[] aKey) throws IOException;

  /**
   * Store the data from the input buffer in a frame.
   *
   * @param aBuffer The input buffer.
   * @param aFrame  The frame to store the data in.
   *
   * @throws IOException If the data could not be written to the input buffer.
   */
  void receiveFrame(SessionInputBuffer aBuffer, WebSocketFrame aFrame) throws IOException;

  /**
   * Send the data from a frame to the output buffer.
   *
   * @param aOutBuffer The output buffer to send data to.
   * @param aFrame     The frame that contains the data to send.
   *
   * @throws IOException If the data could not be written to the output buffer.
   */
  void sendFrame(SessionOutputBuffer aOutBuffer, WebSocketFrame aFrame) throws IOException;

}
