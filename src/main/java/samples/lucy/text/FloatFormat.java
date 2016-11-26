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

import java.util.Locale;

/**
 * Implementation of AbstractNumberFormat for Floats.
 *
 * @since 2013.0
 */
public class FloatFormat extends AbstractNumberFormat {

  private static double MIN_VALUE = -(double) Float.MAX_VALUE;
  private static double MAX_VALUE = (double) Float.MAX_VALUE;

  public FloatFormat() {
    this(null);
  }

  public FloatFormat(Locale aLocale) {
    super(aLocale);
    //We show at least one number after the decimal point, to show it is a floating point value.
    getDelegate().setMinimumFractionDigits(1);
  }

  @Override
  protected Float convert(Number aNumber) {
    double value = aNumber.doubleValue();
    if (value < MIN_VALUE || value > MAX_VALUE) {
      return null;
    }
    return (float) value;
  }
}
