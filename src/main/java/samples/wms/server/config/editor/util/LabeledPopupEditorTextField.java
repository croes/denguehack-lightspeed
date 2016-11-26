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
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.luciad.util.ILcdFilter;

/**
 * Extends LabeledTextField to make the text field itself uneditable.
 * Similar to the filename chooser, a button is appended at the end of the text
 * field that pops up a dialog in which the user can enter a new value.
 */
public class LabeledPopupEditorTextField extends LabeledTextField {

  private java.util.List fFilters = new ArrayList();

  /**
   * A listener for the file browse button. Pops up a file chooser, and puts
   * the path to the selected file in the text field.
   */

  public LabeledPopupEditorTextField(String aLabel, String aDefaultValue) {
    super(aLabel, aDefaultValue);

    getTextField().setEditable(false);
    JButton editButton = new JButton("...");
    editButton.addActionListener(new EditButtonListener(getTextField()));
    editButton.setToolTipText("Click to change value");
    add(BorderLayout.EAST, editButton);
  }

  public void addValidator(ILcdFilter aValidator) {
    fFilters.add(aValidator);
  }

  public void removeValidator(ILcdFilter aValidator) {
    fFilters.remove(aValidator);
  }

  private class EditButtonListener implements ActionListener {

    private JTextField fTextField;
    private boolean fError = false;

    public EditButtonListener(JTextField aTextField) {
      fTextField = aTextField;
    }

    public void actionPerformed(ActionEvent e) {

      String message = "Please enter a new value:";
      if (fError) {
        message = "The value you entered is not valid. " + message;
      }

      String newtext = null;

      newtext = (String) JOptionPane.showInputDialog(fTextField,
                                                     message,
                                                     "Input",
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     null,
                                                     fTextField.getText());

      if (newtext == null) {  // cancel button pressed
        return;
      }

      for (int i = 0; i < fFilters.size(); i++) {
        ILcdFilter f = (ILcdFilter) fFilters.get(i);
        if (!f.accept(newtext)) {
          fError = true;
          actionPerformed(e);
          return;
        }
      }

      fError = false;
      fTextField.setText(newtext);
    }
  }
}
