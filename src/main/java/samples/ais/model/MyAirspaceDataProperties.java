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
// #disclaimer
// #includefilefor AIS
package samples.ais.model;

import static samples.ais.model.CustomAISDataTypes.MyAirspaceType;

import com.luciad.datamodel.TLcdDataProperty;

/**
 * This class provides easy access to the properties of the custom AIS Airspace type.
 * 
 * @see CustomAISDataTypes#MyAirspaceType
 */
public final class MyAirspaceDataProperties {

  private MyAirspaceDataProperties() {
    // Private constructor - class is only used to define constants.
    super();
  }

  /**
   * The name of an airspace. This property is of the type String (<code>TLcdCoreDataTypes#STRING_TYPE</code>).
   */
  public static final TLcdDataProperty NAME                  = MyAirspaceType.getProperty("Name");

  /**
   * The type of an airspace. This property is of the type <code>TLcdAISDataTypes#AirspaceType</code>.
   */
  public static final TLcdDataProperty TYPE                  = MyAirspaceType.getProperty("Type");

  /**
   * The class of an airspace. This property is of the type <code>TLcdAISDataTypes#AirspaceClass</code>.
   */
  public static final TLcdDataProperty CLASS                 = MyAirspaceType.getProperty("Class");

  /**
   * The lower limit of an airspace. This property is of the type Float (<code>TLcdCoreDataTypes#FLOAT_TYPE</code>).
   */
  public static final TLcdDataProperty LOWER_LIMIT           = MyAirspaceType.getProperty("Lower_Limit");

  /**
   * The lower limit reference of an airspace. This property is of the type
   * <code>TLcdAISDataTypes#AltitudeReference</code>.
   */
  public static final TLcdDataProperty LOWER_LIMIT_REFERENCE = MyAirspaceType.getProperty("Lower_Limit_Reference");

  /**
   * The lower limit unit of an airspace. This property is of the type <code>TLcdAISDataTypes#AltitudeUnit</code>.
   */
  public static final TLcdDataProperty LOWER_LIMIT_UNIT      = MyAirspaceType.getProperty("Lower_Limit_Unit");

  /**
   * The upper limit of an airspace. This property is of the type Float (<code>TLcdCoreDataTypes#FLOAT_TYPE</code>).
   */
  public static final TLcdDataProperty UPPER_LIMIT           = MyAirspaceType.getProperty("Upper_Limit");

  /**
   * The upper limit reference of an airspace. This property is of the type
   * <code>TLcdAISDataTypes#AltitudeReference</code>.
   */
  public static final TLcdDataProperty UPPER_LIMIT_REFERENCE = MyAirspaceType.getProperty("Upper_Limit_Reference");

  /**
   * The upper limit unit of an airspace. This property is of the type <code>TLcdAISDataTypes#AltitudeUnit</code>.
   */
  public static final TLcdDataProperty UPPER_LIMIT_UNIT      = MyAirspaceType.getProperty("Upper_Limit_Unit");
}
