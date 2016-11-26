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
package samples.lucy.tableview;

import com.luciad.lucy.util.TLcyCompositeAndFilter;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.util.ILcdFilter;

/**
 * Some utility code that checks whether or not a TLcyModelObjectFilter still has to be added or
 * is already present.
 *
 * This functionality controls whether or not a TLcyModelObjectFilter already present in the layer
 * is used by the visibility column in the table. If it is the filter of the layer, or if the filter
 * is a TLcyCompositeAndFilter containing a TLcyModelObjectFilter, it is used. Otherwise a new
 * TLcyModelObjectFilter is added to interact with the visibility functionality of the table.
 */
public final class TableViewFilterUtil {

  private TableViewFilterUtil() {
  }

  /**
   * This method will return a valid TLcyModelObjectFilter if either the passed filter is a
   * TLcyModelObjectFilter or the passed filter is an TLcyCompositeAndFilter that contains a
   * TLcyModelObjectFilter (not recursive).
   *
   * @param aFilter the filter to check.
   * @param aModel the model for which a {@code TLcyModelObjectFilter} is searched
   *
   * @return a TLcyModelObjectFilter if present in the right place, or null otherwise.
   */
  public static TLcyModelObjectFilter findInstalledFilter(ILcdFilter<?> aFilter, ILcdModel aModel) {
    if (aFilter instanceof TLcyModelObjectFilter && ((TLcyModelObjectFilter) aFilter).getModel() == aModel) {
      return (TLcyModelObjectFilter) aFilter;
    } else if (aFilter instanceof TLcyCompositeAndFilter) {
      TLcyCompositeAndFilter<?> compositeFilter = (TLcyCompositeAndFilter<?>) aFilter;
      int count = compositeFilter.getFilterCount();
      for (int i = 0; i < count; i++) {
        ILcdFilter filter = compositeFilter.getFilter(i);
        if (filter instanceof TLcyModelObjectFilter && ((TLcyModelObjectFilter) filter).getModel() == aModel) {
          return (TLcyModelObjectFilter) filter;
        }
      }
    }
    return null;
  }

  /**
   * This method returns a filter to be used as the layer filter. The returned filter is a
   * combination of the two passed filters as follows:
   * <ul>
   *   <li>a new {@code TLcyCompositeAndFilter} containing only {@code aFilterToAdd} (when {@code aCurrentLayerFilter == null})</li>
   *   <li>a new {@code TLcyCompositeAndFilter} containing {@code aFilterToAdd} and {@code aCurrentLayerFilter} when
   *   the current layer filter != {@code null} and not an instance of {@code TLcyCompositeAndFilter}</li>
   *   <li>{@code aCurrentLayerFilter} when that is already an instance of {@code TLcyCompositeAndFilter}. The
   *   {@code aFilterToAdd} will be added to that composite instance.</li>
   * </ul>
   *
   * @param aCurrentLayerFilter the current filter on the layer
   * @param aFilterToAdd the filter to add to the layer, possible the same instace as {@code aCurrentLayerFilter}
   *
   * @return a filter to set on the layer, never {@code null}
   */
  public static ILcdDynamicFilter combineFilters(ILcdFilter aCurrentLayerFilter,
                                                 ILcdFilter aFilterToAdd) {
    if (aCurrentLayerFilter == null) {
      TLcyCompositeAndFilter compositeAndFilter = new TLcyCompositeAndFilter();
      compositeAndFilter.addFilter(aFilterToAdd);
      return compositeAndFilter;
    }
    if (aCurrentLayerFilter instanceof TLcyCompositeAndFilter) {
      ((TLcyCompositeAndFilter) aCurrentLayerFilter).addFilter(aFilterToAdd);
      return ((TLcyCompositeAndFilter) aCurrentLayerFilter);
    } else {
      TLcyCompositeAndFilter compositeAndFilter = new TLcyCompositeAndFilter();
      compositeAndFilter.addFilter(aCurrentLayerFilter);
      compositeAndFilter.addFilter(aFilterToAdd);
      return compositeAndFilter;
    }
  }

}
