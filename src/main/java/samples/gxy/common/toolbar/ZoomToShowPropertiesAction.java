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
package samples.gxy.common.toolbar;

import java.awt.event.ActionEvent;
import java.util.List;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYView;

import samples.common.action.ShowPropertiesAction;

/**
 * Delegates to ShowPropertiesAction when triggered for a non-empty selection, zooms towards the passed point otherwise.
 */
public class ZoomToShowPropertiesAction extends ALcdObjectSelectionAction {

  private final ILcdAction fZoomToAction;
  private final ILcdAction fShowPropertiesAction;

  public ZoomToShowPropertiesAction(ILcdGXYView aGXYView, ShowPropertiesAction aPropertiesAction) {
    super(aGXYView, aPropertiesAction.getObjectFilter(), 0, 1, true);
    fZoomToAction = new ZoomToAction(aGXYView);
    fShowPropertiesAction = aPropertiesAction;
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    if (aSelection.size() == 0) {
      if (aActionEvent instanceof TLcdActionAtLocationEvent) {
        fZoomToAction.actionPerformed(aActionEvent);
      }
    } else {
      fShowPropertiesAction.actionPerformed(aActionEvent);
    }
  }

}
