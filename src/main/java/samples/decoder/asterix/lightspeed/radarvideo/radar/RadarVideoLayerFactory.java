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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import com.luciad.format.asterix.TLcdASTERIXRadarVideoModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.lightspeed.radarvideo.TLspRadarVideoLayerBuilder;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;

/**
 * Layer factory for radar video models.
 */
public class RadarVideoLayerFactory extends ALspSingleLayerFactory {

  private RadarStyleProperties fRadarStyleProperties;

  public RadarVideoLayerFactory(RadarStyleProperties aRadarStyleProperties) {
    fRadarStyleProperties = aRadarStyleProperties;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    RadarStyler radarStyler1 = new RadarStyler(fRadarStyleProperties, false);
    RadarStyler radarStyler2 = new RadarStyler(fRadarStyleProperties, true);

    return TLspRadarVideoLayerBuilder
        .newBuilder()
        .selectable(false)
        .model(aModel)
        .bodyStyler(TLspPaintState.REGULAR, radarStyler1)
        .bodyStyler(TLspPaintState.SELECTED, radarStyler2)
        .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdASTERIXRadarVideoModelDescriptor;
  }
}
