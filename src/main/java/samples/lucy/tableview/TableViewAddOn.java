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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.addons.modelcustomizer.TLcyModelCustomizerAddOn;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.dataproperty.TLcyDataPropertyValueCustomizerPanelFactories;

/**
 * <p>This sample add-on demonstrates how to show data, that has attributes associated to it,
 * in tabular views. Attributes are modeled by domain objects by implementing the ILcdFeatured
 * or the ILcdDataObject interface. This add-on can:</p>
 *
 * <ul>
 *
 * <li>show an ILcdModel whose domain objects all implement ILcdDataObject with the same TLcdType
 * in a JTable. Every row is one domain object, every column is one of the attributes.</li>
 *
 * <li>show an ILcdModel whose domain objects implement ILcdFeatured in a JTable. Every row
 * is one domain object, every column is one of the attributes.</li>
 *
 * </ul>
 *
 *
 * <p>Select for example the 'world' layer in the layer control, and use the 'File|New|Table'
 * menu item to show the table.  Double click in the table on an object of your choice to
 * select it on the map, and fit the map to it.</p>
 *
 * <p>More detailed instructions on using the table view can be found in 'LucyUserGuide.pdf',
 * under the 'Tote' section.</p>
 *
 * <p>The table view is created by the {@link #createTableViewCustomizerFactory(ILcyLucyEnv)},
 * which uses a <code>ALcyGUIFactory</code> created by the {@link #createTableViewGUIFactory(ITableViewLogic)}
 * method. The recommended way to customize the table view is to return a custom
 * <code>ALcyGUIFactory</code>.</p>
 *
 * <p>Creating an extension of this add-on, and overriding one of those methods allows to
 * customize the behavior of this add-on, without having to change any of its code.</p>
 *
 * <p>This add-on provides its tabular user interface by registering a
 * {@link ILcyCustomizerPanelFactory} to {@code ILcyLucyEnv} which works on {@code TLcyModelContext}'s. It only
 * provides the factory for the UI, it cooperates with the {@link TLcyModelCustomizerAddOn TLcyModelCustomizerAddOn} to
 * make this UI appear for a particular model. Please refer to that class for further explanation regarding that
 * collaboration.
 * </p>
 *
 * <p>The table view provided by this add-on optionally allows to also edit the properties of an object. There is
 * however no generic UI for editing properties, it is always provided by the data format add-ons. So the data formats
 * can for example provide a text field to edit property X, and a spinner to edit property Y. Please refer to
 * {@link TLcyDataPropertyValueCustomizerPanelFactories} for more information.</p>
 *
 * <p>Note: this add-on is the source code of <code>TLcyToteAddOn</code>.  If that add-on needs to
 * be customized, one can do so by removing <code>TLcyToteAddOn</code> from <code>addons.xml</code>,
 * and adding this <code>TableViewAddOn</code> to it.
 */
public class TableViewAddOn extends ALcyPreferencesAddOn {

  private ILcyCustomizerPanelFactory fTableViewCustomizerFactory = null;
  private ITableViewLogic fTableViewLogic;
  private ALcyGUIFactory<? extends ILcyCustomizerPanel> fTableViewGUIFactory;
  private TableViewCustomizerPanelCodec fTableViewCustomizerPanelCodec;

  public TableViewAddOn() {
    this("samples.lucy.tableview.TableViewAddOn.",
         "TableViewAddOn.");
  }

  public TableViewAddOn(String aLongPrefix, String aShortPrefix) {
    super(aLongPrefix, aShortPrefix);
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    fTableViewLogic = createTableViewLogic(aLucyEnv);
    fTableViewGUIFactory = createTableViewGUIFactory(fTableViewLogic);
    fTableViewCustomizerFactory = createTableViewCustomizerFactory(aLucyEnv);
    if (fTableViewCustomizerFactory != null) {
      // The table view is registered with fall back priority, so that add-ons that want
      // to provide a custom table view for their file format can do so by using the normal
      // priority, which will then be tried before this one.
      aLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory().
          addCustomizerPanelFactory(fTableViewCustomizerFactory,
                                    TLcyCompositeCustomizerPanelFactory.PRIORITY_FALLBACK);
    }

    fTableViewCustomizerPanelCodec = createTableViewCustomizerPanelCodec(aLucyEnv,
                                                                         getLongPrefix() + "TableViewCustomizerPanel",
                                                                         getShortPrefix(),
                                                                         fTableViewCustomizerFactory);
    if (fTableViewCustomizerPanelCodec != null) {
      aLucyEnv.getWorkspaceManager().addWorkspaceObjectCodec(fTableViewCustomizerPanelCodec);
    }
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    if (fTableViewCustomizerFactory != null) {
      aLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory().removeCustomizerPanelFactory(fTableViewCustomizerFactory);
    }
    if (fTableViewCustomizerPanelCodec != null) {
      aLucyEnv.getWorkspaceManager().removeWorkspaceObjectCodec(fTableViewCustomizerPanelCodec);
    }

  }

  /**
   * Factory method to create the <code>ILcyCustomizerPanelFactory</code> responsible for the creation
   * of the table views.
   *
   * @param aLucyEnv The Lucy backend that can be used to retrieve settings such as default user
   *                 units etc.
   *
   * @return <code>Null</code> or the new <code>TableViewCustomizerPanelFactory</code> instance.
   */
  protected TableViewCustomizerPanelFactory createTableViewCustomizerFactory(ILcyLucyEnv aLucyEnv) {
    if (fTableViewGUIFactory == null || fTableViewLogic == null) {
      return null;
    }
    return new TableViewCustomizerPanelFactory(new TableViewFilter(fTableViewLogic),
                                               fTableViewGUIFactory,
                                               getShortPrefix(),
                                               getPreferences());
  }

  /**
   * Factory method to create the <code>ITableViewLogic</code> which will be used
   * in the creation of the <code>TableViewGUIFactory</code>
   * @param aLucyEnv The Lucy back-end
   * @return <code>ITableViewLogic</code> which will be used
   * in the creation of the <code>TableViewGUIFactory</code>
   */
  protected ITableViewLogic createTableViewLogic(ILcyLucyEnv aLucyEnv) {
    return new GXYTableViewLogic(aLucyEnv);
  }

  /**
   * <p>Creates the <code>TableViewCustomizerPanelCodec</code>, used to (re)store the table view
   * settings in the workspaces.</p>
   *
   * <p>Override this method and return an extension of <code>TableViewCustomizerPanelCodec</code>
   * to let it store additional properties. Return <code>null</code> to disable this functionality.</p>
   *
   *
   * @param aLucyEnv The Lucy environment.
   * @param aUID The UID for the codec.
   * @param aPrefix The prefix used for every key of key-value pairs stored by the codec.
   * @param aFactory The factory used to create <code>TableViewCustomizerPanel</code> instances.
   * @return The newly created <code>TableViewCustomizerPanelCodec</code>.
   */
  protected TableViewCustomizerPanelCodec createTableViewCustomizerPanelCodec(ILcyLucyEnv aLucyEnv,
                                                                              String aUID,
                                                                              String aPrefix,
                                                                              ILcyCustomizerPanelFactory aFactory) {
    return new TableViewCustomizerPanelCodec(aUID, aPrefix, aFactory, aLucyEnv);
  }

  protected ALcyGUIFactory<? extends ILcyCustomizerPanel> createTableViewGUIFactory(ITableViewLogic aTableViewLogic) {
    if (aTableViewLogic == null) {
      return null;
    }
    return new TableViewGUIFactory(getShortPrefix(), aTableViewLogic, getLucyEnv());
  }
}
