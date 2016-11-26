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
package samples.ogc.wfs.server;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.database.TLcdPrimaryKeyAnnotation;
import com.luciad.format.gml2.model.TLcdGML2AbstractFeature;
import com.luciad.format.gml31.model.TLcdGML31AbstractFeature;
import com.luciad.format.gml32.model.TLcdGML32AbstractFeature;
import com.luciad.ogc.filter.ILcdOGCFeatureIDRetriever;

/**
 * An example implementation of a feature ID retriever with support for <code>ILcdDataObject</code>.
 * If an object is a GML feature or a data object with a primary key annotation,
 * the corresponding id / key is used. In all other cases,  the first two declared
 * properties of the <code>ILcdDataObject</code> object are used as id. Note that this approach does
 * not guarantee uniqueness; preferably, knowledge of the data is used in this case to return a suitable and
 * unique property value.
 */
class WFSFeatureIDRetriever implements ILcdOGCFeatureIDRetriever {

  public String retrieveFeatureID(Object aObject) {
    // If the object is a GML feature, use its GML id.
    if (aObject instanceof TLcdGML2AbstractFeature) {
      final String fid = ((TLcdGML2AbstractFeature) aObject).getFid();
      return fid == null ? "" : fid;
    } else if (aObject instanceof TLcdGML31AbstractFeature) {
      final String id = ((TLcdGML31AbstractFeature) aObject).getId();
      return id == null ? "" : id;

    } else if (aObject instanceof TLcdGML32AbstractFeature) {
      final String id = ((TLcdGML32AbstractFeature) aObject).getId();
      return id == null ? "" : id;

    }

    // If the object is an ILcdDataObject, we check whether its data type has
    // a primary key annotation; if this is the case, we use the referred property
    // as id. If there is no primary key annotation, we use the combination of the
    // first two properties as id.
    if (aObject instanceof ILcdDataObject) {
      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      TLcdPrimaryKeyAnnotation primaryKeyAnnotation = dataObject.getDataType().getAnnotation(TLcdPrimaryKeyAnnotation.class);
      if (primaryKeyAnnotation != null) {
        return String.valueOf(dataObject.getValue(primaryKeyAnnotation.getProperty()));
      } else if (dataObject.getDataType().getDeclaredProperties().size() > 1) {
        Object value1 = dataObject.getValue(dataObject.getDataType().getDeclaredProperties().get(0));
        Object value2 = dataObject.getValue(dataObject.getDataType().getDeclaredProperties().get(1));
        //No properties -> cannot generate ID.
        if ((value1 == null) && (value2 == null)) {
          return null;
        }
        // Best effort to generate an ID out of 2 properties (test for null value)
        String fid = (value1 != null ? value1.toString() : "") + (value2 != null ? value2.toString() : "");
        // The FID must be an XML NCName, so remove invalid characters from it.
        return removeSpecialChars(fid.toCharArray());
      }
    }

    return null;
  }

  private String removeSpecialChars(char aChars[]) {
    StringBuilder stringBuilder = new StringBuilder();

    int end = aChars.length;
    for (int i = 0; i < end; i++) {
      char c = aChars[i];
      if (Character.isLetter(c) || ((i > 0) && Character.isDigit(c))) {
        stringBuilder.append(c);
      }
    }
    return stringBuilder.toString();
  }
}
