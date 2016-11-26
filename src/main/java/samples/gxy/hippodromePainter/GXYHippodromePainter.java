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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoidUtil;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;

import samples.common.MouseCursorFactory;
import samples.gxy.common.ModelViewUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdCache;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;


/**
 * This is the painter-editor for <code>IHippodrome</code> shapes.
 *
 * <p>A <code>IHippodrome</code> shape can be created with three subsequent input points:
 * The first input point defines the location of the start point, the second the location of the end point while the
 * third input point defines the width of the hippodrome. The painter-editor checks the start point, end point and
 * width's validness to determine what to edit.</p>
 *
 * <p>
 * A <code>IHippodrome</code> shape can be edited in the following ways:
 * <ul>
 * <li>
 * Starting a drag-movement on the outline of the hippodrome moves the whole shape to the location where the
 * input point is removed (in view-reference). When the shape is drawn filled, the same behavior happens when
 * starting the drag-movement inside the shape.
 * </li>
 * <li>
 * Starting a drag-movement on one of the drawn reference-points moves it that point to the location where the
 * input point button is removed.
 * </li>
 * <li>
 * Pressing the META-button when starting a drag-movement on the outline of the hippodrome, sets the width of the
 * hippodrome to the shortest distance (in the model-reference) between the location where the input point was
 * removed and the axis of the hippodrome.
 * </li>
 * </ul>
 * </p>
 * <p/>
 * <p/>
 * The reference points of a hippodrome are the hotpoints of the shape and also act as snap-targets, so when
 * translating a hippodrome over another hippodrome the closest reference-points will automatically overlap.
 * </p>
 */
