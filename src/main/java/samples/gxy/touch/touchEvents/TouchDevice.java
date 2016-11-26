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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Timer;

/**
 * This class simulates touch hardware by creating DeviceEvents. These DeviceEvents can be
 * intercepted by adding a DeviceEventListener to this TouchDevice. These events are only
 * valid for the component registered on the TouchDevice.
 */
public class TouchDevice {

  // The ID of this touch device.
  private long fDeviceID;

  // These listeners will receive touch events.
  private List<DeviceEventListener> fListeners = new ArrayList<DeviceEventListener>();

  // Holds a registered component, which will receive events.
  private Component fComponent;

  // Used to implement getUniqueTouchPointID().
  private long fNextTouchPointID = 1;

  /**
   * Create a new touch device with the given device ID.
   *
   * @param aDeviceID a device ID.
   */
  public TouchDevice(long aDeviceID) {
    fDeviceID = aDeviceID;
  }

  /**
   * Add a DeviceEventListener to this TouchDevice. Each DeviceEventListener will be notified
   * when a new TouchEvent is created.
   *
   * @param aListener a DeviceEventListener.
   */
  public void addDeviceEventListener(DeviceEventListener aListener) {
    fListeners.add(aListener);
  }

  /**
   * Remove the DeviceEventListener from this TouchDevice.
   *
   * @param aListener a DeviceEventListener.
   */
  public void removeDeviceEventListener(DeviceEventListener aListener) {
    fListeners.remove(aListener);
  }

  /**
   * Register the given component no this device. The events generated are only
   * valid for this component.
   *
   * @param aRegisteredComponent a component.
   */
  public void setRegisteredComponent(Component aRegisteredComponent) {
    fComponent = aRegisteredComponent;
  }

  /**
   * Returns the ID of this device.
   *
   * @return the ID of this device.
   */
  public long getDeviceID() {
    return fDeviceID;
  }

  /**
   * Start the device. After this call, the device will start generating DeviceEvents.
   */
  public void start() {
    // Create a list of device events. These events contain a time stamp relative to the moment this
    // method is called. Later these events will be converted to events with a correct time stamp.
    List<DeviceEvent> device_events = new ArrayList<>();

    // Pan the view.
    device_events.addAll(generateTouchStroke(new Point(500, 300), new Point(250, 290), 1000, 1500, 10));

    // Zoom in.
    device_events.addAll(generateTouchStroke(new Point(350, 375), new Point(150, 375), 2000, 2500, 10));
    device_events.addAll(generateTouchStroke(new Point(450, 375), new Point(650, 375), 2000, 2500, 10));

    // Rotate the view.
    device_events.addAll(generateTouchStroke(new Point(200, 375), new Point(400, 175), 3000, 3500, 10));
    device_events.addAll(generateTouchStroke(new Point(600, 375), new Point(400, 575), 3000, 3500, 10));

    // Do a mixture.
    device_events.addAll(generateTouchStroke(new Point(400, 475), new Point(700, 475), 4000, 4500, 10));
    device_events.addAll(generateTouchStroke(new Point(450, 475), new Point(150, 475), 4160, 5000, 10));
    device_events.addAll(generateTouchStroke(new Point(200, 575), new Point(600, 575), 4750, 5500, 10));

    // Sorting the events makes it possible to use two touch points at the same time.
    sortDeviceEvents(device_events);

    // Start the timer that passes these events to the listeners at the right time.
    if (device_events.size() > 0) {
      int initial_delay = (int) device_events.get(0).getTimeStamp();
      Timer timer = new Timer(initial_delay, new MyActionListener(device_events));
      timer.setRepeats(false);
      timer.start();
    }
  }

  private List<DeviceEvent> generateTouchStroke(Point aFrom, Point aTo, int aStartTime, int aEndTime, int aMoveInterval) {
    List<DeviceEvent> events = new ArrayList<DeviceEvent>();

    if (aStartTime == aEndTime) {
      return events;
    }

    long id = getUniqueTouchPointID();

    events.add(new DeviceEvent(fComponent, DeviceEvent.EventType.TOUCH_DOWN, id, fDeviceID, aFrom.x, aFrom.y, aStartTime));

    for (int time = aStartTime + aMoveInterval; time < aEndTime; time += aMoveInterval) {
      double progress = (double) (time - aStartTime) / (double) (aEndTime - aStartTime);
      double x = aFrom.getX() + progress * (aTo.getX() - aFrom.getX());
      double y = aFrom.getY() + progress * (aTo.getY() - aFrom.getY());
      events.add(new DeviceEvent(fComponent, DeviceEvent.EventType.TOUCH_MOVED, id, fDeviceID, x, y, time));
    }

    events.add(new DeviceEvent(fComponent, DeviceEvent.EventType.TOUCH_UP, id, fDeviceID, aTo.x, aTo.y, aEndTime));

    return events;
  }

  private long getUniqueTouchPointID() {
    long id = fNextTouchPointID;
    fNextTouchPointID++;
    return id;
  }

  private void sortDeviceEvents(List<DeviceEvent> aEvents) {
    Collections.sort(aEvents, new Comparator<DeviceEvent>() {
      public int compare(DeviceEvent aEvent1, DeviceEvent aEvent2) {
        int diff = (int) (aEvent1.getTimeStamp() - aEvent2.getTimeStamp());
        if (diff != 0) {
          return diff;
        }

        if (aEvent1.getEventType() == DeviceEvent.EventType.TOUCH_UP) {
          return 1;
        }

        return -1;
      }
    });
  }

  private class MyActionListener implements ActionListener {

    private List<DeviceEvent> fDeviceEvents;
    private int fCurrentEventIndex = 0;

    public MyActionListener(List<DeviceEvent> aDeviceEvents) {
      fDeviceEvents = aDeviceEvents;
    }

    public void actionPerformed(ActionEvent e) {
      DeviceEvent current_event = fDeviceEvents.get(fCurrentEventIndex);
      DeviceEvent next_event = null;
      if (fCurrentEventIndex + 1 < fDeviceEvents.size()) {
        next_event = fDeviceEvents.get(fCurrentEventIndex + 1);
      }
      fCurrentEventIndex++;

      if (next_event != null) {
        int next_delay = (int) next_event.getTimeStamp() - (int) current_event.getTimeStamp();
        Timer timer = new Timer(next_delay, this);
        timer.setRepeats(false);
        timer.start();
      }

      for (DeviceEventListener listener : fListeners) {
        listener.deviceEvent(new DeviceEvent(current_event.getComponent(),
                                             current_event.getEventType(),
                                             current_event.getTouchPointID(),
                                             current_event.getTouchDeviceID(),
                                             current_event.getX(),
                                             current_event.getY(),
                                             System.currentTimeMillis()));
      }
    }
  }
}
