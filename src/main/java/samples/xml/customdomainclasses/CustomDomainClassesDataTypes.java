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
package samples.xml.customdomainclasses;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataModelBuilder;

public class CustomDomainClassesDataTypes {

  public static final TLcdDataType ADDRESS_TYPE;

  public static final TLcdDataType _MODEL_TYPE;

  private static final TLcdDataModel DATA_MODEL;

  public static final TLcdDataModel getDataModel() {
    return DATA_MODEL;
  }

  static {
    DATA_MODEL = createDataModel();
    ADDRESS_TYPE = DATA_MODEL.getDeclaredType("AddressType");
    _MODEL_TYPE = DATA_MODEL.getDeclaredType("_Model");
  }

  /**
   * Creates the data model from the schema and set the correct instance classes for the
   * model and address types.
   */
  private static TLcdDataModel createDataModel() {
    TLcdXMLDataModelBuilder builder = new TLcdXMLDataModelBuilder();
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder("sample");
    builder.buildDataModel(dataModelBuilder, "http://www.luciad.com/samples.xml.customdomainclasses",
                           Main.class.getResource("/samples/xml/customdomainclasses/samples.xml.customdomainclasses.xsd").toString());
    dataModelBuilder.typeBuilder("_Model").instanceClass(Model.class);
    dataModelBuilder.typeBuilder("AddressType").instanceClass(Address.class);
    return dataModelBuilder.createDataModel();
  }

}
