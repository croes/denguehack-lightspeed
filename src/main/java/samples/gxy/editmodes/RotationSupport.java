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
package samples.gxy.editmodes;

import java.awt.Graphics;
import java.awt.Point;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdContrastIcon;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.ILcdCloneable;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;

/**
 * Helps adding rotation to painter/editors; it offers methods for rotating shapes and for drawing
 * rotation handles. To complete this class, the following abstract methods must be overridden for a
 * concrete object type:
 * <ul>
 *   <li>{@link #rotationCenterSFCT}, determining the center around which
 *       to rotate objects;</li>
 *   <li>{@link #retrieveObjectRotation(Object)}, retrieving the current
 *       object rotation angle;</li>
 *   <li>{@link #rotateObject(Object, double, ILcdGXYContext)},
 *       performing the actual rotation on the model object.</li>
 * </ul>
 */
public abstract class RotationSupport implements ILcdCloneable {

  private Point fTempAWTPoint = new Point();
  private TLcdXYPoint fTempWorldPoint = new TLcdXYPoint();
  private TLcdXYZPoint fTempModelPoint = new TLcdXYZPoint();
  private final ILcdIcon fRotateHandle;

  protected RotationSupport() {
    ILcdIcon icon = TLcdIconFactory.create(TLcdIconFactory.LOOP_ICON);
    fRotateHandle = new TLcdContrastIcon(icon, 1, 4); // brightens the loop icon
  }

  /**
   * Returns the rotation center of the given object in model coordinates. The default
   * implementation returns the object's focus punt.
   *
   * @param aObject     the object whose rotation center to return
   * @param aCenterSFCT the point into which the location of the rotation center is stored
   */
  protected abstract void rotationCenterSFCT(Object aObject, ILcd2DEditablePoint aCenterSFCT);

  /**
   * Rotates the object clockwise with the given angle around the location given by {@link
   * #rotationCenterSFCT}.
   *
   * @param aObject        the object to rotate
   * @param aRotationAngle the rotation angle to rotate, in degrees
   * @param aGXYContext    the painting context
   */
  public abstract void rotateObject(Object aObject, double aRotationAngle, ILcdGXYContext aGXYContext);

  /**
   * Returns the clockwise rotation angle of the object.
   *
   * @param aObject the object whose rotation to retrieve
   *
   * @return the rotation angle of the object, in degrees
   */
  protected abstract double retrieveObjectRotation(Object aObject);

  /**
   * Returns whether the rotation handle is touched.
   *
   * @param aPaintMode  the render mode
   * @param aGXYContext the controller context
   * @param aObject     the object whose rotation handle to check
   *
   * @return true if the rotation handle is touched, false otherwise.
   */
  public boolean isRotationHandleTouched(int aPaintMode, ILcdGXYContext aGXYContext, Object aObject) {
    ILcdBounds bounds = ((ILcdBounded) aObject).getBounds();
    double bounds_height = bounds.getHeight();
    double bounds_width = bounds.getWidth();

    ILcdPoint modelPoint1 = bounds.getLocation();
    ILcd2DEditablePoint modelPoint2 = modelPoint1.cloneAs2DEditablePoint();
    ILcd2DEditablePoint modelPoint3 = modelPoint1.cloneAs2DEditablePoint();
    ILcd2DEditablePoint modelPoint4 = modelPoint1.cloneAs2DEditablePoint();
    modelPoint2.move2D(modelPoint1.getX() + bounds_width, modelPoint1.getY());
    modelPoint3.move2D(modelPoint1.getX(), modelPoint1.getY() + bounds_height);
    modelPoint4.move2D(modelPoint1.getX() + bounds_width, modelPoint1.getY() + bounds_height);

    try {
      Point[] awtPoints = {new Point(), new Point(), new Point(), new Point()};
      ILcdPoint[] modelPoints = {modelPoint1, modelPoint2, modelPoint3, modelPoint4};

      for (int i = 0; i < awtPoints.length; i++) {
        modelPoint2ViewAWTPointSFCT(modelPoints[i], awtPoints[i], aGXYContext);

        if (isTouched(awtPoints[i], aGXYContext)) {
          return true;
        }
      }

      return false;
    } catch (TLcdOutOfBoundsException e) {
      return false;
    }
  }

