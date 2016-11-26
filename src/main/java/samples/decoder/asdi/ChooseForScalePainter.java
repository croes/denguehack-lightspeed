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
package samples.decoder.asdi;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

/**
 * Implementation of <code>ILcdGXYPainter</code> that chooses from two painters,
 * based on the scale of the view.
 */
public class ChooseForScalePainter implements ILcdGXYPainter, Cloneable, ILcdGXYPainterProvider {
  private ILcdGXYPainter fZoomedOut;
  private ILcdGXYPainter fZoomedIn;
  private double fSwitchScale;

  public ChooseForScalePainter(ILcdGXYPainter aZoomedOut, ILcdGXYPainter aZoomedIn, double aSwitchScale) {
    fZoomedOut = aZoomedOut;
    fZoomedIn = aZoomedIn;
    fSwitchScale = aSwitchScale;
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    setObject(aObject);
    return this;
  }

  public void setObject(Object aObject) {
    fZoomedOut.setObject(aObject);
    fZoomedIn.setObject(aObject);
  }

  public Object getObject() {
    return fZoomedOut.getObject();
  }

  private ILcdGXYPainter choosePainter(ILcdGXYContext aGXYContext) {
    if (aGXYContext.getGXYView().getScale() < fSwitchScale) {
      return fZoomedOut;
    } else {
      return fZoomedIn;
    }
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
    choosePainter(aContext).paint(aGraphics, aMode, aContext);
  }

  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
    choosePainter(aContext).boundsSFCT(aGraphics, aMode, aContext, aBoundsSFCT);
  }

  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
    return choosePainter(aContext).isTouched(aGraphics, aMode, aContext);
  }

  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aContext, Point aPointSFCT) throws TLcdNoBoundsException {
    choosePainter(aContext).anchorPointSFCT(aGraphics, aMode, aContext, aPointSFCT);
  }

  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aContext) {
    return choosePainter(aContext).supportSnap(aGraphics, aContext);
  }

  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aContext) {
    return choosePainter(aContext).snapTarget(aGraphics, aContext);
  }

  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
    return choosePainter(aContext).getCursor(aGraphics, aMode, aContext);
  }

  public String getDisplayName() {
    return fZoomedOut.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    //No properties that can change, so no need to fire events to these listeners
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
  }

  public Object clone() {
    try {
      ChooseForScalePainter clone = (ChooseForScalePainter) super.clone();
      clone.fZoomedOut = (ILcdGXYPainter) fZoomedOut.clone();
      clone.fZoomedIn = (ILcdGXYPainter) fZoomedIn.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
