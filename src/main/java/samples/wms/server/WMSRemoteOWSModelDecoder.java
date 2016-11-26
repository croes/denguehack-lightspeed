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

import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.wms.server.ILcdRemoteOWSModelDecoder;
import com.luciad.wms.server.ILcdRemoteOWSModelDecoderFactory;
import com.luciad.wms.server.TLcdWMSRequestContext;
import com.luciad.wms.sld.model.TLcdSLDUserLayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * The <code>ILcdRemoteOWSModelDecoder</code> interface is used by the WMS to resolve content specified by
 * a remote OWS configuration inside a Styled Layer Descriptor's user layer. Such a remote OWS configuration
 * can be used to specify content residing in an OGC WFS or WCS that needs to be rendered by this WMS.
 * <p/>
 * This implementation adds basic support to decode a remote OWS configuration specifying a WFS supporting
 * GeoJSON output (via the parameter 'application/json') and a single feature type name.
 */
public class WMSRemoteOWSModelDecoder implements ILcdRemoteOWSModelDecoderFactory, ILcdRemoteOWSModelDecoder {

  @Override
  public boolean canDecode(TLcdSLDUserLayer aSLDUserLayer, TLcdWMSRequestContext aWMSRequestContext) {
    // We accept remote OWS configurations specifying a WFS.
    return aSLDUserLayer.getRemoteOWS() != null && "WFS".equals(aSLDUserLayer.getRemoteOWS().getService());
  }

  @Override
  public ILcdModel[] decode(TLcdSLDUserLayer aSLDUserLayer, TLcdWMSRequestContext aWMSRequestContext) throws IOException {
    // Determine the URL to get the data from the WFS.
    String baseUrl = aSLDUserLayer.getRemoteOWS().getOnlineResource().getHref();
    String featureTypeName = aSLDUserLayer.getLayerFeatureConstraint().getFeatureTypeConstraint(0).getFeatureTypeName();
    final String wfsRequest = baseUrl + "?REQUEST=GetFeature&TYPENAME=" + featureTypeName + "&OUTPUTFORMAT=application/json&&SERVICE=WFS&VERSION=1.1.0";

    // Decode the GeoJSON data received from the WFS into a model and return it.
    TLcdGeoJsonModelDecoder decoder = new TLcdGeoJsonModelDecoder();
    decoder.setInputStreamFactory( new ILcdInputStreamFactory() {
      @Override
      public InputStream createInputStream(String s) throws IOException {
        URL url = new URL(wfsRequest);
        URLConnection urlConnection = url.openConnection();
        return urlConnection.getInputStream();
      }
    });
    return new ILcdModel[]{decoder.decode("json")};
  }

  @Override
  public ILcdRemoteOWSModelDecoder createModelDecoder(TLcdSLDUserLayer aSLDUserLayer, TLcdWMSRequestContext aWMSRequestContext) throws IllegalArgumentException {
    return this;
  }
}
