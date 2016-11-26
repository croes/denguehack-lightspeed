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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.multidimensional.ILcdDimension;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.iso19103.TLcdISO19103Measure;

class WeatherUtil {

  private static final String PASCAL = "pascal";
  private static final String METRE = "metre";

  private WeatherUtil(){}

  public static TLcdNetCDFFilteredModel getNetCDFFilteredModel(ILcdModel aILcdModel, String aDisplayName) {
    if (aILcdModel instanceof ILcdModelTreeNode) {
      ILcdModelTreeNode model = (ILcdModelTreeNode) aILcdModel;
      Enumeration<ILcdModel> models = model.models();
      while (models.hasMoreElements()) {
        ILcdModel subModel = models.nextElement();
        if (subModel instanceof TLcdNetCDFFilteredModel) {
          TLcdNetCDFFilteredModel netCDFFilteredModel = (TLcdNetCDFFilteredModel) subModel;
          if (netCDFFilteredModel.getModelDescriptor().getDisplayName().contains(aDisplayName)) {
            return netCDFFilteredModel;
          }
        } else if (subModel instanceof ILcdModelTreeNode) {
          return getNetCDFFilteredModel(subModel, aDisplayName);
        }
      }
    }
    return null;
  }

  public static TLcdDimensionAxis<Date> getTimeAxis(TLcdNetCDFFilteredModel aModel) {
    for (ILcdDimension<?> dimension : aModel.getDimensions()) {
      TLcdDimensionAxis<?> dimensionAxis = dimension.getAxis();
      if (dimensionAxis.getType().equals(Date.class)) {
        return (TLcdDimensionAxis<Date>) dimensionAxis;
      }
    }
    throw new IllegalStateException("No time axis for model " + aModel);
  }

  public static TLcdDimensionAxis<TLcdISO19103Measure> getPressureAxis(TLcdNetCDFFilteredModel aModel) {
    return getDimensionAxis(aModel, PASCAL);
  }

  public static TLcdDimensionAxis<TLcdISO19103Measure> getAltitudeAxis(TLcdNetCDFFilteredModel aModel) {
    return getDimensionAxis(aModel, METRE);
  }

  private static TLcdDimensionAxis<TLcdISO19103Measure> getDimensionAxis(TLcdNetCDFFilteredModel aModel, String aUnit) {
    for (ILcdDimension<?> dimension : aModel.getDimensions()) {
      TLcdDimensionAxis<?> dimensionAxis = dimension.getAxis();
      if (dimensionAxis.getType().equals(TLcdISO19103Measure.class)
          && dimensionAxis.getUnit().getNameOfStandardUnit().toLowerCase().contains(aUnit)) {
        return (TLcdDimensionAxis<TLcdISO19103Measure>) dimensionAxis;
      }
    }
    return null;
  }

  public static TLcdDimensionAxis<TLcdISO19103Measure> createAltitudeAxis() {
    TLcdDimensionAxis.Builder<TLcdISO19103Measure> altitudeAxisBuilder = TLcdDimensionAxis.newBuilder();
    return altitudeAxisBuilder.displayName("altitude").type(TLcdISO19103Measure.class).unit(TLcdDistanceUnit.METRE_UNIT).build();
  }

  public static <E> List<E> list(Iterable<E> aIterable) {
    List<E> list = new ArrayList<>();
    for (E item : aIterable) {
      list.add(item);
    }
    return list;
  }

  /**
   * @param pressureInPascal
   * @return (approximated) altitude in meters
   */
  public static double getAltitude(double pressureInPascal) {
    return -7000*Math.log(pressureInPascal/101325);
  }

  public static int round(double value, int aScale) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(aScale, RoundingMode.HALF_UP);
    return bd.intValue();
  }

  public static String format(Object aValue) {
    if (aValue instanceof Date){
      Date date = (Date) aValue;
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-6"));
      return dateFormat.format(date);
    }
    if (aValue instanceof TLcdISO19103Measure) {
      TLcdISO19103Measure measure = (TLcdISO19103Measure) aValue;
      int roundedValue = WeatherUtil.round(measure.doubleValue(), 0);
      if (measure.getUnitOfMeasure() != null) {
        String uomSymbol = measure.getUnitOfMeasure().getUOMSymbol();
        if (uomSymbol != null && uomSymbol.toLowerCase().contains("hectopascal")) {
          uomSymbol = "hPa";
        }
        return Integer.toString(roundedValue) + " " + uomSymbol;
      } else {
        return Integer.toString(roundedValue);
      }
    }
    return aValue.toString();
  }

}
