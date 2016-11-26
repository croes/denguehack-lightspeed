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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.transformation.ALcdObjectTransformation;
import com.luciad.datamodel.transformation.TLcdDataModelMapping;
import com.luciad.datamodel.transformation.TLcdDataModelMappingValidator;
import com.luciad.datamodel.transformation.TLcdDataModelMappingValidator.LeftOverTargetType;
import com.luciad.datamodel.transformation.TLcdDataModelMappingValidator.TransformationIssue;
import com.luciad.datamodel.transformation.TLcdTransformer;
import com.luciad.format.aixm.model.TLcdAIXMDataTypes;
import com.luciad.format.aixm.model.TLcdAIXMModelListDescriptor;
import com.luciad.format.aixm.model.aerodrome.TLcdAIXMAerodromeDataProperties;
import com.luciad.format.aixm.model.airspace.TLcdAIXMAirspaceDataProperties;
import com.luciad.format.aixm.model.airspace.type.TLcdAirspaceActivityType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51MessageDataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.model.abstractfeature.ELcdAIXM51Interpretation;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMTimeSlice;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51Feature;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeAirspaceActivity;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeYesNo;
import com.luciad.format.aixm51.model.features.airportheliport.airportheliport.TLcdAIXM51AirportHeliportTimeSlice;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceActivation;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceTimeSlice;
import com.luciad.format.gml32.model.TLcdGML32AbstractTimeSlice;
import com.luciad.format.gml32.model.TLcdGML32TimePrimitiveProperty;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.decoder.aixm51.AIXM51DesignatedPointCreator;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.AirspaceClassTransformation;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.AirspaceTypeTransformation;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.FloatToValDistanceVerticalTransformation;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.PointToAIXM51ElevatedPoint;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.StringToCityTransformation;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.StringToMagneticVariation;
import samples.decoder.aixm51.transformation.GenericAIXM51Transformations.TextToNoteTransformation;

/**
 * A transformation from the AIXM 3.3/4.5 domain model to the AIXM 5.1 domain
 * model. Currently, only airspaces and airports are supported.
 * <p/>
 * This class uses a {@link TLcdDataModelMapping} to create a mapping between
 * {@link TLcdAIXMDataTypes AIXM 3.3/4.5} and {@link TLcdAIXM51DataTypes AIXM
 * 5.1}. The AIXM 5.1 domain model is inherently more complex, and as a result,
 * the mapping also needs to be sufficiently advanced to support the
 * transformation. Therefore, this class can be considered as a sample for
 * advanced transformations.
 * <p/>
 * A transformation always consists of the same steps:
 * <ol>
 * <li>Create a {@link TLcdDataModelMapping}</li>
 * <li>Configure a {@link TLcdTransformer} with the
 * <code>TLcdDataModelMapping</code></li>
 * <li>Transform each of the elements in the model one by one using the
 * <code>TLcdTransformer</code></li>
 * </ol>
 * This process is implemented in the {@link #transformToAIXM51(ILcdModel)}
 * method.
 * <p/>
 * Once the <code>TLcdDataModelMapping</code> is set up, the subsequent steps
 * are trivial. However, configuring such a mapping for AIXM 5.x is not
 * straightforward. The main difficulty is that AIXM 5.x uses an extensive time slice
 * based data structure, which is not present in AIXM 3.x/4.x.
 * This transformation challenge is solved by the {@link AIXM51FeatureDataTypeMapping}.
 * <p/>
 * A second challenge is the conversion of the geometry. Whereas AIXM 3.3/4.5
 * used a custom data structure to represent geometries, AIXM 5.x fully relies on GML 3.2.
 * For complex geometries such as airspaces, custom transformations are therefore required
 * to properly map the geometry. This is illustrated for airspaces in this sample:
 * the AIXM 3.3/4.5 airspace domain object (<code>ILcdAirspace</code>) uses
 * the <code>ILcdGeoPath</code> interface to represent the geometry, which
 * has to be mapped to a curve representation compatible with GML 3.2.
 * This mapping is solved by the {@link LonLatGeoPathAsCurve} class, which is
 * used by the {@link AirspaceGeometryTransformation} class.
 */
