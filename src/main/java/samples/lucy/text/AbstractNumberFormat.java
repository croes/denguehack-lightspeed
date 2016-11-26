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
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * This format wraps a NumberFormat, but converts the parse values before returning.
 *
 * @since 2013.0
 */
public abstract class AbstractNumberFormat extends Format {

  private final NumberFormat fDelegate;

  protected AbstractNumberFormat() {
    this(null);
  }

  /**
   * Creates a new {@code AbstractNumberFormat} instance.
   * @param aLocale The locale to be used. When {@code null}, the default locale for formatting will be used.
   */
  protected AbstractNumberFormat(Locale aLocale) {
    fDelegate = aLocale != null ? NumberFormat.getNumberInstance(aLocale) : NumberFormat.getNumberInstance();
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    return fDelegate.format(obj, toAppendTo, pos);
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    int oldIndex = pos.getIndex();
    Number number = (Number) fDelegate.parseObject(source, pos);
    if (number != null) {
      Number result = convert(number);
      if (result == null) {
        pos.setIndex(oldIndex);
        pos.setErrorIndex(0);
        return null;
      } else {
        return result;
      }
    } else {
      return null;
    }
  }

  protected abstract Number convert(Number aNumber);

  protected NumberFormat getDelegate() {
    return fDelegate;
  }
}
