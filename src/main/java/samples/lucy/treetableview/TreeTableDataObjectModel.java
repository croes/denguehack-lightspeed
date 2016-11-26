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

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdAssociationClassAnnotation;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.ILcdUndoableSource;
import com.luciad.gui.TLcdUndoSupport;
import samples.lucy.undo.DataPropertyValueUndoable;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ALcdWeakModelListener;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdFilter;
import com.luciad.util.enumeration.ILcdMorphingFunction;

/**
 * This tree-table implementation represents the properties of an <code>ILcdDataObject</code> object
 * in a tree table.
 */
class TreeTableDataObjectModel extends DefaultTreeTableModel implements ILcdUndoableSource {
  /**
   * Filter which can be used to determine whether a <code>TreeTableDataObjectModel</code> can be
   * created for a <code>TLcyDomainObjectContext</code>
   */
  public static final ILcdFilter<TLcyDomainObjectContext> DOMAIN_OBJECT_CONTEXT_FILTER = new ILcdFilter<TLcyDomainObjectContext>() {
    @Override
    public boolean accept(TLcyDomainObjectContext aObject) {
      return aObject != null && aObject.getDomainObject() instanceof ILcdDataObject;
    }
  };

  /**
   * A formatting for list and set elements {0} is the name of the property {1} is the index of the
   * property in the list
   */
  private static final MessageFormat LIST_ELEMENT_FORMAT = new MessageFormat("{0} [{1}]");
  private static final MessageFormat JAVA_UTIL_LIST_ELEMENT_FORMAT = new MessageFormat("[{1}]");
  /**
   * A formatting for lists and sets {0} is the name of the data type {1} is either "Set" or "List"
   */
  private static final MessageFormat LIST_FORMAT = new MessageFormat("{0} [{1}]");

  private static final String[] NAMES = {
      TLcyLang.getString("Property name"),
      TLcyLang.getString("Value")
  };

  private final TLcdUndoSupport fUndoSupport = new TLcdUndoSupport(this);
  /**
   * When {@code true}:
   * <ul>
   *   <li>Empty collections will not be included in the tree</li>
   *   <li>For properties with a {@link TLcdAssociationClassAnnotation} only the associated
   *   property will be listed</li>
   *   <li>Properties with a null value will not be included in the tree (except when they are
   *   editable)</li>
   * </ul>
   */
  private boolean fHideNullOrEmptyNodes = false;
  private TLcyDomainObjectContext fDomainObjectContext;
  private final ILcyLucyEnv fLucyEnv;

