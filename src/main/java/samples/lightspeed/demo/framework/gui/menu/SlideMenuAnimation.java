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
package samples.lightspeed.demo.framework.gui.menu;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;

import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;

/**
 * Animation that will cause the associated menu panel to slide into the view.
 */
public class SlideMenuAnimation extends MouseAdapter implements MouseMotionListener, ILcdAnimation {

  private static final int CHECK_MOUSE_INSIDE_TIMER_IN_BETWEEN_MILIS = 200;
  private static final int CHECK_MOUSE_INSIDE_TIMER_INITIAL_MILIS = 2000;
  private static final double SLIDE_ANIMATION_SECONDS = 0.2;

  // The slide menu that is being animated
  private SlideMenu fSlideMenu;
  private SlideMenu fParentMenu;

  // Menu specific parameters
  private boolean fSubmenuShown;

  // Animation parameters
  private double fDuration;
  private boolean fRunning;
  private Timer fCheckMouseInsideTimer;

  private double fDirection = 0;
  private double fPercentageVisible = 0;
  private double fStartPercentageVisible = 0;

  private boolean fAutoSlide;
  private boolean fPostponeSlideOut;

  // Normally, slide menus slide back out if the mouse pointer is moved outside the slide menu.
  // However, we avoid slide menus to slide back out when the mouse pointer is being pressed,
  // even if it's outside the slide menu. This can happen when manipulating a slider.
  // In earlier releases we looked at whether the slide menu or any of its sub-components
  // had keyboard focus, but this is a bad criterion, since a radio button for example can
  // hold on to the focus, even if we want the slide menu to slide out again.
  private static Point sMousePressedLocation = null;

