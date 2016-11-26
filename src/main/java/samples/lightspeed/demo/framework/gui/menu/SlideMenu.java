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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.gui.DemoUIColors;

/**
 * This class represents a sliding menu that is located on one side of the screen. When the mouse
 * cursor is near the menu, the menu will slide into the view and will stay in the view for as long
 * as the mouse cursor is on top of the menu; if this is no longer the case, the menu will
 * automatically
 * slide out of the view after a certain amount of time.
 */
public class SlideMenu extends JPanel {

  //The curve of the menu
  private static final int CURVE = 15;

  // If this slide menu is a submenu, this attribute refers to the parent slide menu
  private SlideMenu fParentMenu;

  // The color of the background layer of the slide menu
  private Color fMenuColor;

  // Specifies on which side of the screen the slide menu is located
  private SlideMenuManager.MenuLocation fLocation;

  // Animation used to slide the menu in/out
  private SlideMenuAnimation fAnimation;

  /////////////////////////////

  /**
   * Creates a slide menu that is located on the side of the screen corresponding to
   * <code>aLocation</code>.
   * The offset parameter can be used to control the placement of the slide menu along that side of
   * the screen. The offset is always relative to the zero coordinate on the axis orthogonal to the
   * chosen side of the screen, i.e.
   * <p/>
   * - for WEST and EAST : offset is relative to Y = 0    (i.e. top of the screen)
   * - for NORTH and SOUTH : offset is relative to X = 0  (i.e. left side of the screen)
   *
   * @param aContentPanel a panel containing the GUI content of the slide menu
   */
  public SlideMenu(SlideMenuManager.MenuLocation aLocation, JPanel aContentPanel) {
    this(null, aLocation, aContentPanel);
  }

  /**
   * Creates a slide menu that is parented by another (regular) slide menu.
   *
   * @param aParentMenu   the menu to which this menu will be add as a submenu
   * @param aContentPanel a panel containing the GUI content of the slide menu
   */
  public SlideMenu(SlideMenu aParentMenu, JPanel aContentPanel) {
    this(aParentMenu, aParentMenu.getMenuLocation(), aContentPanel);
  }

  private SlideMenu(SlideMenu aParentMenu, SlideMenuManager.MenuLocation aLocation, JPanel aContentPanel) {
    fParentMenu = aParentMenu;
    fMenuColor = DemoUIColors.PANEL_COLOR;
    fLocation = aLocation;

    if (aContentPanel != null) {
      // Initialize this slide menu with the given content panel
      initPanel(aContentPanel);
    }

    // Animation must be created after slide menu was added to overlay panel
    boolean autoSlideOut = Boolean.parseBoolean(Framework.getInstance().getProperty("menu.slide.panel.autoslideout", "true"));
    fAnimation = SlideMenuAnimation.createMenuAnimation(this, autoSlideOut);
    addMouseListener(fAnimation);
  }

  /**
   * Creates an empty slide menu
   *
   * @param aParentMenu
   */
  public SlideMenu(SlideMenu aParentMenu) {
    this(aParentMenu, null);
  }

  /**
   * Initializes this slide menu with the given content panel.
   *
   * @param aContentPanel the panel containing the actual content of this slide
   *                      menu
   */
  private void initPanel(JPanel aContentPanel) {
    // Configure this panel
    setOpaque(false);
    setLayout(null);

    // Configure and add content panel
    aContentPanel.setOpaque(false);
    add(aContentPanel, BorderLayout.CENTER);

    // Since this slide menu will be added to the overlay panel with NULL layout,
    // this must be done manually (NOTE: this is not the best way to handle this,
    // but it's the easiest way to ensure that the slide menus slide in/out of the
    // view in a correct manner)
    aContentPanel.getLayout().layoutContainer(aContentPanel);

    // Set the size of the panel
    setSize(aContentPanel.getLayout().preferredLayoutSize(aContentPanel));
  }

  /**
   * Returns the location of the panel. The location indicates at which side of the screen the
   * panel
   * is drawn (i.e. north, east, south or west).
   *
   * @return the location of the panel
   */
  public SlideMenuManager.MenuLocation getMenuLocation() {
    return fLocation;
  }

  /**
   * Sets the content panel of this menu, the previous content is removed.
   *
   * @param aNewContentPanel The new content for this menu.
   */
  public void setContentPanel(JPanel aNewContentPanel) {
    removeAll();
    initPanel(aNewContentPanel);
  }

  /**
   * Returns the parent menu of this menu. The value of this attribute can be <code>null</code> if
   * this
   * slide menu does not have a parent.
   *
   * @return the parent menu of this slide menu, or <code>null</code> if this slide menu has no
   *         parent
   */
  public SlideMenu getParentMenu() {
    return fParentMenu;
  }

  public void startSlideIn() {
    fAnimation.startSlideIn();
  }

  public void startSlideOut() {
    fAnimation.startSlideOut();
  }

  public void setSubMenuShowing(boolean aFlag) {

    boolean oldValue = fAnimation.isSubMenuShown();
    fAnimation.setSubMenuShown(aFlag);
    firePropertyChange("subMenuShowing", oldValue, aFlag);
  }

  public boolean isSubMenuShown() {
    return fAnimation.isSubMenuShown();
  }

  /**
   * Overwritten paint to draw a semi-transparent background underneath the content of this panel.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D graphics2D = (Graphics2D) g;

    // Draw background rectangle
    graphics2D.setColor(fMenuColor);
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CURVE, CURVE);
    graphics2D.setColor(DemoUIColors.PANEL_BORDER_COLOR);
    graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CURVE, CURVE);
  }
}
