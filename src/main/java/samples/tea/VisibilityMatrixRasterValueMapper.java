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
package samples.tea;

import com.luciad.tea.*;

/**
 * <p>
 * Implementation of <code>ILcdMatrixRasterValueMapper</code> for mapping a visibility value
 * to a raster value. For the visibility sample, only the methods {@link #getDefaultValue()}
 * and {@link #mapMatrixValue(short, double, TLcdCoverageFillMode)} are required. The first
 * returns the <code>short</code> value corresponding to the interpretation NOT_COMPUTED. It
 * results in a transparent default color. The second method maps special visibility values
 * to <code>short</code> raster values.
 * </p>
 */
public class VisibilityMatrixRasterValueMapper implements ILcdMatrixRasterValueMapper {

  private TLcdVisibilityDescriptor fDescriptor;
  private short                    fDefaultValue;

  /**
   * Returns a <code>short</code> value for the specified visibility interpretation.
   *
   * @param aInterpretation The visibility interpretation to map.
   *
   * @return a <code>short</code> value for the specified visibility interpretation.
   */
  public static short mapInterpretation( TLcdVisibilityInterpretation aInterpretation ) {
    if ( aInterpretation == TLcdVisibilityInterpretation.INVISIBLE     ) { return Short.MAX_VALUE - 5; } else
    if ( aInterpretation == TLcdVisibilityInterpretation.UNCERTAIN     ) { return Short.MAX_VALUE - 4; } else
    if ( aInterpretation == TLcdVisibilityInterpretation.VISIBLE       ) { return Short.MAX_VALUE - 3; } else
    if ( aInterpretation == TLcdVisibilityInterpretation.OUTSIDE_SHAPE ) { return Short.MAX_VALUE - 2; } else
    if ( aInterpretation == TLcdVisibilityInterpretation.NOT_COMPUTED  ) { return Short.MAX_VALUE - 1; } else
    throw new IllegalArgumentException( "Unknown interpretation." );
  }

  public VisibilityMatrixRasterValueMapper( TLcdVisibilityDescriptor aDescriptor ) {
    fDescriptor   = aDescriptor;
    fDefaultValue = mapInterpretation( TLcdVisibilityInterpretation.NOT_COMPUTED );
  }

  public short getDefaultValue() {
    return fDefaultValue;
  }

  public short combineMatrixValues( double[] aMatrixValues, double[] aWeightsForMatrixValues, TLcdCoverageFillMode aFillMode ) {
    // This method is not used by this sample.
    throw new UnsupportedOperationException( "Method not used in this sample." );
  }

  public short mapMatrixValue( short aOldRasterValue, double aMatrixValue, TLcdCoverageFillMode aFillMode ) {
    // This method is used by this sample to convert a visibility matrix
    // view to a raster. It is called with the MAXIMUM fill mode.
    if ( TLcdCoverageFillMode.MAXIMUM.equals( aFillMode ) ) {
      return mapSpecialValue( aMatrixValue );
    }
    throw new IllegalArgumentException( "Unknown fill mode." );
  }

  private short mapSpecialValue( double aMatrixValue ) {
    // This sample only uses special visibility values.
    if ( fDescriptor.isSpecialValue( aMatrixValue ) ) {
      return mapInterpretation( fDescriptor.getSpecialValueInterpretation( aMatrixValue ) );
    }
    throw new IllegalArgumentException( "Normal values are not used in this sample." );
  }

}
