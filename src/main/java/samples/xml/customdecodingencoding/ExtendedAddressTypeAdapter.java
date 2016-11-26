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
import com.luciad.format.xml.bind.ILcdXMLMarshaller;
import com.luciad.format.xml.bind.ILcdXMLUnmarshaller;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedDecoder;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedEncoder;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;

/**
 * Adapter for {@link ExtendedAddress}.
 */
public class ExtendedAddressTypeAdapter extends AbstractSchemaTypeAdapter<ExtendedAddress> {

  private ILcdXMLMarshaller<? super Color> fColorMarshaller;
  private ILcdXMLUnmarshaller<? extends Color> fColorUnmarshaller;

  public ExtendedAddressTypeAdapter(TLcdXMLSchemaBasedEncoder aEncoder) {
    super(aEncoder);
  }

  public ExtendedAddressTypeAdapter(TLcdXMLSchemaBasedDecoder aDecoder) {
    super(aDecoder);
  }

  @Override
  protected void marshalDeclaredContent(ExtendedAddress aObject, XMLStreamWriter aWriter,
                                        ILcdXMLDocumentContext aContext) throws XMLStreamException {
    if (aObject.getLocation() != null) {
      marshalChild(CustomDecodingEncodingConstants.LOCATION_ELEMENT_ID.getElementName(), aObject.getLocation(), aWriter, aContext);
    }
    if (aObject.getColor() != null) {
      getColorMarshaller().marshal(aObject.getColor(), aWriter, aContext);
    }
  }

  private ILcdXMLMarshaller<? super Color> getColorMarshaller() {
    if (fColorMarshaller == null) {
      fColorMarshaller = getMarshaller(CustomDecodingEncodingConstants.COLOR_ELEMENT_ID, Color.class);
    }
    return fColorMarshaller;
  }

  @Override
  protected ExtendedAddress createNewInstance() {
    return new ExtendedAddress();
  }

  @Override
  protected void unmarshalDeclaredContent(ExtendedAddress aResult, XMLStreamReader aReader,
                                          ILcdXMLDocumentContext aContext) throws XMLStreamException {
    skipAllWhiteSpace(aReader);
    while (aReader.isStartElement()) {
      // expect either location or color
      if (aReader.getName().equals(CustomDecodingEncodingConstants.COLOR_ELEMENT_ID.getElementNames()[0])) {
        aResult.setColor(getColorUnmarshaller().unmarshal(aReader, aContext));
      } else if (getSchemaDecoder().getMapping().getSchemaSet().isSubstitutableBy(CustomDecodingEncodingConstants.LOCATION_ELEMENT_ID.getElementName(), aReader.getName())) {
        aResult.setLocation(unmarshalChild(aReader, ILcd2DEditablePoint.class, aContext));
      } else {
        throw new XMLStreamException("Unexpected element", aReader.getLocation());
      }
      aReader.nextTag();
    }
  }

  private ILcdXMLUnmarshaller<? extends Color> getColorUnmarshaller() {
    if (fColorUnmarshaller == null) {
      fColorUnmarshaller = getUnmarshaller(CustomDecodingEncodingConstants.COLOR_ELEMENT_ID, Color.class);
    }
    return fColorUnmarshaller;
  }

}