public class GXYHippodromePainter
    extends ALcdGXYAreaPainter
    implements ILcdGXYPainter, ILcdGXYEditor, ILcdGXYEditorProvider {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(GXYHippodromePainter.class.getName());

  /**
   * Mode to draw the outline of the hippodrome.
   */
  public static final int OUTLINED = ALcdGXYAreaPainter.OUTLINED;
  /**
   * Mode to draw the area of the hippodrome.
   */
  public static final int FILLED = ALcdGXYAreaPainter.FILLED;
  /**
   * Mode to draw the area and the outline of the bounds.
   */
  public static final int OUTLINED_FILLED = ALcdGXYAreaPainter.OUTLINED_FILLED;

  /**
   * The number of required subsequent input points to create a <code>IHippodrome</code>
   */
  public static final int CREATION_CLICK_COUNT = 3;

  /**
   * Indication that nothing of the hippodrome has been touched with the last input point
   */
  private static final int NOT_TOUCHED = 0;
  /**
   * Indication that the hippodrome-arc with <code>aHippodrome.getStartPoint()</code> as center has been touched
   */
  private static final int START_ARC = 1;
  /**
   * Indication that the hippodrome-arc with <code>aHippodrome.getEndPoint()</code> as center has been touched
   */
  private static final int END_ARC = 2;
  /**
   * Indication that the line connecting the start- and end upper-points of the hippodrome has been touched
   */
  private static final int UPPER_LINE = 3;
  /**
   * Indication that the line connecting the start- and end lower-points of the hippodrome has been touched
   */
  private static final int LOWER_LINE = 4;
  /**
   * Indication that the start point of the hippodrome has been touched
   */
  private static final int START_POINT = 5;
  /**
   * Indication that the endpoint of the hippodrome has been touched
   */
  private static final int END_POINT = 6;
  /**
   * Indication that the inner-part of the hippodrome has been touched
   */
  private static final int INNER = 7;

  /**
   * Cursor to return to indicate the object will be reshaped.
   */
  private static final Cursor sCursorReshaping = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

  /**
   * Cursor to return to indicate the object will be reshaped.
   * Direction depends on the shape of the object.
   */
  private static Cursor[] sCursorResizing = {
      Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR),
      Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)
  };

  // The object to be painted.
  private transient IHippodrome fHippodrome;

  private transient Rectangle fTempAWTBounds = new Rectangle();
  private transient TLcdAWTPath fTempAWTPath = new TLcdAWTPath();
  private transient TLcdXYPoint fTempXYWorldPoint = new TLcdXYPoint();
  private transient Point fTempAWTPoint = new Point();
  private transient ILcd3DEditablePoint fTempModelPoint = new TLcdXYZPoint();

  // Determines whether the discretized representation is cached.
  private boolean fPaintCache = true;

  // A temporary copy of the painted hippodrome, for editing.
  private transient IHippodrome fTempHippodrome;

  // Model to model transformation for snapping.
  private ILcdModelModelTransformation fModelReference2ModelReference = new TLcdGeoReference2GeoReference();

  // the icon to draw when a point needs to drawn as a snap-point
  private ILcdIcon fSnapIcon = new TLcdSymbol(TLcdSymbol.RECT, 10, Color.red);

  // Utility.
  private transient ModelViewUtil fReferenceUtil = new ModelViewUtil();

  /**
   * Default constructor.
   */
  public GXYHippodromePainter() {
    super();
  }

  /**
   * Is the set hippodrome drawn using an internal cache or not.
   *
   * @return true if a cache is internally used, false otherwise.
   */
  public boolean isPaintCache() {
    return fPaintCache;
  }

  /**
   * Sets if a cache needs to be used
   *
   * @param aPaintCache if true a cache will be used when the shape needs to be drawn, no use of cache if false
   */
  public void setPaintCache(boolean aPaintCache) {
    fPaintCache = aPaintCache;
  }

  /**
   * <p>Returns whether the hippodrome set on this painter is touched in the context passed.</p>
   *
   * <p>This painter implementation only considers the first input point of the context, even
   * when multiple points are passed (e.g. by using a <code>TLcdGXYContext</code> instance).</p>
   *
   * @param aGraphics   the Graphics on which to check whether the hippodrome is touched.
   * @param aPainterMode the mode of the painter.
   * @param aGXYContext the context in which to check whether the hippodrome is touched.
   * @return true if one of the following is true:
   * <ul>
   * <li>the painter mode contains {@link ILcdGXYPainter#HANDLES} and a reference point is touched ({@link #START_POINT} or {@link #END_POINT}). </li>
   * <li>
   * the mode contains {@link ILcdGXYPainter#HANDLES} and a reference point is touched ({@link #START_POINT} or {@link #END_POINT}).
   * </li>
   * <li>
   * the mode contains {@link ILcdGXYPainter#BODY} and
   * the outline of the hippodrome is touched (the {@link #START_ARC}, {@link #END_ARC}, {@link #UPPER_LINE}
   * or the {@link #LOWER_LINE}).
   * </li>
   * <li>
   * the mode contains {@link ILcdGXYPainter#BODY} and
   * an internal point in the hippodrome is touched and the hippodrome is
   * {@link #FILLED} or {@link #OUTLINED_FILLED}.
   * </li>
   * </ul>
   */
  public boolean isTouched(Graphics aGraphics, int aPainterMode, ILcdGXYContext aGXYContext) {

    // retrieve which part of the hippodrome is touched
    int touchedStatus = retrieveTouchedStatus(aGXYContext, aPainterMode, fHippodrome);

    // return true if a part is touched
    return (touchedStatus != NOT_TOUCHED);
  }

  /**
   * Returns the cursor when painting the hippodrome.
   * @return a 'move' cursor when the hippodrome is touched, a direction arrow when the hippodrome is being reshaped.
   */
  public Cursor getCursor(Graphics aGraphics,
                          int aPainterMode,
                          ILcdGXYContext aGXYContext) {

    // for the translating mode, we check the points and the whole shape
    if ((aPainterMode & ILcdGXYPainter.TRANSLATING) != 0) {

      if (isPointTouched(aGXYContext, fHippodrome, START_POINT) ||
          isPointTouched(aGXYContext, fHippodrome, END_POINT)) {
        return sCursorReshaping;
      } else if (isTouched(aGraphics, aPainterMode, aGXYContext)) {
        return MouseCursorFactory.getMoveCursor();
      }

    }
    // for the reshaping mode, we check the outline
    else if ((aPainterMode & ILcdGXYPainter.RESHAPING) != 0) {

      // 1. Determines what part of the outline is touched.
      int arc_touched = NOT_TOUCHED;
      int line_touched = NOT_TOUCHED;

      if (isArcTouched(aGXYContext, fHippodrome, START_ARC)) {
        arc_touched = START_ARC;
      } else if (isArcTouched(aGXYContext, fHippodrome, END_ARC)) {
        arc_touched = END_ARC;
      } else if (isLineTouched(aGXYContext, fHippodrome, UPPER_LINE)) {
        line_touched = UPPER_LINE;
      } else if (isLineTouched(aGXYContext, fHippodrome, LOWER_LINE)) {
        line_touched = LOWER_LINE;
      }

      // 2. Converts the input point position to model coordinates.
      ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
      ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

      fTempAWTPoint.move(aGXYContext.getX() - aGXYContext.getDeltaX(), aGXYContext.getY() - aGXYContext.getDeltaY());
      vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
      try {
        mwt.worldPoint2modelSFCT(fTempXYWorldPoint, fTempModelPoint);
      } catch (TLcdOutOfBoundsException e) {
        // if the input point position cannot be converted to model coordinates, there's no point
        // in changing the cursor
        return null;
      }

      // 3a One of the hippodrome arcs is touched.
      if (arc_touched != NOT_TOUCHED) {
        ILcdPoint arc_center_point;
        if (arc_touched == START_ARC) {
          arc_center_point = fHippodrome.getStartPoint();
        } else {
          arc_center_point = fHippodrome.getEndPoint();
        }
        double azimuth;
        // get the azimuth from the current position of the input point to the center point of the arc
        ILcdModelReference model_reference = aGXYContext.getGXYLayer().getModel().getModelReference();
        if (model_reference instanceof ILcdGeodeticReference) {
          ILcdGeodeticReference geodetic_reference = (ILcdGeodeticReference) model_reference;
          ILcdEllipsoid ellipsoid = geodetic_reference.getGeodeticDatum().getEllipsoid();
          azimuth = ellipsoid.forwardAzimuth2D(arc_center_point, fTempModelPoint);
        } else {
          // we assume computing in a plane
          azimuth = Math.PI / 2 - Math.atan2(-arc_center_point.getY() + fTempModelPoint.getY(),
                                             -arc_center_point.getX() + fTempModelPoint.getX());
        }
        try {
          return getAzimuthCursor(viewAzimuth(fTempModelPoint, azimuth * TLcdConstant.RAD2DEG, aGXYContext));
        } catch (TLcdOutOfBoundsException e) {
          return null;
        }
      }
      // 3b. One of the hippodrome lines is touched.
      else if (line_touched != NOT_TOUCHED) {

        double azimuth;
        ILcdPoint p1 = line_touched == UPPER_LINE ? fHippodrome.getContourPoint(IHippodrome.START_UPPER_POINT) :
                       fHippodrome.getContourPoint(IHippodrome.END_LOWER_POINT);
        ILcdPoint p2 = line_touched == UPPER_LINE ? fHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT) :
                       fHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT);

        TLcdXYZPoint closest = new TLcdXYZPoint();

        // Finds the model point on the touched hippodrome line and uses the view azimuth at that point.
        ILcdModelReference model_reference = aGXYContext.getGXYLayer().getModel().getModelReference();
        if (model_reference instanceof ILcdGeodeticReference) {
          ILcdEllipsoid ellipsoid = ((ILcdGeodeticReference) model_reference).getGeodeticDatum().getEllipsoid();
          TLcdEllipsoidUtil.closestPointOnGeodesic(p1, p2, fTempModelPoint, ellipsoid, 1e-10, 1.0, closest);
          azimuth = ellipsoid.forwardAzimuth2D(closest, p2) * TLcdConstant.RAD2DEG;
        } else {
          TLcdCartesian.closestPointOnLineSegment(p1, p2, fTempModelPoint, closest);
          azimuth = TLcdCartesian.forwardAzimuth2D(closest, p2) * TLcdConstant.RAD2DEG;
        }
        try {
          // Returns the perpendicular view azimuth.
          return getAzimuthCursor(viewAzimuth(closest, azimuth - 90, aGXYContext));
        } catch (TLcdOutOfBoundsException e) {
          return null;
        }
      }
    }

    // we don't show a specific cursor for this object.
    return null;
  }

  /**
   * Returns a cursor which resembles a movement in the direction of the
   * azimuth passed.
   *
   * @param aAzimuth a forward model azimuth in degrees, 12 o'clock
   * @return a cursor which resembles a movement in the direction of the
   *         azimuth passed.
   */
  private Cursor getAzimuthCursor(double aAzimuth) {
    int index = (int) ((8 * normalizedAzimuth(aAzimuth + 180 / 8)) / 360);
    if (sLogger.isTraceEnabled()) {
      sLogger.trace("GetCursor: angle[" + aAzimuth + "]: index[" + index + "]");
    }
    return sCursorResizing[index];
  }

  // compensates for rotated views or strange projections.
  private double viewAzimuth(ILcdPoint aPoint, double aAzimuth, ILcdGXYContext aGXYContext) throws TLcdOutOfBoundsException {
    double view_azimuth = fReferenceUtil.viewAzimuth(aPoint, aAzimuth, aGXYContext.getModelXYWorldTransformation(), aGXYContext.getGXYViewXYWorldTransformation());
    return normalizedAzimuth(view_azimuth);
  }

  // ensures an azimuth between 0 and 360
  private double normalizedAzimuth(double aAzimuth) {
    double normalized_azimuth = aAzimuth % (360);
    if (normalized_azimuth < 0) {
      normalized_azimuth += 360;
    }
    return normalized_azimuth;
  }

  /**
   * Sets the bounds of the set hippodrome in <code>aBoundsSFCT</code>
   * taking into account the current painterMode <code>aPainterMode</code>.
   */
  public void boundsSFCT(Graphics aGraphics,
                         int aPainterMode,
                         ILcdGXYContext aGXYContext,
                         ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {

    // ...

    // depending on aPainterMode other bounds need to be returned
    if ((aPainterMode & ILcdGXYPainter.CREATING) != 0) {

      // when creating a hippodrome, the bounds are different depending on how far
      // you are in the creation-process.
      if (Double.isNaN(fHippodrome.getEndPoint().getX())) {
        // start point is set, now the endpoint needs to be set; in the meantime a line is
        // drawn between the start point and the current input point-location, therefore calculating
        // the bounds of this drawn line.
        calculateAxisAWTPathSFCT(fHippodrome, aGXYContext, fTempAWTPath);
      } else if (Double.isNaN(fHippodrome.getWidth())) {
        // start- and endpoint are set, now the width of the new hippodrome is being set;
        // make a clone of the set hippodrome, as the set hippodrome isn't yet the complete
        // (resulting) hippodrome
        copyHippodromeToTemp();

        boolean hippodrome_width_changed = privateEdit(
            aGraphics,
            fTempHippodrome,
            aGXYContext,
            ILcdGXYEditor.CREATING);

        if (hippodrome_width_changed) {
          calculateContourAWTPathSFCT(fTempHippodrome, aGXYContext, fTempAWTPath);
        } else {
          // the hippodrome could not be adapted, probably because
          // the current location of the input point could not be transformed to model-coordinates.
          // We just calculate the bounds of the axis
          calculateAxisAWTPathSFCT(fTempHippodrome, aGXYContext, fTempAWTPath);
        }
      }
    }
    else
      if (((aPainterMode & ILcdGXYPainter.TRANSLATING) != 0) ||
          ((aPainterMode & ILcdGXYPainter.RESHAPING) != 0)) {

        // when changing the size or location of the original hippodrome, make a clone of it,
        // so the original hippodrome is unchanged until the complete edit has changed
        copyHippodromeToTemp();

        // apply the editing changes, hereby transforming the painter-modes to editor-modes
        privateEdit(
            aGraphics,
            fTempHippodrome,
            aGXYContext,
            (aPainterMode & ILcdGXYPainter.TRANSLATING) != 0 ? ILcdGXYEditor.TRANSLATED
                                                             : ILcdGXYEditor.RESHAPED);

        // calculate the AWT-path
        calculateContourAWTPathSFCT(fTempHippodrome, aGXYContext, fTempAWTPath);
      }
      else {
        // just need to draw the hippodrome, so no clone and thus calculate the AWT-path on the original hippodrome
        calculateContourAWTPathSFCT(fHippodrome, aGXYContext, fTempAWTPath);
      }

    // use the calculated AWT-path to set the AWT-bounds
    fTempAWTPath.calculateAWTBoundsSFCT(fTempAWTBounds);

    aBoundsSFCT.move2D(fTempAWTBounds.x, fTempAWTBounds.y);
    aBoundsSFCT.setWidth(fTempAWTBounds.width);
    aBoundsSFCT.setHeight(fTempAWTBounds.height);
  }

  private void copyHippodromeToTemp() {
    if (fTempHippodrome == null || (!fTempHippodrome.getClass().equals(fHippodrome.getClass()))) {
      // make a copy of the original hippodrome
      fTempHippodrome = (IHippodrome) fHippodrome.clone();
    } else {
      // make sure it is located at the same position and has the same width as fHippodrome
      fTempHippodrome.moveReferencePoint(fHippodrome.getStartPoint(), IHippodrome.START_POINT);
      fTempHippodrome.moveReferencePoint(fHippodrome.getEndPoint(), IHippodrome.END_POINT);
      fTempHippodrome.setWidth(fHippodrome.getWidth());
    }
  }


  public void paint(Graphics aGraphics, int aPaintMode, ILcdGXYContext aGXYContext) {
    // ...

    // retrieve the pen to use and the currently valid transformations
    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    // print out the received paint-mode
    // this is useful debug output.
    // printPaintMode( aPaintMode );

    boolean paint_axis = false;

    if ((aPaintMode & ILcdGXYPainter.BODY) != 0) {
      // the body of the hippodrome needs to be drawn

      if (
          ((aPaintMode & ILcdGXYPainter.TRANSLATING) != 0) ||
          ((aPaintMode & ILcdGXYPainter.RESHAPING) != 0) ||
          ((aPaintMode & ILcdGXYPainter.CREATING) != 0))
      {
        // Any of the above modes indicate that the hippodrome is being changed. This means
        // that we should not be painting the original object but a modified copy of the
        // object. We cannot edit the object itself, since this is only a paint method, displaying
        // the changes, not executing them.
        copyHippodromeToTemp();

        // edit this copy to apply the edited changes
        // make sure to pass in ILcdGXYEditor-modes based on the received (as argument) paint-modes
        privateEdit(
            aGraphics,
            fTempHippodrome,
            aGXYContext,
            (aPaintMode & ILcdGXYPainter.TRANSLATING) != 0 ? ILcdGXYEditor.TRANSLATED :
            (aPaintMode & ILcdGXYPainter.CREATING) != 0 ? ILcdGXYEditor.CREATING :
            ILcdGXYEditor.RESHAPED
                   );

        IHippodrome hippodrome = fTempHippodrome;

        // when we are busy creating a hippodrome and only the start point has been set,
        // meaning ( ( ( aPaintMode & ILcdGXYPainter.CREATING ) != 0) && ( fCreationClicks = 1 ) ),
        // we can only draw the axis and not the complete hippodrome as the width has not yet been set
        if (
            ((aPaintMode & ILcdGXYPainter.TRANSLATING) != 0)
            || ((aPaintMode & ILcdGXYPainter.RESHAPING) != 0) ||
            (((aPaintMode & ILcdGXYPainter.CREATING) != 0) &&
             (!Double.isNaN(hippodrome.getWidth())))
            )
        // Get the AWT path from the edited shape.
        {
          calculateContourAWTPathSFCT(hippodrome, aGXYContext, fTempAWTPath);
        } else {
          // only the axis needs to be drawn, so only create the bounds of the axis
          calculateAxisAWTPathSFCT(hippodrome, aGXYContext, fTempAWTPath);
          paint_axis = true;
        }
      } else {
        // no editing, just need to draw the original fHippodrome.
        // Get the AWT path, preferably by using the cached general path, or computed directly.
        retrieveAWTPathSFCT(fHippodrome, aGXYContext, fTempAWTPath);
      }

      drawAWTPath(fTempAWTPath, aGraphics, aPaintMode, aGXYContext, paint_axis);
    }

    if (((aPaintMode & ILcdGXYPainter.HANDLES) != 0)) {
      // the reference points are the handles of a hippodrome, so draw them if in that paint-mode.

      // separately catch the exceptions to assure that in that case at least one reference point is drawn.
      try {
        // draw start- and endpoint of hippodrome as hot point
        pen.drawHotPoint(fHippodrome.getStartPoint(), mwt, vwt, aGraphics);
      } catch (TLcdOutOfBoundsException e) {
        // the start point is not visible in this world reference, so we don't draw it.
      }

      try {
        pen.drawHotPoint(fHippodrome.getEndPoint(), mwt, vwt, aGraphics);
      } catch (TLcdOutOfBoundsException e) {
        // the end point is not visible in this world reference, so we don't draw it.
      }
    }

    if ((aPaintMode & ILcdGXYPainter.SNAPS) != 0) {
      // when requested to draw a snap target of the hippodrome, we first need to know which
      // point was returned as snap target, so we can highlight it with the snap icon.

      ILcdPoint snap_target = (ILcdPoint) snapTarget(aGraphics, aGXYContext);

      if (snap_target != null) {
        try {
          pen.moveTo(snap_target, mwt, vwt);
          int x = pen.getX();
          int y = pen.getY();
          fSnapIcon.paintIcon(null,
                              aGraphics,
                              x - (fSnapIcon.getIconWidth() / 2),
                              y - (fSnapIcon.getIconHeight() / 2));
        } catch (TLcdOutOfBoundsException e) {
          // we don't draw the snap target since it is not visible in this world reference.
          // this will actually never happen.
        }
      }
    }
  }

  public Object getObject() {
    return fHippodrome;
  }


  public boolean edit(Graphics aGraphics,
                      int aEditMode,
                      ILcdGXYContext aGXYContext) {

    // debug output
    // printEditMode( aEditMode );

    return privateEdit(aGraphics, fHippodrome, aGXYContext, aEditMode);
  }

  public int getCreationClickCount() {
    return CREATION_CLICK_COUNT;
  }

  public void setObject(Object aObject) {
    // set the original hippodrome
    fHippodrome = (IHippodrome) aObject;
  }

  public ILcdGXYEditor getGXYEditor(Object aObject) {
    if (aObject instanceof IHippodrome) {
      setObject(aObject);
      return this;
    }
    return null;
  }

  public boolean supportSnap(Graphics aGraphics,
                             ILcdGXYContext aGXYContext) {
    return (fHippodrome != null);
  }

  /**
   * Returns whether the current hippodrome can snap to the given target.
   *
   * @param aGraphics   the Graphics on the which the snap target should be accepted.
   * @param aGXYContext the context in which the snap target should be accepted.
   * @return true when
   *         <UL>
   *         <LI>the snap target is an <code>ILcdPoint</code> and
   *         <LI>the snap target is in the same layer or is defined in an equal reference or the model to model transformation
   *         is set so that it can transform the snap target point to a point in the objects model reference
   *         and
   *         <LI>the hippodrome was selected through its start- or endpoint
   *         </UL>
   */
  public boolean acceptSnapTarget(Graphics aGraphics,
                                  ILcdGXYContext aGXYContext) {

    Object snap_target = aGXYContext.getSnapTarget();
    ILcdGXYLayer hippodrome_layer = aGXYContext.getGXYLayer();
    ILcdGXYLayer snap_target_layer = aGXYContext.getSnapTargetLayer();

    // we only snap to points
    if (!(snap_target instanceof ILcdPoint)) {
      return false;
    }

    // we do not snap to ourselves.
    if (snap_target == fHippodrome.getStartPoint() || snap_target == fHippodrome.getEndPoint()) {
      return false;
    }

    ILcdPoint snap_point = (ILcdPoint) snap_target;

    // are the snap target and the hippodrome in the same layer?
    boolean same_layer = (snap_target_layer == hippodrome_layer);

    // or do they have the same model reference?
    ILcdModel snap_target_model = snap_target_layer.getModel();
    ILcdModel hippodrome_model = hippodrome_layer.getModel();
    boolean same_model_ref = same_layer ||
                             ((hippodrome_model.getModelReference().equals(snap_target_model.getModelReference())));

    boolean transformation_supported = false;
    if (!same_model_ref && (fModelReference2ModelReference != null)) {
      fModelReference2ModelReference.setSourceReference(snap_target_model.getModelReference());
      fModelReference2ModelReference.setDestinationReference(hippodrome_model.getModelReference());

      try {
        // retrieve the location of the snap point in the hippodromes reference.
        fModelReference2ModelReference.sourcePoint2destinationSFCT(snap_point, fTempModelPoint);

        // no exception occurred, so compatible references
        transformation_supported = true;
      } catch (TLcdOutOfBoundsException ex) {
        // different model reference and the transformation does not support transforming
        // from one to the other, so we reject the snap target.
        return false;
      }
    }

    // was one of the handles of the hippodrome touched by the input point?
    int touched_status = retrieveTouchedStatus(aGXYContext, ILcdGXYPainter.HANDLES, fHippodrome);

    // are we creating the start or end point?
    boolean creating = Double.isNaN(fHippodrome.getEndPoint().getX());
    // or are we editing the start or end point of an existing hippodrome?
    boolean editing = !Double.isNaN(fHippodrome.getWidth()) && touched_status != NOT_TOUCHED;

    // only accept the target if the transformation allows transforming to it,
    // and if we are dragging or creating one of the points,
    return (same_layer || same_model_ref || transformation_supported) &&
           (creating || editing);
  }

  /**
   * Returns the points of this shape other shapes can snap to.
   * There can be snapped to the start- and endpoint of the hippodrome
   *
   * @param aGraphics   the Graphics on which the snap target is given.
   * @param aGXYContext the context in which the snap target is given.
   * @return the start- or endpoint if the position in the context passed is within the touch sensitivity
   *         (specified by <code>getHotpointSize()</code>)of the pen of the context passed.
   */
  public Object snapTarget(Graphics aGraphics,
                           ILcdGXYContext aGXYContext) {

    Object snap_target = null;

    // determine if the start point is touched,
    if (isPointTouched(aGXYContext, fHippodrome, START_POINT)) {
      snap_target = fHippodrome.getStartPoint();
    }
    // or the endpoint
    else if (isPointTouched(aGXYContext, fHippodrome, END_POINT)) {
      snap_target = fHippodrome.getEndPoint();
    }

    return snap_target;
  }

  public ILcdIcon getSnapIcon() {
    return fSnapIcon;
  }

  public void setSnapIcon(ILcdIcon aSnapIcon) {
    fSnapIcon = aSnapIcon;
  }

  public Object clone() {

    GXYHippodromePainter clone = (GXYHippodromePainter) super.clone();

    // We make a deep clone so that it can be used in another thread,
    // e.g. when using TLcdGXYAsynchronousLayerWrapper.

    // Re-initializes temporary fields.
    clone.fTempAWTBounds = new Rectangle();
    clone.fTempAWTPath = new TLcdAWTPath();
    clone.fTempAWTPoint = new Point();
    clone.fTempXYWorldPoint = new TLcdXYPoint();
    clone.fTempModelPoint = new TLcdXYZPoint();
    clone.fReferenceUtil = new ModelViewUtil();

    // Makes deep copies of fields with mutable objects.
    if (fSnapIcon != null) {
      clone.fSnapIcon = (ILcdIcon) fSnapIcon.clone();
    }
    if (fModelReference2ModelReference != null) {
      try {
        clone.fModelReference2ModelReference = fModelReference2ModelReference.getClass().newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException("Could not clone modelReference2ModelReference field: " + e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Could not clone modelReference2ModelReference field: " + e);
      }
    }
    return clone;
  }

  /**
   * Calculates the bounds of the axis of <code>aHippodrome</code> being formed by its start point
   * and the current input point-location, during creation of  <code>aHippodrome</code> acting as endpoint.
   *
   * @param aHippodrome the hippodrome being created.
   * @param aGXYContext the context in which to calculate the bounds of the axis
   * @param aAWTPathSFCT the calculated bounds as side-effect parameter
   */
  private void calculateAxisAWTPathSFCT(IHippodrome aHippodrome, ILcdGXYContext aGXYContext, ILcdAWTPath aAWTPathSFCT) {

    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    // Resets the current position of the pen.
    pen.resetPosition();

    // resets the given awtPath
    aAWTPathSFCT.reset();

    try {

      // moves the pen to the start-point of the hippodrome
      pen.moveTo(aHippodrome.getStartPoint(), mwt, vwt);

      // moves a temporary view-point to the current input point-location
      fTempAWTPoint.move(aGXYContext.getX(), aGXYContext.getY());

      // transform fTempAWTPoint to model-coordinates
      vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
      mwt.worldPoint2modelSFCT(fTempXYWorldPoint, fTempModelPoint);

      // extend the given awtPath to this model-point
      pen.appendLineTo(fTempModelPoint, mwt, vwt, aAWTPathSFCT);

    } catch (TLcdOutOfBoundsException exc) {
      // one of the points is not visible in this world reference, presumably the second point.
      // we don't put the axis line in the AWT path, so that it is not drawn afterwards.
    }
  }

  /**
   * Applies the editing changes described by <code>aEditMode</code> and <code>aGXYContext</code> to
   * <code>aHippodrome</code>. The context contains information about the current input point(s) position(s) and the previous input point(s)
   * position(s) and transformation to compute the model coordinates of these positions.
   *
   * @param aGraphics the graphics on which the hippodrome would be rendered.
   * @param aHippodrome the hippodrome to edit
   * @param aGXYContext the context in which the editing occurs. This contains amongst others the current input point(s) position(s)
   * and the input point(s) position(s) before a move of the input point(s) occurred.
   * @param aEditMode indicates which type of editing to apply.
   * @return true if the changes had an influence on the hippodrome, false otherwise.
   */
  private boolean privateEdit(Graphics aGraphics,
                              IHippodrome aHippodrome,
                              ILcdGXYContext aGXYContext,
                              int aEditMode) {

    boolean shape_modified = false;
    // ...

    //verify the context before proceeding with the edit operation
    if (isInvalidEditContext(aGXYContext, aEditMode, aHippodrome)) {
      return shape_modified;
    }

    try {
      //loop over all points in the context if necessary. This can be done since the validity of the
      //context is already checked. The editing may still fail as soon as the editing of one point
      //throws an TLcdOutOfBoundsException
      for (int i = 0; i < aGXYContext.getInputPointCount(); i++) {
        //filter the information in the context
        TLcdGXYContext pointContext = getContextForPoint(i, aGXYContext);
        //perform the edit for each point individually
        shape_modified |= privateEditImpl(aGraphics, aHippodrome, pointContext, aEditMode);
      }
    } catch (TLcdOutOfBoundsException exc) {

      // indicate that no change has occurred
      shape_modified = false;
    }

    // when the applied view-changes could also be applied to the model, then clear the cache as it is no longer valid,
    // so when painting a new AWT-path will be calculated instead of retrieved from the cache
    if (shape_modified && (aHippodrome instanceof ILcdCache)) {
      ((ILcdCache) aHippodrome).clearCache();
    }

    return shape_modified;
  }


  /**
   * Applies the editing changes described by <code>aEditMode</code> and <code>aGXYContext</code> to
   * <code>aHippodrome</code>. The context may only contain information about one input point.
   * @param aGraphics the graphics on which the hippodrome would be rendered.
   * @param aHippodrome the hippodrome to edit
   * @param aGXYContext the context in which the editing occurs. This contains amongst others the current input point(s) position(s)
   * and the input point(s) position(s) before a move of the input point(s) occurred.
   * @param aEditMode indicates which type of editing to apply.
   * @return true if the changes had an influence on the hippodrome, false otherwise.
   * @throws TLcdOutOfBoundsException when one of the transformations throws an TLcdOutOfBoundsException
   */
  private boolean privateEditImpl(Graphics aGraphics,
                                  IHippodrome aHippodrome,
                                  ILcdGXYContext aGXYContext,
                                  int aEditMode) throws TLcdOutOfBoundsException {
    boolean aShape_modified = false;
    // retrieve the necessary transformations from the context.
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();
    // clone a point of aHippodrome so that we have point that we can manipulate and that
    // is in the same reference as the hippodrome.
    ILcd3DEditablePoint model_point = aHippodrome.getStartPoint().cloneAs3DEditablePoint();

    if ((aEditMode & ILcdGXYEditor.TRANSLATED) != 0 &&
        (aGXYContext.getDeltaX() != 0 || aGXYContext.getDeltaY() != 0)) {
      // We were/are translating the shape (depending on whether it was called from paint() or edit()).
      // apply different changes to aHippodrome based on input positions and transformations.

      // determine which part has been  changed
      switch (retrieveTouchedStatus(aGXYContext, convertToRenderingMode(aEditMode), aHippodrome)) {
      case START_POINT:

        // the location of the start point is (being) changed,
        // check if it can be snapped to a snap-target
        if (!(aShape_modified = linkToSnapTarget(aHippodrome, aGraphics,
                                                 aEditMode, aGXYContext))) {
          // ...
          // it cannot be moved to a snap-target so move it to the final position of the input point
          // working in view-coordinates

          // transform the startPoint of aHippodrome to view-coordinates
          mwt.modelPoint2worldSFCT(aHippodrome.getStartPoint(), fTempXYWorldPoint);
          vwt.worldPoint2viewAWTPointSFCT(fTempXYWorldPoint, fTempAWTPoint);

          // add the input point-displacement to the view-coordinates of the fTempAWTPoint to reflect
          // the new position of the start point
          fTempAWTPoint.translate(aGXYContext.getDeltaX(), aGXYContext.getDeltaY());

          // transform current input point location back to model-coordinates
          vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
          mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

          // move start point of aHippodrome to model_point
          aHippodrome.moveReferencePoint(model_point, IHippodrome.START_POINT);

          // indicate that aHippodrome has indeed changed.
          aShape_modified = true;
        }

        break;
      case END_POINT:

        // the location of the endpoint is (being) changed,
        //  check if it can be snapped to a snap-target
        if (!(aShape_modified = linkToSnapTarget(aHippodrome, aGraphics,
                                                 aEditMode, aGXYContext))) {
          // ...
          // transform endPoint of aHippodrome to view-coordinates
          mwt.modelPoint2worldSFCT(aHippodrome.getEndPoint(), fTempXYWorldPoint);
          vwt.worldPoint2viewAWTPointSFCT(fTempXYWorldPoint, fTempAWTPoint);

          // add input point-displacement to the view-coordinates of the view_start_point to reflect the new position
          // of the endpoint
          fTempAWTPoint.translate(aGXYContext.getDeltaX(), aGXYContext.getDeltaY());

          // transform fTempAWTPoint back to model-coordinates
          vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
          mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

          // move endpoint of aHippodrome to model_point
          aHippodrome.moveReferencePoint(model_point, IHippodrome.END_POINT);

          // indicate that aHippodrome has indeed changed.
          aShape_modified = true;
        }

        break;
      default: {
        // For all other cases, move the whole shape by moving the start point and end point.
        // Note that the edit controller might want to apply a translation regardless of
        // whether the hippodrome is touched or not.
        // An example use case of this behavior is moving a selected group of objects.
        boolean move_start_point = true;

        // move the whole shape
        // take into account that the startPoint possibly cannot be moved as far as the input point has been moved,
        // because it moves across the edge of the world projection. In that case, try to move the endPoint and
        // calculate the new location of the startPoint based on that
        try {
          // transform startPoint to view-coordinates
          mwt.modelPoint2worldSFCT(aHippodrome.getStartPoint(), fTempXYWorldPoint);
          vwt.worldPoint2viewAWTPointSFCT(fTempXYWorldPoint, fTempAWTPoint);

          // add input point-displacement to the view-coordinates of the fTempAWTPoint to reflect the new position
          fTempAWTPoint.translate(aGXYContext.getDeltaX(), aGXYContext.getDeltaY());

          // transform fTempAWTPoint back to model-coordinates; this might fail if the new location of the
          // start point is outside the world projection
          vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
          mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

        } catch (TLcdOutOfBoundsException e) {
          // moving the start point failed, try the endpoint

          // try to move the endpoint
          mwt.modelPoint2worldSFCT(aHippodrome.getEndPoint(), fTempXYWorldPoint);
          vwt.worldPoint2viewAWTPointSFCT(fTempXYWorldPoint, fTempAWTPoint);

          // add input point-displacement to the view-coordinates of the view_start_point to reflect the new position
          fTempAWTPoint.translate(aGXYContext.getDeltaX(), aGXYContext.getDeltaY());

          // transform current input point location back to model-coordinates
          vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
          mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

          move_start_point = false;
        }

        // move the shape to the new start-position. The move2D-method will accordingly adjust the endpoint
        aHippodrome.move2D(model_point.getX(), model_point.getY(), move_start_point);

        // change has succeeded
        aShape_modified = true;
        break;
      }
      }
    }
    else
      if ((
              ((aEditMode & ILcdGXYEditor.RESHAPED) != 0)
              && (isContourTouched(aGXYContext, aHippodrome)))
          || (((aEditMode & ILcdGXYEditor.CREATING) != 0)
              && (!Double.isNaN(aHippodrome.getEndPoint().getX()))
              && (Double.isNaN(aHippodrome.getWidth())))
          ) {
        // when these modes are received, the user is busy changing or
        // has just changed the width of aHippodrome
        aShape_modified = changeWidth(aGXYContext, aHippodrome);
      }
      else
        if (((aEditMode & ILcdGXYEditor.START_CREATION) != 0)
            || (((aEditMode & ILcdGXYEditor.CREATING) != 0)
                && (!Double.isNaN(aHippodrome.getStartPoint().getX()))
                && (Double.isNaN(aHippodrome.getEndPoint().getX()))
        )) {

          // move the temp. point to the input point coordinates.
          fTempAWTPoint.move(aGXYContext.getX(), aGXYContext.getY());

          // transform input point-position to model-coordinates
          vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
          mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

          if (((aEditMode & ILcdGXYEditor.START_CREATION) != 0)) {
            // we just started creating aHippodrome,
            // so place the start point of the aHippodrome on the input location
            aHippodrome.moveReferencePoint(model_point, IHippodrome.START_POINT);
          } else {
            // we're busy creating the hippodrome and the start point has already been placed,
            // so place the end point of tge aHippodrome on the input location
            aHippodrome.moveReferencePoint(model_point, IHippodrome.END_POINT);
          }

          aShape_modified = true;
        }
    return aShape_modified;
  }

  /**
   * Returns whether the context <code>aGXYContext</code> is invalid or not
   *
   * @param aGXYContext the context to check
   * @param aEditMode   the edit mode
   * @param aHippodrome the hippodrome to edit
   *
   * @return <code>true</code> when the context is invalid and no editing should take place,
   *         <code>false</code> otherwise
   */
  private boolean isInvalidEditContext(ILcdGXYContext aGXYContext, int aEditMode, IHippodrome aHippodrome) {
    //we do not handle more than 2 input points simultaneously
    if (aGXYContext.getInputPointCount() > 2) {
      return true;
    }

    if (aGXYContext.getInputPointCount() > 1) {
      //we only accept more than one input point in case we are translating
      if ((aEditMode & ILcdGXYEditor.TRANSLATED) == 0) {
        return true;
      }
      //in case we are translating, both the start and end point of the hippodrome should be touched
      TLcdGXYContext firstPointContext = getContextForPoint(0, aGXYContext);
      TLcdGXYContext secondPointContext = getContextForPoint(1, aGXYContext);
      int firstTouchedPoint = retrieveTouchedStatus(firstPointContext, convertToRenderingMode(aEditMode), aHippodrome);
      int secondTouchedPoint = retrieveTouchedStatus(secondPointContext, convertToRenderingMode(aEditMode), aHippodrome);
      if (!((firstTouchedPoint == START_POINT && secondTouchedPoint == END_POINT) ||
            (firstTouchedPoint == END_POINT && secondTouchedPoint == START_POINT))) {
        //not both the start and end point are touched, indicate the context should be rejected
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a <code>TLcdGXYContext</code> instance containing only the information of
   * <code>aContext</code> related to the input point at index <code>aIndex</code>
   *
   * @param aIndex   the index
   * @param aContext the context containing the information about all input points
   *
   * @return a <code>TLcdGXYContext</code> instance containing only the information of
   *         <code>aContext</code> related to the input point at index <code>aIndex</code>
   *
   * @throws IndexOutOfBoundsException when <code>aIndex</code> &lt; 0 || <code>aIndex</code> &gt; =
   *                                   <code>aContext.getInputPointCount()</code>
   */
  private TLcdGXYContext getContextForPoint(int aIndex, ILcdGXYContext aContext) throws IndexOutOfBoundsException {
    TLcdGXYContext result = new TLcdGXYContext();
    //copy all relevant information into the new context instance
    result.setX(aContext.getX(aIndex));
    result.setY(aContext.getY(aIndex));
    result.setDeltaX(aContext.getDeltaX(aIndex));
    result.setDeltaY(aContext.getDeltaY(aIndex));
    result.setSnapTarget(aContext.getSnapTarget(aIndex));
    result.setSnapTargetLayer(aContext.getSnapTargetLayer(aIndex));
    result.setGXYLayer(aContext.getGXYLayer());
    result.setGXYPen(aContext.getGXYPen());
    result.setGXYView(aContext.getGXYView());
    result.setGXYViewXYWorldTransformation(aContext.getGXYViewXYWorldTransformation());
    result.setModelXYWorldTransformation(aContext.getModelXYWorldTransformation());
    result.setSensitivity(aContext.getSensitivity());
    //return the result
    return result;
  }

  private boolean changeWidth(ILcdGXYContext aGXYContext,
                              IHippodrome aHippodrome) throws TLcdOutOfBoundsException {
    boolean shape_modified;
    ILcd3DEditablePoint model_point = aHippodrome.getStartPoint().cloneAs3DEditablePoint();

    // move a temp. viewpoint to the current location of the input point
    fTempAWTPoint.move(aGXYContext.getX(), aGXYContext.getY());

    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    // transform input point-position to model-coordinates
    vwt.viewAWTPoint2worldSFCT(fTempAWTPoint, fTempXYWorldPoint);
    mwt.worldPoint2modelSFCT(fTempXYWorldPoint, model_point);

    // calculate distance (in the model) from the input point-location to the axis of aHippodrome
    double distance_to_axis;
    ILcdModelReference model_reference = aGXYContext.getGXYLayer().getModel().getModelReference();
    if (model_reference instanceof ILcdGeodeticReference) {
      distance_to_axis = TLcdEllipsoidUtil.closestPointOnGeodesic(aHippodrome.getStartPoint(), aHippodrome.getEndPoint(), model_point, ((ILcdGeodeticReference) model_reference).getGeodeticDatum().getEllipsoid(), 1e-10,
                                                                  1.0, new TLcdLonLatPoint());
    } else {
      // we assume we are computing in a plane
      distance_to_axis = TLcdCartesian.closestPointOnLineSegment(aHippodrome.getStartPoint(), aHippodrome.getEndPoint(), model_point, new TLcdXYPoint());
    }

    // set this distance as new width of aHippodrome
    aHippodrome.setWidth(distance_to_axis);

    shape_modified = true;
    return shape_modified;
  }

  /**
   * Links to a snap target if it is accepted and returns whether snapping has occurred.
   *
   * @param aHippodrome the hippodrome to link to the snap-target returned by <code>aGXYContext</code>
   * @param aGraphics the Graphics on which the hippodrome will be rendered.
   * @param aEditMode the mode in which the hippodrome is being edited.
   * @param aGXYContext the context is which snapping should occur. The context contains the snap target and the layer
   * the snap target is contained in.
   * @return true if the start point of the hippodrome is moved to a snap target.
   */
  private boolean linkToSnapTarget(IHippodrome aHippodrome,
                                   Graphics aGraphics,
                                   int aEditMode,
                                   ILcdGXYContext aGXYContext) {

    boolean snapped_to_target = false;

    // test if the snap target returned by aGXYContext can be accepted by aHippodrome; in other words,
    // can aHippodrome snap to it
    if (acceptSnapTarget(aGraphics, aGXYContext)) {
      // if it is possible, retrieve which reference-point is touched, so we know which
      // point needs to be moved to that snap-target

      ILcdPoint snap_point = (ILcdPoint) aGXYContext.getSnapTarget();
      ILcdGXYLayer hippodrome_layer = aGXYContext.getGXYLayer();
      ILcdGXYLayer snap_layer = aGXYContext.getSnapTargetLayer();

      // which point is touched ? acceptSnapTarget already ensures that either
      // the start or the end point is touched.
      // Since checking whether an object is touched is a functionality of the painter,
      // the edit mode has to be converted to a rendering mode.
      int renderingMode = convertToRenderingMode(aEditMode);
      int touched_status = retrieveTouchedStatus(aGXYContext, renderingMode, aHippodrome);

      int point_to_move = IHippodrome.START_POINT;

      if (touched_status == END_POINT) {
        point_to_move = IHippodrome.END_POINT;
      }

      // if the snap target is in the same layer or has the same model-references, just move the point
      // of the hippodrome
      if ((snap_layer == hippodrome_layer) ||
          (snap_layer.getModel().getModelReference().equals(
              hippodrome_layer.getModel().getModelReference()))) {

        aHippodrome.moveReferencePoint(snap_point, point_to_move);
        snapped_to_target = true;
      } else {
        try {
          // acceptSnapTarget is called, so the transformation is setup correctly.
          // fTempModelPoint is moved to the location of the snap_point
          fModelReference2ModelReference.setDestinationReference(
              snap_layer.getModel().getModelReference());
          fModelReference2ModelReference.setSourceReference(
              hippodrome_layer.getModel().getModelReference());
          fModelReference2ModelReference.destinationPoint2sourceSFCT(snap_point, fTempModelPoint);

          // move the touched reference-point to the snap-target
          aHippodrome.moveReferencePoint(fTempModelPoint, point_to_move);

          snapped_to_target = true;
        } catch (TLcdOutOfBoundsException e) {
          snapped_to_target = false;
        }
      }
    }

    return snapped_to_target;
  }

  /**
   * Converts an edit mode to a rendering mode. This method is called by
   * {@link #linkToSnapTarget(IHippodrome, Graphics, int, ILcdGXYContext) linkToSnapTarget}
   * to check whether the shape is being touched (which requires a rendering mode), while editing the shape (which
   * passes an edit mode).
   * @param aEditMode the editing mode to convert
   * @return the rendering mode corresponding to the editing mode
   */
  private int convertToRenderingMode(int aEditMode) {
    int renderingMode = ILcdGXYPainter.HANDLES | ILcdGXYPainter.BODY;
    if ((aEditMode & ILcdGXYEditor.TRANSLATED) != 0) {
      renderingMode |= ILcdGXYPainter.TRANSLATING;
    } else if ((aEditMode & ILcdGXYEditor.RESHAPED) != 0) {
      renderingMode |= ILcdGXYPainter.RESHAPING;
    } else if (
        (aEditMode & ILcdGXYEditor.CREATING) != 0 ||
        (aEditMode & ILcdGXYEditor.START_CREATION) != 0
        ) {
      renderingMode |= ILcdGXYPainter.CREATING;
    }
    return renderingMode;
  }

  /**
   * This method is called by this <code>ILcdGXYPainter</code> just before
   * drawing lines on <code>aGraphics</code>. It can be
   * redefined in order to set specific <code>Graphics</code> properties like Color, etc ...
   * It calls <code>getLineStyle().setupGraphics()</code> if such method returns a non <code>null</code>
   * value.
   *
   * @param aGraphics   the Graphics on which the drawing is done.
   * @param aMode       the ILcdGXYPainter mode.
   * @param aGXYContext the context of the drawing.
   */
  private void setupGraphicsForLine(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (getLineStyle() != null) {
      getLineStyle().setupGraphics(aGraphics,
                                   fHippodrome,
                                   aMode,
                                   aGXYContext);
    }
  }

  /**
   * This method is called by this <code>ILcdGXYPainter</code> just before
   * filling hippodromes on <code>aGraphics</code>. It can be
   * redefined in order to set specific <code>Graphics</code> properties like <code>Color</code>, etc ...
   * It calls <code>getLineStyle().setupGraphics()</code> if such method returns a non <code>null</code>
   * value.
   *
   * @param aGraphics   the Graphics on which the drawing is done.
   * @param aMode       the ILcdGXYPainter mode.
   * @param aGXYContext the context of the drawing.
   */
  private void setupGraphicsForFill(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (getFillStyle() != null) {
      getFillStyle().setupGraphics(aGraphics,
                                   fHippodrome,
                                   aMode,
                                   aGXYContext);
    }
  }

  /**
   * Retrieves which part of <code>aHippodrome</code> is touched in the specified <code>aGXYContext</code>.
   *
   * @param aGXYContext the context in which to check what part of the hippodrome is touched. It contains the input
   * point location.
   * @param aRenderingMode the mode in which the hippodrome is rendered. It is possible that depending on this mode,
   * the inside of the hippodrome should be taken into account or not.
   * @param aHippodrome the hippodrome that might have been touched.
   * @return
   * <UL>
   * <LI> START_POINT: if the start point of <code>aHippodrome</code> is touched</LI>
   * <LI> END_POINT: if the end point of <code>aHippodrome</code> is touched</LI>
   * <LI> START_ARC: if the arc with the start point of <code>aHippodrome</code> as center is touched
   * <LI> END_ARC: if the arc with the endpoint of <code>aHippodrome</code> as center is touched
   * <LI> UPPER_LINE: if the line between the upper points of <code>aHippodrome</code> is touched
   * <LI> LOWER_LINE: if the line between the lower points of <code>aHippodrome</code> is touched
   * <LI> INNER: if <code>getDrawMode()</code> is FILLED or OUTLINED_FILLED and a point within the contours of
   *      <code>aHippodrome</code> has been touched
   * <LI> NOT_TOUCHED: otherwise
   * </UL>
   */
  private int retrieveTouchedStatus(ILcdGXYContext aGXYContext,
                                    int aRenderingMode,
                                    IHippodrome aHippodrome) {
    boolean checkHandles = (aRenderingMode & ILcdGXYPainter.HANDLES) != 0;
    boolean checkBody = (aRenderingMode & ILcdGXYPainter.BODY) != 0;

    if (checkHandles) {
      if (isPointTouched(aGXYContext, aHippodrome, START_POINT)) {
        return START_POINT;
      } else if (isPointTouched(aGXYContext, aHippodrome, END_POINT)) {
        return END_POINT;
      }
    }
    if (checkBody) {
      if (isArcTouched(aGXYContext, aHippodrome, START_ARC)) {
        return START_ARC;
      } else if (isArcTouched(aGXYContext, aHippodrome, END_ARC)) {
        return END_ARC;
      } else if (isLineTouched(aGXYContext, aHippodrome, UPPER_LINE)) {
        return UPPER_LINE;
      } else if (isLineTouched(aGXYContext, aHippodrome, LOWER_LINE)) {
        return LOWER_LINE;
      } else if ((getMode() == FILLED || getMode() == OUTLINED_FILLED) ||
                 ((aRenderingMode & ILcdGXYPainter.SELECTED) != 0 &&
                  (getSelectionMode() == FILLED || getSelectionMode() == OUTLINED_FILLED))) {
        // retrieve (or calculate) an AWTPath for aHippodrome to be able to determine if the original
        // input point-location lies within the aHippodrome
        retrieveAWTPathSFCT(aHippodrome, aGXYContext, fTempAWTPath);

        // take into account the original input point-location instead of the current
        // ==> aGXYContext.getX() - aGXYContext.getDeltaX() as argument for polygonContains()
        if (fTempAWTPath.polygonContains(aGXYContext.getX() - aGXYContext.getDeltaX(),
                                         aGXYContext.getY() - aGXYContext.getDeltaY())) {
          return INNER;
        }
      }
    }
    return NOT_TOUCHED;
  }

  /**
   * Checks if on the original location of the input point a hotpoint was touched, being or the startPoint or the endPoint
   *
   * @param aGXYContext      the context in which to check whether a point is touched. It contains the input point location
   * and the transformations, which enables comparison with model coordinates.
   * @param aHippodrome the considered hippodrome
   * @param aHippodromePoint which hot point to consider: <code>START_POINT</code> or <code>END_POINT</code>
   * @return
   *        <UL>
   *        <LI> END_POINT if the end point has been touched
   *        <LI> START_POINT if the start point has been touched
   *        <LI> NOT_TOUCHED if neither the start point neither the end point is touched
   *        </UL>
   */
  private boolean isPointTouched(ILcdGXYContext aGXYContext,
                                 IHippodrome aHippodrome,
                                 int aHippodromePoint) {
    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    ILcdPoint point_to_check;

    if (aHippodromePoint == START_POINT) {
      point_to_check = aHippodrome.getStartPoint();
    } else if (aHippodromePoint == END_POINT) {
      point_to_check = aHippodrome.getEndPoint();
    } else {
      return false;
    }

    return pen.isTouched(
        point_to_check,
        aGXYContext.getX() - aGXYContext.getDeltaX(),
        aGXYContext.getY() - aGXYContext.getDeltaY(),
        getSensitivity(aGXYContext, pen.getHotPointSize()),
        mwt,
        vwt);
  }

  private int getSensitivity(ILcdGXYContext aGXYContext, int aDefaultValue) {
    int sensitivity = aGXYContext.getSensitivity();
    if (sensitivity >= 0) {
      return sensitivity;
    }
    return aDefaultValue;
  }


  /**
   * Gets the AWT-path (in the view-reference thus) corresponding to <code>aHippdorome</code>. This path expresses
   * the hippodrome in AWT coordinates given the transformations in the context.
   * If the shape has a previous version in cache and that is still valid, the cached value is used.
   *
   * @param aHippodrome the hippodrome to retrieve the AWT path for
   * @param aGXYContext the context in which the path should be created. It contains the transformations from model to
   * world and world to view.
   * @param aAWTPathSFCT the path to append the path of the hippodrome to.
   */
  private void retrieveAWTPathSFCT(IHippodrome aHippodrome, ILcdGXYContext aGXYContext,
                                   ILcdAWTPath aAWTPathSFCT) {

    // Do we have a cache and may it be used (defined by fPaintCache)
    if (fPaintCache && aHippodrome instanceof ILcdCache) {

      // Get the general path through the cache.
      ILcdGeneralPath general_path = retrieveGeneralPathThroughCache(aGXYContext, aHippodrome);

      // Convert it into an AWT path.
      fTempAWTPath.reset();
      fTempAWTPath.ensureCapacity(general_path.subPathLength(0));

      // Appends the given path general_path (in world coordinates) to the given path aAWTPathSFCT
      // (in AWT view coordinates), applying the given ILcdGXYViewXYWorldTransformation
      aGXYContext.getGXYPen().appendGeneralPath(general_path,
                                                aGXYContext.getGXYViewXYWorldTransformation(), aAWTPathSFCT);
    } else {
      // We don't have a cache.
      // Compute the AWT path directly.
      calculateContourAWTPathSFCT(aHippodrome, aGXYContext, aAWTPathSFCT);
    }
  }

  /**
   * Gets the general path (in the world-reference thus) corresponding to <code>aHippodrome</code>
   * The shape is used as a cache if appropriate.
   * @param aHippodrome the hippodrome to retrieve the AWT path for
   * @param aGXYContext the context in which the path should be created. It contains the transformations from model to
   * world and world to view.
   * @return the path of the represention of the hippodrome in the given context.
   */
  private ILcdGeneralPath retrieveGeneralPathThroughCache(ILcdGXYContext aGXYContext,
                                                          IHippodrome aHippodrome) {

    // Get the cache.
    ILcdCache cacher = (ILcdCache) aHippodrome;
    HippodromeCache cache = (HippodromeCache) cacher.getCachedObject(this);

    // Make sure we have a cached path.
    TLcdGeneralPath general_path = cache == null ? new TLcdGeneralPath() : cache.fGeneralPath;

    ILcdXYWorldReference current_world_reference = aGXYContext.getGXYView().getXYWorldReference();

    // Is the path valid?
    if (cache == null || !cache.isValid(current_world_reference)) {

      // Cached path not available or valid ==> update the path and cache it.
      calculateContourGeneralPath(aHippodrome, aGXYContext, general_path);

      // Trim it to take up as little space as possible.
      general_path.trimToSize();

      // Put it in the cache.
      cache = new HippodromeCache(current_world_reference, general_path);
      cacher.insertIntoCache(this, cache);
    }

    return general_path;
  }

  /**
   * Calculates the AWT path of <code>aHippodrome</code> based on its contour.
   * The expresses in AWT coordinates what should be painted, given the current settings of the view
   * (in GXYContext).
   *
   * @param aHippodrome the hippodrome of which the AWT path need to be calculated
   * @param aGXYContext the context in which to calculate the bounds of the axis
   * @param aAWTPathSFCT the calculated bounds as side-effect parameter
   */
  private void calculateContourAWTPathSFCT(IHippodrome aHippodrome,
                                           ILcdGXYContext aGXYContext,
                                           ILcdAWTPath aAWTPathSFCT) {

    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    // Resets the current position of the pen.
    pen.resetPosition();

    // reset the AWT-path
    aAWTPathSFCT.reset();

    // a boolean to check if a part of the hippodrome is not visible in the current world reference.
    // we try to paint as much as possible.
    boolean out_of_bounds = false;
    try {

      // moves the pen to the startUpper-point of the hippodrome
      pen.moveTo(aHippodrome.getContourPoint(IHippodrome.START_UPPER_POINT), mwt, vwt);

      // append a line to the endUpper-point
      pen.appendLineTo(aHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT),
                       mwt, vwt, aAWTPathSFCT);

    } catch (TLcdOutOfBoundsException ex) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT), mwt, vwt);
        out_of_bounds = false;
      }
      // append an arc using the endpoint as center and taking into account the backward
      // azimuth of the hippodrome
      pen.appendArc(aHippodrome.getEndPoint(), aHippodrome.getWidth(),
                    aHippodrome.getWidth(), 270 - aHippodrome.getEndStartAzimuth(),
                    -aHippodrome.getEndStartAzimuth(), -180.0, mwt, vwt, aAWTPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.END_LOWER_POINT), mwt, vwt);
        out_of_bounds = false;
      }
      // append a line to the startLower-point
      pen.appendLineTo(aHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT),
                       mwt, vwt, aAWTPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT), mwt, vwt);
      }

      // append an arc using the startPoint as center and taking into account the forward
      // azimuth of the hippodrome.
      pen.appendArc(aHippodrome.getStartPoint(), aHippodrome.getWidth(),
                    aHippodrome.getWidth(), 90.0 - aHippodrome.getStartEndAzimuth(),
                    -aHippodrome.getStartEndAzimuth(), -180.0, mwt, vwt, aAWTPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      // we don't draw the last arc.
    }
  }

  /**
   * Computes the general path corresponding to the given <code>aHippodrome</code>.
   * This expresses what should be painted in World coordinates. This method is mainly used for caching the
   * object in world coordinates, since these coordinates do not change under zoom or pan operations,
   * contrary to AWT coordinates
   * (see {@link #calculateContourAWTPathSFCT(IHippodrome, ILcdGXYContext, ILcdAWTPath)}).
   * @param aHippodrome the hippodrome to calculate the world path for
   * @param aGXYContext the context in which the path should be created. It contains the transformations from model to
   * world and world to view.
   * @param aGeneralPathSFCT the path to append the world path of the hippodrome to.
   */
  private void calculateContourGeneralPath(IHippodrome aHippodrome,
                                           ILcdGXYContext aGXYContext,
                                           ILcdGeneralPath aGeneralPathSFCT) {

    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();

    // reset the pen and the path itself
    pen.resetPosition();
    aGeneralPathSFCT.reset();

    // a boolean to check if a part of the hippodrome is not visible in the current world reference.
    // we try to paint as much as possible.
    boolean out_of_bounds = false;
    try {

      // moves the pen to the startUpper-point of the hippodrome
      pen.moveTo(aHippodrome.getContourPoint(IHippodrome.START_UPPER_POINT), mwt);

      // append a line to the endUpper-point
      pen.appendLineTo(aHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT), mwt, aGeneralPathSFCT);

    } catch (TLcdOutOfBoundsException ex) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT), mwt);
        out_of_bounds = false;
      }
      // append an arc using the endpoint as center and taking into account the backward azimuth of the hippodrome
      pen.appendArc(aHippodrome.getEndPoint(),
                    aHippodrome.getWidth(),
                    aHippodrome.getWidth(),
                    270 - aHippodrome.getEndStartAzimuth(),
                    -aHippodrome.getEndStartAzimuth(),
                    -180.0, mwt, aGeneralPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.END_LOWER_POINT), mwt);
        out_of_bounds = false;
      }
      // append a line to the startLower-point
      pen.appendLineTo(aHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT), mwt, aGeneralPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      out_of_bounds = true;
    }

    try {
      if (out_of_bounds) {
        pen.moveTo(aHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT), mwt);
      }

      // append an arc using the startPoint as center and taking into account the forward azimuth of the hippodrome.
      pen.appendArc(aHippodrome.getStartPoint(),
                    aHippodrome.getWidth(),
                    aHippodrome.getWidth(),
                    90.0 - aHippodrome.getStartEndAzimuth(),
                    -aHippodrome.getStartEndAzimuth(),
                    -180.0, mwt, aGeneralPathSFCT);
    } catch (TLcdOutOfBoundsException e) {
      // we don't draw the last arc.
    }
  }

  /**
   * <p>Checks if the arc indicated by <code>aArcPosition</code> of <code>aHippodrome</code> is
   * touched.</p>
   *
   * <p>This implementation only considers the first input point of the <code>ILcdGXYContext</code>,
   * even when multiple input points would be specified.</p>
   *
   * @param aGXYContext  the context in which to check whether the arc of the hippodrome is touched.
   *                     It contains the input point position, the transformations and the pen that
   *                     defines the path an arc follows.
   * @param aHippodrome  the hippodrome to check for whether the arc is touched.
   * @param aArcPosition indicates which arc should be checked for. The value should be either
   *                     START_ARC or END_ARC.
   *
   * @return true if the specified arc is touched, false otherwise
   */
  private boolean isArcTouched(ILcdGXYContext aGXYContext, IHippodrome aHippodrome, int aArcPosition) {

    // values for aRotAngle, aStartAngle and aArcAngle, see calculateContourAWTPathSFCT
    return aGXYContext.getGXYPen().isArcTouched(
        (aArcPosition == START_ARC) ? aHippodrome.getStartPoint() : aHippodrome.getEndPoint(),
        aHippodrome.getWidth(),
        aHippodrome.getWidth(),
        90.0 - aHippodrome.getStartEndAzimuth(),
        (aArcPosition == START_ARC) ? -aHippodrome.getStartEndAzimuth() : -aHippodrome.getEndStartAzimuth(),
        -180.0,
        aGXYContext.getX() - aGXYContext.getDeltaX(),
        aGXYContext.getY() - aGXYContext.getDeltaY(),
        getSensitivity(aGXYContext, aGXYContext.getGXYPen().getHotPointSize()),
        aGXYContext.getModelXYWorldTransformation(),
        aGXYContext.getGXYViewXYWorldTransformation());
  }

  /**
   * <p>Checks if the line indicated by <code>aLinePosition</code> of <code>aHippodrome</code> is
   * currently touched.</p>
   *
   * <p>This implementation only considers the first input point of the <code>ILcdGXYContext</code>,
   * even when multiple input points would be specified.</p>
   *
   * @param aGXYContext   the context in which to check whether a line of the hippodrome is touched.
   *                      It contains the input point position, the transformations and the pen that
   *                      defines the path an arc follows.
   * @param aHippodrome   the hippodrome to check for whether a line is touched.
   * @param aLinePosition indicates which line should be checked for. The value should be either
   *                      UPPER_LINE or LOWER_LINE.
   *
   * @return true if the specified line is touched, false otherwise
   */
  private boolean isLineTouched(ILcdGXYContext aGXYContext, IHippodrome aHippodrome, int aLinePosition) {

    return aGXYContext.getGXYPen().isLineTouched(
        (aLinePosition == UPPER_LINE) ? aHippodrome.getContourPoint(IHippodrome.START_UPPER_POINT)
                                      : aHippodrome.getContourPoint(IHippodrome.START_LOWER_POINT),
        (aLinePosition == UPPER_LINE) ? aHippodrome.getContourPoint(IHippodrome.END_UPPER_POINT)
                                      : aHippodrome.getContourPoint(IHippodrome.END_LOWER_POINT),
        aGXYContext.getX() - aGXYContext.getDeltaX(),
        aGXYContext.getY() - aGXYContext.getDeltaY(),
        false,
        getSensitivity(aGXYContext, aGXYContext.getGXYPen().getHotPointSize()),
        aGXYContext.getModelXYWorldTransformation(),
        aGXYContext.getGXYViewXYWorldTransformation());
  }

  /**
   * <p>Convenience method to check whether the outline of the Hippodrome is touched</p>
   *
   * <p>This implementation only considers the first input point of the <code>ILcdGXYContext</code>,
   * even when multiple input points would be specified.</p>
   *
   * @param aGXYContext the context in which to check whether the contour of a hippodrome is
   *                    touched. It contains the input point position, the transformations and the
   *                    pen.
   * @param aHippodrome the hippodrome to check for whether the contour is touched.
   *
   * @return true if any part of the contour of the hippodrome is touched in the given context.
   *         False otherwise.
   */
  private boolean isContourTouched(ILcdGXYContext aGXYContext, IHippodrome aHippodrome) {

    return isLineTouched(aGXYContext, aHippodrome, UPPER_LINE)
           || isLineTouched(aGXYContext, aHippodrome, LOWER_LINE)
           || isArcTouched(aGXYContext, aHippodrome, START_ARC)
           || isArcTouched(aGXYContext, aHippodrome, END_ARC);
  }

  /**
   * Debug output method. Call this method at the beginning of paint or isTouched to check which modes are
   * passed to this method and in what order.
   * @param aMode a painter rendering mode.
   */
  private void printPaintMode(int aMode) {
    System.out.print("paintMode includes [" + aMode + "]");

    if ((aMode & ILcdGXYPainter.BODY) != 0) {
      System.out.print("BODY ");
    }
    if ((aMode & ILcdGXYPainter.DEFAULT) != 0) {
      System.out.print("DEFAULT ");
    }
    if ((aMode & ILcdGXYPainter.SELECTED) != 0) {
      System.out.print("SELECTED ");
    }
    if ((aMode & ILcdGXYPainter.TRANSLATING) != 0) {
      System.out.print("TRANSLATING ");
    }
    if ((aMode & ILcdGXYPainter.RESHAPING) != 0) {
      System.out.print("RESHAPING ");
    }
    if ((aMode & ILcdGXYPainter.SNAPS) != 0) {
      System.out.print("SNAPS ");
    }
    if ((aMode & ILcdGXYPainter.HANDLES) != 0) {
      System.out.print("HANDLES ");
    }
    if ((aMode & ILcdGXYPainter.CREATING) != 0) {
      System.out.print("CREATING ");
    }
    System.out.println("");
  }

  /**
   * Debug output method. Call this method at the beginning of edit to check which modes are
   * passed to this method and in what order.
   * @param aMode the editing mode to create a human readable form for.
   */
  private void printEditMode(int aMode) {
    System.out.print("editMode includes ");

    if ((aMode & ILcdGXYEditor.CREATING) != 0) {
      System.out.print("CREATING ");
    }
    if ((aMode & ILcdGXYEditor.RESHAPED) != 0) {
      System.out.print("RESHAPED ");
    }
    if ((aMode & ILcdGXYEditor.START_CREATION) != 0) {
      System.out.print("START_CREATION ");
    }
    if ((aMode & ILcdGXYEditor.TRANSLATED) != 0) {
      System.out.print("TRANSLATED ");
    }
    System.out.println("");
  }

  /**
   * Draws <code>fTempAWTPath</code> using the specified painter-mode.
   * The <code>aDrawAxisPath</code>-argument specifies if a hippodrome
   * needs to be drawn or only an axis (<code>aDrawAxisPath = true</code>);
   * @param aAWTPath the path to render on the given Graphics.
   * @param aGraphics the Graphics to render the path on.
   * @param aRenderingMode the state in which the object should be rendered.
   * @param aGXYContext the context in which to render the path for. This contains the layer for which this painter is
   * used.
   * @param aDrawAxisPath whether the axis should be drawn or not.
   */
  private void drawAWTPath(ILcdAWTPath aAWTPath,
                           Graphics aGraphics,
                           int aRenderingMode,
                           ILcdGXYContext aGXYContext,
                           boolean aDrawAxisPath) {
    if (aDrawAxisPath) {
      setupGraphicsForLine(aGraphics, aRenderingMode, aGXYContext);
      aAWTPath.drawPolyline(aGraphics);
    } else {
      int paint_mode = retrievePaintMode(aRenderingMode);
      switch (paint_mode) {
      case FILLED: {
        setupGraphicsForFill(aGraphics, aRenderingMode, aGXYContext);
        aAWTPath.fillPolygon(aGraphics);
        break;
      }
      case OUTLINED_FILLED: {
        setupGraphicsForFill(aGraphics, aRenderingMode, aGXYContext);
        aAWTPath.fillPolygon(aGraphics);
        setupGraphicsForLine(aGraphics, aRenderingMode, aGXYContext);
        aAWTPath.drawPolyline(aGraphics);
        break;
      }
      case OUTLINED:
      default: {
        setupGraphicsForLine(aGraphics, aRenderingMode, aGXYContext);
        aAWTPath.drawPolyline(aGraphics);
      }
      }
    }
  }

  private int retrievePaintMode(int aRenderingMode) {
    if ((aRenderingMode & ILcdGXYPainter.CREATING) != 0 ||
        (aRenderingMode & ILcdGXYPainter.RESHAPING) != 0 ||
        (aRenderingMode & ILcdGXYPainter.TRANSLATING) != 0) {
      return getEditMode();
    }
    if ((aRenderingMode & ILcdGXYPainter.SELECTED) != 0) {
      return getSelectionMode();
    }
    return getMode();
  }

  /**
   * Private class used as cache-element in the cache of a <code>IHippodrome</code>
   */
  private static class HippodromeCache {

    private ILcdXYWorldReference fXYWorldReference;
    private TLcdGeneralPath fGeneralPath;

    public HippodromeCache(ILcdXYWorldReference aXYWorldReference, TLcdGeneralPath aGeneralPath) {
      fXYWorldReference = aXYWorldReference;
      fGeneralPath = aGeneralPath;
    }

    /**
     * Checks whether the cache is valid for the given world reference.
     * Note that this method is not responsible for checking whether the object itself has changed. Hippodrome objects
     * clear the cache whenever they have been modified.
     *
     * @param aXYWorldReference the world reference for which we would like to retrieve a path.
     * @return true if the set generalPath is non-null and the set world-reference is (still) the same as the one
     *         currently used in the GXYView of <code>aGXYContext</code>
     */
    public boolean isValid(ILcdXYWorldReference aXYWorldReference) {

      return (fGeneralPath != null)
             && (fXYWorldReference != null)
             && (fXYWorldReference.equals(aXYWorldReference));
    }

    public void clear() {
      fGeneralPath = null;
    }
  }
}