  /**
   * Create a new <code>TreeTableDataObjectModel</code> based on <code>aDomainObjectContext</code>.
   *
   * @param aDomainObjectContext The domain object context. This context must be accepted by  the
   *                             {@link #DOMAIN_OBJECT_CONTEXT_FILTER}.
   * @param aCustomizerPanel     The customizer panel, used to determine whether <code>null</code>
   * @param aLucyEnv             The Lucy env.
   */
  public TreeTableDataObjectModel(TLcyDomainObjectContext aDomainObjectContext,
                                  TreeTableViewCustomizerPanel aCustomizerPanel,
                                  ILcyLucyEnv aLucyEnv) {
    super(new TreeTableDataObjectMutableNode(aDomainObjectContext.getDomainObject(),
                                             null,
                                             null,
                                             "",
                                             TreeTableDataObjectMutableNode.Render.Invisible,
                                             null
    ));
    fDomainObjectContext = aDomainObjectContext;
    fLucyEnv = aLucyEnv;

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

  public TLcyDomainObjectContext getDomainObjectContext() {
    return fDomainObjectContext;
  }

  /**
   * Returns the array of data properties that lead to this node, or null if this is the root or a
   * collection node.
   *
   * @param aNode the node.
   * @return an array of data properties, leading from the root (the main data object) to the
   *         passed node, or null if this is not applicable.
   */
  public TLcdDataProperty[] getDataProperties(Object aNode) {
    if (!(aNode instanceof TreeTableDataObjectMutableNode)) {
      return null;
    }
    List<TLcdDataProperty> properties = new ArrayList<>();
    TreeTableDataObjectMutableNode node = (TreeTableDataObjectMutableNode) aNode;
    while (node != getRoot()) {
      if (isCollectionNode(node) && !properties.isEmpty()) {
        //Early out for data objects in collections. Editing inside collections is not supported for now.
        return null;
      }
      properties.add(node.getDataProperty());
      //Add the association property if there is one.
      if (node.getAssociationProperty() != null) {
        properties.add(node.getAssociationProperty());
      }
      node = (TreeTableDataObjectMutableNode) node.getParent();
    }
    if (!properties.isEmpty()) {
      Collections.reverse(properties);
      return properties.toArray(new TLcdDataProperty[properties.size()]);
    }
    return null;
  }

  private boolean isCollectionNode(TreeTableDataObjectMutableNode aNode) {
    return (aNode.getDataProperty().isCollection() || aNode.getUserObject() instanceof Set || aNode.getUserObject() instanceof List) ||
           (aNode.getAssociationProperty() != null &&
            aNode.getAssociationProperty().isCollection());

  }

  private void updateModel() {
    Updater updater = new Updater();
    updater.updateForDataObject((ILcdDataObject) fDomainObjectContext.getDomainObject(),
                                (TreeTableDataObjectMutableNode) getRoot());
  }

  /**
   * Method to filter which properties are shown in the tree table, by only allowing properties with
   * a value that is not null and - in case of an array, list or another <code>ILcdDataObject</code>
   * - not empty.
   *
   * @param aDomainObject The domain object
   *
   * @return <code>true</code> when the domain object is not empty nor <code>null</code>
   */
  private boolean isNotEmptyNorNull(Object aDomainObject) {
    if (aDomainObject == null) {
      return false;
    }
    if (aDomainObject instanceof ILcdDataObject) {
      return !((ILcdDataObject) aDomainObject).getDataType().getProperties().isEmpty();
    } else if (aDomainObject instanceof List) {
      return !((List) aDomainObject).isEmpty();
    } else if (aDomainObject instanceof Set) {
      return !((Set) aDomainObject).isEmpty();
    } else if (aDomainObject.getClass().isArray()) {
      return Array.getLength(aDomainObject) > 0;
    } else {
      return true;
    }
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
    TreeTableDataObjectMutableNode node = (TreeTableDataObjectMutableNode) aObject;
    return node.getValueAt(aColumn);
  }

  @Override
  public boolean isCellEditable(Object aNode, int aColumn) {
    return fDomainObjectContext.getDomainObject() instanceof ILcdDataObject &&
           getDataProperties(aNode) != null &&
           aColumn == 1;
  }

  @Override
  public void setValueAt(Object aValue, Object aNode, int aColumn) {
    if (isCellEditable(aNode, aColumn)) {
      ILcdModel model = fDomainObjectContext.getModel();
      ILcdDataObject dataObject = (ILcdDataObject) fDomainObjectContext.getDomainObject();
      TLcdDataProperty[] properties = getDataProperties(aNode);
      String expression = ExpressionUtility.createExpression(dataObject.getDataType(), properties);
      setValueInDataObject(aValue, model, dataObject, expression);
    }
  }

  private void setValueInDataObject(Object aValue, final ILcdModel aModel,
                                    final ILcdDataObject aDataObject, String aExpression) {

    Object oldValue = ExpressionUtility.retrieveValue(aDataObject, aExpression);
    if (!Objects.equals(oldValue, aValue)) {
      try (Lock autoUnlock = writeLock(aModel)) {
        ExpressionUtility.updateValue(aDataObject, aExpression, aValue);
      }
      fireUndoableHappened(aDataObject, aExpression, oldValue, aValue);
      aModel.elementChanged(aDataObject, ILcdModel.FIRE_NOW);
    }
  }

  @Override
  public void addUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.addUndoableListener(aUndoableListener);
  }

