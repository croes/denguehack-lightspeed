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

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.view.gxy.ILcdGXYContext;

/**
 * Rotation support for instances of {@link LonLatPolygonWithRotation}. Such instances will be
 * rotated around the center of their bounding box.
 */
public class LonLatPolygonRotationSupport extends RotationSupport {

  @Override
  protected void rotationCenterSFCT(Object aObject, ILcd2DEditablePoint aCenterSFCT) {
    LonLatPolygonWithRotation polygon = (LonLatPolygonWithRotation) aObject;
    ILcdBounds bounds = polygon.getBounds();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    ILcdPoint center = new TLcdLonLatPoint(bounds.getLocation().getX() + width / 2, bounds.getLocation().getY() + height / 2);
    aCenterSFCT.move2D(center);
  }

  @Override
  public void rotateObject(Object aObject, double aRotationAngle, ILcdGXYContext aGXYContext) {
    LonLatPolygonWithRotation polygon = (LonLatPolygonWithRotation) aObject;
    TLcdLonLatHeightPoint modelPointSFCT = new TLcdLonLatHeightPoint();
    ILcd2DEditablePoint centerSFCT = new TLcdLonLatPoint();
    rotationCenterSFCT(aObject, centerSFCT);

    for (int i = 0; i < polygon.getPointCount(); i++) {
      modelPointSFCT.move2D(polygon.getPoint(i));
      rotatePoint(centerSFCT, aRotationAngle, modelPointSFCT, aGXYContext);
      polygon.move2DPoint(i, modelPointSFCT.getX(), modelPointSFCT.getY());
    }

    polygon.setRotation(polygon.getRotation() + aRotationAngle);
  }

  @Override
  protected double retrieveObjectRotation(Object aObject) {
    return ((LonLatPolygonWithRotation) aObject).getRotation();
  }

}
