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

import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.shape.TLcdShapeAnnotation;

/**
 * <p>This <code>TreeModel</code> can be applied on any <code>JTree</code> in order for it to represent
 * the information in a <code>TLcdDataModel</code>.</p>
 *
 * <p>Note that a <code>TLcdDataModel</code> can contain an infinite loop when a non-primitive
 * <code>TLcdDataType</code> contains a <code>TLcdDataProperty</code> that in turn (directly
 * or indirectly) contains that same <code>TLcdDataType</code>. This data model has a safe-guard
 * for such events, so no infinite recursions can occur in the tree.</p>
 */
public class DataModelTreeModel extends DefaultTreeModel {
  private ILcdDataModelDescriptor fDataModelDescriptor;

  /**
   * Creates a data model <code>TreeModel</code> given an {@link ILcdDataModelDescriptor}.
   * @param aDataModelDescriptor a <code>TLcdDataModelDescriptor</code>
   */
  public DataModelTreeModel(ILcdDataModelDescriptor aDataModelDescriptor) {
    this(aDataModelDescriptor, false);
    fDataModelDescriptor = aDataModelDescriptor;
  }

  /**
   * Creates a data model <code>TreeModel</code> given an {@link ILcdDataModelDescriptor}.
   * @param aDataModelDescriptor a <code>TLcdDataModelDescriptor</code>
   * @param aDisplayOnlyModelElementTypesInRoot This mode will ensure only model element data types that are present in the
   * given <code>ILcdDataModelDescriptor</code> are visible in the root data model node. Other data types will only
   * be visible if they are declared inside the model element data types themselves.
   */
  public DataModelTreeModel(ILcdDataModelDescriptor aDataModelDescriptor, boolean aDisplayOnlyModelElementTypesInRoot) {
    super(new DataModelNode(aDataModelDescriptor.getDataModel(), new HashSet<TLcdDataType>(), aDisplayOnlyModelElementTypesInRoot, aDataModelDescriptor.getModelElementTypes()));
    fDataModelDescriptor = aDataModelDescriptor;
  }

  /**
   * Returns the <code>ILcdDataModelDescriptor</code> represented byt this <code>DataModelTreeModel</code>.</p>
   * If no <code>ILcdDataModelDescriptor</code> was given at construction, this method will return an
   * <code>ILcdDataModelDescriptor</code> that only contains a <code>TLcdDataModel</code>, and null
   * for everything else.
   * @return the <code>ILcdDataModelDescriptor</code> used by this <code>TreeModel</code>
   */
  public ILcdDataModelDescriptor getDataModelDescriptor() {
    return fDataModelDescriptor;
  }

  /**
   * A class to represent <code>TLcdDataModel</code> nodes.
   */
  public static class DataModelNode extends DefaultMutableTreeNode {

    /**
     * Creates a new <code>DataModelNode</code>
     * @param aDataModel a data model to base this node on
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     */
    public DataModelNode(TLcdDataModel aDataModel, HashSet<TLcdDataType> aSeenDataTypes) {
      this(aDataModel, aSeenDataTypes, false, null);
    }

    /**
     * Creates a new <code>DataModelNode</code>
     * @param aDataModel a data model to base this node on
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     * @param aDisplayOnlyModelElementTypesInRoot  his mode will ensure only model element data types that are present in the
     * given <code>ILcdDataModelDescriptor</code> are visible in the root data model node. Other data types will only
     * be visible if they are declared inside the model element data types themselves.
     * @param aModelElementTypes A list of model element data types
     */
    public DataModelNode(TLcdDataModel aDataModel, HashSet<TLcdDataType> aSeenDataTypes, boolean aDisplayOnlyModelElementTypesInRoot, Set<TLcdDataType> aModelElementTypes) {
      super(aDataModel);
      initializeChildren(aDataModel, aSeenDataTypes, aDisplayOnlyModelElementTypesInRoot, aModelElementTypes);
    }

    private void initializeChildren(TLcdDataModel aDataModel, HashSet<TLcdDataType> aSeenDataTypes, boolean aDisplayOnlyModelElementTypesInRoot, Set<TLcdDataType> aModelElementTypes) {
      for (TLcdDataType dataType : aDataModel.getDeclaredTypes()) {
        if (!aSeenDataTypes.contains(dataType)) {
          //if a new data type has been found, clone the original list and add the found data type
          //to the clone. This way cyclic dependencies are eliminated for each branch, without
          //interfering other tree branches.
          if (!aDisplayOnlyModelElementTypesInRoot || (aModelElementTypes != null && aModelElementTypes.contains(dataType))) {
            HashSet<TLcdDataType> aSeenDataTypesClone = new HashSet<TLcdDataType>(aSeenDataTypes);
            aSeenDataTypesClone.add(dataType);
            add(new DataTypeNode(dataType, true, aSeenDataTypesClone));
          }
        }
      }
      if (aDataModel.getDeclaredDependencies() != null && aDataModel.getDeclaredDependencies().size() > 0) {
        add(new DependentDataModelNode(aDataModel.getDeclaredDependencies(), aSeenDataTypes, aDisplayOnlyModelElementTypesInRoot, aModelElementTypes));
      }
    }
  }

