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
package samples.lucy.cop.addons.missioncontroltheme;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.format.geojson.TLcdGeoJsonModelEncoder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.TLcdShapeList;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Utility class to work with GeoJson based server responses
 */
final class GeoJsonServerCommunicationUtil {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(AGeoJsonRestModelWithUpdates.class);

  private final AGeoJsonRestModelWithUpdates fModelToSupport;

  GeoJsonServerCommunicationUtil(AGeoJsonRestModelWithUpdates aModelToSupport) {
    fModelToSupport = aModelToSupport;
  }

  /**
   * Encode the state of {@code aDomainObject} into a GeoJson string
   * @param aDomainObject The domain object
   * @return A GeoJson string which can be send to the server
   */
  String encodeSingleDomainObject(GeoJsonRestModelElement aDomainObject) {
    try {
      TLcdGeoJsonModelEncoder modelEncoder = new TLcdGeoJsonModelEncoder();
      modelEncoder.setPrettyJson(false);
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      modelEncoder.setOutputStreamFactory(new ILcdOutputStreamFactory() {
        @Override
        public OutputStream createOutputStream(String aDestination) throws IOException {
          return outputStream;
        }
      });

      TLcdVectorModel model = new TLcdVectorModel(fModelToSupport.getModelReference(), fModelToSupport.getModelDescriptor());
      model.addElement(aDomainObject, ILcdModel.NO_EVENT);
      modelEncoder.export(model, "dummySource.json");

      String result = outputStream.toString();
      result = workAroundForNullPropertyValues(result);
      result = retainOnlySingleFeature(result);
      return result;
    } catch (IOException e) {
      LOGGER.error("Could not encode [" + aDomainObject + "] to GeoJson");
      throw new RuntimeException(e);
    }
  }

  /**
   * The roundtrip from the services -> geojson -> domain object -> geojson -> services fails for null properties
   * @param aModelEncoderResult The result from the model encoder
   * @return The adjusted version of the string
   */
  private String workAroundForNullPropertyValues(String aModelEncoderResult) {
    aModelEncoderResult = aModelEncoderResult.replaceAll("\\{\\}", "null");
    return aModelEncoderResult;
  }

  /**
   * The model encoder currently only allows to encode a whole model, while the services
   * expect a single encoded feature. Strip the excessive information from the string
   * @param aModelEncoderResult The result of the model encoder
   * @return The adjusted version of {@code aModelEncoderResult}
   */
  private String retainOnlySingleFeature(String aModelEncoderResult) {
    String featuresString = "features\":[";
    int index = aModelEncoderResult.indexOf(featuresString);
    aModelEncoderResult = aModelEncoderResult.substring(index + featuresString.length());
    //strip of extra "]}" part
    aModelEncoderResult = aModelEncoderResult.substring(0, aModelEncoderResult.length() - 2);
    return aModelEncoderResult;
  }

  /**
   * Converts the server response contained in {@code aFeatureString} to an {@code ILcdModel}
   *
   * @param aFeatureString The server response
   * @return An {@code ILcdModel} containing {@link GeoJsonRestModelElement} instances representing {@code aFeatureString}
   */
  ILcdModel convertServerResponseToModel(String aFeatureString) {
    return convertServerResponseToModel(new ByteArrayInputStream(aFeatureString.getBytes()));
  }

  /**
   * Converts the server response provided by {@code aInputStream} to an {@code ILcdModel}
   * @param aInputStream The input stream which provides the server response
   * @return An {@code ILcdModel} containing {@link GeoJsonRestModelElement} instances representing the contents of {@code aInputStream}
   */
  ILcdModel convertServerResponseToModel(final InputStream aInputStream) {
    TLcdGeoJsonModelDecoder modelDecoder = createGeoJsonModelDecoder();
    modelDecoder.setInputStreamFactory(new ILcdInputStreamFactory() {
      @Override
      public InputStream createInputStream(String aSource) throws IOException {
        return aInputStream;
      }
    });
    try {
      ILcdModel geoJsonModel = modelDecoder.decode("dummySource.json");
      TLcdVectorModel result = new TLcdVectorModel(geoJsonModel.getModelReference(), geoJsonModel.getModelDescriptor());
      @SuppressWarnings("unchecked") Enumeration<ILcdDataObject> elements = geoJsonModel.elements();
      while (elements.hasMoreElements()) {
        ILcdDataObject geoJsonModelElement = elements.nextElement();
        GeoJsonRestModelElement domainObject = new GeoJsonRestModelElement(geoJsonModelElement.getDataType(),
                                                                           fModelToSupport.getServerIDPropertyName(),
                                                                           fModelToSupport.getMobileUniqueIDPropertyName());
        copyDataObjectValues(domainObject, geoJsonModelElement);
        copyAllShapes(domainObject, ((ILcdShapeList) geoJsonModelElement));
        result.addElement(domainObject, ILcdModel.NO_EVENT);
      }
      return result;
    } catch (IOException e) {
      LOGGER.error("The model decoder could not decode the response of the server", e);
      return null;
    }
  }

  List<Integer> convertToIDsList(String aDeletedIDsMessage) {
    aDeletedIDsMessage = aDeletedIDsMessage.replaceAll("[^0-9]+", " ");
    List<String> idsAsStrings = Arrays.asList(aDeletedIDsMessage.trim().split(" "));
    List<Integer> result = new ArrayList<Integer>(idsAsStrings.size());
    for (String idAsString : idsAsStrings) {
      result.add(Integer.parseInt(idAsString));
    }
    return result;
  }

  private TLcdGeoJsonModelDecoder createGeoJsonModelDecoder() {
    TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
    modelDecoder.setDefaultModelReference(fModelToSupport.getModelReference());
    modelDecoder.setModelElementType(fModelToSupport.getDataType());
    return modelDecoder;
  }

  /**
   * Copy all the shapes from one shapelist to another
   * @param aShapeListToUpdate The shapelist which will be updated
   * @param aShapeListToCopyFrom The shapelist to copy from
   */
  static void copyAllShapes(TLcdShapeList aShapeListToUpdate, ILcdShapeList aShapeListToCopyFrom) {
    for (int i = 0; i < aShapeListToCopyFrom.getShapeCount(); i++) {
      aShapeListToUpdate.addShape(aShapeListToCopyFrom.getShape(i));
    }
  }

  /**
   * Copy all the property values from one data object to another
   * @param aDataObjectToUpdateSFCT The data object which will be updated
   * @param aDataObjectToCopyFrom The data object to copy from
   */
  static void copyDataObjectValues(ILcdDataObject aDataObjectToUpdateSFCT, ILcdDataObject aDataObjectToCopyFrom) {
    for (TLcdDataProperty property : aDataObjectToUpdateSFCT.getDataType().getProperties()) {
      aDataObjectToUpdateSFCT.setValue(property.getName(), aDataObjectToCopyFrom.getValue(property.getName()));
    }
  }
}
