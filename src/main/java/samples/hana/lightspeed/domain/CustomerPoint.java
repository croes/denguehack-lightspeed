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
package samples.hana.lightspeed.domain;

import static com.luciad.util.TLcdConstant.DEG2RAD;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;

/**
 * Point with low memory footprint.
 */
public class CustomerPoint implements ILcdPoint, ILcdDataObject {

  private final TLcdDataType fDataType;
  private final double fX;
  private final double fY;
  private final CustomerCategory fCategory;
  private final int fPolicyValue;
  private final InsuranceCompany fInsuranceCompany;

  public CustomerPoint(TLcdDataType aDataType, double aX, double aY, CustomerCategory aCategory, int aPolicyValue, InsuranceCompany aInsuranceCompany) {
    fDataType = aDataType;
    fY = aY;
    fX = aX;
    fCategory = aCategory;
    fPolicyValue = aPolicyValue;
    fInsuranceCompany = aInsuranceCompany;
  }

  @Override
  public TLcdDataType getDataType() {
    return fDataType;
  }

  @Override
  public double getX() {
    return fX;
  }

  @Override
  public double getY() {
    return fY;
  }

  public CustomerCategory getCategory() {
    return fCategory;
  }

  public InsuranceCompany getInsurance() {
    return fInsuranceCompany;
  }

  public int getPolicyValue() {
    return fPolicyValue;
  }

  @Override
  public Object getValue(TLcdDataProperty aTLcdDataProperty) {
    return getValue(aTLcdDataProperty.getName());
  }

  @Override
  public Object getValue(String s) {
    if ("CUSTOMER_CATEGORY".equals(s)) {
      return getCategory().toString();
    } else if ("POLICY_VALUE".equals(s)) {
      return fPolicyValue;
    } else if ("INSURANCE_ID".equals(s)) {
      return getInsurance().toString();
    } else {
      return null;
    }
  }

  @Override
  public void setValue(TLcdDataProperty aProperty, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValue(String s, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasValue(TLcdDataProperty aProperty) {
    return hasValue(aProperty.getName());
  }

  @Override
  public boolean hasValue(String s) {
    return ("CUSTOMER_CATEGORY".equals(s) || "POLICY_VALUE".equals(s) || "INSURANCE_ID".equals(s));
  }

  @Override
  public double getZ() {
    return 0;
  }

  @Override
  public double getCosX() {
    return Math.cos(getX() * DEG2RAD);
  }

  @Override
  public double getCosY() {
    return Math.cos(getY() * DEG2RAD);
  }

  @Override
  public double getSinX() {
    return Math.sin(getX() * DEG2RAD);
  }

  @Override
  public double getSinY() {
    return Math.sin(getY() * DEG2RAD);
  }

  @Override
  public double getTanX() {
    return Math.tan(getX() * DEG2RAD);
  }

  @Override
  public double getTanY() {
    return Math.tan(getY() * DEG2RAD);
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ILcdPoint getFocusPoint() {
    return this;
  }

  @Override
  public boolean contains2D(ILcdPoint aPoint) {
    return contains2D(aPoint.getX(), aPoint.getY());
  }

  @Override
  public boolean contains2D(double aX, double aY) {
    return (aX == fX && aY == fY);
  }

  @Override
  public boolean contains3D(ILcdPoint aPoint) {
    return contains3D(aPoint.getX(), aPoint.getY(), aPoint.getZ());
  }

  @Override
  public boolean contains3D(double aX, double aY, double aZ) {
    return (aX == fX && aY == fY && aZ == 0);
  }

  @Override
  public ILcdBounds getBounds() {
    return new TLcdLonLatPoint(fX, fY);
  }

  @Override
  public Object clone() {
    return new CustomerPoint(fDataType, fX, fY, fCategory, fPolicyValue, fInsuranceCompany);
  }
}
