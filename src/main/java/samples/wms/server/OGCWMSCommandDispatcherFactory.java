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
package samples.wms.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.luciad.wms.server.*;
import com.luciad.wms.server.viewencoder.TLcdGXYViewGIFEncoder;
import com.luciad.wms.server.viewencoder.TLcdGXYViewJPEGEncoder;
import com.luciad.wms.server.viewencoder.TLcdGXYViewPNGEncoder;
import com.luciad.wms.server.viewencoder.TLcdGXYViewSVGEncoder;

import samples.wms.server.updater.CapabilitiesUpdater;

/**
 * An extension of the ALcdOGCSLDWMSCommandDispatcherFactory, which creates the
 * ModelDecoderFactory and WMSGXYLayerFactory to be used by the WMS server.
 */
public class OGCWMSCommandDispatcherFactory extends ALcdOGCSLDWMSCommandDispatcherFactory {

  /**
   * Creates and return the WMSModelDecoderFactory as the ILcdModelDecoderFactory implementation to use.
   * The model decoders created by ILcdModelDecoderFactory are used to decode the data for WMS
   * and SLD named layers. Named layers are defined in the capabilities of a WMS server.
   */
  protected ILcdModelDecoderFactory[] createModelDecoderFactories(ServletConfig aServletConfig)
      throws ServletException {
    return new ILcdModelDecoderFactory[]{new WMSModelDecoderFactory()};
  }

  /**
   * Creates and return the WMSRemoteOWSModelDecoder as the ILcdRemoteOWSModelDecoderFactory implementation to use.
   * The model decoders created by ILcdRemoteOWSModelDecoderFactory are used to decode the data for remote OWS content
   * specified in SLD user layers. A common example is data residing in a WFS.
   */
  protected ILcdRemoteOWSModelDecoderFactory[] createRemoteOWSModelDecoderFactories(ServletConfig aServletConfig) {
    return new ILcdRemoteOWSModelDecoderFactory[]{new WMSRemoteOWSModelDecoder()};
  }

  /**
   * Creates and returns the WMSGXYLayerFactory as the ALcdSLDWMSGXYLayerFactory implementation to use.
   * An ALcdSLDWMSGXYLayerFactory is used to create ILcdGXYLayer instances for all types of layers:
   * named layers with named styles, named layers with user styles and user layers with user styles.
   * Named layers and their corresponding named styles are defined in the capabilities of a WMS server.
   */
  protected ALcdSLDWMSGXYLayerFactory[] createSLDWMSGXYLayerFactories(ServletConfig aServletConfig) {
    return new ALcdSLDWMSGXYLayerFactory[]{new WMSGXYLayerFactory()};
  }

  /**
   * Creates and returns an ILcdWMSCapabilitiesUpdater.
   * An ILcdWMSCapabilitiesUpdater can be used to update the capabilities in the command dispatcher at
   * runtime, after an initial version is decoded at startup with an ILcdWMSCapabilitiesDecoder.
   */
  protected ILcdWMSCapabilitiesUpdater createWMSCapabilitiesUpdater(ServletConfig aServletConfig,
                                                                    TLcdOGCWMSCommandDispatcher aCommandDispatcher, ILcdWMSCapabilitiesDecoder aDecoder) {
    return new CapabilitiesUpdater(aCommandDispatcher);
  }

  /**
   * Creates an ILcdModelProvider, which provides central access to all models.
   * An ILcdModelProvider is used by the command dispatcher to retrieve a model, instead of directly
   * using the model decoder factories. These model decoder factories should only be used
   * by the ILcdModelProvider to decode the necessary models. This centralized
   * access allows implementations to define additional functionality, like a cache mechanism.
   *
   * By default, this method returns an ILcdModelProvider that maintains a cache for all
   * decoded models, and that provides support for multi-dimensional WMS layers.
   * To prevent memory problems with the model cache, it makes use of soft reference objects, which
   * are cleared at the discretion of the garbage collector in response to memory demand.
   */
  protected ILcdModelProvider createModelProvider(ILcdModelDecoderFactory[] aModelDecoderFactories) {
    // The model decoder factories must be used by the model provider to decode models.
    return new WMSMultiDimensionalModelProvider(aModelDecoderFactories);
  }

  /**
   * Creates and returns the ILcdGXYViewEncoder implementations to be used to generate
   * maps in a specific requested format.
   */
  protected ILcdGXYViewEncoder[] createGXYViewEncoders(ServletConfig aServletConfig)
      throws ServletException {
    return new ILcdGXYViewEncoder[]{new TLcdGXYViewJPEGEncoder(),   // encode view into JPEG format
                                    new TLcdGXYViewPNGEncoder(),    // encode view into PNG format
                                    new TLcdGXYViewGIFEncoder(),    // encode view into GIF format
                                    new TLcdGXYViewSVGEncoder()};    // encode view into SVG format
  }

  /**
   * Creates and returns the ILcdWMSGetFeatureInfoRequestEncoder implementations to be used
   * to encode feature information.
   */
  protected ILcdWMSGetFeatureInfoRequestEncoder[]
  createWMSGetFeatureInfoEncoders(ServletConfig aServletConfig) throws ServletException {
    return new ILcdWMSGetFeatureInfoRequestEncoder[]{new WMSPlainTextFeatureInfoEncoder(),
                                                     new WMSJsonFeatureInfoEncoder()};
  }

  /**
   * Creates and returns a TLcdWMSGXYViewFactory that is responsible for creating
   * the view on which each map will be rendered.
   */
  protected TLcdWMSGXYViewFactory createWMSGXYViewFactory(ServletConfig aServletConfig) {
    return new WMSGXYViewFactory();
  }
}
