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
package samples.gxy.transformation.mouseToGeodetic;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModelReference;
import com.luciad.projection.TLcdPolarStereographic;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdDefaultModelXYWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.controller.TLcdGXYDragRectangleController;

/**
 * When the user drags a rectangle, it is transformed into world and view
 * coordinates. The last mouse drag/mouse click location is also transformed into some arbitrary model reference.
 */
public class TransformCoordinatesController extends TLcdGXYDragRectangleController {
  private JTextArea fBoundsOutput;
  private JTextArea fPointOutput;

  public TransformCoordinatesController(JTextArea aBoundsOutput, JTextArea aPointOutput) {
    fBoundsOutput = aBoundsOutput;
    fPointOutput = aPointOutput;
    setName("Transformed dragged area to model coordinates");
    setShortDescription("Drag a rectangle on the map and see its model coordinates");
    setIcon(new TLcdImageIcon("images/gui/i16_cursorlocation.gif"));
    setDragCentered(false);
  }

  @Override
  public void mouseDragged(MouseEvent me) {
    super.mouseDragged(me);
    updateBounds(me);
  }

  @Override
  public void mouseClicked(MouseEvent me) {
    super.mouseClicked(me);
    updateBounds(me);
  }

  private void updateBounds(MouseEvent me) {
    Rectangle rectangle = getCurrentRectangle();
    // See javadoc of super
    if (rectangle != null) {
      convertPoint(me.getPoint());
      if (rectangle.width >= getMouseDraggedSensitivity() ||
          rectangle.height >= getMouseDraggedSensitivity()) {
        convertBounds(rectangle);
      }
    }
  }

  private void convertPoint(Point aViewPoint) {
    // We use an arbitrary model reference: a grid reference with polar projection for the
    // north pole (it can't express locations on the southern hemisphere)
    ILcdModelReference modelReference = new TLcdGridReference(
        new TLcdGeodeticDatum(), new TLcdPolarStereographic(TLcdPolarStereographic.NORTH_POLE));

    ILcdPoint modelPoint = null;
    try {
      modelPoint = getModelPointForViewPoint(aViewPoint.getLocation(), getGXYView(), modelReference);
    } catch (TLcdOutOfBoundsException e) {
      // This happens when the given view bounds can't be transformed to model coordinates.
      // For example:
      // - dragging outside the projection area
      // - when the model coordinate system can't express certain locations on the earth,
      //   for example a TLcdGridReference with a TLcdPolarStereographic projection for the
      //   north pole can't express coordinates at the south pole.
    }
    fPointOutput.setText(formatPoint(aViewPoint.getLocation(), modelPoint));
  }

  private void convertBounds(Rectangle aViewBounds) {
    // We just use the model and transformation of the first layer of the view
    ILcdGXYLayer layer = (ILcdGXYLayer) getGXYView().getLayer(0);
    ILcdBounds modelBounds = null;
    try {
      modelBounds = getModelBoundsForViewBounds(aViewBounds, getGXYView(), layer);
    } catch (TLcdOutOfBoundsException e) {
      //Ignore, see convertPoint
    }
    fBoundsOutput.setText(formatBounds(layer, aViewBounds, modelBounds));
  }

  private String formatPoint(Point aViewPoint, ILcdPoint aModelPoint) {
    StringBuilder builder = new StringBuilder();
    builder.append("Model point for view ").append(aViewPoint);
    builder.append(" in grid reference with polar stereographic (north pole):\n\n");

    if (aModelPoint != null) {
      builder.append("x = ").append(aModelPoint.getX()).append("\n");
      builder.append("y = ").append(aModelPoint.getY());
    } else {
      builder.append("Can't be expressed in this model reference (only northern hemisphere is possible)");
    }
    return builder.toString();
  }

  private String formatBounds(ILcdGXYLayer aLayer, Rectangle aViewBounds, ILcdBounds aModelBounds) {
    StringBuilder builder = new StringBuilder();
    builder.append("Model bounds for view ").append(aViewBounds).append(" in ").
        append(aLayer.getModel().getModelReference()).append(" reference:\n\n");

    if (aModelBounds != null) {
      builder.append("x = ").append(aModelBounds.getLocation().getX()).append("\n");
      builder.append("y = ").append(aModelBounds.getLocation().getY()).append("\n");
      builder.append("width = ").append(aModelBounds.getWidth()).append("\n");
      builder.append("height = ").append(aModelBounds.getHeight());
    } else {
      builder.append("Can't be converted to this reference");
    }
    return builder.toString();
  }


  /**
   * Computes a bounding box in model coordinates that corresponds to the given
   * rectangle in view coordinates.
   *
   * @param aViewBounds The rectangle in view coordinates (pixels)
   * @param aView  the ILcdGXYView on which the rectangle is located
   * @param aLayer the ILcdGXYLayer for whose model we're computing the bounds
   * @return an ILcdBounds in model coordinates
   * @throws TLcdOutOfBoundsException
   *          if the world to model transformation is unsuccessful
   */
  private ILcdBounds getModelBoundsForViewBounds(
      Rectangle aViewBounds,
      ILcdGXYView aView,
      ILcdGXYLayer aLayer
                                                ) throws TLcdOutOfBoundsException {

    // The (2D) world coordinate system works with XY coordinates.
    TLcdXYBounds worldBounds = new TLcdXYBounds();

    // The model coordinate system isn't known. The model reference helps
    // to create compatible bounds (e.g. XY or LonLat).
    ILcdPoint modelPoint = aLayer.getModel().getModelReference().makeModelPoint();
    ILcd3DEditableBounds modelBounds = modelPoint.getBounds().cloneAs3DEditableBounds();

    // We use TLcdGXYContext to easily obtain the appropriate transformations.
    TLcdGXYContext context = new TLcdGXYContext(aView, aLayer);

    // Transform from view to world and from world to model coordinates.
    // Various other methods are available on the transformations, for example to transform
    // just points.
    context.getGXYViewXYWorldTransformation().viewAWTBounds2worldSFCT(aViewBounds, worldBounds);
    context.getModelXYWorldTransformation().worldBounds2modelSFCT(worldBounds, modelBounds);

    return modelBounds;
  }

  /**
   * Converts the given point in view coordinates to a point in the coordinate system of the given
   * model reference.
   *
   * @param aViewPoint The point in view coordinates (pixels)
   * @param aView  the ILcdGXYView on which the point is located
   * @param aModelReference the model reference to convert the point to
   * @return an ILcdPoint in model coordinates
   * @throws TLcdOutOfBoundsException
   *          if the world to model transformation is unsuccessful
   */
  private ILcdPoint getModelPointForViewPoint(
      Point aViewPoint,
      ILcdGXYView aView,
      ILcdModelReference aModelReference
                                             ) throws TLcdOutOfBoundsException {

    // The (2D) world coordinate system works with XY coordinates.
    TLcdXYPoint worldPoint = new TLcdXYPoint();

    // The model coordinate system isn't known. The model reference helps
    // to create a compatible point (e.g. XY or LonLat).
    ILcd3DEditablePoint modelPoint = aModelReference.makeModelPoint().cloneAs3DEditablePoint();

    // Initialize the model-world transformation, it supports all common references.
    TLcdDefaultModelXYWorldTransformation transfo = new TLcdDefaultModelXYWorldTransformation(
        aModelReference,
        aView.getXYWorldReference()
    );

    // Transform from view to world and from world to model coordinates.
    aView.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(aViewPoint, worldPoint);
    transfo.worldPoint2modelSFCT(worldPoint, modelPoint);

    return modelPoint;
  }
}
