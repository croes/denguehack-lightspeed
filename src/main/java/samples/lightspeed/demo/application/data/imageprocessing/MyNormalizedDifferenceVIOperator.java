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

import java.awt.Color;

import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.imaging.operator.TLcdBinaryOp;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.TLcdSemanticsOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;

/**
 * Operator that uses subtract, add, divide, semantics, and index lookup operators to compute
 * the normalized difference vegetation index of a LandSat 7 image.
 */
class MyNormalizedDifferenceVIOperator extends ALcdImageOperatorChain {

  private final TLcdLookupTable fTable;

  public MyNormalizedDifferenceVIOperator(TLcdLookupTable aTable) {
    if (aTable != null) {
      fTable = aTable;
    } else {
      fTable = getColorLookupTable();
    }
  }

  @Override
  public ALcdImage apply(ALcdImage aInput) {
    ALcdBasicImage nirBand = (ALcdBasicImage) TLcdBandSelectOp.bandSelect(aInput, new int[]{3});
    ALcdBasicImage redBand = (ALcdBasicImage) TLcdBandSelectOp.bandSelect(aInput, new int[]{2});

    ALcdBasicImage numerator = TLcdBinaryOp.binaryOp(nirBand, redBand, TLcdBinaryOp.Operation.SUBTRACT);
    ALcdBasicImage denominator = TLcdBinaryOp.binaryOp(nirBand, redBand, TLcdBinaryOp.Operation.ADD);
    ALcdImage divided = TLcdBinaryOp.binaryOp(numerator, denominator, TLcdBinaryOp.Operation.DIVIDE);
    divided = TLcdSemanticsOp.semantics(divided, ALcdBandSemantics.DataType.FLOAT);

    return TLcdIndexLookupOp.indexLookup(divided, fTable, new double[]{0, 0, 0, 0});
  }

  private static TLcdLookupTable getColorLookupTable() {
    Color color0 = new Color(31, 18, 26);
    Color color1 = new Color(148, 47, 37);
    Color color2 = new Color(195, 68, 2);
    Color color3 = new Color(229, 109, 10);
    Color color4 = new Color(243, 185, 70);
    Color color5 = new Color(254, 231, 106);
    Color color6 = new Color(218, 236, 104);
    Color color7 = new Color(160, 209, 77);
    Color color8 = new Color(67, 159, 34);
    Color color9 = new Color(16, 104, 23);
    Color color10 = new Color(22, 87, 44);

    Color[] colors = new Color[]{color0, color1, color2, color3, color4, color5, color6, color7, color8, color9, color10};
    double[] levels = new double[]{-1, -0.5, -0.4, -0.2, -0.1, 0.0, 0.1, 0.2, 0.4, 0.5, 1};

    TLcdColorMap colorMap = new TLcdColorMap(new TLcdInterval(-1, 1), levels, colors);
    return TLcdLookupTable.newBuilder().fromColorMap(colorMap).build();
  }

}
