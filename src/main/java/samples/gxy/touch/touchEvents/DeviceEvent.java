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

/**
 * This class describes device touch events. It contains the component which should receive the
 * events, and the x and y-coordinates of the touch point, relative to the component. I.e. when x
 * and y are 0, they describe the upper left corner of the component.
 *
 * It also contains an event type : DOWN, MOVED or UP, and a time stamp describing when the event
 * happened. Also a touch point ID is given. This allows multi-touch behaviour. A touch point ID
 * is assigned during a DOWN event, and stays constant for a touch point until its UP event.  
 */
public class DeviceEvent {

  public enum EventType {
    TOUCH_DOWN,
    TOUCH_MOVED,
    TOUCH_UP
  }

  private Component fComponent;
  private EventType fEventType;
  private long fTouchPointID;
  private long fTouchDeviceID;
  private double fX;
  private double fY;
  private long fTimeStamp;

  /**
   * Create a new DeviceEvent with the given parameters.
   *
   * @param aComponent     the component which has received the event.
   * @param aEventType     the event type, UP, MOVED or DOWN.
   * @param aTouchPointID  the ID of the touch point.
   * @param aTouchDeviceID the ID of the touch device.
   * @param aX             the x-position of the event, relative to the given component.
   * @param aY             the y-position of the event, relative to the given component.
   * @param aTimeStamp     a time stamp.
   */
  public DeviceEvent(Component aComponent, EventType aEventType, long aTouchPointID, long aTouchDeviceID, double aX, double aY, long aTimeStamp) {
    fComponent = aComponent;
    fEventType = aEventType;
    fTouchPointID = aTouchPointID;
    fTouchDeviceID = aTouchDeviceID;
    fX = aX;
    fY = aY;
    fTimeStamp = aTimeStamp;
  }

  public Component getComponent() {
    return fComponent;
  }

  public EventType getEventType() {
    return fEventType;
  }

  public long getTouchPointID() {
    return fTouchPointID;
  }

  public long getTouchDeviceID() {
    return fTouchDeviceID;
  }

  public double getX() {
    return fX;
  }

  public double getY() {
    return fY;
  }

  public long getTimeStamp() {
    return fTimeStamp;
  }

}
