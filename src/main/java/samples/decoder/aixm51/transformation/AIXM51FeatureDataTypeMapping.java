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
package samples.decoder.aixm51.transformation;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdAssociationClassAnnotation;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.transformation.ALcdObjectTransformation;
import com.luciad.datamodel.transformation.ILcdDataPropertyTransformation;
import com.luciad.datamodel.transformation.ILcdObjectTransformation;
import com.luciad.datamodel.transformation.TLcdDataModelMapping;
import com.luciad.datamodel.transformation.TLcdDataTypeMapping;
import com.luciad.datamodel.transformation.TLcdObjectTransformationProvider;
import com.luciad.datamodel.transformation.TLcdTransformer;
import com.luciad.format.aixm.model.airspace.TLcdAIXMAirspaceDataProperties;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMFeature;
import com.luciad.format.aixm51.model.datatypes.ALcdAIXM51Code;
import com.luciad.format.gml32.model.TLcdGML32AbstractGML;
import com.luciad.format.gml32.model.TLcdGML32CodeWithAuthority;
import com.luciad.format.gml32.model.TLcdGML32DataTypes;
import com.luciad.format.gml32.model.TLcdGML32NilReasonEnumeration;
import com.luciad.model.ILcdModelReference;
import com.luciad.util.ILcdCloneable;
import com.luciad.util.ILcdFunction;

/**
 * An AIXM 5.1 specific mapping class that simplifies mapping of simple domain
 * models to AIXM 5.
 * <p/>
 * This class allows you to map properties of a simple type on the time slice of
 * an AIXM 5.1 feature, and it allows you to specify mappings at the feature
 * type level. The methods that apply to time slice properties explicitly
 * specify this in the method name. All other methods apply to feature type
 * properties.
 * <p/>
 * This class also offers static methods that allow setting/getting an
 * <code>ILcdModelReference</code> on/from a transformation context. These
 * methods have to be used, because a model reference needs to be specified when
 * generating GML geometries.
 * <p/>
 * It also support automatic generation of identifiers and gml:id's. The
 * {@link #setIdProperty(TLcdDataProperty, String)} method should be used to
 * specify a property that can be used as an id, and a suitable prefix.
 * <p/>
 * All other methods can be used to define the mapping of properties from your
 * domain model to the AIXM 5.1 domain model. The signature and use of these
 * methods is similar to that of {@link TLcdDataModelMapping}. However, some of
 * these methods do enhance the mapping.
 * <p/>
 * For instance, if you map {@link TLcdAIXMAirspaceDataProperties#NAME} to
 * {@link com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceTimeSlice#AIRSPACE_NAME_PROPERTY
 * TLcdAIXM51AirspaceTimeSlice#AIRSPACE_NAME_PROPERTY}, you are normally mapping
 * a <code>String</code> to a
 * <code>{@link com.luciad.format.aixm51.model.util.TLcdAIXM51Optional}&lt;java.lang.String&gt;</code>
 * . This would normally require a custom transformation, but this AIXM 5.1
 * specific mapping will automatically take care of wrapping the
 * <code>String</code> in the required <code>TLcdAIXM51Optional</code>.
 *
 */
public final class AIXM51FeatureDataTypeMapping {

  /**
   * A constant that is to be used as a key for the ILcdModelReference in the
   * transformation context.
   */
  static final String SRS = "SRS";

  private static final String ID_PROVIDER = "ID_PROVIDER";

  private TLcdDataTypeMapping fFeatureTypeMapping;
  private final TLcdObjectTransformationProvider fProvider;
  private TLcdDataModelMapping fTimeSliceDataModelMapping;
  private TLcdDataTypeMapping fTimeSliceTypeMapping;

