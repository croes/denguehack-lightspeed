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
package samples.gxy.labels.interactive;

import java.awt.AWTEvent;
import java.awt.Point;
import java.util.List;

import com.luciad.input.ILcdAWTEventListener;

import samples.gxy.common.touch.TouchPointTracker;

/**
 * <p>Utility class which helps in recognizing so-called "pinch-gestures" with two input
 * points.</p>
 *
 * <p>Note: this class assumes all incoming <code>TLcdTouchEvent</code> instances have the same
 * {@linkplain com.luciad.input.touch.TLcdTouchEvent#getTouchDeviceID() device ID} and {@linkplain
 * com.luciad.input.touch.TLcdTouchEvent#getUserID() user ID}. It does not perform any
 * checks on this condition. It is up to the user of this class to fulfil this condition.</p>
 */
public class PinchRecognizer implements ILcdAWTEventListener {
  private double fTotalPinchFactor;
  private double fLastPinchFactor;

  private final TouchPointTracker fTracker;

  public PinchRecognizer() {
    fTracker = new TouchPointTracker(2);
  }

  public void handleAWTEvent(AWTEvent aEvent) {
    fTracker.handleAWTEvent(aEvent);
    if (fTracker.isTrackingInProgress()) {
      //tracking is valid, update the total and last pinch factor
      List<Point> originalLocations = fTracker.getOriginalLocations();
      List<Point> previousLocations = fTracker.getPreviousLocations();
      List<Point> currentLocations = fTracker.getCurrentLocations();
      fTotalPinchFactor = calculateDistance(currentLocations.get(0), currentLocations.get(1)) / calculateDistance(originalLocations.get(0), originalLocations.get(1));
      fLastPinchFactor = calculateDistance(currentLocations.get(0), currentLocations.get(1)) / calculateDistance(previousLocations.get(0), previousLocations.get(1));
    }
  }

  /**
   * <p>Returns <code>true</code> when a pinch is in progress, <code>false</code> otherwise.</p>
   *
   * <p>When this method returns <code>false</code>, all other methods of this class returning pinch
   * related information can return bogus information. Those methods should only be called when this
   * method returns <code>true</code></p>
   *
   * @return <code>true</code> when a pinch is in progress, <code>false</code> otherwise
   */
  public boolean isPinchInProgress() {
    return fTracker.isTrackingInProgress();
  }

  /**
   * Returns an array containing the two points where the pinch started, expressed in screen
   * coordinates
   *
   * @return an array containing the two points where the pinch started
   */
  public Point[] getOriginalLocations() {
    return fTracker.getOriginalLocations().toArray(new Point[2]);
  }

  /**
   * Returns an array containing the current locations of the two points which triggered the pinch,
   * expressed in screen coordinates. The first point in the array corresponds to the current
   * location of the first point of {@link #getOriginalLocations()}.
   *
   * @return an array containing the current locations of the two points which triggered the pinch
   */
  public Point[] getCurrentLocations() {
    return fTracker.getCurrentLocations().toArray(new Point[2]);
  }

  /**
   * Returns an array containing the previous locations of the two points which triggered the pinch,
   * expressed in screen coordinates. The first point in the array corresponds to the previous
   * location of the first point of {@link #getOriginalLocations()}.
   *
   * @return an array containing the previous locations of the two points which triggered the pinch
   */
  public Point[] getPreviousLocations() {
    return fTracker.getCurrentLocations().toArray(new Point[2]);
  }

  /**
   * Returns a factor representing the size of the total pinch. This factor is determined by the
   * difference between the {@linkplain #getCurrentLocations() current} and {@linkplain
   * #getOriginalLocations() original} locations.
   *
   * @return a positive factor representing the size of the absolute pinch. When smaller than one,
   *         the input points approached each other. When larger than one, the distance between the
   *         input points has increased. If one, the distance between the input points remained
   *         constant.
   */
  public double getTotalPinchFactor() {
    return fTotalPinchFactor;
  }

  /**
   * Returns a factor representing the size of the last part of the pinch. This factor is determined
   * by the difference between the {@linkplain #getCurrentLocations() current} and {@linkplain
   * #getPreviousLocations() previous} locations.
   *
   * @return a positive factor representing the size of the absolute pinch. When smaller than one,
   *         the input points approached each other. When larger than one, the distance between the
   *         input points has increased. If one, the distance between the input points remained
   *         constant.
   */
  public double getLastPinchFactor() {
    return fLastPinchFactor;
  }

  /**
   * Calculate the distance between two points. The points must be expressed in the same coordinate
   * system
   *
   * @param aFirstPoint  the first point
   * @param aSecondPoint the second point
   *
   * @return the distance between the two points, expressed in the same coordinate system as was
   *         used for the points
   */
  private double calculateDistance(Point aFirstPoint, Point aSecondPoint) {
    return Math.sqrt(Math.pow(aFirstPoint.x - aSecondPoint.x, 2) + Math.pow(aFirstPoint.y - aSecondPoint.y, 2));
  }
}
