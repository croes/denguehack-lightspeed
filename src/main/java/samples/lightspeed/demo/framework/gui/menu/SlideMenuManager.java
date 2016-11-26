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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import com.luciad.gui.swing.TLcdOverlayLayout;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.gui.ApplicationPanel;

/**
 * Manages all the slide menus used in the application. This class holds a list of slide menus and
 * positions them along the chosen side (<code>NORTH, SOUTH, WEST, EAST</code>) of the screen, such
 * that none of the slide menus that are positioned along the same side overlap.
 * <p>
 * Note that menus with a <code>NORTH</code> (i.e. top) or <code>SOUTH</code> (i.e. bottom) location
 * are positioned relative to the left side of the screen. Menus with a <code>WEST</code> (i.e. left)
 * or <code>EAST</code> (i.e. right) location are positioned relative to the top of the screen.
 *
 * Remarks:
 * <ul>
 * <li> Slide menus that are positioned along different sides of the screen could overlap.
 * <li> There is no limit to adding slide menus to a specific side of the screen, but when adding
 * too many menus, some of the menus will not be positioned within the view bounds any more.
 * </ul>
 */
public class SlideMenuManager {

  /**
   * Constant that defines the how many pixels a component is shown inside the view.
   */
  public static final int SCREEN_INSET = 5;

  /**
   * Constant that defines the spacing between two slide menus at the same side of the location.
   */
  private static final int MENU_SPACING;

  /**
   * Constant that defines the offset of the first slide menu to be added to a specific menu location.
   */
  private static final int INITIAL_OFFSET;

