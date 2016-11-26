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
package samples.symbology.common;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDomainObjectContext;

/**
 * Reverses the order of the points of the selected tactical graph if reversing is supported by the graph.
 */
public class FlipOrientationAction extends ALcdObjectSelectionAction {

  public FlipOrientationAction(ILcdView aView) {
    this(aView, null);
  }

  public FlipOrientationAction(ILcdView aView, ILcdFilter<String> aFilter) {
    super(aView, new SymbolFilter(aFilter));
    setName("Flip orientation");
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    //reverse the points of all selected objects
    Set<ILcdModel> changedModels = new HashSet<>();
    for (TLcdDomainObjectContext domainObjectContext : aSelection) {
      ILcdDataObject dataObject = (ILcdDataObject) domainObjectContext.getDomainObject();
      ILcdModel model = domainObjectContext.getModel();
      try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
        MilitarySymbolFacade.reversePoints(dataObject);
        model.elementChanged(dataObject, ILcdModel.FIRE_LATER);
        changedModels.add(model);
      }
    }

    //fire the changes all together
    for (ILcdModel model : changedModels) {
      model.fireCollectedModelChanges();
    }
  }

  /**
   * Lets the action available only for TLcdEditableAPP6AObject and TLcdEditableMS2525bObject instances
   * whose points are reversible.
   */
  private static class SymbolFilter implements ILcdFilter<TLcdDomainObjectContext> {

    private ILcdFilter<String> fSubFilter;

    private SymbolFilter(ILcdFilter<String> aSubFilter) {
      fSubFilter = aSubFilter;
    }

    @Override
    public boolean accept(TLcdDomainObjectContext aObject) {
      Object domainObject = aObject.getDomainObject();
      EMilitarySymbology militarySymbology = MilitarySymbolFacade.getMilitarySymbology(domainObject);
      boolean subFilterOK = fSubFilter == null || (militarySymbology != null && fSubFilter.accept(militarySymbology.toString()));
      return subFilterOK && MilitarySymbolFacade.canReversePoints(domainObject);
    }
  }

}
