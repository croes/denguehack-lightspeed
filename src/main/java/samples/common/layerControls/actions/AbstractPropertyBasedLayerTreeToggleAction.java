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
package samples.common.layerControls.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

/**
 * <p>Abstract class which helps in creating toggle-actions actions for a layer tree, where the status of
 * the action depends on one property of the layer.</p>
 */
public abstract class AbstractPropertyBasedLayerTreeToggleAction extends AbstractLayerTreeToggleAction {

  private String fLayerPropertyName;
  private MyPropertyChangeListener fListener = new MyPropertyChangeListener(this);

  /**
   * <p>Create a new toggle action for the property with name <code>aLayerPropertyName</code>.</p>
   *
   * @param aLayered           the <code>ILcdTreeLayered</code> instance to create the action for
   * @param aLayerPropertyName the name of the property to create a toggle action for
   * @param aIcon              the icon for the action
   */
  public AbstractPropertyBasedLayerTreeToggleAction(ILcdTreeLayered aLayered, String aLayerPropertyName, ILcdIcon aIcon) {
    super(aLayered, aIcon);
    fLayerPropertyName = aLayerPropertyName;
  }

  @Override
  protected boolean layerSupported(ILcdLayer aLayer) {
    return layerSupportsProperty(aLayer);
  }

  @Override
  protected boolean getLayerStatus(ILcdLayer aLayer) {
    return getLayerProperty(aLayer);
  }

  @Override
  protected void setLayerStatus(boolean aStatus, ILcdLayer aLayer) {
    setLayerProperty(aStatus, aLayer);
  }

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
      layer.removePropertyChangeListener(fListener);
    }
    super.setLayers(aLayers);
    //add the listener to all the layers
    for (ILcdLayer layer : aLayers) {
      layer.addPropertyChangeListener(fListener);
    }
  }

  /**
   * <p>Returns <code>true</code> if and only if the layer <code>aLayer</code> supports the property
   * for which this toggle action is created.</p>
   *
   * @param aLayer the layer to be checked
   *
   * @return <code>true</code> if and only if the layer <code>aLayer</code> supports the property
   *         for which this toggle action is created.
   */
  abstract protected boolean layerSupportsProperty(ILcdLayer aLayer);

  /**
   * <p>Method which sets the layer property for which this action has been created to the value
   * <code>aNewValue</code>.</p>
   *
   * @param aNewValue the new value for the layer property
   * @param aLayer    the layer which must be adjusted
   */
  abstract protected void setLayerProperty(boolean aNewValue, ILcdLayer aLayer);

  /**
   * <p>Returns the current value of the layer property for which this action is created.</p>
   *
   * @param aLayer the layer for which the value of the property is requested
   *
   * @return the current value of the layer property for which this action is created.
   */
  abstract protected boolean getLayerProperty(ILcdLayer aLayer);

  private static class MyPropertyChangeListener implements PropertyChangeListener {

    private WeakReference<AbstractPropertyBasedLayerTreeToggleAction> fObject = null;
    private boolean fActive = true;

    public MyPropertyChangeListener(AbstractPropertyBasedLayerTreeToggleAction aObject) {
      fObject = new WeakReference<AbstractPropertyBasedLayerTreeToggleAction>(aObject);
    }

    public void propertyChange(PropertyChangeEvent aEvent) {
      AbstractPropertyBasedLayerTreeToggleAction object = fObject.get();
      if (object == null) {
        ((ILcdLayer) aEvent.getSource()).removePropertyChangeListener(this);
      } else if (fActive && aEvent != null && object.fLayerPropertyName.equalsIgnoreCase(aEvent.getPropertyName())) {
        object.updateState();
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
