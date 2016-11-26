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
package samples.lucy.frontend.dockableframes;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.lucy.util.language.TLcyLang;

public class JIDELicensePanel extends JPanel {
  private static final int LABEL_COLUMN = 2;
  private static final int TEXT_COLUMN = 4;
  private static final String LICENSE_MESSAGE2 =
      "<html>This sample uses the JIDE docking framework, which requires a license to run.<br>" +
      "You can purchase a license from JIDE's site: <a href='http://www.jidesoft.com'>http://www.jidesoft.com</a>." +
      "<p>" +
      "<p>" +
      "Alternatively you can download the evaluation jars from JIDE's site and replace jide-docking.jar <br>" +
      "and jide-common.jar in the lib directory of the LuciadLightspeed distribution." +
      "</html>";

  private final JTextField fCompanyTextField = new JTextField(25);
  private final JTextField fProductTextField = new JTextField(25);
  private final JTextField fLicenseTextField = new JTextField(25);

  public JIDELicensePanel() {
    setLayout(new FormLayout(
        "3dlu, left:default, 3dlu, fill:default:grow, 3dlu",
        "3dlu, center:default, 12dlu, center:default, 3dlu, center:default, 3dlu, center:default"
    ));


    CellConstraints cc = new CellConstraints();
    int y = 2;
    add(new JLabel(LICENSE_MESSAGE2), cc.xywh(2, y, 3, 1));

    y += 2;
    JLabel company_label = new JLabel(TLcyLang.getString("Company"));
    add(company_label, cc.xy(LABEL_COLUMN, y));
    add(fCompanyTextField, cc.xy(TEXT_COLUMN, y));
    y += 2;
    JLabel product_label = new JLabel(TLcyLang.getString("Product"));
    add(product_label, cc.xy(LABEL_COLUMN, y));
    add(fProductTextField, cc.xy(TEXT_COLUMN, y));
    y += 2;
    JLabel license_label = new JLabel(TLcyLang.getString("License"));
    add(license_label, cc.xy(LABEL_COLUMN, y));
    add(fLicenseTextField, cc.xy(TEXT_COLUMN, y));
  }

  public String getCompany() {
    return fCompanyTextField.getText();
  }

  public String getProduct() {
    return fProductTextField.getText();
  }

  public String getLicense() {
    return fLicenseTextField.getText();
  }
}
