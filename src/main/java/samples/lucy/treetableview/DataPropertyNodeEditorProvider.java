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
package samples.lucy.treetableview;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTreeTable;

import samples.lucy.tableview.ITableCellEditorProvider;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.view.ILcdLayer;

/**
 * Implementation of <code>ITableCellEditorProvider</code> that can create editors for value cells
 * corresponding to a data property node with, provided that a customizer panel can be
 * created by the lucy composite customizer panel factory.
 */
class DataPropertyNodeEditorProvider implements ITableCellEditorProvider {

  private final ILcyLucyEnv fLucyEnv;

  public DataPropertyNodeEditorProvider(ILcyLucyEnv aLucyEnv) {
    if (aLucyEnv == null) {
      throw new IllegalArgumentException("The Lucy environment can't be null!");
    }
    fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean canProvideEditor(JTable aTable, int aRow, int aColumn) {
    if (aColumn != 1 ||
        !(aTable instanceof JXTreeTable) ||
        !(((JXTreeTable) aTable).getTreeTableModel() instanceof TreeTableDataObjectModel)) {
      return false;
    }
    JXTreeTable table = (JXTreeTable) aTable;
    TreeTableDataObjectModel model =
        (TreeTableDataObjectModel) ((JXTreeTable) aTable).getTreeTableModel();
    Object node = table.getPathForRow(aRow).getLastPathComponent();
    return CustomizerUtility.canCreateCustomizerPanel(model.getDomainObjectContext(),
                                                      model.getDataProperties(node),
                                                      fLucyEnv);
  }

  @Override
  public TableCellEditor provideEditor(JTable aTable, int aRow, int aColumn) {
    if (!canProvideEditor(aTable, aRow, aColumn)) {
      throw new IllegalArgumentException("Can't provide an editor, call canProvideEditor first!");
    }
    JXTreeTable table = (JXTreeTable) aTable;
    TreeTableDataObjectModel model =
        (TreeTableDataObjectModel) ((JXTreeTable) aTable).getTreeTableModel();
    Object node = table.getPathForRow(aRow).getLastPathComponent();
    ILcyCustomizerPanel customizerPanel = CustomizerUtility.createCustomizerPanel(
        model.getDomainObjectContext(),
        model.getDataProperties(node),
        fLucyEnv);
    ILcdLayer layer = model.getDomainObjectContext().getLayer();
    return new TreeTableEditorCustomizerPanelAdapter(customizerPanel, layer,
                                                     model.getDataProperties(node)[model.getDataProperties(node).length - 1]);
  }
}
