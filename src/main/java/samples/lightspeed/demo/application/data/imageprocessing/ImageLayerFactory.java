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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdMultilevelImage;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import samples.lightspeed.imaging.multispectral.MultispectralOperatorStyler;

/**
 * Layer factory for the Image Processing theme.
 *
 * Creates a raster layer with an {@link MultispectralOperatorStyler}.
 */
public class ImageLayerFactory extends ALspSingleLayerFactory {

  public ILspLayer createLayer(ILcdModel aModel) {
    MultispectralOperatorStyler styler = new MultispectralOperatorStyler(false);
    return TLspRasterLayerBuilder.newBuilder().model(aModel).styler(TLspPaintRepresentationState.REGULAR_BODY, styler).build();
  }

  public boolean canCreateLayers(ILcdModel aModel) {
    // aModel should contain two compatible ALcdMultilevelImage objects
    return aModel != null && getMultiLevelImages(aModel).size() == 2;
  }

  private List<ALcdMultilevelImage> getMultiLevelImages(ILcdModel aModel) {
    List<ALcdMultilevelImage> images = new ArrayList<ALcdMultilevelImage>(2);
    Enumeration e = aModel.elements();
    while (e.hasMoreElements()) {
      ALcdImage image = ALcdImage.fromDomainObject(e.nextElement());
      if (image instanceof ALcdMultilevelImage) {
        images.add((ALcdMultilevelImage) image);
      }
    }
    return images;
  }
}
