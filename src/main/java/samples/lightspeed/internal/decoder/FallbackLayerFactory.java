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
package samples.lightspeed.internal.decoder;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspToggleStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * @author tomn
 * @since 2012.0
 */
class FallbackLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
/*
    Color fill = new Color(
        0.0f,
        0.7f,
        0.0f,
        0.25f
      );
    Color line = new Color(
        0f, 0f, 0f, 0.8f
      );
*/

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .minimumObjectSizeForPainting(0)
        .objectViewMargin(10)
//        .bodyStyler( TLspPaintState.REGULAR, createStyler( fill, line ) )
        .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  private ILspStyler createStyler(Color aFill, Color aLine) {
    ILspStyler regular = iconShapeToggle(
        TLspIconStyle.newBuilder().build(),
        new TLspStyler(
            TLspFillStyle.newBuilder()
                         .color(aFill)
                         .build(),
            TLspLineStyle.newBuilder()
                         .color(aLine)
                         .build()
        )
    );

    return regular;
  }

  private ILspStyler iconShapeToggle(ILspStyler aIcon, ILspStyler aShape) {
    ALspToggleStyler toggle = new ALspToggleStyler(aIcon, aShape) {
      @Override
      protected boolean isUseSpecialStyler(Object aObject, TLspContext aContext) {
        return aObject instanceof ILcdPolyline ||
               aObject instanceof ILcdPolygon ||
               aObject instanceof ILcdComplexPolygon;
      }
    };
    return toggle;
  }
}
