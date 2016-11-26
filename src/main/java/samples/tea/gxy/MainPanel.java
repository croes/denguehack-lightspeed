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
package samples.tea.gxy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.tea.TLcdProfileViewJPanel;
import com.luciad.tea.TLcdTerrainProfileController;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.tea.Legend;

/**
 * This is the entry class for this demo: either as an <code>JApplet</code> or a
 * standAlone application.
 */
public class MainPanel extends GXYSample {


  private TLcdProfileViewJPanel fProfileView = new TLcdProfileViewJPanel();
  private TLcdTerrainProfileController fTerrainProfileController = new TLcdTerrainProfileController();
  private CreateContoursAction fCreateContoursAction = new CreateContoursAction();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds( 8, 43, 6, 5 );
  }

  @Override
  protected JPanel createBottomPanel() {
    fProfileView.setLineOfSightEnabled( true );
    fProfileView.setProfileReference(new TLcdGeodeticReference( new TLcdGeodeticDatum() ));

    fTerrainProfileController.setProfileView( fProfileView );
    fTerrainProfileController.setForeground( new Color( 70, 100, 50 ) );
    fTerrainProfileController.setGhostColor( Color.black );

    getToolBars()[0].addGXYController(fTerrainProfileController);
    getToolBars()[0].addAction(fCreateContoursAction);

    fProfileView.setMinimumSize( new Dimension( 500, 100 ) );
    fProfileView.setPreferredSize( new Dimension( 500, 100 ) );

    return TitledPanel.createTitledPanel( "Profile view", fProfileView, TitledPanel.NORTH );
  }

  @Override
  protected JPanel createSettingsPanel() {
    double[] contourLevels = TeaLayerFactory.getContourLevels();
    Color [] contourColors = TeaLayerFactory.getContourColors();
    String[] labels = new String[ contourLevels.length];
    for ( int labelIndex = 0; labelIndex < contourLevels.length ; labelIndex++ ) {
      double colorLevel = contourLevels[ labelIndex ];
      labels[ labelIndex ] = colorLevel != ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE
                              ? "\u2264 " + Integer.toString( (int) colorLevel ) + " m"
                              : "Unknown";
    }
    return new Legend( contourColors, labels, false );
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    final ILcdGXYLayer dtedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(getView()).getLayer();
    if ( dtedLayer != null ) {
      // Set world reference to the model reference
      final ILcdModelReference modelReference = dtedLayer.getModel().getModelReference();
      EventQueue.invokeLater( new Runnable() {
        @Override
        public void run() {
          if ( modelReference instanceof ILcdXYWorldReference ) {
            getView().setXYWorldReference( ( ILcdXYWorldReference ) modelReference );
          }
          // the DTED model contains the altitude information.
          ILcdMultilevelRaster multilevelRaster =
                  (ILcdMultilevelRaster) dtedLayer.getModel().elements().nextElement();
          // DTED level 0 is raster at index 1
          ILcdRaster raster = multilevelRaster.getRaster( 1 );
          fCreateContoursAction.setGXYView( getView() );
          fCreateContoursAction.setBounds( raster.getBounds().cloneAs2DEditableBounds() );
          fCreateContoursAction.setRaster( raster );
        }
      } );
    }

  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Terrain elevation display" );
  }
}
