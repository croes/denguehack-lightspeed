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
package samples.gxy.common.layers.factories;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import com.luciad.format.raster.ILcdColorModelFactory;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.util.ELcdInterpolationType;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.gxy.decoder.MapSupport;

/**
 * <i>Important notice: the package {@link com.luciad.imaging} presents a new API
 * for the modeling of raster data. For new projects, it is recommended to use
 * this API instead of {@code ILcdRaster} et al. For visualization in a GXY view,
 * see {@link TLcdGXYImagePainter}</i>
 *<p>
 * This is an example of ILcdGXYLayerFactory for a raster
 * ILcdGXYLayer. It creates and sets up a TLcdGXYLayer
 * for displaying objects contained in an ILcdModel which has a
 * {@link ILcdImageModelDescriptor} or {@link TLcdRasterModelDescriptor} or {@link TLcdMultilevelRasterModelDescriptor}
 *</p>
 *
 * We set the service priority to low so that more specialized (or mixed vector/raster)
 * raster models can be handled by more specialized layer factories.
 */
@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class RasterLayerFactory implements ILcdGXYLayerFactory {

  private final ILcdColorModelFactory fColorModelFactory;
  private boolean fForcePainting = false;
  private boolean fAsynchronousTileRequestAllowed = true;
  private int fWarpBlockSize = 64;
  private float fOpacity = 1.0f;

  public RasterLayerFactory() {
    this(null);
  }

  public RasterLayerFactory(ILcdColorModelFactory aColorModelFactory) {
    fColorModelFactory = aColorModelFactory;
  }

  public static boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor);
  }

  private static boolean isRasterModel(ILcdModel aModel) {
    return canCreateLayers(aModel) &&
           !TLcdModelTreeNodeUtil.isEmptyModel(aModel);
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!isRasterModel(aModel)) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(false);
    layer.setEditable(false);
    layer.setLabeled(false);

    // Set a suitable pen on the layer.
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
    layer.setGXYPainterProvider(createImagePainter());

    return layer;
  }

  private TLcdGXYImagePainter createImagePainter() {
    TLcdGXYImagePainter painter = new TLcdGXYImagePainter();

    painter.setFillOutlineArea(true);

    // The painter should avoid drawing opaque borders around transformed
    // rasters (at a slight performance penalty for rasters with a color map
    // without a transparent index).
    painter.setAvoidOpaqueBorder(true);

    // Various settings to tweak which raster will be painted at which zoom
    // levels. Note that painting very detailed rasters at comparatively low
    // zoom levels can have a bad impact on performance, since a lot of raster
    // data then have to be loaded just to color a few screen pixels.
    //painter.setStartResolutionFactor( 10.0 );
    //painter.setStopResolutionFactor( 0.0 );
    painter.setForcePainting(fForcePainting);
    painter.setWarpBlockSize(fWarpBlockSize);
    painter.setOpacity(fOpacity);

    // You can enable / disable asynchronous tile requests.
    // Enabling asynchronous tile requests is a common default to visualize raster data in applications.
    // In server environments (e.g., raster rendering in an offscreen view, such as in the OGC WMS),
    // synchronous tile requests are generally required.
    painter.setAsynchronousTileRequestAllowed(fAsynchronousTileRequestAllowed);

    //You can specify a color model to be used by the painter, instead of the
    //raster's own color model. This can be useful for getting a different
    //pseudo-coloring of elevation data, for instance.
    if (null != fColorModelFactory) {
      ColorModel colorModel = fColorModelFactory.createColorModel();
      TLcdLookupTable lut = TLcdLookupTable.newBuilder().fromSignedIndexColorModel((IndexColorModel) colorModel).interpolation(ELcdInterpolationType.NONE).build();
      ALcdImageOperatorChain chain = ALcdImageOperatorChain.newBuilder().indexLookup(lut).build();
      painter.setOperatorChain(chain);
    }

    return painter;
  }

  public void setForcePainting(boolean aForcePainting) {
    fForcePainting = aForcePainting;
  }

  public void setAsynchronousTileRequestAllowed(boolean aAsynchronousTileRequestAllowed) {
    fAsynchronousTileRequestAllowed = aAsynchronousTileRequestAllowed;
  }

  public void setWarpBlockSize(int aWarpBlockSize) {
    fWarpBlockSize = aWarpBlockSize;
  }

  public void setOpacity(float aOpacity) {
    fOpacity = aOpacity;
  }

  public float getOpacity() {
    return fOpacity;
  }
}
