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
package samples.gxy.concurrent.painting;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerRunnable;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerTreeNodeWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;
import com.luciad.view.gxy.swing.TLcdGXYBusyLayerTreeNodeCellRenderer;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * <p>This sample illustrates how to make a layer paint asynchronously, making use of the following
 * concepts:</p>
 *
 * <ul>
 * <li>ILcdGXYAsynchronousLayerWrapper to enable asynchronous operations;</li>
 * <li>ILcdGXYAsynchronousPaintQueue to assign a paint buffer and paint thread;</li>
 * <li>TLcdGXYAsynchronousLabelPlacer to combine view label decluttering with
 * asynchronously painted layers;</li>
 * <li>ILcdGXYAsynchronousLayerWrapper#invokeAndWaitOnGXYLayer to ensure safe manipulation of the
 * wrapped layer.</li>
 * <li>ALcdGXYAsynchronousPaintQueueManager to automatically manage the paint queues of the
 * asynchronous layers</li>
 *
 * </ul>
 */
public class MainPanel extends GXYSample {

  protected void createGUI() {
    super.createGUI();
    //activate tracing for the renderer
    Logger logger = Logger.getLogger(TLcdGXYBusyLayerTreeNodeCellRenderer.class.getName());
    logger.setLevel(Level.FINEST);
  }

  protected void addData() throws IOException {

    // Asynchronous painting is handled by a paint queue, encapsulating a thread and buffers
    // for background painting.
    TLcdGXYAsynchronousPaintQueue queue = new TLcdGXYAsynchronousPaintQueue(getView());
    queue.setPriority(Thread.MIN_PRIORITY);

    // Create some layers with raster data.
    ILcdGXYLayer geoTIFFLayer = GXYDataUtil.instance().model(SampleData.BLUE_MARBLE).layer().getLayer();
    ILcdGXYLayer dmedLayer =  GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().getLayer();

    if (geoTIFFLayer != null) {
      // To link a layer with a paint queue you have to wrap it in an asynchronous layer wrapper.
      // From now on, the layer wrapper protects the inner layer: it handles synchronous access
      // and can thus be safely added to the view.
      TLcdGXYAsynchronousLayerWrapper asynchronousDmedLayer =
          new TLcdGXYAsynchronousLayerWrapper(dmedLayer, queue);
      dmedLayer.setLabel(dmedLayer.getLabel() + " [unmanaged]");

      // If we don't pass a paint queue while constructing an asynchronous layer wrapper,
      // we indicate to the paint queue manager that he is responsible for assigning a queue.
      TLcdGXYAsynchronousLayerWrapper asynchronousGeoTIFFLayer =
          new TLcdGXYAsynchronousLayerWrapper(geoTIFFLayer, null);

      // Create a vector layer.
      GXYUnstyledLayerFactory outlinedLayerFactory = new GXYUnstyledLayerFactory();
      outlinedLayerFactory.setFillStyle(null);
      outlinedLayerFactory.setCreateNodes(true);
      ILcdGXYEditableLabelsLayer shpLayer = (ILcdGXYEditableLabelsLayer) GXYDataUtil.instance().model(SampleData.COUNTRIES).layer(outlinedLayerFactory).label("Countries").getLayer();
      TLcdGXYAsynchronousEditableLabelsLayerTreeNodeWrapper asynchronousShpLayer =
          new TLcdGXYAsynchronousEditableLabelsLayerTreeNodeWrapper(shpLayer, null);

      asynchronousShpLayer.setPaintOnTopOfChildrenHint(true);
      asynchronousShpLayer.setSelectable(true);
      asynchronousShpLayer.setLabeled(true);

      // Asynchronous painting also works for layer tree nodes...
      asynchronousShpLayer.addLayer(asynchronousGeoTIFFLayer);
      asynchronousShpLayer.addLayer(asynchronousDmedLayer);

      // ... and for layers with editable labels.
      asynchronousShpLayer.setLabelsEditable(true);

      // For illustration purposes, we add some synchronous and some slow layers:
      TLcdGXYLayerTreeNode polygonLayers = new TLcdGXYLayerTreeNode("Polygons");
      polygonLayers.setPaintOnTopOfChildrenHint(true);
      polygonLayers.addLayer(new TLcdGXYAsynchronousEditableLabelsLayerWrapper(
          DataFactory.createLayerWithMinimalPaintTime(100, 1.3)));
      polygonLayers.addLayer(new TLcdGXYAsynchronousEditableLabelsLayerWrapper(
          DataFactory.createLayerWithMinimalPaintTime(250, 2.6)));
      polygonLayers.addLayer(DataFactory.createSynchronousLayer());
      polygonLayers.addLayer(new TLcdGXYAsynchronousEditableLabelsLayerWrapper(
          DataFactory.createLayerWithMinimalPaintTime(750, 3.9)));

      GXYLayerUtil.addGXYLayer(getView(), asynchronousShpLayer);
      GXYLayerUtil.addGXYLayer(getView(), polygonLayers);
      GXYLayerUtil.fitGXYLayer(getView(), asynchronousDmedLayer);
    }
  }

