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
package samples.gxy.hippodromePainter;

import java.io.IOException;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates how to create a painter for a custom shape, the hippodrome.
 * The hippodrome consists of 2 180 degrees arcs and 2 lines connecting these arcs and at a given distance from the
 * line between the center points (width).
 * <p/>
 * The painter provides functionality to:
 * <ul>
 * <li>paint the shape in default and selected mode.
 * <li>edit the shape by
 * <ul>
 * <li>moving the whole shape,
 * <li>moving the center of the circles,
 * <li>changing the radius of the circles,
 * <li>changing the width.
 * </ul>
 * <li>build a new shape.
 * <li>snap to the center points of the shape.
 * <li>let the center point of the shape snap to other points.
 * </ul>
 * <p/>
 * 2 shape implementations are provided with coordinates expressed in different references.
 */
public class MainPanel extends GXYSample {

  private ILcdGXYLayer fGeodeticHippodromeLayer;
  private ILcdGXYLayer fGridHippodromeLayer;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-4.00, 49.00, 9.00, 5.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Create the hippodrome layers.
    HippodromeLayerFactory factory = new HippodromeLayerFactory();
    fGeodeticHippodromeLayer = factory.createGXYLayer(HippodromeModelFactory.createGeodeticHippodromeModel());
    fGridHippodromeLayer = factory.createGXYLayer(HippodromeModelFactory.createGridHippodromeModel());

    // Create and add controllers to make new hippodromes.
    getToolBars()[0].addGXYController(createNewHippodromeAction(NewControllerModelHippodrome.Mode.GEODETIC));
    getToolBars()[0].addGXYController(createNewHippodromeAction(NewControllerModelHippodrome.Mode.GRID));

    // We want to allow snapping from one hippodrome to the other, so we have to connect them
    // with the snappables list.
    getToolBars()[0].getSnappables().getSnappableLayers().add(fGeodeticHippodromeLayer);
    getToolBars()[0].getSnappables().getSnappableLayers().add(fGridHippodromeLayer);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    // we intend to edit these layers, so these should not be regarded as background
    // layers as they will be repainted frequently.
    GXYLayerUtil.addGXYLayer(getView(), fGeodeticHippodromeLayer, false, false);
    GXYLayerUtil.addGXYLayer(getView(), fGridHippodromeLayer, true, false);
  }

  /**
   * Creates an action to put in the toolbar, that activates the controller to make new hippodromes.
   *
   * @param aType the type of hippodrome that will be created with this controller.
   * @return a controller that enables the user to create a hippodrome of the given type on the map using the mouse.
   */
  private ILcdGXYController createNewHippodromeAction(NewControllerModelHippodrome.Mode aType) {

    TLcdGXYNewController2 newController = new TLcdGXYNewController2();
    newController.setNewControllerModel(new NewControllerModelHippodrome(aType));

    // Sets the objects you want to snap to while creating a new hippodrome.
    newController.setSnappables(getToolBars()[0].getSnappables());
    // We want to activate the edit controller after creation of a new object.
    TLcdGXYSetControllerAction afterCreationAction = new TLcdGXYSetControllerAction(
        getView(), getToolBars()[0].getGXYCompositeEditController()
    );
    newController.setActionToTriggerAfterCommit(afterCreationAction);

    String name = "Create a new " + (aType == NewControllerModelHippodrome.Mode.GEODETIC ? "LonLat" : "XY") + " hippodrome";
    newController.setName(name);
    newController.setShortDescription(name);
    newController.setIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON));

    return newController;
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Implementing a painter/editor");
  }
}
