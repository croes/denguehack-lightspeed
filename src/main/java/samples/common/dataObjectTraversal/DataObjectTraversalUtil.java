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
package samples.common.dataObjectTraversal;

import java.util.Collection;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;

/**
 * This utility class illustrates how a data object can be traversed in a generic way, taking into
 * account property types, collections, null values, ...
 */
public class DataObjectTraversalUtil {

  public static void traverseDataObject(ILcdDataObject aObject) {
    for (TLcdDataProperty property : aObject.getDataType().getProperties()) {
      Object value = aObject.getValue(property);
      if (value != null) {
        if (property.getCollectionType() == null) {
          // Single-valued property
          traverseChild(property.getType(), value);
        } else {
          switch (property.getCollectionType()) {
          case MAP:
            Map<Object, Object> map = (Map<Object, Object>) value;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
              traverseChild(property.getMapKeyType(), entry.getKey());
              traverseChild(property.getType(), entry.getValue());
            }
            break;
          case LIST:
          case SET:
            for (Object element : (Collection<?>) value) {
              traverseChild(property.getType(), element);
            }
            break;
          }
        }
      }
    }
  }

  public static void traverseChild(TLcdDataType aType, Object aObject) {
    if (aType.isPrimitive()) {
      traversePrimitive(aType, aObject);
    } else if (aType.isDataObjectType()) {
      traverseDataObject((ILcdDataObject) aObject);
    } else {
      traverseObject(aObject);
    }
  }

  public static void traverseObject(Object aObject) {
    if (aObject instanceof Map<?, ?>) {
      for (Map.Entry entry : ((Map<?, ?>) aObject).entrySet()) {
        // ...
      }
    } else if (aObject instanceof Collection<?>) {
      for (Object element : (Collection<?>) aObject) {
        traverseObject(element);
      }
    } else if (aObject instanceof ILcdDataObject) {
      traverseDataObject((ILcdDataObject) aObject);
    } else {
      traversePrimitive(null, aObject);
    }
  }

  public static void traversePrimitive(TLcdDataType aType, Object aValue) {
    // ...
  }

}
