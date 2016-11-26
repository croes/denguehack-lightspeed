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
package samples.common;

import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import com.apple.eawt.QuitStrategy;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconImageUtil;
import com.luciad.util.TLcdSystemPropertiesUtil;

/**
 * Class containing utility methods specific to OS X.
 */
public final class MacUtil {

  private MacUtil() {

  }

  /**
   * <p>
   *   Install workarounds for some OS X specific bugs.
   * </p>
   *
   * <p>
   *   This method has no effect when not running on OS X.
   * </p>
   */
  public static void installWorkarounds() {
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        SplitPaneMouseEventFixerMac.install();
      }
    });
  }

  /**
   * Returns {@code true} when the JVM is running on the OS X operating system, and is using
   * the Aqua look-and-feel (default look-and-feel on Mac).
   *
   * @return {@code true} when the JVM is running on the OS X operating system, and is using
   * the Aqua look-and-feel (default look-and-feel on Mac).
   */
  public static boolean isAquaLookAndFeel() {
    return TLcdSystemPropertiesUtil.isMacOS() && "Mac OS X".equals(UIManager.getLookAndFeel().getName());
  }

  /**
   * <p>
   *   A Java application running under OS X needs to perform some extra setup and system properties to look more like a native application.
   *   Examples of this are setting up the dock icon, enabling full screen, properly handling cmd-Q, ... .
   *   The setting of the system properties is done in this method.
   *   Most of those system properties must be set before any Swing UI is created.
   *   Therefore it is recommended to call this method as one of the first methods in the {@code main} method of your application.
   * </p>
   *
   * <p>
   *   Calling this method on non-OS X systems (Windows or Linux) will have no effect.
   * </p>
   *
   * @see #initMacApplication(List, JFrame)
   */
  public static void initMacSystemProperties() {
    if (!TLcdSystemPropertiesUtil.isMacOS()) {
      return;
    }
    //This can always be set to true, as the property is only considered by the Aqua look and feel
    //Setting this property when another look and feel is used will have no effect, but won't cause
    //any problems either
    System.setProperty("apple.laf.useScreenMenuBar", "true");
  }

  /**
   * <p>
   *   A Java application running under OS X needs to perform some extra setup to look more like a native application.
   *   Examples of this are setting up the dock icon, enabling full screen, properly handling cmd-Q, ... .
   * </p>
   *
   * <p>
   *   This method bundles all these setup steps.
   *   Calling this method on non-OS X systems (Windows or Linux) will have no effect.
   * </p>
   *
   * <p>
   *   Note that this method will only work correctly if the main method of the application called
   *   the {@link #initMacSystemProperties()} before any Swing components were created.
   * </p>
   *
   * @param aDockIconCandidates List of images with possible dock icons.
   *                            From this list, the icon with the highest resolution will be chosen.
   *                            Typically, this list is the same as what you pass to {@link JFrame#setIconImages(List)}.
   *                            Use {@code null} or an empty list if you do not want a dock icon.
   * @param aMainFrame The main frame of the application.
   *                   Should not be {@code null}.
   *
   * @see #initMacSystemProperties()
   */
  public static void initMacApplication(List<Image> aDockIconCandidates,
                                        JFrame aMainFrame) {
    if (!TLcdSystemPropertiesUtil.isMacOS()) {
      return;
    }
    Objects.requireNonNull(aMainFrame, "The main frame should not be null");

    Application application = Application.getApplication();
    if (aDockIconCandidates != null && !aDockIconCandidates.isEmpty()) {
      Image dockIcon = aDockIconCandidates.get(0);
      for (Image image : aDockIconCandidates) {
        if (image.getHeight(null) > dockIcon.getHeight(null) &&
            image.getWidth(null) > dockIcon.getWidth(null)) {
          dockIcon = image;
        }
      }
      application.setDockIconImage(dockIcon);
    }

    FullScreenUtilities.setWindowCanFullScreen(aMainFrame, true);

    //Closing all windows will now have the same effect as the user closing the frame
    //instead of the default strategy which exits the JVM
    application.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);

    application.setAboutHandler(new AboutHandler() {
      @Override
      public void handleAbout(AppEvent.AboutEvent aAboutEvent) {
        showAboutDialog(TLcdAWTUtil.findParentWindow(aAboutEvent.getSource()));
      }
    });
  }

  /**
   * This method updates the given dialog to ensure that it gets closed when pressing cmd-w.
   * Calling this method on non-OS X systems (Windows or Linux) will have no effect.
   *
   * @param aDialogSFCT The dialog which will be updated to ensure that it gets closed when pressing cmd-w.
   */
  public static void allowClosingWithCmdW(final JDialog aDialogSFCT) {
    if (!TLcdSystemPropertiesUtil.isMacOS()) {
      return;
    }
    InputMap input_map = aDialogSFCT.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap action_map = aDialogSFCT.getRootPane().getActionMap();
    String key = "lcd_close_the_dialog";
    input_map.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, MetaKeyUtil.getCMDDownMask()), key);
    action_map.put(key, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aDialogSFCT.dispatchEvent(new WindowEvent(aDialogSFCT, WindowEvent.WINDOW_CLOSING));
      }
    });
  }

  private static void showAboutDialog(Component aParentComponent) {
    Image icon = new TLcdIconImageUtil().loadImage("images/luciad_icon32.png");
    String message1 = "LuciadLightspeed based sample application";
    String message2 = "Contains LuciadLightspeed\u2122 Software \u00A9 by Luciad\u00AE, Leuven (Belgium). All Rights Reserved.";

    JOptionPane.showMessageDialog(aParentComponent,
                                  new String[]{message1, message2},
                                  "About",
                                  JOptionPane.INFORMATION_MESSAGE,
                                  icon != null ? new ImageIcon(icon) : null);
  }

  /**
   * Sets the given window full screen or windowed depending on
   * the current status of the window.
   * @param aWindow Window to be toggled full screen or windowed mode.
   */
  public static void setFullScreen(Window aWindow) {
    Application.getApplication().requestToggleFullScreen(aWindow);
  }
}
