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
package samples.wms.client.opengl;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;

import samples.gxy.common.TitledPanel;
import samples.wms.client.ProxyModelFactory;
import samples.wms.client.UrlPanel;
import samples.wms.client.WMSLayerList;

/**
 * Extension of <code>samples.opengl.geocentric.MainPanel</code> that adds WMS data to the view.
 */
public class MainPanel extends samples.opengl.geocentric.MainPanel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class.getName());

  private JTextField fURL;

  private ILcdModel fWMSModel;
  private WMSLayerList fWMSLayerList;

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new WMS3DLayerFactory();
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    setComponentNorthEast(createSettingsPanel());
    setComponentSouth(createURLPanel());
  }

  private JPanel createURLPanel() {
    // Create a button that queries the specified WMS for its list of available feature types.
    JButton querybutton = new JButton("Query");
    querybutton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadWMSData();
        updateWMSLayerListUI();
      }
    });

    fURL = new JTextField(UrlPanel.getDefaultWMSUrl());

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(new JLabel("Server URL"), BorderLayout.WEST);
    panel.add(fURL, BorderLayout.CENTER);
    panel.add(querybutton, BorderLayout.EAST);

    return TitledPanel.createTitledPanel("WMS service", panel);
  }

  private JPanel createSettingsPanel() {
    fWMSLayerList = new WMSLayerList();
    JScrollPane scrollPane = new JScrollPane(fWMSLayerList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    return TitledPanel.createTitledPanel("Available WMS layers", scrollPane);
  }

  protected void addData() {
    loadWMSData();
    updateWMSLayerListUI();

    // Fit on the US, where most of the default WMS data is situated.
    TLcdGLViewFitAction fitAction = new TLcdGLViewFitAction();
    fitAction.setView(getCanvas());
    TLcdXYZBounds bounds = new TLcdXYZBounds(-130, 20, 0, 70, 40, 0);
    TLcdGeoReference2GeoReference ref2ref = new TLcdGeoReference2GeoReference();
    ref2ref.setDestinationReference((ILcdGeoReference) getCanvas().getXYZWorldReference());
    ref2ref.setSourceReference((ILcdGeoReference) new TLcdGeodeticReference());
    ILcd3DEditableBounds editableBounds = bounds.cloneAs3DEditableBounds();
    try {
      ref2ref.sourceBounds2destinationSFCT(bounds, editableBounds);
      fitAction.setBounds(editableBounds);
      fitAction.fit();
    } catch (TLcdNoBoundsException e) {
      sLogger.error("Could not fit on bounds " + bounds + ": " + e.getMessage());
    }
  }

  public void loadWMSData() {
    if (fWMSModel != null) {
      getCanvas().removeModel(fWMSModel);
      fWMSModel = null;
    }
    try {
      fWMSModel = ProxyModelFactory.createWMSModel((fURL != null) ? fURL.getText() : UrlPanel.getDefaultWMSUrl(), this, true);
    } catch (Exception e) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(null, new String[]{
              "Could not find a WMS server to connect with. Please provide a valid URL to a running server.",
          });
        }
      });
    }

    if (fWMSModel != null) {
      getCanvas().addModel(fWMSModel);

      // We add a model listener to be able to update the WMS layer list UI widget after WMS model changes.
      fWMSModel.addModelListener(new ILcdModelListener() {
        @Override
        public void modelChanged(TLcdModelChangedEvent aEvent) {
          updateWMSLayerListUI();
        }
      });
    }
  }

  private void updateWMSLayerListUI() {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (fWMSModel != null) {
          fWMSLayerList.setWMSModel(fWMSModel);
        }
      }
    });
  }
}
