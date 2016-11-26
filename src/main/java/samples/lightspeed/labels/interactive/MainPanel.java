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
package samples.lightspeed.labels.interactive;

import java.io.IOException;
import java.util.Map;

import javax.swing.JToolBar;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.ALspInteractiveLabelProvider;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspInteractiveLabelsController;

import samples.common.SampleData;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * This sample demonstrates how to interactively move labels.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit();
  }

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    Map<Object, String> cityComments = new TLcdWeakIdentityHashMap<>();
    InteractiveLabelProvider interactiveLabelProvider = createInteractiveLabelProvider(cityComments);

    ToolBar toolBar = new ToolBar(aView, this, true, true);

    // Add a controller that can activate interactive labels
    toolBar.addController(createCompositeInteractiveController(interactiveLabelProvider), 2).doClick();
    toolBar.addSpace(3);

    return new JToolBar[]{toolBar};
  }

  private InteractiveLabelProvider createInteractiveLabelProvider(Map<Object, String> aCityComments) {
    RegularSwingLabelComponent regularComponent = new RegularSwingLabelComponent(aCityComments);
    InteractiveSwingLabelComponent interactiveLabelComponent = new InteractiveSwingLabelComponent(aCityComments);
    InteractiveLabelProvider interactiveLabelProvider = new InteractiveLabelProvider(interactiveLabelComponent);
    InteractiveLabelLayerFactory interactiveLabelLayerFactory = new InteractiveLabelLayerFactory(
        interactiveLabelProvider, regularComponent, interactiveLabelComponent, aCityComments);
    ServiceRegistry.getInstance().register(interactiveLabelLayerFactory);
    return interactiveLabelProvider;
  }

  private ILspController createCompositeInteractiveController(ALspInteractiveLabelProvider aInteractiveLabelProvider) {
    ALspController controller = new TLspEditController();

    controller.appendController(new TLspInteractiveLabelsController(aInteractiveLabelProvider));
    controller.appendController(ControllerFactory.createDefaultSelectController());
    controller.appendController(ControllerFactory.createNavigationController());

    controller.setIcon(TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON));

    return controller;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Interactive labels");
  }

}