  static {
    MENU_SPACING = Integer.parseInt(Framework.getInstance().getProperty("menu.slide.panel.spacing", "10"));
    INITIAL_OFFSET = Integer.parseInt(Framework.getInstance().getProperty("menu.offset", "10"));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  // GUI attributes
  private ApplicationPanel fApplicationPanel;
  private MainMenu fMainMenu;
  private ArrayList<SlideMenu> fSlideMenus;
  private ArrayList<SlideMenu> fCenteredSlideMenus;
  private Map<SlideMenu, Component> fSlideMenuAnchors = new HashMap<SlideMenu, Component>();

  /**
   * Creates a slide menu manager.
   *
   * @param aApplicationPanel
   */
  public SlideMenuManager(ApplicationPanel aApplicationPanel) {
    fApplicationPanel = aApplicationPanel;
    fSlideMenus = new ArrayList<SlideMenu>();
    fCenteredSlideMenus = new ArrayList<SlideMenu>();
    fApplicationPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        parentPanelResized();
      }
    });
  }

  public void slideOutAll(boolean aExcludeSubMenus) {
    for (SlideMenu slideMenu : fSlideMenus) {
//      if ( aExcludeSubMenus ) {
      if (slideMenu.getParentMenu() == null) {
        slideMenu.startSlideOut();
      }
//      } else {
//        slideMenu.startSlideOut();
//      }
    }
  }

  public void slideInAll(boolean aExcludeSubMenus) {
    for (SlideMenu slideMenu : fSlideMenus) {
//      if ( aExcludeSubMenus ) {
      if (slideMenu.getParentMenu() == null) {
        slideMenu.startSlideIn();
      }
//      } else {
//        slideMenu.startSlideIn();
//      }
    }
  }

  public void registerMainMenu(MainMenu aMainMenu) {
    fMainMenu = aMainMenu;
    addSlideMenu(aMainMenu.getMainSlideMenu(), true);
  }

  /**
   * Removes the given slide menu from the manager: this method should always be used when removing
   * slide menus!
   *
   * @param aSlideMenu the slide menu that is to be removed
   */
  public void removeSlideMenu(SlideMenu aSlideMenu) {
    fSlideMenus.remove(aSlideMenu);
    fCenteredSlideMenus.remove(aSlideMenu);
    fSlideMenuAnchors.remove(aSlideMenu);
    fApplicationPanel.getOverlayPanel().remove(aSlideMenu);
    revalidate(fApplicationPanel.getOverlayPanel());
    fApplicationPanel.getOverlayPanel().repaint();
  }

  public static void revalidate(Component aComponent) {
    synchronized (aComponent.getTreeLock()) {
      aComponent.invalidate();

      Container root = aComponent.getParent();
      if (root == null) {
        // There's no parents. Just validate itself.
        aComponent.validate();
      } else {
        while (!(root instanceof JComponent) || !((JComponent) root).isValidateRoot()) {
          if (root.getParent() == null) {
            // If there's no validate roots, we'll validate the
            // topmost container
            break;
          }

          root = root.getParent();
        }

        root.validate();
      }
    }
  }

  /**
   * Adds the given slide menu to the view and positions it appropriately.
   *
   * @param aSlideMenu the slide menu that is to be added
   */
  public void addSlideMenu(SlideMenu aSlideMenu) {
    addSlideMenu(aSlideMenu, false, null);
  }

  /**
   * Adds the given slide menu to the view and positions it appropriately. If <code>aCentered</code>
   * is <code>true</code>, the the menu will positioned at the center along the side of the screen
   * where it's located. Take care when trying to add more that one centered menu, because multiple
   * menus that are centered will overlap!
   *
   * @param aSlideMenu the slide menu that is to be added
   * @param aCentered  indicates whether or not the menu will be positioned at the center
   */
  public void addSlideMenu(SlideMenu aSlideMenu, boolean aCentered) {
    addSlideMenu(aSlideMenu, aCentered, null);
  }

  /**
   * Adds the given slide menu to the view and positions it appropriately. If the given component is
   * not <code>null</code>, the slide menu is positioned relative to the given component: i.e. along
   * the same side of the screen with the offset along that side being centered around the center of
   * the given component.
   *
   * @param aSlideMenu the slide menu that is to be added
   * @param aComponent the component relative to which the slide menu is to be positioned
   */
  public void addSlideMenu(SlideMenu aSlideMenu, Component aComponent) {
    addSlideMenu(aSlideMenu, false, aComponent);
  }

  /**
   * Adds the given slide menu to the view and positions it appropriately. If the given component is
   * not <code>null</code>, the slide menu is positioned relative to the given component: i.e. along
   * the same side of the screen with the offset along that side being centered around the center of
   * the given component.
   *
   * NOTE: when <code>aComponent</code> is not <code>null</code>, positioning the given slide menu
   * relative to this component gets priority over the <code>aCentered</code> flag!
   *
   * @param aSlideMenu the slide menu that is to be added
   * @param aCentered  whether the slide menu should be centered along its side of the screen or not
   * @param aComponent the component relative to which the slide menu is to be positioned
   */
  private void addSlideMenu(SlideMenu aSlideMenu, boolean aCentered, Component aComponent) {
    if (aSlideMenu == null) {
      throw new IllegalArgumentException("Cannot add slide menu that is NULL");
    }

    fSlideMenuAnchors.put(aSlideMenu, aComponent);

    // 1. Be sure to add the slide menu to the demo panel's overlay panel
    fApplicationPanel.getOverlayPanel().add(aSlideMenu, TLcdOverlayLayout.Location.NO_LAYOUT);

    // 2. Position the slide menu correctly
    setSlideMenuLocation(aSlideMenu, aCentered, aComponent);
    if (aCentered) {
      fCenteredSlideMenus.add(aSlideMenu);
    }

    // 3. Add menu to the list (this must be done after setSideMenuLocation!)
    fSlideMenus.add(aSlideMenu);

    // 4. Revalidate
    revalidate(fApplicationPanel.getOverlayPanel());
    fApplicationPanel.getOverlayPanel().repaint();
  }

  private void setSlideMenuLocation(SlideMenu aSlideMenu, boolean aCentered, Component aComponent) {
    Container container = aSlideMenu.getParent();
    if (container == null) {
      throw new IllegalStateException("Component must have a parent (swing) component!");
    }
    SlideMenu parentMenu = aSlideMenu.getParentMenu();

    // Retrieve the offset at the side where the menu is to be added
    int offset = aCentered ?
                 getCenteredOffsetAtSide(aSlideMenu) :
                 getOffsetAtSide(aSlideMenu);

    if (parentMenu == null) {
      int posY;
      int posX;
      switch (aSlideMenu.getMenuLocation()) {
      case WEST:
        posY = Math.max(offset, 0);
        posY = Math.min(offset, container.getHeight() - aSlideMenu.getHeight());
        aSlideMenu.setLocation(-aSlideMenu.getWidth() + SCREEN_INSET, posY);
        break;
      case EAST:
        posY = Math.max(offset, 0);
        posY = Math.min(offset, container.getHeight() - aSlideMenu.getHeight());
        aSlideMenu.setLocation(container.getWidth() - SCREEN_INSET, posY);
        break;
      case NORTH:
        posX = Math.max(offset, 0);
        posX = Math.min(offset, container.getWidth() - aSlideMenu.getWidth());
        aSlideMenu.setLocation(posX, -aSlideMenu.getHeight() + SCREEN_INSET);
        break;
      case SOUTH:
        posX = Math.max(offset, 0);
        posX = Math.min(offset, container.getWidth() - aSlideMenu.getWidth());
        aSlideMenu.setLocation(posX, container.getHeight() - SCREEN_INSET);
        break;
      }
    } else {
      int viewWidth = container.getWidth();
      int viewHeight = container.getHeight();
      int viewOffset = SCREEN_INSET;
      int anchorOffset = getOffsetRelativeToComponent(aSlideMenu, aComponent);

      int locX = 0, locY = 0;
      switch (aSlideMenu.getMenuLocation()) {
      case WEST:
        locX = -aSlideMenu.getWidth();
        locY = anchorOffset;
        locY += parentMenu.getLocation().y;
        locY = Math.max(locY, viewOffset);  // check top border
        locY = Math.min(locY, viewHeight - aSlideMenu.getHeight() - viewOffset); // Check bottom border
        break;
      case EAST:
        locX = viewWidth;
        locY = anchorOffset;
        locY += parentMenu.getLocation().y;
        locY = Math.max(locY, viewOffset);  // check top border
        locY = Math.min(locY, viewHeight - aSlideMenu.getHeight() - viewOffset); // Check bottom border
        break;
      case SOUTH:
        locX = anchorOffset;
        locX += parentMenu.getLocation().x;
        locX = Math.max(locX, viewOffset); // check left border
        locX = Math.min(locX, viewWidth - aSlideMenu.getWidth() - viewOffset); // check right border
        locY = viewHeight;
        break;
      case NORTH:
        locX = anchorOffset;
        locX += parentMenu.getLocation().x;
        locX = Math.max(locX, viewOffset); // check left border
        locX = Math.min(locX, viewWidth - aSlideMenu.getWidth() - viewOffset); // check right border
        locY = -aSlideMenu.getHeight();
        break;
      }
      aSlideMenu.setLocation(locX, locY);
    }
  }

  /**
   * Calculates the current offset for the given side of the screen. The returned value indicates
   * the offset (in pixels) relative to the top side of the screen when given location is either
   * <code>WEST</code> or <code>EAST</code>. In case of <code>NORTH</code> and <code>SOUTH</code>,
   * the value indicates the offset relative to the left side of the screen.
   *
   * @param aMenu the menu to compute the offset for
   *
   * @return the current offset (in pixels) at which the next slide menu will be positioned on the
   *         given side of the screen
   */
  private int getOffsetAtSide(SlideMenu aMenu) {
    int offset = INITIAL_OFFSET;
    for (SlideMenu panel : fSlideMenus) {
      if (panel == aMenu) {
        return offset;
      }
      if (panel.getMenuLocation() == aMenu.getMenuLocation()) {
        offset += getParallelLength(panel) + MENU_SPACING;
      }
    }
    return offset;
  }

  /**
   * Returns the offset for the given slide menu when it is to be centered at a specific side of the screen.
   */
  private int getCenteredOffsetAtSide(SlideMenu aMenu) {
    MenuLocation location = aMenu.getMenuLocation();
    if (location == MenuLocation.EAST || location == MenuLocation.WEST) {
      return aMenu.getParent().getHeight() / 2 - aMenu.getHeight() / 2;
    } else if (location == MenuLocation.NORTH || location == MenuLocation.SOUTH) {
      return aMenu.getParent().getWidth() / 2 - aMenu.getWidth() / 2;
    }
    return SCREEN_INSET;
  }

  private int getOffsetRelativeToComponent(SlideMenu aMenu, Component aComponent) {
    MenuLocation location = aMenu.getMenuLocation();
    if (location == MenuLocation.EAST || location == MenuLocation.WEST) {
      return aComponent.getY() + aComponent.getHeight() / 2 - aMenu.getHeight() / 2;
    } else if (location == MenuLocation.NORTH || location == MenuLocation.SOUTH) {
      return aComponent.getX() + aComponent.getWidth() / 2 - aMenu.getWidth() / 2;
    }
    return 0;
  }

  /**
   * Returns the length of the given slide menu in the direction that is parallel to the side of
   * the screen at which the menu is located (as specified by <code>aSlideMenu.getMenuLocation()</code>).
   */
  private int getParallelLength(SlideMenu aSlideMenu) {
    MenuLocation loc = aSlideMenu.getMenuLocation();
    if (loc == MenuLocation.NORTH || loc == MenuLocation.SOUTH) {
      return aSlideMenu.getWidth();
    } else if (loc == MenuLocation.EAST || loc == MenuLocation.WEST) {
      return aSlideMenu.getHeight();
    }
    return 0;
  }

  /**
   * Repositions all the slide menus currently managed by the slide menu manager.
   */
  private void parentPanelResized() {

    // Before repositioning, first retract the whole main menu
    // If this is not done, the main menu will no longer be accessible
    if (fMainMenu == null) {
      return;
    }
    fMainMenu.retractAll();

    int width = fApplicationPanel.getWidth();
    int height = fApplicationPanel.getHeight();

    for (SlideMenu menu : fSlideMenus) {
      // Check if the current menu is centered
      boolean centered = fCenteredSlideMenus.contains(menu);

      setSlideMenuLocation(menu, centered, fSlideMenuAnchors.get(menu));
      // Do nothing if the menu is a submenu
      if (menu.getParentMenu() != null) {
        continue;
      }

      if (!centered) {
        switch (menu.getMenuLocation()) {
        case EAST:
          menu.setLocation(width - SCREEN_INSET, menu.getY());
          break;
        case WEST:
          menu.setLocation(-menu.getWidth() + SCREEN_INSET, menu.getY());
          break;
        case NORTH:
          menu.setLocation(menu.getX(), -menu.getHeight() + SCREEN_INSET);
          break;
        case SOUTH:
          menu.setLocation(menu.getX(), height - SCREEN_INSET);
          break;
        }
      } else {
        // We have to store these values in case we need to reposition any child menus
        // (the offset of the child menus w.r.t. the parent menus is not stored anywhere, so we need
        //  to calculate how much the parent menu moved, and then move the child menus by that same
        //  amount)
        int oldPosition = 0, newPosition = 0;

        switch (menu.getMenuLocation()) {
        case EAST:
          oldPosition = menu.getY();
          menu.setLocation(width - SCREEN_INSET, height / 2 - menu.getHeight() / 2);
          newPosition = menu.getY();
          break;
        case WEST:
          oldPosition = menu.getY();
          menu.setLocation(-menu.getWidth() + SCREEN_INSET, height / 2 - menu.getHeight() / 2);
          newPosition = menu.getY();
          break;
        case NORTH:
          oldPosition = menu.getX();
          menu.setLocation(width / 2 - menu.getWidth() / 2, -menu.getHeight() + SCREEN_INSET);
          newPosition = menu.getX();
          break;
        case SOUTH:
          oldPosition = menu.getX();
          menu.setLocation(width / 2 - menu.getWidth() / 2, height - SCREEN_INSET);
          newPosition = menu.getX();
          break;
        }

        int diff = newPosition - oldPosition;

        // Reposition child menus if current menu is centered
        for (SlideMenu childMenu : fSlideMenus) {
          if (childMenu.getParentMenu() == menu) {
            switch (childMenu.getMenuLocation()) {
            case EAST:
            case WEST:
              childMenu.setLocation(childMenu.getX(), childMenu.getY() + diff);
              break;
            case NORTH:
            case SOUTH:
              childMenu.setLocation(childMenu.getX() + diff, childMenu.getY());
              break;
            }
          }
        }
      }
    }
    slideInAll(true);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Enumeration type indicating the screen location of the menu. The locations correspond to the
   * screen side in following manner:
   * <ul>
   * <li> <code>WEST</code>: left side of the screen
   * <li> <code>EAST</code>: right side of the screen
   * <li> <code>SOUTH</code>: bottom side of the screen
   * <li> <code>NORTH</code>: top side of the screen
   * </ul>
   */
  public enum MenuLocation {
    WEST, EAST, SOUTH, NORTH;
  }
}
