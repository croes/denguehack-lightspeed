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
package samples.ogc.sld.lightspeed;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.earth.model.TLcdEarthModelDescriptor;
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
import com.luciad.ogc.sld.view.gxy.TLcdSLDContext;
import com.luciad.ogc.sld.view.lightspeed.TLspSLDStyler;
import com.luciad.ogc.sld.xml.TLcdSLDFeatureTypeStyleDecoder;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Layer factory that creates SLD styled layers if the data source file is accompanied by an SLD (.sld) file.
 * <p/>
 * For {@code ILcdModelTreeNode} models, an {@code ILcdLayerTreeNode} is created, with a sub-layer
 * for every child model, created using the delegate layer factories.
 *
 * @see TLcdSLDFeatureTypeStyleDecoder
 */
public class SLDFileLayerFactory extends ALspSingleLayerFactory {

  private TLcdSLDFeatureTypeStyleDecoder fSLDDecoder = new TLcdSLDFeatureTypeStyleDecoder();
  private Iterable<ILspLayerFactory> fLayerFactories;

  /**
   * Creates a new factory using the given layer factories for model tree nodes.
   * @param aLayerFactories used for the sub models of a model tree node
   */
  public SLDFileLayerFactory(Iterable<ILspLayerFactory> aLayerFactories) {
    fLayerFactories = aLayerFactories;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    if (TLcdModelTreeNodeUtil.isEmptyModel(aModel) ||
        descriptor instanceof TLcdRasterModelDescriptor ||
        descriptor instanceof TLcdMultilevelRasterModelDescriptor ||
        descriptor instanceof TLcdEarthModelDescriptor) {
      return false;
    }
    try {
      String source = retrieveSLDSourceName(aModel);
      return source != null && fSLDDecoder.decodeFeatureTypeStyle(source) != null;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILspLayer layer = createSingleLayer(aModel);
    if (aModel instanceof ILcdModelTreeNode) {
      TLspCompositeLayerFactory composite = new TLspCompositeLayerFactory(fLayerFactories);
      ILcdLayerTreeNode node = (ILcdLayerTreeNode) layer;
      Enumeration models = ((ILcdModelTreeNode) aModel).models();
      while (models.hasMoreElements()) {
        Collection<ILspLayer> leaves = composite.createLayers((ILcdModel) models.nextElement());
        for (ILspLayer leaf : leaves) {
          node.addLayer(leaf);
        }
      }
    }
    return layer;
  }

  private ILspLayer createSingleLayer(ILcdModel aModel) {
      ILspStyler styler = createStyler(aModel);
      return TLspShapeLayerBuilder.newBuilder()
                                  .model(aModel)
                                  .bodyStyler(TLspPaintState.REGULAR, styler)
                                  .labelStyler(TLspPaintState.REGULAR, styler)
                                  .build();
  }

  public ILspStyler createStyler(ILcdModel aModel) {
    String source = retrieveSLDSourceName(aModel);
    try {
      TLcdSLDFeatureTypeStyle style = fSLDDecoder.decodeFeatureTypeStyle(source);
      return new TLspSLDStyler(style, createSLDContext(aModel));
    } catch (IOException e) {
      // Should not happen since canCreateLayers already checks this
      throw new IllegalStateException("Can not decode SLD file");
    }
  }

  private String retrieveSLDSourceName(ILcdModel aModel) {
    String sourceName = aModel.getModelDescriptor().getSourceName();
    if (sourceName == null) {
      return null;
    }
    int pointIndex = sourceName.lastIndexOf(".");
    return pointIndex > 0 ? sourceName.substring(0, pointIndex) + ".sld" : null;
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
   * This implementation returns an ILcdOGCFeatureIDRetriever that returns the first feature of an object, if model
   * objects implement ILcdDataObject, or the object itself converted to a string, otherwise.
   *
   * @param aModel the model to create a feature ID retriever for.
   * @return The ILcdOGCFeatureIDRetriever to be used for evaluating SLD painting instructions.
   */
  protected ILcdOGCFeatureIDRetriever createFeatureIdRetriever(ILcdModel aModel) {
    return new FeatureIdRetriever();
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
   * Creates a default filter evaluator. Override this method if you want a
   * filter evaluator which can evaluate advanced spatial filtering or if you
   * want to add your own function.
   *
   * @return a default filter evaluator.
   */
  protected ILcdOGCFilterEvaluator createFilterEvaluator() {
    return new TLcdOGCFilterEvaluator();
  }

  // An ID retriever that expects the unique ID of an object to be in the
  // property at position 0, if the object implements ILcdDataObject.
  // Otherwise the result of the toString method is assumed unique.
  private static class FeatureIdRetriever implements ILcdOGCFeatureIDRetriever {
    public String retrieveFeatureID(Object aObject) {
      if (aObject instanceof ILcdDataObject) {
        return ((ILcdDataObject) aObject).getValue(((ILcdDataObject) aObject).getDataType().getDeclaredProperties().get(0)).toString();
      }
      return aObject.toString();
    }
  }
}
