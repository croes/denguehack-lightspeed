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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdAssociationClassAnnotation;
import com.luciad.datamodel.TLcdDataProperty;

/**
 * A tree model wrapper that takes into account the {@link TLcdAssociationClassAnnotation
 * association annotation} while determining the children of a node. It skips elements that have an
 * association annotation, and instead returns the association directly.</p> It assumes all nodes
 * are instances of {@link DataObjectTreeNode object holders}.
 */
public class DataObjectAssociationClassRemovingTreeModel implements RootSettableTreeModel {
  private RootSettableTreeModel fDelegateTreeModel;

  public DataObjectAssociationClassRemovingTreeModel(RootSettableTreeModel aDelegateTreeModel) {
    fDelegateTreeModel = aDelegateTreeModel;
  }

  public Object getRoot() {
    return fDelegateTreeModel.getRoot();
  }

  public void setRootObject(Object aObject) {
    fDelegateTreeModel.setRootObject(aObject);
  }

  public Object getChild(Object aParent, int index) {
    DataObjectTreeNode parentHolder = (DataObjectTreeNode) aParent;
    TLcdDataProperty roleProperty = getRoleProperty((DataObjectTreeNode) aParent);
    if (roleProperty != null) {
      Object parentObject = parentHolder.getValue();
      if (parentObject instanceof ILcdDataObject) {
        int childCount = fDelegateTreeModel.getChildCount(parentHolder);
        for (int i = 0; i < childCount; i++) {
          DataObjectTreeNode child = (DataObjectTreeNode) fDelegateTreeModel.getChild(aParent, i);
          Object childObject = child.getValue();
          if (childObject != null && childObject.equals(((ILcdDataObject) parentObject).getValue(roleProperty))) {
            return fDelegateTreeModel.getChild(child, index);
          }
        }
      }
    }
    return fDelegateTreeModel.getChild(aParent, index);
  }

  public int getChildCount(Object aParent) {
    DataObjectTreeNode parentHolder = (DataObjectTreeNode) aParent;
    TLcdDataProperty roleProperty = getRoleProperty((DataObjectTreeNode) aParent);
    if (roleProperty != null) {
      Object parentObject = parentHolder.getValue();
      if (parentObject instanceof ILcdDataObject) {
        int childCount = fDelegateTreeModel.getChildCount(parentHolder);
        for (int i = 0; i < childCount; i++) {
          DataObjectTreeNode child = (DataObjectTreeNode) fDelegateTreeModel.getChild(aParent, i);
          Object childObject = child.getValue();
          if (childObject != null && childObject.equals(((ILcdDataObject) parentObject).getValue(roleProperty))) {
            return fDelegateTreeModel.getChildCount(child);
          }
        }
      }
    }
    return fDelegateTreeModel.getChildCount(aParent);
  }

  public boolean isLeaf(Object aParent) {
    DataObjectTreeNode parentHolder = (DataObjectTreeNode) aParent;
    TLcdDataProperty roleProperty = getRoleProperty((DataObjectTreeNode) aParent);
    if (roleProperty != null) {
      Object parentObject = parentHolder.getValue();
      if (parentObject instanceof ILcdDataObject) {
        int childCount = fDelegateTreeModel.getChildCount(parentHolder);
        for (int i = 0; i < childCount; i++) {
          DataObjectTreeNode child = (DataObjectTreeNode) fDelegateTreeModel.getChild(aParent, i);
          Object childObject = child.getValue();
          if (childObject != null && childObject.equals(((ILcdDataObject) parentObject).getValue(roleProperty))) {
            return fDelegateTreeModel.isLeaf(child);
          }
        }
      }
    }
    return fDelegateTreeModel.isLeaf(aParent);
  }

  /**
   * Returns either a role property that is part of an association annotation type, or null if the
   * object was not annotated.
   *
   * @param aDataObjectTreeNode An object holder to analyze
   *
   * @return A data property that is a role property, or null
   */
  private TLcdDataProperty getRoleProperty(DataObjectTreeNode aDataObjectTreeNode) {
    if (aDataObjectTreeNode.getProperty() != null) {
      if (aDataObjectTreeNode.getProperty().getType().isAnnotationPresent(TLcdAssociationClassAnnotation.class)) {
        TLcdAssociationClassAnnotation annotation = aDataObjectTreeNode.getProperty().getType().getAnnotation(TLcdAssociationClassAnnotation.class);
        return annotation.getRoleProperty();
      }
    } else {
      Object object = aDataObjectTreeNode.getValue();
      if (object instanceof ILcdDataObject && ((ILcdDataObject) object).getDataType().isAnnotationPresent(TLcdAssociationClassAnnotation.class)) {
        TLcdAssociationClassAnnotation annotation = ((ILcdDataObject) object).getDataType().getAnnotation(TLcdAssociationClassAnnotation.class);
        return annotation.getRoleProperty();
      }
    }
    return null;
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    fDelegateTreeModel.valueForPathChanged(path, newValue);
  }

  public int getIndexOfChild(Object aParent, Object aChild) {
    DataObjectTreeNode parentHolder = (DataObjectTreeNode) aParent;
    TLcdDataProperty roleProperty = getRoleProperty((DataObjectTreeNode) aParent);
    if (roleProperty != null) {
      Object parentObject = parentHolder.getValue();
      if (parentObject instanceof ILcdDataObject) {
        int childCount = fDelegateTreeModel.getChildCount(parentHolder);
        for (int i = 0; i < childCount; i++) {
          DataObjectTreeNode realChild = (DataObjectTreeNode) fDelegateTreeModel.getChild(aParent, i);
          Object childObject = realChild.getValue();
          if (childObject != null && childObject.equals(((ILcdDataObject) parentObject).getValue(roleProperty))) {
            return fDelegateTreeModel.getIndexOfChild(realChild, aChild);
          }
        }
      }
    }
    return fDelegateTreeModel.getIndexOfChild(aParent, aChild);
  }

  public void addTreeModelListener(TreeModelListener l) {
    fDelegateTreeModel.addTreeModelListener(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    fDelegateTreeModel.removeTreeModelListener(l);
  }
}
