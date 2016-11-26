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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import samples.lightspeed.demo.application.DemoApplicationPanel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Controls the user interface changes that need to happen, depending on the currently active theme.
 */
public class ActiveThemeMenuController {

  private SlideMenuManager fSlideMenuManager;
  private DemoApplicationPanel fOwner;
  private ArrayList<SlideMenu> fMenus;
  private boolean fAutoSlideOut;

  public ActiveThemeMenuController(
      SlideMenuManager aSlideMenuManager,
      DemoApplicationPanel aOwner
  ) {
    fSlideMenuManager = aSlideMenuManager;
    fOwner = aOwner;
    fMenus = new ArrayList<SlideMenu>();
    fAutoSlideOut = Boolean.parseBoolean(Framework.getInstance()
                                                  .getProperty("menu.slide.panel.autoslideout", "true"));
  }

  public void removeAllThemePanels() {
    for (SlideMenu menu : fMenus) {
      fSlideMenuManager.removeSlideMenu(menu);
    }
    fMenus.clear();
    fOwner.setSouthDockedComponent(null);
  }

  public void addThemePanels(AbstractTheme activeTheme) {
    List<JPanel> themePanels = activeTheme.getThemePanels();
    if (themePanels != null) {
      if (themePanels.size()==1) {
        SlideMenu slideMenu = new SlideMenu(SlideMenuManager.MenuLocation.WEST, themePanels.get(0));
        addThemeMenu(slideMenu);
      }
      else {
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setOpaque(false);
        combinedPanel.setBackground(new Color(0f,0f,0f,0f));
        for (JPanel panel : themePanels) {
          panel.setOpaque(false);
          combinedPanel.add(panel);
        }
        combinedPanel.setSize(combinedPanel.getLayout().preferredLayoutSize(combinedPanel));
        SlideMenu slideMenu = new SlideMenu(SlideMenuManager.MenuLocation.WEST, combinedPanel);
        addThemeMenu(slideMenu);
      }
    }

    fOwner.setSouthDockedComponent(activeTheme.getSouthDockedComponent());
  }

  private void addThemeMenu(SlideMenu aSlideMenu) {
    boolean centered = Boolean.parseBoolean(Framework.getInstance().getProperty("menu.includeThemeButton", "false"));
    addThemeMenu(aSlideMenu, centered);
  }

  private void addThemeMenu(SlideMenu aSlideMenu, boolean aCentered) {
    fSlideMenuManager.addSlideMenu(aSlideMenu, aCentered);
    fMenus.add(aSlideMenu);
    if (!fAutoSlideOut) {
      aSlideMenu.startSlideIn();
    }
  }
}
