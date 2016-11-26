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
package samples.ogc.wcs.proxy;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;

import com.luciad.gui.swing.TLcdMemoryCheckPanel;
import samples.common.LuciadFrame;
import com.luciad.ogc.wcs.client.TLcdWCSClient;
import com.luciad.ogc.wcs.client.TLcdWCSProxy;
import com.luciad.ogc.wcs.client.TLcdWCSProxyModel;
import com.luciad.ogc.wcs.client.TLcdWCSProxyModelFactory;
import com.luciad.ogc.wcs.common.model.TLcdWCSCapabilities;
import com.luciad.ogc.wcs.common.model.TLcdWCSCoverageDescription;
import com.luciad.ogc.wcs.common.model.TLcdWCSCoverageOffering;
import com.luciad.ogc.wcs.common.model.TLcdWCSInterpolationMethod;
import com.luciad.ogc.wcs.common.model.TLcdWCSSupportedInterpolations;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.concurrent.painting.AsynchronousLayerFactory;

/**
 * This sample demonstrates the use of the LuciadLightspeed WCS client API. Using the
 * client API, applications can create a "proxy" ILcdModel that transparently
 * obtains coverage data from the WCS. To use the sample, enter the URL to a WCS
 * capabilities document in the address bar at the top of the window and click on
 * the "Query WCS service" button. A combobox will be populated with a list of
 * available coverages. Selecting one from the list and clicking "Get coverage"
 * will cause a WCS proxy model to be created and added to the map.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel();

  private ILcdGXYLayer fWCSLayer = null;
  private MyLayerFactory fLayerFactory = new MyLayerFactory();
  private EditableBoundsLayer fAreaOfInterestLayer = null;
  private CoverageBoundsLayer fCoverageBoundsLayer = null;

  private Vector<String> fCoverages = new Vector<>();
  private Map<String, Vector<String>> fInterpolationModes = new HashMap<>();
  private TLcdWCSClient fWCSClient;
  private String fSelectedCoverage = "";
  private String fSelectedInterpolationMode = "";

  private JComboBox fCoverageChooser;
  private JComboBox fInterpolationChooser;
  private JTextField fURL;
  private JButton fGetCoverageButton;

  public void createGUI() {
    // initialize asynchronous painting
    new TLcdGXYAsynchronousPaintQueueManager().setGXYView(fMapJPanel);

    // initialize GUI components

    // Create a text field in which the user can type the WCS URL. Default to localhost.
    fURL = new JTextField("http://localhost:8080/LuciadLightspeedOGC/wcs?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCapabilities");

    // Create a button that queries the specified WCS for its list of available coverages.
    JButton queryButton = new JButton("Query WCS service");
    queryButton.addActionListener(new QueryButtonClickListener());

    // Create a combo box with all available coverages. Selecting one will retrieve it from
    // the server and display it on the map.
    fCoverageChooser = new JComboBox(fCoverages);
    fCoverageChooser.addItemListener(new CoverageListChangeListener());

    fInterpolationChooser = new JComboBox();

    fInterpolationChooser.addItemListener(new InterpolationListChangeListener());

    fGetCoverageButton = new JButton("Get coverage");
    fGetCoverageButton.addActionListener(new GetCoverageButtonClickListener());

    add(BorderLayout.SOUTH, new TLcdMemoryCheckPanel());

    JPanel urlpanel = new JPanel(new BorderLayout(5, 0));
    urlpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    urlpanel.add(new JLabel("Server URL"), BorderLayout.WEST);
    urlpanel.add(fURL, BorderLayout.CENTER);
    urlpanel.add(queryButton, BorderLayout.EAST);

    JPanel serverpanel = new JPanel(new GridBagLayout());
    serverpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, GridBagConstraints.REMAINDER, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0
    );

    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.gridwidth = 1;
    serverpanel.add(new JLabel("Coverage"), gbc);
    gbc.gridx = 1;
    serverpanel.add(Box.createHorizontalStrut(5), gbc);
    gbc.gridx = 2;
    gbc.weightx = 1;
    serverpanel.add(fCoverageChooser, gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridx = 0;

    gbc.gridy++;
    serverpanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.gridwidth = 1;
    serverpanel.add(new JLabel("Interpolation Mode"), gbc);
    gbc.gridx = 1;
    serverpanel.add(Box.createHorizontalStrut(5), gbc);
    gbc.gridx = 2;
    gbc.weightx = 1;
    serverpanel.add(fInterpolationChooser, gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridx = 0;

    gbc.gridy++;
    serverpanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
    serverpanel.add(fGetCoverageButton, gbc);

    // Create the default toolbar and layer control.
    ToolBar tool_bar = new ToolBar(fMapJPanel, true, this);
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    JPanel url_panel = TitledPanel.createTitledPanel(
        "WCS service settings", urlpanel
    );

    JPanel coverage_panel = TitledPanel.createTitledPanel(
        "Query coverage", serverpanel
    );

    JPanel east_panel = new JPanel(new BorderLayout());
    east_panel.add(BorderLayout.NORTH, coverage_panel);
    east_panel.add(BorderLayout.CENTER, layer_control);

    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, tool_bar);
    add(BorderLayout.CENTER, map_panel);
    add(BorderLayout.EAST, east_panel);
    add(BorderLayout.SOUTH, url_panel);
  }

  /**
   * Loads the world data.
   */
  public void addData() {
    // Add the world layer
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);
    // add the area of interest layer.
    fAreaOfInterestLayer = new EditableBoundsLayer(-90, 30, 20, 20);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fAreaOfInterestLayer);

    JDialog progress = ProgressUtil.createProgressDialog(this, "Querying WCS service...");
    ProgressUtil.showDialog(progress);
    try {
      buildCoverageOfferingList(fURL.getText());
      ProgressUtil.hideDialog(progress);
    } catch (IOException e) {
      ProgressUtil.hideDialog(progress);
      String[] message = new String[]{
          e.getMessage(),
          "An exception occurred while connecting to the WCS.",
          "Please check if the WCS is up and running.",
          "To start the LuciadLightspeed WCS server sample, ",
          "run the samples.ogc.server.StartOGCServices sample."
      };
      JOptionPane.showMessageDialog(null, message);
    }
  }

  private void buildCoverageOfferingList(String aServletURL) throws IOException {
    fCoverages.clear();
    doLayout();

    try {
      fWCSClient = TLcdWCSClient.createWCSClient(new URI(aServletURL));

      TLcdWCSCapabilities capabilities = fWCSClient.getCachedCapabilities();

      TLcdWCSCoverageDescription describeCoverage = fWCSClient.describeCoverage(fWCSClient.createDescribeCoverageRequest());
      for (int i = 0; i < describeCoverage.getCoverageOfferingCount(); i++) {
        TLcdWCSCoverageOffering offering = describeCoverage.getCoverageOffering(i);
        String coverageName = offering.getName();
        fCoverages.add(coverageName);
        TLcdWCSSupportedInterpolations supportedInterpolations = offering.getSupportedInterpolations();
        Vector<String> interpolations = new Vector<>();
        for (int j = 0; j < supportedInterpolations.getInterpolationMethodCount(); j++) {
          interpolations.add(supportedInterpolations.getInterpolationMethod(j).toString());
        }
        if (interpolations.isEmpty()) {
          interpolations.add(TLcdWCSInterpolationMethod.NEAREST_NEIGHBOR.toString());
        }
        fInterpolationModes.put(coverageName, interpolations);
      }

      if (fCoverageBoundsLayer != null) {
        GXYLayerUtil.removeGXYLayer(fMapJPanel, fCoverageBoundsLayer, false);
      }
      fCoverageBoundsLayer = new CoverageBoundsLayer(capabilities);
      GXYLayerUtil.addGXYLayer(fMapJPanel, fCoverageBoundsLayer, true, false);

      fCoverageChooser.setEnabled(true);
      fInterpolationChooser.setEnabled(true);
      fGetCoverageButton.setEnabled(true);

      if (fCoverages.size() > 0) {
        fSelectedCoverage = fCoverages.get(0);
        fCoverageChooser.setSelectedItem(fCoverages.get(0));
        fSelectedInterpolationMode = fInterpolationModes.get(fSelectedCoverage).get(0);
        fInterpolationChooser.setSelectedItem(fSelectedInterpolationMode);
      }

      // Redo the layout so the coverage list combo box gets updated.
      fCoverageChooser.setModel(new DefaultComboBoxModel(fCoverages));
      fCoverageChooser.setSelectedIndex(0);

      fInterpolationChooser.setModel(new DefaultComboBoxModel(fInterpolationModes.get(fSelectedCoverage)));
      fInterpolationChooser.setSelectedIndex(0);
      doLayout();
    } catch (Exception e) {
      fCoverageChooser.setEnabled(false);
      fGetCoverageButton.setEnabled(false);

      System.err.println(e.getMessage());
      throw new IOException(aServletURL + " does not appear to point to a valid WCS server");
    }
  }

  private void createWCSProxyLayer() {
    if (fMapJPanel.containsLayer(fWCSLayer)) {
      GXYLayerUtil.removeGXYLayer(fMapJPanel, fWCSLayer, true);
    }

    // Create a proxy factory
    TLcdWCSProxyModelFactory factory = new TLcdWCSProxyModelFactory();
    // Create a proxy model for the selected service and coverage
    TLcdWCSProxyModel model = null;
    try {
      model = factory.createProxyModel(fWCSClient, fSelectedCoverage);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    // Obtain the proxy object from the model, which is an ILcdMultilevelRaster
    TLcdWCSProxy proxy = model.getWCSProxy();
    proxy.setInterpolationMethod(TLcdWCSInterpolationMethod.valueOf(fSelectedInterpolationMode));
    try {
      // Configure the proxy
      proxy.setRasterCount(3);
      proxy.setTileColumnCount(8);
      proxy.setTileRowCount(8);
      proxy.setTilePixelWidth(300);
      proxy.setTilePixelHeight(300);

      ILcdBounds wgs84bounds = fAreaOfInterestLayer.getLonLatBounds();
      TLcdGeoReference2GeoReference transform = new TLcdGeoReference2GeoReference(
          (ILcdGeoReference) fAreaOfInterestLayer.getModel().getModelReference(),
          (ILcdGeoReference) model.getModelReference()
      );
      ILcd3DEditableBounds proxy_bounds =
          model.getModelReference().makeModelPoint().getBounds().cloneAs3DEditableBounds();
      transform.sourceBounds2destinationSFCT(wgs84bounds, proxy_bounds);
      proxy.setBounds(proxy_bounds);

      // Add the proxy model to the map
      fLayerFactory.setLayerLabel(fSelectedCoverage);
      fWCSLayer = AsynchronousLayerFactory.createAsynchronousLayer(fLayerFactory.createGXYLayer(model));
      GXYLayerUtil.addGXYLayer(fMapJPanel, fWCSLayer, true, false);
    } catch (Exception e) {
      fWCSLayer = null;
      JOptionPane.showMessageDialog(fMapJPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

  }

  /**
   * Listener for the "Query WCS service" button.
   */
  private class QueryButtonClickListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JDialog progress = ProgressUtil.createProgressDialog(MainPanel.this, "Querying WCS service...");
      ProgressUtil.showDialog(progress);
      try {
        buildCoverageOfferingList(fURL.getText());
        ProgressUtil.hideDialog(progress);
      } catch (IOException ex) {
        ProgressUtil.hideDialog(progress);
        JOptionPane.showMessageDialog(null, ex.getMessage());
      }
    }
  }

  /**
   * Listener for the "Queryable coverages" combo box.
   */
  private class CoverageListChangeListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        fSelectedCoverage = (String) e.getItem();
        fSelectedInterpolationMode = fInterpolationModes.get(fSelectedCoverage).get(0);
        fInterpolationChooser.setModel(new DefaultComboBoxModel(fInterpolationModes.get(fSelectedCoverage)));
        fInterpolationChooser.setSelectedIndex(0);
      }
    }
  }

  private class InterpolationListChangeListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        fSelectedInterpolationMode = (String) e.getItem();
      }
    }
  }

  /**
   * Listener for the "Get coverages" button.
   */
  private class GetCoverageButtonClickListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      createWCSProxyLayer();
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "WCS proxy");
      }
    });
  }
}
