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

import static org.jdesktop.swingx.decorator.HighlightPredicate.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import samples.common.MetaKeyUtil;
import samples.common.UIColors;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.ILcyPopupMenu;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyPopupMenu;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.util.TLcyGenericComposite;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.collections.ILcdIntList;
import com.luciad.util.collections.TLcdIntArrayList;
import com.luciad.view.ILcdLayer;

/**
 * <p>Factory responsible for the creation of the table view customizer panel.</p>
 *
 * <p>This factory is an extension of <code>ALcyGUIFactory</code>. This is not strictly necessary,
 * but allows to illustrate how a typical <code>ALcyGUIFactory</code> is implemented. Furthermore,
 * it allows to easily customize certain (small) parts of the customizer panel. The extended table view
 * sample further illustrates how to customize GUI elements created in a
 * <code>ALcyGUIFactory</code>.</p>
 *
 * <p>The customizer panel will show data, contained in the <code>ILcdModel</code> of a set
 * <code>TLcyModelContext</code>, in a <code>JTable</code>. The <code>JTable</code> has one row for
 * every object, and one column for every attribute associated with those objects.  It has a right
 * click menu with some context related actions, and a toolbar to synchronize the table selection
 * with the view.</p>
 *
 * <p>This is a read-only customizer panel: the attribute values cannot be changed using this
 * customizer panel.</p>
 *
 * <p>To achieve all this, the <code>TLcyModelContext</code> passed to <code>setObject</code> must
 * match with these requirements:</p>
 *
 * <ul>
 *
 * <li>The <code>ILcdModel</code> of the context must be randomly accessible, therefore it must
 * implement <code>ILcdIntegerIndexedModel</code>.
 *
 * <li>The object of the <code>ILcdModel</code> should contain attributes, so they must implement
 * either the <code>ILcdFeatured</code> or the <code>ILcdDataObject</code> interface, and the
 * <code>ILcdModelDescriptor</code> of the <code>ILcdModel</code> must be either an
 * <code>ILcdFeaturedDescriptor</code> or a <code>ILcdDataModelDescriptor</code>.
 *
 *
 * </ul>
 */
public class TableViewGUIFactory extends ALcyGUIFactory<TableViewCustomizerPanel> {
  /**
   * The properties passed to this factory must contain a <code>TLcyModelContext</code>. This
   * <code>TLcyModelContext</code> should be stored using this key.
   */
  public static final String MODEL_CONTEXT_KEY = "modelContext";
  /**
   * The ID of the active settable which automatically fits the view on the selection in the table.
   *
   * @see #createActiveSettable(int, ALcyProperties)
   */
  public static final int AUTO_FIT_ACTIVE_SETTABLE = 1;
  /**
   * The ID of the active settable which automatically centers the view on the selection in the
   * table.
   *
   * @see #createActiveSettable(int, ALcyProperties)
   */
  public static final int AUTO_CENTER_ACTIVE_SETTABLE = 2;
  /**
   * The ID of the active settable which automatically syncs the view selection with the table
   * selection
   *
   * @see #createActiveSettable(int, ALcyProperties)
   */
  public static final int AUTO_SELECT_ACTIVE_SETTABLE = 3;
  /**
   * The ID of the active settable which locks the created <code>ILcyCustomizerPanel</code> on the
   * current layer.
   *
   * @see #createActiveSettable(int, ALcyProperties)
   */
  public static final int LOCK_ON_CURRENT_LAYER_ACTIVE_SETTABLE = 4;
  /**
   * The ID of the action allowing to find entries in the table view.
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int FIND_ACTION = 11;
  /**
   * The ID of the action to export the contents of the table to a .csv file
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int EXPORT_TO_CSV_ACTION = 12;
  /**
   * The ID of the copy action
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int COPY_ACTION = 13;
  /**
   * The ID of the action which toggles the visibility of the selected objects
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int TOGGLE_VISIBILITY_ACTION = 14;
  /**
   * The ID of the action which fits the view on the table selection, and synchronizes the
   * selection
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int FIT_AND_SELECT_ACTION = 15;
  /**
   * The ID of the action which centers the view on the table selection, and synchronizes the
   * selection
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int CENTER_AND_SELECT_ACTION = 16;
  /**
   * The ID of the action which shows the number of rows in the table
   * @see #createAction(int, ALcyProperties)
   */
  public static final int SHOW_ROW_COUNT_ACTION = 17;
  /**
   * The ID of the toolbar of the customizer panel
   *
   * @see #createActionBar(int, ALcyProperties)
   */
  public static final int TOOL_BAR_ACTION_BAR = 21;
  /**
   * The ID of the pop-up menu of the customizer panel
   *
   * @see #createActionBar(int, ALcyProperties)
   */
  public static final int POP_UP_MENU_ACTION_BAR = 22;
  /**
   * The ID for the table of the customizer panel
   *
   * @see #createTable(int, ALcyProperties)
   */
  public static final int TABLE = 31;
  /**
   * The ID for the default cell renderer provider of the table with ID {@link #TABLE}. Does provider
   * should always be able to provide a renderer, since it will be used as the fallback.
   *
   * @see #createTableCellRendererProvider(int, ALcyProperties)
   */
  public static final int DEFAULT_TABLE_CELL_RENDERER_PROVIDER = 41;
  /**
   * The ID for the model filter cell renderer provider of the table with ID {@link #TABLE}
   *
   * @see #createTableCellRendererProvider(int, ALcyProperties)
   */
  public static final int MODEL_FILTER_CELL_RENDERER_PROVIDER = 42;
  /**
   * The ID for the ISO19013 measure annotation filter cell renderer provider of the table with
   * ID {@link #TABLE}
   *
   * @see #createTableCellRendererProvider(int, ALcyProperties)
   */
  public static final int MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER = 43;

