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
package samples.lucy.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * <p>Wrapper around a format which accepts empty values.</p>
 *
 * <p>It will only delegate the method calls to the wrapped format when
 * for non-empty strings.</p>
 *
 * @since 10.0
 */
public abstract class EmptyFormatWrapper extends Format {
  private final Format fDelegateFormat;
  private final Object fDummyObject;

  /**
   * Create a wrapper around <code>aDelegateFormat</code>
   * @param aDelegateFormat the format to be wrapped.
   */
  protected EmptyFormatWrapper(Format aDelegateFormat) {
    if (aDelegateFormat == null) {
      throw new NullPointerException("The delegate format can not be null.");
    }
    fDelegateFormat = aDelegateFormat;
    fDummyObject = createEmptyValueObject();
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    if (isDummyObject(obj)) {
      return toAppendTo;
    }
    return fDelegateFormat.format(obj, toAppendTo, pos);
  }

  private boolean isDummyObject(Object aObject) {
    if (fDummyObject == null) {
      return aObject == null;
    } else {
      return fDummyObject.equals(aObject);
    }
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    if ("".equals(source)) {
      return fDummyObject;
    }
    return fDelegateFormat.parseObject(source, pos);
  }

  @Override
  public Object parseObject(String source) throws ParseException {
    //Format doesn't handle empty string correctly.
    ParsePosition pos = new ParsePosition(0);
    Object value = parseObject(source, pos);
    if (pos.getErrorIndex() == -1) {
      return value;
    } else {
      throw new ParseException("Can't parse " + source + ".", pos.getErrorIndex());
    }
  }

  /**
   * Create the object which will be returned when this format is asked to parse an empty String.
   *
   * @return the object which will be returned when this format is asked to parse an empty String.
   *         Must not be <code>null</code>.
   */
  protected abstract Object createEmptyValueObject();
}
