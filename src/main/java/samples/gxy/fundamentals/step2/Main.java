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
package samples.gxy.fundamentals.step2;

import java.awt.EventQueue;
import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYViewFitAction;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYPanController;
import com.luciad.view.gxy.controller.TLcdGXYSelectController2;
import com.luciad.view.gxy.controller.TLcdGXYZoomWheelController;
import com.luciad.view.map.TLcdMapJPanel;


/**
 * Fundamentals Sample 2: adding business data.
 * Displays a number of flight plans on top of the background layer created in Sample 1.
 * The flight plans are decoded from a file in a custom format.
 */
public class Main extends samples.gxy.fundamentals.step1.Main {

  private static final String FLIGHTPLAN_MODEL = "Data/Custom1/custom.cfp";

  @Override
  protected void initLayers(TLcdMapJPanel aView) throws IOException {
    addBackgroundLayer(aView);

    FlightPlanModelDecoder flightplanDecoder = new FlightPlanModelDecoder();
    ILcdModel flightplanModel = flightplanDecoder.decode("" + FLIGHTPLAN_MODEL);

    ILcdGXYLayer flightplanLayer = new FlightPlanLayerFactory().createGXYLayer(flightplanModel);

    aView.addGXYLayer(flightplanLayer);
    aView.moveLayerAt(aView.layerCount() - 1, aView.getGridLayer());

    // Fits the map to the bounds of the flightplan layer.
    TLcdGXYViewFitAction fitAction = new TLcdGXYViewFitAction();
    fitAction.fitGXYLayer(flightplanLayer, aView);
  }

  /**
   * Creates a controller that selects using the left mouse button, pans using the middle mouse button,
   * and zooms using the mouse wheel.
   */
  protected void initController(TLcdMapJPanel aView) {
    TLcdGXYSelectController2 selectController = new TLcdGXYSelectController2();
    selectController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());

    TLcdGXYPanController panController = new TLcdGXYPanController();
    panController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().middleMouseButton().build());
    panController.setCursor(selectController.getCursor());
    panController.setDragViewOnPan(true);

    TLcdGXYZoomWheelController zoomWheelController = new TLcdGXYZoomWheelController();

    TLcdGXYCompositeController compositeController = new TLcdGXYCompositeController();
    compositeController.addGXYController(selectController);
    compositeController.addGXYController(panController);
    compositeController.addGXYController(zoomWheelController);
    aView.setGXYController(compositeController);
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Main().start();
      }
    });
  }
}
