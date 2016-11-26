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

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * @author Dieter Meeus
 * @since 2012.1
 */
public class SHPCoastFactory extends ALspSingleLayerFactory {

  public SHPCoastFactory() {

  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    //We separate the detailed and normal layer for different zoomlevels.
    if (aModel.getModelDescriptor().getSourceName().contains("110m")) {
      //less detailed coastline: 1:110 million
      layerBuilder
          .model(aModel)
          .label("Coastline")
          .bodyEditable(false)
          .bodyStyler(TLspPaintState.REGULAR, createStyler())
          .bodyScaleRange(new TLcdInterval(0.00001, 0.0004))
          .selectable(false)
          .layerType(ILspLayer.LayerType.BACKGROUND);
    } else {
      //more detailed coastline: 1:10 million
      layerBuilder
          .model(aModel)
          .label("Coastline")
          .bodyEditable(false)
          .bodyStyler(TLspPaintState.REGULAR, createStyler())
          .bodyScaleRange(new TLcdInterval(0.0004, 0.012))
          .selectable(false)
          .layerType(ILspLayer.LayerType.BACKGROUND);
    }
    return layerBuilder.build();

  }

  private ILspStyler createStyler() {
    return TLspLineStyle.newBuilder()
                        .color(Color.white)
                        .opacity(0.5f)
                        .width(1.5f)
                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                        .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("coastline.shp");
  }
}
