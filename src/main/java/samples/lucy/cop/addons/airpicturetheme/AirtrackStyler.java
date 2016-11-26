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
package samples.lucy.cop.addons.airpicturetheme;

import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.style.icon.OrientedLonLatHeightPoint;

/**
 * {@link com.luciad.view.lightspeed.style.styler.ILspStyler ILspStyler implementation} for the
 * air tracks including a heading symbol
 */
final class AirtrackStyler extends ALspStyler {
  private final TLspIconStyle fHeadingIconStyle;

  private final ALspStyleTargetProvider fTrackStyleTargetProvider = new TrackStyleTargetProvider();

  AirtrackStyler() {
    fHeadingIconStyle = TLspIconStyle.newBuilder().icon(new HeadingIcon()).useOrientation(true).build();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object domainObject : aObjects) {
      if (domainObject instanceof ILcdPoint) {
        styleSingleTrack(domainObject, aStyleCollector, aContext);
      } else if (domainObject instanceof ILcdShapeList) {
        styleSingleTrack(domainObject, aStyleCollector, aContext);
      }
    }
  }

  private void styleSingleTrack(Object aDomainObject, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.object(aDomainObject).geometry(fTrackStyleTargetProvider).style(fHeadingIconStyle).submit();
  }

  /**
   * An {@link ALspStyleTargetProvider} for the track layer domain objects. In order for the heading
   * symbols to point in the correct direction, we need to use {@code ILcdOriented} instances as
   * objects on the styler.
   */
  private static class TrackStyleTargetProvider extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      ILcdPoint point = null;
      if (aObject instanceof ILcdShapeList &&
          ((ILcdShapeList) aObject).getShapeCount() == 1 &&
          ((ILcdShapeList) aObject).getShape(0) instanceof ILcdPoint) {
        point = (ILcdPoint) ((ILcdShapeList) aObject).getShape(0);
      } else if (aObject instanceof ILcdPoint) {
        point = (ILcdPoint) aObject;
      }
      if (point != null) {
        Double heading = (Double) ((ILcdDataObject) aObject).getValue(TracksModel.HEADING_PROPERTY);
        //the heading in the icon is drawn to the right, which matches the 90 degrees of the ILcdOriented
        aResultSFCT.add(new OrientedLonLatHeightPoint(point.getX(), point.getY(), point.getZ(), heading - 90));
      }
    }
  }
}
