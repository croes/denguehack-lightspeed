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
package samples.lightspeed.demo.application.data.osm;

import java.util.HashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;

class RoadUtil {

  public enum HighwayType {
    INTERSTATE_ROUTE,
    US_ROUTE,
    STATE_ROUTE
  }

  public static final int NR_ROAD_PRIORITIES = 11;

  private static final Map<Integer, Integer> CODE_2_PRIORITY_MAP = new HashMap<Integer, Integer>();

  // Mapping of road class to priority,
  static {
    CODE_2_PRIORITY_MAP.put(5100, 9);
    // Major roads
    CODE_2_PRIORITY_MAP.put(5110, 0);
    CODE_2_PRIORITY_MAP.put(5111, 0);
    CODE_2_PRIORITY_MAP.put(5112, 1);
    CODE_2_PRIORITY_MAP.put(5113, 2);
    CODE_2_PRIORITY_MAP.put(5114, 3);
    CODE_2_PRIORITY_MAP.put(5115, 4);
    // Minor roads
    CODE_2_PRIORITY_MAP.put(5120, 5);
    CODE_2_PRIORITY_MAP.put(5121, 5);
    CODE_2_PRIORITY_MAP.put(5122, 6);
    CODE_2_PRIORITY_MAP.put(5123, 7);
    CODE_2_PRIORITY_MAP.put(5124, 8);
    CODE_2_PRIORITY_MAP.put(5130, 9);
    // Ramps
    CODE_2_PRIORITY_MAP.put(5131, 0);
    CODE_2_PRIORITY_MAP.put(5132, 1);
    CODE_2_PRIORITY_MAP.put(5133, 2);
    CODE_2_PRIORITY_MAP.put(5134, 3);
    // Very small roads
    CODE_2_PRIORITY_MAP.put(5140, 10);
    CODE_2_PRIORITY_MAP.put(5141, 10);
    CODE_2_PRIORITY_MAP.put(5142, 10);
    CODE_2_PRIORITY_MAP.put(5150, 10);
    // Paths unsuitable for cars
    CODE_2_PRIORITY_MAP.put(5151, 10);
    CODE_2_PRIORITY_MAP.put(5152, 10);
    CODE_2_PRIORITY_MAP.put(5153, 10);
    CODE_2_PRIORITY_MAP.put(5154, 10);
    CODE_2_PRIORITY_MAP.put(5155, 10);
    // Unknown
    CODE_2_PRIORITY_MAP.put(5199, 10);
  }

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(0.05, 0.1),
      new TLcdInterval(0.1, 0.2),
      new TLcdInterval(0.2, 0.5),
      new TLcdInterval(0.5, 1.0),
      new TLcdInterval(1.0, 2.0),
      new TLcdInterval(1.0, Double.MAX_VALUE)
  };

  private static final double[] GLOBAL_PRIORITIES = {
      1.0, 0.9, 0.8, 0.7, 0.6, 0.5
  };

  public static int getLevelOfDetail(ALspViewXYZWorldTransformation aV2W) {
    return LODSupport.getLevelOfDetail(aV2W, DETAIL_LEVELS);
  }

  public static boolean isVisible(Object aObject, int aLOD) {
    int code = (Integer) ((ILcdDataObject) aObject).getValue("code");
    return getRoadZOrder(code) <= aLOD * 2;
  }

  public static int getPriority(Object aObject, int aLargestPriority, int aSmallestPriority, int aLOD) {
    double relative_priority = getRelativePriority(aObject);
    double global_priority = getGlobalPriority(aLOD);
    double priority = global_priority * 0.5 + relative_priority * 0.5;
    return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
  }

  private static double getRelativePriority(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String fclass = data_object.getValue("fclass").toString();

    if ("motorway".equals(fclass)) {
      return 0.0;
    }
    if ("primary".equals(fclass)) {
      return 0.2;
    }
    if ("secondary".equals(fclass)) {
      return 0.4;
    }
    if ("tertiary".equals(fclass)) {
      return 0.6;
    }
    if ("residential".equals(fclass)) {
      return 0.8;
    }

    // Retrieve the priority of a label relative to an other label of the same layer.
    return 1.0;
  }

  public static boolean isMajorRoad(Object aObject) {
//    int code = ( Integer ) (( ILcdDataObject ) aObject).getValue("code");
//    Integer priority = CODE_2_PRIORITY_MAP.get( code );
//    return priority!=null && priority<5120;

    String fclass = ((ILcdDataObject) aObject).getValue("fclass").toString();

    if ("motorway".equals(fclass)) {
      return true;
    }
    if ("primary".equals(fclass)) {
      return true;
    }
    return false;
  }

  private static double getGlobalPriority(int aLOD) {
    return GLOBAL_PRIORITIES[aLOD];
  }

  /**
   * Returns the road priority corresponding to the given OpenStreetMap feature code.
   *
   * @param aRoadCode an OpenStreetMap feature code.
   *
   * @return the corresponding road priority.
   */
  public static int getRoadZOrder(int aRoadCode) {
    Integer i = CODE_2_PRIORITY_MAP.get(aRoadCode);
    return i != null ? i : NR_ROAD_PRIORITIES;
  }

  /**
   * Returns true if the given object is a highway, false otherwise.
   *
   * @param aObject an OpenStreetMap domain object.
   *
   * @return true if the given object is a highway, false otherwise.
   */
  public static boolean isHighway(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;
    int code = (Integer) dataObject.getValue("code");
    String ref = dataObject.getValue("ref").toString();
    if (code == 5110 || code == 5111) {
      if (ref.startsWith("I ") || ref.startsWith("US ") || ref.startsWith("CA ")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the ID of the given highway object.
   *
   * @param aHighway a given highway object.
   *
   * @return the ID of the given highway object.
   */
  public static HighwayId getHighwayId(Object aHighway) {
    ILcdDataObject road = (ILcdDataObject) aHighway;
    HighwayType type = getHighwayType(road);
    int number = getHighwayNumber(road);
    return new HighwayId(type, number);
  }

  /**
   * Return the type of the given highway object.
   *
   * @param aObject a given highway object.
   *
   * @return the type of the given highway object.
   */
  public static HighwayType getHighwayType(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;
    String ref = dataObject.getValue("ref").toString();

    if (ref.startsWith("I ")) {
      return HighwayType.INTERSTATE_ROUTE;
    } else if (ref.startsWith("US ")) {
      return HighwayType.US_ROUTE;
    } else if (ref.startsWith("CA ")) {
      return HighwayType.STATE_ROUTE;
    }

    throw new IllegalArgumentException("Unknown highway type : " + aObject);
  }

  /**
   * Return the number of the given highway object.
   *
   * @param aObject a highway object.
   *
   * @return the number of the given highway object.
   */
  public static int getHighwayNumber(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String ref = data_object.getValue("ref").toString();
    int i = 0;
    while ((i < ref.length()) && (!Character.isDigit(ref.charAt(i)))) {
      i++;
    }
    int j = i;
    while ((j < ref.length()) && (Character.isDigit(ref.charAt(j)))) {
      j++;
    }
    String number = ref.substring(i, j);
    return number.length() > 0 ? Integer.valueOf(number) : 0;
  }

  /**
   * Represents the unique ID of a highway: the highway type plus the highway number.
   */
  public static class HighwayId {
    private HighwayType fHighwayType;
    private int fHighwayNumber;

    private HighwayId(HighwayType aHighwayType, int aHighwayNumber) {
      fHighwayType = aHighwayType;
      fHighwayNumber = aHighwayNumber;
    }

    public int getHighwayNumber() {
      return fHighwayNumber;
    }

    public HighwayType getHighwayType() {
      return fHighwayType;
    }

    // Implementations for Object
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      HighwayId highwayId = (HighwayId) o;

      if (fHighwayNumber != highwayId.fHighwayNumber) {
        return false;
      }
      if (fHighwayType != highwayId.fHighwayType) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = fHighwayType.hashCode();
      result = 31 * result + fHighwayNumber;
      return result;
    }
  }

}
