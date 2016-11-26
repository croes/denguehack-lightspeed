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
package samples.lightspeed.decoder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.model.TLcdModelList;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.editor.label.TLspLabelEditor;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.common.MapColors;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.labels.GXYLabelPainterFactory;
import samples.lightspeed.common.LspStyleUtil;
import samples.lightspeed.style.raster.RasterLayerFactory;

/**
 * Fallback layer factory that creates layers with default styling.
 * It uses a default {@link TLspShapeLayerBuilder} with a default {@code TLspLabelStyler}.
 * If the model has a {@code ILcdDataModelDescriptor}, labeling is configured for the first data
 * property that is a {@code String}.
 * <p/>
 * For {@code ILcdModelTreeNode} models, an {@code ILcdLayerTreeNode} is created, with a sub-layer
 * for every child model, created using the delegate layer factories.
 * <p/>
 * For customized styling, you can implement your own {@code ILspLayerFactory}.
 * An example of this is given in {@link samples.lightspeed.labels.placement.StatesLayerFactory}.
 * Alternatively, you can rely on external style files, as is done in SLD.
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.FALLBACK_PRIORITY)
public class UnstyledLayerFactory extends ALspSingleLayerFactory {

  private Iterable<? extends ILspLayerFactory> fLayerFactories;

  private TLspFillStyle fFillStyle = TLspFillStyle.newBuilder().color(MapColors.BACKGROUND_FILL).build();
  private TLspFillStyle fSelectedFillStyle = TLspFillStyle.newBuilder().color(MapColors.SELECTION_FILL).build();
  private TLspLineStyle fLineStyle = TLspLineStyle.newBuilder().width(2).color(MapColors.BACKGROUND_OUTLINE).build();
  private TLspLineStyle fSelectedLineStyle = TLspLineStyle.newBuilder().width(2).color(MapColors.SELECTION_OUTLINE).build();
  private TLspIconStyle fIconStyle = TLspIconStyle.newBuilder().icon(MapColors.createIcon(false)).build();
  private TLspIconStyle fSelectedIconStyle = TLspIconStyle.newBuilder().icon(MapColors.createIcon(true)).build();

  /**
   * Creates a new factory using the layer factories in the default registry for model tree nodes.
   */
  public UnstyledLayerFactory() {
    fLayerFactories = ServiceRegistry.getInstance().query(ILspLayerFactory.class);
  }

  /**
   * Creates a new factory using the given layer factories for model tree nodes.
   * @param aLayerFactories used for the sub models of a model tree node
   */
  public UnstyledLayerFactory(Iterable<? extends ILspLayerFactory> aLayerFactories) {
    fLayerFactories = aLayerFactories;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    // Handle (empty) layer tree nodes, and non-raster models
    return isEmptyModel(aModel) || !isRasterModel(aModel);
  }

  private static boolean isRasterModel(ILcdModel aModel) {
    return RasterLayerFactory.canCreateLayersForModel(aModel);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspLayer layer;
    if (isEmptyModel(aModel)) {
      layer = new TLspLayerTreeNode(aModel);
    } else {
      // Create a label styler
      TLcdDataProperty labelProperty = GXYLabelPainterFactory.getDataModelLabelProperty(aModel.getModelDescriptor());
      ALspStyle labelStyle = labelProperty == null ?
                             TLspDataObjectLabelTextProviderStyle.newBuilder().build() :
                             TLspDataObjectLabelTextProviderStyle.newBuilder().expressions(labelProperty.getName()).build();
      TLspTextStyle textStyle = TLspTextStyle.newBuilder().build();
      TLspLabelStyler labelStyler = TLspLabelStyler.newBuilder().styles(labelStyle, textStyle).build();

      layer = TLspShapeLayerBuilder.newBuilder()
                                   .model(aModel)
                                   .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                   .editableSupported(true)
                                   .bodyEditor(new TLspShapeEditor())
                                   .bodyEditable(false)
                                   .labelEditor(new TLspLabelEditor())
                                   .labelEditable(false)
                                   .bodyStyler(TLspPaintState.REGULAR, createStyler(false))
                                   .bodyStyler(TLspPaintState.SELECTED, createStyler(true))
                                   .build();
      layer.setVisible(TLspPaintRepresentation.LABEL,
                       labelProperty != null &&
                       (labelProperty.getName().toLowerCase().contains("name") || TLcdCoreDataTypes.STRING_TYPE.equals(labelProperty.getType()))
      );
    }

    TLspCompositeLayerFactory composite = new TLspCompositeLayerFactory(fLayerFactories);
    if (isEmptyModel(aModel) && aModel instanceof ILcdModelContainer) {
      Enumeration models = ((ILcdModelContainer) aModel).models();
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) layer;
      while (models.hasMoreElements()) {
        Collection<ILspLayer> leaves = composite.createLayers((ILcdModel) models.nextElement());
        for (ILspLayer leaf : leaves) {
          node.addLayer(leaf, node.layerCount());
        }
      }
    }
    return layer;
  }

  private ALspStyler createStyler(boolean aSelected) {
    TLspIconStyle iconStyle = aSelected ? fSelectedIconStyle : fIconStyle;
    TLspFillStyle fillStyle = fFillStyle == null ? null : (aSelected ? fSelectedFillStyle : fFillStyle);
    TLspLineStyle lineStyle = aSelected ? fSelectedLineStyle : fLineStyle;
    return LspStyleUtil.combinePointLineAndFill(iconStyle, fillStyle == null ? Collections.singletonList(lineStyle) : Arrays.asList(fillStyle, lineStyle));
  }

  private boolean isEmptyModel(ILcdModel aModel) {
    if (TLcdModelTreeNodeUtil.isEmptyModel(aModel)) {
      return true;
    } else if (aModel instanceof TLcdModelList) {
      return true;
    }
    return false;
  }

  public void setFillStyle(TLspFillStyle aFillStyle) {
    fFillStyle = aFillStyle;
  }

  public void setLineStyle(TLspLineStyle aLineStyle) {
    fLineStyle = aLineStyle;
  }
}
