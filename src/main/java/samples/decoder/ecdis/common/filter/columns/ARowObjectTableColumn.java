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

import java.util.Objects;

/**
 * Abstract table column that can be used in combination with a {@link samples.decoder.ecdis.common.filter.RowObjectTableModel}.
 *
 * @param <T> the row object type
 */
public abstract class ARowObjectTableColumn<T> {
  private final Class fColumnClass;
  private final String fName;

  /**
   * Creates a new instance.
   *
   * @param aName the column name
   * @param aColumnClass the class of the value contained in this column
   */
  public ARowObjectTableColumn(String aName, Class aColumnClass) {
    fName = Objects.requireNonNull(aName);
    fColumnClass = Objects.requireNonNull(aColumnClass);
  }

  /**
   * Returns the column value for the given row. This is typically the property of a row object or interesting metadata.
   *
   * @param aRowObject the row object
   * @return the column value, can be {@code null}
   */
  public abstract Object getColumnValue(T aRowObject);

  /**
   * Sets the new column value.
   *
   * @param aNewValue the new value, can be {@code null}
   * @param aRowObject the row object that needs to be modified in scope of this edit operation
   */
  public abstract void setColumnValue(Object aNewValue, T aRowObject);

  /**
   * Returns the column name.
   *
   * @return the column name
   */
  public String getName() {
    return fName;
  }

  /**
   * Returns the instance class of value rendered in this column.
   *
   * @return the column class
   */
  public Class getColumnClass() {
    return fColumnClass;
  }

}