  static {
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
          MouseEvent me = (MouseEvent) event;
          if (event.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(me)) {
            sMousePressedLocation = new Point(MouseInfo.getPointerInfo().getLocation());
          } else if (event.getID() == MouseEvent.MOUSE_RELEASED && SwingUtilities.isLeftMouseButton(me)) {
            sMousePressedLocation = null;
          }
        }
      }
    }, AWTEvent.MOUSE_EVENT_MASK);
  }

  /**
   * Creates a slide animation for a slide menu. That is, a menu that is located on a specific
   * side of the screen and slides into the view when the mouse cursor is near or when its parent
   * menu
   * actives the menu callback.
   *
   * @param aSlideMenu    the slide menu that will be associated with this animation
   * @param aAutoSlideOut flag indicating whether the associated menu automatically slides out of
   *                      the view
   *
   * @return a new <code>SlideMenuAnimation</code> instance
   */
  public static SlideMenuAnimation createMenuAnimation(SlideMenu aSlideMenu, boolean aAutoSlideOut) {
    if (aSlideMenu == null) {
      throw new NullPointerException("Can not create menu animation for [null] ");
    }
    return new SlideMenuAnimation(aSlideMenu, aAutoSlideOut);
  }

  private SlideMenuAnimation(SlideMenu aSlideMenu, boolean aAutoSlideOut) {
    fSlideMenu = aSlideMenu;
    fParentMenu = aSlideMenu.getParentMenu();
    fSubmenuShown = false;

    fDuration = SLIDE_ANIMATION_SECONDS;
    fRunning = false;

    fAutoSlide = aAutoSlideOut;

    if (aAutoSlideOut) {
      fCheckMouseInsideTimer = new Timer(CHECK_MOUSE_INSIDE_TIMER_INITIAL_MILIS, new MyActionListener());
      fCheckMouseInsideTimer.setDelay(CHECK_MOUSE_INSIDE_TIMER_IN_BETWEEN_MILIS);
      fCheckMouseInsideTimer.setRepeats(true);
    }
  }

  /**
   * Specifies that the animation is running.
   */
  public boolean isRunning() {
    return fRunning;
  }

  /**
   * Indicates whether this slide menu animation is associated to a submenu or to a normal slide
   * menu.
   *
   * @return <code>true</code> if animation is associated to a submenu, <code>false</code> if
   *         associated
   *         to a normal menu
   */
  public boolean isSubmenu() {
    return fParentMenu != null;
  }

  /**
   * Sets a flag indicating that a submenu of the associated (normal) menu is being shown. This has
   * consequences for the mouse input behaviour, i.e. when a submenu is being shown, the retraction
   * part of the animation is no longer performed automatically.
   *
   * @param aFlag flag indicating whether a submenu of the menu associated to this animation is
   *              being shown
   */
  public void setSubMenuShown(boolean aFlag) {
    fSubmenuShown = aFlag;
  }

  /**
   * Indicates whether or not a submenu of the menu associated to this animation is being shown. If
   * this method returns true, then the slide out animation will not be triggered, unless this is
   * {@link #startSlideOut()} is called manually or when the submenu is being retracted.
   *
   * @return <code>true</code> if the a submenu of the associated menu is shown, <code>false</code>
   *         otherwise
   */
  public boolean isSubMenuShown() {
    return fSubmenuShown;
  }

  /**
   * Starts the animation for sliding the associated menu into the view. This method only has
   * effect
   * if the overlay panel of the associated menu is visible.
   */
  public void startSlideIn() {
    if (fSlideMenu.isVisible() && fSlideMenu.isEnabled()) {
      // Percent visible:
      fDirection = +1;
      fStartPercentageVisible = fPercentageVisible;
      if (fCheckMouseInsideTimer != null && !fCheckMouseInsideTimer.isRunning()) {
        fCheckMouseInsideTimer.start();
      }
      ALcdAnimationManager.getInstance().putAnimation(fSlideMenu, this);
    }
  }

  /**
   * Starts the animation for sliding the associated menu out of the view. This method only has
   * effect
   * if the overlay panel of the associated menu is visible.
   */
  public void startSlideOut() {
    if (fCheckMouseInsideTimer != null && fCheckMouseInsideTimer.isRunning()) {
      fCheckMouseInsideTimer.stop();
    }

    if (fSlideMenu.isVisible()) {
      fDirection = -1;
      fStartPercentageVisible = fPercentageVisible;
      ALcdAnimationManager.getInstance().putAnimation(fSlideMenu, this);
    }

  }

  /**
   * Sets whether the slide out animation should be postponed
   *
   * @param aPostpone whether the slide out animation should be postponed
   */
  public void setPostponeSlideOut(boolean aPostpone) {
    fPostponeSlideOut = aPostpone;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Animation

  public double getDuration() {
    return fDuration;
  }

  public void start() {
    fRunning = true;
  }

  public void stop() {
    fRunning = false;
  }

  public boolean isLoop() {
    return false;
  }

  public void restart() {
  }

  public void setTime(double aTime) {
    fPercentageVisible = fStartPercentageVisible +
                         fDirection * aTime / getDuration() * (fDirection > 0 ? (1 - fStartPercentageVisible) : fStartPercentageVisible);

    try {
      switch (fSlideMenu.getMenuLocation()) {
      case WEST:
        updateWest();
        break;
      case EAST:
        updateEast();
        break;
      case SOUTH:
        updateSouth();
        break;
      case NORTH:
        updateNorth();
        break;
      default:
        updateWest();
        break;
      }
    } catch (Exception e) {
      ALcdAnimationManager.getInstance().removeAnimation(fSlideMenu);
    }
  }

  private void updateNorth() {
    int curY = (int) ((-fSlideMenu
        .getHeight() + (isSubmenu() ? 0 : SlideMenuManager.SCREEN_INSET)) + getCurrentDisplacement() * getLength());
    curY = Math.min((isSubmenu() ? fParentMenu.getHeight() : 0), curY);
    fSlideMenu.setLocation(fSlideMenu.getX(), curY);
  }

  private void updateWest() {
    int curX = (int) ((-fSlideMenu
        .getWidth() + (isSubmenu() ? 0 : SlideMenuManager.SCREEN_INSET)) + getCurrentDisplacement() * getLength());
    curX = Math.min((isSubmenu() ? fParentMenu.getWidth() : 0), curX);
    fSlideMenu.setLocation(curX, fSlideMenu.getY());
  }

  private void updateEast() {
    int endPoint = fSlideMenu.getParent().getWidth() - fSlideMenu
        .getWidth() - (isSubmenu() ? fParentMenu.getWidth() : 0);
    int curX = (int) ((fSlideMenu.getParent()
                                 .getWidth() - (isSubmenu() ? 0 : SlideMenuManager.SCREEN_INSET)) - getCurrentDisplacement() * getLength());
    curX = Math.max(endPoint, curX);
    fSlideMenu.setLocation(curX, fSlideMenu.getY());
  }

  private void updateSouth() {
    int endPoint = fSlideMenu.getParent().getHeight() - fSlideMenu
        .getHeight() - (isSubmenu() ? fParentMenu.getHeight() : 0);
    int curY = (int) ((fSlideMenu.getParent()
                                 .getHeight() - (isSubmenu() ? 0 : SlideMenuManager.SCREEN_INSET)) - getCurrentDisplacement() * getLength());
    curY = Math.max(endPoint, curY);
    fSlideMenu.setLocation(fSlideMenu.getX(), curY);
  }

  private double getLength() {
    switch (fSlideMenu.getMenuLocation()) {
    case WEST:
    case EAST:
      return (isSubmenu() ? fSlideMenu.getWidth() + fParentMenu.getWidth() : fSlideMenu
          .getWidth());
    case SOUTH:
    case NORTH:
      return (isSubmenu() ? fSlideMenu.getHeight() + fParentMenu.getHeight() : fSlideMenu
          .getHeight());
    }
    return 0;
  }

  private double getCurrentDisplacement() {
    // Smoothing function
    return 0.5 * (1.0 - Math.cos(fPercentageVisible * Math.PI));
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Mouse Input Methods

  @Override
  public void mouseEntered(MouseEvent e) {
    if (!isSubmenu() && !isSubMenuShown() && fAutoSlide) {
      startSlideIn();
    }
  }

  @Override
  public void mouseExited(final MouseEvent me) {
    // Ignore mouseExited events, they are unreliable
  }

  //////////////////////////////////////////////////////////////////////////////////////////

  private class MyActionListener implements ActionListener {
    /**
     * Recursively visit all the children of the container.
     * If any of the children is a JTextComponent and
     * owns the focus return true
     * otherwise return false
     */
    private boolean anyChildHasFocus(Container c) {
      Component[] children = c.getComponents();

      for (Component child : children) {
        if (child instanceof JTextComponent && child.isFocusOwner()) {
          return true;
        } else if (child instanceof Container && anyChildHasFocus((Container) child)) {
          return true;
        }
      }

      return false;
    }

    public void actionPerformed(ActionEvent e) {
      if (fPostponeSlideOut) {
        return;
      }

      //if any of the text components in the panel has focus
      //cancel the slideout operation
      if (anyChildHasFocus(fSlideMenu)) {
        return;
      }

      Point location = MouseInfo.getPointerInfo().getLocation();

      Point p1 = new Point(location);

      SwingUtilities.convertPointFromScreen(p1, fSlideMenu);

      boolean mousePressedInsideMe = mousePressedInsideMe();
      if (!isSubMenuShown() && !fSlideMenu.contains(p1) && !mousePressedInsideMe) {
        if (isSubmenu()) {
          Point p2 = new Point(location);
          SwingUtilities.convertPointFromScreen(p2, fParentMenu);
          if (fParentMenu.contains(p2)) {
            return;
          }
        }
        fCheckMouseInsideTimer.stop();
        if (isSubmenu()) {
          fParentMenu.setSubMenuShowing(false);
          fParentMenu.startSlideOut();
        }
        startSlideOut();
      }
    }

    private boolean mousePressedInsideMe() {
      if (sMousePressedLocation == null) {
        return false;
      }
      Point p1 = new Point(sMousePressedLocation);
      SwingUtilities.convertPointFromScreen(p1, fSlideMenu);
      return fSlideMenu.contains(p1);
    }

  }
}
