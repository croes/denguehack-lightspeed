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
package samples.lightspeed.oculus.trackingcamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Timer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdPair;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookFromTrackingCameraConstraint3D;
import com.luciad.view.lightspeed.camera.tracking.TLspModelElementTrackingPointProvider;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.oculus.common.OculusSample;
import samples.lightspeed.trackingcamera.TrackLayerFactory;
import samples.lightspeed.trackingcamera.TrackModelFactory;

/**
 * This sample shows how the <code>TLspLookFromTrackingCameraConstraint3D</code> can be used to look from
 * a moving point in an Oculus view.
 */
public class MainPanel extends OculusSample {

  private Timer fTimer;

  public static void main(String[] args) {
    startSample(MainPanel.class, "Oculus Rift tracking sample");
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    TLcdPair<ILcdModel, Timer> modelAndTimer = TrackModelFactory.createMultiPointModel();
    ILcdModel model = modelAndTimer.getKey();
    fTimer = modelAndTimer.getValue();


    final ILspLayer layer = new TrackLayerFactory().createLayer(model);
    final ILspLayer oculusLayer = new TrackLayerFactory().createLayer(model);
    final ILspLayer oculusBackgroundLayer = LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().getLayer();
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        getView().addLayer(layer);
        getOculusView().addLayer(oculusLayer);
        getOculusView().addLayer(oculusBackgroundLayer);
      }
    });

    // retrieve an array of objects to be tracked by the camera
    // in this example, we take all the elements of the model
    List<Object> elemList = new ArrayList<Object>();
    for (Enumeration elements = model.elements(); elements.hasMoreElements(); ) {
      elemList.add(elements.nextElement());
    }
    Object[] movingObjects = elemList.toArray();
    ILcdModel[] movingModels = new ILcdModel[movingObjects.length];
    for (int i = 0; i < movingModels.length; i++) {
      movingModels[i] = model;
    }

    // add the look from constraint to look from the position of the planes.
    TLspLookFromTrackingCameraConstraint3D lookFromCameraConstraint = createLookFromCameraConstraint3D(true, movingObjects, movingModels);
    TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) getView().getViewXYZWorldTransformation();
    v2w.addConstraint(lookFromCameraConstraint);

    // apply the same constraint to the camera in the Oculus view
    v2w = (TLspViewXYZWorldTransformation3D) getOculusView().getViewXYZWorldTransformation();
    v2w.addConstraint(lookFromCameraConstraint);
  }

  @Override
  protected void tearDown() {
    fTimer.stop();
    super.tearDown();
  }

  /**
   * Creates a 3D look from tracking constraint.
   *
   * @param aFollowOrientation whether or not to follow the orientation of the plane.
   * @param aMovingObjects the tracked objects.
   * @param aMovingModels the models of the tracked objects.
   * @return a 3D look from constraint.
   */
  private TLspLookFromTrackingCameraConstraint3D createLookFromCameraConstraint3D(boolean aFollowOrientation,
                                                                                  Object[] aMovingObjects,
                                                                                  final ILcdModel[] aMovingModels) {
    TLspLookFromTrackingCameraConstraint3D constraint = new TLspLookFromTrackingCameraConstraint3D(aFollowOrientation);
    TLspModelElementTrackingPointProvider trackingPointProvider = new TLspModelElementTrackingPointProvider() {
      @Override
      public ILcdPoint getTargetPoint() {
        // Get the 'default' target point. The altitude of the target point is increased so that the camera is
        // positioned above the 3D plane. This is needed because the size of the 3D plane is exaggerated.
        ILcdPoint targetPoint = super.getTargetPoint();
        if (targetPoint == null) {
          return null;
        }

        TLcdXYZPoint target = new TLcdXYZPoint(targetPoint);

        TLcdXYZPoint modelPoint = new TLcdXYZPoint();
        try {
          TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference();
          g2g.setDestinationReference((ILcdGeoReference) getView().getXYZWorldReference());
          g2g.setSourceReference(aMovingModels[0].getModelReference());

          // Transform the point from world to model coordinates
          g2g.destinationPoint2sourceSFCT(targetPoint, modelPoint);

          // Customize the point altitude, in model reference coordinates
          double dH = 10000;
          modelPoint.move3D(modelPoint.getX(), modelPoint.getY(), dH + modelPoint.getZ());

          // Transform back to world coordinates
          g2g.sourcePoint2destinationSFCT(modelPoint, target);
        } catch (TLcdOutOfBoundsException e) {
          return null;
        }

        // Return the customized point
        return target;
      }
    };
    trackingPointProvider.setTrackedObjects(getView(), aMovingObjects, aMovingModels);
    constraint.setTrackingPointProvider(trackingPointProvider);
    return constraint;
  }
}
