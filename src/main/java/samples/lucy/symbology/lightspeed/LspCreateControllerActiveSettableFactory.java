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
package samples.lucy.symbology.lightspeed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateControllerActiveSettable;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;

/**
 * Makes actions for interactively creating military symbols, based on the add-on's preferences.
 */
final class LspCreateControllerActiveSettableFactory {

  private LspCreateControllerActiveSettableFactory() {
  }

  static ALcyActiveSettable createActiveSettable(ILcyLspMapComponent aMapComponent,
                                                 LspSymbologyCreateControllerModel aControllerModel,
                                                 LspSymbologyCreateControllerModel.CodedSelector aCodedSelector,
                                                 boolean aActivate,
                                                 ALcyProperties aProperties,
                                                 String aPropertiesPrefix,
                                                 ILcyLucyEnv aLucyEnv) {
    if (ILcyLspMapComponent.TOUCH_MAP_TYPE.equals(aMapComponent.getType())) {
      return createTouchActiveSettable(aControllerModel, aCodedSelector, aActivate, aProperties, aPropertiesPrefix, aLucyEnv);
    } else {
      return createMouseActiveSettable(aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    }
  }

  private static ALcyActiveSettable createMouseActiveSettable(LspSymbologyCreateControllerModel aControllerModel,
                                                              LspSymbologyCreateControllerModel.CodedSelector aCodedSelector,
                                                              boolean aActivate,
                                                              ILcyLucyEnv aLucyEnv) {
    TLcyLspCreateControllerActiveSettable activeSettable = TLcyLspCreateControllerActiveSettable.createMouseControllerActiveSettable(aControllerModel, aControllerModel.getMapComponent(), aLucyEnv);
    initActiveSettable(activeSettable, aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    return activeSettable;
  }

  private static ALcyActiveSettable createTouchActiveSettable(LspSymbologyCreateControllerModel aControllerModel,
                                                              LspSymbologyCreateControllerModel.CodedSelector aCodedSelector,
                                                              boolean aActivate,
                                                              ALcyProperties aProperties,
                                                              String aPropertiesPrefix,
                                                              ILcyLucyEnv aLucyEnv) {
    String propertiesPrefix = aPropertiesPrefix + "createObject." + "touchNewController.";
    TLcyLspCreateControllerActiveSettable activeSettable = TLcyLspCreateControllerActiveSettable.createTouchControllerActiveSettable(
        aControllerModel,
        aControllerModel.getMapComponent(),
        aProperties,
        propertiesPrefix,
        aLucyEnv);
    initActiveSettable(activeSettable, aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    return activeSettable;
  }

  private static void initActiveSettable(TLcyLspCreateControllerActiveSettable aActiveSettable,
                                         final LspSymbologyCreateControllerModel aControllerModel,
                                         final LspSymbologyCreateControllerModel.CodedSelector aCodedSelector,
                                         boolean aActivate,
                                         ILcyLucyEnv aLucyEnv) {
    String description = TLcyLang.getString("Click on the map to create a new object. Double click to end.");
    TLspCreateController controller = aActiveSettable.getCreateController();
    controller.addUndoableListener(aLucyEnv.getUndoManager());
    controller.addStatusListener(aLucyEnv);
    controller.setShortDescription(description);
    aControllerModel.setCodedSelector(aActiveSettable.isActive() || aActivate ? aCodedSelector : null);
    aActiveSettable.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("active".equals(evt.getPropertyName())) {
          aControllerModel.setCodedSelector(((Boolean) evt.getNewValue()) ? aCodedSelector : null);
        }
      }
    });
    if (aActivate) {
      aActiveSettable.setActive(true);
    }
  }
}
