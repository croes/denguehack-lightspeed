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
package samples.lightspeed.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.input.touch.TLcdTouchDevice;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.editor.TLspCreateCurveEditorModel;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.common.model.GeodeticModelFactory;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.common.controller.LonLatCreateControllerModel;
import samples.lightspeed.common.touch.MouseToTouchControllerWrapper;
import samples.lightspeed.common.touch.TouchCreateController;

/**
 * ToolBar that has controllers with shape creation and editing capabilities. <p/> The controller
 * models for creation use the layer provided by this toolbar.
 */
public class CreateAndEditToolBar extends ToolBar {

  // Layer used to create shapes in.
  private ILspInteractivePaintableLayer fCreationLayer;

  private final boolean fIncludeCreateControllers;
  private ArrayList<LonLatCreateControllerModel> fControllerModels;
  private boolean fTouchEnabled;
  private Container fTouchButtonContainer;

  /**
   * Creates a new CreateAndEdit toolbar with given view and parent.
   *
   * @param aView              the view for which the toolbar is created
   * @param aParent            the parent owner of the toolbar
   * @param aButtonGroup       a button group the which the controller buttons will be added, if
   *                           this parameter is null a new button group will be created
   */
  public CreateAndEditToolBar(ILspView aView,
                              Component aParent,
                              ButtonGroup aButtonGroup) {
    this(aView, aParent, aButtonGroup, true, false, false);
  }

  /**
   * Creates a new CreateAndEdit toolbar with given view and parent, as well as the option to have
   * with or without create controllers, as well as flat shapes only, or touch enabled.
   *
   * @param aView                     the view for which the toolbar is created
   * @param aParent                   the parent owner of the toolbar
   * @param aButtonGroup              a button group the which the controller buttons will be added,
   *                                  if this parameter is null a new button group will be created
   * @param aIncludeCreateControllers whether or not create controllers should be created
   * @param aFlatShapesOnly           whether or not there should be flat shapes only
   * @param aTouchEnabled             whether or not touch is enabled as the main control device.
   */
  public CreateAndEditToolBar(ILspView aView,
                              Component aParent,
                              ButtonGroup aButtonGroup,
                              boolean aIncludeCreateControllers,
                              boolean aFlatShapesOnly,
                              boolean aTouchEnabled) {
    this(aView, aParent, aButtonGroup, aIncludeCreateControllers, aFlatShapesOnly, aTouchEnabled, null);
  }

  /**
   * Creates a new CreateAndEdit toolbar with given view and parent, as well as the option to have
   * with or without create controllers, as well as flat shapes only, or touch enabled. <p/> This
   * constructor also allows you to provide a custom creation layer
   *
   * @param aView                     the view for which the toolbar is created.
   * @param aParent                   the parent owner of the toolbar
   * @param aButtonGroup              a button group the which the controller buttons will be added,
   *                                  if this parameter is null a new button group will be created
   * @param aIncludeCreateControllers whether or not create controllers should be created
   * @param aFlatShapesOnly           whether or not there should be flat shapes only
   * @param aTouchEnabled             whether or not touch is enabled as the main control device.
   * @param aCreationLayer            a custom creation layer
   */
  public CreateAndEditToolBar(ILspView aView,
                              Component aParent,
                              ButtonGroup aButtonGroup,
                              boolean aIncludeCreateControllers,
                              boolean aFlatShapesOnly,
                              boolean aTouchEnabled,
                              ILspInteractivePaintableLayer aCreationLayer) {
    super(aButtonGroup);
    fIncludeCreateControllers = aIncludeCreateControllers;
    fCreationLayer = aCreationLayer;
    fControllerModels = new ArrayList<>();
    fTouchEnabled = aTouchEnabled;
    if (aParent != null && aParent instanceof LightspeedSample) {
      fTouchButtonContainer = ((LightspeedSample) aParent).getOverlayPanel();
    }
    init(aView, aParent, false, true);
    if (aIncludeCreateControllers && !aFlatShapesOnly) {
      addExtrusionToggleButton();
    }

    TLcdUndoAction undoAction = new TLcdUndoAction(getUndoManager());
    TLcdRedoAction redoAction = new TLcdRedoAction(getUndoManager());

    addAction(undoAction, FILE_GROUP);
    addAction(redoAction, FILE_GROUP);
  }

