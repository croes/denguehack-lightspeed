package com.luciad.dengue.weather;

import com.luciad.dengue.util.DateUtils;
import com.luciad.dengue.util.RasterStyler;
import com.luciad.dengue.util.TimeBasedModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.timeview.TimeSlider;

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
  protected TimeSlider fTimeSlider;

  protected TimeBasedModel fPrecipitationModel;
  protected TimeBasedModel fTemperatureModel;

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
    fPrecipitationModel = addWeatherData(
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
            new Color(0x80FF7F),
            new Color(0x72FFF2),
            new Color(0x8198FF),
            new Color(0xFDFBFF)
        }
    );

    // Temperature
    fTemperatureModel = addWeatherData(
        weatherModelFactory,
        WeatherModelFactory.MEAN_TEMPERATURE,
        new double[]{
            Short.MIN_VALUE,
            -20,
            10,
            40,
            100,
            Short.MAX_VALUE
        },
        new Color[]{
            new Color(0xFDFBFF),
            new Color(0x629751),
            new Color(0xFF8000),
            new Color(0xFF6464),
            new Color(0xFFF73B),
            new Color(0xFDFBFF)
        }
    );
  }

  private TimeBasedModel addWeatherData(WeatherModelFactory aWeatherModelFactory, WeatherModelFactory.MonthlyData aData, double[] aLevels, Color[] aColors) throws IOException {
    TimeBasedModel model = aWeatherModelFactory.createMonthlyModel(
        aData, FIRST_YEAR, LAST_YEAR
    );
    getView().addLayer(
        TLspRasterLayerBuilder
            .newBuilder()
            .model(model)
            .styler(
                TLspPaintRepresentationState.REGULAR_BODY,
                new RasterStyler(new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), aLevels, aColors))
            )
            .build()
    );

    EventQueue.invokeLater(() -> {
      fTimeSlider.addChangeListener(e -> model.setTime(fTimeSlider.getTime()));
      fTimeSlider.setValidRange(DateUtils.date(FIRST_YEAR, 1).toEpochSecond() * 1000,
                                DateUtils.date(LAST_YEAR, 12).toEpochSecond() * 1000,
                                0, 1000);
    });
    return model;
  }

  public static void main(String[] args) {
    startSample(WeatherAnalysis.class, "Weather analysis");
  }
}
