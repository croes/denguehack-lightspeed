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

import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Implementation of TreeModel that makes the properties of an <code>ILcdDataObject</code>
 * available. </p> It assumes all nodes are instances of {@link DataObjectTreeNode object
 * holders}.
 */
public class DataObjectTreeModel implements RootSettableTreeModel {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DataObjectTreeModel.class.getName());
  private EventListenerList fListeners = new EventListenerList();
  private DataObjectTreeNode fRootDataObjectTreeNode;

  public DataObjectTreeModel() {
    this(new DummyRoot());
  }

  public DataObjectTreeModel(Object aRootObject) {
    fRootDataObjectTreeNode = new DataObjectTreeNode(aRootObject);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    fListeners.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    fListeners.remove(TreeModelListener.class, l);
  }

  @Override
  public void setRootObject(Object aRootObject) {
    if (!(aRootObject instanceof DataObjectTreeNode)) {
      sLogger.error("Given root object must be instance of DataObjectTreeNode");
      throw new IllegalArgumentException("Given root object [ " + aRootObject + "] must be instance of DataObjectTreeNode");
    }
    fRootDataObjectTreeNode = (DataObjectTreeNode) aRootObject;
    fireTreeStructureChanged(fRootDataObjectTreeNode, new TreePath(fRootDataObjectTreeNode));
  }

  @Override
  public Object getRoot() {
    return fRootDataObjectTreeNode;
  }

  @Override
  public int getChildCount(Object parentTreeNode) {
    return getChildCount(((DataObjectTreeNode) parentTreeNode));
  }

  private int getChildCount(DataObjectTreeNode parentTreeNode) {
    if (isPartOfRecursion(parentTreeNode)) {
      return 0;
    }
    Object parent = parentTreeNode.getValue();
    if (parent instanceof ILcdDataObject) {
      ILcdDataObject dataObject = (ILcdDataObject) parent;
      List<TLcdDataProperty> dataProperties = dataObject.getDataType().getProperties();
      return dataProperties.size();
    }
    if (parent instanceof List<?>) {
      List<?> list = (List<?>) parent;
      return list.size();
    }
    return 0;
  }

  @Override
  public Object getChild(Object parentNode, int index) {
    return getChild(((DataObjectTreeNode) parentNode), index);
  }

  private Object getChild(DataObjectTreeNode aParentNode, int aIndex) {
    final Object parentObject = (aParentNode).getValue();
    if (parentObject instanceof ILcdDataObject) {
      TLcdDataType parentDataType = ((ILcdDataObject) parentObject).getDataType();
      List<TLcdDataProperty> dataProperties = parentDataType.getProperties();
      TLcdDataProperty property = dataProperties.get(aIndex);
      return new DataObjectTreeNode(aParentNode, property, aIndex);

    } else if (parentObject instanceof List<?>) {
      return new DataObjectTreeNode(aParentNode, aIndex);
    }
    return null;
  }

  /**
   * Returns whether the given node is part of a recursion.
   *
   * @param aNode an <code>DataObjectTreeNode</code>
   *
   * @return true if it is part of a recursion; false otherwise.
   */
  private static boolean isPartOfRecursion(DataObjectTreeNode aNode) {
    Object objectToCheck = aNode.getValue();
    DataObjectTreeNode parentHolder = aNode.getParentNode();
    while (parentHolder != null) {
      if (parentHolder.getValue() == objectToCheck) {
        return true;
      }
      parentHolder = parentHolder.getParentNode();
    }
    return false;
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent == null || child == null) {
      return -1;
    }
    return ((DataObjectTreeNode) child).getIndex();
  }

  @Override
  public boolean isLeaf(Object holder) {
    Object node = ((DataObjectTreeNode) holder).getValue();
    return !(node instanceof ILcdDataObject) && !(node instanceof List<?>);
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    fireTreeStructureChanged(fRootDataObjectTreeNode, path);
  }

  private void fireTreeStructureChanged(Object source, TreePath path) {
    Object[] listeners = fListeners.getListenerList();
    TreeModelEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event
        if (e == null) {
          e = new TreeModelEvent(source, path);
        }
        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
      }
    }
  }

  public static final class DummyRoot {
    @Override
    public String toString() {
      return "Please select an object.";
    }
  }
}
