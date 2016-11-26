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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Enumeration;

import com.luciad.contour.TLcdIntervalContour;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.shape.TLcdFeaturedShapeList;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.util.TLcdInterval;
import com.luciad.util.iso19103.TLcdISO19103Measure;

import samples.lightspeed.demo.application.data.weather.IcingModel.IcingSLDModel;
import samples.lightspeed.demo.application.data.weather.IcingModel.IcingSeverityModel;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;

public class WeatherIcingModelFactory extends AbstractModelFactory {

  private static final String TIME = "time";
  private static final String ALTITUDE = "altitude";
  private static final String PROBABILITY = "prob";
  private static final String CONTOUR_INTERVAL_MIN = "min";
  private static final String CONTOUR_INTERVAL_MAX = "max";

  public WeatherIcingModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    Path sourcePath = Paths.get(aSource);
    String parentPath = sourcePath.toAbsolutePath().toString() + File.separator;

    IcingSeverityModel readIcingSeverityModel = createIcingSeverityModel(parentPath + "severityshapes.shp");
    IcingSLDModel readIcingSLDModel = createIcingSLDModel(parentPath + "sldshapes.shp");

    return new IcingModel(readIcingSeverityModel, readIcingSLDModel);
  }

  private IcingSeverityModel createIcingSeverityModel(String aDestination) throws IOException {
    ILcdModel shpModel = new TLcdSHPModelDecoder().decode(aDestination);

    TLcdDimensionAxis<Date> timeAxis = TLcdDimensionAxis.TIME_AXIS;
    TLcdDimensionAxis<TLcdISO19103Measure> altitudeAxis = WeatherUtil.createAltitudeAxis();

    ILcdFeaturedDescriptor modelDescriptor = (ILcdFeaturedDescriptor) shpModel.getModelDescriptor();

    Enumeration<TLcdFeaturedShapeList> elements = shpModel.elements();
    IcingSeverityModel icingSeverityModel = new IcingSeverityModel();
    icingSeverityModel.setModelReference(shpModel.getModelReference());

    while (elements.hasMoreElements()) {
      TLcdFeaturedShapeList element = elements.nextElement();

      Date date = getDate(modelDescriptor, element);
      double altitude = getAltitude(modelDescriptor, element);
      double probability = getProbability(modelDescriptor, element);
      double min = getMin(modelDescriptor, element);
      double max = getMax(modelDescriptor, element);

      MultiDimensionalValue multiDimensionalValue = createMultiDimensionalValue(timeAxis, altitudeAxis, date, altitude);
      IcingSeverityContour icingSeverityContour = new IcingSeverityContour(createContour(element, min, max), multiDimensionalValue, probability);
      icingSeverityModel.addElement(icingSeverityContour, ILcdModel.FIRE_NOW);
    }

    return icingSeverityModel;
  }

  private IcingSLDModel createIcingSLDModel(String aDestination) throws IOException {
    ILcdModel shpModel = new TLcdSHPModelDecoder().decode(aDestination);
    TLcdDimensionAxis<Date> timeAxis = TLcdDimensionAxis.TIME_AXIS;
    TLcdDimensionAxis<TLcdISO19103Measure> altitudeAxis = WeatherUtil.createAltitudeAxis();

    ILcdFeaturedDescriptor modelDescriptor = (ILcdFeaturedDescriptor) shpModel.getModelDescriptor();

    Enumeration<TLcdFeaturedShapeList> elements = shpModel.elements();
    IcingSLDModel icingSLDModel = new IcingSLDModel();
    icingSLDModel.setModelReference(shpModel.getModelReference());
    while (elements.hasMoreElements()) {
      TLcdFeaturedShapeList element = elements.nextElement();
      Date date = getDate(modelDescriptor, element);
      double altitude = getAltitude(modelDescriptor, element);
      double min = getMin(modelDescriptor, element);
      double max = getMax(modelDescriptor, element);

      MultiDimensionalValue multiDimensionalValue = createMultiDimensionalValue(timeAxis, altitudeAxis, date, altitude);
      IntervalContour intervalContour = new IntervalContour(createContour(element, min, max), multiDimensionalValue);
      icingSLDModel.addElement(intervalContour, ILcdModel.FIRE_NOW);
    }

    return icingSLDModel;
  }

  private Date getDate(ILcdFeaturedDescriptor aModelDescriptor, TLcdFeaturedShapeList aContour) {
    int timeIndex = aModelDescriptor.getFeatureIndex(TIME);
    return new Date(Long.valueOf((String) aContour.getFeature(timeIndex)));
  }

  private double getAltitude(ILcdFeaturedDescriptor aModelDescriptor, TLcdFeaturedShapeList aContour) {
    int altitudeIndex = aModelDescriptor.getFeatureIndex(ALTITUDE);
    return (double) aContour.getFeature(altitudeIndex);
  }

  private double getProbability(ILcdFeaturedDescriptor aModelDescriptor, TLcdFeaturedShapeList aContour) {
    int probabilityIndex = aModelDescriptor.getFeatureIndex(PROBABILITY);
    return (double) aContour.getFeature(probabilityIndex);
  }

  private double getMin(ILcdFeaturedDescriptor aModelDescriptor, TLcdFeaturedShapeList aContour) {
    int contourIntervalMinIndex = aModelDescriptor.getFeatureIndex(CONTOUR_INTERVAL_MIN);
    return Double.valueOf((String) aContour.getFeature(contourIntervalMinIndex));
  }

  private double getMax(ILcdFeaturedDescriptor aModelDescriptor, TLcdFeaturedShapeList aContour) {
    int contourIntervalMaxIndex = aModelDescriptor.getFeatureIndex(CONTOUR_INTERVAL_MAX);
    return Double.valueOf((String) aContour.getFeature(contourIntervalMaxIndex));
  }

  private MultiDimensionalValue createMultiDimensionalValue(TLcdDimensionAxis<Date> aTimeAxis, TLcdDimensionAxis<TLcdISO19103Measure> aAltitudeAxis, Date aDate, double aAltitude) {
    return new MultiDimensionalValue(new InterpolatedAxisValue(aTimeAxis, TLcdDimensionInterval.createSingleValue(Date.class, aDate)),
                                     new InterpolatedAxisValue(aAltitudeAxis, TLcdDimensionInterval.createSingleValue(TLcdISO19103Measure.class, new TLcdISO19103Measure(aAltitude, aAltitudeAxis.getUnit()))));
  }

  private TLcdIntervalContour createContour(TLcdFeaturedShapeList aShapeList, double aMin, double aMax) {
    return new TLcdIntervalContour(aShapeList.getShape(0), new TLcdInterval(aMin, aMax));
  }

}
