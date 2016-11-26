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
package samples.xml.customdecodingencoding;

import java.awt.Color;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataModelBuilder.DataModelAnnotationFactory;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaTypeIdentifier;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataModelBuilder;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLSchemaMappingAnnotation;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;

import samples.xml.customdomainclasses.CustomDomainClassesDataTypes;

public class CustomDecodingEncodingDataTypes {

  public static final TLcdDataType EXTENDED_ADDRESS_TYPE;

  private static final TLcdDataModel DATA_MODEL;

  public static final TLcdDataModel getDataModel() {
    return DATA_MODEL;
  }

  static {
    DATA_MODEL = createDataModel();
    EXTENDED_ADDRESS_TYPE = DATA_MODEL.getDeclaredType("ExtendedAddressType");
  }

  /**
   * Creates the data model from the schema and set the correct instance classes for the
   * model and address types.
   */
  private static TLcdDataModel createDataModel() {
    TLcdXMLDataModelBuilder builder = new TLcdXMLDataModelBuilder(CustomDomainClassesDataTypes.getDataModel()) {

      @Override
      protected void buildType(TLcdDataTypeBuilder aTypeBuilder, TLcdXMLSchemaTypeIdentifier aTypeId) {
        if (aTypeBuilder.getName().equals("PointType")) {
          aTypeBuilder.instanceClass(ILcd2DEditablePoint.class);
          aTypeBuilder.primitive(true);
        } else if (aTypeBuilder.getName().equals("ColorType")) {
          aTypeBuilder.instanceClass(Color.class);
          aTypeBuilder.primitive(true);
        } else {
          super.buildType(aTypeBuilder, aTypeId);
          if (aTypeBuilder.getName().equals("ExtendedAddressType")) {
            aTypeBuilder.instanceClass(ExtendedAddress.class);
          }
        }
      }

    };
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder("customdecodingencoding");
    builder.buildDataModel(dataModelBuilder, "http://www.luciad.com/samples.xml.customdecodingencoding", CustomDecodingEncodingDataTypes.class.getResource("samples.xml.customdecodingencoding.xsd").toString());
    // associates the custom decoder and encoder libraries with the data model
    dataModelBuilder.annotateFromFactory(new DataModelAnnotationFactory<TLcdXMLSchemaMappingAnnotation>() {
      public TLcdXMLSchemaMappingAnnotation createAnnotation(TLcdDataModel aDataModel) {
        return new TLcdXMLSchemaMappingAnnotation(
            new MappingLibrary(aDataModel),
            new DecoderLibrary(aDataModel),
            new EncoderLibrary(aDataModel)
        );
      }
    });
    return dataModelBuilder.createDataModel();
  }

}
