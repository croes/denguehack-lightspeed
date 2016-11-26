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
package samples.xml.customdomainclasses;

import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;

public class Address extends TLcdDataObject {

  private static final TLcdDataType ADDRESS_TYPE = CustomDomainClassesDataTypes.ADDRESS_TYPE;
  private static final TLcdDataProperty STREET_PROPERTY = ADDRESS_TYPE.getDeclaredProperty("street");
  private static final TLcdDataProperty NUMBER_PROPERTY = ADDRESS_TYPE.getDeclaredProperty("number");
  private static final TLcdDataProperty CITY_PROPERTY = ADDRESS_TYPE.getDeclaredProperty("city");

  public Address() {
    this(ADDRESS_TYPE);
  }

  public Address(TLcdDataType aType) {
    super(aType);
  }

  public String getStreet() {
    return (String) getValue(STREET_PROPERTY);
  }

  public void setStreet(String aValue) {
    setValue(STREET_PROPERTY, aValue);
  }

  public int getNumber() {
    return (Integer) getValue(NUMBER_PROPERTY);
  }

  public void setNumber(int aValue) {
    setValue(NUMBER_PROPERTY, aValue);
  }

  public String getCity() {
    return (String) getValue(CITY_PROPERTY);
  }

  public void setCity(String aValue) {
    setValue(CITY_PROPERTY, aValue);
  }

}
