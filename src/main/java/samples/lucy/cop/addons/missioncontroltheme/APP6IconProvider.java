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
package samples.lucy.cop.addons.missioncontroltheme;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.TLcdAPP6AObjectIconProvider;

/**
 * {@code ILcdObjectIconProvider} which returns APP6 icons based
 * on a String representation of the type
 */
final class APP6IconProvider implements ILcdObjectIconProvider {
  private final TLcdAPP6AObjectIconProvider fAPP6IconProvider = new TLcdAPP6AObjectIconProvider();

  @Override
  public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
    if (aObject instanceof String) {
      return fAPP6IconProvider.getIcon(convertStringToAPP6Object((String) aObject));
    }
    throw new IllegalArgumentException("Cannot create icon for " + aObject);
  }

  @Override
  public boolean canGetIcon(Object aObject) {
    if (aObject instanceof String) {
      return fAPP6IconProvider.canGetIcon(convertStringToAPP6Object((String) aObject));
    }
    return false;
  }

  public ILcdAPP6ACoded convertStringToAPP6Object(String aObject) {
    return new TLcdEditableAPP6AObject(aObject, ELcdAPP6Standard.APP_6B);
  }

}
