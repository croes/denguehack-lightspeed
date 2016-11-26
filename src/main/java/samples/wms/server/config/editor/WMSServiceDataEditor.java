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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.luciad.ogc.ows.model.TLcdOWSOnlineResource;
import com.luciad.wms.server.model.TLcdWMSServiceMetaData;
import com.luciad.wms.server.model.TLcdWMSURL;

import samples.wms.server.config.editor.util.LabeledTextField;

/**
 * An editor panel for the WMS service metadata.
 */
class WMSServiceDataEditor extends WMSEditorPanel {

  private TLcdWMSServiceMetaData fMetaData;

  private static final int SERVICE_TITLE = 1;
  private static final int SERVICE_ABSTRACT = 2;
  private static final int SERVICE_URL = 3;

  /**
   * A change listener for any of the data fields.
   */
  private class ChangeMetaDataListener implements DocumentListener {

    private int fTarget;
    private LabeledTextField fTextField;

    public ChangeMetaDataListener(LabeledTextField aField, int aTarget) {
      fTextField = aField;
      fTarget = aTarget;
    }

    public void changedUpdate(DocumentEvent e) {

      String value = fTextField.getText();

      switch (fTarget) {
      case SERVICE_TITLE:
        fMetaData.setServiceTitle(value);
        break;
      case SERVICE_ABSTRACT:
        fMetaData.setServiceAbstract(value);
        break;
      case SERVICE_URL:
        fMetaData.setOnlineResource(new TLcdWMSURL(new TLcdOWSOnlineResource(value), null, null, null));
        break;
      }

      fireEditListeners(fMetaData);
    }

    public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
    }

    public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
    }
  }

  public WMSServiceDataEditor(TLcdWMSServiceMetaData aMetaData) {
    super(new BorderLayout(2, 2));
    fMetaData = aMetaData;

    JPanel editorPanel = new JPanel(new GridLayout(4, 1, 2, 2));
    editorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    LabeledTextField field;

    // Service title.
    field = new LabeledTextField("Title", fMetaData.getServiceTitle());
    field.setLabelWidth(100);
    field.setToolTipText("End-user visible service name");
    field.addDocumentListener(new ChangeMetaDataListener(field, SERVICE_TITLE));
    editorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "servicedata.title");

    // Service abstract.
    field = new LabeledTextField("Abstract", fMetaData.getServiceAbstract());
    field.setLabelWidth(100);
    field.setToolTipText("Service description");
    field.addDocumentListener(new ChangeMetaDataListener(field, SERVICE_ABSTRACT));
    editorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "servicedata.abstract");

    // Service URL.
    field = new LabeledTextField("Resource URL", fMetaData.getOnlineResourceURL().toString());
    field.setLabelWidth(100);
    field.setToolTipText("Server URL");
    field.addDocumentListener(new ChangeMetaDataListener(field, SERVICE_URL));
    editorPanel.add(field);
    WMSEditorHelp.registerComponent(field, "servicedata.url");

    add(BorderLayout.NORTH, editorPanel);
//    add( BorderLayout.NORTH, TitledPanel.createTitledPanel( "WMS service metadata", editor_panel ) );
  }
}
