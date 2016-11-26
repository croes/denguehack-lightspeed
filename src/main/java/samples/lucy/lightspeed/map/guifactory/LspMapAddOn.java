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
package samples.lucy.lightspeed.map.guifactory;

import com.luciad.lucy.addons.lspmap.TLcyLspMapAddOn;
import com.luciad.lucy.gui.ALcyGUIFactory;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;

/**
 * This map add-on is an extension of <code>TLcyLspMapAddOn</code> that creates a custom gui factory
 * instead of the default one.
 */
public class LspMapAddOn extends TLcyLspMapAddOn {
  /**
   * Returns a custom map component factory for the default map component type. This is also possible
   * by modifying the configuration file of the map add-on.
   * @param aMapComponentType The type of the map component
   *
   * @return the map component factory for the requested type
   */
  @Override
  protected ALcyGUIFactory<ILcyLspMapComponent> createGUIFactory(String aMapComponentType) {
    if (ILcyLspMapComponent.DEFAULT_MAP_TYPE.equals(aMapComponentType)) {
      //Return the custom gui factory instance for the default map type
      return new LspMapComponentFactory(getLucyEnv());
    } else {
      //leave the map component factories for all other types untouched
      return super.createGUIFactory(aMapComponentType);
    }
  }

}