  // transforms a model point to an awt point on the view
  private void modelPoint2ViewAWTPointSFCT(ILcdPoint aModelPoint, Point aAWTPoint, ILcdGXYContext aGXYContext) throws TLcdOutOfBoundsException {
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();

    TLcdXYPoint worldPoint = new TLcdXYPoint();
    mwt.modelPoint2worldSFCT(aModelPoint, worldPoint);
    vwt.worldPoint2viewAWTPointSFCT(worldPoint, aAWTPoint);
  }

  // verifies whether the mouse hovers over the awt point
  private boolean isTouched(Point aAWTPoint, ILcdGXYContext aGXYContext) {
    int x_orig = aGXYContext.getX() - aGXYContext.getDeltaX();
    int y_orig = aGXYContext.getY() - aGXYContext.getDeltaY();
    double x = aAWTPoint.getX();
    double y = aAWTPoint.getY();

    return (Math.abs(x - x_orig) < aGXYContext.getGXYPen().getHotPointSize() &&
            Math.abs(y - y_orig) < aGXYContext.getGXYPen().getHotPointSize());
  }

  /**
   * Draws the given object's rotation handles. The handles consists of 4 rotation images, one for
   * each corner of the object's bounding box.
   *
   * @param aGraphics   the graphics to draw onto
   * @param aPaintMode  the paint mode to consider
   * @param aGXYContext the controller context
   * @param aObject     the object whose handle to draw
   */
  public void drawRotationHandles(Graphics aGraphics, int aPaintMode, ILcdGXYContext aGXYContext, Object aObject) {
    ILcd2DEditableBounds bounds = new TLcdXYBounds();

    try {

      aGXYContext.getGXYLayer().getGXYPainter(aObject).boundsSFCT(aGraphics, ILcdGXYPainter.SELECTED, aGXYContext, bounds);
      int x = (int) bounds.getLocation().getX();
      int y = (int) bounds.getLocation().getY();
      int x_width = (int) bounds.getWidth();
      int y_height = (int) bounds.getHeight();

      int image_width = fRotateHandle.getIconWidth();
      int image_height = fRotateHandle.getIconHeight();

      fRotateHandle.paintIcon(null, aGraphics,
                              x - image_width / 2,
                              y - image_height / 2
                             );

      fRotateHandle.paintIcon(null, aGraphics,
                              x - image_width / 2 + x_width,
                              y - image_height / 2
                             );

      fRotateHandle.paintIcon(null, aGraphics,
                              x - image_width / 2,
                              y - image_height / 2 + y_height
                             );

      fRotateHandle.paintIcon(null, aGraphics,
                              x - image_width / 2 + x_width,
                              y - image_height / 2 + y_height
                             );
    } catch (TLcdNoBoundsException e) {
      // there are no bounds, hence we can't draw our bounded box handle
      // do nothing
    }
  }

  /**
   * Rotates the given object according to the angle between the mouse pressed position and the
   * current mouse position. The rotation center is computed by {@link #rotationCenterSFCT}.
   *
   * @param aObject     the object to rotate
   * @param aGXYContext the controller's context
   *
   * @return true if the rotation succeeded, false otherwise
   */
  public boolean rotateObject(Object aObject, ILcdGXYContext aGXYContext) {
    try {
      double rotation_angle;
      rotation_angle = calculateRotationAngle(aGXYContext, aObject);
      rotateObject(aObject, rotation_angle, aGXYContext);
      return true;
    } catch (TLcdOutOfBoundsException e) {
      // no rotation possible
      return false;
    }
  }

