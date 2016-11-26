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
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.dataproperty.ALcyDataPropertyCustomizerPanel;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdDataPropertyValueContext;

/**
 * This adapter converts a <code>ILcyCustomizerPanel</code> to a <code>TableCellEditor</code>,
 * thereby enabling the editing of data property values in the Table View.
 */
public class TableEditorCustomizerPanelAdapter extends AbstractCellEditor implements TableCellEditor,
                                                                                     ILcdDisposable {

  private final ILcyCustomizerPanel fDelegate;
  private ILcdLayer fLayer;
  private EditableStateListener fLayerListener;

  public TableEditorCustomizerPanelAdapter(ILcyCustomizerPanel aCustomizerPanel,
                                           ILcdLayer aLayer,
                                           TLcdDataProperty aDataProperty) {
    fDelegate = aCustomizerPanel;
    if (fDelegate instanceof Container) {
      comboBoxWorkAround((Container) fDelegate);

    }
    fDelegate.addPropertyChangeListener(new ImmediateApplyListener());
    if (aLayer != null) {
      fLayer = aLayer;
      fLayer.addPropertyChangeListener(fLayerListener = new EditableStateListener(this));
    }
    if (aCustomizerPanel instanceof ALcyDataPropertyCustomizerPanel) {
      handleAlignment((ALcyDataPropertyCustomizerPanel) aCustomizerPanel, aDataProperty);
    }
  }

  protected void handleAlignment(ALcyDataPropertyCustomizerPanel aCustomizerPanel, TLcdDataProperty aDataProperty) {
    //Numbers should be right aligned.
    if (NumberPropertyUtil.isNumberClass(aDataProperty.getType().getInstanceClass())) {
      aCustomizerPanel.putValue(ILcyCustomizerPanel.HORIZONTAL_ALIGNMENT_HINT, SwingConstants.RIGHT);
    }
  }

  /**
   * When a combo box is used as an editor in a table, a property is usually set changes some behaviour,
   * especially the behaviour of keys when the combo box has the focus. We apply this workaround,
   * because we have combo boxes contained within a customizer panel.
   */
  private void comboBoxWorkAround(Container aContainer) {
    if (aContainer instanceof JComboBox) {
      ((JComboBox) aContainer).putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
      return;
    }
    for (Component c : aContainer.getComponents()) {
      if (c instanceof Container) {
        comboBoxWorkAround((Container) c);
      }
    }
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    return (Component) fDelegate;
  }

  @Override
  public Object getCellEditorValue() {
    //The editor is active, see cleanUp, the customizer will never have a null object, since it is created
    //for a specific TLcdDataPropertyValueContext.
    TLcdDataPropertyValueContext propertyValue = (TLcdDataPropertyValueContext) fDelegate.getObject();
    return propertyValue.getValue();
  }

  @Override
  public boolean stopCellEditing() {
    if (fDelegate.isChangesValid()) {
      fDelegate.applyChanges();
    } else {
      fDelegate.cancelChanges();
    }
    clearListener();
    return super.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    fDelegate.cancelChanges();
    super.cancelCellEditing();
    clearListener();
  }

  private void clearListener() {
    if (fLayer != null) {
      fLayer.removePropertyChangeListener(fLayerListener);
    }
  }

  /*
   * This is called when the editor is removed from the table. It allows the customizer panel to
   * perform clean up operations, like removing listeners etc...
   */
  @Override
  public void dispose() {
    fDelegate.setObject(null);
  }

  /**
   * This listener applies changes as soon as the customizer panels has them.
   */
  private class ImmediateApplyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("changesPending".equals(evt.getPropertyName())) {
        if ((Boolean) evt.getNewValue()) {
          stopCellEditing();
        }
      }
    }
  }

  private static class EditableStateListener extends ALcdWeakPropertyChangeListener<TableEditorCustomizerPanelAdapter> {

    public EditableStateListener(TableEditorCustomizerPanelAdapter aAdapter) {
      super(aAdapter);
    }

    @Override
    protected void propertyChangeImpl(TableEditorCustomizerPanelAdapter aAdapter, PropertyChangeEvent aPropertyChangeEvent) {
      if ("editable".equals(aPropertyChangeEvent.getPropertyName())) {
        if (!(Boolean) aPropertyChangeEvent.getNewValue()) {
          aAdapter.stopCellEditing();
        }
      }
    }
  }
}
