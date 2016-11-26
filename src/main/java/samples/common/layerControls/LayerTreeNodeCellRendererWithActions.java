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
package samples.common.layerControls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import samples.common.SwingUtil;
import samples.common.layerControls.actions.AbstractLayerTreeAction;
import com.luciad.view.ALcdWeakLayeredListener;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.TLcdLayeredEvent;

/**
 * Extends the delegate cell renderer with a toolbar to configure layer-specific {@link #addAction actions}.
 */
public class LayerTreeNodeCellRendererWithActions extends JPanel implements TreeCellRenderer {

  private final JToolBar fActionBar = new JToolBar();
  private final List<AbstractLayerTreeAction> fActions = new ArrayList<>();
  private final TreeCellRenderer fDelegate;
  private final RepaintListener fListener = new RepaintListener();
  private JTree fTree; // lazily initialized

  public LayerTreeNodeCellRendererWithActions(TreeCellRenderer aDelegate, ILcdLayered aView) {
    fDelegate = aDelegate;
    SwingUtil.makeFlat(fActionBar);
    setLayout(new BorderLayout());
    aView.addLayeredListener(new LayerRemovalListener(this));
  }

  public void addAction(AbstractLayerTreeAction aAction) {
    AbstractButton button = SwingUtil.createButtonForAction(this, aAction, false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    fActionBar.add(button);
    fActions.add(aAction);
    aAction.addPropertyChangeListener(fListener);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    fListener.fActive = false;

    try {
      if (fTree == null) {
        fTree = tree;
      }

      removeAll();
      Component result = fDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      if (value instanceof ILcdLayer) {
        for (AbstractLayerTreeAction action : fActions) {
          List<ILcdLayer> layer = Collections.singletonList((ILcdLayer) value);
          action.setLayers(layer);
        }
      }
      // JTree's tooltips are derived from the cell renderer component.
      // JTree does not know about our delegate, so we need to copy the tooltip manually.
      if (result instanceof JComponent) {
        setToolTipText(((JComponent) result).getToolTipText());
      }
      add(result, BorderLayout.CENTER);
      add(fActionBar, BorderLayout.EAST);
      setOpaque(false);
      return this;
    } finally {
      fListener.fActive = true;
    }
  }

  private void layerRemoved(ILcdLayer aLayer) {
    for (AbstractLayerTreeAction action : fActions) {
      List<ILcdLayer> layers = action.getLayers();
      if (layers.contains(aLayer)) {
        List<ILcdLayer> modified = new ArrayList<>(layers);
        modified.remove(aLayer);
        action.setLayers(modified);
      }
    }
  }

  private class RepaintListener implements PropertyChangeListener {

    // avoids triggering repaints during painting
    boolean fActive = true;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (fActive && fTree != null) {
        fTree.repaint();
      }
    }
  }

  private static class LayerRemovalListener extends ALcdWeakLayeredListener<LayerTreeNodeCellRendererWithActions> {
    private LayerRemovalListener(LayerTreeNodeCellRendererWithActions aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    protected void layeredStateChangeImpl(LayerTreeNodeCellRendererWithActions aToModify, TLcdLayeredEvent aLayeredEvent) {
      if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        aToModify.layerRemoved(aLayeredEvent.getLayer());
      }
    }
  }
}
