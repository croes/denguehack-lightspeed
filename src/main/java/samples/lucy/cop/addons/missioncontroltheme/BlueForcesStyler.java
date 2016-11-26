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

import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.shape.ILcdShapeList;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ASymbolStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * {@code ILspStyler} for the blue forces
 */
final class BlueForcesStyler extends ALspStyler {
  private final APP6IconProvider fIconProvider = new APP6IconProvider();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object domainObject : aObjects) {
      if (domainObject instanceof ILcdShapeList && domainObject instanceof ILcdDataObject) {
        ILcdDataObject dataObject = (ILcdDataObject) domainObject;
        String app6Code = (String) dataObject.getValue(BlueForcesModel.CODE_PROPERTY);
        if (fIconProvider.canGetIcon(app6Code)) {
          ILcdAPP6ACoded app6aCoded = fIconProvider.convertStringToAPP6Object(app6Code);
          aStyleCollector
              .object(domainObject)
              .geometry(((ILcdShapeList) domainObject).getShape(0))
              .style(TLspAPP6ASymbolStyle.newBuilder().app6aCoded(app6aCoded).build())
              .submit();
        }
      }
    }
  }
}
