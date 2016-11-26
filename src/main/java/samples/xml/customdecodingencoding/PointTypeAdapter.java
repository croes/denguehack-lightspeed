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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.luciad.format.xml.bind.ILcdXMLDocumentContext;
import com.luciad.format.xml.bind.schema.ILcdXMLTypeMarshaller;
import com.luciad.format.xml.bind.schema.ILcdXMLTypeUnmarshaller;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;

public class PointTypeAdapter implements ILcdXMLTypeUnmarshaller<ILcd2DEditablePoint>,
                                         ILcdXMLTypeMarshaller<ILcd2DEditablePoint> {

  public ILcd2DEditablePoint unmarshalType(ILcd2DEditablePoint aObject,
                                           XMLStreamReader aReader, ILcdXMLDocumentContext aContext)
      throws XMLStreamException {
    String x = aReader.getAttributeValue(CustomDecodingEncodingConstants.NAMESPACE_URI, "x");
    String y = aReader.getAttributeValue(CustomDecodingEncodingConstants.NAMESPACE_URI, "y");
    aObject.move2D(Double.parseDouble(x), Double.parseDouble(y));
    aReader.nextTag();
    return aObject;
  }

  public void marshalType(ILcd2DEditablePoint aObject,
                          XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext)
      throws XMLStreamException {
    aWriter.writeAttribute(CustomDecodingEncodingConstants.NAMESPACE_URI, "x", Double.toString(aObject.getX()));
    aWriter.writeAttribute(CustomDecodingEncodingConstants.NAMESPACE_URI, "y", Double.toString(aObject.getY()));
  }

}
