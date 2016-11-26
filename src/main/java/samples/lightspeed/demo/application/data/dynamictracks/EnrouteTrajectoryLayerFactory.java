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
package samples.lightspeed.demo.application.data.dynamictracks;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle.BlendOverlapMode;

import samples.lightspeed.common.tracks.EnrouteTrajectoryModelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for models containing trajectories.
 */
public class EnrouteTrajectoryLayerFactory extends AbstractLayerFactory {

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    aModel = getDerivedModel(aModel);

    TLspLineStyle lineStyle = TLspLineStyle.newBuilder()
                                           .color(new Color(1.0f, 1.0f, 1.0f, 0.25f))
        .width(1.5f)
            // We explicitly set blend overlap to true so that
            // in regions with many trajectories they appear
            // brighter.
        .blendOverlap(BlendOverlapMode.ALWAYS)
        .build();

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, lineStyle)
                                .selectable(false)
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null;
  }

  /**
   * Returns the derived trajectories model for a given trajectories model.
   *
   * @param aModel the model
   *
   * @return the derived trajectories model
   */
  public static ILcdModel getDerivedModel(ILcdModel aModel) {
    Map<ILcdModel, ILcdModel> derivedModels = getDerivedModels();
    synchronized (derivedModels) {
      ILcdModel derivedModel = derivedModels.get(aModel);
      if (derivedModel == null) {
        derivedModel = EnrouteTrajectoryModelFactory.deriveTrajectoriesModel(aModel);
        derivedModels.put(aModel, derivedModel);
      }
      return derivedModel;
    }
  }

  private static Map<ILcdModel, ILcdModel> getDerivedModels() {
    String key = "enroute.trajectory.derivedModels";
    final Framework framework = Framework.getInstance();
    synchronized (framework) {
      Map<ILcdModel, ILcdModel> derivedModels = (Map<ILcdModel, ILcdModel>) framework.getSharedValue(key);
      if (derivedModels == null) {
        derivedModels = new WeakHashMap<ILcdModel, ILcdModel>();
        framework.storeSharedValue(key, derivedModels);
      }
      return derivedModels;
    }
  }

}
