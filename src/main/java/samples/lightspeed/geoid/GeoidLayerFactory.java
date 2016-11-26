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
package samples.lightspeed.geoid;

import java.awt.Color;

import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

class GeoidLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    //configure a raster styler with the color map that needs to be applied.
    ILspStyler rasterStyler = TLspRasterStyle.newBuilder()
                                             .colorMap(createColorMap())
                                             .build();
    return TLspRasterLayerBuilder.newBuilder()
                                 .styler(TLspPaintRepresentationState.REGULAR_BODY, rasterStyler)
                                 .model(aModel)
                                 .build();
  }

  private TLcdColorMap createColorMap() {
    // Create an elevation color model for the limited range of geoid heights.
    double[] levels = {
        TLcdDTEDTileDecoder.UNKNOWN_ELEVATION,
        -110.0,
        -40.0,
        40.0,
        110.0,
    };

    Color[] colors = {
        new Color(0, 0, 0, 0),
        new Color(0, 0, 0, 128),
        new Color(0, 0, 255, 128),
        new Color(0, 255, 0, 128),
        new Color(255, 255, 255, 128),

    };

    return new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), levels, colors);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor;
  }
}
