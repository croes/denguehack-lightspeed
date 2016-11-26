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

import java.util.Iterator;

import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.TLcyProperties;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdFilter;

/**
 * <p>This factory creates a {@link TreeTableViewCustomizerPanel} instance whenever a
 * <code>TLcyDomainObjectContext</code> is presented that matches its requirements.</p>
 *
 * <p>Override the {@link TreeTableViewAddOn#createTreeTableGUIFactory()}
 * in order to customize the customizer panel.</p>
 */
public class TreeTableViewCustomizerFactory extends ALcyCustomizerPanelFactory {
  private ALcyProperties fProperties;
  private String fPropertiesPrefix;
  private ALcyGUIFactory<? extends ILcyCustomizerPanel> fGUIFactory;

  public TreeTableViewCustomizerFactory(ILcdFilter aObjectFilter,
                                        ALcyGUIFactory<? extends ILcyCustomizerPanel> aCustomizerPanelFactory,
                                        String aPropertiesPrefix,
                                        ALcyProperties aProperties) {
    super(aObjectFilter);
    fGUIFactory = aCustomizerPanelFactory;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
  }

  @Override
  public ILcyCustomizerPanel createCustomizerPanelImpl(Object aDomainObjectContext) {
    //we can safely do the cast due to the filter
    return fGUIFactory.createGUI(createProperties((TLcyDomainObjectContext) aDomainObjectContext));
  }

  private ALcyProperties createProperties(TLcyDomainObjectContext aDomainObjectContext) {
    ALcyProperties properties = new TLcyProperties();
    Iterator keys = fProperties.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      properties.put(key, fProperties.get(key, null));
    }
    properties.put(fPropertiesPrefix + TreeTableGUIFactory.DOMAIN_OBJECT_CONTEXT_KEY, aDomainObjectContext);
    return properties;
  }
}
