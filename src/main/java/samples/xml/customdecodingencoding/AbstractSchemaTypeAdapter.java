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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.luciad.format.xml.bind.ILcdXMLDocumentContext;
import com.luciad.format.xml.bind.ILcdXMLMarshaller;
import com.luciad.format.xml.bind.ILcdXMLUnmarshaller;
import com.luciad.format.xml.bind.schema.ILcdXMLTypeMarshaller;
import com.luciad.format.xml.bind.schema.ILcdXMLTypeUnmarshaller;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedDecoder;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedEncoder;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaElementIdentifier;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaTypeIdentifier;

/**
 * Abstract helper class that provides support for marshalling and unmarshalling instances
 * based on respectively a <code>{@linkplain TLcdXMLSchemaBasedEncoder}</code> and a
 * <code>{@linkplain TLcdXMLSchemaBasedDecoder}</code>.
 * <p/>
 * Although this class implements both <code>{@linkplain ILcdXMLTypeMarshaller}</code> and
 * <code>{@link ILcdXMLTypeMarshaller}</code>, typically instances take on only one of these
 * two roles. As such, it would have been just as easy to split this class in two and have
 * an abstract schema type marshaller and an abstract schema type unmarshaller. Because the
 * functionality of marshaller and unmarshaller are however very similar and also in order to
 * reduce the amount of classes the two functionalities are merged in this single class.
 * <p/>
 * @param <T> The class for which this adapter is responsible.
 */
public abstract class AbstractSchemaTypeAdapter<T> implements ILcdXMLTypeMarshaller<T>, ILcdXMLTypeUnmarshaller<T> {

  private TLcdXMLSchemaBasedEncoder fSchemaEncoder;
  private TLcdXMLSchemaBasedDecoder fSchemaDecoder;
  private Class<? super T> fParentClass;
  private TLcdXMLSchemaTypeIdentifier fParentID;

  public AbstractSchemaTypeAdapter(TLcdXMLSchemaBasedEncoder aEncoder) {
    fSchemaEncoder = aEncoder;
  }

  public AbstractSchemaTypeAdapter(TLcdXMLSchemaBasedDecoder aDecoder) {
    fSchemaDecoder = aDecoder;
  }

  /**
   * Creates a new type adapter that will delegate unmarshalling of inherited properties
   * to its parent adapter.
   */
  public AbstractSchemaTypeAdapter(TLcdXMLSchemaBasedDecoder aDecoder,
                                   TLcdXMLSchemaTypeIdentifier aParentID, Class<? super T> aParentClass) {
    this(aDecoder);
    fParentClass = aParentClass;
    fParentID = aParentID;
  }

  /**
   * Creates a new type adapter that will delegate marshalling of inherited properties
   * to its parent adapter.
   */
  public AbstractSchemaTypeAdapter(TLcdXMLSchemaBasedEncoder aEncoder,
                                   TLcdXMLSchemaTypeIdentifier aParentID, Class<? super T> aParentClass) {
    this(aEncoder);
    fParentClass = aParentClass;
    fParentID = aParentID;
  }

  /**
   * This is the main method for unmarshalling. First, a new instance is created if
   * none is passed. Then the implementation gets the opportunity to process all
   * (declared) attributes ({@link #unmarshalDeclaredAttributes(Object, XMLStreamReader, ILcdXMLDocumentContext)}. 
   * After this, the parent unmarshaller (if any) is used
   * to handle inherited properties. Finally, the implementation can handle
   * the main content ({@link #unmarshalDeclaredContent(Object, XMLStreamReader, ILcdXMLDocumentContext)).
   */
  public T unmarshalType(T aObject, XMLStreamReader aReader,
                         ILcdXMLDocumentContext aContext) throws XMLStreamException {
    if (aObject == null) {
      aObject = createNewInstance();
    }
    unmarshalDeclaredAttributes(aObject, aReader, aContext);
    ILcdXMLTypeUnmarshaller<? super T> parentMarshaller = getParentUnmarshaller();
    if (parentMarshaller != null) {
      parentMarshaller.unmarshalType(aObject, aReader, aContext);
    } else {
      aReader.next(); // consume start element
    }
    unmarshalDeclaredContent(aObject, aReader, aContext);
    return aObject;
  }

  private ILcdXMLTypeUnmarshaller<? super T> getParentUnmarshaller() {
    if (fParentID == null) {
      return null;
    }
    return getSchemaDecoder().getTypeUnmarshallerProvider().getTypeUnmarshaller(fParentID, fParentClass, true);
  }

