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
package samples.earth.util;

import static com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension.TERRAIN_HEIGHT;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.List;

import com.luciad.imaging.ALcdBandMeasurementSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCode;
import com.luciad.view.ILcdLayer;

public class WeatherUtil {

  protected WeatherUtil() {
  }

  public static boolean isWeatherSingleLayer(ILcdLayer aLayer) {
    return aLayer != null &&
           isWeatherSingleModel(aLayer.getModel());
  }

  public static boolean isWeatherSingleModel(ILcdModel aModel) {
    if (aModel == null
        || aModel instanceof ILcdModelContainer
        || !(aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor)) {
      return false;
    }
    for (Enumeration elements = aModel.elements(); elements.hasMoreElements(); ) {
      if (isWeatherObject(elements.nextElement())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isWeatherModel(ILcdModel aModel) {
    return isWeatherSingleModel(aModel) || isWeatherTreeModel(aModel);
  }

  public static boolean isWeatherTreeModel(ILcdModel aModel) {
    if (aModel instanceof ILcdModelContainer) {
      ILcdModelContainer modelContainer = (ILcdModelContainer) aModel;
      for (int i = 0, n = modelContainer.modelCount(); i != n; i++) {
        if (isWeatherSingleModel(modelContainer.getModel(i))) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isWeatherObject(Object aDomainObject) {
    ALcdImage image = ALcdImage.fromDomainObject(aDomainObject);
    if (image == null) {
      // No image -> not NetCDF.
      return false;
    }

    List<ALcdBandSemantics> allBandSemantics = image.getConfiguration().getSemantics();
    if (allBandSemantics.size() > 2) {
      // More than 2 bands -> not NetCDF.
      return false;
    }

    for (ALcdBandSemantics bandSemantics : allBandSemantics) {
      if (!(bandSemantics instanceof ALcdBandMeasurementSemantics)) {
        // Not a measurement band (probably a color band) -> not NetCDF.
        return false;
      }
      ALcdBandMeasurementSemantics measurementBandSemantics = (ALcdBandMeasurementSemantics) bandSemantics;
      TLcdISO19103MeasureTypeCode measureType = measurementBandSemantics.getUnitOfMeasure().getMeasureType();
      if (measureType == TERRAIN_HEIGHT) {
        // Terrain height -> not NetCDF.
        return false;
      }
      // Any other measurement band -> we assume it's NetCDF.
    }

    return true;
  }

  public static TLcdColorMap retrieveColorMap(Object aWeatherObject, TLcdColorMap aDefaultColorMap) {
    ALcdImage image = ALcdImage.fromDomainObject(aWeatherObject);
    List<ALcdBandSemantics> allBandSemantics = image.getConfiguration().getSemantics();
    if (allBandSemantics.size() != 1) {
      return aDefaultColorMap;
    }
    ALcdBandSemantics bandSemantics = allBandSemantics.get(0);
    if (!(bandSemantics instanceof ALcdBandMeasurementSemantics)) {
      return aDefaultColorMap;
    }
    ALcdBandMeasurementSemantics measurementBandSemantics = (ALcdBandMeasurementSemantics) bandSemantics;
    TLcdISO19103MeasureTypeCode measureType = measurementBandSemantics.getUnitOfMeasure().getMeasureType();
    if (measureType == null) {
      return aDefaultColorMap;
    }
    String measureTypeName = measureType.getName();
    if (measureTypeName != null
        && measureTypeName.contains("geopotential")) {
      // Remap the color map to an other interval that is more suited to visualize this data
      return adjustGeopotentialColorMap(aDefaultColorMap);
    }
    return aDefaultColorMap;
  }

  private static TLcdColorMap adjustGeopotentialColorMap(TLcdColorMap aDefaultColorMap) {
    TLcdInterval interval = new TLcdInterval(-73.82829284667969, 32693.17170715332);
    double[] levels = new double[aDefaultColorMap.getColorCount()];
    Color[] colors = new Color[aDefaultColorMap.getColorCount()];
    for (int i = 0; i < levels.length; i++) {
      double t = Math.pow((double) i / (levels.length - 1), 3.0);
      levels[i] = interval.getMin() + t * (interval.getMax() - interval.getMin());
      colors[i] = aDefaultColorMap.getColor(i);
    }
    return new TLcdColorMap(interval, levels, colors);
  }

  /**
   * Creates a default decimal format for the numbers.
   */
  public static DecimalFormat createDecimalFormat(String aSuffix) {
    // Set the symbols of the decimal format
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator('.');
    symbols.setDecimalSeparator(',');

    // Create the decimal format
    DecimalFormat decimalFormat = new DecimalFormat("#,##0", symbols);
    decimalFormat.setGroupingSize(3);
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setMaximumFractionDigits(1);

    decimalFormat.setPositiveSuffix(aSuffix);
    decimalFormat.setNegativeSuffix(aSuffix);

    return decimalFormat;
  }
}
