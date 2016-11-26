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

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51MessageDataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.model.abstractfeature.ELcdAIXM51Interpretation;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMFeature;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51Feature;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeVerticalStructure;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeYesNo;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51ElevatedPoint;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51Point;
import com.luciad.format.aixm51.model.features.obstacle.TLcdAIXM51VerticalStructurePart;
import com.luciad.format.aixm51.model.features.obstacle.TLcdAIXM51VerticalStructureTimeSlice;
import com.luciad.format.aixm51.xml.TLcdAIXM51ModelEncoder;
import com.luciad.format.gml32.model.ELcdGML32TimeIndeterminateValue;
import com.luciad.format.gml32.model.TLcdGML32CodeWithAuthority;
import com.luciad.format.gml32.model.TLcdGML32TimePeriod;
import com.luciad.format.gml32.model.TLcdGML32TimePosition;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * A sample class to illustrate the creation of an AIXM 5.1 containing a Vertical Structure.
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
 * The code below illustrates how to create a message (model) to store Vertical Structure
 * features and how to create a Vertical Structure feature with one time slice:
 * <ul>
 * <li>the creation of the feature and its time slice is done in the method
 *   createVerticalStructureFeature(),</li>
 * <li>the creation of a message (model) to which this feature can be added is done in the method
 *   createVerticalStructureMessage().</li>
 * </ul>
 * </p>
 * <p>
 * To use this code and visualize the result on a map, the result of createVerticalStructureMessage()
 * can be directly added to an AIXM 5.1 layer (e.g., through the sample class AIXM51LayerFactory)
 * which can then be added to the view.
 * </p>
 */
public class AIXM51VerticalStructureCreator {

  /**
   * Creates a message which is set up to store Vertical Structure feature instances.
   *
   * @return An empty AIXM basic message, configured to contain Vertical Structure instances.
   */
  public static TLcdAIXM51AbstractAIXMMessage createVerticalStructureMessage() {
    // Create a new message configured to contain Vertical Structure feature instances.
    TLcdDataType messageType = TLcdAIXM51MessageDataTypes.AIXM_BASIC_MESSSAGE_TYPE;
    TLcdAIXM51AbstractAIXMMessage message = ( TLcdAIXM51AbstractAIXMMessage ) messageType.newInstance();

    // In the LuciadMap API, an AIXM message is also a model, requiring the
    // typical model configuration settings: model reference and model descriptor.
    message.setModelReference( new TLcdGeodeticReference() );
    TLcdAIXM51ModelDescriptor modelDescriptor =
        new TLcdAIXM51ModelDescriptor( "VerticalStructureCreator", "VerticalStructure",
                                       TLcdAIXM51DataTypes.VerticalStructureType );
    message.setModelDescriptor( modelDescriptor );

    return message;
  }

  /**
   * Creates an AIXM 5.1 Vertical Structure feature, for illustration purposes.
   *
   * @return A Vertical Structure feature, with some values initialized.
   */
  public static TLcdAIXM51AbstractAIXMFeature createVerticalStructureFeature() {
    // 1. Create a Vertical Structure feature. The feature is initialized with a globally unique
    // identifier and a local GML 3.2 id.
    TLcdAIXM51Feature feature = new TLcdAIXM51Feature( TLcdAIXM51DataTypes.VerticalStructureType );
    TLcdGML32CodeWithAuthority identifier = new TLcdGML32CodeWithAuthority();
    identifier.setValueObject( "MyGloballyUniqueId" );
    feature.setIdentifier( identifier );
    feature.setId( "MyLocalId" );

    // 2. Create a time slice for the new feature.
    // In this example, we define one BASELINE time slice,
    // with a fixed start time and an unknown end time.
    // The time interval creation is done in the method createIndeterminateTimePeriod().
    TLcdAIXM51VerticalStructureTimeSlice timeSlice = new TLcdAIXM51VerticalStructureTimeSlice();
    timeSlice.setInterpretation( ELcdAIXM51Interpretation.BASELINE );
    timeSlice.setSequenceNumber( 0 );
    timeSlice.setValidTime( createIndeterminateTimePeriod() );
    feature.getTimeSlices().add( timeSlice );

    // 3. Initialize all desired properties on the Vertical Structure time slice:
    // - location (random example)
    // Note that his can be a point but also a surface.
    TLcdAIXM51VerticalStructurePart part = new TLcdAIXM51VerticalStructurePart();
    TLcdAIXM51ElevatedPoint point = new TLcdAIXM51ElevatedPoint();
    point.move2D( 4, 50.7 );
    part.setHorizontalProjection( point );
    timeSlice.getPart().add( part );

    // - vertical structure type
    timeSlice.setType( TLcdAIXM51CodeVerticalStructure.CRANE );

    // - is it lighted or not
    timeSlice.setLighted( TLcdAIXM51CodeYesNo.NO );

    // - name
    timeSlice.setVerticalStructureName( "CRANE1" );

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
