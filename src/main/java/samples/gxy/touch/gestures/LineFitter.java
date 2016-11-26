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
package samples.gxy.touch.gestures;

import java.awt.Point;
import java.util.List;

/**
 * A utility class that performs linear regression on a set of points.
 */
public class LineFitter {
  private double fDirection;  // The direction of the segment.
  private double fLength;     // The length of the segment.
  private double fLocationX;  // The middle of the segment - x coordinate
  private double fLocationY;  // The middle of the segment - y coordinate

  // The maximum deviation of any point from the segment
  private double fMaxDeviation;

  // The standard deviation
  private double fStdDeviation;

  public double getDirection() {
    return fDirection;
  }

  public double getLength() {
    return fLength;
  }

  public double getLocationX() {
    return fLocationX;
  }

  public double getLocationY() {
    return fLocationY;
  }

  public double getMaxDeviation() {
    return fMaxDeviation;
  }

  public double getStdDeviation() {
    return fStdDeviation;
  }

  public void optimize(List<Point> aPoints) {

    int sum_x = 0;
    int sum_y = 0;
    for (Point point : aPoints) {
      sum_x += point.x;
      sum_y += point.y;
    }

    fLocationX = sum_x / (double) aPoints.size();
    fLocationY = sum_y / (double) aPoints.size();

    double sum_dx2 = 0;
    double sum_dy2 = 0;
    double sum_dxy = 0;
    for (Point point : aPoints) {
      double dx = -point.x + fLocationX;
      double dy = point.y - fLocationY;
      sum_dx2 += dx * dx;
      sum_dy2 += dy * dy;
      sum_dxy += dx * dy;
    }

    final double angle = Math.atan2(-sum_dxy, (sum_dx2 - sum_dy2) / 2.0) / 2.0;
    final double cos = Math.cos(angle);
    final double sin = Math.sin(angle);

    fMaxDeviation = 0;
    fStdDeviation = 0;
    for (Point point : aPoints) {
      double err = Math.abs((point.y - fLocationY) * cos -
                            (point.x - fLocationX) * sin);
      if (fMaxDeviation < err) {
        fMaxDeviation = err;
      }
      fStdDeviation += err * err;
    }
    fStdDeviation = Math.sqrt(fStdDeviation / aPoints.size());

    final Point first = aPoints.get(0);
    final Point last = aPoints.get(aPoints.size() - 1);
    fLength = Math.hypot(last.x - first.x, last.y - first.y);

    double u = (Math.abs(sin) > Math.abs(cos)) ?
               (fLocationY - aPoints.get(0).y) / sin :
               (fLocationX - aPoints.get(0).x) / cos;
    if (u >= 0) {
      // The computed angle of the line segment points in the same direction as the input points.
      fDirection = Math.toDegrees(angle);
    } else {
      // The computed angle points in the opposite direction compared to the input points. 
      fDirection = Math.toDegrees(angle + Math.PI);
    }

    // Normalize the direction from 0 to 360 degrees. 
//    if(fDirection<  0) fDirection+=360;
//    if(fDirection>360) fDirection-=360;
  }
}
