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
package samples.lightspeed.integration.gxy;

import java.awt.Color;
import java.util.Enumeration;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.serviceregistry.ServiceLoaderRegistry;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * A custom layer factory for SHP models.
 */
class ShpGXYLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      return createGXYShpLayer(aModel);
    } else if (aModel.getModelDescriptor() != null && "Shapes".equals(aModel.getModelDescriptor().getTypeName())) {
      return createGeodeticShapesLayer(aModel);
    }
    return null;
  }

  private ILcdGXYLayer createGXYShpLayer(ILcdModel aModel) {
    TLcdGXYLayerTreeNode treeNode = new TLcdGXYLayerTreeNode();
    treeNode.setPaintOnTopOfChildrenHint(true);
    TLcdGXYLayer layer = aModel instanceof ILcdModelTreeNode ? new TLcdGXYLayerTreeNode(aModel) : new TLcdGXYLayer(aModel);
    layer.setGXYPen(new TLcdGeodeticPen());

    TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter(ALcdGXYAreaPainter.OUTLINED_FILLED);
    painter.setLineStyle(new TLcdGXYPainterColorStyle(new Color(255, 255, 251, 180)));
    painter.setFillStyle(new TLcdGXYPainterColorStyle(new Color(205, 200, 193, 130), new Color(255, 0, 0, 180)));
    painter.setSelectionMode(ALcdGXYAreaPainter.OUTLINED_FILLED);
    layer.setGXYPainterProvider(new TLcdGXYShapeListPainter(painter));

    TLcdGXYLabelPainter labelPainter = new TLcdGXYLabelPainter();
    labelPainter.setWithPin(true);
    labelPainter.setWithAnchorPoint(true);
    labelPainter.setForeground(Color.white);
    labelPainter.setHaloEnabled(true);
    labelPainter.setHaloColor(Color.darkGray);
    layer.setGXYLabelPainterProvider(labelPainter);
    layer.setGXYLabelEditorProvider(labelPainter);
    layer.setLabeled(true);

    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setLabelsEditable(true);

    createChildLayers(aModel, layer);

    return layer;
  }

  private void createChildLayers(ILcdModel aModel, ILcdGXYLayer aLayer) {
    if (aModel instanceof ILcdModelTreeNode) {
      Iterable<ILcdGXYLayerFactory> layerFactories = ServiceLoaderRegistry.getInstance().query(ILcdGXYLayerFactory.class);
      TLcdCompositeGXYLayerFactory composite = new TLcdCompositeGXYLayerFactory(layerFactories);
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) aLayer;
      Enumeration models = ((ILcdModelTreeNode) aModel).models();
      while (models.hasMoreElements()) {
        ILcdGXYLayer childLayerLayer = composite.createGXYLayer((ILcdModel) models.nextElement());
        if (childLayerLayer != null) {
          node.addLayer(childLayerLayer);
        }
      }
    }
  }

  private ILcdGXYLayer createGeodeticShapesLayer(ILcdModel aModel) {
    Iterable<ILcdGXYLayerFactory> layerFactories = ServiceLoaderRegistry.getInstance().query(ILcdGXYLayerFactory.class);
    GXYUnstyledLayerFactory layerFactory = new GXYUnstyledLayerFactory(layerFactories);
    layerFactory.setEditingEnabled(true);
    return layerFactory.createGXYLayer(aModel);
  }
}
