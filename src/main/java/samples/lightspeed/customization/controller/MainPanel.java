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
package samples.lightspeed.customization.controller;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.input.touch.TLcdTouchDevice;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.animation.ILcdAnimation;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.TLspClickActionController;
import com.luciad.view.lightspeed.controller.TLspKeyActionController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchAndHoldActionController;
import com.luciad.view.lightspeed.controller.touch.TLspTouchNavigateController;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.common.controller.FlyToObjectAction;
import samples.lightspeed.common.touch.MouseToTouchControllerWrapper;

/**
 * <p>This sample demonstrates how ALspController can be extended to implement a custom
 * controller.</p>
 *
 * <p>The first custom controller is a simple controller that adds an information panel to the view
 * for the first object under the mouse cursor.</p>
 *
 * <p>The second custom controller demonstrates how Key and MouseClicked events can be mapped on
 * certain actions.</p>
 */
public class MainPanel extends LightspeedSample {

  public static final String TOUCH_PROPERTY_NAME = "touchProperty";

  private ILspLayer fCountiesLayer;
  private ILspLayer fCitiesLayer;

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    ToolBar regularToolBar = new ToolBar(aView, this, true, true);

    // Add custom select controller.
    ALspController controller = createCustomSelectController();
    regularToolBar.addController(controller, 0);
    getView().setController(controller);

    // Add controller that executes certain action for key and mouse clicked events.
    regularToolBar.addController(createActionController(), 1);

