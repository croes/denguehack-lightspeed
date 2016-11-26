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

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.network.function.ILcdCrossCountryDistanceFunction;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;

/**
 * An distance function that computes an euclidean distance.
 */
public class EuclideanDistanceFunction implements ILcdCrossCountryDistanceFunction {

  private ILcdBounds fBounds;
  private double fLonLatToMeters;

  public EuclideanDistanceFunction(ILcdBounds aBounds, double aLonLatToMeters) {
    fBounds = aBounds;
    fLonLatToMeters = aLonLatToMeters;
  }

  public double computeDistance(ILcdPoint aStartPoint, ILcdPoint aEndPoint) {
    return fLonLatToMeters * TLcdCartesian.distance2D(aStartPoint, aEndPoint);
  }

  public ILcdBounds getBounds() {
    return fBounds;
  }
}
