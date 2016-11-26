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
package samples.lightspeed.demo.application.data.weather;

import com.luciad.format.netcdf.lightspeed.TLspNetCDFLayerBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.util.ELcdInterpolationType;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

class TemperatureLayerFactory {

  private final TLcdColorMap fColorMap;

  public TemperatureLayerFactory(TLcdColorMap aColorMap) {
    fColorMap = aColorMap;
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    ILspEditableStyledLayer temperatureLayer = TLspNetCDFLayerBuilder
        .newBuilder()
        .model(aModel)
        .styler(TLspPaintRepresentationState.REGULAR_BODY,
                TLspRasterStyle
                    .newBuilder()
                    .startResolutionFactor(Double.POSITIVE_INFINITY)
                    .colorMap(fColorMap)
                    .interpolation(ELcdInterpolationType.LINEAR)
                    .build())
        .build();
    temperatureLayer.setLabel("Weather Temperature");
    return temperatureLayer;
  }

}
