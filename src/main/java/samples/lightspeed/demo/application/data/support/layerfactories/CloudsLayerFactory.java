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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.util.Collection;
import java.util.Collections;

import com.luciad.imaging.operator.util.TLcdColorLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.util.ELcdInterpolationType;
import com.luciad.util.ILcdColorFilter;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.ILspEffectsHintStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.imagefilter.TLspColorLookupTableFilterStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for the cloud coverage layer.
 * @since 2013.0
 */
public class CloudsLayerFactory extends AbstractLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return "Clouds".equals(aModel.getModelDescriptor().getDisplayName());
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    TLspRasterStyle rasterStyle = TLspRasterStyle.newBuilder()
                                                 .interpolation(ELcdInterpolationType.LINEAR)
                                                 .startResolutionFactor(Double.POSITIVE_INFINITY)
                                                 .opacity(0.99f)
                                                 .elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID)
                                                 .effectsHints(ILspEffectsHintStyle.EffectsHint.NO_FOG)
                                                 .build();

    TLspColorLookupTableFilterStyle filterStyle = TLspColorLookupTableFilterStyle.newBuilder()
                                                                                 .filter(
                                                                                     TLcdColorLookupTable.newBuilder()
                                                                                                         .filter(new CloudColorFilter())
                                                                                                         .interpolation(ELcdInterpolationType.LINEAR)
                                                                                                         .alphaMode(TLcdColorLookupTable.AlphaMode.MULTIPLY)
                                                                                                         .build()
                                                                                 )
                                                                                 .build();

    ILspEditableStyledLayer layer = TLspRasterLayerBuilder.newBuilder()
                                                          .model(aModel)
                                                          .styler(
                                                              TLspPaintRepresentationState.REGULAR_BODY,
                                                              new TLspStyler(rasterStyle, filterStyle)
                                                          )
                                                          .build();
    layer.setVisible(false);
    return Collections.<ILspLayer>singleton(layer);
  }

  private static class CloudColorFilter implements ILcdColorFilter {
    @Override
    public void apply(float[] aRGBAColorSFCT) {
      float a = aRGBAColorSFCT[0];
      aRGBAColorSFCT[0] = aRGBAColorSFCT[1] = aRGBAColorSFCT[2] = 1f;
      aRGBAColorSFCT[3] = a;
    }
  }

}
