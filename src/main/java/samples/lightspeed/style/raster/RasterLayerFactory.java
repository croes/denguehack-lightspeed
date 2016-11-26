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
package samples.lightspeed.style.raster;

import java.awt.image.ColorModel;
import java.util.Enumeration;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdColorModelFactory;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdIndexColorModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import samples.lightspeed.decoder.raster.MixedRasterLayerFactory;

/**
 * Layer factory for the raster style sample.
 */
public class RasterLayerFactory extends ALspSingleLayerFactory {

  public static boolean canCreateLayersForModel(ILcdModel aModel) {
    return samples.gxy.common.layers.factories.RasterLayerFactory.canCreateLayers(aModel) ||
           (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor);
  }

  public boolean canCreateLayers(ILcdModel aModel) {
    return canCreateLayersForModel(aModel);
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    return createRasterLayer(aModel, null);
  }

  /**
   * Creates a raster layer that is merged into the view's terrain layer.
   * @param aModel the raster model
   * @return the layer
   */
  public ILspLayer createBackgroundLayer(ILcdModel aModel) {
    return createRasterLayer(aModel, ILspLayer.LayerType.BACKGROUND);
  }

  private ILspLayer createRasterLayer(ILcdModel aModel, ILspLayer.LayerType aLayerType) {
    if (!canCreateLayers(aModel)) {
      return null;
    }

    ILspEditableStyledLayer layer = TLspRasterLayerBuilder
        .newBuilder()
        .model(aModel)
        .label(aModel.getModelDescriptor().getDisplayName())
        .layerType(aLayerType)
        .styler(TLspPaintRepresentationState.REGULAR_BODY, getStyler(aModel))
        .build();
    layer.setLabel("[" + layer.getLayerType().toString().substring(0, 1) + "] " + layer.getLabel());
    return layer;
  }

  private RasterStyler getStyler(ILcdModel aModel) {
    RasterStyler styler = new RasterStyler();
    if (isElevationOnly(aModel)) {
      styler.setColorMap(findColorMap(aModel));
    }
    return styler;
  }

  /**
   * Returns whether the specified model only contains elevation data.
   *
   * @param aModel a model
   *
   * @return {@code true} if {@code aModel} only contains elevation data
   */
  private static boolean isElevationOnly(ILcdModel aModel) {
    return MixedRasterLayerFactory.containsElevationData(aModel) && !MixedRasterLayerFactory.containsImageData(aModel);
  }

  /**
   * Retrieves the color map from a color model.
   *
   * @param aColorModel the color model
   *
   * @return the color map from {@code aColorModel} or the default color map
   */
  public static TLcdColorMap getColorMap(ColorModel aColorModel) {
    if (aColorModel instanceof TLcdIndexColorModel) {
      return ((TLcdIndexColorModel) aColorModel).getColorMap();
    }
    return createDefaultElevationColorMap();
  }

  /**
   * Creates a color map for elevation data in meters (ex. DMED/DTED).<p>
   * <p/>
   * Note that such a coloring could also be applied to Grib data (e.g.
   * pressure, temperature, ...). It would however not make sense to apply such
   * coloring to a true satellite picture.
   *
   * @return default color map
   */
  public static TLcdColorMap createDefaultElevationColorMap() {
    return TLcdColorModelFactory.createElevationColorMap();
  }

  /**
   * Find a {@code TLcdColorMap} that was added by the decoder that created
   * this model. It contains useful detailed information about the data (e.g.
   * bit size per pixel).
   *
   * @param aModel The model to search through.
   *
   * @return The existing color map if one could be found, the default otherwise
   */
  public static TLcdColorMap findColorMap(ILcdModel aModel) {
    Enumeration elements = aModel.elements();
    while (elements.hasMoreElements()) {
      Object object = elements.nextElement();
      ColorModel colorModel = null;
      if (object instanceof ILcdRaster) {
        ILcdRaster raster = (ILcdRaster) object;
        colorModel = raster.getColorModel();
      } else if (object instanceof ILcdMultilevelRaster) {
        ILcdMultilevelRaster multilevel_raster = (ILcdMultilevelRaster) object;
        int index = 0;
        while (index < multilevel_raster.getRasterCount() && colorModel == null) {
          colorModel = multilevel_raster.getRaster(index).getColorModel();
          index++;
        }
      }
      if (colorModel != null) {
        return getColorMap(colorModel);
      }
    }
    return createDefaultElevationColorMap();
  }
}
