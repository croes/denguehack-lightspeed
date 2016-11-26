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
package samples.tea.gxy.los;

import com.luciad.tea.*;

/**
 * Implementation of the interface <code>ILcdMatrixRasterValueMapper</code> which uses the special
 * values from the propagation function <code>TLcdLOSPropagationFunctionFixedHeight</code>.
 * <p/>
 * This implementation maximizes the cone of silence. This is done by giving the special cone of
 * silence value a higher priority than the other values. The priority list from high to low is
 * as follows:
 * <ul>
 *   <li>{@link TLcdLOSPropagationFunctionFixedHeight#CONE_OF_SILENCE}</li>
 *   <li>{@link TLcdLOSPropagationFunctionFixedHeight#VISIBLE}</li>
 *   <li>{@link TLcdLOSPropagationFunctionFixedHeight#UNKNOWN}</li>
 *   <li>{@link TLcdLOSPropagationFunctionFixedHeight#INVISIBLE}</li>
 * </ul>
 */
class MatrixRasterValueMapperFixedHeight implements ILcdMatrixRasterValueMapper {

  public short getDefaultValue() {
    return TLcdLOSPropagationFunctionFixedHeight.INVISIBLE;
  }

  public short combineMatrixValues( double[] aMatrixValues, double[] aWeightsForMatrixValues, TLcdCoverageFillMode aFillMode ) {
    if ( TLcdCoverageFillMode.NEAREST_NEIGHBOR.equals( aFillMode ) ) {
      if ( aMatrixValues.length != 1 ) {
        throw new IllegalArgumentException( "Only one matrix value expected in nearest neighbor mode." );
      }
      return mapMatrixValue( aMatrixValues[0] );
    }
    throw new IllegalArgumentException( "Unknown fill mode." );
  }

  public short mapMatrixValue( short aOldRasterValue, double aMatrixValue, TLcdCoverageFillMode aFillMode ) {
    if ( TLcdCoverageFillMode.MINIMUM.equals( aFillMode ) ||
         TLcdCoverageFillMode.MAXIMUM.equals( aFillMode ) ) {
      return mapMatrixValue( aMatrixValue, aOldRasterValue );
    }
    throw new IllegalArgumentException( "Unknown fill mode." );
  }

  private short mapMatrixValue( double aMatrixValue ) {
    if ( aMatrixValue == TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE ) {
      return TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE;
    }
    if ( aMatrixValue == TLcdLOSPropagationFunctionFixedHeight.VISIBLE ) {
      return TLcdLOSPropagationFunctionFixedHeight.VISIBLE;
    }
    if ( aMatrixValue == TLcdLOSPropagationFunctionFixedHeight.UNKNOWN ) {
      return TLcdLOSPropagationFunctionFixedHeight.UNKNOWN;
    }
    if ( aMatrixValue == TLcdLOSPropagationFunctionFixedHeight.INVISIBLE ) {
      return TLcdLOSPropagationFunctionFixedHeight.INVISIBLE;
    }
    throw new IllegalArgumentException( "Unknown argument found." );
  }

  private short mapMatrixValue( double aMatrixValue, short aOldRasterValue ) {
    if ( aOldRasterValue == TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE ||
         aMatrixValue    == TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE ) {
      return TLcdLOSPropagationFunctionFixedHeight.CONE_OF_SILENCE;
    }
    if ( aOldRasterValue == TLcdLOSPropagationFunctionFixedHeight.VISIBLE ||
         aMatrixValue    == TLcdLOSPropagationFunctionFixedHeight.VISIBLE ) {
      return TLcdLOSPropagationFunctionFixedHeight.VISIBLE;
    }
    if ( aOldRasterValue == TLcdLOSPropagationFunctionFixedHeight.UNKNOWN ||
         aMatrixValue    == TLcdLOSPropagationFunctionFixedHeight.UNKNOWN ) {
      return TLcdLOSPropagationFunctionFixedHeight.UNKNOWN;
    }
    if ( aOldRasterValue == TLcdLOSPropagationFunctionFixedHeight.INVISIBLE ||
         aMatrixValue    == TLcdLOSPropagationFunctionFixedHeight.INVISIBLE ) {
      return TLcdLOSPropagationFunctionFixedHeight.INVISIBLE;
    }
    throw new IllegalArgumentException( "Unknown argument found." );
  }

}
