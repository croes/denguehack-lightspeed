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
package samples.lucy.fundamentals.tracks.model;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * Class containing the {@code ILcdDataModel} for the tracks
 */
public final class TracksDataTypes {

  private static final TLcdDataModel TRACKS_DATA_MODEL;

  public static final TLcdDataType TRACK_RECORDING_DATA_TYPE;

  public static final String FLIGHT_ID = "flightID";
  public static final String CALL_SIGN = "callSign";
  public static final String CURRENT_POSITION = "currentPosition";

  public static final String RECORDED_POSITIONS = "recordedPositions";
  public static final String TIME_STAMPS = "timeStamps";

  static final String TRACK_RECORDING_TYPE = "TrackRecordingType";

  static {
    TRACKS_DATA_MODEL = createDataModel();
    TRACK_RECORDING_DATA_TYPE = TRACKS_DATA_MODEL.getDeclaredType(TRACK_RECORDING_TYPE);
  }

  private static TLcdDataModel createDataModel() {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("http://www.mydomain.com/datamodel/TracksModel");

    TLcdDataTypeBuilder geometryType = builder.typeBuilder("GeometryType");
    geometryType.primitive(true).instanceClass(TLcdLonLatHeightPoint.class);

    TLcdDataTypeBuilder trackRecordingBuilder = builder.typeBuilder(TRACK_RECORDING_TYPE);
    trackRecordingBuilder.addProperty(FLIGHT_ID, TLcdCoreDataTypes.INTEGER_TYPE);
    trackRecordingBuilder.addProperty(CALL_SIGN, TLcdCoreDataTypes.STRING_TYPE);
    trackRecordingBuilder.addProperty(CURRENT_POSITION, geometryType);

    trackRecordingBuilder.addProperty(RECORDED_POSITIONS, geometryType).collectionType(TLcdDataProperty.CollectionType.LIST);
    trackRecordingBuilder.addProperty(TIME_STAMPS, TLcdCoreDataTypes.LONG_TYPE).collectionType(TLcdDataProperty.CollectionType.LIST);

    TLcdDataModel dataModel = builder.createDataModel();

    TLcdDataType type = dataModel.getDeclaredType(TRACK_RECORDING_TYPE);
    type.addAnnotation(new TLcdHasGeometryAnnotation(type.getProperty(CURRENT_POSITION)));

    return dataModel;
  }

  public static TLcdDataModel getDataModel() {
    return TRACKS_DATA_MODEL;
  }
}
