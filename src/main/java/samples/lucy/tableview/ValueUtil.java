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
package samples.lucy.tableview;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Date;

import org.jdesktop.swingx.renderer.StringValue;

import samples.lucy.text.ByteFormat;
import samples.lucy.text.DoubleFormat;
import samples.lucy.text.FloatFormat;
import samples.lucy.text.IntegerFormat;
import samples.lucy.text.LongFormat;
import samples.lucy.text.ShortFormat;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCode;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;

/**
 * Utility class for feature related methods.
 */
public class ValueUtil {

  private static final TLcdGeoReference2GeoReference fPointConverter = new TLcdGeoReference2GeoReference();
  private static final NumberFormat fNumberFormat = NumberFormat.getInstance();

  /**
   * Private constructor. Class only contains static utility methods, so no need to create instances
   * of this class
   */
  private ValueUtil() {
  }

  /**
   * Creates a composite StringValue for specific and common types. If none of these string values
   * can return a string, Object.toString is used.
   *
   * @param aLucyEnv        The Lucy back-end, used to retrieve unit settings from.
   * @param aModelReference The reference of the model, used to format points.
   */
  public static StringValue createCompositeStringValue(ILcyLucyEnv aLucyEnv, ILcdModelReference aModelReference) {
    CompositeStringValue composite = new CompositeStringValue();

    //make sure null values are handled consistently
    composite.addStringValue(ValueUtil.createNullStringValue());
    //make sure the renderer is capable of formatting points, ISO1903Measures, ...
    composite.addStringValue(ValueUtil.createISO19103MeasureStringValue(aLucyEnv));
    composite.addStringValue(ValueUtil.createDateStringValue(aLucyEnv));
    composite.addStringValue(ValueUtil.createPointStringValue(aModelReference, aLucyEnv));
    //Add string values based on the Formats that are used for doubles, ints etc...
    composite.addStringValue(ValueUtil.createDoubleStringValue());
    composite.addStringValue(ValueUtil.createIntegerStringValue());
    composite.addStringValue(ValueUtil.createFloatStringValue());
    composite.addStringValue(ValueUtil.createLongStringValue());
    composite.addStringValue(ValueUtil.createShortStringValue());
    composite.addStringValue(ValueUtil.createByteStringValue());

    return composite;
  }

  /**
   * <p>Creates a <code>StringValue</code> which can format <code>ILcdPoint</code> instances. Note
   * that the returned <code>StringValue</code> will return <code>null</code> for
   * non-<code>ILcdPoint</code> implementations. Therefore, it cannot be used directly with SwingX
   * classes, but it must still be wrapped or contained in a <code>CompositeStringValue</code>.</p>
   *
   * @param aModelReference The reference of the model, containing the point
   * @param aLucyEnv        The Lucy back-end
   *
   * @return a <code>StringValue</code> which can format <code>ILcdPoint</code> instances
   */
  public static StringValue createPointStringValue(final ILcdModelReference aModelReference, final ILcyLucyEnv aLucyEnv) {
    return new StringValue() {
      @Override
      public String getString(Object aObject) {
        if (aObject instanceof ILcdPoint) {
          return formatPoint(aLucyEnv, ((ILcdPoint) aObject), aModelReference);
        }
        return null;
      }
    };
  }

  /**
   * <p>Creates a <code>StringValue</code> which can format <code>ILcdISO19103Measure</code>
   * instances. Note that the returned <code>StringValue</code> will return <code>null</code> for
   * non-<code>ILcdISO19103Measure</code> implementations. Therefore, it cannot be used directly
   * with SwingX classes, but it must still be wrapped or contained in a
   * <code>CompositeStringValue</code>.</p>
   *
   * @param aLucyEnv The Lucy back-end
   *
   * @return a <code>StringValue</code> which can format <code>ILcdISO19103Measure</code> instances
   */
  public static StringValue createISO19103MeasureStringValue(final ILcyLucyEnv aLucyEnv) {
    return new StringValue() {
      @Override
      public String getString(Object aObject) {
        if (aObject instanceof ILcdISO19103Measure) {
          return formatMeasure(aLucyEnv, (ILcdISO19103Measure) aObject);
        }
        return null;
      }
    };
  }

  /**
   * <p>Creates a <code>StringValue</code> which can format <code>Date</code> instances. Note that
   * the returned <code>StringValue</code> will return <code>null</code> for non-<code>Date</code>
   * implementations. Therefore, it cannot be used directly with SwingX classes, but it must still
   * be wrapped or contained in a <code>CompositeStringValue</code>.</p>
   *
   * @param aLucyEnv The Lucy back-end
   *
   * @return a <code>StringValue</code> which can format <code>Date</code> instances
   */
  public static StringValue createDateStringValue(final ILcyLucyEnv aLucyEnv) {
    return new StringValue() {
      @Override
      public String getString(Object aObject) {
        if (aObject instanceof Date) {
          return aLucyEnv.getDefaultDateTimeFormat().format(aObject);
        }
        return null;
      }
    };
  }

  /**
   * <p>Creates a <code>StringValue</code> which can format <code>null</code>. Using this
   * <code>StringValue</code> ensures that <code>null</code> will always be handled consistently.
   * It will always return an empty <code>String</code>.</p>
   *
   * @return a <code>StringValue</code> which can format <code>null</code> values
   */
  public static StringValue createNullStringValue() {
    return new StringValue() {
      @Override
      public String getString(Object aValue) {
        if (aValue == null) {
          return "";
        } else {
          return null;
        }
      }
    };
  }

