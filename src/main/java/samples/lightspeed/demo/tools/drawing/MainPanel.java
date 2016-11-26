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
package samples.lightspeed.demo.tools.drawing;

import static com.luciad.fusion.tilestore.ELfnDataType.MULTIVALUED;
import static com.luciad.gui.TLcdIconFactory.SAVE_ICON;
import static com.luciad.gui.TLcdIconFactory.create;
import static com.luciad.model.ILcdModel.NO_EVENT;
import static com.luciad.view.lightspeed.layer.ILspLayer.LayerType.BACKGROUND;
import static com.luciad.view.lightspeed.layer.TLspPaintRepresentationState.REGULAR_BODY;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.luciad.earth.model.TLcdEarthModelDescriptor;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.util.TLcdEarthCompositeTileSet;
import com.luciad.format.mif.TLcdMIFModelEncoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.fusion.client.ALfnClientEnvironment;
import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ELfnDataType;
import com.luciad.fusion.tilestore.ELfnResourceType;
import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnRasterTileStoreModelDescriptor;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDecoder;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.fusion.client.common.CoverageListPanel;
import samples.fusion.client.common.QueryPanel;
import samples.fusion.client.common.ResourceHandler;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * Main GUI panel of the proxy sample.
 */
public class MainPanel extends LightspeedSample {

  public static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger("com.luciad");

  private ALfnEnvironment fEnvironment;

  private TLfnClientFactory fClientFactory;

  private CoverageListPanel fCoveragePanel;

  private QueryPanel fQueryServerPanel;

  private CreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    final ToolBar toolBar = new ToolBar(aView, this, true, true);
    if (fCreateAndEditToolBar == null) {
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this, toolBar.getButtonGroup()
      ) {
        @Override
        protected ILspController createDefaultController() {
          return toolBar.getDefaultController();
        }
      };
//
//      try {
//        TLcdMIFModelDecoder d = new TLcdMIFModelDecoder();
//        ILcdModel m = d.decode( "Data/internal.data/accuracy/accuracy.mif" );
//        TLspLayer creationLayer = fCreateAndEditToolBar.getCreationLayer();
//        creationLayer.setModel( m );
//        creationLayer.setStyler( TLspPaintRepresentationState.REGULAR_BODY, new AccuracyTheme.AccuracyThemeStyler() );
//        creationLayer.setStyler( TLspPaintRepresentationState.SELECTED_BODY, new AccuracyTheme.AccuracyThemeStyler() );
//        creationLayer.setStyler( TLspPaintRepresentationState.EDITED_BODY, new AccuracyTheme.AccuracyThemeStyler() );
//      } catch ( IOException e ) {
//        e.printStackTrace();
//      }

      fCreateAndEditToolBar.addAction(new ALcdAction("Save drawing as", create(SAVE_ICON)) {
        @Override
        public void actionPerformed(ActionEvent e) {
          JFileChooser fc = new JFileChooser("Data/internal.data/");
          if (fc.showSaveDialog(MainPanel.this) == JFileChooser.APPROVE_OPTION) {
            ILcdModel m = fCreateAndEditToolBar.getCreationLayer().getModel();
            TLcdMIFModelEncoder encoder = new TLcdMIFModelEncoder();
            try {
              encoder.export(m, fc.getSelectedFile().getAbsolutePath());
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        }
      });
    }
    return new ToolBar[]{toolBar, fCreateAndEditToolBar};
  }

  private JPanel getToolBarsPanel(ILspView aView, Object aParent) {
    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
    ToolBar[] toolBars = getToolBars();
    for (JToolBar toolBar : toolBars) {
      toolbarPanel.add(toolBar);
    }
    return toolbarPanel;

  }

