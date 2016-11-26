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
package samples.earth.common;

import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdMultilevelRasterPainter;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterPainter;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;

import samples.gxy.decoder.MapSupport;

/**
 * A simple ILcdGXYLayerFactory for raster data. This class supports both
 * single and multilevel rasters. 
 */
public class RasterLayerFactory implements ILcdGXYLayerFactory {
  private boolean fForcePainting;

  /**
   * Constructs a new raster layer factory.
   *
   * @param aForcePainting whether the rasters should always be painted or not
   *
   * @see #isForcePainting()
   */
  public RasterLayerFactory(boolean aForcePainting) {
    fForcePainting = aForcePainting;
  }

  /**
   * Returns whether the rasters are always be painted or only when they are within the default
   * scale range settings.
   * <p/>
   * If this is set to <code>true</code> the rasters will always be painted, even if this requires
   * processing a lot of the raster data (ex. because the raster is completely inside in the view).
   * Enabling this option may reduce the painting performance but ensures the rasters are always
   * painted. If this option is disabled the outline of the raster will be painted when zoomed out
   * a lot.
   *
   * @return whether the rasters are always be painted or not.
   */
  public boolean isForcePainting() {
    return fForcePainting;
  }

  public void setForcePainting(boolean aForcePainting) {
    fForcePainting = aForcePainting;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayerTreeNode layerTreeNode = createLayerTreeNode(aModel);
    if (layerTreeNode == null) {
      return null;
    }
    if (aModel instanceof ILcdModelTreeNode) {
      ILcdModelTreeNode modelTreeNode = (ILcdModelTreeNode) aModel;
      for (int i = 0; i < modelTreeNode.modelCount(); i++) {
        ILcdGXYLayer layer = createGXYLayer(modelTreeNode.getModel(i));
        if (layer == null) {
          return null;
        }
        layerTreeNode.addLayer(layer);
      }
    }
    return layerTreeNode;
  }

  private TLcdGXYLayerTreeNode createLayerTreeNode(ILcdModel aModel) {
    if ((aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) ||
        (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor)) {
      // Create a layer for a standard raster model.
      TLcdGXYLayerTreeNode gxy_layer = new TLcdGXYLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      gxy_layer.setModel(aModel);
      gxy_layer.setSelectable(false);
      gxy_layer.setEditable(false);
      gxy_layer.setLabeled(false);
      gxy_layer.setVisible(true);

      // Set a suitable pen on the layer.
      gxy_layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

      // Create an ILcdGXYPainter to paint rasters.
      ILcdGXYPainterProvider gxy_painter_provider;
      TLcdMultilevelRasterPainter mlrp = new TLcdMultilevelRasterPainter();
      mlrp.setForcePainting(fForcePainting);
      mlrp.setAvoidOpaqueBorder(true);
      mlrp.setWarpBlockSize(8);

      TLcdRasterPainter rp = new TLcdRasterPainter();
      rp.setForcePainting(fForcePainting);
      rp.setAvoidOpaqueBorder(true);
      rp.setWarpBlockSize(8);

      gxy_painter_provider = new RasterPainter(rp, mlrp);

      // Set it as an ILcdGXYPainterProvider on the layer.
      gxy_layer.setGXYPainterProvider(gxy_painter_provider);

      return gxy_layer;
    } else {
      return null;
    }
  }
}
