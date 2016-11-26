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
package samples.lucy.frontend.mapcentric.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JToolBar;
import javax.swing.OverlayLayout;
import javax.swing.UIManager;

import samples.common.UIColors;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * Contains utility code for map centric add-ons.
 */
public class MapCentricUtil {

  private MapCentricUtil() {
  }

  /**
   * Action bar ID of the side bar
   */
  public static final String SIDE_BAR_TOOL_BAR = "sideBar";
  /**
   * Action bar ID of the bottom side bar
   */
  public static final String BOTTOM_SIDE_BAR_TOOL_BAR = "bottomSideBar";

  public static TLcyToolBar createToolBar(String aID, ALcyProperties aPreferences, JComponent aAcceleratorComponent, ILcyLucyEnv aLucyEnv) {
    TLcyToolBar bar = new TLcyToolBar();
    TLcyActionBarUtil.setupAsConfiguredActionBar(bar, aID, null, aPreferences, "", aAcceleratorComponent, aLucyEnv.getUserInterfaceManager().getActionBarManager());
    return bar;
  }

  public static TLcyToolBar createToolBar(JToolBar aDelegateToolBar, String aID, ALcyProperties aPreferences, JComponent aAcceleratorComponent, ILcyLucyEnv aLucyEnv) {
    TLcyToolBar bar = new TLcyToolBar(aDelegateToolBar);
    TLcyActionBarUtil.setupAsConfiguredActionBar(bar, aID, null, aPreferences, "", aAcceleratorComponent, aLucyEnv.getUserInterfaceManager().getActionBarManager());

    return bar;
  }

  public static Color getDividerColor() {
    return UIColors.getUIColor("MapCentric.dividerColor", UIColors.fgAccent());
  }

  public static int getDividerSize() {
    String key = "MapCentric.dividerSize";
    return UIManager.get(key) != null ? UIManager.getInt(key) : 2;
  }

  /**
   * Creates a panel with a help message on the background. Other content can be added to it,
   * so that the help message is no longer visible. Only one child should be added to it.
   * @param aMessage The message.
   * @return The component with a help messages on its background.
   */
  public static JComponent createContentWithHelpMessage(String aMessage) {
    JLabel help = new JLabel(aMessage, JLabel.CENTER);
    help.setPreferredSize(new Dimension(help.getPreferredSize().width, 200));
    help.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

    // Insert help message at the bottom. If real content is added, it will masque the help message.
    JLayeredPane lp = new JLayeredPane();
    lp.setLayout(new OverlayLayout(lp));
    lp.add(help, new Integer(-1));

    return lp;
  }
}
