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
package samples.lightspeed.customization.hippodrome;

import com.luciad.geodesy.TLcdEllipsoidUtil;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.handle.ALspOutlineResizeHandle;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * Custom handle to resize an IHippodrome, i.e., to change its width.
 *
 * @see HippodromeEditor
 */
class HippodromeOutlineResizeHandle extends ALspOutlineResizeHandle {

  HippodromeOutlineResizeHandle(IHippodrome aHippodrome) {
    super(aHippodrome);
  }

  @Override
  protected ILcdPoint calculateClosestReferencePoint(ILcdPoint aModelPoint, Object aObject, TLspContext aContext) {
    if (!(aObject instanceof IHippodrome)) {
      return null;
    }

    ILcdModelReference modelReference = aContext.getModelReference();
    ILcdPoint startPoint = ((IHippodrome) aObject).getStartPoint();
    ILcdPoint endPoint = ((IHippodrome) aObject).getEndPoint();

    if (modelReference instanceof ILcdGeoReference &&
        ((ILcdGeoReference) modelReference).getCoordinateType() == ILcdGeoReference.GEODETIC) {
      TLcdLonLatPoint closestPointSFCT = new TLcdLonLatPoint();
      TLcdEllipsoidUtil.closestPointOnGeodesic(
          startPoint, endPoint, aModelPoint,
          ((ILcdGeoReference) modelReference).getGeodeticDatum().getEllipsoid(),
          1e-10, 1.0, closestPointSFCT);
      return closestPointSFCT;
    } else {
      TLcdXYPoint closestPointSFCT = new TLcdXYPoint();
      TLcdCartesian.closestPointOnLineSegment(startPoint, endPoint, aModelPoint, closestPointSFCT);
      return closestPointSFCT;
    }
  }

  @Override
  protected String getPropertyName() {
    return "width";
  }
}

