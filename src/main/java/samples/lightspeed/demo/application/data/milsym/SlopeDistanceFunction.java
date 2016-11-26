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
package samples.lightspeed.demo.application.data.milsym;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.util.Collection;
import java.util.Enumeration;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.model.ILcdModel;
import com.luciad.network.function.ALcdCrossCountryHeightProviderDistanceFunction;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.support.los.LOSCoverage;
import samples.lightspeed.demo.framework.application.Framework;

/**
 * An edge value function that returns a large value for a steeper slope.
 */
public class SlopeDistanceFunction extends ALcdCrossCountryHeightProviderDistanceFunction {

  private double fLonLatToMeters;
  private ILspView fView;
  private ILcdGeoReference fGeoReference;

  public SlopeDistanceFunction(ILspView aView, ILcdGeoReference aGeoReference,
                               ILcdHeightProvider aHeightProvider, double aLonLatToMeters) {
    super(aHeightProvider);
    fLonLatToMeters = aLonLatToMeters;
    fView = aView;
    fGeoReference = aGeoReference;
  }

  // TODO: integrate LOS
  protected double computeDistance(ILcdPoint aStartPoint, double aStartValue, ILcdPoint aEndPoint, double aEndValue) {
    if (aStartValue == 0 || aEndValue == 0) {
      return Double.POSITIVE_INFINITY;
    }
    if (aStartValue < 2 || aEndValue < 2) {
      return Double.POSITIVE_INFINITY;
    }

    if (isVisible(aEndPoint, fGeoReference, getLOSLayers())) {
      return Double.POSITIVE_INFINITY;
    }

    double notInLosCirclePenalty = 1;
    // Disabled to avoid traveling to LOS region first
//    if(!isContainedInLoS( aEndPoint, getLOSLayers() )){
//       notInLosCirclePenalty = 1000;
//    }

    // Compute the distance
    double dz = Math.abs(aEndValue - aStartValue);
    double dist = TLcdCartesian.distance2D(aStartPoint, aEndPoint) * fLonLatToMeters;

    // Compute the slope angle in percent
    double slopeAngle = Math.atan2(dz, dist) / (Math.PI * 0.5);

    // Compute the cost
    if (slopeAngle > 0.3) {
      return 100 * dist * notInLosCirclePenalty;
    } else if (slopeAngle > 0.2) {
      return dist * interpolate((slopeAngle - 0.2) / 0.1, 8, 20) * notInLosCirclePenalty;
    } else if (slopeAngle > 0.1) {
      return dist * interpolate((slopeAngle - 0.1) / 0.1, 2, 8) * notInLosCirclePenalty;
    } else {
      return dist * interpolate(slopeAngle / 0.1, 1, 2) * notInLosCirclePenalty;
    }
  }

  private static double interpolate(double aValue, double aMin, double aMax) {
    return aMin + aValue * (aMax - aMin);
  }

  private boolean isVisible(ILcdPoint aPoint, ILcdGeoReference aGeoReference, Collection<ILspLayer> aLOSLayers) {
    if (aLOSLayers == null || aLOSLayers.isEmpty()) {
      return false;
    }

    for (ILspLayer layer : aLOSLayers) {
      ILcdModel model = layer.getModel();
      try (Lock autoUnlock = readLock(model)) {
        Enumeration modelElements = model.elements();
        while (modelElements.hasMoreElements()) {
          Object o = modelElements.nextElement();
          if (o instanceof LOSCoverage) {
            LOSCoverage coverage = (LOSCoverage) o;
            if (isPointVisible(aPoint, aGeoReference, coverage)) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private boolean isContainedInLoS(ILcdPoint aPoint, Collection<ILspLayer> aLOSLayers) {
    if (aLOSLayers == null || aLOSLayers.isEmpty()) {
      return false;
    }

    for (ILspLayer layer : aLOSLayers) {
      ILcdModel model = layer.getModel();
      Enumeration modelElements = model.elements();
      while (modelElements.hasMoreElements()) {
        Object o = modelElements.nextElement();
        if (o instanceof LOSCoverage) {
          LOSCoverage coverage = (LOSCoverage) o;
          if (coverage.contains2D(aPoint)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private boolean isPointVisible(ILcdPoint aPoint, ILcdGeoReference aGeoReference, LOSCoverage aCoverage) {
    return aCoverage != null && aCoverage.isPointVisible(aPoint, aGeoReference, 15);
  }

  private Collection<ILspLayer> getLOSLayers() {
    Framework framework = Framework.getInstance();
    Collection<ILspLayer> losLayers = framework.getLayersWithID("layer.id.milsym.los", fView);
    return losLayers;
  }
}
