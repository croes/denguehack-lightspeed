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
package samples.decoder.asterix;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.asterix.TLcdASTERIXPlotModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXPrecipitationZone;
import com.luciad.format.asterix.TLcdASTERIXTrajectoryModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXWeatherModelDescriptor;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;

import samples.gxy.common.layers.LayerFactoryWrapper;
import samples.gxy.decoder.MapSupport;

/**
 *  Layer factory for the trajectory models produced by the ASTERIX model decoder.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class ASTERIXLayerFactory extends LayerFactoryWrapper {

  public static final Color TRAJECTORY_COLOR = Color.lightGray;

  // There are only 8 levels of Intensity in ASTERIX Cat 8 data.
  // Intensity level 0 signifies no precipitation, so we will only paint levels 1 through 7.
  public static final Map<Integer,Color> WEATHER_COLORS = createColorMap();

  private static  Map<Integer, Color> createColorMap() {
    Map<Integer, Color> colorMap = new HashMap<>();
    colorMap.put(1, new Color(231, 232, 255));
    colorMap.put(2, new Color(157, 162, 255));
    colorMap.put(3, new Color(0, 9, 120));
    colorMap.put(4, new Color(255, 112, 112));
    colorMap.put(5, new Color(255, 51, 51));
    colorMap.put(6, new Color(190, 0, 0));
    colorMap.put(7, new Color(216, 0, 202));
    return Collections.unmodifiableMap(colorMap);
  }

  private static Map<Integer,ILcdGXYPainterStyle> WEATHER_STYLES = new HashMap<>();

  {
    for(Integer intensity : WEATHER_COLORS.keySet()) {
      TLcdG2DLineStyle lineStyle = new TLcdG2DLineStyle();
      lineStyle.setLineWidth(1);
      lineStyle.setColor(WEATHER_COLORS.get(intensity));
      lineStyle.setSelectionLineWidth(1);
      lineStyle.setSelectionColor(Color.red);
      WEATHER_STYLES.put(intensity, lineStyle);
    }
  }

  public ASTERIXLayerFactory() {
    super(new SingleASTERIXLayerFactory());
  }

  private static class SingleASTERIXLayerFactory implements ILcdGXYLayerFactory {

    private static final Color PLOT_FILL = new Color(50, 220, 75);
    private static final Color PLOT_DEFAULT_BORDER = new Color(120, 120, 120);
    private static final Color PLOT_SELECTION_BORDER = new Color(220, 220, 220);

    @Override
    public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
      if (aModel.getModelDescriptor() instanceof TLcdASTERIXTrajectoryModelDescriptor) {
        return createTrajectoryLayer(aModel);
      }
      if (aModel.getModelDescriptor() instanceof TLcdASTERIXPlotModelDescriptor) {
        return createPlotLayer(aModel);
      }
      if (aModel.getModelDescriptor() instanceof TLcdASTERIXWeatherModelDescriptor) {
        return createWeatherLayer(aModel);
      }
      return null;
    }

    private ILcdGXYLayer createPlotLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      TLcdGXYIconPainter painter = new TLcdGXYIconPainter();
      painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 12, PLOT_DEFAULT_BORDER, PLOT_FILL));
      painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 12, PLOT_SELECTION_BORDER, PLOT_FILL));
      layer.setGXYPainterProvider(painter);
      return layer;
    }

    private ILcdGXYLayer createTrajectoryLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter();
      TLcdG2DLineStyle lineStyle = new TLcdG2DLineStyle();
      lineStyle.setLineWidth(1);
      lineStyle.setColor(TRAJECTORY_COLOR);
      lineStyle.setSelectionLineWidth(1);
      lineStyle.setSelectionColor(Color.red);
      painter.setLineStyle(lineStyle);
      layer.setGXYPainterProvider(painter);
      return layer;
    }

    private ILcdGXYLayer createWeatherLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      layer.setGXYPainterProvider(new WeatherPictureGXYPainter());
      return layer;
    }
  }

  public static class WeatherPictureGXYPainter extends TLcdGXYShapeListPainter {
    private TLcdGXYShapePainter fShapePainter;

    public WeatherPictureGXYPainter() {

      fShapePainter = new TLcdGXYShapePainter();

      setShapeGXYPainterProvider(new ILcdGXYPainterProvider() {
        public ILcdGXYPainter getGXYPainter(Object aObject) {

          if (aObject instanceof TLcdASTERIXPrecipitationZone) {
            int intensity = ((TLcdASTERIXPrecipitationZone) aObject).getIntensity();
            if (WEATHER_STYLES.containsKey(intensity)) {
              fShapePainter.setLineStyle(WEATHER_STYLES.get(intensity));
              fShapePainter.setObject(aObject);
              return fShapePainter;
            }
          }
          return null;
        }

        @Override
        public Object clone() {
          try {
            WeatherPictureGXYPainter clone = (WeatherPictureGXYPainter) super.clone();
            clone.fShapePainter = fShapePainter != null ? (TLcdGXYShapePainter) fShapePainter.clone() : null;
            return clone;
          } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone method not supported for the class : " + WeatherPictureGXYPainter.class);
          }
        }
      });
    }
  }
}
