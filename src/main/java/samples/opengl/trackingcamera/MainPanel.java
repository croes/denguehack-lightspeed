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
package samples.opengl.trackingcamera;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.opengl.ALcdGLTrackingCamera;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLGeocentricFixedReferenceCameraAdapter;
import com.luciad.view.opengl.TLcdGLLookAtTrackingCamera;
import com.luciad.view.opengl.TLcdGLLookFromTrackingCamera;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.TLcdGLViewEvent;
import com.luciad.view.opengl.controller.composite.ALcdGLControllerAction;
import com.luciad.view.opengl.controller.composite.TLcdGLCompositeController;
import com.luciad.view.opengl.controller.composite.TLcdGLGeocentricFixedEyeRotationControllerAction;
import com.luciad.view.opengl.controller.composite.TLcdGLGeocentricRotationControllerAction;
import com.luciad.view.opengl.controller.composite.TLcdGLOrientedCameraRotationControllerAction;
import samples.opengl.common.Abstract3DPanel;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.ZSlider;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Main panel of the trackingcamera sample.
 */
public class MainPanel extends Abstract3DPanel {

  private TLcdGLViewCanvas fCanvas;
  private TLcdGeocentricReference fGeocentricRef;
  private JComboBox fCameraChoice;
  private ALcdGLTrackingCamera[] fCameras;
  private int fWhichCamera = 0;
  private ILcdModel[] fMovingModels;
  Object[] fMovingObjects;

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    fCanvas = super.createCanvas();
    fCanvas.addViewListener(
      new ALcdGLViewAdapter() {
        @Override
        public void preRender(TLcdGLViewEvent aViewEvent) {
          if (fCanvas.getCamera() != fCameras[fWhichCamera]) {
            ALcdGLTrackingCamera camera = fCameras[fWhichCamera];
            camera.setCollectChanges(true);
            camera.setWidth(fCanvas.getWidth());
            camera.setHeight(fCanvas.getHeight());
            camera.setCollectChanges(false);
            camera.applyCollectedChanges();
            fCanvas.setCamera(camera);
          }
        }
      }
    );
    
    fGeocentricRef = new TLcdGeocentricReference(new TLcdGeodeticDatum());
    fCanvas.setXYZWorldReference(fGeocentricRef);

    // create three different tracking cameras:
    // 1. fixed reference camera
    // 2. fixed eye camera
    // 3. oriented eye camera
    fCameras = new ALcdGLTrackingCamera[3];
    fCameras[0] = createLookAtCamera();
    fCameras[1] = createLookFromCamera();
    fCameras[2] = createOrientedCamera();

    // set the fixed reference camera as startup camera
    fCanvas.setCamera(fCameras[0]);

