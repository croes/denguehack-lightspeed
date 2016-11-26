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
package samples.gxy.fundamentals.step4;

import java.awt.EventQueue;
import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYViewFitAction;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYPanController;
import com.luciad.view.gxy.controller.TLcdGXYZoomWheelController;
import com.luciad.view.map.TLcdMapJPanel;

import samples.gxy.fundamentals.step3.WayPointModelDecoder;


/**
 * Fundamentals sample: editing business data.
 * Displays a number of way points that can be dragged around.
 */
public class Main extends samples.gxy.fundamentals.step1.Main {

  private static final String WAYPOINT_MODEL = "Data/Custom1/custom.cwp";

  @Override
  protected void initLayers(TLcdMapJPanel aView) throws IOException {
    addBackgroundLayer(aView);

    WayPointModelDecoder wayPointDecoder = new WayPointModelDecoder();
    ILcdModel wayPointModel = wayPointDecoder.decode("" + WAYPOINT_MODEL);

    ILcdGXYLayer waypointLayer = new EditableWayPointLayerFactory().createGXYLayer(wayPointModel);

    // Adds the waypoint layers to the view.
    // Moves the grid layer on top.
    aView.addGXYLayer(waypointLayer);
    aView.moveLayerAt(aView.layerCount() - 1, aView.getGridLayer());

    // Fits the map to the bounds of the waypoint layer.
    TLcdGXYViewFitAction fitAction = new TLcdGXYViewFitAction();
    fitAction.fitGXYLayer(waypointLayer, aView);
  }

  /**
   * Creates a controller that selects and edits using the left mouse button, pans using the middle mouse button,
   * and zooms using the mouse wheel.
   */
  protected void initController(TLcdMapJPanel aView) {
    TLcdGXYEditController2 editController = new TLcdGXYEditController2();
    editController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());

    TLcdGXYPanController panController = new TLcdGXYPanController();
    panController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().middleMouseButton().build());
    panController.setCursor(editController.getCursor());
    panController.setDragViewOnPan(true);

    TLcdGXYZoomWheelController zoomWheelController = new TLcdGXYZoomWheelController();

    TLcdGXYCompositeController compositeController = new TLcdGXYCompositeController();
    compositeController.addGXYController(editController);
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
