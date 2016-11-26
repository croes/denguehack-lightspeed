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
package samples.lightspeed.common.controller;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspDeleteSelectionAction;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.TLspKeyActionController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspInteractiveLabelsController;
import com.luciad.view.lightspeed.controller.navigation.TLspPanController;
import com.luciad.view.lightspeed.controller.navigation.TLspRecenterProjectionController;
import com.luciad.view.lightspeed.controller.navigation.TLspRotateController;
import com.luciad.view.lightspeed.controller.navigation.TLspZoomController;
import com.luciad.view.lightspeed.controller.navigation.TLspZoomToController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.NoopStringTranslator;
import samples.common.action.ShowPopupAction;
import samples.common.action.ShowPropertiesAction;
import samples.common.lightspeed.visualinspection.FlickerController;
import samples.common.lightspeed.visualinspection.PortholeController;
import samples.common.lightspeed.visualinspection.SwipeController;
import samples.lightspeed.common.ToolBar;

/**
 * Factory for some popular controllers, showing typical filtering and chaining functionality.
 * @see samples.lightspeed.common.touch.TouchControllerFactory
 */
public class ControllerFactory {

  /**
   * The default controller used in most samples.
   * @param aUndoableListener an undoable listener for editing functionality.
   */
  public static ALspController createGeneralController(ILcdUndoableListener aUndoableListener, ILspView aView) {
    ShowPropertiesAction propertiesAction = new ShowPropertiesAction(aView, ToolBar.getParentComponent(aView));
    return createGeneralController(aUndoableListener, aView, new ILcdAction[]{propertiesAction}, propertiesAction, null);
  }

  /**
   * The default controller used in most samples, but configured with some custom actions.
   *
   * @param aUndoableListener  an undoable listener for editing functionality.
   * @param aView              The view
   * @param aPopupMenuActions  Actions which will be shown in the pop-up menu
   * @param aDoubleClickAction Action which will be triggered on a double click on a selected object
   * @param aStickyLabelsLayerFilter If not null, layers passing the filters will have their moving labels
   *                                temporarily immobilized whenever the mouse hovers above them
   */
  public static ALspController createGeneralController(ILcdUndoableListener aUndoableListener,
                                                       ILspView aView,
                                                       ILcdAction[] aPopupMenuActions,
                                                       ILcdAction aDoubleClickAction,
                                                       final ILcdFilter<ILspLayer> aStickyLabelsLayerFilter) {
    return createGeneralController(aUndoableListener, aView, aPopupMenuActions, aDoubleClickAction, null, aStickyLabelsLayerFilter);
  }

  /**
   * The default controller used in most samples, but configured with some custom actions.
   *
   * @param aUndoableListener  an undoable listener for editing functionality.
   * @param aView              The view
   * @param aPopupMenuActions  Actions which will be shown in the pop-up menu
   * @param aDoubleClickAction Action which will be triggered on a double click on a selected object
   * @param aInteractiveLabelsController an (optional) interactive labels controller
   * @param aStickyLabelsLayerFilter If not null, layers passing the filters will have their moving labels
   *                                temporarily immobilized whenever the mouse hovers above them
   */
  public static ALspController createGeneralController(ILcdUndoableListener aUndoableListener,
                                                       ILspView aView,
                                                       ILcdAction[] aPopupMenuActions,
                                                       ILcdAction aDoubleClickAction,
                                                       TLspInteractiveLabelsController aInteractiveLabelsController,
                                                       final ILcdFilter<ILspLayer> aStickyLabelsLayerFilter
  ) {
    ALspController zoomToController = createZoomToController();
    ALspController panController = createPanController();
    ALspController zoomController = createZoomController();
    ALspController rotateController = createRotateController();
    ALspController editController = createDefaultEditController(aUndoableListener, aView);
    TLspSelectController selectController = createDefaultSelectController();

    if (aView instanceof ILspAWTView && aPopupMenuActions != null && aPopupMenuActions.length > 0) {
      selectController.setContextAction(new ShowPopupAction(aPopupMenuActions, ((ILspAWTView) aView).getOverlayComponent()));
    }
    if (aDoubleClickAction != null) {
      selectController.setDoubleClickAction(aDoubleClickAction);
    }

    if (aView != null) {
      zoomToController.setAWTFilter(new ZoomToAWTFilter(zoomToController.getAWTFilter(), aView));
    }

    if (aInteractiveLabelsController != null) {
      editController.appendController(aInteractiveLabelsController);
    }
    editController.appendController(zoomToController);
    editController.appendController(selectController);
    editController.appendController(panController);
    editController.appendController(zoomController);
    editController.appendController(rotateController);

    if (aStickyLabelsLayerFilter != null && aView != null) {
      final StickyLabelsController stickyLabelsController = new StickyLabelsController();
      stickyLabelsController.appendController(editController);
      stickyLabelsController.setName(editController.getName());
      editController = stickyLabelsController;
      aView.addLayeredListener(new ILcdLayeredListener() {
        @Override
        public void layeredStateChanged(TLcdLayeredEvent e) {
          if (e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
            ILspLayer layer = (ILspLayer) e.getLayer();
            if (aStickyLabelsLayerFilter.accept(layer)) {
              stickyLabelsController.registerLayer(layer);
            }
          } else if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
            ILspLayer layer = (ILspLayer) e.getLayer();
            stickyLabelsController.unregisterLayer(layer);
          }
        }
      });
    }

