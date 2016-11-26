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
package samples.tea.gxy.visibility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.tea.TLcdDensityBasedRasterElevationProvider;
import com.luciad.tea.TLcdGXYViewBasedTerrainElevationProvider;
import com.luciad.tea.TLcdVisibilityInterpretation;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.tea.AbstractVisibilityAction;
import samples.tea.IntervisibilityPanel;

/**
 * In this sample, we demonstrate the to-polygon (or to-area) and
 * to-polyline (or to-path) intervisibility computations.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(8, 43, 6, 5));

  private ILcdGXYLayer fPointLayer    = InputLayerFactory.createPointLayer();
  private ILcdGXYLayer fPolylineLayer = InputLayerFactory.createPolylineLayer();
  private ILcdGXYLayer fPolygonLayer  = InputLayerFactory.createPolygonLayer();

  private ILcdGXYLayer fToPolylineLayer = VisibilityLayerFactory.createToPolylineLayer();
  private ILcdGXYLayer fToPolygonLayer  = VisibilityLayerFactory.createToPolygonLayer();

  private TLcdGXYViewBasedTerrainElevationProvider fTerrainElevationProvider
          = new TLcdGXYViewBasedTerrainElevationProvider( new TLcdDensityBasedRasterElevationProvider() );

  private GXYVisibilityAction fAction;

  protected void createGUI() {
    // create a terrain elevation provider for DTED and DEM data.
    fTerrainElevationProvider.setGXYView( fMapJPanel );
    fTerrainElevationProvider.setUseOnlyVisibleLayers( true );

    // Create the default tool bar and layer control.
    ToolBar tool_bar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );

    JPanel east_panel = new JPanel( new BorderLayout() );
    east_panel.add( BorderLayout.NORTH, createIntervisibilityPanel() );
    east_panel.add( BorderLayout.CENTER, layer_control );

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH,  tool_bar   );
    add( BorderLayout.CENTER, map_panel  );
    add( BorderLayout.EAST,   east_panel );
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    final ILcdGXYLayer dtedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(fMapJPanel).getLayer();

    // Once the DTED data is available, we can compute a better raster step size for
    // the extraction of the highest N points.
    Object object = dtedLayer.getModel().elements().nextElement();
    if ( object instanceof ILcdMultilevelRaster ) {
      ILcdMultilevelRaster multilevelRaster = (ILcdMultilevelRaster) object;

      // Note that the step size is computed based on the raster's pixel density.
      // For DTED, the multilevel raster will return 4 DTED levels, in spite of
      // the fact that only fewer may be available. In this sample, the raster
      // has only 2 levels, so we will use the second. If we set a more "detailed"
      // level, we won't get better accuracy, but the computation time will be
      // greatly increased (or it may even throw an OutOfMemoryException).
      ILcdRaster DTEDRaster;
      if (multilevelRaster.getRasterCount() >= 2) {
        DTEDRaster = multilevelRaster.getRaster(1);  // Use DTED level 1
      } else {
        DTEDRaster = multilevelRaster.getRaster(0);  // Use DTED level 0
      }
      ILcdGeoReference DTEDRasterReference = (ILcdGeoReference) dtedLayer.getModel().getModelReference();
      fAction.setDTEDRaster(DTEDRaster);
      fAction.setDTEDRasterReference(DTEDRasterReference);
    }

    GXYLayerUtil.addGXYLayer(fMapJPanel, fPointLayer, false, false);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fPolylineLayer, false, false);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fPolygonLayer, false, false);

    GXYLayerUtil.addGXYLayer(fMapJPanel, fToPolylineLayer, false, false);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fToPolygonLayer, false, false);

    fMapJPanel.repaint();
  }

  private JPanel createIntervisibilityPanel() {
    fAction = createAction();
    final IntervisibilityPanel intervisibilityPanel = new IntervisibilityPanel(fAction);

    ILcdModelListener listener = new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        Enumeration elements = aEvent.elements();
        if (elements.hasMoreElements()) {
          intervisibilityPanel.performAction(elements.nextElement());
        }
      }
    };
    fPointLayer.getModel().addModelListener(listener);
    fPolylineLayer.getModel().addModelListener(listener);
    fPolygonLayer.getModel().addModelListener(listener);

    return TitledPanel.createTitledPanel( "Visibility computations", intervisibilityPanel);
  }

  private GXYVisibilityAction createAction() {
    return new GXYVisibilityAction(this, fTerrainElevationProvider, fPointLayer, fPolylineLayer, fPolygonLayer, fToPolylineLayer, fToPolygonLayer);
  }


  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        new LuciadFrame( new MainPanel(), "Visibility" );
      }
    } );
  }

}
