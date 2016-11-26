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
package samples.lucy.editabletables.model;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.ILcdDataObjectFactory;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.iso19103.TLcdISO19103MeasureAnnotation;

/**
 * Simple data model for the <code>EditableTablesPoint</code>.
 */
public class EditableTablesDataTypes {

  public static final TLcdDataModel DATA_MODEL;
  public static final TLcdDataType DATA_TYPE;
  public static final TLcdDataType SUB_DATA_TYPE;

  public static final TLcdDataProperty SUB_STRING_PROPERTY;
  public static final TLcdDataProperty SUB_BOOLEAN_PROPERTY;
  public static final TLcdDataProperty SUB_FLOAT_PROPERTY;
  public static final TLcdDataProperty SUB_SHORT_PROPERTY;

  public static final TLcdDataProperty STRING_PROPERTY;
  public static final TLcdDataProperty BOOLEAN_PROPERTY;
  public static final TLcdDataProperty DOUBLE_PROPERTY;
  public static final TLcdDataProperty INTEGER_PROPERTY;
  public static final TLcdDataProperty READ_ONLY_INT_PROPERTY;
  public static final TLcdDataProperty ENUMERATION_PROPERTY;
  public static final TLcdDataProperty MEASURE_PROPERTY;
  public static final TLcdDataProperty DATA_OBJECT_PROPERTY;

  private static final String DATA_TYPE_NAME = "SampleDataType";
  private static final String ENUMERATION_TYPE_NAME = "EnumerationType";
  private static final String SUB_DATA_TYPE_NAME = "SubDataType";

  private static final String SUB_STRING_PROPERTY_NAME = "SubString";
  private static final String SUB_BOOLEAN_PROPERTY_NAME = "SubBoolean";
  private static final String SUB_FLOAT_PROPERTY_NAME = "SubFloat";
  private static final String SUB_SHORT_PROPERTY_NAME = "SubShort";

  private static final String STRING_PROPERTY_NAME = "String";
  private static final String BOOLEAN_PROPERTY_NAME = "Boolean";
  private static final String DOUBLE_PROPERTY_NAME = "Double";
  private static final String INTEGER_PROPERTY_NAME = "Integer";
  private static final String READ_ONLY_INT_PROPERTY_NAME = "ReadOnlyInteger";
  private static final String ENUMERATION_PROPERTY_NAME = "Enumeration";
  private static final String MEASURE_PROPERTY_NAME = "AltitudeMeasure";
  private static final String DATA_OBJECT1_PROPERTY_NAME = "DataObject";

  static {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder("SampleDataModel");

    dataModelBuilder.typeBuilder(ENUMERATION_TYPE_NAME).
        instanceClass(Direction.class);

    //Create data type for nested data objects
    TLcdDataTypeBuilder subTypeBuilder = dataModelBuilder.typeBuilder(SUB_DATA_TYPE_NAME);
    subTypeBuilder.instanceClass(TLcdDataObject.class);
    subTypeBuilder.dataObjectFactory(new SubDataTypeFactory());
    subTypeBuilder.addProperty(SUB_STRING_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE).nullable(false);
    subTypeBuilder.addProperty(SUB_BOOLEAN_PROPERTY_NAME, TLcdCoreDataTypes.BOOLEAN_TYPE);
    subTypeBuilder.addProperty(SUB_FLOAT_PROPERTY_NAME, TLcdCoreDataTypes.FLOAT_TYPE).nullable(false);
    subTypeBuilder.addProperty(SUB_SHORT_PROPERTY_NAME, TLcdCoreDataTypes.SHORT_TYPE);

    // Create data type for the domain objects.
    TLcdDataTypeBuilder dataTypeBuilder = dataModelBuilder.typeBuilder(DATA_TYPE_NAME);
    dataTypeBuilder.addProperty(STRING_PROPERTY_NAME, TLcdCoreDataTypes.STRING_TYPE);
    dataTypeBuilder.addProperty(BOOLEAN_PROPERTY_NAME, TLcdCoreDataTypes.BOOLEAN_TYPE);
    dataTypeBuilder.addProperty(DOUBLE_PROPERTY_NAME, TLcdCoreDataTypes.DOUBLE_TYPE);
    dataTypeBuilder.addProperty(INTEGER_PROPERTY_NAME, TLcdCoreDataTypes.INTEGER_TYPE);
    dataTypeBuilder.addProperty(READ_ONLY_INT_PROPERTY_NAME, TLcdCoreDataTypes.INTEGER_TYPE);
    dataTypeBuilder.addProperty(ENUMERATION_PROPERTY_NAME, ENUMERATION_TYPE_NAME);
    dataTypeBuilder.addProperty(MEASURE_PROPERTY_NAME, TLcdCoreDataTypes.DOUBLE_TYPE).
        annotate(new TLcdISO19103MeasureAnnotation(TLcdAltitudeUnit.FEET));
    dataTypeBuilder.addProperty(DATA_OBJECT1_PROPERTY_NAME, SUB_DATA_TYPE_NAME);

    //Assign everything to constants for easy access.
    DATA_MODEL = dataModelBuilder.createDataModel();
    SUB_DATA_TYPE = DATA_MODEL.getDeclaredType(SUB_DATA_TYPE_NAME);
    DATA_TYPE = DATA_MODEL.getDeclaredType(DATA_TYPE_NAME);

    SUB_STRING_PROPERTY = SUB_DATA_TYPE.getProperty(SUB_STRING_PROPERTY_NAME);
    SUB_BOOLEAN_PROPERTY = SUB_DATA_TYPE.getProperty(SUB_BOOLEAN_PROPERTY_NAME);
    SUB_FLOAT_PROPERTY = SUB_DATA_TYPE.getProperty(SUB_FLOAT_PROPERTY_NAME);
    SUB_SHORT_PROPERTY = SUB_DATA_TYPE.getProperty(SUB_SHORT_PROPERTY_NAME);

    STRING_PROPERTY = DATA_TYPE.getProperty(STRING_PROPERTY_NAME);
    BOOLEAN_PROPERTY = DATA_TYPE.getProperty(BOOLEAN_PROPERTY_NAME);
    DOUBLE_PROPERTY = DATA_TYPE.getProperty(DOUBLE_PROPERTY_NAME);
    INTEGER_PROPERTY = DATA_TYPE.getProperty(INTEGER_PROPERTY_NAME);
    READ_ONLY_INT_PROPERTY = DATA_TYPE.getProperty(READ_ONLY_INT_PROPERTY_NAME);
    ENUMERATION_PROPERTY = DATA_TYPE.getProperty(ENUMERATION_PROPERTY_NAME);
    MEASURE_PROPERTY = DATA_TYPE.getProperty(MEASURE_PROPERTY_NAME);
    DATA_OBJECT_PROPERTY = DATA_TYPE.getProperty(DATA_OBJECT1_PROPERTY_NAME);
  }

  public enum Direction {

    LEFT("Left"), RIGHT("Right"), UP("Up"), DOWN("Down");

    private final String fDisplayName;

    Direction(String aDisplayName) {
      fDisplayName = aDisplayName;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }
  }

  private static class SubDataTypeFactory implements ILcdDataObjectFactory {
    @Override
    public ILcdDataObject newInstance(TLcdDataType aType) {
      TLcdDataObject object = new TLcdDataObject(aType);
      object.setValue(SUB_STRING_PROPERTY_NAME, "Substring");
      object.setValue(SUB_FLOAT_PROPERTY_NAME, 0.0f);
      return object;
    }
  }
}