  /**
   * Rotates the given model point around the given center.
   *
   * @param aCenter     the center around which to rotate
   * @param aAngle      rotation angle, in degrees clockwise
   * @param aPointSFCT  the point to rotate
   * @param aGXYContext the painting context
   */
  public void rotatePoint(ILcdPoint aCenter, double aAngle, ILcd2DEditablePoint aPointSFCT, ILcdGXYContext aGXYContext) {
    ILcdModelReference model_reference = aGXYContext.getGXYLayer().getModel().getModelReference();
    if (model_reference instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodetic_reference = (ILcdGeodeticReference) model_reference;
      ILcdEllipsoid ellipsoid = geodetic_reference.getGeodeticDatum().getEllipsoid();
      double currentAzimuth = TLcdConstant.RAD2DEG * ellipsoid.forwardAzimuth2D(aCenter, aPointSFCT);
      ellipsoid.geodesicPointSFCT(aCenter, ellipsoid.geodesicDistance(aCenter, aPointSFCT), currentAzimuth + aAngle, aPointSFCT);
    } else {
      aPointSFCT.move2D(aPointSFCT.getX() - aCenter.getX(), aPointSFCT.getY() - aCenter.getY());
      double sin = Math.sin(aAngle * TLcdConstant.DEG2RAD);
      double cos = Math.cos(aAngle * TLcdConstant.DEG2RAD);
      double x = aPointSFCT.getX();
      double y = aPointSFCT.getY();
      aPointSFCT.move2D(x * cos + y * sin, y * cos - x * sin);
      aPointSFCT.move2D(aPointSFCT.getX() + aCenter.getX(), aPointSFCT.getY() + aCenter.getY());
    }
  }

  // Returns the forward azimuth between the mouse pressed and current mouse location, in degrees.
  private double calculateRotationAngle(ILcdGXYContext aGXYContext, Object aObject) throws TLcdOutOfBoundsException {

    fTempAWTPoint.move(aGXYContext.getX() - aGXYContext.getDeltaX(), aGXYContext.getY() - aGXYContext.getDeltaY());
    aGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(fTempAWTPoint, fTempWorldPoint);
    aGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempWorldPoint, fTempModelPoint);
    double mouse_pressed_x = fTempModelPoint.getX();
    double mouse_pressed_y = fTempModelPoint.getY();

    fTempAWTPoint.move(aGXYContext.getX(), aGXYContext.getY());
    aGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(fTempAWTPoint, fTempWorldPoint);
    aGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempWorldPoint, fTempModelPoint);
    double mouse_current_x = fTempModelPoint.getX();
    double mouse_current_y = fTempModelPoint.getY();

    // rotation center
    rotationCenterSFCT(aObject, fTempModelPoint);
    double rotation_center_x = fTempModelPoint.getX();
    double rotation_center_y = fTempModelPoint.getY();

    ILcdModelReference model_reference = aGXYContext.getGXYLayer().getModel().getModelReference();
    if (model_reference instanceof ILcdGeodeticReference) {
      ILcdGeodeticReference geodetic_reference = (ILcdGeodeticReference) model_reference;
      ILcdEllipsoid ellipsoid = geodetic_reference.getGeodeticDatum().getEllipsoid();

      double azimuth_pressed = ellipsoid.forwardAzimuth2D(rotation_center_x, rotation_center_y, mouse_pressed_x, mouse_pressed_y);
      double azimuth_current = ellipsoid.forwardAzimuth2D(rotation_center_x, rotation_center_y, mouse_current_x, mouse_current_y);

      return TLcdConstant.RAD2DEG * (azimuth_current - azimuth_pressed);
    } else {
      double azimuth_pressed = 90 - Math.atan2(mouse_pressed_y - rotation_center_y, mouse_pressed_x - rotation_center_x);
      double azimuth_current = 90 - Math.atan2(mouse_current_y - rotation_center_y, mouse_current_x - rotation_center_x);

      return TLcdConstant.RAD2DEG * (azimuth_current - azimuth_pressed);
    }
  }

  public Object clone() {
    try {
      RotationSupport clone = (RotationSupport) super.clone();
      clone.fTempAWTPoint = new Point();
      clone.fTempModelPoint = new TLcdXYZPoint();
      clone.fTempWorldPoint = new TLcdXYPoint();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Could not call super.clone()");
    }
  }

}

