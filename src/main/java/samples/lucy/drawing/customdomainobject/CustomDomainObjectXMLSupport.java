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
package samples.lucy.drawing.customdomainobject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.luciad.datamodel.TLcdDataProperty;
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

/**
 * This class defines all the xml functionality that is needed to marshal and
 * unmarshal custom domain object instances in the default drawing format.
 *
 * The drawing_CustomDomainObject.xsd in the resources/lucy/drawing/customdomainobject directory
 * contains the schema definition.
 *
 * A custom domain object will be represented in XML as a GML abstract feature.
 */
public class CustomDomainObjectXMLSupport {

  private static final String GML_NS = "http://www.opengis.net/gml/3.2";
  private static final QName GML_ID = new QName(GML_NS, "id");
  private static final QName GML_POS = new QName(GML_NS, "pos");
  private static final String CUSTOM_NS = "http://www.luciad.com/resources/samples/custom/1.0";

  static TLcdXMLSchemaElement createCustomDomainObjectElement(TLcdXMLSchemaElement aAbstractFeatureElement) {
    return new TLcdXMLSchemaElement(
        TLcdXMLSchemaElementIdentifier.newGlobalElementInstance(new QName(CUSTOM_NS, "customDomainObject")),
        new TLcdXMLSchemaType(TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(CUSTOM_NS, "CustomDomainObjectType")), aAbstractFeatureElement.getType()),
        aAbstractFeatureElement);
  }

  /**
   * Adds the required schema information. This schema information is used
   * during (un)marshalling to find the correct (un)marshaller.
   */
  private static TLcdXMLSchemaElement registerSchemaInfo(TLcdXMLSchemaSet aSchemaInfo) {
    TLcdXMLSchemaElement customDomainObjectElement = createCustomDomainObjectElement(aSchemaInfo.getGlobalElement(new QName(GML_NS, "AbstractFeature")));
    aSchemaInfo.registerElement(customDomainObjectElement);
    return customDomainObjectElement;
  }

  /**
   * Returns a {@link ILcdXMLSchemaBasedEncoderLibrary} that is capable of marshalling
   * custom domain objects. This library basically adds the required schema information
   * and registers a {@link CustomDomainObjectMarshaller} for 
   * the {@link CustomDomainObject} class and the 
   * custom domain object XML schema type.
   *
   * @return the {@link ILcdXMLSchemaBasedEncoderLibrary} for the custom domain object type.
   */
  public static ILcdXMLSchemaBasedEncoderLibrary createXMLSchemaBasedEncoderLibrary() {
    return new ILcdXMLSchemaBasedEncoderLibrary() {

      @Override
      public void configureEncoder(TLcdXMLSchemaBasedEncoder aEncoder) {
        TLcdXMLSchemaElement customDomainObjectElement = registerSchemaInfo(aEncoder.getMapping().getSchemaSet());
        aEncoder.getMapping().getSchemaSet().registerElement(customDomainObjectElement);
        aEncoder.registerNamespaceURI(CUSTOM_NS, "custom", null);
        registerClassExports(aEncoder.getMapping().getJavaClassResolver());
        registerTypeMarshallers(customDomainObjectElement, aEncoder.getTypeMarshallerProvider(),
                                aEncoder.getMarshallerProvider());
      }

      /**
       * Makes sure that the custom domain object marshaller can be found in
       * case ILcdShape.class is used as argument for the {@link TLcdXMLMarshallerProvider}. 
       */
      public void registerClassExports(TLcdXMLJavaClassResolver aResolver) {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(CustomDomainObject.class);
        list.add(ILcdShape.class);
        aResolver.registerClassPriorityList(list);
      }

      public void registerTypeMarshallers(TLcdXMLSchemaElement aCustomDomainObjectElement, TLcdXMLTypeMarshallerProvider aTypeProvider,
                                          TLcdXMLMarshallerProvider aProvider) {
        aTypeProvider.registerTypeMarshaller(aCustomDomainObjectElement.getType().getIdentifier(), CustomDomainObject.class, new CustomDomainObjectMarshaller(aProvider));
      }

    };
  }

  /**
   * Returns a {@link ILcdXMLSchemaBasedDecoderLibrary} that is capable of unmarshalling
   * custom domain objects. This library basically adds the required schema information
   * and registers a {@link CustomDomainObjectUnmarshaller} for 
   * the {@link CustomDomainObject} class and the custom domain object XML schema type.
   *
   * @return the {@link ILcdXMLSchemaBasedDecoderLibrary} for the custom domain object type.
   */
  public static ILcdXMLSchemaBasedDecoderLibrary createXMLSchemaBasedDecoderLibrary(final CustomDomainObjectSupplier aSupplier) {
    return new ILcdXMLSchemaBasedDecoderLibrary() {

      @Override
      public void configureDecoder(TLcdXMLSchemaBasedDecoder aUnmarshallerContext) {
        TLcdXMLSchemaElement customElement = registerSchemaInfo(aUnmarshallerContext.getMapping().getSchemaSet());
        registerTypeUnmarshallers(customElement, aUnmarshallerContext.getTypeUnmarshallerProvider(),
                                  aUnmarshallerContext.getUnmarshallerProvider());
      }

      private void registerTypeUnmarshallers(TLcdXMLSchemaElement aCustomElement, TLcdXMLTypeUnmarshallerProvider aTypeProvider,
                                             TLcdXMLUnmarshallerProvider aUnmarshallerProvider) {
        aTypeProvider.registerTypeUnmarshaller(aCustomElement.getType().getIdentifier(), CustomDomainObject.class, new CustomDomainObjectUnmarshaller(aUnmarshallerProvider, aSupplier));
      }

    };
  }

  /**
   * The {@link ILcdXMLTypeMarshaller} for custom domain objects. This class
   * will generate the actual xml output.
   */
  public static class CustomDomainObjectMarshaller implements ILcdXMLTypeMarshaller<CustomDomainObject> {
    private static final QName FEATURE_PROPERTY = new QName("http://www.luciad.com/resources/drawing/1.0", "featureProperty");

    private final TLcdXMLMarshallerProvider fMarshallerProvider;

    public CustomDomainObjectMarshaller(TLcdXMLMarshallerProvider aMarshallerProvider) {
      fMarshallerProvider = aMarshallerProvider;
    }

    @Override
    public void marshalType(CustomDomainObject aObject, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      // write the GML_ID attribute as required by the schema
      aWriter.writeAttribute(GML_ID.getNamespaceURI(), GML_ID.getLocalPart(), TLcyDrawingXMLUtil.nextGmlId(aContext));
      // Marshal properties.
      List<TLcdDataProperty> dataProperties = aObject.getDataType().getProperties();
      for (TLcdDataProperty property : dataProperties) {
        marshalChild(aObject.getValue(property), FEATURE_PROPERTY, aWriter, aContext);
      }
      // Marshal the content, which is in case of a custom domain object a point
      // because CustomDomainObject implements ILcdPoint, a default point marshaller
      // registered by the drawing addon can be used to write out the position.
      marshalChild(aObject, GML_POS, aWriter, aContext);
    }

    private <U> void marshalChild(U aChild, QName aXMLName, XMLStreamWriter aWriter, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdXMLMarshaller<U> marshaller = (ILcdXMLMarshaller<U>) fMarshallerProvider.getMarshaller(aXMLName, aChild.getClass());
      if (marshaller != null) {
        marshaller.marshal(aChild, aWriter, aContext);
      }
    }
  }

  /**
   * The {@link ILcdXMLTypeUnmarshaller} for custom domain objects. This class
   * will create CustomDomainObject instances from the xml input.
   */
  public static class CustomDomainObjectUnmarshaller implements ILcdXMLTypeUnmarshaller<CustomDomainObject> {

    private final CustomDomainObjectSupplier fSupplier;
    private final TLcdXMLUnmarshallerProvider fUnmarshallerProvider;

    public CustomDomainObjectUnmarshaller(TLcdXMLUnmarshallerProvider aUnmarshallerProvider, CustomDomainObjectSupplier aSupplier) {
      fUnmarshallerProvider = aUnmarshallerProvider;
      fSupplier = aSupplier;
    }

    @Override
    public CustomDomainObject unmarshalType(CustomDomainObject aObject, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdModel model = TLcyDrawingXMLUtil.getModel(aContext);
      CustomDomainObject result = (CustomDomainObject) fSupplier.createDomainObject(model);
      aReader.nextTag();
      // unmarshall properties
      List<TLcdDataProperty> properties = result.getDataType().getProperties();
      for (TLcdDataProperty property : properties) {
        result.setValue(property, unmarshalChild(aReader.getName(), Object.class, aReader, aContext));
        aReader.nextTag();
      }

      // retrieve the point
      // the unmarshaller for a point is registered automatically by the drawing addon
      ILcdPoint point = unmarshalChild(aReader.getName(), ILcdPoint.class, aReader, aContext);
      aReader.nextTag();
      result.move2D(point);
      return result;
    }

    private <U> U unmarshalChild(QName aXMLName, Class<U> aClass, XMLStreamReader aReader, ILcdXMLDocumentContext aContext) throws XMLStreamException {
      ILcdXMLUnmarshaller<? extends U> unmarshaller = fUnmarshallerProvider.getUnmarshaller(aXMLName, aClass);
      if (unmarshaller != null) {
        return unmarshaller.unmarshal(aReader, aContext);
      }
      return null;
    }
  }

}
