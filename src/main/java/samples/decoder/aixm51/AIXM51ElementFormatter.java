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
package samples.decoder.aixm51;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMFeature;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMTimeSlice;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51ValDistance;
import com.luciad.format.aixm51.model.features.airportheliport.airportheliport.TLcdAIXM51City;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceGeometryComponent;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceLayerClass;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceVolume;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceVolumeDependency;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51ElevatedPoint;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51Point;
import com.luciad.format.aixm51.model.features.obstacle.TLcdAIXM51VerticalStructurePart;
import com.luciad.format.aixm51.model.util.TLcdAIXM51Association;
import com.luciad.format.aixm51.model.util.TLcdAIXM51Link;
import com.luciad.format.gml32.model.TLcdGML32AbstractTimePrimitive;
import com.luciad.format.gml32.model.TLcdGML32TimeInstant;
import com.luciad.format.gml32.model.TLcdGML32TimePeriod;
import com.luciad.format.gml32.model.TLcdGML32TimePosition;
import com.luciad.format.gml32.model.TLcdGML32TimePrimitiveProperty;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;

/**
 * A utility class to format AIXM 5.1 domain model objects as <code>String</code> objects.
 */
public class AIXM51ElementFormatter {

  private static TLcdLonLatFormatter sLonLatFormatter = new TLcdLonLatFormatter();

  /**
   * Derives a <code>String</code> from <code>aObject</code>. The resulting string is a short description
   * of the contents of the object. For example, in case of a point, the coordinates are returned.
   *
   * @param aObject the AIXM 5.1 domain object for which a <code>String</code> should be created
   * @return A <code>String</code> if the type of aObject is supported, an empty <code>String</code> otherwise.
   */
  public static String objectToString(Object aObject) {
    if (aObject instanceof TLcdGML32TimePrimitiveProperty) {
      return timePrimitivePropertyToString((TLcdGML32TimePrimitiveProperty) aObject);
    }
    if (aObject instanceof TLcdAIXM51Link<?>) {
      Object link = ((TLcdAIXM51Link<?>) aObject).getObject();
      if (link instanceof TLcdAIXM51ElevatedPoint)
        return elevatedPointPropertyToString((TLcdAIXM51ElevatedPoint) link);
      if (link instanceof TLcdAIXM51Point) {
        return pointPropertyToString((TLcdAIXM51Point) link);
      }
      if (link instanceof TLcdAIXM51City) {
        return cityToString((TLcdAIXM51City) link);
      }
      if (link instanceof TLcdAIXM51VerticalStructurePart) {
        return verticalStructurePartPropertyToString((TLcdAIXM51VerticalStructurePart) link);
      }
      if (link instanceof TLcdAIXM51AirspaceLayerClass) {
        return airspaceLayerClassPropertyToString((TLcdAIXM51AirspaceLayerClass) link);
      }
      if (link instanceof TLcdAIXM51AirspaceGeometryComponent) {
        return airspaceGeometryComponentPropertyToString((TLcdAIXM51AirspaceGeometryComponent) link);
      }
      return "";
    }
    if ( aObject instanceof ILcdISO19103Measure ) {
      return measureToString( (ILcdISO19103Measure) aObject );
    }
    if (aObject instanceof ILcdPoint) {
      return pointToString((ILcdPoint) aObject);
    }
    if (aObject instanceof TLcdAIXM51AbstractAIXMFeature)
      return featureToString((TLcdAIXM51AbstractAIXMFeature) aObject);
    if (aObject instanceof ILcdDataObject) {
      return featuredToString((ILcdDataObject) aObject);
    }
    return "";
  }



  /**
   * Converts a <code>TLcdAIXM51AbstractAIXMFeature</code> to a String.
   *
   * @param aFeature An object of type <code>TLcdAIXM5AbstractAIXMFeature</code>.
   * @return A String describing aFeature.
   */
  private static String featureToString(TLcdAIXM51AbstractAIXMFeature aFeature) {
    return aFeature.getDataType().getName();
  }

