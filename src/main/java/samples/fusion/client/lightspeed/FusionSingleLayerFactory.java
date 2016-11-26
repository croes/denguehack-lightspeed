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
package samples.fusion.client.lightspeed;

import static java.awt.Color.RED;

import static com.luciad.fusion.tilestore.ELfnDataType.IMAGE;
import static com.luciad.model.ILcdModel.NO_EVENT;
import static com.luciad.view.lightspeed.layer.ILspLayer.LayerType.INTERACTIVE;
import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;
import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ON_TERRAIN;
import static com.luciad.view.lightspeed.style.TLspFillStyle.StipplePattern.HATCHED;

import com.luciad.fusion.client.view.lightspeed.TLspFusionVectorLayerBuilder;
import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.multidimensional.ILcdMultiDimensionalModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

@LcdService(service = ILspLayerFactory.class)
public class FusionSingleLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLfnTileStoreModelDescriptor;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLfnTileStoreModelDescriptor descriptor = (TLfnTileStoreModelDescriptor) aModel.getModelDescriptor();
    ALfnCoverageMetadata metadata = descriptor.getCoverageMetadata();
    if (metadata == null) {
      throw new IllegalArgumentException("Model contains no coverage.");
    }
    if (metadata.getType() == null) {
      //this means we have a non-tiled coverage
      return createFusionNonTiledLayer(aModel);
    }
    switch (metadata.getType()) {
    case RASTER:
    case IMAGE:
    case ELEVATION:
    case MULTIVALUED:
      return createFusionRasterLayer(aModel);
    case VECTOR:
      return createFusionVectorLayer(aModel);
    }
    throw new IllegalArgumentException("Model contains no supported coverage.");
  }

  private ILspLayer createFusionRasterLayer(ILcdModel aModel) {
    ALfnTileStoreModel model = (ALfnTileStoreModel) aModel;
    TLfnTileStoreModelDescriptor descriptor = (TLfnTileStoreModelDescriptor) model.getModelDescriptor();

    ALfnCoverageMetadata metadata = descriptor.getCoverageMetadata();

    TLspRasterLayerBuilder layerBuilder = TLspRasterLayerBuilder.newBuilder().model(model);

    if (metadata.getType() == IMAGE && "image/png".equals(metadata.getFormat())
        || aModel instanceof ILcdMultiDimensionalModel) {
      // PNG indicates a vector-as-raster coverage, for example S-57.
      // For this case, we use an interactive layer type to reduce oversampling artifacts.
      layerBuilder.layerType(INTERACTIVE);
    }
    return layerBuilder.build();
  }

  private ILspLayer createFusionVectorLayer(ILcdModel aModel) {
    return TLspFusionVectorLayerBuilder.newBuilder().model(aModel).build();
  }

  private ILspLayer createFusionNonTiledLayer(ILcdModel aModel) {
    ALfnTileStoreModel model = (ALfnTileStoreModel) aModel;
    TLfnTileStoreModelDescriptor modelDescriptor = (TLfnTileStoreModelDescriptor) model.getModelDescriptor();
    // A non-tiled coverage model contains no elements.
    // If we want to visualize the bounding box, we create a new model with the bounding box as its element.
    ILcdModel nonTiledCoverageModel = new TLcd2DBoundsIndexedModel(aModel.getModelReference(), modelDescriptor);
    ALfnCoverageMetadata coverageMetadata = modelDescriptor.getCoverageMetadata();
    if (coverageMetadata != null && coverageMetadata.getType() == null) {
      nonTiledCoverageModel.addElement(coverageMetadata.getBoundingBox(), NO_EVENT);
    }
    TLspLineStyle outlineStyle = TLspLineStyle
        .newBuilder()
        .elevationMode(ON_TERRAIN)
        .color(RED)
        .width(2d)
        .build();
    TLspFillStyle fillStyle = TLspFillStyle
        .newBuilder()
        .elevationMode(ON_TERRAIN)
        .color(RED)
        .stipplePattern(HATCHED)
        .build();
    ILspLayer layer = TLspShapeLayerBuilder
        .newBuilder()
        .model(nonTiledCoverageModel)
        .bodyStyles(REGULAR, outlineStyle, fillStyle)
        .build();
    return layer;
  }
}
