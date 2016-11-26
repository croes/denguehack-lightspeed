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
package samples.xml.customdecodingencoding;

import java.awt.Color;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.luciad.format.xml.bind.ILcdXMLDocumentContext;
import com.luciad.format.xml.bind.schema.ILcdXMLDatatypeMarshaller;
import com.luciad.format.xml.bind.schema.ILcdXMLDatatypeUnmarshaller;

/**
 * Adapter for color. As color is represented in XML as a simple type, we use
 * a {@link ILcdXMLDatatypeUnmarshaller} and {@link ILcdXMLDatatypeMarshaller}
 * respectively.
 * <p/>
 * The implementation uses a simple conversion from and to string based on the rgb value
 * of the color.
 */
public class ColorDatatypeAdapter implements ILcdXMLDatatypeMarshaller<Color>, ILcdXMLDatatypeUnmarshaller<Color> {

  public String marshal(Color aValue, XMLStreamWriter aWriter,
                        ILcdXMLDocumentContext aContext) throws XMLStreamException {
    return Integer.toString(aValue.getRGB());
  }

  public Color unmarshal(String aLexicalValue, XMLStreamReader aReader,
                         ILcdXMLDocumentContext aContext) throws XMLStreamException {
    return new Color(Integer.parseInt(aLexicalValue));
  }

}
