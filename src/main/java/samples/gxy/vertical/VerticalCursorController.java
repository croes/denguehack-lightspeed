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
package samples.gxy.vertical;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoidUtil;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.controller.TLcdGXYSelectController2;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;
import com.luciad.view.vertical.ILcdVVCursorChangeEventListener;
import com.luciad.view.vertical.ILcdVVModel;
import com.luciad.view.vertical.TLcdVVCursorChangeEvent;
import com.luciad.view.vertical.TLcdVVJPanel;

import samples.gxy.shortestDistancePainter.ShortestDistanceShape;

/**
 * Selection controller that updates the associated vertical view's cursor position when moving over the select object.
 * In addition, a marker is shown in the GXY view when dragging the vertical view cursor.
 * It assumes that the selected objects are painted as geodesic polylines.
 */
public class VerticalCursorController extends TLcdGXYSelectController2 {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(VerticalCursorController.class.getName());

  private final TLcdGeodetic2Grid fMWT;
  private final ILcdEllipsoid fEllipsoid;
  private final TLcdVVJPanel fVVPanel;
  private final ILcdGXYLayer fSelectionLayer;
  private final TLcdGXYLayer fMarkerLayer;

  /**
   * Creates a new controller that updates the given vertical view panel for any selected object of the given layer.
   * @param aVVPanel        the vertical view to update
   * @param aSelectionLayer the layer containing the corresponding model objects. The model's reference must be
   *                        a {@link TLcdGeodeticReference}.
   */
  public VerticalCursorController(TLcdVVJPanel aVVPanel, ILcdGXYLayer aSelectionLayer) {
    fVVPanel = aVVPanel;
    fSelectionLayer = aSelectionLayer;
    fMWT = new TLcdGeodetic2Grid();
    ILcdModelReference modelReference = aSelectionLayer.getModel().getModelReference();
    fEllipsoid = ((TLcdGeodeticReference) modelReference).getGeodeticDatum().getEllipsoid();
    setShortDescription("Click on a flight to see its vertical view");
    // disable multiple selection
    setMouseDraggedSensitivity(Integer.MAX_VALUE);

    // set up a marker in the GXY view
    ILcdModel model = new TLcdVectorModel(modelReference, new TLcdModelDescriptor(null, "cursor", "Vertical cursor"));
    TLcdLonLatPoint marker = new TLcdLonLatPoint();
    model.addElement(marker, ILcdModel.FIRE_NOW);
    fMarkerLayer = new TLcdGXYLayer(model, "Vertical cursor");
    TLcdGXYShapePainter painterProvider = new TLcdGXYShapePainter();
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.CROSS, 10, Color.BLACK);
    icon.setAntiAliasing(true);
    painterProvider.setIcon(icon);
    painterProvider.setSelectedIcon(null);
    fMarkerLayer.setGXYPainterProvider(painterProvider);
    aVVPanel.addCursorChangeListener(new CursorChangeEventListener(fEllipsoid, marker, model));
  }

  @Override
  public void startInteraction(ILcdGXYView aGXYView) {
    super.startInteraction(aGXYView);
    aGXYView.addGXYLayer(fMarkerLayer);
  }

  @Override
  public void terminateInteraction(ILcdGXYView aGXYView) {
    aGXYView.removeLayer(fMarkerLayer);
    super.terminateInteraction(aGXYView);
  }

  @Override
  protected int selectHowMode(ILcdGXYView aGXYView, Rectangle aSelectionBounds, int aMouseMode, MouseEvent aMouseEvent, int aSelectByWhatMode) {
    // disable multiple selection
    return TLcdGXYSelectControllerModel2.SELECT_HOW_FIRST_TOUCHED;
  }

  @Override
  public void mouseMoved(MouseEvent me) {
    super.mouseMoved(me);

    if (fSelectionLayer.getSelectionCount() == 1) {
      // check if we're nearby
      Object selection = fSelectionLayer.selectedObjects().nextElement();
      TLcdGXYContext context = new TLcdGXYContext(getGXYView(), fSelectionLayer);
      context.setX(me.getX());
      context.setY(me.getY());
      context.setSensitivity(10);
      if (fSelectionLayer.getGXYPainter(selection).isTouched(getGXYView().getGraphics(), ILcdGXYPainter.BODY, context)) {
        TLcdLonLatPoint point = getModelLocation(me.getPoint());
        if (point != null) {
          updateVerticalCursor(point, fVVPanel, fEllipsoid);
        }
      }
    }
  }

  /**
   * Updates the vertical cursor, based on the given model location.
   *
   * @param aPoint        a model point
   * @param aVerticalView the vertical view for which tho update the vertical cursor
   * @param aEllipsoid    the ellipsoid
   */
  public static void updateVerticalCursor(TLcdLonLatPoint aPoint, TLcdVVJPanel aVerticalView, ILcdEllipsoid aEllipsoid) {
    // get a point on the geodesic polyline defined by the vertical view's model
    TLcdLonLatPolyline profilePolyline = getProfilePolyline(aVerticalView.getVVModel(), aEllipsoid);
    if (profilePolyline == null) {
      return;
    }
    ShortestDistanceShape shape = new ShortestDistanceShape(profilePolyline, aPoint, aEllipsoid);
    TLcdLonLatPoint pointSFCT = new TLcdLonLatPoint();
    shape.closestPointOnPolylineSFCT(aPoint, pointSFCT);

    // update the cursor
    int profilePointIndex = getProfilePointIndex(pointSFCT, aVerticalView.getVVModel(), aEllipsoid);
    if (profilePointIndex >= 0) {
      double percentage = getProfilePathPercentage(profilePointIndex, pointSFCT, aVerticalView.getVVModel(), aEllipsoid);
      aVerticalView.setVerticalCursorVisible(true);
      aVerticalView.setVerticalCursorLeftPointIndex(profilePointIndex);
      aVerticalView.setVerticalCursorPercentage(percentage);
    }
  }

  /**
   * Retrieves the percentage value (a real value between 0 and 1) of the given point on a parameterized geodesic line
   * defined by the given main profile point and the next one.
   * @param aProfilePointIndex defines, together with the next profile point, a parameterized geodesic line
   * @param aPoint             the point to retrieve the parameter value of. It must lie on the geodesic line.
   * @param aModel             the vertical view model
   * @param aEllipsoid         the ellipsoid
   * @return a parameter defining how far the given point is located starting from the given main profile point
   */
  public static double getProfilePathPercentage(int aProfilePointIndex, ILcdPoint aPoint, ILcdVVModel aModel, ILcdEllipsoid aEllipsoid) {
    final ILcdPoint baseProfilePoint = aModel.getPoint(aProfilePointIndex);
    double distance = aEllipsoid.geodesicDistance(baseProfilePoint, aPoint);
    double total_distance = aEllipsoid.geodesicDistance(baseProfilePoint, aModel.getPoint(aProfilePointIndex + 1));
    return distance / total_distance;
  }

  /**
   * Returns the index of the main profile point that, together with the next profile point, defines a geodesic line
   * containing the given point.
   * @param aPoint     a point that lies on some geodesic line defined by two consecutive profile points
   * @param aModel     the vertical view model
   * @param aEllipsoid the ellipsoid
   * @return the index of the first profile point defining a line containing the given point
   */
  public static int getProfilePointIndex(ILcdPoint aPoint, ILcdVVModel aModel, ILcdEllipsoid aEllipsoid) {
    ILcdPoint profile_point1;
    ILcdPoint profile_point2 = aModel.getPoint(0);

    for (int index = 1; index < aModel.getPointCount(); index++) {
      profile_point1 = profile_point2;
      profile_point2 = aModel.getPoint(index);
      if (TLcdEllipsoidUtil.contains2D(aEllipsoid, profile_point2, profile_point1, aPoint)) {
        return index - 1;
      }
    }
    if (sLogger.isDebugEnabled()) {
      sLogger.debug("Cannot find a geodesic line containing the given point.");
    }
    return -1;
  }

  /**
   * Returns the model coordinates of the given AWT point.
   * @param aPoint a point in AWT coordinates
   * @return the corresponding point in geodesic model coordinates
   */
  private TLcdLonLatPoint getModelLocation(Point aPoint) {
    TLcdXYPoint world_point = new TLcdXYPoint();
    getGXYView().getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(aPoint, world_point);
    try {
      TLcdLonLatHeightPoint model_point = new TLcdLonLatHeightPoint();
      fMWT.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
      fMWT.setXYWorldReference(getGXYView().getXYWorldReference());
      fMWT.worldPoint2modelSFCT(world_point, model_point);
      return new TLcdLonLatPoint(model_point);
    } catch (TLcdOutOfBoundsException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns a geodesic polyline defined by the vertical view's model.
   *
   * @param aModel     the vertical view model
   * @param aEllipsoid the ellipsoid
   *
   * @return a polyline defined by the vertical view's model
   */
  public static TLcdLonLatPolyline getProfilePolyline(ILcdVVModel aModel, ILcdEllipsoid aEllipsoid) {
    if (aModel != null && aModel.getPointCount() > 1) {
      final TLcdLonLatPolyline line = new TLcdLonLatPolyline();
      line.setEllipsoid(aEllipsoid);
      for (int i = 0; i < aModel.getPointCount(); i++) {
        final ILcdPoint point = aModel.getPoint(i);
        line.insert2DPoint(line.getPointCount(), point.getX(), point.getY());
      }
      return line;
    }
    return null;
  }

  // moves the marker to the current vertical cursor position
  public static class CursorChangeEventListener implements ILcdVVCursorChangeEventListener {
    private final ILcdEllipsoid fEllipsoid;
    private final TLcdLonLatPoint fMarker;
    private final ILcdModel fModel;

    public CursorChangeEventListener(ILcdEllipsoid aEllipsoid, TLcdLonLatPoint aMarker, ILcdModel aModel) {
      fEllipsoid = aEllipsoid;
      fMarker = aMarker;
      fModel = aModel;
    }

    @Override
    public void cursorChange(TLcdVVCursorChangeEvent aEvent) {
      // update the marker in the GXY view
      ILcdPoint left = aEvent.getLeftPoint();
      ILcdPoint rightPoint = aEvent.getRightPoint();
      try (TLcdLockUtil.Lock autolock = TLcdLockUtil.writeLock(fModel)) {
        fEllipsoid.geodesicPointSFCT(left, rightPoint, aEvent.getPercentage(), fMarker);
      }
      fModel.elementChanged(fMarker, ILcdModel.FIRE_NOW);
    }
  }
}
