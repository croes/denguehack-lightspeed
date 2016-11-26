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
package samples.hana.lightspeed.ui;

import java.awt.Component;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import samples.common.TwoColumnLayoutBuilder;

import samples.hana.lightspeed.common.HanaConnectionParameters;

/**
 * Input dialog to ask for URL, user name and password for the HANA database connection.
 */
public class ConnectionDialog {

  public static HanaConnectionParameters showDialog() {
    JTextField url = new JTextField();
    JTextField user = new JTextField();
    JTextField password = new JPasswordField();
    TwoColumnLayoutBuilder builder = TwoColumnLayoutBuilder.newBuilder().
        row().spanBothColumns(new JLabel("<html>This sample requires a HANA database to which sample data can be uploaded.<br/>" +
                                         "Please provide the connection details below, or instead edit <br/>" +
                                         "samples/resources/samples/hana/lightspeed/config.properties.</html>")).build().
                                                               row().columnOne(Box.createVerticalStrut(10)).build().
                                                               row().columnOne(new JLabel("Database URL"), url).columnTwo(new JLabel(" example:  jdbc:sap://host:port/")).build().
                                                               row().columnOne(new JLabel("User name"), user).build().
                                                               row().columnOne(new JLabel("Password"), password).build().
                                                               row().columnOne(Box.createVerticalStrut(20)).build();

    JPanel content = new JPanel();
    builder.populate(content);
    Window[] windows = Window.getWindows();
    Component parent = windows.length > 0 ? windows[0] : null;
    int result = JOptionPane.showConfirmDialog(parent, content, "Connection details", JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION) {
      System.exit(0);
    }
    return new HanaConnectionParameters(url.getText(), user.getText(), password.getText());
  }
}
