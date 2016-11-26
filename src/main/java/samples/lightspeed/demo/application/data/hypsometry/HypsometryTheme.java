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
package samples.lightspeed.demo.application.data.hypsometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricShadingLayerBuilder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.services.effects.ALspLight;

import samples.lightspeed.demo.application.gui.menu.HypsometryPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * This theme demonstrates the capabilities of LuciadLightspeed
 * w.r.t. hypsometry terrain analysis.
 */
public class HypsometryTheme extends AbstractTheme {

  private HypsometryPanelFactory fPanelFactory;
  private List<JPanel> fThemePanels;

  private static void checkIfHypsometryAvailable() {
    // Checking whether the TEA module is available
    TLspHypsometricShadingLayerBuilder.newBuilder();
  }

  public HypsometryTheme() {
    checkIfHypsometryAvailable();
    setName("Hypsometry");
    setCategory("Terrain");
    fPanelFactory = new HypsometryPanelFactory();
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    List<ILspLayer> layers = new ArrayList<ILspLayer>();

    for (ILspView view : aViews) {
      List<ILspLayer> hypsometryLayers = Framework.getInstance().getLayersWithID("layer.id.hypsometry", view);
      layers.addAll(hypsometryLayers);
    }

    return layers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    if (fThemePanels == null) {
      fThemePanels = fPanelFactory.createThemePanels(this);
    }
    return fThemePanels;
  }

  @SuppressWarnings({"unchecked"})
  private List<ILspView> getViews() {
    return Framework.getInstance().getFrameworkContext().getViews();
  }

  private Collection<ALspLight> getLights(ILspView aView) {
    return aView.getServices().getGraphicsEffects().getEffectsByType(ALspLight.class);
  }

  @Override
  public void activate() {
    super.activate();

    for (ILspView view : getViews()) {
      Collection<ALspLight> lights = getLights(view);
      for (ALspLight light : lights) {
        light.setEnabled(false);
      }
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();

    for (ILspView view : getViews()) {
      Collection<ALspLight> lights = getLights(view);
      for (ALspLight light : lights) {
        light.setEnabled(true);
      }
    }
  }
}
