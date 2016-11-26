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
package samples.lightspeed.trackingcamera;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdPair;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.aboveterrain.TLspAboveTerrainCameraConstraint3D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookAtTrackingCameraConstraint2D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookAtTrackingCameraConstraint3D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookFromTrackingCameraConstraint3D;
import com.luciad.view.lightspeed.camera.tracking.TLspModelElementTrackingPointProvider;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * This sample shows how <code>ALspTrackingCamera</code> and its extensions can be used to
 * implement cameras that automatically follow one or more moving objects.
 */
public class MainPanel extends LightspeedSample {

  private ILspAWTView fView;
  private JComboBox fCameraChoice;
  private ALspViewXYZWorldTransformation[] fCameras;
  private ILcdXYZWorldReference[] fReferences;
  private int fWhichCamera = 0;
  private ILcdModel[] fMovingModels;
  private Object[] fMovingObjects;
  private Timer fTimer;

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    return new JToolBar[]{new ToolBar(aView, this, false, false)};
  }

  @Override
  protected ILspAWTView createView(ILspView.ViewType aViewType) {
    fView = super.createView(aViewType);

    // This view listener changes the camera if needed
    fView.addViewListener(new ALspViewAdapter() {
      @Override
      public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
        if (!fReferences[fWhichCamera].equals(fView.getXYZWorldReference()) || fView.getViewXYZWorldTransformation() != fCameras[fWhichCamera]) {
          if (fCameras[fWhichCamera] instanceof TLspViewXYZWorldTransformation2D) {
            TLspViewXYZWorldTransformation2D camera = (TLspViewXYZWorldTransformation2D) fCameras[fWhichCamera];
            TLspViewTransformationUtil.setup2DView(camera, fView, fReferences[fWhichCamera], true);
          } else {
            TLspViewXYZWorldTransformation3D camera = (TLspViewXYZWorldTransformation3D) fCameras[fWhichCamera];
            TLspViewTransformationUtil.setup3DView(camera, fView, fReferences[fWhichCamera], true);
          }
        }
      }
    });

    // Set world reference to a geocentric reference
    fReferences = new ILcdXYZWorldReference[5];
    fReferences[0] = new TLcdGeocentricReference(new TLcdGeodeticDatum());
    fReferences[1] = fReferences[0];
    fReferences[2] = fReferences[0];
    fReferences[3] = new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical());
    fReferences[4] = fReferences[3];

    // create five different tracking cameras:
    // 1. fixed reference camera
    // 2. fixed eye camera
    // 3. oriented eye camera
    fCameras = new ALspViewXYZWorldTransformation[5];
    fCameras[0] = createViewXYZWorldTransformation3D(fView);
    fCameras[1] = createViewXYZWorldTransformation3D(fView);
    fCameras[2] = createViewXYZWorldTransformation3D(fView);
    fCameras[3] = createViewXYZWorldTransformation2D(fView);
    fCameras[4] = createViewXYZWorldTransformation2D(fView);

    return fView;
  }

  private TLspViewXYZWorldTransformation3D createViewXYZWorldTransformation3D(ILspView aView) {
    TLspViewXYZWorldTransformation3D transformation = new TLspViewXYZWorldTransformation3D(aView);
    transformation.addConstraint(new TLspAboveTerrainCameraConstraint3D());
    return transformation;
  }

  private TLspViewXYZWorldTransformation2D createViewXYZWorldTransformation2D(ILspView aView) {
    return new TLspViewXYZWorldTransformation2D(aView);
  }

  /**
   * Creates a 2D tracking constraint.
   * @param aFollowOrientation true to follow the orientation of the tracked object.
   * @param aView              the view.
   * @param aMovingObjects     the tracked objects.
   * @param aMovingModels      the models of the tracked objects.
   * @return a 2D tracking constraint.
   */
  private TLspLookAtTrackingCameraConstraint2D createTrackingConstraint2D(boolean aFollowOrientation,
                                                                          ILspView aView,
                                                                          Object[] aMovingObjects,
                                                                          ILcdModel[] aMovingModels) {
    TLspModelElementTrackingPointProvider trackingPointProvider = new TLspModelElementTrackingPointProvider();
    trackingPointProvider.setTrackedObjects(aView, aMovingObjects, aMovingModels);
    TLspLookAtTrackingCameraConstraint2D constraint = new TLspLookAtTrackingCameraConstraint2D(aFollowOrientation);
    constraint.setTrackingPointProvider(trackingPointProvider);
    return constraint;
  }

  /**
   * Creates a 3D look at tracking constraint.
   * @param aView              the view.
   * @param aMovingObjects     the tracked objects.
   * @param aMovingModels      the models of the tracked objects.
   * @return a 3D look at tracking constraint.
   */
  private TLspLookAtTrackingCameraConstraint3D createLookAtCameraConstraint3D(ILspView aView,
                                                                              Object[] aMovingObjects,
                                                                              ILcdModel[] aMovingModels) {
    TLspModelElementTrackingPointProvider trackingPointProvider = new TLspModelElementTrackingPointProvider();
    trackingPointProvider.setTrackedObjects(aView, aMovingObjects, aMovingModels);
    TLspLookAtTrackingCameraConstraint3D constraint = new TLspLookAtTrackingCameraConstraint3D();
    constraint.setTrackingPointProvider(trackingPointProvider);
    constraint.setMinDistance(50000);
    return constraint;
  }

  /**
   * Creates a 3D look from tracking constraint.
   * @param aFollowOrientation true to follow the orientation of the tracked object.
   * @param aView              the view.
   * @param aMovingObjects     the tracked objects.
   * @param aMovingModels      the models of the tracked objects.
   * @return a 3D look from tracking constraint.
   */
  private TLspLookFromTrackingCameraConstraint3D createLookFromCameraConstraint3D(boolean aFollowOrientation,
                                                                                  ILspView aView,
                                                                                  Object[] aMovingObjects,
                                                                                  ILcdModel[] aMovingModels) {
    TLspLookFromTrackingCameraConstraint3D constraint = new TLspLookFromTrackingCameraConstraint3D(aFollowOrientation);
    TLspModelElementTrackingPointProvider trackingPointProvider = new TLspModelElementTrackingPointProvider() {
      @Override
      public ILcdPoint getTargetPoint() {
        // Get the 'default' target point
        ILcdPoint targetPoint = super.getTargetPoint();
        if (targetPoint == null) {
          return null;
        }

        TLcdXYZPoint target = new TLcdXYZPoint(targetPoint);

        TLcdXYZPoint modelPoint = new TLcdXYZPoint();
        try {
          TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference();
          g2g.setDestinationReference((ILcdGeoReference) fView.getXYZWorldReference());
          g2g.setSourceReference(fMovingModels[0].getModelReference());

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
    trackingPointProvider.setTrackedObjects(aView, aMovingObjects, aMovingModels);
    constraint.setTrackingPointProvider(trackingPointProvider);
    return constraint;
  }

  protected void createGUI() {
    super.createGUI();

    // Add a combobox to choose the tracking mode
    fCameraChoice = new JComboBox();
    fCameraChoice.addItem("[3D]Look at target");
    fCameraChoice.addItem("[3D]Look from target");
    fCameraChoice.addItem("[3D]Oriented like target");
    fCameraChoice.addItem("[2D]Follow location");
    fCameraChoice.addItem("[2D]Follow location and orientation");
    fCameraChoice.setSelectedIndex(0);

    fCameraChoice.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fWhichCamera = fCameraChoice.getSelectedIndex();
      }
    });

    JPanel cameraPanel = new JPanel(new BorderLayout());
    cameraPanel.add(new JLabel("Camera choice"), BorderLayout.NORTH);
    cameraPanel.add(fCameraChoice, BorderLayout.CENTER);
    super.addComponentToRightPanel(cameraPanel);
  }

  protected void addData() throws IOException {
    super.addData();

    ServiceRegistry.getInstance().register(new TrackLayerFactory());

    TLcdPair<ILcdModel, Timer> modelAndTimer = TrackModelFactory.createMultiPointModel();
    ILcdModel model = modelAndTimer.getKey();
    fTimer = modelAndTimer.getValue();
    getView().addLayer(new TrackLayerFactory().createLayer(model));

    // retrieve an array of objects to be tracked by the camera
    // in this example, we take all the elements of the model
    List<Object> elemList = new ArrayList<Object>();
    for (Enumeration elements = model.elements(); elements.hasMoreElements(); ) {
      elemList.add(elements.nextElement());
    }
    fMovingObjects = elemList.toArray();
    fMovingModels = new ILcdModel[fMovingObjects.length];
    for (int i = 0; i < fMovingModels.length; i++) {
      fMovingModels[i] = model;
    }

    // set the model and the objects to be tracked in the tracking camera
    initializeTrackingLookAt3D((TLspViewXYZWorldTransformation3D) fCameras[0], getView());
    initializeTrackingLookFrom3D((TLspViewXYZWorldTransformation3D) fCameras[1], getView(), false);
    initializeTrackingLookFrom3D((TLspViewXYZWorldTransformation3D) fCameras[2], getView(), true);
    initializeTracking2D((TLspViewXYZWorldTransformation2D) fCameras[3], getView(), false);
    initializeTracking2D((TLspViewXYZWorldTransformation2D) fCameras[4], getView(), true);
  }

  @Override
  protected void tearDown() {
    fTimer.stop();
    super.tearDown();
  }

  private void initializeTracking2D(TLspViewXYZWorldTransformation2D aTransformation, ILspView aView, boolean aFollowOrientation) {
    aTransformation.addConstraint(createTrackingConstraint2D(aFollowOrientation, aView, fMovingObjects, fMovingModels));
  }

  private void initializeTrackingLookAt3D(TLspViewXYZWorldTransformation3D aTransformation, ILspView aView) {
    aTransformation.addConstraint(createLookAtCameraConstraint3D(aView, fMovingObjects, fMovingModels));
  }

  private void initializeTrackingLookFrom3D(TLspViewXYZWorldTransformation3D aTransformation, ILspView aView, boolean aFollowOrientation) {
    aTransformation.addConstraint(createLookFromCameraConstraint3D(aFollowOrientation, aView, fMovingObjects, fMovingModels));
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Tracking camera");
  }

}

