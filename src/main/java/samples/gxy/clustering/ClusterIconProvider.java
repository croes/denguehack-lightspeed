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
package samples.gxy.clustering;

import java.awt.Color;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.model.transformation.clustering.TLcdCluster;

import samples.common.MapColors;
import samples.common.UIColors;

/**
 * Icon provider that can return an icon for a given cluster.
 */
public class ClusterIconProvider implements ILcdObjectIconProvider {

  private static final double BASE_ICON_SIZE = 25.0;
  private static final double INNER_ICON_SIZE_FACTOR = 0.7;

  public static final Color DEFAULT_FILL_COLOR = Color.white;
  public static final Color DEFAULT_OUTLINE_COLOR = MapColors.ICON_FILL;
  public static final Color DEFAULT_TEXT_COLOR = MapColors.ICON_FILL;

  private final boolean fSelected;
  private final Color fFillColor;
  private final Color fBorderColor;
  private final Color fTextColor;

  /**
   * Creates a new icon provider that uses default colors for the icon.
   * @param aSelected if selected colors should be used.
   */
  public ClusterIconProvider(boolean aSelected) {
    fSelected = aSelected;
    fFillColor = null;
    fBorderColor = null;
    fTextColor = null;
  }

  /**
   * Creates a new icon provider that uses custom colors for the icon.
   * @param aFillColor   the fill color.
   * @param aBorderColor the border color.
   * @param aTextColor   the text color.
   */
  public ClusterIconProvider(Color aFillColor, Color aBorderColor, Color aTextColor) {
    fSelected = false;
    fFillColor = aFillColor;
    fBorderColor = aBorderColor;
    fTextColor = aTextColor;
  }

  @Override
  public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
    if (aObject instanceof TLcdCluster) {
      TLcdCluster cluster = (TLcdCluster) aObject;
      return createClusterIcon(cluster, getFillColor(), getBorderColor(), getTextColor());
    }
    throw new IllegalArgumentException("ClusterIconProvider can only provide icons for TLcdCluster objects");
  }

  @Override
  public boolean canGetIcon(Object aObject) {
    return aObject instanceof TLcdCluster;
  }

  private Color getFillColor() {
    if (fFillColor != null) {
      return fFillColor;
    }
    return fSelected ? UIColors.alpha(DEFAULT_FILL_COLOR, 111) : DEFAULT_FILL_COLOR;
  }

  private Color getBorderColor() {
    if (fBorderColor != null) {
      return fBorderColor;
    }
    return fSelected ? MapColors.SELECTION : DEFAULT_OUTLINE_COLOR;
  }

  private Color getTextColor() {
    if (fTextColor != null) {
      return fTextColor;
    }
    return fSelected ? UIColors.alpha(DEFAULT_TEXT_COLOR, 111) : DEFAULT_TEXT_COLOR;
  }

  private static ClusterIcon createClusterIcon(TLcdCluster<?> aCluster, Color aFillColor, Color aOutlineColor, Color aTextColor) {
    ClusterIcon icon = new ClusterIcon();
    icon.setOutlineColor(aOutlineColor);
    icon.setFillColor(aFillColor);
    icon.setTextColor(aTextColor);
    if (aCluster != null) {
      configureClusterIconForCluster(aCluster, icon);
    }
    return icon;
  }

  private static void configureClusterIconForCluster(TLcdCluster<?> aCluster, ClusterIcon aIconSFCT) {
    adjustClusterIconSize(aCluster, aIconSFCT);
    aIconSFCT.setText("" + aCluster.getComposingElements().size());
  }

  private static void adjustClusterIconSize(TLcdCluster<?> aCluster, ClusterIcon aIconSFCT) {
    double scaleFactor = Math.log10(aCluster.getComposingElements().size()) / Math.log10(25);
    scaleFactor = clamp(scaleFactor, 1.0, 3.0);
    int outerSize = (int)Math.round(scaleFactor * BASE_ICON_SIZE);
    int innerSize = (int)Math.round(scaleFactor * BASE_ICON_SIZE * INNER_ICON_SIZE_FACTOR);
    outerSize += (outerSize % 2 == 0 ? 1 : 0);
    innerSize -= (innerSize % 2 == 0 ? 1 : 0);
    aIconSFCT.setSize(outerSize, innerSize);
  }

  private static double clamp(double aValue, double aMin, double aMax) {
    return aValue < aMin ? aMin : aValue > aMax ? aMax : aValue;
  }

}
