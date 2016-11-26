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
package samples.lightspeed.demo.application.data.dynamictracks2;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.density.TLspDensityLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspDensityPointStyle;
import com.luciad.view.lightspeed.style.TLspIndexColorModelStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.common.tracks.AirbornTrackProvider;
import samples.lightspeed.demo.application.data.density.DensityIndexColorModelStyleUtil;
import samples.lightspeed.demo.application.data.support.EnrouteAirwaySimulatorModel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Layer factory for dynamic density layers
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>history.point.count</td> <td>int</td> <td>0</td> <td>Specifies the number of history
 * points that are to be drawn for each track</td></tr>
 * <tr> <td>history.point.interval</td> <td>double</td> <td>0</td> <td>Specifies the spacing
 * between the history points of a track</td></tr>
 * </table>
 */
public class DynamicDensityLayerFactoryInternal extends AbstractLayerFactory {

  private double fHistoryPointInterval;
  private int fHistoryPointCount;
  private TLspIndexColorModelStyle fIndexColorModelStyle;

  @Override
  public void configure(Properties aProperties) {
    fHistoryPointCount = Integer.valueOf(aProperties.getProperty("history.point.count", "0"));
    fHistoryPointInterval = Double.valueOf(aProperties.getProperty("history.point.interval", "0"));
    fIndexColorModelStyle = new DensityIndexColorModelStyleUtil().retrieveIndexColorModelStyle(aProperties);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null &&
           Framework.getInstance().getThemeByClass(DynamicTracksThemeInternal.class) != null;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    aModel = getTrackModel(aModel);

    return TLspDensityLayerBuilder.newBuilder()
                                  .model(aModel)
                                  .culling(false)
                                  .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                                  .indexColorModel(fIndexColorModelStyle)
                                  .bodyStyler(new TrackStyler()).build();
  }

  private ILcdModel getTrackModel(ILcdModel aModel) {
    // Write lock because route segments are not thread safe for reading
    try (Lock autoUnlock = writeLock(aModel)) {
      DynamicTracksThemeInternal densityTheme = Framework.getInstance().getThemeByClass(DynamicTracksThemeInternal.class);

      String key = EnrouteAirwaySimulatorModel.getModelKey(aModel);
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) SimulationSupport.getInstance().getSharedSimulatorModel(key);
      if (simulatorModel == null) {
        simulatorModel = new EnrouteAirwaySimulatorModel(aModel, fHistoryPointCount, fHistoryPointInterval);
        SimulationSupport.getInstance().setSharedSimulatorModel(key, simulatorModel);
      }
      SimulationSupport.getInstance().addSimulatorModelForTheme(simulatorModel, densityTheme);

      return simulatorModel.getTrackModels()[0];
    }
  }

  /**
   * Simple density styler for the track objects that are part of the
   * simulated track model on which the density theme is based.
   */
  private static class TrackStyler extends ALspStyler {

    private final TLspDensityPointStyle fDensityStyle;

    public TrackStyler() {
      fDensityStyle = TLspDensityPointStyle.newBuilder()
                                           .worldSize(200000.0f)
                                           .hardness(0.4f)
                                           .build();
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      aStyleCollector
          .geometry(AirbornTrackProvider.getProvider())
          .objects(aObjects)
          .style(fDensityStyle)
          .submit();
    }
  }
}
