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

import static samples.lucy.cop.addons.missioncontroltheme.GeoJsonRestModelCreateControllerFactory.newCreateController;

import javax.swing.JPanel;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.action.lightspeed.TLcyLspSetControllerActiveSettable;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.common.controller.LonLatCreateControllerModel;

/**
 * UI which contains some create controllers
 */
final class CreateControllersPanel extends JPanel {
  private static final String CONTROLLER_TOOLBAR_ID = "controllersToolBar";
  private static final String CONTROLLER_TOOLBAR_PREFIX = CONTROLLER_TOOLBAR_ID + ".";
  private static final String CONTROLLER_ACTIVE_SETTABLE = "activeSettable";

  private static final String POLYGON_PREFIX = "polygon.";
  private static final String POLYLINE_PREFIX = "polyline.";
  private static final String SPOT_REPORT_PREFIX = "spotReport.";

  private final String fPropertiesPrefix;
  private final ALcyProperties fProperties;
  private final ILcyLucyEnv fLucyEnv;

  CreateControllersPanel(String aPropertiesPrefix, ALcyProperties aProperties, ILspInteractivePaintableLayer aAnnotationsLayer, ILspInteractivePaintableLayer aSpotReportsLayer, ILcyLspMapComponent aMapComponent, ILcyLucyEnv aLucyEnv) {
    fPropertiesPrefix = aPropertiesPrefix;
    fProperties = aProperties;
    fLucyEnv = aLucyEnv;

    add(createToolBar(aMapComponent).getComponent());

    ILcyActiveSettable[] activeSettables = new ILcyActiveSettable[]{
        createControllerActiveSettable(POLYGON_PREFIX, aAnnotationsLayer, LonLatCreateControllerModel.Type.POLYGON, aMapComponent),
        createControllerActiveSettable(POLYLINE_PREFIX, aAnnotationsLayer, LonLatCreateControllerModel.Type.POLYLINE, aMapComponent),
        createControllerActiveSettable(SPOT_REPORT_PREFIX, aSpotReportsLayer, LonLatCreateControllerModel.Type.POINT2D, aMapComponent)
    };

    for (ILcyActiveSettable activeSettable : activeSettables) {
      TLcyActionBarUtil.insertInConfiguredActionBars(activeSettable, aMapComponent, fLucyEnv.getUserInterfaceManager().getActionBarManager(), fProperties);
    }
  }

  private ILspController createController(ILspInteractivePaintableLayer aLayer, LonLatCreateControllerModel.Type aType, ILspController aControllerAfterCreate, ILspView aView) {
    AGeoJsonRestModelWithUpdates model = (AGeoJsonRestModelWithUpdates) aLayer.getModel();
    return newCreateController(aLayer, model, aType, aControllerAfterCreate, aView);
  }

  private ILcyActiveSettable createControllerActiveSettable(String aPrefix, ILspInteractivePaintableLayer aLayer, LonLatCreateControllerModel.Type aType, ILcyLspMapComponent aMapComponent) {
    ILspController controller = createController(aLayer, aType, aMapComponent.getMainView().getController(), aMapComponent.getMainView());
    TLcyLspSetControllerActiveSettable activeSettable = new TLcyLspSetControllerActiveSettable(controller, aMapComponent.getMainView(), fLucyEnv);
    activeSettable.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + aPrefix + CONTROLLER_ACTIVE_SETTABLE);
    return activeSettable;
  }

  private ILcyToolBar createToolBar(ILcyLspMapComponent aMapComponent) {
    TLcyToolBar toolBar = new TLcyToolBar();
    toolBar.setProperties(fProperties.subset(fPropertiesPrefix + CONTROLLER_TOOLBAR_PREFIX));
    TLcyActionBarMediatorBuilder.newInstance(fLucyEnv.getUserInterfaceManager().getActionBarManager())
                                .sourceActionBar(CONTROLLER_TOOLBAR_ID, aMapComponent)
                                .targetActionBar(toolBar)
                                .bidirectional()
                                .mediate();
    return toolBar;
  }
}
