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
package samples.lightspeed.fundamentals.step4;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JToolBar;

import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;

import samples.gxy.fundamentals.step3.WayPointModelDecoder;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.fundamentals.step1.BasicLayerFactory;

/**
 * The sample demonstrates how to support on-map editing of vector data, including undo/redo.
 */
public class Main extends samples.lightspeed.fundamentals.step1.Main {

  // Used by the edit controller to track undoable operations.
  private TLcdUndoManager fUndoManager = new TLcdUndoManager();

  @Override
  protected ILspLayerFactory createLayerFactory() {
    // Create a layer factory that composes the way point layer factory
    // and the basic layer factory so that all those model types are supported.
    return new TLspCompositeLayerFactory(new EditableWayPointLayerFactory(), new BasicLayerFactory());
  }

  @Override
  protected void initLayers(ILspView aView) throws IOException {
    super.initLayers(aView);

    // Create the waypoint model and add it to the view
    ILcdModelDecoder waypointModelDecoder = new WayPointModelDecoder();
    ILcdModel waypointModel = waypointModelDecoder.decode("Data/Custom1/custom.cwp");

    Collection<ILspLayer> wayPointLayer = aView.addLayersFor(waypointModel);

    // Fit the view on it
    fitViewExtents(aView, wayPointLayer);
  }

  @Override
  protected JToolBar createToolBar(ILspView aView) {
    JToolBar toolBar = super.createToolBar(aView);

    // Add buttons for undo and redo
    TLcdUndoAction undo = new TLcdUndoAction(fUndoManager);
    toolBar.add(new TLcdSWAction(undo));
    TLcdRedoAction redo = new TLcdRedoAction(fUndoManager);
    toolBar.add(new TLcdSWAction(redo));

    return toolBar;
  }

  @Override
  protected void initController(ILspView aView) {
    // We use a utility method of the samples to assign the undo manager to the edit controller.
    // The controller also allows to select on the map, and to navigate around.
    // Buttons for undo/redo are added to the toolbar in the createToolBar() method.
    ILspController c = ControllerFactory.createGeneralController(fUndoManager, aView);

    // Assign the controller to the view
    aView.setController(c);
  }

  public static void main(String[] args) {
    // Switch to the Event Dispatch Thread, this is required by any Swing based application.
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new Main().start();
      }
    });
  }
}