  @Override
  protected void setupPaintQueueManager(ILcdGXYView aMap) {
    // Layers can share a single paint queue, but they have to be contiguous.
    // To facilitate linking wrappers with paint queues, a paint queue manager can be used.
    // The following code instantiates a default paint queue manager.
    // We've commented out this code because the PaintQueueManagerPanel already installs
    // various paint queue manager implementations.
      /*
      TLcdGXYAsynchronousPaintQueueManager manager = new TLcdGXYAsynchronousPaintQueueManager();
      manager.setGXYView( getView() );
       */
  }

  @Override
  protected JPanel createSettingsPanel() {
    //create the panel with the rendering options
    JPanel renderingPanel = createRenderingPanel();
    //create the panel for selecting the paint queue manager
    JPanel paintQueueManagerPanel = createPaintQueueManagerPanel(getView());
    //combine both panels into one panel, and return that panel
    JPanel togglePanel = new JPanel(new BorderLayout());
    togglePanel.add(renderingPanel, BorderLayout.NORTH);
    togglePanel.add(paintQueueManagerPanel, BorderLayout.SOUTH);
    return togglePanel;
  }

  private JPanel createRenderingPanel() {
    JCheckBox transparencyBox = new JCheckBox("Translucent", false);
    transparencyBox.setToolTipText("When enabled, all raster layers are painted semi-transparently.");
    transparencyBox.addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent aEvent) {
        Enumeration enumeration = getView().layers();
        while (enumeration.hasMoreElements()) {
          Object o = enumeration.nextElement();
          if (o instanceof ILcdGXYAsynchronousLayerWrapper && ((ILcdGXYAsynchronousLayerWrapper) o).getGXYLayer() instanceof TLcdGXYLayer) {
            try {
              final ILcdGXYAsynchronousLayerWrapper layerWrapper = (ILcdGXYAsynchronousLayerWrapper) o;
              // Ensures safe access to the wrapped TLcdGXYLayer object.
              layerWrapper.invokeAndWaitOnGXYLayer(new ILcdGXYAsynchronousLayerRunnable() {
                public void run(ILcdGXYLayer aSafeGXYLayer) {
                  TLcdGXYLayer layer = (TLcdGXYLayer) aSafeGXYLayer;
                  if (layer.getGXYPainterProvider() instanceof TLcdGXYImagePainter) {
                    TLcdGXYImagePainter provider = (TLcdGXYImagePainter) layer.getGXYPainterProvider();
                    provider.setOpacity(aEvent.getStateChange() == ItemEvent.SELECTED ? 0.50f : 1.0f);
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

    JPanel panel = new JPanel(new GridLayout(1, 1));
    panel.add(transparencyBox);
    return TitledPanel.createTitledPanel("Rendering", panel);
  }

  private JPanel createPaintQueueManagerPanel(ILcdGXYView aGXYView) {
    return TitledPanel.createTitledPanel("Paint queue manager", new PaintQueueManagerPanel(aGXYView));
  }

  // Main method

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Asynchronous Painting");
  }

}
