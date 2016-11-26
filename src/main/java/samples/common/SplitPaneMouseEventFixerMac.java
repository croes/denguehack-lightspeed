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
package samples.common;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * This class provides a workaround for bug
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8058703">JDK-8058703</a>.
 */
final class SplitPaneMouseEventFixerMac {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(MacUtil.class);

  private static final Object LOCK = new Object();
  private static AWTEventListener sListener;

  private SplitPaneMouseEventFixerMac() {
  }

  /**
   * <p>
   *   On OS X, {@code JSplitPane} instances often do not allow to change the location of the divider.
   *   An attempt to move the divider results in a black line on the desired position of the divider,
   *   while the actual divider remains on the original location.
   * </p>
   *
   * <p>
   *   This is caused by a {@link MouseEvent#MOUSE_RELEASED} which is not sent to the divider
   *   at the end of the drag of the divider.
   *   Instead, the {@code MOUSE_RELEASED} event is sent to another component.
   * </p>
   *
   * <p>
   *   This is logged in the JDK bug database as issue
   *   <a href="https://bugs.openjdk.java.net/browse/JDK-8058703">JDK-8058703</a>.
   * </p>
   *
   * <p>
   *   Calling this method will only have an effect when running on Mac.
   *   On other operating systems, it will do nothing.
   * </p>
   */
  static void install() {
    synchronized (LOCK) {
      if (sListener == null && TLcdSystemPropertiesUtil.isMacOS()) {
        installListener();
      }
    }
  }

  private static void installListener() {
    LOGGER.info("Installing OS X specific mouse event correction listener to ensure that split panes can be resized.");
    sListener = new EventCorrectionListener();
    Toolkit.getDefaultToolkit().addAWTEventListener(sListener, AWTEvent.MOUSE_EVENT_MASK);
  }

  private static class EventCorrectionListener implements AWTEventListener {
    private WeakReference<Component> fLastMousePressedComponent;

    @Override
    public void eventDispatched(AWTEvent event) {
      MouseEvent mouseEvent = (MouseEvent) event;
      if (mouseEvent.isConsumed()) {
        return;
      }
      if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
        fLastMousePressedComponent = new WeakReference<>(mouseEvent.getComponent());
      } else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
        Component component = mouseEvent.getComponent();
        Component lastMousePressedComponent = fLastMousePressedComponent.get();
        if (lastMousePressedComponent == null ||
            component == null ||
            lastMousePressedComponent == component) {
          return;
        }
        if (lastMousePressedComponent instanceof BasicSplitPaneDivider ||
            "com.jidesoft.docking.ContainerContainerDivider".equals(lastMousePressedComponent.getClass().getName())) {
          retarget(mouseEvent, lastMousePressedComponent);
          mouseEvent.consume();
        }
      }
    }

    private void retarget(MouseEvent aMouseEvent, Component aTargetComponent) {
      Point convertedPoint = SwingUtilities.convertPoint(aMouseEvent.getComponent(),
                                                         aMouseEvent.getPoint(),
                                                         aTargetComponent);

      MouseEvent retargetedEvent = new MouseEvent(aTargetComponent,
                                                  aMouseEvent.getID(),
                                                  aMouseEvent.getWhen(),
                                                  aMouseEvent.getModifiers(),
                                                  convertedPoint.x,
                                                  convertedPoint.y,
                                                  aMouseEvent.getXOnScreen(),
                                                  aMouseEvent.getYOnScreen(),
                                                  aMouseEvent.getClickCount(),
                                                  aMouseEvent.isPopupTrigger(),
                                                  aMouseEvent.getButton());
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Consumed mouse event [" + aMouseEvent + "] and injected [" + retargetedEvent + "] instead");
      }
      aTargetComponent.dispatchEvent(
          retargetedEvent
      );
    }
  }
}