    return fCanvas;
  }

  private TLcdGLLookAtTrackingCamera createLookAtCamera() {
    TLcdGLLookAtTrackingCamera camera = new TLcdGLLookAtTrackingCamera(fCanvas.getXYZWorldReference());
    camera.setMinDistance(50000);

    TLcdGLGeocentricFixedReferenceCameraAdapter adapter = new TLcdGLGeocentricFixedReferenceCameraAdapter((ILcdGeocentricReference) fCanvas.getXYZWorldReference());
    adapter.setCamera(camera);
    adapter.setCollectChanges(true);
    adapter.setLocation(new TLcdLonLatPoint(0, 0));
    adapter.setPitch(-60);
    adapter.setYaw(330);
    adapter.setDistance(5000000f);
    adapter.applyCollectedChanges();
    adapter.setCollectChanges(false);
    
    return camera;
  }

  private TLcdGLLookFromTrackingCamera createLookFromCamera() {
    TLcdGLLookFromTrackingCamera camera = new TLcdGLLookFromTrackingCamera(
          fCanvas.getXYZWorldReference(), true, false
    ) {
      @Override
      protected ILcdPoint chooseTargetPoint() {
        // get the 'default' target point
        TLcdXYZPoint target = new TLcdXYZPoint(super.chooseTargetPoint());

        TLcdXYZPoint modelPoint = new TLcdXYZPoint();
        try {
          TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference();
          g2g.setDestinationReference( (ILcdGeocentricReference)fGeocentricRef );
          g2g.setSourceReference( fMovingModels[0].getModelReference() );

          //transform the point from world to model coordinates
          g2g.destinationPoint2sourceSFCT( super.chooseTargetPoint(), modelPoint );

          // customize the point altitude, in model reference coordinates
          double dH = 10000;
          modelPoint.move3D(modelPoint.getX(), modelPoint.getY(), dH + modelPoint.getZ());

          //transform back to world coordinates
          g2g.sourcePoint2destinationSFCT( modelPoint, target );
        } catch ( TLcdOutOfBoundsException e ) {
          e.printStackTrace();
          //transformation didn't work. Just return the original target point instead.
        }

        // return the customized point
        return target;
      }
    };

    // setup the initial camera orientation (i.e. the gaze direction)
    TLcdGLGeocentricFixedReferenceCameraAdapter adapter = new TLcdGLGeocentricFixedReferenceCameraAdapter((ILcdGeocentricReference) fCanvas.getXYZWorldReference());
    adapter.setCamera(camera);
    adapter.setCollectChanges(true);
    adapter.setLocation(new TLcdLonLatPoint(0, 0));
    adapter.setPitch(10);
    adapter.setYaw(45);
    adapter.setDistance(5000000f);
    adapter.applyCollectedChanges();
    adapter.setCollectChanges(false);

    return camera;
  }

  private TLcdGLLookFromTrackingCamera createOrientedCamera() {
    TLcdGLLookFromTrackingCamera camera = new TLcdGLLookFromTrackingCamera(
          fCanvas.getXYZWorldReference(), true, true);
    camera.setLocalVector(-75000,0,-10000);

    return camera;
  }

  protected void createGUI() {
    super.createGUI();

    fCameraChoice = new JComboBox();
    fCameraChoice.addItem("Look at target");
    fCameraChoice.addItem("Look from target");
    fCameraChoice.addItem("Oriented like target");
    fCameraChoice.setSelectedIndex(0);
    
    fCameraChoice.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fWhichCamera = fCameraChoice.getSelectedIndex();
        if (fWhichCamera == 0) {
          setupControllersForLookAtCamera();
        }
        else if (fWhichCamera == 1) {
          setupControllersForLookFromCamera();
        }
        else if (fWhichCamera == 2) {
          setupControllersForOrientedCamera();
        }
      }
    });

    JPanel cameraPanel = new JPanel(new BorderLayout());
    cameraPanel.add(new JLabel("Camera choice"), BorderLayout.NORTH);
    cameraPanel.add(fCameraChoice, BorderLayout.CENTER);
    super.setComponentNorthEast(cameraPanel);

    setupControllersForLookAtCamera();
  }

  protected ZSlider createZSlider() {
    ZSlider zslider = super.createZSlider();
    zslider.setVisible(false);
    return zslider;
  }

  protected void addData() {
    getCanvas().addLayer(GLViewSupport.createGridLayer(ModelFactory.createGridModel(), getCanvas()));
    ILcdModel model= ModelFactory.createMultiPointModel();
    getCanvas().addModel(model);

    // retrieve an array of objects to be tracked by the camera
    // in this example, we take all the elements of the model
    List<Object> elemList = new ArrayList<Object>();
    for (Enumeration elements = model.elements(); elements.hasMoreElements(); ) {
      elemList.add(elements.nextElement());
    }
    fMovingObjects = elemList.toArray();
    fMovingModels = new ILcdModel[fMovingObjects.length];
    for(int i = 0; i < fMovingModels.length; i++) fMovingModels[i] = model;

    // set the model and the objects to be tracked in the tracking camera
    fCameras[0].setTrackedObjects(fMovingObjects, fMovingModels);
    fCameras[1].setTrackedObjects(fMovingObjects, fMovingModels);
    fCameras[2].setTrackedObjects(new Object[] { fMovingObjects[0] }, new ILcdModel[] { fMovingModels[0] });
  }

  private void setupControllersForLookAtCamera() {
    TLcdGLCompositeController cc = getToolbar().getCompositeController();

    // use a rotation action that rotates around the reference point
    // and NOT around the mouse location
    boolean useMouseCenteredRotation = false;
    cc.setRotateAction(new TLcdGLGeocentricRotationControllerAction(useMouseCenteredRotation));
    cc.getRotateAction().startInteraction( getCanvas() );
  }

  private void setupControllersForLookFromCamera() {
    TLcdGLCompositeController cc = getToolbar().getCompositeController();

    // set a geocentric fixed-eye rotate action
    cc.setRotateAction(new TLcdGLGeocentricFixedEyeRotationControllerAction(fGeocentricRef));

    // disable the pan action
    cc.setPanAction(new NoMotionAction());
  }

  private void setupControllersForOrientedCamera() {
    TLcdGLCompositeController cc = getToolbar().getCompositeController();

    // set a geocentric fixed-eye rotate action
    cc.setRotateAction(new TLcdGLOrientedCameraRotationControllerAction(fGeocentricRef));

    // disable the pan action in the composite controller
    cc.setPanAction(new NoMotionAction());
  }

  /**
   * A controller action that does nothing.
   */
  private static class NoMotionAction extends ALcdGLControllerAction {
    protected void doInteraction(ILcdGLView aGLView, double aX, double aY, double aDeltaX, double aDeltaY) {
      // do nothing
    }
  }

}

