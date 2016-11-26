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
package samples.opengl.terrain.onthefly;

import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.terrain.*;
import com.luciad.format.raster.terrain.opengl.*;
import com.luciad.format.raster.terrain.opengl.paintable.*;
import com.luciad.format.raster.terrain.opengl.texture.*;
import com.luciad.format.raster.terrain.preprocessor.*;
import com.luciad.io.*;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.util.*;
import samples.common.serviceregistry.ServiceRegistry;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter;

import java.io.IOException;

/**
 * A utility class that preprocesses a terrain in memory and loads it into an
 * ILcdGLView.
 */
class TerrainFactory {

  private ILcdGLTerrainPaintable fTerrain;
  private ILcdGLLODChooser fGeometryLOD = new TLcdGLGeometryLODChooser();

  private TLcdGLOffscreenGXYViewTerrainTexture fTexture;
  private TLcdGLOffScreenGXYViewTextureDecoder fTextureDecoder;

  private TLcdGLTerrainPaintableFactory fPaintableFactory = new TLcdGLTerrainPaintableFactory();
  private TLcdGLPaintablePainter fPainter = new TLcdGLTerrainPainter();

  private final String fGeometrySource = "Data/Dted/Alps/dmed";
  private final String fTextureSource = "Data/Dted/Alps/dmed";

  public TerrainFactory() {
    fPainter.setPaintableFactory( fPaintableFactory );
  }

  private void configurePaintableFactory( final ILcdGLView aView, final ILcdInputStreamFactory aInputStreamFactory, final ServiceRegistry aServiceRegistry ) {

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
          ( (ILcdGLMultiLevelTileGrid) fTerrain ).setLODChooser( fGeometryLOD );
        }

        return fTerrain;
      }
    } );

    /**
     * Set up the texture decoder so that we use on-the-fly mapping.
     */
    fTextureDecoder = new TLcdGLOffScreenGXYViewTextureDecoder() {

      private TLcdCompositeModelDecoder fModelDecoder = new TLcdCompositeModelDecoder( aServiceRegistry.query( ILcdModelDecoder.class ) );
      private TLcdCompositeGXYLayerFactory fLayerFactory = new TLcdCompositeGXYLayerFactory( aServiceRegistry.query( ILcdGXYLayerFactory.class ) );

      public ILcdGLTerrainTexture decodeTexture( ILcdMultiLevelTiledTerrain aTerrain, String aSourceName, ILcdGLView aGLView ) {
        fTexture = (TLcdGLOffscreenGXYViewTerrainTexture) super.decodeTexture( aTerrain, aSourceName, aGLView );
        ILcdGXYLayer layer = null;
        try {
          layer = fLayerFactory.createGXYLayer( fModelDecoder.decode( fTextureSource ) );
          fTexture.addGXYLayer( layer );
        } catch ( IOException e ) {
          e.printStackTrace();
        }
        return fTexture;
      }
    };
    fTextureDecoder.setTextureResolution( 10, 6, 9 );
    fTextureDecoder.setDisplayName( "Texture" );

    fPaintableFactory.setTextureDecoder( fTextureDecoder );
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

    // Load the elevation model:
    TLcdDMEDModelDecoder decoder = new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance() );
    ILcdModel dmed = decoder.decode( fGeometrySource );
    ILcdElevationProvider elevationprovider = new TLcdInterpolatingRasterElevationProvider( dmed );

    // Configure and run the preprocessor.
    TLcdTerrainPreprocessor prep = new TLcdTerrainPreprocessor();
    prep.addStatusListener( new MyStatusListener() );
    prep.setOutputStreamFactory( aOutputStreamFactory );
    prep.setXYZWorldReference( aView.getXYZWorldReference() );
    prep.setDestination( "model.trn" );
    prep.setGeometryResolution( 10, 6, 6 );
    prep.setElevationProvider( elevationprovider );
    try {
      prep.preprocessTerrain();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  public void addTerrainToView( ILcdGLView aView, ServiceRegistry aServiceRegistry ) throws IOException {

    // Preprocess a 3D terrain and add it to the specified view.
    IOStreamFactory io_stream_factory = new IOStreamFactory();

    preprocessTerrain( aView, io_stream_factory );
    configurePaintableFactory( aView, io_stream_factory, aServiceRegistry );

    // Load the terrain
    ILcdModel terrain_model = createTerrainModel( "model.trn", io_stream_factory );
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

  public TLcdGLOffScreenGXYViewTextureDecoder getTextureDecoder() {
    return fTextureDecoder;
  }

  public ILcdGLLODChooser getTextureLOD() {
    return fTexture.getLODChooser();
  }

  public TLcdGLOffscreenGXYViewTerrainTexture getTexture() {
    return fTexture;
  }

  private class MyStatusListener implements ILcdStatusListener {

    public void statusChanged( TLcdStatusEvent event ) {
      System.out.println( event.getMessage() + " (" + (int) ( event.getValue() * 100 ) + "%)" );
    }
  }
}
