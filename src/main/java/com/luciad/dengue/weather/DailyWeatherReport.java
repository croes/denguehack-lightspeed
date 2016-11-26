package com.luciad.dengue.weather;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * @author Thomas De Bodt
 */
class DailyWeatherReport implements ILcdBounded {

  private static final TLcdLonLatBounds UNDEFINED_BOUNDS = new TLcdLonLatBounds();

  int stn;
  String wban;
  WeatherStation station;

  long yearmoda;
  double temp;
  double dewp;
  double slp;
  double stp;
  double visib;
  double wdsp;
  double mxspd;
  double gust;
  double max;
  double min;
  double prcp;
  double sndp;
  double frshtt;

  public boolean isValid() {
    return station != null && station.isValid();
  }

  @Override
  public ILcdBounds getBounds() {
    return station == null ? UNDEFINED_BOUNDS : station.getBounds();
  }
}
