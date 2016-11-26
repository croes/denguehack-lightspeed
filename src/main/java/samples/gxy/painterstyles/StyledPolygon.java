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
package samples.gxy.painterstyles;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;

/**
 * A polygon with style settings.
 */
class StyledPolygon extends TLcdLonLatPolygon implements StyledShape {

  private final ShapeStyle fShapeStyle = new ShapeStyle();

  /**
   * Constructs a new <code>StyledPolygon</code> with the given point list.
   * The ellipsoid on which the polyline is defined is {@link TLcdEllipsoid#DEFAULT}.
   *
   * @param aList The point list to be used for the polyline.
   */
  public StyledPolygon(ILcd2DEditablePointList aList) {
    this(aList, TLcdEllipsoid.DEFAULT);
  }

  /**
   * Constructs a new <code>StyledPolyline</code> with the given point list and ellipsoid.
   *
   * @param aList      The point list to be used for the polyline.
   * @param aEllipsoid The ellipsoid on which the polyline is defined.
   */
  public StyledPolygon(ILcd2DEditablePointList aList, ILcdEllipsoid aEllipsoid) {
    super(aList, aEllipsoid);
  }

  public ShapeStyle getShapeStyle() {
    return fShapeStyle;
  }

}
