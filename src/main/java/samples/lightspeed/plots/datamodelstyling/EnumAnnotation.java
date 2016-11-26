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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.datamodel.ILcdAnnotation;

/**
 * Annotation for a {@link com.luciad.datamodel.TLcdDataProperty} that
 * describes the possible different values.
 *
 * Use this when the number of possible different values is a limited, fixed set.
 *
 * @param <T> The type of the property value
 */
public class EnumAnnotation<T> implements ILcdAnnotation {

  private final List<T> fValues;

  /**
   * Use this constructor when you already know the possible values.
   * <p>
   * Use {@link #newBuilder} if you want to determine the possible values while creating the data model.
   * </p>
   *
   * @param aValues The fixed set of possible values
   */
  public EnumAnnotation(Collection<T> aValues) {
    fValues = new ArrayList<T>(aValues);
  }

  /**
   * Use this if you want to determine the possible values while creating the data model.
   * <p>
   * Use {@link #EnumAnnotation} when you already know the possible values up front.
   * </p>
   */
  public static <T> Builder<T> newBuilder() {
    return newBuilder(50);
  }

  /**
   * Use this if you want to determine the possible values while creating the data model.
   * <p>
   * Use {@link #EnumAnnotation} when you already know the possible values up front.
   * </p>
   * @param aMaxDifferentValues The number of different values that are considered an enum
   */
  public static <T> Builder<T> newBuilder(int aMaxDifferentValues) {
    return new Builder<T>(aMaxDifferentValues);
  }

  /**
   * Returns the number of different values.
   *
   * @return The number of different values.
   */
  public int size() {
    return fValues.size();
  }

  /**
   * Returns a number associated with the given value, or {@code -1} if the value does
   * not exist in the enum.
   *
   * @param aValue The value
   * @return A number associated with the given value
   * @see #get(int)
   */
  public int indexOf(T aValue) {
    return fValues.indexOf(aValue);
  }

  /**
   * Returns the enum value associated with the given index.
   *
   * @param aIndex The index
   * @return The enum value associated with the given index.
   * @see #indexOf(T)
   */
  public T get(int aIndex) {
    return fValues.get(aIndex);
  }

  /**
   * Builder/accumulator to determine if the data for a property is an enum.
   */
  public static class Builder<T> {
    private final Map<T, T> fValues = new HashMap<T, T>();
    private final int fMaxDifferentValues;

    private Builder(int aMaxDifferentValues) {
      fMaxDifferentValues = aMaxDifferentValues;
    }

    /**
     * Add a new possible value to the builder.
     * <p>
     * The return value is equal to the input value, but not necessarily the same instance.
     * This can be used as an "interned" value equivalent to the input.
     * </p>
     * @param aValue Add a new possible value to the builder
     * @return A value equal to the input value, already contained in this builder.
     */
    public T accumulate(T aValue) {
      if (fValues.size() <= fMaxDifferentValues) {
        if (fValues.containsKey(aValue)) {
          aValue = fValues.get(aValue);
        } else {
          fValues.put(aValue, aValue);
        }
      }
      return aValue;
    }

    /**
     * Creates a new {@link EnumAnnotation} if the accumulated values are less than the maximum
     * different values.
     * <p>
     * If not, returns {@code null}.
     * </p>
     *
     * @return A new enum annotation, or null if the data does not correspond to an enum.
     */
    public EnumAnnotation<T> build() {
      if (fValues.size() > fMaxDifferentValues) {
        return null;
      } else {
        return new EnumAnnotation<T>(fValues.values());
      }
    }
  }
}
