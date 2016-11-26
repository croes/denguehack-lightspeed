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
package samples.lucy.formatbar;

import java.util.HashMap;
import java.util.Map;

import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;

/**
 * <p>{@link ALcyFormatBar} that keeps state that depends on the selected layer. Property change events
 * are also thrown in case properties of the current layer change.</p>
 *
 * <p>When a layer is removed, all properties
 * for that layer are automatically thrown away.</p>
 */
public abstract class LayerAwareFormatBar<S extends ILcdView & ILcdTreeLayered> extends ALcyFormatBar {

  private Map<ILcdLayer, Map<String, Object>> fState;

  public LayerAwareFormatBar(S aView) {
    fState = new HashMap<ILcdLayer, Map<String, Object>>();
    aView.addLayeredListener(new ILcdLayeredListener() {

      @Override
      public void layeredStateChanged(TLcdLayeredEvent aE) {
        switch (aE.getID()) {
        case TLcdLayeredEvent.LAYER_REMOVED:
          fState.remove(aE.getLayer());
        }
      }

    });
  }

  public void putCurrentValue(String aKey, Object aValue) {
    putValue(getLayer(), aKey, aValue);
  }

  public void putValue(ILcdLayer aLayer, String aKey, Object aValue) {
    Map<String, Object> values = fState.get(aLayer);
    if (values == null) {
      values = new HashMap<String, Object>();
      fState.put(aLayer, values);
    }
    Object oldValue = values.get(aKey);
    values.put(aKey, aValue);
    if (aLayer == getLayer()) {
      firePropertyChange(aKey, oldValue, aValue);
    }
  }

  public Object getCurrentValue(String aKey) {
    return getValue(getLayer(), aKey);
  }

  public Object getValue(ILcdLayer aLayer, String aKey) {
    Map<String, Object> values = fState.get(aLayer);
    if (values == null) {
      return null;
    }
    return values.get(aKey);
  }

}
