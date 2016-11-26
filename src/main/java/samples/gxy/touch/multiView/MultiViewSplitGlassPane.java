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
package samples.gxy.touch.multiView;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.input.ILcdAWTEventListener;
import com.luciad.input.ILcdAWTEventSource;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;

/**
 * This panel extension acts as a glass pane. It intercepts all awt events,
 * and re-dispatches the mouse and touch events to the underlying components.
 * This way it is possible to work in 2 or more different views at the same time.
 */
public class MultiViewSplitGlassPane extends JPanel {

  private JPanel fContentPane;

  /**
   * Creates a new glass pane with the given underlying components.
   * @param aComponents the underlying components to add.
   */
  public MultiViewSplitGlassPane(Component... aComponents) {
    setLayout(new MyOverlayLayout());
    setOpaque(false);

    // Create a content pane containing the given component
    fContentPane = new JPanel();
    fContentPane.setLayout(new BoxLayout(fContentPane, BoxLayout.X_AXIS));
    for (Component component : aComponents) {
      fContentPane.add(component);
    }

    // Add a glass pane over the content pane
    GlassPane glass_pane = new GlassPane();
    add(glass_pane);

    add(fContentPane);
  }

  private class GlassPane extends JComponent implements ILcdAWTEventSource {

    private Component fCurrentMouseComponent;

    private Map<Long, Component> fCurrentTouchComponentMap = new HashMap<Long, Component>();
    private Map<Component, Long> fComponentIdMap = new HashMap<Component, Long>();

    private GlassPane() {
      // Add dummy mouse listener. This is done to make sure this component gets the
      // mouse events instead of the components in the content pane.
      addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
      });
      addMouseMotionListener(new MouseMotionListener() {
        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      // Do not paint anything
    }

    public void addAWTEventListener(ILcdAWTEventListener aAWTListener) {
      // No need to implement
    }

    public void removeAWTEventListener(ILcdAWTEventListener aAWTListener) {
      // No need to implement
    }

    @Override
    protected void processEvent(AWTEvent e) {
      if (e instanceof MouseEvent) {
        handleMouseEventImpl((MouseEvent) e);
      } else if (e instanceof TLcdTouchEvent) {
        handleTouchEventImpl((TLcdTouchEvent) e);
      }
    }

    private void handleMouseEventImpl(MouseEvent e) {
      if (fCurrentMouseComponent != null) {
        MouseEvent new_mouse_event = SwingUtilities.convertMouseEvent(this, e, fCurrentMouseComponent);
        fCurrentMouseComponent.dispatchEvent(new_mouse_event);
        if (new_mouse_event.getID() == MouseEvent.MOUSE_RELEASED) {
          fCurrentMouseComponent = null;
        }
      } else {
        Component deepest_component = SwingUtilities.getDeepestComponentAt(fContentPane, e.getX(), e.getY());
        if (deepest_component != null) {
          MouseEvent new_mouse_event = SwingUtilities.convertMouseEvent(this, e, deepest_component);
          deepest_component.dispatchEvent(new_mouse_event);
          if (new_mouse_event.getID() == MouseEvent.MOUSE_PRESSED) {
            fCurrentMouseComponent = deepest_component;
          }
        }
      }
    }

    private void handleTouchEventImpl(TLcdTouchEvent e) {
      // A touch point ID is assigned to a component starting with a down event. For as long
      // as no up event is passed, it stays assigned to that same component.
      long touch_id = e.getModifiedTouchPoint().getID();
      Component current_component = fCurrentTouchComponentMap.get(touch_id);
      if (current_component != null) {
        TLcdTouchEvent new_touch_event = convertTouchEvent(e, current_component);
        new_touch_event = createTouchPointForComponent(new_touch_event, current_component);
        current_component.dispatchEvent(new_touch_event);
        if (new_touch_event.getModifiedTouchPoint().getState() == TLcdTouchPoint.State.UP) {
          fCurrentTouchComponentMap.remove(touch_id);
        }
      } else {
        Point location = e.getModifiedTouchPoint().getLocation();
        current_component = SwingUtilities.getDeepestComponentAt(fContentPane, location.x, location.y);
        if (current_component != null) {
          TLcdTouchEvent new_touch_event = convertTouchEvent(e, current_component);
          new_touch_event = createTouchPointForComponent(new_touch_event, current_component);
          if (new_touch_event.getModifiedTouchPoint().getState() == TLcdTouchPoint.State.DOWN) {
            fCurrentTouchComponentMap.put(touch_id, current_component);
          }
          current_component.dispatchEvent(new_touch_event);
        }
      }
    }

    private TLcdTouchEvent createTouchPointForComponent(TLcdTouchEvent aTouchEvent, Component aComponent) {
      // Find all touch points which are assigned to the given component
      List<TLcdTouchPoint> touch_points = new ArrayList<TLcdTouchPoint>();
      touch_points.add(aTouchEvent.getModifiedTouchPoint());
      for (TLcdTouchPoint touch_point : aTouchEvent.getStationaryTouchPoints()) {
        Component component = fCurrentTouchComponentMap.get(touch_point.getID());
        if (component == aComponent) {
          touch_points.add(touch_point);
        }
      }

      // Create a new touch point with the correct touch points, and a correct ID.
      return aTouchEvent.cloneAs(
          getTouchEventId(aComponent, aTouchEvent.getModifiedTouchPoint().getID()),
          aTouchEvent.getSource(),
          touch_points,
          aTouchEvent.getTouchDeviceID(),
          aTouchEvent.getUserID(),
          aTouchEvent.getTimeStamp()
                                );
    }

    private long getTouchEventId(Component aComponent, long aTouchPointID) {
      if (fCurrentTouchComponentMap.values().contains(aComponent)) {
        return fComponentIdMap.get(aComponent);
      } else {
        fComponentIdMap.put(aComponent, aTouchPointID);
        return aTouchPointID;
      }
    }

    private TLcdTouchEvent convertTouchEvent(TLcdTouchEvent aTouchEvent, Component aDestination) {
      // Converts the touch event from the source component to the destination component.
      List<TLcdTouchPoint> touch_points = new ArrayList<TLcdTouchPoint>();
      for (TLcdTouchPoint touch_point : aTouchEvent.getTouchPoints()) {
        Point location = SwingUtilities.convertPoint(aTouchEvent.getSource(), touch_point.getLocation(), aDestination);
        touch_points.add(touch_point.cloneAs(touch_point.getState(),
                                             touch_point.getID(),
                                             location,
                                             touch_point.getTapCount(),
                                             touch_point.getType(), false,
                                             touch_point.getContactAreaWidth(),
                                             touch_point.getContactAreaHeight()));
      }
      return aTouchEvent.cloneAs(aTouchEvent.getTouchEventID(),
                                 aDestination,
                                 touch_points,
                                 aTouchEvent.getTouchDeviceID(),
                                 aTouchEvent.getUserID(),
                                 aTouchEvent.getTimeStamp());

    }
  }

