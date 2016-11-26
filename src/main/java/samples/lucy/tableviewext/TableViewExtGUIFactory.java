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
package samples.lucy.tableviewext;

import com.luciad.gui.ILcdAction;
import samples.lucy.tableview.ITableCellRendererProvider;
import samples.lucy.tableview.ITableViewLogic;
import samples.lucy.tableview.TableViewCustomizerPanel;
import samples.lucy.tableview.TableViewFilter;
import samples.lucy.tableview.TableViewGUIFactory;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * Extension of <code>TableViewGUIFactory</code> that adds a new action to the toolbar that allows
 * to change the text color of the second column.  To achieve this, it also sets up a custom cell
 * renderer for that column.
 */
final class TableViewExtGUIFactory extends TableViewGUIFactory {

  private static final int CHANGE_COLOR_ACTION_ID = 132;

  public TableViewExtGUIFactory(String aPropertiesPrefix,
                                ITableViewLogic aTableViewLogic,
                                ILcyLucyEnv aLucyEnv) {
    super(aPropertiesPrefix, aTableViewLogic, aLucyEnv);
    //make sure the change color action is created as well
    getActionIDs().add(CHANGE_COLOR_ACTION_ID);
  }

  @Override
  protected ILcdAction createAction(int aActionID, ALcyProperties aProperties) {
    if (aActionID == CHANGE_COLOR_ACTION_ID) {
      ILcyCustomizerPanel customizerPanel = getCustomizerPanel();
      if (customizerPanel instanceof CustomTableViewCustomizerPanel) {
        ChangeColorAction action = new ChangeColorAction((CustomTableViewCustomizerPanel) customizerPanel);
        action.putValue(TLcyActionBarUtil.ID_KEY, getPropertiesPrefix() + "changeColorAction");
        return action;
      }
      return null;
    } else {
      return super.createAction(aActionID, aProperties);
    }
  }

  @Override
  protected ITableCellRendererProvider createTableCellRendererProvider(int aTableCellRendererProviderID, ALcyProperties aProperties) {
    ITableCellRendererProvider tableCellRendererProvider = super.createTableCellRendererProvider(aTableCellRendererProviderID, aProperties);
    ILcyCustomizerPanel customizerPanel = getCustomizerPanel();
    if (customizerPanel instanceof CustomTableViewCustomizerPanel &&
        aTableCellRendererProviderID == DEFAULT_TABLE_CELL_RENDERER_PROVIDER &&
        tableCellRendererProvider != null) {
      return new CustomTableCellRendererProvider(
          (CustomTableViewCustomizerPanel) customizerPanel, tableCellRendererProvider);
    }
    return tableCellRendererProvider;
  }

  @Override
  protected TableViewCustomizerPanel createCustomizerPanel(ALcyProperties aProperties) {
    return new CustomTableViewCustomizerPanel(new TableViewFilter(getTableViewLogic()),
                                              TLcyLang.getString("Table view"),
                                              getLucyEnv(),
                                              getTableViewLogic(),
                                              (TLcyModelContext) aProperties.get(getPropertiesPrefix() + MODEL_CONTEXT_KEY, null));
  }
}
