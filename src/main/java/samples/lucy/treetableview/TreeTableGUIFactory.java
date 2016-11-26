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

import static org.jdesktop.swingx.decorator.HighlightPredicate.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import samples.common.OptionsPanelScrollPane;
import samples.common.UIColors;
import samples.lucy.tableview.CompositeTableCellEditorProvider;
import samples.lucy.tableview.CompositeTableCellRendererProvider;
import samples.lucy.tableview.ITableCellEditorProvider;
import samples.lucy.tableview.ITableCellRendererProvider;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyActionBar;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.util.TLcyGenericComposite;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.util.collections.ILcdIntList;
import com.luciad.util.collections.TLcdIntArrayList;

/**
 * <p><code>ALcyGUIFactory</code> implementation responsible for the creation of customizer panels
 * featuring a tree table view.</p>
 */
public class TreeTableGUIFactory extends ALcyGUIFactory<TreeTableViewCustomizerPanel> {
  /**
   * The properties passed to this factory must contain a <code>TLcyDomainObjectContext</code>. This
   * <code>TLcyModelContext</code> should be stored using this key.
   */
  public static final String DOMAIN_OBJECT_CONTEXT_KEY = "domainObjectContext";
  /**
   * The ID of the action capable of expanding an entire tree
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int EXPAND_ALL_ACTION = 1;
  /**
   * The ID of the action capable of collapsing an entire tree
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int COLLAPSE_ALL_ACTION = 2;

  /**
   * The ID of the action capable of selecting and fitting the view on the object
   *
   * @see #createAction(int, ALcyProperties)
   */
  public static final int SELECT_AND_FIT_ACTION = 3;

  /**
   * The ID of the active settable capable of hiding the empty nodes
   *
   * @see #createActiveSettable(int, ALcyProperties)
   */
  public static final int HIDE_EMPTY_NODES_ACTIVE_SETTABLE = 11;
  /**
   * The ID of the {@link JXTreeTable}
   *
   * @see #createTreeTable(int, ALcyProperties)
   */
  public static final int TREE_TABLE_TREE_TABLE = 21;
  /**
   * The ID of the model of the {@link #TREE_TABLE_TREE_TABLE}
   *
   * @see #createTreeTableModel(int, ALcyProperties)
   */
  public static final int TREE_TABLE_MODEL_TREE_TABLE = 31;
  /**
   * The ID of the default renderer provider of the {@link #TREE_TABLE_TREE_TABLE}. This provider
   * should be able to provide a renderer for all the cells in the second column, since it is the fallback.
   *
   * @see #createTreeTableCellRendererProvider(int, ALcyProperties)
   */
  public static final int DEFAULT_CELL_RENDERER_PROVIDER_TREE_TABLE = 41;
  /**
   * The ID of the measure annotation renderer provider of the {@link #TREE_TABLE_TREE_TABLE}
   *
   * @see #createTreeTableCellRendererProvider(int, ALcyProperties)
   */
  public static final int MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER_TREE_TABLE = 42;
  /**
   * The ID of the editor provider of the {@link #TREE_TABLE_TREE_TABLE}
   *
   * @see #createTreeTableCellEditorProvider(int, ALcyProperties)
   */
  public static final int TREE_TABLE_CELL_EDITOR_PROVIDER_TREE_TABLE = 51;
  /**
   * The ID of the tool bar
   *
   * @see #createActionBar(int, ALcyProperties)
   */
  public static final int TOOL_BAR_ACTION_BAR = 61;
  private static final String TOOL_BAR_NAME = "treeTableViewToolBar";

  private static final int[] ALL_ACTIONS = new int[]{
      EXPAND_ALL_ACTION,
      COLLAPSE_ALL_ACTION,
      SELECT_AND_FIT_ACTION
  };
  private static final int[] ALL_ACTIVE_SETTABLES = new int[]{
      HIDE_EMPTY_NODES_ACTIVE_SETTABLE
  };
  private static final int[] ALL_TREE_TABLES = new int[]{
      TREE_TABLE_TREE_TABLE
  };
  private static final int[] ALL_TREE_TABLE_MODELS = new int[]{
      TREE_TABLE_MODEL_TREE_TABLE
  };
  private static final int[] ALL_TREE_TABLE_CELL_RENDERER_PROVIDERS = new int[]{
      MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER_TREE_TABLE,
      DEFAULT_CELL_RENDERER_PROVIDER_TREE_TABLE
  };
  private static final int[] ALL_TREE_TABLE_CELL_EDITOR_PROVIDERS = new int[]{
      TREE_TABLE_CELL_EDITOR_PROVIDER_TREE_TABLE
  };
  private static final int[] ALL_ACTION_BARS = new int[]{
      TOOL_BAR_ACTION_BAR
  };
  private String fPropertiesPrefix;

