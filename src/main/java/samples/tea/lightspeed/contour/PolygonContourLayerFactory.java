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
package samples.tea.lightspeed.contour;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.contour.TLcdIntervalContour;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Creates layer for the objects created during contour creation.
 */
class PolygonContourLayerFactory {

  public ILspLayer createLayer(ILcdModel aModel, final TLcdColorMap aColorMap, double[] aContourLevelsSpecial, final Color[] aContourColorsSpecial) {
    final List<Double> specialValues = new ArrayList<>();
    for (double specialValue : aContourLevelsSpecial) {
      specialValues.add(specialValue);
    }

    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                             .label(aModel.getModelDescriptor().getDisplayName())
                                             .bodyStyler(TLspPaintState.REGULAR, new ContourStyler(new ValueToColorMapper() {
                                               @Override
                                               public Color map(double aValue) {
                                                 Color color;
                                                 if (specialValues.contains(aValue)) {
                                                   color = aContourColorsSpecial[specialValues.indexOf(aValue)];
                                                 } else {
                                                   color = aColorMap.retrieveColor(aValue);
                                                 }
                                                 return color;
                                               }
                                             }))
                                             .bodyStyler(TLspPaintState.SELECTED, new ContourStyler(new ValueToColorMapper() {
                                               @Override
                                               public Color map(double aValue) {
                                                 return Color.GREEN;
                                               }
                                             }))
                                             .build();
  }

  private static class ContourStyler extends ALspStyler {

    private final ValueToColorMapper fValueToColorMapper;

    /**
     * Creates a new ContourStyler
     *
     * @param aValueToColorMapper Maps values to colors
     */
    public ContourStyler(ValueToColorMapper aValueToColorMapper) {
      fValueToColorMapper = aValueToColorMapper;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (object instanceof TLcdIntervalContour) {
          TLcdIntervalContour contour = (TLcdIntervalContour) object;
          style(aStyleCollector, contour, contour.getShape(), contour.getInterval().getMin());
        } else if (object instanceof TLcdValuedContour) {
          TLcdValuedContour contour = (TLcdValuedContour) object;
          style(aStyleCollector, contour, contour.getShape(), contour.getValue());
        }
      }
    }

    private void style(ALspStyleCollector aStyleCollector, Object object, ILcdShape aShape, double aValue) {
      aStyleCollector.object(object)
                     .geometry(aShape)
                     .style(TLspFillStyle.newBuilder().color(fValueToColorMapper.map(aValue)).build())
                     .submit();
    }

  }

  private interface ValueToColorMapper {

    Color map(double aValue);

  }

}
