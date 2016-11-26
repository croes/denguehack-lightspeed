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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import com.luciad.ogc.ows.model.ILcdOWSRequest;
import com.luciad.ogc.ows.model.ILcdOWSTransport;
import com.luciad.ogc.ows.model.TLcdOWSHttpTransport;

/**
 * Factory to create an <code>ILcdOWSTransport</code> for initialization of an OGC client.
 *
 * OGC client. support for HTTP Basic Authentication. Such an ILcdOWSTransport is required to create
 * an OGC client (e.g., TLcdWFSClient).
 */
public class OWSTransportFactory {

  private static final String PROPERTY_USERNAME = "username";

  private static final String PROPERTY_PASSWORD = "password";

  /**
   * Creates a transport based on the given properties. The following properties are supported:
   * <html:ul> <html:li>username: username to be used for HTTP Basic Authentication</html:li>
   * <html:li>password: password to be used for HTTP Basic Authentication</html:li> </html:ul>
   *
   * @param aProperties Properties instance with configuration parameters for the transport. Can be
   *                    <code>null</code>.
   *
   * @return a transport based on the given properties.
   */
  public static ILcdOWSTransport createTransport(Properties aProperties) {
    String username = null, password = null;

    if (aProperties != null) {
      username = aProperties.getProperty(PROPERTY_USERNAME);
      password = aProperties.getProperty(PROPERTY_PASSWORD);
    }

    if (username != null && password != null) {
      return new OWSBasicAuthenticationTransport(username, password);
    } else {
      return new TLcdOWSHttpTransport();
    }
  }

  private static class OWSBasicAuthenticationTransport extends TLcdOWSHttpTransport {

    private String fCredentials;

    public OWSBasicAuthenticationTransport(String aUsername, String aPassword) {
      fCredentials = encode(aUsername + ":" + aPassword);
    }

    protected void configureConnection(URI aURI, ILcdOWSRequest aRequest,
                                       HttpURLConnection aConnection) throws IOException {
      super.configureConnection(aURI, aRequest, aConnection);
      aConnection.setRequestProperty("Authorization", "Basic " + fCredentials);
    }

    private String decode(String s) {
      return StringUtils.newStringUtf8(Base64.decodeBase64(s));
    }

    private String encode(String s) {
      return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
    }
  }
}
