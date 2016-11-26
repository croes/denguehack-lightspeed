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
package samples.gxy.undo;

import java.util.Map;

import com.luciad.geodesy.ILcdEllipsoid;
import samples.common.undo.StateAware;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;

/**
 * Extension of TLcdLonLatPolyline that implements StateAware. The state of this object consists of
 * the location of its points.
 */
class Polyline extends TLcdLonLatPolyline implements StateAware {

  private static final String POINTS_KEY = "points";

  public Polyline(ILcdEllipsoid aEllipsoid) {
    super(aEllipsoid);
  }

  public Polyline(ILcd2DEditablePointList aPointList, ILcdEllipsoid aEllipsoid) {
    super(aPointList, aEllipsoid);
  }

  /**
   * Implementation of storeState that stores the state specific to this Polyline: the location of
   * its points.
   *
   * @param aMap The map in which the state should be stored.
   * @param aSourceModel The model of this pointlist
   */
  public void storeState(Map aMap, ILcdModel aSourceModel) {
    //store the location of the points.
    int count = getPointCount();
    ILcdPoint[] points = new ILcdPoint[count];
    for (int i = 0; i < count; i++) {
      // create separate clones of the point, to make this stored state
      // independent of the actual state of the polyline.
      points[i] = (ILcdPoint) getPoint(i).clone();
    }
    aMap.put(POINTS_KEY, points);
  }

  public void restoreState(Map aMap, ILcdModel aTargetModel) {
    ILcdPoint[] points = (ILcdPoint[]) aMap.get(POINTS_KEY);
    if (points != null) {
      equalizeNumberOfPoints(points);
      for (int i = 0; i < points.length; i++) {
        move2DPoint(i, points[i].getX(), points[i].getY());
      }
    }
  }

  /**
   * Make sure that there are the same amount of points in this Polyline as in the stored array of
   * points.
   *
   * @param aPoints The stored array of points.
   */
  private void equalizeNumberOfPoints(ILcdPoint[] aPoints) {
    while (getPointCount() < aPoints.length) {
      insert2DPoint(0, 0.0d, 0.0d);
    }
    while (getPointCount() > aPoints.length) {
      removePointAt(0);
    }
  }
}
