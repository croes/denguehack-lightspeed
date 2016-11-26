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
package samples.lucy.treetableview;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDataPropertyValueContext;

/**
 * Utility class to check whether or not customizers are available for TreeTableDataObject cells,
 * and to create them if they are.
 *
 * @since 2013.0
 */
class CustomizerUtility {

  public static boolean canCreateCustomizerPanel(TLcyDomainObjectContext aDomainObjectContext,
                                                 TLcdDataProperty[] aDataProperties,
                                                 ILcyLucyEnv aLucyEnv) {
    if (aDataProperties != null) {
      for (int i = 0; i < aDataProperties.length - 1; i++) {
        TLcdDataProperty dataProperty = aDataProperties[i];
        if (!(dataProperty.getType().isDataObjectType())) {
          return false;
        }
      }
    }
    TLcdDataPropertyValueContext propertyValue = createPropertyValue(aDomainObjectContext,
                                                                     aDataProperties);
    if (propertyValue == null) {
      return false;
    } else {
      return aLucyEnv.getUserInterfaceManager().
          getCompositeCustomizerPanelFactory().canCreateCustomizerPanel(propertyValue);
    }
  }

  public static ILcyCustomizerPanel createCustomizerPanel(TLcyDomainObjectContext aDomainObjectContext,
                                                          TLcdDataProperty[] aDataProperties,
                                                          ILcyLucyEnv aLucyEnv)
      throws IllegalArgumentException {
    TLcdDataPropertyValueContext propertyValue = createPropertyValue(aDomainObjectContext,
                                                                     aDataProperties);
    if (propertyValue == null) {
      throw new IllegalArgumentException("Can't create an customizer panel, call canCreateCustomizerPanel first!");
    }
    ILcyCustomizerPanel panel = aLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory().
        createCustomizerPanel(propertyValue);
    panel.setObject(propertyValue);
    return panel;
  }

  private static TLcdDataPropertyValueContext createPropertyValue(TLcyDomainObjectContext aDomainObjectContext,
                                                                  TLcdDataProperty[] aDataProperties) {
    if (aDomainObjectContext != null &&
        aDomainObjectContext.getDomainObject() instanceof ILcdDataObject &&
        aDataProperties != null &&
        aDataProperties.length > 0) {
      ILcdView view = aDomainObjectContext.getView();
      ILcdLayer layer = aDomainObjectContext.getLayer();
      ILcdModel model = aDomainObjectContext.getModel();
      ILcdDataObject dataObject = (ILcdDataObject) aDomainObjectContext.getDomainObject();
      String expression = ExpressionUtility.createExpression(dataObject.getDataType(), aDataProperties);
      Object value = ExpressionUtility.retrieveValue(dataObject, aDataProperties);
      return new TLcdDataPropertyValueContext(
          value, expression, dataObject, model, layer, view);
    }
    return null;
  }
}
