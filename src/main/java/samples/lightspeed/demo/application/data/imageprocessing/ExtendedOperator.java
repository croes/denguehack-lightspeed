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

import java.awt.image.ColorModel;

import com.luciad.imaging.ALcdBandColorSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.TLcdBandColorSemanticsBuilder;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.imaging.operator.TLcdSemanticsOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;

import samples.lightspeed.imaging.multispectral.OperatorModel;

/**
 * Operator chain that applies the NDVI filter first if needed.
 */
class ExtendedOperator extends ALcdImageOperatorChain {

  private OperatorModelExtended fOperatorModel;
  private ALcdImageOperatorChain fNormalizedVegetationOperator;
  private OperatorModel.BandSelectOperatorChain fBandSelectOperatorChain;

  ExtendedOperator(OperatorModelExtended aOperatorModel) {
    this(aOperatorModel, null);
  }

  ExtendedOperator(OperatorModelExtended aOperatorModel, TLcdLookupTable aNDVILookUpTable) {
    fOperatorModel = aOperatorModel;
    fNormalizedVegetationOperator = new MyNormalizedDifferenceVIOperator(aNDVILookUpTable);
  }

  public void setBandSelectOperatorChain(OperatorModel.BandSelectOperatorChain aBandSelectOperatorChain) {
    fBandSelectOperatorChain = aBandSelectOperatorChain;
  }

  @Override
  public ALcdImage apply(ALcdImage aInput) {
    ALcdBasicImage inputImage = (ALcdBasicImage) aInput;

    if (fOperatorModel.isNormalizedDifference()) {
      inputImage = (ALcdBasicImage) fNormalizedVegetationOperator.apply(inputImage);
    } else {
      inputImage = (ALcdBasicImage) doBandSelect(inputImage, fOperatorModel.getSelectedBands());
    }

    ALcdImage result = inputImage;

    if (!(result.getConfiguration().getSemantics().get(0) instanceof ALcdBandColorSemantics)) {
      int numberOfSemantics;
      if (fOperatorModel.isNormalizedDifference()) {
        numberOfSemantics = 1;
      } else {
        numberOfSemantics = fOperatorModel.getSelectedBands().length;
      }
      ALcdBandSemantics[] semantics = new ALcdBandSemantics[numberOfSemantics];
      for (int i = 0; i < semantics.length; i++) {
        semantics[i] = TLcdBandColorSemanticsBuilder.newBuilder()
                                                    .colorModel(ColorModel.getRGBdefault())
                                                    .componentIndex(i)
                                                    .buildSingleBandSemantic();
      }

      result = TLcdSemanticsOp.semantics(result, semantics);
    }

    return result;
  }

  private ALcdImage doBandSelect(ALcdImage aImage, int[] aBandSelect) {
    if (fBandSelectOperatorChain != null) {
      aImage = fBandSelectOperatorChain.apply(aImage);
    } else {
      aImage = TLcdBandSelectOp.bandSelect(aImage, aBandSelect);
    }
    return aImage;
  }

}
