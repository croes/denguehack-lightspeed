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
package samples.lucy.frontend.mapcentric.status;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;

/**
 * <p>
 *   Add-on which shows the status messages on the map, and the progress in a toolbar.
 * </p>
 *
 * <p>
 *   The map centric front-end hides the status bar.
 *   As a result, the status messages are no longer visible.
 *   This add-on picks up the status messages and displays them on the map.
 *   When the status message contains progress information, a progress indicator is shown in the tool bar.
 * </p>
 */
public class MapCentricStatusAddOn extends ALcyPreferencesAddOn {
  public MapCentricStatusAddOn() {
    super("samples.lucy.frontend.mapcentric.status.",
          "MapCentricStatusAddOn.");
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    ProgressIndicationAction progressIndicationAction = new ProgressIndicationAction(aLucyEnv);
    progressIndicationAction.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "progressIndicationAction");
    TLcyActionBarUtil.insertInConfiguredActionBars(progressIndicationAction,
                                                   null,
                                                   aLucyEnv.getUserInterfaceManager().getActionBarManager(),
                                                   getPreferences());

    new StatusMessageDisplayer(getShortPrefix(), getPreferences(), aLucyEnv);
  }
}
