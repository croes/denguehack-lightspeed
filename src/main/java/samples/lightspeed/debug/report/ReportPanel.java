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

import java.awt.BorderLayout;
import java.awt.Dimension;

import com.jogamp.opengl.GLException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.view.lightspeed.util.TLspPlatformInfo;

/**
 * Report panel class which contains a text area for visualising the capabilities of the hardware
 * running the sample
 */
public class ReportPanel extends JPanel {

  private JTextArea fTextArea;

  /**
   * Creates the main panel
   */
  public ReportPanel() {

    setLayout(new BorderLayout());

    //Create a new text area in which to print the capabilities report
    fTextArea = new JTextArea(20, 70);
    fTextArea.setEditable(false);

    //Create the toolbar
    JToolBar toolBar = new JToolBar();
    JButton saveButton = new JButton(new TLcdSWAction(new SaveReportAction(fTextArea)));
    saveButton.setToolTipText("Save log");
    saveButton.setText(null);
    toolBar.add(saveButton);
    add(toolBar, BorderLayout.NORTH);

    //Add a scroll pane for scrolling through the text
    JScrollPane scrollPane = new JScrollPane(fTextArea) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = fTextArea.getPreferredSize();
        size.height = Math.min(480, size.height);
        return size;
      }
    };

    setOpaque(true);

    add(scrollPane, BorderLayout.CENTER);

    //Create and print the capabilities report
    createCapabilitiesReport();
  }

  /**
   * Retrieves a capabilities report for the current hardware, stores it in the text output list and
   * prints it to the text area
   */
  private void createCapabilitiesReport() {

    try {
      fTextArea.append(TLspPlatformInfo.getReport());
      fTextArea.setCaretPosition(0);
    } catch (GLException e) {
      fTextArea.append("Unable to generate hardware capability report: GPU might not support PBuffer.\n" +
                       "Please check your drivers");
      fTextArea.setCaretPosition(0);
    }
  }
}
