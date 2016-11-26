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
package samples.gxy.hippodromePainter;

import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdGeoBuffer;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableShape;

/**
 * This is a <code>ILcd2dEditabeShape</code> that represents a Hippodrome-shape.
 * <p/>
 * A hippodrome is defined by two reference-points, a start point and an endpoint (the line between them forms the axis of the hippodrome), and a width that represents
 * the radius of the two side-arcs using the start- or endpoint as their center. Based on this essential information four contour points, lying on the outline of the hippodrome, can be retrieved.
 * Also the azimuths, describing the rotation of the hippodrome-angle can be retrieved.
 * <p/>
 * This class is for sample purposes only.
 * If you need to model this shape, please refer to our {@link ILcdGeoBuffer} implementations.
 */
public interface IHippodrome extends ILcd2DEditableShape {

  /**
   * Constant for the contour-point located <code>getStartEndAzimuth() + 90</code> degrees above the start point of the hippodrome.
   */
  static final int START_UPPER_POINT = 0;
  /**
   * Constant for the contour-point located <code>getStartEndAzimuth() - 90</code> degrees above the start point of the hippodrome.
   */
  static final int START_LOWER_POINT = 1;
  /**
   * Constant for the contour-point located <code>getEndStartAzimuth() + 90</code> degrees above the end point of the hippodrome.
   */
  static final int END_UPPER_POINT = 2;
  /**
   * Constant for the contour-point located <code>getEndStartAzimuth() - 90</code> degrees above the end point of the hippodrome.
   */
  static final int END_LOWER_POINT = 3;
  /**
   * Constant for the start point of the hippodrome.
   */
  static final int START_POINT = 4;
  /**
   * Constant for the end point of the hippodrome.
   */
  static final int END_POINT = 5;


  /**
   * Gets the start point of this hippodrome.
   *
   * @return a reference to the start point.
   */
  ILcdPoint getStartPoint();

  /**
   * Gets the end point of this hippodrome.
   *
   * @return a reference to the end point.
   */
  ILcdPoint getEndPoint();

  /**
   * Retrieves the width used as radius for the arcs of this hippodrome.
   *
   * @return the width in meters.
   */
  double getWidth();


  /**
   * Gets the contour point of this hippodrome defined by <code>aContourPoint</code>.
   *
   * @param aContourPoint possible values: <code>START_UPPER_POINT</code>, <code>START_LOWER_POINT</code>, <code>END_UPPER_POINT</code> or <code>END_LOWER_POINT</code>.
   * @return a reference to the requested contourPoint.
   */
  ILcdPoint getContourPoint(int aContourPoint);

  /**
   * Sets the width used as radius for the arcs of this hippodrome. The shape of this hippodrome will be adjusted accordingly.
   *
   * @param aWidth the width in meters.
   */
  void setWidth(double aWidth);

  /**
   * Gets the forward azimuth (direction: from startPoint to endPoint) of the axis of this hippodrome.
   * This value can be different from {@link #getEndStartAzimuth()} since computations are now always made in a plane.
   *
   * @return the forward azimuth in degrees
   */
  double getStartEndAzimuth();

  /**
   * Gets the backward azimuth (direction: from endPoint to startPoint) of the axis of this hippodrome.
   * See the remark at {@link #getStartEndAzimuth()}.
   *
   * @return the backward azimuth in degrees
   */
  double getEndStartAzimuth();

  /**
   * Moves the reference-point indicated by <code>aReferencePoint</code> to <code>aPoint</code>.
   *
   * @param aPoint      the point to move the requested reference-point to. The coordinates of this point have to be expressed in the same model-reference as this hippodrome.
   * @param aReferencePoint possible values: <code>START_POINT</code> or <code>END_POINT</code>
   */
  void moveReferencePoint(ILcdPoint aPoint, int aReferencePoint);

  /**
   * Moves this hippodrome to the location <code>aX</code>,<code>aY</code> by moving the start- or endpoint (determined by
   * <code>aMoveStartPoint</code> and keeping the other reference-point at the same distance.
   *
   * @param aX              X-coordinate of point to move to.
   * @param aY              Y-coordinate of point to move to.
   * @param aMoveStartPoint if <code>true</code> the start point is moved to (X,Y), otherwise the endpoint is moved to (X,Y).
   */
  void move2D(double aX, double aY, boolean aMoveStartPoint);


  /**
   * Retrieves a composite curve representing the outline of the hippodrome.
   * @return a composite curve representing the outline of the hippodrome
   */
  ILcdCompositeCurve getOutline();
}

