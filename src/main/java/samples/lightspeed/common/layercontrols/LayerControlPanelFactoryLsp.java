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
package samples.lightspeed.common.layercontrols;

import com.luciad.gui.ILcdAction;
import com.luciad.util.collections.ILcdList;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.swing.TLspBusyLayerCellRenderer;
import com.luciad.view.lightspeed.swing.TLspLayerTreeNodeCellRenderer;

import samples.common.LayerPaintExceptionHandler;
import samples.common.layerControls.LayerTreeNodeCellRendererWithActions;
import samples.common.layerControls.actions.EditableLayerAction;
import samples.common.layerControls.actions.LayerPaintExceptionDialogAction;
import samples.common.layerControls.actions.SelectableLayerAction;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.datatransfer.LayerTreeTransferHandler;

/**
 * Populates a LayerControlPanel with actions for selecting ILspLayers, setting visibility, toggling labels, ...
 * In particular, the following actions are supported:
 * <ul>
 * <li>change the layer ordering.</li>
 * <li>change whether the layer is selectable.</li>
 * <li>change whether the layer is editable.</li>
 * <li>change whether the layer is labeled.</li>
 * <li>fit the view to the selected layer.</li>
 * </ul>
 */
public class LayerControlPanelFactoryLsp {

  public static LayerControlPanel createDefaultLayerControlPanel(ILspView aView, ILcdList<ILcdLayer> aSelectedLayers) {
    LayerControlPanel layerControlPanel = new LayerControlPanel(aView, "Layers", aSelectedLayers);
    //sets up drag and drop
    layerControlPanel.getTree().setDragEnabled(true);
    layerControlPanel.getTree().setTransferHandler(new LayerTreeTransferHandler());
    TLspBusyLayerCellRenderer renderer = new TLspBusyLayerCellRenderer(aView, new TLspLayerTreeNodeCellRenderer(aView));
    //allows adding some extra buttons to the tree renderer
    LayerTreeNodeCellRendererWithActions rendererWithActions = new LayerTreeNodeCellRendererWithActions(renderer, aView);
    layerControlPanel.getTree().setCellRenderer(rendererWithActions);

    addMoveLayerButtons(layerControlPanel);
    ILcdAction defaultAction = addDefaultButtons(layerControlPanel, aView);
    addCreateRemoveNodesButtons(layerControlPanel);
    LayerControlPanelFactory2D.addChangeLabelButton(layerControlPanel);
    addToggleTerrainButton(layerControlPanel, aView);
    layerControlPanel.getTree().addMouseListener(new LayerControlPanelFactory2D.DoubleClickMouseListener(defaultAction));
    setupPaintExceptionHandler(layerControlPanel, aView, rendererWithActions);
    return layerControlPanel;
  }

  private static void setupPaintExceptionHandler(final LayerControlPanel aLayerControlPanel,
                                                 ILspView aView,
                                                 LayerTreeNodeCellRendererWithActions aRendererWithActions) {
    if (aView.getPaintExceptionHandler() instanceof LayerPaintExceptionHandler) {
      //links the exception handler to a button in the layer tree.
      LayerPaintExceptionHandler handler = (LayerPaintExceptionHandler) aView.getPaintExceptionHandler();
      aRendererWithActions.addAction(new LayerPaintExceptionDialogAction(aView, aLayerControlPanel, handler));
    }
  }

  public static void addCreateRemoveNodesButtons(LayerControlPanel aLayerControlPanel) {
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        new AddLspLayerTreeNodesAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));

    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        new RemoveLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));

  }

  public static void addToggleTerrainButton(LayerControlPanel aLayerControlPanel, ILspView aView) {
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createToggleButtonForTreeAction(
        new ToggleTerrainElevationAction(aLayerControlPanel.getLayered(), aView),
        false,
        aLayerControlPanel.getSelectedLayers()));
  }

  public static ILcdAction addDefaultButtons(LayerControlPanel aLayerControlPanel, ILspView aView) {
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createToggleButtonForTreeAction(
        new SelectableLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createToggleButtonForTreeAction(
        new EditableLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createToggleButtonForTreeAction(
        new LayerLabelingAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));

    FitLayerAction fitLayerAction = new FitLayerAction(aLayerControlPanel.getLayered(), aView);
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        fitLayerAction,
        false,
        aLayerControlPanel.getSelectedLayers()));

    return fitLayerAction;
  }

  public static void addMoveLayerButtons(LayerControlPanel aLayerControlPanel) {
    LayerControlPanelFactory2D.addMoveLayerButtons(aLayerControlPanel);
  }

}
