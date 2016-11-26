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
package samples.lucy.fundamentals.waypoints;

import java.awt.Component;

import javax.swing.JComponent;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponent;
import com.luciad.lucy.model.ALcyDefaultModelDescriptorFactory;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lucy.util.LayerUtil;

/**
 * {@link ALcyFormatBar} implementation for way point layers
 */
final class WayPointsFormatBar extends ALcyFormatBar {
  /**
   * The actual Swing component representing the format bar
   */
  private final TLcyToolBar fToolBar = new TLcyToolBar();
  private final WayPointsCreateControllerModel fControllerModel;

  public WayPointsFormatBar(TLcyLspMapComponent aLspMapComponent,
                            ALcyProperties aProperties,
                            String aShortPrefix,
                            ALcyDefaultModelDescriptorFactory aDefaultModelDescriptorFactory,
                            ILcyLucyEnv aLucyEnv) {
    putValue(ALcyFormatBar.NAME, "Way Points");
    putValue(ALcyFormatBar.SHORT_DESCRIPTION, "Create way points");

    //Allow TLcyActionBarUtil (and other add-ons) to contribute to our tool bar
    TLcyActionBarManager actionBarManager = aLucyEnv.getUserInterfaceManager().getActionBarManager();
    TLcyActionBarUtil.setupAsConfiguredActionBar(fToolBar,
                                                 WayPointsFormatBarFactory.TOOLBAR_ID,
                                                 aLspMapComponent,
                                                 aProperties,
                                                 aShortPrefix,
                                                 (JComponent) aLspMapComponent.getComponent(),
                                                 actionBarManager);

    TLcyLspCreateLayerAction createLayerAction = new TLcyLspCreateLayerAction(aLucyEnv, aLspMapComponent);
    createLayerAction.setDefaultModelDescriptorFactory(aDefaultModelDescriptorFactory);

    fControllerModel = new WayPointsCreateControllerModel(aLspMapComponent, createLayerAction);
    LayerUtil.insertCreateShapeActiveSettable(aProperties, aShortPrefix, aLucyEnv, aLspMapComponent, fControllerModel);
  }

  @Override
  protected void updateForLayer(ILcdLayer aPreviousLayer, ILcdLayer aLayer) {
    fControllerModel.setCurrentLayer((ILspInteractivePaintableLayer) aLayer);
  }

  @Override
  public boolean canSetLayer(ILcdLayer aLayer) {
    // TLcyLspSafeGuardFormatWrapper already checks the layer
    return true;
  }

  @Override
  public Component getComponent() {
    return fToolBar.getComponent();
  }

}
