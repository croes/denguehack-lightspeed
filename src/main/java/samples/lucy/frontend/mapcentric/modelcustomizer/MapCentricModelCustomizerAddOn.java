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
package samples.lucy.frontend.mapcentric.modelcustomizer;

import java.awt.Component;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.ALcyApplicationPaneTool;

/**
 * Replacement for {@link com.luciad.lucy.addons.modelcustomizer.TLcyModelCustomizerAddOn}.
 *
 * Contrary to that add-on, it doesn't have a menu item to open a table view. Instead, one is
 * created on start-up and exists as long as the application exists. There can only be one
 * table view at any given time, whereas TLcyModelCustomizerAddOn allows multiple.
 */
public class MapCentricModelCustomizerAddOn extends ALcyPreferencesAddOn {
  private ALcyApplicationPaneTool fApplicationPaneTool;
  private ModelCustomizerPanelContainer fContentPane;

  public MapCentricModelCustomizerAddOn() {
    super("samples.lucy.frontend.mapcentric.modelcustomizer.",
          "MapCentricModelCustomizerAddOn.");
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    fContentPane = new ModelCustomizerPanelContainer(aLucyEnv);
    fApplicationPaneTool = new ALcyApplicationPaneTool(getPreferences(), getLongPrefix(), getShortPrefix()) {
      @Override
      protected Component createContent() {
        return fContentPane;
      }
    };
    fApplicationPaneTool.plugInto(aLucyEnv);
  }

  /**
   * <p>
   *   Returns the application pane tool which is used to create the application pane containing the model context
   *   customizer panel.
   * </p>
   * <p>
   *   A possible use-case of this method is showing/hiding the application pane (see {@link com.luciad.lucy.gui.ILcyApplicationPane#setAppVisible(boolean)})
   *   through the API.
   * </p>
   *
   * @return The application pane tool
   */
  public final ALcyApplicationPaneTool getApplicationPaneTool() {
    return fApplicationPaneTool;
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    fApplicationPaneTool.unplugFrom(aLucyEnv);
    super.unplugFrom(aLucyEnv);
  }
}
