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
package samples.realtime.lightspeed.dynamicdensity;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Vector;

import com.luciad.model.ILcdModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.density.TLspDensityLayerBuilder;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.density.DensityStyleType;
import samples.lightspeed.density.DensityStyler;
import samples.lightspeed.density.DensityStylerCustomizer;
import samples.realtime.common.SimulatorControlPanel;
import samples.realtime.common.TrajectorySimulatorModel;

/**
 * Main panel class for the dynamic density sample
 */
public class MainPanel extends LightspeedSample {

  private TLcdSimulator fSimulator;
  private DensityStyler fDensityStyler = new DensityStyler(DensityStyleType.POINTS_PIXEL_SIZED);

  @Override
  protected void addData() throws IOException {
    super.addData();

    //Add some background data
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    ILcdModel citiesModel = LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit().getModel();

    //Create a trajectory simulator model based on the cities model.
    TrajectorySimulatorModel simulatorModel = new TrajectorySimulatorModel(citiesModel, 3000);

    //Add the trajectories and density tracks
    LspDataUtil.instance().model(simulatorModel.getTrajectoryLinesModel()).layer(new TrajectoryLayerFactory()).addToView(getView());
    getView().addLayer(createTracksLayer(simulatorModel.getTrackModels()[0]));

    setupSimulator(simulatorModel);

    addCustomControls();
  }

  private ILspLayer createTracksLayer(ILcdModel aDensityModel) {
    TLspDensityLayerBuilder layerBuilder = TLspDensityLayerBuilder.newBuilder();
    return layerBuilder.model(aDensityModel).layerType(ILspLayer.LayerType.REALTIME)
                               .bodyStyler(fDensityStyler)
                               .culling(false).build();
  }

  private void setupSimulator(TrajectorySimulatorModel aSimulatorModel) {
    fSimulator = new TLcdSimulator();
    fSimulator.setSimulatorModel(aSimulatorModel);
    fSimulator.setPauseOnMousePressed(false);
    fSimulator.setDate(fSimulator.getBeginDate());
    fSimulator.setDelayBetweenUpdateMs(25);
  }

  /**
   * Creates and adds custom user controls to the user interface.
   */
  private void addCustomControls() {
    //Add a density layer customizer panel to the main toolbar
    ToolBar[] toolbars = getToolBars();
    ToolBar viewModeToolBar = toolbars[toolbars.length - 1];
    Vector<DensityStyleType> supportedStyleTypes = new Vector<DensityStyleType>();
    supportedStyleTypes.add(DensityStyleType.POINTS_PIXEL_SIZED);
    supportedStyleTypes.add(DensityStyleType.POINTS_WORLD_SIZED);
    DensityStylerCustomizer chooser = new DensityStylerCustomizer("Density",
                                                                  fDensityStyler,
                                                                  supportedStyleTypes);
    viewModeToolBar.addComponent(chooser);
    //Add a simulator control panel to the bottom of the view
    SimulatorControlPanel controlPanel = new SimulatorControlPanel();
    controlPanel.setSimulator(fSimulator);
    add(controlPanel, BorderLayout.SOUTH);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Dynamic density");
  }

}
