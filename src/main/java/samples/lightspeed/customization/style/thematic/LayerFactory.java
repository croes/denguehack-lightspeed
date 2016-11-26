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

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.common.SampleData;

/**
 * Creates layers that use {@link CountryStyler}.
 */
class LayerFactory extends ALspSingleLayerFactory {
  private CountryStyler fCountryStyler;
  private CountryStyler fSelectedCountryStyler;

  public LayerFactory(CountryStyler aCountryStyler, CountryStyler aSelectedCountryStyler) {
    fCountryStyler = aCountryStyler;
    fSelectedCountryStyler = aSelectedCountryStyler;
  }

  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) &&
           (aModel.getModelDescriptor().getSourceName().endsWith(SampleData.COUNTRIES));
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      return createCustomSHPLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model not recognized");
    }
  }

  private ILspLayer createCustomSHPLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder.newBuilder().
        model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, fCountryStyler)
                                .bodyStyler(TLspPaintState.SELECTED, fSelectedCountryStyler)
                                .selectable(true)
                                .build();
  }
}
