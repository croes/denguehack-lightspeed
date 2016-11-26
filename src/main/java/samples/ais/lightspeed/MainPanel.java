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
package samples.ais.lightspeed;

import java.io.IOException;

import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdModelTreeNode;

import samples.ais.model.ModelFactory;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.FitUtil;

/**
 * <p>This sample demonstrates creation and display of aeronautical data using the
 * LuciadLightspeed AIS Industry Specific Component. The sample sets up a Lightspeed view and adds a few
 * layers with AIS data. Models containing AIS objects are created in the ModelFactory class.
 * Layer creation is performed in the AISLayerFactory class.</p>
 *
 * <p>This sample shows how to customize the visualization of the AIS layers. These customizations are done by
 * using a custom set of Symbology Encoding files in the 'samples/ais/lightspeed' directory. This directory is passed to
 * the TLspAISStyler used to visualize the layers. These are the customizations:
 * <ul>
 *   <li>The waypoint with identifier 'CMB19' is painted using a purple color, see MyWaypointType.sld</li>
 *   <li>Aerodromes are painted using a yellow circle. See MyAerodromeType.sld</li>
 *   <li>Make sure waypoints, airspaces and runways display the correct label content. This is needed because the
 *   custom data types that are used (see CustomAISDataTypes) use different property names than assumed by
 *   TLspAISStyler. For example: TLspAISStyler assumes that the 'lower limit' for airspaces uses the property name
 *   'LowerLimit'. The custom airspace type used in this sample, MyAirspaceType, uses 'Lower_Limit' as property name.
 *   this is corrected in MyWaypointType.sld, MyAirspaceType.sld and MyRunwayType.sld</li>
 *   <li>Make sure runways with a 'Concrete' or 'Gravel' surface are painted using a different style. 'Concrete' runways
 *   are painted with a black fill, and 'Gravel' runways are painted with a dotted fill. See MyRunwayType.sld.</li>
 * </ul>
 * </p>
 *
 * <p>Note that for some types, no sld file is specified in the 'samples/ais/lightspeed' directory. In that case,
 * TLspAISStyler uses a default set of sld files to style the data.</p>
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Use a custom base directory in this sample
    AISLayerFactory layerFactory = new AISLayerFactory(new String[]{"samples/ais/lightspeed"});
    ServiceRegistry.getInstance().register(layerFactory, ServiceRegistry.HIGH_PRIORITY);

    ModelFactory modelFactory = new ModelFactory();

    // Put all models in a model tree node to make sure they end up in a layer tree node
    TLcdModelDescriptor modelDescriptor = new TLcdModelDescriptor("Custom", "Custom AIS", "AIS");
    ILcdModelTreeNode modelTreeNode = new TLcdModelTreeNode(null, modelDescriptor);

    // Create AIS models and add them to the model tree node
    modelTreeNode.addModel(modelFactory.createAerodromeModel());
    modelTreeNode.addModel(modelFactory.createWaypointModel());
    modelTreeNode.addModel(modelFactory.createRouteModel(modelFactory.createWaypointModel()));
    modelTreeNode.addModel(modelFactory.createAirspaceModel());
    modelTreeNode.addModel(modelFactory.createRunwayModel());
    modelTreeNode.addModel(modelFactory.createProcedureModel(modelFactory.createAerodromeModel(),
                                                             modelFactory.createWaypointModel()));

    // Add the model to the view and fit on them
    FitUtil.fitOnLayers(this, getView().addLayersFor(modelTreeNode));
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "AIS Lightspeed");
  }
}
