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

import java.awt.ComponentOrientation;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * This subclass of JTree can display the properties of an <code>ILcdDataObject</code> object. If
 * the properties themselves are also <code>ILcdDataObject</code>, a hierarchical tree is
 * displayed.
 */
public class DataObjectDisplayTree extends JTree {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DataObjectDisplayTree.class.getName());
  private RootSettableTreeModel fTreeModel;
  private ILcdModelListener fRepaintListener = new RepaintModelListener();
  private ILcdModel fDataModel;

  public DataObjectDisplayTree() {
    super(new DataObjectTreeModel());
    fTreeModel = (RootSettableTreeModel) getModel();
  }

  public ILcdModel getDataModel() {
    return fDataModel;
  }

  public void setDataModel(ILcdModel aDataModel) {
    if (fDataModel != null) {
      fDataModel.removeModelListener(fRepaintListener);
    }
    fDataModel = aDataModel;
    if (fDataModel != null) {
      fDataModel.addModelListener(fRepaintListener);
    }
  }

  @Override
  public void setModel(TreeModel aNewModel) {
    if (!(aNewModel instanceof RootSettableTreeModel)) {
      sLogger.warn("Given TreeModel is not instance of RootSettableTreeModel. This will interfere with the correct working of the JTree.");
      fTreeModel = null;
    } else {
      if (fTreeModel != null) {
        ((RootSettableTreeModel) aNewModel).setRootObject(fTreeModel.getRoot());
      }
      fTreeModel = (RootSettableTreeModel) aNewModel;
    }
    super.setModel(aNewModel);
  }

  /**
   * Sets the object of which the properties should be shown.
   *
   * @param aObject The object of which the properties should be shown.
   */
  public void setDataObject(ILcdDataObject aObject) {
    if (fTreeModel != null) {
      fTreeModel.setRootObject(new DataObjectTreeNode(aObject == null ? new DataObjectTreeModel.DummyRoot() : aObject));
    }
  }

  private class RepaintModelListener implements ILcdModelListener {
    @Override
    public void modelChanged(TLcdModelChangedEvent aModelChangedEvent) {
      int code = aModelChangedEvent.getCode();
      if ((code & TLcdModelChangedEvent.OBJECTS_CHANGED) != 0 ||
          (code & TLcdModelChangedEvent.ALL_OBJECTS_CHANGED) != 0) {
        // change orientation so that the layout cache of the tree is flushed
        // and the ellipsis (...) is avoided.
        ComponentOrientation orientation = getComponentOrientation();
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setComponentOrientation(orientation);
        repaint();
      }
    }
  }
}