  /**
   * A node that holds a list of <code>DataModel</code> that are dependent on
   * another <code>DataModel</code>.
   */
  private static class DependentDataModelNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>DependentDataModelNode</code>
     *
     * @param aDeclaredDependencies a list of data models to base this node on
     * @param aSeenDataTypes        A list of <code>TLcdDataType</code>s that should be skipped
     *                              to avoid cyclic recursion.
     * @param aDisplayOnlyModelElementTypesInRoot
     *                              his mode will ensure only model element data types that are present in the
     *                              given <code>ILcdDataModelDescriptor</code> are visible in the root data model node. Other data types will only
     *                              be visible if they are declared inside the model element data types themselves.
     * @param aModelElementTypes    A list of model element data types
     */
    public DependentDataModelNode(Set<TLcdDataModel> aDeclaredDependencies, HashSet<TLcdDataType> aSeenDataTypes, boolean aDisplayOnlyModelElementTypesInRoot, Set<TLcdDataType> aModelElementTypes) {
      super(aDeclaredDependencies);
      initializeChildren(aDeclaredDependencies, aSeenDataTypes, aDisplayOnlyModelElementTypesInRoot, aModelElementTypes);
    }

    private void initializeChildren(Set<TLcdDataModel> aDeclaredDependencies, HashSet<TLcdDataType> aSeenDataTypes, boolean aDisplayOnlyModelElementTypesInRoot, Set<TLcdDataType> aModelElementTypes) {
      for (TLcdDataModel declaredDependency : aDeclaredDependencies) {
        add(new DataModelNode(declaredDependency, aSeenDataTypes, aDisplayOnlyModelElementTypesInRoot, aModelElementTypes));
      }
    }

