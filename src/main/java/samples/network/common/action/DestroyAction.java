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
package samples.network.common.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;

import samples.network.common.graph.GraphManager;

public class DestroyAction extends ALcdAction {

  private ILcdGXYView fView;
  private GraphManager fGraphManager;

  public DestroyAction(ILcdGXYView aView,
                       GraphManager aGraphManager) {
    super("Destroy edge", TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON));
    fView = aView;
    fGraphManager = aGraphManager;
  }

  public void actionPerformed(ActionEvent e) {

    // Make a list with the selected vertex objects.
    List selected_edges = new ArrayList();

    int layer_index = 0;
    for (int index = 0; index < fView.layerCount(); index++) {

      if (fView.getLayer(index).getSelectionCount() > 0) {

        Enumeration selected_objects = fView.getLayer(index).selectedObjects();
        while (selected_objects.hasMoreElements()) {
          Object object = selected_objects.nextElement();
          selected_edges.add(object);
          layer_index = index;
        }
      }
    }

    if (selected_edges.size() == 1) {
      fGraphManager.destroyEdge(selected_edges.get(0));
      fView.invalidateGXYLayer((ILcdGXYLayer) fView.getLayer(layer_index), true, this, "");
    }
  }

}
