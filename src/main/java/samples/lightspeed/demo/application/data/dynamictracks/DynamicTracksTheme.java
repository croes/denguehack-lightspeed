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

import java.util.ArrayList;
import java.util.List;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Theme that displays trajectories as polylines and tracks as points that move along
 * the trajectories.
 * <p/>
 * When the theme is activated, two layers (one for the trajectories and one for the tracks)
 * are retrieved from the demo framework and are made visible. The following keys are used
 * to retrieve those layers:
 * <ul>
 * <li><code>layer.id.enroute.trajectory</code> for the trajectories layer</li>
 * <li><code>layer.id.enroute.track</code> for the tracks layer</li>
 * </ul>
 * Note that this implies that those layers must have been created by the demo framework
 * in order for this theme to work.
 */
public class DynamicTracksTheme extends AbstractTheme {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DynamicTracksTheme.class);

  /**
   * Default constructor.
   */
  public DynamicTracksTheme() {
    setName("Air Tracks");
    setCategory("Tracks");
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<String> layerIds = new ArrayList<String>();
    List<ILspLayer> layers = new ArrayList<ILspLayer>();

    layers.addAll(framework.getLayersWithID("layer.id.enroute.trajectory"));
    layerIds.add("layer.id.enroute.trajectory");

    try {
      // Since the track layer depends on the (optional) realtime package, we need
      // to perform a check before trying to add the layer to this theme
      layers.addAll(framework.getLayersWithID("layer.id.enroute.track"));
      layerIds.add("layer.id.enroute.track");
    } catch (Exception e) {
      sLogger.warn("Track layer could not be added to Dynamic Tracks theme.");
    }
    return layers;
  }

  @Override
  public boolean isSimulated() {
    return true;
  }

}
