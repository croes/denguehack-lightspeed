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
/*
 *
 * Copyright (c) 1999-2015 Luciad All Rights Reserved.
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

import java.io.IOException;
import java.util.List;

import com.luciad.format.raster.TLcdGeoTIFFModelEncoder;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Saves an ILcdModel 's ALcdImage into a GeoTiff using ALcdImageOperatorChain to apply RGB modifications.
 */
public class SaveGeoTiff {
  //more customisation is possible with below properties
//  private static final int IMAGE_WIDTH = 1024;
//  private static final int IMAGE_HEIGHT = 1024;
//  private static final int TILE_WIDTH = 512;
//  private static final int TILE_HEIGHT = 512;
//  private static final int COMPRESSION = TLcdGeoTIFFModelEncoder.COMPRESSION_JPEG_TTN2;
//  private static final float QUALITY = 0.8f;
//  private static final int LEVEL_COUNT = 3;
//  private static final double SCALE_FACTOR = 0.25;

  static private ILcdLogger logger = TLcdLoggerFactory.getLogger(SaveGeoTiff.class.getName());

  /**
   * Will apply a given list of <code>ALcdImageOperatorChain</code> on an image with specific contrast and brigthness and save it to disk.
   * @param chains the <code>ImageOperator</code> to apply on the image. Can be <code>null</code>.
   * @param model the <code>Model</code> from which we take the image
   * @param outputFileName where to save the file
   * @return true if the file could be saved
   */
  public static void save(List<ALcdImageOperatorChain> chains,
                          ILcdModel model,
                          String outputFileName) throws IOException, ConverterException {

    checkNotNull(model, "Model cannot be null");
    checkNotNull(model.elements(), "Model elements cannot be null");

    if (model.elements().hasMoreElements()) {

      ALcdImage image = (ALcdImage) model.elements().nextElement();
      if (chains != null) {
        for (ALcdImageOperatorChain oper : chains) {
          image = oper.apply(image);
        }
      }

      TLcdGeoTIFFModelEncoder encoder = new TLcdGeoTIFFModelEncoder();

      // Set the encoder options if needed
      encoder.setCompression(TLcdGeoTIFFModelEncoder.COMPRESSION_DEFLATE);
      //optionally add more
      //        encoder.setCompression(COMPRESSION);
      //        encoder.setLevelCount(LEVEL_COUNT);
      //        ...

      // Encode the raster modelOper.
      logger.debug("Encoding [" + outputFileName + "] as [" + encoder.getDisplayName() + "]");
      encoder.addStatusListener(new ProgressOutput());
      ILcdModel tmpModel = new TLcdVectorModel(model.getModelReference(), model.getModelDescriptor());
              tmpModel.addElement(image, ILcdModel.NO_EVENT);
      encoder.export(tmpModel, outputFileName);
      tmpModel = null;

    } else {
      String error = "No image element on the model.";
      logger.error(error);
      throw new ConverterException(error);
    }
  }

  private static class ProgressOutput implements ILcdStatusListener {
    private int fLastProgress = 0;

    @Override
    public void statusChanged(TLcdStatusEvent aStatusEvent) {
      if (aStatusEvent.getID() == TLcdStatusEvent.PROGRESS && !aStatusEvent.isProgressIndeterminate()) {
        int progress = (int) (aStatusEvent.getProgress() * 10);
        if (progress != fLastProgress) {
          logger.debug(progress * 10 + "% ");
          fLastProgress = progress;
        }
      }
    }
  }

  private static void checkNotNull(Object aValue, String... aReason) {
    if (aValue == null) {
      StringBuilder sb = new StringBuilder();
      for (String s : aReason) {
        sb.append(s);
        sb.append(" ");
      }
      throw new IllegalArgumentException(sb.toString());
    }
  }

  static class ConverterException extends Exception {
    public ConverterException(String message) {
      super(message);
    }

  }

}
