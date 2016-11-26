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
package samples.lightspeed.labels.placement;

import java.awt.Color;
import java.awt.Font;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Layer factory for states. This layer factory demonstrates how to add simple labels. This
 * layer factory only customizes the label style, and uses the defaults to configure the labeling
 * algorithm. TLspLabelStyler is used for this.
 */
public class StatesLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    TLspStyler regularStyler = new TLspStyler(TLspLineStyle.newBuilder().color(new Color(255, 224, 64)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build());
    TLspStyler selectedStyler = new TLspStyler(TLspFillStyle.newBuilder().color(new Color(255, 0, 0, 128)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build(),
                                               TLspLineStyle.newBuilder().color(new Color(255, 224, 64)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build());

    TLspDataObjectLabelTextProviderStyle textProvider = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("STATE_NAME", "STATE_ABBR").build();

    TLspTextStyle regularTextStyle = TLspTextStyle.newBuilder().font(Font.decode("Default-BOLD-12")).textColor(new Color(255, 224, 64)).haloColor(Color.black).alignment(TLspTextStyle.Alignment.CENTER).build();
    TLspTextStyle selectionTextStyle = regularTextStyle.asBuilder().textColor(Color.red).build();
    TLspTextStyle editingTextStyle = regularTextStyle.asBuilder().textColor(Color.orange).build();

    TLspLabelBoxStyle regularLabelBoxStyle = TLspLabelBoxStyle.newBuilder().frameThickness(1).frameColor(new Color(255, 224, 64)).fillColor(new Color(200, 200, 240, 100)).filled(true).haloColor(Color.black).build();
    TLspLabelBoxStyle selectionLabelBoxStyle = regularLabelBoxStyle.asBuilder().frameColor(Color.red).build();
    TLspLabelBoxStyle editingLabelBoxStyle = regularLabelBoxStyle.asBuilder().frameColor(Color.orange).build();

    TLspLabelOpacityStyle opacityStyle = TLspLabelOpacityStyle.newBuilder().build();
    TLspPinLineStyle pinLineStyle = TLspPinLineStyle.newBuilder().build();

    layerBuilder.model(aModel)
                .selectable(true)
                .labelEditable(true)
                .bodyStyler(TLspPaintState.REGULAR, regularStyler)
                .bodyStyler(TLspPaintState.SELECTED, selectedStyler)
                .bodyStyler(TLspPaintState.EDITED, selectedStyler)
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder().styles(regularTextStyle, regularLabelBoxStyle, textProvider, opacityStyle, pinLineStyle).build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder().styles(selectionTextStyle, selectionLabelBoxStyle, textProvider, opacityStyle, pinLineStyle).build())
                .labelStyler(TLspPaintState.EDITED, TLspLabelStyler.newBuilder().styles(editingTextStyle, editingLabelBoxStyle, textProvider, opacityStyle, pinLineStyle).build())
                .synchronizePainters(false);
    return layerBuilder.build();
  }
}
