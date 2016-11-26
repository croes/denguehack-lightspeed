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
package samples.gxy.common.touch;

import java.awt.AWTEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.luciad.input.ILcdAWTEventListener;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

/**
 * <p>Utility class allowing to follow a certain number of input points for
 * <code>TLcdTouchEvent</code>s.</p>
 *
 * <p>The tracking will be activated when the number of points in the <code>TLcdTouchEvent</code>
 * reaches the specified number of input points, and will stop as soon as one input point is removed
 * or added.</p>
 *
 * <p>The tracking will not be re-activated until a <code>TLcdTouchEvent</code> is found with a new
 * {@linkplain TLcdTouchEvent#getTouchEventID() touch event ID} for which the
 * number of points matches the specified number of input points.</p>
 *
 * <p>Note: this class assumes all incoming <code>TLcdTouchEvent</code> instances have the same
 * {@linkplain TLcdTouchEvent#getTouchDeviceID() device ID} and {@linkplain
 * TLcdTouchEvent#getUserID() user ID}. It does not perform any
 * checks on this condition. It is up to the user of this class to fulfil this condition.</p>
 */
public class TouchPointTracker implements ILcdAWTEventListener {
  /**
   * List with the original locations of the input points, expressed in screen coordinates
   */
  private List<Point> fOriginalLocations;
  /**
   * List with the previous locations of the input points, expressed in screen coordinates
   */
  private List<Point> fPreviousLocations;
  /**
   * List with the current locations of the input points, expressed in screen coordinates
   */
  private List<Point> fCurrentLocations;
  /**
   * List with the {@linkplain TLcdTouchPoint#getID()
   * touch point IDs} of the tracked input points
   */
  private List<Long> fTouchPointIDs;
  /**
   * Boolean indicating whether tracking is in progress or not
   */
  private boolean fTrackingInProgress = false;
  /**
   * Boolean indicating whether tracking has been done for the last touch event ID
   */
  private boolean fTrackingDoneForLastTouchEventID = false;
  /**
   * The ID of the last touch event
   */
  private Long fLastTouchEventID = null;
  /**
   * The number of input points that must be tracked
   */
  private final int fNumberOfPoints;

  /**
   * Create a new <code>TouchPointTracker</code> for <code>aNumberOfInputPoints</code>
   *
   * @param aNumberOfInputPoints the number of input points that must be tracked
   */
  public TouchPointTracker(int aNumberOfInputPoints) {
    fNumberOfPoints = aNumberOfInputPoints;
    fOriginalLocations = new LinkedList<Point>();
    fPreviousLocations = new LinkedList<Point>();
    fCurrentLocations = new LinkedList<Point>();
    fTouchPointIDs = new LinkedList<Long>();
  }

  public void handleAWTEvent(AWTEvent aEvent) {
    //we only accept TLcdTouchEvent instances
    if (aEvent instanceof TLcdTouchEvent) {
      //for the first event, the fLastTouchEventID is still null
      if (fLastTouchEventID == null) {
        fLastTouchEventID = ((TLcdTouchEvent) aEvent).getTouchEventID();
        fTrackingInProgress = false;
        fTrackingDoneForLastTouchEventID = false;
      }

      //when a new touch input sequence starts, we did not yet start tracking
      if (fLastTouchEventID != ((TLcdTouchEvent) aEvent).getTouchEventID()) {
        fTrackingDoneForLastTouchEventID = false;
        fTrackingInProgress = false;
        fLastTouchEventID = ((TLcdTouchEvent) aEvent).getTouchEventID();
      }

      //when tracking is in progress, handle the new information
      if (fTrackingInProgress) {
        continueTracking(((TLcdTouchEvent) aEvent));
      }
      //when no tracking is in progress, only handle the event when we did not track the event previously
      else {
        if (!(fTrackingDoneForLastTouchEventID)) {
          startTracking(((TLcdTouchEvent) aEvent));
        }
      }
    }
  }

