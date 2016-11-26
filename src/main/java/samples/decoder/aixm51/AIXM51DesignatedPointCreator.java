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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51MessageDataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.model.abstractfeature.ELcdAIXM51Interpretation;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMFeature;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51Feature;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeDesignatedPoint;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51Point;
import com.luciad.format.aixm51.model.features.navaidspoints.points.TLcdAIXM51DesignatedPointTimeSlice;
import com.luciad.format.gml32.model.ELcdGML32TimeIndeterminateValue;
import com.luciad.format.gml32.model.TLcdGML32CodeWithAuthority;
import com.luciad.format.gml32.model.TLcdGML32TimePeriod;
import com.luciad.format.gml32.model.TLcdGML32TimePosition;
import com.luciad.reference.TLcdGeodeticReference;

/**
 * A sample class to illustrate the creation of an AIXM 5.1 data set from scratch
 * by using the LuciadMap AIXM 5.1 API. The sample uses the creation of a data set
 * with one Designated Point as an example.
 * <p>
 * Luciad's AIXM 5.1 implementation quite closely follows the AIXM UML / XML design model. For an
 * optimal understanding of the code, it is therefore advised to get familiar first with the general
 * AIXM 5.1 design principles, i.e. the concept of messages, features and time slices.
 * </p>
 * <p>
 * In general, an AIXM 5.1 data set consists of a message, which is related to the concept of a
 * LuciadMap model in the API. This message / model contains one or more features, such as
 * Designated Points, Runways, Airports, Airspaces, ... Each such feature consists of one or more
 * time slices, that represent the state of the feature (i.e. its properties, like text attributes
 * or geometry) at a certain time instant.
 * </p>
 * <p>
 * The code below illustrates how to create a message (model) to store Designated Point
 * features and how to create a Designated Point feature with one time slice:
 * <ul>
 * <li>the creation of the feature and its time slice is done in the method
 *   createDesignatedPointFeature(),</li>
 * <li>the creation of a message (model) to which this feature can be added is done in the method
 *   createDesignatedPointMessage().</li>
 * </ul>
 * </p>
 * <p>
 * To use this code and visualize the result on a map, the result of createDesignatedPointMessage()
 * can be directly added to an AIXM 5.1 layer (e.g., through the sample class AIXM51LayerFactory)
 * which can then be added to the view.
 * </p>
 */
public class AIXM51DesignatedPointCreator {

  /**
   * Creates a message which is set up to store Designated Point feature instances.
   *
   * @return An empty AIXM basic message, configured to contain Designated Point instances.
   */
  public static TLcdAIXM51AbstractAIXMMessage createDesignatedPointMessage() {
    // Create a new message configured to contain Designated Point feature instances.
    TLcdDataType messageType = TLcdAIXM51MessageDataTypes.AIXM_BASIC_MESSSAGE_TYPE;
    TLcdAIXM51AbstractAIXMMessage message = ( TLcdAIXM51AbstractAIXMMessage ) messageType.newInstance();

    // In the LuciadMap API, an AIXM message is also a model, requiring the
    // typical model configuration settings: model reference and model descriptor. 
    message.setModelReference( new TLcdGeodeticReference() );
    TLcdAIXM51ModelDescriptor modelDescriptor =
        new TLcdAIXM51ModelDescriptor( "DesignatedPointCreator", "DesignatedPoint",
                                       TLcdAIXM51DataTypes.DesignatedPointType );
    message.setModelDescriptor( modelDescriptor );

    return message;
  }

  /**
   * Creates an AIXM 5.1 Designated Point feature, for illustration purposes.
   *
   * @return A Designated Point feature, with some values initialized.
   */
  public static TLcdAIXM51AbstractAIXMFeature createDesignatedPointFeature() {
    // 1. Create a Designated Point feature. The feature is initialized with a globally unique
    // identifier and a local GML 3.2 id.
    TLcdAIXM51Feature feature = new TLcdAIXM51Feature( TLcdAIXM51DataTypes.DesignatedPointType );
    TLcdGML32CodeWithAuthority identifier = new TLcdGML32CodeWithAuthority();
    identifier.setValueObject( "MyGloballyUniqueId" );
    feature.setIdentifier( identifier );
    feature.setId( "MyLocalId" );

    // 2. Create a time slice for the new feature.
    // In this example, we define one BASELINE time slice,
    // with a fixed start time and an unknown end time.
    // The time interval creation is done in the method createIndeterminateTimePeriod().
    TLcdAIXM51DesignatedPointTimeSlice timeSlice = new TLcdAIXM51DesignatedPointTimeSlice();
    timeSlice.setInterpretation( ELcdAIXM51Interpretation.BASELINE );
    timeSlice.setSequenceNumber( 0 );
    timeSlice.setValidTime( createIndeterminateTimePeriod() );
    feature.getTimeSlices().add( timeSlice );

    // 3. Initialize all desired properties on the Designated Point time slice:
    // - location (random example)
    TLcdAIXM51Point point = new TLcdAIXM51Point();
    point.move2D( 4, 50.7 );
    timeSlice.setLocation( point );

    // - Designated Point type
    timeSlice.setType( TLcdAIXM51CodeDesignatedPoint.ICAO );

    // - designator
    timeSlice.setDesignator( "DESIG" );

    // - designated point name
    timeSlice.setDesignatedPointName( "DESIG" );

    return feature;
  }

  /**
   * Creates a time period with a random start time and indeterminate end time,
   * for illustration purposes.
   *
   * @return a time period with a random start time and indeterminate end time.
   */
  public static TLcdGML32TimePeriod createIndeterminateTimePeriod() {
    TLcdGML32TimePeriod validTime = new TLcdGML32TimePeriod();
    TLcdGML32TimePosition beginTime = new TLcdGML32TimePosition();
    try {
      beginTime.setValueObject( DatatypeFactory.newInstance().newXMLGregorianCalendar( 2010, 8, 27, 8, 59, 0, 0, 0 ) );
    } catch ( DatatypeConfigurationException e ) {
      e.printStackTrace();
    }
    validTime.setBegin( beginTime );
    TLcdGML32TimePosition endTime = new TLcdGML32TimePosition();
    endTime.setIndeterminatePosition( ELcdGML32TimeIndeterminateValue.UNKNOWN );
    validTime.setEnd( endTime );
    return validTime;
  }
}
