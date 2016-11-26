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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.luciad.lucy.util.TLcyGenericComposite;

/**
 * A implementation of <code>ITableCellRendererProvider</code> according to the composite design pattern.</p>
 */
public final class CompositeTableCellRendererProvider extends TLcyGenericComposite<ITableCellRendererProvider>
    implements ITableCellRendererProvider {

  @Override
  public boolean canProvideRenderer(JTable aTable, int aRow, int aColumn) {
    return findRendererProvider(aTable, aRow, aColumn) != null;
  }

  /*
   * Goes over the list of providers until it finds one that can provide a renderer for the given cell.
   * Returns that renderer.
   */
  @Override
  public TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn) {
    ITableCellRendererProvider rendererProvider = findRendererProvider(aTable, aRow, aColumn);
    if (rendererProvider == null) {
      return null;
    }
    return rendererProvider.provideRenderer(aTable, aRow, aColumn);
  }

  private ITableCellRendererProvider findRendererProvider(JTable aTable, int aRow, int aColumn) {
    for (ITableCellRendererProvider rendererProvider : getList()) {
      if (rendererProvider.canProvideRenderer(aTable, aRow, aColumn)) {
        return rendererProvider;
      }
    }
    return null;
  }
}
