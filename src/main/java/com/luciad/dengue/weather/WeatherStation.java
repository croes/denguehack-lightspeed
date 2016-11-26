package com.luciad.dengue.weather;

import com.luciad.shape.shape2D.TLcdLonLatPoint;

/**
 * @author Thomas De Bodt
 */
public class WeatherStation extends TLcdLonLatPoint {

  int USAF;
  String WBAN;
  String STATION_NAME;
  String CTRY;
  String ST;
  String CALL;
  String ELEV;
  String BEGIN;
  String END;

  public boolean isValid() {
    return !Double.isNaN(getX()) && !Double.isNaN(getY());
  }
}
