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
package samples.lucy.cop;

/**
 * In the COP sample, the paths in the configuration files may refer to the base path of the server
 * by using the {@code %webServerLocation%} and {@code %webSocketServerLocation%} variable. This
 * class can be used to replace this variable by its real value.
 */
public final class PathResolver {
  private static final String WEB_SERVER_LOCATION = "%webServerLocation%";
  private static final String WEB_SERVICE_SERVER_LOCATION = "%webSocketServerLocation%";

  private final String fServerLocation;
  private final String fWebSocketServerLocation;

  /**
   * <p>Creates a new path resolver.</p>
   *
   * <p>You should not use this constructor. An instance of this class is available on the services,
   * and should be retrieved from there.</p>
   *
   * @param aWebServerLocation       The location of the web server
   * @param aWebSocketServerLocation The location of the web socket server
   */
  public PathResolver(String aWebServerLocation, String aWebSocketServerLocation) {
    fServerLocation = aWebServerLocation;
    fWebSocketServerLocation = aWebSocketServerLocation;
  }

  /**
   * Converts {@code aPath} to an absolute path by replacing all the known path variables with their
   * appropriate value
   *
   * @param aPath The path
   *
   * @return The converted version of {@code aPath}
   */
  public String convertPath(String aPath) {
    if (aPath == null) {
      return null;
    }
    return aPath.replaceAll(WEB_SERVER_LOCATION, fServerLocation).replaceAll(WEB_SERVICE_SERVER_LOCATION, fWebSocketServerLocation);
  }
}
