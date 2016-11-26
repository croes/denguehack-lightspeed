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
package samples.lucy.drawing.hippodrome;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.luciad.format.xml.bind.ILcdXMLDocumentContext;
import com.luciad.format.xml.bind.ILcdXMLMarshaller;
import com.luciad.format.xml.bind.ILcdXMLUnmarshaller;
import com.luciad.format.xml.bind.TLcdXMLJavaClassResolver;
import com.luciad.format.xml.bind.TLcdXMLMarshallerProvider;
import com.luciad.format.xml.bind.TLcdXMLUnmarshallerProvider;
import com.luciad.format.xml.bind.schema.*;
import com.luciad.lucy.addons.drawing.format.xml.TLcyDrawingXMLUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * This class defines all the xml functionality that is needed to marshall and
 * unmarshall hippodrome instances in the default drawing format.
 *
 * The drawing_hippodrome.xsd in the resources/lucy/drawing/hippodrome directory
 * contains the schema definition.
 *
 * A hippodrome will be represented in XML as a GML abstract geometry property.
 *
 * In xml, it will look something like this
 * <code>
 *     &lt;ns0:hippodromeProperty&gt;
 *       &lt;ns0:hippodrome gml:id="id3"&gt;
 *         &lt;gml:pos&gt;-5.545528632169954 42.70333780974058 0.0&lt;/gml:pos&gt;
 *         &lt;gml:pos&gt;4.359772575737986 47.251690405208514 0.0&lt;/gml:pos&gt;
 *         &lt;width&gt;245102.2761250427&lt;/width&gt;
 *       &lt;/ns0:hippodrome&gt;
 *     &lt;/ns0:hippodromeProperty&gt;
 * </code>
 *
 * Because we have two xml elements (hippodromeProperty and hippodrome) we also
 * register two marshallers.
 */
public class HippodromeXMLSupport {

