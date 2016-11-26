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
package samples.decoder.asdi.lightspeed;

import java.awt.Color;
import java.util.List;

import com.luciad.format.asdi.TLcdASDIFlightPlanHistoryModelDescriptor;
import com.luciad.format.asdi.TLcdASDITrajectoryModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelList;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.ILcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdXYZPolyline;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.decoder.asdi.ASDILayerFactory;

/**
 *  Layer factory for the trajectory models produced by the ASDI model decoder.
 */
@LcdService(service = ILspLayerFactory.class)
public class LspASDILayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdASDITrajectoryModelDescriptor ||
        aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanHistoryModelDescriptor) {
      return true;
    }
    if (aModel instanceof TLcdModelList && ((TLcdModelList) aModel).getModelCount() > 0) {
      for (int i = 0; i < ((TLcdModelList) aModel).getModelCount(); i++) {
        if (!canCreateLayers(((TLcdModelList) aModel).getModel(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel instanceof TLcdModelList) {
      TLspLayerTreeNode node = new TLspLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      for (int i = 0; i < ((TLcdModelList) aModel).getModelCount(); i++) {
        node.addLayer(createLayer(((TLcdModelList) aModel).getModel(i)));
      }
      return node;
    }
    return createLeafLayer(aModel);
  }

  private ILspLayer createLeafLayer(ILcdModel aModel) {
    boolean history = aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanHistoryModelDescriptor;

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder().model(aModel);
    TLspStyler bodyStyler = new TLspStyler();
    Color color = history ? ASDILayerFactory.HISTORY_COLOR : ASDILayerFactory.TRAJECTORY_COLOR;
    bodyStyler.addStyles(new PolylineStyleTargetProvider(), TLspLineStyle.newBuilder().color(color).build());
    layerBuilder.bodyStyler(TLspPaintState.REGULAR, bodyStyler);
    ILspInteractivePaintableLayer layer = layerBuilder.build();

    if (history) {
      layer.setVisible(false);
    }
    return layer;
  }

  /**
   * Visualizes ASDI trajectories and flight plan histories as polylines.
   */
  private static class PolylineStyleTargetProvider extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (aObject instanceof ILcdShape) {
        aResultSFCT.add(aObject);
        return;
      }
      if (aObject instanceof ILcdPointList) {
        ILcdPointList pointList = (ILcdPointList) aObject;
        ILcd3DEditablePointList polyline = aContext.getModelReference() instanceof ILcdGeodeticReference ?
                                           new TLcdLonLatHeightPolyline() : new TLcdXYZPolyline();
        for (int i = 0; i < pointList.getPointCount(); i++) {
          ILcdPoint point = pointList.getPoint(i);
          polyline.insert3DPoint(i, point.getX(), point.getY(), point.getZ());
        }
        aResultSFCT.add(polyline);
      }
    }
  }
}
