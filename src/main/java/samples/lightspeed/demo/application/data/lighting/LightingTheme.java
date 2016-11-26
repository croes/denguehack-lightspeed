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
package samples.lightspeed.demo.application.data.lighting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.services.effects.ALspGraphicsEffect;
import com.luciad.view.lightspeed.services.effects.ALspLight;
import com.luciad.view.lightspeed.services.effects.TLspFog;

import samples.lightspeed.demo.application.gui.menu.LightingPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

public class LightingTheme extends AbstractTheme {
  private Map<ILspView, List<ALspGraphicsEffect>> fPreviousViewEffectsMap;

  private Map<ILspView, LightingModel> fLightingModels = new IdentityHashMap<ILspView, LightingModel>();
  private Map<ILspView, FogModel> fFogModels = new IdentityHashMap<ILspView, FogModel>();

  private static final String[] LAYER_IDS = new String[]{
      "layer.id.clouds",
      "layer.id.buildings"
  };
  private List<JPanel> fThemePanels;

  @SuppressWarnings({"unchecked"})
  public List<ILspView> getViews() {
    return Framework.getInstance().getFrameworkContext().getViews();
  }

  public LightingTheme() {
    setName("Lighting");
    setCategory("Shapes");

    fPreviousViewEffectsMap = new HashMap<ILspView, List<ALspGraphicsEffect>>();
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();

    List<ILspLayer> layers = new ArrayList<ILspLayer>();
    for (ILspView view : aViews) {
      for (String layerId : LAYER_IDS) {
        layers.addAll(framework.getLayersWithID(layerId, view));
      }
    }

    return layers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    if (fThemePanels == null) {
      fThemePanels = (new LightingPanelFactory(getAllLightingModels(), getAllFogModels())).createThemePanels(this);
    }
    return fThemePanels;
  }

  private LightingModel getLightingModel(ILspView aView) {
    LightingModel lightingModel = fLightingModels.get(aView);
    if (lightingModel == null) {
      lightingModel = new LightingModel(aView);
      fLightingModels.put(aView, lightingModel);
    }
    return lightingModel;
  }

  private List<LightingModel> getAllLightingModels() {
    List<LightingModel> lightingModels = new ArrayList<LightingModel>();

    for (ILspView view : getViews()) {
      lightingModels.add(getLightingModel(view));
    }

    return lightingModels;
  }

  private FogModel getFogModel(ILspView aView) {
    FogModel fogModel = fFogModels.get(aView);
    if (fogModel == null) {
      fogModel = new FogModel(aView);
      fFogModels.put(aView, fogModel);
    }
    return fogModel;
  }

  private List<FogModel> getAllFogModels() {
    List<FogModel> fogModels = new ArrayList<FogModel>();

    for (ILspView view : getViews()) {
      fogModels.add(getFogModel(view));
    }

    return fogModels;
  }

  @Override
  public void activate() {
    super.activate();

    // Store the current graphic effects so we can restore them later when switching to another theme.
    for (ILspView view : getViews()) {
      LightingModel lightingModel = getLightingModel(view);
      FogModel fogModel = getFogModel(view);
      if (view != null) {
        storePreviousEffects(view);
      }
      if (lightingModel != null) {
        lightingModel.activate(view);
      }
      if (fogModel != null) {
        fogModel.activate(view);
      }
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();

    // Restore the previous lighting settings
    for (ILspView view : getViews()) {
      if (view != null) {
        restorePreviousEffects(view);
      }
    }
  }

  /**
   * Store all current light and fog effects in a list,
   * so we can re-enable them again after selecting another theme.
   *
   * @param aView
   */
  private void storePreviousEffects(ILspView aView) {
    List<ALspGraphicsEffect> previousEffects = new ArrayList<ALspGraphicsEffect>();
    for (ALspGraphicsEffect graphicsEffect : aView.getServices().getGraphicsEffects()) {
      if (graphicsEffect instanceof ALspLight || graphicsEffect instanceof TLspFog) {
        previousEffects.add(graphicsEffect);
      }
    }
    // Remove these effects from the view
    for (ALspGraphicsEffect previousEffect : previousEffects) {
      aView.getServices().getGraphicsEffects().remove(previousEffect);
    }

    fPreviousViewEffectsMap.put(aView, previousEffects);
  }

  /**
   * Disable the lights and fog created in the lighting theme
   * and re-enable the initial effects for this view.
   *
   * @param aView
   */
  private void restorePreviousEffects(ILspView aView) {
    if (fPreviousViewEffectsMap.get(aView) != null) {
      //Remove all lighting and fog effects created in the theme
      List<ALspGraphicsEffect> graphicsEffectsToRemove = new ArrayList<ALspGraphicsEffect>();
      for (ALspLight light : aView.getServices().getGraphicsEffects().getEffectsByType(ALspLight.class)) {
        graphicsEffectsToRemove.add(light);
      }
      for (TLspFog fog : aView.getServices().getGraphicsEffects().getEffectsByType(TLspFog.class)) {
        graphicsEffectsToRemove.add(fog);
      }
      // remove the effects
      for (ALspGraphicsEffect graphicsEffect : graphicsEffectsToRemove) {
        aView.getServices().getGraphicsEffects().remove(graphicsEffect);
      }

      // add the initial effects
      for (ALspGraphicsEffect graphicsEffect : fPreviousViewEffectsMap.get(aView)) {
        aView.getServices().getGraphicsEffects().add(graphicsEffect);
      }
    }
  }

}
