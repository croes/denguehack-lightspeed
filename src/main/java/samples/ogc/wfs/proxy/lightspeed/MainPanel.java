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
package samples.ogc.wfs.proxy.lightspeed;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.ogc.wfs.client.TLcdWFSProxyModel;
import com.luciad.ogc.wfs.common.model.TLcdWFSFeatureType;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdExceptionHandler;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.gxy.common.ProgressUtil;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.decoder.UnstyledLayerFactory;
import samples.ogc.wfs.proxy.WFSModelFactory;

/**
 * This sample demonstrates the use of the LuciadLightspeed WFS client API in a Lightspeed view.
 *
 * Using the client API, applications can create a "proxy" ILcdModel that transparently
 * obtains feature data from the WFS.
 *
 * @see com.luciad.ogc.wfs.client.TLcdWFSClient
 * @see TLcdWFSProxyModel.Builder
 */
public class MainPanel extends LightspeedSample {

  private ILspLayer fWFSLayer = null;
  private WFSModelFactory fWFSModelFactory = new WFSModelFactory();

  // determines the WFS URL
  private JTextField fURL;
  // selects the feature type to display
  private JComboBox fFeatureTypeComboBox;

  protected void addData() throws IOException {
    super.addData();
    final ILcdBounds bounds = new TLcdLonLatBounds(-125, 25, 60.00, 30.00);
    FitUtil.fitOnBounds(this, bounds, new TLcdGeodeticReference());
    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        querySelectedWFS(true);
      }
    });
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    List<Component> toolBars = new ArrayList<>();
    toolBars.addAll(Arrays.asList(super.createToolBars(aView)));
    toolBars.add(createWFSURLPanel());
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    addComponentToRightPanel(createFeatureTypeListPanel());
  }

  private JPanel createWFSURLPanel() {
    fURL = new JTextField("http://localhost:8080/LuciadLightspeedOGC/wfs");

    // Create a button that queries the specified WFS for its list of available feature types.
    JButton querybutton = new JButton("Query");
    querybutton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        querySelectedWFS(false);
      }
    });

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(new JLabel("Server URL"), BorderLayout.WEST);
    panel.add(fURL, BorderLayout.CENTER);
    panel.add(querybutton, BorderLayout.EAST);

    return TitledPanel.createTitledPanel("WFS service", panel);
  }

  private JPanel createFeatureTypeListPanel() {
    // Create a combo box with all available feature types. Selecting one will retrieve it from
    // the server and display it on the map.
    fFeatureTypeComboBox = new JComboBox();
    fFeatureTypeComboBox.addItemListener(new FeatureTypeListener());

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(fFeatureTypeComboBox, BorderLayout.CENTER);
    return TitledPanel.createTitledPanel("Available features", panel);
  }

  private void querySelectedWFS(boolean aFirstTime) {
    try {
      JDialog progress = null;
      try {
        progress = ProgressUtil.createProgressDialog(this, "Querying WFS service...");
        ProgressUtil.showDialog(progress);
        Vector<TLcdWFSFeatureType> featureTypes = fWFSModelFactory.buildFeatureTypeList(fURL.getText());
        // Update the feature list combo box.
        fFeatureTypeComboBox.setModel(new DefaultComboBoxModel(featureTypes));
        fFeatureTypeComboBox.setSelectedIndex(-1);
        doLayout();
        if (featureTypes.size() > 0) {
          fFeatureTypeComboBox.setSelectedItem(featureTypes.get(0));
        }
      } finally {
        ProgressUtil.hideDialog(progress);
      }
    } catch (IOException e) {
      if (aFirstTime) {
        String[] message = new String[]{
            e.getMessage(),
            "An exception occurred while connecting to the WFS.",
            "Please check if the WFS is up and running.",
            "To start the LuciadLightspeed WFS server sample, ",
            "run the samples.ogc.server.StartOGCServices sample."
        };
        JOptionPane.showMessageDialog(this, message);
      } else {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
  }

  private void createAndAddWFSLayer(final TLcdWFSFeatureType aFeatureType) {

    try {
      removeWFSLayer();

      TLcdWFSProxyModel model = fWFSModelFactory.createModel(aFeatureType.getName());
      model.setExceptionHandler(new ILcdExceptionHandler() {

        public void handleException(final Exception arg0) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              removeWFSLayer();
              System.out.println("Error while trying to read features: " + arg0.getMessage());
              JOptionPane.showMessageDialog(MainPanel.this, "Error while trying to read features: " + arg0.getMessage());
            }
          });

        }
      });

      ILspLayerFactory layerFactory = new UnstyledLayerFactory();
      addWFSLayer(layerFactory.createLayers(model).iterator().next());
    } catch (Exception ex) {
      System.out.println("Could not retrieve " + aFeatureType.getName() + ": " + ex.getMessage());
    }
  }

  private void addWFSLayer(final ILspLayer aLayer) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fWFSLayer = aLayer;
        getView().addLayer(aLayer);
        try {
          new TLspViewNavigationUtil(getView()).animatedFit(Collections.singleton(aLayer));
        } catch (TLcdNoBoundsException | TLcdOutOfBoundsException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void removeWFSLayer() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (fWFSLayer != null) {
          getView().removeLayer(fWFSLayer);
          fWFSLayer = null;
        }
      }
    });
  }

  /**
   * Creates a WFS layer for the selected feature type.
   */
  private class FeatureTypeListener implements ItemListener {
    public void itemStateChanged(final ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        // Load the WFS layer in a background thread.
        Thread thread = new Thread() {
          @Override
          public void run() {
            createAndAddWFSLayer((TLcdWFSFeatureType) e.getItem());
          }
        };
        thread.start();
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "WFS proxy");
  }
}
