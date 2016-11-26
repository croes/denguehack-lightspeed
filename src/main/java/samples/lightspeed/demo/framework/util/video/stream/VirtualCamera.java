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
package samples.lightspeed.demo.framework.util.video.stream;

import java.nio.FloatBuffer;

import com.luciad.shape.ILcdPoint;

/**
 * Virtual UAV camera that can be painted as a frustum.
 */
public class VirtualCamera {
  private final FloatBuffer fTextureMatrix;
  private final ILcdPoint fEye;
  private final ILcdPoint fRef;
  private final ILcdPoint fUp;
  private final double fNear, fFar;
  private final double fFov;
  private final double fAspectRatio;
  private final double fYaw, fPitch, fRoll;

  public VirtualCamera(FloatBuffer aTextureMatrix, ILcdPoint aEye, ILcdPoint aRef, ILcdPoint aUp, double aNear, double aFar, double aFov, double aAspectRatio, double aYaw, double aPitch, double aRoll) {
    fTextureMatrix = aTextureMatrix;
    fEye = aEye;
    fRef = aRef;
    fUp = aUp;
    fNear = aNear;
    fFar = aFar;
    fFov = aFov;
    fAspectRatio = aAspectRatio;
    fYaw = aYaw;
    fPitch = aPitch;
    fRoll = aRoll;
  }

  public FloatBuffer getTextureMatrix() {
    return fTextureMatrix;
  }

  public ILcdPoint getEye() {
    return fEye;
  }

  public ILcdPoint getRef() {
    return fRef;
  }

  public ILcdPoint getUp() {
    return fUp;
  }

  public double getNear() {
    return fNear;
  }

  public double getFar() {
    return fFar;
  }

  public double getFov() {
    return fFov;
  }

  public double getAspectRatio() {
    return fAspectRatio;
  }

  public double getYaw() {
    return fYaw;
  }

  public double getPitch() {
    return fPitch;
  }

  public double getRoll() {
    return fRoll;
  }
}
