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
package samples.wms.client.ecdis.gxy;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.model.TLcdSLDRule;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.map.TLcdMapJPanel;
import com.luciad.wms.client.gxy.TLcdWMSProxyGXYLayerFactory;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSStyledNamedLayerWrapper;
import com.luciad.wms.sld.model.TLcdSLDNamedLayer;
import com.luciad.wms.sld.model.TLcdSLDStyledLayerDescriptor;
import com.luciad.wms.sld.model.TLcdSLDUserStyle;

import samples.common.LuciadFrame;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.wms.client.ProxyModelFactory;
import samples.wms.client.UrlPanel;
import samples.wms.client.ecdis.gxy.s52.S52DataTypes;
import samples.wms.client.ecdis.gxy.s52.S52DisplaySettingsCustomizer;
import samples.wms.client.ecdis.gxy.s52.S52SLDDataTypes;
import samples.wms.client.gxy.GXYWMSGetFeatureInfoMouseListener;

/**
 * This sample demonstrates how to use an OGC Web Map Service (WMS) from the client side in combination with a
 * ECDIS-enabled server.
 * It connects to the WMS configured the top toolbar and automatically loads all available named layers.
 * <p/>
 */
public class MainPanel extends GXYSample {

  private static final String DEFAULT_SERVER_URL = "http://localhost:8081/LuciadFusion/wms";
  private final String fServerURL;

  private UrlPanel fUrlPanel;
  private ILcdModel fWMSModel;
  private ILcdGXYLayer fWMSLayer;
  private final S52DataTypes.S52DisplaySettings fS52DisplaySettings = new S52DataTypes.S52DisplaySettings();

  public MainPanel() {
    this(null);
  }

