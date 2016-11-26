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
package samples.lucy.gxy.waypoints;

import java.awt.Color;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;

import samples.gxy.fundamentals.step3.WayPointLayerFactory;

/**
 * <p>
 *   Extension of the layer factory in the Lightspeed fundamentals sample which
 *   adds an editor to the created layer.
 * </p>
 */
final class GXYWayPointsLayerFactory extends WayPointLayerFactory {
  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    ILcdGXYLayer layer = super.createGXYLayer(aModel);

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 8, Color.GREEN));
    painter.setSelectedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 8, new Color(67, 157, 227)));
    ((TLcdGXYLayer) layer).setGXYPainterProvider(painter);
    ((TLcdGXYLayer) layer).setGXYEditorProvider(painter);

    return layer;
  }

}
