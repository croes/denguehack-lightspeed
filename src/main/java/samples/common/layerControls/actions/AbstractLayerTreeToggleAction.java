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
import java.util.ArrayList;

import javax.swing.Action;

import com.luciad.gui.ILcdIcon;
import samples.common.layerControls.actions.AbstractLayerTreeAction;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

/**
 * <p>Abstract class which helps in creating toggle-actions for a layer tree, where the status of
 * the actions depends on a setting of the layer.</p>
 */
public abstract class AbstractLayerTreeToggleAction extends AbstractLayerTreeAction {

  /**
   * Create a new toggle action
   *
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for
   * @param aIcon    The icon for the action
   */
  protected AbstractLayerTreeToggleAction(ILcdTreeLayered aLayered, ILcdIcon aIcon) {
    super(aLayered);
    setIcon(aIcon);
    addLayerFilter(new ILcdFilter<ILcdLayer>() {
      @Override
      public boolean accept(ILcdLayer aLayer) {
        return layerSupported(aLayer);
      }
    });
  }

  /**
   * Returns <code>true</code> when the layer supports the setting for which this action is created,
   * <code>false</code> otherwise
   *
   * @param aLayer The layer to check
   *
   * @return <code>true</code> when the layer supports the setting for which this action is created,
   *         <code>false</code> otherwise
   */
  protected abstract boolean layerSupported(ILcdLayer aLayer);

  /**
   * Returns the status of the setting for which this action was created for layer
   * <code>aLayer</code>.
   *
   * @param aLayer The layer
   *
   * @return the status (=boolean value) of the setting for which this action was created for layer
   *         <code>aLayer</code>
   *
   * @see #setLayerStatus(boolean, ILcdLayer)
   */
  protected abstract boolean getLayerStatus(ILcdLayer aLayer);

  /**
   * Sets the status of the setting for which this action was created for <code>aLayer</code> to
   * <code>aStatus</code>
   *
   * @param aStatus The new value for the status
   * @param aLayer  The layer for which the status must be changed
   */
  protected abstract void setLayerStatus(boolean aStatus, ILcdLayer aLayer);

  /**
   * {@inheritDoc}
   *
   * <p>The selected state of the action is also updated to match the value of {@link
   * #shouldBeSelected()}</p>
   */
  @Override
  protected void updateState() {
    putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldBeSelected()));
    super.updateState();
  }

  @Override
  protected boolean shouldBeEnabled() {
    return getFilteredLayers().size() > 0;//when at least one layer passes the filter, enable the action
  }

  /**
   * <p>Returns <code>true</code> if and only if the action should be selected.</p>
   *
   * @return <code>true</code> if and only if the action should be selected.
   */
  protected boolean shouldBeSelected() {
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    //should be selected when all the layers which supports the property have the property configured to true
    for (ILcdLayer layer : layers) {
      if (!(getLayerStatus(layer))) {
        return false;
      }
    }
    return layers.size() > 0;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ArrayList<ILcdLayer> filteredLayers = getFilteredLayers();
    Boolean value = (Boolean) getValue(Action.SELECTED_KEY);
    for (ILcdLayer layer : filteredLayers) {
      setLayerStatus(value, layer);
    }
  }
}