  static final String GML_NS = "http://www.opengis.net/gml/3.2";
  static final QName GML__ABSTRACT_GEOMETRY = new QName(GML_NS, "AbstractGeometry");
  static final QName GML_ABSTRACT_GEOMETRIC_PRIMITIVE = new QName(GML_NS, "AbstractGeometricPrimitive");
  static final QName GML_ID = new QName(GML_NS, "id");
  static final QName GML_POS = new QName(GML_NS, "pos");
  static final TLcdXMLSchemaType GML_GEOMETRY_PROPERTY_TYPE = new TLcdXMLSchemaType(TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(GML_NS, "GeometryPropertyType")), null);

  static final String DRAWING_NS = "http://www.luciad.com/resources/drawing/1.0";
  static final TLcdXMLSchemaElement GEOMETRY_PROPERTY = new TLcdXMLSchemaElement(
      TLcdXMLSchemaElementIdentifier.newGlobalElementInstance(new QName(DRAWING_NS, "geometryProperty")),
      GML_GEOMETRY_PROPERTY_TYPE,
      null);

  static final String HIPPO_NS = "http://www.luciad.com/resources/samples/hippo/1.0";
  static final QName WIDTH = new QName(HIPPO_NS, "width");

  static final TLcdXMLSchemaElement HIPPO_PROPERTY = new TLcdXMLSchemaElement(
      TLcdXMLSchemaElementIdentifier.newGlobalElementInstance(new QName(HIPPO_NS, "hippodromeProperty")),
      new TLcdXMLSchemaType(TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(HIPPO_NS, "HippodromePropertyType")), GML_GEOMETRY_PROPERTY_TYPE),
      GEOMETRY_PROPERTY);
  static final TLcdXMLSchemaElementIdentifier HIPPO = TLcdXMLSchemaElementIdentifier.newGlobalElementInstance(new QName(HIPPO_NS, "hippodrome"));

  static TLcdXMLSchemaElement createHippoElement(TLcdXMLSchemaElement aAbstractGeometricPrimitiveElement) {
    return new TLcdXMLSchemaElement(
        HIPPO,
        new TLcdXMLSchemaType(TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(HIPPO_NS, "HippodromeType")), aAbstractGeometricPrimitiveElement.getType()),
        aAbstractGeometricPrimitiveElement);
  }

  /**
   * Adds the required schema information. This schema information is used
   * during (un)marshalling to find the correct (un)marshaller.
   * @return
   */
  private static TLcdXMLSchemaElement registerSchemaInfo(TLcdXMLSchemaSet aSchemaInfo) {
    TLcdXMLSchemaElement hippo = createHippoElement(aSchemaInfo.getGlobalElement(GML_ABSTRACT_GEOMETRIC_PRIMITIVE));
    aSchemaInfo.registerElement(HIPPO_PROPERTY);
    aSchemaInfo.registerElement(hippo);
    return hippo;
  }

  /**
   * Returns a {@link ILcdXMLSchemaBasedEncoderLibrary} that is capable of marshalling
   * hippodromes. This library basically adds the required schema information
   * and registers a {@link HippodromePropertyMarshaller} and {@link HippodromeMarshaller} for 
   * the {@link IHippodrome} class.
   *
   * @return the {@link ILcdXMLSchemaBasedEncoderLibrary} for the custom domain object type.
   */
  public static ILcdXMLSchemaBasedEncoderLibrary createXMLSchemaBasedEncoderLibrary() {
    return new ILcdXMLSchemaBasedEncoderLibrary() {

      @Override
      public void configureEncoder(TLcdXMLSchemaBasedEncoder aMarshallerContext) {
        // registers 'hippo' as namespace prefix
        // schema location null makes sure no schema location is added to the resulting xml
        aMarshallerContext.registerNamespaceURI(HIPPO_NS, "hippo", null);
        TLcdXMLSchemaElement hippo = registerSchemaInfo(aMarshallerContext.getMapping().getSchemaSet());
        registerClassExports(aMarshallerContext.getMapping().getJavaClassResolver());
        registerTypeMarshallers(hippo, aMarshallerContext.getTypeMarshallerProvider(),
                                aMarshallerContext.getMarshallerProvider());
      }

      public void registerClassExports(TLcdXMLJavaClassResolver aResolver) {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IHippodrome.class);
        list.add(ILcdShape.class);
        aResolver.registerClassPriorityList(list);
      }

      public void registerTypeMarshallers(TLcdXMLSchemaElement aHippo, TLcdXMLTypeMarshallerProvider aTypeProvider,
                                          TLcdXMLMarshallerProvider aProvider) {
        aTypeProvider.registerTypeMarshaller(HIPPO_PROPERTY.getType().getIdentifier(), IHippodrome.class, new HippodromePropertyMarshaller(aProvider));
        aTypeProvider.registerTypeMarshaller(aHippo.getType().getIdentifier(), IHippodrome.class, new HippodromeMarshaller(aProvider));
      }

    };
  }

  public static ILcdXMLSchemaBasedDecoderLibrary createXMLSchemaBasedDecoderLibrary(final HippodromeShapeSupplier aShapeSupplier) {
    return new ILcdXMLSchemaBasedDecoderLibrary() {

      @Override
      public void configureDecoder(TLcdXMLSchemaBasedDecoder aUnmarshallerContext) {
        TLcdXMLSchemaElement hippo = registerSchemaInfo(aUnmarshallerContext.getMapping().getSchemaSet());
        registerTypeUnmarshallers(hippo, aUnmarshallerContext.getTypeUnmarshallerProvider(),
                                  aUnmarshallerContext.getUnmarshallerProvider());
      }

      public void registerTypeUnmarshallers(TLcdXMLSchemaElement aHippo, TLcdXMLTypeUnmarshallerProvider aTypeProvider,
                                            TLcdXMLUnmarshallerProvider aUnmarshallerProvider) {
        aTypeProvider.registerTypeUnmarshaller(HIPPO_PROPERTY.getType().getIdentifier(), IHippodrome.class, new HippodromePropertyUnmarshaller(aUnmarshallerProvider));
        aTypeProvider.registerTypeUnmarshaller(aHippo.getType().getIdentifier(), IHippodrome.class, new HippodromeUnmarshaller(aUnmarshallerProvider, aShapeSupplier));
      }

    };
  }

  /**
   * {@link ASchemaTypeMarshaller} for HippodromeProperty. This is a very simple
   * schema type marshaller that directly delegates to the marshaller for the
   * {@link HippodromeXMLSupport#HIPPO} property.
   */
  public static class HippodromePropertyMarshaller extends ASchemaTypeMarshaller<IHippodrome> {

    public HippodromePropertyMarshaller(TLcdXMLMarshallerProvider aMarshallerProvider) {
      super(aMarshallerProvider);
    }

    @Override
    public void marshalType(IHippodrome aObject, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      marshalChild(aObject, HIPPO.getElementName(), aWriter, aContext);
    }

  }

  /**
   * {@link ASchemaTypeMarshaller} for HippodromeProperty. This is the marshaller
   * that marshals the real state of the hippodrome..
   */
  public static class HippodromeMarshaller extends ASchemaTypeMarshaller<IHippodrome> {

    public HippodromeMarshaller(TLcdXMLMarshallerProvider aMarshallerProvider) {
      super(aMarshallerProvider);
    }

    @Override
    public void marshalType(IHippodrome aObject, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      aWriter.writeAttribute(GML_ID.getNamespaceURI(), GML_ID.getLocalPart(), TLcyDrawingXMLUtil.nextGmlId(aContext));
      marshalChild(aObject.getStartPoint(), GML_POS, aWriter, aContext);
      marshalChild(aObject.getEndPoint(), GML_POS, aWriter, aContext);
      marshalSimpleElement(Double.toString(aObject.getWidth()), WIDTH, aWriter);
    }
  }

  /**
   * {@link ASchemaTypeUnmarshaller} for HippodromeProperty. Immediately
   * delegates to the unmarshaller for hippodrome.
   */
  public static class HippodromePropertyUnmarshaller extends ASchemaTypeUnmarshaller<IHippodrome> {

    public HippodromePropertyUnmarshaller(TLcdXMLUnmarshallerProvider aUnmarshallerProvider) {
      super(aUnmarshallerProvider);
    }

    @Override
    public IHippodrome unmarshalType(IHippodrome aObject, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      aReader.nextTag();
      IHippodrome hippo = unmarshalChild(aReader.getName(), IHippodrome.class, aReader, aContext);
      aReader.nextTag();
      return hippo;
    }

  }

  /**
   * {@link ASchemaTypeUnmarshaller} for Hippodrome. This is the unmarshaller
   * that creates a hippodrome from the real state.
   */
  public static class HippodromeUnmarshaller extends ASchemaTypeUnmarshaller<IHippodrome> {

    private HippodromeShapeSupplier fGeometrySupplier;

    public HippodromeUnmarshaller(TLcdXMLUnmarshallerProvider aUnmarshallerProvider, HippodromeShapeSupplier aShapeSupplier) {
      super(aUnmarshallerProvider);
      fGeometrySupplier = aShapeSupplier;
    }

    @Override
    public IHippodrome unmarshalType(IHippodrome aObject, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdModel model = TLcyDrawingXMLUtil.getModel(aContext);
      IHippodrome result = (IHippodrome) fGeometrySupplier.createShape(model);
      aReader.nextTag();
      ILcdPoint startPoint = unmarshalChild(aReader.getName(), ILcdPoint.class, aReader, aContext);
      aReader.nextTag();
      ILcdPoint endPoint = unmarshalChild(aReader.getName(), ILcdPoint.class, aReader, aContext);
      aReader.nextTag();
      double width = Double.parseDouble(unmarshalSimpleElement(aReader));
      result.setWidth(width);
      result.moveReferencePoint(startPoint, IHippodrome.START_POINT);
      result.moveReferencePoint(endPoint, IHippodrome.END_POINT);
      return result;
    }

  }

  /**
   * Simple helper class that makes implementing ILcdXMLSchemaTypeMarshaller simpler.
   */
  private abstract static class ASchemaTypeMarshaller<T> implements ILcdXMLTypeMarshaller<T> {

    private TLcdXMLMarshallerProvider fMarshallerProvider;

    protected ASchemaTypeMarshaller(TLcdXMLMarshallerProvider aMarshallerProvider) {
      fMarshallerProvider = aMarshallerProvider;
    }

    protected <U> void marshalChild(U aChild, QName aXMLName, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdXMLMarshaller<U> marshaller = (ILcdXMLMarshaller<U>) fMarshallerProvider.getMarshaller(aXMLName, aChild.getClass());
      if (marshaller != null) {
        marshaller.marshal(aChild, aWriter, aContext);
      }
    }

    protected void marshalSimpleElement(String aSimpleContent, QName aXMLName, XMLStreamWriter aWriter) throws XMLStreamException {
      aWriter.writeStartElement(aXMLName.getNamespaceURI(), aXMLName.getLocalPart());
      aWriter.writeCharacters(aSimpleContent);
      aWriter.writeEndElement();
    }
  }

  /**
   * Simple helper class that makes implementing ILcdXMLSchemaTypeUnmarshaller simpler.
   */
  private abstract static class ASchemaTypeUnmarshaller<T> implements ILcdXMLTypeUnmarshaller<T> {

    private TLcdXMLUnmarshallerProvider fUnmarshallerProvider;

    protected ASchemaTypeUnmarshaller(TLcdXMLUnmarshallerProvider aUnmarshallerProvider) {
      fUnmarshallerProvider = aUnmarshallerProvider;
    }

    protected <U> U unmarshalChild(QName aXMLName, Class<U> aClass, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdXMLUnmarshaller<? extends U> unmarshaller = fUnmarshallerProvider.getUnmarshaller(aXMLName, aClass);
      if (unmarshaller != null) {
        return unmarshaller.unmarshal(aReader, aContext);
      }
      return null;
    }

    protected String unmarshalSimpleElement(XMLStreamReader aReader) throws XMLStreamException {
      StringBuffer builder = new StringBuffer();
      while (aReader.next() == XMLStreamReader.CHARACTERS) {
        builder.append(aReader.getText());
      }
      aReader.nextTag();
      return builder.toString();
    }
  }
}
