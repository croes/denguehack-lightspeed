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
package samples.lightspeed.demo.application.data.osm;

import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN;
import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ON_TERRAIN;
import static com.luciad.view.lightspeed.style.TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.TLspWorldSizedLineStyle;

/**
 * To use an icon set, use on of the following:
 * <ul>
 * <li>install "openstreetmap-map-icons-square" Ubuntu package</li>
 * <li>"osm.icons" system property</li>
 * </ul>
 *
 * @since 2012.1
 */
public class OpenStreetMapStyleProvider {

  private final Cache<ALspStyle[]> fIcons = new Cache<ALspStyle[]>();
  private final Cache<ALspStyle[]> fLineStyles = new Cache<ALspStyle[]>();
  private final Cache<ALspStyle[]> fOutlineStyles = new Cache<ALspStyle[]>();
  private final Cache<ALspStyle> fFillStyles = new Cache<ALspStyle>();

  private final OpenStreetMapGeofabrikMapping fGeofabrikMapping;

  private final OpenStreetMapElementStyles.Icon fDefaultIcon = OpenStreetMapStyleUtil.getDefaultIcon();
  private ALspStyle[] fDefaultIconStyle = null;

  private final OpenStreetMapElementStyles fElemStyles;

  private final Map<ALspStyle, ALspStyle> fWorld2FixedLineStyleMap = new HashMap<ALspStyle, ALspStyle>();

  public OpenStreetMapStyleProvider() {
    fGeofabrikMapping = OpenStreetMapGeofabrikMapping.load();
    fElemStyles = OpenStreetMapElementStyles.load();
    OpenStreetMapStyleUtil.load();
  }

  private static final ALspStyle[] NULL_STYLE = new ALspStyle[]{};

  public ALspStyle[] getIcon(ILcdDataObject aDataObject) {
    int code = (Integer) aDataObject.getValue("code");
    ALspStyle[] result = fIcons.get(code);
    if (result == NULL_STYLE) {
      return null;
    }
    if (result == null) {
      String[] kv = fGeofabrikMapping.getKeyValue(Integer.toString(code), (String) aDataObject.getValue("fclass"));
      OpenStreetMapElementStyles.Icon icon = fElemStyles.getIcon(kv[0], kv[1]);
      if (icon == null) {
        fIcons.put(code, NULL_STYLE);
        return null;
      }
      String path = OpenStreetMapStyleUtil.getIconPath(icon);
      ILcdIcon imageIcon = null;
      if (path == null) {
        if (fDefaultIconStyle != null) {
          fIcons.put(code, fDefaultIconStyle);
          return fDefaultIconStyle;
        }
        icon = fDefaultIcon;
        path = OpenStreetMapStyleUtil.getIconPath(icon);
        imageIcon = createSvgIcon(OpenStreetMapStyleUtil.getIconPath(icon), OpenStreetMapIconColorProvider.getColor(path), 8, 8);
       /*  fIcons.put( code, null);
         return null;*/
      }
      if (imageIcon == null) {
        if (path.toLowerCase().endsWith(".svg")) {
          try {
            ILcdIcon icon1 = createSvgIcon(path, OpenStreetMapIconColorProvider.getColor(path), 32, 32);
            imageIcon = icon1;
          } catch (Exception e) {
            if (fDefaultIconStyle != null) {
              fIcons.put(code, fDefaultIconStyle);
              return fDefaultIconStyle;
            }
            icon = fDefaultIcon;
            imageIcon = createSvgIcon(OpenStreetMapStyleUtil.getIconPath(icon), OpenStreetMapIconColorProvider.getColor(path), 8, 8);
          }
        } else {
          if (fDefaultIconStyle != null) {
            fIcons.put(code, fDefaultIconStyle);
            return fDefaultIconStyle;
          }
          icon = fDefaultIcon;
          imageIcon = createSvgIcon(OpenStreetMapStyleUtil.getIconPath(icon), OpenStreetMapIconColorProvider.getColor(path), 8, 8);
        }

      }
      if (imageIcon != null) {
        result = new ALspStyle[]{TLspIconStyle.newBuilder()
                                              .icon(imageIcon)
                                              .scalingMode(WORLD_SCALING_CLAMPED)
                                              .worldSize(50)
                                              .elevationMode(ABOVE_TERRAIN)
                                              .zOrder(RoadUtil.NR_ROAD_PRIORITIES * 2).build(),
                                 TLspViewDisplacementStyle.newBuilder().viewDisplacement(imageIcon.getIconWidth() / 2, imageIcon.getIconHeight() / 2).build()
        };
        if (icon == fDefaultIcon && fDefaultIconStyle == null) {
          fDefaultIconStyle = result;
        }
      }
      fIcons.put(code, result == null ? NULL_STYLE : result);
    }
    return result;
  }

  private ILcdIcon createSvgIcon(String aPath, Color aColor, int i, int i1) {
    final OSMSVGIcon icon = new OSMSVGIcon(aPath, aColor, i, i1);
    icon.setPaintGlow(false);
    return new IconHalo(icon);
  }

  public ALspStyle[] getSelectionIcon(ILcdDataObject aDataObject) {
    return getIcon(aDataObject);
  }

