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
package samples.network.basic.function;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.network.function.ALcdSimpleEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.shape.ILcdPolyline;

import samples.network.numeric.preprocessor.ISimpleEdgeValueFunction;

/**
 * An <code>ILcdEdgeValueFunction</code> that returns for each edge the
 * sum of the geodetic lengths of the lines making up the edge.
 */
public class GeodeticEdgeValueFunction extends ALcdSimpleEdgeValueFunction implements ISimpleEdgeValueFunction {

  private ILcdEllipsoid fEllipsoid;

  public GeodeticEdgeValueFunction() {

    ILcdGeodeticDatum datum = new TLcdGeodeticDatum();
    fEllipsoid = datum.getEllipsoid();

  }

  public GeodeticEdgeValueFunction(ILcdEllipsoid aSEllipsoid) {
    fEllipsoid = aSEllipsoid;
  }

  /**
   * Gets the ellipsoid on which the geodesic distance between points will be calculated.
   *
   * @return
   */
  public ILcdEllipsoid getEllipsoid() {
    return fEllipsoid;
  }

  /**
   * Sets the ellipsoid on which the geodesic distance between points will be calculated.
   *
   * @param aEllipsoid
   */
  public void setEllipsoid(ILcdEllipsoid aEllipsoid) {
    fEllipsoid = aEllipsoid;
  }

  public double computeEdgeValue(ILcdGraph aGraph, Object aEdge, TLcdTraversalDirection aTraversalDirection) {
    return getEdgeValue(aEdge, aTraversalDirection);
  }

  public double getEdgeValue(Object aEdge, TLcdTraversalDirection aTraversalDirection) {
    if (!(aEdge instanceof ILcdPolyline)) {
      throw new IllegalArgumentException("The edges must be ILcdPolyline");
    }
    ILcdPolyline polyline = (ILcdPolyline) aEdge;

    double distance = 0;
    for (int i = 0; i < polyline.getPointCount() - 1; i++) {
      distance +=
          fEllipsoid.geodesicDistance(polyline.getPoint(i), polyline.getPoint(i + 1));
    }
    return distance;
  }
}