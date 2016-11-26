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

import java.util.Collection;

import javax.swing.table.TableModel;

import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.ALcdTransformingModel;

/**
 * Extension of the <code>TableModel</code> interface. This extension exposes:
 *
 * <ul>
 *   <li>
 *     The one-to-one mapping between a row index in the table,
 *     and a domain object in an {@code ILcdIntegerIndexedModel}.
 *     See {@link #getObjectAtRow(int)} and {@link #getRowOfObject(Object)} methods.
 *   </li>
 *   <li>
 *     Extra information about the columns in the table.
 *     See {@link #getColumnDescriptor(int)} and {@link #getColumnTooltipText(int)} methods.
 *   </li>
 *   <li>
 *     Access to the model contained in the {@code TLcyModelContext} for which the table view
 *     customizer panel was created (the so-called "transformed model"),
 *     and the {@code ILcdIntegerIndexedModel} on which this {@code IExtendedTableModel} is based (the "original model").
 *     Those two models are not necessarily the same instance.
 *     See {@link #getTransformedModel()} and {@link #getOriginalModel()} methods for
 *     more information.
 *   </li>
 *   <li>
 *     The mapping between elements in {@link #getOriginalModel()} and
 *     elements in {@link #getTransformedModel()}.
 *     See {@link #originalToTransformed(Object)} and {@link #transformedToOriginal(Object)} methods.
 *   </li>
 * </ul>
 */
public interface IExtendedTableModel extends TableModel {

  /**
   * Returns the domain object from {@link #getOriginalModel()}
   * corresponding to a given row.
   *
   * @param aRowIndex the row index.
   *                  This index is interpreted as a table-view coordinate.
   * @return the domain object corresponding to the row.
   *
   * @see #getRowOfObject(Object)
   * @see javax.swing.JTable#convertRowIndexToModel(int)
   */
  Object getObjectAtRow(int aRowIndex);

  /**
   * Returns the row index of the domain object {@code aDomainObject}
   * from {@link #getOriginalModel()}
   *
   * @param aDomainObject The domain object
   *
   * @return the row index of the domain object {@code aDomainObject},
   *         or -1 if the object is not contained in {@link #getOriginalModel()}
   *
   * @see #getObjectAtRow(int)
   */
  int getRowOfObject(Object aDomainObject);

  /**
   * Returns the column descriptor corresponding to a given column. This object should identify
   * the content of the column somehow.
   *
   * @param aColumnIndex the column index
   * @return the column descriptor corresponding to a given column, or null if none is available.
   */
  Object getColumnDescriptor(int aColumnIndex);

  /**
   * Returns a string explanation of the content of a column. This could for instance be shown in a
   * tooltip over the column header.
   *
   * @param aColumnIndex the column index
   * @return the string description of the content of the column.
   */
  String getColumnTooltipText(int aColumnIndex);

  /**
   * <p>
   *   Returns the {@code ILcdIntegerIndexedModel} which is used as contents for this table model.
   * </p>
   *
   * <p>
   *   Note that this model is not necessarily the same as the model which is contained in the
   *   {@code TLcyModelContext} used to create the table view customizer panel.
   *   In case the model context contains an {@link ALcdTransformingModel},
   *   the contents for this table model is based on the {@link ALcdTransformingModel#getOriginalModel()}.
   * </p>
   *
   * @return the {@code ILcdIntegerIndexedModel} which is used as contents for this table model.
   *
   * @see #getTransformedModel()
   */
  ILcdIntegerIndexedModel getOriginalModel();

  /**
   * <p>
   *   Returns the {@code ILcdModel} which is contained in the {@code TLcyModelContext}
   *   for which the table view customizer panel is created.
   * </p>
   *
   * <p>
   *   Note that this model is not necessarily the same as the model which is used as contents for this table model.
   *   In case the model context contains an {@link ALcdTransformingModel}
   *   the contents for this table model is based on the {@link ALcdTransformingModel#getOriginalModel()}.
   * </p>
   *
   * @return the {@code ILcdModel} contained in the {@code TLcyModelContext} for which the table view customizer panel
   * is created.
   *
   * @see #getOriginalModel()
   */
  ILcdModel getTransformedModel();

  /**
   * <p>
   *   Converts a domain object from the {@link #getOriginalModel()} to domain objects
   *   of the {@link #getTransformedModel()}.
   *   The inverse transformation can be calculated using the
   *   {@link #transformedToOriginal(Object)} method.
   * </p>
   *
   * @param aOriginalModelElement The domain object of {@link #getOriginalModel()}
   *
   * @return Collection containing all the domain objects of {@link #getTransformedModel()} corresponding to
   *         {@code aOriginalModelElement}.
   *         Never {@code null}, but might be empty.
   *
   * @see #transformedToOriginal(Object)
   */
  Collection<Object> originalToTransformed(Object aOriginalModelElement);

  /**
   * <p>
   *   Converts a domain object from the {@link #getTransformedModel()} to domain objects
   *   of the {@link #getOriginalModel()}.
   *   The inverse transformation can be calculated using the
   *   {@link #originalToTransformed(Object)} method.
   * </p>
   *
   * @param aTransformedModelElement The domain object of {@link #getTransformedModel()}
   *
   * @return Collection containing all the domain objects of {@link #getOriginalModel()} corresponding to
   *         {@code aTransformedModelElement}.
   *         Never {@code null}, but might be empty.
   *
   * @see #originalToTransformed(Object)
   */
  Collection<Object> transformedToOriginal(Object aTransformedModelElement);
}
