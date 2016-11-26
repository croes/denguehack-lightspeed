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
package samples.gxy.rectification;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.map.TLcdMapJPanel;
import com.luciad.view.map.TLcdMapLonLatGridLayer;

import samples.common.LuciadFrame;
import samples.common.SamplePanel;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.rectification.io.CompositeModelDecoder;
import samples.gxy.rectification.io.ExportGeotiffRasterRefAction;
import samples.gxy.rectification.io.ExportGeotiffViewRefAction;

/**
 * This sample demonstrates how to combine non-parametric and parametric rectification of a raster
 * model. The sample contains two panels. The left panel contains a non-referenced image and tie
 * points in pixel coordinates, while the right panel contains several layers: the terrain elevation
 * layer, the raster in its original (non-rectified) reference, the parametrically rectified raster
 * (also referred to as orthorectified raster), the non-parametrically (tie-points) rectified raster
 * (also referred to as corrected raster) and finally a layer containing vector data that helps
 * to verify the accuracy of the rectification.
 *
 * At the core of the sample are the classes Orthorectifier and TiePointsRectifier. The first one
 * takes the original raster together with the terrain data and creates the orthorectified raster.
 * The second class takes the orthorectified raster and uses it as input for the tie-point
 * rectification process.
 */
public class MainPanel extends SamplePanel {

  private static final String DEFAULT_RASTER = "Data/Ithaca/GeoTIFF/ikonos.tif";
  private static final String DEFAULT_TERRAIN = "Data/Ithaca/DEM/IthacaEast.dem";
  private static final String DEFAULT_VERIFICATION_DATA = "Data/Ithaca/SHP/109rds.shp";

  // Right panel: rectified rasters and tie points in geographical coordinates
  private TLcdMapJPanel fMapJPanel;

  // The layer manager handles the layer creation and some of the interaction with the user.
  private LayerManager fLayerManager;

  protected void createGUI() {
    setLayout(new BorderLayout());
    fMapJPanel = SampleMapJPanelFactory.createMapJPanel();

    fMapJPanel.setXYWorldReference(new TLcdGridReference(new TLcdGeodeticDatum(),
                                                         new TLcdEquidistantCylindrical()));

    // Turn labeling off on the regular grid layer.
    TLcdMapLonLatGridLayer grid_layer = (TLcdMapLonLatGridLayer) fMapJPanel.getGridLayer();
    grid_layer.setLabeled(false);

    TLcdMapJPanel imgJPanel = new TLcdMapJPanel();
    imgJPanel.setPreferredSize(fMapJPanel.getPreferredSize());
    imgJPanel.removeAllLayers();

    // Create the tie point rectifier and the orthorectifier classes.
    TiePointsRectifier rectifier = new TiePointsRectifier();
    Orthorectifier orthorectifier = new Orthorectifier(this);

    // Create the layer manager. The layer manager glues together the user interface and the models
    // produced by the orthorectifier and the rectifier.
    Iterable<ILcdGXYLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class);
    fLayerManager = new LayerManager(imgJPanel, fMapJPanel, orthorectifier, rectifier, layerFactories);

    // Create a default toolbar and layer control for the right-hand panel. 
    ToolBar tool_bar = new ToolBar(fMapJPanel, true, this);

      // Create buttons for opening/saving a raster file.
      Iterable<ILcdModelDecoder> modelDecoders = ServiceRegistry.getInstance().query(ILcdModelDecoder.class);
      OpenSupport open_support = new OpenSupport(this, modelDecoders);
      open_support.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading file"));
      OpenAction open_action = new OpenAction(open_support);
      ExportGeotiffViewRefAction export_geotiff_view = new ExportGeotiffViewRefAction(fMapJPanel, rectifier);
      ExportGeotiffRasterRefAction export_geotiff_raster = new ExportGeotiffRasterRefAction(fMapJPanel, rectifier);
      tool_bar.addAction(open_action);
      tool_bar.addAction(export_geotiff_raster);
      tool_bar.addAction(export_geotiff_view);
      open_support.addModelProducerListener(new MyModelProducerListener());

    JPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    JPanel img_panel = TitledPanel.createTitledPanel("Image", imgJPanel, TitledPanel.NORTH);
    JPanel map_panel = TitledPanel.createTitledPanel("Map", fMapJPanel, TitledPanel.NORTH |
                                                                        TitledPanel.EAST);

    // Create a custom toolbar that can handle tie points and non-parametric rectification.
    TiePointsToolbar tie_points_toolbar = new TiePointsToolbar(imgJPanel,
                                                               rectifier,
                                                               fLayerManager,
                                                               new CompositeModelDecoder(),
                                                               true);

    JPanel left_panel = new JPanel(new BorderLayout());
    JPanel right_panel = new JPanel(new BorderLayout());

    left_panel.add(BorderLayout.NORTH, tie_points_toolbar);
    left_panel.add(BorderLayout.CENTER, img_panel);

    right_panel.add(BorderLayout.NORTH, tool_bar);
    right_panel.add(BorderLayout.CENTER, map_panel);
    right_panel.add(BorderLayout.EAST, layer_control);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                      left_panel, right_panel);
    add(BorderLayout.CENTER, split);

    split.setDividerLocation(0.5);
    split.setResizeWeight(0.5);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Load the elevation data.
    TLcdDEMModelDecoder dmed_decoder = new TLcdDEMModelDecoder(TLcdSharedBuffer.getBufferInstance());
    ILcdModel model = dmed_decoder.decode("" + DEFAULT_TERRAIN);
    fLayerManager.loadTerrainModel(model);

    // Load the raster to be georeferenced.
    String source_name = "" + DEFAULT_RASTER;
    model = new CompositeModelDecoder().decode(source_name);
    if (model.getModelDescriptor() instanceof TLcdRasterModelDescriptor) {
      fLayerManager.loadRasterModel(model);
    }

    // Load vector data for testing the rectification accuracy.
    TLcdSHPModelDecoder shp_decoder = new TLcdSHPModelDecoder();
    ILcdModel custom_model = shp_decoder.decode("" + DEFAULT_VERIFICATION_DATA);
    Iterable<ILcdGXYLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class);
    ILcdGXYLayer layer = new TLcdCompositeGXYLayerFactory(layerFactories).createGXYLayer(custom_model);
    fLayerManager.setCustomDataLayer(layer);
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Combining orthorectification with tie-point correction", 1200, 600);
      }
    });
  }

  /**
   * When a new custom model is loaded, it creates a layer and passes it on to the layer manager.
   */
  private class MyModelProducerListener implements ILcdModelProducerListener {
    public void modelProduced(TLcdModelProducerEvent aEvent) {
      ILcdModel model = aEvent.getModel();
      Iterable<ILcdGXYLayerFactory> layerFactories = ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class);
      ILcdGXYLayer layer = new TLcdCompositeGXYLayerFactory(layerFactories).createGXYLayer(model);
      fLayerManager.setCustomDataLayer(layer);
    }
  }
}
