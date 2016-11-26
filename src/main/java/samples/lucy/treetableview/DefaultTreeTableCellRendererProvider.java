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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import samples.lucy.tableview.DefaultStringRenderer;
import samples.lucy.tableview.ITableCellRendererProvider;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModel;

/**
 * Default implementation. Can create a renderer for any cell in the not hierarchical column of the
 * TreeTable.
 */

class DefaultTreeTableCellRendererProvider implements ITableCellRendererProvider {

  private final TableCellRenderer fRenderer;

  private final LeftAlignedBooleanRenderer fBooleanRenderer = new LeftAlignedBooleanRenderer();
  private final InvisibleRenderer fInvisibleRenderer = new InvisibleRenderer();
  private final RecursionRenderer fRecursionRenderer = new RecursionRenderer();

  public DefaultTreeTableCellRendererProvider(ILcdModel aModel,
                                              ILcyLucyEnv aLucyEnv) {
    fRenderer = new LeftAlignedTableRenderer(aLucyEnv, aModel);
  }

  @Override
  public boolean canProvideRenderer(JTable aTable, int aRow, int aColumn) {
    return aTable instanceof JXTreeTable &&
           aTable.getColumnCount() == 2 &&
           !((JXTreeTable) aTable).isHierarchical(aColumn);
  }

  @Override
  public TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn) {
    if (!canProvideRenderer(aTable, aRow, aColumn)) {
      throw new IllegalArgumentException("Can't provide a renderer, call canProvideRenderer first!");
    }
    JXTreeTable treeTable = (JXTreeTable) aTable;
    TreePath path = treeTable.getPathForRow(aRow);
    Object node = path.getLastPathComponent();
    if (node instanceof TreeTableDataObjectMutableNode) {
      TreeTableDataObjectMutableNode mutableNode = (TreeTableDataObjectMutableNode) node;
      TreeTableDataObjectMutableNode.Render render = mutableNode.getRenderProperty();
      if (render == TreeTableDataObjectMutableNode.Render.Invisible) {
        return fInvisibleRenderer;
      } else if (render == TreeTableDataObjectMutableNode.Render.Recursive) {
        return fRecursionRenderer;
      } else if (Boolean.class.isAssignableFrom(mutableNode.getObjectClass())) {
        //special case for booleans => checkbox.
        return fBooleanRenderer;
      }
    }
    //Fallback (resorts to Object.toString() if necessary).
    return fRenderer;
  }

  private static class InvisibleRenderer extends DefaultTableRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
    }
  }

  private class RecursionRenderer extends DefaultTableRenderer {

    private static final String RECURSIVE_MESSAGE = "[This property is part of a recursion]";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      return super.getTableCellRendererComponent(table, RECURSIVE_MESSAGE, isSelected, hasFocus, row, column);
    }
  }

  private class LeftAlignedTableRenderer extends DefaultStringRenderer {
    public LeftAlignedTableRenderer(ILcyLucyEnv aLucyEnv, ILcdModel aModel) {
      super(aLucyEnv, aModel);
    }

    @Override
    protected void handleAlignment(Component aComponent, Class<?> aColumnClass) {
      //Always align everything to the left.
      setAlignment(aComponent, SwingConstants.LEFT);
    }
  }
}

