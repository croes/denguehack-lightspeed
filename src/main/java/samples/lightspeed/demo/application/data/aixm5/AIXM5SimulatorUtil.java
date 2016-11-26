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
package samples.lightspeed.demo.application.data.aixm5;

import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51Feature;
import com.luciad.format.aixm51.model.features.procedure.departureprocedure.TLcdAIXM51StandardInstrumentDepartureTimeSlice;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.enumeration.ILcdMorphingFunction;

/**
 * Code that is specific to the realtime component, moved in a separate class to avoid a
 * NoClassDefFoundError.
 */
public class AIXM5SimulatorUtil {

  public static Object createSimulatorModel(ILcdModel aProcedureModel) {
    return new TrajectoryTrackSimulatorModel(aProcedureModel, new ILcdMorphingFunction<Object, ILcd3DEditablePoint[]>() {
      @Override
      public ILcd3DEditablePoint[] morph(Object aObject) {
        //convert the AIXM5.1 procedure to an ILcd3DEditablePoint[]
        if (aObject instanceof TLcdAIXM51Feature) {
          TLcdAIXM51Feature<TLcdAIXM51StandardInstrumentDepartureTimeSlice> feature = (TLcdAIXM51Feature<TLcdAIXM51StandardInstrumentDepartureTimeSlice>) aObject;
          TLcdAIXM51StandardInstrumentDepartureTimeSlice timeSlice = feature.getTimeSlice().get(0);
          ILcdPolyline trajectory = (ILcdPolyline) timeSlice.getFlightTransition().get(0).getTrajectory().getSegments().get(0);
          ILcd3DEditablePoint[] result = new ILcd3DEditablePoint[trajectory.getPointCount()];
          for (int i = 0; i < result.length; i++) {
            result[i] = trajectory.getPoint(i).cloneAs3DEditablePoint();

          }
          return result;
        }
        return null;
      }
    });
  }

  public static ILcdModel getTrackModel(Object aSimulatorModel) {
    return ((ILcdSimulatorModel) aSimulatorModel).getTrackModels()[0];
  }
}
