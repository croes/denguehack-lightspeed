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

import com.luciad.format.raster.terrain.opengl.*;
import com.luciad.format.raster.terrain.opengl.texture.*;
import com.luciad.view.opengl.ILcdGLView;

/**
 * A custom implementation of ILcdGLLODChooser. This LOD chooser can check if
 * individual textures in a TLcdGLCompositeTerrainTexture are enabled or not,
 * and drop the disabled ones to a low detail level. This class extends the
 * default implementation, TLcdGLTextureLODChooser, and delegates to it for
 * textures that are enabled.
 */
class TextureLODChooser extends TLcdGLTextureLODChooser {

  private TLcdGLCompositeTerrainTexture fComposite = null;

  public TextureLODChooser() {
    super();
  }

  protected void handleTile( ILcdGLView aGLView, ILcdGLMultiLevelTileGrid aGrid, ILcdGLMultiLevelTile aTile ) {
    if ( fComposite == null ) {
      super.handleTile( aGLView, aGrid, aTile );
    } else {
      // Check if aGrid is in the composite and if it's enabled.
      boolean isEnabled = false;

      for ( int t = 0; t < fComposite.getTextureCount() ; t++ ) {
        ILcdGLTerrainTexture texture = fComposite.getTexture( t );
        if ( texture == aGrid ) {
          isEnabled = fComposite.isTextureEnabled( t );
          break;
        }
      }
      // If it's enabled, proceed as usual.
      if ( isEnabled ) {
        super.handleTile( aGLView, aGrid, aTile );
      }
      /* Otherwise, drop the tile to a lower LOD. Note that we don't use the
         absolute lowest LOD, because doing so results in extremely poor visual
         quality for tiles that are viewed up close. */
      else {
        aTile.setLevel( aTile.getLevelCount() - 3 );
      }
    }
  }

  public void setComposite( TLcdGLCompositeTerrainTexture aCompositeTerrainTexture ) {
    fComposite = aCompositeTerrainTexture;
  }
}
