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

import java.awt.event.ActionEvent;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.TLcyGenericMapUtil;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.ILcdLayer;

/**
 * Action which can select an object and fit the view on it.
 */
final class SelectAndFitAction extends ALcdAction {

  private final TLcyDomainObjectContext fDomainObjectContext;
  private final ILcyLucyEnv fLucyEnv;

  public SelectAndFitAction(TLcyDomainObjectContext aDomainObjectContext, ILcyLucyEnv aLucyEnv) {
    fDomainObjectContext = aDomainObjectContext;
    fLucyEnv = aLucyEnv;
    setEnabled(fDomainObjectContext.getView() != null &&
               fDomainObjectContext.getLayer() != null);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIT_ICON));
  }

  @Override
  public void actionPerformed(ActionEvent aActionEvent) {
    Object domainObject = fDomainObjectContext.getDomainObject();
    ILcdLayer layer = fDomainObjectContext.getLayer();
    if (layer.isSelectableSupported() &&
        layer.isSelectable() &&
        !layer.isSelected(domainObject)) {
      layer.clearSelection(ILcdFireEventMode.FIRE_LATER);
      layer.selectObject(domainObject, true, ILcdFireEventMode.FIRE_LATER);
      layer.fireCollectedSelectionChanges();
    }

    try {
      new TLcyGenericMapUtil(fLucyEnv).fitOnObjects(fDomainObjectContext.getView(), layer, domainObject);
    } catch (TLcdOutOfBoundsException | TLcdNoBoundsException e) {
      TLcdStatusEvent.sendMessage(fLucyEnv, this, TLcyLang.getString("Can't fit map, object(s) not visible in current projection"), TLcdStatusEvent.Severity.WARNING);
    }
  }
}
