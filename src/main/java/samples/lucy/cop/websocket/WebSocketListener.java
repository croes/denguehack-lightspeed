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

/**
 *
 */
public interface WebSocketListener {

  /**
   * 1000 indicates a normal closure, meaning whatever purpose the
   * connection was established for has been fulfilled.
   */
  int NORMAL_CLOSURE = 1000;

  /**
   * 1001 indicates that an endpoint is "going away", such as a server
   * going down, or a browser having navigated away from a page.
   */
  int GOING_AWAY = 1001;

  /**
   * 1002 indicates that an endpoint is terminating the connection due
   * to a protocol error.
   */
  int PROTOCOL_ERROR = 1002;

  /**
   * 1003 indicates that an endpoint is terminating the connection
   * because it has received a type of data it cannot accept (e.g. an
   * endpoint that understands only text data MAY send this if it
   * receives a binary message).
   */
  int UNSUPPORTED_MESSAGE = 1003;

  /**
   * Reserved.  The specific meaning might be defined in the future.
   */
  int RESERVED1 = 1004;

  /**
   * 1005 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint.  It is designated for use in
   * applications expecting a status code to indicate that no status
   * code was actually present.
   */
  int NO_STATUS_CODE = 1005;

  /**
   * 1006 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint.  It is designated for use in
   * applications expecting a status code to indicate that the
   * connection was closed abnormally, e.g. without sending or
   * receiving a Close control frame.
   */
  int ABNORMAL_TERMINATION = 1006;

  /**
   * 1007 indicates that an endpoint is terminating the connection
   * because it has received data within a message that was not
   * consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
   * data within a text message).
   */
  int INCONSISTENT_MESSAGE = 1007;

  /**
   * 1008 indicates that an endpoint is terminating the connection
   * because it has received a message that violates its policy.  This
   * is a generic status code that can be returned when there is no
   * other more suitable status code (e.g. 1003 or 1009), or if there
   * is a need to hide specific details about the policy.
   */
  int POLICY_VIOLATION = 1008;

  /**
   * 1009 indicates that an endpoint is terminating the connection
   * because it has received a message which is too big for it to
   * process.
   */
  int MESSAGE_TOO_LARGE = 1009;

  /**
   * 1010 indicates that an endpoint (client) is terminating the
   * connection because it has expected the server to negotiate one or
   * more extension, but the server didn't return them in the response
   * message of the WebSocket handshake.  The list of extensions which
   * are needed SHOULD appear in the /reason/ part of the Close frame.
   * Note that this status code is not used by the server, because it
   * can fail the WebSocket handshake instead.
   */
  int MISSING_EXTENSIONS = 1010;

  /**
   * 1011 indicates that a server is terminating the connection because
   * it encountered an unexpected condition that prevented it from
   * fulfilling the request.
   */
  int SERVER_ERROR = 1011;

  /**
   * 1015 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint.  It is designated for use in
   * applications expecting a status code to indicate that the
   * connection was closed due to a failure to perform a TLS handshake
   * (e.g., the server certificate can't be verified).
   */
  int FAILED_TLS_HANDSHAKE = 1015;

  /**
   * Callback when a text message is received.
   *
   * @param aConnection The web socket connection.
   * @param aString     The received text message.
   */
  void onText(WebSocketConnection aConnection, String aString);

  /**
   * Callback when byte data is received.
   *
   * @param aConnection The web socket connection.
   * @param aPayload    The received byte data.
   */
  void onData(WebSocketConnection aConnection, byte[] aPayload);

  /**
   * Callback when the connection is closed.
   *
   * @param aConnection The web socket connection.
   * @param aCode       The code to identify the disconnect.
   * @param aReason     Message specifying the reason of disconnect.
   */
  void onClosed(WebSocketConnection aConnection, int aCode, String aReason);
}
