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
package samples.tea.lightspeed.los;

import com.luciad.earth.util.ALcdEarthTerrainElevationProvider;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.tea.ALcdTerrainElevationProvider;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.TLcdAltitudeDescriptor;
import com.luciad.tea.TLcdCoverageAltitudeMode;

/**
 * <p>
 *   A class that wraps an instance of ALcdEarthTerrainElevationProvider and adapts
 * it to the ILcdAltitudeProvider interface used by Line-Of-Sight calculations. This
 * class will also adapt to the ALcdTerrainElevationProvider abstract class.
 * </p>
 */
public class EarthTerrainElevationAdapter extends ALcdTerrainElevationProvider implements ILcdAltitudeProvider {

  private ALcdEarthTerrainElevationProvider fElevationProvider;

  public EarthTerrainElevationAdapter( ALcdEarthTerrainElevationProvider aElevationProvider ) {
    fElevationProvider = aElevationProvider;
  }

  @Override public double retrieveAltitudeAt( ILcdPoint aPoint, ILcdGeoReference aPointReference ) {
      return fElevationProvider.retrieveElevationAt( aPoint, aPointReference);
    }

  @Override public double retrieveElevationAt( ILcdPoint aPoint, ILcdGeoReference aPointReference ) {
      return fElevationProvider.retrieveElevationAt( aPoint, aPointReference);
    }

  @Override public ILcdGeoReference getPreferredReference() {
    return fElevationProvider.getPreferredReference();
  }

  @Override public TLcdAltitudeDescriptor getAltitudeDescriptor() {
    return TLcdAltitudeDescriptor.getDefaultInstance();
  }

  @Override public TLcdCoverageAltitudeMode getAltitudeMode() {
    //ALcdEarthTerrainElevation providers are always defined above the geoid.
    return TLcdCoverageAltitudeMode.ABOVE_GEOID;
  }
}
