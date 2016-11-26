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
package samples.lightspeed.customization.selection;

import java.io.IOException;

import com.luciad.view.lightspeed.ILspAWTView;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * <p>This sample demonstrates how to customize the selection behaviour by
 * extending TLspSelectController and its controller model.</p>
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    ToolBar regularToolBar = new ToolBar(aView, this, true, false);

    CustomSelectController customController = new CustomSelectController();
    customController.appendController(ControllerFactory.createNavigationController());

    // Add the custom extension of TLspSelectController.
    regularToolBar.remove(0);
    regularToolBar.addController(customController, 0);
    getView().setController(customController);

    return new ToolBar[]{regularToolBar};
  }

  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    LspDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_COUNTIES).layer().label("Counties").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Customized selection");
  }

}
