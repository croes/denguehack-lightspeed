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

import java.lang.ref.WeakReference;

import com.luciad.util.TLcdConstant;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.lightspeed.ILspView;

/**
 * Base class for camera animations.
 */
public abstract class AbstractCameraAnimation extends ALcdAnimation {

  private static final double EARTH_CIRCUMFERENCE = 4e7; // in meters

  WeakReference<ThemeAnimation> fThemeAnimationRef;

  private ILspView fView;
  private double fStartAltitudeExaggeration;
  private double fTargetAltitudeExaggeration;

  public AbstractCameraAnimation(ThemeAnimation aThemeAnimation, ILspView aView, Interpolator aInterpolator, double aDuration) {
    super(aDuration, aView);
    fThemeAnimationRef = new WeakReference<ThemeAnimation>(aThemeAnimation);
    fTargetAltitudeExaggeration = aThemeAnimation.getTheme() == null ? 1.0 : Double.parseDouble(aThemeAnimation.getTheme().getThemeProperties().getProperty("zscale", "1.0"));
    fView = aView;
    setInterpolator(aInterpolator);
  }

  private ThemeAnimation getThemeAnimation() {
    ThemeAnimation result = fThemeAnimationRef.get();
    if (result == null) {
      throw new IllegalStateException("Theme animation has been garbage-collected");
    }
    return result;
  }

  public abstract void update(double aAlpha);

  //////////////////////////////////////////////////////////////////////////////////

  protected double getBestAngle(double aSource, double aReference) {
    double alternative = aSource - 360;
    if (Math.abs(alternative - aReference) < Math.abs(aSource - aReference)) {
      return alternative;
    } else {
      return aSource;
    }
  }

  /**
   * Calculates the value of the parabola with given parameters, for the given parameter. The
   * equation of the parabola is given by: <p/> <math> f(t) = a*t<sup>2</sup> + b*t + c </math>
   *
   * @param aA the coefficient of the quadratic term in the parabolic equation
   * @param aB the coefficient of the linear term in the parabolic equation
   * @param aC the constant term in the parabolic equation
   * @param aT the parameter
   *
   * @return the value of the parabola for the given parameter
   */
  protected static double parabolic(double aA, double aB, double aC, double aT) {
    return aA * aT * aT + aB * aT + aC;
  }

  protected double getB(double aA1, double aA2, double aD) {
    return ((4 * (getHeight(aA1, aA2, aD) - aA1)) - (aA2 - aA1)) / aD;
  }

  protected double getA(double aA1, double aA2, double aD) {
    return (aA2 - aA1 - getB(aA1, aA2, aD) * aD) / (aD * aD);
  }

  protected double getHeight(double aA1, double aA2, double aD) {
    double fraction = aD / (0.5 * EARTH_CIRCUMFERENCE);
    double height = fraction * (TLcdConstant.EARTH_RADIUS / Math.tan(22.5 * 3.141526 / 180));
    return Math.max(Math.max(aA1, aA2), height);
  }

  @Override
  public void start() {
    getThemeAnimation().start();
    fStartAltitudeExaggeration = fView.getAltitudeExaggerationFactor();
  }

  @Override
  public void stop() {
    getThemeAnimation().stop();
  }

  @Override
  public void setTimeImpl(double aTime) {
    double alpha = aTime / getDuration();
    fView.setAltitudeExaggerationFactor((1 - alpha) * fStartAltitudeExaggeration + alpha * fTargetAltitudeExaggeration);
    update(alpha);
  }
}
