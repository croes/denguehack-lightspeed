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
package samples.gxy.common.layerControls.actions;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.ILcdExceptionHandler;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYViewFitAction;

import samples.common.layerControls.actions.AbstractLayerTreeAction;

/**
 * <p>Action to fit the view to multiple gxy layers.</p>
 */
public class FitGXYLayersAction extends AbstractLayerTreeAction {

  private static String STRING_FIT_LAYER = "Fit view to layer";
  private ILcdGXYView fGXYView;
  private TLcdGXYViewFitAction fFitAction = new TLcdGXYViewFitAction();

  /**
   * <p>Create a new fit action for the view <code>aGXYView</code>.</p>
   *
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   * @param aGXYView the view which must be fit onto the layers, set on this action
   */
  public FitGXYLayersAction(ILcdTreeLayered aLayered, ILcdGXYView aGXYView) {
    super(aLayered);
    if (aGXYView == null) {
      throw new NullPointerException("The GXYView must not be null");
    }
    fGXYView = aGXYView;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIT_ICON));
    setShortDescription(STRING_FIT_LAYER);
  }

  public void actionPerformed(final ActionEvent e) {
    //fit to layers. In case of a layer node, fit to the union of all the child layers
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    if (layers.size() > 0) {
      HashSet<ILcdGXYLayer> layersToAdd = new HashSet<ILcdGXYLayer>();
      for (ILcdLayer layer : layers) {
        if (layer instanceof ILcdGXYLayer) {
          layersToAdd.add((ILcdGXYLayer) layer);
        }
      }
      if (layersToAdd.size() > 0) {
        fFitAction.setExceptionHandler(new ILcdExceptionHandler() {
          @Override
          public void handleException(Exception aException) {
            Window parentWindow = TLcdAWTUtil.findParentWindow(e);
            if (aException instanceof TLcdOutOfBoundsException) {
              JOptionPane.showMessageDialog(parentWindow, "Layer not visible in current projection.");
            } else {
              JOptionPane.showMessageDialog(parentWindow, "Could not fit on the layer.\n" + aException.getMessage());
            }
          }
        });
        fFitAction.fitGXYLayers(layersToAdd.toArray(new ILcdGXYLayer[layersToAdd.size()]), fGXYView, null);
      }
    }
  }

  protected boolean shouldBeEnabled() {
    return getFilteredLayers().size() > 0;
  }
}
