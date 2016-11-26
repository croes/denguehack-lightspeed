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
package samples.encoder.kml22.converter;

import java.awt.Color;

import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.style.ELcdKML22ColorMode;
import com.luciad.format.kml22.model.style.ELcdKML22StyleState;
import com.luciad.format.kml22.model.style.TLcdKML22IconStyle;
import com.luciad.format.kml22.model.style.TLcdKML22LabelStyle;
import com.luciad.format.kml22.model.style.TLcdKML22LineStyle;
import com.luciad.format.kml22.model.style.TLcdKML22Pair;
import com.luciad.format.kml22.model.style.TLcdKML22PolyStyle;
import com.luciad.format.kml22.model.style.TLcdKML22Style;
import com.luciad.format.kml22.model.style.TLcdKML22StyleMap;

/**
 * Utility class to build kml styles.
 */
public final class KML22StyleUtil {

  private KML22StyleUtil() {
  }

  /**
   * Create a new label style.
   *
   * @param aID the style's ID
   * @param aColor the label color
   * @param aMode the color mode
   * @param aScale the scale
   *
   * @return a {@link TLcdKML22LabelStyle} instance.
   */
  public static TLcdKML22LabelStyle createLabelStyle(String aID,
                                                     Color aColor,
                                                     ELcdKML22ColorMode aMode,
                                                     double aScale) {
    TLcdKML22LabelStyle labelStyle = new TLcdKML22LabelStyle(TLcdKML22DataTypes.LabelStyleType);
    labelStyle.setId(aID);
    labelStyle.setColor(aColor);
    labelStyle.setColorMode(aMode);
    labelStyle.setScale(aScale);
    return labelStyle;
  }

  /**
   * Create a new line style.
   *
   * @param aID the style's ID
   * @param aColor the line color
   * @param aMode the color mode, either {@link ELcdKML22ColorMode#NORMAL} or {@link ELcdKML22ColorMode#RANDOM}
   * @param aWidth the line width in pixels
   * @return a {@link TLcdKML22LineStyle} instance.
   */
  public static TLcdKML22LineStyle createLineStyle(String aID,
                                                   Color aColor,
                                                   ELcdKML22ColorMode aMode,
                                                   double aWidth) {
    TLcdKML22LineStyle lineStyle = new TLcdKML22LineStyle(TLcdKML22DataTypes.LineStyleType);
    lineStyle.setId(aID);
    lineStyle.setColor(aColor);
    lineStyle.setColorMode(aMode);
    lineStyle.setWidth(aWidth);
    return lineStyle;
  }

  /**
   * Create a new poly style.
   *
   * @param aID the style's ID
   * @param aColor the style color
   * @param aMode the color mode, either {@link ELcdKML22ColorMode#NORMAL} or {@link ELcdKML22ColorMode#RANDOM}
   * @param aFill whether to fill the polygon or not
   * @param aOutline whether to outline the polygon or not
   * @return a {@link TLcdKML22Style} instance.
   */
  public static TLcdKML22PolyStyle createPolyStyle(String aID,
                                                   Color aColor,
                                                   ELcdKML22ColorMode aMode,
                                                   boolean aFill,
                                                   boolean aOutline) {
    TLcdKML22PolyStyle polyStyle = new TLcdKML22PolyStyle(TLcdKML22DataTypes.PolyStyleType);
    polyStyle.setId(aID);
    polyStyle.setColor(aColor);
    polyStyle.setColorMode(aMode);
    polyStyle.setFill(aFill);
    polyStyle.setOutline(aOutline);
    return polyStyle;
  }

  /**
   * Create a style.
   *
   * @param aID the style's ID
   * @param aIconStyle an optional icon style
   * @param aLineStyle an optional line style
   * @param aPolyStyle an optional poly style
   * @param aLabelStyle an optional label style
   * @return a {@link TLcdKML22Style} instance.
   */
  public static TLcdKML22Style createStyle(String aID,
                                           TLcdKML22IconStyle aIconStyle,
                                           TLcdKML22LineStyle aLineStyle,
                                           TLcdKML22PolyStyle aPolyStyle,
                                           TLcdKML22LabelStyle aLabelStyle) {
    TLcdKML22Style style = new TLcdKML22Style(TLcdKML22DataTypes.StyleType);
    style.setId(aID);
    if (aIconStyle != null) {
      style.setIconStyle(aIconStyle);
    }
    if (aLineStyle != null) {
      style.setLineStyle(aLineStyle);
    }
    if (aPolyStyle != null) {
      style.setPolyStyle(aPolyStyle);
    }
    if (aLabelStyle != null) {
      style.setLabelStyle(aLabelStyle);
    }
    return style;
  }

  /**
   * Create a new style map.
   *
   * @param aID the style map ID. May be {@code null} or empty.
   * @param aNormalStyleUrl the URL of the normal style, eg "#normalStyle" for local shared style
   * @param aHighLightStyleUrl the URL of the highlight style, eg "files/someKML.kml#someStyle" for non local style
   * @return a {@link TLcdKML22StyleMap} instance.
   * @throws IllegalArgumentException if either aNormalStyleUrl or aHighLightStyleUrl is {@code null} or empty.
   */
  public static TLcdKML22StyleMap createStyleMap(String aID, String aNormalStyleUrl, String aHighLightStyleUrl) {
    if (aNormalStyleUrl == null || aNormalStyleUrl.isEmpty()) {
      throw new IllegalArgumentException("StyleMap's normal styleUrl MUST be a non-empty string");
    }
    if (aHighLightStyleUrl == null || aHighLightStyleUrl.isEmpty()) {
      throw new IllegalArgumentException("StyleMap's highlight styleUrl MUST be a non-empty string");
    }
    TLcdKML22StyleMap styleMap = new TLcdKML22StyleMap(TLcdKML22DataTypes.StyleMapType);
    styleMap.setId(aID);

    TLcdKML22Pair normal = new TLcdKML22Pair(TLcdKML22DataTypes.PairType);
    normal.setId(aID + "-normal");
    normal.setKey(ELcdKML22StyleState.NORMAL);
    normal.setStyleUrl(aNormalStyleUrl);
    styleMap.getPair().add(normal);

    TLcdKML22Pair highlight = new TLcdKML22Pair(TLcdKML22DataTypes.PairType);
    highlight.setId(aID + "-highlight");
    highlight.setKey(ELcdKML22StyleState.HIGHLIGHT);
    highlight.setStyleUrl(aHighLightStyleUrl);
    styleMap.getPair().add(highlight);

    return styleMap;
  }
}

