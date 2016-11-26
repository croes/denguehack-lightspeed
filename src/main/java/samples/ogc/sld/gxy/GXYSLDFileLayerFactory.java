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
package samples.ogc.sld.gxy;

import java.io.IOException;
import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.ogc.filter.ILcdOGCFeatureIDRetriever;
import com.luciad.ogc.filter.evaluator.ILcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.ILcdPropertyRetrieverProvider;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.TLcdPropertyRetrieverUtil;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.view.gxy.ALcdSLDFeatureTypeStylePainter;
import com.luciad.ogc.sld.view.gxy.ALcdSLDGXYPainterFactory;
import com.luciad.ogc.sld.view.gxy.TLcdSLDContext;
import com.luciad.ogc.sld.view.gxy.TLcdSLDGXYPainterFactory;
import com.luciad.ogc.sld.xml.TLcdSLDFeatureTypeStyleDecoder;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdShape;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;

import samples.common.serviceregistry.ServiceLoaderRegistry;
import samples.gxy.common.AntiAliasedLabelPainter;
import samples.gxy.common.AntiAliasedPainter;
import samples.gxy.decoder.MapSupport;

/**
 * Layer factory that creates SLD styled layers if the data source file is accompanied by an SLD (.sld) file.
 * <p/>
 * For {@code ILcdModelTreeNode} models, an {@code ILcdLayerTreeNode} is created, with a sub-layer
 * for every child model, created using the delegate layer factories.
 * @see TLcdSLDFeatureTypeStyleDecoder
 */
@LcdService(priority = LcdService.FALLBACK_PRIORITY - 1)
public class GXYSLDFileLayerFactory implements ILcdGXYLayerFactory {

  private TLcdSLDFeatureTypeStyleDecoder fSLDDecoder = new TLcdSLDFeatureTypeStyleDecoder();
  private Iterable<ILcdGXYLayerFactory> fLayerFactories;

  /**
   * Creates a new factory using the layer factories in the default registry for model tree nodes.
   */
  public GXYSLDFileLayerFactory() {
    fLayerFactories = ServiceLoaderRegistry.getInstance().query(ILcdGXYLayerFactory.class);
  }

  /**
   * Creates a new factory using the given layer factories for model tree nodes.
   * @param aLayerFactories used for the sub models of a model tree node
   */
  public GXYSLDFileLayerFactory(Iterable<ILcdGXYLayerFactory> aLayerFactories) {
    fLayerFactories = aLayerFactories;
  }

  /**
   * Creates an SLD styled layer if the model's source file is accompanied by an SLD (.sld) file.
   * @param aModel a model that needs to be visualized in an <code>ILcdGXYView</code>
   * @return an SLD styled layer if the model's source file is accompanied by an SLD (.sld) file,
   *         or null if this is not the case
   */
  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdSLDFeatureTypeStyle style = getSLDFeatureStyle(aModel);
    if (style == null) {
      return null;
    }

