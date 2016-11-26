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
package samples.ogc.sld.gxy;

import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.ogc.sld.SLDFeatureTypeStyleStore;

/**
 * A panel that enables choosing a style from a list and applying the style
 * to a Lightspeed layer in another list.
 */
public class StylePanel extends samples.ogc.sld.StylePanel {

  public StylePanel(ILcdLayered aView, ILcdCollection<ILcdLayer> aSelectedLayers, SLDFeatureTypeStyleStore aStyleStore) {
    super(aView, aSelectedLayers, aStyleStore);
  }

  @Override
  protected void applyStyleToLayer(ILcdLayer aLayer, TLcdSLDFeatureTypeStyle aStyle) {
    if (aLayer instanceof TLcdGXYLayer) {
      TLcdGXYLayer selected_gxy_layer = (TLcdGXYLayer) aLayer;
      SLDGXYLayerUtil.getInstance().applyStyleToLayerSFCT(aStyle, selected_gxy_layer);
    } else {
      throw new IllegalArgumentException("Sample only supports TLcdGXYLayer instances, but I got: " + aLayer);
    }
  }

  @Override
  protected void applyOriginalStyleToLayer(Object aOriginalStyle, ILcdLayer aLayer) {
    if (aLayer instanceof TLcdGXYLayer) {
      TLcdGXYLayer selected_gxy_layer = (TLcdGXYLayer) aLayer;
      selected_gxy_layer.setGXYPainterProvider((ILcdGXYPainterProvider) aOriginalStyle);
      selected_gxy_layer.setGXYLabelPainterProvider(null);
      selected_gxy_layer.setLabeled(false);
    }
  }
}
