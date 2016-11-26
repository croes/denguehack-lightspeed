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
package samples.wms.client;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A GUI panel for selecting a server URL. The class also gives access to a set of default WMS urls for usage in the
 * WMS client samples.
 */
public class UrlPanel extends JPanel {

  public static final String LUCIAD_LIGHTSPEED_WMS_URL = "http://localhost:8080/LuciadLightspeedOGC/wms";

  public static final String LUCIAD_FUSION_WMS_URL = "http://localhost:8081/LuciadFusion/wms";

  public static final String[] DEFAULT_WMS_URL_LIST = new String[]{
      LUCIAD_LIGHTSPEED_WMS_URL,
      LUCIAD_FUSION_WMS_URL
  };

  private final JTextField fServerUrlTextField;
  private final JButton fQueryButton;

  public UrlPanel(String aDefaultUrl) {
    super(new BorderLayout(5, 0));

    fServerUrlTextField = new JTextField(aDefaultUrl);
    fQueryButton = new JButton("Query");

    JLabel urlLabel = new JLabel("Server URL: ");

    setLayout(new GridBagLayout());
    add(urlLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(fServerUrlTextField, new GridBagConstraints(2, 0, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    add(fQueryButton, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
  }

  public void setUrl(String aServerUrl) {
    fServerUrlTextField.setText(aServerUrl);
  }

  public void connect() {
    for (ActionListener actionListener : fQueryButton.getActionListeners()) {
      actionListener.actionPerformed(new ActionEvent(fQueryButton, ActionEvent.ACTION_PERFORMED, "connect"));
    }
  }

  public String getURL() {
    return fServerUrlTextField.getText();
  }

  public void addActionListener(ActionListener aActionListener) {
    fServerUrlTextField.addActionListener(aActionListener);
    fQueryButton.addActionListener(aActionListener);
  }

  /**
   * Returns a default WMS URL for usage in WMS client samples.
   * The first available / online WMS URL defined in {@link #DEFAULT_WMS_URL_LIST} is returned.
   * If none can be found, {@link #LUCIAD_LIGHTSPEED_WMS_URL} is returned.
   *
   * @return the first available / online WMS URL defined in {@link #DEFAULT_WMS_URL_LIST} or
   * {@link #LUCIAD_LIGHTSPEED_WMS_URL} otherwise.
   */
  public static String getDefaultWMSUrl() {
    for (String wmsUrl : DEFAULT_WMS_URL_LIST) {
      try {
        URL url = new URL(wmsUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("HEAD");
        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
          return wmsUrl;
        }
      } catch (IOException e) {
        // Ignore - we do not need to log this here.
      }
    }
    return LUCIAD_LIGHTSPEED_WMS_URL;
  }
}
