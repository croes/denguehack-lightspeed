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
package samples.lightspeed.demo.application.data.coastline;

import java.awt.Color;
import java.util.Set;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider;
import com.luciad.fusion.client.view.lightspeed.TLspFusionVectorLayerBuilder;
import com.luciad.fusion.tilestore.model.TLfnVectorTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * @author Dieter Meeus
 * @since 2012.1
 */
public class OSMCoastFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspFusionVectorLayerBuilder layerBuilder = TLspFusionVectorLayerBuilder.newBuilder();

    layerBuilder
        .model(aModel)
        .label("Coastline")
        .bodyStyler(TLspPaintState.REGULAR, createStyler())
        .bodyScaleRange(new TLcdInterval(0.012, Double.MAX_VALUE))
        .selectable(false);

    return layerBuilder.build();
  }

  private ILspStyler createStyler() {
    TLspStyler styler = new TLspStyler();
    TLspLineStyle lineStyle = TLspLineStyle.newBuilder()
                                           .color(Color.white)
                                           .opacity(0.5f)
                                           .width(1.5f)
                                           .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                           .build();
    styler.addStyles(TLspFusionGeometryProvider.LINE, lineStyle);
    return styler;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    if (descriptor instanceof TLfnVectorTileStoreModelDescriptor) {
      Set<TLcdDataType> modelElementTypes = ((TLfnVectorTileStoreModelDescriptor) descriptor).getModelElementTypes();
      for (TLcdDataType modelElementType : modelElementTypes) {
        if (modelElementType.getDeclaredProperty("osm_id") != null) {
          return true;
        }
      }
    }
    return false;
  }

}
