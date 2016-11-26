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
package samples.gxy.concurrent.painting.lightspeed;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.lightspeed.ILcdGXYLspAsynchronousLayerRunnable;
import com.luciad.view.gxy.asynchronous.lightspeed.TLcdGXYLspAsynchronousLayerTreeNodeWrapper;
import com.luciad.view.gxy.asynchronous.lightspeed.TLcdGXYLspAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.lightspeed.manager.TLcdGXYLspAsynchronousPaintQueueManager;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;
import com.luciad.view.gxy.swing.TLcdGXYBusyLayerTreeNodeCellRenderer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;
import samples.gxy.concurrent.painting.DataFactory;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspStyleUtil;
import samples.lightspeed.decoder.UnstyledLayerFactory;

/**
 * <p>This sample illustrates how to integrate Lightspeed layers into a GXY view,
 * making use of the following concepts:</p>
 *
 * <ul>
 * <li>TLcdGXYLspAsynchronousLayerWrapper to add a Lightspeed layer to a GXY layer;</li>
 * <li>ILcdGXYAsynchronousLayerWrapper#invokeAndWaitOnGXYLayer to ensure safe manipulation of the
 * wrapped layers;</li>
 * <li>TLcdGXYLspAsynchronousPaintQueueManager to automatically manage the paint queues of the 
 * asynchronous layers.</li>
 * </ul>
 */
public class MainPanel extends GXYSample {

  @Override
  protected void createGUI() {
    super.createGUI();
    //activate tracing for the renderer
    Logger logger = Logger.getLogger(TLcdGXYBusyLayerTreeNodeCellRenderer.class.getName());
    logger.setLevel(Level.FINEST);

    // labeled TLcdGXYLspAsynchronousLayerWrapper instances don't support a view label placer
    // because they don't implement ILcdGXYEditableLabelsLayer.
    getView().setGXYViewLabelPlacer(null);
  }

  protected void addData() throws IOException {

    // Create some GXY and Lightspeed layers with raster data.
    ILcdGXYLayer geoTIFFLayer = GXYDataUtil.instance().model(SampleData.BLUE_MARBLE).layer().label("Blue marble [Lightspeed]").getLayer();

    ILcdGXYLayer dmedGXYLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().getLayer();
    ILspLayer dmedLspLayer = LspDataUtil.instance().model(dmedGXYLayer.getModel()).layer().getLayer();

    // To link a layer with a paint queue you have to wrap it in an asynchronous layer wrapper.
    // From now on, the layer wrapper protects the inner layer: it handles synchronous access
    // and can thus be safely added to the view.
    // If we don't pass a paint queue while constructing an asynchronous layer wrapper,
    // we indicate to the paint queue manager that he is responsible for assigning a queue.
    TLcdGXYLspAsynchronousLayerWrapper asynchronousDmedLayer =
        new TLcdGXYLspAsynchronousLayerWrapper(dmedGXYLayer, dmedLspLayer);
    dmedGXYLayer.setLabel(dmedGXYLayer.getLabel() + " [Lightspeed]");

    /* We could add the layer wrapper to the the view now, but in this sample we'll add it to
     * a layer tree node.
    GXYLayerUtil.addGXYLayer( getView(), asynchronousDmedLayer );
     */

    // Also here we let the paint queue manager assign a queue.
    ILspLayer geoTIFFLspLayer = LspDataUtil.instance().model(geoTIFFLayer.getModel()).layer().getLayer();
    TLcdGXYLspAsynchronousLayerWrapper asynchronousGeoTIFFLayer =
        new TLcdGXYLspAsynchronousLayerWrapper(geoTIFFLayer, geoTIFFLspLayer);

    // Create a vector layer.
    GXYUnstyledLayerFactory factory = new GXYUnstyledLayerFactory();
    factory.setFillStyle(null);
    factory.setCreateNodes(true);

    TLcdGXYLayer shpLayer = (TLcdGXYLayer)
        GXYDataUtil.instance().model(SampleData.COUNTRIES).layer(factory).label("Countries [Lightspeed]").getLayer();
    // disable labeling
    shpLayer.setGXYLabelPainterProvider(null);

    UnstyledLayerFactory lspFactory = new UnstyledLayerFactory();
    factory.setFillStyle(null);
    ILspLayer shpLspLayer = LspDataUtil.instance().model(shpLayer.getModel()).layer(lspFactory).getLayer();

    // Asynchronous painting also works for layer tree nodes...
    TLcdGXYLspAsynchronousLayerTreeNodeWrapper asynchronousShpLayer =
        new TLcdGXYLspAsynchronousLayerTreeNodeWrapper(shpLayer, shpLspLayer);
    asynchronousShpLayer.setPaintOnTopOfChildrenHint(true);
    asynchronousShpLayer.setLabeled(true);
    asynchronousShpLayer.addLayer(asynchronousGeoTIFFLayer);
    asynchronousShpLayer.addLayer(asynchronousDmedLayer);

    // For illustration purposes, we add some synchronous and some slow layers:
    TLcdGXYLayerTreeNode polygonLayers = new TLcdGXYLayerTreeNode("Polygons");
    polygonLayers.setPaintOnTopOfChildrenHint(true);
    polygonLayers.addLayer(createLightspeedLayerWithMinimalPaintTime(100, 1.3));
    polygonLayers.addLayer(new TLcdGXYAsynchronousEditableLabelsLayerWrapper(
        DataFactory.createLayerWithMinimalPaintTime(250, 2.6)));
    polygonLayers.addLayer(DataFactory.createSynchronousLayer());
    polygonLayers.addLayer(new TLcdGXYAsynchronousEditableLabelsLayerWrapper(
        DataFactory.createLayerWithMinimalPaintTime(750, 3.9)));

    GXYLayerUtil.addGXYLayer(getView(), asynchronousShpLayer);
    GXYLayerUtil.addGXYLayer(getView(), polygonLayers);
    GXYLayerUtil.fitGXYLayer(getView(), asynchronousDmedLayer);
  }

