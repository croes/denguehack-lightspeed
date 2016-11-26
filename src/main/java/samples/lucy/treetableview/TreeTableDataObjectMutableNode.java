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

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A tree-table node that holds the dataobject and its meta-data
 */
class TreeTableDataObjectMutableNode extends AbstractTreeTableNode {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(TreeTableDataObjectMutableNode.class);

  private TLcdDataProperty fDataProperty;
  private TLcdDataProperty fAssociationProperty;
  private String fDisplayName;
  private Render fRenderProperty;
  private DataObjectNodeContext fDataObjectNodeContext;

  public enum Render {
    Normal,
    Invisible,
    Recursive
  }

  TreeTableDataObjectMutableNode(Object aUserObject,
                                 TLcdDataProperty aDataProperty,
                                 TLcdDataProperty aAssociationProperty,
                                 String aDisplayName,
                                 Render aShouldRenderProperty,
                                 DataObjectNodeContext aDataObjectNodeContext) {
    super(aUserObject);
    fDataProperty = aDataProperty;
    fAssociationProperty = aAssociationProperty;
    fDisplayName = aDisplayName;
    fRenderProperty = aShouldRenderProperty;
    fDataObjectNodeContext = aDataObjectNodeContext;
  }

  @Override
  public Class<?> getObjectClass() {
    return fDataProperty.getType().getInstanceClass();
  }

  @Override
  public Object getValueAt(int aColumn) {
    if (aColumn == 1) {
      return calculateUserObject();
    } else {
      return fDisplayName;
    }
  }

  private Object calculateUserObject() {
    if (fDataProperty != null && fDataProperty.getType() != null) {
      TLcdDataType type = fDataProperty.getType();
      if (type.isEnumeration() && getRenderProperty() != Render.Invisible) {
        //use the display name for an enumeration
        Object userObject = getUserObject();
        if (userObject == null && fDataProperty.isNullable()) {
          return userObject;
        }
        try {
          return type.getDisplayName(userObject);
        } catch (IllegalArgumentException e) {
          // There are certain data formats (e.g. KML) where they put values in the data model
          // which are not allowed according to type#getPossibleValues()
          // The getDisplayName method will throw an IllegalArgumentException in that case
          // Fall back to the default behavior in that case
          LOGGER.warn("Encountered an invalid value in the data model", e);
          return getUserObject();
        }
      }
    }
    return getUserObject();
  }

  Render getRenderProperty() {
    return fRenderProperty;
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  public TLcdDataProperty getDataProperty() {
    return fDataProperty;
  }

  public void setDataProperty(TLcdDataProperty aDataProperty) {
    fDataProperty = aDataProperty;
  }

  public TLcdDataProperty getAssociationProperty() {
    return fAssociationProperty;
  }

  public void setAssociationProperty(TLcdDataProperty aAssociationProperty) {
    fAssociationProperty = aAssociationProperty;
  }

  public void setDisplayName(String aDisplayName) {
    fDisplayName = aDisplayName;
  }

  public void setRenderProperty(Render aRenderProperty) {
    fRenderProperty = aRenderProperty;
  }

  public void removeAllChildren() {
    int childCount = getChildCount();
    for (int i = childCount - 1; i >= 0; i--) {
      remove(i);
    }
  }

  @Override
  public void setUserObject(Object object) {
    super.setUserObject(convertUserObject(object));
  }

  DataObjectNodeContext getDataObjectNodeContext() {
    return fDataObjectNodeContext;
  }

  void setDataObjectNodeContext(DataObjectNodeContext aDataObjectNodeContext) {
    fDataObjectNodeContext = aDataObjectNodeContext;
  }
}
