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
package samples.lightspeed.fundamentals.step3;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;

import samples.gxy.fundamentals.step2.FlightPlanModelDecoder;
import samples.gxy.fundamentals.step3.WayPointModelDecoder;
import samples.lightspeed.fundamentals.step1.BasicLayerFactory;
import samples.lightspeed.fundamentals.step2.FlightPlanLayerFactory;


/**
 * The sample demonstrates how to combine data from two different models into an ILcdLayerTreeNode.
 */
public class Main extends samples.lightspeed.fundamentals.step1.Main {
  @Override
  protected ILspLayerFactory createLayerFactory() {
    // Create a layer factory that composes the flight plan and the way point layer
    // factories, and the basic layer factory so that all those model types are supported.
    return new TLspCompositeLayerFactory(
        new FlightPlanLayerFactory(), new WayPointLayerFactory(), new BasicLayerFactory());
  }

  @Override
  protected void initLayers(ILspView aView) throws IOException {
    ////////////////////////////
    // CREATE BACKGROUND AND GRID LAYER

    // Load some background data
    ILcdModelDecoder earthDecoder = new TLcdEarthRepositoryModelDecoder();
    ILcdModel earthModel = earthDecoder.decode("Data/Earth/SanFrancisco/tilerepository.cfg");
    aView.addLayersFor(earthModel);

    // Create and add the grid layer
    aView.addLayer(TLspLonLatGridLayerBuilder.newBuilder().build());

    ////////////////////////////
    // CREATE WAYPOINT LAYER

    // Create the waypoint model
    ILcdModelDecoder waypointModelDecoder = new WayPointModelDecoder();
    ILcdModel waypointModel = waypointModelDecoder.decode("Data/Custom1/custom.cwp");

    // Do not yet add the waypoint model to the view,
    // we will add it as a combined layer later on
    Collection<ILspLayer> waypointLayer = aView.getLayerFactory().createLayers(waypointModel);

    ////////////////////////////
    // CREATE FLIGHT PLAN LAYER

    // FlightPlanModelDecoder can read the custom file format
    ILcdModelDecoder flightPlanModelDecoder = new FlightPlanModelDecoder();

    // Decode custom file to create an ILcdModel for flight plans
    ILcdModel flightPlanModel = flightPlanModelDecoder.decode("Data/Custom1/custom.cfp");

    // As with the waypoint layer, we will not yet add the flight plan layer to the view
    Collection<ILspLayer> flightPlanLayer = aView.getLayerFactory().createLayers(flightPlanModel);

    //////////////////////////////
    // CREATE COMBINED LAYER

    // Create a combined layer that holds both the waypoint- and the flight plan layer
    TLspLayerTreeNode combinedLayer = new TLspLayerTreeNode("Combined Layer");
    combinedLayer.addLayers(flightPlanLayer);
    combinedLayer.addLayers(waypointLayer);

    // Add the combined layer to the view
    aView.addLayer(combinedLayer);

    fitViewExtents(aView, flightPlanLayer);
  }

  public static void main(String[] args) {
    // Switch to the Event Dispatch Thread, this is required by any Swing based application.
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new Main().start();
      }
    });
  }
}
