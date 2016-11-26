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
package samples.lightspeed.demo.application.data.support.los;

import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdLOSCoverageMatrix;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.tea.lightspeed.los.TLspLOSProperties;

import samples.lightspeed.demo.application.data.los.LOSUtil;

/**
 * A line-of-sight coverage.
 */
public class LOSCoverage extends TLcdLonLatCircle {

  private static final Object CACHE_KEY = new Object();

  private ILcdLOSCoverageMatrix fLOSCoverageMatrix;
  private ILcdAltitudeProvider fAltitudeProvider;
  private TLcdCoverageAltitudeMode fCoverageAltitudeMode;
  private ILcdGeoReference fGeoReference;

  public LOSCoverage(ILcdGeoReference aGeoReference) {
    fGeoReference = aGeoReference;
    setEllipsoid(aGeoReference.getGeodeticDatum().getEllipsoid());
  }

  public void computeCoverage(TLspLOSCalculator aLOSCalculator, ILcdAltitudeProvider aAltitudeProvider) {
    // Only compute LOS when needed
    Object o = getCachedObject(CACHE_KEY);
    if (o == null || fLOSCoverageMatrix == null) {
      TLspLOSProperties p = new TLspLOSProperties();
      p.setCenterPoint(
          new TLcdLonLatHeightPoint(getCenter(), 1.0)
      );
      double r = Math.max(getRadius(), 10);
      p.setRadius(r);
      p.setRadiusStep(r / 200.0);
      p.setCenterPointAltitudeMode(TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
      fLOSCoverageMatrix = aLOSCalculator.calculateLOS(
          p,
          aAltitudeProvider,
          fGeoReference
      );
      fAltitudeProvider = aAltitudeProvider;
      fCoverageAltitudeMode = aLOSCalculator.getCoverageAltitudeMode();
      insertIntoCache(CACHE_KEY, CACHE_KEY);
    }
  }

  /**
   * Determines whether the given point is visible. A fixed height of 5 meters above the terrain is
   * used.
   */
  public boolean isPointVisible(ILcdPoint aPoint, ILcdGeoReference aPointReference) {
    return isPointVisible(aPoint, aPointReference, 5.0);
  }

  public boolean isPointVisible(ILcdPoint aPoint, ILcdGeoReference aPointReference, double aHeight) {
    try {
      if (fLOSCoverageMatrix == null) {
        return false;
      }
      return LOSUtil.isPointVisible(fLOSCoverageMatrix,
                                    fAltitudeProvider,
                                    fCoverageAltitudeMode,
                                    aHeight,
                                    aPoint,
                                    aPointReference);
    } catch (Throwable e) {
      return false;
    }
  }

  public ILcdLOSCoverageMatrix getMatrix() {
    return fLOSCoverageMatrix;
  }

  public ILcdGeoReference getGeoReference() {
    return fGeoReference;
  }

  @Override
  public void setRadius(double aRadius) {
    super.setRadius(Math.min(Math.max(aRadius, 10), 25e3));
  }
}
