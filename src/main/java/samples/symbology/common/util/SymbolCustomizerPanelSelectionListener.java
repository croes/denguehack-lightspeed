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
package samples.symbology.common.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.MilitarySymbologyModelDescriptor;
import samples.symbology.common.app6.APP6ModelDescriptor;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.ms2525.MS2525ModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.util.ILcdSelection;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdPair;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;

/**
 * Whenever the user selects a symbol on the map, this listener sets the symbol on the given
 * customizer so that the user can view and edit its properties.
 * <p/>
 * Requires the symbol to be part of a layer with a MilitarySymbologyModelDescriptor.
 */
public class SymbolCustomizerPanelSelectionListener implements ILcdSelectionListener, PropertyChangeListener,
                                                               ILcdLayeredListener {

  private final AbstractSymbolCustomizer fCustomizer;
  private final Set<JComponent> fComponents;

  public SymbolCustomizerPanelSelectionListener(AbstractSymbolCustomizer aCustomizer) {
    fCustomizer = aCustomizer;
    fComponents = new HashSet<JComponent>();
  }

  public void addComponent(JComponent aComponent) {
    fComponents.add(aComponent);
  }

  public void removeComponent(JComponent aComponent) {
    fComponents.remove(aComponent);
  }

  @Override
  public void selectionChanged(TLcdSelectionChangedEvent aEvent) {
    TLcdPair<EMilitarySymbology, Object> symbologyAndSymbol = getSymbologyAndSymbol(aEvent);
    ILcdModel model = ((ILcdLayer) aEvent.getSelection()).getModel();
    if (null != symbologyAndSymbol) {
      fCustomizer.setSymbol(symbologyAndSymbol.getKey(), symbologyAndSymbol.getValue() != null ? model : null, symbologyAndSymbol.getValue());
      fCustomizer.setEnabled(symbologyAndSymbol.getKey() != null && symbologyAndSymbol.getValue() != null && ((ILcdLayer) aEvent.getSelection()).isEditable());
    } else {
      fCustomizer.setSymbol(null, null, null);
      if (fCustomizer.getSymbology() != null) {
        fCustomizer.setEnabled(false);
      }
    }
  }

  protected TLcdPair<EMilitarySymbology, Object> getSymbologyAndSymbol(TLcdSelectionChangedEvent aEvent) {
    ILcdSelection selection = aEvent.getSelection();
    ILcdModel model = ((ILcdLayer) aEvent.getSelection()).getModel();
    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    modelDescriptor = model instanceof ALcdTransformingModel ? ((ALcdTransformingModel) model).getOriginalModel().getModelDescriptor() : modelDescriptor;
    if (modelDescriptor instanceof MilitarySymbologyModelDescriptor) {
      EMilitarySymbology symbology = ((MilitarySymbologyModelDescriptor) modelDescriptor).getSymbology();
      if (selection.getSelectionCount() == 1) {
        Object symbol = selection.selectedObjects().nextElement();
        if (MilitarySymbolFacade.isMilitarySymbol(symbol)) {
          return new TLcdPair<>(symbology, symbol);
        }
      } else if (fCustomizer.getModel() == model && aEvent.containsElement(fCustomizer.getSymbol()) && !aEvent.retrieveChange(fCustomizer.getSymbol())) {
        return new TLcdPair<>(symbology, null);
      }
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("editable")) {
      if (evt.getSource() instanceof ILcdLayer && isSymbologyLayer((ILcdLayer) evt.getSource())) {
        setEnabled(((ILcdLayer) evt.getSource()).isEditable());
      }
    }
  }

  @Override
  public void layeredStateChanged(TLcdLayeredEvent evt) {
    if (evt.getID() == TLcdLayeredEvent.LAYER_ADDED) {
      if (isSymbologyLayer(evt.getLayer())) {
        setEnabled(evt.getLayer().isEditable());
      }
    }
  }

  private void setEnabled(boolean aEnabled) {
    if (fCustomizer.getSymbol() != null) {
      fCustomizer.setEnabled(aEnabled);
    }
    for (JComponent component : fComponents) {
      component.setEnabled(aEnabled);
    }
  }

  private boolean isSymbologyLayer(ILcdLayer aLayer) {
    ILcdModel model = aLayer.getModel();
    if (model == null) {
      return false;
    }
    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    modelDescriptor = model instanceof ALcdTransformingModel ? ((ALcdTransformingModel) model).getOriginalModel().getModelDescriptor() : modelDescriptor;
    return modelDescriptor instanceof APP6ModelDescriptor || modelDescriptor instanceof MS2525ModelDescriptor;
  }

}
