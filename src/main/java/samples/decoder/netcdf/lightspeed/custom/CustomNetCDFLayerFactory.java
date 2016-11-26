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
package samples.decoder.netcdf.lightspeed.custom;

import static samples.earth.util.WeatherUtil.isWeatherSingleModel;

import java.awt.Color;

import com.luciad.format.netcdf.lightspeed.TLspNetCDFLayerBuilder;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspParameterizedRasterIconStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.decoder.netcdf.lightspeed.NetCDFLayerFactory;
import samples.earth.util.WeatherUtil;
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

  public static class CustomNetCDFSingleLayerFactory extends ALspSingleLayerFactory {

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
    public boolean canCreateLayers(ILcdModel aModel) {
      return isWeatherSingleModel(aModel);
    }

    @Override
    public ILspLayer createLayer(ILcdModel aModel) {
      ILspStyler styler;
      if (fParameterizedIcon == null) {
        // Create a default color map
        Object netCDFObject = ImageUtil.getImageObject(aModel);
        TLcdColorMap defaultColorMap = TLspNetCDFLayerBuilder.createDefaultColorMap(netCDFObject);

        // Create a styler using an image operator chain that contains a lookup table operation using a custom color map
        TLcdColorMap colorMap = WeatherUtil.retrieveColorMap(netCDFObject, defaultColorMap);
        styler = createStyler(colorMap);
      } else {
        styler = TLspParameterizedRasterIconStyle.newBuilder()
                                                 .icon(fParameterizedIcon)
                                                 .spacing(fSpacing)
                                                 .haloColor(fHaloColor)
                                                 .haloThickness(fHaloThickness)
                                                 .build();
      }

      return TLspNetCDFLayerBuilder.newBuilder()
                                   .model(aModel)
                                   .styler(TLspPaintRepresentationState.REGULAR_BODY, styler)
                                   .build();
    }
  }

  private static ILspStyler createStyler(TLcdColorMap aColorMap) {
    TLspLineStyle outlineStyle = TLspLineStyle.newBuilder().color(Color.RED).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    TLspFillStyle fillStyle = TLspFillStyle.newBuilder().color(Color.RED).stipplePattern(TLspFillStyle.StipplePattern.HATCHED).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    TLspRasterStyle rasterStyle = TLspRasterStyle.newBuilder().colorMap(aColorMap).build();
    return new TLspStyler(outlineStyle, fillStyle, rasterStyle);
  }
}
