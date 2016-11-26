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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.shape.ALcdShape;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;

import samples.common.model.csv.CSVModelDecoder;

/**
 * <p>
 *   {@code ILcdModelDecoder} for the tracks data.
 *   The tracks data files (extension {@code .trc}) are CSV files using ';' as separator.
 * </p>
 *
 * <p>
 *   Each row of a tracks file contains the following information:
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>Flight ID</b>: a unique identifier for the flight.
 *     All entries with the same flight id are considered recordings of the same flight from point A to B.
 *   </li>
 *   <li>
 *     <b>Call sign</b>: the call sign of the plane
 *   </li>
 *   <li>
 *     <b>Longitude</b>: the longitude of the plane, at the time of the recording
 *   </li>
 *   <li>
 *     <b>Latitude</b>: the latitude of the plane, at the time of the recording
 *   </li>
 *   <li>
 *     <b>Altitude</b>: the altitude of the plane, at the time of the recording
 *   </li>
 *   <li>
 *     <b>Time stamp</b>: the time of the recording
 *   </li>
 * </ul>
 */
public class TracksModelDecoder implements ILcdModelDecoder {

  private static final String TYPE_NAME = "samples.lucy.fundamentals.tracks.model.TracksModelDecoder.tracks";
  private static final String EXTENSION = ".trc";

  private final CSVModelDecoder fCSVModelDecoder = new CSVModelDecoder();

  public TracksModelDecoder() {
    fCSVModelDecoder.setExtensions(new String[]{EXTENSION});
  }

  @Override
  public String getDisplayName() {
    return "Tracks";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.endsWith(EXTENSION);
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    ILcdModel csvModel = fCSVModelDecoder.decode(aSourceName);

    Map<Integer, List<ILcdDataObject>> flightIDToRecordings = groupByFlightID(csvModel);
    List<ILcdDataObject> tracks = new ArrayList<>();

    for (Map.Entry<Integer, List<ILcdDataObject>> flightIDToRecordingsMap : flightIDToRecordings.entrySet()) {

      Integer flightID = flightIDToRecordingsMap.getKey();
      List<ILcdDataObject> allRecordingsForFlight = flightIDToRecordingsMap.getValue();
      Collections.sort(allRecordingsForFlight, new Comparator<ILcdDataObject>() {
        @Override
        public int compare(ILcdDataObject firstRecording, ILcdDataObject secondRecording) {
          return Long.compare(Long.valueOf((String) firstRecording.getValue("TimeStamp")), Long.valueOf((String) secondRecording.getValue("TimeStamp")));
        }
      });

      List<TLcdLonLatHeightPoint> allLocations = new ArrayList<>();
      List<Long> timeStamps = new ArrayList<>(allRecordingsForFlight.size());

      for (ILcdDataObject recording : allRecordingsForFlight) {
        timeStamps.add(Long.valueOf((String) recording.getValue("TimeStamp")));
        allLocations.add((TLcdLonLatHeightPoint) ALcdShape.fromDomainObject(recording));
      }

      Object callSign = allRecordingsForFlight.get(0).getValue("CallSign");
      TLcdLonLatHeightPoint currentLocation = allLocations.get(0);

      // Group all the individual points together into a track
      TLcdDataObject track = new TLcdDataObject(TracksDataTypes.TRACK_RECORDING_DATA_TYPE);
      track.setValue(TracksDataTypes.FLIGHT_ID, flightID);
      track.setValue(TracksDataTypes.CALL_SIGN, callSign);
      track.setValue(TracksDataTypes.CURRENT_POSITION, currentLocation);
      track.setValue(TracksDataTypes.RECORDED_POSITIONS, allLocations);
      track.setValue(TracksDataTypes.TIME_STAMPS, timeStamps);
      tracks.add(track);
    }

    TLcdDataModelDescriptor tracksModelDescriptor = new TLcdDataModelDescriptor(
        aSourceName,
        TYPE_NAME,
        "Tracks",
        TracksDataTypes.getDataModel(),
        Collections.singleton(TracksDataTypes.TRACK_RECORDING_DATA_TYPE),
        Collections.singleton(TracksDataTypes.TRACK_RECORDING_DATA_TYPE)
    );

    ILcdModelReference modelReference = csvModel.getModelReference();
    return new TracksModel(modelReference, tracksModelDescriptor, tracks);
  }

  private Map<Integer, List<ILcdDataObject>> groupByFlightID(ILcdModel aCSVModel) {
    Enumeration elements = aCSVModel.elements();

    Map<Integer, List<ILcdDataObject>> flightIDToRecordingsMap = new HashMap<>();

    while (elements.hasMoreElements()) {
      ILcdDataObject domainObject = (ILcdDataObject) elements.nextElement();

      int flightID = Integer.valueOf((String) domainObject.getValue("FlightID"));
      List<ILcdDataObject> recordingsForFlightID = flightIDToRecordingsMap.get(flightID);
      if (recordingsForFlightID == null) {
        recordingsForFlightID = new ArrayList<>();
        flightIDToRecordingsMap.put(flightID, recordingsForFlightID);
      }
      recordingsForFlightID.add(domainObject);
    }
    return flightIDToRecordingsMap;
  }
}