    // Some general properties like the icon to use in a toolbar and the short description for the
    // tooltip are set on the top controller of the chain.
    editController.setIcon(TLcdIconFactory.create(TLcdIconFactory.ARROW_ICON));
    editController.setShortDescription("<html><p>Navigate/Select/Edit:</p><p><b>Left mouse</b>: select, edit or pan</p><p><b>Mouse wheel</b>: zoom</p><p><b>Right mouse</b>: rotate</p></html>");

    return editController;
  }

  /*
   * Default edit controller, with left mouse button filter.
   */
  public static TLspEditController createDefaultEditController(ILcdUndoableListener aUndoableListener, ILspView aView) {
    TLspEditController editController = new TLspEditController();
    if (aUndoableListener != null) {
      editController.addUndoableListener(aUndoableListener);
    }
    editController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().keyEvents().or().mouseWheelFilter().build());
    if (aView != null) {
      // Append a controller for deleting selected objects, using an action
      TLspDeleteSelectionAction action = new TLspDeleteSelectionAction(aView);
      editController.appendController(new TLspKeyActionController(action, KeyEvent.VK_DELETE));
      if (aUndoableListener != null) {
        //Add undoable listener to the controller so deletion can be undone.
        action.addUndoableListener(aUndoableListener);
      }
    }
    return editController;
  }

  /*
   * Default select controller, with left/right mouse button filter.
   */
  public static TLspSelectController createDefaultSelectController() {
    TLspSelectController selectController = new TLspSelectController();
    selectController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().rightMouseButton().build());
    return selectController;
  }

  /*
   * The default navigation controller: fly-to, pan, zoom and rotate.
   */
  public static ALspController createNavigationController() {
    // First we create the controllers we want to chain.
    ALspController zoomToController = createZoomToController();
    ALspController panController = createPanController();
    ALspController zoomController = createZoomController();
    ALspController rotateController = createRotateController();

    //Chain the controllers together, events will be offered to the first and trickle down.
    zoomToController.appendController(panController);
    zoomToController.appendController(zoomController);
    zoomToController.appendController(rotateController);

    //Set general properties on the top of the chain.
    zoomToController.setIcon(TLcdIconFactory.create(TLcdIconFactory.HAND_ICON));
    zoomToController.setShortDescription(
        "<html><p>Navigate:</p><p><b>Left mouse</b>: <ul><li>Drag: pan</li>" +
        "<li>Double click: fly to</li></ul></p><p><b>Mouse wheel</b>: zoom</p>" +
        "<p><b>Right mouse</b>: rotate</p></html>"
    );

    return zoomToController;
  }

  /*
   * Fly-to controller with left mouse button filter. This controller will only use double
   * click events, so in combination with the applied filter, only left mouse double
   * clicks or right mouse double clicks will trigger a fly-to.
   * Left mouse zooms in and right mouse zooms out.
   */
  private static ALspController createZoomToController() {
    TLspZoomToController zoomToController = new TLspZoomToController();
    zoomToController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        leftMouseButton().or().rightMouseButton().build());
    return zoomToController;
  }

  /*
   * Panning is the backup left mouse button behaviour (if editing is not possible), as well
   * as the default action mapped to the middle mouse button.
   */
  public static ALspController createPanController() {
    // Use a pan controller that consumes events during panning, e.g. mouse wheel events.
    TLspPanController panController = new GreedyPanController();
    panController.setEnableInertia(true);
    panController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        leftMouseButton().or().
                                                            middleMouseButton().or().
                                                            mouseWheelFilter().build());
    return panController;
  }

  /*
   * Zooming is the default action mapped to the mouse-wheel.
   */
  public static ALspController createZoomController() {
    TLspZoomController zoomController = new TLspZoomController();
    zoomController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        mouseWheelFilter().build());
    return zoomController;
  }

  /*
  * Rotating is the default action mapped to the right mouse button.
  */
  public static ALspController createRotateController() {
    TLspRotateController rotateController = new TLspRotateController();
    rotateController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
        rightMouseButton().build());
    return rotateController;
  }

  public static TLspRecenterProjectionController createRecenterProjectionController() {
    final TLspRecenterProjectionController recenterController = new TLspRecenterProjectionController();
    recenterController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());
    ALspController navigationController = createNavigationController();
    recenterController.appendController(navigationController);
    return recenterController;
  }

  public static TLspRulerController createRulerController(ILcdUndoableListener aListener) {
    TLspRulerController ruler = new RulerControllerWithPanel();
    ruler.addUndoableListener(aListener);
    ruler.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().rightMouseButton().or().keyEvents().build());
    ruler.appendController(createNavigationController());
    return ruler;
  }

  public static FlickerController createFlickerController(ILspAWTView aView, ILspController aFallbackController) {
    FlickerController controller = new FlickerController(aView, aFallbackController, new NoopStringTranslator());
    controller.appendController(ControllerFactory.createNavigationController());
    return controller;
  }

  public static SwipeController createSwipeController(ILspAWTView aView, ILcdUndoableListener aUndoableListener, ILspController aFallbackController) {
    SwipeController controller = new SwipeController(aView, aFallbackController, new NoopStringTranslator());
    controller.appendController(ControllerFactory.createGeneralController(aUndoableListener, aView));
    return controller;
  }

  public static PortholeController createPortholeController(ILspAWTView aView, ILcdUndoableListener aUndoableListener, ILspController aFallbackController) {
    PortholeController controller = new PortholeController(aView, aFallbackController, new NoopStringTranslator());
    controller.appendController(ControllerFactory.createGeneralController(aUndoableListener, aView));
    return controller;
  }

  /**
   * <p>Filter which makes sure that an AWT event will never be accepted in case some object is selected.
   * Otherwise it is up to the delegate filter to decide whether such an event is accepted.</p>
   */
  private static class ZoomToAWTFilter implements ILcdFilter<AWTEvent> {
    private ILcdFilter<AWTEvent> fDelegateFilter;
    private ILspView fView;

    public ZoomToAWTFilter(ILcdFilter<AWTEvent> aDelegateFilter, ILspView aView) {
      fDelegateFilter = aDelegateFilter;
      fView = aView;
    }

    @Override
    public boolean accept(AWTEvent aEvent) {
      if (!(aEvent instanceof MouseEvent)) {
        return false;
      }
      if (((MouseEvent) aEvent).getClickCount() != 2) {
        return false;
      }

      for (int i = 0; i < fView.layerCount(); i++) {
        if (fView.getLayer(i).getSelectionCount() > 0) {
          return false;
        }
      }
      return fDelegateFilter == null || fDelegateFilter.accept(aEvent);
    }
  }

}
