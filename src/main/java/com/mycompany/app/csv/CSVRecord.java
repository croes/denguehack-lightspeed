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
package com.mycompany.app.csv;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;

/**
 * Domain object for a CSV file record.
 */
class CSVRecord implements ILcdDataObject, ILcdBounded {

  private final ILcdDataObject fDataObject;

  CSVRecord(ILcdPoint aPoint, TLcdDataType aType) {
    fDataObject = new TLcdDataObject(aType);
    fDataObject.setValue(CSVDataTypes.POINT_GEOMETRY_PROPERTY_NAME, aPoint);
  }

  public ILcdBounds getBounds() {
    ILcdBounded point = (ILcdBounded) getValue(CSVDataTypes.POINT_GEOMETRY_PROPERTY_NAME);
    return point.getBounds();
  }

  public TLcdDataType getDataType() {
    return fDataObject.getDataType();
  }

  public Object getValue(TLcdDataProperty aProperty) {
    return fDataObject.getValue(aProperty);
  }

  public Object getValue(String aPropertyName) {
    return fDataObject.getValue(aPropertyName);
  }

  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    fDataObject.setValue(aProperty, aValue);
  }

  public void setValue(String aPropertyName, Object aValue) {
    fDataObject.setValue(aPropertyName, aValue);
  }

  public boolean hasValue(TLcdDataProperty aProperty) {
    return fDataObject.hasValue(aProperty);
  }

  public boolean hasValue(String aPropertyName) {
    return fDataObject.hasValue(aPropertyName);
  }
}
