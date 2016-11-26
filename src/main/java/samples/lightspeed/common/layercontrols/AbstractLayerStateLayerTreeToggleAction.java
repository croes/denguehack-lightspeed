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

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerStateListener;
import com.luciad.view.lightspeed.layer.TLspLayerPaintStateEvent;
import com.luciad.view.lightspeed.layer.TLspLayerStateEvent;

import samples.common.layerControls.actions.AbstractLayerTreeToggleAction;

/**
 * <p>Abstract class which helps in creating toggle-actions for a layer tree, where the status of
 * the action depends on the layer state.</p>
 *
 * @see ILspLayerStateListener
 * @see TLspLayerStateEvent
 * @see TLspLayerPaintStateEvent
 */
public abstract class AbstractLayerStateLayerTreeToggleAction extends AbstractLayerTreeToggleAction {

  private MyStatusListener fListener = new MyStatusListener(this);
  private TLspLayerStateEvent.Type fLayerStateType;
  private TLspLayerPaintStateEvent.Type fLayerPaintStateType;

  /**
   * Create a new toggle action that is influenced by the change of a layer property for a specific
   * for a specific {@link com.luciad.view.lightspeed.layer.TLspPaintRepresentation}.
   *
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for
   * @param aType    the type of layer state change which influences this action toggles
   * @param aIcon    The icon for the action
   */
  protected AbstractLayerStateLayerTreeToggleAction(ILcdTreeLayered aLayered, TLspLayerStateEvent.Type aType, ILcdIcon aIcon) {
    super(aLayered, aIcon);
    fLayerStateType = aType;
  }

  /**
   * Create a new toggle action that is influenced by the change of a layer property for a specific
   * for a specific {@link com.luciad.view.lightspeed.layer.TLspPaintRepresentationState}.
   *
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for
   * @param aType    the type of layer state change which influences this action toggles
   * @param aIcon    The icon for the action
   */
  protected AbstractLayerStateLayerTreeToggleAction(ILcdTreeLayered aLayered, TLspLayerPaintStateEvent.Type aType, ILcdIcon aIcon) {
    super(aLayered, aIcon);
    fLayerPaintStateType = aType;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    boolean active = fListener.isActive();
    fListener.setActive(false);
    try {
      //make sure the listener is disabled while updating the status of the layers
      super.actionPerformed(e);
    } finally {
      fListener.setActive(active);
    }
  }

  @Override
  public void setLayers(List<ILcdLayer> aLayers) {
    //first remove the listener from all the layers
    List<ILcdLayer> oldLayers = getLayers();
    for (ILcdLayer layer : oldLayers) {
      if (layer instanceof ILspLayer) {
        ((ILspLayer) layer).removeLayerStateListener(fListener);
      }
    }
    super.setLayers(aLayers);
    //add the listener to all the layers
    for (ILcdLayer layer : aLayers) {
      if (layer instanceof ILspLayer) {
        ((ILspLayer) layer).addLayerStateListener(fListener);
      }
    }
  }

  private static class MyStatusListener implements ILspLayerStateListener {

    private WeakReference<AbstractLayerStateLayerTreeToggleAction> fAction = null;
    private boolean fActive = true;

    private MyStatusListener(AbstractLayerStateLayerTreeToggleAction aAction) {
      fAction = new WeakReference<AbstractLayerStateLayerTreeToggleAction>(aAction);
    }

    @Override
    public void layerStateEvent(TLspLayerStateEvent aEvent) {
      AbstractLayerStateLayerTreeToggleAction action = fAction.get();
      if (action == null) {
        aEvent.getLayer().removeLayerStateListener(this);
      } else if (fActive && aEvent.getType() == action.fLayerStateType) {
        action.updateState();
      }
    }

    @Override
    public void layerPaintStateEvent(TLspLayerPaintStateEvent aEvent) {
      AbstractLayerStateLayerTreeToggleAction action = fAction.get();
      if (action == null) {
        aEvent.getLayer().removeLayerStateListener(this);
      } else if (fActive && aEvent.getType() == action.fLayerPaintStateType) {
        action.updateState();
      }
    }

    public boolean isActive() {
      return fActive;
    }

    public void setActive(boolean aActive) {
      fActive = aActive;
    }
  }

}
