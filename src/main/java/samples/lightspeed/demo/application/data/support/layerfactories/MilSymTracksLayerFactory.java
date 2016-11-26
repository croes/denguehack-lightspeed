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
package samples.lightspeed.demo.application.data.support.layerfactories;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bLayerBuilder;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyle;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer.LayerType;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.demo.application.data.dynamictracks.DynamicTracksTheme;
import samples.lightspeed.demo.application.data.milsym.MilSymTheme;
import samples.lightspeed.demo.application.data.support.EnrouteAirwaySimulatorModel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Layer factory for Enroute Track models that displays them using military symbology.
 */
public class MilSymTracksLayerFactory extends AbstractLayerFactory implements ILspLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null &&
           Framework.getInstance().getThemeByClass(DynamicTracksTheme.class) != null;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ILspLayer layer = createLayer(aModel);
    if (layer != null) {
      return Collections.singletonList(layer);
    } else {
      return Collections.emptyList();
    }
  }

  private static TLspMS2525bSymbolStyle createSymbologyStyle() {
    TLcdEditableMS2525bObject coded = new TLcdEditableMS2525bObject("SUAPCF---------");
    coded.setAffiliationValue("Friend");

    return TLspMS2525bSymbolStyle.newBuilder()
                                 .ms2525bCoded(coded)
                                 .build();
  }

  private static TLspVerticalLineStyle createVerticalLineStyle() {
    return TLspVerticalLineStyle.newBuilder().color(new Color(0.3f, 0.3f, 0.3f, 0.5f)).build();
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    ILcdModel[] trackModels = getTracksModel(aModel);

    if (trackModels != null && trackModels.length >= 1) {

      // Create a layer using the shape layer builder
      return TLspMS2525bLayerBuilder.newBuilder()
                                    .model(trackModels[0])
                                    .layerType(LayerType.REALTIME)
                                    .selectable(false)
                                    .culling(false)
                                    .label("Friendly Tracks")
                                    .bodyStyler(TLspPaintState.REGULAR, new TLspStyler(createSymbologyStyle(), createVerticalLineStyle()))
                                    .build();
    } else {
      return null;
    }
  }

  private static ILcdModel[] getTracksModel(ILcdModel aModel) {
    // Write lock because route segments are not thread safe for reading
    try (Lock autoUnlock = writeLock(aModel)) {
      MilSymTheme milSymTheme = Framework.getInstance().getThemeByClass(MilSymTheme.class);
      String key = EnrouteAirwaySimulatorModel.getModelKey(aModel);
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) SimulationSupport.getInstance().getSharedSimulatorModel(key);
      if (simulatorModel != null) {
        SimulationSupport.getInstance().addSimulatorModelForTheme(simulatorModel, milSymTheme);
        return simulatorModel.getTrackModels();
      }
      return null;
    }
  }

}
