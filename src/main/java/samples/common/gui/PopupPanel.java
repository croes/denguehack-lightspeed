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
package samples.common.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.luciad.gui.TLcdAWTUtil;
import samples.common.UIColors;
import com.luciad.util.ILcdPropertyChangeSource;

/**
 * Facility to show and hide a panel in a popup. The main difference with javax.swing.Popup is that:
 * - It always uses JWindow's, which is similar to heavy weight popups. So there is a single code path.
 * - It automatically hides the popup when it is no longer relevant. This is actually the hardest part.
 * - It supports popups in popups, or combo boxes in popups.
 *
 * A popup can be made visible or invisible. It fires a property change event if the visibility changes.
 * See {@link #setPopupVisible(boolean)}.
 */
public final class PopupPanel implements ILcdPropertyChangeSource {

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
  private final JComponent fOwner;
  private final JComponent fContent;
  private final PopupLocation fPopupLocation;
  private final boolean fFocusable;

  private boolean fPopupVisible = false;
  private JWindow fPopup;

  public static PopupPanel create(JComponent aOwner, JComponent aContent) {
    return create(aOwner, aContent, true);
  }

  public static PopupPanel create(JComponent aOwner, JComponent aContent, boolean aFocusable) {
    return create(aOwner, aContent, aFocusable, PopupLocation.DEFAULT);
  }

  /**
   * Creates a new popup.
   * @param aOwner The owner component. This is typically the button that triggers the popup to appear, or a text field.
   *               Do not provide a large component, such as for example the entire frame or so.
   * @param aContent The content to show in the popup.
   * @param aFocusable Use {@code true} if the popup needs to be able to get focus, for example when it contains a text
   *                   field or other interactive components. Use {@code false} if the popup should never get focus. An
   *                   example is a search box popup, where you want to focus to remain in the search text field, and
   *                   never go to the popup.
   * @param aLocation The location calculator for the popup.
   * @return The popup.
   */
  public static PopupPanel create(JComponent aOwner, JComponent aContent, boolean aFocusable, PopupLocation aLocation) {
    return new PopupPanel(aOwner, aContent, aFocusable, aLocation);
  }

