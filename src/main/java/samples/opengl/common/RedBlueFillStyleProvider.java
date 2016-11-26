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


/**
 * A fill style provider for the 3D view that returns a red or a blue color fill style for an object,
 * depending on the paint mode (selected or not).
 * When objects are selected, red is used. Otherwise blue is used.
 */
public class RedBlueFillStyleProvider implements ILcdGLObjectStyleProvider<TLcdGLFillStyle> {

  private TLcdGLFillStyle fRedColorStyle = new TLcdGLFillStyle( Color.red );
  private TLcdGLFillStyle fBlueColorStyle = new TLcdGLFillStyle( Color.blue );


  public TLcdGLFillStyle getStyle( Object aObject, ILcdGLPaintMode aPaintMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aContext ) {
    return aPaintMode.isPaintAsSelected() ? fRedColorStyle : fBlueColorStyle;
  }
}
