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
package samples.opengl.common;

import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.*;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.style.*;

import java.awt.*;
import java.util.WeakHashMap;

/**
 * A painter style for the 3D view that selects a basic color for each object for which it is
 * set up. Once set up for a given object, it will always use the same color for this object.
 */
public class BasicColorPainterStyle implements ILcdGLStyle {
  private static final float[] SELECTED_COLOR = Color.white.getRGBColorComponents( null );
  private static final float[][] DEFAULT_COLOR_LIST = new float[][] {
          Color.blue.getRGBColorComponents( null ),
          Color.cyan.getRGBColorComponents( null ),
          Color.green.getRGBColorComponents( null ),
          Color.magenta.getRGBColorComponents( null ),
          Color.orange.getRGBColorComponents( null ),
          Color.pink.getRGBColorComponents( null ),
          Color.red.getRGBColorComponents( null ),
          Color.yellow.getRGBColorComponents( null )
  };

  private int fColorIndex = 0;
  private WeakHashMap fDefaultColors = new WeakHashMap();
  private float[] old_color = new float[4];
  private float fAlpha = .25f;

  public void setUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
    ILcdGL gl = aGLDrawable.getGL();

    // Here we store the current color. During the cleanup phase of this style we will restore this color.
    gl.glGetFloatv( ILcdGL.GL_CURRENT_COLOR, old_color );

    float[] rgb = null;
    if ( !aMode.isPaintAsSelected() ) {
      rgb = ( (float[]) fDefaultColors.get( aObject ) );
      if ( rgb == null ) {
        rgb = DEFAULT_COLOR_LIST[ fColorIndex ];
        fDefaultColors.put( aObject, rgb );
        fColorIndex = ( fColorIndex + 1 ) % DEFAULT_COLOR_LIST.length;
      }
    } else {
      rgb = SELECTED_COLOR;
    }

    gl.glColor4f( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ], fAlpha );
  }

  public void cleanUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
    ILcdGL gl = aGLDrawable.getGL();

    // Here we restore the old color.
    gl.glColor4fv( old_color );
  }

  public float getAlpha() {
    return fAlpha;
  }

  public void setAlpha( float aAlpha ) {
    fAlpha = aAlpha;
  }
}
