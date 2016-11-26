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
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.view.TLcdMS2525bObjectIconProvider;

/**
 * {@code ILcdObjectIconProvider} which returns MS2525 icons based on
 * a String representation of the type
 */
final class MS2525IconProvider implements ILcdObjectIconProvider {
  private final TLcdMS2525bObjectIconProvider fMS2525IconProvider = new TLcdMS2525bObjectIconProvider();

  @Override
  public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
    if (aObject instanceof String) {
      return fMS2525IconProvider.getIcon(convertStringToMS2525Object((String) aObject));
    }
    throw new IllegalArgumentException("Cannot create icon for " + aObject);
  }

  @Override
  public boolean canGetIcon(Object aObject) {
    if (aObject instanceof String) {
      return fMS2525IconProvider.canGetIcon(convertStringToMS2525Object((String) aObject));
    }
    return false;
  }

  public ILcdMS2525bCoded convertStringToMS2525Object(String aObject) {
    return new TLcdEditableMS2525bObject(aObject, ELcdMS2525Standard.MIL_STD_2525c);
  }
}
