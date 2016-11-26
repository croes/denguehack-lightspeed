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
package samples.lightspeed.plots.datamodelstyling;

import static com.luciad.util.expression.TLcdExpressionFactory.AttributeValueProvider;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;

/**
 * {@link AttributeValueProvider} that maps a data property with {@link EnumAnnotation}
 * to a set of numbers.
 *
 * For example, say the domain objects have values "a", "b" and "c" for the property (combined).
 * Each value "a" will be mapped to 0, "b" to 1 and "c" to 2.
 */
public class EnumAttributeProvider implements AttributeValueProvider<Float> {

  private final TLcdDataProperty fDataProperty;
  private final EnumAnnotation fEnumAnnotation;

  public EnumAttributeProvider(TLcdDataProperty aDataProperty) {
    fDataProperty = aDataProperty;
    fEnumAnnotation = aDataProperty.getAnnotation(EnumAnnotation.class);
    if (fEnumAnnotation == null) {
      throw new IllegalStateException("Property should have an " + EnumAnnotation.class.getSimpleName());
    }
  }

  @Override
  public Float getValue(Object aObject, Object aGeometry) {
    Object value = ((ILcdDataObject) aObject).getValue(fDataProperty);
    return (float) fEnumAnnotation.indexOf(value);
  }
}