public class AIXM45To51Transformation {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AIXM45To51Transformation.class);

  /**
   * Transforms the given model to an AIXM 5.1 model. Not all types are currently supported.
   *
   * @param aAIXMModel An AIXM 3.3/4.5 model, the elements of the model have to implement {@link ILcdDataObject} and need to use
   * data types from {@link TLcdAIXMDataTypes}.
   * @return A valid AIXM 5.1 model.
   */
  public ILcdModel transformToAIXM51(ILcdModel aAIXMModel) {

    TLcdTransformer toAIXM51Transformer = createAIXMTransformer();

    //Create a new AIXM 5.1 model
    TLcdAIXM51AbstractAIXMMessage aixm51Model = new TLcdAIXM51AbstractAIXMMessage(
        TLcdAIXM51MessageDataTypes.AIXM_BASIC_MESSSAGE_TYPE);
    TLcdAIXM51ModelDescriptor descriptor = new TLcdAIXM51ModelDescriptor(
        TLcdAIXM51DataTypes.getDataModel());

    //use better display name for nodes which depends on the source name iso a constant name
    if (TLcdAIXMModelListDescriptor.DISPLAY_NAME.equals(aAIXMModel.getModelDescriptor().getDisplayName())) {
      descriptor.setDisplayName(TLcdIOUtil.getFileName(aAIXMModel.getModelDescriptor().getSourceName()) + " (AIXM4.5)");
    } else {
      descriptor.setDisplayName(aAIXMModel.getModelDescriptor().getDisplayName() + " (AIXM4.5)");
    }
    descriptor.setIsSnapshotModel(false);
    aixm51Model.setModelDescriptor(descriptor);
    aixm51Model.setModelReference(aAIXMModel.getModelReference());
    aixm51Model.setId("MessageID1");

    Enumeration elements = aAIXMModel.elements();
    while (elements.hasMoreElements()) {
      Object object = elements.nextElement();

      //only convert if a target type is available
      if (!toAIXM51Transformer.getProvider().getTargetTypes(((ILcdDataObject) object).getDataType()).isEmpty()) {
        //correctly configure the transformation context
        Map<Object, Object> context = toAIXM51Transformer.createTransformationContext();
        AIXM51FeatureDataTypeMapping.setSRS(aAIXMModel.getModelReference(), context);
        // 1)transform each object to AIXM 5.1 first
        TLcdAIXM51Feature objectsAsAIXM51 = (TLcdAIXM51Feature) toAIXM51Transformer.transform((ILcdDataObject) object, context);
        objectsAsAIXM51.invalidateObject();
        // 2) add it to the model
        aixm51Model.addElement(objectsAsAIXM51, ILcdModel.NO_EVENT);
      }

    }
    aixm51Model.invalidateObject();
    return aixm51Model;
  }

  /**
   * Creates a transformer that can transform AIXM 3.3/4.5 objects into AIXM 5.1 objects.
   *
   * @return a configured transformer
   */
  private static TLcdTransformer createAIXMTransformer() {

    //create a mapping between the two data models, the differences are very large,
    //so it will not be possible to automatically determine correspondences
    TLcdDataModelMapping mapping = new TLcdDataModelMapping(
        TLcdAIXMDataTypes.getDataModel(), TLcdAIXM51DataTypes.getDataModel());

    //create the transformer up front, so we can pass it as an argument to transformations
    //that can use it to delegate transformation of properties
    TLcdTransformer toAIXM51Transformer = new TLcdTransformer();

    //set up the actual mapping for airspaces
    configureAirspaceMapping(mapping, toAIXM51Transformer);

    //set up a mapping for aerodromes
    configureAirportMapping(mapping, toAIXM51Transformer);

    //finally, the transformer can be configured with the mapping
    mapping.configure(toAIXM51Transformer);

    //the validator allows us to print out possible issues with the mapping
    //this is a useful tool when creating a mapping
    List<TransformationIssue> errors = new TLcdDataModelMappingValidator(
        mapping).validate();
    for (TransformationIssue transformationIssue : errors) {
      //ignore left over target types, because there are too many types in AIXM5 that will not be mappable
      if (!(transformationIssue instanceof LeftOverTargetType)) {
        sLogger.debug(transformationIssue.toString());
      }
    }
    return toAIXM51Transformer;
  }

  /**
   * Maps the properties of {@link TLcdAIXMDataTypes#Aerodrome} to {@link TLcdAIXM51DataTypes#AirportHeliportType}
   * @param aMapping The original data model mapping.
   * @param aToAIXM51Transformer A transformer that can be used with the mapping.
   */
  private static void configureAirportMapping(TLcdDataModelMapping aMapping, TLcdTransformer aToAIXM51Transformer) {
    //We wrap the mapping in a helper class that exposes some convenience methods for the
    //mapping to AIXM5, this will improve readability of the mapping code
    AIXM51FeatureDataTypeMapping airportTypeMapping = new AIXM51FeatureDataTypeMapping(
        TLcdAIXMDataTypes.Aerodrome, TLcdAIXM51DataTypes.AirportHeliportType, aMapping,
        aToAIXM51Transformer.getProvider());

    // setup default time slice properties such as the interpretation
    configureDefaultTimesliceProperties(airportTypeMapping);

    // enable automatic id generation
    airportTypeMapping.setIdProperty(TLcdAIXMAerodromeDataProperties.MID, "AIXM.");

    // specify all properties from the aerodrome that map to a corresponding
    // property of the TLcdAIXM51AirportHeliportTimeSlice
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.NAME, TLcdAIXM51AirportHeliportTimeSlice.AIRPORT_HELIPORT_NAME_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.ICAO_CODE, TLcdAIXM51AirportHeliportTimeSlice.LOCATION_INDICATOR_ICAO_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.IATA_CODE, TLcdAIXM51AirportHeliportTimeSlice.DESIGNATOR_IATA_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.ELEVATION, TLcdAIXM51AirportHeliportTimeSlice.FIELD_ELEVATION_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.ELEVATION_ACCURACY, TLcdAIXM51AirportHeliportTimeSlice.FIELD_ELEVATION_ACCURACY_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.SERVED_CITY, TLcdAIXM51AirportHeliportTimeSlice.SERVED_CITY_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.MAGNETIC_VARIATION, TLcdAIXM51AirportHeliportTimeSlice.MAGNETIC_VARIATION_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.MAGNETIC_VARIATION_DATE, TLcdAIXM51AirportHeliportTimeSlice.DATE_MAGNETIC_VARIATION_PROPERTY);
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.REMARK, TLcdAIXM51AirportHeliportTimeSlice.ANNOTATION_PROPERTY);

    // specify a custom transformation for the secondary power supply property
    airportTypeMapping.mapTimeSliceProperty(TLcdAIXMAerodromeDataProperties.SECONDARY_POWER_SUPPLY_DESCRIPTION, TLcdAIXM51AirportHeliportTimeSlice.SECONDARY_POWER_SUPPLY_PROPERTY,
                                            new SecondaryPowerSupplyTransformation());

    // The geometry of the timeslice requires a custom transformation because
    // there is no corresponding property in the AIXM ILcdAerodrome
    airportTypeMapping.mapTimeSliceTargetProperty(TLcdAIXM51AirportHeliportTimeSlice.ARP_PROPERTY, new PointToAIXM51ElevatedPoint());

    // some of the properties that were mapped require a custom type
    // transformation, because they are not trivial,
    // this can be specified directly on the TLcdDataModelMapping
    aMapping.mapType(TLcdCoreDataTypes.STRING_TYPE, TLcdAIXM51DataTypes.CityType, new StringToCityTransformation());
    aMapping.mapType(TLcdCoreDataTypes.FLOAT_TYPE, TLcdAIXM51DataTypes.ValDistanceVerticalType, new FloatToValDistanceVerticalTransformation());
    aMapping.mapType(TLcdCoreDataTypes.STRING_TYPE, TLcdAIXM51DataTypes.ValMagneticVariationBaseType, new StringToMagneticVariation());

  }

  /**
   * Configure the mapping to support transformation from {@link TLcdAIXMDataTypes#Airspace} to
   * {@link TLcdAIXM51DataTypes#AirspaceType}.
   * @param aMapping The mapping to configure
   * @param aTransformer A transformer that can be used with the mapping
   */
  private static void configureAirspaceMapping(TLcdDataModelMapping aMapping, TLcdTransformer aTransformer) {
    //We wrap the mapping in a helper class that exposes some convenience methods for the
    //mapping to AIXM5, this will improve readability of the mapping code
    AIXM51FeatureDataTypeMapping airspaceTypeMapping = new AIXM51FeatureDataTypeMapping(
        TLcdAIXMDataTypes.Airspace, TLcdAIXM51DataTypes.AirspaceType, aMapping,
        aTransformer.getProvider());
    configureDefaultTimesliceProperties(airspaceTypeMapping);

    // enable automatic id generation
    airspaceTypeMapping.setIdProperty(TLcdAIXMAirspaceDataProperties.MID, "AIXM.");

    // specify all properties from the ILcdAirspace that map to a corresponding
    // property of the TLcdAIXM51AirspaceTimeSlice
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.NAME, TLcdAIXM51AirspaceTimeSlice.AIRSPACE_NAME_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.IDENTIFIER, TLcdAIXM51AirspaceTimeSlice.DESIGNATOR_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.LOCATION_INDICATOR, TLcdAIXM51AirspaceTimeSlice.DESIGNATOR_ICAO_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.LOCAL_TYPE_DESIGNATOR, TLcdAIXM51AirspaceTimeSlice.LOCAL_TYPE_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.MILITARY, TLcdAIXM51AirspaceTimeSlice.CONTROL_TYPE_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.LOWER_UPPER_LIMIT, TLcdAIXM51AirspaceTimeSlice.UPPER_LOWER_SEPARATION_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.CLASS, TLcdAIXM51AirspaceTimeSlice.CLASS_CODE_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.TYPE, TLcdAIXM51AirspaceTimeSlice.TYPE_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.REMARK, TLcdAIXM51AirspaceTimeSlice.ANNOTATION_PROPERTY);
    airspaceTypeMapping.mapTimeSliceProperty(TLcdAIXMAirspaceDataProperties.AIRSPACE_ACTIVITY, TLcdAIXM51AirspaceTimeSlice.ACTIVATION_PROPERTY);

    // The geometry of the timeslice requires a custom transformation
    airspaceTypeMapping.mapTimeSliceTargetProperty(TLcdAIXM51AirspaceTimeSlice.GEOMETRY_COMPONENT_PROPERTY, new AirspaceGeometryTransformation());

    // some of the properties that were mapped require a custom type
    // transformation, because they are not trivial,
    // this can be specified directly on the TLcdDataModelMapping
    aMapping.mapType(TLcdAIXMAirspaceDataProperties.TYPE.getType(), TLcdAIXM51DataTypes.CodeAirspaceBaseType, new AirspaceTypeTransformation());
    aMapping.mapType(TLcdAIXMAirspaceDataProperties.AIRSPACE_ACTIVITY.getType(), TLcdAIXM51DataTypes.AirspaceActivationType, new AirspaceActivityTransformation());
    aMapping.mapType(TLcdAIXMAirspaceDataProperties.CLASS.getType(), TLcdAIXM51DataTypes.AirspaceLayerClassType, new AirspaceClassTransformation());
    aMapping.mapType(TLcdCoreDataTypes.FLOAT_TYPE, TLcdAIXM51DataTypes.ValFLType, new GenericAIXM51Transformations.FloatToFlightLevel());
    aMapping.mapType(TLcdCoreDataTypes.STRING_TYPE, TLcdAIXM51DataTypes.NoteType, new TextToNoteTransformation());
  }

  /**
   * All timeslices have some default properties that need to be set.
   */
  private static void configureDefaultTimesliceProperties(AIXM51FeatureDataTypeMapping aAIXM5FeatureTypeMapping) {
    TLcdGML32TimePrimitiveProperty timePrimitive = new TLcdGML32TimePrimitiveProperty();
    timePrimitive.setAbstractTimePrimitive(AIXM51DesignatedPointCreator.createIndeterminateTimePeriod());
    aAIXM5FeatureTypeMapping.setTimeSliceProperty(TLcdGML32AbstractTimeSlice.VALID_TIME_PROPERTY, timePrimitive);
    aAIXM5FeatureTypeMapping.setTimeSliceProperty(TLcdAIXM51AbstractAIXMTimeSlice.INTERPRETATION_PROPERTY, ELcdAIXM51Interpretation.BASELINE);
    aAIXM5FeatureTypeMapping.setTimeSliceProperty(TLcdAIXM51AbstractAIXMTimeSlice.SEQUENCE_NUMBER_PROPERTY, 0L);
  }

  /**
   * A transformation that always returns YES, because the secondary power
   * supply string does not use a fixed format, but always seems to indicate
   * that it is available.
   */
  private static final class SecondaryPowerSupplyTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      //there does not appear to be a standard format for the secondary power supply text that can be parsed
      //we assume that if the text is set, a power supply is available
      return TLcdAIXM51CodeYesNo.YES;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from TLcdAirspaceActivityType to a TLcdAIXM51AirspaceActivation.
   */
  private static final class AirspaceActivityTransformation extends ALcdObjectTransformation {
    private static final Map<TLcdAirspaceActivityType, TLcdAIXM51CodeAirspaceActivity> sCustomMapping = new HashMap<TLcdAirspaceActivityType, TLcdAIXM51CodeAirspaceActivity>();

    private static void map(TLcdAirspaceActivityType aType, TLcdAIXM51CodeAirspaceActivity a51Type) {
      sCustomMapping.put(aType, a51Type);
    }

    static {
      // list all non-trivial mappings
      map(TLcdAirspaceActivityType.TFC_AD, TLcdAIXM51CodeAirspaceActivity.AD_TFC);
      map(TLcdAirspaceActivityType.TFC_HELI, TLcdAIXM51CodeAirspaceActivity.HELI_TFC);
      map(TLcdAirspaceActivityType.ACROBAT, TLcdAIXM51CodeAirspaceActivity.AEROBATICS);
      map(TLcdAirspaceActivityType.DROP, TLcdAIXM51CodeAirspaceActivity.AIR_DROP);
      map(TLcdAirspaceActivityType.ASCENT, TLcdAIXM51CodeAirspaceActivity.RADIOSONDE);
      map(TLcdAirspaceActivityType.SPACEFLT, TLcdAIXM51CodeAirspaceActivity.SPACE_FLIGHT);
      map(TLcdAirspaceActivityType.WORK, TLcdAIXM51CodeAirspaceActivity.AERIAL_WORK);
      map(TLcdAirspaceActivityType.DUSTING, TLcdAIXM51CodeAirspaceActivity.CROP_DUSTING);
      map(TLcdAirspaceActivityType.FIRE, TLcdAIXM51CodeAirspaceActivity.FIRE_FIGHTING);
      map(TLcdAirspaceActivityType.JETCLIMB, TLcdAIXM51CodeAirspaceActivity.JET_CLIMBING);
      map(TLcdAirspaceActivityType.NAVAL, TLcdAIXM51CodeAirspaceActivity.NAVAL_EXER);
      map(TLcdAirspaceActivityType.AIRGUN, TLcdAIXM51CodeAirspaceActivity.AIR_GUN);
      map(TLcdAirspaceActivityType.SHOOT, TLcdAIXM51CodeAirspaceActivity.SHOOTING);
      map(TLcdAirspaceActivityType.BLAST, TLcdAIXM51CodeAirspaceActivity.BLASTING);
      map(TLcdAirspaceActivityType.WATERBLAST, TLcdAIXM51CodeAirspaceActivity.WATER_BLASTING);
      map(TLcdAirspaceActivityType.ANTIHAIL, TLcdAIXM51CodeAirspaceActivity.ANTI_HAIL);
      map(TLcdAirspaceActivityType.BIRD, TLcdAIXM51CodeAirspaceActivity.BIRD_MIGRATION);
      map(TLcdAirspaceActivityType.IND_OIL, TLcdAIXM51CodeAirspaceActivity.REFINERY);
      map(TLcdAirspaceActivityType.IND_CHEM, TLcdAIXM51CodeAirspaceActivity.CHEMICAL);
      map(TLcdAirspaceActivityType.IND_NUCLEAR, TLcdAIXM51CodeAirspaceActivity.NUCLEAR);
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51AirspaceActivation activation = new TLcdAIXM51AirspaceActivation();
      TLcdAirspaceActivityType activity = (TLcdAirspaceActivityType) aObject;
      activation.setActivity(getAIXM51Code(activity));
      return activation;
    }

    private TLcdAIXM51CodeAirspaceActivity getAIXM51Code(TLcdAirspaceActivityType activity) {
      String code = activity.getCode().replaceAll("-", "_");
      TLcdAIXM51CodeAirspaceActivity aixm51Code = TLcdAIXM51CodeAirspaceActivity.getWellKnownValues().get(code);
      if (aixm51Code != null) {
        return aixm51Code;
      } else {
        return new TLcdAIXM51CodeAirspaceActivity("OTHER:" + activity.getName().replaceAll(" ", "_"));
      }
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

}
