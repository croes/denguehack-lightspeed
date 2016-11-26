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
import java.util.Vector;

import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.util.TLcdConstant;

/**
 * A utility class that receives successive TLcdTouchEvents and detects when they can be
 * acceptably interpreted as an ellipse or a part of an ellipse.
 * For all the events that have the same ID (i.e. belong to the same continuous movement), it
 * computes the ellipse that best approximates the touch point locations. If the ellipse
 * properties are within some specified values, an EllipseGesture object is constructed
 * based on the detected parameters.
 */
public class EllipseGestureRecognizer extends AGestureRecognizer {

  // The minimum and maximum allowed lengths of the segment in centimeters.
  private double fMinRadius;
  private double fMaxRadius;

  // The maximum allowed deviation from the ellipse, relative to the semi-minor axis.
  private double fMaxDeviation;

  // The maximum allowed flattening (a-b)/b.
  private double fMaxFlattening;

  public EllipseGestureRecognizer(double aMinRadius, double aMaxRadius,
                                  double aAMaxStdDeviation,
                                  double aMaxFlattening,
                                  long aMaxDuration) {
    super(aMaxDuration);
    fMinRadius = aMinRadius;
    fMaxRadius = aMaxRadius;
    fMaxDeviation = aAMaxStdDeviation;
    fMaxFlattening = aMaxFlattening;
    fMaxDuration = aMaxDuration;

    fEvents = new Vector<TLcdTouchEvent>();
    fStatus = Status.FAILED;
  }

  public EllipseGestureRecognizer() {
    this(1, 15, 0.5, 2, 1000);
  }

  protected void processRecordedEvents(TLcdTouchEvent aLastTouchEvent) {
    List<TLcdTouchPoint> descriptors = aLastTouchEvent.getTouchPoints();
    if (descriptors.size() > 1) {
      // More than one finger - this is not the gesture we are looking for.
      stopGestureRecognition();
      notifyListeners();
      return;
    }

    final boolean lastEvent = descriptors.size() == 1 &&
                              descriptors.get(0).getState() ==
                              TLcdTouchPoint.State.UP;

    if (lastEvent) {
      // This is the last event of the chain. We compute the final answer.
      fGesture = createGesture();
      fEvents.clear();
      if (fGestureTimer != null) {
        fGestureTimer.cancel();
        fGestureTimer = null;
      }
      fStatus = fGesture != null ? Status.RECOGNIZED : Status.FAILED;
      notifyListeners();
      return;
    }

    // Try to detect an early failure.
    if (fEvents.size() < 30) {
      // Not enough points for a clear answer. Return - we are still marked as in PROGRESS.
      return;
    }

    if (createGesture() == null) {
      // The events we have so far do not look like a valid ellipse. Mark as FAILED.
      stopGestureRecognition();
      notifyListeners();
    }
  }

  private EllipseGesture createGesture() {
    // Extract the individual touch points from the list of events
    Vector<Point> points = new Vector<Point>(fEvents.size());
    for (TLcdTouchEvent event : fEvents) {
      points.add(event.getTouchPoints().get(0).getLocation());
    }

    EllipseFitter ellipseFitter = new EllipseFitter();
    if (!ellipseFitter.optimize(points)) {
      return null;
    }

    // Verify if the computed ellipse is acceptable.
    double a = ellipseFitter.getA() * TLcdConstant.I2CM / fDotsPerInch;
    double b = ellipseFitter.getA() * TLcdConstant.I2CM / fDotsPerInch;
    double dev = ellipseFitter.getMaxDeviation() * TLcdConstant.I2CM / fDotsPerInch;
    dev /= Math.min(a, b);

    if (a >= fMinRadius &&
        a <= fMaxRadius &&
        b >= fMinRadius &&
        b <= fMaxRadius &&
        dev <= fMaxDeviation) {

      double cx = ellipseFitter.getLocationX();
      double cy = ellipseFitter.getLocationY();
      double A = ellipseFitter.getA();
      double B = ellipseFitter.getB();
      double rot = Math.toRadians(ellipseFitter.getRotation());
      double cos = Math.cos(rot);
      double sin = Math.sin(rot);

      // Verify the flattening
      if (A > B) {
        if ((A - B) / B > fMaxFlattening) {
          return null;
        }
      } else {
        if ((B - A) / A > fMaxFlattening) {
          return null;
        }
      }

      // Verify if the points describe a nice circular motion and are not jumping forth and back
      // along the contour.
      int orientation = 0;
      double previous_t = 0;
      for (int i = 0; i < points.size(); i++) {
        Point point = points.get(i);
        double x = point.x - cx;
        double y = point.y - cy;

        double xr = x * cos - y * sin;
        double yr = x * sin + y * cos;

        double crt_t = Math.atan2(A * yr, B * xr);
        if (i > 0) {
          int new_orientation = getOrientation(previous_t, crt_t);
          if (orientation != 0 && new_orientation != 0 && orientation != new_orientation) {
            // The consecutive points changed direction abruptly, This is not what we want.
            return null;
          }
          if (new_orientation != 0) {
            orientation = new_orientation;
          }
        }
        previous_t = crt_t;
      }
      return new EllipseGesture(ellipseFitter.getLocationX(),
                                ellipseFitter.getLocationY(),
                                ellipseFitter.getA(),
                                ellipseFitter.getB(),
                                ellipseFitter.getRotation());
    }

    return null;
  }

  /**
   * Returns 0,-1 or 1, depending on the orientation of the closest path from angle1 to angle2
   * @param aAngle1 start angle (radians)
   * @param aAngle2 end angle (radians)
   * @return 0 if the points are too close to decide, -1 if the direction is clockwise, 1 otherwise
   */
  private int getOrientation(double aAngle1, double aAngle2) {
    final double PI2 = Math.PI * 2;
    final double PI = Math.PI;
    double arc = aAngle1 - aAngle2;
    if (Math.abs(arc) < 1e-5) {
      return 0;
    }
    if (arc > PI2) {
      arc -= PI2;
    }
    if (arc < -PI2) {
      arc += PI2;
    }
    return ((arc < 0 && arc >= -PI) || arc > PI) ? 1 : -1;
  }
}
