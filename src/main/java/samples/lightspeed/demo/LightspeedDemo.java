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
package samples.lightspeed.demo;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.TLcdLicenseError;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;

import samples.common.DefaultExceptionHandler;
import samples.common.MacUtil;
import samples.lightspeed.demo.application.DemoApplicationPanel;
import samples.lightspeed.demo.framework.application.LightspeedDemoApplication;
import samples.lightspeed.demo.framework.gui.ApplicationPanel;

/**
 * Main class of the LuciadLightspeed demo application.
 */
public class LightspeedDemo extends LightspeedDemoApplication {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LightspeedDemo.class);

  /**
   * The default viewing mode is single screen mode.
   * <p/>
   * Command-line arguments that are valid:
   * <ul>
   * <li>-dual: start demo in dual screen mode</li>
   * <li>-windowed: the window(s) of the application are
   * not shown fullscreen</li>
   * </ul>
   */
  public static void main(final String[] args) {
    MacUtil.installWorkarounds();
    MacUtil.initMacSystemProperties();
    try {
      TLcdAWTUtil.invokeAndWaitWithExc(new Runnable() {
        public void run() {
          try {
            TLcdIconFactory.setDefaultTheme(TLcdIconFactory.Theme.WHITE_THEME);
            new TLspShapePainter();
            new LightspeedDemo(args);
          } catch (UnsatisfiedLinkError e) {
            sLogger.error("The application could not find the OpenGL libraries in its library path.", e);
            JOptionPane.showMessageDialog( null, new String[] {DefaultExceptionHandler.UNSATISFIED_LINK_ERROR_MESSAGE} );
          } catch (TLcdLicenseError e) {
            sLogger.error("License error: please check if the correct license is in your class path.");
            int split_index = e.getMessage().indexOf('.') + 1;
            JOptionPane.showMessageDialog(null, new String[]{
                e.getMessage().substring(0, split_index),
                e.getMessage().substring(split_index),
                "Please check if the correct license is in your class path."
            }, "License Error", JOptionPane.WARNING_MESSAGE);
          }
        }
      });
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  public LightspeedDemo(String[] args) {
    super(args);
  }

  @Override
  protected ApplicationPanel createDemoPanel(boolean a2DView, int aWidth, int aHeight) {
    return new DemoApplicationPanel(a2DView, aWidth, aHeight);
  }
}
