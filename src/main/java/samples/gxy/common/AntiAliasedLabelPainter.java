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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;

/**
 * Label painter wrapper that performs rendering with anti-aliasing.
 */
public class AntiAliasedLabelPainter implements ILcdGXYLabelPainter2, ILcdGXYLabelPainterProvider {

  private ILcdGXYLabelPainter2 fDelegatePainter;
  private ILcdGXYLabelPainterProvider fDelegatePainterProvider;
  // either fDelegatePainter, or the painter obtained from the provider
  private ILcdGXYLabelPainter2 fActualPainter;

  public AntiAliasedLabelPainter(ILcdGXYLabelPainter2 aPainter) {
    this(aPainter, null);
  }

  public AntiAliasedLabelPainter(ILcdGXYLabelPainterProvider aPainterProvider) {
    this(null, aPainterProvider);
  }

  private AntiAliasedLabelPainter(ILcdGXYLabelPainter2 aPainter, ILcdGXYLabelPainterProvider aPainterProvider) {
    fDelegatePainter = aPainter;
    fActualPainter = fDelegatePainter;
    fDelegatePainterProvider = aPainterProvider;
  }

  public ILcdGXYLabelPainterProvider getDelegatePainterProvider() {
    return fDelegatePainterProvider;
  }

  public ILcdGXYLabelPainter2 getDelegatePainter() {
    return fDelegatePainter;
  }

  /**
   * Always returns this painter itself.
   * @param aObject the object for which a painter is requested
   * @return this label itself
   */
  public ILcdGXYLabelPainter2 getGXYLabelPainter(Object aObject) {
    if (fDelegatePainterProvider != null) {
      fActualPainter = (ILcdGXYLabelPainter2) fDelegatePainterProvider.getGXYLabelPainter(aObject);
    } else {
      fActualPainter = fDelegatePainter;
    }
    if (fActualPainter != null) {
      fActualPainter.setObject(aObject);
      return this;
    }
    return null;
  }

  @Override
  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    // Make sure the labels are anti-aliased
    Graphics2D g2d = (Graphics2D) aGraphics;
    RenderingHints rendering_hints = g2d.getRenderingHints();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    fActualPainter.paintLabel(aGraphics, aMode, aGXYContext);

    // Restore the rendering hints
    g2d.setRenderingHints(rendering_hints);
  }

  @Override
  public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
    return fActualPainter.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
  }

  @Override
  public boolean isLabelTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fActualPainter.isLabelTouched(aGraphics, aMode, aGXYContext);
  }

  @Override
  public void labelAnchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    fActualPainter.labelAnchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
  }

  @Override
  public boolean supportLabelSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fActualPainter.supportLabelSnap(aGraphics, aGXYContext);
  }

  @Override
  public Object labelSnapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fActualPainter.labelSnapTarget(aGraphics, aGXYContext);
  }

  @Override
  public Cursor getLabelCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fActualPainter.getLabelCursor(aGraphics, aMode, aGXYContext);
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

  @Override
  public void setLabelLocation(TLcdLabelLocation aLabelLocation) {
    fActualPainter.setLabelLocation(aLabelLocation);
  }

  @Override
  public void setObject(Object aObject) {
    fActualPainter.setObject(aObject);
  }

  @Override
  public TLcdLabelLocation getLabelLocation() {
    return fActualPainter.getLabelLocation();
  }

  @Override
  public Object clone() {
    try {
      AntiAliasedLabelPainter clone = (AntiAliasedLabelPainter) super.clone();

      // Make deep copies of non-transient fields containing mutable objects.
      if (fDelegatePainter != null) {
        clone.fDelegatePainter = (ILcdGXYLabelPainter2) fDelegatePainter.clone();
      }
      if (fDelegatePainterProvider != null) {
        clone.fDelegatePainterProvider = (ILcdGXYLabelPainterProvider) fDelegatePainterProvider.clone();
      }
      if (fDelegatePainterProvider == null) {
        clone.fActualPainter = clone.fDelegatePainter;
      } else {
        clone.fActualPainter = (ILcdGXYLabelPainter2) clone.fActualPainter.clone();
      }

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setLabelIndex(int aLabelIndex) {
    fActualPainter.setLabelIndex(aLabelIndex);
  }

  @Override
  public int getLabelIndex() {
    return fActualPainter.getLabelIndex();
  }

  @Override
  public int getLabelCount(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fActualPainter.getLabelCount(aGraphics, aGXYContext);
  }

  @Override
  public int getSubLabelIndex() {
    return fActualPainter.getSubLabelIndex();
  }

  @Override
  public void setSubLabelIndex(int aSubLabelIndex) {
    fActualPainter.setSubLabelIndex(aSubLabelIndex);
  }

  @Override
  public int getSubLabelCount(int aLabelIndex) {
    return fActualPainter.getSubLabelCount(aLabelIndex);
  }

  @Override
  public Object getObject() {
    return fActualPainter.getObject();
  }

  @Override
  public void setLocationIndex(int aLocationIndex) {
    fActualPainter.setLocationIndex(aLocationIndex);
  }

  @Override
  public int getLocationIndex() {
    return fActualPainter.getLocationIndex();
  }

  @Override
  public int getPossibleLocationCount(Graphics aGraphics) {
    return fActualPainter.getPossibleLocationCount(aGraphics);
  }
}
