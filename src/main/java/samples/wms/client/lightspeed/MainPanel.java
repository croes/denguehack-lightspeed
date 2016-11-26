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
package samples.wms.client.lightspeed;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.action.LayerCustomizerSupport;
import samples.common.dimensionalfilter.LayerDimensionalFilterCustomizer;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.TitledPanel;
import samples.wms.client.ProxyModelFactory;
import samples.wms.client.UrlPanel;
import samples.wms.client.common.WMSSettingsPanel;

/**
 * Main panel for the WMS sample.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private UrlPanel fUrlPanel;
  private final WMSLayerFactory fWMSLayerFactory = new WMSLayerFactory();
  private final String[] fArgs;

  // UI to choose the WMS layers
  private WMSSettingsPanel fWMSSettingsPanel;
  private LayerCustomizerSupport<ILspView, ILspLayer> fWMSSettingPanelSupport;

  // On-screen widget to support WMS TIME and ELEVATION dimension filtering.
  private LayerDimensionalFilterCustomizer fLayerDimensionalFilterCustomizer;

  public MainPanel(String[] aArgs) {
    fArgs = aArgs;
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    java.util.List<Component> toolBars = new ArrayList<Component>();
    toolBars.addAll(Arrays.asList(super.createToolBars(aView)));
    toolBars.add(createURLPanel());
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  private JPanel createURLPanel() {
    fUrlPanel = new UrlPanel(UrlPanel.LUCIAD_LIGHTSPEED_WMS_URL);
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

  private JPanel createSettingsPanel() {
    fWMSSettingsPanel = new WMSSettingsPanel();
    fWMSSettingsPanel.addPropertyChangeListener("tiled", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Boolean tiled = (Boolean) evt.getNewValue();
        fWMSLayerFactory.setTiled(tiled);
        for (int i = 0; i < getView().layerCount(); i++) {
          ILspLayer layer = getView().getLayer(i);
          ILcdModel model = layer.getModel();
          if (fWMSLayerFactory.canCreateLayers(model)) {
            getView().removeLayer(layer);
            ILspLayer newLayer = fWMSLayerFactory.createLayers(model).iterator().next();
            getView().addLayer(newLayer);
            getView().moveLayerAt(i, newLayer);
          }
        }
      }
    });
    fWMSLayerFactory.setTiled(fWMSSettingsPanel.isTiled());
    return fWMSSettingsPanel;
  }

  private ILcdModel loadWMSData(final String aURL) {
    try {
      ILcdModel wmsModel = ProxyModelFactory.createWMSModel(aURL, this, false);
      if (wmsModel != null) {
        Collection<ILspLayer> layers = fWMSLayerFactory.createLayers(wmsModel);
        if (!layers.isEmpty()) {
          ILspLayer wmsLayer = layers.iterator().next();
          getView().addLayer(wmsLayer);

          // We add a model listener to be able to update the WMS layer list UI widget after WMS model changes.
          wmsModel.addModelListener(new ILcdModelListener() {
            @Override
            public void modelChanged(TLcdModelChangedEvent aEvent) {
              updateWMSLayerListUI(aEvent.getModel());
            }
          });
        }
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

  @Override
  protected void createGUI() {
    super.createGUI();
    addComponentToRightPanel(createSettingsPanel());

    Iterable<DimensionalFilterProvider> query = ServiceRegistry.getInstance().query(DimensionalFilterProvider.class);
    fLayerDimensionalFilterCustomizer = new LayerDimensionalFilterCustomizer(getView(), getSelectedLayers(), getOverlayPanel(), query);
    
    // Add GetFeatureInfo support
    getView().getHostComponent().addMouseListener(createGetFeatureInfoMouseListener(getView()));

    fWMSSettingPanelSupport = new LayerCustomizerSupport<ILspView, ILspLayer>(getView(), getSelectedLayers()) {
      @Override
      protected void layerAdded(ILspView aView, ILspLayer aLayer) {
      }

      @Override
      protected void layerRemoved(ILspView aView, ILspLayer aLayer) {
        if (fWMSLayerFactory.canCreateLayers(aLayer.getModel())) {
          fWMSSettingsPanel.setWMSModel(null);
        }
      }

      @Override
      protected void layerSelected(ILspView aView, ILspLayer aLayer) {
        if (fWMSLayerFactory.canCreateLayers(aLayer.getModel())) {
          fWMSSettingsPanel.setWMSModel(aLayer.getModel());
        }
      }
    };
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    String serverUrl = fArgs.length > 0 ? fArgs[0] : UrlPanel.getDefaultWMSUrl();
    fUrlPanel.setUrl(serverUrl);
    fUrlPanel.connect();
  }

  private LspWMSGetFeatureInfoMouseListener createGetFeatureInfoMouseListener(ILspAWTView aView) {
    return new LspWMSGetFeatureInfoMouseListener(aView) {
      @Override
      protected boolean isSelectControllerActive(ILspView aView) {
        ILspController selectController = getToolBars()[0].getDefaultController();
        return aView.getController() == selectController;
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

  @Override
  protected void tearDown() {
    fLayerDimensionalFilterCustomizer.dispose();
    fWMSSettingPanelSupport.dispose();
    super.tearDown();
  }

  public static void main(String[] args) {
    startSample(MainPanel.class, args, "Lightspeed WMS client");
  }
}
