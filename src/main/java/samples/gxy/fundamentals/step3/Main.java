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
package samples.gxy.fundamentals.step3;

import java.awt.EventQueue;
import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.TLcdGXYViewFitAction;
import com.luciad.view.map.TLcdMapJPanel;

import samples.gxy.fundamentals.step2.FlightPlanLayerFactory;
import samples.gxy.fundamentals.step2.FlightPlanModelDecoder;


/**
 * Fundamentals sample: combining business data in a layer tree node.
 * Displays a number of way points on top of the flight plans created in Sample 2.
 * The way points are decoded from a file in a custom format. The flight plan layer and
 * the way point layer are combined in an ILcdLayerTreeNode.
 */
public class Main extends samples.gxy.fundamentals.step2.Main {

  private static final String FLIGHTPLAN_MODEL = "Data/Custom1/custom.cfp";
  private static final String WAYPOINT_MODEL = "Data/Custom1/custom.cwp";

  @Override
  protected void initLayers(TLcdMapJPanel aView) throws IOException {
    addBackgroundLayer(aView);

    FlightPlanModelDecoder flightplanDecoder = new FlightPlanModelDecoder();
    ILcdModel flightplanModel = flightplanDecoder.decode("" + FLIGHTPLAN_MODEL);

    WayPointModelDecoder waypointDecoder = new WayPointModelDecoder();
    ILcdModel waypointModel = waypointDecoder.decode("" + WAYPOINT_MODEL);

    ILcdGXYLayer flightplanLayer = new FlightPlanLayerFactory().createGXYLayer(flightplanModel);
    ILcdGXYLayer waypointLayer = new WayPointLayerFactory().createGXYLayer(waypointModel);

    // Creates a layer for the combined model. 
    TLcdGXYLayerTreeNode combinedLayer = new TLcdGXYLayerTreeNode("Combined layer");
    combinedLayer.addLayer(flightplanLayer);
    combinedLayer.addLayer(waypointLayer);

    // Adds the flightplan/waypoint layers to the view.
    // Moves the grid layer on top.
    aView.addGXYLayer(combinedLayer);
    aView.moveLayerAt(aView.layerCount() - 1, aView.getGridLayer());

    // Fits the map to the bounds of the flightplan layer.
    TLcdGXYViewFitAction fitAction = new TLcdGXYViewFitAction();
    fitAction.fitGXYLayer(flightplanLayer, aView);
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Main().start();
      }
    });
  }
}
