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

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider;
import com.luciad.gui.TLcdSymbol;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * @author Dieter Meeus
 * @since 2012.1
 */
public class OpenStreetMapPlacesLayerFactory extends OpenStreetMapLayerFactory {

  @Override
  protected ILspStyler createBodyStyler(GeometryType aGeometryType) {
    TLspStyler styler = new TLspStyler();
    TLspIconStyle iconStyle = TLspIconStyle.newBuilder()
                                           .icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 8, Color.darkGray, Color.lightGray))
                                           .offset(0, -4)
                                           .elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN)
                                           .build();
    styler.addStyles(TLspFusionGeometryProvider.POINT, iconStyle);
    return styler;
  }

  @Override
  protected ILspStyler createLabelStyler(GeometryType aGeometryType) {
    return new PlacesLabelStyler();
  }

  public static class PlacesLabelStyler extends ALspLabelStyler {

    private static final ILcdInterval[] DETAIL_LEVELS = {
        new TLcdInterval(5e-5, 1e-4),
        new TLcdInterval(1e-4, 2e-4),
        new TLcdInterval(2e-4, 5e-4),
        new TLcdInterval(5e-4, 1e-3),
        new TLcdInterval(1e-3, 5e-3),
        new TLcdInterval(5e-3, 1e-2),
        new TLcdInterval(1e-2, 2e-2),
        new TLcdInterval(2e-2, 1e-1),
        new TLcdInterval(1e-1, Double.MAX_VALUE)
    };

    private static final double[] GLOBAL_PRIORITIES = {
        1.0, 0.75, 0.5, 0.25, 0.0, 0.0, 0.0, 0.0, 0.0
    };

    private static final boolean[][] LABELED = {{true, true, true, true, true, true, true, true, false},
                                                {false, false, true, true, true, true, true, true, false},
                                                {false, false, false, false, true, true, true, true, false}};

    private static final TLspTextStyle[] TEXT_STYLES = {
        TLspTextStyle.newBuilder().font("Dialog-BOLD-12").textColor(Color.darkGray).build(),
        TLspTextStyle.newBuilder().font("Dialog-BOLD-10").textColor(Color.darkGray).build(),
        TLspTextStyle.newBuilder().font("Dialog-BOLD-8").textColor(Color.darkGray).build(),
    };

    private TLspDataObjectLabelTextProviderStyle fTextProviderStyle;

    public PlacesLabelStyler() {
      fTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder()
                                                               .expressions("name")
                                                               .build();
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      int lod = LODSupport.getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      for (Object object : aObjects) {
        int size = getCitySize(object);

        if (!LABELED[size][lod]) {
          continue;
        }

        int priority = getPriority(object, 0, 1000, lod);

        aStyleCollector.object(object)
                       .locations(6, TLspLabelLocationProvider.Location.SOUTH)
                       .priority(priority)
                       .styles(TEXT_STYLES[size],
                               fTextProviderStyle)
                       .submit();
      }
    }

    public int getPriority(Object aObject, int aLargestPriority, int aSmallestPriority, int aLOD) {
      double relative_priority = getRelativePriority(aObject);
      double global_priority = getGlobalPriority(aLOD);
      double priority = global_priority * 0.5 + relative_priority * 0.5;
      return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
    }

    private double getRelativePriority(Object aObject) {
      // Retrieve the priority of a label relative to an other label of the same layer.
      ILcdDataObject data_object = (ILcdDataObject) aObject;
      String fclass = data_object.getValue("fclass").toString();
      int population = (Integer) data_object.getValue("population");

      double min = 1.0;
      double max = 1.0;
      double f = 0.5;
      if ("city".equals(fclass)) {
        min = 0.00;
        max = 0.25;
        f = 1.0 - Math.min(population, 5000000) / 5000000;
      } else if ("town".equals(fclass)) {
        min = 0.25;
        max = 0.50;
        f = 1.0 - Math.min(population, 500000) / 500000;
      } else if ("village".equals(fclass)) {
        min = 0.50;
        max = 0.75;
        f = 0.5;
      } else if ("hamlet".equals(fclass)) {
        min = 0.75;
        max = 1.00;
        f = 0.5;
      }

      return (min + ((max - min) * f));
    }

    private static double getGlobalPriority(int aLOD) {
      return GLOBAL_PRIORITIES[aLOD];
    }

    private static int getCitySize(Object aObject) {
      ILcdDataObject data_object = (ILcdDataObject) aObject;
      String fclass = data_object.getValue("fclass").toString();
      if (fclass.equals("city")) {
        return 0;
      }
      if (fclass.equals("town")) {
        return 1;
      }
      return 2;
    }
  }
}
