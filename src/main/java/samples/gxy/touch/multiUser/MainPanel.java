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
package samples.gxy.touch.multiUser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNavigateController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;

import samples.common.SampleData;
import samples.common.model.GeodeticModelFactory;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.NewShapeControllerModel;
import samples.gxy.touch.GXYTouchSample;
import samples.gxy.touch.editing.TouchNewController;

/**
 * Main class for the multi user sample
 */
public class MainPanel extends GXYTouchSample {

  private ILcdGXYLayer fLonLatShapeGXYLayer1;
  private ILcdGXYLayer fLonLatShapeGXYLayer2;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Create a multi-user controller and activate it.
    ILcdGXYController controller = getGXYNewController(new TLcdImageIcon("images/shape/i32_geodesic.png"), "Create a geodetic polyline");
    getTouchToolBar().addGXYController(controller);
    getView().setGXYController(getTouchToolBar().getGXYController(controller));
  }

  private ILcdGXYController getGXYNewController(ILcdIcon aShapeIcon, String aDescription) {
    ControllerSettingsNotifier notifier = new ControllerSettingsNotifier(getView());
    NewShapeControllerModel model1 = new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYLINE, notifier) {
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return fLonLatShapeGXYLayer1;
      }
    };
    TLcdGXYTouchNewController new_controller1 = new TouchNewController(model1, getOverlayPanel(), TLcdOverlayLayout.Location.NORTH_WEST);
    new_controller1.setShortDescription(aDescription);
    new_controller1.setName("new1");
    new_controller1.setNextGXYController(new TLcdGXYTouchNavigateController());

    NewShapeControllerModel model2 = new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYLINE, notifier) {
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return fLonLatShapeGXYLayer2;
      }
    };
    TLcdGXYTouchNewController new_controller2 = new TouchNewController(model2, getOverlayPanel(), TLcdOverlayLayout.Location.NORTH_EAST);
    new_controller2.setShortDescription(aDescription);
    new_controller2.setName("new2");
    new_controller2.setNextGXYController(new TLcdGXYTouchNavigateController());

    SplitTouchEventsMultiUserController multi_controller = new SplitTouchEventsMultiUserController();
    multi_controller.setUserController(SplitTouchEventsMultiUserController.USER_1, new_controller1);
    multi_controller.setUserController(SplitTouchEventsMultiUserController.USER_2, new_controller2);
    multi_controller.setIcon(aShapeIcon);
    multi_controller.setName("Multi User");
    multi_controller.setShortDescription("Create lines with multiple touch users");

    return multi_controller;
  }

  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView());

    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();
    GeodeticModelFactory modelFactory = new GeodeticModelFactory();
    fLonLatShapeGXYLayer1 = factory.createGXYLayer(modelFactory.createSimpleModel());
    fLonLatShapeGXYLayer2 = factory.createGXYLayer(modelFactory.createSimpleModel());

    ShapeGXYLayerFactory.retrieveGXYPainterEditor(fLonLatShapeGXYLayer1).setLineStyle(new TLcdGXYPainterColorStyle(Color.black, Color.red));
    ShapeGXYLayerFactory.retrieveGXYPainterEditor(fLonLatShapeGXYLayer1).setLineStyle(new TLcdGXYPainterColorStyle(Color.blue, Color.yellow));

    fLonLatShapeGXYLayer1.getModel().removeAllElements(ILcdFireEventMode.NO_EVENT);
    fLonLatShapeGXYLayer1.setLabel("User 1 shapes");
    GXYLayerUtil.addGXYLayer(getView(), fLonLatShapeGXYLayer1, true, false);

    fLonLatShapeGXYLayer2.getModel().removeAllElements(ILcdFireEventMode.NO_EVENT);
    fLonLatShapeGXYLayer2.setLabel("User 2 shapes");
    GXYLayerUtil.addGXYLayer(getView(), fLonLatShapeGXYLayer2, true, false);
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Multi User");
  }
}
