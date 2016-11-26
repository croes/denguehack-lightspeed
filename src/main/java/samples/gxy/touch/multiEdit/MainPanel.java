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
package samples.gxy.touch.multiEdit;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.ALcdGXYChainableController;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNavigateController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchSelectEditController;

import samples.common.SampleData;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.hippodromePainter.HippodromeLayerFactory;
import samples.gxy.hippodromePainter.HippodromeModelFactory;
import samples.gxy.hippodromePainter.NewControllerModelHippodrome;
import samples.gxy.touch.GXYTouchSample;
import samples.gxy.touch.editing.TouchNewController;
import samples.gxy.touch.editing.TouchSelectEditController;

/**
 * This sample demonstrates how to create a touch controlled painter for a custom shape, the
 * hippodrome. It demonstrates roughly the same functionality as the hippodromePainter sample.
 * The main difference is that touch input is used instead of mouse input. Because touch input
 * isn't limited to a single point, we also demonstrate edit behaviour with multiple fingers,
 * and show how an editor can be written using more than one input point.
 */
public class MainPanel extends GXYTouchSample {

  private ILcdGXYLayer fGeodeticHippodromeLayer;
  private ILcdGXYLayer fGridHippodromeLayer;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-4.00, 49.00, 9.00, 5.00);
  }

  @Override
  protected TouchToolBar createTouchToolBar(boolean aTouchSupported) {
    // Create the hippodrome layers.
    HippodromeLayerFactory factory = new HippodromeLayerFactory();
    fGeodeticHippodromeLayer = factory.createGXYLayer(HippodromeModelFactory.createGeodeticHippodromeModel());
    fGridHippodromeLayer = factory.createGXYLayer(HippodromeModelFactory.createGridHippodromeModel());

    // Add an edit-by-touch controller to the toolbar and set it as the active controller
    TLcdGXYTouchSelectEditController editController =
        createGXYTouchEditController(new TLcdImageIcon("images/gui/i16_handgrab.gif"),
                                     "Edit by touch");
    //use instant editing mode for multi-point editing
    editController.setInstantEditing(true);

    TouchToolBar toolBar = new TouchToolBar(getView(), true, aTouchSupported, this, getOverlayPanel(), editController);

    // create and add a controller to activate the controller to make geodetic hippodromes.
    addGXYNewController(toolBar,
                        createNewHippodromeModel(NewControllerModelHippodrome.Mode.GEODETIC),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON),
                        "Create a new Lon Lat hippodrome"
    );

    // create and add a controller to activate the controller to make XY hippodromes.
    addGXYNewController(toolBar,
                        createNewHippodromeModel(NewControllerModelHippodrome.Mode.GRID),
                        TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON),
                        "Create a new XY hippodrome"
    );

    // We want to allow snapping from one hippodrome to the other, so we have to connect them
    // with the snappables list.
    toolBar.getSnappables().getSnappableLayers().add(fGeodeticHippodromeLayer);
    toolBar.getSnappables().getSnappableLayers().add(fGridHippodromeLayer);
    return toolBar;
  }

  @Override
  protected void addData() {
    // Add the world layer
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    // we intend to edit these layers, so these should not be regarded as background
    // layers as they will be repainted frequently.
    GXYLayerUtil.addGXYLayer(getView(), fGeodeticHippodromeLayer, false, false);
    GXYLayerUtil.addGXYLayer(getView(), fGridHippodromeLayer, true, false);
  }

  /**
   * Creates the edit-by-touch controller
   *
   * @param aIcon        the icon for the controller in the toolbar
   * @param aDescription the description for the controller in the toolbar
   *
   * @return the created controller
   */
  private TLcdGXYTouchSelectEditController createGXYTouchEditController(TLcdImageIcon aIcon, String aDescription) {
    //make an edit controller which can handle up to 2 touch points
    TLcdGXYTouchSelectEditController controller = new TouchSelectEditController(getOverlayPanel(), 2);
    controller.setIcon(aIcon);                      // Set the icon
    controller.setShortDescription(aDescription);   // Set the short descriptions
    controller.setName("");                         // Set the name

    //append a navigation controller to the edit controller
    controller.setNextGXYController(new TLcdGXYTouchNavigateController());

    return controller;
  }

  /**
   * Creates a new-by-touch controller and adds it to the toolbar
   *
   * @param aToolbarSW the toolbar to add the controller to
   * @param aModel the model of the controller
   * @param aIcon the icon for the controller in the toolbar
   * @param aDescription the description for the controller in the toolbar
   * @return the created controller
   */
  private ALcdGXYChainableController addGXYNewController(TouchToolBar aToolbarSW,
                                                         ALcdGXYNewControllerModel2 aModel,
                                                         ILcdIcon aIcon,
                                                         String aDescription) {
    TLcdGXYTouchNewController newController = new TouchNewController(aModel, getOverlayPanel());
    // Switch to edit mode after creating the ILcdShape
    newController.setActionToTriggerAfterCommit(
        new TLcdGXYSetControllerAction(getView(), aToolbarSW.getWrappedController(aToolbarSW.getEditController()))
    );
    newController.setIcon(aIcon);
    newController.setShortDescription(aDescription);

    // Set the objects you want to snap to while creating a new hippodrome.
    newController.setSnappables(aToolbarSW.getSnappables());

    // Add pan functionality.
    newController.appendGXYController(new TLcdGXYTouchNavigateController());

    // Add the controller to the toolbar
    aToolbarSW.addGXYController(newController);

    return newController;
  }

  /**
   * Returns a <code>ALcdGXYNewControllerModel2</code> instance for the creation of new hippodrome
   * shapes of the specified type.
   * @param aType the type of the hippodrome
   * @return a <code>ALcdGXYNewControllerModel2</code> instance for the creation of new hippodrome shapes
   */
  private ALcdGXYNewControllerModel2 createNewHippodromeModel(NewControllerModelHippodrome.Mode aType) {
    return new NewControllerModelHippodrome(aType);
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Implementing a touch editor");
  }
}