  /**
   * Returns a model reference based on the context. The context can be used to
   * pass on relevant information to transformations.
   *
   * @param aContext
   *          A transformation context
   * @return The model reference that is retrieved from the context.
   */
  public static ILcdModelReference getSRS(Map<Object, Object> aContext) {
    ILcdModelReference modelReference = (ILcdModelReference) aContext.get(SRS);
    if (modelReference == null) {
      throw new IllegalStateException("There was no SRS set on the transformation context, please correctly set an ILcdModelReference on the transformation context.");
    }
    return modelReference;
  }

  /**
   * Configures the given transformation context with the model reference. All
   * transformation contexts need to be configured with this method before the
   * transformation.
   *
   * @param aReference
   *          The model reference that is to be used in the transformation.
   * @param aTransformationContext
   *          The context to configure.
   * @see TLcdTransformer#createTransformationContext()
   */
  public static void setSRS(ILcdModelReference aReference, Map<Object, Object> aTransformationContext) {
    aTransformationContext.put(SRS, aReference);
  }

  /**
   * Creates a new type mapping of a given type to an AIXM 5.1 feature type.
   *
   * @param aAIXMType
   *          A type that should be mapped onto aAIXM51FeatureType
   * @param aAIXM51FeatureType
   *          An AIXM 5.1 feature type, should extend from
   *          {@link TLcdAIXM51DataTypes#AbstractAIXMFeatureType}
   * @param aMapping
   *          The mapping on which this type mapping should be created.
   * @param aProvider
   *          A transformation provider that can be used to transform properties
   *          from the source type to the target type.
   */
  public AIXM51FeatureDataTypeMapping(TLcdDataType aAIXMType,
                                      final TLcdDataType aAIXM51FeatureType, TLcdDataModelMapping aMapping,
                                      TLcdObjectTransformationProvider aProvider) {
    fProvider = aProvider;
    fFeatureTypeMapping = aMapping.mapType(aAIXMType, aAIXM51FeatureType);
    fTimeSliceDataModelMapping = new TLcdDataModelMapping(aAIXMType.getDataModel(), aAIXM51FeatureType.getDataModel());
    fTimeSliceTypeMapping = fTimeSliceDataModelMapping.mapType(aAIXMType, aAIXM51FeatureType.getProperty("timeSlice").getType().getProperties().get(1).getType());

    fFeatureTypeMapping.mapTargetProperty("timeSlice", new ALcdObjectTransformation() {

      @Override
      public Object transform(Object aObject, Map<Object, Object> aContext) {
        TLcdTransformer timeSliceTransformer = new TLcdTransformer();
        fTimeSliceDataModelMapping.configure(timeSliceTransformer);
        Object theTimeslice = timeSliceTransformer.transform((ILcdDataObject) aObject, aContext);
        provideDefaultGMLIDs(theTimeslice, aContext);
        ILcdDataObject timesliceWrapper = aAIXM51FeatureType.getProperty("timeSlice").getType().newInstance();
        timesliceWrapper.setValue(timesliceWrapper.getDataType().getProperties().get(1), theTimeslice);
        return Collections.singletonList(timesliceWrapper);
      }

      @Override
      protected Object invert(Object aObject, Map<Object, Object> aContext) {
        throw new UnsupportedOperationException();
      }

    });
  }

  /**
   * Maps the given source property using the given transformation on the target object. This property mapping
   * is always uni-directional. To make the type mapping bi-directional, probably additional property mappings need
   * to be registered on this object's inverse type mapping.
   *
   * @param aAIXMProperty the source property to map
   * @param aTransformation the transformation to use to map the source property
   */
  public void mapSourceProperty(TLcdDataProperty aAIXMProperty, ILcdDataPropertyTransformation aTransformation) {
    fFeatureTypeMapping.mapProperty(aAIXMProperty, aTransformation);
  }