  @Override
  protected void setupPaintQueueManager(ILcdGXYView aMap) {
    // Layers can share a single paint queue, but they have to be contiguous.
    // To facilitate linking wrappers with paint queues, a paint queue manager can be used.
    // The following code instantiates a paint queue manager that can work with Lightspeed-capable
    // layer wrappers.
    TLcdGXYLspAsynchronousPaintQueueManager manager = new TLcdGXYLspAsynchronousPaintQueueManager();
    manager.setGXYView(aMap);
  }

  @Override
  protected JPanel createSettingsPanel() {
    JCheckBox transparencyBox = new JCheckBox("Translucent", false);
    transparencyBox.setToolTipText("When enabled, all raster layers are painted semi-transparently.");
    transparencyBox.addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent aEvent) {
        final float transparency = aEvent.getStateChange() == ItemEvent.SELECTED ? 0.50f : 1.0f;
        Enumeration enumeration = getView().layers();
        while (enumeration.hasMoreElements()) {
          Object o = enumeration.nextElement();
          if (o instanceof TLcdGXYLspAsynchronousLayerWrapper) {
            try {
              final TLcdGXYLspAsynchronousLayerWrapper layerWrapper = (TLcdGXYLspAsynchronousLayerWrapper) o;
              // Ensures safe access to the wrapped layers.
              layerWrapper.invokeAndWaitOnGXYAndLspLayer(new ILcdGXYLspAsynchronousLayerRunnable() {
                public void run(ILspLayer aSafeLspLayer, ILcdGXYLayer aSafeGXYLayer) {
                  TLcdGXYLayer layer = (TLcdGXYLayer) aSafeGXYLayer;
                  if (aSafeLspLayer instanceof TLspRasterLayer) {
                    setRasterTransparency(((TLspRasterLayer) aSafeLspLayer).getStyler(TLspPaintRepresentationState.REGULAR_BODY), transparency);
                  }
                  if (layer.getGXYPainterProvider() instanceof TLcdGXYImagePainter) {
                    TLcdGXYImagePainter provider = (TLcdGXYImagePainter) layer.getGXYPainterProvider();
                    provider.setOpacity(transparency);
                    // Makes sure that the wrapper is repainted.
                    getView().invalidateGXYLayer(layerWrapper, true, this, "Painter property changed.");
                  }
                }
              });
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }
      }
    });

    JPanel togglePanel = new JPanel(new GridLayout(1, 1));
    togglePanel.add(transparencyBox);
    return TitledPanel.createTitledPanel("Rendering", togglePanel);
  }

  private void setRasterTransparency(ILspStyler aStyler, float aTransparency) {
    TLspRasterStyle previousStyle = LspStyleUtil.getCustomizableStyle(aStyler, TLspRasterStyle.class);
    TLspRasterStyle newStyle = previousStyle.asBuilder().modulationColor((new Color(1, 1, 1, aTransparency))).build();
    LspStyleUtil.setCustomizableStyle(aStyler, newStyle);
  }

  private ILcdGXYLayer createLightspeedLayerWithMinimalPaintTime(final long aMinimalPaintTime, double aDelta) {
    ILcdGXYLayer gxyLayer = DataFactory.createLayerWithMinimalPaintTime(aMinimalPaintTime, aDelta);
    gxyLayer.setLabel("Polygon [Lightspeed," + aMinimalPaintTime + "]");
    TLspLayer lspLayer = new TLspLayer(gxyLayer.getModel()) {
      @Override
      public TLspPaintProgress paint(
          ILcdGLDrawable aGLDrawable,
          TLspPaintPhase aMode,
          TLspPaintRepresentationState aPaintRepresentationState,
          ILspView aView
      ) {
        try {
          if (isVisible()) {
            Thread.sleep(aMinimalPaintTime);
          }
        } catch (InterruptedException e) {
          // simply abort painting
        }
        return super.paint(aGLDrawable, aMode, aPaintRepresentationState, aView);
      }
    };
    lspLayer.setPainter(TLspPaintRepresentation.BODY, new TLspShapePainter());
    return new TLcdGXYLspAsynchronousLayerWrapper(gxyLayer, lspLayer);
  }

  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        final MainPanel sample = new MainPanel();
        new LuciadFrame(sample, "Lightspeed Painting", 900, 600) {
          @Override
          public void dispose() {
            // We override the dispose to make sure the layers
            // are removed and resources held by the Lightspeed layers
            // are disposed.
            sample.getView().removeAllLayers();
            super.dispose();
          }
        };
      }
    });
  }

}
