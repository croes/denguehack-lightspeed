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
package samples.gxy.decoder.raster.multispectral;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.Enumeration;
import java.util.List;

import com.luciad.imaging.ALcdBandMeasurementSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.imaging.TLcdBandColorSemanticsBuilder;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerRunnable;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

/**
 * Contains some utility methods for working with {@link ALcdImage}'s.
 */
public final class ImageUtil {

  private ImageUtil() {
  }

  /**
   * Searches the given layer's model for an {@code ALcdImage} object. If none can be found,
   * the layer cannot be used with this customizer.
   *
   * @param aLayer the layer to inspect
   *
   * @return an {@code ALcdImage} object from the layer's model, or {@code null} if none can be found
   */
  public static ALcdImage getImage(ILcdLayer aLayer) {
    if ((aLayer != null) && (aLayer.getModel() != null)) {
      Object imageObject = getImageObject(aLayer.getModel());
      if (imageObject != null) {
        return ALcdImage.fromDomainObject(imageObject);
      }
    }
    return null;
  }

  /**
   * Searches the given layer's model for an ALcdBasicImage object. If none can be found,
   * the layer cannot be used with this customizer.
   *
   * @param aModel the model to inspect
   * @return an ALcdBasicImage object from the model, or null if none can be found
   */
  public static Object getImageObject(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor)) {
      return null;
    }
    Enumeration elements = aModel.elements();
    if (elements.hasMoreElements()) {
      Object element = elements.nextElement();
      ALcdImage image = ALcdImage.fromDomainObject(element);
      if (image != null) {
        return element;
      }
    }
    return null;
  }

  /**
   * Sets an image operator chain for an image.
   *
   * @param aLayer         the GXY layer containing the image
   * @param aImage         the image
   * @param aOperatorChain the image operator chain to set
   */
  public static void setImageOperatorChain(ILcdGXYLayer aLayer, final ALcdImage aImage, final ALcdImageOperatorChain aOperatorChain) {
    // Unpack asynchronous layer wrapper if necessary
    if (aLayer instanceof ILcdGXYAsynchronousLayerWrapper) {
      ((ILcdGXYAsynchronousLayerWrapper) aLayer).invokeLaterOnGXYLayerInEDT(new ILcdGXYAsynchronousLayerRunnable() {
        @Override
        public void run(ILcdGXYLayer aSafeGXYLayer) {
          setImageOperatorChain(aSafeGXYLayer, aImage, aOperatorChain);

        }

      });
    } else {
      // Try to retrieve the painter provider
      if (aLayer instanceof TLcdGXYLayer) {
        TLcdGXYLayer layer = (TLcdGXYLayer) aLayer;
        // If the layer has a TLcdGXYImagePainter, we can assign an operator chain
        ILcdGXYPainter gxyPainter = layer.getGXYPainterProvider().getGXYPainter(aImage);
        if (gxyPainter instanceof TLcdGXYImagePainter) {
          TLcdGXYImagePainter painter = (TLcdGXYImagePainter) gxyPainter;
          painter.setOperatorChain(aOperatorChain);
          layer.invalidate();
        }
      }
    }
  }

  /**
   * Creates a band select operator chain.
   *
   * @param aBands the bands to be selected
   *
   * @return the band select operator chain
   */
  public static ALcdImageOperatorChain createBandSelectOperatorChain(int[] aBands) {
    ALcdImageOperatorChain.Builder chainBuilder = ALcdImageOperatorChain.newBuilder();

    // Select bands
    chainBuilder.bandSelect(aBands);

    // Interpret as color
    List<ALcdBandSemantics> targetSemantics;
    if (aBands.length == 1) {
      // Gray
      targetSemantics = TLcdBandColorSemanticsBuilder.newBuilder().colorModel(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE)).buildSemantics();
    } else if (aBands.length == 3) {
      // RGB
      targetSemantics = TLcdBandColorSemanticsBuilder.newBuilder().colorModel(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE)).buildSemantics();
    } else {
      throw new IllegalArgumentException("Unsupported number of bands: " + aBands.length);
    }
    chainBuilder.semantics(targetSemantics.toArray(new ALcdBandSemantics[targetSemantics.size()]));

    return chainBuilder.build();
  }

  public static boolean isMultispectral(ALcdImage aImage) {
    if (aImage == null) {
      return false;
    }
    List<ALcdBandSemantics> allBandSemantics = aImage.getConfiguration().getSemantics();
    // We use <= 2 because that's the threshold that is used in the BandSelectLayeredListener as well.
    if (allBandSemantics.size() <= 2) {
      return false;
    }
    for (ALcdBandSemantics bandSemantics : allBandSemantics) {
      if (!(bandSemantics instanceof ALcdBandMeasurementSemantics)) {
        return false;
      }
    }
    return true;
  }
}
