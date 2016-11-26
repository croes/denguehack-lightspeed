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
package samples.fusion.client.gxy;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.luciad.fusion.client.ALfnClientEnvironment;
import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.core.TLfnTileCoordinates;
import com.luciad.fusion.tilestore.ALfnCoverage;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ELfnResourceType;
import com.luciad.fusion.tilestore.TLfnDigestTileStore;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnTileStoreDataSource;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDecoder;
import com.luciad.fusion.tilestore.model.TLfnVectorTileStoreModelDescriptor;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.fusion.client.common.CoverageListPanel;
import samples.fusion.client.common.QueryPanel;
import samples.fusion.client.common.ResourceHandler;
import samples.fusion.client.common.Util;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.projections.GXYCenterMapController;

/**
 * Sample demonstrating how to create and use a proxy model to connect to a LuciadFusion server, to retrieve tiles from
 * the server and to display the tile data in a 2D map.
 * <p/>
 * To use this sample a LuciadFusion server must be running. You can for example use the
 * <code>samples.fusion.server</code> sample to start a local LuciadFusion server.
 */
public class MainPanel extends SamplePanel {

  protected final ALfnEnvironment fEnvironment;

  protected final ALfnClientEnvironment fClientEnvironment;

  protected final TLfnClientFactory fClientFactory;

  protected CoverageListPanel fCoverageListPanel;

  protected QueryPanel fQueryServerPanel;

  protected TLcdMapJPanel fMapJPanel;

  private VectorFusionLayerFactory fVectorLayerFactory;

  // Using a shared paint queue for all coverages reduces memory usage.
  // This is especially useful when a large number of coverages is being loaded.
  private TLcdGXYAsynchronousPaintQueue fSharedPaintQueue;

  private RasterFusionLayerFactory fRasterFusionLayerFactory = new RasterFusionLayerFactory();

  public MainPanel() {
    super();
    fEnvironment = ALfnEnvironment.newInstance();
    fClientEnvironment = ALfnClientEnvironment.newInstance(fEnvironment);
    fClientFactory = new TLfnClientFactory(fClientEnvironment);
  }

  @Override
  protected void createGUI() {
    fMapJPanel = SampleMapJPanelFactory.createMapJPanel();
    fMapJPanel.setGXYViewLabelPlacer(new TLcdGXYAsynchronousLabelPlacer(new TLcdGXYLocationListLabelingAlgorithm()));

    // Create the default tool bar
    ToolBar toolBar = new ToolBar(fMapJPanel, true, this);
    toolBar.getAdvancedRulerController().setDistanceFormat(new TLcdDistanceFormat(TLcdDistanceUnit.METRE_UNIT));
    toolBar.addGXYController(new GXYCenterMapController());

    // Create a map panel

    TitledPanel mapPanel = TitledPanel.createTitledPanel("Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST);

    JPanel eastPanel = new JPanel(new GridBagLayout());

    // Create a panel to query themes from the server
    fCoverageListPanel = new CoverageListPanel(new GetCoverageListener());
    fCoverageListPanel.setSelectionMode(CoverageListPanel.SelectionMode.MULTIPLE_COVERAGES);
    JPanel coveragePanel = TitledPanel.createTitledPanel("Coverages", fCoverageListPanel);
    eastPanel.add(coveragePanel,
                  new GridBagConstraints(0, 0, 1, 1, 0, .2, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                                         new Insets(0, 0, 0, 0), 0, 0));

    // Create a layer control
    LayerControlPanel layerControl = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);
    eastPanel.add(layerControl,
                  new GridBagConstraints(0, 2, 1, 1, 0, .8, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                                         new Insets(0, 0, 0, 0), 0, 0));

    // Create a panel to query capabilities from the server
    fQueryServerPanel = new QueryPanel(fEnvironment, fClientFactory, this, fCoverageListPanel,
                                       ELfnResourceType.COVERAGE);
    JPanel urlPanel = TitledPanel.createTitledPanel("LuciadFusion service settings", fQueryServerPanel);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(toolBar, BorderLayout.NORTH);
    topPanel.add(urlPanel, BorderLayout.SOUTH);

    // Add the components
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    add(mapPanel, BorderLayout.CENTER);
    add(eastPanel, BorderLayout.EAST);

    // Create a shared paint queue
    fSharedPaintQueue = new TLcdGXYAsynchronousPaintQueue(fMapJPanel, TLcdGXYAsynchronousPaintQueue.BODIES_AND_SKIP);
    fSharedPaintQueue.setPriority(Thread.MIN_PRIORITY);
  }

