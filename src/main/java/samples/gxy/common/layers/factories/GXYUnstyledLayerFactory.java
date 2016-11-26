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
package samples.gxy.common.layers.factories;

import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;

import samples.common.MapColors;
import samples.common.serviceregistry.ServiceLoaderRegistry;
import samples.gxy.common.labels.GXYLabelPainterFactory;
import samples.gxy.decoder.MapSupport;

/**
 * Fallback layer factory for vector layers with default styling.<br/>
 * It uses a default {@code TLcdGXYShapePainter} and {@code TLcdGXYLabelPainter}.
 * If the model has a {@code ILcdDataModelDescriptor}, labeling is configured for the first data
 * property that is a {@code String}.
 * <p/>
 * For {@code ILcdModelTreeNode} models, an {@code ILcdLayerTreeNode} is created, with a sub-layer
 * for every child model, created using the delegate layer factories.
 * <p/>
 * For customized styling, you can implement your own {@code ILcdGXYLayerFactory}.
 * An example of this is given in {@link samples.gxy.editing.ShapeGXYLayerFactory}.
 * Alternatively, you can rely on external style files, such as as done in the SLD optional component.
 */
@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.FALLBACK_PRIORITY)
public class GXYUnstyledLayerFactory implements ILcdGXYLayerFactory {

  private final Iterable<? extends ILcdGXYLayerFactory> fLayerFactories;
  private boolean fEditingEnabled = false;
  private boolean fCreateNodes = false;
  private boolean fLabelsWithPin = false;

  private ILcdGXYPainterStyle fFillStyle = new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_FILL);
  private ILcdGXYPainterStyle fLineStyle = TLcdStrokeLineStyle.newBuilder().lineWidth(2).color(MapColors.BACKGROUND_OUTLINE).selectionColor(MapColors.SELECTION).build();

  /**
   * Creates a new factory using the layer factories in the default ServiceRegistry for model tree nodes.
   */
  public GXYUnstyledLayerFactory() {
    fLayerFactories = ServiceLoaderRegistry.getInstance().query(ILcdGXYLayerFactory.class);
  }

  /**
   * Creates a new factory using the given layer factories for model tree nodes.
   * @param aLayerFactories used for the sub models of a model tree node
   */
  public GXYUnstyledLayerFactory(Iterable<? extends ILcdGXYLayerFactory> aLayerFactories) {
    fLayerFactories = aLayerFactories;
  }

  public void setEditingEnabled(boolean aEditingEnabled) {
    fEditingEnabled = aEditingEnabled;
  }

  public void setFillStyle(ILcdGXYPainterStyle aFillStyle) {
    fFillStyle = aFillStyle;
  }

  public void setLineStyle(ILcdGXYPainterStyle aLineStyle) {
    fLineStyle = aLineStyle;
  }

  public void setCreateNodes(boolean aCreateNodes) {
    fCreateNodes = aCreateNodes;
  }

  public void setLabelsWithPin(boolean aLabelsWithPin) {
    fLabelsWithPin = aLabelsWithPin;
  }

  private static boolean isRasterModel(ILcdModel aModel) {
    return RasterLayerFactory.canCreateLayers(aModel);
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (isRasterModel(aModel) && !TLcdModelTreeNodeUtil.isEmptyModel(aModel)) {
      return null;
    }

    TLcdGXYLayer layer = aModel instanceof ILcdModelTreeNode || fCreateNodes ?
                         new TLcdGXYLayerTreeNode(aModel) :
                         new TLcdGXYLayer(aModel);

    if (!TLcdModelTreeNodeUtil.isEmptyModel(aModel)) {
      layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(), false));
      TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
      painter.setLineStyle(fLineStyle);
      painter.setFillStyle(fFillStyle);
      painter.setMode(fFillStyle == null ? ALcdGXYAreaPainter.OUTLINED : ALcdGXYAreaPainter.OUTLINED_FILLED);
      painter.setIcon(MapColors.createIcon(false));
      painter.setSelectedIcon(MapColors.createIcon(true));
      layer.setGXYPainterProvider(painter);
      layer.setGXYEditorProvider(painter);
      layer.setEditable(fEditingEnabled);

      TLcdGXYLabelPainter labelPainter;
      labelPainter = getGXYLabelPainter(aModel, fLabelsWithPin);
      layer.setGXYLabelPainterProvider(labelPainter);
      layer.setGXYLabelEditorProvider(labelPainter);
      layer.setLabelsEditable(fEditingEnabled);
      layer.setLabeled(false);
    }

    if (aModel instanceof ILcdModelTreeNode) {
      TLcdCompositeGXYLayerFactory composite = new TLcdCompositeGXYLayerFactory(fLayerFactories);
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) layer;
      Enumeration models = ((ILcdModelTreeNode) aModel).models();
      while (models.hasMoreElements()) {
        ILcdGXYLayer subLayer = composite.createGXYLayer((ILcdModel) models.nextElement());
        if (subLayer != null) {
          node.addLayer(subLayer);
        }
      }
    }
    return layer;
  }

  public TLcdGXYLabelPainter getGXYLabelPainter(ILcdModel aModel, boolean aWithPin) {
    return GXYLabelPainterFactory.createGXYLabelPainter(aModel, aWithPin);
  }

  /**
   * When passing a layer tree node, makes the requested amount of (non-empty) layers visible, and the other layers invisible.
   * @param aLayer the candidate layer tree node
   * @param aMaxVisibleLayers the amount of layers to keep visible
   */
  public static void setVisible(ILcdLayer aLayer, int aMaxVisibleLayers) {
    setVisible(aLayer, 0, aMaxVisibleLayers);
  }

  private static int setVisible(ILcdLayer aLayer, int aCurrentVisibleLayers, int aMaxVisibleLayers) {

    boolean visible = aCurrentVisibleLayers < aMaxVisibleLayers;
    aLayer.setVisible(visible);

    if (aLayer instanceof ILcdLayerTreeNode) {
      ILcdLayerTreeNode treeLayer = (ILcdLayerTreeNode) aLayer;
      // only count a node if it actually contains data
      if (visible && !TLcdLayerTreeNodeUtil.isEmptyNode(aLayer)) {
        aCurrentVisibleLayers++;
      }

      // Layers are ordered from bottom to top, so we have to iterate them backwards.
      for (Enumeration<ILcdLayer> layers = treeLayer.layersBackwards(); layers.hasMoreElements(); ) {
        aCurrentVisibleLayers = setVisible(layers.nextElement(), aCurrentVisibleLayers, aMaxVisibleLayers);
      }
    } else {
      if (visible) {
        aCurrentVisibleLayers++;
      }
    }
    return aCurrentVisibleLayers;
  }

}
