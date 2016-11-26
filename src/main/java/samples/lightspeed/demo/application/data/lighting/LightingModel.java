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
package samples.lightspeed.demo.application.data.lighting;

import java.awt.Color;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.services.effects.TLspAmbientLight;
import com.luciad.view.lightspeed.services.effects.TLspAtmosphere;
import com.luciad.view.lightspeed.services.effects.TLspDirectionalLight;
import com.luciad.view.lightspeed.services.effects.TLspGraphicsEffects;
import com.luciad.view.lightspeed.services.effects.TLspHeadLight;

/**
 * Changes the lighting of an {@link ILspView}.
 */
public class LightingModel {
  private static final Color AMBIENT_LIGHT_COLOR = new Color(32, 64, 96);
  public static final Color SUN_COLOR = new Color(255, 230, 153);

  private final TLspAtmosphere fAtmosphere;
  private final TLspAmbientLight fAmbientLight;
  private final TLspDirectionalLight fTimeOfDayLight;
  private final TLspHeadLight fAutoLight;
  private final Color fOriginalSkyColor;

  private Mode fMode;

  /**
   * Creates a new lighting model.
   *
   * @param aView the view whose lighting should be controlled by this model
   */
  public LightingModel(ILspView aView) {
    // An ambient light avoids making faces that are not oriented towards the light completely black
    fAmbientLight = new TLspAmbientLight(AMBIENT_LIGHT_COLOR);
    // A directional light can be used to simulate the sun in a geocentric view
    fTimeOfDayLight = new TLspDirectionalLight(SUN_COLOR, new TLcdXYZPoint(1, 1, 1));
    // A head light automatically follows the camera
    fAutoLight = new TLspHeadLight(aView, SUN_COLOR);


    fAtmosphere = (TLspAtmosphere) aView.getServices().getGraphicsEffects().getEffectsByType(TLspAtmosphere.class).toArray()[0];
    fOriginalSkyColor = fAtmosphere.getSkyColor();

    setMode(Mode.AUTO);
  }

  public void activate(ILspView aView) {
    TLspGraphicsEffects fx = aView.getServices().getGraphicsEffects();
    fx.add(fAmbientLight);
    fx.add(fAutoLight);
    fx.add(fTimeOfDayLight);
  }

  /**
   * Returns the current lighting mode.
   *
   * @return the current lighting mode, never {@code null}
   */
  public Mode getMode() {
    return fMode;
  }

  /**
   * Sets the current lighting mode.
   *
   * @param aMode the current lighting mode, never {@code null}
   */
  public void setMode(Mode aMode) {
    if (aMode == null) {
      throw new NullPointerException("Lighting mode");
    }
    fMode = aMode;
    fAmbientLight.setEnabled(aMode != Mode.OFF);
    fAutoLight.setEnabled(aMode == Mode.AUTO);
    fTimeOfDayLight.setEnabled(aMode == Mode.TIME_OF_DAY);

    // Restore default color
    fAmbientLight.setColor(AMBIENT_LIGHT_COLOR);

    // Restore default atmosphere color
    fAtmosphere.setSkyColor(fOriginalSkyColor);
    fAtmosphere.setHorizonColor(Color.white);
  }

  /**
   * Sets the orientation of the automatic light.
   *
   * @param aYawOffset the yaw offset in degrees
   * @param aPitch     the pitch in degrees
   *
   * @see TLspHeadLight#getYawOffset()
   * @see TLspHeadLight#getPitch()
   */
  public void setAutoLightOrientation(double aYawOffset, double aPitch) {
    fAutoLight.setYawOffset(aYawOffset);
    fAutoLight.setPitch(aPitch);
  }

  /**
   * Sets the orientation of the time-of-day light.
   *
   * @param aTimeOfDay the GMT hour of the day in {@code [0, 24]}
   */
  public void setTimeOfDayLightOrientation(double aTimeOfDay) {
    fTimeOfDayLight.setDirectionVector(computeGeocentricSunDirection(aTimeOfDay));
  }

  /**
   * Computes the approximate geocentric direction towards the sun as specified <a
   * href="http://aa.usno.navy.mil/faq/docs/SunApprox.php">here</a>.
   *
   * @param aHourOfDay the GMT hour of the day in {@code [0, 24]}
   *
   * @return the direction towards the sun
   */
  private static ILcdPoint computeGeocentricSunDirection(double aHourOfDay) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    long noon = calendar.getTimeInMillis();
    calendar.set(2000, Calendar.JANUARY, 1, 12, 0);
    long greenwichNoon = calendar.getTimeInMillis();
    // The number of days (positive or negative) since Greenwich noon, Terrestrial Time, on 1 January 2000 (J2000.0).
    double D = (double) (noon - greenwichNoon) / ((double) TimeUnit.DAYS.toMillis(1));
    // The mean anomaly of the Sun (actually, of the Earth in its orbit around the Sun, but it is convenient to pretend the Sun orbits the Earth)
    double g = (3557.529 + 0.98560028 * D) % 360.0; // degrees
    // The mean longitude of the Sun
    double q = (280.459 + 0.98564736 * D) % 360.0; // degrees
    // The ecliptic longitude of the Sun
    double L = q + 1.915 * Math.sin(Math.toRadians(g)) + 0.020 * Math.sin(2 * Math.toRadians(g)); // degrees
    // Mean obliquity of the ecliptic
    double epsilon = 23.439 - 0.00000036 * D; // degrees
    // Right ascension
//    double alpha = Math.toDegrees( Math.atan2( Math.cos( Math.toRadians( epsilon ) ) * Math.sin( Math.toRadians( L ) ), Math.cos( Math.toRadians( L ) ) ) );
    // Declination
    double delta = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(epsilon)) * Math.sin(Math.toRadians(L))));

    // Account for the rotation of the earth
    double alpha = (24 - aHourOfDay) / 24.0 * 360.0;

    // Compute geocentric direction
    TLcdLonLatPoint llPt = new TLcdLonLatPoint(alpha, delta);
    TLcdXYZPoint geocentricPt = new TLcdXYZPoint();
    TLcdEllipsoid.DEFAULT.llh2geocSFCT(llPt, geocentricPt);
    return geocentricPt;
  }

  /**
   * Sets the color of the ambient light.
   *
   * @param aColor the color of the ambient light
   */
  public void setAmbientLightColor(Color aColor) {
    fAmbientLight.setColor(aColor);
  }

  /**
   * Enumeration for the mode of lighting.
   */
  public static enum Mode {
    /**
     * Light that automatically follows the camera
     */
    AUTO,
    /**
     * Fixed light for a specific time of day.
     */
    TIME_OF_DAY,
    /**
     * No lighting.
     */
    OFF
  }
}
