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
package samples.ais.gxy;

import java.io.IOException;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdLambert1972BelgiumGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.map.TLcdMapJPanel;

import samples.ais.model.ModelFactory;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates creation and display of aeronautical data using the LuciadLightspeed AIS package.
 * This MainPanel class sets up a view and adds a few layers with AIS data.
 * Models containing AIS objects are hardcoded in the ModelFactory class. Layer creation
 * is performed in the LayerFactory class.
 * <p/>
 * This sample uses aerodromes, waypoints, routes, navaids and procedures. This data is
 * added to the view in the <code>addAISData</code> method.
 */
public class MainPanel extends GXYSample {

  private ILcdGXYLayerFactory fLayerFactory = new LayerFactory();

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = super.createMap();
    map.setXYWorldReference(new TLcdLambert1972BelgiumGridReference());
    return map;
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(2.00, 49.50, 5.00, 2.00);
  }

  /**
   * Loads the background data and adds layers containing
   * AIS data on top of the background data in the method
   * {@link #addAISData()}.
   */
  @Override
  protected void addData() throws IOException {
    super.addData();
    addAISData();
  }

  /**
   * This method creates several ILcdModels containing aeronatical data objects and adds
   * the to the view.
   */
  private void addAISData() {
    // We're using a hard coded model factory to create the AIS data
    ModelFactory ais_model_factory = new ModelFactory();

    // Aerodromes
    ILcd2DBoundsIndexedModel aerodrome_model = ais_model_factory.createAerodromeModel();
    GXYDataUtil.instance().model(aerodrome_model).layer(fLayerFactory).addToView(getView());

    // Waypoints
    ILcd2DBoundsIndexedModel waypoint_model = ais_model_factory.createWaypointModel();
    GXYDataUtil.instance().model(waypoint_model).layer(fLayerFactory).addToView(getView());

    // Routes: these require waypoints, so we pass the previously created waypoint model.
    ILcdModel route_model = ais_model_factory.createRouteModel(waypoint_model);
    GXYDataUtil.instance().model(route_model).layer(fLayerFactory).addToView(getView());

    // Airspaces
    ILcdModel airspace_model = ais_model_factory.createAirspaceModel();
    GXYDataUtil.instance().model(airspace_model).layer(fLayerFactory).addToView(getView());

    // Runways
    ILcdModel runway_model = ais_model_factory.createRunwayModel();
    GXYDataUtil.instance().model(runway_model).layer(fLayerFactory).addToView(getView());

    // Procedures
    ILcdModel procedure_model = ais_model_factory.createProcedureModel(aerodrome_model, waypoint_model);
    GXYDataUtil.instance().model(procedure_model).layer(fLayerFactory).addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Creating AIS objects using a geodetic reference");
  }

}
