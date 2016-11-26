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
package samples.hana.lightspeed.styling;

import static com.luciad.util.expression.TLcdExpressionFactory.*;
import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ON_TERRAIN;

import java.util.Collection;

import javax.swing.Icon;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.hana.lightspeed.domain.CustomerCategory;
import samples.hana.lightspeed.domain.CustomerPoint;
import samples.hana.lightspeed.domain.InsuranceCompany;

/**
 * Styler that creates {@link TLspPlotStyle plot styles} to visualize individual policy holders.
 */
public class CustomerPlotStyler extends ALspStyler {

  private static final ILcdIcon[] sCustomerCategoryIcons = new ILcdIcon[]{new TLcdImageIcon("samples/hana/lightspeed/Icon/category/accommodation.png"),
                                                                          new TLcdImageIcon("samples/hana/lightspeed/Icon/category/hostel.png"),
                                                                          new TLcdImageIcon("samples/hana/lightspeed/Icon/category/restaurant.png"),
                                                                          new TLcdImageIcon("samples/hana/lightspeed/Icon/category/chalet.png"),
                                                                          new TLcdImageIcon("samples/hana/lightspeed/Icon/category/museum.png")};

  private static final ILcdIcon[] sInsuranceCompanyIcons = new ILcdIcon[InsuranceCompany.values().length];

  static {
    for (int i = 0; i < sInsuranceCompanyIcons.length; i++) {
      sInsuranceCompanyIcons[i] = new TLcdResizeableIcon(new TLcdImageIcon("samples/hana/lightspeed/insurance/" + InsuranceCompany.values()[i].name() + ".png"), 60, -1);
    }
  }

  private static final ILcdIcon[] sPolicyValueIcons = new ILcdIcon[]{new PolicyValueIcon(0),
                                                                     new PolicyValueIcon(1),
                                                                     new PolicyValueIcon(2),
                                                                     new PolicyValueIcon(3),
                                                                     new PolicyValueIcon(4),
                                                                     new PolicyValueIcon(5),
                                                                     new PolicyValueIcon(6),
                                                                     new PolicyValueIcon(7),
                                                                     new PolicyValueIcon(8),
                                                                     new PolicyValueIcon(9),
                                                                     new PolicyValueIcon(10)};

  private static final ILcdExpression<Integer> sCustomerCategory = attribute("type", Integer.class, new AttributeValueProvider<Integer>() {
    @Override
    public Integer getValue(Object aObject, Object aShape) {
      return ((CustomerPoint) aObject).getCategory().ordinal();
    }
  });
  private static final ILcdExpression<Integer> sInsuranceCompany = attribute("insurance", Integer.class, new AttributeValueProvider<Integer>() {
    @Override
    public Integer getValue(Object aObject, Object aShape) {
      return ((CustomerPoint) aObject).getInsurance().ordinal();
    }
  });
  private static final ILcdExpression<Integer> sPolicyValue = attribute("policyValue", Integer.class, new AttributeValueProvider<Integer>() {
    @Override
    public Integer getValue(Object aObject, Object aShape) {
      return ((CustomerPoint) aObject).getPolicyValue();
    }
  });

  private static final ILcdExpression<ILcdIcon> sIconByCustomerCategory = map(sCustomerCategory, sCustomerCategoryIcons, sCustomerCategoryIcons[0]);
  private final ILcdExpression<ILcdIcon> sIconByPolicyValue;

  private static final TLspPlotStyle sDensityStyle = TLspPlotStyle.newBuilder()
                                                                  .density(true)
                                                                  .scale(0.5f)
                                                                  .elevationMode(ON_TERRAIN).build();

  private boolean fDensityMode = true;
  private CustomerCategory fCurrentCustomerCategory;
  private InsuranceCompany fCurrentInsuranceCompany;
  private int fMinPolicyValue = 0;

  public CustomerPlotStyler(final int aMaxPolicyValue) {
    ILcdExpression<? extends Number> policyValueRating = attribute("policyRating", Integer.class, new AttributeValueProvider<Integer>() {
      @Override
      public Integer getValue(Object o, Object o2) {
        return (10 * ((CustomerPoint) o).getPolicyValue()) / aMaxPolicyValue;
      }
    });
    sIconByPolicyValue = map(policyValueRating, sPolicyValueIcons, sPolicyValueIcons[0]);
  }

  public static Icon getSwingIcon(InsuranceCompany aInsuranceCompany) {
    return new TLcdSWIcon(sInsuranceCompanyIcons[aInsuranceCompany.ordinal()]);
  }

  public static Icon getSwingIcon(CustomerCategory aCustomerCategory) {
    return new TLcdSWIcon(sCustomerCategoryIcons[aCustomerCategory.ordinal()]);
  }

  public void setDensity(boolean aDensity) {
    fDensityMode = aDensity;
    fireStyleChangeEvent();
  }

  public void setCustomerCategory(CustomerCategory aCustomerCategory) {
    fCurrentCustomerCategory = aCustomerCategory;
    fireStyleChangeEvent();
  }

  public void setInsuranceCompany(InsuranceCompany aInsuranceCompany) {
    fCurrentInsuranceCompany = aInsuranceCompany;
    fireStyleChangeEvent();
  }

  public void setMinPolicyValue(int aMinPolicyValue) {
    fMinPolicyValue = aMinPolicyValue;
    fireStyleChangeEvent();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aTLspContext) {
    if (fDensityMode) {
      aStyleCollector.objects(aObjects).style(sDensityStyle).submit();
      return;
    }

    ILcdExpression<Boolean> visible = gte(sPolicyValue, fMinPolicyValue);
    if (fCurrentCustomerCategory != null) {
      visible = and(visible, eq(sCustomerCategory, fCurrentCustomerCategory.ordinal()));
    }
    if (fCurrentInsuranceCompany != null) {
      visible = and(visible, eq(sInsuranceCompany, fCurrentInsuranceCompany.ordinal()));
    }

    TLspPlotStyle.Builder<?> baseStyle = TLspPlotStyle.newBuilder()
                                                      .visibility(visible)
                                                      .elevationMode(ON_TERRAIN)
                                                      .automaticScaling(30.0);

    aStyleCollector.objects(aObjects).style(baseStyle.icon(sIconByPolicyValue).build()).submit();
    aStyleCollector.objects(aObjects).style(baseStyle.icon(sIconByCustomerCategory).build()).submit();
  }
}
