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

import com.luciad.datamodel.ILcdAnnotation;

/**
 * Annotation for a {@link com.luciad.datamodel.TLcdDataProperty} that
 * describes the range of the values.
 *
 * Use this for properties that have a continuous range of values.
 *
 * @param <T> The type of the property value, e.g. Double or Integer.
 */
public class RangeAnnotation<T extends Number> implements ILcdAnnotation {

  private final T fLowerBound;
  private final T fUpperBound;

  /**
   * Use this constructor if the lower and upper bound are known up front.
   * <p>
   * Use {@link #newBuilder()} if you want to determine the bounds while decoding data.
   * </p>
   *
   * @param aLowerBound The lower bound, not null
   * @param aUpperBound The upper bound, not null
   */
  public RangeAnnotation(T aLowerBound, T aUpperBound) {
    fLowerBound = aLowerBound;
    fUpperBound = aUpperBound;
  }

  /**
   * Use this if you want to determine the bounds while decoding data.
   * <p>
   * Use {@link #RangeAnnotation(T, T)} if the bounds are known up front.
   * </p>
   */
  public static <T extends Number> Builder<T> newBuilder() {
    return new Builder<T>();
  }

  /**
   * Returns the lower bound of the range.
   * @return the lower bound of the range.
   */
  public T getLowerBound() {
    return fLowerBound;
  }

  /**
   * Returns the upper bound of the range.
   * @return the upper bound of the range.
   */
  public T getUpperBound() {
    return fUpperBound;
  }

  /**
   * Builder/accumulator to determine the range of the dataset.
   */
  public static class Builder<T extends Number> {
    private T fLowerBound = null;
    private T fUpperBound = null;

    private Builder() {
    }

    /**
     * Add a real value to the builder.  The bounds will be extended to include this value.
     *
     * @param aValue The value to accumulate
     * @return this
     */
    public Builder<T> accumulate(T aValue) {
      if (fLowerBound == null && fUpperBound == null) {
        fLowerBound = aValue;
        fUpperBound = aValue;
      } else {
        if (aValue.doubleValue() < fLowerBound.doubleValue()) {
          fLowerBound = aValue;
        }
        if (aValue.doubleValue() > fUpperBound.doubleValue()) {
          fUpperBound = aValue;
        }
      }

      return this;
    }

    /**
     * Creates a {@link RangeAnnotation}, if at least one value was added.
     * <p>
     * If no values were accumulated, returns {@code null}.
     * </p>
     *
     * @return A new {@link RangeAnnotation}, or {@code null} if no values were accumulated.
     */
    public RangeAnnotation<T> build() {
      if (fUpperBound == null || fLowerBound == null) {
        return null;
      } else {
        return new RangeAnnotation<T>(fLowerBound, fUpperBound);
      }
    }
  }
}
