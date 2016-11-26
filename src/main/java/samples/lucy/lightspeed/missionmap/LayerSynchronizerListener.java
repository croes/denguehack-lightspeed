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
package samples.lucy.lightspeed.missionmap;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.TLcyCompositeLayerStyleProvider;
import com.luciad.util.ALcdWeakChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Listener which applies the style changes of the preparation layer on the preview layer
 *
 */
class LayerSynchronizerListener extends ALcdWeakChangeListener<ILspLayer> {

  private final ILcyLucyEnv fLucyEnv;

  /**
   * Create a new synchronizer which applies the style changes made to <code>aPreparationLayer</code> to
   * <code>aPreviewLayer</code>. The listener will automatically be added.
   * @param aPreparationLayer The preparation layer
   * @param aPreviewLayer The preview layer
   * @param aLucyEnv The Lucy back-end
   */
  LayerSynchronizerListener(ILspLayer aPreparationLayer, ILspLayer aPreviewLayer, ILcyLucyEnv aLucyEnv) {
    super(aPreviewLayer);
    fLucyEnv = aLucyEnv;
    TLcyCompositeLayerStyleProvider styleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    if (styleProvider.canGetStyle(aPreparationLayer)) {
      ILcyLayerStyle style = styleProvider.getStyle(aPreparationLayer);

      if (styleProvider.canApplyStyle(style, aPreviewLayer)) {
        styleProvider.applyStyle(style, aPreviewLayer);
      }

      if (style != null) {
        style.addChangeListener(this);
      }
    }
  }

  @Override
  protected void stateChangedImpl(ILspLayer aPreviewLayer, TLcdChangeEvent aChangeEvent) {
    ILcyLayerStyle preparationStyle = (ILcyLayerStyle) aChangeEvent.getSource();
    TLcyCompositeLayerStyleProvider styleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    if (styleProvider.canApplyStyle(preparationStyle, aPreviewLayer)) {
      styleProvider.applyStyle(preparationStyle, aPreviewLayer);
    }
  }
}
