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
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.style.*;

/**
 * An ILcdGLStyle that chooses from one of two user-supplied styles depending
 * on whether or not objects are being painted in selection mode.
 */
public class SelectionTogglePainterStyle implements ILcdGLStyle {
  private ILcdGLStyle fDefaultStyle;
  private ILcdGLStyle fSelectionStyle;

  /**
   * Creates a new SelectionTogglePainterStyle for the two given input styles.
   * @param aDefaultStyle the style to be used for the default painting mode
   * @param aSelectionStyle the style to be used for selection painting mode
   */
  public SelectionTogglePainterStyle(ILcdGLStyle aDefaultStyle, ILcdGLStyle aSelectionStyle) {
    super();
    fDefaultStyle = aDefaultStyle;
    fSelectionStyle = aSelectionStyle;
  }

  public void setUp(ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext) {
    if (aMode.isPaintAsSelected()) fSelectionStyle.setUp(aGLDrawable, aObject, aMode, aStyleMode, aGLContext);
    else fDefaultStyle.setUp(aGLDrawable, aObject, aMode, aStyleMode, aGLContext);
  }

  public void cleanUp(ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext) {
    if (aMode.isPaintAsSelected()) fSelectionStyle.cleanUp(aGLDrawable, aObject, aMode, aStyleMode, aGLContext);
    else fDefaultStyle.cleanUp(aGLDrawable, aObject, aMode, aStyleMode, aGLContext);
  }
}
