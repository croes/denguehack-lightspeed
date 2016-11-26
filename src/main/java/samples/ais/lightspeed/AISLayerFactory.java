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
package samples.ais.lightspeed;

import com.luciad.ais.model.TLcdAISDataTypes;
import com.luciad.ais.view.lightspeed.TLspAISStyler;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

@LcdService(service = ILspLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class AISLayerFactory extends ALspSingleLayerFactory {

  private final String[] fBaseDirectories;

  public AISLayerFactory() {
    this(new String[0]);
  }

  public AISLayerFactory(String[] aBaseDirectories) {
    fBaseDirectories = aBaseDirectories;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    if (aModel instanceof ILcdModelContainer) {
      // Leave the creation of layer tree nodes to other layer factories
      return false;
    }
    if (!(aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor)) {
      return false;
    }
    ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();
    return dataModelDescriptor.getDataModel() == TLcdAISDataTypes.getDataModel() ||
           dataModelDescriptor.getDataModel().getDependencies().contains(TLcdAISDataTypes.getDataModel());
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspAISStyler styler = new TLspAISStyler(fBaseDirectories);
    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, styler)
                                .labelStyler(TLspPaintState.REGULAR, styler)
                                .build();
  }

}
