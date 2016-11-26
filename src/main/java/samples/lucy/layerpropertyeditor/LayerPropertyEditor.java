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
package samples.lucy.layerpropertyeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import samples.lucy.layerpropertyeditor.CustomizerPanelImmediateApplyListener;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.context.TLcyLayerContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * A <code>LayerPropertyEditor</code> is an extension of a <code>JPanel</code> which
 * shows the customizer panel for the selected layer. This panel shows the properties
 * of the selected layer.   
 */
public class LayerPropertyEditor extends JPanel {

  private ILcyLucyEnv fLucyEnv;
  private ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> fMapComponent;

  ILcyCustomizerPanel fCurrentCustomizer;
  private final JLabel fNoSelectionComponent = new JLabel(TLcyLang.getString("No valid layers selected"), SwingConstants.CENTER);

  private final LayerSelectionListener fLayerSelectionListener = new LayerSelectionListener();
  private final PropertyChangeListener fApplyListener = new CustomizerPanelImmediateApplyListener();

  /**
   * Create a new LayerPropertyEditor for the given lucy environment.
   *
   * @param aLucyEnv the lucy environment to use.
   */
  public LayerPropertyEditor(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(fNoSelectionComponent, BorderLayout.CENTER);
  }

  /**
   * Returns the map component containing the layer control with the selected layers.
   * @return the map component containing the layer control with the selected layers.
   */
  public ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> getMapComponent() {
    return fMapComponent;
  }

  /**
   * Sets a new map component containing the layer control with the selected layers.
   * @param aMapComponent the new map component.
   */
  public void setMapComponent(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    // Remove the layer selection listener from the previous layer control
    if (fMapComponent != null) {
      fMapComponent.removePropertyChangeListener(fLayerSelectionListener);
    }

    fMapComponent = aMapComponent;

    // Add the layer selection listener to the current layer control
    if (fMapComponent != null) {
      fMapComponent.addPropertyChangeListener(fLayerSelectionListener);
    }

    // Update editor with the selected layers of the new layer control.
    updateFromSelection();
  }

  private final EventListenerList fListeners = new EventListenerList();

  public void addChangeListener(ILcdChangeListener aChangeListener) {
    fListeners.add(ILcdChangeListener.class, aChangeListener);
  }

  private void fireChangeEvent() {
    // Guaranteed to return a non-null array
    Object[] listeners = fListeners.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    TLcdChangeEvent event = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ILcdChangeListener.class) {
        // Lazily create the event:
        if (event == null) {
          event = new TLcdChangeEvent(this);
        }
        ((ILcdChangeListener) listeners[i + 1]).stateChanged(event);
      }
    }
  }

  private void updateFromSelection() {
    // Clean up the old customizer.
    if (fCurrentCustomizer != null) {
      fCurrentCustomizer.setObject(null);
      fCurrentCustomizer.removePropertyChangeListener(fApplyListener);
      fCurrentCustomizer = null;
    }

    // Update customizer.
    ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> map_component = getMapComponent();
    if (map_component != null) {
      List<? extends ILcdLayer> selected_layers = map_component.getSelectedLayersAsList();
      ILcdView mainView = map_component.getMainView();

      int nrOfSelectedObjects = selected_layers.size();
      if (nrOfSelectedObjects == 0) {
        showComponent(fNoSelectionComponent);
      } else if (nrOfSelectedObjects == 1) {
        // Only a single selected layer, find customizer for that layer.
        TLcyLayerContext context = new TLcyLayerContext(selected_layers.get(0), mainView);
        fCurrentCustomizer = createCustomizerPanelForContext(context);
        showComponent(fCurrentCustomizer != null ? (Component) fCurrentCustomizer : fNoSelectionComponent);
        fireChangeEvent();
      } else {
        // Multiple layers selected, find customizer for those layers.
        TLcyLayerContext[] contexts = new TLcyLayerContext[nrOfSelectedObjects];
        for (int i = 0; i < selected_layers.size(); i++) {
          contexts[i] = new TLcyLayerContext(selected_layers.get(i), mainView);
        }

        fCurrentCustomizer = createCustomizerPanelForContext(contexts);
        showComponent(fCurrentCustomizer != null ? (Component) fCurrentCustomizer : fNoSelectionComponent);
        fireChangeEvent();
      }
    }
  }

  private void showComponent(Component aComponent) {
    removeAll();
    add(aComponent, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  private ILcyCustomizerPanel createCustomizerPanelForContext(Object aContext) {
    // Get the composite factory from Lucy containing all registered customizer factories.
    TLcyCompositeCustomizerPanelFactory customizer_factory
        = fLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory();
    // Check if the factory can create a customizer for the given aContext.
    if (customizer_factory.canCreateCustomizerPanel(aContext)) {
      // Create a customizer for the given aContext.
      ILcyCustomizerPanel customizer_panel = customizer_factory.createCustomizerPanel(aContext);
      // Set the object to be edited.
      customizer_panel.setObject(aContext);
      // Add a listener to immediately apply all changes.
      customizer_panel.addPropertyChangeListener(fApplyListener);
      return customizer_panel;
    }
    return null;
  }

  /**
   * This listener will update the editor when the layer selection in
   * the layer control changes.
   */
  private class LayerSelectionListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent aEvent) {
      if ("selectedLayersAsList".equals(aEvent.getPropertyName())) {
        updateFromSelection();
      }
    }
  }
}
