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
package samples.lightspeed.customization.style.thematic;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdColor;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler that colors the objects in the world.shp model according to each country's
 * population.
 */
class CountryStyler extends ALspStyler implements ILcdSelectionListener {
  private Color fColor1 = new Color(0, 0, 128);
  private Color fColor2 = new Color(255, 192, 0);

  @Override
  public void selectionChanged(TLcdSelectionChangedEvent aTLcdSelectionChangedEvent) {
    fireStyleChangeEvent();
  }

  public void setColorMin(Color aColor) {
    fColor1 = aColor;
    fireStyleChangeEvent();
  }

  public void setColorMax(Color aColor) {
    fColor2 = aColor;
    fireStyleChangeEvent();
  }

  public Color getColorMin() {
    return fColor1;
  }

  public Color getColorMax() {
    return fColor2;
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

    Double pop = (Double) obj.getValue("pop_est");

    double f = Math.min(pop / 1e8, 1f);

    return Arrays.<ALspStyle>asList(
        TLspFillStyle.newBuilder()
                     .color(TLcdColor.interpolate(fColor1, fColor2, (float) Math.sqrt(f))).elevationMode(ElevationMode.ON_TERRAIN)
                     .build(),
        TLspLineStyle.newBuilder().color(TLcdColor.interpolate(fColor2, fColor1, f)).width(1.5f)
                     .elevationMode(ElevationMode.ON_TERRAIN).build()
    );
  }
}
