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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSNamedLayerStyle;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdOGCWMSProxyModelDecoder;
import com.luciad.wms.client.model.TLcdWMSClient;
import com.luciad.wms.client.model.TLcdWMSStyledNamedLayerWrapper;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory to create WMS models.
 */
public class WMSModelFactory extends AbstractModelFactory {

  private static final String PROPERTY_URL = "url";

  private static final String PROPERTY_LAYER_ID = "layerId";

  private static final String PROPERTY_STYLE_ID = "styleId";

  private final TLcdInputStreamFactory fInputStreamFactory;

  public WMSModelFactory(String aType) {
    super(aType);
    fInputStreamFactory = new TLcdInputStreamFactory();
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    // Load a WMS properties file.
    Properties properties = new Properties();
    // Use an InputStreamFactory to make sure sources are loaded from classpath.
    InputStream stream = fInputStreamFactory.createInputStream(aSource);
    try {
      properties.load(stream);
    } finally {
      stream.close();
    }
    String url = properties.getProperty(PROPERTY_URL);
    String layerId = properties.getProperty(PROPERTY_LAYER_ID);
    String styleId = properties.getProperty(PROPERTY_STYLE_ID, "");
    TLcdWMSClient client;
    try {
      client = TLcdWMSClient.createWMSClient(new URI(url),
                                             OWSTransportFactory.createTransport(properties),
                                             null);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    ILcdModel model = new TLcdOGCWMSProxyModelDecoder().decode(client);
    ALcdWMSProxy proxy = (ALcdWMSProxy) model.elements().nextElement();
    proxy.setMapFormat("image/png");
    proxy.setBackgroundImageTransparent(true);

    registerWMSLayer(model, layerId, styleId);

    return model;
  }

  private static void registerWMSLayer(ILcdModel aModel, String aLayerName, String aLayerStyle) {
    ALcdWMSProxy proxy = (ALcdWMSProxy) aModel.elements().nextElement();

    ALcdWMSNamedLayer wmsLayer = retrieveWMSLayer(proxy.getWMSRootNamedLayer(0), aLayerName);
    ALcdWMSNamedLayerStyle wmsLayerStyle = retrieveWMSLayerStyle(wmsLayer, aLayerStyle);
    proxy.addStyledNamedLayer(new TLcdWMSStyledNamedLayerWrapper(wmsLayer, wmsLayerStyle));
  }

  private static ALcdWMSNamedLayer retrieveWMSLayer(ALcdWMSNamedLayer aLayer, String aName) {
    ALcdWMSNamedLayer layer = null;

    if (aName.equals(aLayer.getNamedLayerName())) {
      layer = aLayer;
    } else {
      for (int i = 0; i < aLayer.getChildWMSNamedLayerCount(); i++) {
        layer = retrieveWMSLayer(aLayer.getChildWMSNamedLayer(i), aName);
        if (layer != null) {
          break;
        }
      }
    }

    return layer;
  }

  private static ALcdWMSNamedLayerStyle retrieveWMSLayerStyle(ALcdWMSNamedLayer aLayer, String aName) {
    for (int i = 0; i < aLayer.getNamedLayerStyleCount(); i++) {
      if (aName.equals(aLayer.getNamedLayerStyle(i).getStyleName())) {
        return aLayer.getNamedLayerStyle(i);
      }
    }
    return null;
  }
}
