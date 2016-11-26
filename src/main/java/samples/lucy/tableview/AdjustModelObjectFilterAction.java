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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.luciad.gui.ILcdAction;
import samples.common.MetaKeyUtil;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Action that toggles the display value of the selected rows, that is, the checkboxes
 * are toggled.
 */
class AdjustModelObjectFilterAction extends AbstractAction implements ILcdAction {
  private final JXTable fTable;

  public AdjustModelObjectFilterAction(JXTable aTable) {
    super(TLcyLang.getString("Toggle visibility"));
    fTable = aTable;
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, MetaKeyUtil.getCMDDownMask());
    putValue(Action.ACCELERATOR_KEY, keyStroke);
    fTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateEnabledState();
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //make sure to use the table here and not the table model, as the table could have
    //applied sorting. When that is the case, a row index in the table does not correspond to the
    //row index in the table model.
    int[] selected_rows = fTable.getSelectedRows();
    int display_col_index = ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX;
    if (selected_rows.length > 0 &&
        fTable.getColumnCount() > display_col_index &&
        fTable.getColumnClass(display_col_index) == Boolean.class &&
        fTable.getRowCount() > 0) {

      Boolean towards = Boolean.FALSE;
      for (int selected_row : selected_rows) {
        if (Boolean.FALSE.equals(fTable.getValueAt(selected_row, display_col_index))) {
          towards = Boolean.TRUE;
          break;
        }
      }
      TableModel tableModel = fTable.getModel();
      for (int i = 0, selected_rowsLength = selected_rows.length; i < selected_rowsLength; i++) {
        int selected_row = fTable.convertRowIndexToModel(selected_rows[i]);

        if (tableModel instanceof CustomizerPanelTableModel) {
          ((CustomizerPanelTableModel) tableModel).setValueAt(towards, selected_row, display_col_index, i != selected_rowsLength - 1);
        } else {
          tableModel.setValueAt(towards, selected_row, display_col_index);
        }
      }
    }
  }

  private void updateEnabledState() {
    //the enable state depends on the state of the checkboxint[] selected_rows = fTable.getSelectedRows();
    int[] selected_rows = fTable.getSelectedRows();
    int display_col_index = ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX;
    if (selected_rows.length > 0 &&
        fTable.getColumnCount() > display_col_index &&
        fTable.getColumnClass(display_col_index) == Boolean.class &&
        fTable.getRowCount() > 0) {
      boolean enabled = false;
      for (int i = 0; i < selected_rows.length && !enabled; i++) {
        int selected_row = selected_rows[i];
        if (fTable.isCellEditable(selected_row, display_col_index)) {
          enabled = true;
        }
      }
      setEnabled(enabled);
    }
  }
}
