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
package samples.lightspeed.demo.application.data.uav;

import java.nio.IntBuffer;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcd3DOrientationSettable;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

public class UAVIconModelFactory extends AbstractModelFactory {

  private OrientedPoint fPoint = new OrientedPoint();

  public UAVIconModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) {
    GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance().getSharedValue("videoStream");

    final TLcdVectorModel result = new TLcdVectorModel(
        new TLcdGeocentricReference(),
        new TLcdModelDescriptor(aSource, "UAVIcon", "UAVIcon")
    );

    videoStream.addVideoStreamListener(new GStreamerVideoStream.VideoStreamListener() {
      @Override
      public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
        updatePoint(aCamera);
        result.elementChanged(fPoint, ILcdModel.FIRE_NOW);
      }
    });

    updatePoint(videoStream.getCurrentCamera());

    result.addElement(fPoint, ILcdModel.NO_EVENT);
    return result;
  }

  private void updatePoint(VirtualCamera aCamera) {
    VirtualCamera camera = aCamera/*vs.getCurrentCamera()*/;
    if (camera != null) {
      fPoint.move3D(camera.getEye());
      fPoint.setOrientation(camera.getYaw());
    }
  }

  private static class OrientedPoint extends TLcdXYZPoint implements ILcd3DOrientationSettable {
    private double fPitch, fOrientation;

    public OrientedPoint() {
      super();
    }

    public double getPitch() {
      return fPitch;
    }

    public double getRoll() {
      return 0;
    }

    public double getOrientation() {
      return fOrientation;
    }

    public void setPitch(double v) {
      fPitch = v;
    }

    public void setRoll(double v) {
    }

    public void setOrientation(double v) {
      fOrientation = v;
    }
  }
}
