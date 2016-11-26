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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModel;

/**
 * Default implementation. Can create a renderer for any cell. The ultimate fallback is
 * Object.toString().
 */
class DefaultTableCellRendererProvider implements ITableCellRendererProvider {

  private final TableCellRenderer fStringRenderer;
  private final TableCellRenderer fCheckBoxRenderer;

  public DefaultTableCellRendererProvider(IExtendedTableModel aExtendedTableModel, ILcyLucyEnv aLucyEnv) {
    ILcdModel model = aExtendedTableModel.getOriginalModel();
    fStringRenderer = new DefaultStringRenderer(aLucyEnv, model);
    fCheckBoxRenderer = new CheckboxTableCellRenderer();
  }

  @Override
  public boolean canProvideRenderer(JTable aTable, int aRow, int aColumn) {
    return true;
  }

  @Override
  public TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn) {
    if (Boolean.class.isAssignableFrom(aTable.getColumnClass(aColumn))) {
      // show a checkbox for booleans iso the String representation of the boolean
      return fCheckBoxRenderer;
    }
    return fStringRenderer;
  }

}
