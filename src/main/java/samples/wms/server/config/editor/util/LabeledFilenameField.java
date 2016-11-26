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
package samples.wms.server.config.editor.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * Extends LabeledTextField to add a file browser, which automatically puts
 * the name of the selected file into the text field. The text field itself is
 * made read-only.
 */
public class LabeledFilenameField extends LabeledTextField {

  private String fInitialDir = "./";
  private int fSelectionMode = JFileChooser.FILES_ONLY;

  public LabeledFilenameField(String aLabel, String aDefaultValue) {
    super(aLabel, aDefaultValue);

    getTextField().setEditable(false);
    JButton browseButton = new JButton("...");
    browseButton.addActionListener(new BrowseButtonListener(getTextField()));
    browseButton.setToolTipText("Click to browse for files");
    add(BorderLayout.EAST, browseButton);
  }

  public void setInitialDir(String aPath) {
    fInitialDir = aPath;
  }

  public void setFileSelectionMode(int aMode) {
    fSelectionMode = aMode;
  }

  /**
   * A listener for the file browse button. Pops up a file chooser, and puts
   * the path to the selected file in the text field.
   */
  private class BrowseButtonListener implements ActionListener {

    private JTextField fTextField;

    public BrowseButtonListener(JTextField aTextField) {
      fTextField = aTextField;
    }

    public void actionPerformed(ActionEvent e) {

      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setCurrentDirectory(new File(fInitialDir));
      fileChooser.setFileSelectionMode(fSelectionMode);
      int returnValue = fileChooser.showOpenDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
        fTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    }
  }
}
