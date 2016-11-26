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
package samples.tea.lightspeed.contour;

import java.util.HashMap;
import java.util.Map;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.TLcdImageModelHeightProviderFactory;

import samples.lightspeed.imaging.AbstractImageMatrixView;

/**
 * MatrixView of a model with an image.
 * It uses the first level for multilevel images.
 *
 * The following <code>ALcdImage</code> sub-types are supported:
 * <ul>
 *   <li><code>ALcdBasicImage</code></li>
 *   <li><code>ALcdImageMosaic</code></li>
 *   <li><code>ALcdMultilevelImage</code></li>
 *   <li><code>ALcdMultilevelImageMosaic</code></li>
 * </ul>
 */
class ImageHeightMatrixView extends AbstractImageMatrixView {

  public ImageHeightMatrixView(ILcdModel aModel) {
    super(aModel);
  }

  @Override
  protected RetrievalFunction createRetrievalFunction(ILcdModel aModel, double aSampleDensity) {
    return new ImageHeightRetrievalFunction(aModel, aSampleDensity);
  }

  private class ImageHeightRetrievalFunction implements RetrievalFunction {

    private final ILcdHeightProvider fHeightProvider;

    public ImageHeightRetrievalFunction(ILcdModel aModel, double aSampleDensity) {
      TLcdImageModelHeightProviderFactory modelHeightProviderFactory = new TLcdImageModelHeightProviderFactory();
      Map<String, Object> requiredPropertiesSFCT = new HashMap<>();
      requiredPropertiesSFCT.put(TLcdImageModelHeightProviderFactory.KEY_GEO_REFERENCE, aModel.getModelReference());
      Map<String, Object> optionalProperties = new HashMap<>();
      optionalProperties.put(TLcdImageModelHeightProviderFactory.KEY_INTERPOLATE_DATA, false);
      optionalProperties.put(TLcdImageModelHeightProviderFactory.KEY_PIXEL_DENSITY, aSampleDensity);
      fHeightProvider = modelHeightProviderFactory.createHeightProvider(aModel, requiredPropertiesSFCT, optionalProperties);
    }

    @Override
    public double retrieveValue(ILcdPoint aPoint) {
      return fHeightProvider.retrieveHeightAt(aPoint);
    }

  }

}