  public MainPanel(String aServerURL) {
    super();
    fServerURL = aServerURL;
    fS52DisplaySettings.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (fWMSModel != null) {
          ALcdWMSProxy wmsProxy = (ALcdWMSProxy) fWMSModel.elements().nextElement();
          wmsProxy.clearCache();
          fWMSModel.elementChanged(wmsProxy, ILcdModel.FIRE_NOW);
        }
      }
    });
  }

  protected void createGUI() {
    super.createGUI();

    // Add GetFeatureInfo support
    getView().addMouseListener(createGetFeatureInfoMouseListener(getView()));
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

  @Override
  protected JPanel createSettingsPanel() {
    return new S52DisplaySettingsCustomizer(fS52DisplaySettings);
  }

  @Override
  protected Component[] createToolBars() {
    List<Component> toolBars = new ArrayList<Component>();
    toolBars.addAll(Arrays.asList(super.createToolBars()));
    toolBars.add(createURLPanel());
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  private JPanel createURLPanel() {
    fUrlPanel = new UrlPanel(fServerURL);
    fUrlPanel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadWMSData(fUrlPanel.getURL());
      }
    });

    return TitledPanel.createTitledPanel("WMS service", fUrlPanel);
  }

  /**
   * Loads the sample data.
   */
  protected void addData() throws IOException {
    super.addData();
    loadWMSData(fServerURL);
  }

  private void loadWMSData(final String aURL) {
    if (fWMSLayer != null) {
      GXYLayerUtil.removeGXYLayer(getView(), fWMSLayer, true);
    }
    try {
      fWMSModel = ProxyModelFactory.createWMSModel(aURL, this, new TLcdDataModel[]{S52SLDDataTypes.getDataModel(), S52DataTypes.getDataModel()}, false, false);

      // The WMS model contains a single ALcdWMSProxy instance that acts as proxy towards the WMS server
      // and that defines which data has to be loaded.
      ALcdWMSProxy wmsProxy = (ALcdWMSProxy) fWMSModel.elements().nextElement();

      // Check whether the WMS server defines any S52-SLD WMS layers.
      List<ALcdWMSNamedLayer> s52SLDLayers = new ArrayList<ALcdWMSNamedLayer>();
      determineS52SLDLayersSFCT(wmsProxy, s52SLDLayers);

      if (!s52SLDLayers.isEmpty()) {
        //The WMS server defines S52-SLD layers:
        //1. We register them on the proxy object, to indicate that we want to get maps for them.
        wmsProxy.clearStyledNamedLayers();
        for (ALcdWMSNamedLayer namedLayer : s52SLDLayers) {
          wmsProxy.addStyledNamedLayer(new TLcdWMSStyledNamedLayerWrapper(namedLayer));
        }

        //2. We define a corresponding SLD to define the desired styling.
        wmsProxy.setStyledLayerDescriptor(createSLDStyledLayerDescriptor(s52SLDLayers, fS52DisplaySettings));
        ILcdGXYLayer layer = createWMSLayer(fWMSModel);

        // Enable asynchronous painting for the WMS layer.
        // This is strongly advisable for potentially slow layers such as the WMS.
        // Without asynchronous painting, the WMS layer is painted on the event dispatching thread,
        // which causes the graphical user interface to freeze during repaint. With a slow WMS,
        // this can considerably diminish the experience of an end-user. With asynchronous painting,
        // the WMS layer is painted on a background thread, causing the GUI to remain responsive during each repaint.
        // More information on asynchronous painting in LuciadLightspeed can be found in the LuciadLightspeed Developer's Guide.
        TLcdGXYAsynchronousPaintQueue queue = new TLcdGXYAsynchronousPaintQueue(getView(), TLcdGXYAsynchronousPaintQueue.BODIES_AND_SKIP);
        queue.setPriority(Thread.MIN_PRIORITY);
        fWMSLayer = new TLcdGXYAsynchronousLayerWrapper(layer, queue);
        GXYLayerUtil.addGXYLayer(getView(), fWMSLayer);

        // We fit on the layer.
        GXYLayerUtil.fitGXYLayer(getView(), fWMSLayer);
      }
    } catch (Exception e) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(null, new String[]{
              "Could not find a LuciadFusion WMS server to connect with. Please provide a valid URL to a running server.",
              });
        }
      });
    }
  }

  protected ILcdGXYLayer createWMSLayer(ILcdModel aModel) {
    return new TLcdWMSProxyGXYLayerFactory().createGXYLayer(aModel);
  }

  private static TLcdSLDStyledLayerDescriptor createSLDStyledLayerDescriptor(List<ALcdWMSNamedLayer> aS52SLDLayers,
                                                                             S52DataTypes.S52DisplaySettings aDisplaySettings) {
    S52SLDDataTypes.S52Symbolizer symbolizer = new S52SLDDataTypes.S52Symbolizer();
    symbolizer.setDisplaySettings(aDisplaySettings);

    TLcdSLDRule rule = new TLcdSLDRule();
    rule.addSymbolizer(symbolizer);

    TLcdSLDFeatureTypeStyle featureTypeStyle = new TLcdSLDFeatureTypeStyle();
    featureTypeStyle.addRule(rule);

    TLcdSLDUserStyle layerStyle = new TLcdSLDUserStyle();
    layerStyle.setDefault(true);
    layerStyle.addFeatureTypeStyle(featureTypeStyle);

    TLcdSLDStyledLayerDescriptor styledLayerDescriptor = new TLcdSLDStyledLayerDescriptor();

    for (ALcdWMSNamedLayer aS52SLDLayer : aS52SLDLayers) {
      TLcdSLDNamedLayer layer = new TLcdSLDNamedLayer();
      layer.setName(aS52SLDLayer.getNamedLayerName());
      layer.addLayerStyle(layerStyle);
      styledLayerDescriptor.addLayer(layer);
    }

    return styledLayerDescriptor;
  }

  private void determineS52SLDLayersSFCT(ALcdWMSProxy aWMSProxy, List<ALcdWMSNamedLayer> aS52SLDLayersSFCT) {
    for (int i = 0; i < aWMSProxy.getWMSRootNamedLayerCount(); i++) {
      determineS52SLDLayersSFCT(aWMSProxy.getWMSRootNamedLayer(i), aS52SLDLayersSFCT);
    }
  }

  private void determineS52SLDLayersSFCT(ALcdWMSNamedLayer aWMSLayer, List<ALcdWMSNamedLayer> aS52SLDLayersSFCT) {
    if (aWMSLayer.getNamedLayerName() != null && isS52SLDLayer(aWMSLayer)) {
      aS52SLDLayersSFCT.add(aWMSLayer);
    }
    for (int i = 0; i < aWMSLayer.getChildWMSNamedLayerCount(); i++) {
      determineS52SLDLayersSFCT(aWMSLayer.getChildWMSNamedLayer(i), aS52SLDLayersSFCT);
    }
  }

  private static boolean isS52SLDLayer(ALcdWMSNamedLayer aLayer) {
    for (int i = 0; i < aLayer.getKeywordCount(); i++) {
      if (aLayer.getKeyword(i).equals("S52-SLD")) {
        return true;
      }
    }
    return false;
  }

  // Main method
  public static void main(final String[] aArgs) {
    final String server = aArgs.length > 0 ? aArgs[0] : DEFAULT_SERVER_URL;
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(server), "WMS client with integrated ECDIS support");
      }
    });
  }
}
