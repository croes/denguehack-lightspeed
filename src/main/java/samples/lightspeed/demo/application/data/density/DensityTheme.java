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
package samples.lightspeed.demo.application.data.density;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.gui.menu.DensityPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Main class for the density theme.
 * <p>
 * This theme shows both a static and a dynamic density plot. For both plots, the color serves
 * as an indication of how much objects are at a specific location where blue means a low amount
 * of objects, red means a lot of objects.
 */
public class DensityTheme extends AbstractTheme {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DensityTheme.class);
  private static final String DYNAMIC_LAYER_ID = "layer.id.density.dynamic";
  private static final String STATIC_LAYER_ID = "layer.id.density.static";

  private DensityPanelFactory fDensityPanelFactory;
  private List<String> fLayerIDs;

  /**
   * Default constructor.
   */
  public DensityTheme() {
    setName("Density");
    setCategory("Tracks");
    fDensityPanelFactory = new DensityPanelFactory();
    fLayerIDs = new ArrayList<String>();
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = new ArrayList<ILspLayer>();

    for (ILspView view : aViews) {
      layers.addAll(framework.getLayersWithID(STATIC_LAYER_ID, view));
      if (!fLayerIDs.contains(STATIC_LAYER_ID)) {
        fLayerIDs.add(STATIC_LAYER_ID);
      }

      try {
        // Since the dynamic density layer depends on the (optional) realtime package,
        // we need to perform a check before trying to add the layer to this theme
        layers.addAll(framework.getLayersWithID(DYNAMIC_LAYER_ID, view));
        if (!fLayerIDs.contains(DYNAMIC_LAYER_ID)) {
          fLayerIDs.add(DYNAMIC_LAYER_ID);
        }
      } catch (Exception e) {
        sLogger.warn("Dynamic density layer could not be added to Density theme.");
      }
    }

    return layers;
  }

  @Override
  public void activate() {
    // We override the activate method because we only want
    // the static density plot displayed when the theme is
    // activated, since static and dynamic density will not
    // be shown simultaneously

    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    for (ILspView view : views) {
      List<ILspLayer> layers = Framework.getInstance().getLayersWithID("layer.id.density.static", view);
      for (ILspLayer layer : layers) {
        layer.setVisible(true);
      }
    }

    SimulationSupport.getInstance().activateTheme(this);
    SimulationSupport.getInstance().startSimulator();
  }

  @Override
  public boolean isSimulated() {
    return true;
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fDensityPanelFactory.createThemePanels(this);
  }

  /**
   * Returns the ID's of the layers that are part of this theme.
   *
   * @return a list of String-ID's that reference the layers of this theme
   */
  public List<String> getLayerIDs() {
    return fLayerIDs;
  }

}
