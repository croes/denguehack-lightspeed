package com.luciad.dengue.weather;

import com.luciad.imaging.TLcdImageModelDescriptor;
import com.luciad.internal.util.TLinMeasureTypeCode;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;
import com.luciad.util.iso19103.TLcdUnitOfMeasureFactory;
import com.luciad.dengue.util.DataSet;
import com.luciad.dengue.util.TimeBaseModel;

import java.io.File;
import java.io.IOException;

/**
 * @author Thomas De Bodt
 */
public class WeatherModelFactory {

  // precipitation in millimetres per month
  public static final MonthlyData PRECIPITATION = new MonthlyData(
      "Precipitation",
      "CRUTS/pre/cru_ts_3_10_01.1901.2009.pre_{yyyy}_{m}.asc",
      TLcdUnitOfMeasureFactory.createUnitOfMeasure("Precipitation", "mm/month", TLinMeasureTypeCode.RATIO, "mm/month", 1, 1)
  );

  // daily mean temperature	in degrees Celsius
  public static final MonthlyData MEAN_TEMPERATURE = new MonthlyData(
      "Mean temperature",
      "CRUTS/tmp/cru_ts_3_10.1901.2009.tmp_{yyyy}_{m}.asc",
      TLcdUnitOfMeasureFactory.createUnitOfMeasure("Daily mean temperature", "degrees", TLcdISO19103MeasureTypeCodeExtension.TEMPERATURE, "degrees", 1, 1)
  );

  public TimeBaseModel createMonthlyModel(MonthlyData aData, int aFirstYear, int aLastYear) throws IOException {
    File file = new File(DataSet.ROOT, aData.fPattern);
    String displayName = String.format("%s:%d-%d", file.getParent(), aFirstYear, aLastYear);
    return new MonthlyWeatherModel(
        new TLcdGeodeticReference(),
        new TLcdImageModelDescriptor(displayName, aData.fName, aData.fName),
        file.getPath(), aFirstYear, aLastYear,
        aData.fUnitOfMeasure
    );
  }

  public TimeBaseModel createDailyStationModel() throws IOException {

    return null;
  }

  public static class MonthlyData {
    private final String fName;
    private final String fPattern;
    private final ILcdISO19103UnitOfMeasure fUnitOfMeasure;

    public MonthlyData(String aName, String aPattern, ILcdISO19103UnitOfMeasure aUnitOfMeasure) {
      fName = aName;
      fPattern = aPattern;
      fUnitOfMeasure = aUnitOfMeasure;
    }
  }
}
