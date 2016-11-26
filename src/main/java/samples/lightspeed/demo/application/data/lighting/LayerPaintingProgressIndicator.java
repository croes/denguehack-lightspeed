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
package samples.lightspeed.demo.application.data.lighting;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdRotatingIcon;
import samples.common.HaloLabel;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Indicates painting progress of a (set of) layers on a view
 */
final class LayerPaintingProgressIndicator implements ILcdDisposable {

  private final ILcdFilter<ILspLayer> fLayerFilter;
  private final ILspView fView;

  private final Map<ILspLayer, Boolean> fPaintingInProgressMap = new HashMap<>();
  private final PaintingProgressListener fPaintingProgressListener = new PaintingProgressListener();
  private final ILcdLayeredListener fLayeredListener = new ILcdLayeredListener() {
    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      ILspLayer layer = (ILspLayer) e.getLayer();
      switch (e.getID()) {
      case TLcdLayeredEvent.LAYER_ADDED:
        layerAdded(layer);
        break;
      case TLcdLayeredEvent.LAYER_REMOVED:
        layerRemoved(layer);
        break;
      default:
        //do nothing
      }
    }
  };
  private final JPanel fProgressIndicatorPanel;

  /**
   *
   * @param aLayerFilter The painting progress indicator will only appear for layers which are accepted by the filter.
   *                     When layers that are not accepted by the filter are being painted, nothing will be shown
   * @param aView
   * @param aIndicatorText Text which will appear next to the rotating progress icon.
   */
  LayerPaintingProgressIndicator(ILcdFilter<ILspLayer> aLayerFilter, ILspView aView, String aIndicatorText) {
    fLayerFilter = aLayerFilter;
    fView = aView;

    Enumeration layers = fView.layers();
    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();
      layerAdded(layer);
    }
    fView.addLayeredListener(fLayeredListener);

    fProgressIndicatorPanel = new JPanel(new BorderLayout());
    fProgressIndicatorPanel.setOpaque(false);

    JLabel progressIndicatorLabel = new HaloLabel(aIndicatorText);
    Font currentFont = progressIndicatorLabel.getFont();
    progressIndicatorLabel.setFont(new Font(currentFont.getName(), currentFont.getStyle(), 24));
    fProgressIndicatorPanel.add(progressIndicatorLabel, BorderLayout.EAST);
    fProgressIndicatorPanel.add(new JLabel("", new TLcdRotatingIcon("images/icons/busy_black_and_white_32.png"), SwingConstants.CENTER), BorderLayout.WEST);
    if (fView instanceof ALspAWTView) {
      Container overlayComponent = ((ALspAWTView) fView).getOverlayComponent();
      overlayComponent.add(fProgressIndicatorPanel, TLcdOverlayLayout.Location.CENTER);
    }

    updateVisibility();
  }

  @Override
  public void dispose() {
    fView.removeLayeredListener(fLayeredListener);
    Enumeration layers = fView.layers();
    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();
      layerRemoved(layer);
    }
    if (fView instanceof ALspAWTView) {
      Container overlayComponent = ((ALspAWTView) fView).getOverlayComponent();
      overlayComponent.remove(fProgressIndicatorPanel);
    }
  }

  private void layerAdded(ILspLayer aLayer) {
    if (fLayerFilter.accept(aLayer)) {
      aLayer.addStatusListener(fPaintingProgressListener);
    }
  }

  private void layerRemoved(ILspLayer aLayer) {
    if (fLayerFilter.accept(aLayer)) {
      aLayer.removeStatusListener(fPaintingProgressListener);
      fPaintingInProgressMap.remove(aLayer);
      updateVisibility();
    }
  }

  private void updateVisibility() {
    Collection<Boolean> values = fPaintingInProgressMap.values();
    boolean shouldBeVisible = false;
    for (Boolean value : values) {
      if (value) {
        shouldBeVisible = true;
      }
    }
    fProgressIndicatorPanel.setVisible(shouldBeVisible);
  }

  private final class PaintingProgressListener implements ILcdStatusListener<ILspLayer> {
    @Override
    public void statusChanged(TLcdStatusEvent<ILspLayer> aStatusEvent) {
      ILspLayer layer = aStatusEvent.getSource();
      switch (aStatusEvent.getID()) {
      case TLcdStatusEvent.START_BUSY:
        fPaintingInProgressMap.put(layer, Boolean.TRUE);
        updateVisibility();
        ;
        break;
      case TLcdStatusEvent.END_BUSY:
        fPaintingInProgressMap.put(layer, Boolean.FALSE);
        updateVisibility();
        ;
        break;
      default:
        //do nothing
      }
    }
  }
}
