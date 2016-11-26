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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.util.TLcdConstant;

/**
 * A utility class that receives successive TLcdTouchEvents and detects when they can be interpreted
 * as a flick gesture.
 * For all the events that have the same ID (i.e. belong to the same continuous movement), it
 * computes the line segment that best approximates the touch point locations. If the line segment
 * accurately matches a predefined length and direction, a FlickGesture object is constructed 
 * based on the detected parameters.
 */
public class FlickGestureRecognizer extends AGestureRecognizer {

  // The minimum and maximum allowed lengths of the segment in centimeters.
  private double fMinSegmentLength = 1;
  private double fMaxSegmentLength = 6;

  // The direction of the segment in degrees, measured counterclockwise from 3 o'clock.
  private double fSegmentDirection = 0;

  // The maximum allowed arc error, measured in degrees.
  private double fMaxDirectionError;

  // The maximum allowed deviation from the best-fitting line, divided by the segment's length.
  private double fMaxRelativeDeviation;

  // How many fingers should be used for this gesture.
  private int fExpectedFingerCount;

  /**
   * Constructs a flick gesture recognizer with the given parameters.
   * @param aMinSegmentLength the minimum length of the line segment, in centimeters.
   * @param aMaxSegmentLength the maximum length of the line segment, in centimeters.
   * @param aMaxRelativeDeviation the maximum deviation from a straight line, relative to the segment length
   * @param aSegmentDirection the trigonometric direction of the line segment .
   * @param aMaxDirectionError the maximum angle deviation from the given direction (in degrees).
   * @param aFingerCount the number of fingers that should be used for this gesture.
   */
  public FlickGestureRecognizer(double aMinSegmentLength,
                                double aMaxSegmentLength,
                                double aMaxRelativeDeviation,
                                double aSegmentDirection,
                                double aMaxDirectionError,
                                long aMaxDuration,
                                int aFingerCount) {
    super(aMaxDuration);
    fMinSegmentLength = aMinSegmentLength;
    fMaxSegmentLength = aMaxSegmentLength;
    fSegmentDirection = aSegmentDirection;
    fMaxDirectionError = aMaxDirectionError;
    fMaxRelativeDeviation = aMaxRelativeDeviation;
    fExpectedFingerCount = aFingerCount;
  }

  public FlickGestureRecognizer() {
    this(3, 4, 0.05, 0, 30, 200, 1);
  }

  protected void processRecordedEvents(TLcdTouchEvent aLastTouchEvent) {
    List<TLcdTouchPoint> descriptors = aLastTouchEvent.getTouchPoints();
    if (descriptors.size() > fExpectedFingerCount) {
      // More fingers than allowed - this is not the gesture we are looking for.
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
    if (fEvents.size() < 10) {
      // Not enough points for a clear answer. Return - we are still marked as in PROGRESS.
      return;
    }

    if (createGesture() == null) {
      // The events we have so far do not look like a valid flick gesture. Mark as FAILED.
      stopGestureRecognition();
      notifyListeners();
    }
  }

  /**
   * This method parses the list of touch events and separates the locations of each finger into a
   * distinct vector of points. A line segment is fitted to each vector, then the following checks
   * are made:
   *   - each segment must comply with the length and direction limits specified by the user
   *   - all segments must be approximately parallel and of the same length
   *
   * @return a FlickGesture if all conditions are met, null otherwise.
   */
  protected IGesture createGesture() {

    // We use a hashmap to separate the location of the individual fingers. The keys are the IDs of
    // the touch point descriptors (fingers) and the values are vectors containing the consecutive
    // locations of the corresponding fingers.
    HashMap<Long, Vector<Point>> id2points = new HashMap<Long, Vector<Point>>(fEvents.size());
    for (TLcdTouchEvent event : fEvents) {

      // There is a single non-stationary descriptor in each touch event. Let's find it.
      for (TLcdTouchPoint descriptor : event.getTouchPoints()) {
        if (descriptor.getState() ==
            TLcdTouchPoint.State.STATIONARY) {
          // Stationary descriptors are just duplicates of previous TOUCH_DOWN or TOUCH_MOVE events.
          // Skip them.
          continue;
        }

        // Add the location of this finger to the correct vector.
        Long touchPointID = descriptor.getID();
        Vector<Point> points = id2points.get(touchPointID);
        if (points == null) {
          points = new Vector<Point>();
          id2points.put(touchPointID, points);
        }
        points.add(new Point(descriptor.getLocation()));
      }
    }

    if (id2points.size() < fExpectedFingerCount) {
      return null;
    }

    // Variables used for computing the average location, length and direction of the entire
    // group of segments.
    double x = 0;
    double y = 0;
    double length = 0;

    // Create a LineFitter for each vector of finger locations.     
    LineFitter[] lineFitters = new LineFitter[id2points.keySet().size()];
    int index = 0;
    Iterator<Vector<Point>> it = id2points.values().iterator();
    if (it.hasNext()) {
      Vector<Point> points = it.next();
      if (points.size() < 2) {
        return null;
      }
      LineFitter lineFitter = new LineFitter();
      lineFitter.optimize(points);

      // Verify if the computed line segment matches the user requirements.
      double lengthInCM = lineFitter.getLength() * TLcdConstant.I2CM / fDotsPerInch;
      if (lengthInCM >= fMinSegmentLength &&
          lengthInCM <= fMaxSegmentLength &&
          lineFitter.getMaxDeviation() / lineFitter.getLength() <= fMaxRelativeDeviation &&
          TLcdCartesian.containsAngle(fSegmentDirection - fMaxDirectionError / 2,
                                      fMaxDirectionError,
                                      lineFitter.getDirection())) {

        // Verify if the current segment is parallel to and of the same length as the first segment.
        if (index > 0) {
          final double relative_length =
              (lineFitter.getLength() - lineFitters[0].getLength()) / lineFitters[0].getLength();
          if (Math.abs(relative_length) > 0.1) {
            // The length of the current segment is too different from the length of the first.
            return null;
          }

          if (!TLcdCartesian.containsAngle(lineFitters[0].getDirection() - 15, 30,
                                           lineFitter.getDirection())) {
            // The current segment is not parallel to the first segment.
            return null;
          }
        }
      } else {
        // This segment does not match the required gesture parameters.
        return null;
      }
      lineFitters[index++] = lineFitter;
      x += lineFitter.getLocationX();
      y += lineFitter.getLocationY();
      length += lineFitter.getLength();
    }

    return new FlickGesture(fEvents.get(0).getSource(), x, y, length, fSegmentDirection);
  }
}