  /**
   * Converts a <code>TLcdAIXM51AirspaceGeometryComponent</code> to a String.
   *
   * @param aObject An object of type TLcdAIXM51AirspaceGeometryComponent</code>
   * @return A String describing aObject.
   */
  private static String airspaceGeometryComponentPropertyToString(TLcdAIXM51AirspaceGeometryComponent aObject) {
    if (aObject == null) {
      return "";
    }
    TLcdAIXM51AirspaceVolume airspaceVolume = aObject.getTheAirspaceVolume();
    if (airspaceVolume == null) {
      return null;
    }
    TLcdAIXM51AirspaceVolumeDependency contributor = airspaceVolume.getContributorAirspace();
    if (contributor != null) {
      return "Operation: " + aObject.getOperation() + " Contributor airspace: " + ((TLcdAIXM51Association<?>) contributor.getValue(TLcdAIXM51AirspaceVolumeDependency.THE_AIRSPACE_PROPERTY)).getLinkInfo();
    }
    if (airspaceVolume.getCentreline() != null) {
      return "Corridor airspace with width: " + measureToString( airspaceVolume.getWidth() );
    }
    return "Regular airspace: Lower: " + measureToString( airspaceVolume.getLowerLimit() ) + " Upper: " + measureToString( airspaceVolume.getUpperLimit() );
  }

  /**
   * Converts a <code>TLcdAIXM51AirspaceLayerClass</code> to a String.
   *
   * @param aLayerClass An object of type <code>TLcdAIXM51AirspaceLayerClass</code>
   * @return A String describing aLayerClass.
   */
  private static String airspaceLayerClassPropertyToString(TLcdAIXM51AirspaceLayerClass aLayerClass) {
    try {
      return "Class: " + aLayerClass.getClassification();
    }
    catch (RuntimeException e) {
      return "";
    }
  }

  /**
   * Converts a <code>TLcdAIXM51VerticalStructurePart</code> to a String.
   *
   * @param aVerticalStructurePart An object of type <code>TLcdAIXM51VerticalStructurePart</code>
   * @return A String describing aVerticalStructurePart.
   */
  private static String verticalStructurePartPropertyToString(TLcdAIXM51VerticalStructurePart aVerticalStructurePart) {
    if (aVerticalStructurePart == null)
      return "";
    TLcdAIXM51ValDistance verticalExtent = aVerticalStructurePart.getVerticalExtent();
    if (verticalExtent != null) {
      return "Height: " + measureToString( verticalExtent );
    }
    return "";
  }



  /**
   * Converts a <code>ILcdISO19103Measure</code> to a String.
   * 
   * @param aMeasure
   *          An object of type <code>ILcdISO19103Measure</code>
   * @return A String describing aMeasure.
   */
  private static String measureToString( ILcdISO19103Measure aMeasure ) {
    double value = aMeasure.getValue();
    if ( !Double.isNaN( value ) ) {
      ILcdISO19103UnitOfMeasure uom = aMeasure.getUnitOfMeasure();
      if ( uom != null ) {
        return value + uom.getUOMSymbol();
      }
      return value + "";

    } else {
      return "";
    }
  }


  /**
   * Converts a <code>TLcdAIXM51Point</code> to a String.
   *
   * @param aLink An object of type <code>TLcdAIXM51Point</code>
   * @return A String describing aLink.
   */
  private static String pointPropertyToString(TLcdAIXM51Point aLink) {
    return pointToString(aLink);
  }

  /**
   * Converts a <code>TLcdAIXM51ElevatedPoint</code> to a String.
   *
   * @param aElevatedPoint An object of type <code>TLcdAIXM51ElevatedPoint</code>
   * @return A String describing aElevatedPoint.
   */
  private static String elevatedPointPropertyToString(TLcdAIXM51ElevatedPoint aElevatedPoint) {
    String pointString = pointToString(aElevatedPoint);
    String elevation = aElevatedPoint.getElevation() != null ? measureToString( aElevatedPoint.getElevation() ) : "";
    return pointString + " " + elevation;
  }

