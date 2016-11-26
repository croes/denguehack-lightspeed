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
package samples.opengl.terrain.geoid;

import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.format.raster.terrain.ILcdMultiLevelTiledTerrain;
import com.luciad.format.raster.terrain.TLcdTerrainModelDecoder;
import com.luciad.format.raster.terrain.opengl.ILcdGLLODChooser;
import com.luciad.format.raster.terrain.opengl.ILcdGLMultiLevelTileGrid;
import com.luciad.format.raster.terrain.opengl.TLcdGLTerrainPainter;
import com.luciad.format.raster.terrain.opengl.paintable.*;
import com.luciad.format.raster.terrain.opengl.texture.ILcdGLTerrainTexture;
import com.luciad.format.raster.terrain.opengl.texture.TLcdGLTerrainTextureDecoder;
import com.luciad.format.raster.terrain.opengl.texture.TLcdGLTiledTerrainTexture;
import com.luciad.format.raster.terrain.preprocessor.ALcdElevationProvider;
import com.luciad.format.raster.terrain.preprocessor.ILcdElevationProvider;
import com.luciad.format.raster.terrain.preprocessor.TLcdBakedMapTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdTerrainPreprocessor;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeoidGeodeticDatumFactory;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.*;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter;

import java.awt.*;
import java.awt.image.ColorModel;
import java.io.IOException;

/**
 * A utility class that preprocesses a terrain in memory and loads it into an ILcdGLView.
 */
class TerrainFactory {

  public static final double[] DTED_LEVELS = {
      TLcdDTEDTileDecoder.UNKNOWN_ELEVATION,
      -100.0, -75.0, -50.0, -25.0, -10.0, 0, 10.0, 25.0, 50.0, 75.0, 100.0
  };

  public static final Color[]  DTED_COLORS = new Color[] {
      new Color(   0,   0,   0, 0 ),        // UNKNOWN
      new Color(  94,  39, 202 ),           // -100
      new Color(  16,  21, 176 ),           //  -75
      new Color(  20,  48, 239 ),           //  -50
      new Color(  53, 134, 236 ),           //  -25
      new Color(  42, 145, 179 ),           //  -10
      new Color(  44, 203, 167 ),           //    0
      new Color(  53, 202, 149 ),           //   10
      new Color(  41, 207,  81 ),           //   25
      new Color( 174, 206,  29 ),           //   50
      new Color( 232, 138,  29 ),           //   75
      new Color( 255,   0,   0 ),           //  100
  };

  private ILcdGLTerrainPaintable fTerrain;
  private ILcdGLLODChooser fGeometryLOD = new TLcdGLGeometryLODChooser();

  private TLcdGLTerrainPaintableFactory fPaintableFactory = new TLcdGLTerrainPaintableFactory();
  private TLcdGLPaintablePainter fPainter = new TLcdGLTerrainPainter();

  public TerrainFactory() {
    fPainter.setPaintableFactory( fPaintableFactory );
  }

  private void configurePaintableFactory( final ILcdInputStreamFactory aInputStreamFactory ) {

    // Set the default quality of the geometry.
    fGeometryLOD.setQuality( 1.5 );

    /* Set up the geometry decoder so that we don't use on-the-fly loading, and
       retain a reference to the terrain paintable. */
    fPaintableFactory.setGeometryDecoder( new TLcdGLTerrainGeometryDecoder() {

      public ILcdGLTerrainPaintable decodeTerrain(
          ILcdMultiLevelTiledTerrain aTerrain,
          String aSourceName,
          ILcdGLView aGLView
      ) throws IOException {
        // Force all-in-memory terrain tiles.
        setTerrainTileFactory(
            new TLcdGLAllInMemoryTerrainTileFactory(
                aTerrain, aSourceName, aInputStreamFactory
            )
        );
        // Retain a reference to the paintable.
        fTerrain = super.decodeTerrain( aTerrain, aSourceName, aGLView );
        // Set up an LOD chooser.
        if ( fTerrain instanceof ILcdGLMultiLevelTileGrid ) {
          ( ( ILcdGLMultiLevelTileGrid ) fTerrain ).setLODChooser( fGeometryLOD );
        }
        if ( fTerrain instanceof TLcdGLTerrainPaintable ) {
          LayerFactory.fAboveGroundPainter.setTerrainPaintable( ( TLcdGLTerrainPaintable ) fTerrain );
        }
        return fTerrain;
      }
    } );

    /**
     * Set up the texture decoder so that we don't use on-the-fly loading.
     */
    fPaintableFactory.setTextureDecoder( new TLcdGLTerrainTextureDecoder() {
      protected ILcdGLTerrainTexture createTexture(
          ILcdMultiLevelTiledTerrain aTerrain,
          String aSourceName,
          int aIndex,
          ILcdGLView aGLView,
          ILcdInputStreamFactory aInputStreamFactory
      ) throws IOException {
        // Force the use of an all-in-memory terrain texture.
        TLcdGLTiledTerrainTexture texture = new TLcdGLTiledTerrainTexture();
        texture.setTargetView( aGLView );
        texture.decode( aSourceName, aInputStreamFactory, aIndex, aTerrain );
        return texture;
      }

      public ILcdGLTerrainTexture decodeTexture(
          ILcdMultiLevelTiledTerrain aTerrain,
          String aSourceName,
          ILcdGLView aGLView
      ) throws IOException {
        // Make sure we use the proper input stream factory.
        setInputStreamFactory( aInputStreamFactory );
        return super.decodeTexture( aTerrain, aSourceName, aGLView );
      }
    } );
  }

