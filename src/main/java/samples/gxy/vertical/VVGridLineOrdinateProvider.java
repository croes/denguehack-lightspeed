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
package samples.gxy.vertical;

import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.view.vertical.ILcdVVGridLineOrdinateProvider;
import com.luciad.view.vertical.TLcdDefaultVVGridLineOrdinateProvider;
import com.luciad.view.vertical.TLcdVVGridLineOrdinateProviderRVSM;

/**
 * Provides grid line ordinate information from the default provider except for the
 * altitude unit {@link TLcdAltitudeUnit#FLIGHT_LEVEL FLIGHT_LEVEL}. For this unit, the RVSM
 * (Reduced Vertical Separation Minimum) provider is used.
 */
class VVGridLineOrdinateProvider implements ILcdVVGridLineOrdinateProvider {

  private ILcdVVGridLineOrdinateProvider fVVGridLevelProviderDefault = new TLcdDefaultVVGridLineOrdinateProvider();
  private ILcdVVGridLineOrdinateProvider fVVGridLevelProviderRVSM = new TLcdVVGridLineOrdinateProviderRVSM();

  public ILcdInterval getSnappedRange(double aScale, ILcdInterval aRange, TLcdAltitudeUnit aUnit) {
    if (TLcdAltitudeUnit.FLIGHT_LEVEL.equals(aUnit)) {
      return fVVGridLevelProviderRVSM.getSnappedRange(aScale, aRange, aUnit);
    }
    return fVVGridLevelProviderDefault.getSnappedRange(aScale, aRange, aUnit);
  }

  public double[] getGridLineOrdinates(double aScale, ILcdInterval aRange, TLcdAltitudeUnit aUnit) {
    if (TLcdAltitudeUnit.FLIGHT_LEVEL.equals(aUnit)) {
      return fVVGridLevelProviderRVSM.getGridLineOrdinates(aScale, aRange, aUnit);
    }
    return fVVGridLevelProviderDefault.getGridLineOrdinates(aScale, aRange, aUnit);
  }

  public double[] getSubGridLineOrdinates(double aScale, ILcdInterval aRange, TLcdAltitudeUnit aUnit) {
    if (TLcdAltitudeUnit.FLIGHT_LEVEL.equals(aUnit)) {
      return fVVGridLevelProviderRVSM.getSubGridLineOrdinates(aScale, aRange, aUnit);
    }
    return fVVGridLevelProviderDefault.getSubGridLineOrdinates(aScale, aRange, aUnit);
  }
}
