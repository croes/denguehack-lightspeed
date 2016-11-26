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
package samples.decoder.asdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import com.luciad.format.asdi.TLcdASDITrack;
import com.luciad.format.asdi.TLcdASDITrackTODataTypes;
import com.luciad.format.asdi.TLcdASDITrackTZDataTypes;
import com.luciad.format.asdi.TLcdASDITrajectory;
import com.luciad.format.asdi.TLcdASDITrajectoryModelDescriptor;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.realtime.ALcdTimeIndexedSimulatorModel;
import com.luciad.realtime.TLcdTrackModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * Creates an <CODE>TrackSimulatorModel</CODE> that starts from ASDI trajectories and creates
 * time-dependent track representations for those.
 */
public class TrackSimulatorModel extends ALcdTimeIndexedSimulatorModel {
  private static boolean INTERPOLATE = false;
  private ILcdModel fTrajectoryModel;

  /**
   * Constructs a new empty <code>TrackSimulatorModel</code>. Use <code>setTrajectoryModel</code> before
   * using this instance.
   */
  public TrackSimulatorModel() {
  }

  public void setTrajectoryModel(ILcdModel aTrajectoryModel) {
    if (!(aTrajectoryModel.getModelDescriptor() instanceof TLcdASDITrajectoryModelDescriptor)) {
      throw new IllegalArgumentException("Given model [" + aTrajectoryModel + "] not a trajectory model");
    }
    fTrajectoryModel = aTrajectoryModel;
    ILcdModel track_model = createTrackModel(aTrajectoryModel);
    Collection tracks = createTracks(aTrajectoryModel);
    if (tracks.size() == 0) {
      throw new IllegalArgumentException(
          "Trajectory model  [" + aTrajectoryModel + "] contains no tracks for simulation.");
    }

    init(track_model, tracks);
  }

  public ILcdModel getTrajectoryModel() {
    return fTrajectoryModel;
  }

  private Collection createTracks(ILcdModel aTrajectoryModel) {
    //create a track for every trajectory.
    List tracks = new ArrayList();
    for (Enumeration elements = aTrajectoryModel.elements(); elements.hasMoreElements(); ) {
      TLcdASDITrajectory trajectory = (TLcdASDITrajectory) elements.nextElement();
      if (trajectory.getPointCount() > 0) {
        tracks.add(createTrack(trajectory));
      }
    }
    return tracks;
  }

  protected TLcdASDITrack createTrack(TLcdASDITrajectory aTrajectory) {
    return new TLcdASDITrack(aTrajectory);
  }

  private ILcdModel createTrackModel(ILcdModel aModel) {
    TLcdASDITrajectoryModelDescriptor descriptor = (TLcdASDITrajectoryModelDescriptor) aModel.getModelDescriptor();

    //create a model that will hold the tracks.
    ILcdBounds bounds = new TLcdLonLatBounds(-180, -90, 360, 180);
    if (aModel instanceof ILcdBounded) { //This is normally the case
      bounds = ((ILcdBounded) aModel).getBounds();
    }
    TLcdTrackModel track_model = new TLcdTrackModel(bounds);
    track_model.setModelReference(aModel.getModelReference());

    //derive a model descriptor for the tracks from the model descriptor of the trajectories.
    ILcdModelDescriptor model_descriptor = createModelDescriptor(aModel, descriptor);
    track_model.setModelDescriptor(model_descriptor);

    return track_model;
  }

  protected SimulationModelDescriptor createModelDescriptor(
      ILcdModel aTrajectoryModel,
      TLcdASDITrajectoryModelDescriptor aTrajectoryModelDescriptor) {
    boolean isTOModel = TrackSelectionMediator.isTOModel(aTrajectoryModel);
    return new TrackSimulationModelDescriptor(
        aTrajectoryModelDescriptor.getSourceName(),
        "ASDI Tracks",
        "Tracks " + (isTOModel ? "TO" : "TZ"),
        aTrajectoryModelDescriptor,
        this,
        isTOModel ? TLcdASDITrackTODataTypes.TrackTOType : TLcdASDITrackTZDataTypes.TrackTZType);
  }

  protected long getBeginTime(Object aTrack) {
    return ((TLcdASDITrack) aTrack).getTrajectory().getBeginTime();
  }

  protected long getEndTime(Object aTrack) {
    return ((TLcdASDITrack) aTrack).getTrajectory().getEndTime();
  }

  /**
   * Update the given track to the given time. This will lookup the corresponding point index in the
   * trajectory associated with the track and update the track to represent the trajectory at that
   * point.
   *
   * @param aTrackModel The model of the given track.
   * @param aTrackSFCT The track to update.
   * @param aDate      The date/time that will be used to lookup the location of the tracks.
   */
  protected boolean updateTrackForDateSFCT(ILcdModel aTrackModel, Object aTrackSFCT, Date aDate) {
    long time = aDate.getTime();
    TLcdASDITrack track = (TLcdASDITrack) aTrackSFCT;
    TLcdASDITrajectory trajectory = track.getTrajectory();

    // Interpolate for a smoother flight.
    // Avoid interpolion if the index is -1, because that means the time
    // value is outside the timerange of the trajectory.
    // Also avoid interpolating if there is no next point, or if there is no
    // time delta between the current and the next point.
    if (INTERPOLATE) {
      //lookup the point index for the given time
      int index = trajectory.getIndexForTimeStamp(time);

      if (index != -1 &&
          index < (trajectory.getPointCount() - 1) &&
          (trajectory.getTime(index + 1) - trajectory.getTime(index)) != 0) {

        float ratio = (float) (time - trajectory.getTime(index))
                      /
                      (float) (trajectory.getTime(index + 1) - trajectory.getTime(index));

        ILcdPoint first_point = trajectory.getPoint(index);
        ILcdPoint next_point = trajectory.getPoint(index + 1);

        // True geodesic interpolation, needed at edges of map
        TLcdEllipsoid ellipsoid = new TLcdEllipsoid();
        ellipsoid.geodesicPointSFCT(first_point, next_point, ratio, track);

        //Update the state of the track, see TLcdASDITrack.
        track.setTrajectoryPointIndex(index);

        return true;
      } else {
        track.updateForIndex(index);
        return true;
      }
    } else {
      //Update the track using the default mechanism

      //Verify if the current index is still valid or not
      int current = track.getTrajectoryPointIndex();
      if (current == -1 ||
          current >= (trajectory.getPointCount() - 1) ||
          time < trajectory.getTime(current) ||
          time >= trajectory.getTime(current + 1)) {
        track.updateForIndex(trajectory.getIndexForTimeStamp(time));
        return true;
      }
      // else: The current index is still valid.
    }
    return false;
  }
}
