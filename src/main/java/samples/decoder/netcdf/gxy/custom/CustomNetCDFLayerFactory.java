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
package samples.decoder.netcdf.gxy.custom;

import java.awt.Color;

import com.luciad.format.netcdf.gxy.TLcdNetCDFGXYPainterProvider;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.painter.TLcdGXYImageIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.decoder.netcdf.gxy.NetCDFLayerFactory;
import samples.earth.util.WeatherUtil;
import samples.gxy.common.AntiAliasedPainter;
import samples.gxy.decoder.raster.multispectral.ImageUtil;

/**
 * NetCDF layer factory that adds customizations like changing the used color map.
 */
public class CustomNetCDFLayerFactory extends NetCDFLayerFactory {

  public CustomNetCDFLayerFactory() {
    this(null, 50, Color.white, 0);
  }

  public CustomNetCDFLayerFactory(ILcdParameterizedIcon aParameterizedIcon, int aSpacing, Color aHaloColor, int aHaloThickness) {
    super(new CustomNetCDFSingleLayerFactory(aParameterizedIcon, aSpacing, aHaloColor, aHaloThickness), Integer.MAX_VALUE);
  }

  public static class CustomNetCDFSingleLayerFactory extends NetCDFSingleLayerFactory {

    private final ILcdParameterizedIcon fParameterizedIcon;
    private final int fSpacing;
    private final Color fHaloColor;
    private final int fHaloThickness;

    public CustomNetCDFSingleLayerFactory(ILcdParameterizedIcon aParameterizedIcon, int aSpacing, Color aHaloColor, int aHaloThickness) {
      fParameterizedIcon = aParameterizedIcon;
      fSpacing = aSpacing;
      fHaloColor = aHaloColor;
      fHaloThickness = aHaloThickness;
    }

    @Override
    protected TLcdNetCDFGXYPainterProvider createPainterProvider(ILcdModel aModel) {
      return new TLcdNetCDFGXYPainterProvider(aModel) {
        @Override
        protected ILcdGXYPainter createImagePainter(ILcdModel aModel) {
          // Create a default color map
          Object netCDFObject = ImageUtil.getImageObject(aModel);

          if (fParameterizedIcon == null) {
            TLcdColorMap defaultColorMap = TLcdNetCDFGXYPainterProvider.createDefaultColorMap(netCDFObject);

            // Create an image operator chain that contains a lookup table operation using a custom color map
            TLcdColorMap colorMap = WeatherUtil.retrieveColorMap(netCDFObject, defaultColorMap);
            TLcdLookupTable lookupTable = TLcdLookupTable.newBuilder().fromColorMap(colorMap).build();
            ALcdImageOperatorChain colorMapLookupOperator = ALcdImageOperatorChain.newBuilder().indexLookup(lookupTable).build();

            // Return an image painter that uses this lookup table
            TLcdGXYImagePainter imagePainter = new TLcdGXYImagePainter();
            imagePainter.setOperatorChain(colorMapLookupOperator);
            imagePainter.setFillOutlineArea(true);
            return imagePainter;
          } else {
            TLcdGXYImageIconPainter imageIconPainter = new TLcdGXYImageIconPainter();
            imageIconPainter.setParameterizedIcon(fParameterizedIcon);
            imageIconPainter.setDeltaX(fSpacing);
            imageIconPainter.setDeltaY(fSpacing);
            imageIconPainter.setHaloColor(fHaloColor);
            imageIconPainter.setHaloThickness(fHaloThickness);
            return new AntiAliasedPainter((ILcdGXYPainter) imageIconPainter);
          }
        }
      };
    }
  }
}
