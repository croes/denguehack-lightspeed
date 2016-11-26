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
package samples.common.dimensionalfilter.model;

import java.util.Collection;

import com.luciad.model.ILcdModel;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * <p>Implementations of this class filter a layer based on a {@linkplain #setFilterValue(Comparable) specific parameter
 * value} that is chosen from a {@linkplain #getPossibleValues() list of possible values}. An example of this could
 * be to filter the contents of a model based on a chosen {@code Date}.</p>
 *
 * <p>Implementations of this class are created by a format specific {@link DimensionalFilterProvider FilterProvider}. The
 * filter provider specifies how to filter by returning a collection of {@code Filter} instances. The
 * {@link DimensionalFilterManager FilterManager} class is initialized with such a filter provider. When a layer is registered
 * on a {@code FilterManager}, the filter provider returns a collection of {@code Filter} instances for that layer,
 * and groups them in a {@link DimensionalFilterGroup FilterGroup}. This allows to combine similar filters (for example with
 * the same name and type), making it possible to filter multiple layers at the same time.</p>
 *
 * @since 2015.0
 * @see DimensionalFilterManager
 */
public abstract class DimensionalFilter {

  private final ILcdLayered fLayered;
  private final ILcdLayer fLayer;
  private final Class<? extends Comparable> fType;
  private final Collection<? extends Comparable> fPossibleValues;
  private final ILcdISO19103UnitOfMeasure fUnit;
  private final boolean fPositive;

  /**
   * Creates a new filter.
   * @param aLayered the layered (view) for which this filter can be applied
   * @param aLayer the layer for which this filter can be applied
   * @param aType the type of the possible values
   * @param aUnit the unit of measure of this filter
   * @param aPositive determines if the filter type is an increasing (height, altitude etc.) or decreasing one (depth etc.)
   * @param aPossibleValues the possible values.
   */
  protected DimensionalFilter(ILcdLayered aLayered,
                              ILcdLayer aLayer,
                              Class<? extends Comparable> aType,
                              ILcdISO19103UnitOfMeasure aUnit,
                              boolean aPositive,
                              Collection<? extends Comparable> aPossibleValues) {
    fLayered = aLayered;
    fLayer = aLayer;
    fType = aType;
    fUnit = aUnit;
    fPositive = aPositive;
    fPossibleValues = aPossibleValues;
  }

  /**
   * Returns the layered this filter is applied on.
   *
   * @return the layered this filter is applied on.
   */
  public final ILcdLayered getLayered() {
    return fLayered;
  }

  /**
   * Returns the target layer.
   *
   * @return the target layer.
   */
  public final ILcdLayer getLayer() {
    return fLayer;
  }

  /**
   * Returns the type used by the target object.
   * @return the type used by the target object.
   */
  public final Class<? extends Comparable> getType() {
    return fType;
  }

  /**
   * Returns the unit of measure used by the target object.
   *
   * @return the unit of measure used by the target object.
   */
  public final ILcdISO19103UnitOfMeasure getUnit() {
    return fUnit;
  }

  /**
   * Returns the increasing/decreasing type of the filter
   * @return Returns the increasing/decreasing type of the filter
   */
  public boolean isPositive() {
    return fPositive;
  }

  /**
   * Returns the possible values used for filtering.
   * @return the possible values used for filtering.
   */
  public final Collection<? extends Comparable> getPossibleValues() {
    return fPossibleValues;
  }

  /**
   * Sets the filter for the new value
   * @param aValue filter value
   */
  public abstract void setFilterValue(Comparable aValue);

  /**
   * Returns the name used by the target object. In order for a filter to be compatible with a
   * {@link DimensionalFilterGroup filter model}, its name should be equal.
   * @return the name used by the target object.
   */
  public abstract String getName();

  /**
   * Returns true if this dimensional filter can extract the
   * current filter value of the given model
   * @param aModel model to be tested
   * @return true if current filter value can be extracted from the model, false otherwise
   */
  public abstract boolean canGetFilterValueFromModel(ILcdModel aModel);

  /**
   * returns the current filter value of the given model
   * @param aModel model whose current filter value is returned
   * @return current filter value of the given model
   * @throws IllegalArgumentException if the filter value can not be extracted from the model ({@link DimensionalFilter#canGetFilterValueFromModel(ILcdModel)} returns false).
   */
  public abstract Comparable getFilterValueFromModel(ILcdModel aModel);

}
