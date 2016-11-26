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
package samples.lucy.cop.gazetteer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.gxy.clustering.ClusterIconProvider;
import samples.lightspeed.common.StylerWrapper;

/**
 * Wraps an {@link ILspStyler} to add cluster styling.
 */
final class ClusterAwareGazetteerStylerWrapper extends StylerWrapper implements ILspCustomizableStyler {

  private static final String STYLE_PREFIX = "style.";
  private static final String CLUSTER_PREFIX = "cluster.";
  private static final String FILL_COLOR_SUFFIX = "fillColor";
  private static final String OUTLINE_COLOR_SUFFIX = "outlineColor";
  private static final String TEXT_COLOR_SUFFIX = "textColor";

  private final ILspCustomizableStyler fDelegate;

  private final Map<String, Color> fTypeToFillColor = new HashMap<>();
  private final Map<String, Color> fTypeToOutlineColor = new HashMap<>();
  private final Map<String, Color> fTypeToTextColor = new HashMap<>();

  public ClusterAwareGazetteerStylerWrapper(ILspCustomizableStyler aDelegate,
                                            String aPropertyPrefix, ALcyProperties aProperties) {
    super(aDelegate);
    fDelegate = aDelegate;
    retrieveColors(aPropertyPrefix, aProperties, GazetteerModel.Type.AIRPORT, "airport");
    retrieveColors(aPropertyPrefix, aProperties, GazetteerModel.Type.AIRPORT, "heliport");
    retrieveColors(aPropertyPrefix, aProperties, GazetteerModel.Type.HOSPITAL, "hospital");
    retrieveColors(aPropertyPrefix, aProperties, GazetteerModel.Type.SCHOOL, "school");
  }

  private void retrieveColors(String aPropertyPrefix, ALcyProperties aProperties, GazetteerModel.Type aType, String aFeatureName) {
    String prefix = aPropertyPrefix + STYLE_PREFIX + aType.name() + "." + aFeatureName + "." + CLUSTER_PREFIX;

    String key = aType.name() + "." + aFeatureName;
    fTypeToFillColor.put(key, aProperties.getColor(prefix + FILL_COLOR_SUFFIX, ClusterIconProvider.DEFAULT_FILL_COLOR));
    fTypeToOutlineColor.put(key, aProperties.getColor(prefix + OUTLINE_COLOR_SUFFIX, ClusterIconProvider.DEFAULT_OUTLINE_COLOR));
    fTypeToTextColor.put(key, aProperties.getColor(prefix + TEXT_COLOR_SUFFIX, ClusterIconProvider.DEFAULT_TEXT_COLOR));
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    List<TLcdCluster> clusters = new ArrayList<>();
    List<Object> nonClusters = new ArrayList<>();
    for (Object object : aObjects) {
      if (object instanceof TLcdCluster) {
        clusters.add((TLcdCluster) object);
      } else {
        nonClusters.add(object);
      }
    }
    super.style(nonClusters, aStyleCollector, aContext);
    styleClusters(clusters, aStyleCollector);
  }

  private void styleClusters(Collection<TLcdCluster> aClusters, ALspStyleCollector aStyleCollector) {
    for (TLcdCluster cluster : aClusters) {
      Set composingElements = cluster.getComposingElements();
      Object element = composingElements.iterator().next();
      GazetteerModel.Type type = GazetteerModel.retrieveType(element);
      String key = type.name() + ".";
      switch (type) {
      case SCHOOL:
        key += "school";
        break;
      case HOSPITAL:
        key += "hospital";
        break;
      case AIRPORT:
        String featureName = (String) ((ILcdDataObject) element).getValue("featureName");
        if (featureName.toLowerCase().contains("heliport")) {
          key += "heliport";
        } else {
          key += "airport";
        }
        break;
      }
      ClusterIconProvider iconProvider = new ClusterIconProvider(fTypeToFillColor.get(key), fTypeToOutlineColor.get(key), fTypeToTextColor.get(key));
      ILcdIcon icon = iconProvider.getIcon(cluster);
      aStyleCollector.object(cluster)
                     .style(TLspIconStyle.newBuilder().icon(icon).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).zOrder(5).build())
                     .submit();
    }
  }

  @Override
  public Collection<TLspCustomizableStyle> getStyles() {
    //we do not expose the styling of the clustering
    return fDelegate.getStyles();
  }
}
