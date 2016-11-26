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
package samples.ogc.wfs.proxy;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import javax.xml.namespace.QName;

import com.luciad.ogc.wfs.client.TLcdWFSClient;
import com.luciad.ogc.wfs.client.TLcdWFSProxyModel;
import com.luciad.ogc.wfs.common.model.TLcdWFSCapabilities;
import com.luciad.ogc.wfs.common.model.TLcdWFSFeatureType;
import com.luciad.ogc.wfs.common.model.TLcdWFSFeatureTypeList;

public class WFSModelFactory {

  // Cached client.
  private TLcdWFSClient fWFSClient;

  public Vector<TLcdWFSFeatureType> buildFeatureTypeList(String aServletURL) throws IOException {
    Vector<TLcdWFSFeatureType> types = new Vector<>();

    try {
      fWFSClient = TLcdWFSClient.createWFSClient(new URI(aServletURL));
      TLcdWFSCapabilities wfsCapabilities = fWFSClient.getCachedCapabilities();
      TLcdWFSFeatureTypeList featureTypeList = wfsCapabilities.getFeatureTypeList();
      if (featureTypeList != null) {
        for (int i = 0; i < featureTypeList.getFeatureTypeCount(); i++) {
          TLcdWFSFeatureType featureType = featureTypeList.getFeatureType(i);
          types.add(featureType);
        }
      } else {
        throw new IOException("Can't get feature type list from " + aServletURL);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      final IOException ioException = new IOException(aServletURL + " does not appear to point to a valid WFS server");
      ioException.initCause(e);
      throw ioException;
    }
    return types;
  }

  public TLcdWFSProxyModel createModel(final QName aFeatureTypeName) throws IOException {
    return TLcdWFSProxyModel.Builder.newBuilder().server(fWFSClient).featureTypeName(aFeatureTypeName).build();
  }

}
