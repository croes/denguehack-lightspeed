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
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import com.luciad.format.xml.TLcdXMLName;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.filter.model.TLcdOGCBBoxOperator;
import com.luciad.ogc.filter.model.TLcdOGCFilter;
import com.luciad.ogc.filter.model.TLcdOGCPropertyName;
import com.luciad.ogc.wfs.client.TLcdWFSClient;
import com.luciad.ogc.wfs.client.TLcdWFSProxyModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory to create WFS models.
 */
public class WFSModelFactory extends AbstractModelFactory {

  private static final String PROPERTY_URL = "url";

  private static final String PROPERTY_FEATURE_TYPE = "featureType";

  private static final String PROPERTY_BBOX = "bbox";

  private static final String PROPERTY_GEOMETRY_NAME = "geometryName";

  private final TLcdInputStreamFactory fInputStreamFactory;

  public WFSModelFactory(String aType) {
    super(aType);
    fInputStreamFactory = new TLcdInputStreamFactory();
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    // Load a WFS properties file.
    Properties properties = new Properties();
    // Use an InputStreamFactory to make sure sources are loaded from classpath.
    InputStream stream = fInputStreamFactory.createInputStream(aSource);
    try {
      properties.load(stream);
    } finally {
      stream.close();
    }
    String url = properties.getProperty(PROPERTY_URL);
    String featureType = properties.getProperty(PROPERTY_FEATURE_TYPE);

    TLcdWFSClient client;
    try {
      client = TLcdWFSClient.createWFSClient(new URI(url),
                                             OWSTransportFactory.createTransport(properties),
                                             null);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }

    TLcdWFSProxyModel proxyModel =
        TLcdWFSProxyModel.Builder.newBuilder().server(client).featureTypeName(featureType).build();

    TLcdOGCFilter filter = createFilter(properties);
    if (filter != null) {
      proxyModel.setFilter(filter);
    }

    return proxyModel;
  }

  protected TLcdOGCFilter createFilter(Properties aProperties) {
    if (aProperties != null) {
      String bbox = aProperties.getProperty(PROPERTY_BBOX);
      String geometryName = aProperties.getProperty(PROPERTY_GEOMETRY_NAME);
      if (bbox != null && geometryName != null) {
        StringTokenizer tokenizer = new StringTokenizer(bbox, ",");
        double minx = Double.parseDouble(tokenizer.nextToken());
        double miny = Double.parseDouble(tokenizer.nextToken());
        double maxx = Double.parseDouble(tokenizer.nextToken());
        double maxy = Double.parseDouble(tokenizer.nextToken());

        TLcdOGCBBoxOperator bboxOperator = new TLcdOGCBBoxOperator();
        bboxOperator.setBounds(new TLcdLonLatBounds(minx, miny, maxx - minx, maxy - miny));
        bboxOperator.setBoundsGeoReference(new TLcdGeodeticReference());
        bboxOperator.setPropertyName(new TLcdOGCPropertyName(TLcdXMLName.getInstance(new QName(geometryName))));
        return new TLcdOGCFilter(bboxOperator);
      }
    }

    return null;
  }
}
