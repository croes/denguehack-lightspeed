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
package samples.ogc.wfs.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.geojson.TLcdFeatureMetaDataProvider;
import com.luciad.format.geojson.TLcdGeoJsonModelEncoder;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.model.ILcdModelReferenceFormatter;
import com.luciad.ogc.filter.ILcdOGCFeatureIDRetriever;
import com.luciad.ogc.wfs.ILcdWFSClientModelEncoderFactory;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeList;
import com.luciad.ogc.wfs.ILcdWFSModelEncoder;
import com.luciad.ogc.wfs.ILcdWFSModelSchemaEncoder;
import com.luciad.ogc.wfs.TLcdWFSClientModelEncoderFactory;
import com.luciad.ogc.wfs.TLcdWFSModelEncoder;
import com.luciad.ogc.wfs.TLcdWFSRequestContext;

/**
 * An  <code>ILcdWFSClientModelEncoderFactory</code> implementation that adds support for GeoJSON as
 * data exchange format towards clients.
 */
class WFSClientModelEncoderFactory implements ILcdWFSClientModelEncoderFactory {
  private static final String WFS_VERSION_1_0_0 = "1.0.0";

  private static final String JSON_OUTPUT_FORMAT_1 = "JSON";
  private static final String JSON_OUTPUT_FORMAT_2 = "application/json";

  private ILcdWFSClientModelEncoderFactory fDefaultModelEncoderFactory;
  private ILcdModelReferenceFormatter fReferenceFormatter;

  public WFSClientModelEncoderFactory(ILcdModelReferenceFormatter aFormatter) {

    fReferenceFormatter = aFormatter;

    TLcdWFSClientModelEncoderFactory defaultModelEncoderFactory = new TLcdWFSClientModelEncoderFactory();
    defaultModelEncoderFactory.setModelReferenceFormatter(fReferenceFormatter);
    fDefaultModelEncoderFactory = defaultModelEncoderFactory;
  }

  /**
   * Map ILcdDataTypes to ILcdOGCFeatureIDRetrievers.  This can only be done for instances of {@link
   * WFSFeatureType}, other TLcdWFSFeatureTypes are ignored.
   */
  private Map<TLcdDataType, ILcdOGCFeatureIDRetriever> getDataTypeToWFSFeatureTypeMap(ILcdWFSFeatureTypeList aAvailableFeatureTypes) {

    Map<TLcdDataType, ILcdOGCFeatureIDRetriever> result = new HashMap<>();
    int count = aAvailableFeatureTypes.getFeatureTypeCount();
    WFSFeatureType wfsFeatureType;
    for (int i = 0; i < count; i++) {
      if (aAvailableFeatureTypes.getFeatureType(i) instanceof WFSFeatureType) {
        wfsFeatureType = (WFSFeatureType) aAvailableFeatureTypes.getFeatureType(i);
        result.put(wfsFeatureType.getDataType(), wfsFeatureType.getFeatureIDRetriever());
      }
    }
    return result;
  }

  /**
   * Build a GeoJson encoder that is configured with an ILcdFeatureMetaDataProvider that delegates
   * the retrieval of a GeoJson Feature's ID to the ILcdOGCFeatureIDRetrievers that were configured
   * for the request
   */
  private TLcdGeoJsonModelEncoder buildGeoJsonModelEncoder(TLcdWFSRequestContext aContext) {

    TLcdGeoJsonModelEncoder geoJsonModelEncoder = new TLcdGeoJsonModelEncoder();
    geoJsonModelEncoder.setModelReferenceFormatter(fReferenceFormatter);

    // calculate a map that maps the TLcdDataType of the WFS Features this server supports
    // to their ILcdOGCFeatureIDRetrievers
    final Map<TLcdDataType, ILcdOGCFeatureIDRetriever> dataTypeToIdRetrieverMap =
        getDataTypeToWFSFeatureTypeMap(aContext.getAvailableFeatureTypes());

    // Create a TLcdGeoJsonEncodeOptions configured with a MetaDataProvider that delegates the retrieval
    // of the Feature's GeoJson ID to the ILcdOGCFeatureIDRetriever of its WFSFeatureType
    geoJsonModelEncoder.setFeatureMetaDataProvider(new TLcdFeatureMetaDataProvider() {
      @Override
      public String getIdPropertyName(Object aObject) {
        return "WFSID";
      }

      /**
       * Avoid adding properties with value null to the GeoJSON response
       */
      @Override
      public Set<String> getPropertyNames(Object aObject) {
        Set<String> allProps = super.getPropertyNames(aObject);
        Set<String> result = new HashSet<>();
        for (String prop : allProps) {
          if (getPropertyValue(aObject, prop) != null && !prop.equals("geometry")) {
            result.add(prop);
          }
        }
        return result;
      }

      @Override
      public Object getPropertyValue(Object aObject, String aPropertyName) {
        if (!aPropertyName.equals("WFSID")) {
          return super.getPropertyValue(aObject, aPropertyName);
        }
        // adapted behavior for WFSID property name
        ILcdDataObject dataObject = (aObject instanceof ILcdDataObject) ? (ILcdDataObject) aObject : null;
        if ((dataObject != null) && (dataObject.getDataType() != null)) {
          return dataTypeToIdRetrieverMap.get(dataObject.getDataType()).retrieveFeatureID(aObject);
        }
        //no ID if not a dataobject
        return null;
      }
    });
    geoJsonModelEncoder.setPrettyJson(false);

    return geoJsonModelEncoder;
  }

