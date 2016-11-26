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
package samples.lucy.clustering;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.lightspeed.ALcyLspStyleFormat;
import com.luciad.lucy.map.lightspeed.ALcyLspStyleRepository;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyler;

import samples.common.MapColors;
import samples.common.SampleData;
import samples.lightspeed.clustering.ClusterIgnoringLabelStylerWrapper;

/**
 * <p>
 *   {@code ALcyLspStyleFormat} extension for a specific dataset,
 *   which adds support for clustering.
 * </p>
 */
final class ClusteringFormat extends ALcyLspStyleFormat {

  private static final double SCALE_THRESHOLD_LABELS = 3e-3;
  static final String HUMANITARIAN_EVENTS_LABEL = "Humanitarian events";

  static final ILcdFilter<String> SOURCE_NAME_FILTER = new ILcdFilter<String>() {
    @Override
    public boolean accept(String aObject) {
      return SampleData.HUMANITARIAN_EVENTS.equals(aObject);
    }
  };

  protected ClusteringFormat(ILcyLucyEnv aLucyEnv, String aLongPrefix, String aShortPrefix, ALcyProperties aPreferences) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aPreferences, new ILcdFilter<ILcdModel>() {
      @Override
      public boolean accept(ILcdModel aModel) {
        return SOURCE_NAME_FILTER.accept(aModel.getModelDescriptor().getSourceName());
      }
    });
  }

  @Override
  protected ILspLayerFactory createLayerFactoryImpl() {
    return new ALspSingleLayerFactory() {
      @Override
      public boolean canCreateLayers(ILcdModel aModel) {
        //The safeguard format wrapper (created in the add-on) does all the required checks
        return true;
      }

      @Override
      public ILspLayer createLayer(ILcdModel aModel) {
        ILcdModel model;
        //when copying layers between maps, the incoming model might already be a clustered model
        //unpack all incoming models to retrieve the original model, to avoid sharing a clustered model between different views
        //except during workspace decoding. Then we need to restore the workspace exactly like it was
        if (getLucyEnv().getWorkspaceManager().isDecodingWorkspace()) {
          model = aModel;
        } else {
          ILcdModel unpackedModel = unpackModel(aModel);
          model = transformModel(unpackedModel);
        }

        ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(getLucyEnv());

        TLspCustomizableStyler singleEventStyler = new TLspCustomizableStyler(
            new TLspCustomizableStyle(
                TLspIconStyle.newBuilder()
                             .icon(MapColors.createIcon(false))
                             .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                             .build(), true)
        );
        ILspStyler bodyStyler = new ClusterBodyStyler(singleEventStyler);
        ILspStyler labelStyler = new ClusterIgnoringLabelStylerWrapper(
            new TLspCustomizableStyler(TLspTextStyle.newBuilder().build(),
                                       TLspLabelBoxStyle.newBuilder().padding(2).build(),
                                       TLspPinLineStyle.newBuilder().build(),
                                       TLspLabelOpacityStyle.newBuilder().build(),
                                       TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("EVENT_TYPE").build())
        );

        return TLspShapeLayerBuilder.newBuilder().model(model)
                                .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                                .bodyStyler(TLspPaintState.SELECTED, styleRepository.createSelectionStyler(bodyStyler))
                                .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                .labelStyler(TLspPaintState.SELECTED, styleRepository.createSelectionStyler(labelStyler))
                                .labelScaleRange(new TLcdInterval(SCALE_THRESHOLD_LABELS, Double.MAX_VALUE))
                                .label(HUMANITARIAN_EVENTS_LABEL)
                                .build();

      }
    };
  }

  static ALcdTransformingModel transformModel(ILcdModel aModel) {
    TLcdClusteringTransformer transformer =
        samples.lightspeed.clustering.MainPanel.createClusteringTransformer(null);
    return TLcdTransformingModelFactory.createTransformingModel(aModel, transformer);
  }

  private static ILcdModel unpackModel(ILcdModel aModel) {
    if (aModel instanceof ALcdTransformingModel) {
      return unpackModel(((ALcdTransformingModel) aModel).getOriginalModel());
    }
    return aModel;
  }
}
