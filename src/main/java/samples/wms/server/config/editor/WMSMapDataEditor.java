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
package samples.wms.server.config.editor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.wms.server.model.TLcdWMSCapabilities;

import samples.wms.server.config.editor.util.LabeledComponent;
import samples.wms.server.config.editor.util.LabeledFilenameField;

/**
 * Editor panel for global map data (root folder, output formats).
 */
class WMSMapDataEditor extends WMSEditorPanel {

  private TLcdWMSCapabilities fCapabilities;

  private static final String OUTPUT_FORMATS[] = {
      "image/gif",
      "image/jpeg",
      "image/png",
      "image/svg+xml",
      "application/x-shockwave-flash"
  };
  private static final int NUM_OUTPUT_FORMATS = 5;

  /**
   * Checkbox action listener for enabling and disabling output formats.
   */
  private class OutputFormatListener implements ActionListener {

    private String fFormat;
    private JCheckBox fCheck;

    public OutputFormatListener(JCheckBox aCheck, String aFormat) {
      fFormat = aFormat;
      fCheck = aCheck;
    }

    public void actionPerformed(ActionEvent e) {

      if (fCheck.isSelected()) {
        fCapabilities.addMapFormat(fFormat);
      } else {
        fCapabilities.removeMapFormat(fFormat);
      }
      fireEditListeners(fCapabilities);
    }
  }

  /**
   * Change listener for the root map data folder.
   */
  private class PathChangeListener implements DocumentListener {

    private LabeledFilenameField fField;

    public PathChangeListener(LabeledFilenameField aTextField) {
      fField = aTextField;
    }

    public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
      fCapabilities.setMapDataFolder(fField.getText());
      fireEditListeners(fCapabilities);
    }
  }

  public WMSMapDataEditor(TLcdWMSCapabilities aCapabilities) {
    super(new BorderLayout(2, 2));
    fCapabilities = aCapabilities;

    JPanel editorPanel = new JPanel(new BorderLayout(4, 4));
    editorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Add the data root folder editor.
    LabeledFilenameField field = new LabeledFilenameField("Data folder", fCapabilities.getMapDataFolder());
    field.setLabelWidth(150);
    field.setInitialDir(fCapabilities.getMapDataFolder());
    field.setToolTipText("The root directory from which map data is loaded");
    field.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    field.addDocumentListener(new PathChangeListener(field));
    editorPanel.add(BorderLayout.NORTH, field);
    WMSEditorHelp.registerComponent(field, "mapdata.mapdatafolder");

    // Add the output format list.
    JPanel formatPanel = new JPanel(new GridLayout(NUM_OUTPUT_FORMATS, 1, 2, 2));
    formatPanel.setToolTipText("Check the output formats the server should publish");
    WMSEditorHelp.registerComponent(formatPanel, "mapdata.outputformats");

    for (int i = 0; i < NUM_OUTPUT_FORMATS; i++) {
      boolean allowed = false;

      for (int j = 0; j < fCapabilities.getMapFormatCount(); j++) {
        if (fCapabilities.getMapFormat(j).equals(OUTPUT_FORMATS[i])) {
          allowed = true;
          break;
        }
      }

      JCheckBox chk = new JCheckBox(OUTPUT_FORMATS[i], allowed);
      chk.addActionListener(new OutputFormatListener(chk, OUTPUT_FORMATS[i]));
      chk.setToolTipText("Check the output formats the server should publish");
      formatPanel.add(chk);
    }

    LabeledComponent format = new LabeledComponent("Allowed output formats", formatPanel);
    format.setLabelWidth(150);
    editorPanel.add(BorderLayout.CENTER, format);

    add(BorderLayout.NORTH, editorPanel);
  }
}
