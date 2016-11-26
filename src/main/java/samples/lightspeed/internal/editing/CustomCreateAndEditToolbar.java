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
package samples.lightspeed.internal.editing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.shape.ILcdShape;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.editor.TLspCreateCurveEditorModel;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.lightspeed.common.CompositeCurveTypeChooserPanel;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.PanelVisibilityAction;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * @author Daniel Balog
 * @since 2012.0
 */
public class CustomCreateAndEditToolbar extends CreateAndEditToolBar {

  private boolean fLonLat;
  private List<XYCreateControllerModel> fXYControllerModels;
  private boolean fHasAShape;

  public CustomCreateAndEditToolbar(
      ILspView aView,
      Component aParent,
      ButtonGroup aButtonGroup,
      ILspController aFallbackController,
      boolean aIncludeCreateControllers,
      boolean aFlatShapesOnly,
      boolean aTouchEnabled,
      ILspInteractivePaintableLayer aCreationLayer,
      boolean aLonLat,
      boolean aHasAShape
  ) {
    super(aView, aParent, aButtonGroup, aIncludeCreateControllers, aFlatShapesOnly, aTouchEnabled, aCreationLayer);
    fHasAShape = aHasAShape;
    if (!aLonLat) {
      init();
    }
  }

  @Override
  protected void createControllers() {
    //Do nothing, see init instead.
  }

  private void init() {
    addSpace();

    addXYCreateControllers();
  }

  /**
   * Creates and adds Creation controllers for several shape types to the toolbar.
   */
  private void addXYCreateControllers() {

    ILspInteractivePaintableLayer layer = getCreationLayer();

    ILspEditor editor = layer.getEditor(TLspPaintRepresentation.BODY);
    PanelVisibilityAction panelVisibilityAction = null;
    if (editor instanceof TLspShapeEditor) {
      CompositeCurveTypeChooserPanel panel = new CompositeCurveTypeChooserPanel((TLspCreateCurveEditorModel) ((TLspShapeEditor) editor).getCompositeCurveCreateModel());
      if (getView() instanceof ILspAWTView) {
        //Add composite curve buttons to view overlay panel.
        ((ILspAWTView) getView()).getOverlayComponent().add(panel, TLcdOverlayLayout.Location.NORTH_WEST);
      }
      panelVisibilityAction = new PanelVisibilityAction(panel, "Creation Started", "Creation Stopped");
      //Make invisible and use action to make it visible
      panel.setVisible(false);
    }

    // For each shape type, add both a regular- and an extruded shape creation controller
    addCreateController(layer, XYCreateControllerModel.Type.TEXT, TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.POINT2D, TLcdIconFactory.create(TLcdIconFactory.DRAW_POINT_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.BOUNDS, TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON), null);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.POLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.POLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.COMPLEXPOLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), null);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.CIRCLE, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.CIRCLE_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_3_POINTS_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ELLIPSE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON), null);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.ARC, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ARC_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ARC_BY_BULGE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ARC_BY_CENTER, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_CENTERPOINT_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ARCBAND, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON), null);
    addCreateController(layer, XYCreateControllerModel.Type.ARC_BAND_3D, TLcdIconFactory.create(TLcdIconFactory.DRAW_3D_ARC_BAND_ICON), null);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.COMPOSITECURVE, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_NON_CLOSED_ICON), panelVisibilityAction);
    addCreateController(layer, XYCreateControllerModel.Type.COMPOSITERING, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_CLOSED_ICON), panelVisibilityAction);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.GEOBUFFER, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), null);
    addSpace();
    addCreateController(layer, XYCreateControllerModel.Type.POLYLINE_3D, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), null);

  }

  @Override
  protected void setCreateExtrudedShapes(boolean aExtruded) {
    for (XYCreateControllerModel xyControllerModel : fXYControllerModels) {
      xyControllerModel.setCreateExtrudedShape(aExtruded);
    }
  }

  /**
   * Creates and adds a creation controller for the given type to the toolbar. Navigation
   * functionality is also added, to allow the user to pan, zoom and rotate the view when the
   * controller is active.
   *
   * @param aLayer  the layer to be used by the controller model
   * @param aType   the type of shape that the controller model will be creating
   * @param aIcon   a representative icon for display in the toolbar
   * @param aAction the action to call when creation is started (using command "Creation Started") or stopped (using command "Creation Stopped")
   */
  private void addCreateController(
      final ILspInteractivePaintableLayer aLayer,
      XYCreateControllerModel.Type aType,
      ILcdIcon aIcon,
      final ILcdAction aAction
  ) {

    // Each creation controller must have a controller model (in this case a lon-lat creation model)
    XYCreateControllerModel cm = new XYCreateControllerModel(aType, aLayer) {
      @Override
      public Object create(ILspView aView, ILspLayer aLayer) {
        Object o = super.create(aView, aLayer);
        if (fHasAShape && (o != null) && (o instanceof ILcdShape)) {
          o = new HasAShape((ILcdShape) o);
        }
        return o;
      }
    };
    if (fXYControllerModels == null) {
      fXYControllerModels = new ArrayList<XYCreateControllerModel>();
    }
    fXYControllerModels.add(cm);

    // Create and initialize creation controller
    TLspCreateController createController = new TLspCreateController(cm) {
      @Override
      public void startInteraction(ILspView aView) {
        //Notify action of creation started
        if (aAction != null) {
          aAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Started"));
        }
        super.startInteraction(aView);
      }

      @Override
      public void terminateInteraction(ILspView aView) {
        //Notify action of creation started
        if (aAction != null) {
          aAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Stopped"));
        }
        super.terminateInteraction(aView);
      }
    };

    createController.addUndoableListener(getUndoManager());
    createController.setShortDescription(aType.toString());
    createController.setIcon(aIcon);
    createController.setActionToTriggerAfterCommit(
        new TLspSetControllerAction(getView(), getDefaultController())
    );

    createController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        leftMouseButton().or().
                                                               rightMouseButton().or().
                                                               keyEvents().build());

    ALspController navigation = ControllerFactory.createNavigationController();

    createController.appendController(navigation);

    final AbstractButton createdButton;
    createdButton = addController(createController);
    aLayer.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("visible".equals(evt.getPropertyName())) {
          //Adjust the enabled state of the button based on the visibility of the layer
          createdButton.setEnabled(aLayer.isVisible());
        }
      }
    });
    getView().addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(TLcdLayeredEvent e) {
        if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED && e.getLayer() == aLayer) {
          //disable button when layer is removed from view
          createdButton.setEnabled(false);
        } else if (e.getID() == TLcdLayeredEvent.LAYER_ADDED && e.getLayer() == aLayer) {
          //enable button when layer is added back to view
          createdButton.setEnabled(true);
        }
      }
    });
  }

}
