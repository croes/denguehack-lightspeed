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
package samples.fusion.client.gxy;

import static java.awt.Color.RED;

import static com.luciad.model.ILcdModel.NO_EVENT;
import static com.luciad.view.gxy.TLcdGXYHatchedFillStyle.Pattern.SLASH;
import static com.luciad.view.gxy.painter.ALcdGXYAreaPainter.OUTLINED_FILLED;

import java.util.EnumSet;

import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYHatchedFillStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;

/**
 * A GXY layer factory for non-tiled fusion coverages which paints the bounds of the coverage.
 *
 * @since 2013.1
 */
public class NonTiledFusionLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    if (!(modelDescriptor instanceof TLfnTileStoreModelDescriptor)) {
      return null;
    }
    TLfnTileStoreModelDescriptor tileStoreModelDescriptor = (TLfnTileStoreModelDescriptor) modelDescriptor;
    ALfnCoverageMetadata coverageMetadata = tileStoreModelDescriptor.getCoverageMetadata();
    if (coverageMetadata != null && coverageMetadata.getType() == null) {
      // A non-tiled coverage has the following characteristics:
      // - if it's an instance of TLfnCoverageMetadata
      // - its type is null
      // A non-tiled coverage model contains no elements.
      // If we want to visualize the bounding box, we create a new model with the bounding box as its element.
      ILcdModel nonTiledCoverageModel = new TLcd2DBoundsIndexedModel(aModel.getModelReference(), modelDescriptor);
      nonTiledCoverageModel.addElement(coverageMetadata.getBoundingBox(), NO_EVENT);
      TLcdGXYLayer layer = new NoSelectionLayer(nonTiledCoverageModel);
      TLcdGXYBoundsPainter boundingBoxPainter = new TLcdGXYBoundsPainter();
      TLcdG2DLineStyle lineStyle = new TLcdG2DLineStyle();
      lineStyle.setColor(RED);
      lineStyle.setLineWidth(2);
      boundingBoxPainter.setLineStyle(lineStyle);
      TLcdGXYHatchedFillStyle fillStyle = new TLcdGXYHatchedFillStyle();
      fillStyle.setLineColor(RED);
      fillStyle.setPattern(EnumSet.of(SLASH));
      boundingBoxPainter.setFillStyle(fillStyle);
      boundingBoxPainter.setMode(OUTLINED_FILLED);
      layer.setGXYPainterProvider(boundingBoxPainter);
      return layer;
    }
    return null;
  }
}
