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
package samples.lightspeed.imaging.multispectral.curves;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler for the color component curves.
 */
public class CurvesStyler extends ALspStyler {

  private static final List<List<ALspStyle>> sStyles = Arrays.asList(
      Arrays.<ALspStyle>asList(TLspLineStyle.newBuilder().color(new Color(255, 0, 0)).width(2f).zOrder(1).build()),
      Arrays.<ALspStyle>asList(TLspLineStyle.newBuilder().color(new Color(0, 255, 0)).width(2f).zOrder(1).build()),
      Arrays.<ALspStyle>asList(TLspLineStyle.newBuilder().color(new Color(0, 0, 255)).width(2f).zOrder(1).build()),
      Arrays.<ALspStyle>asList(TLspLineStyle.newBuilder().color(new Color(255, 255, 255)).width(2f).zOrder(1).build())
  );

  private static final ALspStyle sEditStyle = TLspLineStyle.newBuilder().color(Color.WHITE).opacity(0.5f).zOrder(0).build();

  private static final ALspStyleTargetProvider STYLE_TARGET_PROVIDER = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (aObject instanceof TLcdXYPolyline) {
        aResultSFCT.add(new CatmullRomCurve((TLcdXYPolyline) aObject));
      }
    }
  };

  private static final ALspStyleTargetProvider STYLE_EDIT_TARGET_PROVIDER = new ALspStyleTargetProvider() {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      aResultSFCT.add(aObject);
    }
  };

  public CurvesStyler() {
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      CatmullRomEditLine componentCurve = (CatmullRomEditLine) object;
      int index = componentCurve.getChannel();
      aStyleCollector.object(object).geometry(STYLE_TARGET_PROVIDER).styles(sStyles.get(index)).submit();
      aStyleCollector.object(object).geometry(STYLE_EDIT_TARGET_PROVIDER).styles(sEditStyle).submit();
    }
  }
}
