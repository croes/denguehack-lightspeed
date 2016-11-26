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
package samples.lightspeed.style.raster;

import static samples.common.SampleData.*;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.luciad.model.ILcdModelDecoder;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.formatsupport.OpenAction;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.style.raster.gui.BoundsCustomizerPanel;
import samples.lightspeed.style.raster.gui.ColorMapCustomizerPanel;
import samples.lightspeed.style.raster.gui.RasterColorStylePanel;

/**
 * The Raster Style sample illustrates how the style of a raster can be
 * changed on the fly:
 * <ul>
 *   <li>Change the outline and fill of the raster's bounds.</li>
 *   <li>Change brightness, contrast and opacity.</li>
 *   <li>Set and change a custom filter (interactive raster layers only).</li>
 *   <li>Change the color model for elevation layers.</li>
 *   </ul>
 * <p/>
 * The sample starts with a raster style with 100% brightness, 100% contrast, 100%
 * opacity and no custom filter.
 * For more detailed information on how to use raster styles and providers, see
 * the developer's guide or the documented source code of this sample.
 * <p/>
 * The sample shows 3 types of layers:
 * <ul>
 *   <li>Interactive raster layers: the style of these layers can be changed interactively. They
 *   are marked with a [I] prefix.</li>
 *   <li>Background raster layers: the style of these layers can be changed but not as smoothly
 *   as interactive layers. However they generally require less processing and memory for
 *   painting. These layers are marked with a [B] prefix.</li>
 *   <li>Terrain layer: this layer is a proxy for the view's terrain. The view's terrain contains
 *   all background layers at the bottom of the view, hence changing its style will affect all
 *   those layers. Its style can be changed interactively.</li>
 * </ul>
 */
public class MainPanel extends LightspeedSample {

  public static final String TERRAIN_LAYER_NAME = "Terrain layer";

  private BoundsCustomizerPanel fBoundsCustomizerPanel;
  private RasterColorStylePanel fRasterColorStylePanel;
  private ColorMapCustomizerPanel fColorMapCustomizerPanel;

  protected void addData() {
    // Add grid layer
    LspDataUtil.instance().grid().addToView(getView());

    RasterLayerFactory layerFactory = new RasterLayerFactory();
    ServiceRegistry.getInstance().register(layerFactory);

    /**
     * Add a proxy for the view's terrain layer. Changing the style of this layer will affect all
     * background layers at the bottom of the view. Its style can be changed interactively.
     */
    getView().addLayer(new TerrainLayerProxy(getView()));

    /**
     * Add a few rasters as background. The style of these layers can be changed but not as smoothly
     * as interactive raster layers.
     */
    getView().addLayer(layerFactory.createBackgroundLayer(LspDataUtil.instance().model(SAN_FRANCISCO).getModel()));
    getView().addLayer(layerFactory.createBackgroundLayer(LspDataUtil.instance().model(SF_ELEVATION).getModel()));

    /**
     * Add a few interactive raster layers (the default behavior for ILcdRaster and
     * ILcdMultilevelRaster models). The style of these layers can be changed interactively.
     */
    getView().addLayersFor(LspDataUtil.instance().model(WASHINGTON).getModel());
    getView().addLayersFor(LspDataUtil.instance().model(LAKE_WHITNEY).getModel());
    Collection<ILspLayer> layers = getView().addLayersFor(LspDataUtil.instance().model(ALPS_ELEVATION).getModel());

    // Fit on the Alps layer.
    FitUtil.fitOnLayers(this, layers);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    LspOpenSupport openSupport = new LspOpenSupport(getView(), ServiceRegistry.getInstance().query(ILcdModelDecoder.class));
    openSupport.addStatusListener(getStatusBar());
    getToolBars()[0].addAction(new OpenAction(openSupport), ToolBar.FILE_GROUP);

    // Add a panel to configure the raster layer style
    JPanel stylePanel = new JPanel();
    stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.Y_AXIS));

    fBoundsCustomizerPanel = new BoundsCustomizerPanel();
    fRasterColorStylePanel = new RasterColorStylePanel();
    fColorMapCustomizerPanel = new ColorMapCustomizerPanel();
    stylePanel.add(fBoundsCustomizerPanel);
    stylePanel.add(fRasterColorStylePanel);
    stylePanel.add(fColorMapCustomizerPanel);

    add(stylePanel, BorderLayout.WEST);

    getSelectedLayers().addCollectionListener(new ILcdCollectionListener<ILcdLayer>() {
      @Override
      public void collectionChanged(TLcdCollectionEvent<ILcdLayer> aCollectionEvent) {
        Iterator<ILcdLayer> iterator = aCollectionEvent.getSource().iterator();
        configureLayer(iterator.hasNext() ? (ILspLayer) iterator.next() : null);
      }
    });
  }

  private void configureLayer(ILspLayer layer) {
    RasterStyler rasterStyler = null;
    if ((layer != null) && (layer instanceof ILspStyledLayer)) {
      ILspStyler styler = ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof RasterStyler) {
        rasterStyler = (RasterStyler) styler;
      }
    }
    // Some features (ex. image filter) are only supported for interactive layers and the view's terrain
    boolean interactiveLayer = rasterStyler != null &&
                               (layer.getLayerType() == ILspLayer.LayerType.INTERACTIVE ||
                                layer instanceof TerrainLayerProxy);

    fBoundsCustomizerPanel.setStyler(rasterStyler);
    fRasterColorStylePanel.setStyler(rasterStyler, interactiveLayer);
    fColorMapCustomizerPanel.setStyler(rasterStyler);
  }

  /**
   * A proxy for the view's terrain layer.
   * <p/>
   * The only purpose of this layer is to be able to select it in the layer tree and change its
   * style.
   *
   * @see com.luciad.view.lightspeed.services.terrain.ILspTerrainSupport
   */
  private static class TerrainLayerProxy extends TLspRasterLayer implements ILspEditableStyledLayer {
    private final ILspView fView;

    public TerrainLayerProxy(ILspView aView) {
      super(RasterModelFactory.createEmptyModel(TERRAIN_LAYER_NAME), TERRAIN_LAYER_NAME, LayerType.BACKGROUND);
      fView = aView;
      setStyler(TLspPaintRepresentationState.REGULAR_BODY, new RasterStyler());
    }

    @Override
    public void setStyler(TLspPaintRepresentationState aPaintRepresentationState, ILspStyler aStyler) {
      /**
       * Change the style of the view's terrain.
       */
      if (fView != null) {
        fView.getServices().getTerrainSupport().setBackgroundStyler(aStyler);
      }
    }

    @Override
    public ILspStyler getStyler(TLspPaintRepresentationState aPaintRepresentationState) {
      return fView != null ? fView.getServices().getTerrainSupport().getBackgroundStyler() : null;
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Raster styling");
  }

}
