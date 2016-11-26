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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ALcdWeakChangeListener;
import com.luciad.util.TLcdChangeEvent;

/**
 * <p>This table header renderer normally delegates to the default CellRenderer. Only when it's
 * about the display column, this renderer will use itself as a display component (so a checkbox).
 * The selected state of the checkbox depends on the filter of the {@code ModelFilterTableModelDecorator},
 * and the enabled state depends on whether the whole layer filter accepts the domain object.</p>
 *
 * <p>It listens to changes in the table model to update the checkbox state when needed (the
 * checkbox is checked when at least one object is displayed).</p>
 *
 * <p>It listens to mouse events on the table header, to simulate a click in the checkbox.</p>
 *
 * <p>It listens to property changes in the UIManager, to update the look&feel if needed. This is
 * necessary because this component is not part of the component hierarchy.</p>
 *
 * <p>It is an action listener to itself, to update the table model when the display status of all
 * objects is changed.</p>
 */
class ModelObjectFilterTableHeaderRenderer extends JPanel implements TableCellRenderer, TableModelListener,
                                                                     MouseListener, ActionListener {
  /**
   * Install the renderer on {@code aTableSFCT}. This method should only be called when a renderer is
   * already present on the table, allowing it to be wrapped.
   * @param aTableSFCT The table
   * @param aTableModel The table model. This method will only have an effect when {@code aTableModel} is
   * an instance of {@code ModelFilterTableModelDecorator}
   * @param aFilter The filter which was installed on the layer contained in {@code aModelContext}
   */
  static void install(JTable aTableSFCT,
                      TableModel aTableModel,
                      TLcyModelObjectFilter aFilter) {
    if (aTableModel instanceof ModelFilterTableModelDecorator) {
      JTableHeader table_header = aTableSFCT.getTableHeader();
      TableColumn column = aTableSFCT.getColumnModel().getColumn(ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX);
      TableCellRenderer defaultRenderer = column.getCellRenderer() != null ? column.getCellRenderer() : table_header.getDefaultRenderer();
      ModelObjectFilterTableHeaderRenderer renderer = new ModelObjectFilterTableHeaderRenderer(aTableSFCT,
                                                                                               defaultRenderer,
                                                                                               (ModelFilterTableModelDecorator) aTableModel,
                                                                                               aFilter);
      column.setHeaderRenderer(renderer);
      table_header.addMouseListener(renderer);
      aTableModel.addTableModelListener(renderer);

      //Listen to UI changes, but avoid memory leaks
      UIManager.addPropertyChangeListener(new WeakUIUpdater(renderer));
      if (defaultRenderer instanceof Component) {
        UIManager.addPropertyChangeListener(new WeakUIUpdater((Component) defaultRenderer));
      }
    }
  }

  private final TableCellRenderer fDefaultCellRenderer;
  private final TLcyModelObjectFilter fFilter;
  private final ModelFilterTableModelDecorator fDisplayDecorator;
  private final JCheckBox fCheckBox = new JCheckBox();

  private final JTable fTable;

  private ModelObjectFilterTableHeaderRenderer(JTable aTable,
                                               TableCellRenderer aDefaultCellRenderer,
                                               ModelFilterTableModelDecorator aDecorator,
                                               TLcyModelObjectFilter aFilter) {
    fTable = aTable;
    fDisplayDecorator = aDecorator;
    fDefaultCellRenderer = aDefaultCellRenderer;
    fFilter = aFilter;
    fFilter.addChangeListener(new WeakChangeListener(this));
    setLayout(new CenteredHorizontalLayoutManager());
    add(fCheckBox);
    fCheckBox.addActionListener(this);
    fCheckBox.setSelected(true);
    fCheckBox.setBorderPainted(false);
    fCheckBox.setMargin(new Insets(0, 2, 0, 2));
    fCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
    fCheckBox.setText(" ");//make it match with the ModelObjectFilterTableCellRenderer
    setToolTipText(TLcyLang.getString("Toggles visibility of all objects"));
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (fDisplayDecorator != null &&
        table.convertColumnIndexToModel(column) == ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX) {
      Component orig = fDefaultCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      //Copy visualization properties from what it would originally be to our component
      setBackground(orig.getBackground());
      setForeground(orig.getForeground());
      setAlignmentX(orig.getAlignmentX());
      setAlignmentY(orig.getAlignmentY());
      setEnabled(orig.isEnabled());
      setFont(orig.getFont());
      if (orig instanceof JComponent) {
        setBorder(((JComponent) orig).getBorder());
      }
      return this;
    } else {
      return fDefaultCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }

  @Override
  public void tableChanged(TableModelEvent aEvent) {
    if (fDisplayDecorator != null &&
        aEvent.getColumn() == ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX) {
      updateCheckBox();
    }
  }

  private void updateCheckBox() {
    boolean selected = fFilter.getNumberOfAcceptedObjects() != 0;
    if (selected != fCheckBox.isSelected()) {
      fCheckBox.setSelected(selected);
      fTable.getTableHeader().repaint();
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //get the clicked column
    int view_column = fTable.getTableHeader().columnAtPoint(e.getPoint());

    //convert view column index to model column index
    int model_column = fTable.convertColumnIndexToModel(view_column);

    if (model_column == ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX) {
      Rectangle header_rect = fTable.getTableHeader().getHeaderRect(view_column);
      setBounds(0, 0, header_rect.width, header_rect.height);
      doLayout();
      if (SwingUtilities.getDeepestComponentAt(this, e.getX() - header_rect.x, e.getY() - header_rect.y) == fCheckBox) {
        fCheckBox.doClick();
      }
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //when the checkbox is clicked
    fDisplayDecorator.changeDisplayStatusAll(fCheckBox.isSelected());
  }

  public static class CenteredHorizontalLayoutManager implements LayoutManager {
    @Override
    public void layoutContainer(Container parent) {
      Component component = parent.getComponent(0);
      Dimension min = component.getMinimumSize();
      Rectangle parent_bounds = parent.getBounds();
      Insets insets = parent.getInsets();
      component.setBounds(
          Math.min(parent_bounds.width - insets.right - min.width, Math.max(insets.left, (parent_bounds.width - min.width) / 2)),
          parent_bounds.y + insets.top,
          min.width,
          parent_bounds.height - insets.top - insets.bottom);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      Dimension size = parent.getComponent(0).getMinimumSize();
      Insets insets = parent.getInsets();
      size.width += insets.left + insets.right;
      size.height += insets.top + insets.bottom;
      return size;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return minimumLayoutSize(parent);
    }
  }

  /**
   * Listener that updates the UI of the component given at construction time, whenever the UI
   * changes globally.  Takes care to avoid memory leaks as UIManager.addPropertyChangeListener is a
   * static method.
   */
  private static class WeakUIUpdater implements PropertyChangeListener {
    private final WeakReference<Component> fObject;

    public WeakUIUpdater(Component aComponent) {
      fObject = new WeakReference<Component>(aComponent);
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
      final Component component = fObject.get();
      if (component != null) {
        if (evt.getPropertyName() == null || "lookAndFeel".equals(evt.getPropertyName())) {
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              SwingUtilities.updateComponentTreeUI(component);
            }
          });
        }
      } else {
        UIManager.removePropertyChangeListener(this);
      }
    }
  }

  private static class WeakChangeListener extends ALcdWeakChangeListener<ModelObjectFilterTableHeaderRenderer> {

    private WeakChangeListener(ModelObjectFilterTableHeaderRenderer aRenderer) {
      super(aRenderer);
    }

    @Override
    protected void stateChangedImpl(final ModelObjectFilterTableHeaderRenderer aModelObjectFilterTableHeaderRenderer, TLcdChangeEvent aChangeEvent) {
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        @Override
        public void run() {
          aModelObjectFilterTableHeaderRenderer.updateCheckBox();
        }
      });
    }
  }
}
