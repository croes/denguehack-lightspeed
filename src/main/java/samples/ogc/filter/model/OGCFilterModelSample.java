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
package samples.ogc.filter.model;

import static com.luciad.ogc.filter.model.TLcdOGCFilterFactory.*;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.ogc.filter.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * This class provides several examples of filter model constructs.
 */
public class OGCFilterModelSample {
  public OGCFilterModelSample() {

    /*
      <ogc:PropertyName xmlns="http://someserver/myns">lastName</ogc:PropertyName>
    */
    TLcdOGCPropertyName propertyName = property("lastName", "http://someserver/myns");


    /*
      <ogc:PropertyName xmlns="http://someserver/myns">Person/lastName</ogc:PropertyName>
    */
    TLcdOGCXPath xpath;
    xpath = new TLcdOGCXPath("Person/lastName", "http://someserver/myns");
    propertyName = new TLcdOGCPropertyName(xpath);


    /*
      <ogc:Literal>John Smith</ogc:Literal>
    */
    TLcdOGCLiteral literal = literal("John Smith");


    /*
      <ogc:Literal>6000000</ogc:Literal>
    */
    literal = literal(6000000);


    /*
      <ogc:Function name="SIN" >
        <ogc:Literal>3.14159265359</ogc:Literal>
      </ogc:Function>
    */
    TLcdOGCFunction function = function("SIN", literal(Math.PI));


    /*
      <ogc:Add>
        <ogc:PropertyName xmlns="http://someserver/myns">lane</ogc:PropertyName>
        <ogc:Literal>-1</ogc:Literal>
      </ogc:Add>
    */
    TLcdOGCBinaryOperator binaryOperator = add(property("lane", "http://someserver/myns"),
                                               literal(-1));

    /*
      <ogc:PropertyIsGreaterThan>
        <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
        <ogc:Literal>0</ogc:Literal>
      </ogc:PropertyIsGreaterThan>
    */
    TLcdOGCBinaryComparisonOperator binaryComparisonOperator =
        gt(property("elevation", "http://someserver/myns"), literal(0));

    /*
      <ogc:PropertyIsLike wildCard="*" singleChar="?" escapeChar="\">
        <ogc:PropertyName xmlns="http://someserver/myns">lastName</ogc:PropertyName>
        <ogc:Literal>John*</ogc:Literal>
      </ogc:PropertyIsLike>
    */
    TLcdOGCIsLikeOperator isLikeOperator =
        like(property("lastName", "http://someserver/myns"), "John*");


    /*
      <ogc:BBOX>
        <ogc:PropertyName>Geometry</ogc:PropertyName>
        <gml:Envelope srsName="EPSG:4326">
          <gml:lowerCorner>13.0983 31.5899</gml:lowerCorner>
          <gml:upperCorner>35.5472 42.8143</gml:upperCorner>
        </gml:Envelope>
      </ogc:BBOX>
    */
    TLcdOGCBBoxOperator bboxOperator = new TLcdOGCBBoxOperator(
        property("Geometry", "http://www.opengis.net/ogc"),
        new TLcdLonLatBounds(13.0983, 31.5899, (35.5472 - 13.0983), (42.8143 - 31.5899)),
        new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    /*
      <ogc:Intersects>
        <ogc:PropertyName>Geometry</ogc:PropertyName>
        <gml:Envelope srsName="EPSG:4326">
          <gml:lowerCorner>13.0983 31.5899</gml:lowerCorner>
          <gml:upperCorner>35.5472 42.8143</gml:upperCorner>
        </gml:Envelope>
      </ogc:Intersects>
    */
    TLcdOGCBinarySpatialOperator binarySpatialOperator = new TLcdOGCBinarySpatialOperator(
        TLcdOGCBinarySpatialOperator.INTERSECTS,
        property("Geometry", "http://www.opengis.net/ogc"),
        new TLcdLonLatBounds(13.0983, 31.5899, (35.5472 - 13.0983), (42.8143 - 31.5899)),
        new TLcdGeodeticReference(new TLcdGeodeticDatum()));


    /*
      <ogc:And>
        <ogc:Not>
          <ogc:PropertyIsNull>
            <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
          </ogc:PropertyIsNull>
        </ogc:Not>
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
          <ogc:Literal>0</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:And>
    */
    TLcdOGCBinaryLogicOperator binaryLogicOperator =
        and(not(isNull(property("elevation", "http://someserver/myns"))),
            eq(property("elevation", "http://someserver/myns"), literal(0)));



    /*
      <ogc:PropertyIsBetween>
        <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
        <ogc:LowerBoundary>
          <ogc:Literal>0</ogc:Literal>
        </ogc:LowerBoundary>
        <ogc:UpperBoundary>
          <ogc:Literal>800</ogc:Literal>
        </ogc:UpperBoundary>
      </ogc:PropertyIsBetween>
    */
    TLcdOGCIsBetweenOperator isBetweenOperator =
        between(property("elevation", "http://someserver/myns"),
                literal(0),
                literal(800));


    /*
      <ogc:PropertyIsNull>
        <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
      </ogc:PropertyIsNull>
    */
    TLcdOGCIsNullOperator isNullOperator =
        isNull(property("elevation", "http://someserver/myns"));


    /*
      <ogc:Not>
        <ogc:PropertyIsNull>
          <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
        </ogc:PropertyIsNull>
      </ogc:Not>
    */
    TLcdOGCNotOperator notOperator =
        not(isNull(property("elevation", "http://someserver/myns")));


    /*
      <ogc:Filter>
        <ogc:PropertyIsGreaterThan>
          <ogc:PropertyName xmlns="http://someserver/myns">elevation</ogc:PropertyName>
          <ogc:Literal>0</ogc:Literal>
        </ogc:PropertyIsGreaterThan>
      </ogc:Filter>
    */
    TLcdOGCFilter filter = new TLcdOGCFilter(
        gt(property("elevation", "http://someserver/myns"), literal(0)));

    /*
      <ogc:Filter>
        <ogc:GmlObjectId gml:id="city.123"/>
        <ogc:GmlObjectId gml:id="city.456"/>
      </ogc:Filter>
    */
    filter = new TLcdOGCFilter(new String[]{"city.123", "city.456"});

  }
}
