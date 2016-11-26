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
package samples.lightspeed.customization.style.highlighting;

import java.io.IOException;

import javax.swing.JToolBar;

import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ALspController;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * This sample demonstrates how the animation and style frameworks can be used to highlight
 * the object under the cursor.
 */
public class MainPanel extends LightspeedSample {

  private HighlightController fHighlightController;

  @Override
  protected ILspAWTView createView() {
    return createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected JToolBar[] createToolBars(final ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected void createControllers() {
        ALspController controller = ControllerFactory.createGeneralController(getUndoManager(), aView);
        fHighlightController = new HighlightController();
        fHighlightController.appendController(controller);

        // Set our navigation controller as the initial controller
        getView().setController(fHighlightController);
      }
    };
    return new JToolBar[]{toolBar};
  }

  protected void addData() throws IOException {
    super.addData();
    AnimatedHighlightSHPLayerFactory layerFactory = new AnimatedHighlightSHPLayerFactory();
    layerFactory.addHighlightController(fHighlightController);
    LspDataUtil.instance().model("Data/Shp/World/world.shp").layer(layerFactory).label("Highlighted Countries").addToView(getView()).fit();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Highlighted style");
  }

}
