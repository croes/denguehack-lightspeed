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
package samples.gxy.touch.touchEvents;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.luciad.input.TLcdAWTEventDispatcher;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

/**
 * This DeviceEventListener receives DeviceEvents and converts them to TLcdTouchEvents.
 * Finally it dispatches the created TLcdTouchEvent.
 */
public class TouchEventFactory implements DeviceEventListener {

  // The ID of this device.
  private long fTouchDeviceID;

  // Stores the currently known touch points. These are all part of the current event.
  private List<TLcdTouchPoint> fCurrentlyKnowTouchPoints = new ArrayList<TLcdTouchPoint>();

  // Stores the current event ID. This ID remains the same from the moment one touch point is
  // created, to the moment all touch points are lifted again.
  private long fCurrentEventID;

  // Stores the current component the which the current event is dispatched.
  private Component fCurrentComponent;

  /**
   * Create a new TouchEventFactory that converts DeviceEvents to TLcdTouchEvents. It only
   * accepts events from the device with the given ID.
   *
   * @param aTouchDeviceID a touch device ID.
   */
  public TouchEventFactory(long aTouchDeviceID) {
    fTouchDeviceID = aTouchDeviceID;
  }

  public void deviceEvent(DeviceEvent aEvent) {
    // Only accept events from the device with fTouchDeviceID as ID. Ignore all other events.
    if (aEvent.getTouchDeviceID() != fTouchDeviceID) {
      return;
    }

    if (aEvent.getEventType() == DeviceEvent.EventType.TOUCH_DOWN) {
      if (touchPointExists(aEvent.getTouchPointID())) {
        // There should never be two touch point descriptors with the same ID.
        throw new IllegalArgumentException("A touch descriptor with touch point ID [" + aEvent.getTouchPointID() + "] already exists.");
      }

      if (fCurrentlyKnowTouchPoints.size() == 0) {
        // Make sure a new touch ID is used.
        fCurrentEventID = TLcdTouchEvent.createTouchEventID();
      }

      if (fCurrentlyKnowTouchPoints.size() == 0) {
        // The event should be dispatched to the 'deepest' component.
        int x = (int) aEvent.getX();
        int y = (int) aEvent.getY();
        fCurrentComponent = SwingUtilities.getDeepestComponentAt(aEvent.getComponent(), x, y);
        if (fCurrentComponent == null) {
          System.out.println("Cannot find target location for event " + aEvent);
          // We don't know where to dispatch the component to. It may have been closed in the mean time, so abort.
          return;
        }
      }
    } else if (aEvent.getEventType() == DeviceEvent.EventType.TOUCH_MOVED ||
               aEvent.getEventType() == DeviceEvent.EventType.TOUCH_UP) {
      if (!touchPointExists(aEvent.getTouchPointID())) {
        // A touch point should exist before it can be lifted.
        throw new IllegalStateException("A touch descriptor with touch point ID [" + aEvent.getTouchPointID() + "] should already exist.");
      }
    }

    TLcdTouchPoint touch_point = createTouchPoint(aEvent);
    TLcdTouchEvent touch_event = createTouchEvent(touch_point, aEvent.getTimeStamp());
    TLcdAWTEventDispatcher.getInstance().handleAWTEvent(touch_event);
  }

  private TLcdTouchPoint createTouchPoint(DeviceEvent aEvent) {
    TLcdTouchPoint.State state;
    if (aEvent.getEventType() == DeviceEvent.EventType.TOUCH_DOWN) {
      state = TLcdTouchPoint.State.DOWN;
    } else if (aEvent.getEventType() == DeviceEvent.EventType.TOUCH_MOVED) {
      state = TLcdTouchPoint.State.MOVED;
    } else if (aEvent.getEventType() == DeviceEvent.EventType.TOUCH_UP) {
      state = TLcdTouchPoint.State.UP;
    } else {
      state = TLcdTouchPoint.State.STATIONARY;
    }

    // Convert the coordinates, which are relative to aEvent.getComponent() to coordinates
    // relative to fCurrentComponent;
    int x = (int) aEvent.getX();
    int y = (int) aEvent.getY();
    Point location_relative_to_current_component =
        SwingUtilities.convertPoint(aEvent.getComponent(), new Point(x, y), fCurrentComponent);

    return new TLcdTouchPoint(state,
                              aEvent.getTouchPointID(),
                              location_relative_to_current_component,
                              1,
                              TLcdTouchPoint.UNKNOWN_TOUCH_POINT_TYPE,
                              false);
  }

  private TLcdTouchEvent createTouchEvent(TLcdTouchPoint aTouchPoint,
                                          long aTimeStamp) {
    switch (aTouchPoint.getState()) {
    case STATIONARY: {
      throw new IllegalArgumentException("No events should be created for stationary touch points");
    }
    case DOWN: {
      addDescriptor(aTouchPoint);
      return createTouchEventImpl(aTouchPoint, aTimeStamp);
    }
    case MOVED: {
      replaceDescriptor(aTouchPoint);
      return createTouchEventImpl(aTouchPoint, aTimeStamp);
    }
    case UP: {
      removeDescriptor(aTouchPoint.getID());
      return createTouchEventImpl(aTouchPoint, aTimeStamp);
    }
    default: {
      // May never happen.
      throw new IllegalArgumentException("Unknown state");
    }
    }
  }

  private TLcdTouchEvent createTouchEventImpl(TLcdTouchPoint aTouchPoint,
                                              long aTimeStamp) {
    List<TLcdTouchPoint> touch_points = new ArrayList<TLcdTouchPoint>();

    // Add the given TLcdTouchPointDescriptor to the list. this descriptor is never stationary.
    touch_points.add(aTouchPoint);

    // Add the other currently know descriptors. These should always be stationary.
    for (TLcdTouchPoint touch_point : fCurrentlyKnowTouchPoints) {
      if (touch_point.getID() != aTouchPoint.getID()) {
        touch_points.add(new TLcdTouchPoint(TLcdTouchPoint.State.STATIONARY,
                                            touch_point.getID(),
                                            touch_point.getLocation(),
                                            touch_point.getTapCount(),
                                            touch_point.getType(),
                                            touch_point.isConsumed()));
      }
    }

    return new TLcdTouchEvent(fCurrentEventID,
                              fCurrentComponent,
                              touch_points,
                              fTouchDeviceID,
                              TLcdTouchEvent.UNKNOWN_TOUCH_DEVICE_USER,
                              aTimeStamp);
  }

  private void addDescriptor(TLcdTouchPoint aTouchPoint) {
    fCurrentlyKnowTouchPoints.add(aTouchPoint);
  }

  private void replaceDescriptor(TLcdTouchPoint aTouchPoint) {
    removeDescriptor(aTouchPoint.getID());
    addDescriptor(aTouchPoint);
  }

  private void removeDescriptor(long aTouchPointID) {
    TLcdTouchPoint touch_point_to_remove = null;
    for (TLcdTouchPoint touch_point : fCurrentlyKnowTouchPoints) {
      if (touch_point.getID() == aTouchPointID) {
        touch_point_to_remove = touch_point;
      }
    }

    if (touch_point_to_remove != null) {
      fCurrentlyKnowTouchPoints.remove(touch_point_to_remove);
    }
  }

  private boolean touchPointExists(long aTouchPointID) {
    for (TLcdTouchPoint touch_point : fCurrentlyKnowTouchPoints) {
      if (touch_point.getID() == aTouchPointID) {
        return true;
      }
    }
    return false;
  }
}