  @Override
  public void removeUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.removeUndoableListener(aUndoableListener);
  }

  /*
   * Create a data property value undoable.
   */
  private void fireUndoableHappened(ILcdDataObject aDataObject, String aExpression,
                                    Object aOldValue, Object aNewValue) {
    fUndoSupport.fireUndoableHappened(new DataPropertyValueUndoable(fDomainObjectContext.getModel(),
                                                                    aDataObject, aExpression,
                                                                    aOldValue, aNewValue));
  }

  private class Updater {

    private int fPropertyIndex = 0;
    private int fDisplayedPropertyCount = 0;

    private final Stack<TreeTableDataObjectMutableNode> fStack = new Stack<>();

    public void updateForDataObject(ILcdDataObject aDataObject, TreeTableDataObjectMutableNode aNode) {
      aNode.setUserObject(aDataObject);
      aNode.setRenderProperty(TreeTableDataObjectMutableNode.Render.Invisible);
      aNode.setDisplayName(null);

      modelSupport.firePathChanged(new TreePath(aNode));

      if (aDataObject != null) {
        iterateNode(aNode, new DataObjectListIterator(aDataObject), new HashSet<>());
      } else {
        removeAllChildren(new TreeTableDataObjectMutableNode[]{aNode});
      }
    }

    /**
     * Use the {@code aPossibleChildrenIterator} to check all the candidate child nodes, and update
     * {@code aNode} accordingly
     * @param aNode The node to update. Children will be added/removed in necessary
     * @param aPossibleChildrenIterator The iterator over the possible children
     * @param aSeenUserObjects The user objects which are already seen while creating the tree structure. The {@code ILcdDataModel}
     *                         allows for loops in the structure, which would end in an infinite tree. By keeping
     *                         track of the user objects which were already encountered, we can detect this and
     *                         stop the tree.
     */
    private void iterateNode(TreeTableDataObjectMutableNode aNode, ListIterator<DataObjectNodeContext> aPossibleChildrenIterator, Set<Object> aSeenUserObjects) {
      fStack.push(aNode);
      fDisplayedPropertyCount = 0;

      //update the nodes in the model to reflect the new state of the children
      while (aPossibleChildrenIterator.hasNext()) {
        fPropertyIndex = aPossibleChildrenIterator.nextIndex();
        DataObjectNodeContext candidateChildContext = aPossibleChildrenIterator.next();

        ListIterator<DataObjectNodeContext> iterator;

        if (!fHideNullOrEmptyNodes || isNotEmptyNorNull(candidateChildContext.getObject()) || canBeEdited(aNode, candidateChildContext)) {
          candidateChildContext = calculateNextChildContext(candidateChildContext);

          if (candidateChildContext.getObject() instanceof ILcdDataObject) {
            ILcdDataObject aDataObject = (ILcdDataObject) candidateChildContext.getObject();
            iterator = new DataObjectListIterator(aDataObject);
          } else if (candidateChildContext.getObject() instanceof List) {
            iterator = new DataContextListIterator((List) candidateChildContext.getObject(), candidateChildContext.getDataProperty());
          } else if (candidateChildContext.getObject() instanceof Set) {
            iterator = new DataContextSetListIterator((Set) candidateChildContext.getObject(), candidateChildContext.getDataProperty());
          } else {
            iterator = null;
          }

          //if the property value was itself a ILcdDataObject or List, show the children of these as well, unless
          //we have already shown the children of this object, in which case we stop the iteration to
          //avoid cyclic trees.
          if (iterator != null) {
            if (!aSeenUserObjects.contains(candidateChildContext.getObject())) {
              //record these values, because the call to iterate will modify these fields.
              int old_index = fPropertyIndex;

              TreeTableDataObjectMutableNode created_or_updated_node;
              Set<Object> seenUserObjectsClone = new HashSet<>(aSeenUserObjects);
              seenUserObjectsClone.add(candidateChildContext.getObject());
              created_or_updated_node = updateNode(candidateChildContext, TreeTableDataObjectMutableNode.Render.Invisible);

              int old_displayed_featureindex = fDisplayedPropertyCount;
              try {
                if (created_or_updated_node != null) {
                  iterateNode(created_or_updated_node, iterator, seenUserObjectsClone);
                }
              } finally {
                fPropertyIndex = old_index;
                fDisplayedPropertyCount = old_displayed_featureindex;
                fStack.pop();
              }
            } else {
              TreeTableDataObjectMutableNode updated_or_created_node = updateNode(candidateChildContext, TreeTableDataObjectMutableNode.Render.Recursive);
              removeAllChildren(getPathFromStack(new TreeTableDataObjectMutableNode[]{updated_or_created_node}));
            }
          } else {
            TreeTableDataObjectMutableNode updated_or_created_node =
                updateNode(candidateChildContext, TreeTableDataObjectMutableNode.Render.Normal);
            removeAllChildren(getPathFromStack(new TreeTableDataObjectMutableNode[]{updated_or_created_node}));
          }

        }
      }

      //remove the remaining children
      TreeTableDataObjectMutableNode node = fStack.peek();
      int remaining_child_count = node.getChildCount();
      int nr_of_children_to_delete = remaining_child_count - fDisplayedPropertyCount;
      if (nr_of_children_to_delete > 0) {
        int[] children = new int[nr_of_children_to_delete];
        for (int i = 0; i < children.length; i++) {
          children[i] = fDisplayedPropertyCount + i;
        }
        Object[] child_objects = new Object[nr_of_children_to_delete];
        for (int i = 0; node.getChildCount() > fDisplayedPropertyCount; i++) {
          child_objects[i] = node.getChildAt(fDisplayedPropertyCount);
          node.remove(fDisplayedPropertyCount);
        }
        modelSupport.fireChildrenRemoved(new TreePath(getPathFromStack()), children, child_objects);
      }
    }

    /**
     * Updates the node in the treemodel according to the specified feature.
     *
     * @param aDataObjectContext   The data object context which should be displayed in the tree.
     * @param aShouldRenderFeature Whether or not the value should displayed in the value column of
     *                             the tree-table.
     *
     * @return The mode already present in the treemodel at the treepath of the feature, or a newly
     *         created and inserted node if there was no node present yet.
     */

    private TreeTableDataObjectMutableNode updateNode(DataObjectNodeContext aDataObjectContext, TreeTableDataObjectMutableNode.Render aShouldRenderFeature) {
      TreeTableDataObjectMutableNode node;

      TreeTableDataObjectMutableNode last_node = fStack.peek();
      if (fDisplayedPropertyCount < last_node.getChildCount()) {
        //there already was a node at the specified tree-path, update its values
        node = (TreeTableDataObjectMutableNode) last_node.getChildAt(fDisplayedPropertyCount);
        node.setUserObject(getUserObject(aDataObjectContext));
        node.setDataProperty(aDataObjectContext.getDataProperty());
        node.setAssociationProperty(aDataObjectContext.getAssociationProperty());
        node.setRenderProperty(aShouldRenderFeature);
        node.setDataObjectNodeContext(aDataObjectContext);
        node.setDisplayName(getDisplayName(last_node.getDataObjectNodeContext(), aDataObjectContext, fPropertyIndex));

        Object[] path = getPathFromStack();
        modelSupport.fireChildChanged(new TreePath(path), fDisplayedPropertyCount, node);
        fDisplayedPropertyCount++;
      } else {
        //no node exists yet, create one and add it to the tree.
        node = new TreeTableDataObjectMutableNode(getUserObject(aDataObjectContext),
                                                  aDataObjectContext.getDataProperty(),
                                                  aDataObjectContext.getAssociationProperty(),
                                                  getDisplayName(last_node.getDataObjectNodeContext(), aDataObjectContext, fPropertyIndex),
                                                  aShouldRenderFeature,
                                                  aDataObjectContext
        );
        last_node.add(node);
        Object[] path = getPathFromStack();
        modelSupport.fireChildAdded(new TreePath(path), fDisplayedPropertyCount, node);
        fDisplayedPropertyCount++;
      }
      return node;
    }

    private Object getUserObject(DataObjectNodeContext aDataObjectNodeContext) {
      return aDataObjectNodeContext.getObject();
    }

    //this method removes all the children from the node specified by the path, and fires the correct
    //tree event.
    private void removeAllChildren(TreeTableDataObjectMutableNode[] aTreePath) {
      TreeTableDataObjectMutableNode aNode = aTreePath[aTreePath.length - 1];
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

    private TreeTableDataObjectMutableNode[] getPathFromStack() {
      return getPathFromStack(new TreeTableDataObjectMutableNode[0]);
    }

    private TreeTableDataObjectMutableNode[] getPathFromStack(TreeTableDataObjectMutableNode[] aAppend) {
      TreeTableDataObjectMutableNode[] result = new TreeTableDataObjectMutableNode[fStack.size() + aAppend.length];
      fStack.copyInto(result);
      System.arraycopy(aAppend, 0, result, fStack.size(), aAppend.length);
      return result;
    }

    private String getDisplayName(DataObjectNodeContext aParentNodeContext, DataObjectNodeContext aDataObjectContext, int aIndex) {
      Object aParentUserObject = aParentNodeContext != null ? getUserObject(aParentNodeContext) : null;
      //we distinguish between collections defined on the data model itself, and java.util.Collection entries
      if (collectionEntryFromDataModelCollection(aParentNodeContext, aParentUserObject, aDataObjectContext)) {
        return LIST_ELEMENT_FORMAT.format(new Object[]{aDataObjectContext.getTypeDisplayName(), aIndex + 1});
      }
      if (collectionEntryFromJavaUtilCollectionAndNotDataModelCollection(aParentNodeContext, aParentUserObject, aDataObjectContext)) {
        return JAVA_UTIL_LIST_ELEMENT_FORMAT.format(new Object[]{aDataObjectContext.getTypeDisplayName(), aIndex + 1});
      }
      if (aDataObjectContext.getObject() instanceof List) {
        return LIST_FORMAT.format(new Object[]{aDataObjectContext.getPropertyDisplayName(), "List"});
      } else if (aDataObjectContext.getObject() instanceof Set) {
        return LIST_FORMAT.format(new Object[]{aDataObjectContext.getPropertyDisplayName(), "Set"});
      }
      return aDataObjectContext.getPropertyDisplayName();
    }

    private boolean collectionEntryFromJavaUtilCollectionAndNotDataModelCollection(DataObjectNodeContext aParentNodeContext, Object aParentUserObject, DataObjectNodeContext aDataObjectContext) {
      return !collectionEntryFromDataModelCollection(aParentNodeContext, aParentUserObject, aDataObjectContext) &&
             (aParentUserObject instanceof List || aParentUserObject instanceof Set);
    }

    private boolean collectionEntryFromDataModelCollection(DataObjectNodeContext aParentNodeContext, Object aParentUserObject, DataObjectNodeContext aDataObjectContext) {
      boolean parentUserObjectIsCollection = aParentUserObject instanceof List || aParentUserObject instanceof Set;
      boolean propertyHasCollectionType = aDataObjectContext.getDataProperty() != null &&
                                          (aDataObjectContext.getDataProperty().getCollectionType() == TLcdDataProperty.CollectionType.LIST ||
                                           aDataObjectContext.getDataProperty().getCollectionType() == TLcdDataProperty.CollectionType.SET);
      boolean associatedPropertyOfParentHasCollectionType =
          fHideNullOrEmptyNodes && //this check is only relevant when the associated properties are resolved
          aParentNodeContext != null &&
          aParentNodeContext.getAssociationProperty() != null &&
          (aParentNodeContext.getAssociationProperty().getCollectionType() == TLcdDataProperty.CollectionType.LIST ||
           aParentNodeContext.getAssociationProperty().getCollectionType() == TLcdDataProperty.CollectionType.SET);
      return parentUserObjectIsCollection &&
             (propertyHasCollectionType || associatedPropertyOfParentHasCollectionType);
    }

    /**
     * This method checks whether the property contained in {@code aCurrentNextChildContext} contains a
     * {@link TLcdAssociationClassAnnotation}. If it does, the returned {@code DataObjectNodeContext}
     * will contain the associated property if {@link #fHideNullOrEmptyNodes} is set to
     * {@code true}
     *
     * @param aCurrentNextChildContext The current next child object
     *
     * @return Either an updated version of the next object, or <code>aCurrentNextChildContext</code>
     * @see #fHideNullOrEmptyNodes
     */
    private DataObjectNodeContext calculateNextChildContext(DataObjectNodeContext aCurrentNextChildContext) {
      if (!fHideNullOrEmptyNodes) {
        //when no filtering is needed, just return the same context
        return aCurrentNextChildContext;
      }
      if (aCurrentNextChildContext.getDataProperty().getType().isAnnotationPresent(TLcdAssociationClassAnnotation.class)) {
        TLcdDataProperty currentProperty = aCurrentNextChildContext.getDataProperty();
        TLcdAssociationClassAnnotation annotation = currentProperty.getType().getAnnotation(TLcdAssociationClassAnnotation.class);
        final TLcdDataProperty roleProperty = annotation.getRoleProperty();

        final ILcdMorphingFunction<Object, Object> convertToAssociatedPropertyValueFunction = new ILcdMorphingFunction<Object, Object>() {
          @Override
          public Object morph(Object aOriginalObject) {
            if (aOriginalObject instanceof ILcdDataObject) {
              return ((ILcdDataObject) aOriginalObject).getValue(roleProperty);
            }
            return aOriginalObject;
          }
        };

        Object currentNextChildContextObject = aCurrentNextChildContext.getObject();
        if (currentNextChildContextObject instanceof ILcdDataObject) {
          Object newObject = convertToAssociatedPropertyValueFunction.morph(currentNextChildContextObject);
          return new DataObjectNodeContext(newObject, roleProperty.getType(), roleProperty, currentProperty.getDisplayName(), currentProperty);
        } else if (currentProperty.getCollectionType() == TLcdDataProperty.CollectionType.LIST &&
                   currentNextChildContextObject instanceof List) {
          List<Object> newList = new ArrayList<>();
          for (Object userObject : ((List) currentNextChildContextObject)) {
            newList.add(convertToAssociatedPropertyValueFunction.morph(userObject));
          }
          return new DataObjectNodeContext(newList, roleProperty.getType(), roleProperty, currentProperty.getDisplayName(), currentProperty);
        } else if (currentProperty.getCollectionType() == TLcdDataProperty.CollectionType.SET &&
                   currentNextChildContextObject instanceof Set) {
          HashSet<Object> newSet = new HashSet<>();
          for (Object userObject : ((Set) currentNextChildContextObject)) {
            newSet.add(convertToAssociatedPropertyValueFunction.morph(userObject));
          }
          return new DataObjectNodeContext(newSet, roleProperty.getType(), roleProperty, currentProperty.getDisplayName(), currentProperty);
        }
        return aCurrentNextChildContext;
      }
      return aCurrentNextChildContext;
    }
  }

  /*
   * Show cells that can be edited, even if they are initially null or empty.
   */
  private boolean canBeEdited(TreeTableDataObjectMutableNode aNode,
                              DataObjectNodeContext aChildCandidateDataContext) {
    if (aNode.getDataProperty() != null &&
        aNode.getDataProperty().equals(aChildCandidateDataContext.getDataProperty())) {
      //Node in collection
      return false;
    }

    TLcdDataProperty[] parentProperties;
    if (aNode == getRoot()) {
      parentProperties = new TLcdDataProperty[0];
    } else {
      parentProperties = getDataProperties(aNode);
    }
    if (parentProperties == null) {
      return false;
    }
    TLcdDataProperty[] dataProperties = new TLcdDataProperty[parentProperties.length + 1];
    System.arraycopy(parentProperties, 0, dataProperties, 0, parentProperties.length);
    dataProperties[dataProperties.length - 1] = aChildCandidateDataContext.getDataProperty();
    return CustomizerUtility.canCreateCustomizerPanel(fDomainObjectContext,
                                                      dataProperties,
                                                      fLucyEnv);
  }

  /**
   * Iterator implementation that iterates of the features of an ILcdDataObject. This is implemented
   * to have uniform iterating capabilities over an ILcdDataObject as over a Collection.
   */
  private static class DataObjectListIterator implements ListIterator<DataObjectNodeContext> {
    private final ILcdDataObject fDataObject;
    private int fIndex = 0;

    public DataObjectListIterator(ILcdDataObject aDataObject) {
      fDataObject = aDataObject;
    }

    @Override
    public boolean hasNext() {
      return fIndex < fDataObject.getDataType().getProperties().size();
    }

    @Override
    public DataObjectNodeContext next() {
      TLcdDataProperty property = fDataObject.getDataType().getProperties().get(fIndex++);
      return new DataObjectNodeContext(fDataObject.getValue(property), property.getType(), property); //post-increment
    }

    @Override
    public int nextIndex() {
      return fIndex;
    }

    //the remaining methods are not implemented as they are not used
    @Override
    public void add(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DataObjectNodeContext previous() {
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
    public void set(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A list iterator that wraps all its elements in a DataObjectContext for a Set.
   */
  private static class DataContextSetListIterator implements ListIterator<DataObjectNodeContext> {
    private Iterator fIterator;
    private int fIndex;
    private TLcdDataProperty fDataProperty;

    public DataContextSetListIterator(Set aSet, TLcdDataProperty aDataProperty) {
      fDataProperty = aDataProperty;
      fIterator = aSet.iterator();
    }

    @Override
    public boolean hasNext() {
      return fIterator.hasNext();
    }

    @Override
    public DataObjectNodeContext next() {
      fIndex++;
      return new DataObjectNodeContext(fIterator.next(), fDataProperty.getType(), fDataProperty);
    }

    @Override
    public int nextIndex() {
      return fIndex;
    }

    @Override
    public void add(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DataObjectNodeContext previous() {
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
    public void set(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * A list iterator that wraps all its elements in a DataObjectContext
   */
  private static class DataContextListIterator implements ListIterator<DataObjectNodeContext> {
    private ListIterator fDelegateIterator;
    private TLcdDataProperty fDataProperty;

    public DataContextListIterator(List aList, TLcdDataProperty aDataProperty) {
      fDataProperty = aDataProperty;
      fDelegateIterator = aList.listIterator();
    }

    @Override
    public boolean hasNext() {
      return fDelegateIterator.hasNext();
    }

    @Override
    public DataObjectNodeContext next() {
      return new DataObjectNodeContext(fDelegateIterator.next(), fDataProperty.getType(), fDataProperty);
    }

    @Override
    public int nextIndex() {
      return fDelegateIterator.nextIndex();
    }

    @Override
    public void add(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DataObjectNodeContext previous() {
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
    public void set(DataObjectNodeContext e) {
      throw new UnsupportedOperationException();
    }
  }

  private static class ModelListener extends ALcdWeakModelListener<TreeTableDataObjectModel> {

    private ModelListener(TreeTableDataObjectModel aTreeTableDataObjectModel) {
      super(aTreeTableDataObjectModel);
    }

    @Override
    protected void modelChangedImpl(TreeTableDataObjectModel aToModify, TLcdModelChangedEvent aModelChangedEvent) {
      aToModify.updateModel();
    }
  }
}
