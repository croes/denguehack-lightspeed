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

import java.lang.reflect.Constructor;
import java.util.Map;

import com.luciad.ais.model.airspace.type.TLcdAirspaceClass;
import com.luciad.ais.model.airspace.type.TLcdAirspaceType;
import com.luciad.datamodel.transformation.ALcdObjectTransformation;
import com.luciad.datamodel.transformation.ILcdObjectTransformation;
import com.luciad.format.aixm51.model.datatypes.ALcdAIXM51Code;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeAirspace;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeAirspaceClassification;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeNotePurpose;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51TextNote;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51UomDistanceVertical;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51UomFL;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51ValDistanceVertical;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51ValFL;
import com.luciad.format.aixm51.model.features.airportheliport.airportheliport.TLcdAIXM51City;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceLayerClass;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51ElevatedPoint;
import com.luciad.format.aixm51.model.features.notes.TLcdAIXM51LinguisticNote;
import com.luciad.format.aixm51.model.features.notes.TLcdAIXM51Note;
import com.luciad.shape.ILcdPoint;

/**
 * A class containing transformations from generic format independent types to
 * AIXM 5.1 types.
 */
public final class GenericAIXM51Transformations {

  /**
   * A transformation from a <code>String</code> to a
   * <code>TLcdAIXM51DataTypes.ValMagneticVariationBaseType</code>, which is
   * basically a <code>Double</code>.
   */
  public static final class StringToMagneticVariation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      return Double.parseDouble(aObject.toString());
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from a <code>Float</code> to a
   * <code>TLcdAIXM51ValDistanceVertical</code>.
   */
  public static final class FloatToValDistanceVerticalTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51ValDistanceVertical verticalDistance = new TLcdAIXM51ValDistanceVertical();
      verticalDistance.setValue((Float) aObject);
      // we assume that this is in meters
      verticalDistance.setUom(TLcdAIXM51UomDistanceVertical.M);
      return verticalDistance;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from a <code>String</code> to a
   * <code>TLcdAIXM51City</code>.
   */
  public static final class StringToCityTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51City city = new TLcdAIXM51City();
      city.setCityName(aObject.toString());
      return city;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from a String to a <code>TLcdAIXM51Note</code>.
   */
  public static final class TextToNoteTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51Note note = new TLcdAIXM51Note();
      note.setPurpose(TLcdAIXM51CodeNotePurpose.REMARK);
      TLcdAIXM51LinguisticNote linguisticNote = new TLcdAIXM51LinguisticNote();
      TLcdAIXM51TextNote textNote = new TLcdAIXM51TextNote();
      textNote.setValueObject((String) aObject);
      linguisticNote.setNote(textNote);
      note.getTranslatedNote().add(linguisticNote);
      return note;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from a float to a {@link TLcdAIXM51ValFL}, the unit of
   * measure is assumed to be flight levels.
   */
  public static final class FloatToFlightLevel implements ILcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51ValFL value = new TLcdAIXM51ValFL();
      value.setUom(TLcdAIXM51UomFL.FL);
      value.setValue((Float) aObject);
      return value;
    }

    @Override
    public ILcdObjectTransformation getInverse() {
      return null;
    }
  }

  /**
   * A transformation from an <code>ILcdPoint</code> to a
   * <code>TLcdAIXM51ElevatedPoint</code>.
   */
  public static final class PointToAIXM51ElevatedPoint extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAIXM51ElevatedPoint elevatedPoint = new TLcdAIXM51ElevatedPoint();
      ILcdPoint aerodrome = (ILcdPoint) aObject;
      elevatedPoint.move3D(aerodrome);
      return elevatedPoint;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from <code>TLcdAirspaceType</code> to
   * <code>TLcdAIXM51CodeAirspace</code>.
   */
  public static final class AirspaceTypeTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAirspaceType type = (TLcdAirspaceType) aObject;
      String typeCode = type.getCode().replaceAll("-", "_");
      TLcdAIXM51CodeAirspace code = TLcdAIXM51CodeAirspace.getWellKnownValues().get(typeCode);
      if (code != null) {
        return code;
      } else if (TLcdAirspaceType.R_AMC.equals(type)) {
        return TLcdAIXM51CodeAirspace.R;
      } else if (TLcdAirspaceType.D_AMC.equals(type)) {
        return TLcdAIXM51CodeAirspace.D;
      } else {

        return new TLcdAIXM51CodeAirspace("OTHER:" + typeCode);
      }
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A transformation from <code>TLcdAirspaceClass</code> to
   * <code>TLcdAIXM51AirspaceLayerClass</code>.
   */
  public static final class AirspaceClassTransformation extends ALcdObjectTransformation {
    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      TLcdAirspaceClass airspaceClass = (TLcdAirspaceClass) aObject;
      TLcdAIXM51AirspaceLayerClass aixm51Class = new TLcdAIXM51AirspaceLayerClass();
      if (airspaceClass.toString().startsWith("Class ")) {
        aixm51Class.setClassification(TLcdAIXM51CodeAirspaceClassification.getWellKnownValues().get(airspaceClass.toString().substring(6, 7)));
      } else {
        aixm51Class.setClassification(new TLcdAIXM51CodeAirspaceClassification("OTHER:" + airspaceClass.toString()));
      }
      return aixm51Class;
    }

    @Override
    protected Object invert(Object aObject, Map<Object, Object> aContext) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A generic transformation to a <code>ALcdAIXM51Code</code>. This is used as
   * a fall back, but in a lot of cases, a more specific transformation for a
   * specific type of code may be needed.
   * <p/>
   * This transformation is used by {@link AIXM51FeatureDataTypeMapping}, so
   * there is no need to register it explicitly.
   */
  public static final class ObjectToAIXM51CodeTransformation implements ILcdObjectTransformation {

    private final Class<? extends ALcdAIXM51Code> fAIXMCodeClass;

    public ObjectToAIXM51CodeTransformation(Class<? extends ALcdAIXM51Code> aAIXMCodeClass) {
      super();
      fAIXMCodeClass = aAIXMCodeClass;
    }

    @Override
    public Object transform(Object aObject, Map<Object, Object> aContext) {
      String code = aObject.toString();
      ALcdAIXM51Code aixm51Code = ALcdAIXM51Code.getWellKnown(fAIXMCodeClass, code);

      if (aixm51Code != null) {
        return aixm51Code;
      } else {
        try {
          Constructor<? extends ALcdAIXM51Code> constructor = fAIXMCodeClass.getConstructor(String.class);
          if (constructor != null) {
            return null;// constructor.newInstance( "Other:" + code );
          } else {
            throw new IllegalArgumentException("Can not create code of type: " + fAIXMCodeClass);
          }
        } catch (Exception e) {
          throw new IllegalArgumentException("Can not create code of type: " + fAIXMCodeClass, e);
        }
      }
    }

    @Override
    public ILcdObjectTransformation getInverse() {
      return null;
    }
  }

}
