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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Properties;

import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;

/**
 * Style configuration for SASS-C data.
 */
public class SassCStyleConfig {

  private final Properties fProperties;
  private final EnumMap<SassCAttribute, ILcdInterval> fFilterIntervals = new EnumMap<SassCAttribute, ILcdInterval>(SassCAttribute.class);
  private final EnumMap<SassCAttribute, List<Color>> fColors = new EnumMap<SassCAttribute, List<Color>>(SassCAttribute.class);
  private final List<SassCAttribute> fColorAttributes;

  public SassCStyleConfig(Properties aProperties) {
    fProperties = aProperties;
    for (SassCAttribute attribute : SassCAttribute.values()) {
      initAttribute(attribute);
    }
    fColorAttributes = Collections.unmodifiableList(new ArrayList<SassCAttribute>(fColors.keySet()));
  }

  public ILcdInterval getFilterInterval(SassCAttribute aAttribute) {
    return fFilterIntervals.get(aAttribute);
  }

  public List<SassCAttribute> getSupportedColorAttributes() {
    return fColorAttributes;
  }

  public List<Color> getColors(SassCAttribute aAttribute) {
    return fColors.get(aAttribute);
  }

  private void initAttribute(SassCAttribute aAttribute) {
    if (aAttribute.isContinuous()) {
      Double min = getRequiredDouble("data.filter." + aAttribute.getName() + ".min");
      Double max = getRequiredDouble("data.filter." + aAttribute.getName() + ".max");
      fFilterIntervals.put(aAttribute, new TLcdInterval(min, max));
    }

    ArrayList<Color> colors = new ArrayList<Color>();
    while (true) {
      Color color = getColor("data.color." + aAttribute.getName() + ".color" + colors.size());
      if (color == null) {
        break;
      }
      colors.add(color);
    }
    if (!colors.isEmpty()) {
      fColors.put(aAttribute, colors);
    }
  }

  private Color getColor(String aKey) {
    String value = fProperties.getProperty(aKey, null);
    if (value == null) {
      return null;
    }
    if (value.startsWith("0x")) {
      try {
        return new Color(Integer.parseInt(value.substring(2), 16));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Value for color property " + aKey + " is not a valid hexadecimal number: " + value);
      }
    } else {
      String[] components = value.split(",");
      if (components.length != 3) {
        throw new IllegalArgumentException("Value for color for property " + aKey + " does not have 3 components: " + value);
      }
      try {
        float[] values = new float[3];
        for (int i = 0; i < values.length; i++) {
          values[i] = Float.parseFloat(components[i]);
          if (!(values[i] >= 0.f && values[i] <= 1.f)) {
            throw new IllegalArgumentException("Value for color for property " + aKey + " has values outside [0,1]: " + value);
          }
        }
        return new Color(values[0], values[1], values[2]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Color property " + aKey + " has invalid value: " + value);
      }
    }
  }

  private Double getRequiredDouble(String aKey) {
    String value = fProperties.getProperty(aKey, null);
    if (value == null) {
      throw new IllegalArgumentException("Property " + aKey + " is missing");
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid value for property " + aKey + ": " + value, e);
    }
  }
}
