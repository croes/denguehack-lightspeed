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
package samples.lightspeed.common;

import java.io.IOException;
import java.util.Collection;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.fundamentals.step1.Main;

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
 *   LspDataUtil.instance().model("path/to/mydatafile").layer().addtoView(view).fit();
 * </pre>
 *
 * For a step-by-step explanation of how to visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 *
 * @see ServiceRegistry
 */
public class LspDataUtil {

  private String fSource;
  private ILcdModel fModel;
  private ILspLayer fLayer;
  private ILspView fView;

  public static LspDataUtil instance() {
    return new LspDataUtil();
  }

  /**
   * Takes the given model as input.
   */
  public LspDataUtil model(ILcdModel aSource) {
    fModel = aSource;
    return this;
  }

  /**
   * Creates a Lon Lat grid layer.
   */
  public LspDataUtil grid() {
    fLayer = TLspLonLatGridLayerBuilder.newBuilder().build();
    return this;
  }

  /**
   * Decodes the given source, optionally using the given model decoders.
   */
  public LspDataUtil model(String aSource, ILcdModelDecoder... aDecoders) {
    fSource = aSource;
    TLcdCompositeModelDecoder decoder;
    if (aDecoders.length == 0) {
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
  public LspDataUtil layer(ILspLayerFactory... aLayerFactories) {
    checkNotNull(fModel, "Specify a model before calling the layer method");
    TLspCompositeLayerFactory factory;
    if (aLayerFactories.length == 0) {
      factory = new TLspCompositeLayerFactory(ServiceRegistry.getInstance().query(ILspLayerFactory.class));
    } else {
      factory = new TLspCompositeLayerFactory(aLayerFactories);
    }
    Collection<ILspLayer> layers = factory.createLayers(fModel);
    checkNotNull(layers, "Could not create a layer for " + fModel.getModelDescriptor().getSourceName() + ". Make sure" +
                         "that the given layer factory can create a layer for the model, or (when not specifying a " +
                         "layer factory) that annotation processing is enabled.");
    fLayer = layers.iterator().next();
    return this;
  }

  /**
   * Changes the label of the created layer.
   */
  public LspDataUtil label(String aLabel) {
    checkNotNull(fLayer, "Create a layer before calling the label method");
    fLayer.setLabel(aLabel);
    return this;
  }

  /**
   * Determines whether the created layer is selectable or not.
   */
  public LspDataUtil selectable(boolean aSelectable) {
    checkNotNull(fLayer, "Create a layer before calling the selectable method");
    fLayer.setSelectable(aSelectable);
    return this;
  }

  public LspDataUtil editable(boolean aEditable) {
    checkNotNull(fLayer, "Create a layer before calling the editable method");
    fLayer.setEditable(aEditable);
    return this;
  }

  public LspDataUtil labeled(boolean aLabeled) {
    checkNotNull(fLayer, "Create a layer before calling the labeled method");
    fLayer.setVisible(TLspPaintRepresentation.LABEL, aLabeled);
    return this;
  }

  public LspDataUtil addToView(final ILspView aView) {
    if (fLayer == null) {
      layer();
    }
    fView = aView;
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        aView.addLayer(fLayer);
      }
    });
    return this;
  }

  public LspDataUtil fit() {
    checkNotNull(fLayer, "Create a layer before calling the fit method");
    FitUtil.fitOnLayers(null, fView, false, fLayer);
    return this;
  }

  public String getSource() {
    return fSource;
  }

  public ILcdModel getModel() {
    return fModel;
  }

  public ILspLayer getLayer() {
    return fLayer;
  }

  private void checkNotNull(Object aValue, String aReason) {
    if (aValue == null) {
      throw new IllegalArgumentException(aReason);
    }
  }

  private void checkNotNull(Collection<?> aValue, String... aReason) {
    if (aValue == null) {
      StringBuilder sb = new StringBuilder("");
      for (String s : aReason) {
        sb.append(s);
        sb.append(" ");
      }
      throw new IllegalArgumentException(sb.toString());
    }
  }

}
