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
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage;

/**
 * Utility class for retrieving and updating values using expression identifiers. We use the
 * {@link TLcdDataObjectExpressionLanguage} to create expressions.
 */
public class ExpressionUtility {

  private static final TLcdDataObjectExpressionLanguage sLanguage =
      new TLcdDataObjectExpressionLanguage();

  public static Object retrieveValue(ILcdDataObject aDataObject, TLcdDataProperty... aDataProperties) {
    //short cut for common case.
    if (aDataProperties.length == 1) {
      return aDataObject.getValue(aDataProperties[0]);
    }
    return sLanguage.evaluate(createExpression(aDataObject.getDataType(), aDataProperties), aDataObject);
  }

  public static Object retrieveValue(ILcdDataObject aDataObject, String aExpression) {
    return sLanguage.evaluate(aExpression, aDataObject);
  }

  public static void updateValue(ILcdDataObject aDataObject, String aExpression, Object aValue) {
    sLanguage.update(aExpression, aDataObject, aValue);
  }

  public static String createExpression(TLcdDataType aDataType, TLcdDataProperty... aDataProperties) {
    return TLcdDataObjectExpressionLanguage.createExpression(aDataType, aDataProperties);
  }
}
