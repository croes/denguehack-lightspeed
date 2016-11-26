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
package samples.opengl.selectionmodel;

import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.painter.*;

/**
 * This ILcdGLLabelPainter implementation provides support for labeling all objects
 * or just the selected objects. This is controlled through the property selectionOnlyLabeled.
 * The label painter delegates the actual painting to another label painter.
 * The default is to only label selected objects.
 */
class SelectionOnlyLabelPainter implements ILcdGLLabelPainter {

  private ILcdGLLabelPainter fDelegateLabelPainter;
  private boolean fSelectionOnlyLabeled = true;

  public SelectionOnlyLabelPainter( ILcdGLLabelPainter aDelegateLabelPainter ) {
    fDelegateLabelPainter = aDelegateLabelPainter;
  }

  public ILcdGLLabelPainter getDelegateLabelPainter() {
    return fDelegateLabelPainter;
  }

  public void setDelegateLabelPainter( ILcdGLLabelPainter aDelegateLabelPainter ) {
    fDelegateLabelPainter = aDelegateLabelPainter;
  }

  public boolean isSelectionOnlyLabeled() {
    return fSelectionOnlyLabeled;
  }

  public void setSelectionOnlyLabeled( boolean aSelectionOnlyLabeled ) {
    fSelectionOnlyLabeled = aSelectionOnlyLabeled;
  }

  /**
   * Paints a label when isSelectionOnlyLabeled is false (label all objects) or when
   * a selected object is being labeled.
   *
   * @param g
   * @param o
   * @param aMode
   * @param aContext
   */
  public void paintLabel( ILcdGLDrawable g, Object o, ILcdGLPaintMode aMode, ILcdGLContext aContext ) {
    if ( ( !isSelectionOnlyLabeled() ) || ( aMode.isPaintAsSelected() && aMode.isPaintingLabels() ) ) {
      fDelegateLabelPainter.paintLabel( g, o, aMode, aContext );
    }
  }
}
