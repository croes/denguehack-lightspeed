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
package samples.common.dataModelViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.luciad.datamodel.TLcdDataProperty;

import samples.common.dataModelDisplayTree.DataModelTreeModel;

/**
 * Generates a <code>String</code> that can be used to retrieve a property, using {@linkplain
 * com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage#compile(String)}
 *
 * @see com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage
 * @see com.luciad.datamodel.expression.TLcdDataObjectExpressionContext
 * @see com.luciad.datamodel.expression.ILcdDataObjectExpression
 */
public class PropertyStringGenerator {
  /**
   * Given a <code>TreePath</code> that is created exclusively with nodes found in <code>DataModelTreeModel</code>, this
   * method creates a String representation of the sequence of properties to use as input in a data object expression
   * language parser.
   *
   * @param aTreePath a <code>TreePath</code>
   *
   * @return a string suitable for a data object expression language
   */
  public String generateString(TreePath aTreePath) {
    Object[] callTree = generateCallTree(aTreePath.getPath());
    try {
      return generateStringFromCallTree(callTree);
    } catch (IllegalArgumentException e) {
      return "Selected Property is not valid.";
    }
  }

  /**
   * Based on a call tree, generate a string suitable for an expression language.
   *
   * @param aCallTree an array of <code>TLcdDataProperty</code> instances
   *
   * @return a string suitable for a data object expression language
   */
  private String generateStringFromCallTree(Object[] aCallTree) {
    String generationString = "";
    for (int i = 0; i < aCallTree.length; i++) {
      if (i != 0) {
        generationString += ".";
      }
      if (aCallTree[i] instanceof TLcdDataProperty) {
        generationString += "" + ((TLcdDataProperty) aCallTree[i]).getName() + "";
      } else {
        throw new IllegalArgumentException("Illegal argument in call tree: " + aCallTree[i]);
      }
    }
    return generationString;
  }

  /**
   * Given a list of tree nodes, generates a list of <code>TLcdDataProperty</code> instances that should be followed in
   * sequence to obtain value of the property that is needed. This method is recursive and will only produce valid
   * result for
   *
   * @param aTreePath a list of tree nodes
   *
   * @return a list of <code>TLcdDataProperty</code>
   */
  private Object[] generateCallTree(Object[] aTreePath) {
    return generateCallTree(aTreePath, aTreePath.length - 1);
  }

  /**
   * A method recursive method for finding the <code>TLcdDataProperty</code> instances in a list of tree nodes.
   *
   * @param aList               a list of tree nodes
   * @param aObjectToCheckIndex an integer used as a stop condition for the recursion
   *
   * @return a list of <code>Object</code> instances
   */
  private Object[] generateCallTree(Object[] aList, int aObjectToCheckIndex) {
    if (aObjectToCheckIndex < 0) {
      return aList;
    }
    Object objectToCheck = aList[aObjectToCheckIndex];
    if (objectToCheck instanceof DataModelTreeModel.PropertyNode) {
      aList[aObjectToCheckIndex] = ((DefaultMutableTreeNode) objectToCheck).getUserObject();
      return generateCallTree(aList, aObjectToCheckIndex - 1);
    } else if (objectToCheck instanceof DataModelTreeModel.DataPropertiesNode ||
               objectToCheck instanceof DataModelTreeModel.DataTypeNode ||
               objectToCheck instanceof DataModelTreeModel.DataModelNode ||
               objectToCheck instanceof DataModelTreeModel.SuperTypeNode) {
      List<Object> objects = Arrays.asList(aList);
      ArrayList<Object> objectsList = new ArrayList<Object>(objects);
      objectsList.remove(aObjectToCheckIndex);
      return generateCallTree(objectsList.toArray(), aObjectToCheckIndex - 1);
    }
    throw new IllegalArgumentException("Given argument is invalid");
  }
}
