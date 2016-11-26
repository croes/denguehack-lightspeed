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
package samples.lightspeed.demo.application.data.sassc;

import static com.luciad.util.expression.TLcdExpressionFactory.*;

import java.awt.Color;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.gui.ILcdIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.util.expression.ILcdParameter;
import com.luciad.util.expression.TLcdExpressionFactory;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.application.data.sassc.icons.SASSCIcon;
import samples.lightspeed.demo.application.data.sassc.icons.SymbolsNames;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.ASasRecord;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.ASasSensor;

/**
 * Styler for Eurocontrol SASS-C data.
 */
class SassCStyler extends ALspStyler {

//  private static final String INITIAL_SELECTED_SENSOR = "*";

  private final SassCStyleConfig fConfig;
  private SoftReference<SassCModelDescriptor> fModelDescriptorRef;
  private final EnumMap<SassCAttribute, Attribute> fAttributes =
      new EnumMap<SassCAttribute, Attribute>(SassCAttribute.class);

  private SassCAttribute fColorAttribute;

  private ALspStyle fStyle;

  private boolean fPaintDensity = false;

  SassCStyler(SassCStyleConfig aConfig) {
    fConfig = aConfig;
    fAttributes.put(
        SassCAttribute.TIME,
        new ContinuousAttribute(
            SassCAttribute.TIME,
            attribute("Time", Float.class, new TimeAttribute(getMinTime(), getMaxTime())),
            0.f, 1.f
        )
    );
    fAttributes.put(
        SassCAttribute.HEIGHT,
        new ContinuousAttribute(
            SassCAttribute.HEIGHT,
            attribute("Height", Float.class, new HeightAttribute()),
            (float) getMinHeight(), (float) getMaxHeight()
        )
    );
    TLcdArrayList<ASasSensor> sensorsList = new TLcdArrayList<ASasSensor>();
    fAttributes.put(
        SassCAttribute.SENSOR,
        new DiscreteAttribute<ASasSensor>(
            SassCAttribute.SENSOR,
            attribute("Sensor", Float.class, new SensorAttribute(Collections.<ASasSensor>emptyList())),
            sensorsList
        )
    );
    if (!getSupportedColorAttributes().isEmpty()) {
      fColorAttribute = getSupportedColorAttributes().get(0);
    }
  }

  public boolean isPaintDensity() {
    return fPaintDensity;
  }

  public void setPaintDensity(boolean aPaintDensity) {
    fPaintDensity = aPaintDensity;
    fStyle = null;
    fireStyleChangeEvent();
  }

  public List<SassCAttribute> getSupportedColorAttributes() {
    return fConfig.getSupportedColorAttributes();
  }

  public SassCAttribute getColorAttribute() {
    return fColorAttribute;
  }

  public void setColorAttribute(SassCAttribute aColorAttribute) {
    fColorAttribute = aColorAttribute;
    invalidateStyle();
  }

  public long getMinTime() {
    return (long) fConfig.getFilterInterval(SassCAttribute.TIME).getMin();
  }

  public long getMaxTime() {
    return (long) fConfig.getFilterInterval(SassCAttribute.TIME).getMax();
  }

  public double getTimeRangeMinimum() {
    return getMinTime() +
           getContinuousAttribute(SassCAttribute.TIME).getMinParam().getValue() *
           (getMaxTime() - getMinTime());
  }

  public double getTimeRangeMaximum() {
    return getMinTime() +
           getContinuousAttribute(SassCAttribute.TIME).getMaxParam().getValue() *
           (getMaxTime() - getMinTime());
  }

  public void setTimeRange(double aMin, double aMax) {
    float normalizedMin = (float) ((aMin - getMinTime()) / (getMaxTime() - getMinTime()));
    float normalizedMax = (float) ((aMax - getMinTime()) / (getMaxTime() - getMinTime()));
    getContinuousAttribute(SassCAttribute.TIME).setRange(normalizedMin, normalizedMax);
  }

  public double getMinHeight() {
    return (long) fConfig.getFilterInterval(SassCAttribute.HEIGHT).getMin();
  }

  public double getMaxHeight() {
    return (long) fConfig.getFilterInterval(SassCAttribute.HEIGHT).getMax();
  }

  public double getHeightRangeMinimum() {
    return getContinuousAttribute(SassCAttribute.HEIGHT).getMinParam().getValue();
  }

  public double getHeightRangeMaximum() {
    return getContinuousAttribute(SassCAttribute.HEIGHT).getMaxParam().getValue();
  }

  public void setHeightRange(double aMinTime, double aMaxTime) {
    getContinuousAttribute(SassCAttribute.HEIGHT).setRange((float) aMinTime, (float) aMaxTime);
  }

