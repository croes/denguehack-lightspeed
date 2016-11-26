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
package samples.lucy.cop.addons.airpicturetheme;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;

import samples.lucy.cop.addons.missioncontroltheme.WebSocketModel;

/**
 * Model representing the track data
 *
 */
final class TracksModel extends WebSocketModel {

  private static final String TYPE_NAME = "samples.lucy.cop.tracks";

  public static final TLcdDataModel DATA_MODEL;
  public static final TLcdDataType TRACK_TYPE;
  public static final TLcdDataProperty ID_PROPERTY;
  public static final TLcdDataProperty CALLSIGN_PROPERTY;
  public static final TLcdDataProperty HEADING_PROPERTY;

  static {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("samples.lucy.cop.tracks");
    TLcdDataTypeBuilder track = builder.typeBuilder("track");
    track.addProperty("callsign", TLcdCoreDataTypes.STRING_TYPE);
    track.addProperty("heading", TLcdCoreDataTypes.DOUBLE_TYPE);
    track.addProperty("uid", TLcdCoreDataTypes.LONG_TYPE);

    DATA_MODEL = builder.createDataModel();
    TRACK_TYPE = DATA_MODEL.getDeclaredType("track");
    CALLSIGN_PROPERTY = TRACK_TYPE.getProperty("callsign");
    HEADING_PROPERTY = TRACK_TYPE.getProperty("heading");
    ID_PROPERTY = TRACK_TYPE.getProperty("uid");
  }

  private static final String TRACKS_PREFIX = "tracks.";

  TracksModel(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(aPropertiesPrefix + TRACKS_PREFIX, aProperties, TYPE_NAME, "Tracks", DATA_MODEL, TRACK_TYPE, ID_PROPERTY.getName(), aLucyEnv);
  }
}
