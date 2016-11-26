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
package samples.symbology.nvg.lightspeed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.symbology.lightspeed.ClusterAwareMilitarySymbolStyler;
import samples.symbology.nvg.common.NVGSymbolClusteringUtil;

/**
 * A styler that styles clusters of NVG objects.
 * If all elements of the cluster are represented by the same military symbol, the cluster also adopts this symbol (though a larger version thereof).
 * If not, the cluster is styled as a filled circle.
 */
public class ClusterAwareNVGSymbolStyler extends ClusterAwareMilitarySymbolStyler {

  private static final Color NON_MILITARY_SYMBOL_CLUSTER_COLOR = new Color(165, 189, 41, 200);

  public ClusterAwareNVGSymbolStyler(ILspStyler aSymbolStyler, TLspPaintState aPaintState) {
    super(aSymbolStyler, aPaintState);
  }

  @Override
  protected List<ALspStyle> getClusterStyling(TLcdCluster<?> aCluster, List<ALspStyle> aStylingOfAComposingElement) {
    Object element = aCluster.getComposingElements().iterator().next();
    if (NVGSymbolClusteringUtil.hasMilitarySymbol(element)) {
      if (NVGSymbolClusteringUtil.allSameSymbol(aCluster)) {
        return getAdaptedStylingForCluster(aStylingOfAComposingElement, getClusterSizeFactor(aCluster));
      } else {
        Object militarySymbol = NVGSymbolClusteringUtil.getMilitarySymbol(element);
        return getNonMilitarySymbolClusterStyling(aCluster, getAffiliationColor(militarySymbol));
      }
    } else {
      return getNonMilitarySymbolClusterStyling(aCluster, NON_MILITARY_SYMBOL_CLUSTER_COLOR);
    }
  }

  private List<ALspStyle> getAdaptedStylingForCluster(List<ALspStyle> aStyles, double aClusterScaleFactor) {
    List<ALspStyle> result = new ArrayList<>();
    for (ALspStyle aLspStyle : aStyles) {
       if (aLspStyle instanceof TLspIconStyle) {
         TLspIconStyle style = (TLspIconStyle) aLspStyle;
         ILcdIcon icon;
         if (isSelected()) {
           icon = new TransparentIconWrapper(style.getIcon(), 111 / 255f);
         } else {
           icon = style.getIcon();
         }
         result.add(style.asBuilder().icon(icon).scale(aClusterScaleFactor).zOrder(5).build());
      } else {
        result.add(aLspStyle);
      }
    }
    return result;
  }

}
