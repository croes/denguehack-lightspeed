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

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;

/**
 * <p>Each element in the {@link DataObjectTreeModel} must be an instance of this class. This class
 * represents a container containing all the relevant information of the {@code TLcdDataModel} and
 * {@code ILcdDataObject} for the visualization in the {@code JTree}.</p>
 * <p/>
 * <p>A typical rendering in the tree would be composed out of {@link #getName()} (e.g. the property
 * name) and a rendering of {@link #getValue()} (e.g. the actual value of that property).</p>
 */
public class DataObjectTreeNode {

  /**
   * The object to render.
   */
  private final Object fObject;
  /**
   * The property that was used to retrieve the object. Can be null if object was based on list.
   */
  private final TLcdDataProperty fProperty;
  /**
   * The parent holder of the object to be rendered.
   */
  private final DataObjectTreeNode fParentHolder;
  /**
   * Index of the object with respective to its parent.
   */
  private final int fIndex;
  /**
   * The name of the object. This does not include the value.
   */
  private String fName;

  /**
   * Creates a node representing the root
   *
   * @param aRoot The root object
   */
  public DataObjectTreeNode(Object aRoot) {
    this(aRoot, null, null, -1);
  }

  /**
   * Creates a node representing a certain {@code TLcdDataProperty}
   *
   * @param aParentNode The parent node
   * @param aProperty   The property represented by this node
   */
  public DataObjectTreeNode(DataObjectTreeNode aParentNode, TLcdDataProperty aProperty, int aPropertyIndex) {
    this(null, aParentNode, aProperty, aPropertyIndex);
  }

  /**
   * Creates a node representing an entry in a Collection (see {@link
   * TLcdDataProperty#getCollectionType()})
   *
   * @param aParentNode The parent node
   * @param aIndex      The index in the collection
   */
  public DataObjectTreeNode(DataObjectTreeNode aParentNode, int aIndex) {
    this(null, aParentNode, null, aIndex);
  }

  private DataObjectTreeNode(Object aObject, DataObjectTreeNode aParentNode, TLcdDataProperty aProperty, int aIndex) {
    fProperty = aProperty;
    fObject = aObject;
    fParentHolder = aParentNode;
    fIndex = aIndex;
  }

  /**
   * Returns the parent node, or {@code null} when this node is the root node
   *
   * @return the parent node
   */
  public DataObjectTreeNode getParentNode() {
    return fParentHolder;
  }

  /**
   * Returns the property represented by this node. Can be {@code null} in case of the root node or
   * when this node represents an entry in a {@code Collection}
   *
   * @return the property represented by this node
   */
  public TLcdDataProperty getProperty() {
    return fProperty;
  }

  /**
   * Returns the value to render. A typical representation in the tree would consist out of {@link
   * #getName()} and {@code #getValue()}
   *
   * @return the value to render
   */
  public Object getValue() {
    if (fParentHolder == null) {
      return fObject;
    }
    Object parentObject = fParentHolder.getValue();
    if (parentObject instanceof ILcdDataObject) {
      ILcdDataObject parent = (ILcdDataObject) parentObject;
      return parent.getValue(fProperty);
    }
    if (parentObject instanceof List<?>) {
      List<?> list = (List<?>) parentObject;
      return list.get(fIndex);
    }
    return null;
  }

  /**
   * Returns the name of the property. A typical representation in the tree would consist out of
   * {@code #getName()} and {@link #getValue()}
   *
   * @return the name
   */
  public String getName() {
    if (fName == null) {
      fName = calculateName();
    }
    return fName;
  }

  @Override
  public String toString() {
    String result = getName();
    if (result.length() > 0) {
      result += ": ";
    }
    Object o = getValue();
    if (o != null && !(o instanceof ILcdDataObject) && !(o instanceof List<?>)) {
      result += o.toString();
    }
    if (o instanceof ILcdDataObject) {
      result += ((ILcdDataObject) o).getDataType().getName();
    }
    return result;
  }

  private String calculateName() {
    if (fParentHolder == null) {
      return "";
    }
    Object parent = fParentHolder.getValue();
    if (parent == null) {
      return "";
    }
    if (fProperty != null) {
      return fProperty.getDisplayName() != null ? fProperty.getDisplayName() : fProperty.getName();
    } else if (fIndex != -1) {
      return Integer.toString(fIndex);
    }
    return "";
  }

  public int getIndex() {
    return fIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DataObjectTreeNode that = (DataObjectTreeNode) o;

    if (fIndex != that.fIndex) {
      return false;
    }
    if (fObject != null ? !fObject.equals(that.fObject) : that.fObject != null) {
      return false;
    }
    if (fParentHolder != null ? !fParentHolder.equals(that.fParentHolder) : that.fParentHolder != null) {
      return false;
    }
    if (fProperty != null ? !fProperty.equals(that.fProperty) : that.fProperty != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = fObject != null ? fObject.hashCode() : 0;
    result = 31 * result + (fProperty != null ? fProperty.hashCode() : 0);
    result = 31 * result + (fParentHolder != null ? fParentHolder.hashCode() : 0);
    result = 31 * result + fIndex;
    return result;
  }
}