  /**
   * Returns a GeoJSON model encoder if the output format matches.  If not, the request is delegated
   * to the default GML implementation <code>TLcdWFSClientModelEncoderFactory</code>.
   */
  @Override
  public ILcdWFSModelEncoder createModelEncoder(ILcdOutputStreamFactory aOutputStreamFactory,
                                                String aOutputFormat,
                                                TLcdWFSRequestContext aContext) {

    if (aOutputFormat.equalsIgnoreCase(JSON_OUTPUT_FORMAT_1) || aOutputFormat.equalsIgnoreCase(JSON_OUTPUT_FORMAT_2)) {
      TLcdGeoJsonModelEncoder geoJsonEncoder = buildGeoJsonModelEncoder(aContext);
      geoJsonEncoder.setOutputStreamFactory(aOutputStreamFactory);
      return new TLcdWFSModelEncoder(geoJsonEncoder, "application/json");
    } else {
      return fDefaultModelEncoderFactory.createModelEncoder(aOutputStreamFactory, aOutputFormat, aContext);
    }
  }

  @Override
  public int getSupportedOutputFormatCount(TLcdWFSRequestContext aContext) {
    if (isWFSVersion1_0_0(aContext)) {
    // We add GeoJSON as supported output format, next to the default supported
    // output formats (GML). See getSupportedOutputFormat(int, TLcdWFSRequestContext)
    // for more info about the version differentiation.
      return fDefaultModelEncoderFactory.getSupportedOutputFormatCount(aContext) + 1;
    } else {
      return fDefaultModelEncoderFactory.getSupportedOutputFormatCount(aContext) + 2;
    }
  }

  @Override
  public String getSupportedOutputFormat(int aIndex, TLcdWFSRequestContext aContext) {
    // We add GeoJSON as supported output format, next to the default supported
    // output formats (GML). There are two common mime types to refer to this format,
    // JSON and application/json. The latter can not be used with WFS 1.0.0,
    // since the WFS 1.0.0 capabilities represent output formats as XML tags and '/' is not
    // an allowed character in an XML tag.
    String result;
    if (isWFSVersion1_0_0(aContext)) {
      switch (aIndex) {
      case 0:
        return JSON_OUTPUT_FORMAT_1;
      default:
        return fDefaultModelEncoderFactory.getSupportedOutputFormat(aIndex - 1, aContext);
      }
    } else {
      // Add two JSON aliases as additional output formats, next to the GML output formats.
      // In the capabilities, they will be advertised as two output formats.
      switch (aIndex) {
      case 0:
        return JSON_OUTPUT_FORMAT_1;
      case 1:
        return JSON_OUTPUT_FORMAT_2;
      default:
        return fDefaultModelEncoderFactory.getSupportedOutputFormat(aIndex - 2, aContext);
      }
    }
  }

  /**
   * @return {@code true} if the version is "1.0.0" or if it is unknown ({@code null})
   */
  private static boolean isWFSVersion1_0_0(TLcdWFSRequestContext aContext) {
    String version = aContext.getVersion();
    return version == null || version.equals(WFS_VERSION_1_0_0);
  }

  /**
   * We delegate to the default model encoder factory for the model schema encoder that is used
   * to encode the response for DescribeFeatureType requests.
   * There are no changes needed for JSON, since this format does not come with a schema.
   */
  @Override
  public ILcdWFSModelSchemaEncoder createModelSchemaEncoder(final ILcdOutputStreamFactory aOutputStreamFactory, String aOutputFormatName, TLcdWFSRequestContext aContext) {
    return fDefaultModelEncoderFactory.createModelSchemaEncoder(aOutputStreamFactory, aOutputFormatName, aContext);
  }

  /**
   * We delegate to the default model encoder factory for the schema output formats that can be used
   * to encode the response for DescribeFeatureType requests.
   * There are no changes needed for JSON, since this format does not come with a schema.
   */
  @Override
  public int getSupportedSchemaOutputFormatCount(TLcdWFSRequestContext aContext) {
    return fDefaultModelEncoderFactory.getSupportedSchemaOutputFormatCount(aContext);
  }

  /**
   * We delegate to the default model encoder factory for the schema output formats that can be used
   * to encode the response for DescribeFeatureType requests.
   * There are no changes needed for JSON, since this format does not come with a schema.
   */
  @Override
  public String getSupportedSchemaOutputFormat(int aIndex, TLcdWFSRequestContext aContext) {
    return fDefaultModelEncoderFactory.getSupportedSchemaOutputFormat(aIndex, aContext);
  }
}
