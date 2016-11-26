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

import java.io.IOException;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51MessageDataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMFeature;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51ValDistanceVertical;
import com.luciad.format.aixm51.xml.TLcdAIXM51ModelDecoder;
import com.luciad.format.aixm51.xml.TLcdAIXM51XPathFactory;
import com.luciad.format.xml.bind.TLcdXMLXPathConstants;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A sample class to illustrate the creation of an AIXM 5.1 data set starting from a template in XML,
 * by using the LuciadMap AIXM 5.1 API. The sample uses an Airspace feature as example.
 * <p>
 * The XML template is decoded as an AIXM 5.1 feature and further initialized with
 * specific properties. The resulting feature can then be added to an AIXM 5.1
 * message for visualization purposes.
 * </p>
 */
public class AIXM51AirspaceCreator {

  /**
   * Creates a message which is set up to store Airspace feature instances.
   *
   * @return An empty AIXM basic message, configured to contain Airspace instances.
   */
  public static TLcdAIXM51AbstractAIXMMessage createAirspaceMessage() {
    // Create a new message configured to contain Airspace feature instances.
    TLcdDataType messageType = TLcdAIXM51MessageDataTypes.AIXM_BASIC_MESSSAGE_TYPE;
    TLcdAIXM51AbstractAIXMMessage message = ( TLcdAIXM51AbstractAIXMMessage ) messageType.newInstance();

    // In the LuciadMap API, an AIXM message is also a model, requiring the
    // typical model configuration settings: model reference and model descriptor.
    message.setModelReference( new TLcdGeodeticReference() );
    TLcdAIXM51ModelDescriptor modelDescriptor =
        new TLcdAIXM51ModelDescriptor( "AirspaceCreator", "Airspace",
                                       TLcdAIXM51DataTypes.AirspaceType );
    message.setModelDescriptor( modelDescriptor );

    return message;
  }

  /**
   * Creates an AIXM 5.1 airspace feature, for illustration purposes.
   * The airspace volume is a surface, consisting of an exterior outline
   * defined by a linear ring.
   *
   * @param aBaseDirectory the base directory for resource files
   * @return An airspace feature, with some values initialized.
   */
  public static TLcdAIXM51AbstractAIXMFeature createAirspace(String aBaseDirectory) {
    TLcdAIXM51ModelDecoder decoder = new TLcdAIXM51ModelDecoder();

    // Read airspace feature from file.
    TLcdAIXM51AbstractAIXMFeature airspace;
    try {
      ILcdModel model = decoder.decode( aBaseDirectory + "Data/AIXM/5.1/airspace.aixm51" );
      airspace = (TLcdAIXM51AbstractAIXMFeature ) model.elements().nextElement();
    }
    catch (IOException e) {
      TLcdLoggerFactory.getLogger( AIXM51AirspaceCreator.class.getName()).warn("Airspace feature could not be decoded: " + e.getMessage());
      throw new RuntimeException("Could not create airspace feature: " + e.getMessage());
    }

    // Adapt the upper limit of the airspace, using an XPath expression to retrieve the property.
    try {
      XPathExpression expression = new TLcdAIXM51XPathFactory().newXPath().compile("//aixm:upperLimit");
      TLcdAIXM51ValDistanceVertical limit = ( TLcdAIXM51ValDistanceVertical ) expression.evaluate(airspace, TLcdXMLXPathConstants.DOMAIN_OBJECT);
      if ( limit != null ) {
        limit.setValueObject( "40000" );
      }
    }
    catch (XPathExpressionException e) {
      TLcdLoggerFactory.getLogger( AIXM51AirspaceCreator.class.getName()).warn("The XPath expression to retrieve the airspace upper limit property could not be evaluated: " + e.getMessage());
    }

    return airspace;
  }
}
