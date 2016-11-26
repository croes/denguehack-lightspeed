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
package samples.symbology.lightspeed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.UIColors;
import samples.lightspeed.clustering.ClusterAwareStylerWrapper;
import samples.symbology.common.util.MilitarySymbolClusteringUtil;
import samples.symbology.common.util.MilitarySymbolFacade;

/**
 * A styler that styles clusters of military symbols.
 * If all elements of the cluster are represented by the same symbol, the cluster also adopts this symbol (though a larger version thereof).
 * If not, the cluster is styled as a filled circle in the affiliation color.
 */
public abstract class ClusterAwareMilitarySymbolStyler extends ClusterAwareStylerWrapper {

  public ClusterAwareMilitarySymbolStyler(ILspStyler aSymbolStyler, TLspPaintState aPaintState) {
    super(aSymbolStyler, aPaintState);
  }

  @Override
  protected List<ALspStyle> getClusterStyling(TLcdCluster<?> aCluster, List<ALspStyle> aStylingOfAComposingElement) {
    Object element = aCluster.getComposingElements().iterator().next();
    List<ALspStyle> result = new ArrayList<>();
    if (MilitarySymbolFacade.isMilitarySymbol(element)) {
      double clusterScaleFactor = getClusterSizeFactor(aCluster);
      Color affiliationColor = getAffiliationColor(element);
      if (MilitarySymbolClusteringUtil.allSameSymbol(aCluster)) {
        List<ALspStyle> symbolStylingForCluster = getSymbolStylingForCluster(aStylingOfAComposingElement, clusterScaleFactor, affiliationColor);
        result.addAll(symbolStylingForCluster);
        return result;
      } else {
        return getNonMilitarySymbolClusterStyling(aCluster, affiliationColor);
      }
    } else {
      return result;
    }
  }

  protected Color getAffiliationColor(Object aElement) {
    return MilitarySymbolFacade.getAffiliationColor(aElement, MilitarySymbolFacade.getAffiliationValue(aElement));
  }

  private List<ALspStyle> getSymbolStylingForCluster(List<ALspStyle> aStyles, double aClusterScaleFactor, Color aAffiliationColor) {
    List<ALspStyle> result = new ArrayList<>();
    for (ALspStyle style : aStyles) {
      ALspStyle adaptedStyle = getAdaptedStyle(style, aClusterScaleFactor, aAffiliationColor);
      if (adaptedStyle != null) {
        result.add(adaptedStyle);
      }
    }
    return result;
  }

  protected ALspStyle getAdaptedStyle(ALspStyle aStyle, double aClusterScaleFactor, Color aAffiliationColor) {
    return aStyle;
  }

  protected List<ALspStyle> getNonMilitarySymbolClusterStyling(TLcdCluster aCluster, Color aColor) {
    TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, (int) (25 * getClusterSizeFactor(aCluster)), aColor.brighter(), aColor);
    symbol.setAntiAliasing(true);
    if (isSelected()) {
      TLcdSymbol selectedSymbol = (TLcdSymbol) symbol.clone();
      selectedSymbol.setBorderColor(selectedSymbol.getBorderColor().brighter().brighter());
      Color newFillColor = UIColors.alpha(selectedSymbol.getFillColor(), 111);
      selectedSymbol.setFillColor(newFillColor);
      return getIconStyles(selectedSymbol);
    } else {
      return getIconStyles(symbol);
    }
  }


  private List<ALspStyle> getIconStyles(TLcdSymbol aSymbol) {
    return Arrays.asList(TLspIconStyle.newBuilder().icon(aSymbol).zOrder(5).build(),
                         TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, aSymbol.getIconHeight() / 2).build());
  }

  protected double getClusterSizeFactor(TLcdCluster<?> aCluster) {
    return clamp(Math.log10(aCluster.getComposingElements().size()), 1.33, 3);
  }

  private double clamp(double aValue, double aMin, double aMax) {
    return aValue < aMin ? aMin : aValue > aMax ? aMax : aValue;
  }

}
