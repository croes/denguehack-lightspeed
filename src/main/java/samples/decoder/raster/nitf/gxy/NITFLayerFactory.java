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
package samples.decoder.raster.nitf.gxy;

import com.luciad.format.cgm.gxy.TLcdCGMGXYPainterProvider;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdNITFModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ALcdGXYPen;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.gxy.decoder.MapSupport;

/**
 * This ILcdGXYLayerFactory is suitable for models containing NITF data, which
 * may be a combination of rasters and CGM vector data.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class NITFLayerFactory implements ILcdGXYLayerFactory {

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    if (aModel.getModelDescriptor() instanceof TLcdNITFModelDescriptor) {
      NITFGXYPainterProvider nitfGXYPainterProvider = new NITFGXYPainterProvider();
      ALcdGXYPen gxyPen = MapSupport.createPen(aModel.getModelReference());

      TLcdGXYLayer rasterLayer = new TLcdGXYLayer();
      rasterLayer.setModel(aModel);
      rasterLayer.setLabel("Raster");
      rasterLayer.setSelectable(false);
      rasterLayer.setFilter(new RasterLayerFilter());
      rasterLayer.setGXYPainterProvider(nitfGXYPainterProvider);
      rasterLayer.setGXYPen(gxyPen);

      TLcdGXYLayer vectorLayer = new TLcdGXYLayer();
      vectorLayer.setModel(aModel);
      vectorLayer.setLabel("Vector");
      vectorLayer.setSelectable(true);
      vectorLayer.setFilter(new CGMLayerFilter());
      vectorLayer.setGXYPainterProvider(nitfGXYPainterProvider);
      vectorLayer.setGXYPen(gxyPen);

      TLcdGXYLayerTreeNode node = new TLcdGXYLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      node.addLayer(rasterLayer);
      node.addLayer(vectorLayer);

      return node;
    }
    return null;
  }

  /**
   * This ILcdGXYPainterProvider provides painters for NITF rasters and for CGM
   * elements.
   */
  private static class NITFGXYPainterProvider implements ILcdGXYPainterProvider {

    private TLcdGXYImagePainter fRasterPainter = new TLcdGXYImagePainter();
    private TLcdCGMGXYPainterProvider fCGMGXYPainterProvider = new TLcdCGMGXYPainterProvider();

    private NITFGXYPainterProvider() {
      fRasterPainter.setFillOutlineArea(true);
    }

    public ILcdGXYPainter getGXYPainter(Object aObject) {
      try {
        fRasterPainter.setObject(aObject);
        return fRasterPainter;
      } catch (IllegalArgumentException ignored) {
        return fCGMGXYPainterProvider.getGXYPainter(aObject);
      }
    }

    public Object clone() {
      try {
        NITFGXYPainterProvider clone = (NITFGXYPainterProvider) super.clone();
        clone.fRasterPainter = (TLcdGXYImagePainter) fRasterPainter.clone();
        clone.fCGMGXYPainterProvider = (TLcdCGMGXYPainterProvider) fCGMGXYPainterProvider.clone();
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new UnsupportedOperationException(e.getMessage());
      }
    }
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
      return aObject instanceof ILcdRaster || aObject instanceof ILcdMultilevelRaster;
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
      return !(aObject instanceof ILcdRaster || aObject instanceof ILcdMultilevelRaster);
    }
  }
}