    if (!canCreateLayer(aModel, style)) {
      return null;
    }
    ILcdGXYLayer gxyLayer = createGXYLayer(aModel, style);
    if (aModel instanceof ILcdModelTreeNode) {
      TLcdCompositeGXYLayerFactory composite = new TLcdCompositeGXYLayerFactory(fLayerFactories);
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) gxyLayer;
      Enumeration models = ((ILcdModelTreeNode) aModel).models();
      while (models.hasMoreElements()) {
        ILcdGXYLayer subLayer = composite.createGXYLayer((ILcdModel) models.nextElement());
        if (subLayer != null) {
          node.addLayer(subLayer);
        }
      }
    }
    return gxyLayer;
  }

  private TLcdSLDFeatureTypeStyle getSLDFeatureStyle(ILcdModel aModel) {
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    if (TLcdModelTreeNodeUtil.isEmptyModel(aModel) ||
        descriptor instanceof TLcdRasterModelDescriptor ||
        descriptor instanceof TLcdMultilevelRasterModelDescriptor) {
      return null;
    }
    try {
      String source = retrieveSLDSourceName(aModel);
      return source != null ? fSLDDecoder.decodeFeatureTypeStyle(source) : null;
    } catch (IOException e) {
      return null;
    }
  }

  private String retrieveSLDSourceName(ILcdModel aModel) {
    String sourceName = aModel.getModelDescriptor().getSourceName();
    if (sourceName == null) {
      return null;
    }
    int pointIndex = sourceName.lastIndexOf(".");
    return pointIndex >= 0 ? sourceName.substring(0, pointIndex) + ".sld" : null;
  }

  private boolean canCreateLayer(ILcdModel aILcdModel, TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle) {
    Enumeration<?> elements = aILcdModel.elements();
    Object probe_object;
    return aSLDFeatureTypeStyle != null &&
           (probe_object = elements.nextElement()) != null &&
           (probe_object instanceof ILcdShape ||
            probe_object instanceof ILcdRaster ||
            probe_object instanceof ILcdMultilevelRaster);
  }

  private ILcdGXYLayer createGXYLayer(ILcdModel aModel, TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle) {
    TLcdGXYLayer layer = aModel instanceof ILcdModelTreeNode ? new TLcdGXYLayerTreeNode(aModel) : new TLcdGXYLayer(aModel);
    layer.setLabel(aModel.getModelDescriptor().getDisplayName());
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(), false));

    // create an SLD context based on the model
    TLcdSLDContext sld_context = createSLDContext(aModel);

    // create a painter based on the feature type style and an SLD context
    ALcdSLDFeatureTypeStylePainter painter =
        createFeatureTypeStylePainter(aSLDFeatureTypeStyle, sld_context);

    // set the painter as painter provider on the layer
    layer.setGXYPainterProvider(new AntiAliasedPainter((ILcdGXYPainterProvider) painter));
    // this is required for the text symbolizers, as they may be implemented
    // as ILcdGXYLabelPainter instances.
    layer.setGXYLabelPainterProvider(painter);
    // ...

    layer.setLabeled(true);
    layer.setVisible(true);
    layer.setEditable(false);

    return layer;
  }

  private ALcdSLDFeatureTypeStylePainter createFeatureTypeStylePainter(TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle, TLcdSLDContext aSLDContext) {
    ALcdSLDGXYPainterFactory sld_painter_factory = new TLcdSLDGXYPainterFactory();
    return sld_painter_factory.createFeatureTypeStylePainter(aSLDFeatureTypeStyle, aSLDContext);
  }

  private TLcdSLDContext createSLDContext(ILcdModel aModel) {
    ILcdOGCFilterEvaluator filter_evaluator = createFilterEvaluator();
    ILcdOGCFeatureIDRetriever feature_id_retriever = createFeatureIdRetriever(aModel);
    ILcdPropertyRetrieverProvider property_retriever_provider = createPropertyRetrieverProvider(aModel);

    return new TLcdSLDContext(filter_evaluator,
                              (ILcdGeoReference) aModel.getModelReference(),
                              feature_id_retriever,
                              property_retriever_provider);
  }

  /**
   * This implementation returns the default ILcdPropertyRetrieverProvider created by {@link TLcdPropertyRetrieverUtil}.
   *
   * @param aModel the model to create a property retriever provider for.
   * @return the ILcdPropertyRetrieverProvider to be used for evaluating SLD painting instructions.
   */
  protected ILcdPropertyRetrieverProvider createPropertyRetrieverProvider(ILcdModel aModel) {
    return TLcdPropertyRetrieverUtil.getDefaultPropertyRetrieverProvider(aModel);
  }

  /**
   * This implementation returns an ILcdOGCFeatureIDRetriever that returns the first declared property of an object, if model
   * objects implement ILcdDataObject, or the object itself converted to a string, otherwise.
   *
   * @param aModel the model to create a feature ID retriever for.
   * @return The ILcdOGCFeatureIDRetriever to be used for evaluating SLD painting instructions.
   */
  protected ILcdOGCFeatureIDRetriever createFeatureIdRetriever(ILcdModel aModel) {
    return new FeatureIdRetriever();
  }

  /**
   * Creates a default filter evaluator. Override this method if you want
   * a filter evaluator which can evaluate advanced spatial filtering or
   * if you want to add your own function.
   * @return a default filter evaluator.
   */
  protected ILcdOGCFilterEvaluator createFilterEvaluator() {
    return new TLcdOGCFilterEvaluator();
  }

  // An ID retriever that expects the unique ID of an object to be in the
  // property at position 0, if the object implements ILcdDataObject.
  // Otherwise the result of the toString method is assumed unique.
  private class FeatureIdRetriever implements ILcdOGCFeatureIDRetriever {
    public String retrieveFeatureID(Object aObject) {
      if (aObject instanceof ILcdDataObject) {
        return ((ILcdDataObject) aObject).getValue(((ILcdDataObject) aObject).getDataType().getDeclaredProperties().get(0)).toString();
      }
      return aObject.toString();
    }
  }
}
