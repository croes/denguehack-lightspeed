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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import com.luciad.format.s57.TLcdS57UnifiedModelDecoder;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

public class ECDISModelFactory extends AbstractModelFactory {

  private boolean fUseSENC = false;

  public ECDISModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    TLcdS57UnifiedModelDecoder s57ModelDecoder = new TLcdS57UnifiedModelDecoder();
    s57ModelDecoder.setUseSENCCache(fUseSENC);
    s57ModelDecoder.setSENCCacheDir(getSENCRoot(aSource));
    return s57ModelDecoder.decode(aSource);
  }

  /**
   * In the release, the data is loaded from classpath, but the SENC cache needs to be a file.
   */
  private File getSENCRoot(String aSource) {
    File resolvedSource = new File(aSource);

    if (!resolvedSource.exists()) {
      URL resource = this.getClass().getClassLoader().getResource(aSource);
      if (resource != null) {
        try {
          resolvedSource = new File(resource.toURI());
        } catch (URISyntaxException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    if (!resolvedSource.exists()) {
      throw new IllegalStateException("Cannot find absolute path for " + aSource);
    }

    File sencRoot = new File(resolvedSource.getAbsoluteFile().getParentFile().getParentFile(), "SENC");
    if (!sencRoot.exists()) {
      throw new IllegalStateException("Cannot determine SENC root for " + aSource);
    }

    return sencRoot;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fUseSENC = Boolean.parseBoolean(aProperties.getProperty("s57.senc.cache", "false"));
  }

  // Generate complete SENC cache for NOAA dataset
  public static void main_(String[] args) throws Exception {
    TLcdS57UnifiedModelDecoder modelDecoder = new TLcdS57UnifiedModelDecoder();
    modelDecoder.setUseSENCCache(true);
    modelDecoder.setSENCCacheDir(new File("Data/internal/NOAA/SENC/"));
    ILcdModel model = modelDecoder.decode("Data/internal/NOAA/ENC_ROOT/CATALOG.031");
    Enumeration e = model.elements();
    int count = 0;
    while (e.hasMoreElements()) {
      Object o = e.nextElement();
      if ((++count % 100000) == 0) {
        System.err.println("" + count);
      }
    }
    model.dispose();
    System.err.println("Total count: " + count);
  }

  // Test model with SENC
  public static void main__(String[] args) throws Exception {
    String aSource = "Data/internal/NOAA/ENC_ROOT/CATALOG.031";
    ECDISModelFactory modelFactory = new ECDISModelFactory(null);
    modelFactory.fUseSENC = true;
    ILcdModel model = modelFactory.createModel(aSource);
    model.elements().nextElement();
  }
}
