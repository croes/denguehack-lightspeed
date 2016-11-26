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
package samples.lightspeed.decoder.csv;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspPlotStyle;

import samples.common.MapColors;
import samples.common.model.csv.CSVModelDescriptor;

/**
 * Layer factory for models decoded from CSV files.
 * Because CSV files are often used for big data, a density visualization is used.
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class CSVLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspPlotLayerBuilder builder = TLspPlotLayerBuilder.newBuilder();
    TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 10, MapColors.INTERACTIVE_FILL);
    symbol.setAntiAliasing(true);

    TLspPlotStyle.Builder<?> icon = TLspPlotStyle.newBuilder();
    icon.automaticScaling(500);
    icon.elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN);
    icon.density(true);

    builder.bodyStyles(TLspPaintState.REGULAR, icon.build());
    return builder.model(aModel).build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof CSVModelDescriptor;
  }
}
