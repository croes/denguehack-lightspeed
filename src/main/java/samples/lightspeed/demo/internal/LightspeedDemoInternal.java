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
package samples.lightspeed.demo.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.aboveterrain.TLspAboveTerrainCameraConstraint3D;

import samples.common.DefaultExceptionHandler;
import samples.common.MacUtil;
import samples.lightspeed.demo.application.InternalDemoApplicationPanel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.application.LightspeedDemoApplication;
import samples.lightspeed.demo.framework.gui.ApplicationPanel;

/**
 * @author Jef-Aram Van Gorp
 */
public class LightspeedDemoInternal extends LightspeedDemoApplication {

  /**
   * The default viewing mode is dual-fullscreen mode. This means that if no
   * command-line arguments are supplied, both a 2D and a 3D view will be opened
   * fullscreen on both monitors. If only one monitor is available, only a 2D
   * view will be opened.
   * <p/>
   * Command-line arguments that are valid: -no3d : no 3D view is created -no2d
   * : no 2D view is created -windowed : the window(s) of the application are
   * not shawn fullscreen
   *
   */
  public static void main(final String[] args) {
    MacUtil.installWorkarounds();
    MacUtil.initMacSystemProperties();
    // Launch Lightspeed Demo application
    try {
      TLcdAWTUtil.invokeAndWaitWithExc(new Runnable() {
        public void run() {
          TLcdIconFactory.setDefaultTheme(TLcdIconFactory.Theme.WHITE_THEME);
          boolean waitForInternalDemoLauncher = Arrays.asList(args).contains("-waitAfterThemeLoading");
          if (waitForInternalDemoLauncher) {
            waitAfterThemeLoading();
          }

          boolean force32bit = Arrays.asList(args).contains("-force32bit");

          int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
          if (bitness != 64) {
            if (force32bit) {
              System.out.println("LightspeedDemoInternal requires a 64-bit JVM. Please be aware " +
                                 "that performance in a 32-bit environment will be severely reduced.");
            } else {
              System.out.println("LightspeedDemoInternal requires a 64-bit JVM. To force running " +
                                 "with a 32-bit JVM anyway, add '-force32bit' to the command line. " +
                                 "Do not forget to reduce the heap size parameters in this case, " +
                                 "and please be aware that performance will be severely reduced.");
              System.exit(32);
            }
          }

          try {
            String[] args2 = new String[args.length + 2];
            System.arraycopy(args, 0, args2, 2, args.length);
            args2[0] = "-config";
            args2[1] = "samples/lightspeed/demo/demo.internal.properties";
            new LightspeedDemoInternal(args2);
          } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( null, new String[] {DefaultExceptionHandler.UNSATISFIED_LINK_ERROR_MESSAGE} );
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

  private static void waitAfterThemeLoading() {
    final Framework framework = Framework.getInstance();
    framework.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("themes".equals(evt.getPropertyName()) && evt.getOldValue() == null && evt.getNewValue() != null) {
          // First make sure all models are actually loaded
          framework.ensureModelsAreLoaded();
          String tempDir = System.getProperty("java.io.tmpdir");
          File file = new File(tempDir, "lsp.demo.internal.lock");
          System.out.println("Writing lock: " + file.getAbsolutePath());
          try {
            boolean success = file.createNewFile();
            if (success) {
              System.out.println("Waiting for InternalDemoLauncher to remove lock file.");
              while (file.exists()) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  break;
                }
              }
            }
          } catch (IOException e) {
            System.out.println("Using -waitAfterThemeLoading, but could not create lock file");
          }
          System.out.println("Continuing launching the demo.");

        }
      }
    });
  }

  public LightspeedDemoInternal(String[] args) {
    super(args);
  }

  @Override
  protected ApplicationPanel createDemoPanel(boolean a2DView, int aWidth, int aHeight) {
    ApplicationPanel result = new InternalDemoApplicationPanel(a2DView, aWidth, aHeight);

    final Framework framework = Framework.getInstance();

    String minimumAltitude = framework.getProperty("camera.aboveterrain.minimumaltitude");
    if (minimumAltitude != null) {
      ApplyMinimumAltitudeListener listener = new ApplyMinimumAltitudeListener(Double.parseDouble(minimumAltitude));
      result.getView().addPropertyChangeListener(listener);
      listener.applyMinimumAltitude(result.getView());
    }

    return result;
  }

  /**
   * Sets the minimum altitude on any {@link TLspAboveTerrainCameraConstraint3D}
   * whenever the view's transformation changes.
   */
  private static class ApplyMinimumAltitudeListener implements PropertyChangeListener {
    private final double fMinimumAltitude;

    public ApplyMinimumAltitudeListener(double aMinimumAltitude) {
      fMinimumAltitude = aMinimumAltitude;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("viewXYZWorldTransformation")) {
        ILspView view = (ILspView) evt.getSource();
        applyMinimumAltitude(view);
      }
    }

    public void applyMinimumAltitude(ILspView aView) {
      if (aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D) {
        TLspViewXYZWorldTransformation3D viewXYZWorldTransformation = (TLspViewXYZWorldTransformation3D) aView.getViewXYZWorldTransformation();
        for (ALspCameraConstraint<TLspViewXYZWorldTransformation3D> constraint3D : viewXYZWorldTransformation.getConstraints()) {
          if (constraint3D instanceof TLspAboveTerrainCameraConstraint3D) {
            ((TLspAboveTerrainCameraConstraint3D) constraint3D).setMinAltitude(fMinimumAltitude);
          }
        }
      }
    }
  }
}