  public static StringValue createDoubleStringValue() {
    return new FormatAdapter(Double.class, new DoubleFormat());
  }

  public static StringValue createIntegerStringValue() {
    return new FormatAdapter(Integer.class, new IntegerFormat());
  }

  public static StringValue createFloatStringValue() {
    return new FormatAdapter(Float.class, new FloatFormat());
  }

  public static StringValue createLongStringValue() {
    return new FormatAdapter(Long.class, new LongFormat());
  }

  public static StringValue createShortStringValue() {
    return new FormatAdapter(Short.class, new ShortFormat());
  }

  public static StringValue createByteStringValue() {
    return new FormatAdapter(Byte.class, new ByteFormat());
  }

  /**
   * Converts the given point in the given model reference to a human readable representation for
   * that point.  It respects the ILcyLucyEnv.getDefaultModelReference() property, so that points
   * are transformed into that target coordinate system if needed.
   *
   * @param aLucyEnv        Used to find the default lucy formats.
   * @param aPoint          The point to format.
   * @param aPointReference The reference in which the point is defined.
   *
   * @return A human readable representation for the given point.
   */
  private static String formatPoint(ILcyLucyEnv aLucyEnv, ILcdPoint aPoint, ILcdModelReference aPointReference) {
    //Convert the point to the default model reference set in ILcyLucyEnv
    TLcdXYZPoint converted_point = new TLcdXYZPoint();
    ILcdModelReference destination_ref = aLucyEnv.getDefaultModelReference();
    if (destination_ref == null) {
      //No conversion needed, display coordinate in its own coordinate system
      converted_point.move3D(aPoint);
      destination_ref = aPointReference;
    } else {
      fPointConverter.setSourceReference(aPointReference);
      fPointConverter.setDestinationReference(destination_ref);
      try {
        fPointConverter.sourcePoint2destinationSFCT(aPoint, converted_point);
      } catch (TLcdOutOfBoundsException ignore) {
        //Coordinate cannot be expressed in the target coordinate system.
        return "-";
      }
    }

    if (isGeodeticReference(destination_ref)) {
      return aLucyEnv.getDefaultLonLatPointFormat().format(converted_point);
    } else {
      TLcdDistanceFormat distance = aLucyEnv.getDefaultDistanceFormat();
      return distance.format(converted_point.getX()) + ", " + distance.format(converted_point.getY());
    }
  }

  private static boolean isGeodeticReference(ILcdModelReference aModelReference) {
    return aModelReference instanceof ILcdGeoReference &&
           ((ILcdGeoReference) aModelReference).getCoordinateType() == ILcdGeoReference.GEODETIC;
  }

  /**
   * Converts the given ILcdISO19103Measure to a human readable String.
   *
   * @param aLucyEnv Used to find the default lucy formats.
   * @param aMeasure The measure to format.
   *
   * @return A human readable representation for the given measure.
   */
  private static String formatMeasure(ILcyLucyEnv aLucyEnv, ILcdISO19103Measure aMeasure) {
    ILcdISO19103UnitOfMeasure uom = aMeasure.getUnitOfMeasure();
    if (uom != null) {
      Format format;
      TLcdISO19103MeasureTypeCode uom_type = uom.getMeasureType();

      //the default value we will try to format
      Object value = uom.convertToStandard(aMeasure.getValue());

      if (uom_type == TLcdISO19103MeasureTypeCodeExtension.ALTITUDE) {
        format = aLucyEnv.getDefaultAltitudeFormat();
      } else if (uom_type == TLcdISO19103MeasureTypeCode.DISTANCE) {
        format = aLucyEnv.getDefaultDistanceFormat();
      } else if (uom_type == TLcdISO19103MeasureTypeCode.ANGLE) {
        format = aLucyEnv.getDefaultAzimuthFormat();
      } else if (uom_type == TLcdISO19103MeasureTypeCode.TIME) {
        format = aLucyEnv.getDefaultDateTimeFormat();
        //standard unit for time is seconds, but DateFormat expects milliseconds
        value = new Date((long) (uom.convertToStandard(aMeasure.getValue()) * 1000.0));
      } else if (uom_type == TLcdISO19103MeasureTypeCodeExtension.DURATION) {
        format = aLucyEnv.getDefaultDurationFormat();
        value = aMeasure;
      } else if (uom_type == TLcdISO19103MeasureTypeCode.VELOCITY) {
        format = aLucyEnv.getDefaultSpeedFormat();
      } else {
        format = null;
      }

      if (format != null) {
        return format.format(value);
      }
    }
    String symbol = uom == null ? "" : uom.getUOMSymbol();
    return fNumberFormat.format(aMeasure.getValue()) + " " + symbol;
  }

  private static class FormatAdapter implements StringValue {
    private final Class<?> fClass;
    private final Format fDelegateFormat;

    public FormatAdapter(Class<?> aClass, Format aDelegateFormat) {
      fClass = aClass;
      fDelegateFormat = aDelegateFormat;
    }

    @Override
    public String getString(Object value) {
      if (fClass.isInstance(value)) {
        return fDelegateFormat.format(value);
      } else {
        return null;
      }
    }
  }
}
