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

import java.lang.reflect.Field;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Works around Linux specific window problems.
 */
public class XWindowUtil {

  private static final ILcdLogger LOG = TLcdLoggerFactory.getLogger(XWindowUtil.class);

  // Currently only works around Gnome problems, but can be extended for other window managers.
  public static void workAroundWindowManagerProblems() {
    // Not running this in the EDT may causes issues with frame icons.
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        String desktopSession = System.getenv("DESKTOP_SESSION");
        if (desktopSession != null && desktopSession.toLowerCase().contains("gnome")) {
          try {
            Class<?> xwm = Class.forName("sun.awt.X11.XWM");
            Field awt_wmgr = xwm.getDeclaredField("awt_wmgr");
            awt_wmgr.setAccessible(true);
            Field other_wm = xwm.getDeclaredField("OTHER_WM");
            other_wm.setAccessible(true);
            Field undetermined_wm = xwm.getDeclaredField("UNDETERMINED_WM");
            undetermined_wm.setAccessible(true);
            if (awt_wmgr.get(null).equals(other_wm.get(null)) ||
                awt_wmgr.get(null).equals(undetermined_wm.get(null))) {
              Field metacity_wm = xwm.getDeclaredField("METACITY_WM");
              metacity_wm.setAccessible(true);
              awt_wmgr.set(null, metacity_wm.get(null));
              LOG.info("Installed Gnome Window manager workaround");
            }
          } catch (Exception e) {
            LOG.warn("Gnome Window manager workaround failed: " + e.getMessage(), e);
          }
        }
      }
    });
  }

}
