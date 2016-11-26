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

import samples.lucy.tableview.ITableViewLogic;
import samples.lucy.tableview.TableViewAddOn;
import samples.lucy.tableview.TableViewCustomizerPanelCodec;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;

/**
 * This addon shows how to extend TableViewAddOn with some extra functionality:
 * <ul>
 * <li>Add an action to the table view to change the color of the second column.
 * <li>Change the font color of the second column for the tree table view.
 * </ul>
 *
 * @see #TableViewAddOn
 */
public class ExtendedTableViewAddOn extends TableViewAddOn {

  @Override
  protected ALcyGUIFactory<? extends ILcyCustomizerPanel> createTableViewGUIFactory(ITableViewLogic aTableViewLogic) {
    if (aTableViewLogic == null) {
      return null;
    }
    return new TableViewExtGUIFactory(getShortPrefix(), aTableViewLogic, getLucyEnv());
  }

  @Override
  protected TableViewCustomizerPanelCodec createTableViewCustomizerPanelCodec(ILcyLucyEnv aLucyEnv, String aUID, String aPrefix, ILcyCustomizerPanelFactory aFactory) {
    return new CustomTableViewCustomizerPanelCodec(aUID, aPrefix, aFactory, aLucyEnv);
  }
}
