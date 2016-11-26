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

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.util.TLcdConstant;

/**
 * This is a generic class for implementing a gesture recognizer. It handles the following tasks:
 * - buffering of the events
 * - maintaining the status of the recognizer (processing, recognized, failed)
 * - maintaining the list of associated listeners
 * - canceling the recognition if the gesture's maximum allowed duration is exceeded
 * - canceling the recognition if the speed of the gesture is too low.
 */
public abstract class AGestureRecognizer implements IGestureRecognizer {
  // The maximum allowed duration of the gesture, in milliseconds.
  protected long fMaxDuration;

  // A temporary buffer for storing the touch events currently being analyzed.
  protected Vector<TLcdTouchEvent> fEvents;

  // The ID of the touch events currently being analyzed.
  protected int fTouchEventID;

  protected Status fStatus;
  protected IGesture fGesture;

  // Timer for detecting if a gesture takes too long.
  protected Timer fGestureTimer;

  protected Vector<IGestureRecognizerListener> fListeners;

  // Minimum mouse speed, in centimeters per second
  private double fMinSpeed;

  // Take screen resolution into account.
  protected static int fDotsPerInch;

  static {
    try {
      fDotsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
    } catch (HeadlessException ex) {
      fDotsPerInch = 90;  // Safe value
    }
  }

  public AGestureRecognizer(long aMaxDuration) {
    fMaxDuration = aMaxDuration;
    fEvents = new Vector<TLcdTouchEvent>();
    fStatus = Status.PROCESSING;
    fGesture = null;
    fGestureTimer = null;
    fListeners = new Vector<IGestureRecognizerListener>();
    fMinSpeed = 1;
  }

  public void handleEvent(TLcdTouchEvent aTouchEvent) {
    if (fStatus != Status.PROCESSING) {
      // We will wait until somebody calls startGestureRecognition.
      return;
    }

    if (fEvents.size() == 0) {

      List<TLcdTouchPoint> descriptors = aTouchEvent.getTouchPoints();
      if (descriptors.size() == 1 &&
          descriptors.get(0).getTapCount() == 1 &&
          descriptors.get(0).getState() ==
          TLcdTouchPoint.State.DOWN) {

        // First event in a new chain - initialize the event ID and start timing the gesture.
        fTouchEventID = aTouchEvent.getID();
        fGestureTimer = new Timer(true);
        fGestureTimer.schedule(new GestureTimeoutTask(), fMaxDuration);
      } else {
        // This event does not start a new event chain. Ignore it.
        return;
      }
    } else if (fTouchEventID != aTouchEvent.getID()) {
      // Not the event chain that we are currently tracking. Ignore it.
      return;
    }

    fEvents.add(aTouchEvent);

    if (!fastMotion()) {
      stopGestureRecognition();
      notifyListeners();
      return;
    }
    processRecordedEvents(aTouchEvent);
  }

  public Status getStatus() {
    return fStatus;
  }

  public IGesture getGesture() {
    return fGesture;
  }

  public void startGestureRecognition() {
    fEvents.clear();
    fStatus = Status.PROCESSING;
    if (fGestureTimer != null) {
      fGestureTimer.cancel();
    }
  }

  public void stopGestureRecognition() {
    fEvents.clear();
    fStatus = Status.FAILED;
    if (fGestureTimer != null) {
      fGestureTimer.cancel();
      fGestureTimer = null;
    }
  }

  private boolean fastMotion() {
    if (fEvents.size() > 1) {

      // Find the average speed over the last 50 msec
      long time_span = 0;
      double pixel_distance = 0;
      for (int i = fEvents.size() - 1; i >= 1; i--) {
        TLcdTouchEvent ev1 = fEvents.get(i - 1);
        TLcdTouchEvent ev2 = fEvents.get(i);
        time_span += ev2.getTimeStamp() - ev1.getTimeStamp();
        double dist = pixelDistance(ev1, ev2);
        if (dist >= 0) {
          pixel_distance += dist;
        }
        if (time_span > 50) {
          double cm_distance = pixel_distance * TLcdConstant.I2CM / fDotsPerInch;
          final double speed = 1000 * cm_distance / time_span;
          return speed >= fMinSpeed;
        }
      }
    }
    return true;
  }

  /**
   * Computes the distance between two consecutive touch events. If the events are not comparable
   * (eg. the second event if not a POINT_MOVED event), the distance is negative.
   */
  private double pixelDistance(TLcdTouchEvent aEvent1, TLcdTouchEvent aEvent2) {
    if (aEvent1.getID() != aEvent2.getID()) {
      // These are two separate chains of events. Normally the older is a touch-up and the newer is
      // a touch-down. We simply return the distance between them.
      TLcdTouchPoint descriptor1 = aEvent1.getTouchPoints().get(0);
      TLcdTouchPoint descriptor2 = aEvent1.getTouchPoints().get(0);
      return Math.hypot(descriptor1.getLocation().x - descriptor2.getLocation().x,
                        descriptor1.getLocation().y - descriptor2.getLocation().y);
    }

    for (TLcdTouchPoint descriptor2 : aEvent2.getTouchPoints()) {
      // At least one descriptor must be non-stationary, let's find it.
      if (descriptor2.getState() ==
          TLcdTouchPoint.State.STATIONARY) {
        continue;
      }

      // Find out where this touch point was during the previous event. 
      for (TLcdTouchPoint descriptor1 : aEvent1.getTouchPoints()) {
        if (descriptor1.getID() == descriptor2.getID()) {
          return Math.hypot(descriptor1.getLocation().x - descriptor2.getLocation().x,
                            descriptor1.getLocation().y - descriptor2.getLocation().y);
        }
      }
    }
    return -1;
  }

  /**
   * This method should process the events recorded so far and update the recognizer state if
   * needed.
   * @param aLastTouchEvent the last touch event, provided for convenience (it is already present in
   *        the fEvents list).  
   */
  abstract void processRecordedEvents(TLcdTouchEvent aLastTouchEvent);

  public void addStatusListener(IGestureRecognizerListener aListener) {
    fListeners.add(aListener);
  }

  public void removeStatusListener(IGestureRecognizerListener aListener) {
    fListeners.remove(aListener);
  }

  protected void notifyListeners() {
    for (IGestureRecognizerListener listener : fListeners) {
      listener.gestureRecognizerStatusChanged(AGestureRecognizer.this);
    }
  }

  private class GestureTimeoutTask extends TimerTask {
    public void run() {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          stopGestureRecognition();
          notifyListeners();
        }
      });
    }
  }
}
