package com.luciad.dengue.weather;

import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.timeview.TimeSlider;
import com.luciad.dengue.util.DateUtils;
import com.luciad.dengue.util.TimeBaseModel;

import java.awt.*;
import java.io.IOException;

/**
 * @author Thomas De Bodt
 */
public class WeatherAnalysis extends LightspeedSample {

  static {
    useBlackLime();
  }

  private static final int FIRST_YEAR = 2000;
  private static final int LAST_YEAR = 2005;
  private TimeSlider fTimeSlider;

  @Override
  protected void createGUI() {
    super.createGUI();

    fTimeSlider = new TimeSlider();
    addComponentBelow(fTimeSlider);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    WeatherModelFactory weatherModelFactory = new WeatherModelFactory();

    // Precipitation
    addWeatherData(
        weatherModelFactory,
        WeatherModelFactory.PRECIPITATION,
        new double[]{
            Short.MIN_VALUE,
            0,
            100,
            1000,
            Short.MAX_VALUE
        },
        new Color[]{
            new Color(0xFDFBFF),
            new Color(0x629751),
            new Color(0xFF8000),
            new Color(0xFF6464),
            new Color(0xFDFBFF)
        }
    );

    // Temperature
    addWeatherData(
        weatherModelFactory,
        WeatherModelFactory.MEAN_TEMPERATURE,
        new double[]{
            Short.MIN_VALUE,
            -20,
            10,
            40,
            Short.MAX_VALUE
        },
        new Color[]{
            new Color(0xFDFBFF),
            new Color(0x6AB3FF),
            new Color(0xFFCE58),
            new Color(0xFF4545),
            new Color(0xFDFBFF)
        }
    );
  }

  private void addWeatherData(WeatherModelFactory aWeatherModelFactory, WeatherModelFactory.MonthlyData aData, double[] aLevels, Color[] aColors) throws IOException {
    TimeBaseModel model = aWeatherModelFactory.createMonthlyModel(
        aData, FIRST_YEAR, LAST_YEAR
    );
    getView().addLayer(
        TLspRasterLayerBuilder
            .newBuilder()
            .model(model)
            .styler(
                TLspPaintRepresentationState.REGULAR_BODY,
                new TLspStyler(
                    (ALspStyle)TLspRasterStyle
                        .newBuilder()
                        .startResolutionFactor(Double.POSITIVE_INFINITY)
                        .colorMap(new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), aLevels, aColors))
                        .build()
                )
            )
            .build()
    );

    EventQueue.invokeLater(() -> {
      fTimeSlider.addChangeListener(e -> model.setTime(fTimeSlider.getTime()));
      fTimeSlider.setValidRange(DateUtils.date(FIRST_YEAR, 1).toEpochSecond() * 1000,
                                DateUtils.date(LAST_YEAR, 12).toEpochSecond() * 1000,
                                0, 1000);
    });
  }

  public static void main(String[] args) {
    startSample(WeatherAnalysis.class, "Weather analysis");
  }
}
