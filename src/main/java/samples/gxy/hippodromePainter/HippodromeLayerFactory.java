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
package samples.gxy.hippodromePainter;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ALcdGXYPen;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;
import samples.gxy.editing.ShapeGXYLayerFactory;

public class HippodromeLayerFactory implements ILcdGXYLayerFactory {

  /**
   * Create a layer specific for drawing and editing IHippodrome objects.
   *
   * @param aModel the model to contain hippodrome objects.
   *
   * @return a layer to display the hippodromes in the model passed.
   */
  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(true);
    layer.setEditable(true);

    ALcdGXYPen pen = MapSupport.createPen(aModel.getModelReference(), false);
    layer.setGXYPen(pen);

    GXYHippodromePainter painter = new GXYHippodromePainter();
    painter.setSnapIcon(ShapeGXYLayerFactory.SNAP_ICON);
    painter.setFillStyle(ShapeGXYLayerFactory.FILL_STYLE);
    painter.setLineStyle(ShapeGXYLayerFactory.LINE_STYLE);
    painter.setMode(GXYHippodromePainter.OUTLINED_FILLED);
    painter.setSelectionMode(GXYHippodromePainter.OUTLINED_FILLED);
    painter.setEditMode(GXYHippodromePainter.OUTLINED);

    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);
    return layer;
  }
}
