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
package samples.lightspeed.imageprojection;

import java.awt.Color;
import java.util.Collection;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZLine;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjector;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler to visualize the frustum of a projector.
 */
class FrustumStyler extends ALspStyler {

  private final TLspLineStyle fLineStyle;
  private final ILspImageProjectionLayer fProjectionLayer;

  public FrustumStyler(ILspImageProjectionLayer aImageProjectionLayer) {
    fProjectionLayer = aImageProjectionLayer;
    fLineStyle = TLspLineStyle
        .newBuilder()
        .width(2)
        .color(Color.WHITE)
        .opacity(0.8f)
        .build();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      ImageProjector imageProjector = (ImageProjector) object;
      TLspImageProjector projector = imageProjector.getProjector();
      ILspView view = aContext.getView();
      paintLineTo(object, projector, new TLcdXYPoint(0, 0), aStyleCollector, view);
      paintLineTo(object, projector, new TLcdXYPoint(0, 1), aStyleCollector, view);
      paintLineTo(object, projector, new TLcdXYPoint(1, 0), aStyleCollector, view);
      paintLineTo(object, projector, new TLcdXYPoint(1, 1), aStyleCollector, view);
    }
  }

  private void paintLineTo(Object object, TLspImageProjector aProjector, ILcdPoint aPointOnImage, ALspStyleCollector aStyleCollector, ILspView aView) {
    ILcdPoint p1OnTerrain = fProjectionLayer.projectPoint(aPointOnImage, aProjector, aView);
    if (p1OnTerrain != null) {
      TLcdXYZLine l1 = new TLcdXYZLine(new TLcdXYZPoint(p1OnTerrain), new TLcdXYZPoint(aProjector.getEyePoint()));
      aStyleCollector.object(object).geometry(l1).style(fLineStyle).submit();
    }
  }
}
