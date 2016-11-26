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
package samples.lucy.frontend.mapcentric.onboarding;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * <p>Shows user on-boarding help when the application is started for the first time on a certain system. It helps users
 * to get quickly started, and understand the absolute basics of an application. They have to press the 'Got It!' button
 * before they can start using the application, encouraging them to really read the info.</p>
 *
 * <p>It overlays the main window with a partially transparent dialog, on which the on-boarding information is
 * displayed. The on-boarding information consists of a series of images, named NORTH_WEST.png, NORTH_EAST.png etc.
 * displayed at the respective locations. The images are assumed to be at {@value OverlayHelpUtil#IMAGE_DPI} dpi,
 * they are rescaled if the current system has a different dpi setting. As the UI itself also adapts to the screen dpi,
 * the help should match the UI regardless the screen dpi.</p>
 *
 * <p>If you want to modify the on-boarding help info, the png images were exported from on-boarding.svg. Please refer
 * to that file for further instructions.</p>
 *
 * <p>For testing or demonstration purposes, pressing 'shift F1' also shows the on-boarding help.</p>
 */
public class OnBoardingHelpAddOn extends ALcyPreferencesAddOn {
  public static final String SHOW_PROPERTY = "showOnBoardingHelpAtStartup";
  private static final String IMAGES_PATH_PROPERTY = "imagesPath";

  public OnBoardingHelpAddOn() {
    super("samples.lucy.frontend.mapcentric.onboarding.",
          "OnBoardingHelpAddOn.");
  }

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    ALcyProperties userPrefs = getPreferencesTool().getUserPreferences().subset(getShortPrefix());
    final boolean showHelp = userPrefs.getBoolean(SHOW_PROPERTY, true);
    userPrefs.putBoolean(SHOW_PROPERTY, false); //do not show help again

    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
          aLucyEnv.removeLucyEnvListener(this);
          String imagePath = getPreferences().subset(getShortPrefix()).getString(IMAGES_PATH_PROPERTY,
                                                                                 "no path for images configured");

          if (aLucyEnv.getTopLevelComponentCount() > 0) {
            final Window mainWindow = TLcdAWTUtil.findParentWindow(aLucyEnv.getTopLevelComponent(0));
            if (mainWindow.isShowing() && mainWindow instanceof RootPaneContainer) {
              if (showHelp) {
                OverlayHelpUtil.showOverlayHelp(mainWindow, imagePath, "Got it!");
              }
              showHelpUsingHotKeyUsefulForTesting(mainWindow, imagePath);
            }
          }
        }
      }
    });
  }

  private void showHelpUsingHotKeyUsefulForTesting(final Window aMainWindow, final String aImagePath) {
    JComponent component = ((RootPaneContainer) aMainWindow).getRootPane();
    String id = "showOnBoardingHelp";
    component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift F1"), id);
    component.getActionMap().put(id, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        OverlayHelpUtil.showOverlayHelp(aMainWindow, aImagePath, "Got it!");
      }
    });
  }
}
