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
package samples.lightspeed.lidar;

import static java.awt.Color.black;
import static java.awt.Color.white;

import static com.luciad.util.expression.TLcdExpressionFactory.*;
import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;

import java.awt.Color;
import java.util.Collection;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.las.TLcdLASModelDescriptor;
import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.lidar.lightspeed.TLspLIDARLayerBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Styler for LAS data that supports various modes.
 * <p>
 * The following styling options are available if the layer supports it:
 * <ul>
 *   <li><b>Color</b>: uses the color information available in the file.</li>
 *   <li><b>Height</b>: shows a color gradient over the combined height range of all layers.</li>
 *   <li><b>Classification</b>: shows a different color per class.</li>
 *   <li><b>Intensity</b>: shows greyscale gradient based on the intensity available in the file.</li>
 *   <li><b>Infrared</b>: shows a color gradient based on the infrared information available in the file.</li>
 * </ul>
 * </p>
 *
 * @since 2014.0
 */
public class LASStyler extends ALspStyler {

  // All the .las properties that this styler can use for styling.
  public static final String[] SUPPORTED_PROPERTIES = new String[]{TLcdLASModelDescriptor.COLOR,
                                                                   TLcdLASModelDescriptor.CLASSIFICATION,
                                                                   TLcdLASModelDescriptor.INTENSITY,
                                                                   TLcdLASModelDescriptor.HEIGHT,
                                                                   TLcdLASModelDescriptor.INFRARED};

  private static final Color[] HEIGHT_COLORS = TLcdDTEDColorModelFactory.getDefaultColors();

  private static final Color[] INFRARED_COLORS = {
      Color.BLACK,
      new Color(14, 1, 119),
      new Color(179, 7, 151),
      new Color(233, 97, 3),
      new Color(254, 227, 56),
      Color.WHITE,
  };

  private static final Color DEFAULT_CLASSIFICATION_COLOR = white;
  private static final Color[] CLASSIFICATION_COLORS = {
      DEFAULT_CLASSIFICATION_COLOR, // Created, never classified
      new Color(192, 192, 192), // Unclassified
      new Color(128, 80, 0), // Ground
      new Color(128, 240, 64), // Low vegetation
      new Color(96, 192, 32), // Medium Vegetation
      new Color(64, 128, 0), // High Vegetation
      new Color(128, 128, 158), // Building
      new Color(192, 64, 32), // Low Point (noise)
      new Color(192, 192, 32), // Model Key-point (mass point)
      new Color(32, 64, 192) // Water
  };

  private final TLcdLASModelDescriptor fModelDescriptor;
  private final TLspPlotStyle fDefaultStyle;
  private TLspPlotStyle fStyle;

  public LASStyler(TLcdLASModelDescriptor aModelDescriptor, TLspPlotStyle aDefaultStyle) {
    fModelDescriptor = aModelDescriptor;
    fDefaultStyle = aDefaultStyle;
    fStyle = fDefaultStyle;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).styles(fStyle).submit();
  }

  /**
   * Indicates whether the given property (name) can be used for styling for the layer
   * associated with this styler.
   * <p>
   * <ul>
   *   <li>The property must be one of {@link #SUPPORTED_PROPERTIES}.</li>
   *   <li>A property can be used for styling if the model associated with the layer has this property and has sensible data for it.</li>
   * </ul>
   * </p>
   *
   * @param aProperty A property name (see {@link TLcdLASModelDescriptor}).
   * @return {@code true} if the layer has data to use this property as style, {@code false} otherwise.
   */
  public boolean canUseStyleProperty(String aProperty) {
    return getColorExpression(aProperty, new TLcdInterval(0, 1)) != null;
  }

  /**
   * Use the given property for styling.
   * <p>
   * See {@link #canUseStyleProperty} to verify if a property is supported by this styler/layer:
   * <ul>
   *   <li>The property must be one of {@link #SUPPORTED_PROPERTIES}.</li>
   *   <li>The property can be null to get default styling.</li>
   * </ul>
   * </p>
   *
   * @param aProperty A property name (see {@link TLcdLASModelDescriptor}) to use for styling
   * @param aHeightInterval The height range of all .las layers combined.
   */
  public void setStyleProperty(String aProperty, ILcdInterval aHeightInterval) {
    ILcdExpression<Color> color = getColorExpression(aProperty, aHeightInterval);

    //if aProperty is not a supported property, use Height styling which is always available.
    fStyle = fDefaultStyle.asBuilder()
                          .modulationColor(color == null ? getColorExpression(TLcdLASModelDescriptor.HEIGHT, aHeightInterval) : color)
                          .build();

    fireStyleChangeEvent();
  }

  /**
   * Determines the color {@link ILcdExpression expression} that will be used for styling.
   * @return A color expression, or {@code null} if the property cannot be used by this styler/layer.
   */
  private ILcdExpression<Color> getColorExpression(String aProperty, ILcdInterval aHeightInterval) {
    TLcdDataProperty dataProperty = fModelDescriptor.getDataType().getDeclaredProperty(aProperty);

    if (dataProperty == null) {
      return null;
    }

    ILcdInterval range = fModelDescriptor.getPropertyRange(dataProperty);

    if (aProperty.equals(TLcdLASModelDescriptor.COLOR)) {
      ILcdExpression<Color> color = attribute(Color.class, dataProperty);
      return color;
    } else if (aProperty.equals(TLcdLASModelDescriptor.HEIGHT)) {
      ILcdExpression<Float> height = attribute(Float.class, dataProperty);
      ILcdExpression<Float> fraction = fraction(height, (float) aHeightInterval.getMin(), (float) aHeightInterval.getMax());
      return mixmap(fraction, HEIGHT_COLORS);
    } else if (aProperty.equals(TLcdLASModelDescriptor.CLASSIFICATION)) {
      if (range.getMin() == range.getMax()) {
        return null;
      }
      ILcdExpression<Byte> classification = attribute(Byte.class, dataProperty);
      return map(classification, CLASSIFICATION_COLORS, DEFAULT_CLASSIFICATION_COLOR);
    } else if (aProperty.equals(TLcdLASModelDescriptor.INTENSITY)) {
      if (range.getMin() == range.getMax()) {
        return null;
      }
      ILcdExpression intensity = attribute(Integer.class, dataProperty);
      ILcdExpression fraction = fraction(intensity, range.getMin(), range.getMax());
      return mix(black, white, fraction);
    } else if (aProperty.equals(TLcdLASModelDescriptor.INFRARED)) {
      ILcdExpression nir = attribute(Integer.class, dataProperty);
      ILcdExpression fraction = div(nir, 256 * 256.f);
      return mixmap(fraction, INFRARED_COLORS);
    } else {
      return fDefaultStyle.getColor();
    }
  }

}
