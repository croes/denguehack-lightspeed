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

import java.lang.reflect.Array;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.util.ILcdFilter;

/**
 * A tree model wrapper that filters out empty values.</p>
 * All nodes are assumed to be {@link DataObjectTreeNode} instances.
 */
public class DataObjectEmptyValueFilteringTreeModel implements RootSettableTreeModel {
  private RootSettableTreeModel fDelegateTreeModel;
  private ILcdFilter fEmptyPropertyFilter = new EmptyDataObjectFilter();

  public DataObjectEmptyValueFilteringTreeModel(RootSettableTreeModel aDelegateTreeModel) {
    fDelegateTreeModel = aDelegateTreeModel;
  }

  public Object getRoot() {
    return fDelegateTreeModel.getRoot();
  }

  public void setRootObject(Object aObject) {
    fDelegateTreeModel.setRootObject(aObject);
  }

  public Object getChild(Object parent, int index) {
    int count = -1;
    int childCount = fDelegateTreeModel.getChildCount(parent);
    if (fEmptyPropertyFilter == null) {
      return fDelegateTreeModel.getChild(parent, index);
    } else {
      for (int i = 0; i < childCount; i++) {
        Object child = fDelegateTreeModel.getChild(parent, i);
        if (fEmptyPropertyFilter.accept(child)) {
          count++;
        }
        if (count == index) {
          return child;
        }
      }
    }
    return null;
  }

  public int getChildCount(Object parent) {
    int delegateChildCount = fDelegateTreeModel.getChildCount(parent);
    if (fEmptyPropertyFilter == null) {
      return delegateChildCount;
    } else {
      int counter = 0;
      for (int i = 0; i < delegateChildCount; i++) {
        if (!fEmptyPropertyFilter.accept(fDelegateTreeModel.getChild(parent, i))) {
          counter++;
        }
      }
      return delegateChildCount - counter;
    }
  }

  public boolean isLeaf(Object node) {
    return fDelegateTreeModel.isLeaf(node);
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    fDelegateTreeModel.valueForPathChanged(path, newValue);
  }

  public int getIndexOfChild(Object parent, Object child) {
    if (parent == null || child == null) {
      return -1;
    }
    if (fEmptyPropertyFilter == null) {
      return fDelegateTreeModel.getIndexOfChild(parent, child);
    } else if (fDelegateTreeModel.getIndexOfChild(parent, child) == -1) {
      return -1;
    } else {
      int childCount = fDelegateTreeModel.getChildCount(parent);
      int index = 0;
      for (int i = 0; i < childCount; i++) {
        Object childNode = fDelegateTreeModel.getChild(parent, i);
        if (fEmptyPropertyFilter.accept(childNode)) {
          if (child.equals(childNode)) {
            return index;
          }
          index++;
        }
      }
      return -1;
    }
  }

  public void addTreeModelListener(TreeModelListener l) {
    fDelegateTreeModel.addTreeModelListener(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    fDelegateTreeModel.removeTreeModelListener(l);
  }

  /**
   * Gets the filter that is used to filter out tree elements without values
   * @return A filter; or null.
   */
  public ILcdFilter getEmptyPropertyFilter() {
    return fEmptyPropertyFilter;
  }

  /**
   * Sets a filter that is capable of filtering out tree elements without values.
   * @param aFilter a filter, or null to remove the current filter.
   */
  public void setEmptyPropertyFilter(ILcdFilter aFilter) {
    fEmptyPropertyFilter = aFilter;
  }

  /**
   * <code>ILcdFilter</code> that is used to restrict the features that are displayed in the tree table,
   * by only allowing features with a value that is not null and - in case of an array, list or
   * another <code>ILcdDataObject<code> - not empty.
   */
  private static class EmptyDataObjectFilter implements ILcdFilter {
    public boolean accept(Object aDomainObject) {
      if (aDomainObject != null) {
        if (aDomainObject instanceof DataObjectTreeNode) {
          aDomainObject = ((DataObjectTreeNode) aDomainObject).getValue();
        }
        if (aDomainObject != null) {
          if (aDomainObject instanceof ILcdDataObject) {
            return containsData((ILcdDataObject) aDomainObject);
          } else if (aDomainObject instanceof List) {
            return ((List) aDomainObject).size() > 0;
          } else if (aDomainObject.getClass().isArray()) {
            return Array.getLength(aDomainObject) > 0;
          } else {
            return true;
          }
        }
      }
      return false;
    }

    private boolean containsData(ILcdDataObject aDataObject) {
      List<TLcdDataProperty> dataProperties = aDataObject.getDataType().getProperties();
      for (TLcdDataProperty dataProperty : dataProperties) {
        if (aDataObject.getValue(dataProperty) != null) {
          return true;
        }
      }
      return false;
    }
  }

}