  @Override
  protected void addData() {
    // Add world shapes as background data
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);
  }

  protected void addFusionModelToView(ResourceHandler.ResourceInfo aResourceInfo) {
    String coverageId = aResourceInfo.getId();
    // Create a model for the selected client and coverage
    try {
      ALfnTileStore tileStore = fQueryServerPanel.getTileStore();
      ILcdModel model = createModel(fEnvironment, tileStore, coverageId);
      ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
      ILcdGXYLayer layer;
      if (modelDescriptor instanceof TLfnVectorTileStoreModelDescriptor) {
        VectorFusionLayerFactory layerFactory = getVectorLayerFactory();
        layer = layerFactory.createGXYLayer(model);
      } else {
        layer = fRasterFusionLayerFactory.createGXYLayer(model);
      }
      Util.addLayerForModel(fMapJPanel, layer, true, fSharedPaintQueue);
    } catch (Exception e) {
      TLcdLoggerFactory.getLogger("com.luciad").warn("Failed to retrieve coverage: " + aResourceInfo, e);
      JOptionPane.showMessageDialog(TLcdAWTUtil.findParentFrame(MainPanel.this),
                                    "Failed to retrieve coverage: " + aResourceInfo + ", reason: " + e.getMessage(),
                                    "Coverage retrieval result", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected VectorFusionLayerFactory createVectorLayerFactory() {
    return new VectorFusionLayerFactory();
  }

  public VectorFusionLayerFactory getVectorLayerFactory() {
    if (fVectorLayerFactory == null) {
      fVectorLayerFactory = createVectorLayerFactory();
    }
    return fVectorLayerFactory;
  }

  protected ALfnTileStoreModel createModel(ALfnEnvironment aEnvironment,
                                           ALfnTileStore aTileStore,
                                           String aCoverageId) throws IOException {
    TLfnTileStoreModelDecoder modelDecoder = new TLfnTileStoreModelDecoder(aEnvironment);
    TLfnDigestTileStore digestTileStore =
        new TLfnDigestTileStore(aTileStore,
                                true,
                                new InvalidTileMessageDigestHandler());
    return modelDecoder.decode(digestTileStore, aCoverageId);
  }

  private class GetCoverageListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      for (ResourceHandler.ResourceInfo resourceInfo : fCoverageListPanel.getSelectedCoverages()) {
        addFusionModelToView(resourceInfo);
      }
    }
  }

  private static class InvalidTileMessageDigestHandler extends TLfnDigestTileStore.InvalidMessageDigestHandler {

    @Override
    public void invalidMessageDigest(ALfnCoverage aCoverage, TLfnTileCoordinates aTileCoordinates) {
      System.out.println(
          "Tile signature mismatches for tile: " + aTileCoordinates + " in coverage: " + aCoverage.getMetadata()
                                                                                                  .getName());
    }
  }

  public static void main(String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new LuciadFrame(new MainPanel(), "LuciadFusion Client", 800, 600);
      }
    });
  }

  /**
   * Stand-alone code snippet illustrating how you can discover data sources from a URI, select one, and decode that to
   * a model.
   * Use this if you don't know the coverage ID beforehand.
   */
  public static void discoverDataSourcesAndDecode() throws IOException {
    TLfnTileStoreModelDecoder modelDecoder = new TLfnTileStoreModelDecoder();
    List<TLfnTileStoreDataSource> dataSources = modelDecoder.discoverDataSources("http://localhost:8080/LuciadFusion/lts");
    TLfnTileStoreDataSource selectedDataSource = dataSources.get(0);
    ALfnTileStoreModel model = modelDecoder.decodeSource(selectedDataSource);
  }

  /**
   * Stand-alone code snippet illustrating how you can decode a model directly from a coverage ID.
   * Use this if you know the coverage ID beforehand.
   */
  public static void decodeByCoverageID() throws IOException {
    TLfnTileStoreModelDecoder modelDecoder = new TLfnTileStoreModelDecoder();
    ALfnTileStore tileStore = modelDecoder.decodeTileStore("http://localhost:8080/LuciadFusion/lts");
    String knownCoverageID = "earth_image";
    ALfnTileStoreModel model = modelDecoder.decode(tileStore, knownCoverageID);
  }
}
