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

/**
 * Describes a "Z" gesture - several connected line segments.
 */
public class ZGesture extends AWTEvent implements IGesture {
  private int fSegmentCount;  // How many segments we are looking for.
  private double fLocationX;  // The center of "Z" shape.
  private double fLocationY;

  private double[] fSegmentLength;    // Length of each segment in pixels.
  private double[] fSegmentLocationX; // Center of each segment.
  private double[] fSegmentLocationY;
  private double[] fDirection;        // Direction of each segment.

  /**
   * The AWT identifier of this event type.
   */
  public static final int EventID = RESERVED_ID_MAX + 21232;

  public ZGesture(Object aSource, int aSegmentCount) {
    super(aSource, EventID);

    fSegmentCount = aSegmentCount;
    fSegmentLength = new double[aSegmentCount];
    fSegmentLocationX = new double[aSegmentCount];
    fSegmentLocationY = new double[aSegmentCount];
    fDirection = new double[aSegmentCount];
  }

  public int getSegmentCount() {
    return fSegmentCount;
  }

  public void setSegmentCount(int aSegmentCount) {
    fSegmentCount = aSegmentCount;
  }

  public double getSegmentLength(int aSegmentIndex) {
    return fSegmentLength[aSegmentIndex];
  }

  public void setSegmentLength(int aSegmentIndex, double aSegmentLength) {
    fSegmentLength[aSegmentIndex] = aSegmentLength;
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

  public double getSegmentLocationX(int aSegmentIndex) {
    return fSegmentLocationX[aSegmentIndex];
  }

  public void setSegmentLocationX(int aSegmentIndex, double aSegmentLocationX) {
    fSegmentLocationX[aSegmentIndex] = aSegmentLocationX;
  }

  public double getSegmentLocationY(int aSegmentIndex) {
    return fSegmentLocationY[aSegmentIndex];
  }

  public void setSegmentLocationY(int aSegmentIndex, double aSegmentLocationY) {
    fSegmentLocationY[aSegmentIndex] = aSegmentLocationY;
  }

  public double getDirection(int aSegmentIndex) {
    return fDirection[aSegmentIndex];
  }

  public void setDirection(int aSegmentIndex, double aDirection) {
    fDirection[aSegmentIndex] = aDirection;
  }
}
