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
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.style.ILcdGLObjectStyleProvider;
import com.luciad.view.opengl.style.ILcdGLStyleMode;
import com.luciad.view.opengl.style.TLcdGLFillStyle;

import java.awt.Color;
import java.util.WeakHashMap;

/**
 * A fill style provider for the 3D view that selects a basic color fill style for an object.
 */
public class BasicFillStyleProvider implements ILcdGLObjectStyleProvider<TLcdGLFillStyle> {

  private static final float ALPHA = 0.25f;
  private static final TLcdGLFillStyle[] DEFAULT_STYLES = new TLcdGLFillStyle[] {
      new TLcdGLFillStyle(Color.blue, ALPHA),
      new TLcdGLFillStyle(Color.cyan, ALPHA),
      new TLcdGLFillStyle(Color.green, ALPHA),
      new TLcdGLFillStyle(Color.magenta, ALPHA),
      new TLcdGLFillStyle(Color.orange, ALPHA),
      new TLcdGLFillStyle(Color.pink, ALPHA),
      new TLcdGLFillStyle(Color.red, ALPHA),
      new TLcdGLFillStyle(Color.yellow, ALPHA)
  };

  private static final TLcdGLFillStyle SELECTION_STYLE = new TLcdGLFillStyle(Color.white);

  private int fStyleIndex = 0;
  private WeakHashMap<Object, TLcdGLFillStyle> fObject2Style = new WeakHashMap<Object, TLcdGLFillStyle> ();

  
  public TLcdGLFillStyle getStyle( Object aObject, ILcdGLPaintMode aPaintMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aContext ) {
    TLcdGLFillStyle style;
    if ( !aPaintMode.isPaintAsSelected() ) {
      style = fObject2Style.get( aObject );
      if ( style == null ) {
        style = DEFAULT_STYLES[ fStyleIndex ];
        fObject2Style.put( aObject, style );
        fStyleIndex = ( fStyleIndex + 1 ) % DEFAULT_STYLES.length;
      }
    } else {
      style = SELECTION_STYLE;
    }
    return style;
  }


}