  private ILcdIntList fTreeTableIDs;
  private JXTreeTable[] fTreeTables;

  private ILcdIntList fTreeTableModelIDs;
  private TreeTableModel[] fTreeTableModels;

  private ILcdIntList fTreeTableCellRendererProviderIDs;
  private ITableCellRendererProvider[] fTreeTableCellRendererProviders;

  private ILcdIntList fTreeTableCellEditorProviderIDs;
  private ITableCellEditorProvider[] fTreeTableCellEditorsProviders;

  private ILcdIntList fActionBarIDs;
  private ILcyActionBar[] fActionBars;
  private TreeTableViewCustomizerPanel fCustomizerPanel;

  /**
   * Create a new <code>TreeTableGUIFactory</code> which will use all the IDs specified as public
   * fields of this class.
   *
   * @param aPropertiesPrefix The prefix which is used in the properties, passed in the {@link
   *                          #createGUI(ALcyProperties)} method
   * @param aLucyEnv          The Lucy back-end
   */
  public TreeTableGUIFactory(String aPropertiesPrefix, ILcyLucyEnv aLucyEnv) {
    this(aPropertiesPrefix,
         aLucyEnv,
         ALL_ACTIONS,
         ALL_ACTIVE_SETTABLES,
         new int[0],
         new int[0],
         ALL_ACTION_BARS,
         ALL_TREE_TABLES,
         ALL_TREE_TABLE_MODELS,
         ALL_TREE_TABLE_CELL_RENDERER_PROVIDERS,
         ALL_TREE_TABLE_CELL_EDITOR_PROVIDERS);
  }

