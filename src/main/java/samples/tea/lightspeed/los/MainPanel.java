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
package samples.tea.lightspeed.los;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.terrain.TLcdEarthTileSetElevationProvider;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.tea.lightspeed.los.gui.LOSParametersPanel;
import samples.tea.lightspeed.los.gui.LOSStylePanel;
import samples.tea.lightspeed.los.model.LOSCoverageInputShape;
import samples.tea.lightspeed.los.view.LOSCoverageStyler;
import samples.tea.lightspeed.los.view.LOSLayerFactory;

/**
 * This sample demonstrates Lightspeed's ability to perform Line-Of-Sight computations on a 3D terrain at interactive rates.
 */
public class MainPanel extends LightspeedSample {

  // X- and Y-coordinate of the view's initial reference point
  private static final double CENTER_X = -122.45;
  private static final double CENTER_Y = 37.76;

  private LOSCoverageStyler fLOSCoverageStyler;
  private ILspLayer fLOSInputLayer;
  private ILspLayer fLOSOutputLayer;
  private LOSParametersPanel fLOSParametersPanel;
  private LOSLayerFactory fLOSLayerFactory;

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView( ILspView.ViewType.VIEW_3D );
    TLspViewXYZWorldTransformation3D w2v = ( TLspViewXYZWorldTransformation3D ) view
        .getViewXYZWorldTransformation();

    TLcdXYZPoint lookAt = new TLcdXYZPoint();
    TLcdEllipsoid.DEFAULT.llh2geocSFCT( new TLcdLonLatHeightPoint( CENTER_X, CENTER_Y, 0 ), lookAt );
    w2v.lookAt( lookAt, 8e3, 0, -45, 0 );

    return view;
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fLOSCoverageStyler = new LOSCoverageStyler();
    LOSStylePanel losStylePanel = new LOSStylePanel( fLOSCoverageStyler );
    losStylePanel.setOpaque( true );
    final TitledPanel styleTitledPanel = TitledPanel.createTitledPanel( "Style", losStylePanel );   
    fLOSParametersPanel = new LOSParametersPanel();
    final TitledPanel parametersTitledPanel = TitledPanel.createTitledPanel( "Parameters", fLOSParametersPanel );
    JPanel combined = new JPanel();
    combined.setLayout( new BoxLayout( combined, BoxLayout.Y_AXIS) );
    combined.add( styleTitledPanel );
    combined.add(parametersTitledPanel);
    fLOSParametersPanel.setEnabled( false );
    addComponentToRightPanel( combined );
  }

  protected void addData() throws IOException{
    // Create an earth model (and layer) which will be used for the LoS calculations
    ILcdModel earthModel = LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).getModel();

    try {
      TLspLOSCalculator losCalculator = new TLspLOSCalculator();
      losCalculator.setCoverageAltitudeMode( TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL );
      fLOSLayerFactory = new LOSLayerFactory( losCalculator );
      fLOSLayerFactory.setLOSCoverageStyler(fLOSCoverageStyler);
      getView().addLayeredListener( new MyAltitudeSettingLayerListener(fLOSLayerFactory) );
      ServiceRegistry.getInstance().register(fLOSLayerFactory);

      getView().addLayersFor(earthModel);

      // Create the line of sight model and layers
      TLcdGeodeticReference geodeticReference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
      LOSCoverageInputShape losCoverageInputShape = new LOSCoverageInputShape( new TLcdLonLatPoint( CENTER_X, CENTER_Y ), 2500.0, 200.0, 0.0, 360.0, 3.0, 0.0, 180.0, geodeticReference.getGeodeticDatum().getEllipsoid() );
      final TLcdVectorModel losInputModel = new TLcdVectorModel(
          geodeticReference,
          new TLcdModelDescriptor( "LOSInputModel", "LOSInputModel", "LOSInputModel" )
      );
      losInputModel.addElement( losCoverageInputShape, ILcdModel.FIRE_NOW );
      Collection<ILspLayer> layers = getView().addLayersFor( losInputModel );


      for ( ILspLayer layer : layers ) {
        if ( layer.getLabel().equals( LOSLayerFactory.LOS_INPUT_LAYER_LABEL ) ) {
          fLOSInputLayer = layer;
          fLOSInputLayer.addSelectionListener( new LOSInputSelectionListener() );
        }
        else if ( layer.getLabel().equals( LOSLayerFactory.LOS_OUTPUT_LAYER_LABEL ) ) {
          fLOSOutputLayer = layer;
        }
      }
    } catch (final RuntimeException e) {
      getView().addLayersFor(earthModel);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          StringBuilder message = new StringBuilder();
          message.append("The following error occurred while initializing the sample data:\r\n").append("\n");
          String formattedError = e.getMessage().replaceAll("(.{85})", "$1\n");
          message.append(formattedError);
          JOptionPane.showMessageDialog(getView().getHostComponent(), message, "Error while starting sample", JOptionPane.ERROR_MESSAGE);
        }
      });
    }

  }

  /**
   * A layered listener that updates the altitude provider of the given <code>LOSLayerFactory</code>
   * as soon as a layer is added to the view.
   */
  private static class MyAltitudeSettingLayerListener implements ILcdLayeredListener {
    private final LOSLayerFactory fLosLayerFactory;

    public MyAltitudeSettingLayerListener( LOSLayerFactory aLosLayerFactory ) {
      fLosLayerFactory = aLosLayerFactory;
    }

    @Override
    public void layeredStateChanged( TLcdLayeredEvent e ) {
      ILspView view = ( ILspView ) e.getLayered();
      ILcdEarthTileSet elevationTileSet = view.getServices().getTerrainSupport().getElevationTileSet();
      TLcdEarthTileSetElevationProvider elevationProvider = new TLcdEarthTileSetElevationProvider( elevationTileSet, 1, 0, 64 );
      elevationProvider.setForceAsynchronousTileRequests( false );
      int tileLevel = 10;
      elevationProvider.setMaxSynchronousLevel( tileLevel );
      elevationProvider.setMaxTileLevel( tileLevel );
      EarthTerrainElevationAdapter earthTerrainElevationAdapter = new EarthTerrainElevationAdapter( elevationProvider );
      fLosLayerFactory.setAltitudeProvider( earthTerrainElevationAdapter );
    }
  }

  /**
   * Inner class that listens to changes in selection of the input LOS layer and adapts the
   * LOSParametersPanel and LOSStylePanel accordingly.
   */
  private class LOSInputSelectionListener implements ILcdSelectionListener {
    @Override
    public void selectionChanged( TLcdSelectionChangedEvent aSelectionEvent ) {
      Enumeration enumeration = fLOSInputLayer.selectedObjects();
      ArrayList list = Collections.list( enumeration );
      if ( list.size() == 1 ) {
        Object selectedObject = list.get( 0 );
        if ( selectedObject instanceof LOSCoverageInputShape ) {
          fLOSParametersPanel.setActiveCoverageInputShape( fLOSInputLayer.getModel(), ( LOSCoverageInputShape ) selectedObject );
          fLOSParametersPanel.setEnabled( true );
        }
      }
      else {
        fLOSParametersPanel.setActiveCoverageInputShape( null, null );
        fLOSParametersPanel.setEnabled( false );
      }
    }
  }

  /**
   * We override the tearDown() method to make sure that the TLspLOSCalculator is disposed
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    if (fLOSLayerFactory!=null) {
      fLOSLayerFactory.dispose();
    }
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        startSample(MainPanel.class, "Line-of-sight");
      }
    } );
  }

}
