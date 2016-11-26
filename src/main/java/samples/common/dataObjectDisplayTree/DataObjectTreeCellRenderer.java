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
package samples.common.dataObjectDisplayTree;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;

/**
 * Default <code>TreeCellRenderer</code> for <code>{@linkplain ILcdDataObject}</code> objects.
 */
public class DataObjectTreeCellRenderer extends DefaultTreeCellRenderer {

  private List<DataTypeCellRenderer> fRegisteredCellRenderers = new ArrayList<DataTypeCellRenderer>();

  /**
   * Adds a <code>DataTypeCellRenderer</code> to this <code>DataObjectTreeCellRenderer</code>.
   *
   * @param aDataTypeCellRenderer a <code>DataTypeCellRenderer</code>
   */
  public void addCellRenderer(DataTypeCellRenderer aDataTypeCellRenderer) {
    fRegisteredCellRenderers.add(aDataTypeCellRenderer);
  }

  /**
   * Removes a <code>DataTypeCellRenderer</code> from this <code>DataObjectTreeCellRenderer</code>.
   *
   * @param aDataTypeCellRenderer a <code>DataTypeCellRenderer</code>
   */
  public void removeCellRenderer(DataTypeCellRenderer aDataTypeCellRenderer) {
    fRegisteredCellRenderers.remove(aDataTypeCellRenderer);
  }

  /**
   * This cell renderer renders tree cells using the names from the properties of the
   * <code>ILcdDataObject</code> instances. If a property does not implement
   * <code>ILcdDataObject</code>, it will use <code>toString()</code> to determine the name.
   */
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                boolean leaf, int row, boolean hasFocus) {
    DataObjectTreeNode node = (DataObjectTreeNode) value;
    String displayString = null;
    for (DataTypeCellRenderer registeredCellRenderer : fRegisteredCellRenderers) {
      if (registeredCellRenderer.canRender(node)) {
        displayString = registeredCellRenderer.render(node);
        break;
      }
    }

    if (displayString == null) {
      Object object = node.getValue();
      // Determine the actual string.
      if (leaf && object != null) {
        String propertyName = node.getName();
        TLcdDataProperty property = node.getProperty();
        if (property == null) {
          //a null property for a leaf might indicate a collection entry. The actual property is located in the
          //parent node
          //when not a collection type
          DataObjectTreeNode parentNode = node.getParentNode();
          if (parentNode != null &&
              parentNode.getProperty() != null &&
              parentNode.getProperty().getCollectionType() == TLcdDataProperty.CollectionType.LIST) {
            property = parentNode.getProperty();
          }
        }
        String propertyValue = property != null ?
                               property.getType().getDisplayName(object) :
                               object.toString();
        displayString = formatLeaf(propertyName, propertyValue);
      } else {
        displayString = node.toString(); //default
        if (displayString.length() == 0 && object != null) {
          displayString = ((ILcdDataObject) object).getDataType().getDisplayName();
        }
      }
    }

    JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    label.setText(displayString);
    return label;
  }

  static String formatLeaf(String propertyName, String propertyValue) {
    return propertyName.length() > 0 ?
           propertyName + ": " + propertyValue :
           propertyName + propertyValue;
  }

  /**
   * <p>Factory method to create a new renderer instance.</p>
   *
   * <p>The returned instance will already contain some extra {@link DataTypeCellRenderer} instances
   * to cover the most common use-cases.</p>
   *
   * @return A new, pre-configured {@code DataObjectTreeCellRenderer} instance
   */
  public static DataObjectTreeCellRenderer createRenderer() {
    DataObjectTreeCellRenderer result = new DataObjectTreeCellRenderer();
    result.addCellRenderer(new ISOMeasureTreeCellRenderer());
    result.addCellRenderer(new ISOMeasureAnnotatedTreeCellRenderer());

    return result;
  }
}
