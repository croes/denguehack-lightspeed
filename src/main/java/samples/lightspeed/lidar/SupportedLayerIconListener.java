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
package samples.lightspeed.lidar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.luciad.gui.ILcdIcon;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Listener to {@link StyleModel} that adapts the layer icons to indicate whether
 * the layer supports the currently selected styling.
 * <p>
 * If the layer does not support the style, it gets a warning icon.
 * Otherwise, it gets its original icon.
 * </p>
 *
 * @since 2014.0
 */
public class SupportedLayerIconListener implements ILcdChangeListener {

  private final Map<ILcdLayer, ILcdIcon> fIcons = new HashMap<ILcdLayer, ILcdIcon>();
  private final StyleModel fStyleModel;

  private SupportedLayerIconListener(StyleModel aStyleModel) {
    fStyleModel = aStyleModel;
    restore();
    styleChange();
  }

  public static SupportedLayerIconListener plug(StyleModel aStyleModel) {
    SupportedLayerIconListener listener = new SupportedLayerIconListener(aStyleModel);
    aStyleModel.addChangeListener(listener);
    return listener;
  }

  public void unplug() {
    restore();
    fStyleModel.removeChangeListener(this);
  }

  @Override
  public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
    restore();
    styleChange();
  }

  private void restore() {
    for (ILcdLayer layer : fIcons.keySet()) {
      layer.setIcon(fIcons.get(layer));
    }
    fIcons.clear();
  }

  private void styleChange() {
    Collection<ILspLayer> stylerLayers = fStyleModel.getLASLayersThatCanUseStyleProperty(fStyleModel.getStyleProperty());
    for (ILspLayer layer : fStyleModel.getLASLayers()) {
      if (!stylerLayers.contains(layer)) {
        fIcons.put(layer, layer.getIcon());
        layer.setIcon(StylePanel.WARNING_ICON);
      }
    }
  }
}
