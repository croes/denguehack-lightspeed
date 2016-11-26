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
 * <p>
 * Implementation of the interface <code>ILcdMatrixRasterValueMapper</code> which uses the special
 * values from the propagation function <code>TLcdLOSRadarPropagationFunctionSkyBackground</code>.
 * </p>
 * <p>
 * This implementation maximizes the visible area. This is done by giving the normal values a higher
 * priority than the other values. The priority list from high to low is as follows:
 * <ul>
 *   <li>normal values</li>
 *   <li>{@link TLcdLOSRadarPropagationFunctionSkyBackground#UNKNOWN}</li>
 *   <li>{@link TLcdLOSRadarPropagationFunctionSkyBackground#INVISIBLE}</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note that</b> this method converts <code>double</code> values retrieved from the given matrix to
 * <code>short</code> values which are used in the resulting raster. When a matrix value is found larger
 * than <code>Short.MAX_VALUE</code>, the resulting raster pixel is set to <code>Short.MAX_VALUE</code>.
 * </p>
 */
class MatrixRasterValueMapperRadarSkyBackground implements ILcdMatrixRasterValueMapper {

  public short getDefaultValue() {
    return TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE;
  }

  public short combineMatrixValues( double[] aMatrixValues, double[] aWeightsForMatrixValues, TLcdCoverageFillMode aFillMode ) {
    if ( TLcdCoverageFillMode.NEAREST_NEIGHBOR.equals( aFillMode ) ) {
      if ( aMatrixValues.length != 1 ) {
        throw new IllegalArgumentException( "Only one matrix value expected in nearest neighbor mode." );
      }
      return (short) Math.min( Math.ceil( aMatrixValues[0] ), Short.MAX_VALUE );
    }
    throw new IllegalArgumentException( "Unknown fill mode." );
  }

  public short mapMatrixValue( short aOldRasterValue, double aMatrixValue, TLcdCoverageFillMode aFillMode ) {
    if ( TLcdCoverageFillMode.MINIMUM.equals( aFillMode ) ) {
      return mapMatrixValueMinimum( aOldRasterValue, aMatrixValue );
    }
    if ( TLcdCoverageFillMode.MAXIMUM.equals( aFillMode ) ) {
      return mapMatrixValueMaximum( aOldRasterValue, aMatrixValue );
    }
    throw new IllegalArgumentException( "Unknown fill mode." );
  }

  private short mapMatrixValueMinimum( short aOldRasterValue, double aMatrixValue ) {
    boolean old_special = isSpecialValue( aOldRasterValue );
    boolean new_special = isSpecialValue( aMatrixValue    );

    if ( !old_special && !new_special ) {
      if ( aMatrixValue < aOldRasterValue ) {
        return (short)Math.min( Math.ceil( aMatrixValue ), Short.MAX_VALUE );
      }
      return aOldRasterValue;
    }
    else if ( !old_special && new_special ) {
      return aOldRasterValue;
    }
    else if ( !new_special && old_special ) {
      return (short)Math.min( Math.ceil( aMatrixValue ), Short.MAX_VALUE );
    }
    if ( aOldRasterValue == TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN ||
         aMatrixValue    == TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN ) {
      return TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN;
    }
    if ( aOldRasterValue == TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE ||
         aMatrixValue    == TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE ) {
      return TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE;
    }
    throw new IllegalArgumentException( "Unknown argument found." );
  }

  private short mapMatrixValueMaximum( short aOldRasterValue, double aMatrixValue ) {
    boolean old_special = isSpecialValue( aOldRasterValue );
    boolean new_special = isSpecialValue( aMatrixValue    );

    if ( !old_special && !new_special ) {
      if ( aMatrixValue > aOldRasterValue ) {
        return (short)Math.min( Math.ceil( aMatrixValue ), Short.MAX_VALUE );
      }
      return aOldRasterValue;
    }
    else if ( !old_special && new_special ) {
      return aOldRasterValue;
    }
    else if ( !new_special && old_special ) {
      return (short)Math.min( Math.ceil( aMatrixValue ), Short.MAX_VALUE );
    }
    if ( aOldRasterValue == TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN ||
         aMatrixValue    == TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN ) {
      return TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN;
    }
    if ( aOldRasterValue == TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE ||
         aMatrixValue    == TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE ) {
      return TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE;
    }
    throw new IllegalArgumentException( "Unknown argument found." );
  }

  private static boolean isSpecialValue( double aValue ) {
    return aValue == TLcdLOSRadarPropagationFunctionSkyBackground.INVISIBLE ||
           aValue == TLcdLOSRadarPropagationFunctionSkyBackground.UNKNOWN;
  }

}
