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
package samples.decoder.arinc.lightspeed;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.luciad.ais.view.lightspeed.TLspAISStyler;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.arinc.model.TLcdARINCDataTypes;
import com.luciad.format.arinc.view.lightspeed.TLspARINCLayerBuilder;
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
public class ARINCLayerFactory extends ALspSingleLayerFactory {

  private static final TLcdDataType[] sDataTypeOrder = new TLcdDataType[]{
      TLcdARINCDataTypes.MORA,
      TLcdARINCDataTypes.FIRUIR,
      TLcdARINCDataTypes.Airspace,
      TLcdARINCDataTypes.RestrictiveAirspace,
      TLcdARINCDataTypes.Runway,
      TLcdARINCDataTypes.Aerodrome,
      TLcdARINCDataTypes.Heliport,
      TLcdARINCDataTypes.AerodromeCommunication,
      TLcdARINCDataTypes.Procedure,
      TLcdARINCDataTypes.Holding,
      TLcdARINCDataTypes.ATSRoute,
      TLcdARINCDataTypes.EnrouteCommunication,
      TLcdARINCDataTypes.WayPoint,
      TLcdARINCDataTypes.VOR,
      TLcdARINCDataTypes.DME,
      TLcdARINCDataTypes.NDB,
      TLcdARINCDataTypes.TACAN,
      TLcdARINCDataTypes.Marker,
      TLcdARINCDataTypes.Localizer,
      TLcdARINCDataTypes.GlidePath,
      TLcdARINCDataTypes.ILS,
  };

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return "ARINC".equalsIgnoreCase(aModel.getModelDescriptor().getTypeName());
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
      return TLspARINCLayerBuilder.newBuilder()
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
