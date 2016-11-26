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
package samples.lightspeed.customization.paintrepresentation;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.painter.shape.TLspShapePaintingHints;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Creates layers with a regular shape painter for the regular paint representations and a bounds painter for the
 * BOUNDS_BODY.
 */
class LayerFactoryWithBoundsRepresentation extends ALspSingleLayerFactory {

  /**
   * The custom paint representation is defined as a static constant. It can be
   * reused across different layers.
   */
  private static final TLspPaintRepresentation BOUNDS_BODY = TLspPaintRepresentation.getInstance("BOUNDS_BODY", 50);

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    // Creates a layer with a BODY painter
    ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                           .model(aModel)
                                           .bodyEditable(true)
                                           .paintingHints(TLspShapePaintingHints.MAX_QUALITY).build();
    // Adds a new paint representation called BOUNDS_BODY
    return addBoundsPainter(layer);
  }

  /**
   * Adds a {@code BoundsPainter} to the given layer using the custom {@link #BOUNDS_BODY}
   * paint representation.
   *
   * @param aLayer the layer to which the bounded painter should be added
   * @return the same layer
   */
  private static ILspLayer addBoundsPainter(ILspLayer aLayer) {
    TLspStyler styler = new TLspStyler();
    styler.addStyles(
        new BoundsStyler(),
        TLspFillStyle.newBuilder().color(new Color(0.5f, 0.5f, 0.5f, 0.2f)).build(),
        TLspLineStyle.newBuilder().color(new Color(0.5f, 0.5f, 0.5f, 0.8f)).build());

    TLspShapePainter painter = new TLspShapePainter();
    painter.setStyler(TLspPaintState.REGULAR, styler);
    painter.setStyler(TLspPaintState.SELECTED, styler);
    painter.setStyler(TLspPaintState.EDITED, styler);
    ((TLspLayer) aLayer).addPaintRepresentation(BOUNDS_BODY);
    ((TLspLayer) aLayer).setPainter(BOUNDS_BODY, painter);

    return aLayer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }
}
