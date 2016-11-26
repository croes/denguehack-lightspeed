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

import java.awt.Component;
import java.util.EventObject;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * <p><code>TableCellRenderer</code> and <code>TableCellEditor</code> for the visibility column of a
 * table model decorated with a <code>ModelFilterTableModelDecorator</code>.</p>
 */
class ModelObjectFilterTableCellRendererEditor implements TableCellRenderer, TableCellEditor {

  private TableCellRenderer fDelegateRenderer;
  private TableCellEditor fDelegateEditor;

  private final CopyOnWriteArrayList<CellEditorListener> fListenerList = new CopyOnWriteArrayList<CellEditorListener>();

  public ModelObjectFilterTableCellRendererEditor() {
    JTable table = new JTable();
    fDelegateRenderer = new CheckboxTableCellRenderer();
    fDelegateEditor = table.getDefaultEditor(Boolean.class);
    fDelegateEditor.addCellEditorListener(new MyCellEditorListener());
  }

  @Override
  public Component getTableCellRendererComponent(JTable aTable,
                                                 Object aValue,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int aRow,
                                                 int aColumn) {
    Component tableCellRendererComponent = fDelegateRenderer.getTableCellRendererComponent(aTable, aValue, isSelected, hasFocus, aRow, aColumn);
    adjustComponent(tableCellRendererComponent);
    return tableCellRendererComponent;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    Component component = fDelegateEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
    adjustComponent(component);
    return component;
  }

  @Override
  public Object getCellEditorValue() {
    return fDelegateEditor.getCellEditorValue();
  }

  @Override
  public boolean isCellEditable(EventObject anEvent) {
    return fDelegateEditor.isCellEditable(anEvent);
  }

  @Override
  public boolean shouldSelectCell(EventObject anEvent) {
    return fDelegateEditor.shouldSelectCell(anEvent);
  }

  @Override
  public boolean stopCellEditing() {
    return fDelegateEditor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    fDelegateEditor.cancelCellEditing();
  }

  @Override
  public void addCellEditorListener(CellEditorListener l) {
    fListenerList.add(l);
  }

  @Override
  public void removeCellEditorListener(CellEditorListener l) {
    fListenerList.remove(l);
  }

  private void fireEditingStopped() {
    ChangeEvent event = new ChangeEvent(this);
    for (CellEditorListener l : fListenerList) {
      l.editingStopped(event);
    }
  }

  private void fireEditingCancelled() {
    ChangeEvent event = new ChangeEvent(this);
    for (CellEditorListener l : fListenerList) {
      l.editingCanceled(event);
    }
  }

  private void adjustComponent(Component aTableCellRendererComponent) {
    // Reset JCheckbox to its default empty text/tool tip.
    if (aTableCellRendererComponent instanceof JCheckBox) {
      ((JCheckBox) aTableCellRendererComponent).setText("");
      ((JCheckBox) aTableCellRendererComponent).setToolTipText(null);
    }
  }

  private class MyCellEditorListener implements CellEditorListener {

    @Override
    public void editingStopped(ChangeEvent e) {
      fireEditingStopped();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
      fireEditingCancelled();
    }
  }
}
