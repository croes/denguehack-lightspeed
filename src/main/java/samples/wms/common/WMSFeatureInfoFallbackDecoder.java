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
import java.util.Collections;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdPoint;

class WMSFeatureInfoFallbackDecoder implements WMSFeatureInfoDecoder {

  @Override
  public ILcdModel decode(InputStream aInputStream, String aFeatureInfoFormat, ILcdPoint aRequestLocation, ILcdModelReference aRequestLocationReference, String aDisplayName) throws IOException {
    if (aRequestLocation == null || aRequestLocationReference == null) {
      throw new IllegalArgumentException("The request location must be set on the TLcdWMSGetFeatureInfoContext");
    }
    ILcdDataObject dataObject = new TLcdDataObject(WMSGetFeatureInfoModelDataTypes.DATA_TYPE);

    dataObject.setValue(WMSGetFeatureInfoModelDataTypes.LOCATION_PROPERTY, aRequestLocation);
    dataObject.setValue(WMSGetFeatureInfoModelDataTypes.CONTENT_FORMAT_PROPERTY, aFeatureInfoFormat);
    byte[] content = TLcdIOUtil.toByteArray(aInputStream);
    dataObject.setValue(WMSGetFeatureInfoModelDataTypes.CONTENT_PROPERTY, content);
    String name = "WMS Feature Info";
    dataObject.setValue(WMSGetFeatureInfoModelDataTypes.NAME_PROPERTY, name);

    ILcdModelDescriptor modelDescriptor = createModelDescriptor(WMSGetFeatureInfoModelDataTypes.DATA_MODEL, Collections.singleton(WMSGetFeatureInfoModelDataTypes.DATA_TYPE), aDisplayName);
    TLcdVectorModel model = new TLcdVectorModel(aRequestLocationReference, modelDescriptor);
    model.addElement(dataObject, ILcdModel.NO_EVENT);
    return model;
  }

  private static ILcdModelDescriptor createModelDescriptor(TLcdDataModel aDataModel, Set<TLcdDataType> aModelElementTypes, String aDisplayName) {
    return new TLcdDataModelDescriptor(null, WMSGetFeatureInfoModelFactory.MODEL_DESCRIPTOR_TYPE_NAME, aDisplayName, aDataModel, aModelElementTypes, null);
  }
}
