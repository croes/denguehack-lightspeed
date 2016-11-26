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

import java.awt.AWTEvent;

import org.hiranabe.vecmath.Vector3d;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.controller.ALspController;

/**
 * Custom controller that can be used in an Oculus view. It allows for the camera to be moved forwards and backwards.
 * The movement will be in the direction the user is actually looking with the Oculus. Further, the yaw angle can
 * be adjusted.
 */
public class OculusMoveController extends ALspController {

  private ILcdHeightProvider fHeightProvider;
  private double fHeightAboveTerrain;
  private ILcdModel fPositionModel;

  public OculusMoveController(ILcdHeightProvider aHeightProvider, double aHeightAboveTerrain, ILcdModel aPositionModel) {
    fHeightProvider = aHeightProvider;
    fHeightAboveTerrain = aHeightAboveTerrain;
    fPositionModel = aPositionModel;
  }

  @Override
  public void startInteraction(ILspView aView) {
    super.startInteraction(aView);
    // Add an animation so that we can continually check if a key was pressed.
    TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) aView.getViewXYZWorldTransformation();
    MoveAnimation moveAnimation = new MoveAnimation(v2w, fPositionModel, fHeightProvider, fHeightAboveTerrain);
    ALcdAnimationManager.getInstance().putAnimation(this, moveAnimation);
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    super.terminateInteraction(aView);
    ALcdAnimationManager.getInstance().removeAnimation(this);
  }


  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    return null;
  }

  private static class MoveAnimation extends ALcdAnimation {

    private final TLspViewXYZWorldTransformation3D fV2w;
    private final KeyPressedChecker fKeyPressedChecker;
    private ILcdHeightProvider fHeightProvider;
    private double fHeightAboveTerrain;
    private ILcdModel fPositionModel;

    private MoveAnimation(TLspViewXYZWorldTransformation3D aV2w, ILcdModel aPositionModel, ILcdHeightProvider aHeightProvider, double aHeightAboveTerrain) {
      fV2w = aV2w;
      fKeyPressedChecker = new KeyPressedChecker();
      fHeightProvider = aHeightProvider;
      fHeightAboveTerrain = aHeightAboveTerrain;
      fPositionModel = aPositionModel;
    }

    @Override
    protected void setTimeImpl(double aTime) {

      ILcdPoint eye = fV2w.getEyePoint();
      ILcdPoint reference = fV2w.getReferencePoint();

      // determine the direction vector the user is currently looking at.
      final TLcdXYZPoint newEyePoint = new TLcdXYZPoint(eye);
      Vector3d moveVector = new Vector3d((reference.getX() - eye.getX()),
                                         (reference.getY() - eye.getY()),
                                         (reference.getZ() - eye.getZ()));
      moveVector.normalize();

      int factor = 5;

      double distance = fV2w.getDistance();
      double yaw = fV2w.getYaw();
      double pitch = fV2w.getPitch();
      double roll = fV2w.getRoll();

      boolean apply = false;
      if (fKeyPressedChecker.isUpPressed()) {
        // move the eye point via the direction vector.
        newEyePoint.translate3D(moveVector.x * factor, moveVector.y * factor, moveVector.z * factor);
        apply = true;
      } else if (fKeyPressedChecker.isDownPressed()) {
        newEyePoint.translate3D(-moveVector.x * factor, -moveVector.y * factor, -moveVector.z * factor);
        apply = true;
      } else if (fKeyPressedChecker.isLeftPressed()) {
        yaw = yaw - 2;
        apply = true;
      } else if (fKeyPressedChecker.isRightPressed()) {
        yaw = yaw + 2;
        apply = true;
      }

      if (apply) {
        elevateEyePoint(fV2w.getView().getXYZWorldReference(), newEyePoint, fHeightAboveTerrain);
        fV2w.lookFrom(newEyePoint, distance, yaw, pitch, roll);
        ILcd3DEditablePoint position = (ILcd3DEditablePoint) fPositionModel.elements().nextElement();
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(fPositionModel)) {
          TLcdEllipsoid.DEFAULT.geoc2llhSFCT(newEyePoint, position);
        }
        fPositionModel.elementChanged(position, ILcdModel.FIRE_NOW);
      }

    }

    private void elevateEyePoint(ILcdXYZWorldReference aXYZWorldReference, ILcd3DEditablePoint aLookFrom,
                                 double aAltitude) {
      if (aXYZWorldReference instanceof ILcdGeocentricReference) {
        // place the eye point at the correct height.
        ILcdEllipsoid ellipsoid = ((ILcdGeocentricReference) aXYZWorldReference).getGeodeticDatum().getEllipsoid();
        TLcdLonLatHeightPoint llhPoint = new TLcdLonLatHeightPoint();
        ellipsoid.geoc2llhSFCT(aLookFrom, llhPoint);
        double terrainHeight = fHeightProvider.retrieveHeightAt(llhPoint);
        llhPoint.move3D(llhPoint.getX(), llhPoint.getY(), Double.isNaN(terrainHeight) ? aAltitude : (aAltitude + terrainHeight));
        ellipsoid.llh2geocSFCT(llhPoint, aLookFrom);
      } else {
        aLookFrom.move3D(aLookFrom.getX(), aLookFrom.getY(), aAltitude);
      }
    }

    @Override
    public boolean isLoop() {
      return true;
    }
  }
}
