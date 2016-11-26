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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import com.luciad.gui.TLcdAWTUtil;

/**
 * Allows retrieving properties of the graphics device, using defaults in a headless environment.
 */
public class ScreenSupport {
  private static final int DEFAULT_SCREEN_RESOLUTION = 96;
  private static int CACHED_SCREEN_RESOLUTION;
  private static boolean SCREEN_RESOLUTION_CACHED = false;

  private ScreenSupport() {
  }

  /**
   * Set a custom screen resolution.
   *
   * @param aScreenResolution the new screen resolution.
   */
  public static void setScreenResolution(int aScreenResolution) {
    CACHED_SCREEN_RESOLUTION = aScreenResolution;
    SCREEN_RESOLUTION_CACHED = true;
  }

  /**
   * <p>Returns the (cached) screen resolution, or a default resolution when running in headless mode. This method will
   * only retrieve the screen resolution once. As soon as this method has been called once, it will store the value and
   * return it on subsequent calls.</p>
   *
   * <p>Note that the first time this method is called it uses {@code SwingUtilities#invokeAndWait}.</p>
   * @return The cached screen resolution
   */
  public static int getScreenResolution() {
    if (!SCREEN_RESOLUTION_CACHED) {
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        public void run() {
          try {
            // We can't use the Toolkit.getDefaultToolkit().getScreenResolution() method
            // in a headless environment (e.g. server application).
            // Note: 'Headless' operation refers to the ability to run a Java program without a
            // monitor, mouse or keyboard.  In unix environments, it usually means that X
            // or an equivalent windowing system does not have to be present.
            if (GraphicsEnvironment.isHeadless()) {
              CACHED_SCREEN_RESOLUTION = DEFAULT_SCREEN_RESOLUTION;
            } else {
              CACHED_SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();
            }
          } catch (Exception e) {
            CACHED_SCREEN_RESOLUTION = DEFAULT_SCREEN_RESOLUTION;
          }
        }
      });
      SCREEN_RESOLUTION_CACHED = true;
    }
    return CACHED_SCREEN_RESOLUTION;
  }
}
