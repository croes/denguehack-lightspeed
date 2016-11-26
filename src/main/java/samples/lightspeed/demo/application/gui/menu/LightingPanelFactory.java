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
package samples.lightspeed.demo.application.gui.menu;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import samples.lightspeed.demo.application.data.lighting.FogModel;
import samples.lightspeed.demo.application.data.lighting.FogPanelBuilder;
import samples.lightspeed.demo.application.data.lighting.LightingModel;
import samples.lightspeed.demo.application.data.lighting.LightingPanelBuilder;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Creates theme panels for the lighting theme.
 */
public class LightingPanelFactory implements IThemePanelFactory {

  private List<LightingModel> fLightingModels;
  private List<FogModel> fFogModels;

  public LightingPanelFactory(List<LightingModel> aLightingModels, List<FogModel> aFogModels) {
    fLightingModels = aLightingModels;
    fFogModels = aFogModels;
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    if (aTheme.getLayers().isEmpty()) {
      return null;
    }
    List<JPanel> panels = new ArrayList<JPanel>();

    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    LightingPanelBuilder lightingPanelBuilder = new LightingPanelBuilder(fLightingModels);
    panels.add(lightingPanelBuilder.createContentPanel("Lighting", isTouchUI));

    FogPanelBuilder fogPanelBuilder = new FogPanelBuilder(fFogModels);
    panels.add(fogPanelBuilder.createContentPanel("Fog", isTouchUI));

    return panels;
  }
}
