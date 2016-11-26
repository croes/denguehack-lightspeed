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
package samples.lucy.frontend.mapcentric.previewer;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Shows a given component when the mouse is hovering over it. It hides it when the mouse pointer
 * has left it.
 *
 * At start-up, the component is visible even if the mouse pointer is not hovering over it. This has
 * two reasons:
 * - It informs users about the additional UI.
 * - It is compatible with touch devices: the component is always visible as the 'mouse' never
 *   leaves it.
 *
 * In fact, the roll over component and the component to hide are two separate components. This
 * offers more flexibility: you can hide only a part of a component.
 */
class MouseRollOverTracker {

  public static void install(final JComponent aRollOverComponent, final HideableChildrenPanel aComponentToHide) {
    // Global hook on all mouse events. Simply listening to events in aRollOverComponent doesn't
    // work as the events on child components would not come through.
    Toolkit.getDefaultToolkit().addAWTEventListener(
        new WeakAWTEventListener(aRollOverComponent, aComponentToHide), AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }

  /**
   * AWTEventListener that tracks the mouse pointer, and shows/hides the panel when needed.
   * It uses weak references to avoid memory leaks resulting from the globally registered AWT
   * event listener.
   */
  private static class WeakAWTEventListener implements AWTEventListener {
    private final WeakReference<JComponent> fRollOverComponent;
    private final WeakReference<HideableChildrenPanel> fComponentToHide;
    private final Timer fHideTimer;
    private boolean fShouldShow = true;

    public WeakAWTEventListener(JComponent aRollOverComponent, HideableChildrenPanel aComponentToHide) {
      fRollOverComponent = new WeakReference<JComponent>(aRollOverComponent);
      fComponentToHide = new WeakReference<HideableChildrenPanel>(aComponentToHide);

      // Hide the component using a delay.
      fHideTimer = new Timer(3000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          HideableChildrenPanel hideable = fComponentToHide.get();
          if (hideable != null) {
            hideable.setPaintChildren(false);
          }
        }
      });
      fHideTimer.setRepeats(false);
    }

    @Override
    public void eventDispatched(AWTEvent event) {
      JComponent rollOver = fRollOverComponent.get();
      HideableChildrenPanel toHide = fComponentToHide.get();

      // Clean-up after ourselves if GC decided to clean up the panels
      if (rollOver == null || toHide == null) {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        return;
      }

      boolean shouldShow = shouldShow(rollOver, (MouseEvent) event);
      if (shouldShow != fShouldShow) {
        fShouldShow = shouldShow;
        if (shouldShow) {
          fHideTimer.stop();
          toHide.setPaintChildren(true);
        } else {
          fHideTimer.start();
        }
      }
    }
  }

  private static boolean shouldShow(JComponent aRollOver, MouseEvent e) {
    if (e.getComponent() != null) {
      // Show the panel if the mouse hovers over (one of the children of) the roll over component
      Component hovered = SwingUtilities.getDeepestComponentAt(e.getComponent(), e.getX(), e.getY());
      return hovered != null && SwingUtilities.isDescendingFrom(hovered, aRollOver);
    } else {
      return false;
    }
  }

  /**
   * Panel that can hide its children. The difference with a simple setVisible call is that
   * setVisible also impacts the layout.
   */
  public static class HideableChildrenPanel extends JPanel {
    private boolean fPaintChildren = true;

    public boolean isPaintChildren() {
      return fPaintChildren;
    }

    public void setPaintChildren(boolean aPaintChildren) {
      fPaintChildren = aPaintChildren;
      repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
      if (fPaintChildren) {
        super.paintChildren(g);
      }
    }
  }
}