  /**
   * Create a new <code>TableViewGUIFactory</code> which will use all the IDs specified in the
   * constructor.
   *
   * @param aPropertiesPrefix         The prefix which is used in the properties, passed in the
   *                                  {@link #createGUI(ALcyProperties)}
   *                                  method
   * @param aLucyEnv                  The Lucy back-end
   * @param aActionIDs                All action ID's
   * @param aActiveSettableIDs        All active settable ID's
   * @param aComponentIDs             All Component ID's
   * @param aPanelIDs                 All Panel ID's
   * @param aActionBarIDs             All Action Bar ID's
   * @param aTreeTableIDs             All TreeTable ID's
   * @param aTreeTableModelIDs        All TreeTable Model ID's
   * @param aTreeTableCellRendererProviderIDs All TreeTable Cell Renderer ID's
   * @param aTreeTableCellEditorProviderIDs All TreeTable Cell Editor ID's
   */
  public TreeTableGUIFactory(String aPropertiesPrefix,
                             ILcyLucyEnv aLucyEnv,
                             int[] aActionIDs,
                             int[] aActiveSettableIDs,
                             int[] aComponentIDs,
                             int[] aPanelIDs,
                             int[] aActionBarIDs,
                             int[] aTreeTableIDs,
                             int[] aTreeTableModelIDs,
                             int[] aTreeTableCellRendererProviderIDs,
                             int[] aTreeTableCellEditorProviderIDs) {
    super(aLucyEnv, aActionIDs, aActiveSettableIDs, aComponentIDs, aPanelIDs);
    fPropertiesPrefix = aPropertiesPrefix;
    fActionBarIDs = createIntList(aActionBarIDs);
    fTreeTableIDs = createIntList(aTreeTableIDs);
    fTreeTableModelIDs = createIntList(aTreeTableModelIDs);
    fTreeTableCellRendererProviderIDs = createIntList(aTreeTableCellRendererProviderIDs);
    fTreeTableCellEditorProviderIDs = createIntList(aTreeTableCellEditorProviderIDs);
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
  protected TreeTableViewCustomizerPanel createGUIContent(ALcyProperties aProperties) {
    TreeTableViewCustomizerPanel customizerPanel = getCustomizerPanel();
    customizerPanel.setLayout(new BorderLayout());
    JXTreeTable treeTable = getTreeTable(TREE_TABLE_TREE_TABLE);
    if (treeTable != null) {
      OptionsPanelScrollPane scrollPane = new OptionsPanelScrollPane(treeTable);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      customizerPanel.add(scrollPane, BorderLayout.CENTER);
    }
    ILcyActionBar toolBar = getActionBar(TOOL_BAR_ACTION_BAR);
    if (toolBar != null) {
      customizerPanel.add(toolBar.getComponent(), BorderLayout.NORTH);
    }
    return customizerPanel;
  }

  @Override
  protected ILcdAction createAction(int aActionID, ALcyProperties aProperties) {
    switch (aActionID) {
    case EXPAND_ALL_ACTION:
      JXTreeTable treeTable = getTreeTable(TREE_TABLE_TREE_TABLE);
      if (treeTable != null) {
        TreeExpandAction action = new TreeExpandAction(true, treeTable);
        action.putValue(TLcyActionBarUtil.ID_KEY, getPropertiesPrefix() + "expandAllAction");
        return action;
      }
      return null;
    case COLLAPSE_ALL_ACTION:
      treeTable = getTreeTable(TREE_TABLE_TREE_TABLE);
      if (treeTable != null) {
        TreeExpandAction action = new TreeExpandAction(false, treeTable);
        action.putValue(TLcyActionBarUtil.ID_KEY, getPropertiesPrefix() + "collapseAllAction");
        return action;
      }
      return null;
    case SELECT_AND_FIT_ACTION:
      TLcyDomainObjectContext domainObjectContext = retrieveDomainObjectContext(aProperties);
      SelectAndFitAction action = new SelectAndFitAction(domainObjectContext, getLucyEnv());
      action.putValue(TLcyActionBarUtil.ID_KEY, getPropertiesPrefix() + "selectAndFitAction");
      return action;
    default:
      throw new IllegalArgumentException("Action ID[" + aActionID + "] unknown");
    }
  }

  @Override
  protected ILcyActiveSettable createActiveSettable(int aActiveSettableID, ALcyProperties aProperties) {
    switch (aActiveSettableID) {
    case HIDE_EMPTY_NODES_ACTIVE_SETTABLE:
      HideEmptyActiveSettable activeSettable = new HideEmptyActiveSettable(getCustomizerPanel()
      );
      activeSettable.putValue(TLcyActionBarUtil.ID_KEY, getPropertiesPrefix() + "hideEmptyActiveSettable");
      return activeSettable;
    default:
      throw new IllegalArgumentException("Active settable ID[" + aActiveSettableID + "] unknown");
    }
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
   * This function creates all the <code>JXTreeTable</code>s using {@link #createTreeTable(int,
   * ALcyProperties)} with all given tree table ID's (given in
   * constructor).
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory.
   */
  private void setupTreeTables(ALcyProperties aProperties) {
    fTreeTables = new JXTreeTable[fTreeTableIDs.size()];
    for (int i = 0; i < fTreeTableIDs.size(); i++) {
      fTreeTables[i] = createTreeTable(fTreeTableIDs.get(i), aProperties);
    }
  }

