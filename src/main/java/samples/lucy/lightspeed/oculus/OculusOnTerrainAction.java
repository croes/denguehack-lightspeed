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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdSymbol;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.TLcyMapManagerEvent;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.height.TLcyViewHeightProvider;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.concurrent.TLcdLockUtil.Lock;
import com.luciad.util.height.ALcdModelHeightProviderFactory;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;

import samples.lightspeed.oculus.controller.OculusMoveController;
import samples.lucy.util.LayerUtil;

/**
 * This action will open a new Oculus view with a movement controller configured on it. In the Oculus view the camera will be placed
 * at a fixed height above the terrain at the last right-click location. In the main view a layer is added with a point indicating where
 * the camera in the Oculus view is currently located. This point is editable, allowing you to reposition the camera in the Oculus view.
 *
 * By using the arrow keys it is possible to move around in the Oculus view. The camera will remain at the same height
 * above the terrain while moving around.
 *
 **/
class OculusOnTerrainAction extends ALcdAction implements ILcdDisposable {

  private static final double HEIGHT_ABOVE_TERRAIN = 20;

  private final ILcyLucyEnv fLucyEnv;
  private final ILcyLspMapComponent fMapComponent;
  private Point fLastViewCoordinate;
  private ILcdModel fLocationModel;
  private ILspLayer fLocationLayer;

  public OculusOnTerrainAction(ILcyLspMapComponent aMapComponent, ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fMapComponent = aMapComponent;

  }

  public void setClickedCoordinate(Point aPoint) {
    fLastViewCoordinate = aPoint;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ILspView view = fMapComponent.getMainView();
    ALspViewXYZWorldTransformation viewXYZWorldTransformation = view.getViewXYZWorldTransformation();
    if (!(viewXYZWorldTransformation instanceof TLspViewXYZWorldTransformation3D)) {
      return;
    }

    ILcd3DEditablePoint location = new TLcdXYZPoint();
    try {
      viewXYZWorldTransformation.viewAWTPoint2worldSFCT(fLastViewCoordinate, ALspViewXYZWorldTransformation.LocationMode.ELLIPSOID, location);
      TLcdDefaultModelXYZWorldTransformation transformation = new TLcdDefaultModelXYZWorldTransformation(new TLcdGeodeticReference(), view.getXYZWorldReference());
      transformation.worldPoint2modelSFCT(location, location);

    } catch (TLcdOutOfBoundsException e1) {
      return;
    }

    if (fLocationModel == null) {
      fLocationModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("", "", "Oculus position"));
      fLocationModel.addElement(location, ILcdModel.NO_EVENT);

      initializeOculusView(fLocationModel);

      // Add the model with the location to the main view and add a look-at constraint to keep this point in the view.
      fLocationLayer = TLspShapeLayerBuilder.newBuilder()
                                            .model(fLocationModel)
                                            .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder()
                                                                                             .icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 20))
                                                                                             .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                                                             .build())
                                            .bodyEditable(true)
                                            .build();

      // Close the Oculus view when the layer in the main view is removed.
      fMapComponent.getMainView().addLayeredListener(new ILcdLayeredListener() {
        @Override
        public void layeredStateChanged(TLcdLayeredEvent e) {
          if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED && e.getLayer().equals(fLocationLayer)) {
            OculusViewManager.destroyOculusView();
            fLocationModel = null;
            fMapComponent.getMainView().removeLayeredListener(this);
          }
        }
      });

      fLucyEnv.getCombinedMapManager().addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
        @Override
        public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
          if (aMapManagerEvent.getId() == TLcyMapManagerEvent.MAP_COMPONENT_REMOVED &&
              aMapManagerEvent.getMapComponent() == fMapComponent) {
            OculusViewManager.destroyOculusView();
            fLocationModel = null;
            fLucyEnv.getCombinedMapManager().removeMapManagerListener(this);
          }
        }
      });

      fLocationLayer.selectObject(location, true, ILcdFireEventMode.FIRE_NOW);
      fMapComponent.getMainView().addLayer(fLocationLayer);
    } else {
      ILcd3DEditablePoint previousLocation = (ILcd3DEditablePoint) fLocationModel.elements().nextElement();
      previousLocation.move3D(location);
      fLocationModel.elementChanged(previousLocation, ILcdModel.FIRE_NOW);
    }
  }

  /**
   * Initializes an Oculus view with a movement controller and places the camera at the location determined by the
   * point in the provided model.
   *
   * @param aLocationModel the model containing the location
   */
  private void initializeOculusView(final ILcdModel aLocationModel) {
    final ILspView oculusView = OculusViewManager.getOculusView(this);

    LayerUtil.copyLayers(fMapComponent.getMainView(), oculusView, fLucyEnv);

    final Map<String, Object> requiredProperties = new HashMap<>();
    requiredProperties.put(ALcdModelHeightProviderFactory.KEY_GEO_REFERENCE, new TLcdGeodeticReference());
    final ILcdHeightProvider heightProvider = new TLcyViewHeightProvider(fMapComponent.getMainView(), requiredProperties, Collections.<String, Object>emptyMap(), fLucyEnv);

    placeOnTerrain(oculusView, heightProvider, aLocationModel);

    aLocationModel.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        if (aEvent.getCode() == TLcdModelChangedEvent.OBJECT_CHANGED) {
          aLocationModel.removeModelListener(this);
          placeOnTerrain(oculusView, heightProvider, aLocationModel);
          aLocationModel.addModelListener(this);
        }
      }
    });

    ILspController controller = new OculusMoveController(heightProvider, HEIGHT_ABOVE_TERRAIN, aLocationModel);
    oculusView.setController(controller);
  }

  /**
   * Positions the camera on the surface of the earth.
   *
   * @param aOculusView the oculus view
   * @param aHeightProvider a height provider, used to determine the height at the requested camera's location.
   * @param aModel the model containing the position
   *
   */
  private void placeOnTerrain(final ILspView aOculusView, final ILcdHeightProvider aHeightProvider, final ILcdModel aModel) {

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) aOculusView.getViewXYZWorldTransformation();
        try (Lock autoUnlock = TLcdLockUtil.writeLock(aModel)) {
          ILcd3DEditablePoint position = (ILcd3DEditablePoint) aModel.elements().nextElement();
          double height = aHeightProvider.retrieveHeightAt(position);
          position.move3D(position.getX(), position.getY(), height + HEIGHT_ABOVE_TERRAIN);
          aModel.elementChanged(position, ILcdModel.FIRE_LATER);

          TLcdXYZPoint wp = new TLcdXYZPoint();
          TLcdEllipsoid.DEFAULT.llh2geocSFCT(
              position,
              wp
          );

          v2w.lookFrom(wp, 1000, v2w.getYaw(), 0, 0);
        } finally {
          aModel.fireCollectedModelChanges();
        }
      }
    });
  }

  @Override
  public void dispose() {
    fMapComponent.getMainView().removeLayer(fLocationLayer);
  }
}
