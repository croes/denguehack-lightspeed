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
package samples.encoder.kml22.converter;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22ExtendedData;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22Schema;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22SchemaData;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22SimpleData;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22SimpleField;
import com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature;
import com.luciad.format.kml22.model.feature.TLcdKML22Document;
import com.luciad.format.kml22.model.feature.TLcdKML22Placemark;

/**
 * Utility class to build {@link TLcdKML22AbstractFeature} instance and convert {@link ILcdDataObject} into KML extended Data.
 */
public final class KML22FeatureUtil {

  /**
   * Create a new instance of <code>TLcdKML22Document</code> with the given name.
   *
   * @param aName the name of the document
   * @return a new TLcdKML22Document
   */
  public static TLcdKML22Document createDocument(String aName) {
    TLcdKML22Document doc = new TLcdKML22Document(TLcdKML22DataTypes.DocumentType);
    doc.setName(aName);
    return doc;
  }

  /**
   * Create a new instance of <code>TLcdKML22Placemark</code> with the given name.
   *
   * @param aName the name of the placemark
   * @return a new TLcdKML22Placemark
   */
  public static TLcdKML22Placemark createPlacemark(String aName) {
    TLcdKML22Placemark placemark = new TLcdKML22Placemark(TLcdKML22DataTypes.PlacemarkType);
    placemark.setName(aName);
    return placemark;
  }

  /**
   * Convert a data type into <code>TLcdKML22Schema</code>.
   *
   * @param aDataType the data type to be converted from.
   * @return a KML schema for user-defined data.
   */
  public static TLcdKML22Schema createSchema(TLcdDataType aDataType) {
    TLcdKML22Schema schema = new TLcdKML22Schema();
    schema.setId(aDataType.getName());
    schema.setName(aDataType.getName());
    for (TLcdDataProperty property : aDataType.getProperties()) {
      TLcdKML22SimpleField simpleField = new TLcdKML22SimpleField();
      simpleField.setName(property.getName());
      simpleField.setDisplayName(property.getDisplayName());
      simpleField.setType(property.getType().getDisplayName());
      schema.getSimpleField().add(simpleField);
    }
    return schema;
  }

  /**
   * Convert a data object into <code>TLcdKML22ExtendedData</code>. If a data property refers to another data object.
   * Only the {@link Object#toString() toString} of this data object will be included in the resulting extended data.
   *
   * @param aDataObject the data object to be converted
   * @param aSchemaURL the URL to a <code>TLcdKML22Schema</code> that models the data type of aDataObject
   * @return instance of TLcdKML22ExtendedData
   */
  public static TLcdKML22ExtendedData createExtendedData(ILcdDataObject aDataObject, String aSchemaURL) {
    TLcdKML22ExtendedData extendedData = new TLcdKML22ExtendedData();
    TLcdDataType dataType = aDataObject.getDataType();
    TLcdKML22SchemaData schemaData = new TLcdKML22SchemaData();
    schemaData.setSchemaUrl(aSchemaURL);
    extendedData.getSchemaData().add(schemaData);
    for (TLcdDataProperty property : dataType.getProperties()) {
      TLcdKML22SimpleData data = new TLcdKML22SimpleData();
      data.setName(property.getName());
      Object value = aDataObject.getValue(property);
      data.setValueObject(value != null ? value.toString() : null);
      schemaData.getSimpleData().add(data);
    }
    return extendedData;
  }

  private KML22FeatureUtil() {
  }
}