  /**
   * Creates a <code>JXTreeTable</code> for the given ID.
   *
   * @param aTreeTableID The ID describing which <code>JXTreeTable</code> to create
   * @param aProperties  The properties. Can be used to tune the behavior of this factory
   *
   * @return the newly created <code>JXTreeTable</code>, or <code>null</code>
   */
  protected JXTreeTable createTreeTable(int aTreeTableID, ALcyProperties aProperties) {
    switch (aTreeTableID) {
    case TREE_TABLE_TREE_TABLE:
      TreeTableModel model = getTreeTableModel(TREE_TABLE_MODEL_TREE_TABLE);
      if (model != null) {
        TLcyDomainObjectContext domainObjectContext = retrieveDomainObjectContext(aProperties);
        EditableTreeTable table = new EditableTreeTable(domainObjectContext.getLayer());

        //compose the renderer providers
        CompositeTableCellRendererProvider rendererProvider = new CompositeTableCellRendererProvider();
        ITableCellRendererProvider tableCellRendererProvider = getTreeTableCellRendererProvider(MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER_TREE_TABLE);
        if (tableCellRendererProvider != null) {
          rendererProvider.add(tableCellRendererProvider);
        }
        //add fallback
        tableCellRendererProvider = getTreeTableCellRendererProvider(DEFAULT_CELL_RENDERER_PROVIDER_TREE_TABLE);
        if (tableCellRendererProvider != null) {
          rendererProvider.add(tableCellRendererProvider, TLcyGenericComposite.PRIORITY_FALLBACK);
        }
        table.setRendererProvider(rendererProvider);

        //compose the editor provider
        CompositeTableCellEditorProvider editorProvider = new CompositeTableCellEditorProvider();
        ITableCellEditorProvider tableCellEditorProvider = getTreeTableCellEditorProvider(TREE_TABLE_CELL_EDITOR_PROVIDER_TREE_TABLE);
        if (tableCellEditorProvider != null) {
          editorProvider.add(tableCellEditorProvider);
        }
        table.setEditorProvider(editorProvider);

        table.setRootVisible(false);

        // Set up alternate row coloring. Provide roll-over effect for editable cells.
        Color editableColor = UIColors.getUIColor("nimbusOrange", new Color(255, 195, 107));
        table.setHighlighters(
            new HighlighterFactory.UIColorHighlighter(HighlightPredicate.ODD),
            new ColorHighlighter(new AndHighlightPredicate(EDITABLE, ROLLOVER_CELL), editableColor, null));

        table.getTableHeader().setReorderingAllowed(false);
        table.setOpenIcon(null);
        table.setClosedIcon(null);
        table.setLeafIcon(null);
        table.setTreeTableModel(model);
        return table;
      }
      return null;
    default:
      throw new IllegalArgumentException("Tree Table ID[" + aTreeTableID + "] unknown");
    }
  }

  /**
   * <p>Returns the <code>JXTreeTable</code></p> for the given ID.
   *
   * @param aTreeTableID The ID describing which <code>JXTreeTable</code> to return
   *
   * @return the <code>JXTreeTable</code> for the given ID
   */
  public JXTreeTable getTreeTable(int aTreeTableID) {
    return objectForID(aTreeTableID, fTreeTableIDs, fTreeTables);
  }

  /**
   * <p>Returns a list with all tree table IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing tree table IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the tree table IDs.
   */
  protected ILcdIntList getTreeTableIDs() {
    return fTreeTableIDs;
  }

  /**
   * This function creates all the <code>TreeTableModel</code>s using {@link
   * #createTreeTableModel(int, ALcyProperties)} with all the given
   * tree table model ID's (given in constructor).
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory.
   */
  private void setupTreeTableModels(ALcyProperties aProperties) {
    fTreeTableModels = new TreeTableModel[fTreeTableModelIDs.size()];
    for (int i = 0; i < fTreeTableModelIDs.size(); i++) {
      fTreeTableModels[i] = createTreeTableModel(fTreeTableModelIDs.get(i), aProperties);
    }
  }

  /**
   * Creates a <code>TreeTableModel</code> for the given ID.
   *
   * @param aTreeTableModelID The ID describing which <code>TreeTableModel</code> to create
   * @param aProperties       The properties. Can be used to tune the behavior of this factory.
   *
   * @return the newly created <code>TreeTableModel</code>, or <code>null</code>
   */
  protected TreeTableModel createTreeTableModel(int aTreeTableModelID, ALcyProperties aProperties) {
    switch (aTreeTableModelID) {
    case TREE_TABLE_MODEL_TREE_TABLE:
      TLcyDomainObjectContext domainObjectContext = retrieveDomainObjectContext(aProperties);
      if (TreeTableDataObjectModel.DOMAIN_OBJECT_CONTEXT_FILTER.accept(domainObjectContext)) {
        TreeTableDataObjectModel treeTableModel = new TreeTableDataObjectModel(domainObjectContext, getCustomizerPanel(), getLucyEnv());
        treeTableModel.addUndoableListener(getLucyEnv().getUndoManager());
        return treeTableModel;
      } else if (TreeTableFeaturedModel.DOMAIN_OBJECT_CONTEXT_FILTER.accept(domainObjectContext)) {
        return new TreeTableFeaturedModel(domainObjectContext, getCustomizerPanel());
      }

      return null;
    default:
      throw new IllegalArgumentException("Tree Table Model ID[" + aTreeTableModelID + "] unknown");
    }
  }

