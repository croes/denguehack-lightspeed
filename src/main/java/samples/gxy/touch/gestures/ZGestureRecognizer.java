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

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

/**
 * Recognizer for a "Z" gesture event - several connected line segments, belonging to the same
 * chain of events.  
 */
public class ZGestureRecognizer extends AGestureRecognizer {

  // The maximum allowed deviation from the best-fitting line, divided by the segment's length.
  private double fMaxRelativeDeviation;

  private int fSegmentCount;

  public ZGestureRecognizer() {
    this(3, 0.1);
  }

  public ZGestureRecognizer(int aSegmentCount, double aMaxRelativeDeviation) {
    super(800);     // The gesture should not take more than 400 milliseconds.
    fSegmentCount = aSegmentCount;
    fMaxRelativeDeviation = aMaxRelativeDeviation;
  }

  public void processRecordedEvents(TLcdTouchEvent aLastTouchEvent) {
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
      fGesture = createGesture(fSegmentCount);
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
    if (fEvents.size() < 60) {
      // Not enough points for a clear answer. Return - we are still marked as in PROGRESS.
      return;
    }

    // Try to find several connected segments.
    for (int segments = fSegmentCount; segments >= 1; segments--) {
      if (createGesture(segments) != null) {
        // The events we have so far can be approximated by one or more connected segments. 
        return;
      }
    }
    // The events we have so far do not look like a valid Z gesture. Mark as FAILED.
    stopGestureRecognition();
    notifyListeners();
  }

  private ZGesture createGesture(int aSegmentCount) {
    // Impose a reasonable constraint on the number of input points.
    if (fEvents.size() < 2 * aSegmentCount) {
      return null;
    }

    // Extract the individual touch points from the list of events
    Vector<Point> points = new Vector<Point>(fEvents.size());
    for (TLcdTouchEvent event : fEvents) {
      points.add(event.getTouchPoints().get(0).getLocation());
    }

    // Attempt to detect the N line segments in the event chain.
    double[] deviations = new double[aSegmentCount];      // Deviation from straight line.
    int[] segment_ends = new int[aSegmentCount + 1];     // Start/end indexes for each segment
    for (int i = 1; i < segment_ends.length; i++) {
      segment_ends[i] = i * points.size() / aSegmentCount;
    }
    segment_ends[aSegmentCount] = points.size();

    // Avoid any segment from degenerating into too few points. 
    int min_segment_length = segment_ends[1] / 3;
    if (min_segment_length < 2) {
      min_segment_length = 2;
    }

    LineFitter lineFitter = new LineFitter();
    computeAllSegmentDeviations(points, segment_ends, deviations);
    boolean improved;
    do {
      improved = false;
      for (int i = 0; i < aSegmentCount - 1; i++) {
        if (segment_ends[i + 1] - segment_ends[i] > min_segment_length) {
          lineFitter.optimize(points.subList(segment_ends[i], segment_ends[i + 1] - 1));
          double dev1 = lineFitter.getMaxDeviation();
          lineFitter.optimize(points.subList(segment_ends[i + 1] - 1, segment_ends[i + 2]));
          double dev2 = lineFitter.getMaxDeviation();

          if (dev1 + dev2 < deviations[i] + deviations[i + 1]) {
            improved = true;
            segment_ends[i + 1]--;
            deviations[i] = dev1;
            deviations[i + 1] = dev2;
            continue;
          }
        }
        if (segment_ends[i + 2] - segment_ends[i + 1] > min_segment_length) {
          lineFitter.optimize(points.subList(segment_ends[i], segment_ends[i + 1] + 1));
          double dev1 = lineFitter.getMaxDeviation();
          lineFitter.optimize(points.subList(segment_ends[i + 1] + 1, segment_ends[i + 2]));
          double dev2 = lineFitter.getMaxDeviation();

          if (dev1 + dev2 < deviations[i] + deviations[i + 1]) {
            improved = true;
            segment_ends[i + 1]++;
            deviations[i] = dev1;
            deviations[i + 1] = dev2;
          }
        }
      }
    } while (improved);

    double previous_direction = 0;
    boolean previous_left = false;
    ZGesture gesture = new ZGesture(fEvents.get(0).getSource(), aSegmentCount);
    for (int i = 0; i < aSegmentCount; i++) {
      lineFitter.optimize(points.subList(segment_ends[i], segment_ends[i + 1]));
      final double direction = lineFitter.getDirection();

      gesture.setDirection(i, direction);
      gesture.setSegmentLocationX(i, lineFitter.getLocationX());
      gesture.setSegmentLocationY(i, lineFitter.getLocationY());
      gesture.setSegmentLength(i, lineFitter.getLength());
      if (lineFitter.getMaxDeviation() / lineFitter.getLength() > fMaxRelativeDeviation) {
        return null;
      }

      if (i > 0) {
        // Minimum turn angle left or right from one segment to the next.
        final double MIN_TURN = 110;
        final double ARC = 180 - MIN_TURN;
        boolean left = TLcdCartesian.containsAngle(previous_direction + MIN_TURN, ARC, direction);
        boolean right = TLcdCartesian.containsAngle(previous_direction + 180, ARC, direction);
        if (!left && !right) {
          // This segment does not sufficiently change direction wrt the previous segment.
          return null;
        }
        if (i > 1) {
          if (left == previous_left) {
            // This is a "zig-zig" instead of a "zig-zag".
            // In other words, the segments describe a triangle instead of a "Z" shape. 
            return null;
          }
        }
        previous_left = left;
      }
      previous_direction = direction;
    }
    double cx = 0;
    double cy = 0;
    for (Point point : points) {
      cx += point.x;
      cy += point.y;
    }
    gesture.setLocationX(cx / points.size());
    gesture.setLocationX(cy / points.size());
    return gesture;
  }

  private void computeAllSegmentDeviations(List<Point> aPoints, int[] aIndexes, double[] aDeviations) {
    LineFitter lineFitter = new LineFitter();
    for (int i = 0; i < aDeviations.length; i++) {
      lineFitter.optimize(aPoints.subList(aIndexes[i], aIndexes[i + 1]));
      aDeviations[i] = lineFitter.getMaxDeviation();
    }
  }
}