  /**
   * Similar to javax.swing.OverlayLayout, but it makes sure to ignore the alignments of the
   * child components. So it just provides every child with all available space.
   */
  private static class MyOverlayLayout implements LayoutManager2 {

    public void addLayoutComponent(Component comp, Object constraints) {
    }

    public Dimension maximumLayoutSize(Container target) {
      Dimension max = new Dimension(0, 0);
      for (Component c : target.getComponents()) {
        Dimension component_max = c.getMaximumSize();
        if (max.width < component_max.width) {
          max.width = component_max.width;
        }
        if (max.height < component_max.height) {
          max.height = component_max.height;
        }
      }
      max.width += target.getInsets().left + target.getInsets().right;
      max.height += target.getInsets().top + target.getInsets().bottom;
      return max;
    }

    public float getLayoutAlignmentX(Container target) {
      return 0.5f;
    }

    public float getLayoutAlignmentY(Container target) {
      return 0.5f;
    }

    public void invalidateLayout(Container target) {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
      Dimension preferred_size = new Dimension(0, 0);
      for (Component c : parent.getComponents()) {
        Dimension component_preferred_size = c.getPreferredSize();
        if (preferred_size.width < component_preferred_size.width) {
          preferred_size.width = component_preferred_size.width;
        }
        if (preferred_size.height < component_preferred_size.height) {
          preferred_size.height = component_preferred_size.height;
        }
      }
      preferred_size.width += parent.getInsets().left + parent.getInsets().right;
      preferred_size.height += parent.getInsets().top + parent.getInsets().bottom;
      return preferred_size;
    }

    public Dimension minimumLayoutSize(Container parent) {
      Dimension min = new Dimension(0, 0);
      for (Component c : parent.getComponents()) {
        Dimension component_min = c.getMinimumSize();
        if (min.width < component_min.width) {
          min.width = component_min.width;
        }
        if (min.height < component_min.height) {
          min.height = component_min.height;
        }
      }
      min.width += parent.getInsets().left + parent.getInsets().right;
      min.height += parent.getInsets().top + parent.getInsets().bottom;
      return min;
    }

    public void layoutContainer(Container parent) {
      int x = parent.getInsets().left;
      int y = parent.getInsets().top;
      int width = parent.getWidth() - parent.getInsets().left - parent.getInsets().right;
      int height = parent.getHeight() - parent.getInsets().top - parent.getInsets().bottom;
      for (Component c : parent.getComponents()) {
        c.setBounds(x, y, width, height);
      }
    }
  }
}
