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
package samples.lucy.frontend.mapcentric.modelcustomizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import samples.lucy.frontend.mapcentric.gui.MapCentricUtil;
import samples.lucy.layerpropertyeditor.CustomizerPanelImmediateApplyListener;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;

/**
 * Displays an ILcyCustomizerPanel for the model of the selected layer of the currently active map.
 * Note that selecting objects on the map automatically triggers updates in the layer selection.
 * It listens for changes in the selected layer, and updates the customizer panel accordingly.
 *
 * If no customizer panel can be displayed, a help message appears.
 */
class ModelCustomizerPanelContainer extends JPanel {
  private final PropertyChangeListener fApplyListener = new CustomizerPanelImmediateApplyListener();
  private final PropertyChangeListener fLayerSelectionListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("selectedLayersAsList".equals(evt.getPropertyName())) {
        updateCustomizerPanelAfterTriggeringLazyLoading();
      }
    }
  };
  private final ILcdLayeredListener fLayerRemovalListener = new ILcdLayeredListener() {
    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        updateCustomizerPanelAfterTriggeringLazyLoading();
      }
    }
  };

  private final ILcyLucyEnv fLucyEnv;
  private final JComponent fContentPane;

  private ILcyCustomizerPanel fCustomizerPanel;

  public ModelCustomizerPanelContainer(ILcyLucyEnv aLucyEnv) {
    super(new BorderLayout());
    fLucyEnv = aLucyEnv;

    fContentPane = MapCentricUtil.createContentWithHelpMessage(
        TLcyLang.getString("Select items with tabular data on the map or in the layers pane."));
    add(fContentPane, BorderLayout.CENTER);

    initListeners();
  }

  public ILcyCustomizerPanel getCustomizerPanel() {
    return fCustomizerPanel;
  }

  private void initListeners() {
    // Init for the currently active map, and track changes
    initForMap(fLucyEnv.getCombinedMapManager().getActiveMapComponent());
    fLucyEnv.getCombinedMapManager().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("activeMapComponent")) {
          tearDownForMap((ILcyGenericMapComponent) evt.getOldValue());
          initForMap((ILcyGenericMapComponent) evt.getNewValue());
        }
      }
    });
  }

  private void tearDownForMap(ILcyGenericMapComponent aMap) {
    if (aMap != null) {
      aMap.removePropertyChangeListener(fLayerSelectionListener);
      getView(aMap).removeLayeredListener(fLayerRemovalListener);
    }
  }

  private void initForMap(ILcyGenericMapComponent aMap) {
    if (aMap != null) {
      aMap.addPropertyChangeListener(fLayerSelectionListener);
      getView(aMap).addLayeredListener(fLayerRemovalListener);
    }
    updateCustomizerPanelAfterTriggeringLazyLoading();
  }

  private ILcdLayered getView(ILcyGenericMapComponent aMap) {
    return (ILcdLayered) aMap.getMainView();
  }

  private TLcyModelContext getModelContext() {
    ILcyGenericMapComponent map = fLucyEnv.getCombinedMapManager().getActiveMapComponent();
    if (map != null) {
      List layers = map.getSelectedLayersAsList();
      if (!layers.isEmpty()) {
        // A new layer is selected, use it.
        ILcdLayer layer = (ILcdLayer) layers.get(0);
        return new TLcyModelContext(layer.getModel(), layer, map.getMainView());
      }
    }
    return null;
  }

  private boolean stayPinned() {
    // The current customizer might not want to be updated (HINT_PINNED). Respect it if possible.
    boolean pinnedHint = Boolean.TRUE.equals(fCustomizerPanel.getValue(ILcyCustomizerPanel.HINT_PINNED));

    boolean pinnedPossible = false;
    // Cast is OK as we only ever set TLcyModelContext to it. context is never null.
    TLcyModelContext context = (TLcyModelContext) fCustomizerPanel.getObject();
    ILcyGenericMapComponent activeMap = fLucyEnv.getCombinedMapManager().getActiveMapComponent();
    if (activeMap != null) {
      // If the layer is no longer part of the view, pinning is no longer possible
      pinnedPossible = ((ILcdLayered) activeMap.getMainView()).containsLayer(context.getLayer());
    }

    return pinnedHint && pinnedPossible;
  }

  private void updateCustomizerPanelAfterTriggeringLazyLoading() {
    final TLcyModelContext context = getModelContext();
    LazyLoader.getInstance(fLucyEnv).triggerLazyLoadingOfModel(context, new Runnable() {
      @Override
      public void run() {
        updateCustomizerPanel(context);
      }
    });
  }

  private void updateCustomizerPanel(TLcyModelContext aContext) {
    if (fCustomizerPanel != null) {
      if (stayPinned()) {
        return;
      }

      // If no changes, return
      if (fCustomizerPanel.getObject().equals(aContext)) {
        return;
      }

      // Try to re-use the same customizer if possible
      if (aContext != null && fCustomizerPanel.canSetObject(aContext)) {
        fCustomizerPanel.setObject(aContext);
        return;
      }

      // De-init existing customizer panel as it cannot be reused
      fCustomizerPanel.removePropertyChangeListener(fApplyListener);
      fCustomizerPanel.setObject(null);
      fContentPane.remove((Component) fCustomizerPanel);
      fContentPane.revalidate();
      fCustomizerPanel = null;
    }

    // Try to create a new customizer
    TLcyCompositeCustomizerPanelFactory cc = new TLcyCompositeCustomizerPanelFactory(fLucyEnv);
    if (aContext != null && cc.canCreateCustomizerPanel(aContext)) {
      fCustomizerPanel = cc.createCustomizerPanel(aContext);
      fCustomizerPanel.setObject(aContext);
      fCustomizerPanel.addPropertyChangeListener(fApplyListener);
      fContentPane.add((Component) fCustomizerPanel);
      fContentPane.revalidate();
    }
  }
}
