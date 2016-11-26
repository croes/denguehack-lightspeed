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
package samples.decoder.raster.nitf.lightspeed;

import com.luciad.format.cgm.lightspeed.TLspCGMStyler;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdNITFModelDescriptor;
import com.luciad.imaging.ALcdImage;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * Layer factory for NITF models.
 */
@LcdService(service = ILspLayerFactory.class)
public class NITFLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdNITFModelDescriptor;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspLayer rasterLayer = TLspRasterLayerBuilder
        .newBuilder()
        .model(aModel)
        .label("Raster")
        .filter(new RasterLayerFilter()).build();
    ILspLayer cgmLayer = TLspShapeLayerBuilder
        .newBuilder()
        .model(aModel)
        .bodyStyler(TLspPaintState.REGULAR, new TLspCGMStyler())
        .label("Vector")
        .filter(new CGMLayerFilter()).build();
    TLspLayerTreeNode node = new TLspLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
    node.addLayer(rasterLayer);
    node.addLayer(cgmLayer);
    return node;
  }

  private static class RasterLayerFilter implements ILcdDynamicFilter {
    @Override
    public void addChangeListener(ILcdChangeListener aListener) {
      // no changes
    }

    @Override
    public void removeChangeListener(ILcdChangeListener aListener) {
      // no changes
    }

    @Override
    public boolean accept(Object aObject) {
      return aObject instanceof ILcdRaster || aObject instanceof ILcdMultilevelRaster || aObject instanceof ALcdImage;
    }
  }

  private class CGMLayerFilter implements ILcdDynamicFilter {
    @Override
    public void addChangeListener(ILcdChangeListener aListener) {
      // no changes
    }

    @Override
    public void removeChangeListener(ILcdChangeListener aListener) {
      // no changes
    }

    @Override
    public boolean accept(Object aObject) {
      return !(aObject instanceof ILcdRaster || aObject instanceof ILcdMultilevelRaster || aObject instanceof ALcdImage);
    }
  }
}