  public ALspStyle[] getLineStyle(ILcdDataObject aDataObject) {
    int code = (Integer) aDataObject.getValue("code");
    ALspStyle[] result = fLineStyles.get(code);
    if (result == null) {
      String[] kv = fGeofabrikMapping.getKeyValue(Integer.toString(code), (String) aDataObject.getValue("fclass"));
      OpenStreetMapElementStyles.Line line = fElemStyles.getLine(kv[0], kv[1]);
      if (line == null) {
        line = OpenStreetMapStyleUtil.getDefaultLine();
      }

      int priority = RoadUtil.getRoadZOrder(code);
      int zOrder = 2 * RoadUtil.NR_ROAD_PRIORITIES - priority;

      TLspWorldSizedLineStyle mainStyle = newStrokeStyle(line, zOrder);
      ALspStyle outlineStyle = mainStyle.asBuilder()
                                        .width(mainStyle.getWidth() * 1.4f)
                                        .color(mainStyle.getColor().darker())
                                        .elevationMode(mainStyle.getElevationMode())
                                        .zOrder(mainStyle.getZOrder() - RoadUtil.NR_ROAD_PRIORITIES).build();
      result = new ALspStyle[]{mainStyle, outlineStyle};
      fLineStyles.put(code, result);
    }
    return result;
  }

  public ALspStyle[] getOutlineStyle(ILcdDataObject aDataObject) {
    int code = (Integer) aDataObject.getValue("code");
    ALspStyle[] result = fOutlineStyles.get(code);
    if (result == null) {
      String[] kv = fGeofabrikMapping.getKeyValue(Integer.toString(code), (String) aDataObject.getValue("fclass"));
      OpenStreetMapElementStyles.Line line = fElemStyles.getLine(kv[0], kv[1]);
      if (line == null) {
        OpenStreetMapElementStyles.Area area = fElemStyles.getArea(kv[0], kv[1]);
        if (area == null) {
          area = OpenStreetMapStyleUtil.getDefaultArea();
        }
        line = new OpenStreetMapElementStyles.Line();
        line.fColor = area.fColor.darker();
        line.fRealWidth = 1;
        line.fWidth = 1;
      }
      result = new ALspStyle[]{TLspLineStyle.newBuilder()
                                            .color(line.fColor)
                                            .elevationMode(ON_TERRAIN)
                                            .width(line.fWidth)
                                            .zOrder(0)
                                            .build()};
      fOutlineStyles.put(code, result);
    }
    return result;
  }

  public ALspStyle getFillStyle(ILcdDataObject aDataObject) {
    int code = (Integer) aDataObject.getValue("code");
    ALspStyle result = fFillStyles.get(code);
    if (result == null) {
      String[] kv = fGeofabrikMapping.getKeyValue(Integer.toString(code), (String) aDataObject.getValue("fclass"));
      OpenStreetMapElementStyles.Area area = fElemStyles.getArea(kv[0], kv[1]);
      if (area == null) {
        area = OpenStreetMapStyleUtil.getDefaultArea();
      }
      result = TLspFillStyle.newBuilder()
                            .color(area.fColor)
                            .opacity(0.4f)
                            .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                            .build();
      fFillStyles.put(code, result);
    }
    return result;
  }

  public ALspStyle[] convertToFixedWidthLineStyle(ALspStyle[] aWorldSizedLineStyles) {
    ALspStyle[] fixedWidthStyles = new ALspStyle[aWorldSizedLineStyles.length];
    for (int i = 0; i < aWorldSizedLineStyles.length; i++) {
      if (aWorldSizedLineStyles[i] instanceof TLspWorldSizedLineStyle) {
        fixedWidthStyles[i] = convertToFixedWidthLineStyle((TLspWorldSizedLineStyle) aWorldSizedLineStyles[i]);
      } else {
        fixedWidthStyles[i] = aWorldSizedLineStyles[i];
      }
    }
    return fixedWidthStyles;
  }

  public ALspStyle convertToFixedWidthLineStyle(TLspWorldSizedLineStyle aWorldSizedLineStyle) {
    TLspLineStyle lineStyle = (TLspLineStyle) fWorld2FixedLineStyleMap.get(aWorldSizedLineStyle);
    if (fWorld2FixedLineStyleMap.get(aWorldSizedLineStyle) == null) {
      double lineWidth = Math.max(1, aWorldSizedLineStyle.getWidth() / 50);
      lineStyle = TLspLineStyle.newBuilder()
                               .width(lineWidth)
                               .zOrder(aWorldSizedLineStyle.getZOrder())
                               .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                               .color((aWorldSizedLineStyle).getColor()).build();
      fWorld2FixedLineStyleMap.put(aWorldSizedLineStyle, lineStyle);
    }
    return lineStyle;
  }

  static TLspWorldSizedLineStyle newStrokeStyle(OpenStreetMapElementStyles.Line aLine, int aZOrder) {
    TLspWorldSizedLineStyle worldStyle = TLspWorldSizedLineStyle.newBuilder()
                                                                .width(aLine.fRealWidth * 2f)
                                                                .zOrder(aZOrder)
                                                                .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                                .color(aLine.fColor).build();

    // builder.setSelectedLineWidth(aLine.realWidth * 3.0f);
    // builder.setSelectedColor(YELLOW);
    if (aLine.fDashed) {
      // Stroking is not supported yet for world-sized lines.
      //worldStyle.setStroking(new float[] { 2*aLine.dashX, 2*aLine.dashY });
    }
    return worldStyle;
  }

  private static class Cache<T> {

    private Map<Integer, T> fCache = new HashMap<Integer, T>();

    public void put(int aCode, T aObject) {
      fCache.put(aCode, aObject);
    }

    public T get(int aCode) {
      return fCache.get(aCode);
    }
  }
}
