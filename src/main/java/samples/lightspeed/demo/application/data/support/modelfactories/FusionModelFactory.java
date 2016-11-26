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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import com.luciad.fusion.client.ALfnClientEnvironment;
import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDecoder;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;

public class FusionModelFactory extends AbstractModelFactory {

  private static final String PROPERTY_URL = "url";

  private static final String PROPERTY_COVERAGE_ID = "coverageId";

  private final ALfnEnvironment fEnvironment;

  private final ALfnClientEnvironment fClientEnvironment;

  private final TLfnTileStoreModelDecoder fModelDecoder;

  private final TLcdInputStreamFactory fInputStreamFactory;

  public FusionModelFactory(String aType) {
    super(aType);
    fEnvironment = ALfnEnvironment.newInstance();
    fClientEnvironment = ALfnClientEnvironment.newInstance(fEnvironment);
    TLfnClientFactory clientFactory = new TLfnClientFactory(fClientEnvironment);
    fModelDecoder = new TLfnTileStoreModelDecoder(fEnvironment, clientFactory);
    fInputStreamFactory = new TLcdInputStreamFactory();
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    // Load an LFN properties file.
    Properties properties = new Properties();
    // Use an InputStreamFactory to make sure sources are loaded from classpath.
    InputStream stream = fInputStreamFactory.createInputStream(aSource);
    try {
      properties.load(stream);
    } finally {
      stream.close();
    }
    String url = properties.getProperty(PROPERTY_URL);
    // Workaround for the fact that the TileStoreModelDecoder does not load from classpath.
    if (!url.startsWith("file:") && !url.startsWith("http:")) {
      url = Framework.getInstance().getDataPath(url);
    }
    String coverageId = properties.getProperty(PROPERTY_COVERAGE_ID);
    ALfnTileStoreModel model = fModelDecoder.decode(url);

    model.setCoverageId(coverageId);
    return model;
  }

  private String getUrl(String aUrl) throws IOException {
    if (!new File(aUrl).exists()) {
      ClassLoader loader = getClass().getClassLoader();
      URL loaderUrl = loader != null ? loader.getResource(aUrl) : this.getClass().getResource(aUrl);
      if (loaderUrl != null) {
        try {
          aUrl = loaderUrl.toURI().getPath();
        } catch (URISyntaxException e) {
          throw new IOException(e);
        }
      }
    }
    return aUrl;
  }
}
