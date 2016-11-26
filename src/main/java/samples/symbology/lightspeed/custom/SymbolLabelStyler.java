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
package samples.symbology.lightspeed.custom;

import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.OBJECT_DEPENDENT;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.luciad.gui.TLcdImageIcon;
import samples.symbology.common.util.ViewDisplacementUtil;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.shape.ILcdShape;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspOnPathLabelingAlgorithm;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

/**
 * Styler that implements the following customizations for the symbol labels:
 * <ul>
 * <li>Use {@link Symbol} domain objects instead of {@link com.luciad.symbology.milstd2525b.model.ILcdMS2525bShape}</li>
 * <li>Add a country flag label</li>
 * <li>Support custom (i.e. non-MS2525b) symbol codes</li>
 * </ul>
 */
public class SymbolLabelStyler extends SymbolStylerBase {

  private static final Object CUSTOM_SUBLABEL_ID = "CUSTOM_SYMBOL_LABEL";
  private static final Object FLAG_SUBLABEL_ID = "FLAG_LABEL";
  private static final ILspLabelingAlgorithm ON_PATH_ALGORITHM = new TLspOnPathLabelingAlgorithm();

  private static final TLspIconStyle UNKNOWN_FLAG_STYLE = TLspIconStyle.newBuilder().build();

  private HashMap<String, TLspIconStyle> fFlags = new HashMap<String, TLspIconStyle>();
  private TLcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  /**
   * Creates a new symbol styler. The boolean parameter indicates whether the
   * styler will be used for selected or non-selected objects.
   *
   * @param aSelected true if the styler should show objects in selected state
   */
  public SymbolLabelStyler(boolean aSelected) {
    super(aSelected);
  }

  @Override
  protected void styleStandardSymbol(
      Symbol aSymbol,
      boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  ) {
    ILcdShape geometry = aSymbol.getGeometry();
    // Add the standard symbology labels
    TLspMS2525bSymbolStyle symbologyStyle = (TLspMS2525bSymbolStyle) createSymbologyStyle(aSymbol);
    aStyleCollector
        .object(aSymbol)
        .geometry(geometry)
        .style(symbologyStyle)
        .submit();

    if (aInScaleRange) {
      // Add the country flag when zoomed in
      ALspLabelStyleCollector labelStyleCollector = (ALspLabelStyleCollector) aStyleCollector;

      String symbolCode = aSymbol.getSymbolCode();
      String countryCode = symbolCode.substring(12, 14);
      //get the elevation mode of the symbol and use it for flag too
      ILspWorldElevationStyle.ElevationMode elevationMode = ViewDisplacementUtil.getIconElevationModeForIconSymbol(geometry, symbologyStyle.getElevationMode());
      TLspIconStyle flagStyle = getCountryFlag(countryCode, elevationMode);
      if (flagStyle != UNKNOWN_FLAG_STYLE) {
        boolean is3D = aContext.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D;
        //if view is 3D and and the symbol has a 0 altitude, we should apply a displacement to the flag as we do for symbol too
        if (ViewDisplacementUtil.shouldUseViewDisplacement(is3D, geometry, elevationMode)) {
          //get displacement of the flag
          labelStyleCollector.styles(getViewDisplacement(), flagStyle);
        } else {
          labelStyleCollector.styles(flagStyle);
        }
        labelStyleCollector
            .object(aSymbol)
            .geometry(geometry)
            .label(FLAG_SUBLABEL_ID)
            .locations(20, TLspLabelLocationProvider.Location.NORTH_EAST)
            .submit();
      }
    }
  }

  @Override
  protected void styleCustomSymbol(
      Symbol aSymbol,
      final CustomSymbolCodeDescriptor aDescriptor,
      boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  ) {
    if (aInScaleRange) {
      ALspLabelStyleCollector labelStyleCollector = (ALspLabelStyleCollector) aStyleCollector;

      // Add the country flag when zoomed in
      String countryCode = aDescriptor.getCountry();
      ILcdShape geometry = aSymbol.getGeometry();
      //get the elevationmode of the symbol and use it for flag too
      ILspWorldElevationStyle.ElevationMode elevationMode = ViewDisplacementUtil.getIconElevationModeForIconSymbol(geometry, OBJECT_DEPENDENT);
      TLspIconStyle flagStyle = getCountryFlag(countryCode, elevationMode);
      if (flagStyle != UNKNOWN_FLAG_STYLE) {
        labelStyleCollector
            .object(aSymbol)
            .geometry(aSymbol.getGeometry())
            .styles(flagStyle)
            .label(FLAG_SUBLABEL_ID)
            .locations(20, TLspLabelLocationProvider.Location.NORTH_EAST)
            .submit();
      }

      // Determine the affiliation color.
      TLcdDefaultMS2525bStyle style = getStyle();
      Color affiliationColor = style.isAffiliationColorEnabled() ?
                               style.getAffiliationColor(aDescriptor.getAffiliation()) :
                               style.getColor();

      // Add the text embedded in the symbol code
      labelStyleCollector
          .object(aSymbol)
          .geometry(aSymbol.getGeometry())
          .styles(
              new ALspLabelTextProviderStyle() {
                @Override
                public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
                  return new String[]{aDescriptor.getText()};
                }
              },
              TLspTextStyle.newBuilder()
                           .textColor(affiliationColor)
                           .haloThickness(isSelected() ? 2 : 0)
                           .haloColor(SELECTION_COLOR)
                           .build()
          )
          .label(CUSTOM_SUBLABEL_ID)
          .algorithm(ON_PATH_ALGORITHM)
          .submit();
    }
  }

  /**
   * Creates and returns a TLspViewDisplacementStyle for the current style
   * @return Creates and returns a TLspViewDisplacementStyle for the current style
   */
  private TLspViewDisplacementStyle getViewDisplacement() {
    TLcdDefaultMS2525bStyle style = getStyle();
    int offsetX = (int) (style.getSizeSymbol() / 2.0);
    int offsetY = (int) (style.getSizeSymbol() / 2.0);
    return TLspViewDisplacementStyle.newBuilder().viewDisplacement(offsetX, offsetY).build();
  }

  private TLspIconStyle getCountryFlag(String aCountryCode, ILspWorldElevationStyle.ElevationMode aElevationMode) {
    String key = aCountryCode.toLowerCase();
    if (key.contains("*")) {
      return UNKNOWN_FLAG_STYLE;
    }

    TLspIconStyle flag = fFlags.get(key);
    if (flag == null) {
      String sourceName = "images/countryflags/" + key + ".png";
      try {
        BufferedImage image = ImageIO.read(
            fInputStreamFactory.createInputStream(sourceName)
        );
        flag = TLspIconStyle.newBuilder()
                            .icon(new TLcdImageIcon(image))
                            .elevationMode(aElevationMode)
                            .scale(0.5)
                            .build();
      } catch (IOException e) {
        flag = UNKNOWN_FLAG_STYLE;
      }
      fFlags.put(key, flag);
    }
    return flag;
  }
}
