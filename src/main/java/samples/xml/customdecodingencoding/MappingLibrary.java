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
package samples.xml.customdecodingencoding;

import java.util.ArrayList;
import java.util.List;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.format.xml.bind.ILcdXMLDocumentContext;
import com.luciad.format.xml.bind.ILcdXMLObjectFactory;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedMapping;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataObjectMappingLibrary;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

class MappingLibrary extends TLcdXMLDataObjectMappingLibrary {

  public MappingLibrary(TLcdDataModel aDataModel) {
    super(aDataModel);
  }

  @Override
  protected void doConfigureMapping(TLcdXMLSchemaBasedMapping aMapping) {
    super.doConfigureMapping(aMapping);
    aMapping.getTypeObjectFactoryProvider().registerTypeObjectFactory(
        CustomDecodingEncodingConstants.POINT_TYPE_ID,
        ILcd2DEditablePoint.class, new ILcdXMLObjectFactory<ILcd2DEditablePoint>() {

      public ILcd2DEditablePoint createObject(ILcdXMLDocumentContext aContext) {
        return new TLcdLonLatPoint();
      }

      public ILcd2DEditablePoint resolveObject(ILcd2DEditablePoint aObject, ILcdXMLDocumentContext aContext) {
        return aObject;
      }
    }
                                                                     );
    // register the interface
    List<Class<?>> interfaces = new ArrayList<Class<?>>();
    interfaces.add(ILcd2DEditablePoint.class);
    aMapping.getJavaClassResolver().registerClassPriorityList(interfaces);
  }

}