  public ILcdList<ASasSensor> getSensors() {
    return getDiscreteAttribute(SassCAttribute.SENSOR).getValues();
  }

  public void setSelectedSensor(ASasSensor aSensor) {
    getDiscreteAttribute(SassCAttribute.SENSOR).setValue(aSensor);
  }

  public ASasSensor getSelectedSensor() {
    return (ASasSensor) getDiscreteAttribute(SassCAttribute.SENSOR).getValue();
  }

  private ContinuousAttribute getContinuousAttribute(SassCAttribute aAttribute) {
    return (ContinuousAttribute) fAttributes.get(aAttribute);
  }

  private DiscreteAttribute getDiscreteAttribute(SassCAttribute aAttribute) {
    return (DiscreteAttribute) fAttributes.get(aAttribute);
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).style(getStyle()).submit();
  }

  public synchronized void init(ILcdModel aModel) {
    SassCModelDescriptor actualMd = (SassCModelDescriptor) aModel.getModelDescriptor();
    if (fModelDescriptorRef != null && fModelDescriptorRef.get() == actualMd) {
      return; // ok
    }

    DiscreteAttribute sensorAttr = getDiscreteAttribute(SassCAttribute.SENSOR);
    sensorAttr.getValues().addAll(actualMd.getSensors());
    sensorAttr.setObjectAttribute(
        attribute("Sensor", Float.class, new SensorAttribute(actualMd.getSensors()))
    );

    // Configure initial style
//    for(ASasSensor sensor : actualMd.getSensors()) {
//      if(sensor.getName().matches(INITIAL_SELECTED_SENSOR)) {
//        setSelectedSensor(sensor);
//        break;
//      }
//    }
    setColorAttribute(SassCAttribute.SENSOR);
    setHeightRange(getMinHeight(), getMaxHeight() * 0.3);

    fModelDescriptorRef = new SoftReference<SassCModelDescriptor>(actualMd);
  }

  private synchronized ALspStyle getStyle() {
    if (fStyle == null) {
      TLspPlotStyle.Builder<?> builder = TLspPlotStyle.newBuilder();
      if (!fPaintDensity) {
        builder.icon(getIconMap(getDiscreteAttribute(SassCAttribute.SENSOR)));
      } else {
        builder.density(true);
      }
      builder.automaticScaling(500);
      ILcdExpression<Boolean> visExpr = null;
      for (Attribute attr : fAttributes.values()) {
        ILcdExpression<Boolean> expr = attr.getVisibleExpression();
        if (visExpr == null) {
          visExpr = expr;
        } else {
          visExpr = and(visExpr, expr);
        }
      }
      if (visExpr != null) {
        builder.visibility(visExpr);
      }
      if (fColorAttribute != null) {
        Attribute attr = fAttributes.get(fColorAttribute);
        List<Color> colors = fConfig.getColors(fColorAttribute);
        ILcdExpression<Float> normalizedExpr;
        if (attr instanceof ContinuousAttribute) {
          ContinuousAttribute cAttr = (ContinuousAttribute) attr;
          normalizedExpr = div(sub(attr.getObjectAttribute(), cAttr.getMinParam()), sub(cAttr.getMaxParam(), cAttr.getMinParam()));
        } else {
          DiscreteAttribute<?> dAttr = (DiscreteAttribute) attr;
          normalizedExpr = div(dAttr.getObjectAttribute(), (float) dAttr.getIndexCount() - 1);
        }
        CaseExpression<Color> colorExpr = cases(colors.get(colors.size() - 1));
        for (int i = 0; i < colors.size() - 1; i++) {
          Color c1 = colors.get(i);
          Color c2 = colors.get(i + 1);
          float min = (float) i / (float) (colors.size() - 1);
          float max = (float) (i + 1) / (float) (colors.size() - 1);
          colorExpr = colorExpr.when(
              lt(normalizedExpr, max),
              mix(constant(c1), constant(c2), min(max(mul(sub(normalizedExpr, min), max - min), 0.f), 1.f))
          );
        }
        builder.modulationColor(colorExpr);
      }
      fStyle = builder.build();
    }
    return fStyle;
  }

  private ILcdExpression<ILcdIcon> getIconMap(DiscreteAttribute aAttribute) {
    SymbolsNames[] symbols = SymbolsNames.values();
    ILcdIcon[] icons = new ILcdIcon[aAttribute.getIndexCount()];
    for (int i = 0; i < icons.length; i++) {
      icons[i] = SASSCIcon.createIcon2(symbols[i % symbols.length].getName(), 10, Color.white);
    }

    return map(aAttribute.getObjectAttribute(), icons, icons[0]);
  }

  private void invalidateStyle() {
    fStyle = null;
    fireStyleChangeEvent();
  }

  /**
   * The time attribute, normalized in {@code [0, 1]}.
   */
  private static class TimeAttribute implements AttributeValueProvider<Float> {
    private final long fMinTime;
    private final long fMaxTime;

    public TimeAttribute(long aMinTime, long aMaxTime) {
      fMinTime = aMinTime;
      fMaxTime = aMaxTime;
    }

    @Override
    public Float getValue(Object aDomainObject, Object aGeometry) {
      ASasRecord record = (ASasRecord) aDomainObject;
      return (float) (record.getTimeOfDetection() - fMinTime) /
             (float) (fMaxTime - fMinTime);
    }
  }

  /**
   * The height attribute.
   */
  private static class HeightAttribute implements AttributeValueProvider<Float> {
    @Override
    public Float getValue(Object aDomainObject, Object aGeometry) {
      return (float) ((ASasRecord) aDomainObject).getZ();
    }
  }

  /**
   * The sensor attribute.
   */
  private static class SensorAttribute implements AttributeValueProvider<Float> {

    private final Map<Integer, Float> fMapping;

    public SensorAttribute(List<ASasSensor> aSensors) {
      fMapping = new HashMap<Integer, Float>();
      for (ASasSensor sensor : aSensors) {
        fMapping.put(sensor.getSensorId(), (float) fMapping.size());
      }
    }

    @Override
    public Float getValue(Object aDomainObject, Object aGeometry) {
      Float sensorId = fMapping.get(((ASasRecord) aDomainObject).getDataSourceId());
      if (sensorId == null) {
        sensorId = DiscreteAttribute.UNKNOWN_VALUE;
      }
      return sensorId;
    }

    public int size() {
      return fMapping.size();
    }
  }

  private static interface Attribute {
    public ILcdExpression<Float> getObjectAttribute();

    public ILcdExpression<Boolean> getVisibleExpression();
  }

  /**
   * A continuous attribute in a specific range.
   */
  private static class ContinuousAttribute implements Attribute {
    private final ILcdExpression<Float> fObjectAttribute;
    private final ILcdParameter<Float> fMin;
    private final ILcdParameter<Float> fMax;

    private ContinuousAttribute(SassCAttribute aAttribute, ILcdExpression<Float> aObjectAttribute, float aMin, float aMax) {
      fObjectAttribute = aObjectAttribute;
      fMin = parameter("min_" + aAttribute.getName(), aMin);
      fMax = parameter("max_" + aAttribute.getName(), aMax);
    }

    public ILcdExpression<Float> getObjectAttribute() {
      return fObjectAttribute;
    }

    public ILcdParameter<Float> getMinParam() {
      return fMin;
    }

    public ILcdParameter<Float> getMaxParam() {
      return fMax;
    }

    public void setRange(float aMin, float aMax) {
      fMin.setValue(aMin);
      fMax.setValue(aMax);
    }

    public ILcdExpression<Boolean> getVisibleExpression() {
      return between(getObjectAttribute(), getMinParam(), getMaxParam());
    }
  }

  /**
   * A discrete attribute with a limited number of values.
   */
  private static class DiscreteAttribute<T> implements Attribute {
    private static final float UNKNOWN_VALUE = -1f;

    private ILcdExpression<Float> fObjectAttribute;
    private final ILcdList<T> fValues;
    private final ILcdParameter<Float> fValue;

    private DiscreteAttribute(SassCAttribute aAttribute, ILcdExpression<Float> aObjectAttribute, ILcdList<T> aValues) {
      fObjectAttribute = aObjectAttribute;
      fValues = aValues;
      fValue = parameter("value_" + aAttribute.getName(), UNKNOWN_VALUE);
    }

    public ILcdExpression<Float> getObjectAttribute() {
      return fObjectAttribute;
    }

    public void setObjectAttribute(ILcdExpression<Float> aObjectAttribute) {
      fObjectAttribute = aObjectAttribute;
    }

    @Override
    public ILcdExpression<Boolean> getVisibleExpression() {
      return ifThenElse(lt(fValue, 0.f), constant(true), eq(fObjectAttribute, fValue));
    }

    public ILcdParameter<Float> getValueParam() {
      return fValue;
    }

    public ILcdList<T> getValues() {
      return fValues;
    }

    public T getValue() {
      int index = getIndex();
      return index == -1 ? null : fValues.get(index);
    }

    public void setValue(T aValue) {
      setIndex(fValues.indexOf(aValue));
    }

    public void setIndex(int aIndex) {
      fValue.setValue((float) aIndex);
    }

    public int getIndex() {
      return fValue.getValue().intValue();
    }

    public int getIndexCount() {
      return fValues.size();
    }
  }
}
