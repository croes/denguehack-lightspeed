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
package samples.gxy.decoder.mtm;

import java.awt.Color;

import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;

/**
 * A basic painter provider that can display points, polylines and polygons.
 */
class BasicMTMPainterProvider implements ILcdGXYPainterProvider {

  /**
   * Internal icon painter (used for 2D points).
   */
  protected TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();

  /**
   * Internal polyline painter.
   */
  protected TLcdGXYPointListPainter fPolylinePainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);

  /**
   * Internal polygon painter.
   */
  protected TLcdGXYPointListPainter fPolygonPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.OUTLINED_FILLED);

  /**
   * Internal shape list painter.
   */
  protected TLcdGXYShapeListPainter fShapeListPainter = new TLcdGXYShapeListPainter(this);

  /**
   * Creates a new BasicMTMPainterProvider.
   */
  public BasicMTMPainterProvider() {
    fIconPainter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 5, new Color(200, 150, 190)));
    fIconPainter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.RECT, 7, Color.red));

    fPolylinePainter.setLineStyle(new TLcdG2DLineStyle(new Color(140, 150, 210), Color.red));

    fPolygonPainter.setLineStyle(new TLcdG2DLineStyle(Color.gray, Color.red));
    fPolygonPainter.setFillStyle(new TLcdGXYPainterColorStyle(new Color(222, 205, 139), Color.red));
  }

  /**
   * Gets the internal painter used for polygons.
   */
  public TLcdGXYPointListPainter getPolygonPainter() {
    return fPolygonPainter;
  }

  /**
   * Sets the painter for polygons.
   */
  public void setPolygonPainter(TLcdGXYPointListPainter aPolygonPainter) {
    fPolygonPainter = aPolygonPainter;
  }

  /**
   * Gets the internal painter used for 2D points.
   */
  public TLcdGXYIconPainter getIconPainter() {
    return fIconPainter;
  }

  /**
   * Sets the painter for 2D points.
   */
  public void setIconPainter(TLcdGXYIconPainter aIconPainter) {
    fIconPainter = aIconPainter;
  }

  /**
   * Gets the internal painter used for polylines.
   */
  public TLcdGXYPointListPainter getPolylinePainter() {
    return fPolylinePainter;
  }

  /**
   * Sets the painter for polylines.
   */
  public void setPolylinePainter(TLcdGXYPointListPainter aPolylinePainter) {
    fPolylinePainter = aPolylinePainter;
  }

  /**
   * Gets the painter for shape lists.
   */
  public TLcdGXYShapeListPainter getShapeListPainter() {
    return fShapeListPainter;
  }

  /**
   * Sets the painter for shape lists.
   */
  public void setShapeListPainter(TLcdGXYShapeListPainter aShapeListPainter) {
    fShapeListPainter = aShapeListPainter;
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    if (aObject instanceof ILcdPoint) {
      fIconPainter.setObject(aObject);
      return fIconPainter;
    }
    if (aObject instanceof ILcdPolygon || aObject instanceof ILcdComplexPolygon) {
      fPolygonPainter.setObject(aObject);
      return fPolygonPainter;
    }
    if (aObject instanceof ILcdPolyline) {
      fPolylinePainter.setObject(aObject);
      return fPolylinePainter;
    }
    if (aObject instanceof ILcdShapeList) {
      fShapeListPainter.setObject(aObject);
      return fShapeListPainter;
    }
    return null;
  }

  public Object clone() {
    BasicMTMPainterProvider result = new BasicMTMPainterProvider();

    result.fIconPainter = (TLcdGXYIconPainter) fIconPainter.clone();
    result.fPolygonPainter = (TLcdGXYPointListPainter) fPolygonPainter.clone();
    result.fPolylinePainter = (TLcdGXYPointListPainter) fPolylinePainter.clone();
    result.fShapeListPainter = (TLcdGXYShapeListPainter) fShapeListPainter.clone();

    return result;
  }
}