  @Override
  protected ILspAWTView createView() {
    fEnvironment = ALfnEnvironment.newInstance();
    ALfnClientEnvironment clientEnvironment = ALfnClientEnvironment.newInstance(fEnvironment);
    fClientFactory = new TLfnClientFactory(clientEnvironment);
    return createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // create a panel to query tilesets from the server
    fCoveragePanel = new CoverageListPanel(new CreateLayerListener());
    fCoveragePanel.setSelectionMode(CoverageListPanel.SelectionMode.MULTIPLE_COVERAGES);
    JPanel coveragePanel = TitledPanel.createTitledPanel("Coverages", fCoveragePanel);

    JPanel mainCoveragePanel = new JPanel();
    mainCoveragePanel.setLayout(new GridBagLayout());
    mainCoveragePanel.add(coveragePanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH,
                                                                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    mainCoveragePanel.add(Box.createVerticalGlue(), new GridBagConstraints(0, 2, 1, 1, 0, 1, GridBagConstraints.SOUTH,
                                                                           GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    JScrollPane scrollPane = new JScrollPane(mainCoveragePanel) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(200, 250);
      }
    };
    addComponentToRightPanel(scrollPane);

    // create a panel to perform a query to an Earth server
    fQueryServerPanel = new QueryPanel(fEnvironment, fClientFactory, this, fCoveragePanel, ELfnResourceType.COVERAGE);
    JPanel urlPanel = TitledPanel.createTitledPanel("LuciadFusion service settings", fQueryServerPanel);

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(getToolBarsPanel(getView(), this), BorderLayout.NORTH);
    northPanel.add(urlPanel, BorderLayout.SOUTH);

    add(northPanel, BorderLayout.NORTH);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    getView().addLayer(fCreateAndEditToolBar.getCreationLayer());
  }

  protected void createFusionLayer() {
    createFusionLayer(fCoveragePanel.getSelectedCoverages());
  }

  private void createFusionLayer(ResourceHandler.ResourceInfo... resourceInfo) {
    for (int i = 0; i < resourceInfo.length; i++) {
      // Create a model for the selected client and coverage
      try {
        ILcdModel model = createModel(fEnvironment, fQueryServerPanel.getTileStore(), resourceInfo[i].getId());
        ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
        if (modelDescriptor instanceof TLfnRasterTileStoreModelDescriptor) {
          ILspLayer layer = createFusionRasterLayer(model);
          if (layer != null) {
            getView().addLayer(layer);
          }
        }
      } catch (Exception e) {
        TLcdLoggerFactory.getLogger("com.luciad").warn("Failed to retrieve coverage: " + resourceInfo, e);
        JOptionPane.showMessageDialog(TLcdAWTUtil.findParentFrame(this), "Failed to retrieve coverage: " + resourceInfo
                                                                         + ", reason: " + e.getMessage(), "Coverage retrieval result", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public ILspLayer createFusionRasterLayer(ILcdModel aModel) {
    ALfnTileStoreModel model = (ALfnTileStoreModel) aModel;
    TLfnTileStoreModelDescriptor desc = (TLfnTileStoreModelDescriptor) model.getModelDescriptor();

    ILcdEarthTileSet tileSet = (ILcdEarthTileSet) model.elements().nextElement();
    TLspLayerTreeNode node = new TLspLayerTreeNode(desc.getDisplayName());
    ALfnCoverageMetadata metadata = desc.getCoverageMetadata();
    // V120-2521 Multivalued raster coverages are currently not supported. If we don't skip them, we get an ugly NPE
    // from TLspTextureLayerTileSet.
    if (metadata != null && metadata.getType() != MULTIVALUED) {
      TLcdEarthCompositeTileSet filtered = new TLcdEarthCompositeTileSet(tileSet.getBounds(), tileSet.getLevelCount(),
                                                                         tileSet.getTileRowCount(0), tileSet.getTileColumnCount(0));
      String id = metadata.getId();
      filtered.registerCoverage(tileSet, tileSet.getTileSetCoverage(id));

      TLcdVectorModel filteredModel = new TLcdVectorModel(model.getModelReference(), new TLcdEarthModelDescriptor(id,
                                                                                                                  id, id));
      filteredModel.addElement(filtered, NO_EVENT);

      TLspRasterLayerBuilder layerBuilder = TLspRasterLayerBuilder.newBuilder()
                                                                  .model(filteredModel)
                                                                  .label(id)
                                                                  .layerType(BACKGROUND);
      if (metadata.getType() == ELfnDataType.ELEVATION) {
        // V120-2521 Add a styler for elevation coverages.
        layerBuilder.styler(REGULAR_BODY, createElevationStyle());
      }
      node.addLayer(layerBuilder.build());
    }

    if (node.layerCount() == 0) {
      return null;
    }

    if (node.layerCount() == 1) {
      return (ILspLayer) node.getLayer(0);
    }

    return node;
  }

  // #snippet CREATE_MODEL
  protected ALfnTileStoreModel createModel(ALfnEnvironment aEnvironment, ALfnTileStore aTileStore,
                                           String aCoverageId) throws IOException {
    TLfnTileStoreModelDecoder modelDecoder = new TLfnTileStoreModelDecoder(aEnvironment);
    return modelDecoder.decode(aTileStore, aCoverageId);
  }
// #endsnippet

  /**
   * Listener for the "Create terrain" action.
   */
  private class CreateLayerListener implements ActionListener {

    public void actionPerformed(ActionEvent aEvent) {
      createFusionLayer();
    }
  }

  private TLspRasterStyle createElevationStyle() {
    double[] levels = new double[]{0, 500, 2000, 4000};
    Color[] colors = new Color[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED};
    TLcdDTEDColorModelFactory modelFactory = new TLcdDTEDColorModelFactory();
    modelFactory.setLevels(levels);
    modelFactory.setColors(colors);
    ColorModel colorModel = modelFactory.createColorModel();
    return TLspRasterStyle.newBuilder().colorModel(colorModel).build();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "");
  }

}
