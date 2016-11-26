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
import java.text.ParsePosition;

/**
 * Format that works with Strings.  So it doesn't perform any real formatting or parsing, but it
 * is convenient to let ValidatingTextField work with plain Strings.
 */
public class StringFormat extends EmptyFormatWrapper {

  public StringFormat() {
    super(new DelegateStringFormat());
  }

  @Override
  protected Object createEmptyValueObject() {
    return "";
  }

  public static Format createStringFormat(boolean aAcceptEmptyStrings) {
    return aAcceptEmptyStrings ? new StringFormat() : new DelegateStringFormat();
  }

  private static class DelegateStringFormat extends Format {
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      if (obj instanceof String) {
        String s = (String) obj;
        pos.setEndIndex(pos.getEndIndex() + s.length());
        return toAppendTo.append(s);
      } else {
        throw new IllegalArgumentException("StringFormat: could not format object [" + obj + "] since it is no string");
      }
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
      if (!source.isEmpty()) {
        pos.setIndex(source.length());
        return source;
      } else {
        pos.setErrorIndex(0);
        return null;
      }
    }

  }
}