  /**
   * <p>When tracking is in progress, handle the next <code>TLcdTouchEvent</code>.</p>
   *
   * <p>This method can either stop the tracking, or update the relevant point information.</p>
   *
   * @param aTouchEvent the incoming <code>TLcdTouchEvent</code>
   */
  private void continueTracking(TLcdTouchEvent aTouchEvent) {
    TLcdTouchPoint.State state = aTouchEvent.getModifiedTouchPoint().getState();
    if (state != TLcdTouchPoint.State.MOVED) {
      //this means an extra touch point is created, or a touch point is removed. Stop the tracking
      fTrackingInProgress = false;
      //clear the lists
      fOriginalLocations.clear();
      fCurrentLocations.clear();
      fPreviousLocations.clear();
      fTouchPointIDs.clear();
    } else {
      //copy the current locations into the previous locations
      fPreviousLocations.clear();
      fPreviousLocations.addAll(fCurrentLocations);

      //find the modified touch point
      int index = fTouchPointIDs.indexOf(aTouchEvent.getModifiedTouchPoint().getID());

      //remove the corresponding point from the current locations
      fCurrentLocations.remove(index);

      //store the new location in screen coordinates
      Point modifiedPoint = aTouchEvent.getModifiedTouchPoint().getLocation();
      fCurrentLocations.add(index, modifiedPoint);
    }
  }

  /**
   * <p>When no tracking is in progress, handle the incoming <code>TLcdTouchEvent</code> and
   * indicate tracking started if necessary</p>
   *
   * @param aTouchEvent the incoming <code>TLcdTouchEvent</code>
   */
  private void startTracking(TLcdTouchEvent aTouchEvent) {
    List<TLcdTouchPoint> touchPointList = aTouchEvent.getTouchPoints();
    //we only track events with the correct number of points
    if (touchPointList.size() == fNumberOfPoints) {
      //store all information
      for (TLcdTouchPoint descriptor : touchPointList) {
        Point pointInScreenCoordinates = descriptor.getLocation();
        fOriginalLocations.add(pointInScreenCoordinates);
        fPreviousLocations.add(pointInScreenCoordinates);
        fCurrentLocations.add(pointInScreenCoordinates);
        fTouchPointIDs.add(descriptor.getID());
      }

      //indicate tracking has started
      fTrackingInProgress = true;

      //indicate tracking has been started for the current event
      fTrackingDoneForLastTouchEventID = true;
    }
  }

  /**
   * <p>The locations of the tracked input points when the tracking started. Locations are expressed
   * in screen coordinates.</p>
   *
   * <p>The order of the points in the returned list matches the order of the lists returned in
   * {@link #getPreviousLocations()} and {@link #getCurrentLocations()}.</p>
   *
   * <p>The result of this method is only valid when {@link #isTrackingInProgress()} returns
   * <code>true</code>.</p>
   *
   * @return the original locations of the tracked input points
   */
  public List<Point> getOriginalLocations() {
    return copyPointList(fOriginalLocations);
  }

  /**
   * <p>The locations of the tracked input points during the previous event. Locations are expressed
   * in screen coordinates.</p>
   *
   * <p>The order of the points in the returned list matches the order of the lists returned in
   * {@link #getOriginalLocations()} and {@link #getCurrentLocations()}.</p>
   *
   * <p>The result of this method is only valid when {@link #isTrackingInProgress()} returns
   * <code>true</code>.</p>
   *
   * @return the previous locations of the tracked input points
   */
  public List<Point> getPreviousLocations() {
    return copyPointList(fPreviousLocations);
  }

  /**
   * <p>The current locations of the tracked input points. Locations are expressed in screen
   * coordinates.</p>
   *
   * <p>The order of the points in the returned list matches the order of the lists returned in
   * {@link #getOriginalLocations()} and {@link #getPreviousLocations()}.</p>
   *
   * <p>The result of this method is only valid when {@link #isTrackingInProgress()} returns
   * <code>true</code>.</p>
   *
   * @return the current locations of the tracked input points
   */
  public List<Point> getCurrentLocations() {
    //return a copy of the list
    return copyPointList(fCurrentLocations);
  }

  /**
   * Returns <code>true</code> when tracking is in progress, and the {@link
   * #getOriginalLocations()}, {@link #getPreviousLocations()} and {@link #getCurrentLocations()}
   * return valid input.
   *
   * @return <code>true</code> when tracking is in progress, <code>false</code> otherwise
   */
  public boolean isTrackingInProgress() {
    return fTrackingInProgress;
  }

  private List<Point> copyPointList(List<Point> aPointList) {
    List<Point> result = new ArrayList<Point>();
    for (Point point : aPointList) {
      result.add(new Point(point));
    }
    return result;
  }
}
