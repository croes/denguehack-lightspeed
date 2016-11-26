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
package samples.lightspeed.demo.application.data.weather;

import static samples.lightspeed.demo.application.data.weather.TemperatureContourLayerFactory.getContourRasterValueFilter;

import java.awt.Color;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.swing.TLcdRangeSlider;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.demo.framework.gui.DemoUIColors;

/**
 * A range slider to filter raster values.
 */
class RasterValueRangeSlider extends TLcdRangeSlider {

  private final WeatherModel fWeatherModel;
  private final List<ILspLayer> fTemperatureLayers;
  private final List<ILspLayer> fTemperatureContourLayers;

  public RasterValueRangeSlider(WeatherModel aWeatherModel, List<ILspLayer> aTemperatureLayers, List<ILspLayer> aTemperatureContourLayers) {
    super(aWeatherModel.getMinimumRasterValue(), aWeatherModel.getMaximumRasterValue());
    fWeatherModel = aWeatherModel;
    fTemperatureLayers = aTemperatureLayers;
    fTemperatureContourLayers = aTemperatureContourLayers;
    setOpaque(false);
    addChangeListener(new TemperatureChangeListener());
  }

  private class TemperatureChangeListener implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
      updateTemperatureModel();
      updateTemperatureContourModel();
    }

    private void updateTemperatureModel() {
      TLcdColorMap newColorMap = createColorMapWithTransparentColorOutsideRoundedSliderRange();

      for (ILspLayer temperatureLayer : fTemperatureLayers) {
        ILspEditableStyledLayer layer = (ILspEditableStyledLayer) temperatureLayer;
        ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
        if (styler instanceof TLspRasterStyle) {
          TLspRasterStyle rasterStyle = (TLspRasterStyle) styler;
          TLspRasterStyle newRasterStyle = rasterStyle.asBuilder().colorMap(newColorMap).build();
          layer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, newRasterStyle);
        }
      }

    }

    private TLcdColorMap createColorMapWithTransparentColorOutsideRoundedSliderRange() {
      ContourModel contourModel = fWeatherModel.getContourModel();
      TLcdColorMap colorMap = contourModel.getColorMap();
      Double min = getLowerBound(contourModel.getLevels(), getRangeMinimum());
      Double max = getHigherBound(contourModel.getLevels(), getRangeMaximum());

      Color minColor = colorMap.retrieveColor(min);
      Color maxColor = colorMap.retrieveColor(max);
      double epsilon = 1e-6;

      TLcdColorMap result = new TLcdColorMap();
      result.removeAll();

      for (int i = 0; i < colorMap.getLevelCount(); i++) {
        if (colorMap.getLevel(i) > min && colorMap.getLevel(i) < max) {
          result.insertLevel(result.getLevelCount(), colorMap.getLevel(i));
          result.insertColor(result.getColorCount(), colorMap.getColor(i));
        }
      }

      result.insertLevel(0, min);
      result.insertColor(0, minColor);

      result.insertLevel(0, min - epsilon);
      result.insertColor(0, DemoUIColors.TRANSPARENT);

      result.insertLevel(result.getLevelCount(), max);
      result.insertColor(result.getColorCount(), maxColor);

      result.insertLevel(result.getLevelCount(), max + epsilon);
      result.insertColor(result.getColorCount(), DemoUIColors.TRANSPARENT);

      return result;
    }

    private Double getLowerBound(double[] aLevels, double aValue) {
      NavigableSet<Double> levels = toNavigableSet(aLevels);
      Double lower = levels.lower(aValue);
      if (lower != null) {
        return lower;
      } else {
        return levels.first();
      }
    }

    private Double getHigherBound(double[] aLevels, double aValue) {
      NavigableSet<Double> levels = toNavigableSet(aLevels);
      Double higher = levels.higher(aValue);
      if (higher != null) {
        return higher;
      } else {
        return levels.last();
      }
    }

    private NavigableSet<Double> toNavigableSet(double[] aLevels) {
      TreeSet<Double> result = new TreeSet<>();
      for (double level : aLevels) {
        result.add(level);
      }
      return result;
    }

    private void updateTemperatureContourModel() {
      for (ILspLayer layer : fTemperatureContourLayers) {
        ContourRasterValueFilter filter = getContourRasterValueFilter(layer);
        if (filter != null) {
          filter.setRasterRange(getRangeMinimum(), getRangeMaximum());
        }
      }
    }

  }

}
