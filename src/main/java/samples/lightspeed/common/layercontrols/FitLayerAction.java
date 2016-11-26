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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.layerControls.actions.AbstractLayerTreeAction;

/**
 * Action that fits the view using an animated fly to on the selected node in a layer tree.
 */
public class FitLayerAction extends AbstractLayerTreeAction {

  private final TLspViewNavigationUtil fNavigationActions;

  public FitLayerAction(ILcdTreeLayered aLayered, ILspView aView) {
    super(aLayered);
    setShortDescription("Fit view to selected layer(s)");
    fNavigationActions = new TLspViewNavigationUtil(aView);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIT_ICON));
  }

  protected boolean shouldBeEnabled() {
    return getFilteredLayers().size() > 0;
  }

  public void actionPerformed(ActionEvent e) {
    ArrayList<ILspLayer> layers = new ArrayList<ILspLayer>();
    for (ILcdLayer layer : getFilteredLayers()) {
      layers.add((ILspLayer) layer);
    }
    try {
      fNavigationActions.animatedFit(layers);
    } catch (TLcdOutOfBoundsException ex) {
      Window parentWindow = TLcdAWTUtil.findParentWindow(e);
      JOptionPane.showMessageDialog(parentWindow, "Layer not visible in current projection.");
    } catch (TLcdNoBoundsException noBoundsEx) {
      Window parentWindow = TLcdAWTUtil.findParentWindow(e);
      JOptionPane.showMessageDialog(parentWindow, "Could not fit on the layer.\n" + noBoundsEx.getMessage());
    }
  }
}
