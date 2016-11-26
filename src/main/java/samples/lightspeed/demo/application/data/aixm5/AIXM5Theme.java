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
package samples.lightspeed.demo.application.data.aixm5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.format.aixmcommon.view.lightspeed.TLspAIXMStyler;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * <p>Main class for the AIXM5 theme.</p>
 *
 * <p>The theme shows some static AIXM5 data of the Chicago area.</p>
 *
 */
public class AIXM5Theme extends AbstractTheme {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AIXM5Theme.class);

  private Object fSimulatorModel = null;

  public AIXM5Theme() {
    super();
    setName("AIXM5");
    setCategory("Shapes");
    loadRequiredClassForQuickFail(TLspAIXMStyler.class);
  }

  @Override
  public List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = new ArrayList<ILspLayer>();

    layers.addAll(framework.getLayersWithID("layer.id.aixm5.airport"));

    try {
      // We do this check to prevent that the theme cannot be loaded when
      // when the realtime package is not available
      Class.forName("com.luciad.realtime.TLcdSimulator");
      ILcdModel procedureModel = framework.getModelWithID("model.id.aixm5.procedure");
      Object simulatorModel = AIXM5SimulatorUtil.createSimulatorModel(procedureModel);
      SimulationSupport simulationSupport = SimulationSupport.getInstance();
      simulationSupport.addSimulatorModelForTheme(simulatorModel, this);
      if (fSimulatorModel != null) {
        simulationSupport.removeSimulatorModelForTheme(fSimulatorModel, this);
      }
      fSimulatorModel = simulatorModel;

      for (ILspView view : aViews) {
        Collection<ILspLayer> realtimeLayers = new TrajectoryTrackLayerFactory().createLayers(
            AIXM5SimulatorUtil.getTrackModel(simulatorModel));
        layers.addAll(realtimeLayers);
        view.addLayer(realtimeLayers.iterator().next());

        framework.registerLayers("layer.id.aixm5.tracks", view, realtimeLayers);
      }

    } catch (ClassNotFoundException e) {
      sLogger.warn("Procedure layer can not be added to the AIXM5 theme. Realtime component is not available.");
    }

    return layers;
  }

  @Override
  public boolean isSimulated() {
    return true;
  }

}
