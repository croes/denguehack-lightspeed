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
package samples.wms.common;

import java.io.IOException;
import java.io.InputStream;

import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;

class WMSFeatureInfoJsonDecoder implements WMSFeatureInfoDecoder {

  @Override
  public ILcdModel decode(final InputStream aInputStream, String aFeatureInfoFormat, ILcdPoint aRequestLocation, ILcdModelReference aRequestLocationReference, String aDisplayName) throws IOException {
    final String dummySourceName = "getFeatureInfo.json";
    ILcdModel result = createJsonModel(aInputStream, dummySourceName);
    return WMSGetFeatureInfoModelFactory.ensureElementDataTypesAreExposed(result, aDisplayName);
  }

  private static ILcdModel createJsonModel(final InputStream aInputStream, final String aDummyFileName) throws IOException {
    // Setup the model decoder with an input stream factory that always returns aInputStream
    TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
    final TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
    modelDecoder.setInputStreamFactory(new ILcdInputStreamFactory() {
      @Override
      public InputStream createInputStream(String aSource) throws IOException {
        if (aSource.equals(aDummyFileName)) {
          return aInputStream;
        } else {
          return inputStreamFactory.createInputStream(aSource);
        }
      }
    });

    // Source name doesn't exist. This doesn't really matter since we always use aInputStream.
    return modelDecoder.decode(aDummyFileName);
  }
}
