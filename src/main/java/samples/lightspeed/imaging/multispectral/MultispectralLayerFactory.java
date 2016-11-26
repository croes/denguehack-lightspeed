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
package samples.lightspeed.imaging.multispectral;

import static com.luciad.util.service.LcdService.HIGH_PRIORITY;

import java.util.Enumeration;

import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import samples.gxy.decoder.raster.multispectral.ImageUtil;

/**
 * Layer factory used by the sample. Installs a {@link MultispectralOperatorStyler} on
 * the layer, which is linked
 * to the GUI controls.
 */
@LcdService(service = ILspLayerFactory.class, priority = HIGH_PRIORITY)
public class MultispectralLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    MultispectralOperatorStyler styler = new MultispectralOperatorStyler(true);

    return TLspRasterLayerBuilder.newBuilder()
                                 .model(aModel)
                                 .styler(TLspPaintRepresentationState.REGULAR_BODY, styler)
                                 .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor && isMultispectral(aModel);
  }

  /**
   * Returns whether the specified model only contains multispectral image data.
   *
   * @param aModel a model
   * @return {@code true} if the model only contains multispectral image data.
   */
  public static boolean isMultispectral(ILcdModel aModel) {
    Enumeration elements = aModel.elements();
    if (elements.hasMoreElements()) {
      do {
        ALcdImage image = ALcdImage.fromDomainObject(elements.nextElement());
        if (!ImageUtil.isMultispectral(image)) {
          return false;
        }
      } while (elements.hasMoreElements());
      return true;
    } else {
      // V160-1941: Empty models are never multispectral.
      return false;
    }
  }
}
