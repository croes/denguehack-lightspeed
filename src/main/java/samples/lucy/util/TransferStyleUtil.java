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
package samples.lucy.util;

import java.util.Enumeration;
import java.util.List;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.ILcyLayerStyleProvider;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;

/**
 * Class containing utility method to transfer the style from one layer to another. The style is
 * copied using the available style providers, and if the source and destination layers have
 * corresponding children the style of those is transferred as well.
 */
public class TransferStyleUtil {

  /**
   * Copy the style from one styled layer to another, using style providers. Recursively applied
   * to children as well.
   * @param aLayerSource The layer to get the style from.
   * @param aLayerDestSFCT The layer to apply the style to.
   * @param aLucyEnv The lucy environment containing the style providers.
   */
  public static void transferStyledLayerStyle(ILcdLayer aLayerSource, ILcdLayer aLayerDestSFCT, ILcyLucyEnv aLucyEnv) {
    //If new layer and source layer are styled, copy the style properties
    List<ILcyLayerStyleProvider> styleProviders = aLucyEnv.getServices(ILcyLayerStyleProvider.class);
    for (int i = 0; i < styleProviders.size(); i++) {
      if (styleProviders.get(i).canGetStyle(aLayerSource)) {
        ILcyLayerStyle style = styleProviders.get(i).getStyle(aLayerSource);
        for (ILcyLayerStyleProvider styleProvider : styleProviders) {
          if (styleProvider.canApplyStyle(style, aLayerDestSFCT)) {
            styleProvider.applyStyle(style, aLayerDestSFCT);
            break;
          }
        }
        break;
      }
    }

    //if both layers are tree nodes copy the style of the corresponding children recursively
    if (aLayerSource instanceof ILcdLayerTreeNode && aLayerDestSFCT instanceof ILcdLayerTreeNode) {
      ILcdLayerTreeNode treeSource = (ILcdLayerTreeNode) aLayerSource;
      ILcdLayerTreeNode treeDest = (ILcdLayerTreeNode) aLayerDestSFCT;

      for (int i = 0; i < treeSource.layerCount(); i++) {
        ILcdLayer sourceChildLayer = treeSource.getLayer(i);
        ILcdLayer targetChildLayer = findChildLayer(treeDest, sourceChildLayer.getModel());
        if (targetChildLayer != null) {
          transferStyledLayerStyle(sourceChildLayer, targetChildLayer, aLucyEnv);
        }
      }
    }
  }

  /**
   * Returns the child layer of <code>aParentNode</code> with model <code>aModelOfSearchedLayer</code>
   * @param aParentNode The parent node
   * @param aModelOfSearchedLayer The model of the layer that is searched. Must not be <code>null</code>
   * @return the child layer with model <code>aModelOfSearchedLayer</code>, or <code>null</code> in case no such
   * layer is found
   */
  private static ILcdLayer findChildLayer(ILcdLayerTreeNode aParentNode,
                                          ILcdModel aModelOfSearchedLayer) {
    Enumeration layers = aParentNode.layers();
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      if (aModelOfSearchedLayer.equals(layer.getModel())) {
        return layer;
      }
    }
    return null;
  }
}