  protected void addExtrusionToggleButton() {
    final JToggleButton button = new JToggleButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.EXTRUDE_ICON)));
    button.setToolTipText("Toggles between extruded shape creation mode and normal shape creation mode.");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final boolean extruded = button.isSelected();
        setCreateExtrudedShapes(extruded);
      }
    });
    add(button);
  }

  /**
   * Propagates a state change in the extrusion toggle button to the create controller models.
   *
   * @param aExtruded indicates whether or not extruded shapes should be created
   */
  protected void setCreateExtrudedShapes(boolean aExtruded) {
    for (LonLatCreateControllerModel controllerModel : fControllerModels) {
      controllerModel.setCreateExtrudedShape(aExtruded);
    }
  }

  /**
   * Gets the creation layer associated to the new controllers for creating flat shapes.
   *
   * @return the layer used by the controller models to create new flat shapes in
   */
  public ILspInteractivePaintableLayer getCreationLayer() {
    if (fCreationLayer == null) {
      ILcdModel emptyModel = GeodeticModelFactory.createEmptyModel();
      fCreationLayer = (ILspInteractivePaintableLayer)
          LspDataUtil.instance().model(emptyModel).layer().editable(true).getLayer();
    }
    return fCreationLayer;
  }

  /**
   * Adds create controllers for several LuciadLightspeed shapes to the default controllers
   * (navigation, selection) for a Lightspeed sample toolbar.
   */
  @Override
  protected void createControllers() {
    // Add new controllers, if requested.
    if (fIncludeCreateControllers) {
      addCreateControllers();
    }
  }

  /**
   * Creates and adds Creation controllers for several shape types to the toolbar.
   */
  private void addCreateControllers() {

    ILspInteractivePaintableLayer layer = getCreationLayer();

    ILspEditor editor = layer.getEditor(TLspPaintRepresentation.BODY);
    PanelVisibilityAction panelVisibilityAction = null;
    if (editor instanceof TLspShapeEditor) {
      CompositeCurveTypeChooserPanel panel = new CompositeCurveTypeChooserPanel((TLspCreateCurveEditorModel) ((TLspShapeEditor) editor).getCompositeCurveCreateModel());
      if (getView() instanceof ILspAWTView && ((ILspAWTView) getView()).getOverlayComponent() != null) {
        //Add composite curve buttons to view overlay panel.
        ((ILspAWTView) getView()).getOverlayComponent().add(panel, TLcdOverlayLayout.Location.NORTH_WEST);
      }
      panelVisibilityAction = new PanelVisibilityAction(panel, "Creation Started", "Creation Stopped");
      //Make invisible and use action to make it visible
      panel.setVisible(false);
    }

    // For each shape type, add both a regular- and an extruded shape creation controller

    addCreateController(layer, LonLatCreateControllerModel.Type.TEXT, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON), null);

    addCreateController(layer, LonLatCreateControllerModel.Type.POINT2D, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_POINT_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.BOUNDS, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON), null);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.POLYLINE, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.RHUMB_POLYLINE, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_RHUMBLINE_POLYLINE_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.POLYGON, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.COMPLEXPOLYGON, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), null);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.CIRCLE, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.CIRCLE_BY_3_POINTS, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_3_POINTS_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ELLIPSE, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON), null);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.ARC, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ARC_BY_3_POINTS, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ARC_BY_BULGE, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ARC_BY_CENTER, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_CENTERPOINT_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ARCBAND, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.ARC_BAND_3D, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_3D_ARC_BAND_ICON), null);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.COMPOSITECURVE, true, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_NON_CLOSED_ICON), panelVisibilityAction);
    addCreateController(layer, LonLatCreateControllerModel.Type.COMPOSITERING, true, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_CLOSED_ICON), panelVisibilityAction);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.BUFFER, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.GEOBUFFER, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.LONLATHEIGHTBUFFER, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.VARIABLE_GEO_BUFFER, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_VARIABLE_BUFFER_ICON), null);
    addSpace();
    addCreateController(layer, LonLatCreateControllerModel.Type.POLYLINE_3D, true, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.DOME, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_DOME_ICON), null);
    addCreateController(layer, LonLatCreateControllerModel.Type.SPHERE, false, TLcdIconFactory.create(TLcdIconFactory.DRAW_SPHERE_ICON), null);

  }

  /**
   * Creates and adds a creation controller for the given type to the toolbar. Navigation
   * functionality is also added, to allow the user to pan, zoom and rotate the view when the
   * controller is active.
   *
   * @param aLayer  the layer to be used by the controller model
   * @param aType   the type of shape that the controller model will be creating
   * @param aWithCommitButton in case of touch editing a commit button can be added to the view to for example end polygon creation
   * @param aIcon   a representative icon for display in the toolbar
   * @param aAction the action to call when creation is started (using command "Creation Started")
   *                or stopped (using command "Creation Stopped")
   */
  private void addCreateController(final ILspInteractivePaintableLayer aLayer, LonLatCreateControllerModel.Type aType, boolean aWithCommitButton, ILcdIcon aIcon, final ILcdAction aAction) {

    // Each creation controller must have a controller model (in this case a lon-lat creation model)
    LonLatCreateControllerModel cm = new LonLatCreateControllerModel(aType, aLayer);
    fControllerModels.add(cm);

    // Create and initialize creation controller
    TLspCreateController createController = makeCreateController(cm, aAction, aWithCommitButton);

    createController.addUndoableListener(getUndoManager());
    createController.setShortDescription(aType.toString());
    createController.setIcon(aIcon);
    createController.setActionToTriggerAfterCommit(
        new TLspSetControllerAction(getView(), getDefaultController())
    );

    if (!fTouchEnabled) {
      createController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
          leftMouseButton().or().
                                                                 rightMouseButton().or().
                                                                 keyEvents().build());

      ALspController navigation = ControllerFactory.createNavigationController();

      createController.appendController(navigation);

    }
    final AbstractButton createdButton;
    if (fTouchEnabled &&
        TLcdTouchDevice.getInstance().getTouchDeviceStatus() != TLcdTouchDevice.Status.READY) {
      createdButton = addController(new MouseToTouchControllerWrapper(createController));

    } else {
      createdButton = addController(createController);
    }
    connectButtonToLayer(getView(), aLayer, createdButton);
  }

  public static void connectButtonToLayer(final ILspView aView,
                                          final ILspInteractivePaintableLayer aLayer,
                                          final AbstractButton aCreatedButton) {
    aLayer.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("visible".equals(evt.getPropertyName())) {
          //Adjust the enabled state of the button based on the visibility of the layer
          aCreatedButton.setEnabled(aLayer.isVisible());
        }
      }
    });
    aView.addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(TLcdLayeredEvent e) {
        if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED && e.getLayer() == aLayer) {
          //disable button when layer is removed from view
          aCreatedButton.setEnabled(false);
        } else if (e.getID() == TLcdLayeredEvent.LAYER_ADDED && e.getLayer() == aLayer) {
          //enable button when layer is added back to view
          aCreatedButton.setEnabled(true);
        }
      }
    });
  }

  /**
   * Makes a new create-controller based on a controller model.
   * @param aCreateControllerModel the controller model
   * @param aInteractionAction the action to perform when interaction has started or stopped
   * @param aWithCommitButton whether or not to include a commit button
   * @return the create controller
   */
  private TLspCreateController makeCreateController(final LonLatCreateControllerModel aCreateControllerModel, final ILcdAction aInteractionAction, final boolean aWithCommitButton) {
    TLspCreateController createController;
    if (fTouchEnabled) {
      createController = new TouchCreateController(fTouchButtonContainer, aCreateControllerModel, aWithCommitButton) {
        @Override
        protected void startInteractionImpl(ILspView aView) {
          //Notify action of creation started
          if (aInteractionAction != null) {
            aInteractionAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Started"));
          }
          super.startInteractionImpl(aView);
        }

        @Override
        public void terminateInteraction(ILspView aView) {
          //Notify action of creation started
          if (aInteractionAction != null) {
            aInteractionAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Stopped"));
          }
          super.terminateInteraction(aView);
        }
      };
    } else {
      createController = new TLspCreateController(aCreateControllerModel) {
        @Override
        public void startInteraction(ILspView aView) {
          //Notify action of creation started
          if (aInteractionAction != null) {
            aInteractionAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Started"));
          }
          super.startInteraction(aView);
        }

        @Override
        public void terminateInteraction(ILspView aView) {
          //Notify action of creation started
          if (aInteractionAction != null) {
            aInteractionAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Creation Stopped"));
          }
          super.terminateInteraction(aView);
        }
      };
    }
    return createController;
  }

  /**
   * Creates and adds a creation controller for the given type to the toolbar. Navigation
   * functionality is also added, to allow the user to pan, zoom and rotate the view when the
   * controller is active.
   *
   * @param aCreateControllerModel the controller model
   * @param aIcon                  a representative icon for display in the toolbar
   * @param aShortDescription      a short descriptive name for the controller
   */
  public void addCreateController(ALspCreateControllerModel aCreateControllerModel, ILcdIcon aIcon, String aShortDescription) {

    // Create and initialize creation controller
    TLspCreateController createController = new TLspCreateController(aCreateControllerModel);

    createController.addUndoableListener(getUndoManager());
    createController.setIcon(aIcon);
    createController.setActionToTriggerAfterCommit(
        new TLspSetControllerAction(getView(), getDefaultController())
    );

    createController.setShortDescription(aShortDescription);
    createController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        leftMouseButton().or().
                                                               rightMouseButton().or().
                                                               keyEvents().build());
    ALspController navigation = ControllerFactory.createNavigationController();

    createController.appendController(navigation);

    // Add creation controller to toolbar
    addController(createController);
  }

}
