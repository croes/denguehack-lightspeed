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
package samples.lightspeed.decoder.pol;

import com.luciad.format.pol.ILcdPOLColorTable;
import com.luciad.format.pol.ILcdPOLTypeTable;
import com.luciad.format.pol.TLcdPOLColorTable;
import com.luciad.format.pol.TLcdPOLModelDescriptor;
import com.luciad.format.pol.lightspeed.TLspPOLStyler;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * Layer factory implementation for POL data.
 */
@LcdService(service = ILspLayerFactory.class)
public class POLLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdPOLModelDescriptor;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdPOLModelDescriptor)) {
      throw new IllegalArgumentException("Can't create a layer for[" + aModel + "]: not a POL- ILcdModel !");
    } else {
      TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
      TLspPOLStyler styler = new TLspPOLStyler();
      ILcdPOLTypeTable typeTable = ((TLcdPOLModelDescriptor) aModel.getModelDescriptor())
          .getTypeTable();
      ILcdPOLColorTable colorTable = TLcdPOLColorTable.createDefaultPolColorTable();
      styler.setTypeTable(typeTable);
      styler.setColorTable(colorTable);
      layerBuilder.model(aModel)
                  .selectable(true)
                  .bodyEditable(false)
                  .bodyStyler(TLspPaintState.REGULAR, styler);
      return layerBuilder.build();
    }
  }
}
