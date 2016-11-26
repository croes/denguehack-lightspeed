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
package samples.gxy.touch.multiUser;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

/**
 * This controller divides incoming touch events and gives them a specific user id, based on the
 * location of the touch down. Touch downs on the left side (and all corresponding move and up events)
 * are sent to user1 and touch downs on the right side to user2. This illustrates that it is possible
 * for multiple users to interact with the same view.
 */
public class SplitTouchEventsMultiUserController extends MultiUserController {

  public static final String USER_1 = "user1";
  public static final String USER_2 = "user2";

  private long fCurrentTouchEventID1;
  private long fCurrentTouchEventID2;

  private Set<Long> fTouchPointSet1 = new HashSet<Long>();
  private Set<Long> fTouchPointSet2 = new HashSet<Long>();

  public void handleAWTEvent(AWTEvent awtEvent) {
    if (awtEvent instanceof TLcdTouchEvent) {
      TLcdTouchEvent touch_event = (TLcdTouchEvent) awtEvent;
      long modified_touch_point_id = touch_event.getModifiedTouchPoint().getID();

      String user;
      if (touch_event.getModifiedTouchPoint().getState() == TLcdTouchPoint.State.DOWN) {
        user = USER_1;
        if (touch_event.getModifiedTouchPoint().getLocation().getX() > getGXYView().getWidth() / 2) {
          user = USER_2;
        }

        if (getTouchPointSetForUser(user).isEmpty()) {
          setTouchEventIDForUser(user, TLcdTouchEvent.createTouchEventID());
        }

        getTouchPointSetForUser(user).add(modified_touch_point_id);
      } else {
        user = getUserForTouchPoint(modified_touch_point_id);
      }

      long touch_event_id = getTouchEventIDForUser(user);

      List<TLcdTouchPoint> touch_points = new ArrayList<TLcdTouchPoint>();
      for (TLcdTouchPoint touch_point : touch_event.getTouchPoints()) {
        Set<Long> touch_point_set = getTouchPointSetForUser(user);
        if (touch_point_set.contains(touch_point.getID())) {
          touch_points.add(touch_point);
        }
      }

      if (touch_event.getModifiedTouchPoint().getState() == TLcdTouchPoint.State.UP) {
        getTouchPointSetForUser(user).remove(modified_touch_point_id);
      }

      TLcdTouchEvent new_event = touch_event.cloneAs(
          touch_event_id,
          touch_event.getSource(),
          touch_points,
          touch_event.getTouchDeviceID(),
          user,
          touch_event.getTimeStamp()
                                                    );

      super.handleAWTEvent(new_event);
    }
  }

  private Set<Long> getTouchPointSetForUser(String aUserID) {
    if (aUserID.equals(USER_1)) {
      return fTouchPointSet1;
    } else if (aUserID.equals(USER_2)) {
      return fTouchPointSet2;
    } else {
      throw new RuntimeException("Unknown user ID : " + aUserID);
    }
  }

  private String getUserForTouchPoint(long aTouchPointID) {
    if (fTouchPointSet1.contains(aTouchPointID)) {
      return USER_1;
    }
    if (fTouchPointSet2.contains(aTouchPointID)) {
      return USER_2;
    }
    return TLcdTouchEvent.UNKNOWN_TOUCH_DEVICE_USER;
  }

  private long getTouchEventIDForUser(String aUserID) {
    if (aUserID.equals(USER_1)) {
      return fCurrentTouchEventID1;
    } else if (aUserID.equals(USER_2)) {
      return fCurrentTouchEventID2;
    } else {
      throw new RuntimeException("Unknown user ID : " + aUserID);
    }
  }

  private void setTouchEventIDForUser(String aUserID, long aTouchEventID) {
    if (aUserID.equals(USER_1)) {
      fCurrentTouchEventID1 = aTouchEventID;
    } else if (aUserID.equals(USER_2)) {
      fCurrentTouchEventID2 = aTouchEventID;
    } else {
      throw new RuntimeException("Unknown user ID : " + aUserID);
    }
  }

}
