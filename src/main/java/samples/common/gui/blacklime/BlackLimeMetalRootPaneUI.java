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
package samples.common.gui.blacklime;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalRootPaneUI;

/**
 * Extension of MetalRootPaneUI, to inherit all the behavior for lay out, resizing, title rendering etc. It inherits
 * from MetalLookAndFeel instead of from NimbusLookAndFeel because Nimbus doesn't provide anything for software rendered
 * root panes, it only support dialogs of the native OS.
 *
 * This class is only public because it is instantiated by Swing using reflection.
 */
public class BlackLimeMetalRootPaneUI extends MetalRootPaneUI {
  private static boolean fMetalThemeChanged = false;

  // This method is called by Swing via reflection
  @SuppressWarnings("UnusedParameters")
  public static ComponentUI createUI(JComponent c) {
    if (!fMetalThemeChanged) {
      fMetalThemeChanged = true;
      MetalLookAndFeel.setCurrentTheme(new MyMetalTheme());
    }
    return new BlackLimeMetalRootPaneUI();
  }

  /**
   * Colors are chosen so that the 'bumps' to drag the window aren't visible. As the metal look and feel is
   * only used here, for root panes, this doesn't really harm.
   */
  private static class MyMetalTheme extends DefaultMetalTheme {
    @Override
    public ColorUIResource getPrimaryControlHighlight() {
      return ColorPalette.darkestGrey;
    }

    @Override
    public ColorUIResource getPrimaryControlDarkShadow() {
      return ColorPalette.darkestGrey;
    }

    @Override
    public ColorUIResource getControlHighlight() {
      return ColorPalette.darkestGrey;
    }

    @Override
    public ColorUIResource getControlDarkShadow() {
      return ColorPalette.darkestGrey;
    }

    @Override
    public ColorUIResource getControl() {
      return ColorPalette.darkestGrey;
    }

    @Override
    public ColorUIResource getWindowTitleBackground() {
      // Re-wire this color, needed if the JIDE docking framework is used on Linux. It then uses this color
      // instead of the one from the UI properties.
      Color color = UIManager.getColor("DockableFrame.activeTitleBorderColor");
      return color != null ? new ColorUIResource(color) : super.getWindowTitleBackground();
    }

    @Override
    public ColorUIResource getWindowTitleInactiveBackground() {
      // Re-wire this color, needed if the JIDE docking framework is used on Linux. It then uses this color
      // instead of the one from the UI properties.
      Color color = UIManager.getColor("DockableFrame.inactiveTitleBorderColor");
      return color != null ? new ColorUIResource(color) : super.getWindowTitleInactiveBackground();
    }
  }
}
