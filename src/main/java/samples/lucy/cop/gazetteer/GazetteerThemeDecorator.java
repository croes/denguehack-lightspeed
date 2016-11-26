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
package samples.lucy.cop.gazetteer;

import java.awt.Color;
import java.awt.Font;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.lightspeed.ALcyLspStyleRepository;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lucy.cop.PathResolver;
import samples.lucy.theme.Theme;

/**
 * <p>Decorator for a theme which adds a gazetteer layer and
 * the UI as application pane</p>
 */
public final class GazetteerThemeDecorator implements Theme {
  private static final String GAZETTEER_PREFIX = "gazetteer.";
  private static final String LAYER_PREFIX = GAZETTEER_PREFIX + "layer.";
  private static final String SERVER_ADDRESS = GAZETTEER_PREFIX + "server.address";

  private final Theme fDelegate;
  private final ILcyLucyEnv fLucyEnv;
  private final GazetteerModel fGazetteerModel;
  private final ILspLayer fGazetteerLayer;
  private final GazetteerApplicationPaneTool fApplicationPaneTool;

  public GazetteerThemeDecorator(Theme aDelegate, String aPropertyPrefix, ALcyProperties aProperties, String aLongPrefix, ILcyLucyEnv aLucyEnv) {
    fDelegate = aDelegate;
    fLucyEnv = aLucyEnv;
    fGazetteerModel = new GazetteerModel(retrieveServerAddress(aPropertyPrefix, aProperties, aLucyEnv));
    fGazetteerModel.addStatusListener(aLucyEnv);
    fGazetteerLayer = createGazetteerLayer(fGazetteerModel, aPropertyPrefix, aProperties, aLucyEnv);
    fApplicationPaneTool = new GazetteerApplicationPaneTool(aProperties, aLongPrefix + GAZETTEER_PREFIX, aPropertyPrefix + GAZETTEER_PREFIX, fGazetteerModel, fGazetteerLayer);
    fApplicationPaneTool.plugInto(aLucyEnv);
    hideGazetteerUI();
  }

  private ILspLayer createGazetteerLayer(GazetteerModel aGazetteerModel, String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    String layerPrefix = aPropertyPrefix + LAYER_PREFIX;
    String label = aProperties.getString(layerPrefix + "label", null);

    ILspStyler bodyStyler = new ClusterAwareGazetteerStylerWrapper(
        new GazetteerStyler(aPropertyPrefix + GAZETTEER_PREFIX, aProperties),
        aPropertyPrefix + GAZETTEER_PREFIX, aProperties);
    ILspStyler selectionStyler = ALcyLspStyleRepository.getInstance(aLucyEnv).createSelectionStyler(bodyStyler);

    ILspStyler labelStyler = buildLabelStyler();
    ILspStyler selectedLabelStyler = ALcyLspStyleRepository.getInstance(aLucyEnv).createSelectionStyler(labelStyler);

    TLcdClusteringTransformer transformer =
        TLcdClusteringTransformer.newBuilder()
                                 .defaultParameters()
                                   .clusterSize(200)
                                   .minimumPoints(2)
                                   .build()
                                 .build();
    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                               .model(TLcdTransformingModelFactory.createTransformingModel(aGazetteerModel, transformer))
                                                               .label(label)
                                                               .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                                                               .bodyStyler(TLspPaintState.SELECTED, selectionStyler)
                                                               .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                                               .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
                                                               .build();
    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, false);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);
    return layer;
  }

  private ILspStyler buildLabelStyler() {
    TLspDataObjectLabelTextProviderStyle labelTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("featureName").build();
    TLspTextStyle textStyle = TLspTextStyle.newBuilder().font(new Font("Arial", Font.PLAIN, 11)).textColor(Color.white).haloColor(Color.black).haloThickness(1).build();

    return TLspLabelStyler.newBuilder().styles(labelTextProviderStyle, textStyle).build();
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    assertSameLucyEnv(aLucyEnv);
    fDelegate.activate(aLucyEnv);
    addGazetteerLayerToActiveMapComponent(aLucyEnv);
    showGazetteerUI();
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    assertSameLucyEnv(aLucyEnv);
    fDelegate.deactivate(aLucyEnv);
    removeGazetteerLayerFromActiveMapComponent(aLucyEnv);
    hideGazetteerUI();
  }

  private void showGazetteerUI() {
    fApplicationPaneTool.setApplicationPaneActive(true);
  }

  private void hideGazetteerUI() {
    fApplicationPaneTool.setApplicationPaneActive(false);
  }

  private void addGazetteerLayerToActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    getActiveMapComponent(aLucyEnv).getMainView().addLayer(fGazetteerLayer);
  }

  private void removeGazetteerLayerFromActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    getActiveMapComponent(aLucyEnv).getMainView().removeLayer(fGazetteerLayer);
  }

  @Override
  public String getDisplayName() {
    return fDelegate.getDisplayName();
  }

  private void assertSameLucyEnv(ILcyLucyEnv aLucyEnv) {
    if (aLucyEnv != fLucyEnv) {
      throw new UnsupportedOperationException("The theme can only be used for one ILcyLucyEnv.");
    }
  }

  private ILcyGenericMapComponent<ILspView, ILspLayer> getActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    return mapManager.getActiveMapComponent();
  }

  private static String retrieveServerAddress(String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    return aLucyEnv.getService(PathResolver.class).convertPath(aProperties.getString(aPropertyPrefix + SERVER_ADDRESS, "http://localhost:8072/"));
  }
}
