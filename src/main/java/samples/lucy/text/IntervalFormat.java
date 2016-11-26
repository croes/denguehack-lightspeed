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

import com.luciad.util.ILcdInterval;

/**
 * NumberFormat which only accepts values in a certain interval.
 */
public class IntervalFormat extends Format {
  private Format fDelegateFormat = NumberFormat.getInstance();
  private ILcdInterval fInterval;

  /**
   * Create a new format for the interval <code>aInterval</code>. A general purpose default locale
   * NumberFormat is used to parse and format the values.
   * @param aInterval the interval
   */
  public IntervalFormat(ILcdInterval aInterval) {
    fInterval = aInterval;
  }

  public IntervalFormat(Format aDelegateFormat, ILcdInterval aInterval) {
    fDelegateFormat = aDelegateFormat;
    fInterval = aInterval;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    if (obj instanceof Number &&
        (((Number) obj).doubleValue() < fInterval.getMin() || ((Number) obj).doubleValue() > fInterval.getMax())) {
      return toAppendTo;
    } else {
      return fDelegateFormat.format(obj, toAppendTo, pos);
    }
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    int old_index = pos.getIndex();
    int old_error_index = pos.getErrorIndex();
    Object parsedObject = fDelegateFormat.parseObject(source, pos);
    if (parsedObject instanceof Number) {
      Number result = (Number) parsedObject;
      if (result.doubleValue() < fInterval.getMin() ||
          result.doubleValue() > fInterval.getMax()) {
        pos.setIndex(old_index);
        pos.setErrorIndex(old_error_index);
        return null;
      }
      return result;
    }
    return parsedObject;
  }

  /**
   * Returns the delegate format, used to parse and format the values.
   *
   * @return the delegate format.
   */
  public Format getDelegateFormat() {
    return fDelegateFormat;
  }
}
