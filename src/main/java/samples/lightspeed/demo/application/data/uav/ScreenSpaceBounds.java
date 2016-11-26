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
package samples.lightspeed.demo.application.data.uav;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;

/**
 * Represents a bounds in screen space.
 * <p>
 * Note that the bounds are defined relative to an anchor
 */
public class ScreenSpaceBounds {

  private double fRelX;
  private double fRelY;
  private double fWidth;
  private double fHeight;
  private ScreenAnchor fAnchor;

  public ScreenSpaceBounds(double aRelX, double aRelY, double aW, double aH, ScreenAnchor aAnchor) {
    fRelX = aRelX;
    fRelY = aRelY;
    fWidth = aW;
    fHeight = aH;
    fAnchor = aAnchor;
  }

  /**
   * Returns the relative horizontal position in the range [0, 1]
   *
   * @return the relative horizontal position in the range [0, 1]
   */
  public double getRelX() {
    return fRelX;
  }

  /**
   * Returns the relative vertical position in the range [0, 1]
   *
   * @return the relative vertical position in the range [0, 1]
   */
  public double getRelY() {
    return fRelY;
  }

  /**
   * Returns the absolute width in pixels.
   *
   * @return the absolute width in pixels
   */
  public double getWidth() {
    return fWidth;
  }

  /**
   * Returns the absolute height in pixels.
   *
   * @return the absolute height in pixels
   */
  public double getHeight() {
    return fHeight;
  }

  /**
   * Returns the anchor relative to which the position of the bounds
   * <code>( getRelX() , getRelY() )</code>is defined.
   *
   * @return the anchor relative to which the position of the bounds is defined
   *
   * @see ScreenAnchor
   */
  public ScreenAnchor getAnchor() {
    return fAnchor;
  }

  /**
   * Retrieves the bounds' absolute coordinates and size in screen space.
   *
   * @param aViewWidth  the width of the view in which the bounds are displayed
   * @param aViewHeight the height of the view in which the bounds are displayed
   * @param aResultSFCT the result parameter in which the absolute bounds are stored
   */
  public void retrieveAbsoluteBoundsSFCT(int aViewWidth, int aViewHeight, ILcd2DEditableBounds aResultSFCT) {
    double x = (fRelX + fAnchor.getHorizontalPosition()) * aViewWidth;
    double y = (fRelY + fAnchor.getVerticalPosition()) * aViewHeight;
    aResultSFCT.move2D(x, y);
    aResultSFCT.setWidth(fWidth);
    aResultSFCT.setHeight(fHeight);
  }

  /**
   * Returns the actual location of the given relative point <code>(aRelX,aRelY)</code>
   * in screen space coordinates. The given point is defined relative to the anchor of
   * the bounds.
   *
   * @param aViewWidth  the width of the view in which the bounds are displayed
   * @param aViewHeight the height of the view in which the bounds are displayed
   * @param aRelX       the X-coordinate of the relative point
   * @param aRelY       the Y-coordinate of the relative point
   * @param aResultSFCT the result parameter
   *
   * @see #getAnchor()
   */
  public void retrieveAbsolutePointSFCT(int aViewWidth, int aViewHeight,
                                        double aRelX, double aRelY,
                                        ILcd3DEditablePoint aResultSFCT) {
    aResultSFCT.move2D(getAbsoluteX(aViewWidth, aRelX),
                       getAbsoluteY(aViewHeight, aRelY));
  }

  private double getAbsoluteX(int aViewWidth, double aRelX) {
    return (fRelX + fAnchor.getHorizontalPosition()) * aViewWidth + fWidth * aRelX;
  }

  private double getAbsoluteY(int aViewHeight, double aRelY) {
    return (fRelY + fAnchor.getVerticalPosition()) * aViewHeight + fHeight * aRelY;
  }

  /**
   * Translates the bounds with the given amount of pixels in both X- and Y-direction.
   *
   * @param aDeltaX     the amount of pixels (i.e. screen coordinates) to move in the X-direction
   * @param aDeltaY     the amount of pixels (i.e. screen coordinates) to move in the Y-direction
   * @param aViewWidth  the width of the view in which the bounds are painted
   * @param aViewHeight the height of the view in which the bounds are painted
   */
  public void translate2D(double aDeltaX, double aDeltaY, int aViewWidth, int aViewHeight) {
    setAbsolutePosition(getAbsoluteX(aViewWidth, 0) + aDeltaX,
                        getAbsoluteY(aViewHeight, 0) + aDeltaY,
                        aViewWidth, aViewHeight);
  }

  /**
   * Sets the absolute bounds parameters of the bounds.
   *
   * @param aBounds     the absolute bounds parameters
   * @param aViewWidth  the width of the view in which the bounds are painted
   * @param aViewHeight the height of the view in which the bounds are painted
   */
  public void setAbsoluteBounds(ILcdBounds aBounds, int aViewWidth, int aViewHeight) {
    fWidth = aBounds.getWidth();
    fHeight = aBounds.getHeight();
    setAbsolutePosition(aBounds.getLocation().getX(), aBounds.getLocation().getY(), aViewWidth, aViewHeight);
  }

  /**
   * Sets the absolute position of the bounds in screen space.
   *
   * @param aX          the absolute X-coordinate
   * @param aY          the absolute Y-coordinate
   * @param aViewWidth  the width of the view in which the bounds are painted
   * @param aViewHeight the height of the view in which the bounds are painted
   */
  public void setAbsolutePosition(double aX, double aY, int aViewWidth, int aViewHeight) {
    double absCenterX = aX + fWidth / 2;
    double absCenterY = aY + fHeight / 2;
    boolean left = absCenterX <= aViewWidth / 2;
    boolean top = absCenterY <= aViewHeight / 2;
    fAnchor = ScreenAnchor.getAnchor(left, top);
    fRelX = (aX - fAnchor.getHorizontalPosition() * aViewWidth) / aViewWidth;
    fRelY = (aY - fAnchor.getVerticalPosition() * aViewHeight) / aViewHeight;
  }

  /**
   * The anchor location for the bounds.
   * <p>
   * Note that each of the enumerations is associated to two coordinates. These coordinates are
   * fractions of the view's width and height. They are bound to the range [0,1]. The actual
   * screen space location of the anchor can be calculated as follows:
   * <code>
   * ( anchor.getHorizontalPosition() * viewWidth , anchor.getVerticalPosition() * viewHeight )
   * </code>
   */
  public enum ScreenAnchor {
    TOP_LEFT(0, 0),
    TOP_RIGHT(1, 0),
    BOTTOM_LEFT(0, 1),
    BOTTOM_RIGHT(1, 1);

    private final int fHorizontalPosition;
    private final int fVerticalPosition;

    ScreenAnchor(int aHorizontalPosition, int aVerticalPosition) {
      fHorizontalPosition = aHorizontalPosition;
      fVerticalPosition = aVerticalPosition;
    }

    public int getHorizontalPosition() {
      return fHorizontalPosition;
    }

    public int getVerticalPosition() {
      return fVerticalPosition;
    }

    public static ScreenAnchor getAnchor(boolean aLeft, boolean aTop) {
      if (aLeft && aTop) {
        return ScreenAnchor.TOP_LEFT;
      }
      if (!aLeft && aTop) {
        return ScreenAnchor.TOP_RIGHT;
      }
      if (aLeft && !aTop) {
        return ScreenAnchor.BOTTOM_LEFT;
      }
      return ScreenAnchor.BOTTOM_RIGHT;
    }
  }
}
