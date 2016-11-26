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
package samples.lucy.cop.addons.missioncontroltheme;

import java.util.UUID;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.TLcdShapeList;

/**
 * Domain object for {@link AGeoJsonRestModelWithUpdates}
 */
final class GeoJsonRestModelElement extends TLcdShapeList implements ILcdDataObject {

  private final TLcdDataObject fDataObject;
  private final String fServerIDPropertyName;

  /**
   * Create a new domain object
   * @param aDataType The data type for the object
   * @param aServerIDPropertyName The name of the property containing the server id
   * @param aMobileUniqueIDPropertyName The name of the property containing the unique id used by LuciadMobile
   */
  public GeoJsonRestModelElement(TLcdDataType aDataType, String aServerIDPropertyName, String aMobileUniqueIDPropertyName) {
    fServerIDPropertyName = aServerIDPropertyName;
    fDataObject = new TLcdDataObject(aDataType);
    setValue(aMobileUniqueIDPropertyName, UUID.randomUUID());
  }

  public int getID() {
    return ((Number) getValue(fServerIDPropertyName)).intValue();
  }

  @Override
  public TLcdDataType getDataType() {
    return fDataObject.getDataType();
  }

  @Override
  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    fDataObject.setValue(aProperty, aValue);
  }

  @Override
  public boolean hasValue(TLcdDataProperty aProperty) {
    return fDataObject.hasValue(aProperty);
  }

  @Override
  public Object getValue(String aPropertyName) {
    return fDataObject.getValue(aPropertyName);
  }

  @Override
  public void setValue(String aPropertyName, Object aValue) {
    fDataObject.setValue(aPropertyName, aValue);
  }

  @Override
  public boolean hasValue(String aPropertyName) {
    return fDataObject.hasValue(aPropertyName);
  }

  @Override
  public Object getValue(TLcdDataProperty aProperty) {
    return fDataObject.getValue(aProperty);
  }

  @Override
  public synchronized int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object aObject) {
    return aObject == this;
  }
}
