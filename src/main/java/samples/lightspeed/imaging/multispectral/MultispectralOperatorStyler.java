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

import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.Collection;

import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.imagefilter.TLspImageProcessingStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler used by the sample.
 */
public class MultispectralOperatorStyler extends ALspStyler {

  // The image operator chain to use
  private ALcdImageOperatorChain fImageOperatorChain = ALcdImageOperatorChain.newBuilder().build();

  private OperatorModel fOperatorModel;
  private boolean fWithVectorStyles;
  private ColorModel fColorModel;

  // Basic styles that are always output
  private TLspRasterStyle fDefaultRasterStyle = TLspRasterStyle.newBuilder().build();
  private final TLspLineStyle fOutlineStyle = TLspLineStyle.newBuilder()
                                                           .color(Color.RED)
                                                           .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                           .build();
  private final TLspFillStyle fFillStyle = TLspFillStyle.newBuilder()
                                                        .color(Color.RED)
                                                        .stipplePattern(TLspFillStyle.StipplePattern.HATCHED)
                                                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                        .build();

  public MultispectralOperatorStyler(boolean aWithVectorStyles) {
    fWithVectorStyles = aWithVectorStyles;
  }

  /**
   * Get the image operator chain currently set on this styler
   *
   * @return the image operator chain
   */
  public ALcdImageOperatorChain getImageOperatorChain() {
    return fImageOperatorChain;
  }

  /**
   * Sets the image operator chain on this styler.
   *
   * @param aImageOperatorChain the operator chain to be used
   */
  public void setImageOperatorChain(ALcdImageOperatorChain aImageOperatorChain) {
    fImageOperatorChain = aImageOperatorChain;
    fireStyleChangeEvent();
  }

  public void setColorModel(ColorModel aColorModel) {
    fColorModel = aColorModel;
    fDefaultRasterStyle = TLspRasterStyle.newBuilder().colorModel(fColorModel).build();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    // Create a operator style
    TLspImageProcessingStyle operatorStyle = TLspImageProcessingStyle.newBuilder()
                                                                     .operatorChain(fImageOperatorChain)
                                                                     .build();

    TLspRasterStyle rasterStyle;
    if (fOperatorModel != null) {
      //opacity, contrast and brightness are set directly on the styler
      rasterStyle = fDefaultRasterStyle.asBuilder()
                                       .opacity(fOperatorModel.getOpacity())
                                       .contrast(fOperatorModel.getContrast())
                                       .brightness(fOperatorModel.getBrightness())
                                       .colorModel(fColorModel)
                                       .build();
    } else {
      rasterStyle = fDefaultRasterStyle;
    }

    // Submit it along with the basic styles
    if (fWithVectorStyles) {
      aStyleCollector
          .objects(aObjects)
          .styles(fFillStyle, fOutlineStyle, rasterStyle, operatorStyle)
          .submit();
    } else {
      aStyleCollector
          .objects(aObjects)
          .styles(rasterStyle, operatorStyle)
          .submit();
    }
  }

  /**
   * @return the operator model
   */
  public OperatorModel getOperatorModel() {
    return fOperatorModel;
  }

  /**
   * @param aOperatorModel the operator model
   */
  public void setOperatorModel(OperatorModel aOperatorModel) {
    fOperatorModel = aOperatorModel;
  }
}
