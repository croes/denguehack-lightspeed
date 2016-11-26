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
package samples.lucy.lightspeed.oculus;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.oculus.TLspOculusDeviceBuilder;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspAWTView;

/**
 * This add-on plugs in an action that allows to open a new Oculus view to track an object and one to position
 * the Oculus view on the terrain. The pop-up menu that appears when right clicking the map contains two entries related
 * to the Oculus view. The one for tracking an object is only available when a point is right clicked.
 */
public class OculusAddOn extends ALcyPreferencesAddOn {

  private MyMapManagerListener fMapManagerListener;

  public OculusAddOn() {
    super(ALcyTool.getLongPrefix(OculusAddOn.class), ALcyTool.getShortPrefix(OculusAddOn.class));
  }

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    if (!TLcdSystemPropertiesUtil.isWindows()) {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          Component topLevelComponent = aLucyEnv.getUserInterfaceManager().getTopLevelComponent(0);
          JOptionPane pane = new JOptionPane(TLcyLang.getString("The Oculus add-on is only supported on Microsoft Windows."),
                                             JOptionPane.ERROR_MESSAGE);
          JDialog dialog = pane.createDialog(topLevelComponent, UIManager.getString("OptionPane.messageDialogTitle", null));
          dialog.setModal(false);
          dialog.setLocationRelativeTo(topLevelComponent);
          dialog.setVisible(true);
        }
      });
    }

    loadRequiredClassForQuickFail();
    TLcyCombinedMapManager mapManager = aLucyEnv.getCombinedMapManager();
    fMapManagerListener = new MyMapManagerListener();
    mapManager.addMapManagerListener(fMapManagerListener, true);
    boolean monoscopic = getPreferences().getBoolean(getShortPrefix() + "view.monoscopic", false);
    boolean mirror = getPreferences().getBoolean(getShortPrefix() + "view.mirror", true);
    OculusViewManager.setMonoscopic(monoscopic);
    OculusViewManager.setMirrorEnabled(mirror);
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    TLcyCombinedMapManager mapManager = aLucyEnv.getCombinedMapManager();
    mapManager.removeMapManagerListener(fMapManagerListener);
    fMapManagerListener = null;
  }

  private void loadRequiredClassForQuickFail() {
    // Load a class from the required Additional or Industry Specific Component to provoke a NoClassDefFoundError
    // if the jar file is not present.  If not, the NoClassDefFoundError might happen at an
    // unexpected location, for example when a new map is created, which can crash the application.
    TLspOculusDeviceBuilder.class.getName();
  }

  private void addOculusActions(ILcyLspMapComponent aMapComponent) {
    ALcdAction oculusAction = new OculusTrackAction(aMapComponent, getLucyEnv());
    oculusAction.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "openTrackingOculusView");
    TLcyActionBarUtil.insertInConfiguredActionBars(oculusAction, aMapComponent, getLucyEnv().getUserInterfaceManager().getActionBarManager(), getPreferences());

    final OculusOnTerrainAction oculusOnTerrainAction = new OculusOnTerrainAction(aMapComponent, getLucyEnv());
    oculusOnTerrainAction.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "openOnTerrainOculusView");
    TLcyActionBarUtil.insertInConfiguredActionBars(oculusOnTerrainAction, aMapComponent, getLucyEnv().getUserInterfaceManager().getActionBarManager(), getPreferences());

    ((ILspAWTView) aMapComponent.getMainView()).getHostComponent().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          oculusOnTerrainAction.setClickedCoordinate(e.getPoint());
        }
      }
    });
  }

  /**
   * Listener to check if a new map was added.
   */
  private class MyMapManagerListener implements ILcyGenericMapManagerListener {

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent aEvent) {
      if (aEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
        ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> mapComponent = aEvent.getMapComponent();
        if (mapComponent instanceof ILcyLspMapComponent) {
          addOculusActions((ILcyLspMapComponent) mapComponent);
        }
      }
    }
  }
}
