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
package samples.decoder.gdf.network;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.format.gdf.ILcdGDFAttribute;
import com.luciad.format.gdf.ILcdGDFFeature;
import com.luciad.format.gdf.TLcdGDFModelDecoder;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.formatsupport.OpenTransferHandler;
import samples.decoder.gdf.network.function.GDFGeodeticEdgeValueFunction;
import samples.decoder.gdf.view.gxy.GDFLayerFactory;
import samples.decoder.gdf.view.gxy.GDFRenderingSettings;
import samples.gxy.common.ProgressUtil;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.network.basic.function.GeodeticDistanceFunction;
import samples.network.common.ANetworkSample;
import samples.network.common.gui.INameProvider;

/**
 * This sample shows how to use GDF data in combination with the Network API.
 * The actual network calculations are done by the {@code GDFGraphPreprocessor}.
 */
public class MainPanel extends ANetworkSample {

  private TLcdGDFModelDecoder fGDFModelDecoder;

  private GDFRenderingSettings fGDFRenderingSettings = new GDFRenderingSettings();
  private GDFLayerFactory fGDFLayerFactory = new GDFLayerFactory(fGDFRenderingSettings, getGraphManager());

  private GDFGraphPreprocessor fGraphPreprocessor;

  private ILcdGXYLayer fDataLayer;

  public MainPanel() {
    super();

    // Functions
    GDFGeodeticEdgeValueFunction geodeticEVF = new GDFGeodeticEdgeValueFunction();
    GeodeticDistanceFunction geodeticDF = new GeodeticDistanceFunction();
    getGraphManager().setEdgeValueFunction(geodeticEVF);
    getGraphManager().setHeuristicEstimateFunction(geodeticDF);

    fGDFModelDecoder = new TLcdGDFModelDecoder();
    fGraphPreprocessor = new GDFGraphPreprocessor(getGraphManager());
  }

  protected void createGUI() {
    super.createGUI();

    // Add the Open action open to the tool bar.
    OpenSupport openSupport = new MyOpenSupport(this, Collections.singletonList(fGDFModelDecoder));
    OpenAction openAction = new OpenAction(openSupport);
    // Add drag and drop support.
    fMapJPanel.setTransferHandler(new OpenTransferHandler(openSupport));

    fToolbar.addSpace();
    fToolbar.addAction(openAction);
    fToolbar.setEdgeNameProvider(new StreetNameProvider());

    // Create the layer control and rendering settings panel.

    LayerControlPanel layerControl = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    JPanel eastPanel = new JPanel(new BorderLayout());
    eastPanel.add(layerControl, BorderLayout.CENTER);
    add(BorderLayout.EAST, eastPanel);
  }

  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          JOptionPane.showMessageDialog(MainPanel.this, new String[]{
              "The LuciadLightspeed distribution does not contain GDF data.",
              "Please use your own data to load into this sample."
          });
        }
      });
  }

  private void loadData(ILcdModel aModel) {
    // Remove existing data layer.
    if (fDataLayer != null) {
      fMapJPanel.removeLayer(fDataLayer);
      fMapJPanel.setNumberOfCachedBackgroundLayers(fMapJPanel.getNumberOfCachedBackgroundLayers() - 1);
    }

    // Create graph.
    fGraphPreprocessor.preprocessModel(aModel);

    // Create new data layer.
    fDataLayer = fGDFLayerFactory.createGXYLayer(aModel);
    GXYLayerUtil.fitGXYLayer(fMapJPanel, fDataLayer);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fDataLayer, true, false);
  }

  /**
   * Retrieves the GDF street name attribute from GDF street features.
   */
  private static class StreetNameProvider implements INameProvider {

    public String getName(Object aObject) {
      return getAddress((ILcdGDFFeature) aObject);
    }

    private String getAddress(ILcdGDFFeature aFeature) {
      for (int i = 0; i < aFeature.getAttributeCount(); i++) {
        ILcdGDFAttribute attribute = aFeature.getAttribute(i);
        for (int j = 0; j < attribute.getAttributeCount(); j++) {
          if (attribute.getAttributeType(j).getAttributeTypeCode().equals("ON")) {
            return attribute.getAttributeValue(j).toString();
          }
        }
      }
      return "";
    }
  }

  private class MyOpenSupport extends OpenSupport {

    private JDialog fProgressDialog;

    public MyOpenSupport(Component aParent, List<? extends ILcdModelDecoder> aModelDecoders) {
      super(aParent, aModelDecoders);
    }

    @Override
    protected void startLoading(String aSource, ILcdModelDecoder aModelDecoder) {
      if (fProgressDialog == null) {
        fProgressDialog = ProgressUtil.createProgressDialog(MainPanel.this, "Loading GDF data...");
      }
      // Add a progress bar to the model decoder.
      if (fProgressDialog instanceof ILcdStatusListener) {
        if (fGDFModelDecoder == aModelDecoder) {
          TLcdStatusInputStreamFactory isf = new TLcdStatusInputStreamFactory();
          isf.addStatusEventListener((ILcdStatusListener) fProgressDialog);
          fGraphPreprocessor.addStatusListener((ILcdStatusListener) fProgressDialog);
          fGDFModelDecoder.setInputStreamFactory(isf);
        }
      }
    }

    @Override
    protected void loadingTerminated(String aSource, ILcdModelDecoder aModelDecoder) {
      // Remove progress bar from model decoder and dispose dialog.
      if (fProgressDialog instanceof ILcdStatusListener) {
        if (fGDFModelDecoder == aModelDecoder) {
          TLcdStatusInputStreamFactory isf = (TLcdStatusInputStreamFactory) fGDFModelDecoder.getInputStreamFactory();
          isf.removeStatusEventListener((ILcdStatusListener) fProgressDialog);
          fGraphPreprocessor.removeStatusListener((ILcdStatusListener) fProgressDialog);
          fProgressDialog.dispose();
          fProgressDialog = null;
        }
      }
    }

    @Override
    protected void modelDecoded(String aSource, ILcdModel aModel) {
      loadData(aModel);
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Network analysis of GDF data");
      }
    });
  }
}
