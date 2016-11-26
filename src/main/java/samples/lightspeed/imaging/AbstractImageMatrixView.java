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

import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdImageMosaic;
import com.luciad.imaging.ALcdMultilevelImage;
import com.luciad.imaging.ALcdMultilevelImageMosaic;
import com.luciad.imaging.ELcdImageSamplingMode;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYPoint;

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
public abstract class AbstractImageMatrixView implements ILcdMatrixView {

  private static final int LEVEL = 1;

  private final RetrievalFunction fRetrievalFunction;
  private final double fPixelSizeX;
  private final double fPixelSizeY;

  private final ILcdPoint fLocation;
  private final TLcdXYPoint fGridOffset;
  private final int fColumnCount;
  private final int fRowCount;

  public interface RetrievalFunction {

    double retrieveValue(ILcdPoint aPoint);

  }

  public AbstractImageMatrixView(ILcdModel aModel) {
    ALcdImage image = ALcdImage.fromDomainObject(aModel.elements().nextElement());
    if (image == null) {
      throw new IllegalArgumentException("Model contains an unsupported type.");
    }
    ELcdImageSamplingMode samplingMode;
    if (image instanceof ALcdMultilevelImage) {
      ALcdMultilevelImage multilevelImage = (ALcdMultilevelImage) image;
      ALcdBasicImage basicImage = multilevelImage.getLevel(LEVEL);
      fPixelSizeX = basicImage.getConfiguration().getPixelSizeX();
      fPixelSizeY = basicImage.getConfiguration().getPixelSizeY();
      samplingMode = basicImage.getConfiguration().getSamplingMode();
    } else if (image instanceof ALcdMultilevelImageMosaic) {
      ALcdMultilevelImageMosaic multilevelImageMosaic = (ALcdMultilevelImageMosaic) image;
      ALcdImageMosaic imageMosaic = multilevelImageMosaic.getLevel(LEVEL);
      fPixelSizeX = imageMosaic.getConfiguration().getPixelSizeX();
      fPixelSizeY = imageMosaic.getConfiguration().getPixelSizeY();
      samplingMode = imageMosaic.getConfiguration().getSamplingMode();
    } else if (image instanceof ALcdBasicImage) {
      ALcdBasicImage basicImage = (ALcdBasicImage) image;
      fPixelSizeX = basicImage.getConfiguration().getPixelSizeX();
      fPixelSizeY = basicImage.getConfiguration().getPixelSizeY();
      samplingMode = basicImage.getConfiguration().getSamplingMode();
    } else if (image instanceof ALcdImageMosaic) {
      ALcdImageMosaic imageMosaic = (ALcdImageMosaic) image;
      fPixelSizeX = imageMosaic.getConfiguration().getPixelSizeX();
      fPixelSizeY = imageMosaic.getConfiguration().getPixelSizeY();
      samplingMode = imageMosaic.getConfiguration().getSamplingMode();
    } else {
      throw new IllegalArgumentException("Model contains an unsupported image type " + image.getClass());
    }
    double sampleDensity = 1.0 / (fPixelSizeX * fPixelSizeY);

    int widthInPixels = (int) (image.getBounds().getWidth()/fPixelSizeX);
    int heightInPixels = (int) (image.getBounds().getHeight() / fPixelSizeY);
    fColumnCount = samplingMode == ELcdImageSamplingMode.AREA ? widthInPixels : widthInPixels + 1;
    fRowCount = samplingMode == ELcdImageSamplingMode.AREA ? heightInPixels : heightInPixels + 1;

    fLocation = image.getBounds().getLocation();
    fGridOffset = samplingMode == ELcdImageSamplingMode.AREA ? new TLcdXYPoint(fPixelSizeX/2, fPixelSizeY/2) : new TLcdXYPoint(0, 0);

    fRetrievalFunction = createRetrievalFunction(aModel, sampleDensity);
  }

  protected abstract RetrievalFunction createRetrievalFunction(ILcdModel aModel, double aSampleDensity);

  @Override
  public double getValue(int i, int j) {
    return fRetrievalFunction.retrieveValue(getPoint(i, j));
  }

  @Override
  public double retrieveAssociatedPointX(int i, int j) {
    return getPoint(i, j).getX();
  }

  @Override
  public double retrieveAssociatedPointY(int i, int j) {
    return getPoint(i, j).getY();
  }

  private ILcdPoint getPoint(int i, int j) {
    ILcd2DEditablePoint point = fLocation.cloneAs2DEditablePoint();
    point.translate2D(i * fPixelSizeX, (getRowCount() - j - 1) * fPixelSizeY);
    point.translate2D(fGridOffset.getX(), fGridOffset.getY());
    return point;
  }

  @Override
  public int getColumnCount() {
    return fColumnCount;
  }

  @Override
  public int getRowCount() {
    return fRowCount;
  }

}
