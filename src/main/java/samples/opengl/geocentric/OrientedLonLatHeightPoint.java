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
package samples.opengl.geocentric;

import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcd3DOrientationSettable;

/**
 * An extension of TLcdLonLatHeightPoint that also includes a 3D orientation.
 */
class OrientedLonLatHeightPoint extends TLcdLonLatHeightPoint implements ILcd3DOrientationSettable {

  private double fYaw = 0;
  private double fPitch = 0;
  private double fRoll = 0;

  public OrientedLonLatHeightPoint( double aX, double aY, double aZ, double aYaw, double aPitch, double aRoll ) {
    super( aX, aY, aZ );
    fYaw = aYaw;
    fPitch = aPitch;
    fRoll = aRoll;
  }

  public double getPitch() {
    return fPitch;
  }

  public double getRoll() {
    return fRoll;
  }

  public double getOrientation() {
    return fYaw;
  }

  public void setPitch( double aPitch ) {
    fPitch = aPitch;
  }

  public void setRoll( double aRoll ) {
    fRoll = aRoll;
  }

  public void setOrientation( double aYaw ) {
    fYaw = aYaw;
  }
}