  /**
   * Converts a <code>TLcdAIXM51City</code> to a String.
   *
   * @param aCity An object of type <code>TLcdAIXM51City</code>
   * @return A String describing aCity.
   */
  private static String cityToString(TLcdAIXM51City aCity) {
    if (aCity != null) {
      return aCity.getCityName();
    }
    else {
      return "";
    }
  }

  /**
   * This method tries to create a string from an <code>ILcdDataObject</code>
   * by testing for some generally occurring feature names.
   *
   * @param aObject An <code>ILcdDataObject</code> of unknown type
   * @return A valid String describing the contents of the unknown <code>ILcdDataObject</code>,
   *         or an empty String if it does not contain some generally occurring elements.
   */
  private static String featuredToString(ILcdDataObject aObject) {
    TLcdDataProperty valueIndex = aObject.getDataType().getProperty("value");
    if (valueIndex != null && aObject.getValue(valueIndex) != null) {
      TLcdDataProperty uomIndex = aObject.getDataType().getProperty("uom");
      if (uomIndex != null && aObject.getValue(uomIndex) != null) {
        return aObject.getValue(valueIndex).toString() + aObject.getValue(uomIndex).toString();
      }
      return aObject.getValue(valueIndex).toString();
    }

    // Note: timeslice property types do not have a common base class, but they all have the same xml name.
    if (isTimeSlicePropertyType(aObject.getDataType())) {
      TLcdAIXM51AbstractAIXMTimeSlice timeslice = (TLcdAIXM51AbstractAIXMTimeSlice) aObject.getValue(aObject.getDataType().getProperties().get(1));
      return String.valueOf(timeslice.getSequenceNumber());
    }
    return "";
  }

  private static boolean isTimeSlicePropertyType(TLcdDataType aDataObjectType) {
    if (aDataObjectType.getProperties().size() != 2) {
      return false;
    }
    TLcdDataProperty timesliceProperty = aDataObjectType.getProperties().get(1);
    return timesliceProperty.getName().equals("TimeSlice") && TLcdAIXM51DataTypes.AbstractAIXMTimeSliceType.isAssignableFrom(timesliceProperty.getType());
  }

  /**
   * Converts an <code>ILcdPoint</code> to a String.
   *
   * @param aPoint an <code>ILcdPoint</code>
   * @return A String describing aPoint
   */
  private static String pointToString(ILcdPoint aPoint) {
    return sLonLatFormatter.format(aPoint.getX(), aPoint.getY());
  }

  /**
   * Converts an <code>TLcdGML32TimePrimitiveProperty</code> to a String.
   *
   * @param aTimePrimitiveProperty An object of type <code>TLcdGML32TimePrimitiveProperty</code>
   * @return A String describing aTimePrimitiveProperty.
   */
  private static String timePrimitivePropertyToString(TLcdGML32TimePrimitiveProperty aTimePrimitiveProperty) {
    TLcdGML32AbstractTimePrimitive timePrimitive = aTimePrimitiveProperty.getAbstractTimePrimitive();
    if (timePrimitive instanceof TLcdGML32TimePeriod) {

      return "From: " + ((TLcdGML32TimePosition) ((TLcdGML32TimePeriod) timePrimitive).getBegin()).getValueObject() +
              " To: " + ((TLcdGML32TimePosition) ((TLcdGML32TimePeriod) timePrimitive).getEnd()).getValueObject();
    }
    if (timePrimitive instanceof TLcdGML32TimeInstant) {
      return ((TLcdGML32TimeInstant) timePrimitive).getTimePosition().getValueObject().toString();
    }
    return "";
  }
}
