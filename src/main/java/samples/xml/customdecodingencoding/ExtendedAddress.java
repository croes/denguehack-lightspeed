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

import java.awt.Color;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;

import samples.xml.customdomainclasses.Address;

public class ExtendedAddress extends Address {

  private static final TLcdDataType EXTENDED_ADDRESS_TYPE = CustomDecodingEncodingDataTypes.EXTENDED_ADDRESS_TYPE;
  private static final TLcdDataProperty LOCATION_PROPERTY = EXTENDED_ADDRESS_TYPE.getDeclaredProperty("location");
  private static final TLcdDataProperty COLOR_PROPERTY = EXTENDED_ADDRESS_TYPE.getDeclaredProperty("color");

  public ExtendedAddress() {
    this(EXTENDED_ADDRESS_TYPE);
  }

  public ExtendedAddress(TLcdDataType aType) {
    super(aType);
  }

  public ILcd2DEditablePoint getLocation() {
    return (ILcd2DEditablePoint) getValue(LOCATION_PROPERTY);
  }

  public void setLocation(ILcd2DEditablePoint aLocation) {
    setValue(LOCATION_PROPERTY, aLocation);
  }

  public Color getColor() {
    return (Color) getValue(COLOR_PROPERTY);
  }

  public void setColor(Color aColor) {
    setValue(COLOR_PROPERTY, aColor);
  }
}
