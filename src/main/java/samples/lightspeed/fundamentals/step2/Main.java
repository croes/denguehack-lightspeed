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
package samples.lightspeed.fundamentals.step2;

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
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;

import samples.gxy.fundamentals.step2.FlightPlanModelDecoder;
import samples.lightspeed.fundamentals.step1.BasicLayerFactory;

/**
 * The sample demonstrates how to decode and display data that is stored in a file in a custom
 * format.
 */
public class Main extends samples.lightspeed.fundamentals.step1.Main {
  @Override
  protected ILspLayerFactory createLayerFactory() {
    // Create a layer factory that composes both the flight plan layer factory and the
    // basic implementation (that supports shp and rasters)
    return new TLspCompositeLayerFactory(
        new FlightPlanLayerFactory(), new BasicLayerFactory());
  }

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
    // CREATE FLIGHT PLAN LAYER

    // FlightPlanModelDecoder can read the custom file format
    ILcdModelDecoder decoder = new FlightPlanModelDecoder();

    // Decode custom file to create an ILcdModel for flight plans
    ILcdModel flightPlanModel = decoder.decode("Data/Custom1/custom.cfp");

    // Calling addLayer() will cause the view to invoke its layer factory with
    // the given model and then add the resulting layer to itself
    Collection<ILspLayer> flightPlanLayer = aView.addLayersFor(flightPlanModel);

    // Fit the view to the flight plan layer we just added.
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
