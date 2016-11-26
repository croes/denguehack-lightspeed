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
package samples.gxy.swingTable;

import java.awt.Rectangle;
import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdFitSelectionInViewClipAction;

/**
 * This class implements a layer selection listener <code>ILcdSelectionListener</code> and a
 * table selection listener <code>ListSelectionListener</code>. Use this class when you want
 * to connect a layer selection to a table selection. The layer must contain a model of type
 * <code>ILcdIntegerIndexedModel</code> and the corresponding table must hold the unsorted
 * list of the model elements. In this case, a selected row index can be used to retrieve the
 * corresponding model element, no conversion is needed.
 */
public class SelectionMediator implements ILcdSelectionListener, ListSelectionListener {

  private ILcdGXYLayer fGXYLayer;
  private JTable fTable;
  private ILcdGXYView fGXYView;

  public SelectionMediator(ILcdGXYLayer aGXYLayer,
                           ILcdGXYView aGXYView,
                           JTable aTable) {
    fGXYView = aGXYView;
    fGXYLayer = aGXYLayer;
    fTable = aTable;
  }

  // implementation of ILcdSelectionListener
  public void selectionChanged(TLcdSelectionChangedEvent aEvent) {
    ILcdIntegerIndexedModel layer_model = (ILcdIntegerIndexedModel) fGXYLayer.getModel();
    ListSelectionModel table_model = fTable.getSelectionModel();
    table_model.removeListSelectionListener(this);
    for (Enumeration e = aEvent.selectedElements(); e.hasMoreElements(); ) {
      int row = layer_model.indexOf(e.nextElement());
      table_model.addSelectionInterval(row, row);
    }
    for (Enumeration e = aEvent.deselectedElements(); e.hasMoreElements(); ) {
      int row = layer_model.indexOf(e.nextElement());
      table_model.removeSelectionInterval(row, row);
    }
    table_model.addListSelectionListener(this);

    // scroll to selected row
    if (table_model.getMinSelectionIndex() != -1) {
      Rectangle cell_rect = fTable.getCellRect(table_model.getMinSelectionIndex(), 0, true);
      if (cell_rect != null) {
        fTable.scrollRectToVisible(cell_rect);
      }
    }
  }

  // implementation of ListSelectionListener
  public void valueChanged(ListSelectionEvent aEvent) {
    if (!aEvent.getValueIsAdjusting()) {
      ILcdIntegerIndexedModel layer_model = (ILcdIntegerIndexedModel) fGXYLayer.getModel();
      fGXYLayer.removeSelectionListener(this);
      for (int index = aEvent.getFirstIndex(), last = aEvent.getLastIndex(); index <= last; index++) {
        Object object = layer_model.elementAt(index);
        fGXYLayer.selectObject(object, fTable.isRowSelected(index), ILcdFireEventMode.FIRE_LATER);
      }
      TLcdFitSelectionInViewClipAction fitSelectionAction = new TLcdFitSelectionInViewClipAction(fGXYView);
      fitSelectionAction.actionPerformed(null);

      fGXYLayer.fireCollectedSelectionChanges();
      fGXYLayer.addSelectionListener(this);
    }
  }
}
