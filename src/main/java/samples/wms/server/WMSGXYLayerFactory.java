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
package samples.wms.server;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.format.mif.TLcdMIFModelDescriptor;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdMultilevel2DBoundsIndexedModel;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;
import com.luciad.wms.server.ALcdSLDWMSGXYLayerFactory;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.sld.model.TLcdSLDNamedLayer;
import com.luciad.wms.sld.model.TLcdSLDUserLayer;

import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.gxy.decoder.MapSupport;
import samples.gxy.decoder.custom1.Custom1LayerFactory;
import samples.gxy.decoder.custom1.Custom1ModelDecoder;
import samples.gxy.decoder.mif.MIFLayerFactory;
import samples.gxy.decoder.mtm.BasicMTMLayerFactory;
import samples.ogc.sld.gxy.SLDGXYLayerFactory;

/**
 * This ALcdSLDWMSGXYLayerFactory implementation can create an ILcdGXYLayer for
 * an ILcdModel decoded with a decoder created by the ModelDecoderFactory.
 * This includes models created from a shape file, a raster or decoded
 * with a Custom1ModelDecoder.
 */
public class WMSGXYLayerFactory extends ALcdSLDWMSGXYLayerFactory {

  // ILcdWMSGXYLayerFactory for a SHP model
  private WMSSHPLayerFactory fSHPLayerFactory = new WMSSHPLayerFactory();
  // ILcdGXYLayerFactory for models decoded with a Custom1ModelDecoder
  private Custom1LayerFactory fCustom1LayerFactory = new Custom1LayerFactory();
  // ILcdGXYLayerFactory for user-defined data
  private SLDGXYLayerFactory fSLDLayerFactory = new SLDGXYLayerFactory();
  // ILcdGXYLayerFactory for MIF models
  private MIFLayerFactory fMIFLayerFactory = new MIFLayerFactory();
  // ILcdGXYLayerFactory for multilevel models
  private BasicMTMLayerFactory fMTMLayerFactory = new BasicMTMLayerFactory();
  // ILcdGXYLayerFactory for a raster / image models
  private RasterLayerFactory fRasterLayerFactory = new RasterLayerFactory();
  // ILcdGXYLayerFactory for Earth models
  private EarthLayerFactory fEarthLayerFactory = new EarthLayerFactory();

  public WMSGXYLayerFactory() {
    // We disable asynchronous tile requests in the raster / earth layer factories,
    // to make sure that any requested map is completely rendered before it is sent the client.
    fRasterLayerFactory.setAsynchronousTileRequestAllowed(false);
    fEarthLayerFactory.setAsynchronousTileRequestAllowed(false);
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel, ALcdWMSLayer aALcdWMSLayer, String aStyle) {
    // Note: layer factory method for named layer/named style combination

    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();

    // if it's a SHP model, use the BasicSHPLayerFactory to create the layer
    if (modelDescriptor instanceof TLcdSHPModelDescriptor) {
      return fSHPLayerFactory.createGXYLayer(aModel, aALcdWMSLayer, aStyle, null);
    }

    // if it's an image / raster model, use the RasterLayerFactory to create the layer
    if (modelDescriptor instanceof ILcdImageModelDescriptor ||
        modelDescriptor instanceof TLcdRasterModelDescriptor ||
        modelDescriptor instanceof TLcdMultilevelRasterModelDescriptor) {
      return fRasterLayerFactory.createGXYLayer(aModel);
    }

    // if it's an Earth model, use the EarthLayerFactory to create the layer
    if (modelDescriptor instanceof ILcdEarthModelDescriptor) {
      return fEarthLayerFactory.createGXYLayer(aModel);
    }

    // if it's a custom1 model, use the Custom1LayerFactory to create the layer
    if (Custom1ModelDecoder.TYPE_NAME.equals(modelDescriptor.getTypeName())) {
      return fCustom1LayerFactory.createGXYLayer(aModel);
    }

    // if it's a MIF model, use the MIFLayerFactory to create the layer
    if (modelDescriptor instanceof TLcdMIFModelDescriptor) {
      return fMIFLayerFactory.createGXYLayer(aModel);
    }

    // if it's a multilevel model, BasicMTMLayerFactory to create the layer
    if (aModel instanceof TLcdMultilevel2DBoundsIndexedModel) {
      return fMTMLayerFactory.createGXYLayer(aModel);
    }

    // return null otherwise
    return null;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel, TLcdSLDNamedLayer aNamedLayer,
                                     ALcdWMSLayer aWMSLayer, TLcdSLDFeatureTypeStyle[] aStyle) {
    // Note: layer factory method for named layer/user-defined style combination.
    return fSLDLayerFactory.createGXYLayer(aModel, aStyle);
  }

  public ILcdGXYLayer[] createGXYLayer(ILcdModel[] aModel, TLcdSLDUserLayer aUserLayer,
                                       TLcdSLDFeatureTypeStyle[] aStyle) {
    // Note: layer factory method for user-defined layer/user-defined style combination.
    ILcdGXYLayer[] layers = new ILcdGXYLayer[aModel.length];
    for (int i = 0; i < aModel.length; i++) {
      layers[i] = fSLDLayerFactory.createGXYLayer(aModel[i], aStyle);
    }
    return layers;
  }

  // Layer factory for Earth based models.
  private static class EarthLayerFactory implements ILcdGXYLayerFactory {

    private boolean fAsynchronousTileRequestAllowed = true;

    @Override
    public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
      if (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor) {
        TLcdGXYLayer layer = new TLcdGXYLayer();
        layer.setModel(aModel);
        layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
        TLcdGXYImagePainter painter = new TLcdGXYImagePainter();
        painter.setAsynchronousTileRequestAllowed(fAsynchronousTileRequestAllowed);
        layer.setGXYPainterProvider(painter);
        return layer;
      }
      return null;
    }

    void setAsynchronousTileRequestAllowed(boolean aAsynchronousTileRequestAllowed) {
      fAsynchronousTileRequestAllowed = aAsynchronousTileRequestAllowed;
    }
  }
}
