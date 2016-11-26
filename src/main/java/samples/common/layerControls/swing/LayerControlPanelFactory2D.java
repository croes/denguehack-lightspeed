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
package samples.common.layerControls.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.swing.TLcdGXYBusyLayerTreeNodeCellRenderer;
import com.luciad.view.swing.TLcdLayerTree;

import samples.common.LayerPaintExceptionHandler;
import samples.common.gxy.GXYViewUtil;
import samples.common.layerControls.LayerTreeNodeCellRendererWithActions;
import samples.common.layerControls.actions.AbstractLayerTreeAction;
import samples.common.layerControls.actions.EditLabelAction;
import samples.common.layerControls.actions.EditableLayerAction;
import samples.common.layerControls.actions.LayerPaintExceptionDialogAction;
import samples.common.layerControls.actions.MoveLayerInFlatListAction;
import samples.common.layerControls.actions.MoveLayersAction;
import samples.common.layerControls.actions.SelectableLayerAction;
import samples.common.layerControls.swing.datatransfer.LayerTreeTransferHandler;
import samples.gxy.common.layerControls.actions.AddGXYLayerTreeNodesAction;
import samples.gxy.common.layerControls.actions.FitGXYLayersAction;
import samples.gxy.common.layerControls.actions.LabeledGXYLayerAction;
import samples.gxy.common.layerControls.actions.RemoveGXYLayersAction;

/**
 * Populates a LayerControlPanel with actions for selecting ILcdGXYLayers, setting visibility, toggling labels, ...
 * In particular, the following actions are supported:
 * <ul>
 * <li>change the layer ordering.</li>
 * <li>change whether the layer is selectable.</li>
 * <li>change whether the layer is editable.</li>
 * <li>change whether the layer is labeled.</li>
 * <li>fit the view to the selected layer.</li>
 * </ul>
 */
public class LayerControlPanelFactory2D {
  /**
   * <p>Private constructor, since this class is not meant to have any instances.</p>
   */
  private LayerControlPanelFactory2D() {

  }

  /**
   * <p>Creates the default swing layer control panel for the view <code>aGXYView</code>. This
   * default layer control panel features a drag and drop tree and the buttons created in {@link
   * #addMoveLayerButtons(LayerControlPanel)}, {@link #addDefaultGXYButtons(LayerControlPanel,
   * ILcdGXYView)} and {@link #addCreateRemoveNodeButtons(LayerControlPanel)}.
   * </p>
   *
   * @param aView the view to create a layer control for
   *
   * @return the default swing layer control panel for the view <code>aGXYView</code>
   */
  public static LayerControlPanel createDefaultGXYLayerControlPanel(ILcdGXYView aView) {
    return createDefaultGXYLayerControlPanel(aView, new TLcdArrayList<ILcdLayer>());
  }

  public static LayerControlPanel createDefaultGXYLayerControlPanel(ILcdGXYView aView, ILcdList<ILcdLayer> aSelectedLayersSFCT) {
    LayerControlPanel layerControlPanel = new LayerControlPanel(aView, "Layers", aSelectedLayersSFCT);
    TLcdGXYBusyLayerTreeNodeCellRenderer renderer = new TLcdGXYBusyLayerTreeNodeCellRenderer(aView);
    //allows adding some extra buttons to the tree renderer
    LayerTreeNodeCellRendererWithActions rendererWithActions = new LayerTreeNodeCellRendererWithActions(renderer, aView);
    layerControlPanel.getTree().setCellRenderer(rendererWithActions);

    //add drag and drop functionality to the tree
    layerControlPanel.getTree().setDragEnabled(true);
    layerControlPanel.getTree().setTransferHandler(new LayerTreeTransferHandler());
    addMoveLayerButtons(layerControlPanel);
    ILcdAction defaultAction = addDefaultGXYButtons(layerControlPanel, aView);
    layerControlPanel.getTree().addMouseListener(new DoubleClickMouseListener(defaultAction));
    addCreateRemoveNodeButtons(layerControlPanel);
    addChangeLabelButton(layerControlPanel);
    setupPaintExceptionHandler(layerControlPanel, aView, rendererWithActions);
    return layerControlPanel;
  }

