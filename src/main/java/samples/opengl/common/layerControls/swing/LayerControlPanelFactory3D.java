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

package samples.opengl.common.layerControls.swing;

import com.luciad.util.collections.TLcdArrayList;
import com.luciad.view.ILcdLayer;
import com.luciad.view.opengl.ILcdGLView;

import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.datatransfer.LayerTreeTransferHandler;
import samples.opengl.common.layerControls.swing.opengl.actions.AddGLLayerTreeNodesAction;
import samples.opengl.common.layerControls.swing.opengl.actions.CenterLayerAction;
import samples.opengl.common.layerControls.swing.opengl.actions.FillLayerAction;
import samples.opengl.common.layerControls.swing.opengl.actions.FitClipLayerAction;
import samples.opengl.common.layerControls.swing.opengl.actions.FitLayerAction;
import samples.opengl.common.layerControls.swing.opengl.actions.OutlineLayerAction;
import samples.opengl.common.layerControls.swing.opengl.actions.RemoveGLLayersAction;

/**
 * Factory that creates default GL layer control panels to avoid code duplication.
 * This panel will include the following buttons:
 * - all buttons to change the layer ordering.
 * - a button to fit the view to the selected layer.
 * - a button to fit the view to the selected layer and adjust the cutoff planes.
 * - a button to change whether the layers wireframe should be painted.
 * - a button to change whether the layers surfaces should be painted filled.
 */
public class LayerControlPanelFactory3D {

  public static LayerControlPanel createDefaultGLLayerControlPanel(ILcdGLView aView) {
    LayerControlPanel layerControlPanel = new LayerControlPanel(aView, "Layers", new TLcdArrayList<ILcdLayer>());
    layerControlPanel.getTree().setDragEnabled( true );
    layerControlPanel.getTree().setTransferHandler( new LayerTreeTransferHandler() );
    addMoveLayerButtons(layerControlPanel);
    addDefaultGLButtons(layerControlPanel, aView);
    addCreateRemoveNodeButtons( layerControlPanel );
    addChangeLabelButton( layerControlPanel );
    return layerControlPanel;
  }

  public static void addDefaultGLButtons(LayerControlPanel aLayerControlPanel, ILcdGLView aView) {
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        new FitLayerAction( aLayerControlPanel.getLayered(), aView ),
        false,
        aLayerControlPanel.getSelectedLayers()));


    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createToggleButtonForTreeAction(
        new OutlineLayerAction( aLayerControlPanel.getLayered() ),
        false,
        aLayerControlPanel.getSelectedLayers()));

  }

  public static void addMoveLayerButtons( LayerControlPanel aLayerControlPanel ) {
    LayerControlPanelFactory2D.addMoveLayerButtons( aLayerControlPanel );
  }

  public static void addCreateRemoveNodeButtons( LayerControlPanel aLayerControlPanel ) {
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        new AddGLLayerTreeNodesAction( aLayerControlPanel.getLayered() ),
        false,
        aLayerControlPanel.getSelectedLayers()));
    aLayerControlPanel.getHorizontalToolBar().add(LayerControlPanelFactory2D.createButtonForTreeAction(
        new RemoveGLLayersAction( aLayerControlPanel.getLayered() ),
        false,
        aLayerControlPanel.getSelectedLayers()));
  }

  public static void addChangeLabelButton( LayerControlPanel aLayerControlPanel){
    LayerControlPanelFactory2D.addChangeLabelButton(aLayerControlPanel);
  }
}
