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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ALcdWeakModelListener;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.util.ILcdFeaturedDescriptorProvider;
import com.luciad.util.ILcdFilter;

/**
 * This tree-table implementation represents the features of an <code>ILcdFeatured</code> object in
 * a tree table. It uses a variation on the Visitor pattern to obtain the values for each of the
 * nodes in the tree.
 */
class TreeTableFeaturedModel extends DefaultTreeTableModel {
  /**
   * Filter which can be used to determine whether a <code>TreeTableFeaturedModel</code> can be
   * created for a <code>TLcyDomainObjectContext</code>
   */
  public static final ILcdFilter<TLcyDomainObjectContext> DOMAIN_OBJECT_CONTEXT_FILTER = new ILcdFilter<TLcyDomainObjectContext>() {
    @Override
    public boolean accept(TLcyDomainObjectContext aContext) {
      //this filter only accepts domain objects that are ILcdFeatured and for which
      //the ILcdFeaturedDescriptor can be retrieved.
      if (aContext != null) {
        //Only allow featured objects that are not data object. We have an explicit check
        //for this, because data objects should always be viewed in the more advanced data
        //object tree table view.
        if (aContext.getDomainObject() instanceof ILcdFeatured
            && !(aContext.getDomainObject() instanceof ILcdDataObject)) {
          ILcdFeatured featured = (ILcdFeatured) aContext.getDomainObject();
          return TreeTableFeaturedModel.retrieveFeaturedDescriptor(aContext.getModel(), featured, false) != null;
        }
      }
      return false;
    }
  };
  private TLcyDomainObjectContext fDomainObjectContext;

  private static ILcdFeaturedDescriptor retrieveFeaturedDescriptor(ILcdModel aModel,
                                                                   ILcdFeatured aDomainObject,
                                                                   boolean aMustFindProvider) {
    ILcdFeaturedDescriptor descriptor = null;
    if (aModel.getModelDescriptor() instanceof ILcdFeaturedDescriptorProvider) {
      ILcdFeaturedDescriptorProvider provider = (ILcdFeaturedDescriptorProvider) aModel.getModelDescriptor();
      descriptor = provider.getFeaturedDescriptor(aDomainObject);
    } else if (aModel instanceof ILcdFeaturedDescriptorProvider) {
      ILcdFeaturedDescriptorProvider provider = (ILcdFeaturedDescriptorProvider) aModel;
      descriptor = provider.getFeaturedDescriptor(aDomainObject);
    }

    if (!aMustFindProvider) {
      if (descriptor == null) {
        if (aModel instanceof ILcdFeaturedDescriptor) {
          descriptor = (ILcdFeaturedDescriptor) aModel;
        } else if (aModel.getModelDescriptor() instanceof ILcdFeaturedDescriptor) {
          descriptor = (ILcdFeaturedDescriptor) aModel.getModelDescriptor();
        }
      }
    }

    return descriptor;
  }

  private static final MessageFormat FORMAT = new MessageFormat(TLcyLang.getString("Feature {0}"));

  private static final String[] NAMES = {
      TLcyLang.getString("Feature name"),
      TLcyLang.getString("Value")
  };

  private boolean fHideNullOrEmptyNodes = false;

