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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceObjectCodec;
import com.luciad.lucy.workspace.TLcyWorkspaceAbortedException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * <code>ALcyWorkspaceCodec</code> for {@link TableViewCustomizerPanel} instances
 *
 */
public class TableViewCustomizerPanelCodec extends ALcyWorkspaceObjectCodec {
  private static final String LAYER = "layer";
  private static final String VIEW = "view";
  private static final String HIDDEN_COLUMNS = "hiddenColumns";
  private static final String COLUMN_ORDER = "columnOrder";
  private static final String SORTED_COLUMNS = "sortedColumns";
  private static final String SORTED_COLUMNS_ORDER = "sortedColumnsOrder";
  private static final String SORT_ASCENDING = "ascending";
  private static final String SORT_DESCENDING = "descending";
  private static final String AUTO_CENTER = "autoCenter";
  private static final String AUTO_FIT = "autoFit";
  private static final String AUTO_SELECT = "autoSelect";
  private static final String LOCKED_ON_CURRENT_LAYER = "lockedOnCurrentLayer";
  private static final String NAME = "name";

  private final String fUID;
  private final String fPrefix;
  private ILcyCustomizerPanelFactory fCustomizerPanelFactory;
  private ILcyLucyEnv fLucyEnv;

  public TableViewCustomizerPanelCodec(String aUID,
                                       String aPrefix,
                                       ILcyCustomizerPanelFactory aCustomizerPanelFactory,
                                       ILcyLucyEnv aLucyEnv) {
    fUID = aUID;
    fPrefix = aPrefix;
    fCustomizerPanelFactory = aCustomizerPanelFactory;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public boolean canEncodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent) {
    boolean result = aObject != null &&
                     aObject.getClass().equals(TableViewCustomizerPanel.class) &&
                     fCustomizerPanelFactory.canCreateCustomizerPanel(((ILcyCustomizerPanel) aObject).getObject());
    if (result) {
      //also check whether the layer and the view contained in the model context can be encoded
      TLcyModelContext modelContext = (TLcyModelContext) ((TableViewCustomizerPanel) aObject).getObject();
      if (modelContext != null) {
        result = aWSCodec.canEncodeReference(modelContext.getLayer()) &&
                 aWSCodec.canEncodeReference(modelContext.getView());
      }
    }
    return result;
  }

