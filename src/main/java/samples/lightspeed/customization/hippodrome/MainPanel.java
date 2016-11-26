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
package samples.lightspeed.customization.hippodrome;

import javax.swing.Box;
import javax.swing.JToolBar;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * This sample demonstrates how to create a painter for a custom shape, the hippodrome.
 * <p/>
 * The hippodrome consists of two 180 degrees arcs and two lines connecting these arcs and at a given
 * distance from the line between the center points (width).
 */
public class MainPanel extends LightspeedSample {
  // Custom toolbar which adds editing and creation controllers to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBar;
  private CreateControllerModelHippodrome fCreateControllerModelGeodetic;
  private CreateControllerModelHippodrome fCreateControllerModelGrid;

  // We override the createToolBars method to additionally return a CreateAndEditToolBar
  // with new controllers to create geodetic and cartesian hippodromes.

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    fCreateControllerModelGeodetic = new CreateControllerModelHippodrome(CreateControllerModelHippodrome.Mode.GEODETIC);
    fCreateControllerModelGrid = new CreateControllerModelHippodrome(CreateControllerModelHippodrome.Mode.GRID);

    final ToolBar regularToolBar = new ToolBar(aView, this, true, true);
    if (fCreateAndEditToolBar == null) {
      fCreateAndEditToolBar = new CreateAndEditToolBar(
          aView, this,
          regularToolBar.getButtonGroup(),
          false, false, false
      ) {
        @Override
        protected ILspController createDefaultController() {
          return regularToolBar.getDefaultController();
        }

        @Override
        protected void setCreateExtrudedShapes(boolean aExtruded) {
          super.setCreateExtrudedShapes(aExtruded);
          fCreateControllerModelGeodetic.setExtruded(aExtruded);
          fCreateControllerModelGrid.setExtruded(aExtruded);
        }

        @Override
        protected void createControllers() {
          super.createControllers();
          addCreateController(
              fCreateControllerModelGeodetic,
              TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON),
              "Geodetic Hippodrome"
          );
          addCreateController(
              fCreateControllerModelGrid,
              TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON),
              "Grid Hippodrome"
          );
          addComponent(Box.createHorizontalGlue());
          addExtrusionToggleButton();
        }
      };
      fCreateAndEditToolBar.remove(fCreateAndEditToolBar.getComponentCount() - 1);

    }
    return new JToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  protected void addData() {
    HippodromeLayerFactory hippodromeLayerFactory = new HippodromeLayerFactory();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").addToView(getView());

    ILspLayer hip1 = LspDataUtil.instance().model(HippodromeModelFactory.createGeodeticHippodromeModel()).layer(hippodromeLayerFactory).addToView(getView()).getLayer();
    ILspLayer hip2 = LspDataUtil.instance().model(HippodromeModelFactory.createGridHippodromeModel()).layer(hippodromeLayerFactory).addToView(getView()).getLayer();

    FitUtil.fitOnLayers(this, hip1, hip2);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Hippodrome");
  }

}
