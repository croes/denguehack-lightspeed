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
package samples.lucy.lightspeed.oculus;

import com.luciad.oculus.TLspOculusDeviceBuilder;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspStereoscopicDevice;
import com.luciad.view.lightspeed.TLspViewBuilder;

/**
 * This manager makes sure any previous Oculus view is destroyed before creating another one.
 */
class OculusViewManager {

  private static final Object sOculusLock = new Object();
  private static ILspView sOculusView = null;
  private static ILcdDisposable sDisposableAction = null;
  private static boolean sMonoscopic = false;
  private static boolean sMirrorEnabled = true;
  private static TLspStereoscopicDevice sOculusDevice = TLspOculusDeviceBuilder.newBuilder()
                                                                               .mirrorView(sMirrorEnabled)
                                                                               .monoscopic(sMonoscopic)
                                                                               .build();

  /**
   * Sets whether or not a mirror view is shown.
   *
   * @param aMirrorEnabled whether or not the mirror view will be shown.
   */
  public static void setMirrorEnabled(boolean aMirrorEnabled) {
    sMirrorEnabled = aMirrorEnabled;
    initializeDevice();
  }

  /**
   * Sets whether or not the view will be rendered monoscopic.
   *
   * @param aMonoscopic whether or not the view is rendered monoscopic.
   */
  public static void setMonoscopic(boolean aMonoscopic) {
    sMonoscopic = aMonoscopic;
    initializeDevice();
  }

  private static void initializeDevice() {
    sOculusDevice = TLspOculusDeviceBuilder.newBuilder()
                                           .mirrorView(sMirrorEnabled)
                                           .monoscopic(sMonoscopic)
                                           .build();
  }

  /**
   * Get an instance of an Oculus view
   *
   * @param aDisposableAction the action requesting an Oculus view
   * @return the Oculus view
   */
  public static ILspView getOculusView(ILcdDisposable aDisposableAction) {
    synchronized (sOculusLock) {
      if (sOculusView != null) {
        sDisposableAction.dispose();
        destroyOculusView();
      }

      sDisposableAction = aDisposableAction;
      sOculusView = TLspViewBuilder.newBuilder()
                                   .buildStereoscopicView(sOculusDevice);
      return sOculusView;
    }
  }

  /**
   * Destroy the Oculus view.
   */
  public static void destroyOculusView() {
    synchronized (sOculusLock) {
      if (sOculusView != null) {
        sOculusView.destroy();
        sOculusView = null;
        sDisposableAction = null;
      }
    }
  }

}
