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
package samples.lightspeed.clustering;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.common.MapColors;
import samples.common.UIColors;

/**
 * Creates a layer that can handle clusters.
 *
 * @since 2016.0
 */
class ClusterLayerFactory extends ALspSingleLayerFactory {

  // Scale at which labels become visible
  private static final double SCALE_THRESHOLD_LABELS = 3e-3;

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspIconStyle element = TLspIconStyle.newBuilder()
                                         .icon(MapColors.createIcon(false))
                                         .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                         .build();
    TLspIconStyle selectedElement = TLspIconStyle.newBuilder()
                                                 .icon(MapColors.createIcon(true))
                                                 .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                 .build();

    TLspStyler regularStyler = new TLspStyler(element);
    TLspStyler selectionStyler = new TLspStyler(selectedElement);

    TLspTextStyle fElementTextStyle = TLspTextStyle.newBuilder()
                                                   .textColor(UIColors.mid(MapColors.ICON_OUTLINE, Color.WHITE, 0.5))
                                                   .haloThickness(0)
                                                   .build();
    TLspLabelStyler labelStyler = TLspLabelStyler.newBuilder().styles(fElementTextStyle).build();

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .label("Clustered events")
                                .bodyStyler(TLspPaintState.REGULAR, new ClusterAwareStylerWrapper(regularStyler, TLspPaintState.REGULAR))
                                .bodyStyler(TLspPaintState.SELECTED, new ClusterAwareStylerWrapper(selectionStyler, TLspPaintState.SELECTED))
                                .labelStyler(TLspPaintState.REGULAR, new ClusterIgnoringLabelStylerWrapper(labelStyler))
                                .labelStyler(TLspPaintState.SELECTED, new ClusterIgnoringLabelStylerWrapper(labelStyler))
                                .labelScaleRange(new TLcdInterval(SCALE_THRESHOLD_LABELS, Double.MAX_VALUE))
                                .bodyEditable(true)
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

}
