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

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;

class WMSFeatureInfoHtmlDecoder implements WMSFeatureInfoDecoder {

  private final WMSFeatureInfoDecoder fFallbackDecoder = new WMSFeatureInfoFallbackDecoder();

  @Override
  public ILcdModel decode(InputStream aInputStream, String aFeatureInfoFormat, ILcdPoint aRequestLocation, ILcdModelReference aRequestLocationReference, String aDisplayName) throws IOException {
    ILcdModel result = fFallbackDecoder.decode(aInputStream, aFeatureInfoFormat, aRequestLocation, aRequestLocationReference, aDisplayName);
    return checkIfHtmlModelIsEmpty(result);
  }

  private static ILcdModel checkIfHtmlModelIsEmpty(ILcdModel aModel) {
    ILcdDataObject dataObject = (ILcdDataObject) aModel.elements().nextElement();
    byte[] content = (byte[]) dataObject.getValue(WMSGetFeatureInfoModelDataTypes.CONTENT_PROPERTY);
    if (content == null || content.length == 0) {
      return null;
    }
    String htmlString = new String(content);
    String startBodyTag = "<body>";
    String endBodyTag = "</body>";
    int startBody = htmlString.indexOf(startBodyTag);
    int endBody = htmlString.indexOf(endBodyTag);
    if (startBody == -1 && endBody == -1) {
      return null;
    }
    String body = htmlString.substring(startBody + startBodyTag.length(), endBody).trim();
    if (body.length() == 0) {
      // Empty html body
      return null;
    }
    return aModel;
  }
}
