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
package samples.fusion.client.metadata;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.customization.paintrepresentation.BoundsStyler;

/**
 * Layer factory for painting only the bounds of model rather than the model itself.
 */
class BoundsLayerFactory {

  public ILspLayer createLayer(ILcdModel aModel) {
    TLspStyler regularStyler = new TLspStyler();
    regularStyler.addStyles(new BoundsStyler(), TLspFillStyle.newBuilder().color(new Color(200, 200, 200, 75))
                                                             .elevationMode(ElevationMode.ON_TERRAIN).build(),
                            TLspLineStyle.newBuilder().color(Color.white).elevationMode(ElevationMode.ON_TERRAIN)
                                         .build());

    TLspStyler selectionStyler = new TLspStyler();
    selectionStyler.addStyles(new BoundsStyler(), TLspFillStyle.newBuilder().color(new Color(255, 50, 0, 75))
                                                               .elevationMode(ElevationMode.ON_TERRAIN).build(),
                              TLspLineStyle.newBuilder().color(Color.yellow).elevationMode(ElevationMode.ON_TERRAIN)
                                           .build());

    return TLspShapeLayerBuilder.newBuilder().model(aModel).selectable(true).bodyEditable(false)
                                .bodyStyler(TLspPaintState.REGULAR, regularStyler)
                                .bodyStyler(TLspPaintState.SELECTED, selectionStyler).build();
  }

}
