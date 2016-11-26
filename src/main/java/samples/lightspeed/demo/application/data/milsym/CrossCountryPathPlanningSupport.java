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
import com.luciad.network.algorithm.routing.TLcdCrossCountryShortestRouteAlgorithm;
import com.luciad.network.function.ALcdCrossCountryHeightProviderDistanceFunction;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.lightspeed.ILspView;

public class CrossCountryPathPlanningSupport {

  public static final double MAX_DISTANCE = 50e3; // 50km

  public static ILcdRoute<ILcdPoint, ILcdPolyline> getShortestRoute(ILcdPoint startPoint, ILcdPoint endPoint, ILcdGeoReference aReference,
                                                                    ILspView aView) {

    final TLcdLonLatBounds smallerRegion = new TLcdLonLatBounds(startPoint);
    smallerRegion.setToIncludePoint2D(endPoint);

    double maxSize = 0.5 * Math.max(smallerRegion.getWidth(), smallerRegion.getHeight());
    smallerRegion.translate2D(-maxSize, -maxSize);
    smallerRegion.setWidth(smallerRegion.getWidth() + 2 * maxSize);
    smallerRegion.setHeight(smallerRegion.getHeight() + 2 * maxSize);

    double meters = aReference.getGeodeticDatum().getEllipsoid().geodesicDistance(startPoint, endPoint);
    double lonlat = TLcdCartesian.distance2D(startPoint, endPoint);

    double lonLatToMeters = meters / lonlat;

    // Step size of roughly 30 meters
    double dlonlat = lonlat / (meters / 30);

    TLcdCrossCountryShortestRouteAlgorithm algorithm = new TLcdCrossCountryShortestRouteAlgorithm(dlonlat, dlonlat);

    ALcdCrossCountryHeightProviderDistanceFunction distanceFunction =
        new SlopeDistanceFunction(aView, aReference,
                                  aView.getServices().getTerrainSupport().getModelHeightProvider(aReference, 1 / Math.pow(dlonlat, 2)),
                                  lonLatToMeters);

    return algorithm.getShortestRoute(
        startPoint, endPoint,
        distanceFunction,
        new EuclideanDistanceFunction(new TLcdLonLatBounds(-180, -90, 360, 180), lonLatToMeters),
        Double.MAX_VALUE
    );
  }
}
