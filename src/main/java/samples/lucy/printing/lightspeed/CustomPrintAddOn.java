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
package samples.lucy.printing.lightspeed;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Arrays;

import com.luciad.gui.TLcdCompositePageable;
import com.luciad.gui.TLcdWatermarkPrintable;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.print.lightspeed.TLcyLspPrintAddOn;
import com.luciad.lucy.addons.print.lightspeed.TLcyLspPrintContext;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.printing.TitlePageable;

/**
 * <p>Custom extension of the {@code TLcyLspPrintAddOn}. This add-on adds the following behavior:</p>
 *
 * <ul>
 *   <li>Adds a quick print action to the toolbar of the Lightspeed maps</li>
 *   <li>Adds a watermark to all Lightspeed prints</li>
 *   <li>Adds a title page to all Lightspeed prints</li>
 * </ul>
 */
public class CustomPrintAddOn extends TLcyLspPrintAddOn {

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    addQuickPrintActionToEachMapComponent();
  }

  private void addQuickPrintActionToEachMapComponent() {
    TLcyLspMapManager mapManager = getLucyEnv().getService(TLcyLspMapManager.class);
    mapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILspView, ILspLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          ILcyLspMapComponent mapComponent = (ILcyLspMapComponent) aMapManagerEvent.getMapComponent();
          QuickPrintAction action = new QuickPrintAction(CustomPrintAddOn.this, mapComponent);
          action.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "customQuickPrintAction");
          TLcyActionBarUtil.insertInConfiguredActionBars(action, mapComponent,
                                                         getLucyEnv().getUserInterfaceManager().getActionBarManager(), getPreferences());
        }
      }
    }, true);
  }

  @Override
  public Printable createPageDecorator(TLcyLspPrintContext aPrintContext) {
    return new TLcdWatermarkPrintable();
  }

  @Override
  public Pageable createPrintPageable(Pageable aPageable, TLcyLspPrintContext aPrintContext) {
    // Reuse the original page format for the title page, but make sure it's portrait.
    PageFormat originalPageFormat = aPageable.getPageFormat(0);
    PageFormat titlePageFormat = (PageFormat) originalPageFormat.clone();
    titlePageFormat.setOrientation(PageFormat.PORTRAIT);

    // Create and append a title page.
    // Note that the title page will not be visible in the print preview.
    TitlePageable titlePageable = new TitlePageable(titlePageFormat);
    return new TLcdCompositePageable(Arrays.asList(titlePageable, aPageable));
  }
}
