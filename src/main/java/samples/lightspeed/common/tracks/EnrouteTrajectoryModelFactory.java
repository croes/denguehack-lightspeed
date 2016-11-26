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
package samples.lightspeed.common.tracks;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.Enumeration;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;

/**
 * Derives an enhanced trajectories model from a given trajectories model.
 */
public class EnrouteTrajectoryModelFactory {

  /**
   * Derives an enhanced trajectories model from a given trajectories model
   * @param aModel the model from which the enhanced model should be derived
   * @return an enhanced trajectories model
   */
  public static ILcdModel deriveTrajectoriesModel(ILcdModel aModel) {
    try (Lock autoUnlock = writeLock(aModel)) {
      TLcd2DBoundsIndexedModel bim = new TLcd2DBoundsIndexedModel();
      bim.setModelReference(aModel.getModelReference());
      bim.setModelDescriptor(new TrajectoriesModelDescriptor(aModel.getModelDescriptor().getSourceName()));

      Enumeration<?> airways = aModel.elements();
      while (airways.hasMoreElements()) {
        ILcdShapeList o = (ILcdShapeList) airways.nextElement();
        ILcdShape[] array = new ILcdShape[o.getShapeCount()];
        for (int iseg = 0; iseg < o.getShapeCount(); iseg++) {
          ILcdShape shape = o.getShape(iseg);
          if (shape instanceof ILcdCompositeCurve) {
            //very basic support for lines wrapped in curves (GML)
            shape = ((ILcdCompositeCurve) shape).getCurves().get(0);
          }
          ILcdPointList segment = (ILcdPointList) shape;
          TLcdLonLatHeightPoint[] segPoints = new TLcdLonLatHeightPoint[segment.getPointCount()];
          for (int p = 0; p < segPoints.length; p++) {
            ILcdPoint segP = segment.getPoint(p);
            segPoints[p] = new TLcdLonLatHeightPoint(
                segP.getX(),
                segP.getY(),
                EnrouteAirwayTrack.CRUISE_ALT
            );
          }

          array[iseg] = new TLcdLonLatHeightPolyline(
              new TLcd3DEditablePointList(segPoints, false),
              TLcdEllipsoid.DEFAULT
          );
        }
        TLcdShapeList segments = new TLcdShapeList(array);
        bim.addElement(segments, ILcdModel.NO_EVENT);
      }
      return bim;
    }
  }

  public static class TrajectoriesModelDescriptor extends TLcdModelDescriptor {

    public TrajectoriesModelDescriptor(String aSourceName) {
      super(aSourceName, "EnhancedTrajectory", "Trajectories");
    }
  }
}