  /**
   * The ID for the table model of the table with ID {@link #TABLE}
   *
   * @see #createTableModel(int, ALcyProperties)
   */
  public static final int TABLE_MODEL = 51;

  /**
   * The ID for the table cell editor provider for the model filter column of the table
   * with ID {@link #TABLE}
   *
   * @see #createTableCellEditorProvider(int, ALcyProperties)
   */
  public static final int MODEL_FILTER_CELL_EDITOR_PROVIDER = 61;

  /**
   * The ID for the table cell editor provider for columns with data property values
   * of the table with ID {@link #TABLE}
   *
   * @see #createTableCellEditorProvider(int, ALcyProperties)
   */
  public static final int DATA_PROPERTY_CELL_EDITOR_PROVIDER = 62;

  /**
   * This array will be used to include all active settables by default. Extensions can still adjust
   * the active settable IDs by using the {@link #getActiveSettableIDs()} method and altering the
   * returned list.
   */
  private static final int[] ALL_ACTIVE_SETTABLES = new int[]{
      AUTO_FIT_ACTIVE_SETTABLE,
      AUTO_CENTER_ACTIVE_SETTABLE,
      AUTO_SELECT_ACTIVE_SETTABLE,
      LOCK_ON_CURRENT_LAYER_ACTIVE_SETTABLE
  };
  /**
   * This array will be used to include all actions by default. Extensions can still adjust the
   * action IDs by using the {@link #getActionIDs()} method and altering the returned list.
   */
  private static final int[] ALL_ACTIONS = new int[]{
      FIND_ACTION,
      EXPORT_TO_CSV_ACTION,
      COPY_ACTION,
      FIT_AND_SELECT_ACTION,
      CENTER_AND_SELECT_ACTION,
      TOGGLE_VISIBILITY_ACTION,
      SHOW_ROW_COUNT_ACTION
  };
  /**
   * This array will be used to include all action bars by default. Extensions can still adjust the
   * action bar IDs by using the {@link #getActionBarIDs()} method and altering the returned list.
   */
  private static final int[] ALL_ACTION_BARS = new int[]{
      TOOL_BAR_ACTION_BAR,
      POP_UP_MENU_ACTION_BAR
  };
  /**
   * This array will be used to include all tables by default. Extensions can still adjust the table
   * IDs by using the {@link #getTableIDs()} method and altering the returned list.
   */
  private static final int[] ALL_TABLES = new int[]{
      TABLE
  };
  /**
   * This array will be used to include all table models by default. Extensions can still adjust the
   * table model IDs by using the {@link #getTableModelIDs()} method and altering the returned
   * list.
   */
  private static final int[] ALL_TABLE_MODELS = new int[]{
      TABLE_MODEL
  };
  /**
   * This array will be used to include all table cell renderer providers by default. Extensions can still
   * adjust the table cell renderer IDs by using the {@link #getTableCellRendererProviderIDs()} method and
   * altering the returned list.
   */
  private static final int[] ALL_TABLE_CELL_RENDERER_PROVIDERS = new int[]{
      MODEL_FILTER_CELL_RENDERER_PROVIDER,
      MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER,
      DEFAULT_TABLE_CELL_RENDERER_PROVIDER
  };
  /**
   * This array will be used to include all table cell editor providers by default. Extensions can still
   * adjust the table cell editor IDs by using the {@link #getTableCellEditorProviderIDs()} method and altering
   * the returned list
   */
  private static final int[] ALL_TABLE_CELL_EDITOR_PROVIDERS = new int[]{
      MODEL_FILTER_CELL_EDITOR_PROVIDER,
      DATA_PROPERTY_CELL_EDITOR_PROVIDER
  };
  /**
   * Name for the {@link #TOOL_BAR_ACTION_BAR} action bar
   */
  private static final String TOOL_BAR_NAME = "tableViewToolBar";
  /**
   * Name for the {@link #POP_UP_MENU_ACTION_BAR} action bar
   */
  private static final String POP_UP_NAME = "tableViewPopupMenu";

  private ILcdIntList fActionBarsIDs;
  private ILcyActionBar[] fActionBars;

  private ILcdIntList fTableIDs;
  private JTable[] fTables;

  private ILcdIntList fTableModelIDs;
  private IExtendedTableModel[] fTableModels;

  private ILcdIntList fTableCellRendererProviderIDs;
  private ITableCellRendererProvider[] fTableCellRendererProviders;

  private ILcdIntList fTableCellEditorProviderIDs;
  private ITableCellEditorProvider[] fTableCellEditorProviders;

  private TableViewCustomizerPanel fCustomizerPanel;

  private RowFilter<TableModel, Integer> fRowFilter;
  private String fPropertiesPrefix;

  //the active settables to fit, select and center will share one ITableViewLogic
  //instance. Therefore this is stored in a field. However, no separate create-get methods available for
  //extensions since this is createActiveSettable specific behavior
  private ITableViewLogic fTableViewLogic;