  /**
   * Maps the given source property on the given target property, of the AIXM
   * 5.1 time slice type. Source values are transformed into target values using
   * the given transformation
   *
   * @param aAIXMProperty
   *          the source property
   * @param aAIXM51TimeSliceProperty
   *          the target property
   */
  public void mapTimeSliceProperty(TLcdDataProperty aAIXMProperty, TLcdDataProperty aAIXM51TimeSliceProperty) {
    fTimeSliceTypeMapping.mapProperty(aAIXMProperty, aAIXM51TimeSliceProperty, new AIXM51TimeSlicePropertyTransformation(aAIXMProperty, aAIXM51TimeSliceProperty, fProvider));
  }

  /**
   * <p>Maps the given time slice target property using the given transformation. The transformation should transform instances
   * of this object's source type into appropriate values for the given target property.</p>
   *
   * <p>This method is for instance used to map properties whose values depends on the value of multiple
   * source properties.</p>
   *
   * @param aTargetProperty the target property
   * @param aTransformation the transformation used to transform a source instance into a value for the target property
   */
  public void mapTimeSliceTargetProperty(TLcdDataProperty aTargetProperty, final ILcdObjectTransformation aTransformation) {
    if (aTargetProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class) != null) {
      ILcdObjectTransformation wrappedTransformation = new AssociationPropertyTransformation(aTransformation, aTargetProperty);
      fTimeSliceTypeMapping.mapTargetProperty(aTargetProperty, wrappedTransformation);
    } else {
      fTimeSliceTypeMapping.mapTargetProperty(aTargetProperty, new DefaultGMLIdTransformationWrapper(aTransformation));
    }
  }

  /**
   * Maps a property of the source feature type to a property of the AIXM 5.1 time slice type.
   * @param aSourceProperty A source type property
   * @param aTargetProperty An AIXM 5.1 time slice type property.
   * @param aTransformation The transformation to use to transform source values into target values.
   */
  public void mapTimeSliceProperty(TLcdDataProperty aSourceProperty, TLcdDataProperty aTargetProperty, ILcdObjectTransformation aTransformation) {
    if (aTargetProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class) != null) {
      ILcdObjectTransformation wrappedTransformation = new AssociationPropertyTransformation(aTransformation, aTargetProperty);
      fTimeSliceTypeMapping.mapProperty(aSourceProperty, aTargetProperty, wrappedTransformation);
    } else {
      fTimeSliceTypeMapping.mapProperty(aSourceProperty, aTargetProperty, aTransformation);
    }
  }

  /**
   * Sets a property of the result time slice to a constant value.
   * @param aTargetProperty The property to set
   * @param aValue The constant value.
   */
  public void setTimeSliceProperty(TLcdDataProperty aTargetProperty, final Object aValue) {
    mapTimeSliceTargetProperty(aTargetProperty, new ConstantValueTransformation(aValue));
  }

  /**
   *
   * A simple transformation wrapper that adds GML id's to the result if they are not set.
   *
   */
  private final class DefaultGMLIdTransformationWrapper extends ALcdObjectTransformation {
    private final ILcdObjectTransformation fTransformation;

    private DefaultGMLIdTransformationWrapper(ILcdObjectTransformation aTransformation) {
      fTransformation = aTransformation;
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      Object result = fTransformation.transform(aObject, aContext);
      provideDefaultGMLIDs(result, aContext);
      return result;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   *
   * A simple transformation that always returns the same value. Cloneable values are cloned
   * to ensure that it is possible to modify the returned value.
   *
   */
  private final class ConstantValueTransformation extends ALcdObjectTransformation {
    private final Object fValue;

    private ConstantValueTransformation(Object aValue) {
      fValue = aValue;
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      if (fValue instanceof ILcdCloneable) {
        return ((ILcdCloneable) fValue).clone();
      } else {
        return fValue;
      }
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      return null;
    }
  }

  /**
   * A transformation wrapper that makes sure that takes care of wrapping an object in the required wrapper.
   *
   */
  private static final class AssociationPropertyTransformation implements ILcdObjectTransformation {
    private final ILcdObjectTransformation fTransformation;
    private final TLcdDataProperty fTargetProperty;

    private AssociationPropertyTransformation(ILcdObjectTransformation aTransformation, TLcdDataProperty aTargetProperty) {
      fTransformation = aTransformation;
      fTargetProperty = aTargetProperty;
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdDataProperty roleProperty = fTargetProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class).getRoleProperty();
      Object transformedValue = fTransformation.transform(aObject, aContext);
      provideDefaultGMLIDs(transformedValue, aContext);
      ILcdDataObject associationObject = fTargetProperty.getType().newInstance();
      if (transformedValue != null) {
        associationObject.setValue(roleProperty, transformedValue);
      } else {
        associationObject.setValue("nilReason", new TLcdGML32NilReasonEnumeration("other:mappingFailed"));
      }
      return fTargetProperty.isCollection() ? Collections.singletonList(associationObject) : associationObject;
    }

    @Override
    public ILcdObjectTransformation getInverse() {
      return fTransformation.getInverse();
    }
  }

  /**
   * A default transformation for AIXM 5.1 time slice properties. It hides part of the complexity of
   * the AIXM 5.1 domain model, so transformations can be specified more easily.
   */
  private static final class AIXM51TimeSlicePropertyTransformation extends ALcdObjectTransformation {
    private final TLcdDataProperty fTargetProperty;
    private final TLcdObjectTransformationProvider fProvider;
    private final TLcdDataProperty fSourceProperty;

    public AIXM51TimeSlicePropertyTransformation(
        TLcdDataProperty aSourceProperty, TLcdDataProperty aTargetProperty,
        TLcdObjectTransformationProvider aProvider) {
      fSourceProperty = aSourceProperty;
      fTargetProperty = aTargetProperty;
      fProvider = aProvider;
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      return transformValue(fSourceProperty.getType(), aObject, aContext);
    }

    private Object transformValue(TLcdDataType aValueType, Object aValue,
                                  Map<Object, Object> aContext) {
      TLcdDataType valueType = aValueType;
      if (aValue instanceof ILcdDataObject && !valueType.isPrimitive()) {
        valueType = ((ILcdDataObject) aValue).getDataType();
      } else if (valueType.isPrimitive() && !fTargetProperty.getType().isPrimitive()
                 && fTargetProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class) != null) {
        TLcdDataProperty roleProperty = fTargetProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class).getRoleProperty();
        TLcdDataType targetType = roleProperty.getType();
        ILcdObjectTransformation transformation = fProvider
            .getObjectTransformation(valueType, targetType);
        if (transformation == null && ALcdAIXM51Code.class.isAssignableFrom(targetType.getInstanceClass())) {
          transformation = new GenericAIXM51Transformations.ObjectToAIXM51CodeTransformation((Class<? extends ALcdAIXM51Code>) targetType.getInstanceClass());
        }
        if (transformation != null) {
          Object transformedValue = transformation.transform(aValue, aContext);
          provideDefaultGMLIDs(transformedValue, aContext);
          ILcdDataObject associationObject = fTargetProperty.getType().newInstance();
          if (transformedValue != null) {
            associationObject.setValue(roleProperty, transformedValue);
          } else {
            associationObject.setValue("nilReason", new TLcdGML32NilReasonEnumeration("other:mappingFailed"));
          }
          return fTargetProperty.isCollection() ? Collections.singletonList(associationObject) : associationObject;
        }
      }
      ILcdObjectTransformation transformation = fProvider
          .getObjectTransformation(valueType, fTargetProperty.getType());
      if (transformation == null) {
        throw new IllegalArgumentException("Can't find transformation from "
                                           + valueType + " to " + fTargetProperty.getType());
      }
      Object transformedValue = transformation.transform(aValue, aContext);
      return transformedValue;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * The method fills in gml:id attributes on all objects that extend from TLcdGML32AbstractGML. This
   * is convenient because gml:id attributes are mandatory.
   *
   * @param aValue The value on which the id has to be set, and which will be traversed recursively if it is an ILcdDataObject
   * @param aContext A transformation context.
   */
  private static void provideDefaultGMLIDs(Object aValue, final Map<Object, Object> aContext) {
    if (aValue instanceof ILcdDataObject) {
      //all abstract GML objects need an id!
      final GMLIdProvider idProvider = (GMLIdProvider) aContext.get(ID_PROVIDER);
      if (idProvider != null) {
        applyFunctionToAllProperties(new ILcdFunction() {
          @Override
          public boolean applyOn(Object aObject) throws IllegalArgumentException {
            if (aObject instanceof ILcdDataObject &&
                TLcdGML32DataTypes.AbstractGMLType
                                  .isAssignableFrom(((ILcdDataObject) aObject).getDataType())
                && ((ILcdDataObject) aObject).getValue(TLcdGML32AbstractGML.ID_PROPERTY) == null) {
              ((ILcdDataObject) aObject).setValue(TLcdGML32AbstractGML.ID_PROPERTY, idProvider.getGMLId((TLcdGML32AbstractGML) aObject, aContext));
            }
            return true;
          }
        }, aValue);
      }
    }
  }

  private static void setFeatureId(Object aId, Object aAIXM5Feature, String aPrefix) {
    TLcdGML32CodeWithAuthority identifier = new TLcdGML32CodeWithAuthority();
    identifier.setValueObject(aId.toString());
    identifier.setCodeSpace(URI.create(aPrefix));
    TLcdAIXM51AbstractAIXMFeature feature = (TLcdAIXM51AbstractAIXMFeature) aAIXM5Feature;
    feature.setIdentifier(identifier);
    feature.setId(aPrefix + aId.toString());
  }

  /**
   * A convenience method that applies a function to a hierarchy of <code>ILcdDataObject</code> objects.
   * @param aFunction The function to apply to each value and data object in the hierarchy
   * @param aDataObject The data object which will be traversed recursively.
   */
  private static void applyFunctionToAllProperties(ILcdFunction aFunction, Object aDataObject) {

    aFunction.applyOn(aDataObject);
    if (aDataObject instanceof ILcdDataObject) {
      final List<TLcdDataProperty> properties = ((ILcdDataObject) aDataObject).getDataType().getProperties();
      for (TLcdDataProperty property : properties) {
        final Object propertyValue = ((ILcdDataObject) aDataObject).getValue(property);
        if (propertyValue != null) {
          applyFunctionToAllProperties(aFunction, propertyValue);
        }
      }

    } else if (aDataObject instanceof List) {
      for (Object listElement : (List) aDataObject) {
        applyFunctionToAllProperties(aFunction, listElement);
      }
    }
  }

  /**
   * A provider for gml:id attributes; the returned values must
   * be unique within a model.
   *
   */
  private static final class GMLIdProvider {
    private int fIndex = 0;
    private final String fId;

    public GMLIdProvider(String aId) {
      super();
      fId = aId;
    }

    public String getGMLId(TLcdGML32AbstractGML aObject, Map<Object, Object> aContext) {
      return fId + "_" + fIndex++;
    }
  }

  /**
   * Sets a data property of the source type that can be used as an unique ID.
   * @param aMid The id property
   * @param aPrefix The prefix to use for all id's.
   */
  public void setIdProperty(final TLcdDataProperty aMid, final String aPrefix) {
    mapSourceProperty(aMid, new ILcdDataPropertyTransformation() {

      @Override
      public void transform(ILcdDataObject aSourceObject, TLcdDataProperty aSourceProperty, Object aValue, Object aTargetObject, Map<Object, Object> aContext) {
        Object id = aSourceObject.getValue(aMid);
        setFeatureId(id, aTargetObject, aPrefix);
        aContext.put(ID_PROVIDER, new GMLIdProvider(aPrefix + id.toString()));
        provideDefaultGMLIDs(aTargetObject, aContext);

      }
    });
  }
}
