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
package samples.decoder.aixm5;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm5.model.TLcdAIXM5DataTypes;
import com.luciad.format.aixm5.model.abstractfeature.TLcdAIXM5AbstractAIXMFeature;
import com.luciad.format.aixm5.model.abstractfeature.TLcdAIXM5AbstractAIXMTimeSlice;
import com.luciad.format.aixm5.model.datatypes.ELcdAIXM5UomDistance;
import com.luciad.format.aixm5.model.datatypes.ELcdAIXM5UomDistanceVertical;
import com.luciad.format.aixm5.model.datatypes.ELcdAIXM5UomTemperature;
import com.luciad.format.aixm5.model.datatypes.TLcdAIXM5OptionalValDistance;
import com.luciad.format.aixm5.model.datatypes.TLcdAIXM5ValDistance;
import com.luciad.format.aixm5.model.datatypes.TLcdAIXM5ValDistanceVertical;
import com.luciad.format.aixm5.model.datatypes.TLcdAIXM5ValTemperature;
import com.luciad.format.aixm5.model.features.airportheliport.airportheliport.TLcdAIXM5City;
import com.luciad.format.aixm5.model.features.airspace.TLcdAIXM5AirspaceGeometryComponent;
import com.luciad.format.aixm5.model.features.airspace.TLcdAIXM5AirspaceLayerClass;
import com.luciad.format.aixm5.model.features.airspace.TLcdAIXM5AirspaceVolume;
import com.luciad.format.aixm5.model.features.airspace.TLcdAIXM5AirspaceVolumeDependency;
import com.luciad.format.aixm5.model.features.geometry.TLcdAIXM5ElevatedPoint;
import com.luciad.format.aixm5.model.features.geometry.TLcdAIXM5Point;
import com.luciad.format.aixm5.model.features.obstacle.TLcdAIXM5VerticalStructurePart;
import com.luciad.format.aixm5.model.util.TLcdAIXM5Association;
import com.luciad.format.aixm5.model.util.TLcdAIXM5Link;
import com.luciad.format.gml32.model.TLcdGML32AbstractTimePrimitive;
import com.luciad.format.gml32.model.TLcdGML32TimeInstant;
import com.luciad.format.gml32.model.TLcdGML32TimePeriod;
import com.luciad.format.gml32.model.TLcdGML32TimePosition;
import com.luciad.format.gml32.model.TLcdGML32TimePrimitiveProperty;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdLonLatFormatter;

/**
 * A utility class to format AIXM 5.0 domain model objects as <code>String</code> objects.
 *
 */
public class AIXM5ElementFormatter {

  private static TLcdLonLatFormatter sLonLatFormatter = new TLcdLonLatFormatter();

  /**
   * Derives a <code>String</code> from <code>aObject</code>. The resulting string is a short description
   * of the contents of the object. For example, in case of a point, the coordinates are returned.
   *
   * @param aObject the AIXM 5.0 domain object for which a <code>String</code> should be created
   * @return A <code>String</code> if the type of aObject is supported, an empty <code>String</code> otherwise.
   */
  public static String objectToString(Object aObject) {
//    return aObject.toString();
    if ( aObject instanceof TLcdGML32TimePrimitiveProperty ) {
      return timePrimitivePropertyToString( ( TLcdGML32TimePrimitiveProperty ) aObject);
    }
    if ( aObject instanceof TLcdAIXM5Link<?> ) {
      Object link = ( ( TLcdAIXM5Link<?> ) aObject ).getObject();
      if ( link instanceof TLcdAIXM5ElevatedPoint )
        return elevatedPointPropertyToString( ( TLcdAIXM5ElevatedPoint ) link );
      if ( link instanceof TLcdAIXM5Point ) {
        return pointPropertyToString( ( TLcdAIXM5Point ) link );
      }
      if ( link instanceof TLcdAIXM5City ) {
        return cityToString( ( TLcdAIXM5City ) link );
      }
      if ( link instanceof TLcdAIXM5VerticalStructurePart ) {
        return verticalStructurePartPropertyToString( ( TLcdAIXM5VerticalStructurePart ) link );
      }
      if ( link instanceof TLcdAIXM5AirspaceLayerClass ) {
        return airspaceLayerClassPropertyToString( ( TLcdAIXM5AirspaceLayerClass ) link );
      }
      if ( link instanceof TLcdAIXM5AirspaceGeometryComponent ) {
        return airspaceGeometryComponentPropertyToString( ( TLcdAIXM5AirspaceGeometryComponent ) link );
      }
      return "";
    }
    if (  aObject instanceof TLcdAIXM5ValDistanceVertical ) {
      return distanceVerticalToString( ( TLcdAIXM5ValDistanceVertical ) aObject );
    }
    if (  aObject instanceof TLcdAIXM5ValDistance ) {
      return distanceToString( ( TLcdAIXM5ValDistance ) aObject );
    }
    if (aObject instanceof ILcdPoint) {
      return pointToString((ILcdPoint) aObject);
    }
    if (  aObject instanceof TLcdAIXM5ValTemperature ) {
      return temperatureToString( ( TLcdAIXM5ValTemperature ) aObject );
    }
    if ( aObject instanceof TLcdAIXM5AbstractAIXMFeature )
      return featureToString( ( TLcdAIXM5AbstractAIXMFeature ) aObject );
    if ( aObject instanceof ILcdDataObject ) {
      return featuredToString( ( ILcdDataObject ) aObject );
    }
    return "";
  }


