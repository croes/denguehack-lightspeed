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

import java.beans.PropertyChangeListener;

import javax.swing.table.TableModel;

/**
 * <p>An extension of {@link TableModel} which allows to use a modifiable
 * <code>TableModel</code> inside an <code>ILcyCustomizerPanel</code>.</p>
 *
 * <p>When modifying a regular <code>TableModel</code> (e.g. through a <code>CellEditor</code> set
 * on a <code>JTable</code>) the <code>TableModel</code> is immediately adjusted. On an
 * <code>ILcyCustomizerPanel</code>, making a change will not immediately change the {@link
 * com.luciad.lucy.gui.customizer.ILcyCustomizerPanel#getObject() object} of the customizer panel,
 * but {@link com.luciad.lucy.gui.customizer.ILcyCustomizerPanel#isChangesPending() indicate} that
 * changes are made.</p>
 *
 * <p>This interface allows to combine both behaviours. When an object <code>A</code> is set on an
 * <code>ILcyCustomizerPanel</code>, and the UI of that panel contains a <code>TableModel</code>
 * representing (a part of) <code>A</code>, changes made to the <code>TableModel</code> should not
 * update <code>A</code> but fire a <code>PropertyChangeEvent</code> for their "changesPending"
 * property. It is then up to the <code>ILcyCustomizerPanel</code> implementation to call {@link
 * #applyChangesOnObject()} in its {@link com.luciad.lucy.gui.customizer.ILcyCustomizerPanel#applyChanges()}
 * method.</p>
 */
public interface CustomizerPanelTableModel extends TableModel {
  /**
   * Updates the <code>Object</code> represented by this <code>TableModel</code> according to the
   * current state of the user interface.  This means all pending changes are committed.
   *
   * @return <code>true</code> if the changes were applied or if there were no changes,
   *         <code>false</code> if the changes could not be applied.
   */
  boolean applyChangesOnObject();

  /**
   * Returns <code>true</code> if changes are pending.  A change is a modification made by the user,
   * e.g., the modified content of a text field. Pending means the method <code>applyChanges</code>
   * was not invoked after the change occurred. <p/> A property change event
   * <code>"changesPending"</code> must be fired if the return value of this method is changed. This
   * can for example be used to enable/disable an apply button.
   *
   * @return <code>true</code> if there are pending changes, <code>false</code> otherwise.
   */
  boolean isChangesPending();

  /**
   * <p>Adds the given listener to the list of listeners, so that it will receive property change
   * events.</p>
   *
   * @param aListener The listener to add.
   *
   * @see #removePropertyChangeListener(PropertyChangeListener)
   */
  void addPropertyChangeListener(PropertyChangeListener aListener);

  /**
   * Removes the given listener so that it no longer receives property change events.
   *
   * @param aListener The listener to remove.
   *
   * @see #addPropertyChangeListener(PropertyChangeListener)
   */
  void removePropertyChangeListener(PropertyChangeListener aListener);

  /**
   * Sets the value at <code>aRowIndex</code> and <code>aColumnIndex</code> to
   * <code>aNewValue</code>, and allows to indicate more changes will follow immediately afterwards.
   * This allows to perform multiple changes before firing a {@link javax.swing.event.TableModelEvent},
   * and combine all those changes in one {@link javax.swing.event.TableModelEvent}
   *
   * @param aNewValue    The new value
   * @param aRowIndex    The row index
   * @param aColumnIndex The column index
   * @param aIsAdjusting <code>true</code> when more changes will follow immediately,
   *                     <code>false</code> when no more changes are expected or when the last of a
   *                     set of changes is done. The model should only fire an event when set to
   *                     <code>false</code>
   */
  void setValueAt(Object aNewValue, int aRowIndex, int aColumnIndex, boolean aIsAdjusting);

}
