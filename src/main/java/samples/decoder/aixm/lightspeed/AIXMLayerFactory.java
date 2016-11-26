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
package samples.decoder.aixm.lightspeed;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.luciad.ais.view.lightspeed.TLspAISStyler;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm.model.TLcdAIXMDataTypes;
import com.luciad.format.aixm.view.lightspeed.TLspAIXMLayerBuilder;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdInitialLayerIndexProvider;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

@LcdService(service = ILspLayerFactory.class)
public class AIXMLayerFactory extends ALspSingleLayerFactory {

  private static final TLcdDataType[] sDataTypeOrder = new TLcdDataType[]{
      TLcdAIXMDataTypes.Geoborder,
      TLcdAIXMDataTypes.Airspace,
      TLcdAIXMDataTypes.AssociationBasedAirspace,
      TLcdAIXMDataTypes.AirspaceCorridor,
      TLcdAIXMDataTypes.Runway,
      TLcdAIXMDataTypes.Aerodrome,
      TLcdAIXMDataTypes.ATSRoute,
      TLcdAIXMDataTypes.WayPoint,
      TLcdAIXMDataTypes.Procedure,
      TLcdAIXMDataTypes.VOR,
      TLcdAIXMDataTypes.DME,
      TLcdAIXMDataTypes.TACAN,
      TLcdAIXMDataTypes.NDB,
      TLcdAIXMDataTypes.GlidePath,
      TLcdAIXMDataTypes.ILS,
      TLcdAIXMDataTypes.Localizer,
      TLcdAIXMDataTypes.Marker,
      TLcdAIXMDataTypes.Obstacle,
  };

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return "AIXM".equalsIgnoreCase(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public ILspLayer createLayer(final ILcdModel aModel) {
    if (aModel instanceof ILcdModelContainer) {
      TLspLayerTreeNode layer = new TLspLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      layer.setInitialLayerIndexProvider(new InitialLayerIndexProvider(sDataTypeOrder));

      Enumeration models = ((ILcdModelContainer) aModel).models();
      while (models.hasMoreElements()) {
        ILcdModel subModel = (ILcdModel) models.nextElement();
        ILspLayer subLayer = createLayer(subModel);
        layer.addLayer(subLayer);
      }
      return layer;
    } else {
      return TLspAIXMLayerBuilder.newBuilder()
                                 .model(aModel)
                                 .build();
    }
  }

  private static class InitialLayerIndexProvider implements ILcdInitialLayerIndexProvider {

    private final Map<TLcdDataType, Integer> fOrderMap;
    private final int fMaxOrderIndex;

    public InitialLayerIndexProvider(TLcdDataType[] aDataTypeOrder) {
      fOrderMap = new HashMap<TLcdDataType, Integer>();
      for (int i = 0; i < aDataTypeOrder.length; i++) {
        TLcdDataType dataType = aDataTypeOrder[i];
        fOrderMap.put(dataType, i);
      }
      fMaxOrderIndex = aDataTypeOrder.length;
    }

    @Override
    public int getInitialLayerIndex(ILcdLayer aLayer, ILcdLayerTreeNode aLayerNode) {
      int newLayerOrderIndex = getOrderIndex(aLayer);
      for (int i = 0; i < aLayerNode.layerCount(); i++) {
        ILcdLayer layer = aLayerNode.getLayer(i);
        int orderIndex = getOrderIndex(layer);
        if (newLayerOrderIndex < orderIndex) {
          return i;
        }
      }
      return aLayerNode.layerCount();
    }

    private int getOrderIndex(ILcdLayer aLayer) {
      for (Map.Entry<TLcdDataType, Integer> entry : fOrderMap.entrySet()) {
        if (hasType(aLayer.getModel(), entry.getKey())) {
          return entry.getValue();
        }
      }
      return fMaxOrderIndex;
    }
  }

  private static boolean hasType(ILcdModel aModel, TLcdDataType aType) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    if (!(modelDescriptor instanceof ILcdDataModelDescriptor)) {
      return false;
    }
    ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) modelDescriptor;
    return dataModelDescriptor.getModelElementTypes().contains(aType);
  }

}
