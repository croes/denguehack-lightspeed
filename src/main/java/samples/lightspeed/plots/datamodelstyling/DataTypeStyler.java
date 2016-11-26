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
package samples.lightspeed.plots.datamodelstyling;

import static java.util.Arrays.asList;

import static com.luciad.util.expression.TLcdExpressionFactory.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.util.expression.ILcdParameter;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Generates {@link TLspPlotStyle}s for {@link TLcdDataType} properties.
 * <p>
 * See {@link #updateStyle} for the actual creation of a {@link TLspPlotStyle}.
 * </p>
 * <p>
 * Can work with data properties that have:
 * <ul>
 *   <li>{@link EnumAnnotation}</li>: discrete set of values
 *   <li>{@link RangeAnnotation}</li>: continuous set of numbers
 * </ul>
 * </p>
 */
public class DataTypeStyler extends ALspStyler {

  private static final int ICON_SIZE = 13;

  private final TLcdDataType fDataType;
  private final Map<TLcdDataProperty, ILcdExpression<Float>> fAttributes = new HashMap<TLcdDataProperty, ILcdExpression<Float>>();
  private final Map<String, ILcdParameter<Float>> fParameters = new HashMap<String, ILcdParameter<Float>>();

  private Collection<TLcdDataProperty> fVisibilityProperties = Collections.emptyList();
  private TLcdDataProperty fColorProperty = null;
  private TLcdDataProperty fIconProperty = null;

  private TLspPlotStyle fStyle = TLspPlotStyle.newBuilder().density(true).build();
  private boolean fUseDensity = false;

  public DataTypeStyler(TLcdDataType aDataType) {
    fDataType = aDataType;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).styles(fStyle).submit();
  }

  /**
   * Configure a visible range for a {@link TLcdDataProperty} that has a {@link RangeAnnotation}.
   * Elements will be visible if they are in [aMinValue..aMaxValue].
   */
  public void setRangeParameters(TLcdDataProperty aProperty, double aMinValue, double aMaxValue) {
    RangeAnnotation rangeAnnotation = aProperty.getAnnotation(RangeAnnotation.class);
    double min = rangeAnnotation.getLowerBound().doubleValue();
    double max = rangeAnnotation.getUpperBound().doubleValue();

    getParameter(aProperty, "min").setValue((float) ((aMinValue - min) / (max - min)));
    getParameter(aProperty, "max").setValue((float) ((aMaxValue - min) / (max - min)));
  }

  /**
   * @return The currently configured minimum value for the property.
   */
  public double getRangeMinParameter(TLcdDataProperty aProperty) {
    RangeAnnotation rangeAnnotation = aProperty.getAnnotation(RangeAnnotation.class);
    double min = rangeAnnotation.getLowerBound().doubleValue();
    double max = rangeAnnotation.getUpperBound().doubleValue();

    float value = getParameter(aProperty, "min").getValue();

    return min + ((max - min) * value);
  }

  /**
   * @return The currently configured maximum value for the property.
   */
  public double getRangeMaxParameter(TLcdDataProperty aProperty) {
    RangeAnnotation rangeAnnotation = aProperty.getAnnotation(RangeAnnotation.class);
    double min = rangeAnnotation.getLowerBound().doubleValue();
    double max = rangeAnnotation.getUpperBound().doubleValue();

    float value = getParameter(aProperty, "max").getValue();

    return min + ((max - min) * value);
  }

  /**
   * Configure the visible value for a {@link TLcdDataProperty} that has a {@link EnumAnnotation}.
   * Elements will be visible if they are equal to aValue.
   */
  public void setEnumParameter(TLcdDataProperty aProperty, Object aValue) {
    EnumAnnotation enumAnnotation = aProperty.getAnnotation(EnumAnnotation.class);
    int index = enumAnnotation.indexOf(aValue);

    getParameter(aProperty, "").setValue((float) index);
  }

  /**
   * Sets the properties to use for visibility filtering.
   */
  public void setVisibilityProperties(TLcdDataProperty... aProperties) {
    List<TLcdDataProperty> newProperties = asList(aProperties);
    if (!fVisibilityProperties.equals(newProperties)) {
      fVisibilityProperties = newProperties;
      updateStyle();
    }
  }

  /**
   * Sets the property used to color the icons.
   */
  public void setColorProperty(TLcdDataProperty aProperty) {
    fColorProperty = aProperty;
    updateStyle();
  }

  /**
   * @return The property used to color icons
   */
  public TLcdDataProperty getColorProperty() {
    return fColorProperty;
  }

  /**
   * Sets the property used to determine the icon content
   */
  public void setIconProperty(TLcdDataProperty aProperty) {
    fIconProperty = aProperty;
    updateStyle();
  }

  /**
   * @return The property used to determine icon content
   */
  public TLcdDataProperty getIconProperty() {
    return fIconProperty;
  }

  /**
   * Enable of disable density painting.
   * When enabled, other styling properties will be ignored, but visibility filtering will not.
   */
  public void setUseDensity(boolean aUseDensity) {
    boolean old = fUseDensity;
    fUseDensity = aUseDensity;
    if (old != fUseDensity) {
      updateStyle();
    }
  }

  /**
   * @return Whether density painting is enabled or not
   */
  public boolean isUseDensity() {
    return fUseDensity;
  }

  /**
   * Creates a new {@link TLspPlotStyle} based on the visibility, color and icon settings.
   */
  private void updateStyle() {
    ILcdExpression<Boolean> visibility = constant(true);
    for (TLcdDataProperty property : fVisibilityProperties) {
      if (property.isAnnotationPresent(EnumAnnotation.class)) {
        visibility = and(visibility, eq(getAttribute(property), getParameter(property, "")));
      } else if (property.isAnnotationPresent(RangeAnnotation.class)) {
        visibility = and(visibility, between(getAttribute(property), getParameter(property, "min"), getParameter(property, "max")));
      }
    }

    ILcdExpression<Color> color = constant(Color.lightGray.brighter());
    if (fColorProperty != null && fColorProperty.isAnnotationPresent(RangeAnnotation.class)) {
      color = mix(constant(Color.orange), constant(Color.cyan), fraction(getAttribute(fColorProperty), getParameter(fColorProperty, "min"), getParameter(fColorProperty, "max")));
    }
    if (fColorProperty != null && fColorProperty.isAnnotationPresent(EnumAnnotation.class)) {
      EnumAnnotation enumAnnotation = fColorProperty.getAnnotation(EnumAnnotation.class);
      color = map(getAttribute(fColorProperty), getColors(enumAnnotation.size()), Color.red);
    }

    ILcdExpression<ILcdIcon> icon = null; // by default, dots are used
    if (fIconProperty != null && fIconProperty.isAnnotationPresent(EnumAnnotation.class)) {
      EnumAnnotation enumAnnotation = fIconProperty.getAnnotation(EnumAnnotation.class);
      icon = map(getAttribute(fIconProperty), getIcons(enumAnnotation.size()), new TLcdImageIcon());
    }

    if (!fUseDensity) {
      fStyle = TLspPlotStyle.newBuilder()
                            .visibility(visibility)
                            .modulationColor(color)
                            .icon(icon)
                            .automaticScaling(500)
                            .build();
    } else {

      fStyle = TLspPlotStyle.newBuilder()
                            .visibility(visibility)
                            .density(true)
                            .scale(1.5f)
                            .automaticScaling(500)
                            .build();
    }

    fireStyleChangeEvent();
  }

  /**
   * Generates a set of different colors.
   */
  public Color[] getColors(int aCount) {
    Random random = new Random(123456);
    Color[] colors = new Color[aCount];
    for (int i = 0; i < colors.length; i++) {
      colors[i] = new Color(Color.HSBtoRGB(random.nextFloat(), 1f, 1f));
    }

    return colors;
  }

  /**
   * Generates a set of different icons.
   */
  public ILcdIcon[] getIcons(int aCount) {
    ILcdIcon[] icons = new ILcdIcon[aCount];
    for (int i = 0; i < icons.length; i++) {
      icons[i] = new TLcdSymbol(i % TLcdSymbol.N_ICON, ICON_SIZE, Color.white, Color.white);
    }

    return icons;
  }

  private ILcdParameter<Float> getParameter(TLcdDataProperty aProperty, String aSuffix) {
    String name = aProperty.getName() + aSuffix;

    ILcdParameter<Float> parameter = fParameters.get(name);
    if (parameter == null) {
      parameter = parameter(name, 0f);
      fParameters.put(name, parameter);
    }
    return parameter;
  }

  private ILcdExpression<Float> getAttribute(TLcdDataProperty aProperty) {
    ILcdExpression<Float> attribute = fAttributes.get(aProperty);
    if (attribute == null) {
      if (aProperty.isAnnotationPresent(EnumAnnotation.class)) {
        attribute = attribute(aProperty.getName(), Float.class, new EnumAttributeProvider(aProperty));
      } else if (aProperty.isAnnotationPresent(RangeAnnotation.class)) {
        attribute = attribute(aProperty.getName(), Float.class, new RangeAttributeProvider(aProperty));
      } else {
        return null;
      }
      fAttributes.put(aProperty, attribute);
    }
    return attribute;
  }

  /**
   * @return All attributes used by this styler.  This can be used to force pre-loading these attributes using {@link com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder#mandatoryAttributes(ILcdExpression[])}
   */
  public ILcdExpression[] getAttributes() {
    Collection<ILcdExpression> attributes = new ArrayList<ILcdExpression>();
    for (TLcdDataProperty property : fDataType.getProperties()) {
      ILcdExpression<Float> attribute = getAttribute(property);
      if (attribute != null) {
        attributes.add(attribute);
      }
    }
    return attributes.toArray(new ILcdExpression[attributes.size()]);
  }

  public TLspPlotStyle getStyle() {
    return fStyle;
  }
}
