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
package samples.lightspeed.fundamentals.step1;

import java.awt.Color;

import com.luciad.earth.model.TLcdEarthModelDescriptor;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

/**
 * Basic ILspLayerFactory implementation that supports SHP data (vector) and
 * Luciad Earth repositories (raster).
 */
public class BasicLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    // Check the model descriptor to see if this is a SHP model or an Earth
    // repository model
    return (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof TLcdEarthModelDescriptor);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    // Create a layer depending on the type of model.
    if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      // Create a layer with the given model.
      // Create the fill and line styles using builders. The elevation mode for both
      // styles is set to ON_TERRAIN, so that the data will be draped over the 3D terrain
      // when the view is set to 3D.
      TLspFillStyle fill = TLspFillStyle.newBuilder()
                                        .color(Color.GREEN)
                                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                        .stipplePattern(TLspFillStyle.StipplePattern.HALF_TONE_2x2)
                                        .build();
      TLspLineStyle line = TLspLineStyle.newBuilder()
                                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                        .build();

      TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
      layerBuilder.model(aModel);
      layerBuilder.layerType(ILspLayer.LayerType.BACKGROUND);

      // Assign the line and fill styles to the bodies in their regular object state
      layerBuilder.bodyStyles(TLspPaintState.REGULAR, fill, line);

      // Derive the selection styling from the regular stying, but change the color
      layerBuilder.selectable(true);
      layerBuilder.bodyStyles(TLspPaintState.SELECTED,
                              fill.asBuilder().color(Color.RED).build(),
                              line.asBuilder().color(Color.RED).width(3).build());
      return layerBuilder.build();
    } else if (aModel.getModelDescriptor() instanceof TLcdEarthModelDescriptor) {
      // Create a raster layer using its builder, using all default settings.
      return TLspRasterLayerBuilder.newBuilder().model(aModel).build();
    } else {
      return null;
    }
  }
}