  /**
   * Returns the <code>TreeTableModel</code> for the given ID.
   *
   * @param aTreeTableModelID The ID describing which <code>TreeTableModel</code> to return
   *
   * @return the <code>TreeTableModel</code> for the given ID
   */
  public TreeTableModel getTreeTableModel(int aTreeTableModelID) {
    return objectForID(aTreeTableModelID, fTreeTableModelIDs, fTreeTableModels);
  }

  /**
   * <p>Returns a list with all tree table model IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing tree table model IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the tree table model IDs.
   */
  protected ILcdIntList getTreeTableModelIDs() {
    return fTreeTableModelIDs;
  }

  /**
   * This function creates all the <code>ITableCellRendererProviders</code> using {@link
   * #createTreeTableCellRendererProvider(int, ALcyProperties)} with all the
   * given tree table cell renderer provider ID's (given in constructor).
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory.
   */
  private void setupTreeTableCellRendererProviders(ALcyProperties aProperties) {
    //noinspection unchecked
    fTreeTableCellRendererProviders = new ITableCellRendererProvider[fTreeTableCellRendererProviderIDs.size()];
    for (int i = 0; i < fTreeTableCellRendererProviderIDs.size(); i++) {
      fTreeTableCellRendererProviders[i] = createTreeTableCellRendererProvider(fTreeTableCellRendererProviderIDs.get(i), aProperties);
    }
  }

  /**
   * Creates a mapping between classes and <code>TableCellRenderer</code> instances for the given
   * ID.
   *
   * @param aTreeTableCellRendererProviderID The ID describing which mapping between classes and
   *                                 <code>TableCellRenderer</code> instances to create
   * @param aProperties              The properties. Can be used to tune the behavior of this
   *                                 factory.
   *
   * @return the newly created mapping between classes and <code>TableCellRenderer</code> instances,
   *         or <code>null</code>
   */
  protected ITableCellRendererProvider createTreeTableCellRendererProvider(int aTreeTableCellRendererProviderID,
                                                                           ALcyProperties aProperties) {
    switch (aTreeTableCellRendererProviderID) {
    case DEFAULT_CELL_RENDERER_PROVIDER_TREE_TABLE:
      TLcyDomainObjectContext domainObjectContext = retrieveDomainObjectContext(aProperties);
      ILcdModel model = domainObjectContext.getModel();
      if (model != null) {
        return new DefaultTreeTableCellRendererProvider(model, getLucyEnv());
      }
      return null;
    case MEASURE_ANNOTATION_CELL_RENDERER_PROVIDER_TREE_TABLE:
      return new TreeTableMeasureAnnotationCellRendererProvider(getLucyEnv());
    default:
      throw new IllegalArgumentException("Tree Table Cell Renderer ID[" + aTreeTableCellRendererProviderID + "] unknown");
    }
  }

  /**
   * Returns the <code>ITableCellRendererProvider</code> instance for the given ID.
   *
   * @param aTreeTableCellRendererProviderID The ID describing which
   *                                         <code>ITableCellRendererProvider</code> instances to return
   *
   * @return the  the <code>ITableCellRendererProvider</code> instance for the given ID.
   */
  public ITableCellRendererProvider getTreeTableCellRendererProvider(int aTreeTableCellRendererProviderID) {
    return objectForID(aTreeTableCellRendererProviderID, fTreeTableCellRendererProviderIDs, fTreeTableCellRendererProviders);
  }

  /**
   * <p>Returns a list with all tree table cell renderer provider IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing tree table cell renderer IDs to/from this factory is achieved by altering this
   * list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the tree table cell renderer provider IDs.
   */
  protected ILcdIntList getTreeTableCellRendererProviderIDs() {
    return fTreeTableCellRendererProviderIDs;
  }

  /**
   * This function creates all the <code>TableCellEditorProviders</code> using {@link
   * #createTreeTableCellEditorProvider(int, ALcyProperties)} with all the
   * given tree table cell editor provider ID's (given in constructor).
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory.
   */
  private void setupTreeTableCellEditorProviders(ALcyProperties aProperties) {
    //noinspection unchecked
    fTreeTableCellEditorsProviders = new ITableCellEditorProvider[fTreeTableCellEditorProviderIDs.size()];
    for (int i = 0; i < fTreeTableCellEditorProviderIDs.size(); i++) {
      fTreeTableCellEditorsProviders[i] = createTreeTableCellEditorProvider(fTreeTableCellEditorProviderIDs.get(i), aProperties);
    }
  }

