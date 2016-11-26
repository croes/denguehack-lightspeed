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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.util.Collection;

import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

public class OpenStreetMapPointsLayerFactory extends OpenStreetMapLayerFactory {

  @Override
  protected ILspStyler createLabelStyler(GeometryType aGeometryType) {
    return new PointsLabelStyler();
  }

  private static class PointsLabelStyler extends ALspLabelStyler {

    private static final TLspTextStyle TEXT_STYLE = TLspTextStyle.newBuilder().font("Dialog-BOLD-12").textColor(Color.white).haloColor(new Color(0.0f, 0.32156864f, 0.45882353f)).build();

    private static final ILcdInterval[] DETAIL_LEVELS = {
        new TLcdInterval(0.05, 0.1),
        new TLcdInterval(0.1, 0.2),
        new TLcdInterval(0.2, 0.5),
        new TLcdInterval(0.5, 1.0),
        new TLcdInterval(1.0, Double.MAX_VALUE)
    };

    private static final double[] GLOBAL_PRIORITIES = {
        1.0, 0.9, 0.8, 0.7, 0.6
    };

    private ALspStyle fTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder()
                                                                               .expressions("name")
                                                                               .build();

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      int lod = LODSupport.getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      for (Object object : aObjects) {
        aStyleCollector.object(object)
                       .locations(10, TLspLabelLocationProvider.Location.SOUTH)
                       .priority(getPriority(object, 0, 1000, lod))
                       .styles(TEXT_STYLE, fTextProviderStyle)
                       .submit();
      }
    }

    public static int getPriority(Object aObject, int aLargestPriority, int aSmallestPriority, int aLOD) {
      double relative_priority = getRelativePriority(aObject);
      double global_priority = getGlobalPriority(aLOD);
      double priority = global_priority * 0.5 + relative_priority * 0.5;
      return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
    }

    private static double getRelativePriority(Object aObject) {
      // Retrieve the priority of a label relative to an other label of the same layer.
      return 1.0;
    }

    private static double getGlobalPriority(int aLOD) {
      return GLOBAL_PRIORITIES[aLOD];
    }
  }
}
