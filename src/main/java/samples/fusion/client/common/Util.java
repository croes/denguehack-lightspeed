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
package samples.fusion.client.common;

import java.util.HashSet;
import java.util.Set;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdFitGXYLayerInViewClipAction;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;

import samples.gxy.common.layers.GXYLayerUtil;

/**
 * Utility class.
 */
public class Util {

  private Util() {
  }

  public static void addLayerForModel(ILcdGXYView aView, ILcdGXYLayer aLayer, boolean aFitToLayer) {
    // Create a layer
    TLcdGXYAsynchronousPaintQueue queue = new TLcdGXYAsynchronousPaintQueue(aView,
                                                                            TLcdGXYAsynchronousPaintQueue.BODIES_AND_SKIP);
    queue.setPriority(Thread.MIN_PRIORITY);
    addLayerForModel(aView, aLayer, aFitToLayer, queue);
  }

  public static void addLayerForModel(ILcdGXYView aView, ILcdGXYLayer aLayer, boolean aFitToLayer,
                                      TLcdGXYAsynchronousPaintQueue aPaintQueue) {
    // Create a layer
    if (aLayer instanceof ILcdGXYEditableLabelsLayer) {
      aLayer = new TLcdGXYAsynchronousEditableLabelsLayerWrapper((ILcdGXYEditableLabelsLayer) aLayer, aPaintQueue);
    } else {
      aLayer = new TLcdGXYAsynchronousLayerWrapper(aLayer, aPaintQueue);
    }

    // Add the layer to the view
    GXYLayerUtil.addGXYLayer(aView, aLayer);

    // Avoid warping
    if (aLayer.getModel().getModelReference() instanceof ILcdXYWorldReference) {
      aView.setXYWorldReference((ILcdXYWorldReference) aLayer.getModel().getModelReference());
    } else if (aLayer.getModel().getModelReference() instanceof ILcdGeodeticReference) {
      aView.setXYWorldReference(new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical()));
    }

    if (aFitToLayer) {
      // Fit to the layer
      TLcdFitGXYLayerInViewClipAction.doFit(aLayer, aView, null);
    }
  }

  public static String[] getProperties(ILcdModel aModel, int aMaxPropertyCount) {
    ILcdDataModelDescriptor descriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();
    Set<String> properties = new HashSet<>();
    for (TLcdDataType dataType : descriptor.getModelElementTypes()) {
      for (TLcdDataProperty property : dataType.getProperties()) {
        if (TLcdCoreDataTypes.STRING_TYPE.isAssignableFrom(property.getType())) {
          properties.add(property.getName());
        }
        if (properties.size() >= aMaxPropertyCount) {
          break;
        }
      }
    }
    return properties.toArray(new String[properties.size()]);
  }
}
