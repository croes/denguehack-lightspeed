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
package samples.tea.gxy.hypsometry;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ColorModel;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.earth.repository.TLcdEarthTileRepository;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdBufferedTile;
import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.hypsometry.*;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SamplePanel;
import samples.common.formatsupport.GXYOpenSupport;
import samples.common.formatsupport.OpenAction;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample demonstrates some of the hypsometric functionality of
 * the terrain analysis (tea) Industry Specific Component.
 * <p/>
 * Five additional layers are created for a standard elevation model,
 * showing shading, slope, ridge/valley, orientation, and azimuth,
 * respectively. The shading layer is semi-transparent. You can see
 * the other layers by toggling their visibilities.
 */
public class MainPanel extends SamplePanel {

  private static final String DEFAULT_SOURCE = "Data/Earth/SanFrancisco/" + TLcdEarthTileRepository.ROOT_FILENAME;

  private String fSource;

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel( new TLcdLonLatBounds( 8, 43, 6, 5 ) );

  private ILcdGXYLayer fElevationLayer;

  private ILcdRaster fHypsometricRasterAzimuth;
  private ILcdRaster fHypsometricRasterOrientation;
  private ILcdRaster fHypsometricRasterRidgeValley;
  private ILcdRaster fHypsometricRasterSlope;
  private ILcdRaster fHypsometricRasterShading;

  private HypsometryUtil.HypsometryEarthTileContext fHypsometricEarthTileSetContextAzimuth;
  private HypsometryUtil.HypsometryEarthTileContext fHypsometricEarthTileSetContextOrientation;
  private HypsometryUtil.HypsometryEarthTileContext fHypsometricEarthTileSetContextRidgeValley;
  private HypsometryUtil.HypsometryEarthTileContext fHypsometricEarthTileSetContextSlope;
  private HypsometryUtil.HypsometryEarthTileContext fHypsometricEarthTileSetContextShading;

  private Format fHypsometricFormatAzimuth;
  private Format fHypsometricFormatOrientation;
  private Format fHypsometricFormatRidgeValley;
  private Format fHypsometricFormatSlope;
  private Format fHypsometricFormatShading;

  private JLabel fLabelAzimuth      = new JLabel( "Unknown" );
  private JLabel fLabelOrientation  = new JLabel( "Unknown" );
  private JLabel fLabelRidgeValley  = new JLabel( "Unknown" );
  private JLabel fLabelSlope        = new JLabel( "Unknown" );
  private JLabel fLabelShading      = new JLabel( "Unknown" );

  private GXYOpenSupport fOpenSupport;
  private MouseMotionAdapter fMouseMotionAdapter;
  private ILcdGXYLayerFactory fElevationLayerFactory = new ElevationLayerFactory();

  @Override
  protected void createGUI() {
    //Create an asynchronous paint queue manager
    TLcdGXYAsynchronousPaintQueueManager queueManager = new TLcdGXYAsynchronousPaintQueueManager();
    queueManager.setGXYView( fMapJPanel );

    // Create the default toolbar and layer control.
    ToolBar toolBar      = new ToolBar( fMapJPanel, true, this );
    LayerControlPanel layerControl = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel( fMapJPanel );

    JPanel eastPanel = new JPanel( new BorderLayout() );
    eastPanel.add( BorderLayout.NORTH, createHypsometricInfoPanel() );
    eastPanel.add( BorderLayout.CENTER, layerControl );

    // Create a titled panel around the map panel
    TitledPanel mapPanel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH,  toolBar   );
    add( BorderLayout.CENTER, mapPanel  );
    add( BorderLayout.EAST,   eastPanel );

    fOpenSupport = new GXYOpenSupport( fMapJPanel, Arrays.asList( new TLcdEarthRepositoryModelDecoder(),
            new TLcdDMEDModelDecoder() ,
            new TLcdDEMModelDecoder() ), Collections.singleton( fElevationLayerFactory ) );
    fOpenSupport.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading data"));

