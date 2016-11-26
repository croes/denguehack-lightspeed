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
package samples.lucy.search.service.location;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.ILcdPoint;

/**
 * Domain object which will be inserted in the model for the search result layer
 * @since 2016.0
 */
final class SearchResultDomainObject implements ILcdDataObject {

  private final ILcdPoint fPoint;
  private final ILcdDataObject fDataObject;

  SearchResultDomainObject(ILcdPoint aPoint, String aLabel) {
    fPoint = aPoint;
    fDataObject = new TLcdDataObject(SearchResultModelDescriptor.SEARCH_RESULT_TYPE);
    fDataObject.setValue(SearchResultModelDescriptor.DESCRIPTION_PROPERTY, aLabel);
    fDataObject.setValue(SearchResultModelDescriptor.GEOMETRY_PROPERTY, aPoint);
  }

  ILcdPoint getPoint() {
    return fPoint;
  }

  @Override
  public TLcdDataType getDataType() {
    return fDataObject.getDataType();
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
  public boolean hasValue(TLcdDataProperty aProperty) {
    return fDataObject.hasValue(aProperty);
  }

  @Override
  public boolean hasValue(String aPropertyName) {
    return fDataObject.hasValue(aPropertyName);
  }
}