  /**
   * Create a new <code>TableViewGUIFactory</code> which will use all the IDs specified as public
   * fields of this class.
   *
   * @param aPropertiesPrefix     The prefix which is used in the properties, passed in the {@link
   *                              #createGUI(ALcyProperties)}
   *                              method
   * @param aTableViewLogic Dependency injection for the logic of fitting and/or centering a
   *                              view on certain domain objects. This is used by the {@link
   *                              #AUTO_FIT_ACTIVE_SETTABLE} and the {@link #AUTO_CENTER_ACTIVE_SETTABLE}.Those
   *                              active settables will behave differently for GXY layers and GL
   *                              layers. Must not be <code>null</code>.
   * @param aLucyEnv              The Lucy back-end
   */
  public TableViewGUIFactory(String aPropertiesPrefix,
                             ITableViewLogic aTableViewLogic,
                             ILcyLucyEnv aLucyEnv) {
    this(aPropertiesPrefix,
         aTableViewLogic,
         aLucyEnv,
         ALL_ACTIONS,
         ALL_ACTIVE_SETTABLES,
         new int[]{},
         new int[]{},
         ALL_ACTION_BARS,
         ALL_TABLES,
         ALL_TABLE_MODELS,
         ALL_TABLE_CELL_RENDERER_PROVIDERS,
         ALL_TABLE_CELL_EDITOR_PROVIDERS);
  }

  /**
   * Create a new <code>TableViewGUIFactory</code> which will use all the IDs specified in the
   * constructor.
   *
   * @param aPropertiesPrefix     The prefix which is used in the properties, passed in the {@link
   *                              #createGUI(ALcyProperties)}
   *                              method
   * @param aTableViewLogic       Dependency injection for the logic of fitting and/or centering a
   *                              view on certain domain objects. This is used by the {@link
   *                              #AUTO_FIT_ACTIVE_SETTABLE} and the {@link #AUTO_CENTER_ACTIVE_SETTABLE}.Those
   *                              active settables will behave differently for GXY layers and GL
   *                              layers. Must not be <code>null</code>.
   * @param aLucyEnv              The Lucy back-end
   * @param aActionIDs            All action ID's
   * @param aActiveSettableIDs    All active settable ID's
   * @param aComponentIDs         All Component ID's
   * @param aPanelIDs             All Panel ID's
   * @param aActionBarIDs         All Action Bar ID's
   * @param aTableIDs             All Table ID's
   * @param aTableModelIDs        All Table Model ID's
   * @param aTableCellRendererProviderIDs All Table Cell Renderer ID's
   * @param aTableCellEditorProviderIDs   All Table Cell Editor ID's
   */
  public TableViewGUIFactory(String aPropertiesPrefix,
                             ITableViewLogic aTableViewLogic,
                             ILcyLucyEnv aLucyEnv,
                             int[] aActionIDs,
                             int[] aActiveSettableIDs,
                             int[] aComponentIDs,
                             int[] aPanelIDs,
                             int[] aActionBarIDs,
                             int[] aTableIDs,
                             int[] aTableModelIDs,
                             int[] aTableCellRendererProviderIDs,
                             int[] aTableCellEditorProviderIDs) {
    super(aLucyEnv, aActionIDs, aActiveSettableIDs, aComponentIDs, aPanelIDs);
    fPropertiesPrefix = aPropertiesPrefix;
    fTableViewLogic = aTableViewLogic;
    fActionBarsIDs = createIntList(aActionBarIDs);
    fTableIDs = createIntList(aTableIDs);
    fTableModelIDs = createIntList(aTableModelIDs);
    fTableCellRendererProviderIDs = createIntList(aTableCellRendererProviderIDs);
    fTableCellEditorProviderIDs = createIntList(aTableCellEditorProviderIDs);
  }

  /**
   * Utility method to convert an <code>int[]</code> to an <code>ILcdIntList</code>
   *
   * @param aIntArray the array
   *
   * @return The converted array
   */
  private static ILcdIntList createIntList(int[] aIntArray) {
    ILcdIntList list = new TLcdIntArrayList();
    for (int i : aIntArray) {
      list.add(i);
    }
    return list;
  }

  @Override
  protected TableViewCustomizerPanel createGUIContent(ALcyProperties aProperties) {
    TableViewCustomizerPanel customizerPanel = getCustomizerPanel();

    customizerPanel.setLayout(new BorderLayout());
    JTable table = getTable(TABLE);

    ILcyActionBar toolBar = getActionBar(TOOL_BAR_ACTION_BAR);
    if (toolBar != null) {
      customizerPanel.add(toolBar.getComponent(), BorderLayout.NORTH);
    }
    if (table != null) {
      customizerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
      ILcyActionBar popupMenu = getActionBar(POP_UP_MENU_ACTION_BAR);
      if (popupMenu instanceof ILcyPopupMenu) {
        table.addMouseListener(new TableViewPopupMenuListener(table, (ILcyPopupMenu) popupMenu));
        table.addMouseListener(new TableViewDefaultActionListener(POP_UP_NAME,
                                                                  customizerPanel,
                                                                  getLucyEnv()));
      }
    }

    return customizerPanel;
  }