  @Override
  public void encodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, OutputStream aOut) throws IOException, TLcyWorkspaceAbortedException {
    TLcyStringProperties properties = new TLcyStringProperties();

    encodeObjectImp(aWSCodec, aObject, properties);

    new TLcyStringPropertiesCodec().encode(properties, aOut);
  }

  protected ILcyCustomizerPanelFactory getCustomizerPanelFactory() {
    return fCustomizerPanelFactory;
  }

  protected ILcyLucyEnv getLucyEnv() {
    return fLucyEnv;
  }

  protected void encodeObjectImp(ALcyWorkspaceCodec aWSCodec, Object aObject, TLcyStringProperties aPropertiesSFCT) throws IOException {
    TableViewCustomizerPanel panel = (TableViewCustomizerPanel) aObject;

    TLcyModelContext modelContext = (TLcyModelContext) panel.getObject();
    encodeModelContext(modelContext, aPropertiesSFCT, aWSCodec);

    encodeTable(panel.getTable(), aPropertiesSFCT);

    encodeFitCenterSelectState(panel, aPropertiesSFCT);

    encodeHintPinned(panel, aPropertiesSFCT);

    encodeName(panel, aPropertiesSFCT);
  }

  private void encodeModelContext(TLcyModelContext aModelContext, ALcyProperties aPropertiesSFCT, ALcyWorkspaceCodec aWSCodec) throws IOException {
    if (aModelContext != null) {
      aPropertiesSFCT.putString(fPrefix + LAYER, aWSCodec.encodeReference(aModelContext.getLayer()));
      aPropertiesSFCT.putString(fPrefix + VIEW, aWSCodec.encodeReference(aModelContext.getView()));
    }
  }

  private void encodeTable(JTable aTable, TLcyStringProperties aPropertiesSFCT) {
    if (aTable != null) {
      //store the hidden columns
      List<String> hiddenColumns = new ArrayList<String>();
      for (int i = 0, c = aTable.getModel().getColumnCount(); i < c; i++) {
        int viewColumnIndex = aTable.convertColumnIndexToView(i);
        if (viewColumnIndex == -1) {
          hiddenColumns.add(aTable.getModel().getColumnName(i));
        }
      }
      aPropertiesSFCT.putStringArray(fPrefix + HIDDEN_COLUMNS, hiddenColumns.toArray(new String[hiddenColumns.size()]));

      //store the column order
      List<String> columnOrder = new ArrayList<String>();
      for (int i = 0, c = aTable.getColumnCount(); i < c; i++) {
        columnOrder.add(aTable.getModel().getColumnName(aTable.convertColumnIndexToModel(i)));
      }
      aPropertiesSFCT.putStringArray(fPrefix + COLUMN_ORDER, columnOrder.toArray(new String[columnOrder.size()]));

      //store the column sorting
      if (aTable instanceof JXTable) {
        List<String> sortedColumns = new ArrayList<String>();
        List<String> sortedColumnsOrder = new ArrayList<String>();
        for (int i = 0, c = aTable.getColumnModel().getColumnCount(); i < c; i++) {
          SortOrder sortOrder = ((JXTable) aTable).getSortOrder(i);
          if (sortOrder != null && sortOrder != SortOrder.UNSORTED) {
            sortedColumns.add(aTable.getModel().getColumnName(aTable.convertColumnIndexToModel(i)));
            sortedColumnsOrder.add(sortOrder == SortOrder.ASCENDING ? SORT_ASCENDING : SORT_DESCENDING);
          }
        }
        aPropertiesSFCT.putStringArray(fPrefix + SORTED_COLUMNS, sortedColumns.toArray(new String[sortedColumns.size()]));
        aPropertiesSFCT.putStringArray(fPrefix + SORTED_COLUMNS_ORDER, sortedColumnsOrder.toArray(new String[sortedColumnsOrder.size()]));
      }
    }
  }

  private void encodeFitCenterSelectState(TableViewCustomizerPanel aPanel, TLcyStringProperties aPropertiesSFCT) {
    //encode center/fit/select settings
    aPropertiesSFCT.putBoolean(fPrefix + AUTO_CENTER, aPanel.isAutoCenter());
    aPropertiesSFCT.putBoolean(fPrefix + AUTO_FIT, aPanel.isAutoFit());
    aPropertiesSFCT.putBoolean(fPrefix + AUTO_SELECT, aPanel.isAutoSelect());
  }

  private void encodeHintPinned(TableViewCustomizerPanel aPanel, TLcyStringProperties aPropertiesSFCT) {
    Object value = aPanel.getValue(ILcyCustomizerPanel.HINT_PINNED);
    if (value instanceof Boolean) {
      aPropertiesSFCT.putBoolean(fPrefix + LOCKED_ON_CURRENT_LAYER, (Boolean) value);
    }
  }

  private void encodeName(TableViewCustomizerPanel aPanel, TLcyStringProperties aPropertiesSFCT) {
    Object name = aPanel.getValue(ILcyCustomizerPanel.NAME);
    if (name instanceof String) {
      aPropertiesSFCT.putString(fPrefix + NAME, (String) name);
    }
  }

  @Override
  public Object createObject(ALcyWorkspaceCodec aWSCodec, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    ALcyProperties properties = new TLcyStringPropertiesCodec().decode(aIn);

    return createObjectImpl(aWSCodec, properties);
  }

  protected Object createObjectImpl(ALcyWorkspaceCodec aWSCodec, final ALcyProperties aProperties) throws IOException {
    final TLcyModelContext modelContext = createModelContext(aWSCodec, aProperties);
    if (modelContext == null) {
      return null;
    }

    final ILcyCustomizerPanel[] result = new ILcyCustomizerPanel[]{null};

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        if (fCustomizerPanelFactory.canCreateCustomizerPanel(modelContext)) {
          ILcyCustomizerPanel customizerPanel = fCustomizerPanelFactory.createCustomizerPanel(modelContext);
          customizerPanel.setObject(modelContext);
          if (customizerPanel instanceof TableViewCustomizerPanel) {
            decodeTableSettings((TableViewCustomizerPanel) customizerPanel, aProperties);

            decodeFitCenterSelectState((TableViewCustomizerPanel) customizerPanel, aProperties);

            decodeHintPinned(customizerPanel, aProperties);

            decodeName(customizerPanel, aProperties);
          }
          result[0] = customizerPanel;
        }
      }
    });
    if (result[0] == null) {
      aWSCodec.getLogListener().warn(this, "Not restoring the table view customizer panel " +
                                           "because the customizer factory was unable to create a new table view.");
    }
    return result[0];
  }

  private void decodeName(ILcyCustomizerPanel aCustomizerPanel, ALcyProperties aProperties) {
    Object name = aCustomizerPanel.getValue(ILcyCustomizerPanel.NAME);
    aCustomizerPanel.putValue(ILcyCustomizerPanel.NAME, aProperties.getString(fPrefix + NAME, name instanceof String ? ((String) name) : ""));
  }

  private void decodeHintPinned(ILcyCustomizerPanel aCustomizerPanel, ALcyProperties aProperties) {
    Object value = aCustomizerPanel.getValue(ILcyCustomizerPanel.HINT_PINNED);
    aCustomizerPanel.putValue(ILcyCustomizerPanel.HINT_PINNED, aProperties.getBoolean(fPrefix + LOCKED_ON_CURRENT_LAYER, value instanceof Boolean && (Boolean) value));
  }

  private void decodeFitCenterSelectState(TableViewCustomizerPanel aCustomizerPanel, ALcyProperties aProperties) {
    aCustomizerPanel.setAutoFit(aProperties.getBoolean(fPrefix + AUTO_FIT, aCustomizerPanel.isAutoFit()));
    aCustomizerPanel.setAutoCenter(aProperties.getBoolean(fPrefix + AUTO_CENTER, aCustomizerPanel.isAutoCenter()));
    aCustomizerPanel.setAutoSelect(aProperties.getBoolean(fPrefix + AUTO_SELECT, aCustomizerPanel.isAutoSelect()));
  }

  private void decodeTableSettings(TableViewCustomizerPanel aCustomizerPanel, ALcyProperties aProperties) {
    /*
* The implementation of this method needs to be very defensive, as the ILcdModel that was stored
* and that was restored is not guaranteed to be equal.  The file from which the ILcdModel was loaded
* can for example be replaced with another file, or the file might have been removed.
*/
    JTable table = aCustomizerPanel.getTable();

    //restore the hidden columns
    if (table instanceof JXTable) {
      List<String> hiddenColumns = Arrays.asList(aProperties.getStringArray(fPrefix + HIDDEN_COLUMNS, new String[0]));
      for (int i = 0, c = hiddenColumns.size(); i < c; i++) {
        int col = table.convertColumnIndexToView(findColumnIndex(hiddenColumns.get(i), table));
        if (col != -1) {
          ((JXTable) table).getColumnExt(col).setVisible(false);
        }
      }
    }

    //restore the column order
    List<String> column_order = Arrays.asList(aProperties.getStringArray(fPrefix + COLUMN_ORDER, new String[0]));
    for (int i = 0, c = column_order.size(); i < c; i++) {
      int col = table.convertColumnIndexToView(findColumnIndex(column_order.get(i), table));
      if (col != -1 && i < table.getColumnCount()) {
        table.moveColumn(col, i);
      }
    }

    //restore the column sorting
    if (table instanceof JXTable) {
      List<String> sorted_columns = Arrays.asList(aProperties.getStringArray(fPrefix + SORTED_COLUMNS, new String[0]));
      List<String> sorted_columns_order = Arrays.asList(aProperties.getStringArray(fPrefix + SORTED_COLUMNS_ORDER, new String[0]));
      for (int i = 0, c = sorted_columns.size(); i < c; i++) {
        int col = table.convertColumnIndexToView(findColumnIndex(sorted_columns.get(i), table));
        boolean ascending = !SORT_DESCENDING.equals(sorted_columns_order.get(i));

        if (col != -1) {
          ((JXTable) table).setSortOrder(col, ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING);
        }
      }
    }
  }

  private TLcyModelContext createModelContext(ALcyWorkspaceCodec aWSCodec, ALcyProperties aProperties) throws IOException {
    String layerReference = aProperties.getString(fPrefix + LAYER, null);
    String viewReference = aProperties.getString(fPrefix + VIEW, null);

    if (layerReference == null) {
      aWSCodec.getLogListener().warn(this, "Not restoring the table view customizer panel because no reference to the layer was stored.");
      return null;
    }
    if (viewReference == null) {
      aWSCodec.getLogListener().warn(this, "Not restoring the table view customizer panel because no reference to the view was stored.");
    }

    ILcdLayer layer = (ILcdLayer) aWSCodec.decodeReference(layerReference);
    ILcdView view = (ILcdView) aWSCodec.decodeReference(viewReference);
    if (layer == null) {
      aWSCodec.getLogListener().warn(this, "Not restoring the table view customizer panel " +
                                           "because the ILcdLayer could not be restored from the workspace.");
      return null;
    } else if (view == null) {
      aWSCodec.getLogListener().warn(this, "Not restoring the table view customizer panel " +
                                           "because the ILcdView could not be restored from the workspace.");
      return null;
    }
    return new TLcyModelContext(layer.getModel(), layer, view);
  }

  @Override
  public void decodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    //do nothing
  }

  private int findColumnIndex(String aColumnName, JTable aTable) {
    TableModel model = aTable.getModel();
    for (int i = 0, c = model.getColumnCount(); i < c; i++) {
      if (aColumnName != null && aColumnName.equals(model.getColumnName(i))) {
        return i;
      }
    }
    return -1;
  }

  protected String getPrefix() {
    return fPrefix;
  }
}
