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
package samples.gxy.editmodes;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

/**
 * Wraps and switches between {@link ILcdGXYPainter ILcdGXYPainter}s according
 * to the {@link MultiModeController.Mode Mode} of a {@link MultiModeController}.
 */
public class MultiModePainterWrapper implements ILcdGXYPainter, ILcdGXYPainterProvider {

  private MultiModeController fController;
  private Object fObject;
  private EnumMap<MultiModeController.Mode, ILcdGXYPainter> fPainters;

  /**
   * Creates a new multi-mode painter wrapper.
   *
   * @param aController the multi-mode controller whose mode this wrapper needs to select the
   *                    painter
   * @param aPainters   the painters to be wrapped and the mode they need to be used in
   */
  public MultiModePainterWrapper(MultiModeController aController, EnumMap<MultiModeController.Mode, ILcdGXYPainter> aPainters) {
    fController = aController;
    fPainters = aPainters;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    retrieveDelegatePainter().addPropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    retrieveDelegatePainter().anchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
  }

  @Override
  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
    retrieveDelegatePainter().boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
  }

  @Override
  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return retrieveDelegatePainter().getCursor(aGraphics, aMode, aGXYContext);
  }

  @Override
  public String getDisplayName() {
    return retrieveDelegatePainter().getDisplayName();
  }

  @Override
  public Object getObject() {
    return fObject;
  }

  @Override
  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return retrieveDelegatePainter().isTouched(aGraphics, aMode, aGXYContext);
  }

  @Override
  public void paint(Graphics aGraphics, int aRenderMode, ILcdGXYContext aGXYContext) {
    retrieveDelegatePainter().paint(aGraphics, aRenderMode, aGXYContext);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    retrieveDelegatePainter().removePropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void setObject(Object aObject) {
    fObject = aObject;

    for (ILcdGXYPainter painter : fPainters.values()) {
      painter.setObject(aObject);
    }
  }

  @Override
  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return retrieveDelegatePainter().snapTarget(aGraphics, aGXYContext);
  }

  @Override
  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return retrieveDelegatePainter().supportSnap(aGraphics, aGXYContext);
  }

  @Override
  public Object clone() {
    try {
      MultiModePainterWrapper clone = (MultiModePainterWrapper) super.clone();
      clone.fPainters = fPainters.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("super.clone should be supported but it isn't.", e);
    }
  }

  @Override
  public ILcdGXYPainter getGXYPainter(Object arg0) {
    setObject(arg0);
    return this;
  }

  // selects the painter according to the mode of the multi-mode edit controller.
  private ILcdGXYPainter retrieveDelegatePainter() {
    ILcdGXYPainter painter = fPainters.get(fController.getMode());

    if (painter == null && fController.getMode() != MultiModeController.Mode.DEFAULT) {
      return fPainters.get(MultiModeController.Mode.DEFAULT);
    }

    return painter;
  }
}
