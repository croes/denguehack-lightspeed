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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.luciad.contour.ILcdContourBuilder;
import com.luciad.contour.TLcdPolylineContourFinder;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.contour.TLcdXYPolylineContourBuilder;
import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.multidimensional.ILcdDimension;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.iso19103.TLcdISO19103Measure;

class ContourModel extends AbstractContourModel {

  private static final Color[] COLORS = new Color[]{new Color(921890), new Color(6005464), new Color(8306672), new Color(12577534), new Color(10211472), new Color(8699768), new Color(10339963), new Color(12045442), new Color(13555606), new Color(14738590), new Color(15722920), new Color(16704401), new Color(16696399), new Color(16685353), new Color(15495188), new Color(13388802), new Color(10040324), new Color(6694150), new Color(2362626)};

  private MatrixViews fMatrixViews;
  private final TLcdColorMap fColorMap;

  public ContourModel(TLcdNetCDFFilteredModel aTemperatureModel) {
    super(aTemperatureModel);
    fColorMap = createColorMap(getLevels()[0], getLevels()[getLevels().length - 1]);
  }

  protected void createContours(MatrixViews aMatrixViews) {
    fMatrixViews = aMatrixViews;
    for (Map.Entry<MultiDimensionalValue, ILcdMatrixView> matrixViewEntry : fMatrixViews) {
      MultiDimensionalValue multiDimensionalValue = matrixViewEntry.getKey();
      createContours(multiDimensionalValue, matrixViewEntry.getValue(), true);
    }
  }

  public void createContours(MultiDimensionalValue aMultiDimensionalValue) {
    ILcdMatrixView matrixView = createNewInterpolatedMatrixView(aMultiDimensionalValue);
    createContours(aMultiDimensionalValue, matrixView, false);
  }

  private void createContours(MultiDimensionalValue aValue, ILcdMatrixView aMatrixView, boolean aOriginal) {
    TLcdPolylineContourFinder contourFinder = new TLcdPolylineContourFinder();
    ILcdContourBuilder contourBuilder = new TLcdXYPolylineContourBuilder(new ContourBuilderFunction(this, aValue, aOriginal));
    contourFinder.findContours(contourBuilder, aMatrixView, getLevels(), null);
  }

  private static class ContourBuilderFunction implements ILcdFunction {

    private final ContourModel fModel;
    private final MultiDimensionalValue fValue;
    private final boolean fOriginal;

    public ContourBuilderFunction(ContourModel aModel, MultiDimensionalValue aValue, boolean aOriginal) {
      fModel = aModel;
      fValue = aValue;
      fOriginal = aOriginal;
    }

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      TLcdValuedContour contour = (TLcdValuedContour) aObject;
      if (contour.getBounds().isDefined()) {
        try( TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(fModel) ){
          fModel.addElement(new Contour(contour, fValue, fOriginal), ILcdFireEventMode.FIRE_LATER);
        } finally {
          fModel.fireCollectedModelChanges();
        }
      }
      return true;
    }
  }

  private static TLcdColorMap createColorMap(double min, double max) {
    TLcdInterval interval = new TLcdInterval(min, max);
    double range = interval.getMax() - interval.getMin();

    double[] levels = new double[COLORS.length];
    for (int i = 0; i < levels.length; i++) {
      levels[i] = min + i*range/ (COLORS.length - 1);
    }
    return new TLcdColorMap(interval,
                            levels,
                            getSlightlyTransparentVersionOf(COLORS));
  }

  private static Color[] getSlightlyTransparentVersionOf(Color[] aColors) {
    Color[] result = new Color[aColors.length];
    for (int i = 0; i < aColors.length; i++) {
      Color color = aColors[i];
      result[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), 210);
    }
    return result;
  }

  public TLcdColorMap getColorMap() {
    return fColorMap;
  }

  public boolean hasContoursFor(MultiDimensionalValue aMultiDimensionalValue) {
    return fMatrixViews.getMatrixView(aMultiDimensionalValue) != null;
  }

  private ILcdMatrixView createNewInterpolatedMatrixView(MultiDimensionalValue aMultiDimensionalValue) {
    TLcdDimensionAxis<Date> timeAxis = getTimeAxis();
    TLcdDimensionAxis<TLcdISO19103Measure> depthAxis = getPressureAxis();

    InterpolatedAxisValue interpolatedTimeValue = aMultiDimensionalValue.getAxisValue(timeAxis);
    InterpolatedAxisValue interpolatedDepthValue = aMultiDimensionalValue.getAxisValue(depthAxis);

    //Interpolate
    MultiDimensionalValue value1 = new MultiDimensionalValue(new InterpolatedAxisValue(interpolatedTimeValue.getFirstValue()), new InterpolatedAxisValue(interpolatedDepthValue.getFirstValue()));
    MultiDimensionalValue value2 = new MultiDimensionalValue(new InterpolatedAxisValue(interpolatedTimeValue.getFirstValue()), new InterpolatedAxisValue(interpolatedDepthValue.getSecondValue()));
    MultiDimensionalValue value3 = new MultiDimensionalValue(new InterpolatedAxisValue(interpolatedTimeValue.getSecondValue()), new InterpolatedAxisValue(interpolatedDepthValue.getFirstValue()));
    MultiDimensionalValue value4 = new MultiDimensionalValue(new InterpolatedAxisValue(interpolatedTimeValue.getSecondValue()), new InterpolatedAxisValue(interpolatedDepthValue.getSecondValue()));

    ILcdMatrixView matrixView1 = fMatrixViews.getMatrixView(value1);
    ILcdMatrixView matrixView2 = fMatrixViews.getMatrixView(value2);
    ILcdMatrixView matrixView3 = fMatrixViews.getMatrixView(value3);
    ILcdMatrixView matrixView4 = fMatrixViews.getMatrixView(value4);

    InterpolatedMatrixView firstBlend = new InterpolatedMatrixView(matrixView1, matrixView2, interpolatedDepthValue.getBlendFactor());
    InterpolatedMatrixView secondBlend = new InterpolatedMatrixView(matrixView3, matrixView4, interpolatedDepthValue.getBlendFactor());
    InterpolatedMatrixView finalBlend = new InterpolatedMatrixView(firstBlend, secondBlend, interpolatedTimeValue.getBlendFactor());

    fMatrixViews.addMatrixView(aMultiDimensionalValue, finalBlend);

    return finalBlend;
  }

  private TLcdDimensionAxis<Date> getTimeAxis() {
    return WeatherUtil.getTimeAxis(getModel());
  }

  private TLcdDimensionAxis<TLcdISO19103Measure> getPressureAxis() {
    return WeatherUtil.getPressureAxis(getModel());
  }

  MultiDimensionalValue createMultiDimensionalValueForCurrentFilterParameters() {
    Collection<InterpolatedAxisValue> axisValues = new ArrayList<>();
    TLcdNetCDFFilteredModel model = getModel();
    TLcdDimensionFilter filter = model.getDimensionFilter();
    for (ILcdDimension<?> dimension : model.getDimensions()) {
      TLcdDimensionAxis<?> axis = dimension.getAxis();
      axisValues.add(new InterpolatedAxisValue(axis, filter.getInterval(axis)));
    }
    return new MultiDimensionalValue(axisValues);
  }
}