  /**
   *
   * @param aFeature A feature of type <code>TLcdAIXM5AbstractAIXMFeature</code>.
   * @return A String describing aFeature.
   */
  private static String featureToString( TLcdAIXM5AbstractAIXMFeature aFeature) {
    return aFeature.getDataType().getName();
  }

  /**
   *
   * @param aObject ILcdFeatured of type AirspaceGeometryComponentPropertyType
   * @return  A String describing aProperty .
   */
  private static String airspaceGeometryComponentPropertyToString(TLcdAIXM5AirspaceGeometryComponent aObject) {
    try {
      TLcdAIXM5AirspaceVolume airspaceVolume = aObject.getTheAirspaceVolume();
      TLcdAIXM5AirspaceVolumeDependency contributor = airspaceVolume.getContributorAirspace();
      if ( contributor != null ) {
        return "Operation: " + aObject.getOperation() + " Contributor airspace: " + ( ( TLcdAIXM5Association<?> ) contributor.getValue( TLcdAIXM5AirspaceVolumeDependency.THE_AIRSPACE_PROPERTY ) ).getLinkInfo();
      }
      if ( airspaceVolume.getCentreline() != null ) {
        return "Corridor airspace with width: " + distanceToString( airspaceVolume.getWidth() );
      }
      return "Regular airspace: Lower: " + distanceVerticalToString( airspaceVolume.getLowerLimit() ) + " Upper: " + distanceVerticalToString( airspaceVolume.getUpperLimit() );
    }
    catch (NullPointerException e) {
      return "";
    }
  }

  /**
   *
   * @param aLayerClass ILcdFeatured of type AirspaceLayerClassPropertyType
   * @return  A String describing aProperty -.
   */
  private static String airspaceLayerClassPropertyToString(TLcdAIXM5AirspaceLayerClass aLayerClass) {
    try {
      return "Class: " + aLayerClass.getClassCode();
    }
    catch (RuntimeException e) {
      return "";
    }
  }

  /**
   *
   * @param aVerticalStructurePart  A ILcdFeatured of type VerticalStructurePartPropertyType
   * @return  A String describing aProperty .
   */
  private static String verticalStructurePartPropertyToString(TLcdAIXM5VerticalStructurePart aVerticalStructurePart) {
    if ( aVerticalStructurePart == null )
      return "";
    TLcdAIXM5OptionalValDistance verticalExtent = aVerticalStructurePart.getVerticalExtent();
    if ( verticalExtent != null ) {
      return "Height: " + distanceToString( verticalExtent );
    }
    return "";
  }

/**
 *
 * @param aDistance A ILcdFeatured of type ValDistanceType
 * @return  A String describing aDistance .
 *
 * @since 9.0
 */
  private static String distanceToString(TLcdAIXM5ValDistance aDistance) {
    Double value = aDistance.getValueObject();
    if (value != null) {
      ELcdAIXM5UomDistance uom = aDistance.getUom();
      if (uom != null) {
        return value.toString() + uom;
      }
      return value.toString();
    }
    return "";
  }

  /**
   *
   * @param aTemperature  ILcdFeatured of type ValTemperatureType
   * @return  A String describing aTemperature .
   */
  private static String temperatureToString(TLcdAIXM5ValTemperature aTemperature) {
    Double value = aTemperature.getValueObject();
    if (value != null) {
      ELcdAIXM5UomTemperature uom = aTemperature.getUom();
      if (uom != null) {
        return value.toString() + uom;
      }
      return value.toString();
    }
    return "";
  }

