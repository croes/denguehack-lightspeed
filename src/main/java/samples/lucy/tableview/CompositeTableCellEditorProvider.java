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
import javax.swing.table.TableCellEditor;

import com.luciad.lucy.util.TLcyGenericComposite;

/**
 * A implementation of <code>ITableCellEditorProvider</code> according to the composite design pattern.</p>
 */
public final class CompositeTableCellEditorProvider extends TLcyGenericComposite<ITableCellEditorProvider>
    implements ITableCellEditorProvider {

  /*
   * This method returns true if one of the registered editor providers can provide an editor for
   * specified cell.
   */
  @Override
  public boolean canProvideEditor(JTable aTable, int aRow, int aColumn) {
    return findEditorProvider(aTable, aRow, aColumn) != null;
  }

  /*
   * Goes over the list of providers until it finds one that can provide a editor for the given cell.
   * Returns that editor.
   */
  @Override
  public TableCellEditor provideEditor(JTable aTable, int aRow, int aColumn) {
    ITableCellEditorProvider editorProvider = findEditorProvider(aTable, aRow, aColumn);
    if (editorProvider == null) {
      return null;
    }
    return editorProvider.provideEditor(aTable, aRow, aColumn);
  }

  private ITableCellEditorProvider findEditorProvider(JTable aTable, int aRow, int aColumn) {
    for (ITableCellEditorProvider editorProvider : getList()) {
      if (editorProvider.canProvideEditor(aTable, aRow, aColumn)) {
        return editorProvider;
      }
    }
    return null;
  }
}
