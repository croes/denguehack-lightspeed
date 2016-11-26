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
package samples.common.shape;

import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.shape2D.ALcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.ILcd2DEditablePolyline;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.ILcd3DEditablePolyline;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.shape.shape3D.TLcdXYZPolyline;

/**
 * Convenience factory for creating cartesian or lon-lat shapes.
 */
public class ShapeFactory {

  public static ALcd2DEditablePoint create2DPoint(ILcdModelReference aModelReference, double aX, double aY) {
    return aModelReference instanceof ILcdGeodeticReference ? new TLcdLonLatPoint(aX, aY) : new TLcdXYPoint(aX, aY);
  }

  public static ALcd2DEditablePoint create3DPoint(ILcdModelReference aModelReference, double aX, double aY, double aZ) {
    return aModelReference instanceof ILcdGeodeticReference ? new TLcdLonLatHeightPoint(aX, aY, aZ) : new TLcdXYZPoint(aX, aY, aZ);
  }

  public static ILcd2DEditablePolyline create2DPolyline(ILcdModelReference aModelReference, ILcd2DEditablePoint... aPoint) {
    ILcd2DEditablePointList list = new TLcd2DEditablePointList(aPoint, false);
    return aModelReference instanceof ILcdGeodeticReference ? new TLcdLonLatPolyline(list) : new TLcdXYPolyline(list);
  }

  public static ILcd2DEditablePolyline create2DPolyline(ILcdModelReference aModelReference, ILcd2DEditablePointList aPointList) {
    return aModelReference instanceof ILcdGeodeticReference ? new TLcdLonLatPolyline(aPointList) : new TLcdXYPolyline(aPointList);
  }

  public static ILcd3DEditablePolyline create3DPolyline(ILcdModelReference aModelReference, ILcd3DEditablePoint... aPoint) {
    ILcd3DEditablePointList list = new TLcd3DEditablePointList(aPoint, false);
    return aModelReference instanceof ILcdGeodeticReference ? new TLcdLonLatHeightPolyline(list) : new TLcdXYZPolyline(list);
  }

}
