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
package samples.decoder.netcdf.gxy;

import static java.util.Collections.singleton;

import com.luciad.format.netcdf.TLcdNetCDFModelDescriptor;
import com.luciad.format.netcdf.gxy.TLcdNetCDFGXYPainterProvider;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;
import samples.gxy.decoder.MapSupport;

/**
 * Layer factory for NetCDF models, identified by a {@link TLcdNetCDFModelDescriptor}.
 * It creates tree layers for NetCDF tree models, and single layers for single NetCDF models.
 * <p/>
 * It enables the initial visibility of NetCDF tree layers up to a max.
 * number of visible layers.
 * <p/>
 * This layer factory will be picked up by the {@link samples.gxy.decoder.MainPanel GXY decoder sample}. You can use that
 * sample to load a NetCDF URL.
 *
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class NetCDFLayerFactory extends GXYUnstyledLayerFactory {

  private final ILcdGXYLayerFactory fSingleLayerFactory;
  private final int fMaxVisibleLayers;

  /**
   * Constructs a default NetCDF layer factory with a {@link NetCDFSingleLayerFactory} and max.
   * visible layers of 1.
   */
  public NetCDFLayerFactory() {
    this(new NetCDFSingleLayerFactory(), 1);
  }

  protected NetCDFLayerFactory(ILcdGXYLayerFactory aSingleLayerFactory, int aMaxVisibleLayers) {
    super(singleton(aSingleLayerFactory));
    fSingleLayerFactory = aSingleLayerFactory;
    fMaxVisibleLayers = aMaxVisibleLayers;
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!TLcdNetCDFModelDescriptor.TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName())) {
      return null;
    }
    if (aModel instanceof ILcdModelTreeNode) {
      // Delegate to the superclass to handle NetCDF tree layers.
      ILcdGXYLayer layer = super.createGXYLayer(aModel);
      setVisible(layer, fMaxVisibleLayers);
      return layer;
    } else {
      // Delegate to the single layer factory to handle single NetCDF layers.
      return fSingleLayerFactory.createGXYLayer(aModel);
    }
  }

  public static class NetCDFSingleLayerFactory implements ILcdGXYLayerFactory {

    @Override
    public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
      if (!TLcdNetCDFModelDescriptor.TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName())
          || aModel instanceof ILcdModelTreeNode) {
        return null;
      }

      // Create the layer.
      ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
      final TLcdGXYLayer layer = new TLcdGXYLayer(aModel) {
        @Override
        public boolean isSelectableSupported() {
          return false;
        }
      };
      layer.setSelectable(false);

      // Set a suitable pen on the layer.
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

      // Create and configure the painter.
      TLcdNetCDFGXYPainterProvider painterProvider = createPainterProvider(aModel);
      layer.setGXYPainterProvider(painterProvider);

      return layer;
    }

    protected TLcdNetCDFGXYPainterProvider createPainterProvider(ILcdModel aModel) {
      return new TLcdNetCDFGXYPainterProvider(aModel) {
        @Override
        protected ILcdGXYPainter createImagePainter(ILcdModel aModel) {
          ILcdGXYPainter imagePainter = super.createImagePainter(aModel);
          if (imagePainter instanceof TLcdGXYImagePainter) {
            ((TLcdGXYImagePainter) imagePainter).setFillOutlineArea(true);
          }
          return imagePainter;
        }
      };
    }
  }
}
