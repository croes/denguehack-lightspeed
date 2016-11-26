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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Action which shows the number of rows in a table.
 */
final class ShowRowCountAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  private final JTable fTable;

  ShowRowCountAction(JTable aTable) {
    fTable = aTable;
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    if (aActionBar instanceof ILcyToolBar) {
      JLabel label = new JLabel(createLabelText(fTable.getRowCount()));
      updateLabelStyle(label);
      WeakLabelUpdateListener listener = new WeakLabelUpdateListener(label, fTable);
      fTable.addPropertyChangeListener(listener);
      fTable.getModel().addTableModelListener(listener);
      return label;
    }
    return aDefaultComponent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //retrieve the parent frame to show a dialog
    Frame parentFrame = TLcdAWTUtil.findParentFrame(e);
    JOptionPane.showInputDialog(parentFrame,
                                createLabelText(fTable.getRowCount()),
                                TLcyLang.getString("Row count"),
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                new String[]{TLcyLang.getString("Close")},
                                TLcyLang.getString("Close"));
  }

  static String createLabelText(int aRowCount) {
    return TLcyLang.getString("Row count") + ": " + aRowCount;
  }

  private static void updateLabelStyle(JLabel aLabelSFCT) {
    Font original = aLabelSFCT.getFont();
    Font adjusted = original.deriveFont(Font.PLAIN, original.getSize() - 1);
    aLabelSFCT.setFont(adjusted);
  }

  private static class WeakLabelUpdateListener implements TableModelListener, PropertyChangeListener {
    private WeakReference<JLabel> fLabel;
    private final JTable fTable;

    private WeakLabelUpdateListener(JLabel aLabel, JTable aTable) {
      fTable = aTable;
      fLabel = new WeakReference<JLabel>(aLabel);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      final JTable table = (JTable) evt.getSource();
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          boolean updated = updateLabel();
          if (!updated) {
            table.removePropertyChangeListener(WeakLabelUpdateListener.this);
          }
        }
      });
    }

    @Override
    public void tableChanged(TableModelEvent e) {
      final TableModel tableModel = (TableModel) e.getSource();
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          boolean updated = updateLabel();
          if (!updated) {
            tableModel.removeTableModelListener(WeakLabelUpdateListener.this);
          }
        }
      });
    }

    private boolean updateLabel() {
      JLabel label = fLabel.get();
      if (label != null) {
        label.setText(createLabelText(fTable.getRowCount()));
        return true;
      }
      return false;
    }
  }
}
