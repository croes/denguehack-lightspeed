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
package samples.lucy.cop.addons.missioncontroltheme;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import com.luciad.earth.model.TLcdEarthModelDescriptor;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.lightspeed.ALcyLspStyleRepository;
import com.luciad.lucy.map.lightspeed.TLcyLspCompositeLayerFactory;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bLayerBuilder;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.editor.operation.ILspEditingStateListener;
import com.luciad.view.lightspeed.editor.operation.TLspEditingStateEvent;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.lucy.theme.Theme;

/**
 * Mission control theme.
 */
final class MissionControlTheme implements Theme {

  private static final String CREATE_CONTROLLER_PREFIX = "createController.";

  private final AnnotationModel fAnnotationModel;
  private final ILspInteractivePaintableLayer fAnnotationLayer;

  private final BlueForcesModel fBlueForcesModel;
  private final ILspInteractivePaintableLayer fBlueForcesLayer;

  private final CreateControllersApplicationPaneTool fCreateControllersApplicationPaneTool;
  private final SpotReportsModel fSpotReportsModel;
  private final ILspInteractivePaintableLayer fSpotReportsLayer;
  private final ILspLayer fBostonLayer;

  MissionControlTheme(String aPropertiesPrefix, ALcyProperties aProperties, String aLongPrefix, ILcyLucyEnv aLucyEnv) {
    fAnnotationModel = new AnnotationModel(aPropertiesPrefix, aProperties, aLucyEnv);
    fAnnotationLayer = createAnnotationLayer(fAnnotationModel, aLucyEnv);

    fSpotReportsModel = new SpotReportsModel(aPropertiesPrefix, aProperties, aLucyEnv);
    fSpotReportsLayer = createSpotReportsLayer(fSpotReportsModel, aLucyEnv);

    fBlueForcesModel = new BlueForcesModel(aPropertiesPrefix, aProperties, aLucyEnv);
    fBlueForcesLayer = createBlueForcesLayer(fBlueForcesModel, aLucyEnv);
    fBlueForcesModel.activate();

    ILcdModel bostonModel = createBostonModel(aPropertiesPrefix, aProperties);
    fBostonLayer = new TLcyLspCompositeLayerFactory(aLucyEnv).createLayers(bostonModel).iterator().next();

    fCreateControllersApplicationPaneTool = new CreateControllersApplicationPaneTool(aProperties, aLongPrefix + CREATE_CONTROLLER_PREFIX, aPropertiesPrefix + CREATE_CONTROLLER_PREFIX, fAnnotationLayer, fSpotReportsLayer);
    fCreateControllersApplicationPaneTool.plugInto(aLucyEnv);
    fCreateControllersApplicationPaneTool.setApplicationPaneActive(false);

    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.DISPOSING) {
          fAnnotationModel.dispose();
          fBlueForcesModel.dispose();
          fSpotReportsModel.dispose();
        }
      }
    });

  }

  private ILcdModel createBostonModel(String aPropertiesPrefix, ALcyProperties aProperties) {
    String sourceName = aProperties.getString(aPropertiesPrefix + "boston.sourceName", "Data/PNGTileSet/boston/");
    URI baseUri = getUriFor(sourceName);
    BostonTileSet tileSet = new BostonTileSet(baseUri);
    TLcdVectorModel bostonModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdEarthModelDescriptor(baseUri.toString(), "Background", aProperties.getString(aPropertiesPrefix + "boston.label", "Boston")));
    bostonModel.addElement(tileSet, ILcdModel.NO_EVENT);
    return bostonModel;
  }

  private URI getUriFor(String aSourceName) {
    URL resource = getClass().getClassLoader().getResource(aSourceName);
    if (resource != null) {
      try {
        //get the directory of the source name
        return resource.toURI();
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    File file = new File(aSourceName);
    if (file.exists()) {
      return file.toURI();
    }

    return null;
  }

  private ILspInteractivePaintableLayer createAnnotationLayer(AGeoJsonRestModelWithUpdates aAnnotationModel, ILcyLucyEnv aLucyEnv) {
    final ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(aLucyEnv);

    ILspStyler bodyStyler = new AnnotationsStyler();
    ILspStyler selectedBodyStyler = styleRepository.createSelectionStyler(bodyStyler);

    ILspStyler labelStyler = TLspLabelStyler.newBuilder()
                                                 .styles(TLspDataObjectLabelTextProviderStyle.newBuilder().expressions(AnnotationModel.TEXT_PROPERTY.getName()).build(),
                                                         TLspTextStyle.newBuilder().font(new Font("Arial", Font.PLAIN, 11)).textColor(Color.white).haloColor(Color.black).haloThickness(1).build())
                                                 .build();
    ILspStyler selectedLabelStyler = styleRepository.createSelectionStyler(labelStyler);

    ILspInteractivePaintableLayer layer =
        TLspShapeLayerBuilder.newBuilder()
                             .model(aAnnotationModel)
                             .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                             .bodyStyler(TLspPaintState.SELECTED, selectedBodyStyler)
                             .labelStyler(TLspPaintState.REGULAR, labelStyler)
                             .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
                             .editableSupported(true)
                             .bodyEditable(true)
                             .build();
    layer.setEditable(true);

    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, false);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);

    layerShouldCommitChangesWhenEditingIsFinished(layer, aAnnotationModel);
    return layer;
  }

  private ILspInteractivePaintableLayer createBlueForcesLayer(ILcdModel aBlueForcesModel, ILcyLucyEnv aLucyEnv) {

    final ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(aLucyEnv);

    ILspStyler styler = new BlueForcesStyler();
    ILspStyler selectedStyler = styleRepository.createSelectionStyler(styler);

    ILspInteractivePaintableLayer layer =
        TLspAPP6ALayerBuilder.newBuilder()
                             .layerType(ILspLayer.LayerType.REALTIME)
                             .model(aBlueForcesModel)
                             .bodyStyler(TLspPaintState.REGULAR, styler)
                             .bodyStyler(TLspPaintState.SELECTED, selectedStyler)
                             .build();
    return layer;
  }

  private ILspInteractivePaintableLayer createSpotReportsLayer(AGeoJsonRestModelWithUpdates aSpotReportsModel, ILcyLucyEnv aLucyEnv) {
    final ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(aLucyEnv);

    ILspStyler bodyStyler = new SpotReportsStyler();
    ILspStyler selectedBodyStyler = styleRepository.createSelectionStyler(bodyStyler);

    ILspInteractivePaintableLayer layer = TLspMS2525bLayerBuilder.newBuilder()
                                                                 .model(aSpotReportsModel)
                                                                 .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                                                                 .bodyStyler(TLspPaintState.SELECTED, selectedBodyStyler)
                                                                 .bodyEditable(true)
                                                                 .build();
    layer.setEditable(true);

    layerShouldCommitChangesWhenEditingIsFinished(layer, aSpotReportsModel);
    return layer;
  }

  private static void layerShouldCommitChangesWhenEditingIsFinished(ILspInteractivePaintableLayer aLayer, final AGeoJsonRestModelWithUpdates aModel) {
    aLayer.addEditingStateListener(new ILspEditingStateListener() {
      @Override
      public void editingStateChanged(TLspEditingStateEvent aEvent) {
        if (aEvent.getChangeType() == TLspEditingStateEvent.ChangeType.END_EDITING) {
          try (Lock autoUnlock = writeLock(aModel)) {
            aModel.editingFinished(((GeoJsonRestModelElement) aEvent.getObject()));
          }
        }
      }
    });
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    ILspView view = getActiveMapComponent(aLucyEnv).getMainView();
    view.addLayer(fAnnotationLayer);
    view.addLayer(fSpotReportsLayer);
    view.addLayer(fBlueForcesLayer);
    view.addLayer(fBostonLayer);

    try {
      new TLspViewNavigationUtil(view).animatedFit(Collections.<ILspLayer>singletonList(fBlueForcesLayer));
    } catch (TLcdNoBoundsException | TLcdOutOfBoundsException e) {
      //ignore
    }

    fCreateControllersApplicationPaneTool.setApplicationPaneActive(true);
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    ILspView view = getActiveMapComponent(aLucyEnv).getMainView();
    view.removeLayer(fAnnotationLayer);
    view.removeLayer(fSpotReportsLayer);
    view.removeLayer(fBlueForcesLayer);
    view.removeLayer(fBostonLayer);

    fCreateControllersApplicationPaneTool.setApplicationPaneActive(false);
  }

  private ILcyGenericMapComponent<ILspView, ILspLayer> getActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    return mapManager.getActiveMapComponent();
  }

  @Override
  public String getDisplayName() {
    return "Mission Control";
  }
}
