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
package samples.lucy.drawing.customdomainobject;

import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

/**
 * To keep the focus of the example on integration in the drawing addon,
 * the domain object has been kept as simple as possible. It basically is
 * a lon lat point which implements ILcdDataObject.
 */
public class CustomDomainObject extends TLcdLonLatPoint implements ILcdDataObject {

  private TLcdDataType fDataType;
  private TLcdDataObject fDataObject;

  public CustomDomainObject(Object[] aProperties, TLcdDataType aDataType) {
    fDataObject = new TLcdDataObject(aDataType);
    fDataType = aDataType;
    List<TLcdDataProperty> properties = aDataType.getProperties();
    for (int i = 0; i < properties.size(); i++) {
      TLcdDataProperty property = properties.get(i);
      fDataObject.setValue(property, aProperties[i]);
    }
  }

  @Override
  public TLcdDataType getDataType() {
    return fDataType;
  }

  @Override
  public Object getValue(TLcdDataProperty aProperty) {
    return fDataObject.getValue(aProperty);
  }

  @Override
  public Object getValue(String aPropertyName) {
    return fDataObject.getValue(aPropertyName);
  }

  @Override
  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    fDataObject.setValue(aProperty, aValue);
  }

  @Override
  public void setValue(String aPropertyName, Object aValue) {
    fDataObject.setValue(aPropertyName, aValue);
  }

  @Override
  public String toString() {
    return "Custom domain object";
  }

  @Override
  public boolean hasValue(TLcdDataProperty aProperty) {
    return fDataObject.hasValue(aProperty);
  }

  @Override
  public boolean hasValue(String aPropertyName) {
    return fDataObject.hasValue(aPropertyName);
  }
}