  private ILcdModel createTerrainModel(
      String aSourceName,
      ILcdInputStreamFactory aInputStreamFactory
  ) throws IOException {
    // Decode a preprocessed 3D terrain from the specified source.
    TLcdTerrainModelDecoder dec = new TLcdTerrainModelDecoder();
    dec.setInputStreamFactory( aInputStreamFactory );
    return dec.decode( aSourceName );
  }

  private void preprocessTerrain( ILcdGLView aView, ILcdOutputStreamFactory aOutputStreamFactory ) throws IOException {

    TLcdGeoidGeodeticDatumFactory factory = new TLcdGeoidGeodeticDatumFactory();
    ILcdGeodeticDatum datum = factory.createGeodeticDatum( TLcdGeoidGeodeticDatumFactory.EGM96_GRID_BILINEAR );
    ILcdElevationProvider elevationprovider = new MyGeodeticDatumAltitudeProvider( datum );

    // Configure the terrain texture generator:
    TLcdBakedMapTextureGenerator generator = new TLcdBakedMapTextureGenerator();
    generator.setElevationProvider( elevationprovider );
    generator.setColorModel( createColorModel() );

    // Configure and run the preprocessor.
    TLcdTerrainPreprocessor prep = new TLcdTerrainPreprocessor();
    prep.addStatusListener( new MyStatusListener() );
    prep.setOutputStreamFactory( aOutputStreamFactory );
    prep.setXYZWorldReference( new TLcdGridReference( new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical() ) );
    prep.setDestination( "terrain/geoid.trn" );
    prep.setGeometryResolution( 16, 8, 6 );
    prep.setElevationProvider( elevationprovider );
    prep.addTexture( 16, 8, 6, "Texture map", generator );
    try {
      prep.preprocessTerrain();
    }
    catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  public void addTerrainToView( ILcdGLView aView ) throws IOException {

    // Preprocess a 3D terrain and add it to the specified view.
    IOStreamFactory io_stream_factory = new IOStreamFactory();

    preprocessTerrain( aView, io_stream_factory );
    configurePaintableFactory( io_stream_factory );

    // Load the terrain
    ILcdModel terrain_model = createTerrainModel( "terrain/geoid.trn", io_stream_factory );
    // Put it on a layer
    TLcdGLLayer layer = new TLcdGLLayer( terrain_model );
    // Set our painter on the layer
    layer.setPainter( fPainter );
    layer.setLabel( terrain_model.getModelDescriptor().getDisplayName() );
    // Add the layer to the view
    aView.addLayer( layer );

    // Fit the view to the terrain.
    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( aView );
    fit.setLayer( layer );
    fit.fit();
  }

  public ILcdGLTerrainPaintable getTerrain() {
    return fTerrain;
  }

  public ILcdGLLODChooser getGeometryLOD() {
    return fGeometryLOD;
  }

  public static ColorModel createColorModel() {
    return createColorModel( DTED_LEVELS, DTED_COLORS );
  }

  public static ColorModel createColorModel( double[] aLevels, Color[] aColors ) {
    TLcdColorMap color_map = new TLcdColorMap(
        new TLcdInterval( Short.MIN_VALUE, Short.MAX_VALUE ), aLevels, aColors
    );
    return new TLcdIndexColorModel( 16, color_map );
  }

  private class MyStatusListener implements ILcdStatusListener {

    public void statusChanged( TLcdStatusEvent event ) {
      System.out.println( event.getMessage() + " (" + ( int ) ( event.getValue() * 100 ) + "%)" );
    }
  }

  private static class MyGeodeticDatumAltitudeProvider extends ALcdElevationProvider {

    private static final TLcdLonLatBounds WORLD = new TLcdLonLatBounds( -180, -90, 360, 180 );

    private ILcdGeodeticDatum fGeodeticDatum;
    private ILcdModelReference fModelReference;

    public MyGeodeticDatumAltitudeProvider( ILcdGeodeticDatum aGeodeticDatum ) {
      fGeodeticDatum = aGeodeticDatum;
    }

    public double retrieveElevationAt( double aX, double aY ) {
      return fGeodeticDatum.getHeight( aX, aY );
    }

    public ILcdModelReference getModelReference() {
      if ( fModelReference == null ) {
        fModelReference = new TLcdGeodeticReference( fGeodeticDatum );
      }
      return fModelReference;
    }

    public ILcdBounds getBounds() {
      return WORLD;
    }
  }

}
