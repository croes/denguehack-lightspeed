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
package samples.lightspeed.demo.application.data.airspaces;

import static com.luciad.view.lightspeed.label.TLspLabelPlacer.DEFAULT_NO_DECLUTTER_GROUP;
import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.CENTER;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.ais.model.airspace.TLcdFeaturedAirspace;
import com.luciad.format.arinc.model.airspace.ILcdARINCAirspaceFeature;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

/**
 * Styler for labels of airspaces.
 */
public class AirspaceLabelStyler extends ALspLabelStyler {

  private final List<? extends ALspStyle> fStyles;

  public AirspaceLabelStyler(double aAlpha) {
    fStyles = Arrays.asList(
        TLspTextStyle.newBuilder().font("Default-BOLD-16").textColor(Color.white).haloColor(Color.black).build(),
        TLspLabelBoxStyle.newBuilder().frameThickness(1).frameColor(Color.white).haloColor(Color.black).padding(3).build(),
        TLspLabelOpacityStyle.newBuilder().opacity((float) aAlpha).build(),
        new MyTextProviderStyle());
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects)
                   .group(DEFAULT_NO_DECLUTTER_GROUP)
                   .locations(20, CENTER)
                   .styles(fStyles)
                   .submit();
  }

  private static class MyTextProviderStyle extends ALspLabelTextProviderStyle {
    @Override
    public String[] getText(Object aObject, Object aSubLabelID, TLspContext aContext) {
      if (aObject instanceof TLcdExtrudedShape) {
        aObject = ((TLcdExtrudedShape) aObject).getBaseShape();
      }

      if (aObject instanceof ILcdFeatured) {
        TLcdFeaturedAirspace featured = (TLcdFeaturedAirspace) aObject;
        ILcdFeaturedDescriptor descriptor = (ILcdFeaturedDescriptor) aContext.getLayer().getModel().getModelDescriptor();
        List<String> result = new ArrayList<String>(5);

        int featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.CLASS);
        result.add(featured.getFeature(featureIndex).toString());

        featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.NAME);
        result.add(featured.getFeature(featureIndex).toString());

/*
        featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.COMMUNICATIONS_NAME);
        result.add(featured.getFeature(featureIndex).toString());
*/

        StringBuilder builder = new StringBuilder();
        featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.UPPER_LIMIT);
        builder.append(featured.getFeature(featureIndex).toString());
        featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.UPPER_LIMIT_UNIT);
        builder.append(" ").append(featured.getFeature(featureIndex).toString());
        featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.UPPER_LIMIT_REFERENCE);
        builder.append("(").append(featured.getFeature(featureIndex).toString()).append(")");
        result.add(builder.toString());

        try {
          builder = new StringBuilder();
          featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.LOWER_LIMIT);
          builder.append(featured.getFeature(featureIndex).toString());
          featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.LOWER_LIMIT_UNIT);
          builder.append(" ").append(featured.getFeature(featureIndex).toString());
          featureIndex = descriptor.getFeatureIndex(ILcdARINCAirspaceFeature.LOWER_LIMIT_REFERENCE);
          builder.append("(").append(featured.getFeature(featureIndex).toString()).append(")");
          result.add(builder.toString());
        } catch (Exception e) {
          result.add("");
        }

        return result.toArray(new String[]{});
      } else {
        return null;
      }
    }
  }
}