  /**
   *
   * @param aVertical ILcdFeatured of type ValDistanceVerticalType
   * @return   A String describing aFeature.
   */
  private static String distanceVerticalToString(TLcdAIXM5ValDistanceVertical aVertical) {
    String value = aVertical.getValueObject();
    ELcdAIXM5UomDistanceVertical uom = aVertical.getUom();
    if (uom != null) {
      return value + uom;
    }
    else
      return value;
  }

  /**
   *
   * @param aLink ILcdFeatured of type PointPropertyType
   * @return   A String describing aProperty.
   */
  private static String pointPropertyToString( TLcdAIXM5Point aLink ) {
    return pointToString( aLink );
  }

  /**
   *
   * @param aElevatedPoint  A ILcdFeatured of type ElevatedPointPropertyType
   * @return  A String describing aProperty.
   */
  private static String elevatedPointPropertyToString(TLcdAIXM5ElevatedPoint aElevatedPoint) {
    String pointString = pointToString( aElevatedPoint );
    String elevation = aElevatedPoint.getElevation() != null ? aElevatedPoint.getElevation().toString() : "";
    return pointString + " " + elevation;
  }


  /**
   *
   * @param aCity a ILcdFeatured of type CityPropertyType
   * @return   A String describing aProperty.
   */
  private static String cityToString(TLcdAIXM5City aCity) {
    return aCity == null ? "" : aCity.getCityName();
  }

  /**
   * This method tries to create a string from an ILcdFeatured by testing for some generally occuring feature names.
   * @param aObject An ILcdFeatured of unknown type
   * @return A valid String describing the contents of the unknown feature, or an empty String if the feature does not contain
   * some generally occuring elements.
   */
  private static String featuredToString(ILcdDataObject aObject) {
    TLcdDataProperty valueIndex = aObject.getDataType().getProperty( "value" );
    if (valueIndex != null && aObject.getValue(valueIndex) != null) {
      TLcdDataProperty uomIndex = aObject.getDataType().getProperty( "uom" );
      if (uomIndex != null && aObject.getValue(uomIndex) != null) {
        return aObject.getValue(valueIndex).toString() + aObject.getValue(uomIndex).toString();
      }
      return aObject.getValue(valueIndex).toString();
    }
    /*
     * timeslice property types do not have a common base class, but they all have the same xml name.
     */
    if ( isTimeSlicePropertyType( aObject.getDataType() ) ) {
      TLcdAIXM5AbstractAIXMTimeSlice timeslice = (TLcdAIXM5AbstractAIXMTimeSlice) aObject.getValue( aObject.getDataType().getProperties().get( 1 ) );
      return timeslice.getSequenceNumber();
    }
    return "";
  }

  private static boolean isTimeSlicePropertyType( TLcdDataType aDataObjectType ) {
    if ( aDataObjectType.getProperties().size() != 2 ) {
      return false;
    }
    TLcdDataProperty timesliceProperty = aDataObjectType.getProperties().get( 1 );
    return timesliceProperty.getName().equals( "TimeSlice" ) && TLcdAIXM5DataTypes.AbstractAIXMTimeSliceType.isAssignableFrom( timesliceProperty.getType() );
  }


  /**
   *
   * @param aPoint an ILcdPoint
   * @return A String describint aPoint
   */
  private static String pointToString( ILcdPoint aPoint) {
    return sLonLatFormatter.format(aPoint.getX(), aPoint.getY());
  }

  /**
   *
   * @param aTimePrimitiveProperty  ILcdFeatured of type gml32:TimePrimitivePropertyType
   * @return  A String describing aProperty.
   */
  private static String timePrimitivePropertyToString(TLcdGML32TimePrimitiveProperty aTimePrimitiveProperty) {
    TLcdGML32AbstractTimePrimitive timePrimitive = aTimePrimitiveProperty.getAbstractTimePrimitive();
    if ( timePrimitive instanceof TLcdGML32TimePeriod ) {
//      TLcdGML32TimePeriod time_period = (TLcdGML32TimePeriod) timePrimitive;

      return "From: " + ( ( TLcdGML32TimePosition ) ( ( TLcdGML32TimePeriod ) timePrimitive ).getBegin() ).getValueObject() +
              " To: " + ( ( TLcdGML32TimePosition ) ( ( TLcdGML32TimePeriod ) timePrimitive ).getEnd() ).getValueObject();
    }
    if ( timePrimitive instanceof TLcdGML32TimeInstant ) {
//      TLcdGML32TimeInstant time_instant = (TLcdGML32TimeInstant) timePrimitive;
      Object valueObject = ( ( TLcdGML32TimeInstant ) timePrimitive ).getTimePosition().getValueObject();
      return valueObject == null ? "" : valueObject.toString();
    }
    return "";
  }
}
