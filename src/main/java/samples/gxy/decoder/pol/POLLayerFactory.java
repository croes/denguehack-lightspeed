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
package samples.gxy.decoder.pol;

import com.luciad.format.pol.ILcdPOLColorTable;
import com.luciad.format.pol.ILcdPOLTypeTable;
import com.luciad.format.pol.TLcdPOLColorTable;
import com.luciad.format.pol.TLcdPOLLayer;
import com.luciad.format.pol.TLcdPOLModelDescriptor;
import com.luciad.format.pol.TLcdPOLPainter;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.gxy.decoder.MapSupport;

/**
 * Default layer factory for models containing POL data.
 * This layer factory will create <code>TLcdPOLLayer</code> objects, with
 * a <code>TLcdPOLPainter</code> as painter provider.
 */
@LcdService
public class POLLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdPOLModelDescriptor)) {
      return null;
    } else {
      TLcdPOLLayer layer = new TLcdPOLLayer(aModel);
      layer.setSelectable(true);
      layer.setEditable(false);
      layer.setLabeled(false);
      layer.setVisible(true);
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
      TLcdPOLPainter geometry_painter = new TLcdPOLPainter();
      ILcdPOLTypeTable typeTable = ((TLcdPOLModelDescriptor) aModel.getModelDescriptor()).getTypeTable();
      ILcdPOLColorTable colorTable = TLcdPOLColorTable.createDefaultPolColorTable();
      geometry_painter.setTypeTable(typeTable);
      geometry_painter.setColorTable(colorTable);
      layer.setColorTable(colorTable);
      layer.setGXYPainterProvider(geometry_painter);
      return layer;
    }
  }
}
