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
package samples.lucy.clustering;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.gui.TLcdSymbol;
import com.luciad.lucy.addons.layercustomizer.lightspeed.TLcyLspLayerCustomizerAddOn;
import com.luciad.lucy.format.lightspeed.ALcyLspStyleFormat;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.gxy.clustering.ClusterIconProvider;
import samples.lightspeed.common.StylerWrapper;

/**
 * <p>
 *   Styler for the body paint representation of the clustered layer.
 * </p>
 *
 * <ul>
 *   <li>
 *     The styler implements {@code ILspCustomizableStyler} so that the {@link ClusteringFormat}
 *     class can extend from {@link ALcyLspStyleFormat}.
 *   </li>
 *   <li>
 *     The clusters only have a body style, and no label style.
 *     The benefits of this approach are:
 *     <ul>
 *       <li>
 *         The textual information of the cluster will always be painted if the cluster is painted.
 *         There will be no decluttering.
 *       </li>
 *       <li>
 *         It allows the user to toggle the visibility of the labels of the individual events,
 *         without loosing the textual information of the cluster.
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *     By exposing all the styles through the {@code ILspCustomizableStyler} interface,
 *     the user can alter the styling through the layer properties panel.
 *     This functionality is provided by the {@link TLcyLspLayerCustomizerAddOn}.
 *   </li>
 * </ul>
 */
final class ClusterBodyStyler extends StylerWrapper implements ILspCustomizableStyler {

  static final String CLUSTER_ICON_STYLE_IDENTIFIER = "cluster_icon_style";
  static final String CLUSTER_TEXT_STYLE_IDENTIFIER = "cluster_text_style";
  private final TLspCustomizableStyle fClusterIconStyle = new TLspCustomizableStyle(
      TLspIconStyle.newBuilder()
                   .icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 25, Color.BLACK, Color.BLACK))
                   .zOrder(5)
                   .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED)
                   .worldSize(2000000)
                   .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                   .build(), true, CLUSTER_ICON_STYLE_IDENTIFIER, "Cluster icon style");
  private final TLspCustomizableStyle fClusterTextStyle = new TLspCustomizableStyle(
      TLspTextStyle.newBuilder()
                   .build(), true, CLUSTER_TEXT_STYLE_IDENTIFIER, "Cluster text style");

  private final List<TLspCustomizableStyle> fStyles = Arrays.asList(fClusterIconStyle,
                                                                    fClusterTextStyle);
  private final ILspStyler fDelegateStyler;

  ClusterBodyStyler(ILspStyler aDelegateStyler) {
    super(aDelegateStyler);
    fDelegateStyler = aDelegateStyler;
    PropertyChangeListener styleChangesListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        fireStyleChangeEvent();
      }
    };
    for (TLspCustomizableStyle customizableStyle : fStyles) {
      customizableStyle.addPropertyChangeListener(styleChangesListener);
    }
  }

  @Override
  public Collection<TLspCustomizableStyle> getStyles() {
    if (fDelegateStyler instanceof ILspCustomizableStyler) {
      List<TLspCustomizableStyle> styles = new ArrayList<>();
      styles.addAll(((ILspCustomizableStyler) fDelegateStyler).getStyles());
      styles.addAll(fStyles);
      return styles;
    }
    return Collections.unmodifiableCollection(fStyles);
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    Set<Object> nonClusterObjects = new HashSet<>();
    Set<TLcdCluster> clusterObjects = new HashSet<>();
    for (Object object : aObjects) {
      if (object instanceof TLcdCluster) {
        clusterObjects.add((TLcdCluster) object);
      } else {
        nonClusterObjects.add(object);
      }
    }
    fDelegateStyler.style(nonClusterObjects, aStyleCollector, aContext);
    styleClusterObjects(clusterObjects, aStyleCollector);
  }

  private void styleClusterObjects(Collection<TLcdCluster> aClusters, ALspStyleCollector aStyleCollector) {
    for (TLcdCluster cluster : aClusters) {
      aStyleCollector.object(cluster).styles(createClusterBodyStyles(cluster)).submit();
    }
  }

  private ALspStyle[] createClusterBodyStyles(TLcdCluster aCluster) {
    TLcdSymbol symbol = (TLcdSymbol) ((TLspIconStyle) fClusterIconStyle.getStyle()).getIcon();
    TLspTextStyle textStyle = (TLspTextStyle) fClusterTextStyle.getStyle();

    ClusterIconProvider iconProvider = new ClusterIconProvider(symbol.getFillColor(), symbol.getBorderColor(), textStyle.getTextColor());
    TLspIconStyle iconStyle =
        ((TLspIconStyle) fClusterIconStyle.getStyle()).asBuilder()
                                                      .icon(iconProvider.getIcon(aCluster))
                                                      .build();

    return new ALspStyle[]{iconStyle};
  }
}
