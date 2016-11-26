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
 * Wrapper around an existing format which only accepts positive Numbers.
 */
public class PositiveNumberFormat extends Format {
  private Format fDelegateFormat;

  public PositiveNumberFormat(Format aDelegateFormat) {
    fDelegateFormat = aDelegateFormat;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    return fDelegateFormat.format(obj, toAppendTo, pos);
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    int before_parse_index = pos.getIndex();
    Object object = fDelegateFormat.parseObject(source, pos);
    if (object instanceof Number) {
      double value = ((Number) object).doubleValue();
      if (value < 0) {
        pos.setIndex(before_parse_index);
        pos.setErrorIndex(before_parse_index);
        return null;
      }
    }
    return object;
  }

}