  private PopupPanel(JComponent aOwner, JComponent aContent, boolean aFocusable, PopupLocation aPopupLocation) {
    if (aOwner == null || aContent == null) {
      throw new IllegalArgumentException("Owner or content must not be null");
    }
    fOwner = aOwner;
    fContent = aContent;
    fFocusable = aFocusable;
    fPopupLocation = aPopupLocation;

    // Keep the look and feel up to date
    LookAndFeelChangeListener.install(aContent);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aListener);
  }

  private JWindow createPopup() {
    Window ownerWindow = TLcdAWTUtil.findParentWindow(fOwner);
    if (ownerWindow == null) {
      throw new IllegalStateException("Could not find parent window of owner. Popups need an owner, which in turn "
                                      + "needs to be part of some frame or window.");
    }
    final JWindow popup = new JWindow(ownerWindow);
    popup.setType(Window.Type.POPUP);
    popup.setFocusableWindowState(fFocusable);
    popup.add(fContent);
    initBorder(popup);

    // Hide popup for various events
    HideWhenClickingElsewhereListener.install(this);
    HideOnFocusLostListener.install(this);
    HideWhenOwnerMovedOrResizedListener.intall(fOwner, this);

    // Hide on escape
    String actionKey = getClass().getName() + ":close";
    InputMap inputMap = fContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionKey);
    fContent.getActionMap().put(actionKey, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setPopupVisible(false);
      }
    });

    // Re-wire the window events to the popupVisible property change events
    popup.addHierarchyListener(new HierarchyListener() {
      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
          boolean oldVisible = fPopupVisible;
          fPopupVisible = popup.isShowing();

          if (oldVisible && !fPopupVisible) {
            // Unlink the content from the window. Not strictly needed, but it is more sensible behavior.
            popup.setContentPane(new JPanel());

            popup.dispose();
            fPopup = null;
          }
          fPropertyChangeSupport.firePropertyChange("popupVisible", oldVisible, fPopupVisible);
        }
      }
    });

    return popup;
  }

  private void initBorder(JWindow aPopup) {
    Border border;

    // Nimbus doesn't use a regular border like all other look and feels, so emulate it fetching the color from the
    // UIManager.
    if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
      Color borderColor = UIColors.getUIColor("nimbusBorder", UIColors.fg());
      border = BorderFactory.createLineBorder(borderColor, 1);
    } else {
      border = new JPopupMenu().getBorder();
    }
    ((JComponent) aPopup.getContentPane()).setBorder(border);
  }

  public boolean isPopupVisible() {
    return fPopupVisible;
  }

  public void setPopupVisible(boolean aVisible) {
    if (aVisible == isPopupVisible()) {
      return;
    }

    if (aVisible) {
      fPopup = createPopup();
      fPopup.pack();
      Point location = fPopupLocation.calculatePopupLocation(fOwner, fPopup);
      SwingUtilities.convertPointToScreen(location, fOwner);
      fPopup.setLocation(location);
    }

    fPopup.setVisible(aVisible);
  }

  /**
   * Returns true if the given child is either a descendant of the owner, or a descendant of the popup.
   * Descendant is pretty liberal for the popup in that it also allows the popup to spawn child popups.
   * @param aChild The child.
   * @return True if the given child is somehow related to this popup.
   */
  boolean isDescendentOrOwner(Component aChild) {
    return isDescendingFrom(aChild, fOwner) ||
           isDescendingFrom(aChild, fPopup);
  }

  private boolean isDescendingFrom(Component aChild, Component aParent) {
    Component current = aChild;
    while (current != null) {
      if (current == aParent) {
        return true;
      }
      current = getParentOrInvoker(current);
    }
    return false;
  }

  private Component getParentOrInvoker(Component aComponent) {
    if (aComponent instanceof JPopupMenu) {
      return ((JPopupMenu) aComponent).getInvoker();
    } else {
      return aComponent.getParent();
    }
  }

  /**
   * Hides the popup when clicking or scrolling outside of the popup and its descendants.
   * It does so by registering a global AWTEvent listener.
   */
  private static class HideWhenClickingElsewhereListener implements AWTEventListener {

    private final WeakReference<PopupPanel> fPopup;

    public static void install(PopupPanel aPopup) {
      final HideWhenClickingElsewhereListener listener = new HideWhenClickingElsewhereListener(aPopup);
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      java.security.AccessController.doPrivileged(
          new java.security.PrivilegedAction<Void>() {
            public Void run() {
              toolkit.addAWTEventListener(listener,
                                          AWTEvent.MOUSE_EVENT_MASK |
                                          AWTEvent.MOUSE_WHEEL_EVENT_MASK
              );
              return null;
            }
          }
      );
    }

    private HideWhenClickingElsewhereListener(PopupPanel aPopup) {
      fPopup = new WeakReference<>(aPopup);
    }

    @Override
    public void eventDispatched(AWTEvent aEvent) {
      PopupPanel popupWindow = fPopup.get();
      if (popupWindow == null) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
              public Void run() {
                toolkit.removeAWTEventListener(HideWhenClickingElsewhereListener.this);
                return null;
              }
            }
        );
      } else {
        handleEventImpl(aEvent, popupWindow);
      }
    }

    private void handleEventImpl(AWTEvent aEvent, PopupPanel aPopupWindow) {
      Component source = ((MouseEvent) aEvent).getComponent();
      if (aEvent.getID() == MouseEvent.MOUSE_PRESSED ||
          aEvent.getID() == MouseEvent.MOUSE_WHEEL) {

        if (!aPopupWindow.isDescendentOrOwner(source)) {
          aPopupWindow.setPopupVisible(false);
        }
      }
    }
  }

  /**
   * Hide the popup when the ancestor is moved or resized, for example by dragging the window.
   */
  private static class HideWhenOwnerMovedOrResizedListener extends ComponentAdapter implements AncestorListener {
    private final JComponent fOwner;
    private final WeakReference<PopupPanel> fPopup;
    private Rectangle fOwnerScreenBounds;

    public static void intall(JComponent aOwner, PopupPanel aPopup) {
      HideWhenOwnerMovedOrResizedListener listener = new HideWhenOwnerMovedOrResizedListener(aOwner, aPopup);
      aOwner.addComponentListener(listener);
      aOwner.addAncestorListener(listener);
    }

    public HideWhenOwnerMovedOrResizedListener(JComponent aOwner, PopupPanel aPopup) {
      fOwner = aOwner;
      fPopup = new WeakReference<>(aPopup);
      fOwnerScreenBounds = getScreenBounds(fOwner);
    }

    private Rectangle getScreenBounds(JComponent aComponent) {
      if (aComponent.isShowing()) {
        Point origin = aComponent.getLocation();
        SwingUtilities.convertPointToScreen(origin, aComponent);
        return new Rectangle(origin, aComponent.getSize());
      } else {
        return new Rectangle(0, 0, 0, 0);
      }
    }

    private void hideIfNeeded() {
      final PopupPanel popup = fPopup.get();
      if (popup == null) {
        fOwner.removeComponentListener(this);
        fOwner.removeAncestorListener(this);
      } else {
        Rectangle currentScreenBounds = getScreenBounds(fOwner);
        if (!currentScreenBounds.equals(fOwnerScreenBounds)) {
          fOwnerScreenBounds = currentScreenBounds;

          // Use an invoke layer, some OS's are picky when hiding windows during resizing others.
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              popup.setPopupVisible(false);
            }
          });
        }
      }
    }

    @Override
    public void componentResized(ComponentEvent aComponentEvent) {
      hideIfNeeded();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      hideIfNeeded();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      hideIfNeeded();
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
      hideIfNeeded();
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
      //not interested in this case
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
      hideIfNeeded();
    }
  }

  /**
   * Hides the popup when the focus is moved to another window. Do _not_ hide the popup when focus goes to one of
   * it's child windows, to support popup-in-popup scenario's.
   */
  private static class HideOnFocusLostListener implements PropertyChangeListener {
    private WeakReference<PopupPanel> fPopup;
    private boolean fHadFocus = false;

    public static void install(PopupPanel aPopup) {
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new HideOnFocusLostListener(aPopup));
    }

    private HideOnFocusLostListener(PopupPanel aPopup) {
      fPopup = new WeakReference<>(aPopup);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      PopupPanel popup = fPopup.get();
      if (popup == null) {
        // Prevent memory leaks with this global listener.
        ((KeyboardFocusManager) evt.getSource()).removePropertyChangeListener(this);
        return;
      }

      if ("focusOwner".equals(evt.getPropertyName())) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) {
          // Focus can be temporary null, ignore that and await the next event.
          return;
        }

        boolean hasFocus = popup.isDescendentOrOwner(focusOwner);
        boolean lostFocus = fHadFocus && !hasFocus;
        if (lostFocus) {
          popup.setPopupVisible(false);
        }
        fHadFocus = hasFocus;
      }
    }
  }
}
