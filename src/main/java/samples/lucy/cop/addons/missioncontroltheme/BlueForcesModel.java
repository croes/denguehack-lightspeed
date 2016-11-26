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

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * Blue forces model
 */
final class BlueForcesModel extends WebSocketModel {

  private static final TLcdDataModel DATA_MODEL;
  private static final TLcdDataType BLUE_FORCES_TYPE;
  private static final TLcdDataProperty ID_PROPERTY;
  static final TLcdDataProperty CODE_PROPERTY;
  static final TLcdDataProperty NAME_PROPERTY;
  static final TLcdDataProperty EFFECTIVENESS_PROPERTY;

  static {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("samples.lucy.cop.addons.missioncontroltheme.BlueForcesModel");
    TLcdDataTypeBuilder blueForces = builder.typeBuilder("BlueForces");
    blueForces.addProperty("id", TLcdCoreDataTypes.LONG_TYPE);
    blueForces.addProperty("name", TLcdCoreDataTypes.STRING_TYPE);
    blueForces.addProperty("code", TLcdCoreDataTypes.STRING_TYPE);
    blueForces.addProperty("heading", TLcdCoreDataTypes.STRING_TYPE);
    blueForces.addProperty("combatEffectiveness", TLcdCoreDataTypes.DOUBLE_TYPE);
    blueForces.addProperty("fuel", TLcdCoreDataTypes.STRING_TYPE);

    DATA_MODEL = builder.createDataModel();
    BLUE_FORCES_TYPE = DATA_MODEL.getDeclaredType("BlueForces");
    ID_PROPERTY = BLUE_FORCES_TYPE.getProperty("id");
    CODE_PROPERTY = BLUE_FORCES_TYPE.getProperty("code");
    NAME_PROPERTY = BLUE_FORCES_TYPE.getProperty("name");
    EFFECTIVENESS_PROPERTY = BLUE_FORCES_TYPE.getProperty("combatEffectiveness");
  }

  private static final String BLUE_FORCES_PREFIX = "blueforces.";
  private static final String TYPE_NAME = "samples.lucy.cop.addons.missioncontroltheme.BlueForcesModel";

  BlueForcesModel(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(aPropertiesPrefix + BLUE_FORCES_PREFIX, aProperties, TYPE_NAME, "Blue forces", DATA_MODEL, BLUE_FORCES_TYPE, ID_PROPERTY.getName(), aLucyEnv);
  }
}
