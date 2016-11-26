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
package samples.lightspeed.common.tracks;

import java.util.ArrayList;
import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;

/**
 * Factory that creates {@link LonLatTrajectories}.
 *
 * @since 2012.0
 */
public class LonLatTrajectoriesFactory {

  /**
   * Creates <code>aCount</code> connections between elements of <code>aModel</code>.
   *
   * @param aModel
   *        model holding the elements between which connections are created
   * @param aCount
   *        the number of connections to create
   * @param aModelReference
   *        the model reference for the trajectories model
   * @return a collection of polylines and points on those polylines
   */
  public static LonLatTrajectories createTrajectories(ILcdModel aModel, int aCount, ILcdModelReference aModelReference) {
    LonLatTrajectories.Builder builder = new LonLatTrajectories.Builder(aModelReference);

    // Get all the shapes.
    ArrayList<ILcdShape> shapes = new ArrayList<ILcdShape>();
    for (Enumeration<?> e = aModel.elements(); e.hasMoreElements(); ) {
      shapes.add((ILcdShape) e.nextElement());
    }
    // And create aCount connections between the focus points of two random shapes.
    final int size = shapes.size();
    for (int i = 0; i < aCount; i++) {
      ILcdShape shape0 = shapes.get((int) (size * Math.random()));
      ILcdShape shape1 = shapes.get((int) (size * Math.random()));
      if (shape0.equals(shape1)) {
        i--;
        continue;
      }
      ILcdPoint p0 = shape0.getFocusPoint();
      ILcdPoint p1 = shape1.getFocusPoint();

      TLcdLonLatHeightPolyline polyline = new TLcdLonLatHeightPolyline();
      final TLcdLonLatHeightPoint tempPoint = new TLcdLonLatHeightPoint();
      for (double j = 0; j <= LonLatTrajectories.HEIGHT_STEPS; j++) {
        ((ILcdGeodeticReference) aModelReference).getGeodeticDatum().getEllipsoid().geodesicPointSFCT(p0, p1, j / LonLatTrajectories.HEIGHT_STEPS, tempPoint);
        polyline.insert3DPoint((int) j, tempPoint.getX(), tempPoint.getY(),
                               LonLatTrajectories
                                   .getHeightOnLine(j / LonLatTrajectories.HEIGHT_STEPS)
        );
      }
      builder.add(polyline);
    }

    // Return the resulting Trajectories object.
    return builder.build();
  }

}
