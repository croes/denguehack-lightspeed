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
package samples.gxy.common;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

/**
 * Painter wrapper that performs anti-aliased rendering.
 */
public class AntiAliasedPainter implements ILcdGXYPainter, ILcdGXYPainterProvider {

  private ILcdGXYPainter fDelegatePainter;
  private ILcdGXYPainterProvider fDelegatePainterProvider;
  // either fDelegatePainter, or the painter obtained from the provider
  private ILcdGXYPainter fActualPainter;

  public AntiAliasedPainter(ILcdGXYPainter aPainter) {
    this(aPainter, null);
  }

  public AntiAliasedPainter(ILcdGXYPainterProvider aPainterProvider) {
    this(null, aPainterProvider);
  }

  private AntiAliasedPainter(ILcdGXYPainter aPainter, ILcdGXYPainterProvider aPainterProvider) {
    fDelegatePainter = aPainter;
    fActualPainter = fDelegatePainter;
    fDelegatePainterProvider = aPainterProvider;
  }

  public ILcdGXYPainterProvider getDelegatePainterProvider() {
    return fDelegatePainterProvider;
  }

  public ILcdGXYPainter getDelegatePainter() {
    return fDelegatePainter;
  }

  /**
   * Always returns this painter itself.
   * @param aObject the object for which a painter is requested
   * @return this label itself
   */
  public ILcdGXYPainter getGXYPainter(Object aObject) {
    if (fDelegatePainterProvider != null) {
      fActualPainter = fDelegatePainterProvider.getGXYPainter(aObject);
    } else {
      fActualPainter = fDelegatePainter;
    }
    fActualPainter.setObject(aObject);
    return this;
  }

  @Override
  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    Graphics2D g2d = (Graphics2D) aGraphics;
    RenderingHints hintsToRestore = g2d.getRenderingHints();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    fActualPainter.paint(aGraphics, aMode, aGXYContext);
    g2d.setRenderingHints(hintsToRestore);
  }

  @Override
  public void setObject(Object aObject) {
    fActualPainter.setObject(aObject);
  }

  @Override
  public Object getObject() {
    return fActualPainter.getObject();
  }

  @Override
  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
    fActualPainter.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
  }

  @Override
  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fActualPainter.isTouched(aGraphics, aMode, aGXYContext);
  }

  @Override
  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    fActualPainter.anchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
  }

  @Override
  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fActualPainter.supportSnap(aGraphics, aGXYContext);
  }

  @Override
  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fActualPainter.snapTarget(aGraphics, aGXYContext);
  }

  @Override
  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fActualPainter.getCursor(aGraphics, aMode, aGXYContext);
  }

  @Override
  public String getDisplayName() {
    return fActualPainter.getDisplayName();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fActualPainter.addPropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fActualPainter.removePropertyChangeListener(aPropertyChangeListener);
  }

  public Object clone() {
    try {
      AntiAliasedPainter clone = (AntiAliasedPainter) super.clone();

      // Make deep copies of non-transient fields containing mutable objects.
      if (fDelegatePainter != null) {
        clone.fDelegatePainter = (ILcdGXYPainter) fDelegatePainter.clone();
      }
      if (fDelegatePainterProvider != null) {
        clone.fDelegatePainterProvider = (ILcdGXYPainterProvider) fDelegatePainterProvider.clone();
      }
      if (fDelegatePainterProvider == null) {
        clone.fActualPainter = clone.fDelegatePainter;
      } else {
        clone.fActualPainter = (ILcdGXYPainter) clone.fActualPainter.clone();
      }

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
