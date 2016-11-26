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
package samples.lightspeed.imaging.multispectral;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.luciad.gui.ILcdAction;
import com.luciad.imaging.ALcdImage;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.services.asynchronous.ILspTaskExecutor;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.SampleData;
import samples.gxy.decoder.raster.multispectral.ImageUtil;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * <p>
 * This sample illustrates how multispectral raster imagery can be visualized and analyzed
 * in an ILspView.
 * </p>
 * <p>
 * The sample loads two 7-band multispectral GeoTIFF files of the Las Vegas area recorded
 * in 2000 and 2003 respectively. When you select one of these layers,
 * a panel below the layer controls allows you to
 * configure how this image is displayed. You can choose which bands are
 * used and in which order. With the curves panel you can perform histogram equalization
 * or define a curve by yourself. Furthermore, you can adjust the brightness, contrast, opacity
 * and sharpness. You can load additional imagery files.
 * </p>
 * <p>
 * The toolbar allows you to enable swipe and flicker controllers to visually compare
 * the layers.
 * </p>
 * <p>
 * The sample makes use of TLspImageProcessingStyle and the ALcdImageOperator
 * API to perform the band selection and dynamic range adjustments.
 * </p>
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private Map<ILcdLayer, OperatorModel> fOperatorModels = new HashMap<ILcdLayer, OperatorModel>();
  private ImageCustomizerPanelProvider fImageLayerCustomizer;
  private JPanel fRightPanelComponent;
  private ILcdLayer fCurrentLayer;
  private LayerVisibilityListener fVisibilityListener;

  public MainPanel(String[] aArgs) {
    super(aArgs);
  }

  @Override
  protected void createBandSelectListener() {
    // not needed
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fImageLayerCustomizer = new ImageCustomizerPanelProvider();

    // Add the image customizer below the layer controls
    fRightPanelComponent = new JPanel();
    fRightPanelComponent.setLayout(new GridLayout(1, 1));
    fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(null));
    addComponentToRightPanel(fRightPanelComponent, BorderLayout.SOUTH);

    fVisibilityListener = new LayerVisibilityListener();

    //Add listener to check when a layer is removed to disable the customizer panel
    getView().addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(TLcdLayeredEvent e) {
        if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
          fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(null));
          repaintRightPanel();
          fOperatorModels.remove(e.getLayer());
        }
      }
    });

    // Add a listener to the layer control which sets the currently selected
    // layer on the image customizer
    getSelectedLayers().addCollectionListener(new ILcdCollectionListener<ILcdLayer>() {
      @Override
      public void collectionChanged(TLcdCollectionEvent<ILcdLayer> aCollectionEvent) {
        fRightPanelComponent.removeAll();
        if (fCurrentLayer != null) {
          fCurrentLayer.removePropertyChangeListener(fVisibilityListener);
        }
        // If a single layer was selected, set it on the customizer
        if (aCollectionEvent.getSource().size() == 1) {
          ILspLayer layer = (ILspLayer) aCollectionEvent.getSource().iterator().next();

          OperatorModel operatorModel = fOperatorModels.get(layer);
          if (operatorModel == null && layer instanceof TLspRasterLayer) {
            TLspRasterLayer rasterLayer = (TLspRasterLayer) layer;
            if (rasterLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY) instanceof MultispectralOperatorStyler) {
              ALcdImage image = ImageUtil.getImage(layer);
              if (image != null) {
                ILspTaskExecutor taskExecutor = getView().getServices().getTaskExecutor();
                operatorModel = new OperatorModel(layer.getModel(), image, taskExecutor);
                operatorModel.addChangeListener(new OperatorModelChangeListener(layer));
                fOperatorModels.put(layer, operatorModel);
                operatorModel.fireChangeEvent(new PropertyChangeEvent(operatorModel, "", "", ""));
              }
            }
          }

          fCurrentLayer = layer;
          layer.addPropertyChangeListener(fVisibilityListener);

          if (layer.isVisible()) {
            fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(operatorModel));
          } else {
            fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(null));
          }

          if (operatorModel != null) {
            ILspEditableStyledLayer styledLayer = (ILspEditableStyledLayer) layer;
            ILspStyler styler = styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            if (styler instanceof MultispectralOperatorStyler) {
              ((MultispectralOperatorStyler) styler).setOperatorModel(operatorModel);
            }
          }

        } else {
          fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(null));
        }
        repaintRightPanel();
      }
    });
  }

  @Override
  protected ILcdAction createSaveAction() {
    return new SaveImageAction(getView(), getSelectedLayers());
  }

  private void repaintRightPanel() {
    fRightPanelComponent.revalidate();
    fRightPanelComponent.repaint();
  }

  protected void addData() throws IOException {
    super.addData();
    MultispectralLayerFactory layerFactory = new MultispectralLayerFactory();
    LspDataUtil.instance().model(SampleData.LAS_VEGAS_2000).layer(layerFactory).label("Las Vegas 2000").addToView(getView());
    LspDataUtil.instance().model(SampleData.LAS_VEGAS_2003).layer(layerFactory).label("Las Vegas 2003").addToView(getView()).fit();
  }

  @Override
  protected void tearDown() {
    super.tearDown();
    fImageLayerCustomizer.dispose();
  }

  @Override
  protected ILspLayerFactory createLayerFactory() {
    return new TLspCompositeLayerFactory(new MultispectralLayerFactory(), super.createLayerFactory());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, aArgs, "Multispectral imaging");
  }

  private class LayerVisibilityListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equalsIgnoreCase("visible")) {
        OperatorModel operatorModel = null;
        if ((Boolean) evt.getNewValue()) {
          operatorModel = fOperatorModels.get(evt.getSource());
        }
        fRightPanelComponent.removeAll();
        fRightPanelComponent.add(fImageLayerCustomizer.getPanelForOperatorModel(operatorModel));
        repaintRightPanel();
      }
    }
  }

}
