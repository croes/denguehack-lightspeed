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
package samples.gxy.touch.gestures;

import java.awt.AWTEvent;
import java.awt.HeadlessException;
import java.awt.Toolkit;

import com.luciad.util.TLcdConstant;

/**
 * A flick gesture is a rapid and rectilinear one-finger movement over a short distance.
 */
public class FlickGesture extends AWTEvent implements IGesture {

  // Center of the line segment.
  private double fLocationX;
  private double fLocationY;

  // Length of the segment in pixels.
  private double fLength;

  // Angle of the line segment (trigonometric direction, in degrees)
  private double fDirection;

  /**
   * The AWT identifier of this event type.
   */
  public static final int EventID = RESERVED_ID_MAX + 21231;

  public FlickGesture(Object aSource,
                      double aLocationX,
                      double aLocationY,
                      double aLength,
                      double aDirection) {
    super(aSource, EventID);
    fLocationX = aLocationX;
    fLocationY = aLocationY;
    fLength = aLength;
    fDirection = aDirection;
  }

  public double getLocationX() {
    return fLocationX;
  }

  public void setLocationX(double aLocationX) {
    fLocationX = aLocationX;
  }

  public double getLocationY() {
    return fLocationY;
  }

  public void setLocationY(double aLocationY) {
    fLocationY = aLocationY;
  }

  public double getLength() {
    return fLength;
  }

  public void setLength(double aLength) {
    fLength = aLength;
  }

  public double getDirection() {
    return fDirection;
  }

  public void setDirection(double aDirection) {
    fDirection = aDirection;
  }

  public String toString() {
    int dots_per_inch;
    try {
      dots_per_inch = Toolkit.getDefaultToolkit().getScreenResolution();
    } catch (HeadlessException ex) {
      dots_per_inch = 90;  // Safe value
    }

    return "FlickGesture{" +
           "\n fLocationX   =" + fLocationX +
           "\n fLocationY   =" + fLocationY +
           "\n fLength (cm) =" + fLength * TLcdConstant.I2CM / dots_per_inch +
           "\n fDirection   =" + fDirection +
           '}';
  }
}
