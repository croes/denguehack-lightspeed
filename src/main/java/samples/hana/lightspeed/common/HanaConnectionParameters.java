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
package samples.hana.lightspeed.common;

import com.luciad.gui.TLcdAWTUtil;

import samples.hana.lightspeed.ui.ConnectionDialog;

/**
 * Singleton holding the HANA database connection parameters URL, user name and password.
 */
public class HanaConnectionParameters {
  private final String fUrl;
  private final String fUser;
  private final String fPassword;
  private static HanaConnectionParameters sParams;

  public static HanaConnectionParameters getInstance() {
    if (sParams == null) {
      String url = Configuration.get("database.url");
      String user = Configuration.get("database.username");
      String password = Configuration.get("database.password");
      sParams = new HanaConnectionParameters(url, user, password);

      while (!sParams.isValid()) {
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            sParams = ConnectionDialog.showDialog();
          }
        });
      }

    }
    return sParams;
  }

  public HanaConnectionParameters(String aUrl, String aUser, String aPassword) {
    fUrl = aUrl;
    fUser = aUser;
    fPassword = aPassword;
  }

  public String getUrl() {
    return fUrl;
  }

  public String getUser() {
    return fUser;
  }

  public String getPassword() {
    return fPassword;
  }

  private boolean isValid() {
    return !("".equals(fUrl) || "".equals(fUser) || "".equals(fPassword));
  }
}
