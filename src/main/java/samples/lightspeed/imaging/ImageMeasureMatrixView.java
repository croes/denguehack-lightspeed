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
package samples.lightspeed.imaging;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.measure.ALcdMeasureProvider;
import com.luciad.util.measure.TLcdImageModelMeasureProviderFactory;

/**
 * MatrixView of a model with an image.
 * It uses the first level for multilevel images.
 * In case of multiband images; only the first band is used.
 *
 * The image also needs a <code>ALcdBandMeasurementSemantics</code> measurement.
 * The following <code>ALcdImage</code> sub-types are supported:
 * <ul>
 *   <li><code>ALcdBasicImage</code></li>
 *   <li><code>ALcdImageMosaic</code></li>
 *   <li><code>ALcdMultilevelImage</code></li>
 *   <li><code>ALcdMultilevelImageMosaic</code></li>
 * </ul>
 */
public class ImageMeasureMatrixView extends AbstractImageMatrixView {

  public ImageMeasureMatrixView(ILcdModel aModel) {
    super(aModel);
}

  @Override
  protected RetrievalFunction createRetrievalFunction(ILcdModel aModel, double aSampleDensity) {
    return new MeasureRetrievalFunction(aModel, aSampleDensity);
  }

  private static class MeasureRetrievalFunction implements RetrievalFunction {

    private static final int BAND_INDEX = 0;

    private final ILcdModel fModel;
    private final ALcdMeasureProvider fMeasureProvider;
    private final ALcdMeasureProvider.Parameters fParameters;

    public MeasureRetrievalFunction(ILcdModel aModel, double aSampleDensity) {
      fModel = aModel;
      fParameters = ALcdMeasureProvider.Parameters.newBuilder().sampleDensity(aSampleDensity).build();
      TLcdImageModelMeasureProviderFactory providerFactory = new TLcdImageModelMeasureProviderFactory();
      fMeasureProvider = providerFactory.createMeasureProvider(aModel);
    }

    @Override
    public double retrieveValue(ILcdPoint aPoint) {
      TLcdISO19103Measure[] measures = fMeasureProvider.retrieveMeasuresAt(aPoint, fModel.getModelReference(), fParameters);
      if (measures.length == 0) {
        return Double.NaN;
      }
      //Only the first value is returned
      return measures[BAND_INDEX].doubleValue();
    }

  }

}
