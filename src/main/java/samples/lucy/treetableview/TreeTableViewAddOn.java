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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.dataproperty.TLcyDataPropertyValueCustomizerPanelFactories;

/**
 * <p>This sample add-on provides a user interface to show the properties (a.k.a. attributes) associated to a domain
 * object. It uses a tree tabular view having two columns: a property name column and a property value column.
 * The tree aspect of this table allows to expand nested properties, if there are any.</p>
 *
 * <p>Select for example the 'world' layer in the layer control, make it selectable and use the select
 * controller to double-click a country. This opens the object properties and shows the tree table.</p>
 *
 * <p>Properties are modeled by domain objects by implementing the <code>ILcdDataObject</code> interface (or the
 * <code>ILcdFeatured</code> interface). Any domain objects (a.k.a. model elements) that do not implement these
 * interfaces cannot benefit from this add-on. This add-on provides its tree table user interface by registering a
 * {@link ILcyCustomizerPanelFactory} to {@code ILcyLucyEnv} which works on {@code TLcyDomainObjectContext}'s. It only
 * provides the factory for the UI, it cooperates with the
 * {@link com.luciad.lucy.addons.selectioneditor.TLcySelectionEditorAddOn TLcySelectionEditorAddOn} to make this
 * UI appear when a particular object is selected on the map. Please refer to that class for further explanation
 * regarding that collaboration.</p>
 *
 * <p>The {@link ILcyCustomizerPanelFactory} is registered with {@linkplain ILcyLucyEnv#PRIORITY_FALLBACK fall-back}
 * priority, so that data format add-ons can provide a factory that is more specific to their
 * data. By simply registering a factory with {@linkplain ILcyLucyEnv#PRIORITY_DEFAULT default priority}, theirs will
 * take precedence.</p>
 *
 * <p>The user interface provided by this add-on optionally allows to also edit the properties of an object. There is
 * however no generic UI for editing properties, it is always provided by the data format add-ons. So the data formats
 * can for example provide a text field to edit property X, and a spinner to edit property Y. Please refer to
 * {@link TLcyDataPropertyValueCustomizerPanelFactories} for more information.</p>
 *
 */
public class TreeTableViewAddOn extends ALcyPreferencesAddOn {

  private ALcyGUIFactory<? extends ILcyCustomizerPanel> fTreeTableGUIFactory;
  private ILcyCustomizerPanelFactory fCustomizerPanelFactory;

  public TreeTableViewAddOn() {
    this("samples.lucy.treetableview.TreeTableViewAddOn.",
         "TreeTableViewAddOn.");
  }

  public TreeTableViewAddOn(String aLongPrefix, String aShortPrefix) {
    super(aLongPrefix, aShortPrefix);
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    fTreeTableGUIFactory = createTreeTableGUIFactory();
    fCustomizerPanelFactory = createCustomizerPanelFactory();
    if (fCustomizerPanelFactory != null) {
      aLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory().addFallbackCustomizerPanelFactory(fCustomizerPanelFactory);
    }
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    if (fCustomizerPanelFactory != null) {
      aLucyEnv.getUserInterfaceManager().getCompositeCustomizerPanelFactory().removeCustomizerPanelFactory(fCustomizerPanelFactory);
    }
    fCustomizerPanelFactory = null;
    fTreeTableGUIFactory = null;
  }

  protected ILcyCustomizerPanelFactory createCustomizerPanelFactory() {
    return new TreeTableViewCustomizerFactory(new TreeTableFilter(),
                                              fTreeTableGUIFactory,
                                              getShortPrefix(),
                                              getPreferences());
  }

  /**
   * Create the GUI factory responsible for the creation of the tree table customizer panels
   * @return a new GUI factory responsible for the creation of the tree table customizer panels
   */
  protected ALcyGUIFactory<? extends ILcyCustomizerPanel> createTreeTableGUIFactory() {
    return new TreeTableGUIFactory(getShortPrefix(), getLucyEnv());
  }

}
