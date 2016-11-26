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
package samples.lucy.cop.gazetteer;

import java.util.Arrays;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * {@code ILspStyler} for the gazetteer layer, based on the object type
 */
final class GazetteerStyler extends ALspCustomizableStyler {

  private static final TLcdSymbol DEFAULT_ICON = new TLcdSymbol(TLcdSymbol.CIRCLE);

  private static final String STYLE_PREFIX = "style.";
  private static final String ICON_PREFIX = "icon.";
  private static final String ICON_SOURCE = ICON_PREFIX + "source";

  private final TLspCustomizableStyle fAirportStyle;
  private final TLspCustomizableStyle fHeliportStyle;
  private final TLspCustomizableStyle fHospitalStyle;
  private final TLspCustomizableStyle fSchoolStyle;

  GazetteerStyler(String aPropertyPrefix, ALcyProperties aProperties) {
    fAirportStyle = createStyle(aPropertyPrefix, aProperties, GazetteerModel.Type.AIRPORT, "airport");
    fHeliportStyle = createStyle(aPropertyPrefix, aProperties, GazetteerModel.Type.AIRPORT, "heliport");
    fSchoolStyle = createStyle(aPropertyPrefix, aProperties, GazetteerModel.Type.SCHOOL, "school");
    fHospitalStyle = createStyle(aPropertyPrefix, aProperties, GazetteerModel.Type.HOSPITAL, "hospital");
  }

  private TLspCustomizableStyle createStyle(String aPropertyPrefix, ALcyProperties aProperties, GazetteerModel.Type aType, String aFeatureName) {
    String prefix = aPropertyPrefix + STYLE_PREFIX + aType.name() + "." + aFeatureName + ".";
    String source = aProperties.getString(prefix + ICON_SOURCE, null);
    ILcdIcon icon = DEFAULT_ICON;
    if (source != null) {
      icon = TLcdIconFactory.create(source);
    }
    return new TLspCustomizableStyle(TLspIconStyle.newBuilder().icon(icon).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build(),
                                     true,
                                     aType.name() + "." + aFeatureName,
                                     aType.getDisplayName() + " [" + aFeatureName + "]");
  }

  @Override
  public Collection<TLspCustomizableStyle> getStyles() {
    return Arrays.asList(fAirportStyle, fHeliportStyle, fHospitalStyle, fSchoolStyle);
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      GazetteerModel.Type type = GazetteerModel.retrieveType(object);
      switch (type) {
      case HOSPITAL:
        aStyleCollector.object(object).style(fHospitalStyle.getStyle()).submit();
        break;
      case SCHOOL:
        aStyleCollector.object(object).style(fSchoolStyle.getStyle()).submit();
        break;
      case AIRPORT:
        String featureName = (String) ((ILcdDataObject) object).getValue("featureName");
        ALspStyle style = null;
        if (featureName.toLowerCase().contains("heliport")) {
          style = fHeliportStyle.getStyle();
        } else {
          style = fAirportStyle.getStyle();
        }
        aStyleCollector.object(object).style(style).submit();
        break;
      }
    }
  }
}
