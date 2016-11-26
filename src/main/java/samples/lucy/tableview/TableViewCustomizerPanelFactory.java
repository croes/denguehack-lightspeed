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

import java.util.Iterator;

import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.TLcyProperties;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdFilter;

/**
 * <code>ILcyCustomizerPanelFactory</code> for table views created by
 * a <code>TableViewGUIFactory</code>.
 */
public class TableViewCustomizerPanelFactory extends ALcyCustomizerPanelFactory {
  private ALcyGUIFactory<? extends ILcyCustomizerPanel> fCustomizerPanelFactory;
  private String fPropertiesPrefix;
  private final ALcyProperties fProperties;

  public TableViewCustomizerPanelFactory(ILcdFilter aFilter,
                                         ALcyGUIFactory<? extends ILcyCustomizerPanel> aGUIFactory,
                                         String aPropertiesPrefix,
                                         ALcyProperties aProperties) {
    super(aFilter);
    fCustomizerPanelFactory = aGUIFactory;
    fPropertiesPrefix = aPropertiesPrefix;
    fProperties = aProperties;
  }

  @Override
  protected ILcyCustomizerPanel createCustomizerPanelImpl(Object aObject) {
    //we can safely do the cast due to the filter
    return fCustomizerPanelFactory.createGUI(createProperties((TLcyModelContext) aObject));
  }

  /**
   * Returns a new <code>ALcyProperties</code> object containing all the properties passed in the
   * constructor, and the model context.
   *
   * @param aModelContext The model context
   *
   * @return a new <code>ALcyProperties</code> object containing all the properties passed in the
   *         constructor, and the model context.
   */
  private ALcyProperties createProperties(TLcyModelContext aModelContext) {
    ALcyProperties properties = new TLcyProperties();
    Iterator keys = fProperties.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      properties.put(key, fProperties.get(key, null));
    }
    properties.put(fPropertiesPrefix + TableViewGUIFactory.MODEL_CONTEXT_KEY, aModelContext);
    return properties;
  }

  /**
   * Returns the prefix used in the properties
   * @return the prefix used in the properties
   */
  public String getPropertiesPrefix() {
    return fPropertiesPrefix;
  }

  /**
   * Returns the properties
   * @return the properties
   */
  public ALcyProperties getProperties() {
    return fProperties;
  }
}
