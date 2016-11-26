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
package samples.lucy.lightspeed.style.customizablestyle;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * <code>ILspCustomizableStyler</code> which uses a different style for 'big' and 'small' countries
 */
class CountryCustomizableStyler extends ALspCustomizableStyler {
  //set an identifier on the style. This avoids that the styles for big and small countries are
  //shown as one style in the layer properties panel when showing the properties of multiple
  //layers at once
  private static final String BIG_COUNTRIES_IDENTIFIER = "bigCountries";
  private static final String SMALL_COUNTRIES_IDENTIFIER = "smallCountries";

  private List<TLspCustomizableStyle> fBigCountryStyles = new ArrayList<TLspCustomizableStyle>();
  private List<TLspCustomizableStyle> fSmallCountryStyles = new ArrayList<TLspCustomizableStyle>();

  /**
   * Create a new styler for the countries layer
   */
  CountryCustomizableStyler() {
    //initialize the styles

    fSmallCountryStyles.add(new TLspCustomizableStyle(TLspLineStyle.newBuilder().elevationMode(ElevationMode.ON_TERRAIN).color(Color.DARK_GRAY).width(2).build(), true,
                                                      SMALL_COUNTRIES_IDENTIFIER, "Small countries line style"));
    fSmallCountryStyles.add(new TLspCustomizableStyle(TLspFillStyle.newBuilder().elevationMode(ElevationMode.ON_TERRAIN).color(Color.WHITE).build(), true,
                                                      SMALL_COUNTRIES_IDENTIFIER, "Small countries fill style"));

    fBigCountryStyles.add(new TLspCustomizableStyle(TLspLineStyle.newBuilder().elevationMode(ElevationMode.ON_TERRAIN).color(Color.DARK_GRAY).width(2).build(), true,
                                                    BIG_COUNTRIES_IDENTIFIER, "Big countries line style"));
    fBigCountryStyles.add(new TLspCustomizableStyle(TLspFillStyle.newBuilder().elevationMode(ElevationMode.ON_TERRAIN).color(Color.PINK).build(), true,
                                                    BIG_COUNTRIES_IDENTIFIER, "Big countries fill style"));

    PropertyChangeListener styleChangeListener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent aEvt) {
        fireStyleChangeEvent();
      }
    };
    for (TLspCustomizableStyle style : fSmallCountryStyles) {
      style.addPropertyChangeListener(styleChangeListener);
    }
    for (TLspCustomizableStyle style : fBigCountryStyles) {
      style.addPropertyChangeListener(styleChangeListener);
    }
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      aStyleCollector.object(object).styles(getStyles(object, aContext)).submit();
    }
  }

  private List<ALspStyle> getStyles(Object aObject, TLspContext aContext) {
    if (!(aObject instanceof ILcdDataObject)) {
      return null;
    }
    ILcdDataObject obj = (ILcdDataObject) aObject;
    Integer pop = (Integer) obj.getValue("POP_1994");

    List<TLspCustomizableStyle> customizableStyles = pop > 50e6 ? fBigCountryStyles : fSmallCountryStyles;
    List<ALspStyle> result = new ArrayList<ALspStyle>(2);
    for (TLspCustomizableStyle customizableStyle : customizableStyles) {
      if (customizableStyle.isEnabled()) {
        result.add(customizableStyle.getStyle());
      }
    }
    return result;

  }

  @Override
  public Collection<TLspCustomizableStyle> getStyles() {
    List<TLspCustomizableStyle> result = new ArrayList<TLspCustomizableStyle>();
    result.addAll(fBigCountryStyles);
    result.addAll(fSmallCountryStyles);
    return Collections.unmodifiableCollection(result);
  }
}