    @Override
    public String toString() {
      return "[Dependent Data Models]";
    }
  }

  /**
   * A class to represent <code>TLcdDataType</code> nodes.
   */
  public static class DataTypeNode extends DefaultMutableTreeNode {
    private TLcdDataType fType;

    /**
     * Creates a new <code>DataTypeNode</code>
     * @param aType a data type to base this node on
     * @param aIncludeSuper Whether or not to include the super type in this data type node.
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     *                       to avoid cyclic recursion.
     */
    public DataTypeNode(TLcdDataType aType, boolean aIncludeSuper, HashSet<TLcdDataType> aSeenDataTypes) {
      super(aType);
      fType = aType;
      initChildren(aType, aIncludeSuper, aSeenDataTypes);
    }

    public TLcdDataType getType() {
      return fType;
    }

    private void initChildren(TLcdDataType aType, boolean aIncludeSuper, HashSet<TLcdDataType> aSeenDataTypes) {
      add(new DefaultMutableTreeNode("Display name: " + aType.getDisplayName()));
      add(new DefaultMutableTreeNode("Abstract: " + aType.isAbstract()));
      add(new DefaultMutableTreeNode("Primitive: " + aType.isPrimitive()));
      add(new DefaultMutableTreeNode("Instance class: " + aType.getInstanceClass()));
      if (aIncludeSuper &&
          aType.getSuperType() != null &&
          aType.getSuperType() != TLcdCoreDataTypes.OBJECT_TYPE &&
          aType.getSuperType() != TLcdCoreDataTypes.DATA_OBJECT_TYPE) {
        add(new SuperTypeNode(aType, aSeenDataTypes));
      }
      if (aType.isEnumeration()) {
        add(new EnumNode(aType));
      }
      if (aType.getAnnotation(TLcdShapeAnnotation.class) != null) {
        add(new ShapeNode(aType));
      }
      if (aType.getProperties().size() > 0) {
        add(new DataPropertiesNode(aType, aSeenDataTypes));
      }
    }

    @Override
    public String toString() {
      TLcdDataType type = (TLcdDataType) getUserObject();
      return type.getName();
    }
  }

  /**
   * A class to represent the super type of a <code>TLcdDataType</code>
   */
  public static class SuperTypeNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>SuperTypeNode</code>
     * @param aDataType a data type to base this node on
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     *                       to avoid cyclic recursion.
     */
    public SuperTypeNode(TLcdDataType aDataType, HashSet<TLcdDataType> aSeenDataTypes) {
      super(aDataType);
      initChildren(aDataType, aSeenDataTypes);
    }

    private void initChildren(TLcdDataType aType, HashSet<TLcdDataType> aSeenDataTypes) {
      //We don't include the super data-type for a data type that is part of a different super-node,
      //to avoid infinite recursions
      add(new DataTypeNode(aType.getSuperType(), false, aSeenDataTypes));
    }

    @Override
    public String toString() {
      return "Super type";
    }
  }

  /**
   * A class to represent enumeration nodes.
   */
  public static class EnumNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>EnumNode</code>
     * @param aDataType a data type to base this node on
     */
    public EnumNode(TLcdDataType aDataType) {
      super(aDataType);
      initChildren(aDataType);
    }

    private void initChildren(TLcdDataType aType) {
      for (Object value : aType.getPossibleValues()) {
        add(new DefaultMutableTreeNode(value + ": " + aType.getDisplayName(value)));
      }
    }

    @Override
    public String toString() {
      return "Enumeration values";
    }

  }

  /**
   * A class to represent shape nodes.
   */
  public static class ShapeNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>ShapeNode</code>
     * @param aDataType a data type to base this node on
     */
    public ShapeNode(TLcdDataType aDataType) {
      super(aDataType);
      initChildren(aDataType);
    }

    private void initChildren(TLcdDataType aType) {
      TLcdShapeAnnotation annotation = aType.getAnnotation(TLcdShapeAnnotation.class);
      for (Class clazz : annotation.getShapeClasses()) {
        add(new DefaultMutableTreeNode(clazz.getSimpleName()));
      }
      if (!annotation.isShapeMandatory()) {
        add(new DefaultMutableTreeNode("NONE "));
      }
    }

    @Override
    public String toString() {
      return "Shapes";
    }

  }

  /**
   * A class to represent a list of <code>TLcdDataProperty</code> nodes.
   */
  public static class DataPropertiesNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>DataPropertiesNode</code>
     * @param aDataType a data type to base this node on
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     *                       to avoid cyclic recursion.
     */
    public DataPropertiesNode(TLcdDataType aDataType, HashSet<TLcdDataType> aSeenDataTypes) {
      super(aDataType);
      initChildren(aDataType, aSeenDataTypes);
    }

    private void initChildren(TLcdDataType aType, HashSet<TLcdDataType> aSeenDataTypes) {
      for (TLcdDataProperty p : aType.getDeclaredProperties()) {
        add(new PropertyNode(p, aSeenDataTypes));
      }
    }

    @Override
    public String toString() {
      return "Properties";
    }

  }

  /**
   * A class to represent a single <code>TLcdDataProperty</code> node.
   */
  public static class PropertyNode extends DefaultMutableTreeNode {
    /**
     * Creates a new <code>PropertyNode</code>
     * @param aProperty a data property to base this node on
     * @param aSeenDataTypes A list of <code>TLcdDataType</code>s that should be skipped
     *                       to avoid cyclic recursion.
     */
    public PropertyNode(TLcdDataProperty aProperty, HashSet<TLcdDataType> aSeenDataTypes) {
      super(aProperty);
      initChildren(aProperty, aSeenDataTypes);
    }

    private void initChildren(TLcdDataProperty aProperty, HashSet<TLcdDataType> aSeenDataTypes) {
      TLcdDataType dataType = aProperty.getType();
      if (!aSeenDataTypes.contains(dataType)) {
        //if a new data type has been found, clone the original list and add the found data type
        //to the clone. This way cyclic dependencies are eliminated for each branch, without
        //interfering other tree branches.
        HashSet<TLcdDataType> aSeenDataTypesClone = new HashSet<TLcdDataType>(aSeenDataTypes);
        aSeenDataTypesClone.add(dataType);
        add(new DataTypeNode(dataType, false, aSeenDataTypesClone));
      }
    }

    @Override
    public String toString() {
      TLcdDataProperty property = (TLcdDataProperty) getUserObject();
      String result = property.getName() + " (" + property.getDisplayName() + ") : " + property.getType().getName();
      if (property.getCollectionType() != null) {
        switch (property.getCollectionType()) {
        case LIST:
          result = "[list] " + result;
          break;
        case SET:
          result = "[set] " + result;
          break;
        case MAP:
          result = "[map] " + result;
          break;
        }
      }
      return result;
    }
  }

}
