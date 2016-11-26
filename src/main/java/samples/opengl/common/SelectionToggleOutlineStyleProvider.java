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


/**
 * A style provider that chooses from one of two user-supplied style providers depending
 * on whether or not objects are being painted in selection mode.
 */
public class SelectionToggleOutlineStyleProvider implements ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> {

  private final ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> fDefaultStyleProvider;
  private final ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> fSelectionStyleProvider;

  public SelectionToggleOutlineStyleProvider( ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> aDefaultStyleProvider, ILcdGLObjectStyleProvider<TLcdGLOutlineStyle> aSelectionStyleProvider ) {
    super();
    fDefaultStyleProvider = aDefaultStyleProvider;
    fSelectionStyleProvider = aSelectionStyleProvider;
  }

  public TLcdGLOutlineStyle getStyle( Object aObject, ILcdGLPaintMode aPaintMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aContext ) {
    if (aPaintMode.isPaintAsSelected()) {
      return fSelectionStyleProvider.getStyle( aObject, aPaintMode, aStyleMode, aContext );
    }
    else {
      return fDefaultStyleProvider.getStyle( aObject, aPaintMode, aStyleMode, aContext );
    }
  }
}