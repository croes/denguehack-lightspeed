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
package samples.lightspeed.lidar;

import com.luciad.format.las.TLcdLASModelDescriptor;
import samples.lightspeed.lidar.LASStyler;
import com.luciad.lidar.lightspeed.TLspLIDARLayerBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.TLspPlotStyle;

/**
 * Layer factory for LAS data that uses a {@link LASStyler} to support various styling options.
 *
 * @since 2014.0
 */
@LcdService(service = ILspLayerFactory.class)
public class LASLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdLASModelDescriptor);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    // Note: using a LASStyler is optional, you can also simply pass no styler to get the default styling.

    TLspPlotStyle defaultStyle = TLspLIDARLayerBuilder.getDefaultStyle(aModel);
    LASStyler lasStyler = new LASStyler((TLcdLASModelDescriptor) aModel.getModelDescriptor(), defaultStyle);

    return TLspLIDARLayerBuilder.newBuilder()
                                .model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, lasStyler)
                                .build();
  }
}