    toolBar.addAction( new OpenAction( fOpenSupport ) );
    fOpenSupport.addModelProducerListener( new ILcdModelProducerListener() {
      @Override
      public void modelProduced( final TLcdModelProducerEvent aModelProducerEvent ) {
        //Every time we open a file, we create a new set of hypsometric layers, and remove the old ones
        Runnable runnable = new Runnable() {
          public void run() {
            List<ILcdGXYLayer> layersToBeRemoved = new ArrayList<ILcdGXYLayer>(  );
            for( int i = 0 ; i < fMapJPanel.layerCount() ; i++ ){
              ILcdGXYLayer layer = ( ILcdGXYLayer ) fMapJPanel.getLayer( i );
              if(layer!=fMapJPanel.getGridLayer()){
                layersToBeRemoved.add( layer );
              }
            }
            for ( ILcdGXYLayer layerToBeremoved : layersToBeRemoved ) {
              GXYLayerUtil.removeGXYLayer( fMapJPanel, layerToBeremoved, false );
            }
            //Re-initialize the model
            initializeForModel( aModelProducerEvent.getModel() );
            //Move the grid layer back up
            if ( fMapJPanel.containsLayer( fMapJPanel.getGridLayer() ) ) {
              GXYLayerUtil.moveGXYLayer( fMapJPanel, fMapJPanel.layerCount()-1, fMapJPanel.getGridLayer());
            }
          }
        };
        TLcdAWTUtil.invokeLater(runnable);
      }
    } );
  }

  @Override
  protected void addData() {
    String source = fSource != null
            ? fSource
            : "" + DEFAULT_SOURCE;
      fOpenSupport.openSource( source );
  }

  private void initializeForModel( ILcdModel aModel ) {
    // Add a standard layer containing the elevation model to the view.
    fElevationLayer = fElevationLayerFactory.createGXYLayer( aModel );
    GXYLayerUtil.addGXYLayer( fMapJPanel, fElevationLayer );
    GXYLayerUtil.fitGXYLayer( fMapJPanel, fElevationLayer );

    DecimalFormat decimalFormat1 = new DecimalFormat();
    decimalFormat1.setMaximumFractionDigits( 2 );
    decimalFormat1.setMinimumFractionDigits( 2 );
    decimalFormat1.setMaximumIntegerDigits( 3 );
    decimalFormat1.setMinimumIntegerDigits( 3 );

    DecimalFormat decimalFormat2 = new DecimalFormat();
    decimalFormat2.setMaximumFractionDigits( 2 );
    decimalFormat2.setMinimumFractionDigits( 2 );

    ILcdHypsometricNormalProvider normalProvider =
            //new TLcdHypsometric2x2NormalProvider();
            new TLcdHypsometric3x3NormalProvider();

    // Add a layer showing the azimuth of the terrain.
    ILcdHypsometricValueProvider azimuth
        = new TLcdHypsometricOrientationAngle( 0.0, 1.0, normalProvider );
    ILcdGXYLayer azimuthLayer = createHypsometricRasterLayer(
        aModel,
        "Azimuth",
        true,
        azimuth,
        HypsometryUtil.createAzimuthColorModel()
    );
    fHypsometricRasterAzimuth = createHypsometricRaster( azimuthLayer );
    fHypsometricEarthTileSetContextAzimuth = HypsometryUtil.createHypsometricEarthTileSetContext( azimuthLayer );
    fHypsometricFormatAzimuth = HypsometryUtil.createHypsometricFormat( azimuth, new HypsometryUtil.ConvertToDegreeFormat( decimalFormat1 ) );
    GXYLayerUtil.addGXYLayer( fMapJPanel, azimuthLayer );

    // Add a layer showing the orientation of the terrain.
    ILcdHypsometricValueProvider orientation
        = new TLcdHypsometricOrientation( 0.0, 1.0, normalProvider );
    ILcdGXYLayer orientationLayer = createHypsometricRasterLayer(
        aModel,
        "Orientation",
        false,
        orientation,
        HypsometryUtil.createOrientationColorModel()
    );
    fHypsometricRasterOrientation = createHypsometricRaster( orientationLayer );
    fHypsometricEarthTileSetContextOrientation = HypsometryUtil.createHypsometricEarthTileSetContext( orientationLayer );
    fHypsometricFormatOrientation = HypsometryUtil.createHypsometricFormat( orientation, decimalFormat2 );
    GXYLayerUtil.addGXYLayer( fMapJPanel, orientationLayer );

    // Add a layer showing the ridges and valleys of the terrain.
    ILcdHypsometricValueProvider ridgeValley
        = new TLcdHypsometricCrease( new Rectangle( -2, -2, 5, 5 ), TLcdHypsometricCrease.RIDGE_AND_VALLEY );
    ILcdGXYLayer ridgeValleyLayer = createHypsometricRasterLayer(
        aModel,
        "Ridge/valley",
        false,
        ridgeValley,
        HypsometryUtil.createRidgeValleyColorModel()
    );
    fHypsometricRasterRidgeValley = createHypsometricRaster( ridgeValleyLayer );
    fHypsometricEarthTileSetContextRidgeValley = HypsometryUtil.createHypsometricEarthTileSetContext( ridgeValleyLayer );
    fHypsometricFormatRidgeValley = HypsometryUtil.createHypsometricFormat( ridgeValley, new HypsometryUtil.ConvertToDegreeFormat( decimalFormat1 ) );
    GXYLayerUtil.addGXYLayer( fMapJPanel, ridgeValleyLayer );

    // Add a layer showing the slope of the terrain.
    ILcdHypsometricValueProvider slope
        = new TLcdHypsometricSlopeAngle( 0.0, 0.0, 1.0, normalProvider );
    ILcdGXYLayer slopeLayer = createHypsometricRasterLayer(
        aModel,
        "Slope",
        false,
        slope,
        HypsometryUtil.createSlopeAngleColorModel()
    );
    fHypsometricRasterSlope = createHypsometricRaster( slopeLayer );
    fHypsometricEarthTileSetContextSlope = HypsometryUtil.createHypsometricEarthTileSetContext( slopeLayer );
    fHypsometricFormatSlope = HypsometryUtil.createHypsometricFormat( slope, new HypsometryUtil.ConvertToDegreeFormat( decimalFormat1 ) );
    GXYLayerUtil.addGXYLayer( fMapJPanel, slopeLayer );

    // Add a layer showing the shading of the terrain.
    ILcdHypsometricValueProvider shading
        = new TLcdHypsometricSlope( -1.0, 1.0, 1.0, normalProvider );
    ILcdGXYLayer shadingLayer = createHypsometricRasterLayer(
        aModel,
        "Shading",
        false,
        shading,
        HypsometryUtil.createShadingColorModel()
    );
    fHypsometricRasterShading = createHypsometricRaster( shadingLayer );
    fHypsometricEarthTileSetContextShading = HypsometryUtil.createHypsometricEarthTileSetContext( shadingLayer );
    fHypsometricFormatShading = HypsometryUtil.createHypsometricFormat( shading, decimalFormat2 );
    GXYLayerUtil.addGXYLayer( fMapJPanel, shadingLayer );

    // Add mouse motion listener to update the hypsometric values.
    if ( fMouseMotionAdapter==null ) {
      fMouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseMoved( MouseEvent aEvent ) {
          fLabelAzimuth.setText( fHypsometricFormatAzimuth.format( retrieveHypsometricValue( aEvent.getPoint(), fHypsometricRasterAzimuth == null ? fHypsometricEarthTileSetContextAzimuth : fHypsometricRasterAzimuth ) ) );
          fLabelOrientation.setText( fHypsometricFormatOrientation.format( retrieveHypsometricValue( aEvent.getPoint(), fHypsometricRasterOrientation == null ? fHypsometricEarthTileSetContextOrientation : fHypsometricRasterOrientation ) ) );
          fLabelRidgeValley.setText( fHypsometricFormatRidgeValley.format( retrieveHypsometricValue( aEvent.getPoint(), fHypsometricRasterRidgeValley == null ? fHypsometricEarthTileSetContextRidgeValley : fHypsometricRasterRidgeValley ) ) );
          fLabelSlope.setText( fHypsometricFormatSlope.format( retrieveHypsometricValue( aEvent.getPoint(), fHypsometricRasterSlope == null ? fHypsometricEarthTileSetContextSlope : fHypsometricRasterSlope ) ) );
          fLabelShading.setText( fHypsometricFormatShading.format( retrieveHypsometricValue( aEvent.getPoint(), fHypsometricRasterShading == null ? fHypsometricEarthTileSetContextShading : fHypsometricRasterShading ) ) );
        }
      };
    }
    fMapJPanel.removeMouseMotionListener( fMouseMotionAdapter );
    fMapJPanel.addMouseMotionListener( fMouseMotionAdapter );
  }

  /**
   * Creates a small panel containing the read-outs of the various hypsometric functions.
   * @return a JPanel
   */
  private JPanel createHypsometricInfoPanel() {
    JPanel panel1 = new JPanel( new GridLayout( 0, 2, 4, 4 ) );
    panel1.add( new JLabel( "Azimuth"      ) ); panel1.add( fLabelAzimuth     );
    panel1.add( new JLabel( "Ridge/Valley" ) ); panel1.add( fLabelRidgeValley );
    panel1.add( new JLabel( "Slope"        ) ); panel1.add( fLabelSlope       );

    JPanel panel2 = new JPanel( new GridLayout( 0, 2, 4, 4 ) );
    panel2.add( new JLabel( "Orientation"  ) ); panel2.add( fLabelOrientation );
    panel2.add( new JLabel( "Shading"      ) ); panel2.add( fLabelShading     );

    JPanel labelPanel = new JPanel();
    labelPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    labelPanel.setLayout( new BoxLayout( labelPanel, BoxLayout.PAGE_AXIS ) );
    labelPanel.add( panel1 );
    labelPanel.add( Box.createVerticalStrut( 8 ) );
    labelPanel.add( panel2 );
    return TitledPanel.createTitledPanel( "Hypsometric values", labelPanel );
  }



  /**
   * Creates a hypsometric raster layer, capable of visualizing the results of a
   * hypsometric calculation on an elevation dataset.
   * @param aModel        The model to create a hypsometric layer for. This can be
   *                      a model containing a raster, a multilevel raster or an
   *                      earth elevation tileset.
   * @param aLabel        The label to give the layer
   * @param aVisible      Whether the layer should be visible or not after creation
   * @param aHypsometricValueProvider The value provider to use to calculate hypsometric values
   * @param aHypsometricColorModel    The color model to use to interpret the hypsometric
   *                                  values.
   * @return A layer capable of calculating and visualizing the results of a hypsometric value
   *         provider.
   */
  private ILcdGXYLayer createHypsometricRasterLayer( ILcdModel                    aModel,
                                                     String                       aLabel,
                                                     boolean                      aVisible,
                                                     ILcdHypsometricValueProvider aHypsometricValueProvider,
                                                     ColorModel                   aHypsometricColorModel ) {

    TLcdGXYLayer gxyLayer = new TLcdGXYLayer();
    gxyLayer.setModel( aModel );
    gxyLayer.setLabel( aLabel );
    gxyLayer.setVisible( aVisible );

    // Create a painter to paint hypsometric rasters or multilevel rasters.
    TLcdHypsometricTileFactory tileFactory = new TLcdHypsometricTileFactory(
        aHypsometricValueProvider,
        HypsometryUtil.MINIMUM_HYPSOMETRIC_VALUE,
        HypsometryUtil.MAXIMUM_HYPSOMETRIC_VALUE,
        HypsometryUtil.UNKNOWN_HYPSOMETRIC_VALUE,
        aHypsometricColorModel,
        TLcdSharedBuffer.getBufferInstance()
    );

    ILcdHypsometricRasterFactory rasterFactory =
        new TLcdHypsometricRasterFactory( tileFactory, HypsometryUtil.UNKNOWN_HYPSOMETRIC_VALUE );

    ILcdHypsometricMultilevelRasterFactory multilevelRasterFactory =
        new TLcdHypsometricMultilevelRasterFactory( rasterFactory );

    ILcdGXYPainterProvider painter = null;
    if ( aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor ) {
      painter = new TLcdHypsometricRasterPainter( rasterFactory );
    } else if( aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor ) {
      painter = new TLcdHypsometricMultilevelRasterPainter( multilevelRasterFactory );
    }else if(aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor){
      painter = new TLcdHypsometricEarthPainter( tileFactory );
    }

    // The painters can act as their own painter providers.
    gxyLayer.setGXYPainterProvider( painter );

    return gxyLayer;
  }

  /**
   * Creates a hypsometric raster for a given layer. The hypsometric raster will contain the result
   * of the hypsometric value provider of the layer.
   *
   * Note: This method only returns a non-null raster when the input layer contains a raster
   * or a multilevel raster. Earth elevation tilesets can retrieve their values straight
   * from the painter.
   * @param aHypsometricRasterLayer a layer containing an elevation raster or a multilevel
   *                                elevation raster.
   * @return A raster containing hypsometric values; or null
   */
  private ILcdRaster createHypsometricRaster( ILcdGXYLayer aHypsometricRasterLayer ) {
    if ( aHypsometricRasterLayer instanceof TLcdGXYLayer ) {
      Object object = aHypsometricRasterLayer.getModel().elements().nextElement();
      if ( object instanceof ILcdMultilevelRaster ) {
        ILcdHypsometricMultilevelRasterFactory hypsometricMultilevelRasterFactory
            = ( ( TLcdHypsometricMultilevelRasterPainter ) ( ( TLcdGXYLayer ) aHypsometricRasterLayer ).getGXYPainterProvider() ).getHypsometricMultilevelRasterFactory();

        ILcdMultilevelRaster multilevelRaster              = ( ILcdMultilevelRaster ) object;
        ILcdMultilevelRaster hypsometricMultilevelRaster  = hypsometricMultilevelRasterFactory.createHypsometricMultilevelRaster( multilevelRaster, HypsometryUtil.computeElevationScale( multilevelRaster.getBounds(), fMapJPanel, fElevationLayer ) );
        return hypsometricMultilevelRaster.getRaster( hypsometricMultilevelRaster.getRasterCount() >= 2 ? 1 : 0 );
      }
      else if ( object instanceof ILcdRaster ) {
        ILcdHypsometricRasterFactory hypsometricRasterFactory
            = ( ( TLcdHypsometricRasterPainter ) ( ( TLcdGXYLayer ) aHypsometricRasterLayer ).getGXYPainterProvider() ).getHypsometricRasterFactory();

        ILcdRaster raster = ( ILcdRaster ) object;
        return hypsometricRasterFactory.createHypsometricRaster( raster, HypsometryUtil.computeElevationScale( raster.getBounds(), fMapJPanel, fElevationLayer ) );
      }
    }
    return null;
  }

  /**
   * Retrieves a hypsometric value for a given point.
   *
   * @param aPoint A point in model coordinates to retrieve hypsometric values for
   * @param aObject Either an ILcdRaster, or a HypsometryEarthTileContext (which contains both
   *                the ILcdEarthTileSet of the model and the painter)
   * @return a hypsometric value as it was calculated by a hypsometric value provider. The result will
   *         need to be formatted to the correct measure.
   */
  private double retrieveHypsometricValue( Point aPoint, Object aObject ) {
    try {
      ILcdModelXYWorldTransformation   mwt = HypsometryUtil.createModelXYWorldTransformation( fMapJPanel, fElevationLayer );
      ILcdGXYViewXYWorldTransformation vwt = new TLcdGXYViewXYWorldTransformation( fMapJPanel );

      TLcdXYZPoint worldPoint = new TLcdXYZPoint();
      TLcdXYZPoint modelPoint = new TLcdXYZPoint();
      vwt.viewAWTPoint2worldSFCT( aPoint, worldPoint );
      mwt.worldPoint2modelSFCT( worldPoint, modelPoint );

      if ( aObject instanceof ILcdRaster ) {
        return ( ( ILcdRaster ) aObject ).retrieveValue( modelPoint.getX(), modelPoint.getY() );
      }else if(aObject instanceof HypsometryUtil.HypsometryEarthTileContext ){
        return ( ( HypsometryUtil.HypsometryEarthTileContext ) aObject ).getGXYRasterPainter().retrieveCachedHypsometricValue( modelPoint, ( ( HypsometryUtil.HypsometryEarthTileContext ) aObject ).getEarthTileSet() );
      }
    }
    catch ( Exception aException ) {
      // exception found, use unknown value
      return HypsometryUtil.UNKNOWN_HYPSOMETRIC_VALUE;
    }
    return HypsometryUtil.UNKNOWN_HYPSOMETRIC_VALUE;
  }

  /**
   * Runs the sample as a stand-alone application in a frame. Accepts an
   * optional elevation raster source to be displayed instead of the default.
   * @param aArgs Arguments to the main method.
   */
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        MainPanel sample = new MainPanel();
        sample.fSource = aArgs.length == 0 ? null : aArgs[ 0 ];
        new LuciadFrame( sample, "Hypsometry" );
      }
    } );
  }

}
