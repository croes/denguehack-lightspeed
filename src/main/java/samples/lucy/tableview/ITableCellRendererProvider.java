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
package samples.lucy.tableview;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Implementations of this interface are responsible for evaluating whether or not they
 * can provide an renderer for a specific cell, and for providing it if they are capable.
 *
 * @since 2013.0
 */
public interface ITableCellRendererProvider {

  /**
   * Evaluates whether or not this instance can provide an renderer.
   *
   * @param aTable the table to provide an editor for.
   * @param aRow the row index in table (view) coordinates.
   * @param aColumn the column index in table (view) coordinates.
   * @return true if this instance can provide an renderer, false otherwise.
   */
  boolean canProvideRenderer(JTable aTable, int aRow, int aColumn);

  /**
   * Returns a <code>TableCellEditor</code> that can be used to edit a specific cell in a
   * <code>JTable</code>.
   *
   * @param aTable the table to provide an editor for.
   * @param aRow the row index in table (view) coordinates.
   * @param aColumn the column index in table (view) coordinates.
   * @return an renderer for the specified table cell.
   * @throws IllegalArgumentException if <code>canProvideRenderer</code> evaluates to false.
   */
  TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn);

}
