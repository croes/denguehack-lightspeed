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
package samples.lightspeed.common;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.geometry.cartesian.TLcdCartesian;

/**
 * UI component that allows the user to set an angle in a user friendly manner.
 */
public class AngleControlComponent extends JComponent {

  private static final double EPSILON = 1e-6;

  private double fMinimumAngle;
  private double fMaximumAngle;
  private double fAngle;

  private boolean fAdjusting;
  private Type fType;

  private CopyOnWriteArraySet<ChangeListener> fListeners = new CopyOnWriteArraySet<ChangeListener>();
  private ChangeEvent fChangeEvent = new ChangeEvent(this);

  public AngleControlComponent(Type aType, double aMinimumAngle, double aMaximumAngle) {
    if (aType == null) {
      throw new IllegalArgumentException();
    }
    if (aMinimumAngle > aMaximumAngle) {
      throw new IllegalArgumentException("Minimum angle must be smaller than maximum angle");
    }

    fType = aType;
    fMinimumAngle = aMinimumAngle;
    fMaximumAngle = aMaximumAngle;
    updateUI();
  }

  public void updateUI() {
    if (fType.equals(Type.SEGMENT)) {
      setUI(new AngleSegmentControlUI());
    } else if (fType.equals(Type.FULL)) {
      setUI(new FullAngleControlUI());
    }
  }

  /**
   * Returns the currently selected angle.
   * @return the currently selected angle
   */
  public double getAngle() {
    return fAngle;
  }

  /**
   * Sets the angle.
   *
   * @param aAngle the angle to set in degrees.
   */
  public void setAngle(double aAngle) {
    if (!equal(fAngle, aAngle)) {
      if (aAngle < fMinimumAngle || aAngle > fMaximumAngle) {
        // Clamp to the closest angle
        double a = TLcdCartesian.normalizeAngle(aAngle);
        double min = TLcdCartesian.normalizeAngle(fMinimumAngle);
        double max = TLcdCartesian.normalizeAngle(fMaximumAngle);
        double deltaMin = Math.abs(a - min) % 360.0;
        deltaMin = Math.min(deltaMin, 360.0 - deltaMin);
        double deltaMax = Math.abs(a - max) % 360.0;
        deltaMax = Math.min(deltaMax, 360.0 - deltaMax);
        if (deltaMin < deltaMax) {
          aAngle = fMinimumAngle;
        } else {
          aAngle = fMaximumAngle;
        }
      }
      fAngle = aAngle;
      fireChangeEvent();
    }
  }

  /**
   * Returns the minimum angle.
   * @return the minimum angle
   */
  public double getMinimumAngle() {
    return fMinimumAngle;
  }

  /**
   * Sets the minimum angle.
   *
   * @param aMinimumAngle the minimum angle
   */
  public void setMinimumAngle(double aMinimumAngle) {
    if (!equal(fMinimumAngle, aMinimumAngle)) {
      fMinimumAngle = aMinimumAngle;
      fireChangeEvent();
    }
  }

  /**
   * Returns the maximum angle.
   * @return the maximum angle
   */
  public double getMaximumAngle() {
    return fMaximumAngle;
  }

  /**
   * Sets the maximum angle.
   *
   * @param aMaximumAngle the maximum angle
   */
  public void setMaximumAngle(double aMaximumAngle) {
    if (!equal(fMaximumAngle, aMaximumAngle)) {
      fMaximumAngle = aMaximumAngle;
      fireChangeEvent();
    }
  }

  public void addChangeListener(ChangeListener aListener) {
    fListeners.add(aListener);
  }

  public void removeChangeListener(ChangeListener aListener) {
    fListeners.remove(aListener);
  }

  public boolean isAdjusting() {
    return fAdjusting;
  }

  public void setAdjusting(boolean aAdjusting) {
    if (fAdjusting != aAdjusting) {
      fAdjusting = aAdjusting;
      fireChangeEvent();
    }
  }

  protected void fireChangeEvent() {
    for (ChangeListener listener : fListeners) {
      listener.stateChanged(fChangeEvent);
    }
  }

  static boolean equal(double aValue1, double aValue2) {
    return (aValue1 >= aValue2 - EPSILON && aValue1 <= aValue2 + EPSILON) ||
           (aValue2 >= aValue1 - EPSILON && aValue2 <= aValue1 + EPSILON);
  }

  /**
   * An angle type.
   */
  public static enum Type {
    /**
     * An angle that is valid over an arc segment. Can for example be used for a vertical angle
     * (e.g. "tilt").
     */
    SEGMENT,
    /**
     * An angle that is valid over the full 360 degree range. Can for example be used for an angle
     * in the ground plane (e.g. "heading").
     */
    FULL
  }

}
