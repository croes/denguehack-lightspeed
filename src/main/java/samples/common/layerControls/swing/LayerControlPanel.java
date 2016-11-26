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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdList;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.swing.TLcdLayerTree;

import samples.common.SwingUtil;
import samples.gxy.common.TitledPanel;

/**
 * A TLcdLayerTree-based layer control panel.
 * It adds a horizontal and vertical toolbar, and exposes the selected layers.
 */
public class LayerControlPanel extends JPanel {

  private final ILcdTreeLayered fTreeLayered;

  private final JScrollPane fScrollPane;
  private final JToolBar fHorizontalToolBar = new JToolBar();
  private final JToolBar fVerticalToolBar = new JToolBar(JToolBar.VERTICAL);

  private final TLcdLayerTree fTree;
  private final ILcdList<ILcdLayer> fSelectedLayers;

  /**
   * <p>Create a new layer control panel for the <code>ILcdTreeLayered</code> instance.</p>
   * @param aLayered          the <code>ILcdTreeLayered</code> instance to create a layer control
   *                          for
   * @param aTitle            the title of the layer control panel
   * @param aSelectedLayersSFCT the list of selected layers. If the user selects a layer in the tree,
   *                            the layer will be added to this list.
   *                            The tree will not pick up external changes to the list.
   */
  public LayerControlPanel(ILcdTreeLayered aLayered, String aTitle, ILcdList<ILcdLayer> aSelectedLayersSFCT) {
    fTreeLayered = aLayered;
    fSelectedLayers = aSelectedLayersSFCT;
    fTree = new TLcdLayerTree(aLayered);
    fTree.setRootVisible(false);
    fTree.setShowsRootHandles(true);
    fTree.setEditable(false);
    fTree.setOpaque(false);
    fTree.setMinimumSize(new Dimension(180, (int) fTree.getMinimumSize().getHeight()));
    fScrollPane = new JScrollPane(fTree);
    fScrollPane.setPreferredSize(fTree.getMinimumSize());
    fScrollPane.setMinimumSize(fTree.getMinimumSize());
    init(aTitle);
    fTree.getModel().addTreeModelListener(new SelectNewlyAddedNodeListener(fTree));
    getTree().addTreeSelectionListener(new MyTreeSelectionListener());
  }

  /**
   * Returns the <code>ILcdLayered</code> of which the content is displayed.
   *
   * @return the <code>ILcdLayered</code> of which the content is displayed.
   */
  public ILcdTreeLayered getLayered() {
    return fTreeLayered;
  }

  /**
   * <p>Initialise the layer control panel.</p>
   *
   * @param aTitle the title for the panel
   */
  private void init(String aTitle) {

    fScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    buttonPanel.add(Box.createHorizontalGlue(), gbc);
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    SwingUtil.makeFlat(fHorizontalToolBar);
    buttonPanel.add(fHorizontalToolBar, gbc);
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    buttonPanel.add(Box.createHorizontalGlue(), gbc);

    JPanel content = new JPanel(new BorderLayout());
    content.add(fScrollPane, BorderLayout.CENTER);

    SwingUtil.makeFlat(fVerticalToolBar);
    content.add(fVerticalToolBar, BorderLayout.EAST);
    content.add(buttonPanel, BorderLayout.SOUTH);

    setLayout(new BorderLayout());
    add(TitledPanel.createTitledPanel(aTitle, content), BorderLayout.CENTER);
  }

  public JToolBar getHorizontalToolBar() {
    return fHorizontalToolBar;
  }

  public JToolBar getVerticalToolBar() {
    return fVerticalToolBar;
  }

  /**
   * <p>Returns the first of the selected layers in the map layer control, or <code>null</code> when
   * no layers are selected.</p>
   *
   * @return the first of the selected layers in the map layer control, or <code>null</code> when no
   *         layers are selected.
   *
   * @see #getSelectedLayers()
   */
  public ILcdLayer getSelectedLayer() {
    return fSelectedLayers.isEmpty() ? null : fSelectedLayers.get(0);
  }

  /**
   * Returns the currently selected layers in the map layer control.
   * The collection is automatically updated for selection changes through the layer tree UI or tree API.
   * Programmatic changes to this collection are not allowed.
   *
   * @return a collection containing the currently selected layers in the map layer control
   */
  public ILcdCollection<ILcdLayer> getSelectedLayers() {
    return fSelectedLayers;
  }

  /**
   * <p>Returns the tree used to display the hierarchical layer structure.</p>
   *
   * @return the tree used to display the hierarchical layer structure
   */
  public TLcdLayerTree getTree() {
    return fTree;
  }

  private class MyTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      for (TreePath p : e.getPaths()) {
        ILcdLayer layer = (ILcdLayer) p.getLastPathComponent();
        boolean added = e.isAddedPath(p);
        if (added) {
          fSelectedLayers.add(layer);
        } else {
          fSelectedLayers.remove(layer);
        }
      }
    }
  }
}