  @Override
  protected ILcdAction createAction(int aActionID, ALcyProperties aProperties) {
    ILcdAction action = null;
    JTable table = getTable(TABLE);
    switch (aActionID) {
    case FIND_ACTION:
      if (table instanceof JXTable &&
          ((JXTable) table).getSearchable() != null) {
        action = new FindInTableAction(table, ((JXTable) table).getSearchable());
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "findAction");
      }
      break;
    case EXPORT_TO_CSV_ACTION:
      if (table instanceof JXTable) {
        action = new ExportToCSVAction((JXTable) table);
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "exportToCSVAction");
      }
      break;
    case COPY_ACTION:
      if (table != null) {
        action = new CopyAction(table);
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "copyAction");
      }
      break;
    case TOGGLE_VISIBILITY_ACTION:
      if (table instanceof JXTable) {
        if (fTableViewLogic.findOrAddFilter(retrieveModelContextFromProperties(aProperties).getLayer(), (IExtendedTableModel) table.getModel()) != null) {
          action = new AdjustModelObjectFilterAction((JXTable) table);
          action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "toggleVisibilityAction");
        }
      }
      break;
    case FIT_AND_SELECT_ACTION:
      if (table instanceof JXTable) {
        action = new FitAction(getCustomizerPanel().getFitCenterSelectionActiveSettableSupport());
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "fitAction");
      }
      break;
    case CENTER_AND_SELECT_ACTION:
      if (table instanceof JXTable) {
        action = new CenterAction(getCustomizerPanel().getFitCenterSelectionActiveSettableSupport());
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "centerAction");
      }
      break;
    case SHOW_ROW_COUNT_ACTION:
      if (table != null) {
        action = new ShowRowCountAction(table);
        action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "showRowCountAction");
      }
      break;
    default:
      throw new IllegalArgumentException("Action ID[" + aActionID + "] unknown");
    }
    return action;
  }

  @Override
  protected ILcyActiveSettable createActiveSettable(int aActiveSettableID, ALcyProperties aProperties) {
    ILcyActiveSettable activeSettable = null;
    switch (aActiveSettableID) {
    case LOCK_ON_CURRENT_LAYER_ACTIVE_SETTABLE:
      activeSettable = new PinCustomizerPanelActiveSettable(getCustomizerPanel());
      //set the TLcyActionBarUtil.ID_KEY, allowing to insert the active settable in the configured
      //action bars by the ALcyGUIFactory class
      activeSettable.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "lockOnCurrentLayerActiveSettable");
      break;
    case AUTO_FIT_ACTIVE_SETTABLE:
      if (getTable(TABLE) != null) {
        activeSettable = new AutoFitActiveSettable(getCustomizerPanel());
        activeSettable.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "autoFitActiveSettable");
      }
      break;
    case AUTO_CENTER_ACTIVE_SETTABLE:
      if (getTable(TABLE) != null) {
        activeSettable = new AutoCenterActiveSettable(getCustomizerPanel());
        activeSettable.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "autoCenterActiveSettable");
      }
      break;
    case AUTO_SELECT_ACTIVE_SETTABLE:
      if (getTable(TABLE) != null) {
        activeSettable = new AutoSelectActiveSettable(getCustomizerPanel());
        activeSettable.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + "autoSelectActiveSettable");
      }
      break;
    default:
      throw new IllegalArgumentException("Active settable ID[" + aActiveSettableID + "] unknown");
    }
    return activeSettable;
  }

  @Override
  protected Component createComponent(int aComponentID, ALcyProperties aProperties) {
    throw new IllegalArgumentException("Component ID[" + aComponentID + "] unknown");
  }

  @Override
  protected Component createPanel(int aPanelID, ALcyProperties aProperties) {
    throw new IllegalArgumentException("Panel ID[" + aPanelID + "] unknown");
  }

  /**
   * <p>Returns the list with all action bar IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing action bar IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the action bar IDs
   */
  protected ILcdIntList getActionBarIDs() {
    return fActionBarsIDs;
  }

  /**
   * <p>Returns the list with all table IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing table IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the table IDs
   */
  protected ILcdIntList getTableIDs() {
    return fTableIDs;
  }

  /**
   * <p>Returns the list with all table model IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing table model IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the table model IDs
   */
  protected ILcdIntList getTableModelIDs() {
    return fTableModelIDs;
  }

  /**
   * <p>Returns the list with all table cell renderer provider IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing table cell renderer provider IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the table cell renderer provider IDs
   */
  protected ILcdIntList getTableCellRendererProviderIDs() {
    return fTableCellRendererProviderIDs;
  }

  /**
   * <p>Returns the list with all table cell editor provider IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing table cell editor IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the table cell editor provider IDs
   */
  protected ILcdIntList getTableCellEditorProviderIDs() {
    return fTableCellEditorProviderIDs;
  }

  /**
   * This method creates the customizer panel using the {@link #createCustomizerPanel(ALcyProperties)
   * createCustomizerPanel} method and stores it. Access to the created customizer panel is achieved
   * through the {@link #getCustomizerPanel() getCustomizerPanel} method
   *
   * @param aProperties The properties. Can be used to customize the behavior of this factory
   */
  private void setupCustomizerPanel(ALcyProperties aProperties) {
    fCustomizerPanel = createCustomizerPanel(aProperties);
    if (fCustomizerPanel == null) {
      throw new UnsupportedOperationException("The created customizer panel must not be null");
    }
  }

  /**
   * <p>Returns a new <code>TableViewCustomizerPanel</code>. This customizer panel should not
   * contain anything.</p>
   *
   * <p>The contents of the customizer panel will be created by the other methods of this factory,
   * and set on the customizer panel afterwards.</p>
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   *
   * @return a new <code>TableViewCustomizerPanel</code>. Must not be <code>null</code>.
   */
  protected TableViewCustomizerPanel createCustomizerPanel(ALcyProperties aProperties) {
    ITableViewLogic tableViewLogic = getTableViewLogic();
    return new TableViewCustomizerPanel(new TableViewFilter(tableViewLogic),
                                        TLcyLang.getString("Table view"),
                                        getLucyEnv(),
                                        tableViewLogic,
                                        retrieveModelContextFromProperties(aProperties));
  }

  /**
   * <p>Returns the <code>ILcyCustomizerPanel</code> currently under construction.</p>
   *
   * <p>The panel is originally created in {@link #createCustomizerPanel(ALcyProperties)
   * createCustomizerPanel} and the contents is created and added in the other methods of this
   * factory.</p>
   *
   * @return the <code>ILcyCustomizerPanel</code> currently under construction
   */
  public TableViewCustomizerPanel getCustomizerPanel() {
    return fCustomizerPanel;
  }

  /**
   * This method creates all the action bars using the {@link #createActionBar(int,
   * ALcyProperties)} method with the IDs passed in the
   * constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupActionBars(ALcyProperties aProperties) {
    fActionBars = new ILcyActionBar[fActionBarsIDs.size()];
    TLcyActionBarManager actionBarManager = getLucyEnv().getUserInterfaceManager().getActionBarManager();
    for (int i = 0; i < fActionBarsIDs.size(); i++) {
      Integer actionBarID = fActionBarsIDs.get(i);
      ILcyActionBar actionBar = createActionBar(actionBarID, aProperties);
      fActionBars[i] = actionBar;
      if (actionBar != null) {
        switch (actionBarID) {
        case TOOL_BAR_ACTION_BAR:
          TLcyActionBarUtil.setupAsConfiguredActionBar(actionBar, TOOL_BAR_NAME, getCustomizerPanel(), aProperties, fPropertiesPrefix, getCustomizerPanel(), actionBarManager);
          break;
        case POP_UP_MENU_ACTION_BAR:
          TLcyActionBarUtil.setupAsConfiguredActionBar(actionBar, POP_UP_NAME, getCustomizerPanel(), aProperties, fPropertiesPrefix, getCustomizerPanel(), actionBarManager);
          break;
        }
      }
    }
  }

  /**
   * <p>Creates an <code>ILcyActionBar</code> for the given ID.</p>
   *
   * @param aActionBarID The ID describing which action bar to create.
   * @param aProperties  The properties. Can be used to tune the behavior of this factory.
   *
   * @return The newly created <code>ILcyActionBar</code>, or <code>null</code>
   */
  protected ILcyActionBar createActionBar(int aActionBarID, ALcyProperties aProperties) {
    ILcyActionBar bar;
    switch (aActionBarID) {
    case TOOL_BAR_ACTION_BAR:
      bar = new TLcyToolBar(getLucyEnv().getHelpManager());
      break;
    case POP_UP_MENU_ACTION_BAR:
      bar = new TLcyPopupMenu(new JPopupMenu());
      break;
    default:
      throw new IllegalArgumentException("Action bar ID[" + aActionBarID + "] unknown");
    }
    return bar;
  }

  /**
   * <p>Returns the <code>ILcyActionBar</code> for the given ID.</p>
   *
   * @param aActionBarID The ID describing which action bar to return.
   *
   * @return the <code>ILcyActionBar</code> for the given ID.
   */
  public ILcyActionBar getActionBar(int aActionBarID) {
    return objectForID(aActionBarID, fActionBarsIDs, fActionBars);
  }

  /**
   * <p>Creates a <code>RowFilter</code>.</p>
   *
   * @param aProperties  The properties. Can be used to tune the behavior of this factory.
   *
   * @return The newly created <code>RowFilter</code>, or <code>null</code>
   */
  protected RowFilter<TableModel, Integer> createRowFilter(ALcyProperties aProperties) {
    TLcyModelContext modelContext = retrieveModelContextFromProperties(aProperties);
    return new LayerRowFilter(fTableViewLogic, modelContext);
  }

  /**
   * <p>Returns the <code>RowFilter</code>.</p>
   *   *
   * @return the <code>RowFilter</code>.
   */
  public RowFilter<TableModel, Integer> getRowFilter() {
    return fRowFilter;
  }

  /**
   * This method creates the row filter using the {link #createRowFilter} method.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupRowFilter(ALcyProperties aProperties) {
    fRowFilter = createRowFilter(aProperties);
  }

  /**
   * This method creates all the tables using the {@link #createTable(int,
   * ALcyProperties)}  method with the IDs passed in the
   * constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupTables(ALcyProperties aProperties) {
    fTables = new JTable[fTableIDs.size()];
    for (int i = 0; i < fTableIDs.size(); i++) {
      fTables[i] = createTable(fTableIDs.get(i), aProperties);
      if (fTableIDs.get(i) == TABLE && fTables[i] != null) {
        getCustomizerPanel().setTable(fTables[i]);
      }
    }
  }

  /**
   * <p>Creates a <code>JTable</code> for the given ID.</p>
   *
   * @param aTableID    The ID describing which table to create
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   *
   * @return the <code>JTable</code> for the given ID. May be <code>null</code>
   */
  protected JTable createTable(int aTableID, ALcyProperties aProperties) {
    JTable table = null;
    switch (aTableID) {
    case TABLE:
      //due to the order in the setup method, the table model and renderer providers are already created,
      //and available through the getters
      IExtendedTableModel extendedTableModel = getTableModel(TABLE_MODEL);
      if (extendedTableModel != null) {
        TLcyModelContext modelContext = retrieveModelContextFromProperties(aProperties);
        EditableTable jxTable = new EditableTable(extendedTableModel);
        jxTable.setTransferHandler(new TableTransferHandler(jxTable));
        jxTable.setModel(extendedTableModel);
        //Add row filter.
        if (getRowFilter() != null) {
          jxTable.setRowFilter(getRowFilter());
        }

        //allow to toggle between ascending,descending,none ordering
        jxTable.setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);

        //match the initial selection of the table with the selection of the layer
        ILcdLayer layer = modelContext.getLayer();
        if (layer != null) {
          Enumeration enumeration = layer.selectedObjects();
          while (enumeration.hasMoreElements()) {
            Object selectedObjectOnMap = enumeration.nextElement();
            Collection<Object> objects = extendedTableModel.transformedToOriginal(selectedObjectOnMap);
            for (Object object : objects) {
              int modelIndex = extendedTableModel.getRowOfObject(object);
              int viewIndex = jxTable.convertRowIndexToView(modelIndex);
              if (viewIndex != -1) {
                jxTable.getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
              }
            }
          }
        }

        //compose the renderer providers
        CompositeTableCellRendererProvider rendererProvider = new CompositeTableCellRendererProvider();
        ITableCellRendererProvider tableCellRendererProvider = getTableCellRendererProvider(MODEL_FILTER_CELL_RENDERER_PROVIDER);
        if (tableCellRendererProvider != null) {
          //Add with high priority, it is unlikely any renderer takes precedence for the first column.
          rendererProvider.add(tableCellRendererProvider, 50);
        }
        tableCellRendererProvider = getTableCellRendererProvider(MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER);
        if (tableCellRendererProvider != null) {
          rendererProvider.add(tableCellRendererProvider);
        }
        //Fallback renderer, can render any cell if needed.
        tableCellRendererProvider = getTableCellRendererProvider(DEFAULT_TABLE_CELL_RENDERER_PROVIDER);
        if (tableCellRendererProvider != null) {
          rendererProvider.add(tableCellRendererProvider, TLcyGenericComposite.PRIORITY_FALLBACK);
        }
        jxTable.setRendererProvider(rendererProvider);

        //compose the editor providers
        CompositeTableCellEditorProvider editorProvider = new CompositeTableCellEditorProvider();
        ITableCellEditorProvider tableCellEditorProvider = getTableCellEditorProvider(MODEL_FILTER_CELL_EDITOR_PROVIDER);
        if (tableCellEditorProvider != null) {
          editorProvider.add(tableCellEditorProvider);
        }
        tableCellEditorProvider = getTableCellEditorProvider(DATA_PROPERTY_CELL_EDITOR_PROVIDER);
        if (tableCellEditorProvider != null) {
          editorProvider.add(tableCellEditorProvider);
        }
        jxTable.setEditorProvider(editorProvider);

        TLcyModelObjectFilter filter = fTableViewLogic.findOrAddFilter(modelContext.getLayer(), extendedTableModel);
        if (filter != null) {
          //avoid that clicking on the checkbox column header would trigger a sort operation
          jxTable.getColumnExt(ModelFilterTableModelDecorator.DISPLAY_COLUMN_INDEX).setSortable(false);

          //set renderer that adds checkbox to header of first column
          ModelObjectFilterTableHeaderRenderer.install(jxTable, extendedTableModel, filter);
        }

        jxTable.setHorizontalScrollEnabled(true);
        jxTable.setColumnControlVisible(true);

        // Set up alternate row coloring. Provide roll-over effect for editable cells.
        Color editableColor = UIColors.getUIColor("nimbusOrange", new Color(255, 195, 107));
        jxTable.setHighlighters(
            new HighlighterFactory.UIColorHighlighter(HighlightPredicate.ODD),
            new ColorHighlighter(new AndHighlightPredicate(EDITABLE, ROLLOVER_CELL), editableColor, null));

        //set proto-type value on each column. Just search the first 10 rows of the table model
        //and use the longest value. This avoids that for very large models the calculation of the
        //column sizes takes too much time
        int columnCount = jxTable.getColumnCount();
        int rowCount = jxTable.getRowCount();
        for (int column = 0; column < columnCount; column++) {
          Object prototypeValue = null;
          Dimension prototypeSize = null;
          for (int row = 0; row < 10 && row < rowCount; row++) {
            Object valueAt = jxTable.getValueAt(row, column);
            if (prototypeValue == null) {
              prototypeValue = valueAt;

              Component prototypeComponent = jxTable.getCellRenderer(row, column).getTableCellRendererComponent(jxTable,
                                                                                                                prototypeValue,
                                                                                                                false,
                                                                                                                false,
                                                                                                                row,
                                                                                                                column);
              prototypeSize = prototypeComponent.getPreferredSize();
            } else {
              //compare the value with the current prototype
              Component currentComponent = jxTable.getCellRenderer(row, column).getTableCellRendererComponent(jxTable,
                                                                                                              valueAt,
                                                                                                              false,
                                                                                                              false,
                                                                                                              row,
                                                                                                              column);
              Dimension currentSize = currentComponent.getPreferredSize();
              if (currentSize.width > prototypeSize.width) {
                prototypeValue = valueAt;
                prototypeSize = currentSize;
              }
            }
          }
          if (prototypeValue != null) {
            jxTable.getColumnExt(column).setPrototypeValue(prototypeValue);
          }
        }

        //remove some of the default key bindings
        jxTable.getActionMap().remove("find");
        jxTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(MetaKeyUtil.getCMDModifierKey() + " F"));
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyStroke.getKeyStroke("SPACE").getKeyCode(), 0, true);
        jxTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(keyStroke);
        //remove original entry in input map
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, MetaKeyUtil.getCMDDownMask());
        //remove did not work, override does.
        jxTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, "a random string");

        InputMap parent = jxTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent();
        if (parent != null) {
          parent.remove(KeyStroke.getKeyStroke("ENTER"));
        }

        table = jxTable;
      }
      break;
    default:
      throw new IllegalArgumentException("Table ID[" + aTableID + "] unknown");
    }
    return table;
  }

  /**
   * <p>Returns the <code>JTable</code> for the given ID.</p>
   *
   * @param aTableID The ID describing which table to return
   *
   * @return the <code>JTable</code> for the given ID.
   */
  public JTable getTable(int aTableID) {
    return objectForID(aTableID, fTableIDs, fTables);
  }

  /**
   * This method creates all the table cell renderer providers using the {@link #createTableCellRendererProvider(int,
   * ALcyProperties)}  method with the IDs passed in the
   * constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupTableCellRendererProviders(ALcyProperties aProperties) {
    //noinspection unchecked
    fTableCellRendererProviders = new ITableCellRendererProvider[fTableCellRendererProviderIDs.size()];
    for (int i = 0; i < fTableCellRendererProviderIDs.size(); i++) {
      fTableCellRendererProviders[i] = createTableCellRendererProvider(fTableCellRendererProviderIDs.get(i), aProperties);
    }
  }

  /**
   * <p>Creates a <code>ITableCellRendererProvider</code> for the given ID.</p>
   *
   * @param aTableCellRendererProviderID The ID describing which table cell renderer mapping to create
   * @param aProperties          The properties. Can be used to tune the behavior of this factory
   *
   * @return the <code>ITableCellRendererProvider</code> for the given ID. May be
   *         <code>null</code>
   */
  protected ITableCellRendererProvider createTableCellRendererProvider(int aTableCellRendererProviderID, ALcyProperties aProperties) {
    ITableCellRendererProvider result = null;
    TLcyModelContext modelContext = retrieveModelContextFromProperties(aProperties);
    IExtendedTableModel tableModel = getTableModel(TABLE_MODEL);
    switch (aTableCellRendererProviderID) {
    case MODEL_FILTER_CELL_RENDERER_PROVIDER:
      if (tableModel != null) {
        TLcyModelObjectFilter modelObjectFilter = fTableViewLogic.findOrAddFilter(modelContext.getLayer(), tableModel);
        if (modelObjectFilter != null) {
          ILcdFilter<?> filter = fTableViewLogic.retrieveLayerFilter(modelContext.getLayer());
          if (filter != null) {
            ModelObjectFilterTableCellRendererEditor renderer =
                new ModelObjectFilterTableCellRendererEditor();
            result = new ModelObjectFilterRendererEditorProvider(renderer);
          }
        }
      }
      break;
    case MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER:
      result = new TableMeasureAnnotationCellRendererProvider(getLucyEnv());
      break;
    case DEFAULT_TABLE_CELL_RENDERER_PROVIDER:
      if (tableModel != null) {
        result = new DefaultTableCellRendererProvider(tableModel, getLucyEnv());
      }
      break;
    default:
      throw new IllegalArgumentException("Table cell renderer provider ID[" + aTableCellRendererProviderID + "] unknown");
    }
    return result;
  }

  /**
   * <p>Returns the table cell renderer provider for the given ID.</p>
   *
   * @param aTableCellRendererProviderID The ID of the table cell renderer
   *
   * @return the mapping between classes and <code>TableCellRenderer</code> for the given ID
   */
  public ITableCellRendererProvider getTableCellRendererProvider(int aTableCellRendererProviderID) {
    return objectForID(aTableCellRendererProviderID, fTableCellRendererProviderIDs, fTableCellRendererProviders);
  }

  /**
   * This method creates all the table cell editor providers using the
   * {@link #createTableCellEditorProvider(int, ALcyProperties)}
   * method with the IDs passed in the constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupTableCellEditorProviders(ALcyProperties aProperties) {
    //noinspection unchecked
    fTableCellEditorProviders = new ITableCellEditorProvider[fTableCellEditorProviderIDs.size()];
    for (int i = 0; i < fTableCellEditorProviderIDs.size(); i++) {
      fTableCellEditorProviders[i] = createTableCellEditorProvider(fTableCellEditorProviderIDs.get(i), aProperties);
    }
  }

  /**
   * <p>Creates a mapping between classes and <code>TableCellEditor</code>s for the given ID.</p>
   *
   * @param aTableCellEditorProviderID The ID describing which table cell editor mapping to create
   * @param aProperties          The properties. Can be used to tune the behavior of this factory
   *
   * @return the mapping between classes and <code>TableCellEditor</code> for the given ID. May be
   *         <code>null</code>
   */
  protected ITableCellEditorProvider createTableCellEditorProvider(int aTableCellEditorProviderID, ALcyProperties aProperties) {
    TLcyModelContext modelContext = retrieveModelContextFromProperties(aProperties);
    IExtendedTableModel tableModel = getTableModel(TABLE_MODEL);
    switch (aTableCellEditorProviderID) {
    case MODEL_FILTER_CELL_EDITOR_PROVIDER:
      if (tableModel != null) {
        //make sure the checkboxes are disabled
        TLcyModelObjectFilter modelObjectFilter = fTableViewLogic.findOrAddFilter(modelContext.getLayer(), tableModel);
        if (modelObjectFilter != null) {
          ILcdFilter<?> filter = fTableViewLogic.retrieveLayerFilter(modelContext.getLayer());
          if (filter != null) {
            ModelObjectFilterTableCellRendererEditor editor = new ModelObjectFilterTableCellRendererEditor();
            return new ModelObjectFilterRendererEditorProvider(editor);
          }
        }
      }
      break;
    case DATA_PROPERTY_CELL_EDITOR_PROVIDER:
      return new DataPropertyEditorProvider(getLucyEnv(), modelContext);
    default:
      throw new IllegalArgumentException("Table cell editor provider ID[" + aTableCellEditorProviderID + "] unknown");
    }
    return null;
  }

  /**
   * <p>Returns the mapping between classes and <code>TableCellEditor</code> for the given ID.</p>
   *
   * @param aTableCellEditorProviderID The ID of the table cell editor
   *
   * @return the mapping between classes and <code>TableCellEditor</code> for the given ID
   */
  public ITableCellEditorProvider getTableCellEditorProvider(int aTableCellEditorProviderID) {
    return objectForID(aTableCellEditorProviderID, fTableCellEditorProviderIDs, fTableCellEditorProviders);
  }

  /**
   * This method creates all the table models using the {@link #createTableModel(int,
   * ALcyProperties)}  method with the IDs passed in the
   * constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupTableModels(ALcyProperties aProperties) {
    fTableModels = new IExtendedTableModel[fTableModelIDs.size()];
    for (int i = 0; i < fTableModelIDs.size(); i++) {
      fTableModels[i] = createTableModel(fTableModelIDs.get(i), aProperties);
    }
  }

  /**
   * <p>Creates a <code>TableModel</code> for the given ID.</p>
   *
   * @param aTableModelID The ID describing which table model to create
   * @param aProperties   The properties. Can be used to tune the behavior of this factory
   *
   * @return the <code>TableModel</code> for the given ID. May be <code>null</code>
   */
  protected IExtendedTableModel createTableModel(int aTableModelID, ALcyProperties aProperties) {
    IExtendedTableModel tableModel = null;
    switch (aTableModelID) {
    case TABLE_MODEL:
      TLcyModelContext modelContext = retrieveModelContextFromProperties(aProperties);

      ILcdModel model = modelContext.getModel();
      if (DataObjectTableModel.acceptsDataObjectModel(modelContext.getModel())) {
        DataObjectTableModel dataObjectTableModel = new DataObjectTableModel(model);
        dataObjectTableModel.addUndoableListener(getLucyEnv().getUndoManager());
        tableModel = dataObjectTableModel;
      } else if (FeaturedTableModel.acceptsFeaturedModel(model)) {
        tableModel = new FeaturedTableModel(model);
      }
      //if possible, wrap the model with a ModelFilterTableModelDecorator
      TLcyModelObjectFilter filter = fTableViewLogic.findOrAddFilter(modelContext.getLayer(), tableModel);
      ILcdFilter<?> layerFilter = fTableViewLogic.retrieveLayerFilter(modelContext.getLayer());
      if (filter != null && tableModel != null && layerFilter != null) {
        tableModel = new ModelFilterTableModelDecorator(tableModel.getOriginalModel(),
                                                        tableModel,
                                                        filter,
                                                        layerFilter);
      }

      break;
    default:
      throw new IllegalArgumentException("Table model ID[" + aTableModelID + "] unknown");
    }
    return tableModel;
  }

  /**
   * <p>Returns the <code>TableModel</code> for the given ID.</p>
   *
   * @param aTableModelID The ID of the table model
   *
   * @return the <code>TableModel</code> for the given ID
   */
  public IExtendedTableModel getTableModel(int aTableModelID) {
    return objectForID(aTableModelID, fTableModelIDs, fTableModels);
  }

  @Override
  protected void setup(final ALcyProperties aProperties) {
    // See javadoc of setup method on threading
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        setupCustomizerPanel(aProperties);
        setupActionBars(aProperties);
        setupTableModels(aProperties);
        setupTableCellRendererProviders(aProperties);
        setupTableCellEditorProviders(aProperties);
        setupRowFilter(aProperties);
        setupTables(aProperties);
      }
    });
    super.setup(aProperties);
  }

  @Override
  protected void cleanup(ALcyProperties aProperties) {
    super.cleanup(aProperties);
    fActionBars = null;
    fCustomizerPanel = null;
    fTables = null;
    fTableModels = null;
    fTableCellRendererProviders = null;
    fTableCellEditorProviders = null;
    fRowFilter = null;
  }

  private <T> T objectForID(int aID, ILcdIntList aIDs, T[] aObjects) {
    for (int i = 0; i < aIDs.size(); i++) {
      if (aIDs.get(i) == aID) {
        return aObjects[i];
      }
    }
    throw new IllegalArgumentException("Given ID[" + aID + "] is an unknown ID.");
  }

  private TLcyModelContext retrieveModelContextFromProperties(ALcyProperties aProperties) {
    Object modelContext = aProperties.get(fPropertiesPrefix + MODEL_CONTEXT_KEY, null);
    if (modelContext == null) {
      throw new IllegalArgumentException("The properties passed to this factory must contain the model context. See TableViewGUIFactory.MODEL_CONTEXT_KEY");
    }
    return (TLcyModelContext) modelContext;
  }

  /**
   * Returns the prefix used in the properties
   *
   * @return the prefix used in the properties
   */
  protected String getPropertiesPrefix() {
    return fPropertiesPrefix;
  }

  @Override
  protected Object getActionContext(int aActionID, ALcyProperties aProperties) {
    return getCustomizerPanel();
  }

  @Override
  protected Object getActiveSettableContext(int aActiveSettableID, ALcyProperties aProperties) {
    return getCustomizerPanel();
  }

  /**
   * Returns the logic to center/fit on selected objects, passed in the constructor
   *
   * @return the logic to center/fit on selected objects, passed in the constructor
   */
  public ITableViewLogic getTableViewLogic() {
    return fTableViewLogic;
  }
}
