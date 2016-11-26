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
package samples.gxy.grid.multilevel;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

/**
 * A <code>ILcdGXYPainter</code> implementation that renders <code>ILcdMultilevelCoordinate</code>
 * objects as a <code>ILcdBounds</code> on a <code>ILcdGXYView</code>.
 */
public class MultilevelGridElementPainter extends ALcdGXYPainter
    implements ILcdGXYPainter, ILcdGXYPainterProvider {

  public static final int FILLED = TLcdGXYBoundsPainter.FILLED;
  public static final int OUTLINED_FILLED = TLcdGXYBoundsPainter.OUTLINED_FILLED;
  public static final int OUTLINED = TLcdGXYBoundsPainter.OUTLINED;

  private TLcdGXYBoundsPainter fPainterToDelegateTo = new TLcdGXYBoundsPainter();
  private ILcdMultilevelGrid fMultilevelGrid;
  private Object fCurrentObject;
  private ILcd2DEditableBounds fTempEditableBounds = new TLcdXYBounds();
  private ILcdGXYPainterStyle fFillStyle, fLineStyle;
  private int fMode;

  public MultilevelGridElementPainter(ILcdMultilevelGrid aMultilevelGrid) {
    fMultilevelGrid = aMultilevelGrid;
    fPainterToDelegateTo.setLineStyle(new MyEmptyPainterStyle());
    fPainterToDelegateTo.setFillStyle(new MyEmptyPainterStyle());
  }

  /**
   * Sets the object to render. Only <code>ILcdMultilevelCoordinate</code> implementations are rendered.
   * @param aObject the <code>ILcdMultilevelCoordinate</code> to render. The coordinate should be defined in the grid set
   * to the painter.
   * @throws IllegalArgumentException when an object is passed that does not implement <code>ILcdMultilevelCoordinate</code>,
   * or when the coordinate passed does not fit in the grid set to the painter.
   */
  public void setObject(Object aObject) {
    if (aObject instanceof ILcdMultilevelGridCoordinate) {
      try {
        TLcdMultilevelGridUtil.multilevelCoordinateBoundsSFCT(
            (ILcdMultilevelGridCoordinate) aObject,
            fMultilevelGrid, fTempEditableBounds);
      } catch (TLcdNoBoundsException e) {
        throw new IllegalArgumentException("The multilevel coordinate is not valid for the grid set to this painter.");
      }
      fCurrentObject = aObject;
      fPainterToDelegateTo.setObject(fTempEditableBounds);
    } else {
      throw new IllegalArgumentException("Multilevel grid element painter can only handle ILcdMultilevelCoordinate implementations.");
    }
  }

  public Object getObject() {
    return fCurrentObject;
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (fMode == FILLED || fMode == OUTLINED_FILLED) {
      if (fFillStyle != null) {
        fFillStyle.setupGraphics(aGraphics, fCurrentObject, aMode, aGXYContext);
      }
    }

    if (fMode == OUTLINED || fMode == OUTLINED_FILLED) {
      if (fLineStyle != null) {
        fLineStyle.setupGraphics(aGraphics, fCurrentObject, aMode, aGXYContext);
      }
    }
    fPainterToDelegateTo.paint(aGraphics, aMode, aGXYContext);
    if ((aMode & ILcdGXYPainter.SELECTED) != 0) {
      if (fLineStyle != null) {
        fLineStyle.setupGraphics(aGraphics, fCurrentObject, aMode, aGXYContext);
      }
      fPainterToDelegateTo.setMode(TLcdGXYBoundsPainter.OUTLINED);
      fPainterToDelegateTo.paint(aGraphics, aMode, aGXYContext);
      fPainterToDelegateTo.setMode(fMode);
    }
  }

  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds a2DEditableBounds) throws TLcdNoBoundsException {
    fPainterToDelegateTo.boundsSFCT(aGraphics, aMode, aGXYContext, a2DEditableBounds);
  }

  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fPainterToDelegateTo.isTouched(aGraphics, aMode, aGXYContext);
  }

  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPoint) throws TLcdNoBoundsException {
    fPainterToDelegateTo.anchorPointSFCT(aGraphics, aMode, aGXYContext, aPoint);
  }

  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fPainterToDelegateTo.supportSnap(aGraphics, aGXYContext);
  }

  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fPainterToDelegateTo.snapTarget(aGraphics, aGXYContext);
  }

  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fPainterToDelegateTo.getCursor(aGraphics, aMode, aGXYContext);
  }

  public String getDisplayName() {
    return fPainterToDelegateTo.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPainterToDelegateTo.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPainterToDelegateTo.removePropertyChangeListener(aPropertyChangeListener);
  }

  public Object clone() {
    Object clone = super.clone();
    MultilevelGridElementPainter multilevelGridElementPainter = (MultilevelGridElementPainter) clone;
    multilevelGridElementPainter.fPainterToDelegateTo = (TLcdGXYBoundsPainter) fPainterToDelegateTo.clone();
    multilevelGridElementPainter.fTempEditableBounds = (ILcd2DEditableBounds) fTempEditableBounds.clone();
    return multilevelGridElementPainter;
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    if (aObject instanceof ILcdMultilevelGridCoordinate) {
      this.setObject(aObject);
      return this;
    }

    return null;
  }

  public void setMode(int aMode) {
    fMode = aMode;
    fPainterToDelegateTo.setMode(aMode);
  }

  public void setLineStyle(ILcdGXYPainterStyle aGXYPainterStyle) {
    fLineStyle = aGXYPainterStyle;
  }

  public void setFillStyle(ILcdGXYPainterStyle aGXYPainterStyle) {
    fFillStyle = aGXYPainterStyle;
  }

  private static class MyEmptyPainterStyle implements ILcdGXYPainterStyle {
    public void setupGraphics(Graphics graphics, Object object, int i, ILcdGXYContext aGXYContext) {
    }
  }
}
