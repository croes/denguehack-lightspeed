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
import javax.swing.SwingConstants;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;

import com.luciad.datamodel.TLcdDataProperty;
import samples.lucy.tableview.AbstractMeasureAnnotationCellRendererProvider;
import com.luciad.lucy.ILcyLucyEnv;

/**
 * Implementation of <code>AbstractMeasureAnnotationCellRendererProvider</code> for the table view.
 */
class TreeTableMeasureAnnotationCellRendererProvider extends AbstractMeasureAnnotationCellRendererProvider {

  public TreeTableMeasureAnnotationCellRendererProvider(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv, SwingConstants.LEFT);
  }

  @Override
  protected TLcdDataProperty getDataProperty(JTable aTable, int aRow, int aColumn) {
    if (aTable instanceof JXTreeTable &&
        ((JXTreeTable) aTable).getTreeTableModel() instanceof TreeTableDataObjectModel) {
      JXTreeTable treeTable = (JXTreeTable) aTable;
      TreeTableDataObjectModel tableModel = (TreeTableDataObjectModel) treeTable.getTreeTableModel();
      TreePath path = treeTable.getPathForRow(aRow);
      Object node = path.getLastPathComponent();
      TLcdDataProperty[] properties = tableModel.getDataProperties(node);
      //properties can be null for collection types / those can't be edited anyway.
      if (properties != null) {
        return properties[properties.length - 1];
      }
    }
    return null;
  }
}
