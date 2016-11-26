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
package samples.gxy.statisticalPainter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

/**
 * Implementation of a painter for displaying the change in population density using colored icons of different sizes.
 * In this sample, the painter is only suitable for these particular properties.
 */
class PopulationChangePainter implements ILcdGXYPainter {

  static final Color POSITIVE_COLOR = new Color(20, 235, 20);
  static final Color NEGATIVE_COLOR = Color.red;

  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();

  public PopulationChangePainter() {
  }

  public void setObject(Object aObject) {
    fIconPainter.setObject(calculateIconPoint((ILcdShapeList) aObject));
    fIconPainter.setIcon(calculateIcon((ILcdDataObject) aObject));
  }

  public Object getObject() {
    return fIconPainter.getObject();
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    fIconPainter.paint(aGraphics, aMode, aGXYContext);
  }

  private ILcdIcon calculateIcon(ILcdDataObject aDataObject) {
    int size = PopulationUtil.getPopulationChange(
        aDataObject);
    Color color;
    if (size >= 0) {
      color = POSITIVE_COLOR;
    } else {
      size = -size;
      color = NEGATIVE_COLOR;
    }
    return new TLcdSymbol(TLcdSymbol.FILLED_RECT, size, color, color);
  }

  private ILcdPoint calculateIconPoint(ILcdShapeList aShapeListWithPolygon) {
    ILcdComplexPolygon polygon = (ILcdComplexPolygon) aShapeListWithPolygon.getShape(0);
    ArrayList<ILcdPolygon> list = new ArrayList<ILcdPolygon>();
    for (int i = 1; i < polygon.getPolygonCount(); i++) {
      list.add(polygon.getPolygon(i));
    }
    TLcdXYPoint insidePointSFCT = new TLcdXYPoint();
    TLcdCartesian.computeInsidePoint(polygon.getPolygon(0), list, insidePointSFCT);
    return insidePointSFCT;
  }

  public void boundsSFCT(Graphics aGraphics,
                         int aMode,
                         ILcdGXYContext aGXYContext,
                         ILcd2DEditableBounds aBoundsSFCT)
      throws TLcdNoBoundsException {
    fIconPainter.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
  }

  public boolean isTouched(Graphics aGraphics,
                           int aMode,
                           ILcdGXYContext aGXYContext) {
    return fIconPainter.isTouched(aGraphics, aMode, aGXYContext);
  }

  public void anchorPointSFCT(Graphics aGraphics,
                              int aMode,
                              ILcdGXYContext aGXYContext,
                              Point aPointSFCT)
      throws TLcdNoBoundsException {
    fIconPainter.anchorPointSFCT(aGraphics,
                                 aMode,
                                 aGXYContext,
                                 aPointSFCT);
  }

  public boolean supportSnap(Graphics aGraphics,
                             ILcdGXYContext aGXYContext) {
    return fIconPainter.supportSnap(aGraphics, aGXYContext);
  }

  public Object snapTarget(Graphics aGraphics,
                           ILcdGXYContext aGXYContext) {
    return fIconPainter.snapTarget(aGraphics, aGXYContext);
  }

  public Cursor getCursor(Graphics aGraphics,
                          int aMode,
                          ILcdGXYContext aGXYContext) {
    return fIconPainter.getCursor(aGraphics, aMode, aGXYContext);
  }

  public String getDisplayName() {
    return fIconPainter.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fIconPainter.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fIconPainter.removePropertyChangeListener(aPropertyChangeListener);
  }

  public Object clone() {
    try {
      PopulationChangePainter clone = (PopulationChangePainter) super.clone();
      clone.fIconPainter = (TLcdGXYIconPainter) fIconPainter.clone();
      return clone;
    } catch (CloneNotSupportedException ex) {
      // Cannot happen as this class extends Object and implements Cloneable
      throw new RuntimeException(ex);
    }
  }

}

