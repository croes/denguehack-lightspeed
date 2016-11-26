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

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import com.luciad.lucy.util.TLcyCompositeAndFilter;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.util.ILcdFilter;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A row filter that filters rows based on whether the corresponding domain object is filtered in
 * the layer for which the table view was created.
 */
class LayerRowFilter extends RowFilter<TableModel, Integer> {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(TableViewGUIFactory.class);

  private final ITableViewLogic fTableViewLogic;
  private final TLcyModelContext fModelContext;

  public LayerRowFilter(ITableViewLogic aTableViewLogic, TLcyModelContext aModelContext) {
    fTableViewLogic = aTableViewLogic;
    fModelContext = aModelContext;
  }

  @Override
  public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
    TableModel tableModel = entry.getModel();
    if (tableModel instanceof IExtendedTableModel) {
      Object object = ((IExtendedTableModel) tableModel).getObjectAtRow(entry.getIdentifier());
      TLcyCompositeAndFilter layerFilter = retrieveLayerFilter();
      if (layerFilter != null) {
        return accept(layerFilter, object);
      }
    }
    return true;
  }

  private boolean accept(TLcyCompositeAndFilter aLayerFilter, Object aObject) {
    int count = 0;
    for (int i = 0; i < aLayerFilter.getFilterCount(); i++) {
      ILcdFilter filter = aLayerFilter.getFilter(i);
      if (filter instanceof TLcyModelObjectFilter) {
        count++;
        if (count > 1) {
          sLogger.warn("Two TLcyModelObjectFilters detected in the root TLcyCompositeAndFilter node. " +
                       "This is not a good idea!");
        }
      } else if (!filter.accept(aObject)) {
        return false;
      }
    }
    return true;
  }

  private TLcyCompositeAndFilter retrieveLayerFilter() {
    ILcdFilter filter = fTableViewLogic.retrieveLayerFilter(fModelContext.getLayer());
    if (filter instanceof TLcyCompositeAndFilter) {
      return (TLcyCompositeAndFilter) filter;
    } else {
      return null;
    }
  }

}
