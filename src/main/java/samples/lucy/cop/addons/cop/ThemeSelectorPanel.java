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
package samples.lucy.cop.addons.cop;

import javax.swing.JPanel;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;

import samples.lucy.theme.ThemeManager;

/**
 * UI which contains the theme selector tool bar
 *
 */
final class ThemeSelectorPanel extends JPanel {
  private static final String THEME_SELECTOR_TOOLBAR_ID = "themeSelectorToolBar";
  private static final String THEME_SELECTOR_TOOLBAR_PREFIX = THEME_SELECTOR_TOOLBAR_ID + ".";

  private static final String THEME_CHOOSE_ACTION_ID = "themeChooserAction";

  private final String fPropertiesPrefix;
  private final ALcyProperties fProperties;
  private final ILcyLucyEnv fLucyEnv;

  ThemeSelectorPanel(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv, ILcyLspMapComponent aMapComponent) {
    fPropertiesPrefix = aPropertiesPrefix;
    fProperties = aProperties;
    fLucyEnv = aLucyEnv;

    add(createToolBar(aMapComponent).getComponent());

    final ThemeChooserAction themeChooserAction = new ThemeChooserAction(aLucyEnv.getService(ThemeManager.class), aLucyEnv);
    themeChooserAction.putValue(TLcyActionBarUtil.ID_KEY, aPropertiesPrefix + THEME_CHOOSE_ACTION_ID);
    TLcyActionBarUtil.insertInConfiguredActionBars(themeChooserAction, aMapComponent, aLucyEnv.getUserInterfaceManager().getActionBarManager(), aProperties);
  }

  private ILcyToolBar createToolBar(ILcyLspMapComponent aMapComponent) {
    TLcyToolBar toolBar = new TLcyToolBar();
    toolBar.setProperties(fProperties.subset(fPropertiesPrefix + THEME_SELECTOR_TOOLBAR_PREFIX));
    TLcyActionBarMediatorBuilder.newInstance(fLucyEnv.getUserInterfaceManager().getActionBarManager())
                                .sourceActionBar(THEME_SELECTOR_TOOLBAR_ID, aMapComponent)
                                .targetActionBar(toolBar)
                                .bidirectional()
                                .mediate();
    return toolBar;
  }
}
