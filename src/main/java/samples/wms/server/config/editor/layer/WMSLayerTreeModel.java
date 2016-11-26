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
package samples.wms.server.config.editor.layer;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * Data model for the WMS layer tree view.
 */
class WMSLayerTreeModel implements TreeModel {

  private ALcdWMSLayer fRoot;
  private EventListenerList fListenerList = new EventListenerList();

  public WMSLayerTreeModel(ALcdWMSLayer aRoot) {
    fRoot = aRoot;
  }

  public Object getRoot() {
    return fRoot;
  }

  public Object getChild(Object aParent, int aIndex) {
    if (aParent instanceof ALcdWMSLayer) {
      return ((ALcdWMSLayer) aParent).getChildWMSLayer(aIndex);
    } else {
      return null;
    }
  }

  public int getChildCount(Object aParent) {
    if (aParent instanceof ALcdWMSLayer) {
      return ((ALcdWMSLayer) aParent).getChildWMSLayerCount();
    } else {
      return 0;
    }
  }

  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  public void valueForPathChanged(TreePath aPath, Object aNewValue) {
  }

  public int getIndexOfChild(Object aParent, Object aChild) {
    if (aParent instanceof ALcdWMSLayer) {
      ALcdWMSLayer parent = (ALcdWMSLayer) aParent;
      for (int i = 0; i < parent.getChildWMSLayerCount(); i++) {
        ALcdWMSLayer child = (ALcdWMSLayer) parent.getChildWMSLayer(i);
        if (child.equals(aChild)) {
          return i;
        }
      }
    }
    return -1;
  }

  public void addTreeModelListener(TreeModelListener aListener) {
    fListenerList.add(TreeModelListener.class, aListener);
  }

  public void removeTreeModelListener(TreeModelListener aListener) {
    fListenerList.remove(TreeModelListener.class, aListener);
  }

  /**
   * The only event raised by this model is TreeStructureChanged with the
   * root as path, i.e. the whole tree has changed.
   */
  public void fireTreeStructureChanged(Object aRoot) {
    Object[] listeners = fListenerList.getListenerList();

    TreeModelEvent event = new TreeModelEvent(this, new Object[]{aRoot});
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(event);
      }
    }
  }
}
