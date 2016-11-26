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
package samples.wms.server.config.editor.layer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.luciad.gui.TLcdSymbol;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSCapabilities;
import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.wms.server.config.editor.WMSEditListener;
import samples.wms.server.config.editor.WMSEditorHelp;
import samples.wms.server.config.editor.WMSEditorPanel;
import samples.wms.server.config.editor.util.GUIIconProvider;

/**
 * This component presents the user with a tree view containing the WMS layer
 * hierarchy. When a layer is selected, the right half of the component
 * displays a panel with editing controls for the selected layer.
 */
public class WMSLayerSelector extends WMSEditorPanel {

  private static final int LAYER_ADD = 1;
  private static final int LAYER_REMOVE = 2;
  private static final int LAYER_UP = 3;
  private static final int LAYER_DOWN = 4;

  private JSplitPane fSplitPane;
  private JTree fTree;
  private WMSLayerTreeModel fTreeModel;

  private TLcdWMSCapabilities fCapabilities;

  /**
   * Creates a new <code>WMSLayerSelector</code> instance.
   *
   * @param aCapabilities The capabilities to be edited.
   */
  public WMSLayerSelector(TLcdWMSCapabilities aCapabilities) {
    super(new BorderLayout(2, 2));
    fCapabilities = aCapabilities;

    // Add "haspaintstyle" properties to the layers if necessary.
    addPaintStylePresenceProperty(fCapabilities.getRootWMSLayer(0));

    // Create a model for the tree view, with an initial root layer.
    fTreeModel = new WMSLayerTreeModel(aCapabilities.getRootWMSLayer(0));

    fSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    add(BorderLayout.CENTER, fSplitPane);

    // Create the layer tree.
    fTree = new JTree();
    fTree.setModel(fTreeModel);
    fTree.setCellRenderer(new CustomTreeCellRenderer());
    fTree.addTreeSelectionListener(new LayerSelectListener());
    fTree.setToolTipText("An overview of all available map layers");
    fTree.setExpandsSelectedPaths(true);
    WMSEditorHelp.registerComponent(fTree, "layers.treeview");

    JScrollPane scroll = new JScrollPane(fTree);
    scroll.setMinimumSize(new Dimension(200, 200));

    JPanel treepanel = new JPanel(new BorderLayout());
    treepanel.add(BorderLayout.CENTER, scroll);

    // Create the layer control buttons.
    JPanel buttonpanel = new JPanel(new GridLayout(1, 4, 2, 2));
    JButton button;

    button = new JButton(GUIIconProvider.getIcon("images/icons/add_item_16.png"));
    button.addActionListener(new LayerControlButtonListener(LAYER_ADD));
    button.setToolTipText("Add a new layer as a child of the selected one");
    buttonpanel.add(button);
    WMSEditorHelp.registerComponent(button, "layers.treeview.add");

    button = new JButton(GUIIconProvider.getIcon("images/icons/remove_item_16.png"));
    button.addActionListener(new LayerControlButtonListener(LAYER_REMOVE));
    button.setToolTipText("Remove the selected layer from the tree");
    buttonpanel.add(button);
    WMSEditorHelp.registerComponent(button, "layers.treeview.remove");

    button = new JButton(GUIIconProvider.getIcon("images/icons/move_up_16.png"));
    button.addActionListener(new LayerControlButtonListener(LAYER_UP));
    button.setToolTipText("Move the selected layer up");
    buttonpanel.add(button);
    WMSEditorHelp.registerComponent(button, "layers.treeview.moveup");

    button = new JButton(GUIIconProvider.getIcon("images/icons/move_down_16.png"));
    button.addActionListener(new LayerControlButtonListener(LAYER_DOWN));
    button.setToolTipText("Move the selected layer down");
    buttonpanel.add(button);
    WMSEditorHelp.registerComponent(button, "layers.treeview.movedown");

    treepanel.add(BorderLayout.SOUTH, buttonpanel);

    fSplitPane.setLeftComponent(treepanel);

    /* Add a blank panel to the right half of the view. WHen a layer is
       selected, this will be replaced with the appropriate layer editor. */
    fSplitPane.setRightComponent(new JPanel());
  }

  private boolean layerExists(String aLayerName, ALcdWMSLayer aRootLayer) {

    if (aLayerName.equals(aRootLayer.getName())) {
      return true;
    }

    for (int i = 0; i < aRootLayer.getChildWMSLayerCount(); i++) {
      ALcdWMSLayer c = aRootLayer.getChildWMSLayer(i);
      if (layerExists(aLayerName, c)) {
        return true;
      }
    }

    return false;
  }

  private boolean layerExists(String aLayerName) {
    ALcdWMSLayer c = fCapabilities.getRootWMSLayer(0);
    if (layerExists(aLayerName, c)) {
      return true;
    }

    return false;
  }

  private String newLayerName() {
    int i = 0;
    String name = "new_layer_" + i;
    while (layerExists(name)) {
      i++;
      name = "new_layer_" + i;
    }

    return name;
  }