  /**
   * Create a new <code>TreeTableFeaturedModel</code> based on <code>aDomainObjectContext</code>.
   *
   * @param aDomainObjectContext The domain object context. This context must be accepted by  the
   *                             {@link #DOMAIN_OBJECT_CONTEXT_FILTER}.
   * @param aCustomizerPanel     The customizer panel, used to determine whether <code>null</code>
   *                             and/or empty nodes must be hidden. May be <code>null</code>
   */
  public TreeTableFeaturedModel(TLcyDomainObjectContext aDomainObjectContext,
                                TreeTableViewCustomizerPanel aCustomizerPanel) {
    super(new TreeTableFeaturedMutableNode("", false, ""));
    fDomainObjectContext = aDomainObjectContext;
    if (aCustomizerPanel != null) {
      fHideNullOrEmptyNodes = aCustomizerPanel.isHideNullOrEmptyNodes();
      aCustomizerPanel.addPropertyChangeListener("hideNullOrEmptyNodes", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          fHideNullOrEmptyNodes = (Boolean) evt.getNewValue();
          updateModel();
        }
      });
    }

    updateModel();

    //add listener to model which updates the tree model when the model changes
    ILcdModel model = aDomainObjectContext.getModel();
    if (model != null) {
      model.addModelListener(new ModelListener(this));
    }
  }

  private void updateModel() {
    Updater updater = new Updater(fDomainObjectContext.getModel());
    updater.updateForFeatured((ILcdFeatured) fDomainObjectContext.getDomainObject(),
                              (TreeTableFeaturedMutableNode) getRoot());
    //indicate the whole tree has been changed
    modelSupport.fireTreeStructureChanged(new TreePath(TreeTableFeaturedModel.this.getRoot()));
  }

  @Override
  public String getColumnName(int aColumn) {
    return NAMES[aColumn];
  }

  @Override
  public int getColumnCount() {
    return NAMES.length;
  }

  @Override
  public Object getValueAt(Object aObject, int aColumn) {
    TreeTableFeaturedMutableNode node = (TreeTableFeaturedMutableNode) aObject;
    return node.getValueAt(aColumn);
  }

  /**
   * Method to filter which features are shown in the tree table, by only allowing features with a
   * value that is not null and - in case of an array, list or another <code>ILcdFeatured</code> -
   * not empty.
   *
   * @param aDomainObject The domain object
   *
   * @return <code>true</code> when the domain object is not empty nor <code>null</code>
   */
  private boolean isNotEmptyOrNull(Object aDomainObject) {
    if (aDomainObject != null) {
      if (aDomainObject instanceof ILcdFeatured) {
        return ((ILcdFeatured) aDomainObject).getFeatureCount() > 0;
      } else if (aDomainObject instanceof List) {
        return !((List) aDomainObject).isEmpty();
      } else if (aDomainObject.getClass().isArray()) {
        return ((Object[]) aDomainObject).length > 0;
      } else {
        return true;
      }
    }
    return false;
  }

  private class Updater {

    private final ILcdModel fModel;

    private ILcdFeaturedDescriptor fFeaturedDescriptor = null;
    private int fFeatureIndex = 0;
    private int fDisplayedFeatureCount = 0;

    private final Stack<TreeTableFeaturedMutableNode> fStack = new Stack<>();

    public Updater(ILcdModel aModel) {
      fModel = aModel;
    }

    public void updateForFeatured(ILcdFeatured aFeatured, TreeTableFeaturedMutableNode aNode) {
      aNode.setUserObject(aFeatured);
      aNode.setShouldRenderFeature(false);
      aNode.setFeatureClass(ILcdFeatured.class);
      aNode.setDisplayName(String.valueOf(aFeatured));

      modelSupport.firePathChanged(new TreePath(aNode));

      if (aFeatured != null) {
        ILcdFeaturedDescriptor featured_descriptor = retrieveFeaturedDescriptor(fModel, aFeatured, false);
        iterateNode(aNode, featured_descriptor, new FeaturedListIterator(aFeatured));
      } else {
        removeAllChildren(new TreeTableFeaturedMutableNode[]{aNode});
      }
    }

    private void iterateNode(TreeTableFeaturedMutableNode aNode, ILcdFeaturedDescriptor aFeaturedDescriptor, ListIterator aIterator) {
      fStack.push(aNode);
      fFeaturedDescriptor = aFeaturedDescriptor;
      fDisplayedFeatureCount = 0;

      //update the nodes in the model to reflect the new state of the children
      while (aIterator.hasNext()) {
        fFeatureIndex = aIterator.nextIndex();
        Object feature = aIterator.next();

        ListIterator iterator;
        ILcdFeaturedDescriptor descriptor;

        if (!fHideNullOrEmptyNodes || isNotEmptyOrNull(feature)) {
          if (feature instanceof ILcdFeatured) {
            ILcdFeatured featured = (ILcdFeatured) feature;
            descriptor = retrieveFeaturedDescriptor(fModel, featured, true);
            iterator = new FeaturedListIterator(featured);
          } else if (feature instanceof List) {
            iterator = ((List) feature).listIterator();
            descriptor = null;
          } else {
            iterator = null;
            descriptor = null;
          }

          //if the feature was itself a ILcdFeatured or List, show the children of these as well.
          if (iterator != null) {
            //record these values, because the call to iterate will modify these fields.
            ILcdFeaturedDescriptor old_descriptor = fFeaturedDescriptor;
            int old_index = fFeatureIndex;

            TreeTableFeaturedMutableNode created_or_updated_node = updateNode(feature, false);
            int old_displayed_featureindex = fDisplayedFeatureCount;
            try {
              iterateNode(created_or_updated_node, descriptor, iterator);
            } finally {
              fFeaturedDescriptor = old_descriptor;
              fFeatureIndex = old_index;
              fDisplayedFeatureCount = old_displayed_featureindex;
              fStack.pop();
            }
          } else {
            TreeTableFeaturedMutableNode updated_or_created_node = updateNode(feature, feature != null);
            removeAllChildren(getPathFromStack(new TreeTableFeaturedMutableNode[]{updated_or_created_node}));
          }
        }
      }

      //remove the remaining children
      TreeTableFeaturedMutableNode node = fStack.peek();
      int remaining_child_count = node.getChildCount();
      int nr_of_children_to_delete = remaining_child_count - fDisplayedFeatureCount;
      if (nr_of_children_to_delete > 0) {
        int[] children = new int[nr_of_children_to_delete];
        for (int i = 0; i < children.length; i++) {
          children[i] = fDisplayedFeatureCount + i;
        }
        Object[] child_objects = new Object[nr_of_children_to_delete];
        for (int i = 0; node.getChildCount() > fDisplayedFeatureCount; i++) {
          child_objects[i] = node.getChildAt(fDisplayedFeatureCount);
          node.remove(fDisplayedFeatureCount);
        }

        modelSupport.fireChildrenRemoved(new TreePath(getPathFromStack()), children, child_objects);
      }
    }

    /**
     * Updates the node in the treemodel according to the specified feature.
     *
     * @param aFeature             The feature which should be displayed in the tree.
     * @param aShouldRenderFeature Whether or not the value should displayed in the value column of
     *                             the tree-table.
     *
     * @return The mode already present in the treemodel at the treepath of the feature, or a newly
     *         created and inserted node if there was no node present yet.
     */
    private TreeTableFeaturedMutableNode updateNode(Object aFeature, boolean aShouldRenderFeature) {
      TreeTableFeaturedMutableNode node;

      TreeTableFeaturedMutableNode last_node = fStack.peek();
      if (fDisplayedFeatureCount < last_node.getChildCount()) {
        //there already was a node at the specified tree-path, update its values
        node = (TreeTableFeaturedMutableNode) last_node.getChildAt(fDisplayedFeatureCount);
        node.setUserObject(aFeature);
        node.setShouldRenderFeature(aShouldRenderFeature);
        node.setDisplayName(getFeatureName());
        node.setFeatureClass(getFeatureClass());

        Object[] path = getPathFromStack();
        modelSupport.fireChildChanged(new TreePath(path), fDisplayedFeatureCount, node);
        fDisplayedFeatureCount++;
      } else {
        //no node exists yet, create one and add it to the tree.
        node = new TreeTableFeaturedMutableNode(aFeature, aShouldRenderFeature, getFeatureName(), getFeatureClass());
        last_node.add(node);
        Object[] path = getPathFromStack();
        modelSupport.fireChildAdded(new TreePath(path), fDisplayedFeatureCount, node);
        fDisplayedFeatureCount++;
      }
      return node;
    }

    //this method removes all the children from the node specified by the path, and fires the correct
    //tree event.
    private void removeAllChildren(TreeTableFeaturedMutableNode[] aTreePath) {
      TreeTableFeaturedMutableNode aNode = aTreePath[aTreePath.length - 1];
      if (aNode.getChildCount() != 0) {
        //before actually removing the children, record which children are present so these can
        //be used in the tree-event.
        int[] child_indices = new int[aNode.getChildCount()];
        for (int i = 0; i < child_indices.length; i++) {
          child_indices[i] = i;
        }
        Object[] children = new Object[aNode.getChildCount()];
        for (int i = 0; i < aNode.getChildCount(); i++) {
          children[i] = aNode.getChildAt(i);
        }
        aNode.removeAllChildren();
        modelSupport.fireChildrenRemoved(new TreePath(aTreePath), child_indices, children);
      }
    }

    private TreeTableFeaturedMutableNode[] getPathFromStack() {
      return getPathFromStack(new TreeTableFeaturedMutableNode[0]);
    }

    private TreeTableFeaturedMutableNode[] getPathFromStack(TreeTableFeaturedMutableNode[] aAppend) {
      TreeTableFeaturedMutableNode[] result = new TreeTableFeaturedMutableNode[fStack.size() + aAppend.length];
      fStack.copyInto(result);
      System.arraycopy(aAppend, 0, result, fStack.size(), aAppend.length);
      return result;
    }

    private String getFeatureName() {
      if (fFeaturedDescriptor != null) {
        return fFeaturedDescriptor.getFeatureName(fFeatureIndex);
      } else {
        return FORMAT.format(new Object[]{fFeatureIndex + 1});
      }
    }

    private Class getFeatureClass() {
      if (fFeaturedDescriptor != null) {
        Class feature_class = fFeaturedDescriptor.getFeatureClass(fFeatureIndex);
        return feature_class != null ? feature_class : Object.class;
      } else {
        return Object.class;
      }
    }
  }

  /**
   * Iterator implementation that iterates of the features of an ILcdFeatured. This is implemented
   * to have uniform iterating capabilities over an ILcdFeatured as over a Collection.
   */
  private static class FeaturedListIterator implements ListIterator {
    private final ILcdFeatured fFeatured;
    private int fIndex = 0;

    public FeaturedListIterator(ILcdFeatured aFeatured) {
      fFeatured = aFeatured;
    }

    @Override
    public boolean hasNext() {
      return fIndex < fFeatured.getFeatureCount();
    }

    @Override
    public Object next() {
      return fFeatured.getFeature(fIndex++); //post-increment
    }

    @Override
    public int nextIndex() {
      return fIndex;
    }

    //the remaining methods are not implemented as they are not used
    @Override
    public void add(Object e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object previous() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int previousIndex() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object e) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A tree-table node that holds the feature and its meta-data.
   */
  private static class TreeTableFeaturedMutableNode extends AbstractTreeTableNode {
    private boolean fShouldRenderFeature;
    private String fDisplayName;
    private Class fFeatureClass;

    public TreeTableFeaturedMutableNode(Object userObject, boolean aShouldRenderFeature, String aDisplayName) {
      this(userObject, aShouldRenderFeature, aDisplayName, Object.class);
    }

    public TreeTableFeaturedMutableNode(Object userObject, boolean aShouldRenderFeature, String aDisplayName, Class aFeatureClass) {
      super(convertUserObject(userObject));
      setFeatureClass(aFeatureClass);
      fDisplayName = aDisplayName;
      fShouldRenderFeature = aShouldRenderFeature;

      if (fFeatureClass == null) {
        throw new IllegalArgumentException();
      }
    }

    public void setDisplayName(String aDisplayName) {
      fDisplayName = aDisplayName;
    }

    public void setFeatureClass(Class aFeatureClass) {
      fFeatureClass = convertUserObjectClass(getUserObject(), aFeatureClass);
    }

    @Override
    public void setUserObject(Object object) {
      super.setUserObject(convertUserObject(object));
    }

    public void setShouldRenderFeature(boolean aShouldRenderFeature) {
      fShouldRenderFeature = aShouldRenderFeature;
    }

    @Override
    public Object getValueAt(int aColumn) {
      if (aColumn == 1 && fShouldRenderFeature) {
        return getUserObject();
      } else if (aColumn == 1) {
        return null;
      } else {
        return fDisplayName;
      }
    }

    @Override
    public boolean getAllowsChildren() {
      return ILcdFeatured.class.isAssignableFrom(fFeatureClass) ||
             List.class.isAssignableFrom(fFeatureClass) ||
             getUserObject() instanceof ILcdFeatured ||
             getUserObject() instanceof List;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    public void removeAllChildren() {
      int childCount = getChildCount();
      for (int i = childCount - 1; i >= 0; i--) {
        remove(i);
      }
    }

    @Override
    public Class<?> getObjectClass() {
      return fFeatureClass;
    }
  }

  private static class ModelListener extends ALcdWeakModelListener<TreeTableFeaturedModel> {

    private ModelListener(TreeTableFeaturedModel aTreeTableFeaturedModel) {
      super(aTreeTableFeaturedModel);
    }

    @Override
    protected void modelChangedImpl(TreeTableFeaturedModel aToModify, TLcdModelChangedEvent aModelChangedEvent) {
      aToModify.updateModel();
    }
  }

}
