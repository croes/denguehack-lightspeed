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
package samples.wms.client.gxy;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.map.TLcdMapJPanel;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;
import com.luciad.wms.client.model.TLcdWMSStyledNamedLayerWrapper;

import samples.common.LuciadFrame;
import samples.common.action.LayerCustomizerSupport;
import samples.common.dimensionalfilter.LayerDimensionalFilterCustomizer;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.projections.GXYCenterMapController;
import samples.gxy.projections.ProjectionComboBox;
import samples.wms.client.ProxyModelFactory;
import samples.wms.client.ProxyWrapper;
import samples.wms.client.UrlPanel;
import samples.wms.client.common.WMSSettingsPanel;

/**
 * This sample demonstrates how to use an OGC Web Map Service (WMS) from the client side. It
 * connects to the WMS configured in samples/wms/client/settings.cfg and automatically loads all
 * available named layers. By default, the configured WMS service is the LuciadLightspeed WMS sample
 * service running on localhost:8080. <p> A few additional capabilities are also demonstrated: <ul>
 * <li>A selection controller is available in the toolbar that enables you to click on the map. For
 * each click, a GetFeatureInfo request is sent to the WMS if this optional request is supported,
 * and the response is painted as a label next to the clicked point. The samples expects a
 * predefined custom text-based format as response (see WMSGetFeatureInfoController), since there is
 * no standardized GetFeatureInfo exchange format. This capability is therefore optimized to be used
 * with the LuciadLightspeed WMS sample service, which uses this text-based format as GetFeatureInfo
 * exchange format.</li> <li>An auto-update checkbox is available in the toolbar that lets you
 * monitor the WMS for any updates in its layer configuration. By default, all available named
 * layers are loaded initially. If this checkbox is selected, the layer configuration will be
 * monitored at each navigation action (pan, zoom, ...), and any changes (i.e., added/removed
 * layers) will be automatically applied.</li> </ul> </p>
 */
public class MainPanel extends GXYSample {

  private static final boolean DEFAULT_AUTO_UPDATE_ENABLED = false;

  private final String fServerUrl;
  private UrlPanel fUrlPanel;
  private final WMSLayerFactory fWMSLayerFactory = new WMSLayerFactory();

  // UI to choose the WMS layers
  private WMSSettingsPanel fWMSSettingsPanel;
  private LayerCustomizerSupport<ILcdGXYView, ILcdGXYLayer> fWMSSettingPanelSupport;

  // On-screen widget to support WMS TIME and ELEVATION dimension filtering.
  private LayerDimensionalFilterCustomizer fLayerDimensionalFilterCustomizer;

