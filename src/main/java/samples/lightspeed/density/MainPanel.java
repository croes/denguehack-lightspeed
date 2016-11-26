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
package samples.lightspeed.density;

import java.awt.Component;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JToolBar;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.density.TLspDensityLayerBuilder;

import samples.common.SampleData;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * This sample is intended to demonstrate how vector data can be rendered as a density plot. The
 * data is visualized with a color which depends on the amount of overlap of objects within a given
 * region. This region can be expressed both in world size as in pixel size.
 */
public class MainPanel extends LightspeedSample {

  protected void addData() throws IOException {
    super.addData();
    // Add a cities layer
    LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").labeled(false).addToView(getView());

    // Create trajectories between the cities
    ILcdModel trajectoriesModel = TrajectoriesModelFactory.createTrajectoriesModel();
    DensityStyler densityStyler = new DensityStyler(DensityStyleType.LINES_PIXEL_SIZED);
    ILspLayer trajectoriesLayer = TLspDensityLayerBuilder.newBuilder()
                                                         .model(trajectoriesModel)
                                                         .bodyStyler(densityStyler)
                                                         .label("Trajectories")
                                                         .culling(false).build();

    // Add the trajectories layer
    getView().addLayer(trajectoriesLayer);
    Vector<DensityStyleType> supportedStyleTypes = new Vector<DensityStyleType>();
    supportedStyleTypes.add(DensityStyleType.LINES_PIXEL_SIZED);
    supportedStyleTypes.add(DensityStyleType.LINES_WORLD_SIZED);
    addCustomizerToToolBar(trajectoriesLayer.getLabel(), densityStyler, supportedStyleTypes);

    FitUtil.fitOnLayers(this, trajectoriesLayer);
  }

  /**
   * Adds a new DensityStylerCustomizer to the toolbar.
   *
   * @param aLabel                      Title of the customizer panel
   * @param aDensityStyler              Density styler to be customized
   * @param aSupportedStyleTypes        Style types supported by the customizer
   */
  private void addCustomizerToToolBar(String aLabel,
                                      DensityStyler aDensityStyler,
                                      Vector<DensityStyleType> aSupportedStyleTypes) {
    // Add customizers to the toolbar to configure the density plot properties.
    ToolBar[] toolbars = getToolBars();
    ToolBar viewModeToolBar = toolbars[toolbars.length - 1];
    DensityStylerCustomizer chooser = new DensityStylerCustomizer(aLabel, aDensityStyler, aSupportedStyleTypes);
    viewModeToolBar.addComponent(chooser);
  }

  /**
   * Adds an additional toolbar used for customizing the density layers in this sample.
   *
   * @param aView       the view to which the toolbar will be associated
   *
   * @return the default toolbar plus the density layer customizer
   */
  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    Component[] regularToolBars = super.createToolBars(aView);
    ToolBar viewModeToolBar = new ToolBar(aView, this);
    Component[] result = new JToolBar[regularToolBars.length + 1];
    System.arraycopy(regularToolBars, 0, result, 0, regularToolBars.length);
    result[regularToolBars.length] = viewModeToolBar;
    return result;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Density plots");
  }

}
