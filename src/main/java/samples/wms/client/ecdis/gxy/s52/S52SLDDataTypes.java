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
package samples.wms.client.ecdis.gxy.s52;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataModelBuilder;
import com.luciad.format.xml.util.TLcdXMLEntityResolver;
import com.luciad.ogc.sld.model.ALcdSLDSymbolizer;
import com.luciad.ogc.sld.model.TLcdSEDataTypes;

/**
 * Data model for the S-52-SLD XML Schema.
 */
public class S52SLDDataTypes {

  private static final String S52SLD = "http://www.luciad.com/ecdis/s52-sld/1.0";

  private static final String SCHEMA_LOCATION = S52SLDDataTypes.class.getClassLoader().getResource("samples/wms/client/ecdis/s52-sld.xsd").toString();

  private static final String S52_SYMBOLIZER_TYPE_STRING = "S52SymbolizerType";
  private static final String DISPLAY_SETTINGS_STRING = "displaySettings";

  private static final TLcdDataModel DATA_MODEL;
  public static final TLcdDataType S52_SYMBOLIZER_TYPE;

  static {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder(S52SLD);

    // Initialize the model using the XML Schema information
    TLcdXMLDataModelBuilder xmlDataModelBuilder = new TLcdXMLDataModelBuilder(TLcdSEDataTypes.getDataModel(), S52DataTypes.getDataModel());
    xmlDataModelBuilder.setEntityResolver(new TLcdXMLEntityResolver());
    xmlDataModelBuilder.buildDataModel(dataModelBuilder, SCHEMA_LOCATION);

    // Configure the Java instance class for the Symbolizer type
    dataModelBuilder.typeBuilder(S52_SYMBOLIZER_TYPE_STRING).instanceClass(S52Symbolizer.class);
    DATA_MODEL = dataModelBuilder.createDataModel();

    S52_SYMBOLIZER_TYPE = DATA_MODEL.getDeclaredType(S52_SYMBOLIZER_TYPE_STRING);
  }

  public static TLcdDataModel getDataModel() {
    return DATA_MODEL;
  }

  private S52SLDDataTypes() {
  }

  public static class S52Symbolizer extends ALcdSLDSymbolizer {

    public S52Symbolizer() {
      super(S52_SYMBOLIZER_TYPE);
    }

    public S52Symbolizer(TLcdDataType aType) {
      super(aType);
    }

    public S52DataTypes.S52DisplaySettings getDisplaySettings() {
      return (S52DataTypes.S52DisplaySettings) getValue(DISPLAY_SETTINGS_STRING);
    }

    public void setDisplaySettings(S52DataTypes.S52DisplaySettings aDisplaySettings) {
      setValue(DISPLAY_SETTINGS_STRING, aDisplaySettings);
    }

  }

}
