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
package samples.opengl.terrain.viewer;

import com.luciad.format.raster.terrain.*;
import com.luciad.format.raster.terrain.opengl.*;
import com.luciad.format.raster.terrain.opengl.paintable.*;
import com.luciad.format.raster.terrain.opengl.texture.*;
import com.luciad.model.ILcdModel;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter;

import java.io.IOException;

/**
 * A utility class to load preprocessed terrains. Demonstrates the various
 * options for hooking into the API.
 */
class TerrainFactory {

  private ILcdGLTerrainPaintable fTerrain;
  private ILcdGLLODChooser fGeometryLOD = new TLcdGLGeometryLODChooser();

  private ILcdGLTerrainTexture fTexture;
  private TextureLODChooser fTextureLOD = new TextureLODChooser();

  private TLcdGLTerrainPaintableFactory fPaintableFactory = new TLcdGLTerrainPaintableFactory();
  private TLcdGLPaintablePainter fPainter = new TLcdGLTerrainPainter();

  public TerrainFactory() {
    fPainter.setPaintableFactory( fPaintableFactory );

    /* Set up the geometry decoder so that we can retain a reference to the
       terrain and to the LOD chooser. */
    fPaintableFactory.setGeometryDecoder( new TLcdGLTerrainGeometryDecoder() {
      public ILcdGLTerrainPaintable decodeTerrain(
              ILcdMultiLevelTiledTerrain aTerrain,
              String aSourceName,
              ILcdGLView aGLView
      ) throws IOException {
        fTerrain = super.decodeTerrain( aTerrain, aSourceName, aGLView );
        if ( fTerrain instanceof ILcdGLMultiLevelTileGrid ) {
          ( (ILcdGLMultiLevelTileGrid) fTerrain ).setLODChooser( fGeometryLOD );
        }
        return fTerrain;
      }
    } );

    /* Set up the texture decoder so that we install a custom LOD chooser and
       retain a reference to the ILcdGLTerrainTexture. */
    fPaintableFactory.setTextureDecoder( new TLcdGLTerrainTextureDecoder() {

      /**
       * Installs a custom LOD chooser on the given terrain texture. If the
       * texture is a composite, the LOD chooser will be propagated to its
       * subtextures.
       */
      private void setLODChooser( ILcdGLTerrainTexture aTexture ) {
        if ( aTexture instanceof ILcdGLMultiLevelTileGrid ) {
          ( (ILcdGLMultiLevelTileGrid) aTexture ).setLODChooser( fTextureLOD );
        } else if ( aTexture instanceof TLcdGLCompositeTerrainTexture ) {
          TLcdGLCompositeTerrainTexture c = (TLcdGLCompositeTerrainTexture) aTexture;
          for ( int i = 0; i < c.getTextureCount() ; i++ ) {
            setLODChooser( c.getTexture( i ) );
          }
          /* The composite may only be populated later, so attach a listener
             that will install our LOD chooser on any new textures. */
          c.addTextureListener( new ILcdGLCompositeTerrainTextureChangeListener() {
            public void textureAdded( int aIndex, ILcdGLTerrainTexture aTexture ) {
              setLODChooser( aTexture );
            }

            public void textureToggled( int aIndex, ILcdGLTerrainTexture aTexture, boolean aEnabled ) {
            }

            public void textureModeChanged( int aIndex, ILcdGLTerrainTexture aTexture, int aMode ) {
            }
          } );
        }
      }

      public ILcdGLTerrainTexture decodeTexture(
              ILcdMultiLevelTiledTerrain aTerrain,
              String aSourceName,
              ILcdGLView aGLView
      ) throws IOException {
        fTexture = super.decodeTexture( aTerrain, aSourceName, aGLView );
        if ( fTexture instanceof TLcdGLCompositeTerrainTexture ) {
          fTextureLOD.setComposite( (TLcdGLCompositeTerrainTexture) fTexture );
        }
        setLODChooser( fTexture );
        return fTexture;
      }
    } );
  }

  private ILcdModel createTerrainModel( String aSourceName ) throws IOException {
    // Load the preprocessed terrain model from the specified source.
    TLcdTerrainModelDecoder dec = new TLcdTerrainModelDecoder();
    return dec.decode( aSourceName );
  }

  public void addTerrainToView( ILcdGLView aView, String aSourceName ) throws IOException {
    // Load the terrain
    ILcdModel terrain_model = createTerrainModel( aSourceName );
    // Put it on a layer
    TLcdGLLayer layer = new TLcdGLLayer( terrain_model );
    // Set our painter on the layer
    layer.setPainter( fPainter );
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

  public ILcdGLTerrainTexture getTexture() {
    return fTexture;
  }

  public ILcdGLLODChooser getTextureLOD() {
    return fTextureLOD;
  }
}
