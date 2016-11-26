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
import com.luciad.view.opengl.style.TLcdGLOutlineStyle;

import java.awt.Color;
import java.util.WeakHashMap;


/**
 * An outline style provider for the 3D view that selects a basic color outline style for an object.
 */
public class BasicOutlineStyleProvider implements ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> {

  private static final float ALPHA = 0.25f;
  private static final TLcdGLOutlineStyle[] DEFAULT_STYLES = new TLcdGLOutlineStyle[] {
      new TLcdGLOutlineStyle( Color.blue, ALPHA),
      new TLcdGLOutlineStyle(Color.cyan, ALPHA),
      new TLcdGLOutlineStyle(Color.green, ALPHA),
      new TLcdGLOutlineStyle(Color.magenta, ALPHA),
      new TLcdGLOutlineStyle(Color.orange, ALPHA),
      new TLcdGLOutlineStyle(Color.pink, ALPHA),
      new TLcdGLOutlineStyle(Color.red, ALPHA),
      new TLcdGLOutlineStyle(Color.yellow, ALPHA)
  };

  private static final TLcdGLOutlineStyle SELECTION_STYLE = new TLcdGLOutlineStyle(Color.white);

  private int fStyleIndex = 0;
  private WeakHashMap<Object, TLcdGLOutlineStyle> fObject2Style = new WeakHashMap<Object, TLcdGLOutlineStyle> ();


  public TLcdGLOutlineStyle getStyle( Object aObject, ILcdGLPaintMode aPaintMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aContext ) {
    TLcdGLOutlineStyle style;
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

