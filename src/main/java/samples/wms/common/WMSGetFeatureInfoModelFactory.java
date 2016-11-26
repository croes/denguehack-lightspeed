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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdPoint;

/**
 * Converts input streams to ILcdModel instances that can be used for visualization.
 */
public final class WMSGetFeatureInfoModelFactory {

  static final String MODEL_DESCRIPTOR_TYPE_NAME = "WMSGetFeatureInfoType";

  private static final Map<String, WMSFeatureInfoDecoder> DECODERS = new LinkedHashMap<>();
  static {
    DECODERS.put("application/json", new WMSFeatureInfoJsonDecoder());
    DECODERS.put("application/vnd.ogc.gml/3.1.1", new WMSFeatureInfoGMLDecoder());
    DECODERS.put("application/vnd.ogc.gml", new WMSFeatureInfoGMLDecoder());
    DECODERS.put("text/html", new WMSFeatureInfoHtmlDecoder());
    DECODERS.put("text/plain", new WMSFeatureInfoPlainTextDecoder());
  }

  public static boolean isGetFeatureInfoModel(ILcdModel aModel) {
    return MODEL_DESCRIPTOR_TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
  }

  public static ILcdModel convertToModel(InputStream aInputStream, String aFeatureInfoFormat, ILcdPoint aRequestLocation, ILcdModelReference aRequestLocationReference, String aDisplayName) throws IOException {
    BufferedInputStream inputStream = new BufferedInputStream(aInputStream);
    if (isEmptyInputStream(inputStream)) {
      return null;
    }

    WMSFeatureInfoDecoder decoder = DECODERS.get(aFeatureInfoFormat);
    if (decoder == null) {
      decoder = new WMSFeatureInfoFallbackDecoder();
    }
    return decoder.decode(inputStream, aFeatureInfoFormat, aRequestLocation, aRequestLocationReference, aDisplayName);
  }

  private static boolean isEmptyInputStream(BufferedInputStream aInputStream) throws IOException {
    // Check if the input stream is empty. If so, return nothing
    if (aInputStream == null) {
      return true;
    }
    aInputStream.mark(1);
    int firstByte = aInputStream.read();
    if (firstByte == -1) {
      return true;
    }
    aInputStream.reset();
    return false;
  }

  /**
   * Makes sure that {@code MODEL_DESCRIPTOR_TYPE_NAME} is used as type name. This is needed to recognise models as
   * GetFeatureInfo models. This method also exposes the element data types.
   * @param aModel the model
   * @return the adjusted model.
   */
  static ILcdModel ensureElementDataTypesAreExposed(ILcdModel aModel, String aDisplayName) {
    if (aModel == null || !(aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor)) {
      return null;
    }

    ILcdDataModelDescriptor oldDescriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();
    TLcdVectorModel newModel = new TLcdVectorModel();
    newModel.setModelReference(aModel.getModelReference());

    Set<TLcdDataType> dataTypes = new HashSet<>();

    Enumeration elements = aModel.elements();
    while (elements.hasMoreElements()) {
      Object element = elements.nextElement();
      if (element instanceof ILcdDataObject) {
        ILcdDataObject dataObject = (ILcdDataObject) element;
        dataTypes.add(dataObject.getDataType());
        newModel.addElement(dataObject, ILcdModel.NO_EVENT);
      }
    }
    newModel.setModelDescriptor(new TLcdDataModelDescriptor(null, MODEL_DESCRIPTOR_TYPE_NAME, aDisplayName, oldDescriptor.getDataModel(), dataTypes, null));
    return newModel;
  }
}
