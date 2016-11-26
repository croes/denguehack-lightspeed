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

import java.awt.Color;
import java.awt.image.ColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.util.ELcdInterpolationType;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * Styler that submits {@link TLspRasterStyle} objects.
 * <p/>
 * The styler is configurable and fires an event when it is modified.
 */
public class RasterStyler extends ALspStyler implements ILspCustomizableStyler {

  private enum Style {
    RASTER,
    BOUNDS_OUTLINE,
    BOUNDS_FILL
  }

  private Map<Style, TLspCustomizableStyle> fCustomizableStyles = new HashMap<>();
  private RasterFilter fFilter;
  private List<ALspStyle> fStyleList;

  public RasterStyler() {
    this(1.f);
  }

  public RasterStyler(float aOpacity) {
    this(TLspRasterStyle.newBuilder().opacity(aOpacity).build());
  }

  public RasterStyler(TLspRasterStyle aRasterStyle) {
    fCustomizableStyles.put(Style.RASTER, new TLspCustomizableStyle(aRasterStyle, true));
    fCustomizableStyles.put(Style.BOUNDS_OUTLINE, new TLspCustomizableStyle(TLspLineStyle.newBuilder().color(Color.RED).elevationMode(ElevationMode.ON_TERRAIN).build(), true));
    fCustomizableStyles.put(Style.BOUNDS_FILL, new TLspCustomizableStyle(TLspFillStyle.newBuilder().color(Color.RED).stipplePattern(TLspFillStyle.StipplePattern.HATCHED).elevationMode(ElevationMode.ON_TERRAIN).build(), true));
    PropertyChangeListener listener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        invalidateStyles();
      }
    };
    for (TLspCustomizableStyle style : fCustomizableStyles.values()) {
      style.addPropertyChangeListener(listener);
    }
    invalidateStyles();
  }

  public void setBrightness(float aBrightness) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      customizableStyle.setStyle(((TLspRasterStyle) customizableStyle.getStyle()).asBuilder().brightness(aBrightness).build());
    }
    invalidateStyles();
  }

  public void setContrast(float aContrast) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      customizableStyle.setStyle(((TLspRasterStyle) customizableStyle.getStyle()).asBuilder().contrast(aContrast).build());
    }
    invalidateStyles();
  }

  public void setOpacity(float aOpacity) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      customizableStyle.setStyle(((TLspRasterStyle) customizableStyle.getStyle()).asBuilder().opacity(aOpacity).build());
    }
    invalidateStyles();
  }

  public void setInterpolationType(ELcdInterpolationType aInterpolationType) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      customizableStyle.setStyle(((TLspRasterStyle) customizableStyle.getStyle()).asBuilder().interpolation(aInterpolationType).build());
    }
    invalidateStyles();
  }

  public float getOpacity() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      return ((TLspRasterStyle) customizableStyle.getStyle()).getModulationColor().getAlpha() / 255f;
    }
    return 1.0f;
  }

  public float getBrightness() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      return ((TLspRasterStyle) customizableStyle.getStyle()).getBrightness();
    }
    return 1;
  }

  public float getContrast() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      return ((TLspRasterStyle) customizableStyle.getStyle()).getContrast();
    }
    return 1;
  }

  public ELcdInterpolationType getInterpolationType() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      return ((TLspRasterStyle) customizableStyle.getStyle()).getInterpolationType();
    }
    return null;
  }

  public void setColorMap(TLcdColorMap aColorMap) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      customizableStyle.setStyle(((TLspRasterStyle) customizableStyle.getStyle()).asBuilder().colorMap(aColorMap).build());
    }
    invalidateStyles();
  }

  public TLcdColorMap getColorMap() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.RASTER);
    if ( customizableStyle.getStyle() instanceof TLspRasterStyle) {
      TLcdColorMap colorMap = ((TLspRasterStyle) customizableStyle.getStyle()).getColorMap();
      if (colorMap != null) {
        return colorMap;
      }
      ColorModel colorModel = ((TLspRasterStyle) customizableStyle.getStyle()).getColorModel();
      if (colorModel != null) {
        return RasterLayerFactory.getColorMap(colorModel);
      }
    }
    return null;
  }

  public Color getBoundsColor() {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.BOUNDS_OUTLINE);
    if ( customizableStyle.getStyle() instanceof TLspLineStyle) {
      return ((TLspLineStyle) customizableStyle.getStyle()).getColor();
    }
    return null;
  }

  public void setBoundsColor(Color aColor) {
    TLspCustomizableStyle customizableStyle = fCustomizableStyles.get(Style.BOUNDS_OUTLINE);
    if ( customizableStyle.getStyle() instanceof TLspLineStyle) {
      customizableStyle.setStyle(((TLspLineStyle) customizableStyle.getStyle()).asBuilder().color(aColor).build());
    }
    customizableStyle = fCustomizableStyles.get(Style.BOUNDS_FILL);
    if ( customizableStyle.getStyle() instanceof TLspFillStyle) {
      customizableStyle.setStyle(((TLspFillStyle) customizableStyle.getStyle()).asBuilder().color(aColor).build());
    }
    invalidateStyles();
  }

  public RasterFilter getFilter() {
    return fFilter;
  }

  public void setFilter(RasterFilter aFilter) {
    if (fFilter == null ? aFilter != null : !fFilter.equals(aFilter)) {
      fFilter = aFilter;
      invalidateStyles();
    }
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).styles(getStyleList()).submit();
  }

  public Collection<TLspCustomizableStyle> getStyles() {
    return fCustomizableStyles.values();
  }

  private List<ALspStyle> getStyleList() {
    if (fStyleList == null) {
      ArrayList<ALspStyle> styles = new ArrayList<ALspStyle>(fCustomizableStyles.size());
      for (TLspCustomizableStyle style : fCustomizableStyles.values()) {
        styles.add(style.getStyle());
      }
      if (fFilter != null && fFilter.getStyle() != null) {
        styles.add(fFilter.getStyle());
      }
      fStyleList = styles;
    }
    return fStyleList;
  }

  private void invalidateStyles() {
    fStyleList = null;
    fireStyleChangeEvent();
  }
}
