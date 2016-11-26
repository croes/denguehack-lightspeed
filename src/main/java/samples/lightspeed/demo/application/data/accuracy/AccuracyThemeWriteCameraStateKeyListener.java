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
package samples.lightspeed.demo.application.data.accuracy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.ThemeAnimation;
import samples.lightspeed.demo.framework.util.CameraFileUtil;

/**
 * Keylistener that saves the current camera state of the associated view, when the "N" key is
 * pressed.
 */
class AccuracyThemeWriteCameraStateKeyListener implements KeyListener {

  private static ILcdLogger sLogger = TLcdLoggerFactory
      .getLogger(AccuracyThemeWriteCameraStateKeyListener.class);

  private ILspView fView;
  private AccuracyTheme fTheme;

  public AccuracyThemeWriteCameraStateKeyListener(AccuracyTheme aAccuracyTheme, ILspView aView) {
    fView = aView;
    fTheme = aAccuracyTheme;
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyChar() == 'A' && Framework.getInstance().getActiveTheme() == fTheme) {
      sLogger.info("Dumping camera info.");
      if (fView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation2D) {
        CameraFileUtil.write2DCamera(fView, ThemeAnimation.get2DCameraFile(fTheme.getActiveSubTheme()));
      } else {
        CameraFileUtil.write3DCamera(fView, ThemeAnimation.get3DCameraFile(fTheme.getActiveSubTheme()));
      }
    }
  }
}
