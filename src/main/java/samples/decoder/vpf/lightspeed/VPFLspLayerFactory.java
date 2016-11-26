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
package samples.decoder.vpf.lightspeed;

import com.luciad.format.vpf.TLcdVPFFeatureClass;
import com.luciad.format.vpf.TLcdVPFModelDescriptor;
import com.luciad.format.vpf.lightspeed.TLspVPFLayerBuilder;
import com.luciad.util.service.LcdService;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * An <code>{@link ILspLayerFactory}</code> for VPF models.
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class VPFLspLayerFactory extends ALspSingleLayerFactory {

  private boolean fSimplifiedGeoSymStyling = false;

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdVPFModelDescriptor;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspLayer layer = TLspVPFLayerBuilder
        .newBuilder()
        .model(aModel)
        .simplifiedStyling(fSimplifiedGeoSymStyling)
        .build();

    if (layer instanceof ILcdLayerTreeNode) {
      enableTileReferenceLayersInTree((ILcdLayerTreeNode) layer);
    } else {
      enableTileReferenceLayer(layer);
    }

    return layer;
  }

  public boolean isSimplifiedGeoSymStyling() {
    return fSimplifiedGeoSymStyling;
  }

  public void setSimplifiedGeoSymStyling(boolean aSimplifiedGeoSymStyling) {
    fSimplifiedGeoSymStyling = aSimplifiedGeoSymStyling;
  }

  /**
   * Removes the scale range from the tile reference coverage layers and sets them visible.
   * This makes it easier to locate where the data is when outside the scale range.
   *
   * @param aLayerTree a VPF layer tree
   *
   */
  private void enableTileReferenceLayersInTree(ILcdLayerTreeNode aLayerTree) {
    for (int i = 0; i < aLayerTree.layerCount(); i++) {
      ILcdLayer layer = aLayerTree.getLayer(i);
      if (layer instanceof ILcdLayerTreeNode) {
        enableTileReferenceLayersInTree((ILcdLayerTreeNode) layer);
      } else {
        enableTileReferenceLayer(layer);
      }
    }
  }

  /**
   * Removes the scale range from the tile reference coverage layer and sets it visible.
   * This makes it easier to locate where the data is when outside the scale range.
   *
   * @param aLayer a VPF layer
   *
   */
  private void enableTileReferenceLayer(ILcdLayer aLayer) {
    TLcdVPFModelDescriptor modelDescriptor = (TLcdVPFModelDescriptor) aLayer.getModel().getModelDescriptor();
    TLcdVPFFeatureClass featureClass = modelDescriptor.getVPFFeatureClass();
    if (featureClass != null) {
      if ("tileref".equalsIgnoreCase(featureClass.getCoverage().getName())) {
        if (aLayer instanceof TLspLayer) {
          ((TLspLayer) aLayer).setScaleRange(TLspPaintRepresentation.BODY, new TLcdInterval(0, Double.MAX_VALUE));
        }
        aLayer.setVisible(true);
      }
    }
  }

}
