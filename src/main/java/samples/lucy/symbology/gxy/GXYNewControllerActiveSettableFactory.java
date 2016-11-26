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
package samples.lucy.symbology.gxy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.gui.ILcdUndoableSource;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.map.action.ALcyGXYNewControllerActiveSettable;
import com.luciad.lucy.map.action.TLcyGXYNewControllerActiveSettable;
import com.luciad.lucy.map.action.TLcyGXYTouchNewControllerActiveSettable;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdStatusSource;
import com.luciad.view.gxy.ILcdGXYController;

/**
 * Makes actions for interactively creating military symbols, based on the add-on's preferences.
 */
class GXYNewControllerActiveSettableFactory {

  static ALcyActiveSettable createActiveSettable(GXYSymbologyNewControllerModel aControllerModel,
                                                 GXYSymbologyNewControllerModel.CodedSelector aCodedSelector,
                                                 boolean aActivate,
                                                 ALcyProperties aProperties,
                                                 String aPropertiesPrefix,
                                                 ILcyLucyEnv aLucyEnv) {
    String inputType = aProperties.getString(aPropertiesPrefix + "controllerInput", "mouse");
    if ("touch".equals(inputType)) {
      return createTouchActiveSettable(aControllerModel, aCodedSelector, aActivate, aProperties, aPropertiesPrefix, aLucyEnv);
    } else {
      return createMouseActiveSettable(aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    }
  }

  private static ALcyActiveSettable createMouseActiveSettable(GXYSymbologyNewControllerModel aControllerModel,
                                                              GXYSymbologyNewControllerModel.CodedSelector aCodedSelector,
                                                              boolean aActivate,
                                                              ILcyLucyEnv aLucyEnv) {
    TLcyGXYNewControllerActiveSettable activeSettable = new TLcyGXYNewControllerActiveSettable(aControllerModel, aControllerModel.getMapComponent(), aLucyEnv);
    initActiveSettable(activeSettable, aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    return activeSettable;
  }

  private static ALcyActiveSettable createTouchActiveSettable(GXYSymbologyNewControllerModel aControllerModel,
                                                              GXYSymbologyNewControllerModel.CodedSelector aCodedSelector,
                                                              boolean aActivate,
                                                              ALcyProperties aProperties,
                                                              String aPropertiesPrefix,
                                                              ILcyLucyEnv aLucyEnv) {
    String propertiesPrefix = aPropertiesPrefix + "createObject." + "touchNewController.";
    TLcyGXYTouchNewControllerActiveSettable activeSettable = new TLcyGXYTouchNewControllerActiveSettable(
        aControllerModel,
        aControllerModel.getMapComponent(),
        aLucyEnv,
        aProperties,
        propertiesPrefix
    );
    initActiveSettable(activeSettable, aControllerModel, aCodedSelector, aActivate, aLucyEnv);
    return activeSettable;
  }

  private static void initActiveSettable(ALcyGXYNewControllerActiveSettable aActiveSettable,
                                         final GXYSymbologyNewControllerModel aControllerModel,
                                         final GXYSymbologyNewControllerModel.CodedSelector aCodedSelector,
                                         boolean aActivate,
                                         ILcyLucyEnv aLucyEnv) {
    ILcdGXYController newController = aActiveSettable.getGXYNewController();
    if (newController instanceof ILcdUndoableSource) {
      ((ILcdUndoableSource) newController).addUndoableListener(aLucyEnv.getUndoManager());
    }
    if (newController instanceof ILcdStatusSource) {
      ((ILcdStatusSource) newController).addStatusListener(aLucyEnv);
    }
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
