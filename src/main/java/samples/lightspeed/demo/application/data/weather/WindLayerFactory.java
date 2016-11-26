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
package samples.lightspeed.demo.application.data.weather;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.luciad.earth.view.gxy.util.ALcdEarth2DVectorIcon;
import com.luciad.earth.view.gxy.util.TLcdEarthArrowIcon;
import com.luciad.format.netcdf.lightspeed.TLspNetCDFLayerBuilder;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.TLspParameterizedRasterIconStyle;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;


class WindLayerFactory {

  public ILspLayer createLayer(ILcdModel aModel) {
    ILspEditableStyledLayer windLayer = TLspNetCDFLayerBuilder.newBuilder()
                                                              .model(aModel)
                                                              .build();
    windLayer.setLabel("Weather Wind");
    return customizedIcons(windLayer);
  }

  private ILspLayer customizedIcons(ILspEditableStyledLayer aWindLayer) {
    ILspStyler styler = aWindLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL);
    if (styler instanceof ILspCustomizableStyler) {
      ILspCustomizableStyler customizableStyler = (ILspCustomizableStyler) styler;
      for (TLspCustomizableStyle customizableStyle : customizableStyler.getStyles()) {
        if (customizableStyle.getStyle() instanceof TLspParameterizedRasterIconStyle) {
          TLspParameterizedRasterIconStyle iconStyle = (TLspParameterizedRasterIconStyle) customizableStyle.getStyle();
          TLcdEarthArrowIcon newIcon = new ScaleWithStrengthEarthArrowIcon(ALcdEarth2DVectorIcon.Parameterization.STRENGTH_2D, createTransparentWhiteColorMap());
          TLspParameterizedRasterIconStyle newStyle = TLspParameterizedRasterIconStyle
                                                        .newBuilder()
                                                        .all(iconStyle)
                                                        .icon(newIcon)
                                                        .spacing(50)
                                                        .build();
          customizableStyle.setStyle(newStyle);
        }
      }
    }
    return aWindLayer;
  }

  private TLcdColorMap createTransparentWhiteColorMap() {
    return new TLcdColorMap(new TLcdInterval(0, 28),
                            new double[] {0, 28},
                            new Color[] {new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 0), Color.WHITE});
  }

  private static class ScaleWithStrengthEarthArrowIcon extends TLcdEarthArrowIcon {

    private static final double WIND_STRENGTH_SCALE_FACTOR = 16;

    public ScaleWithStrengthEarthArrowIcon(Parameterization aParameterization, TLcdColorMap aColorMap) {
      super(aParameterization, aColorMap);
    }

    @Override
    public int getHeight(double[] aParameters) {
      return (int) (super.getHeight(aParameters) * getScaleFactor(aParameters));
    }

    @Override
    public int getWidth(double[] aParameters) {
      return (int) (super.getWidth(aParameters) * getScaleFactor(aParameters));
    }

    @Override
    public void paintIcon(Graphics aGraphics, int aX, int aY, double[] aParameters) {
      Graphics newGraphics = aGraphics.create();
      Graphics2D graphics2D = (Graphics2D) newGraphics;
      double scaleFactor = getScaleFactor(aParameters);
      graphics2D.scale(scaleFactor, scaleFactor);
      super.paintIcon(graphics2D, aX, aY, aParameters);
    }

    private double getScaleFactor(double[] aParameters) {
      return Math.min(getStrength(aParameters) / WIND_STRENGTH_SCALE_FACTOR, 3);
    }

  }

}
