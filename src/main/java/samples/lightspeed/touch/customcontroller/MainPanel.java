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
package samples.lightspeed.touch.customcontroller;

import java.io.IOException;

import javax.swing.JToolBar;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.input.touch.TLcdTouchDevice;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchAndHoldActionController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchNavigateController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.gxy.common.touch.TouchUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.controller.FlyToObjectAction;
import samples.lightspeed.common.touch.MouseToTouchControllerWrapper;
import samples.lightspeed.common.touch.TouchToolBar;

/**
 * This sample demonstrates two custom touch controllers.
 * <p/>
 * The first controller displays a tooltip for objects that are touched.<br/>
 * The second controller fits the view to the bounds of an object if a touch-and-hold action is performed
 * on that object.
 */
public class MainPanel extends LightspeedSample {

  private ILspLayer fCountiesLayer;
  private ILspLayer fCitiesLayer;

  @Override
  protected void createGUI() {
    super.createGUI();

    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    TouchToolBar toolBar = new TouchToolBar(aView, this, true, true);

    // Add custom controllers.

    ILspController controller = createInformationPanelController();
    toolBar.addController(controller, 1);
    getView().setController(controller);

    toolBar.addController(createFlyToController(), 2);

    return new JToolBar[]{toolBar};
  }

  protected void addData() throws IOException {
    super.addData();

    fCountiesLayer = LspDataUtil.instance().model(SampleData.US_COUNTIES).layer().label("Counties").labeled(false).addToView(getView()).getLayer();
    fCitiesLayer = LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").labeled(false).addToView(getView()).fit().getLayer();
  }

  private class MyInformationPanelController extends TouchInformationPanelController {

    @Override
    protected String[] getPropertyNames(ILspLayer aLayer, ILcdDataObject aObject) {
      if (aLayer == fCitiesLayer) {
        return new String[]{"CITY", "STATE", "TOT_POP"};
      } else if (aLayer == fCountiesLayer) {
        return new String[]{"NAME", "STATE_NAME", "POP1996"};
      } else {
        return super.getPropertyNames(aLayer, aObject);
      }
    }
  }

  private ILspController createInformationPanelController() {
    MyInformationPanelController controller = new MyInformationPanelController();
    controller.appendController(new TLspTouchNavigateController());
    if (TLcdTouchDevice.getInstance().getTouchDeviceStatus() != TLcdTouchDevice.Status.READY) {
      return new MouseToTouchControllerWrapper(controller);
    } else {
      return controller;
    }
  }

  private ILspController createFlyToController() {
    TLspTouchAndHoldActionController touchAndHoldController = new TLspTouchAndHoldActionController();
    touchAndHoldController.setPostTouchAndHoldAction(new FlyToObjectAction(getView()));
    touchAndHoldController.appendController(new TLspTouchNavigateController());

    touchAndHoldController.setName("Fit on touch-and-hold");
    touchAndHoldController.setShortDescription("Fit to object after touch-and-hold");
    touchAndHoldController.setIcon(TLcdIconFactory.create(TLcdIconFactory.FIT_ICON));
    if (TLcdTouchDevice.getInstance().getTouchDeviceStatus() != TLcdTouchDevice.Status.READY) {
      return new MouseToTouchControllerWrapper(touchAndHoldController);
    } else {
      return touchAndHoldController;
    }
  }

  public static void main(final String[] aArgs) {
    TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    startSample(MainPanel.class, "Custom touch controller");
  }

}
