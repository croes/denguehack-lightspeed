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
package samples.symbology.nvg.gxy;

import com.luciad.format.nvg.gxy.TLcdNVGGXYLabelPainterProvider;
import com.luciad.format.nvg.gxy.TLcdNVGGXYPainterProvider;
import com.luciad.format.nvg.xml.TLcdNVGModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * Layer factory for models containing NVG data.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class NVGGXYLayerFactory implements ILcdGXYLayerFactory {

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdNVGModelDescriptor) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);

      //Create and configure the painter.
      TLcdNVGGXYPainterProvider painter = createNVGPainterProvider();
      layer.setGXYPainterProvider(painter);
      layer.setGXYEditorProvider(painter);

      //Create and configure the label painter.
      TLcdNVGGXYLabelPainterProvider labelPainter = createNVGLabelPainterProvider();
      layer.setGXYLabelPainterProvider(labelPainter);

      //Set a suitable pen on the layer.
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(), false));

      layer.setSelectable(true);
      layer.setEditable(true);
      layer.setLabeled(true);

      return layer;
    } else {
      return null;
    }
  }
  protected TLcdNVGGXYPainterProvider createNVGPainterProvider() {
    return new TLcdNVGGXYPainterProvider();
  }
  protected TLcdNVGGXYLabelPainterProvider createNVGLabelPainterProvider() {
    return new TLcdNVGGXYLabelPainterProvider();
  }

}
