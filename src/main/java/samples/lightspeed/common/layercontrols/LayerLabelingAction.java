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

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerPaintStateEvent;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;

/**
 * Action to toggle the labeled state of a layer.
 * The action does not take into account if the selection is labeled or not.
 */
public class LayerLabelingAction extends AbstractLayerStateLayerTreeToggleAction {
  private static final String STRING_LABELED = "Toggle layer labeled";

  /**
   * <p>Create an action to toggle the labeled property of a layer.</p>
   *
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for
   */
  public LayerLabelingAction(ILcdTreeLayered aLayered) {
    super(aLayered, TLspLayerPaintStateEvent.Type.VISIBILITY_CHANGED, TLcdIconFactory.create(TLcdIconFactory.LABEL_ICON));
    setShortDescription(STRING_LABELED);
  }

  @Override
  protected boolean layerSupported(ILcdLayer aLayer) {
    if (aLayer instanceof ILspLayer) {
      return ((ILspLayer) aLayer).getPaintRepresentations().contains(TLspPaintRepresentation.LABEL);
    }
    return false;
  }

  @Override
  protected boolean getLayerStatus(ILcdLayer aLayer) {
    //only active when the label paint representation is visible
    ILspLayer lspLayer = (ILspLayer) aLayer;
    //using regular_label in stead of label makes sure the layer label control button is pressed only when body is labeled
    return lspLayer.isVisible(TLspPaintRepresentationState.REGULAR_LABEL);
  }

  @Override
  protected void setLayerStatus(boolean aStatus, ILcdLayer aLayer) {
    //set label paint representation visible
    ILspLayer lspLayer = (ILspLayer) aLayer;
    lspLayer.setVisible(TLspPaintRepresentation.LABEL, aStatus);
  }

}
