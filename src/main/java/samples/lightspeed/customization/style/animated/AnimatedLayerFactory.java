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
package samples.lightspeed.customization.style.animated;

import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * LayerFactory for the animated style sample.
 */
class AnimatedLayerFactory extends ALspSingleLayerFactory {

  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) &&
           getNamedObjectFromModel(aModel, "United States") != null;
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      return createCustomSHPLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model not recognized");
    }
  }

  private ILspLayer createCustomSHPLayer(ILcdModel aModel) {
    Object usa = getNamedObjectFromModel(aModel, "United States");
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .selectable(false)
                .bodyStyler(TLspPaintState.REGULAR, new AnimatedAreaStyler(aModel, usa));
    return layerBuilder.build();
  }

  /**
   * Helper method that fetches a domain object with a given name.
   *
   * @param aModel the model to fetch the object from
   * @param aName  the name of the object (corresponding to the toString method)
   *
   * @return the domain object inside the featured shape list with the given name
   */
  private Object getNamedObjectFromModel(ILcdModel aModel, String aName) {
    Enumeration e = aModel.elements();
    while (e.hasMoreElements()) {
      Object element = e.nextElement();
      if (element instanceof ILcdDataObject && ((ILcdDataObject) element).getValue("name").equals(aName)) {
        return element;
      }
    }
    return null;
  }

}
