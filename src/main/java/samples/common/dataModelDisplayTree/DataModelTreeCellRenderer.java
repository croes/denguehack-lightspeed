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
package samples.common.dataModelDisplayTree;

import java.awt.Color;
import java.awt.Component;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.model.ILcdDataModelDescriptor;

/**
 * A <code>TreeCellRenderer</code> for a <code>JTree</code> with a <code>DataModelTreeModel</code>.
 */
public class DataModelTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final Color MODEL_ELEMENT_TEXT_COLOR = new Color(0, 115, 62);

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component rendererComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    if (tree.getModel() instanceof DataModelTreeModel) {
      ILcdDataModelDescriptor dataModelDescriptor = ((DataModelTreeModel) tree.getModel()).getDataModelDescriptor();
      if (value instanceof DataModelTreeModel.DataTypeNode) {
        if (isModelElementDataType(dataModelDescriptor, ((DataModelTreeModel.DataTypeNode) value).getType())) {
          //If the given node is a data type node, and it is one of the model element data types,
          //we give it a special color.
          rendererComponent.setForeground(MODEL_ELEMENT_TEXT_COLOR);
        }
      }
    }
    return rendererComponent;
  }

  /**
   * Checks if a given data type is a model element data type
   * @param aDataModelDescriptor a model descriptor
   * @param aDataType a data type to check
   * @return true if aDataType is part of the model elements of aDataModelDescriptor
   */
  private boolean isModelElementDataType(ILcdDataModelDescriptor aDataModelDescriptor, TLcdDataType aDataType) {
    if (aDataModelDescriptor == null || aDataType == null) {
      return false;
    }
    Set<TLcdDataType> modelElementDataTypes = aDataModelDescriptor.getModelElementTypes();
    if (modelElementDataTypes != null) {
      for (TLcdDataType modelElementDataType : modelElementDataTypes) {
        if (modelElementDataType.equals(aDataType)) {
          return true;
        }
      }
    }
    return false;
  }
}
