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
package samples.gxy.touch.touchEvents;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.controller.ILcdGXYChainableController;
import com.luciad.view.gxy.controller.touch.ALcdGXYTouchChainableController;

import samples.gxy.common.OverlayPanel;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.touch.GXYTouchSample;

/**
 * This sample demonstrates how to create TLcdTouchEvents from low level device events.
 * It does so by creating a TouchDevice object that simulates touch hardware by creating
 * a list of hard-coded events.
 * <p>
 * The TouchEventFactory intercepts these low level events, and converts them into
 * TLcdTouchEvents. These TLcdTouchEvents can then be used by LuciadLightspeed in the controllers.
 */
public class MainPanel extends GXYTouchSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-115, 22.50, 40.00, 30.00);
  }

  @Override
  protected TouchToolBar createTouchToolBar(boolean aTouchSupported) {
    // Touch input is always supported because we simulate it.
    TouchToolBar toolBar = new TouchToolBar(getView(), true, true, this, getOverlayPanel()) {
      @Override
      protected ILcdGXYController wrapController(ILcdGXYController aController) {
        ILcdGXYController controller = super.wrapController(aController);
        // Visualizes all touch input with icons.
        if (controller instanceof ILcdGXYChainableController) {
          TouchPointDrawingController touchPointDrawingController = new TouchPointDrawingController();
          touchPointDrawingController.setNextGXYController((ILcdGXYChainableController) controller);
          return touchPointDrawingController;
        }
        return controller;
      }
    };

    // The synthetic touch events simulate navigation, so we activate that controller.
    getView().setGXYController(toolBar.getWrappedController(toolBar.getNavigateController()));
    return toolBar;
  }

  @Override
  protected JComponent createLayerPanel() {
    // focus on the map
    return null;
  }

  @Override
  protected void addOverlayComponents(OverlayPanel aOverlayPanel) {
    // don't obscure the map
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Create a new TouchDevice that simulates touch hardware by creating a series of DeviceEvents.
    TouchDevice simulation_device = new TouchDevice(12345);
    simulation_device.setRegisteredComponent(this);

    // Create a TouchEventFactory that converts DeviceEvents to TLcdTouchEvents.
    TouchEventFactory event_factory = new TouchEventFactory(simulation_device.getDeviceID());
    simulation_device.addDeviceEventListener(event_factory);

    // Start generating DeviceEvents.
    simulation_device.start();
  }

  /**
   * This controller intercepts all TLcdTouchEvents, and draws an icon for every touch
   * point on the view. It delegates all events to the next controller in the chain.
   */
  private static class TouchPointDrawingController extends ALcdGXYTouchChainableController {

    // Stores the current touch point locations in view coordinates.
    private List<Point> fTouchLocations = new ArrayList<Point>();

    // Icon to draw on the touch point locations.
    private ILcdIcon fIcon;

    public TouchPointDrawingController() {
      fIcon = new TLcdImageIcon("samples/images/hand.png");
    }

    public ILcdIcon getIcon() {
      if (getNextGXYController() != null) {
        return getNextGXYController().getIcon();
      }
      return new TLcdImageIcon("images/gui/i32_unknown.gif");
    }

    public void paintImpl(Graphics aGraphics) {
      for (Point point : fTouchLocations) {
        fIcon.paintIcon((Component) getGXYView(), aGraphics, point.x - 6, point.y - 2);
      }
    }

    @Override
    public void handleAWTEvent(AWTEvent aEvent) {
      if (aEvent instanceof TLcdTouchEvent) {
        TLcdTouchEvent touch_event = (TLcdTouchEvent) aEvent;

        // Collect all touch point locations.
        fTouchLocations.clear();
        for (TLcdTouchPoint touch_point : touch_event.getTouchPoints()) {
          if (touch_point.getState() != TLcdTouchPoint.State.UP) {
            fTouchLocations.add(new Point(touch_point.getLocation()));
          }
        }
      }
      super.handleAWTEvent(aEvent);
    }

    @Override
    protected void handleEventImpl(TLcdTouchEvent aTouchEvent) {
      // Repaint the view to make sure the touch point locations are correctly painted.
      getGXYView().repaint();
    }

    @Override
    protected List<TLcdTouchPoint> touchPointMoved(List<TLcdTouchPoint> aTrackedTouchPoints, TLcdTouchPoint aTouchMoved) {
      return new ArrayList<>();
    }

    @Override
    protected List<TLcdTouchPoint> touchPointAvailable(List<TLcdTouchPoint> aTouchPoints, TLcdTouchPoint aTouchDown) {
      return new ArrayList<>();
    }

    @Override
    protected List<TLcdTouchPoint> touchPointWithdrawn(List<TLcdTouchPoint> aTouchPoints, TLcdTouchPoint aTouchUp) {
      return new ArrayList<>();
    }
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Touch events");
  }
}