  private static void setupPaintExceptionHandler(final LayerControlPanel aLayerControlPanel,
                                                 ILcdGXYView aView,
                                                 LayerTreeNodeCellRendererWithActions aRendererWithActions) {
    //sets up an exception handler that is linked to a button in the layer tree.
    LayerPaintExceptionHandler handler = new LayerPaintExceptionHandler();
    GXYViewUtil.setPaintExceptionHandler(aView, handler);
    aRendererWithActions.addAction(new LayerPaintExceptionDialogAction(aView, aLayerControlPanel, handler));
  }

  /**
   * Adds buttons to the layer control panel to:
   * <ul>
   * <li>Toggle the selectable status of the selected layer(s),</li>
   * <li>Toggle the editable status of the selected layers(s),</li>
   * <li>Toggle the labeled status of the selected layer(s),</li>
   * <li>Fit the view to the selected layer(s).</li>
   * </ul>
   *
   * @param aLayerControlPanel the layer control panel to add the buttons to
   * @param aGXYView           the view which must be fit to the selected layer(s)
   */
  public static ILcdAction addDefaultGXYButtons(LayerControlPanel aLayerControlPanel, ILcdGXYView aGXYView) {
    aLayerControlPanel.getHorizontalToolBar().add(createToggleButtonForTreeAction(
        new SelectableLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
    aLayerControlPanel.getHorizontalToolBar().add(createToggleButtonForTreeAction(
        new EditableLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
    aLayerControlPanel.getHorizontalToolBar().add(createToggleButtonForTreeAction(
        new LabeledGXYLayerAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
    FitGXYLayersAction defaultAction = new FitGXYLayersAction(aLayerControlPanel.getLayered(), aGXYView);
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        defaultAction,
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
    return defaultAction;
  }

  /**
   * Adds move buttons (up, down, top, bottom) to the layer control panel allowing to move the
   * selected layer(s).
   *
   * @param aLayerControlPanel the layer control panel
   */
  public static void addMoveLayerButtons(LayerControlPanel aLayerControlPanel) {
    aLayerControlPanel.getVerticalToolBar().add(createButtonForTreeAction(
        new MoveLayersAction(aLayerControlPanel.getLayered(), MoveLayersAction.MOVE_TOP),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getVerticalToolBar().add(createButtonForTreeAction(
        new MoveLayersAction(aLayerControlPanel.getLayered(), MoveLayersAction.MOVE_UP),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getVerticalToolBar().add(createButtonForTreeAction(
        new MoveLayersAction(aLayerControlPanel.getLayered(), MoveLayersAction.MOVE_DOWN),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getVerticalToolBar().add(createButtonForTreeAction(
        new MoveLayersAction(aLayerControlPanel.getLayered(), MoveLayersAction.MOVE_BOTTOM),
        false,
        aLayerControlPanel.getSelectedLayers()));
  }

  /**
   * Adds move buttons (up, down) to the layer control panel allowing to move the
   * selected layer in the flat layer list.
   *
   * @param aLayerControlPanel the layer control panel
   */
  public static void addMoveLayerInFlatListButtons(LayerControlPanel aLayerControlPanel) {
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        new MoveLayerInFlatListAction(aLayerControlPanel.getLayered(), true),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        new MoveLayerInFlatListAction(aLayerControlPanel.getLayered(), false),
        false,
        aLayerControlPanel.getSelectedLayers()));
  }

  /**
   * <p>Adds buttons to create nodes and remove layers to the layer control panel.</p>
   *
   * @param aLayerControlPanel the layer control panel
   */
  public static void addCreateRemoveNodeButtons(LayerControlPanel aLayerControlPanel) {
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        new AddGXYLayerTreeNodesAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        new RemoveGXYLayersAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()
    ));
  }

  /**
   * <p>Adds a button allowing to change the label of a layer.</p>
   *
   * @param aLayerControlPanel the layer control panel
   */
  public static void addChangeLabelButton(LayerControlPanel aLayerControlPanel) {
    aLayerControlPanel.getHorizontalToolBar().add(createButtonForTreeAction(
        new EditLabelAction(aLayerControlPanel.getLayered()),
        false,
        aLayerControlPanel.getSelectedLayers()));
  }

  /**
   * <p>Returns a button representing a tree action, and keep the state of the action and button in
   * sync and in sync with the selected layers in the layer control panel.</p>
   *
   * @param aAction   the action
   * @param aShowText set to <code>true</code> when the text of the action must be shown on the
   *                  button
   * @param aSelectedLayers the layers to keep the action in sync with
   *
   * @return a button representing a tree action
   */
  public static JButton createButtonForTreeAction(AbstractLayerTreeAction aAction, boolean aShowText, ILcdCollection<ILcdLayer> aSelectedLayers) {
    TLcdSWAction action = new TLcdSWAction(aAction);
    JButton result = new JButton(action);

    initButton(aAction, aShowText, aSelectedLayers, result);

    return result;
  }

  private static void initButton(final AbstractLayerTreeAction aAction, boolean showText, ILcdCollection<ILcdLayer> aSelectedLayers, AbstractButton aResultSFCT) {
    aResultSFCT.setHideActionText(!showText);
    aResultSFCT.setMargin(new Insets(2, 2, 2, 2));
    //make sure the button is in the right state
    aAction.setLayers(new ArrayList<>(aSelectedLayers));
    //keep it updated
    aAction.installSelectionListener(aSelectedLayers);
  }

  /**
   * <p>Returns a toggle button representing a tree toggle action. The state of the action and
   * button are linked, and the layers of the action are automatically updated with the selected
   * layers of the layer control panel.</p>
   *
   * @param aAction   the action. Must use <code>AbstractPropertyBasedLayerTreeToggleAction.SELECTED_KEY</code>
   *                  to indicate its toggle state.
   * @param aShowText set to <code>true</code> when the text of the action must be shown on the
   *                  button
   * @param aSelectedLayers the layers to synchronize the action with
   *
   * @return a toggle button representing a tree toggle action
   *
   * @see samples.common.layerControls.actions.AbstractPropertyBasedLayerTreeToggleAction#setLayers(java.util.List)
   */
  public static JToggleButton createToggleButtonForTreeAction(AbstractLayerTreeAction aAction, boolean aShowText, ILcdCollection<ILcdLayer> aSelectedLayers) {
    JToggleButton result = new JToggleButton(new TLcdSWAction(aAction));
    initButton(aAction, aShowText, aSelectedLayers, result);
    return result;
  }

  public static class DoubleClickMouseListener extends MouseAdapter {
    private final ILcdAction fAction;

    public DoubleClickMouseListener(ILcdAction aAction) {
      fAction = aAction;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() > 1 && isOnLabel(e)) {
        fAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
      }
    }

    // Only trigger double-click effect if clicked on the label, not the checkbox
    private boolean isOnLabel(MouseEvent e) {
      TLcdLayerTree tree = (TLcdLayerTree) e.getSource();
      int row = tree.getRowForLocation(((Double) e.getPoint().getX()).intValue(), ((Double) e.getPoint().getY()).intValue());
      if (row != -1) {
        Rectangle rowBounds = tree.getRowBounds(row);
        TreePath path = tree.getPathForRow(row);
        Component renderer = tree.getCellRenderer().getTreeCellRendererComponent(
            tree, path.getLastPathComponent(), tree.isPathSelected(path), tree.isExpanded(path),
            tree.getModel().isLeaf(path.getLastPathComponent()), row, false);
        renderer.setBounds(rowBounds);
        refreshLayout(renderer);
        Point p = new Point(e.getX() - rowBounds.x, e.getY() - rowBounds.y);
        Component comp = SwingUtilities.getDeepestComponentAt(renderer, p.x, p.y);
        return (comp instanceof JLabel);
      }
      return false;
    }

    private void refreshLayout(Component aComponent) {
      aComponent.doLayout();
      if (aComponent instanceof Container) {
        Component[] components = ((Container) aComponent).getComponents();
        for (Component component : components) {
          refreshLayout(component);
        }
      }
    }
  }
}