  /**
   * A utility function to test for the presence of a paint style.
   */
  private boolean hasPaintStyle(ALcdWMSLayer aLayer) {
    return (
        (aLayer.getProperty("mode") != null) ||
        (aLayer.getProperty("pointstyle.icon") != null) ||
        (aLayer.getProperty("linestyle.color") != null) ||
        (aLayer.getProperty("linestyle.width") != null) ||
        (aLayer.getProperty("fillstyle.color") != null)
    );
  }

  /**
   * If the given layer has a paint style, add a convenience property named
   * "haspaintstyle" (of type Boolean) to it, to facilitate later editing of
   * the paint style (specifically to allow the paint style to be removed).
   */
  private void addPaintStylePresenceProperty(ALcdWMSLayer aLayer) {
    aLayer.putProperty("haspaintstyle", Boolean.valueOf(hasPaintStyle(aLayer)));
    for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
      addPaintStylePresenceProperty(aLayer.getChildWMSLayer(i));
    }
  }

  /**
   * A listener for the layer control buttons.
   */
  private class LayerControlButtonListener implements ActionListener {

    private int fAction;

    public LayerControlButtonListener(int aAction) {
      fAction = aAction;
    }

    public void actionPerformed(ActionEvent e) {

      TreePath path = fTree.getSelectionPath();
      if (path == null) {
        return;
      }

      // Get the selected layer (if any).
      TLcdWMSLayer layer = null;
      Object node = path.getPathComponent(path.getPathCount() - 1);
      if (node instanceof TLcdWMSLayer) {
        layer = (TLcdWMSLayer) node;
      }

      // Get the selected layer's parent layer.
      TLcdWMSLayer parent = null;
      node = (path.getPathCount() >= 2 ? path.getPathComponent(path.getPathCount() - 2) : null);
      if (node instanceof TLcdWMSLayer) {
        parent = (TLcdWMSLayer) node;
      }

      switch (fAction) {
      case LAYER_ADD:
          /* Add a new child layer to the selected layer, or a new root layer
             if there is no selection. */

        if (layer != null) {
          TLcdWMSLayer newLayer = new TLcdWMSLayer();
          newLayer.setName(newLayerName());
          newLayer.setTitle("Untitled layer");
          newLayer.setNameVisible(true);
          newLayer.putProperty("pointstyle.icon", new TLcdSymbol(TLcdSymbol.CIRCLE, 10, Color.black));
          newLayer.putProperty("mode", "outline_filled");
          newLayer.putProperty("fillstyle.color", Color.white);
          layer.addChildWMSLayer(newLayer);

          // Create path to the new layer.
          TreePath newPath = path.pathByAddingChild(newLayer);

          // Update the tree model.
          fTreeModel.fireTreeStructureChanged(fTreeModel.getRoot());

          // Make sure the new node is visible.
          fTree.scrollPathToVisible(newPath);
          fTree.setSelectionPath(path);
        }
        break;
      case LAYER_REMOVE:
        // Delete the selected layer from the tree.
        if (layer != null && parent != null) {
          parent.removeChildWMSLayer(layer);
          fSplitPane.setRightComponent(new JPanel());
        }

        // Update the tree model.
        fTreeModel.fireTreeStructureChanged(fTreeModel.getRoot());

        break;
      case LAYER_UP:
        // Move the selected layer up by one position.
        if (layer != null && parent != null) {
          int i = parent.getChildWMSLayerIndex(layer);
          if (i > 0) {
            parent.moveLayerAt(layer, i - 1);
          }
        }

        // Update the tree model.
        fTreeModel.fireTreeStructureChanged(fTreeModel.getRoot());

        break;
      case LAYER_DOWN:
        // Move the selected layer down by one position.
        if (layer != null && parent != null) {
          int i = parent.getChildWMSLayerIndex(layer);
          if (i < parent.getChildWMSLayerCount() - 1) {
            parent.moveLayerAt(layer, i + 1);
          }
        }

        // Update the tree model.
        fTreeModel.fireTreeStructureChanged(fTreeModel.getRoot());

        break;
      }

      // Fire my own listeners and update the tree view.
      fireEditListeners(fCapabilities);
      fTree.revalidate();
    }
  }

  /**
   * A listener for layer edit events.
   */
  private class LayerEditListener implements WMSEditListener {
    public void editPerformed(Object aEditedObject) {
      // Fire my own listeners and update the tree view.
      fireEditListeners(aEditedObject);
      fTree.revalidate();
    }
  }

  /**
   * A listener for the selection changes in the tree view.
   */
  private class LayerSelectListener implements TreeSelectionListener {

    public void valueChanged(TreeSelectionEvent e) {
      /* When the selection changes, remove the current layer editor and build
         a new one for the selected layer. */
      Object node = e.getPath().getLastPathComponent();
      if (node instanceof TLcdWMSLayer) {
        if (fTreeModel.getRoot() == node) {
          WMSRootLayerEditor editor = new WMSRootLayerEditor((TLcdWMSLayer) node);
          editor.addEditListener(new LayerEditListener());
          fSplitPane.setRightComponent(editor);
        } else {
          WMSLayerEditor editor = new WMSLayerEditor((TLcdWMSLayer) node);
          editor.addEditListener(new LayerEditListener());
          fSplitPane.setRightComponent(editor);
        }
      } else {
        fSplitPane.setRightComponent(new JPanel());
      }
    }
  }
}
