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

import java.util.List;

import com.luciad.util.ILcdPropertyChangeSource;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * <p>A dimensional filter provider can provide {@link DimensionalFilter DimensionalFilter} instances for a given layer. Applications that want to provide
 * data filtering or {@link com.luciad.multidimensional.ILcdMultiDimensional multidimensional} model filtering for their
 * layers have to implement instance(s) of this interface and make them available to the application, for example as an
 * {@link com.luciad.util.service.LcdService LcdService}.</p>
 *
 * <p>Implementations will return 0 or more {@link DimensionalFilter DimensionalFilter} instances for supported layers
 * (see {@link #canHandleLayer canHandleLayer}).</p>
 *
 * @since 2015.0
 * @see DimensionalFilterManager
 */
public interface DimensionalFilterProvider extends ILcdPropertyChangeSource {

  /**
   * Event code which should make the target DimensionalFilterManager to swap the
   * old filters with the new filters provided in PropertyChangeEvent
   */
  String UPDATE_FILTERS = "updateFilters";

  /**
   * Checks whether the given layer is supported by this factory.
   *
   * @param aLayer the layer for which a filter can be provider
   * @param aLayered the view for which this filter should apply
   * @return <code>true</code> if this factory can create filters for the given context's layer, <code>false</code> otherwise.
   */
  boolean canHandleLayer(ILcdLayer aLayer, ILcdLayered aLayered);

  /**
   * Creates the filters for the given context's layer. A single layer may be filtered in more than one dimension.
   *
   * @param aLayer the layer for which a filter can be provided
   * @param aLayered the view for which this filter should apply
   *
   * @return the list of filters.
   */
  List<DimensionalFilter> createFilters(ILcdLayer aLayer, ILcdLayered aLayered);
}
