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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import com.luciad.lucy.gui.ILcyPopupMenu;

/**
 * Listener responsible for showing the popup menu and changing the selection if necessary 
 */
class TableViewPopupMenuListener extends MouseAdapter {
  private JTable fTable;
  private ILcyPopupMenu fPopupMenu;

  public TableViewPopupMenuListener(JTable aTable, ILcyPopupMenu aPopupMenu) {
    fTable = aTable;
    fPopupMenu = aPopupMenu;
  }

  private void showPopup(MouseEvent aMouseEvent) {
    if (aMouseEvent.isPopupTrigger()) {
      //change the selection when the selection is not touched
      Point point = new Point(aMouseEvent.getPoint());
      int row = fTable.rowAtPoint(point);
      if (!fTable.isRowSelected(row)) {
        int column = fTable.columnAtPoint(point);
        fTable.changeSelection(row, column, false, false);
      }
      //show the popup menu
      if (fPopupMenu != null) {
        fPopupMenu.show(aMouseEvent.getComponent(),
                        aMouseEvent.getX(),
                        aMouseEvent.getY());
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent aMouseEvent) {
    showPopup(aMouseEvent);
  }

  @Override
  public void mouseReleased(MouseEvent aMouseEvent) {
    showPopup(aMouseEvent);
  }
}
