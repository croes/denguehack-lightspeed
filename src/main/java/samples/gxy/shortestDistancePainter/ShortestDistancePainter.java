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
package samples.gxy.shortestDistancePainter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.text.NumberFormat;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYEditorProvider;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

/**
 * This is the painter-editor for ShortestDistanceShape objects.
 * <p/>
 * The object ShortestDistanceShape is a composition of a polyline
 * and a point with as extra logic that this is used for representation of
 * the shortest distance path of the point to the polyline.
 * Existing painters for the composed shape object are used for drawing.
 * For drawing the shortest distance path of the point to the geodesic polyline
 * the utility ILcdGXYPen is used.
 * <p/>
 * The painter-editor allows to move the ILcdPoint object of the
 * ShortestDistanceShape object to any location while constantly showing
 * the shortest distance path of this ILcdPoint to the geodesic
 * polyline.
 * <p/>
 * Also while reshaping the polyline the shortest distance path is constantly shown.
 */

class ShortestDistancePainter
    extends ALcdGXYPainter
    implements ILcdGXYPainter, ILcdGXYPainterProvider, ILcdGXYEditor, ILcdGXYEditorProvider {

  // the current object (shape) to use for painting/editing
  private ShortestDistanceShape fObject;

  // polyline painter : used to delegate painting/editing the polyline part of the ShortestDistanceShape
  private TLcdGXYPointListPainter fPolylinePainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
  // icon painter : used to delegate painting/editing the point part of the ShortestDistanceShape
  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();

  // cursor shown if the object would be reshaped
  public static Cursor cursorReshaping = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

  // variables used for intermediate calculations
  private TLcdXYZPoint fTempModelPoint = new TLcdXYZPoint();
  private TLcdLonLatPoint fShortestPoint = new TLcdLonLatPoint();

  public ShortestDistancePainter() {
    // we don't want the polyline painter to use caching
    fPolylinePainter.setPaintCache(false);
    fPolylinePainter.setLineStyle(new TLcdGXYPainterColorStyle(Color.WHITE));
  }

  /**
   * Sets the object aObject to be drawn by the paint method.
   * Calls the method setObject for each of the painters used
   * for the composing objects.
   * <p/>
   * The object aObject has to be an ShortestDistanceShape object.
   */
  public void setObject(Object aObject) {
    fObject = (ShortestDistanceShape) aObject;
    fPolylinePainter.setObject(fObject.getPolyline());
    fIconPainter.setObject(fObject.getPoint());
  }

  /**
   * Returns the current ShortestDistanceShape to be painted.
   *
   * @return the current ShortestDistanceShape to be painted.
   */
  public Object getObject() {
    return fObject;
  }

  /**
   * Returns an editor ILcdGXYEditor for a ShortestDistanceShape object.
   *
   * @return an editor ILcdGXYEditor for a ShortestDistanceShape object.
   */
  public ILcdGXYEditor getGXYEditor(Object aObject) {
    if (aObject != getObject()) {
      setObject(aObject);
    }
    return this;
  }

  /**
   * Paints the current getObject() on a aGraphics in
   * a given mode aMode for the specified aGXYContext.
   * <p/>
   * The first part of the code is necessary for being able to draw the shortest
   * distance path of the point to the polyline while editing is going on.
   * If the polyline is reshaped for example, we need to know its representation
   * in model coordinates in order to be able to calculate the shortest distance
   * path. However, we do not really want to edit shape at this
   * time but only paint how it would look like after being edited. For this
   * reason a clone of the ShortestDistanceShape is made which is edited.
   * The method setObject is called to set the cloned object as the
   * object to be edited by the ILcdGXYEditor.
   * For painting we have to restore the object to be painted to the non-edited
   * object.
   * <p/>
   * The icon painter and the polyline painters then can be called to draw the
   * point and the polyline respectively, taking into account the drawing mode.
   * <p/>
   * Only the shortest distance path still has to be drawn. For this we ask the
   * ShortestDistanceShape shortest what the closest point is
   * on the polyline. The ILcdGXYPen is used to draw the geodesic
   * line between these two points.
   * <p/>
   * If one wouldn't be interested to see the shortest distance path while
   * editing the object, one wouldn't have to perform the cloning either.
   */
  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {

    // assign fObject to shortest : this method uses the variable shortest
    ShortestDistanceShape shortest = fObject;

    // first find out if the object is being edited
    // we need to take some actions in order to be able to paint the edited object nicely
    int edited_object = 0;
    if (((aMode & ILcdGXYPainter.RESHAPING) != 0) ||
        ((aMode & ILcdGXYPainter.TRANSLATING) != 0)) {

      // reference to non edited object
      ShortestDistanceShape non_edited_object = fObject;
      // make a copy/clone which we can edit safely
      ShortestDistanceShape edit_object = (ShortestDistanceShape) fObject.clone();
      // set the copied object as the object to be considered
      setObject(edit_object);
      // set the corresponding ILcdGXYEditor mode
      int edit_mode = 0;
      if ((aMode & ILcdGXYPainter.RESHAPING) != 0) {
        edit_mode = edit_mode | ILcdGXYEditor.RESHAPED;
      }
      if ((aMode & ILcdGXYPainter.TRANSLATING) != 0) {
        edit_mode = edit_mode | ILcdGXYEditor.TRANSLATED;
      }
      // pretend we actually perform the edit operation (but we do it on the copy)
      edited_object = privateEdit(aGraphics, edit_mode, aGXYContext);
      // assign the edited object to shortest for future reference
      shortest = edit_object;
      // restore the original object
      setObject(non_edited_object);
    }

    // draw polyline and point
    // set the mode for painting the polyline
    // if the icon has been edited (edited_object==1) we don't want the polyline
    // to be painted in translated mode
    int mode = aMode;
    if (edited_object == 1) {
      if ((mode & ILcdGXYPainter.TRANSLATING) != 0) {
        mode -= ILcdGXYPainter.TRANSLATING;
      }
    }
    // delegate painting of polyline to fPolylinePainter with adjusted ILcdGXYPainter painting mode
    fPolylinePainter.paint(aGraphics, mode, aGXYContext);

    // set the mode for painting the point
    // if the polyline has been edited (edited_object==2) we don't want the point
    // to be painted in translated mode
    mode = aMode;
    if (edited_object == 2) {
      if ((mode & ILcdGXYPainter.TRANSLATING) != 0) {
        mode -= ILcdGXYPainter.TRANSLATING;
      }
    }
    // delegate painting of point to fIconPainter with adjusted ILcdGXYPainter painting mode
    fIconPainter.paint(aGraphics, mode, aGXYContext);

    // draw shortest path from point to polyline

    // get the pen utility from the ILcdGXYContext
    ILcdGXYPen pen = aGXYContext.getGXYPen();
    try {
      // get the distance of the shortest path and the corresponding point on the polyline
      double distance = shortest.closestPointOnPolylineSFCT(shortest.getPoint(), fShortestPoint);
      // let the pen draw the shortest distance path from the point to the polyline
      // applying the necessary transformations
      pen.drawLine(shortest.getPoint(), fShortestPoint,
                   aGXYContext.getModelXYWorldTransformation(),
                   aGXYContext.getGXYViewXYWorldTransformation(),
                   aGraphics);
      // draw the distance on the graphics
      drawDistance(shortest.getPoint(), fShortestPoint, distance, aGXYContext, aGraphics);
    } catch (TLcdOutOfBoundsException e) {
      // if there would be a TLcdOutOfBoundsException, i.e., because the shortest path is
      // not visible within the projection don't perform any action
    }

  }

  NumberFormat fNumberFormat = NumberFormat.getInstance();

  /**
   * Draws the distance of the shortest distance path on the Graphics.
   */
  private void drawDistance(ILcdPoint aP1,
                            ILcdPoint aP2,
                            double aDistance,
                            ILcdGXYContext aGXYContext,
                            Graphics aGraphics) {

    try {

      ILcdGXYPen pen = aGXYContext.getGXYPen();
      // find the screen coordinates of the first point of the shortest line path
      pen.moveTo(aP1,
                 aGXYContext.getModelXYWorldTransformation(),
                 aGXYContext.getGXYViewXYWorldTransformation());
      int x1 = pen.getX();
      int y1 = pen.getY();
      // find the screen coordinates of the second point of the shortest line path
      pen.moveTo(aP2,
                 aGXYContext.getModelXYWorldTransformation(),
                 aGXYContext.getGXYViewXYWorldTransformation());

      // set (x,y) screen location to about halfway the two points
      int x = (x1 + pen.getX()) / 2;
      int y = (y1 + pen.getY()) / 2;

      fNumberFormat.setMaximumFractionDigits(0);
      fNumberFormat.setGroupingUsed(false);

      // format the distance :
      // if larger than 1000km express it in km, otherwise the distance is expressed in meters
      // draw it on the graphics
      if (aDistance > 1000000) {
        aGraphics.drawString(fNumberFormat.format(Math.rint(aDistance / 1000)) + " km ", x, y);
      } else {
        aGraphics.drawString(fNumberFormat.format(Math.rint(aDistance)) + " m ", x, y);
      }

    } catch (TLcdOutOfBoundsException e) {
    }

  }

  private TLcdXYBounds fTempXYBounds = new TLcdXYBounds();

  /**
   * Moves the gived bounds to the AWT bounds of getObject().
   */
  public void boundsSFCT(Graphics aGraphics,
                         int aMode,
                         ILcdGXYContext aGXYContext,
                         ILcd2DEditableBounds aBoundsSFCT)
      throws TLcdNoBoundsException {

    boolean has_bounds = false;

    // bounds of polyline
    try {
      fPolylinePainter.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
      has_bounds = true;
    } catch (TLcdNoBoundsException e1) {
      // polyline is out of sight and has no bounds in view coordinates
    }

    // bounds of point (icon)
    try {
      if (has_bounds) {
        // make union of bounds of polyline and point object
        fIconPainter.boundsSFCT(aGraphics, aMode, aGXYContext, fTempXYBounds);
        aBoundsSFCT.setTo2DUnion(fTempXYBounds);
      } else {
        // bounds is the bounds of the point object
        fIconPainter.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
        has_bounds = true;
      }
    } catch (TLcdNoBoundsException e2) {
    }

    // bounds of shortest distance path
    ILcdGXYPen pen = aGXYContext.getGXYPen();
    fObject.closestPointOnPolylineSFCT(fObject.getPoint(), fShortestPoint);
    if (has_bounds) {
      // union with calculated bounds
      pen.lineBoundsSFCT(fObject.getPoint(), fShortestPoint,
                         aGXYContext.getModelXYWorldTransformation(),
                         aGXYContext.getGXYViewXYWorldTransformation(),
                         fTempXYBounds);
      aBoundsSFCT.setTo2DUnion(fTempXYBounds);
    } else {
      // bounds is the bounds of the shortest path between the point and the polyline
      pen.lineBoundsSFCT(fObject.getPoint(), fShortestPoint,
                         aGXYContext.getModelXYWorldTransformation(),
                         aGXYContext.getGXYViewXYWorldTransformation(),
                         aBoundsSFCT);
      has_bounds = true;
    }

    // if the object is not visible, i.c., no bounds are calculated throw a TLcdNoBoundsException
    if (!has_bounds) {
      throw new TLcdNoBoundsException();
    }
  }

  /**
   * Returns if the object is touched.
   *
   * @return if the object is touched.
   */
  public boolean isTouched(Graphics aGraphics,
                           int aMode,
                           ILcdGXYContext aGXYContext) {

    // the object is touched if either the point is touched or the polyline is touched
    return fIconPainter.isTouched(aGraphics, aMode, aGXYContext) ||
           fPolylinePainter.isTouched(aGraphics, aMode, aGXYContext);
  }

  /**
   * Returns true if one of the painters accepts snap targets.
   *
   * @return true if one of the painters accepts snap targets.
   */
  public boolean acceptSnapTarget(Graphics aGraphics,
                                  ILcdGXYContext aGXYContext) {

    return fPolylinePainter.acceptSnapTarget(aGraphics, aGXYContext) ||
           fIconPainter.acceptSnapTarget(aGraphics, aGXYContext);

  }

  /**
   * Returns true if one of the painters supports snap targets.
   *
   * @return true if one of the painters supports snap targets.
   */
  public boolean supportSnap(Graphics aGraphics,
                             ILcdGXYContext aGXYContext) {

    return fPolylinePainter.supportSnap(aGraphics, aGXYContext) ||
           fIconPainter.supportSnap(aGraphics, aGXYContext);
  }

  /**
   * Returns the snap target of the current context aGXYContext.
   *
   * @return the snap target of the current context aGXYContext.
   */
  public Object snapTarget(Graphics aGraphics,
                           ILcdGXYContext aGXYContext) {

    Object object = fIconPainter.snapTarget(aGraphics, aGXYContext);

    if (object != null) {
      return object;
    }

    return fPolylinePainter.snapTarget(aGraphics, aGXYContext);
  }

  /**
   * Returns the number of points needed to create object.
   *
   * @return the number of points needed to create object.
   */
  public int getCreationClickCount() {
    return fPolylinePainter.getCreationClickCount();
  }

  /**
   * Edits the current object getObject() and returns <tt>true</tt>
   * if it actually did change.
   */
  public boolean edit(Graphics aGraphics,
                      int aMode,
                      ILcdGXYContext aGXYContext) {

    return (privateEdit(aGraphics, aMode, aGXYContext) != 0);
  }

  /**
   * Edits the current object getObject() and returns an integer
   * indicating which object it actually did change.
   * <ul>
   * <li> 0: nothing changed
   * <li> 1: point edited
   * <li> 2: polyline edited
   * </ul>
   */
  public int privateEdit(Graphics aGraphics,
                         int aEditMode,
                         ILcdGXYContext aGXYContext) {

    int object_has_changed = 0;

    // the isTouched() method requires a paint mode, not an edit mode.
    int paint_mode = ILcdGXYPainter.BODY + ILcdGXYPainter.SELECTED
                     + ((aEditMode & ILcdGXYEditor.TRANSLATED) != 0 ? ILcdGXYPainter.TRANSLATING : 0)
                     + ((aEditMode & ILcdGXYEditor.RESHAPED) != 0 ? ILcdGXYPainter.RESHAPING : 0);

    // if the point is touched, call the edit method of the icon painter
    if (fIconPainter.isTouched(aGraphics, paint_mode, aGXYContext)) {
      object_has_changed = fIconPainter.edit(aGraphics, aEditMode, aGXYContext) ? 1 : 0;
    }
    // if the point is not edited, call the edit method of the polyline painter/editor
    else if (fPolylinePainter.isTouched(aGraphics, paint_mode, aGXYContext)) {
      object_has_changed = fPolylinePainter.edit(aGraphics, aEditMode, aGXYContext) ? 2 : 0;
    }
    return object_has_changed;
  }

  /**
   * Returns a Cursor.
   *
   * @return a Cursor.
   */
  public Cursor getCursor(Graphics aGraphics,
                          int aMode,
                          ILcdGXYContext aGXYContext) {

    if (((aMode & ILcdGXYPainter.RESHAPING) != 0) ||
        ((aMode & ILcdGXYPainter.TRANSLATING) != 0)) {

      // return the reshaping cursor if the point is touched
      if (fIconPainter.isTouched(aGraphics, aMode, aGXYContext)) {
        return cursorReshaping;
      }

      // delegate the getCursor call to the polyline painter
      return fPolylinePainter.getCursor(aGraphics, aMode, aGXYContext);

    }

    // return no cursor, a default one will be used
    return null;
  }

}
