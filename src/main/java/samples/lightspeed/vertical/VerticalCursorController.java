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
package samples.lightspeed.vertical;

import static samples.gxy.vertical.VerticalCursorController.updateVerticalCursor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.query.TLspIsTouchedQuery;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.vertical.TLcdVVJPanel;

import samples.gxy.vertical.VerticalCursorController.CursorChangeEventListener;

/**
 * Controller that updates the associated vertical view's cursor position when moving over the selected object.
 * In addition, a marker is shown in the Lightspeed view when dragging the vertical view cursor.
 * It assumes that the selected objects are painted as geodesic polylines.
 */
public class VerticalCursorController extends ALspController {

  private final ILcdEllipsoid fEllipsoid;
  private final TLcdVVJPanel fVVPanel;
  private final ILspInteractivePaintableLayer fSelectionLayer;
  private final ILspLayer fMarkerLayer;

  /**
   * Creates a new controller that updates the given vertical view panel for any selected object
   * of the given layer.
   * @param aVVPanel        the vertical view to update
   * @param aSelectionLayer the layer containing the corresponding model objects.
   */
  public VerticalCursorController(TLcdVVJPanel aVVPanel, ILspInteractivePaintableLayer aSelectionLayer) {
    fSelectionLayer = aSelectionLayer;
    fVVPanel = aVVPanel;
    ILcdModelReference modelReference = aSelectionLayer.getModel().getModelReference();
    fEllipsoid = ((ILcdGeoReference) modelReference).getGeodeticDatum().getEllipsoid();
    setShortDescription("Click on a flight to see its vertical view");

    // set up a marker in the Lightspeed view
    ILcdModel model = new TLcdVectorModel(modelReference, new TLcdModelDescriptor(null, "cursor", "Vertical cursor"));
    TLcdLonLatPoint marker = new TLcdLonLatPoint();
    model.addElement(marker, ILcdModel.FIRE_NOW);

    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.CROSS, 10, Color.BLACK);
    icon.setAntiAliasing(true);
    TLspIconStyle iconStyle = TLspIconStyle.newBuilder().icon(icon).build();
    fMarkerLayer = TLspShapeLayerBuilder.newBuilder().model(model).bodyStyles(TLspPaintState.REGULAR, iconStyle).build();
    aVVPanel.addCursorChangeListener(new CursorChangeEventListener(fEllipsoid, marker, model));
  }

  @Override
  public void startInteraction(ILspView aView) {
    super.startInteraction(aView);
    aView.addLayer(fMarkerLayer);
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    aView.removeLayer(fMarkerLayer);
    super.terminateInteraction(aView);
  }

  @Override
  public void handleAWTEvent(AWTEvent aAWTEvent) {
    Point mouseLocation = null;
    if (aAWTEvent instanceof MouseEvent && aAWTEvent.getID() == MouseEvent.MOUSE_MOVED) {
      MouseEvent mouseEvent = (MouseEvent) aAWTEvent;
      mouseLocation = mouseEvent.getPoint();
    }
    super.handleAWTEvent(aAWTEvent);

    // check if we're nearby the vertical view's model
    if (mouseLocation != null && fSelectionLayer.getSelectionCount() == 1) {
      TLspContext context = new TLspContext(fSelectionLayer, getView());
      Boolean touched = fSelectionLayer.query(new TLspIsTouchedQuery(
          fSelectionLayer.selectedObjects().nextElement(), TLspPaintRepresentation.BODY,
          new TLcdXYPoint(mouseLocation.getX(), mouseLocation.getY()), 10), context);
      if (touched) {
        TLcdLonLatPoint modelPoint = getModelLocation(new TLcdXYPoint(mouseLocation.getX(), mouseLocation.getY()), context);
        if (modelPoint != null) {
          updateVerticalCursor(modelPoint, fVVPanel, fEllipsoid);
        }
      }
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    return aAWTEvent;
  }

  /**
   * Returns the model coordinates of the given AWT point.
   * @param aViewPoint a point in AWT coordinates
   * @return the corresponding point in model coordinates
   */
  private TLcdLonLatPoint getModelLocation(ILcdPoint aViewPoint, TLspContext aContext) {
    TLcdXYZPoint world_point = new TLcdXYZPoint();
    aContext.getViewXYZWorldTransformation().viewPoint2WorldSFCT(aViewPoint, world_point);
    try {
      TLcdLonLatHeightPoint model_point = new TLcdLonLatHeightPoint();
      ILcdModelXYZWorldTransformation m2w = aContext.getModelXYZWorldTransformation();
      m2w.worldPoint2modelSFCT(world_point, model_point);
      return new TLcdLonLatPoint(model_point);
    } catch (TLcdOutOfBoundsException e) {
      // Do nothing
    }
    return null;
  }
}
