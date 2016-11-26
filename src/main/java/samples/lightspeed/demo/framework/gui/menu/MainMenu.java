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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IdentityHashMap;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * Main menu of the application.
 */
public class MainMenu implements ActionListener {

  // constants
  public static final String MENU_ACTION_EXPAND = "expand";

  // The slide menu that is used as main menu
  private SlideMenu fMainMenu;

  // The submenu that is currently being shown
  private SlideMenu fActiveSubMenu;

  // The last button that was pressed (and for which the submenu is now shown)
  private ScalableButton fLastButton;

  // Maps main menu buttons to their associated submenus
  IdentityHashMap<ScalableButton, SlideMenu> fSubMenus;

  // Color attributes
  private Color fColor;
  private Color fSelectedColor;

  // Determines whether the main menu is slid out of the view when a submenu is disabled
  private boolean fAutoSlideOut;

  /**
   * Creates a new main menu with the given slide menu.
   *
   * @param aMainMenu the slide menu that will represent the main menu
   */
  public MainMenu(SlideMenu aMainMenu) {
    fMainMenu = aMainMenu;
    fColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);
    fSelectedColor = new Color(1.0f, 1.0f, 1.0f, 0.2f);
    fSubMenus = new IdentityHashMap<ScalableButton, SlideMenu>();
    fAutoSlideOut = Boolean.parseBoolean(Framework.getInstance().getProperty("menu.slide.panel.autoslideout", "true"));

    fMainMenu.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("subMenuShowing".equals(evt.getPropertyName()) && evt.getNewValue().equals(false)) {
          disableAllActiveSubMenus();
        }
      }
    });
  }

  public SlideMenu getMainSlideMenu() {
    return fMainMenu;
  }

  public void setColor(Color aColor) {
    fColor = aColor;
  }

  public void setSelectedColor(Color aSelectedColor) {
    fSelectedColor = aSelectedColor;
  }

  public void actionPerformed(ActionEvent e) {
    // When pressing the main menu button of the currently opened submenu again: retract main- and submenu
    if (e.getSource() == fLastButton) {
      retractAll();
      fLastButton = null;
    } else if (e.getActionCommand().equals(MENU_ACTION_EXPAND)) { // open submenu associated to main menu button
      expandSubMenu((ScalableButton) e.getSource());
      fLastButton = (ScalableButton) e.getSource();
    } else {
      retractAll();
      fLastButton = null;
    }
  }

  public void registerSubMenu(SlideMenuManager aSlideMenuManager, ScalableButton aButton, SlideMenu aSlideMenu) {
    // Make sure the given slide menu is visible
    aSlideMenu.setVisible(true);

    // Make this main menu the action listener of the given button
    aButton.setActionCommand(MENU_ACTION_EXPAND);
    aButton.addActionListener(this);

    // Add slide menu
    aSlideMenuManager.addSlideMenu(aSlideMenu, aButton);

    // Add button and submenu to this main menu
    fSubMenus.put(aButton, aSlideMenu);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  public void retractAll() {

    for (ScalableButton button : fSubMenus.keySet()) {
      button.retract();
    }

    // Slide the active submenu out of the view
    if (fActiveSubMenu != null) {
      slideOutActiveSubMenu();
    }

    fActiveSubMenu = null;

    // Slide the main menu out of the view
    if (fAutoSlideOut) {
      fMainMenu.setSubMenuShowing(false);
      fMainMenu.startSlideOut();
    }

    fLastButton = null;
  }

  private void expandSubMenu(ScalableButton aButton) {

    SlideMenu tempMenu;
    if (fActiveSubMenu != null) {
      tempMenu = fSubMenus.get(aButton);

      // If the menu associated to the button is already shown: do nothing
      if (tempMenu == fActiveSubMenu) {
        return;
      }

      // Otherwise: slide out current active submenu and 'decolor' associated button
      slideOutActiveSubMenu();
    }

    // set the new active submenu and slide it into the view
    fActiveSubMenu = fSubMenus.get(aButton);

    fMainMenu.setSubMenuShowing(true);
    fActiveSubMenu.startSlideIn();
    aButton.setBackgroundColor(fSelectedColor);
  }

  private void slideOutActiveSubMenu() {
    fActiveSubMenu.startSlideOut();
    for (ScalableButton scalableButton : fSubMenus.keySet()) {
      if (fActiveSubMenu == fSubMenus.get(scalableButton)) {
        scalableButton.setBackgroundColor(fColor);
      }
    }
  }

  private void disableAllActiveSubMenus() {
    for (ScalableButton scalableButton : fSubMenus.keySet()) {
      if (fActiveSubMenu == fSubMenus.get(scalableButton)) {
        scalableButton.setBackgroundColor(fColor);
      }
    }
    for (ScalableButton button : fSubMenus.keySet()) {
      button.retract();
    }
    fActiveSubMenu = null;
    fLastButton = null;
    fMainMenu.setSubMenuShowing(false);

  }
}
