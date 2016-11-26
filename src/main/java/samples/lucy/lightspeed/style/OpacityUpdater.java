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
package samples.lucy.lightspeed.style;

import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * A class that synchronizes the value of a JSlider with the opacity of all Lightspeed layers in the
 * view that use a filled style and/or a line style.
 */
class OpacityUpdater implements ChangeListener {
  private final JSlider fSlider;
  private ILspView fLspView;

  OpacityUpdater(ILspView aView, JSlider aSlider) {
    fSlider = aSlider;
    fLspView = aView;

    aView.addLayeredListener(new ILcdLayeredListener() {

      @Override
      public void layeredStateChanged(TLcdLayeredEvent aE) {
        if (aE.getID() == TLcdLayeredEvent.LAYER_ADDED) {
          opacityChanged();
        }
      }
    });
  }

  @Override
  public void stateChanged(ChangeEvent aE) {
    opacityChanged();

  }

  private void opacityChanged() {
    final float opacity = ((float) 100 - fSlider.getValue()) * .01f;
    //we can perform the cast since an ILspView only contains ILspLayers
    @SuppressWarnings({"unchecked"})
    final Enumeration<ILspLayer> layers = fLspView.layers();
    while (layers.hasMoreElements()) {
      ILspLayer layer = layers.nextElement();
      if (layer instanceof ILspStyledLayer && layer.getLayerType() != ILspLayer.LayerType.BACKGROUND) {
        updateOpacity(opacity, (ILspStyledLayer) layer);
      }
    }
  }

  /**
   * Update the opacity value of the styles contained in the style of <code>aStyledLayer</code> to <code>aOpacity</code>
   * @param aOpacity The new opacity value
   * @param aStyledLayer The layer for which the style which will be updated
   */
  private void updateOpacity(float aOpacity, ILspStyledLayer aStyledLayer) {
    ILspStyler styler = aStyledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    if (styler instanceof ILspCustomizableStyler) {
      Collection<TLspCustomizableStyle> styles = ((ILspCustomizableStyler) styler).getStyles();
      for (TLspCustomizableStyle customizableStyle : styles) {
        ALspStyle style = customizableStyle.getStyle();
        if (style instanceof TLspFillStyle) {
          final TLspFillStyle derivedStyle = TLspFillStyle.newBuilder().all(style).opacity(aOpacity).build();
          customizableStyle.setStyle(derivedStyle);
        } else if (style instanceof TLspLineStyle) {
          final TLspLineStyle derivedStyle = TLspLineStyle.newBuilder().all(style).opacity(aOpacity).build();
          customizableStyle.setStyle(derivedStyle);
        } else if (style instanceof TLspRasterStyle) {
          final TLspRasterStyle derivedStyle = TLspRasterStyle.newBuilder().all(style).opacity(aOpacity).build();
          customizableStyle.setStyle(derivedStyle);
        }
      }
    }
  }
}
