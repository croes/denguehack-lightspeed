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
package samples.lucy.format.generated.lightspeed;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.lightspeed.ALcyLspGeneralFormat;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lucy.format.generated.GeneratedFormatAddOn;

/**
 * Lightspeed counter part of {@link samples.lucy.format.generated.GeneratedFormat}. It provides the layer factory.
 */
class GeneratedLspFormat extends ALcyLspGeneralFormat {
  public GeneratedLspFormat(ILcyLucyEnv aLucyEnv, String aLongPrefix, String aShortPrefix, ALcyProperties aProperties) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aProperties);
  }

  @Override
  protected ILspLayerFactory createLayerFactory() {
    return new ALspSingleLayerFactory() {
      @Override
      public ILspLayer createLayer(ILcdModel aModel) {
        return TLspShapeLayerBuilder.newBuilder().model(aModel).build();
      }

      /**
       * Always true, we're protected by a safeguard wrapper that will test it for us, see
       * {@link GeneratedLspFormatAddOn#createFormatWrapper}.
       */
      @Override
      public boolean canCreateLayers(ILcdModel aModel) {
        return true;
      }
    };
  }

  @Override
  public boolean canHandleModel(ILcdModel aModel) {
    return GeneratedFormatAddOn.isGeneratedModel(aModel);
  }
}
