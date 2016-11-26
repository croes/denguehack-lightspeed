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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.luciad.input.ILcdAWTEventListener;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

public class MouseToTouchEventAdapter implements MouseListener, MouseMotionListener, MouseWheelListener {
  private ILcdAWTEventListener fTarget;

  private static final long DEVICE_ID = 4959874578L;
  private static final String USER_ID = "MouseToTouchEventAdapter";

  private static long id = TLcdTouchEvent.createTouchEventID();

  public MouseToTouchEventAdapter(ILcdAWTEventListener aTarget) {
    fTarget = aTarget;
  }

  public void mousePressed(MouseEvent e) {
    List<TLcdTouchPoint> descriptors = new ArrayList<TLcdTouchPoint>();
    int button = getButton(e);
    if (button == MouseEvent.BUTTON1) {
      descriptors.add(new TLcdTouchPoint(TLcdTouchPoint.State.DOWN,
                                         button, e.getPoint(),
                                         e.getClickCount(),
                                         "mouse",
                                         false, 0, 0));
      fTarget.handleAWTEvent(new TLcdTouchEvent(id, e.getComponent(), descriptors, DEVICE_ID, USER_ID, System.currentTimeMillis()));
    }
  }

  public void mouseReleased(MouseEvent e) {
    List<TLcdTouchPoint> descriptors = new ArrayList<TLcdTouchPoint>();
    int button = getButton(e);
    if (button == MouseEvent.BUTTON1) {
      descriptors.add(new TLcdTouchPoint(TLcdTouchPoint.State.UP, button, e.getPoint(), e.getClickCount(), "mouse", false, 0, 0));
      fTarget.handleAWTEvent(new TLcdTouchEvent(id, e.getComponent(), descriptors, DEVICE_ID, USER_ID, System.currentTimeMillis()));
      id = TLcdTouchEvent.createTouchEventID();
    }
  }

  public void mouseDragged(MouseEvent e) {
    int button = getButton(e);
    if (button == MouseEvent.BUTTON1) {
      List<TLcdTouchPoint> descriptors = new ArrayList<TLcdTouchPoint>();
      descriptors.add(new TLcdTouchPoint(TLcdTouchPoint.State.MOVED, button, e.getPoint(), e.getClickCount(), "mouse", false, 0, 0));
      fTarget.handleAWTEvent(new TLcdTouchEvent(id, e.getComponent(), descriptors, DEVICE_ID, USER_ID, System.currentTimeMillis()));
    }
  }

  private int getButton(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      return MouseEvent.BUTTON1;
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      return MouseEvent.BUTTON2;
    } else if (SwingUtilities.isRightMouseButton(e)) {
      return MouseEvent.BUTTON3;
    } else {
      return MouseEvent.NOBUTTON;
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
  }
}

