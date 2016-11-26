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
package samples.gxy.common.touch;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.luciad.input.touch.TLcdTouchDevice;

import samples.common.gui.blacklime.BlackLimeLookAndFeel;

/**
 * Used to check the touch device status and update the look and feel.
 */
public class TouchUtil {

  public static void setTouchLookAndFeel(Component aTopLevelComponent) {
    try {
      NimbusLookAndFeel laf = new NimbusLookAndFeel();
      UIManager.setLookAndFeel(laf);
      BlackLimeLookAndFeel.installTouchDefaults(UIManager.getLookAndFeelDefaults());
      BlackLimeLookAndFeel.workAroundBug_JDK_8057791(laf);
    } catch (UnsupportedLookAndFeelException aE) {
      throw new RuntimeException(aE);
    }

    // Initialize the Nimbus look and feel to avoid a bug on JDK 1.6 (fixed in 1.7)
    new JComboBox().getMinimumSize();

    // Refresh the given component (that doesn't have to be part of a window yet)
    if (aTopLevelComponent != null) {
      SwingUtilities.updateComponentTreeUI(aTopLevelComponent);
    }
    // Refresh any possible existing windows, such as for example the info dialog
    for (Window w : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(w);
    }
  }

  public static boolean checkTouchDevice(Component aParentComponent) {
    String[] aReason = new String[1];
    if (!isTouchDeviceSupported(aReason)) {
      JOptionPane.showMessageDialog(aParentComponent, aReason);
      return false;
    } else {
      return true;
    }
  }

  public static boolean isTouchDeviceSupported(String[] aReasonSFCT) {
    if (aReasonSFCT.length == 0) {
      throw new IllegalArgumentException("The size of the passed array should be at least 1");
    }
    switch (TLcdTouchDevice.getInstance().getTouchDeviceStatus()) {
    case READY:
      //do nothing, everything is good
      aReasonSFCT[0] = "";
      return true;
    case UNSUPPORTED:
      aReasonSFCT[0] = "Touch input is not supported on your system.";
      break;
    case DEVICE_MISSING:
      aReasonSFCT[0] = "No touch device has been detected.";
      break;
    case NOT_READY:
      aReasonSFCT[0] = "The touch device is not ready.";
      break;
    case DRIVER_MISSING:
      aReasonSFCT[0] = "The Luciad touch library could not be loaded.";
      break;
    case UNKNOWN_ERROR:
      aReasonSFCT[0] = "An unknown error occurred while trying to enable touch event support.";
      break;
    }
    return false;
  }
}
