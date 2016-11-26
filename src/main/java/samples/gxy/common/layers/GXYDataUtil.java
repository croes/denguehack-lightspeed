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
package samples.gxy.common.layers;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;

import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.labels.LayerBasedGXYLabelingAlgorithmProvider;
import samples.gxy.concurrent.painting.AsynchronousLayerFactory;
import samples.gxy.fundamentals.step1.Main;

/**
 * Utility class to quickly decode and visualize data.
 * It automatically uses the model decoders and layer factories exposed as a service, but
 * this behavior can be overridden.
 * <p/>
 * The methods should be called in the following order:
 * <ul>
 * <li>first set up a model</li>
 * <li>create a layer</li>
 * <li>manipulate layer settings and/or add/fit the layer to a view</li>
 * </ul>
 * <p/>
 * Example usage:
 * <pre>
 *   // decodes the model, creates a layer, adds it to the given view and fits on it
 *   GXYDataUtil.instance().model("path/to/mydatafile").layer().addtoView(view).fit();
 * </pre>
 *
 * For a step-by-step explanation of how to visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 *
 * @see ServiceRegistry
 */
public class GXYDataUtil {

  private String fSource;
  private ILcdModel fModel;
  private ILcdGXYLayer fLayer;
  private ILcdGXYView fView;

  public static GXYDataUtil instance() {
    return new GXYDataUtil();
  }

  /**
   * Takes the given model as input.
   */
  public GXYDataUtil model(ILcdModel aSource) {
    fModel = aSource;
    return this;
  }

  /**
   * Decodes the given source, optionally using the given model decoders.
   */
  public GXYDataUtil model(String aSource, ILcdModelDecoder... aDecoders) {
    fSource = aSource;
    TLcdCompositeModelDecoder decoder;
    if ( aDecoders.length == 0) {
      decoder = new TLcdCompositeModelDecoder(ServiceRegistry.getInstance().query(ILcdModelDecoder.class));
    } else {
      decoder = new TLcdCompositeModelDecoder(aDecoders);
    }
    try {
      fModel = decoder.decode(aSource);
    } catch (IOException e) {
      throw new RuntimeException("Could not decode " + aSource, e);
    }
    return this;
  }

  /**
   * Creates a layer for the set model, optionally using the given layer factories.
   */
  public GXYDataUtil layer(ILcdGXYLayerFactory ... aLayerFactories) {
    checkNotNull(fModel, "Specify a model before calling the layer method");
    TLcdCompositeGXYLayerFactory factory;
    if (aLayerFactories.length == 0) {
      factory = new TLcdCompositeGXYLayerFactory(ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class));
    } else {
      factory = new TLcdCompositeGXYLayerFactory(aLayerFactories);
    }
    fLayer = factory.createGXYLayer(fModel);
    return this;
  }

  public GXYDataUtil asynchronous() {
    checkNotNull(fLayer, "Create a layer before calling the label method");
    fLayer = AsynchronousLayerFactory.createAsynchronousLayer(fLayer);
    return this;
  }

  /**
   * Changes the label of the created layer.
   */
  public GXYDataUtil label(String aLabel) {
    checkNotNull(fLayer, "Create a layer before calling the label method");
    fLayer.setLabel(aLabel);
    return this;
  }

  /**
   * Determines whether the created layer is selectable or not.
   */
  public GXYDataUtil selectable(boolean aSelectable) {
    checkNotNull(fLayer, "Create a layer before calling the selectable method");
    fLayer.setSelectable(aSelectable);
    return this;
  }

  public GXYDataUtil addToView(ILcdGXYView aView) {
    if ( fLayer == null) {
      layer();
    }
    checkNotNull(fLayer, "Could not create a layer");
    fView = aView;
    GXYLayerUtil.addGXYLayer(aView, fLayer);
    return this;
  }

  public GXYDataUtil fit() {
    checkNotNull(fLayer, "Create a layer before calling the fit method");
    GXYLayerUtil.fitGXYLayer(fView, fLayer);
    return this;
  }

  public GXYDataUtil labelingAlgorithm(final ILcdGXYLabelingAlgorithm aAlgorithm) {
    ILcdGXYLabelLabelingAlgorithmProvider algorithmProvider = new LayerBasedGXYLabelingAlgorithmProvider(fLayer, aAlgorithm);
    // GXYLabelingAlgorithmProvider picks up the labeling algorithm providers that are registered in the ServiceRegistry
    ServiceRegistry.getInstance().register(algorithmProvider);
    return this;
  }

  public String getSource() {
    return fSource;
  }

  public ILcdModel getModel() {
    return fModel;
  }

  public ILcdGXYLayer getLayer() {
    return fLayer;
  }

  private void checkNotNull(Object aValue, String aReason) {
    if ( aValue == null ) {
      throw new IllegalArgumentException(aReason);
    }
  }

}
