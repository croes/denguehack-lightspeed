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
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.StringValue;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.lucy.ILcyLucyEnv;

/**
 * Implementation of <code>AbstractMeasureAnnotationCellRendererProvider</code> for the table view.
 *
 * @since 2013.0
 */
public class TableMeasureAnnotationCellRendererProvider extends AbstractMeasureAnnotationCellRendererProvider {

  public TableMeasureAnnotationCellRendererProvider(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv, SwingConstants.RIGHT);
  }

  public TableMeasureAnnotationCellRendererProvider(StringValue aMeasureStringValue) {
    super(aMeasureStringValue, SwingConstants.RIGHT);
  }

  @Override
  protected TLcdDataProperty getDataProperty(JTable aTable, int aRow, int aColumn) {
    if (aTable.getModel() instanceof IExtendedTableModel) {
      IExtendedTableModel tableModel = (IExtendedTableModel) aTable.getModel();
      int column = aTable.convertColumnIndexToModel(aColumn);
      Object descriptor = tableModel.getColumnDescriptor(column);
      if (descriptor instanceof TLcdDataProperty[]) {
        TLcdDataProperty[] properties = (TLcdDataProperty[]) descriptor;
        // The relevant property is the last in the list, that is the property whose value is being
        // edited.
        return properties[properties.length - 1];
      }
    }
    return null;
  }
}
