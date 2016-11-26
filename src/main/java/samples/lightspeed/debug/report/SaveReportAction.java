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
package samples.lightspeed.debug.report;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;

/**
 * An action for saving the report that is displayed in the <code>JTextArea</code> of the report panel
 * of this sample
 */
public class SaveReportAction extends ALcdAction {

  private JTextArea fTextArea;

  /**
   * The default constructor for this save report action
   */
  public SaveReportAction(JTextArea aTextArea) {
    fTextArea = aTextArea;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    setShortDescription("Save log");
    setName("Save");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //Create a new save file dialog
    Frame parentFrame = TLcdAWTUtil.findParentFrame(e);
    FileDialog fileDialog = new FileDialog(parentFrame, "Save log file as: ", FileDialog.SAVE);
    //Set the file name filter to the .txt extension
    fileDialog.setFilenameFilter(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".txt");
      }
    });
    fileDialog.setVisible(true);

    if (fileDialog.getFile() == null) {
      return;
    }
    //Set the extension of the file to .txt, should this not already be the case
    if (!fileDialog.getFile().endsWith(".txt")) {
      fileDialog.setFile(fileDialog.getFile() + ".txt");
    }

    //Create a new file based on the file name and directory retrieved from the file dialog
    File saveFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
    BufferedWriter writer = null;

    //For each String in the text output of the main panel, write this String to a new line in
    // the text file using a buffered writer
    try {
      writer = new BufferedWriter(new FileWriter(saveFile));
      String[] lines = fTextArea.getText().split("\\n");
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException exception) {
      JOptionPane.showMessageDialog(parentFrame, "Error while writing to log file");
    } finally {
      //Close the buffered writer
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (IOException exception) {
        JOptionPane.showMessageDialog(parentFrame, "Error while trying to close writer");
      }
    }
  }
}
