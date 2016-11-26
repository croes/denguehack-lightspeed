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
package samples.lightspeed.demo.framework.data.themes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.util.CameraFileUtil;

/**
 * Animation that is used to move the camera to a user defined position on the map, when switching
 * to the associated theme.
 */
public class ThemeAnimation {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ThemeAnimation.class);

  // Files containing the parameters for 2D- or 3D-view for a specific theme
  private String fSource3D;
  private File fFile3D;
  private long fFile3DLastModified;
  private String fSource2D;
  private File fFile2D;
  private long fFile2DLastModified;

  // Camera parameters, resp. for 2D- and 3D-view
  private double[] f2DParameters;
  private double[] f3DParameters;

  // Flag indicating whether the user has canceled the animation between one update and the next
  private boolean fCanceled;

  // List of views that will be animated when the animation is started
  private List<ILspView> fViews;

  // List of listeners that are notified when the animation starts and stops
  private List<AnimationListener> fListeners;

  // Animation duration in seconds
  private double fDuration;

  // The theme associated to this animation
  private AbstractTheme fTheme;

  private ALcdAnimation.Interpolator fInterpolator;

  /**
   * Creates a theme animation for the theme with given name.
   *
   * @param aTheme     the theme associated to this animation
   * @param aViews     the views that are to be animated
   */
  public ThemeAnimation(AbstractTheme aTheme, List<ILspView> aViews) {
    this(aTheme.getName(), aViews);
    fTheme = aTheme;
  }

  public ThemeAnimation(String aThemeName, List<ILspView> aViews) {
    this(aThemeName, aViews, new EaseOutInterpolator(), Float.parseFloat(Framework.getInstance().getProperty("camera.transition.duration", "3")));
  }

  /**
   * Creates a theme animation for the theme with given name.
   *
   * @param aTheme     the theme associated to this animation
   * @param aViews     the views that are to be animated
   * @param aInterpolator interpolator used for the animations
   * @param aDuration  the duration of the theme animation in seconds
   */
  public ThemeAnimation(AbstractTheme aTheme, List<ILspView> aViews, ALcdAnimation.Interpolator aInterpolator, double aDuration) {
    this(aTheme.getName(), aViews, aInterpolator, aDuration);
    fTheme = aTheme;
  }

  public ThemeAnimation(String aThemeName, List<ILspView> aViews, ALcdAnimation.Interpolator aInterpolator, double aDuration) {
    fInterpolator = aInterpolator;
    fDuration = aDuration;
    fCanceled = false;
    fViews = aViews;
    fListeners = new ArrayList<AnimationListener>();

    fSource3D = get3DCameraSourceName(aThemeName);
    fFile3D = get3DCameraFile(aThemeName);
    fFile3DLastModified = fFile3D.lastModified();
    f3DParameters = CameraFileUtil.read3DCamera(fSource3D);

    fSource2D = get2DCameraSourceName(aThemeName);
    fFile2D = get2DCameraFile(aThemeName);
    fFile2DLastModified = fFile2D.lastModified();
    f2DParameters = CameraFileUtil.read2DCamera(fSource2D);
  }

  public AbstractTheme getTheme() {
    return fTheme;
  }

  public static File get2DCameraFile(AbstractTheme aTheme) {
    return get2DCameraFile(aTheme.getName());
  }

  public static File get3DCameraFile(AbstractTheme aTheme) {
    return get3DCameraFile(aTheme.getName());
  }

  public static String get2DCameraSourceName(String aThemeName) {
    String cameraFileName = aThemeName.replace("_", "__").replace(" ", "_");
    return IOUtil.getSourceName(getCameraFileDir(), cameraFileName + "_2D_Camera.txt");
  }

  public static File get2DCameraFile(String aThemeName) {
    String cameraFileName = aThemeName.replace("_", "__").replace(" ", "_");
    return IOUtil.getFile(getCameraFileDir(), cameraFileName + "_2D_Camera.txt");
  }

  public static String get3DCameraSourceName(String aThemeName) {
    String cameraFileName = aThemeName.replace("_", "__").replace(" ", "_");
    return IOUtil.getSourceName(getCameraFileDir(), cameraFileName + "_3D_Camera.txt");
  }

  public static File get3DCameraFile(String aThemeName) {
    String cameraFileName = aThemeName.replace("_", "__").replace(" ", "_");
    return IOUtil.getFile(getCameraFileDir(), cameraFileName + "_3D_Camera.txt");
  }

  public static String getCameraFileDir() {
    return Framework.getInstance().getProperty("camera.config.path", "");
  }

  public void addAnimationListener(AnimationListener aListener) {
    fListeners.add(aListener);
  }

  public void removeAnimationListener(AnimationListener aListener) {
    fListeners.remove(aListener);
  }

  private boolean needsUpdate2D() {
    return fFile2D.lastModified() > fFile2DLastModified;
  }

  private boolean needsUpdate3D() {
    return fFile3D.lastModified() > fFile3DLastModified;
  }

  public void doAnimation() {
    if (fViews.isEmpty()) {
      sLogger.info("Can not start theme animation, reason: no views to animate");
      return;
    }
    if (needsUpdate2D()) {
      f2DParameters = CameraFileUtil.read2DCamera(fSource2D);
    }
    if (needsUpdate3D()) {
      f3DParameters = CameraFileUtil.read3DCamera(fSource3D);
    }

    boolean animationSubmitted = false;
    for (ILspView v : fViews) {
      if (v.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation2D) {

        if (canDoAnimatedFit(v)) {
          CameraAnimation2D animation = new CameraAnimation2D(this, v, fInterpolator, fDuration);
          animation.setGoalWorldOrigin(new TLcdXYPoint(f2DParameters[0], f2DParameters[1]));
          animation.setGoalViewOrigin(f2DParameters[2], f2DParameters[3]);
          animation.setGoalScale(f2DParameters[4], f2DParameters[5]);
          animation.setGoalRotation(f2DParameters[6]);
          ALcdAnimationManager.getInstance().putAnimation(
              v.getViewXYZWorldTransformation(),
              animation
          );
          animationSubmitted = true;
        }
      } else {
        if (f3DParameters != null) {
          if (v.getXYZWorldReference() instanceof TLcdGeocentricReference) {
            GeocentricCameraAnimation3D animation = new GeocentricCameraAnimation3D(this, v, fInterpolator, fDuration);
            animation.setGoalLocation(new TLcdXYZPoint(f3DParameters[0],
                                                       f3DParameters[1],
                                                       f3DParameters[2]));
            animation.setGoalDistance(f3DParameters[3]);
            animation.setGoalPitch(f3DParameters[4]);
            animation.setGoalYaw(f3DParameters[5]);
            ALcdAnimationManager.getInstance().putAnimation(v.getViewXYZWorldTransformation(),
                                                            animation);
            animationSubmitted = true;
          } else {
            CartesianCameraAnimation3D animation = new CartesianCameraAnimation3D(this, v, fInterpolator, fDuration);
            animation.setGoalLocation(new TLcdXYZPoint(f3DParameters[0],
                                                       f3DParameters[1],
                                                       f3DParameters[2]));
            animation.setGoalDistance(f3DParameters[3]);
            animation.setGoalPitch(f3DParameters[4]);
            animation.setGoalYaw(f3DParameters[5]);
            ALcdAnimationManager.getInstance().putAnimation(v.getViewXYZWorldTransformation(),
                                                            animation);
            animationSubmitted = true;
          }
        }
      }
    }
    if (!animationSubmitted) {
      stop();
    }
  }

  /**
   * Checks whether we can do an animated fit.
   *
   * This is the case under the following conditions:
   * <ul>
   * <li>if camera parameters are provided</li>
   * </ul>
   */
  private boolean canDoAnimatedFit(ILspView v) {
    if (f2DParameters == null) {
      return false;
    }
    if (!(v.getXYZWorldReference() instanceof ILcdGridReference)) {
      return true;
    }
    ILcdGridReference gridReference = (ILcdGridReference) v.getXYZWorldReference();
    return (gridReference.getProjection() instanceof TLcdEquidistantCylindrical);
  }

  public boolean isCanceled() {
    return fCanceled;
  }

  public void setCanceled(boolean aCanceled) {
    fCanceled = aCanceled;
  }

  public double getDuration() {
    return fDuration;
  }

  public void start() {
    Set<AnimationListener> listeners = new HashSet<AnimationListener>(fListeners);
    for (AnimationListener listener : listeners) {
      listener.animationStarted();
    }
  }

  public void stop() {
    if (!isCanceled()) {
      Set<AnimationListener> listeners = new HashSet<AnimationListener>(fListeners);
      for (AnimationListener listener : listeners) {
        listener.animationStopped();
      }
    }
    setCanceled(false);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Interface that should be implemented by objects that need to be notified when the theme
   * animation
   * has started and/or stopped.
   */
  public static interface AnimationListener {

    /**
     * Callback method for when the animation started.
     */
    void animationStarted();

    /**
     * Callback method for when the animation stopped.
     */
    void animationStopped();

  }

  private static class EaseOutInterpolator implements ALcdAnimation.Interpolator {
    @Override
    public double transform(double aTime) {
      return 1 - Math.pow(1 - aTime, 2);
    }
  }
}
