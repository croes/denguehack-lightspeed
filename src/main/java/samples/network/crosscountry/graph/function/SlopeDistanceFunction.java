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
package samples.network.crosscountry.graph.function;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.network.function.ALcdCrossCountryRasterDistanceFunction;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;

/**
 * An edge value function that returns a large value for a steeper slope.
 */
public class SlopeDistanceFunction extends ALcdCrossCountryRasterDistanceFunction {

  private double fHeightScale;

  public SlopeDistanceFunction(ILcdRaster aRaster) {
    super(aRaster);

    ILcdBounds rasterBounds = getRaster().getBounds();
    double distX = TLcdEllipsoid.DEFAULT.geodesicDistance(rasterBounds.getLocation().getX(),
                                                          rasterBounds.getLocation().getY(),
                                                          rasterBounds.getLocation().getX() + rasterBounds.getWidth(),
                                                          rasterBounds.getLocation().getY());
    double distY = TLcdEllipsoid.DEFAULT.geodesicDistance(rasterBounds.getLocation().getX(),
                                                          rasterBounds.getLocation().getY(),
                                                          rasterBounds.getLocation().getX(),
                                                          rasterBounds.getLocation().getY() + rasterBounds.getHeight());
    fHeightScale = (rasterBounds.getWidth() / distX + rasterBounds.getHeight() / distY) * 0.5;
  }

  protected double computeDistance(ILcdPoint aStartPoint, double aStartValue, ILcdPoint aEndPoint, double aEndValue) {
    if (aStartValue == 0 || aEndValue == 0) {
      return Double.POSITIVE_INFINITY;
    }
    if (aStartValue < 0 || aEndValue < 0) {
      return Double.POSITIVE_INFINITY;
    }

    // Compute the distance
    double dz = (aEndValue - aStartValue) * fHeightScale;
    double dist = TLcdCartesian.distance2D(aStartPoint, aEndPoint);

    // Compute the slope angle in percent
    double slopeAngle = Math.atan2(Math.abs(dz), dist) / (Math.PI * 0.5);

    // Compute the cost
    if (slopeAngle > 0.3) {
      return Double.POSITIVE_INFINITY;
    } else if (slopeAngle > 0.2) {
      return dist * interpolate((slopeAngle - 0.2) / 0.1, 8, 20);
    } else if (slopeAngle > 0.1) {
      return dist * interpolate((slopeAngle - 0.1) / 0.1, 2, 8);
    } else {
      return dist * interpolate(slopeAngle / 0.1, 1, 2);
    }
  }

  private double interpolate(double aValue, double aMin, double aMax) {
    return aMin + aValue * (aMax - aMin);
  }
}
