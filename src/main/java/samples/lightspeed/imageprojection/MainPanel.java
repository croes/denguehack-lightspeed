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
package samples.lightspeed.imageprojection;

import java.io.IOException;

import com.luciad.imaging.TLcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.icons3d.FlyCircleAnimation;
import samples.lightspeed.icons3d.OrientedPoint;

/**
 * This samples demonstrates the image projection feature.
 * It can for example be used to visualize the video feed of UAV.
 */
public class MainPanel extends LightspeedSample {

  private static final double LON = -122.51;
  private static final double LAT = 37.84;
  private static final double ALTITUDE = 500;

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  protected void addData() throws IOException {
    super.addData();

    // Model and layer for the plane projecting the image
    ILcdModel planeModel = createPlaneModel();
    IconLayerFactory planeLayerFactory = new IconLayerFactory();
    getView().addLayer(planeLayerFactory.createLayer(planeModel));

    // Model and layer for the image projection
    ILcdModel imageProjectionModel = createImageProjectionModel(planeModel);
    ProjectionLayerFactory projectionLayerFactory = new ProjectionLayerFactory();
    ILspImageProjectionLayer projectionLayer = (ILspImageProjectionLayer) projectionLayerFactory.createLayer(imageProjectionModel);
    getView().addLayer(projectionLayer);

    // Layer for the outlines of the image projection
    getView().addLayer(createFrustumLayer(projectionLayer));

    // Fit on the plane
    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(LON - 0.02, LAT - 0.02, 0.04, 0.04), new TLcdGeodeticReference());
  }

  /**
   * Creates a layer that shows the frustum (e.g. outline) of the image projection.
   *
   * @param aImageProjectionLayer the image projection layer
   *
   * @return the frustum layer
   */
  private static ILspLayer createFrustumLayer(ILspImageProjectionLayer aImageProjectionLayer) {
    return TLspShapeLayerBuilder
        .newBuilder(ILspLayer.LayerType.INTERACTIVE)
        .model(aImageProjectionLayer.getModel())
        .selectable(false)
        .bodyEditable(false)
        .label("Frustum")
        .culling(false)
        .bodyStyler(TLspPaintState.REGULAR, new FrustumStyler(aImageProjectionLayer))
        .build();
  }

  /**
   * Creates a model with a plane.
   *
   * @return the model
   */
  private static ILcdModel createPlaneModel() {
    TLcdVectorModel planeModel = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor("Plane", "Plane", "Plane")
    );

    OrientedPoint pt = new OrientedPoint(LON, LAT, ALTITUDE, 0, 0, 0);
    planeModel.addElement(pt, ILcdModel.NO_EVENT);

    // Start an animation that moves the plane around
    TLcdLonLatHeightPoint center = new TLcdLonLatHeightPoint(pt);
    FlyCircleAnimation animation = new FlyCircleAnimation(planeModel, pt, center, 2000, true, 100);
    ALcdAnimationManager.getInstance().putAnimation(pt, animation);

    return planeModel;
  }

  /**
   * Creates an image projection model from a plane model
   *
   * @param aPlaneModel the model with the plane projecting the image
   *
   * @return the image projection model
   */
  private static ILcdModel createImageProjectionModel(final ILcdModel aPlaneModel) {
    // TLspImageProjectionLayerBuilder requires a model with at least one element and
    // an ILcdGeocentricReference or ILcdGridReference as model reference
    final TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeocentricReference(),
        new TLcdImageModelDescriptor("ImageProjection", "ImageProjection", "ImageProjection")
    );
    final OrientedPoint plane = (OrientedPoint) aPlaneModel.elements().nextElement();
    final ILcdModelReference planeRef = aPlaneModel.getModelReference();
    final ImageProjector projector = new ImageProjector(new TLcdGeocentricReference(), plane, planeRef);
    model.addElement(projector, ILcdModel.NO_EVENT);

    // Update the model as the plane moves around
    aPlaneModel.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
          projector.setLocation(plane, planeRef);
          model.elementChanged(projector, ILcdModel.FIRE_LATER);
        }
        model.fireCollectedModelChanges();
      }
    });

    return model;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Image projection");
  }

}