  public MainPanel(String aServerUrl) {
    super();
    fServerUrl = aServerUrl;
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-120, 20, 50, 40);
  }

  @Override
  public void createGUI() {
    super.createGUI();

    // Create a checkbox to enable/disable monitoring a WMS for layer configuration updates.
    JCheckBox autoUpdateWMS = new JCheckBox("Auto-update WMS", DEFAULT_AUTO_UPDATE_ENABLED);
    autoUpdateWMS.addActionListener(new AutoUpdateWMSListener());

    // Creates and configures the toolbar.
    ToolBar toolBar = getToolBars()[0];
    toolBar.addComponent(autoUpdateWMS);
    toolBar.addComponent(new ProjectionComboBox(getView(), 0));

    // Add GetFeatureInfo support
    getView().addMouseListener(createGetFeatureInfoMouseListener(getView()));

    // Links the selected layers to a filter GUI component
    Iterable<DimensionalFilterProvider> query = ServiceRegistry.getInstance().query(DimensionalFilterProvider.class);
    fLayerDimensionalFilterCustomizer = new LayerDimensionalFilterCustomizer(getView(), getSelectedLayers(), getOverlayPanel(), query);

    fWMSSettingPanelSupport = new LayerCustomizerSupport<ILcdGXYView, ILcdGXYLayer>(getView(), getSelectedLayers()) {
      @Override
      protected void layerAdded(ILcdGXYView aView, ILcdGXYLayer aLayer) {
      }

      @Override
      protected void layerRemoved(ILcdGXYView aView, ILcdGXYLayer aLayer) {
        if (fWMSLayerFactory.canCreateGXYLayer(aLayer.getModel())) {
          fWMSSettingsPanel.setWMSModel(null);
        }
      }

      @Override
      protected void layerSelected(ILcdGXYView aView, ILcdGXYLayer aLayer) {
        if (fWMSLayerFactory.canCreateGXYLayer(aLayer.getModel())) {
          fWMSSettingsPanel.setWMSModel(aLayer.getModel());
        }
      }
    };
  }

  @Override
  protected Component[] createToolBars() {
    java.util.List<Component> toolBars = new ArrayList<Component>();
    toolBars.addAll(Arrays.asList(super.createToolBars()));
    toolBars.add(createURLPanel());
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  @Override
  protected JPanel createSettingsPanel() {
    fWMSSettingsPanel = new WMSSettingsPanel();
    fWMSSettingsPanel.addPropertyChangeListener("tiled", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        boolean tiled = (Boolean) evt.getNewValue();
        fWMSLayerFactory.setTiled(tiled);
        for (int i = 0; i < getView().layerCount(); i++) {
          ILcdLayer layer = getView().getLayer(i);
          ILcdModel model = layer.getModel();
          if (fWMSLayerFactory.canCreateGXYLayer(model)) {
            getView().removeLayer(layer);
            ILcdGXYLayer newLayer = fWMSLayerFactory.createGXYLayer(model);
            getView().addGXYLayer(newLayer);
            getView().moveLayerAt(i, newLayer);
          }
        }
      }
    });
    return fWMSSettingsPanel;
  }

  private JPanel createURLPanel() {
    fUrlPanel = new UrlPanel(fServerUrl);
    fUrlPanel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ILcdModel wmsModel = loadWMSData(MainPanel.this.fUrlPanel.getURL());
        if (wmsModel != null) {
          updateWMSLayerListUI(wmsModel);
        }
      }
    });

    return TitledPanel.createTitledPanel("WMS service", fUrlPanel);
  }

  @Override
  public void addData() throws IOException {
    super.addData();
    loadWMSData(fServerUrl);
  }

  private GXYWMSGetFeatureInfoMouseListener createGetFeatureInfoMouseListener(TLcdMapJPanel aView) {
    return new GXYWMSGetFeatureInfoMouseListener(aView) {
      @Override
      protected boolean isSelectControllerActive(ILcdGXYView aView) {
        TLcdGXYCompositeController selectController = getToolBars()[0].getGXYCompositeEditController();
        return aView.getGXYController() == selectController;
      }
    };
  }

  private void updateWMSLayerListUI(final ILcdModel aWMSModel) {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        if (aWMSModel != null) {
          fWMSSettingsPanel.setWMSModel(aWMSModel);
        }
      }
    });
  }

  private ILcdModel loadWMSData(final String aURL) {
    try {
      ILcdModel wmsModel = ProxyModelFactory.createWMSModel(aURL, this, DEFAULT_AUTO_UPDATE_ENABLED);
      if (wmsModel != null) {
        ILcdGXYLayer wmsLayer = fWMSLayerFactory.createGXYLayer(wmsModel);
        GXYLayerUtil.addGXYLayer(getView(), wmsLayer);

        // We add a model listener to be able to update the WMS layer list UI widget after WMS model changes.
        wmsModel.addModelListener(new ILcdModelListener() {
          @Override
          public void modelChanged(TLcdModelChangedEvent aEvent) {
            updateWMSLayerListUI(aEvent.getModel());
          }
        });

        return wmsModel;
      }
    } catch (Exception e) {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          JOptionPane.showMessageDialog(null, new String[]{
              "Could not find a WMS server to connect with. Please provide a valid URL to a running server.",
          });
        }
      });
    }
    return null;
  }


  private static void addRecursivelyLayers(
      ALcdWMSProxy aWMSProxy,
      ALcdWMSNamedLayer aNamedLayer) {
    // We can only load layers with a name. Layers without a name are groups of layers that cannot be requested
    // by themselves.
    if (aNamedLayer.getNamedLayerName() != null) {
      aWMSProxy.addStyledNamedLayer(new TLcdWMSStyledNamedLayerWrapper(aNamedLayer));
    }

    for (int i = 0; i < aNamedLayer.getChildWMSNamedLayerCount(); i++) {
      addRecursivelyLayers(aWMSProxy, aNamedLayer.getChildWMSNamedLayer(i));
    }
  }

  /**
   * This class listens to the checkbox 'Auto-update WMS'. When this checkbox is selected, the
   * server is contacted for each zoom/pan action to check whether the list of WMS layers has been
   * updated. If a change is detected (i.e., added or removed layers), the new WMS layer
   * configuration is automatically taken into account. When this checkbox is not selected, the WMS
   * layer configuration is only retrieved once and never updated afterwards.
   */
  private class AutoUpdateWMSListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
      boolean autoUpdate = ((JCheckBox) aActionEvent.getSource()).isSelected();

      for (int i = 0; i < getView().layerCount(); i++) {
        ILcdLayer layer = getView().getLayer(i);
        ILcdModel model = layer.getModel();
        if (fWMSLayerFactory.canCreateGXYLayer(model)) {
          enableAutoUpdateForWMSModel(model, autoUpdate);
        }
      }
    }

    private void enableAutoUpdateForWMSModel(ILcdModel aWMSModel, boolean aAutoUpdate) {
      // The proxy model can be null if there is a problem with the WMS server.
      if (aWMSModel != null) {
        TLcdLockUtil.writeLock(aWMSModel);
        try {
          ALcdWMSProxy proxy = (ALcdWMSProxy) aWMSModel.elements().nextElement();

          // Set autoUpdate property on proxy wrapper.
          if (proxy instanceof ProxyWrapper) {
            ((ProxyWrapper) proxy).setAutoUpdateEnabled(aAutoUpdate);
          }

          // Remove registered layers on proxy.
          for (int i = proxy.getStyledNamedLayerCount() - 1; i >= 0; i--) {
            proxy.removeStyledNamedLayer(i);
          }

          // Reinitialize proxy layer configuration.
          ProxyModelFactory.addLayers(proxy, proxy.getWMSCapabilities().getWMSRootNamedLayer(0));

          // Fire a model changed event.
          aWMSModel.elementChanged(proxy, ILcdFireEventMode.FIRE_LATER);
        } finally {
          TLcdLockUtil.writeUnlock(aWMSModel);
        }

        aWMSModel.fireCollectedModelChanges();
      }
    }
  }

  @Override
  protected void tearDown() {
    fLayerDimensionalFilterCustomizer.dispose();
    fWMSSettingPanelSupport.dispose();
    super.tearDown();
  }

  // Main method
  public static void main(final String[] aArgs) {
    final String serverUrl = aArgs.length == 0 ? UrlPanel.getDefaultWMSUrl() : aArgs[0];
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new LuciadFrame(new MainPanel(serverUrl), "LuciadLightspeed WMS client");
      }
    });
  }
}
