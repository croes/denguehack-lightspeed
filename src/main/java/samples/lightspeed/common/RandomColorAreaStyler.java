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
package samples.lightspeed.common;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Provides area styles where the fill color is selected randomly.
 */
public class RandomColorAreaStyler extends ALspStyler {

  private final ElevationMode fElevationMode;
  private TLspLineStyle fLineStyle;
  private TLcdWeakIdentityHashMap<Object, List<ALspStyle>> fObject2Color = new TLcdWeakIdentityHashMap<Object, List<ALspStyle>>();
  private double fAlpha = 0.75;

  public RandomColorAreaStyler(Color aLineColor, float aLineWidth, ElevationMode aElevationMode) {
    this(aLineColor, aLineWidth, aElevationMode, 0.75);
  }

  public RandomColorAreaStyler(Color aLineColor, float aLineWidth, ElevationMode aElevationMode, double aFillAlpha) {
    fElevationMode = aElevationMode;
    fLineStyle = TLspLineStyle.newBuilder().color(aLineColor).width(aLineWidth).elevationMode(aElevationMode).build();
    fAlpha = aFillAlpha;
  }

  /**
   * By default returns aObject, but subclasses can override this to return
   * a different key per object.
   *
   * @param aObject the object to get a cache key for
   *
   * @return the key for the given object
   */
  protected Object getCacheKeyForObject(Object aObject) {
    return aObject;
  }

  private Color getRandomColor() {
    return new Color(
        (float) (0.5 + Math.random() / 2),
        (float) (0.5 + Math.random() / 2),
        (float) (0.5 + Math.random() / 2),
        (float) fAlpha
    );
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      Object key = getCacheKeyForObject(object);
      List<ALspStyle> result = fObject2Color.get(key);
      if (result == null) {
        result = new ArrayList<ALspStyle>(2);
        result.add(TLspFillStyle.newBuilder().color(getRandomColor()).elevationMode(fElevationMode).build());
        result.add(fLineStyle);
        fObject2Color.put(key, result);
      }
      aStyleCollector.object(object).styles(result).submit();
    }

  }
}