  /**
   * Creates the <code>ITableCellEditorProvider</code> for the given ID.
   *
   * @param aTreeTableCellEditorProviderID The ID describing which mapping between classes and
   *                                       <code>TableCellEditor</code> instances to create
   * @param aProperties                    The properties. Can be used to tune the behavior of this
   *                                       factory.
   *
   * @return the newly created table cell editor provider,
   *         or <code>null</code>
   */
  protected ITableCellEditorProvider createTreeTableCellEditorProvider(int aTreeTableCellEditorProviderID, ALcyProperties aProperties) {
    switch (aTreeTableCellEditorProviderID) {
    case TREE_TABLE_CELL_EDITOR_PROVIDER_TREE_TABLE:
      return new DataPropertyNodeEditorProvider(getLucyEnv());
    default:
      throw new IllegalArgumentException("Tree Table Cell Editor ID[" + aTreeTableCellEditorProviderID + "] unknown");
    }
  }

  /**
   * Returns the <code>ITableCellEditorProvider</code> for the given ID.
   *
   * @param aTreeTableCellEditorProviderID The ID describing which mapping between classes and <code>TableCellEditor</code> instances to
   *                                 return
   *
   * @return the mapping between classes and <code>TableCellEditor</code> instances for the given ID.
   */
  public ITableCellEditorProvider getTreeTableCellEditorProvider(int aTreeTableCellEditorProviderID) {
    return objectForID(aTreeTableCellEditorProviderID, fTreeTableCellEditorProviderIDs, fTreeTableCellEditorsProviders);
  }

  /**
   * <p>Returns a list with all tree table cell editor IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing tree table cell editor IDs to/from this factory is achieved by altering this
   * list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the tree table cell editor IDs.
   */
  protected ILcdIntList getTreeTableCellEditorProviderIDs() {
    return fTreeTableCellEditorProviderIDs;
  }

  /**
   * This method creates all the action bars using the {@link #createActionBar(int,
   * ALcyProperties)} method with the IDs passed in the
   * constructor.
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   */
  private void setupActionBars(ALcyProperties aProperties) {
    fActionBars = new ILcyActionBar[fActionBarIDs.size()];
    TLcyActionBarManager actionBarManager = getLucyEnv().getUserInterfaceManager().getActionBarManager();
    for (int i = 0; i < fActionBarIDs.size(); i++) {
      Integer actionBarID = fActionBarIDs.get(i);
      ILcyActionBar actionBar = createActionBar(actionBarID, aProperties);
      fActionBars[i] = actionBar;
      if (actionBar != null) {
        switch (actionBarID) {
        case TOOL_BAR_ACTION_BAR:
          TLcyActionBarMediatorBuilder.newInstance(actionBarManager)
                                      .sourceActionBar(TOOL_BAR_NAME, getCustomizerPanel())
                                      .targetActionBar(actionBar)
                                      .bidirectional()
                                      .mediate();
          if (actionBar instanceof ALcyActionBar) {
            //Make sure the group priorities from the config file are used
            ((ALcyActionBar) actionBar).setProperties(aProperties.subset(fPropertiesPrefix + TOOL_BAR_NAME + "."));
          }
          break;
        }
      }
    }
  }

  /**
   * Creates an <code>ILcyActionBar</code> for the given ID
   *
   * @param aActionBarID The ID describing which <code>ILcyActionBar</code> to create
   * @param aProperties  The properties. Can be used to tune the behavior of this factory
   *
   * @return the newly created <code>ILcyActionBar</code>, or <code>null</code>
   */
  protected ILcyActionBar createActionBar(int aActionBarID, ALcyProperties aProperties) {
    switch (aActionBarID) {
    case TOOL_BAR_ACTION_BAR:
      return new TLcyToolBar();
    default:
      throw new IllegalArgumentException("Action Bar ID[" + aActionBarID + "] unknown");
    }
  }

  /**
   * Returns the <code>ILcyActionBar</code> for the given ID
   *
   * @param aActionBarID The ID describing which <code>ILcyActionBar</code> to return
   *
   * @return the <code>ILcyActionBar</code> for the given ID
   */
  public ILcyActionBar getActionBar(int aActionBarID) {
    return objectForID(aActionBarID, fActionBarIDs, fActionBars);
  }

