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

import static samples.gxy.vertical.MainPanel.*;

import java.awt.Color;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.JToolBar;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.vertical.TLcdVVJPanel;

import samples.gxy.vertical.Flight;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * This sample shows how to use the vertical view package.
 */
public class MainPanel extends LightspeedSample {

  private static final Color TERRAIN_COLOR = new Color(180, 180, 180, 128);

  private TerrainFlightVVModel fFlightVVModel;
  private TLcdVVJPanel fVerticalView;
  private ILspInteractivePaintableLayer fFlightLayer;

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected ILspController createDefaultController() {
        return MainPanel.this.createDefaultController(getUndoManager());
      }
    };
    AbstractButton button = toolBar.addController(createFlightCreateController(toolBar), 1);
    CreateAndEditToolBar.connectButtonToLayer(getView(), getFlightLayer(), button);
    return new JToolBar[]{toolBar};
  }

  private VerticalCursorController createDefaultController(TLcdUndoManager aUndoManager) {
    ALspController defaultController = ControllerFactory.createGeneralController(aUndoManager, getView());
    // Add a vertical cursor controller that updates the associated vertical view's cursor position.
    VerticalCursorController verticalCursorController = new VerticalCursorController(getVerticalView(), getFlightLayer());
    verticalCursorController.appendController(defaultController);
    verticalCursorController.setShortDescription(defaultController.getShortDescription());
    verticalCursorController.setIcon(defaultController.getIcon());
    verticalCursorController.setName(defaultController.getName());
    return verticalCursorController;
  }

  private ALspController createFlightCreateController(ToolBar aToolBar) {
    // Each creation controller must have a controller model (in this case a lon-lat creation model)
    ALspCreateControllerModel createControllerModel = new ALspCreateControllerModel() {

      @Override
      public ILspInteractivePaintableLayer getLayer(ILspView aView) {
        return fFlightLayer;
      }

      @Override
      public TLspPaintRepresentation getPaintRepresentation(ILspInteractivePaintableLayer aLayer, ILspView aView) {
        return TLspPaintRepresentation.BODY;
      }

      @Override
      public Object create(ILspView aView, ILspLayer aLayer) {
        return new Flight();
      }
    };
    // Create and initialize creation controller
    TLspCreateController createController = new TLspCreateController(createControllerModel);

    createController.addUndoableListener(aToolBar.getUndoManager());
    createController.setShortDescription("Create a new Flight");
    createController.setIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON));
    createController.setActionToTriggerAfterCommit(new TLspSetControllerAction(getView(), aToolBar.getDefaultController()));
    createController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
                                      leftMouseButton().or().
                                                               rightMouseButton().or().
                                                               keyEvents().build()
    );

    ALspController navigation = ControllerFactory.createNavigationController();
    createController.appendController(navigation);

    return createController;
  }

  protected void createGUI() {
    super.createGUI();

    // Create the vertical view
    addComponentBelow(createVerticalViewWithControllers(getVerticalView()));

    // Add a panel to configure the vertical view's altitude unit
    addComponentToRightPanel(createUnitPanel(getVerticalView()));
  }

  protected void addData() throws IOException {
    super.addData();

    ILspInteractivePaintableLayer flightLayer = getFlightLayer();

    // listener that will listen for changes in the selection of the flight layer
    // and will set the selected flight on the FlightVVModel
    FlightSelectionListener flightSelectionListener = new FlightSelectionListener(getVerticalViewModel());
    flightLayer.addSelectionListener(flightSelectionListener);
    flightLayer.selectObject(flightLayer.getModel().elements().nextElement(), true, ILcdFireEventMode.FIRE_NOW);

    // Add the layer with the flights to the view
    getView().addLayer(flightLayer);
    FitUtil.fitOnLayers(this, flightLayer);
  }

  private TLcdVVJPanel getVerticalView() {
    if (fVerticalView == null) {
      createVerticalView();
    }
    return fVerticalView;
  }

  private TerrainFlightVVModel getVerticalViewModel() {
    if (fFlightVVModel == null) {
      createVerticalView();
    }
    return fFlightVVModel;
  }

  private void createVerticalView() {
    fFlightVVModel = new TerrainFlightVVModel();
    fFlightVVModel.setView(getView());
    fVerticalView = samples.gxy.vertical.MainPanel.createVerticalView(fFlightVVModel, new Color[]{TERRAIN_COLOR, AIRSPACE_COLOR});
  }

  private ILspInteractivePaintableLayer getFlightLayer() {
    if (fFlightLayer == null) {
      ILcdModel model = FlightModelFactory.createFlightModel();
      fFlightLayer = (ILspInteractivePaintableLayer) new LayerFactory().createLayer(model);
    }
    return fFlightLayer;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Vertical view");
  }

}

