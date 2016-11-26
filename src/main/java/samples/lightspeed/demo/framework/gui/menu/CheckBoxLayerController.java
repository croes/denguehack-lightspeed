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
package samples.lightspeed.demo.framework.gui.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.jgoodies.binding.value.AbstractValueModel;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * This value model is used to bind the boolean value of a checkbox to the visibility property of
 * multiple layers.
 */
public class CheckBoxLayerController extends AbstractValueModel implements PropertyChangeListener {

  private List<ILspLayer> fLayers;

  public CheckBoxLayerController(List<ILspLayer> aLayers) {
    fLayers = aLayers;
    for (ILspLayer layer : fLayers) {
      layer.addPropertyChangeListener(this);
    }
  }

  @Override
  public Object getValue() {
    return fLayers.get(0).isVisible();
  }

  @Override
  public void setValue(Object o) {
    Boolean value = (Boolean) o;
    for (ILspLayer layer : fLayers) {
      layer.setVisible(value);
    }
    fireValueChange(null, value);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("visible")) {
      //layer visibility has changed
      fireValueChange(evt.getOldValue(), evt.getNewValue());
    }
  }
}