  /**
   * <p>Returns a list with all action bar IDs.</p>
   *
   * <p>By default, this list only contains the IDs passed in the constructor. Adding and/or
   * removing action bar IDs to/from this factory is achieved by altering this list.</p>
   *
   * <p>Warning: do not alter the list when the factory is creating the GUI. Only modify it before
   * or after the {@link #createGUI(ALcyProperties)} method.</p>
   *
   * @return a list with the action bar IDs.
   */
  protected ILcdIntList getActionBarIDs() {
    return fActionBarIDs;
  }

  /**
   * This method creates the customizer panel using the {@link #createCustomizerPanel(ALcyProperties)
   * createCustomizerPanel} method and stores it. Access to the created customizer panel is achieved
   * through the {@link #getCustomizerPanel() getCustomizerPanel} method
   *
   * @param aProperties The properties. Can be used to customize the behavior of this factory
   */
  private void setupCustomizerPanel(ALcyProperties aProperties) {
    TreeTableViewCustomizerPanel customizerPanel = createCustomizerPanel(aProperties);
    if (customizerPanel == null) {
      throw new NullPointerException("Created customizer panel cannot be null");
    }
    fCustomizerPanel = customizerPanel;
    //since we are going to create a customizer panel for a certain domain object context, we already
    //call setObject
    TLcyDomainObjectContext domainObjectContext = retrieveDomainObjectContext(aProperties);
    if (customizerPanel.canSetObject(domainObjectContext)) {
      customizerPanel.setObject(domainObjectContext);
    }
  }

  /**
   * <p>Returns a new <code>ILcyCustomizerPanel</code>. This customizer panel should not contain
   * anything.</p>
   *
   * <p>The contents of the customizer panel will be created by the other methods of this factory,
   * and set on the customizer panel afterwards.</p>
   *
   * @param aProperties The properties. Can be used to tune the behavior of this factory
   *
   * @return a new <code>ILcyCustomizerPanel</code>. Must not be <code>null</code>.
   */
  protected TreeTableViewCustomizerPanel createCustomizerPanel(ALcyProperties aProperties) {
    return new TreeTableViewCustomizerPanel(getLucyEnv(), new TreeTableFilter());
  }

  /**
   * <p>Returns the <code>TreeTableViewCustomizerPanel</code> currently under construction.</p>
   *
   * <p>The panel is originally created in {@link #createCustomizerPanel(ALcyProperties)
   * createCustomizerPanel} and the contents is created and added in the other methods of this
   * factory.</p>
   *
   * @return the <code>ILcyCustomizerPanel</code> currently under construction
   */
  public TreeTableViewCustomizerPanel getCustomizerPanel() {
    return fCustomizerPanel;
  }

  @Override
  protected void setup(final ALcyProperties aProperties) {
    // See javadoc of setup method on threading
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        setupCustomizerPanel(aProperties);
        setupActionBars(aProperties);
        setupTreeTableModels(aProperties);
        setupTreeTableCellRendererProviders(aProperties);
        setupTreeTableCellEditorProviders(aProperties);
        setupTreeTables(aProperties);
      }
    });
    super.setup(aProperties);
  }

  @Override
  protected void cleanup(ALcyProperties aProperties) {
    fTreeTables = null;
    fTreeTableModels = null;
    fTreeTableCellRendererProviders = null;
    fTreeTableCellEditorsProviders = null;
    fActionBars = null;
    fCustomizerPanel = null;
    super.cleanup(aProperties);
  }

  private <T> T objectForID(int aID, ILcdIntList aIDs, T[] aObjects) {
    for (int i = 0; i < aIDs.size(); i++) {
      if (aIDs.get(i) == aID) {
        return aObjects[i];
      }
    }
    throw new IllegalArgumentException("Given ID[" + aID + "] is an unknown ID.");
  }

  private TLcyDomainObjectContext retrieveDomainObjectContext(ALcyProperties aProperties) {
    TLcyDomainObjectContext result = (TLcyDomainObjectContext) aProperties.get(fPropertiesPrefix + DOMAIN_OBJECT_CONTEXT_KEY, null);
    if (result == null) {
      throw new IllegalArgumentException("The properties passed to this factory must contain the domain object. See TreeTableGUIFactory.DOMAIN_OBJECT_CONTEXT_KEY");
    }
    return result;
  }

  /**
   * Returns the prefix used in the properties passed to this factory.
   *
   * @return the prefix used in the properties passed to this factory.
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
}