    return new ToolBar[]{regularToolBar};
  }

  protected void addData() throws IOException {
    super.addData();

    fCountiesLayer = LspDataUtil.instance().model(SampleData.US_COUNTIES).layer().label("Counties").addToView(getView()).getLayer();
    fCitiesLayer = LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit().getLayer();
  }

  private class MyInformationPanelController extends InformationPanelController {

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

  private ALspController createCustomSelectController() {
    // Make a tool tip controller that adds the tool tip components to the overlay panel.
    InformationPanelController controller = new MyInformationPanelController();

    controller.appendController(new TLspSelectController());
    controller.appendController(ControllerFactory.createNavigationController());

    return controller;
  }

  private ILspController createActionController() {
    if ("true".equals(System.getProperty(TOUCH_PROPERTY_NAME))) {
      // Touch and hold controller.
      TLspTouchAndHoldActionController touchAndHoldController = new TLspTouchAndHoldActionController();
      touchAndHoldController.setPostTouchAndHoldAction(new FlyToObjectAction(getView()));
      touchAndHoldController.appendController(new TLspTouchNavigateController());

      touchAndHoldController.setIcon(new TLcdImageIcon("images/gui/i16_pan.gif"));
      if (TLcdTouchDevice.getInstance().getTouchDeviceStatus() != TLcdTouchDevice.Status.READY) {
        return new MouseToTouchControllerWrapper(touchAndHoldController);
      } else {
        return touchAndHoldController;
      }
    } else {
      // Define action controllers for the mouse.
      TLspClickActionController flyToController = new TLspClickActionController(new FlyToObjectAction(getView()), 1);
      flyToController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().and().ctrlFilter(true).build());

      TLspClickActionController fullScreenController = new TLspClickActionController(getFullScreenAction(), 2);
      fullScreenController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());

      // Define action controllers for the numpad keys, + key and - key on the keyboard.
      final ContinuousPan left = new ContinuousPan(getView(), ContinuousPan.LEFT, 600);
      TLspKeyActionController keyControllerLeft = new TLspKeyActionController(
          start(left), stop(left), KeyEvent.VK_LEFT);

      final ContinuousPan right = new ContinuousPan(getView(), ContinuousPan.RIGHT, 600);
      TLspKeyActionController keyControllerRight = new TLspKeyActionController(
          start(right), stop(right), KeyEvent.VK_RIGHT);

      final ContinuousPan up = new ContinuousPan(getView(), ContinuousPan.UP, 600);
      TLspKeyActionController keyControllerUp = new TLspKeyActionController(
          start(up), stop(up), KeyEvent.VK_UP);

      final ContinuousPan down = new ContinuousPan(getView(), ContinuousPan.DOWN, 600);
      TLspKeyActionController keyControllerDown = new TLspKeyActionController(
          start(down), stop(down), KeyEvent.VK_DOWN);

      final ContinuousRotate yawLeft = new ContinuousRotate(getView(), 60, 0);
      TLspKeyActionController keyControllerInsert = new TLspKeyActionController(
          start(yawLeft), stop(yawLeft), KeyEvent.VK_INSERT);

      final ContinuousRotate yawRight = new ContinuousRotate(getView(), -60, 0);
      TLspKeyActionController keyControllerDelete = new TLspKeyActionController(
          start(yawRight), stop(yawRight), KeyEvent.VK_DELETE);

      final ContinuousRotate pitchUp = new ContinuousRotate(getView(), 0, 60);
      TLspKeyActionController keyControllerPageUp = new TLspKeyActionController(
          start(pitchUp), stop(pitchUp), KeyEvent.VK_PAGE_UP);

      final ContinuousRotate pitchDown = new ContinuousRotate(getView(), 0, -60);
      TLspKeyActionController keyControllerPageDown = new TLspKeyActionController(
          start(pitchDown), stop(pitchDown), KeyEvent.VK_PAGE_DOWN);

      final ContinuousZoom zoomOut = new ContinuousZoom(getView(), 0.5);
      TLspKeyActionController keyControllerEnd = new TLspKeyActionController(
          start(zoomOut), stop(zoomOut), KeyEvent.VK_END);

      final ContinuousZoom zoomIn = new ContinuousZoom(getView(), 2.0);
      TLspKeyActionController keyControllerHome = new TLspKeyActionController(
          start(zoomIn), stop(zoomIn), KeyEvent.VK_HOME);

      TLspKeyActionController keyControllerFullScreen = new TLspKeyActionController(
          null, getFullScreenAction(), KeyEvent.VK_F);

      flyToController.appendController(fullScreenController);
      flyToController.appendController(keyControllerLeft);
      flyToController.appendController(keyControllerRight);
      flyToController.appendController(keyControllerUp);
      flyToController.appendController(keyControllerDown);
      flyToController.appendController(keyControllerInsert);
      flyToController.appendController(keyControllerDelete);
      flyToController.appendController(keyControllerPageUp);
      flyToController.appendController(keyControllerPageDown);
      flyToController.appendController(keyControllerEnd);
      flyToController.appendController(keyControllerHome);
      flyToController.appendController(keyControllerFullScreen);

      flyToController.appendController(ControllerFactory.createNavigationController());

      flyToController.setIcon(TLcdIconFactory.create(TLcdIconFactory.PROPERTIES_ICON));

      flyToController.setShortDescription(
          "<html>" +
          "<p>Navigate:</p>" +
          "<p>- <b>Left mouse</b>: pan</p>" +
          "<p>- <b>Mouse wheel</b>: zoom</p>" +
          "<p>- <b>Right mouse</b>: rotate</p>" +
          "<p>- <b>META + click</b>: fly to animation</p>" +
          "<p></p>" +
          "<p>Actions:</p>" +
          "<p>- <b>Arrow keys</b>: pan the view left, up, right and down.</p>" +
          "<p>- <b>Home and End</b>: zoom in and out.</p>" +
          "<p>- <b>Insert and Delete</b>: rotate yaw left and right.</p>" +
          "<p>- <b>Page Up and Page Down</b>: rotate pitch forward and backward (only 3D).</p>" +
          "<p>- <b>F key</b>: toggle full screen mode.</p>" +
          "<p>- <b>Double click</b>: toggle full screen mode.</p>" +
          "</html>"
      );

      return flyToController;
    }
  }

  private ILcdAction start(final AbstractContinuousAnimation aContinuousAnimation) {
    return new ALcdAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aContinuousAnimation.start();
      }
    };
  }

  private ILcdAction stop(final AbstractContinuousAnimation aContinuousAnimation) {
    return new ALcdAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aContinuousAnimation.stop();
      }
    };
  }

  /*
  * Rotation center is required in world coordinates. We want the center of the view.
  */
  private static ILcdPoint getRotateCenter(ALspViewXYZWorldTransformation aViewXYZWorldTransformation) {
    if (aViewXYZWorldTransformation instanceof TLspViewXYZWorldTransformation3D) {
      // Simply return the reference point.
      return ((TLspViewXYZWorldTransformation3D) aViewXYZWorldTransformation).getReferencePoint();
    } else if (aViewXYZWorldTransformation instanceof TLspViewXYZWorldTransformation2D) {
      TLspViewXYZWorldTransformation2D w2v = (TLspViewXYZWorldTransformation2D) aViewXYZWorldTransformation;
      int x = (int) (aViewXYZWorldTransformation.getWidth() * 0.5);
      int y = (int) (aViewXYZWorldTransformation.getHeight() * 0.5);

      // If the view origin is the center, return the world origin.
      if (w2v.getViewOrigin().x == x &&
          w2v.getViewOrigin().y == y) {
        return w2v.getWorldOrigin();
      } else {
        // Calculate the appropriate world origin.
        TLcdXYPoint viewPoint = new TLcdXYPoint(x, y);
        TLcdXYZPoint worldPoint = new TLcdXYZPoint();
        w2v.viewPoint2WorldSFCT(viewPoint, worldPoint);
        return worldPoint;
      }
    } else {
      throw new IllegalStateException("Unknown ViewXYWWorldTransformation: " + aViewXYZWorldTransformation);
    }
  }

  /**
   * Makes continuous panning via distinct directions possible. Supported directions are:
   *
   * <ul> <li>LEFT</li> <li>RIGHT</li> <li>UP</li> <li>DOWN</li> </ul>
   */
  private static class ContinuousPan extends AbstractContinuousAnimation {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    private int fDirection;
    private int fPanSpeed;

    public ContinuousPan(ILspView aView, int aDirection, int aPanSpeed) {
      super(aView);
      fDirection = aDirection;
      fPanSpeed = aPanSpeed;
    }

    protected ILcdAnimation createAnimation(ILspView aView) {
      double x = 0, y = 0;

      switch (fDirection) {
      case LEFT:
        x = -fPanSpeed;
        break;
      case RIGHT:
        x = +fPanSpeed;
        break;
      case UP:
        y = -fPanSpeed;
        break;
      case DOWN:
        y = +fPanSpeed;
        break;
      }

      return new TLspViewNavigationUtil(aView).animatedContinuousPan(x, y);
    }
  }

  /**
   * Makes continuous rotation based on yaw speed and pitch speed possible.
   */
  private static class ContinuousRotate extends AbstractContinuousAnimation {

    private double fYawSpeed;
    private double fPitchSpeed;

    public ContinuousRotate(ILspView aView, double aYawSpeed, double aPitchSpeed) {
      super(aView);
      fYawSpeed = aYawSpeed;
      fPitchSpeed = aPitchSpeed;
    }

    protected ILcdAnimation createAnimation(ILspView aView) {
      ILcdPoint rotateCenter = getRotateCenter(aView.getViewXYZWorldTransformation());
      return new TLspViewNavigationUtil(aView).animatedContinuousRotate(rotateCenter, fYawSpeed, fPitchSpeed);
    }
  }

  /**
   * Makes continuous zooming to the center of the view possible.
   */
  private static class ContinuousZoom extends AbstractContinuousAnimation {

    private double fZoomSpeed;

    public ContinuousZoom(ILspView aView, double aZoomSpeed) {
      super(aView);
      fZoomSpeed = aZoomSpeed;
    }

    protected ILcdAnimation createAnimation(ILspView aView) {
      ILcdPoint center = new TLcdXYPoint(aView.getWidth() * 0.5, aView.getHeight() * 0.5);
      return new TLspViewNavigationUtil(aView).animatedContinuousZoom(center, fZoomSpeed);
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Custom controllers");
  }

}
