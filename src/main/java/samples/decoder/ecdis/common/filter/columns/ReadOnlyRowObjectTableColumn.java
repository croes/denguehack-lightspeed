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
package samples.decoder.ecdis.common.filter.columns;

/**
 * {@link ARowObjectTableColumn } extension for a table column whose value should be read-only.
 */
public class ReadOnlyRowObjectTableColumn<T> extends ARowObjectTableColumn<T> {

  private IFunction<T, ?> fColumnValueFunction;

  /**
   * Creates a new instance.
   *
   * @param aName the column name which will be displayed as column header in the table view
   * @param aColumnClass the type of value contained in this column
   * @param aColumnValueFunction the function to retrieve the column value based on a given row object
   */
  public ReadOnlyRowObjectTableColumn(String aName, Class aColumnClass, IFunction<T, ?> aColumnValueFunction) {
    super(aName, aColumnClass);
    fColumnValueFunction = aColumnValueFunction;
  }

  @Override
  public Object getColumnValue(T aRowObject) {
    return fColumnValueFunction.apply(aRowObject);
  }

  @Override
  public void setColumnValue(Object aNewValue, T aRowObject) {

  }
}
