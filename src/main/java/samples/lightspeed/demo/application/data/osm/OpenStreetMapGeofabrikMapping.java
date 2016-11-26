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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class OpenStreetMapGeofabrikMapping {

  private final List<Mapping> fMappings;

  public OpenStreetMapGeofabrikMapping(List<Mapping> aMappings) {
    fMappings = aMappings;
  }

  public String[] getKeyValue(String aCode, String aClass) {
    for (Mapping mapping : fMappings) {
      if (aCode.matches(mapping.fCode)) {
        return new String[]{mapping.fKey, (mapping.fValue == null) ? aClass : mapping.fValue};
      }
    }

    throw new IllegalArgumentException("Unknown code " + aCode);
  }

  public List<Mapping> getMappings() {
    return Collections.unmodifiableList(fMappings);
  }

  private static OpenStreetMapGeofabrikMapping instance;

  public static OpenStreetMapGeofabrikMapping load() {
    if (instance == null) {
      instance = load(OpenStreetMapElementStyles.class.getClassLoader().getResourceAsStream("samples/lightspeed/demo/osm/geofabrikmapping.xml"));
    }
    return instance;
  }

  private static OpenStreetMapGeofabrikMapping load(InputStream aXmlStream) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      String expression = "//mapping";
      NodeList nodes = (NodeList) xpath.evaluate(expression, new InputSource(aXmlStream), XPathConstants.NODESET);

      List<Mapping> mappings = new ArrayList<Mapping>();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        NamedNodeMap attributes = node.getAttributes();
        Node code = attributes.getNamedItem("code");
        Node key = attributes.getNamedItem("k");
        Node value = attributes.getNamedItem("v");
        mappings.add(new Mapping(code.getTextContent(),
                                 key.getTextContent(),
                                 value == null ? null : value.getTextContent()));
      }

      return new OpenStreetMapGeofabrikMapping(mappings);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  public static class Mapping {
    public final String fCode;
    public final String fKey;
    public final String fValue;

    private Mapping(String aCode, String aKey, String aValue) {
      fCode = aCode;
      fKey = aKey;
      fValue = aValue;
    }
  }
}
