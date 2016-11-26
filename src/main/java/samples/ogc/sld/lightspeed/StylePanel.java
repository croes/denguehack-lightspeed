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
package samples.ogc.sld.lightspeed;

import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.view.lightspeed.TLspSLDStyler;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.ogc.sld.SLDFeatureTypeStyleStore;

/**
 * A panel that enables choosing a style from a list and applying the style
 * to an ILcdGXYLayer in another list.
 */
class StylePanel extends samples.ogc.sld.StylePanel {

  public StylePanel(ILcdLayered aView, ILcdCollection<ILcdLayer> aSelectedLayers) {
    super(aView, aSelectedLayers, new SLDFeatureTypeStyleStore(""));
  }

  /**
   * Applies a {@code TLcdSLDFeatureTypeStyle } to Lightspeed layers. A
   * {@code TLspSLDStyler} is created based on the given feature type style,
   * and used for both the BODY and LABEL paint representation.
   */
  @Override
  protected void applyStyleToLayer(ILcdLayer aLayer, TLcdSLDFeatureTypeStyle aStyle) {
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer styledLayer = (ILspEditableStyledLayer) aLayer;
      final TLspSLDStyler styler = new TLspSLDStyler(aStyle, SLDFeatureTypeStyleStore.createSLDContext(aLayer.getModel()));
      if (styledLayer.getPaintRepresentations().contains(TLspPaintRepresentation.BODY)) {
        styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, styler);
      }
      if (styledLayer.getPaintRepresentations().contains(TLspPaintRepresentation.LABEL)) {
        styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_LABEL, styler);
      }
    } else {
      throw new IllegalArgumentException("Sample only supports Lightspeed layers that implement ILspEditableStyledLayer, but I got: " + aLayer);
    }
  }

  @Override
  protected Object getOriginalStyle(ILcdLayer aLayer) {
    if (aLayer instanceof ILspStyledLayer) {
      ILspStyledLayer styledLayer = (ILspStyledLayer) aLayer;
      if (styledLayer.getPaintRepresentations().contains(TLspPaintRepresentation.LABEL)) {
        return new ILspStyler[]{
            styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY),
            styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL)
        };
      } else {
        return new ILspStyler[]{
            styledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY)
        };
      }
    } else if (aLayer instanceof ILspLayer) {
      return null;//can happen for performance overlay
    } else {
      return super.getOriginalStyle(aLayer);
    }
  }

  @Override
  protected void applyOriginalStyleToLayer(Object aOriginalStyle, ILcdLayer aLayer) {
    if (aOriginalStyle instanceof ILspStyler[] && aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer styledLayer = (ILspEditableStyledLayer) aLayer;

      if (styledLayer.getPaintRepresentations().contains(TLspPaintRepresentation.BODY)) {
        styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, ((ILspStyler[]) aOriginalStyle)[0]);
      }

      if (styledLayer.getPaintRepresentations().contains(TLspPaintRepresentation.LABEL)) {
        if (((ILspStyler[]) aOriginalStyle).length == 2) {
          styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_LABEL, ((ILspStyler[]) aOriginalStyle)[1]);
        } else {
          styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_LABEL, null);
        }
      }
    }
  }

}
