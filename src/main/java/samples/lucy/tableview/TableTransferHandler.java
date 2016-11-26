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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

/**
 * <p><code>TransferHandler</code> for <code>JTables</code> that uses the String values as provided by the
 * renderer of the table. This provides better results when e.g. copy-pasting entries from the table to
 * another application.</p>
 *
 * <p>It creates a new table in the background with only String objects, and returns the
 * transferable of that table.</p>
 */
public class TableTransferHandler extends TransferHandler {

  private JXTable fTable;

  public TableTransferHandler(JXTable aTable) {
    fTable = aTable;
  }

  @Override
  public void exportToClipboard(JComponent aComponent, Clipboard aClipboard, int aAction) throws IllegalStateException {
    JTable table = createTable();
    table.getTransferHandler().exportToClipboard(table, aClipboard, aAction);
    cleanUpTable(table);
  }

  @Override
  public void exportAsDrag(JComponent aComponent, InputEvent aEvent, int aAction) {
    JTable table = createTable();
    table.getTransferHandler().exportAsDrag(table, aEvent, aAction);
    cleanUpTable(table);
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    //this transfer handler should not create any transferables
    return null;
  }

  /**
   * Create a table, representing <code>fTable</code>, containing only Strings
   *
   * @return a table containing only Strings
   */
  private JTable createTable() {
    JTable table = new JTable(new StringTableModel(fTable));
    table.setSelectionModel(fTable.getSelectionModel());
    return table;
  }

  /**
   * When finished with the created table, this method should be called. This makes sure the
   * original table and model do not keep references to the temporary table we created. By replacing
   * the model and selection model, the table removes it listeners from the old model and selection model
   * @param aTable The created table
   */
  private void cleanUpTable(JTable aTable) {
    aTable.setModel(new DefaultTableModel());
    aTable.setSelectionModel(new DefaultListSelectionModel());
  }

  /**
   * Table model based on the current view state of the <code>JXTable</code>
   */
  private static class StringTableModel extends AbstractTableModel {
    private JXTable fDelegateTable;

    private StringTableModel(JXTable aTable) {
      fDelegateTable = aTable;
    }

    @Override
    public int getRowCount() {
      return fDelegateTable.getRowCount();
    }

    @Override
    public int getColumnCount() {
      return fDelegateTable.getColumnCount();
    }

    @Override
    public Object getValueAt(int aRowIndex, int aColumnIndex) {
      return fDelegateTable.getStringAt(aRowIndex, aColumnIndex);
    }
  }
}
