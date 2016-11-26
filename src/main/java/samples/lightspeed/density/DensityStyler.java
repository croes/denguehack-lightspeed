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
package samples.lightspeed.density;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspSoftDensityStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspDensityLineStyle;
import com.luciad.view.lightspeed.style.TLspDensityPointStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler implementation for the density plot sample. This styler maintains a single {@code
 * TLspDensityStyle} for every {@link DensityStyleType}. It maintains a current {@link
 * DensityStyleType} and corresponding density style. This density style's width and hardness can be
 * modified using the appropriate setters. Updating the {@link DensityStyleType} restores the last
 * density style for that type, if it exists. Else the default density style for that particular
 * type is created.
 */
public final class DensityStyler extends ALspStyler {

  //Default hardness
  private static final double DEFAULT_HARDNESS = 0.5;
  //Map storing the latest density style used for every
  //DensityStyleType. This ensures that users can return
  //to the latest state for every DensityStyleType.
  private final Map<DensityStyleType, ALspSoftDensityStyle> fStylesMap;
  //Style target provider
  private final ALspStyleTargetProvider fProvider;
  //The current density style
  private ALspSoftDensityStyle fCurrentStyle;
  //The current density style, wrapped in a list.
  private List<ALspStyle> fStyles;
  //The type of the current style.
  private DensityStyleType fStyleType;

  /**
   * Initializes a default density styler.
   *
   * @param aStyleType the initial style type
   */
  public DensityStyler(DensityStyleType aStyleType) {
    this(aStyleType, null);
  }

  /**
   * Initializes the density styler with the given style type and style target provider.
   *
   * @param aStyleType the initial style type
   * @param aProvider  the default style target provider, can be null
   */
  public DensityStyler(DensityStyleType aStyleType, ALspStyleTargetProvider aProvider) {
    fStylesMap = new HashMap<DensityStyleType, ALspSoftDensityStyle>();
    setDensityStyleType(aStyleType);
    fProvider = aProvider;
  }

  /**
   * Returns the density style type of the current style.
   *
   * @return the density style type of the current style.
   */
  public DensityStyleType getDensityStyleType() {
    return fStyleType;
  }

  /**
   * Updates the style type and changes the density style to correspond to the new type. The old
   * density style is stored for the old type. If a density style has been stored for the
   * <code>aStyleType</code> it is restored. If not a new density style is created with the default
   * width value of that type.
   *
   * @param aStyleType the new style type.
   */
  public void setDensityStyleType(DensityStyleType aStyleType) {
    fStylesMap.put(fStyleType, fCurrentStyle);
    fStyleType = aStyleType;
    if (fStylesMap.containsKey(aStyleType)) {
      fCurrentStyle = fStylesMap.get(aStyleType);
    } else {
      ALspSoftDensityStyle.Builder builder = fStyleType.isPointStyle() ?
                                             TLspDensityPointStyle.newBuilder() : TLspDensityLineStyle.newBuilder();
      if (fStyleType.isWorldSize()) {
        builder.worldSize(fStyleType.getDefaultSize());
      } else {
        builder.pixelSize(fStyleType.getDefaultSize());
      }
      builder.hardness(DEFAULT_HARDNESS);
      fCurrentStyle = builder.build();
    }
    updateList();
    fireStyleChangeEvent();
  }

  /**
   * Updates the fStyles field.
   */
  private void updateList() {
    fStyles = Collections.singletonList((ALspStyle) fCurrentStyle);
  }

  /**
   * Creates a new density style with a different hardness value.
   *
   * @param aHardness the new hardness value
   */
  private void updateHardness(double aHardness) {
    fCurrentStyle = getBuilder().hardness(aHardness).build();
    updateList();
  }

  /**
   * Creates a new density style with a different width value.
   *
   * @param aWidth the new size value
   */
  private void updateWidth(double aWidth) {
    if (fCurrentStyle.isWorldSize()) {
      fCurrentStyle = getBuilder().worldSize(aWidth).build();
    } else {
      fCurrentStyle = getBuilder().pixelSize(aWidth).build();
    }
    updateList();
  }

  /**
   * Changes the hardness of the density style returned by this provider.
   *
   * @param aHardness the new hardness value
   */
  public void setHardness(double aHardness) {
    if (aHardness < 0.0f || aHardness > 1.0) {
      throw new IllegalArgumentException("Hardness value should be between 0.0 and 1.0");
    }
    updateHardness(aHardness);
    fireStyleChangeEvent();
  }

  /**
   * Changes the size of the density style returned by this provider.
   *
   * @param aWidth the new width value
   */
  public void setWidth(double aWidth) {
    if (aWidth < fStyleType.getMinimumSize() || aWidth > fStyleType.getMaximumSize()) {
      throw new IllegalArgumentException("Width of the provided style is not in valid interval");
    }
    updateWidth(aWidth);
    fireStyleChangeEvent();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (fProvider == null) {
      aStyleCollector.objects(aObjects).styles(fStyles).submit();
    } else {
      aStyleCollector.geometry(fProvider).objects(aObjects).styles(fStyles).submit();
    }
  }

  /**
   * Returns an ALspSoftDensityStyle.Builder instance initialized with the parameters of the current
   * style
   *
   * @return an ALspSoftDensityStyle.Builder instance initialized with the parameters of the current
   *         style
   */
  private ALspSoftDensityStyle.Builder getBuilder() {
    if (fStyleType.isPointStyle()) {
      return ((TLspDensityPointStyle) fCurrentStyle).asBuilder();
    } else {
      return ((TLspDensityLineStyle) fCurrentStyle).asBuilder();
    }
  }

  /**
   * Returns the minimum allowed width of this DensityStyler
   *
   * @return the minimum allowed width of this DensityStyler
   */
  public double getMinimumWidth() {
    return fStyleType.getMinimumSize();
  }

  /**
   * Returns the maximum allowed width of this DensityStyler
   *
   * @return the maximum allowed width of this DensityStyler
   */
  public double getMaximumWidth() {
    return fStyleType.getMaximumSize();
  }

  /**
   * Returns the current hardness of this DensityStyler
   *
   * @return the current hardness of this DensityStyler
   */
  public double getCurrentHardness() {
    return fCurrentStyle.getHardness();
  }

  /**
   * Returns the current width of this DensityStyler
   *
   * @return the current width of this DensityStyler
   */
  public double getCurrentWidth() {
    return fCurrentStyle.getWidth();
  }
}
