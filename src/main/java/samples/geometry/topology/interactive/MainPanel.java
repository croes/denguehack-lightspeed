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
package samples.geometry.topology.interactive;

import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geometry.ellipsoidal.TLcdEllipsoidalAdvancedBinaryTopology;
import com.luciad.gui.ILcdIcon;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.controllers.NewShapeControllerModel;


/**
 * This editing sample extension demonstrates how to interactively check topology relations.
 */
public class MainPanel extends samples.gxy.editing.MainPanel {

  private TopologyRelationProvider fRelationProvider;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds( -85.00, 25.00, 20.00, 15.00 );
  }

  @Override
  protected JPanel createSettingsPanel() {
    fRelationProvider = new TopologyRelationProvider(new TLcdEllipsoidalAdvancedBinaryTopology( new TLcdEllipsoid() ));
    return TitledPanel.createTitledPanel( "Topology relations", fRelationProvider );
  }

  @Override
  protected void addNewShapeController(NewShapeControllerModel.ShapeType aShapeType, ILcdIcon aIcon, String aTooltip, ToolBar aToolBarSFCT) {
    // Topology relations can be checked for all but these shapes:
    if ( aShapeType != NewShapeControllerModel.ShapeType.TEXT &&
         aShapeType != NewShapeControllerModel.ShapeType.COMPOSITE_CURVE &&
         aShapeType != NewShapeControllerModel.ShapeType.COMPOSITE_RING ) {
      super.addNewShapeController(aShapeType, aIcon, aTooltip, aToolBarSFCT);
    }
  }

  /**
   * Loads the background data and adds the layer in which
   * the newly created shapes will be added, on top of the
   * background data.
   */
  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();
    final ILcdGXYLayer lonLatLayer = factory.createGXYLayer( new ShapeModelFactory().createModel() );
    getToolBars()[0].getSnappables().getSnappableLayers().add(lonLatLayer);

    // Adds the layer to the map panel just beneath the grid layer
    GXYLayerUtil.addGXYLayer( getView(), lonLatLayer, true, false );

    TopologySelectionListener selectionListener = new TopologySelectionListener( fRelationProvider, lonLatLayer );
    lonLatLayer.addSelectionListener( selectionListener );
    lonLatLayer.getModel().addModelListener( new TopologyModelListener( fRelationProvider ) );
  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Interactive topology" );
  }

}
