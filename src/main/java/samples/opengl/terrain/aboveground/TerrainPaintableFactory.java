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
package samples.opengl.terrain.aboveground;

import com.luciad.format.raster.terrain.opengl.TLcdGLAboveGroundPointlistPainter;
import com.luciad.format.raster.terrain.opengl.paintable.*;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintable.ILcdGLPaintable;
import com.luciad.view.opengl.paintablefactory.TLcdGLPaintableFactoryMode;

/**
 * A terrain paintable factory that passes created terrain paintables on
 * to a TLcdGLAboveGroundPolylinePainter instance.
 */
class TerrainPaintableFactory extends TLcdGLTerrainPaintableFactory {

  private TLcdGLAboveGroundPointlistPainter fPainter;

  public TerrainPaintableFactory( TLcdGLAboveGroundPointlistPainter aPainter ) {
    fPainter = aPainter;
  }

  public ILcdGLPaintable createPaintable( ILcdGLDrawable aGLDrawable, Object aObject, TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext ) {
    TLcdGLTerrainPaintable paintable = (TLcdGLTerrainPaintable) super.createPaintable( aGLDrawable, aObject, aMode, aContext );

    fPainter.setTerrainPaintable( paintable );

    return paintable;
  }
}