  /**
   * This method should be overridden by implementations that need to unmarshal content
   * (i.e. child elements and/or simple content).
   * The implementation should only
   * marshal declared content (i.e. content that is not inherited).
   */
  protected void unmarshalDeclaredContent(T aResult, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
  }

  /**
   * This method should be overridden by implementations that need to unmarshal attributes.
   * The implementation should only
   * unmarshal declared attributes (i.e. attributes that are not inherited).
   */
  protected void unmarshalDeclaredAttributes(T aResult, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
  }

  protected abstract T createNewInstance();

  protected void skipAllWhiteSpace(XMLStreamReader aReader) throws XMLStreamException {
    int event_type = aReader.getEventType();
    while ((event_type == XMLStreamConstants.CHARACTERS && aReader.isWhiteSpace()) // skip whitespace
           || (event_type == XMLStreamConstants.CDATA && aReader.isWhiteSpace())   // skip whitespace
           || event_type == XMLStreamConstants.SPACE
           || event_type == XMLStreamConstants.PROCESSING_INSTRUCTION
           || event_type == XMLStreamConstants.COMMENT
        ) {
      event_type = aReader.next();
    }
  }

  public TLcdXMLSchemaBasedDecoder getSchemaDecoder() {
    return fSchemaDecoder;
  }

  /**
   * This is the main method for marshalling. First the implementation gets the opportunity to process all
   * (declared) attributes ({@link #marshalDeclaredAttributes(Object, XMLStreamWriter, ILcdXMLDocumentContext)}. 
   * After this, the parent marshaller (if any) is used
   * to handle inherited properties. Finally, the implementation can handle
   * the main content ({@link #marshalDeclaredContent(Object, XMLStreamWriter, ILcdXMLDocumentContext)).
   */
  public void marshalType(T aObject, XMLStreamWriter aWriter,
                          ILcdXMLDocumentContext aContext) throws XMLStreamException {
    marshalDeclaredAttributes(aObject, aWriter, aContext);
    ILcdXMLTypeMarshaller<? super T> parentMarshaller = getParentMarshaller();
    if (parentMarshaller != null) {
      parentMarshaller.marshalType(aObject, aWriter, aContext);
    }
    marshalDeclaredContent(aObject, aWriter, aContext);
  }

  private ILcdXMLTypeMarshaller<? super T> getParentMarshaller() {
    if (fParentID == null) {
      return null;
    }
    return getSchemaEncoder().getTypeMarshallerProvider().getTypeMarshaller(fParentID, fParentClass, true);
  }

  /**
   * This method should be overridden by implementations that need to marshal content 
   * (i.e. child elements and possible simple content). The implementation should only
   * marshal declared content (i.e. content that is not inherited).
   */
  protected void marshalDeclaredContent(T aObject, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
  }

  /**
   * This method should be overridden by implementations that need to marshal attributes.
   * The implementation should only
   * marshal declared attributes (i.e. attributes that are not inherited).
   */
  protected void marshalDeclaredAttributes(T aObject, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
  }

  /**
   * Marshals the given child element. This is implemented by delegating to an appropriate 
   * marshaller for the given name and the class of the child.
   */
  protected <U> void marshalChild(QName aName, U aChild, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
    if (aChild != null) {
      ILcdXMLMarshaller<? super U> childMarshaller = getSchemaEncoder().getMarshallerProvider().getMarshaller(aName, (Class<U>) aChild.getClass());
      childMarshaller.marshal(aChild, aWriter, aContext);
    }
  }

  protected <U> ILcdXMLMarshaller<? super U> getMarshaller(TLcdXMLSchemaElementIdentifier aElement, Class<U> aClass) {
    return getSchemaEncoder().getMarshallerProvider().getMarshaller(aElement, aClass);
  }

  /**
   * Unmarshals the given child element. This is implemented by delegating to an appropriate 
   * unmarshaller for the current name and the given class.
   */
  protected <U> U unmarshalChild(XMLStreamReader aReader, Class<U> aClass, ILcdXMLDocumentContext aContext) throws XMLStreamException {
    ILcdXMLUnmarshaller<? extends U> unmarshaller = getSchemaDecoder().getUnmarshallerProvider().
        getUnmarshaller(aReader.getName(), aClass);
    U result = unmarshaller.unmarshal(aReader, aContext);
    return result;
  }

  protected <U> ILcdXMLUnmarshaller<? extends U> getUnmarshaller(TLcdXMLSchemaElementIdentifier aSchemaElement,
                                                                 Class<U> aClass) {
    return getSchemaDecoder().getUnmarshallerProvider().getUnmarshaller(aSchemaElement, aClass);
  }

  public TLcdXMLSchemaBasedEncoder getSchemaEncoder() {
    return fSchemaEncoder;
  }

}
