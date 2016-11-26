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
package samples.ogc.server;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Sample extension of <code>com.thetransactioncompany.cors.CORSFilter</code>
 * that monitors the <code>cors.allowOrigin</code> filter property.
 * If this property's value is a wildcard (enabling access from any host), a warning is printed to inform
 * the user that this should preferably be changed in a production / operational environment.
 */
public class CORSFilter extends com.thetransactioncompany.cors.CORSFilter {

  private static final String PARAMETER_ALLOW_ORIGIN = "cors.allowOrigin";

  @Override
  public void init(FilterConfig aFilterConfig) throws ServletException {
    super.init(aFilterConfig);

    if ("*".equals(aFilterConfig.getInitParameter(PARAMETER_ALLOW_ORIGIN))) {
      System.out.println("INFO: The CORS Filter property 'cors.allowOrigin' is set to *, enabling access from any host. It is recommended to change this in a production / operational environment.");
    }
  }
}
