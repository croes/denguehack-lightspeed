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
package samples.lightspeed.oculus.controller;

import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luciad.earth.tileset.terrain.TLcdEarthHeightProviderFactory;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.concurrent.TLcdLockUtil.Lock;
import com.luciad.util.height.ALcdRasterModelHeightProviderFactory;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;

import samples.common.SampleData;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.oculus.common.OculusSample;

/**
 * This sample shows how you can create an <code>ALspController</code> that allows you to move around when using an
 * Oculus view. The controller will keep you positioned at the same height above the terrain.
 */
public class MainPanel extends OculusSample {

  private static final double HEIGHT_ABOVE_TERRAIN = 200;

  public static void main(String[] args) {
    startSample(MainPanel.class, "Oculus Rift controller sample");
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // The position of where to place the camera.
    ILcd3DEditablePoint position = new TLcdLonLatHeightPoint(-122.5, 37.75, 0);
    final ILcdModel model = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("", "", "Position"));
    model.addElement(position, ILcdModel.NO_EVENT);
    TLcdSymbol positionIcon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE);
    positionIcon.setSize(20);

    final ILspLayer positionLayer = TLspShapeLayerBuilder.newBuilder()
                                                         .model(model)
                                                         .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(positionIcon).build())
                                                         .bodyEditable(true)
                                                         .build();
    //add the background layer to the Oculus view and the position layer to the main view.
    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        ILspLayer oculusBackgroundLayer = LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().getLayer();
        getOculusView().addLayer(oculusBackgroundLayer);

        getView().addLayer(positionLayer);
        FitUtil.fitOnLayers(MainPanel.this, positionLayer);
      }
    });

    // create a height provider with for the terrain model.
    ILcdModel terrainModel = LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).getModel();
    Map<String, Object> requiredProperties = new HashMap<>();
    requiredProperties.put(ALcdRasterModelHeightProviderFactory.KEY_GEO_REFERENCE, terrainModel.getModelReference());
    final ILcdHeightProvider heightProvider = new TLcdEarthHeightProviderFactory().createHeightProvider(terrainModel, requiredProperties, Collections.<String, Object>emptyMap());
    placeOnTerrain(heightProvider, model);

    model.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        if (aEvent.getCode() == TLcdModelChangedEvent.OBJECT_CHANGED) {
          model.removeModelListener(this);
          placeOnTerrain(heightProvider, model);
          model.addModelListener(this);
        }
      }
    });

    // add the controller to the view.
    ILspController controller = new OculusMoveController(heightProvider, HEIGHT_ABOVE_TERRAIN, model);
    getOculusView().setController(controller);
  }

  /**
   * Positions the camera on the surface of the earth.
   *
   * @param aHeightProvider a height provider, used to determine the height at the requested camera's location.
   * @param aModel the model containing the position
   *
   */
  private void placeOnTerrain(final ILcdHeightProvider aHeightProvider, final ILcdModel aModel) {

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try (Lock autoUnlock = writeLock(aModel)) {
          TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) getOculusView().getViewXYZWorldTransformation();
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
}
