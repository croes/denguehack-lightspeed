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
package samples.tea.lightspeed.visibility;

import java.awt.Color;

import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.tea.TLcdVisibilityInterpretation;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.tea.VisibilityMatrixRasterValueMapper;

/**
 * A simple factory class which creates the different visibility layers.
 */
class VisibilityLayerFactory {

  private static double[] sColorLevels = null;
  private static Color[] sColors = null;

  public static double[] getColorLevels() {
    if (sColorLevels == null) {
      sColorLevels = new double[] {
              VisibilityMatrixRasterValueMapper.mapInterpretation(TLcdVisibilityInterpretation.INVISIBLE),
              VisibilityMatrixRasterValueMapper.mapInterpretation(TLcdVisibilityInterpretation.UNCERTAIN),
              VisibilityMatrixRasterValueMapper.mapInterpretation(TLcdVisibilityInterpretation.VISIBLE),
              VisibilityMatrixRasterValueMapper.mapInterpretation(TLcdVisibilityInterpretation.OUTSIDE_SHAPE),
              VisibilityMatrixRasterValueMapper.mapInterpretation(TLcdVisibilityInterpretation.NOT_COMPUTED),
      };
    }
    return sColorLevels;
  }

  public static Color[] getColors() {
    if (sColors == null) {
      sColors = new Color[] {
              Color.RED,
              Color.ORANGE,
              Color.GREEN,
              new Color(0f, 0f, 0f, 0f),
              new Color(0f, 0f, 0f, 0f),
      };
    }
    return sColors;
  }

  /**
   * Returns a layer with a model that can contain a to-polyline intervisibility result.
   * @return a layer with a model that can contain a to-polyline intervisibility result.
   */
  public static ILspLayer createToPolylineLayer() {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the to-polyline intervisibility results", "To Polyline", "To Polyline"
    ));

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                             .selectable(false)
                                             .bodyEditable(false)
                                             .bodyStyler(TLspPaintState.REGULAR, new PolylineVisibilityStyler())
                                             .label(model.getModelDescriptor().getDisplayName())
                                             .build();
  }

  /**
   * Returns a layer with a model that can contain a to-polygon intervisibility result.
   * @return a layer with a model that can contain a to-polygon intervisibility result.
   */
  public static ILspLayer createToPolygonLayer() {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdRasterModelDescriptor(
        "Layer containing the to-polygon intervisibility results", "To Polygon", "To Polygon", false
    ));

    TLcdColorMap colorMap = new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), getColorLevels(), getColors());

    return TLspRasterLayerBuilder.newBuilder().model(model)
                                              .styler(TLspPaintRepresentationState.REGULAR_BODY, TLspRasterStyle.newBuilder()
                                                                                                                .colorMap(colorMap)
                                                                                                                .startResolutionFactor(1000)
                                                                                                                .build())
                                              .label(model.getModelDescriptor().getDisplayName())
                                              .build();
  }

}
