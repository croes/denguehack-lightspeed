package com.luciad.dengue.weather;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * @author Thomas De Bodt
 */
public class DailyWeatherReport implements ILcdBounded {

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

  public long getYearmoda() {
    return yearmoda;
  }

  public double getTemp() {
    if(temp==9999.9){
      return Double.NaN;
    }
    return temp;
  }

  public double getDewp() {
    if(dewp==9999.9){
      return Double.NaN;
    }
    return dewp;
  }

  public double getSlp() {
    if(slp==9999.9){
      return Double.NaN;
    }
    return slp;
  }

  public double getStp() {
    if(stp==9999.9){
      return Double.NaN;
    }
    return stp;
  }

  public double getVisib() {
    if(visib==999.9){
      return Double.NaN;
    }
    return visib;
  }

  public double getWdsp() {
    if(wdsp==999.9){
      return Double.NaN;
    }
    return wdsp;
  }

  public double getMxspd() {
    if(mxspd==999.9){
      return Double.NaN;
    }
    return mxspd;
  }

  public double getGust() {
    if(gust==999.9){
      return Double.NaN;
    }
    return gust;
  }

  public double getMax() {
    if(max==9999.9){
      return Double.NaN;
    }
    return max;
  }

  public double getMin() {
    if(min==9999.9){
      return Double.NaN;
    }
    return min;
  }

  public double getPrcp() {
    if(prcp==99.9){
      return Double.NaN;
    }
    return prcp;
  }

  public double getSndp() {
    if(sndp==999.9){
      return Double.NaN;
    }
    return sndp;
  }

  public double getFrshtt() {
    if(frshtt==999.9){
      return Double.NaN;
    }
    return frshtt;
  }

  @Override
  public ILcdBounds getBounds() {
    return station == null ? UNDEFINED_BOUNDS : station.getBounds();
  }
}
