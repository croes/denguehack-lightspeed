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
package samples.decoder.asterix;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.luciad.format.asterix.TLcdASTERIXDataTypes;
import com.luciad.format.asterix.TLcdASTERIXFilteredModel;
import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.format.asterix.TLcdASTERIXTrajectory;
import com.luciad.format.asterix.TLcdASTERIXTrajectoryModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXWeatherModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXWeatherPicture;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelList;
import com.luciad.realtime.ALcdTimeIndexedSimulatorModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulatorModelList;
import com.luciad.realtime.TLcdTrackModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdTimeBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Creates time-dependent representations of ASTERIX data that can be simulated over time.
 */
public class ASTERIXTrackSimulatorModelFactory {

  private final static ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ASTERIXTrackSimulatorModelFactory.class.getName());

  private final static boolean INTERPOLATE = true;

  /**
   * Creates a simulator model for the given ASTERIX layer.
   * @param aTrajectoryModel the trajectory model to simulate
   * @return the simulator model
   */
  public ILcdSimulatorModel createSimulatorModel(ILcdModel aTrajectoryModel) {
    TLcdSimulatorModelList result = new TLcdSimulatorModelList();
    addSimulatorModels(result, aTrajectoryModel);
    return result;
  }

  private void addSimulatorModels(TLcdSimulatorModelList aResult, ILcdModel aModel) {
    if (aModel instanceof TLcdModelList) {
      TLcdModelList modelList = (TLcdModelList) aModel;
      for (int i = 0; i < modelList.getModelCount(); i++) {
        ILcdModel model = modelList.getModel(i);
        addSimulatorModels(aResult, model);
      }
    } else {
      ALcdTimeIndexedSimulatorModel simulatorModel = createSimulatorModelForSingleModel(aModel);
      if (simulatorModel != null) {
        if (simulatorModel.getTrackModels() != null) {
          aResult.addSimulatorModel(simulatorModel);
        } else {
          LOGGER.warn("Track model [" + aModel.getModelDescriptor().getDisplayName() + "] contains no tracks for simulation. Simulation not created.");
        }
      } else {
        LOGGER.warn("Model [" + aModel.getModelDescriptor().getDisplayName() + "] cannot be simulated.");
      }
    }
  }

  protected ALcdTimeIndexedSimulatorModel createSimulatorModelForSingleModel(ILcdModel aModel) {
    if (TimeIndexedTrackSimulatorModel.isSupported(aModel)) {
      return new TimeIndexedTrackSimulatorModel(aModel);
    } else if (TimeIndexedWeatherSimulatorModel.isSupported(aModel)) {
      return new TimeIndexedWeatherSimulatorModel(aModel);
    }
    return null;
  }

  /**
   * Simulator model for trajectory data.
   */
  public static class TimeIndexedTrackSimulatorModel extends ALcdTimeIndexedSimulatorModel {

    private IdentityHashMap<TLcdASTERIXTrajectory, TLcdASTERIXTrack> fTrackMap =
        new IdentityHashMap<TLcdASTERIXTrajectory, TLcdASTERIXTrack>();

    private ILcdModel fStaticModel;

    public static boolean isSupported(ILcdModel aModel) {
      return (aModel.getModelDescriptor() instanceof TLcdASTERIXTrajectoryModelDescriptor) &&
             aModel.elements().hasMoreElements();
    }

    public TimeIndexedTrackSimulatorModel(ILcdModel aTrajectoryModel) {
      fStaticModel = aTrajectoryModel;
      init(createTrackModel(aTrajectoryModel), createTracks(aTrajectoryModel));
    }

    private ILcdModel createTrackModel(ILcdModel aTrajectoryModel) {
      ILcdBounds bounds = (aTrajectoryModel instanceof ILcdBounded) ? ((ILcdBounded) aTrajectoryModel).getBounds() : new TLcdLonLatBounds(-180, -90, 360, 180);
      TLcdTrackModel trackModel = new TLcdTrackModel(bounds) {
        /*
         * We extend TLcdTrackModel to stop being able to remove tracks.
         */
        @Override
        public boolean canRemoveElement(Object aObject) {
          return false;
        }
      };
      trackModel.setModelReference(aTrajectoryModel.getModelReference());
      trackModel.setModelDescriptor(createSimulatorModelDescriptor(aTrajectoryModel.getModelDescriptor()));
      aTrajectoryModel.addModelListener(new TrackModelUpdater(this, this, trackModel));
      return trackModel;
    }

    protected TLcdASTERIXTrack createTrack(TLcdASTERIXTrajectory aTrajectory) {
      return new TLcdASTERIXTrack(aTrajectory);
    }

    private Collection<TLcdASTERIXTrack> createTracks(ILcdModel aTrajectoryModel) {
      fTrackMap.clear();
      List<TLcdASTERIXTrack> tracks = new ArrayList<TLcdASTERIXTrack>();
      for (Enumeration elements = aTrajectoryModel.elements(); elements.hasMoreElements(); ) {
        TLcdASTERIXTrajectory trajectory = (TLcdASTERIXTrajectory) elements.nextElement();
        if (trajectory.getPointCount() > 0) {
          TLcdASTERIXTrack track = createTrack(trajectory);
          tracks.add(track);
          fTrackMap.put(trajectory, track);
        }
      }
      return tracks;
    }

    protected ILcdModelDescriptor createSimulatorModelDescriptor(ILcdModelDescriptor aModelDescriptor) {
      TLcdASTERIXTrajectoryModelDescriptor modelDescriptor = (TLcdASTERIXTrajectoryModelDescriptor) aModelDescriptor;
      return new TrackSimulationModelDescriptor(
          modelDescriptor.getSourceName(),
          "ASTERIX Tracks",
          "Tracks " + modelDescriptor.getDisplayName(),
          modelDescriptor.getTrackDataType());
    }

    public final ILcdModel getStaticModel() {
      return fStaticModel;
    }

    @Override
    protected long getBeginTime(Object aTrack) {
      return ((TLcdASTERIXTrack) aTrack).getTrajectory().getBeginTime();
    }

    @Override
    protected long getEndTime(Object aTrack) {
      return ((TLcdASTERIXTrack) aTrack).getTrajectory().getEndTime();
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
    @Override
    protected boolean updateTrackForDateSFCT(ILcdModel aTrackModel, Object aTrackSFCT, Date aDate) {
      long time = aDate.getTime();
      TLcdASTERIXTrack track = (TLcdASTERIXTrack) aTrackSFCT;
      TLcdASTERIXTrajectory trajectory = track.getTrajectory();

      //lookup the point index for the given time
      int index = trajectory.getIndexForTimeStamp(time);

      // Interpolate for a smoother flight.
      // Avoid interpolation if the index is -1, because that means the time
      // value is outside the timerange of the trajectory.
      // Also avoid interpolating if there is no next point, or if there is no
      // time delta between the current and the next point.
      if (INTERPOLATE &&
          index != -1 &&
          index < (trajectory.getPointCount() - 1) &&
          (trajectory.getTime(index + 1) - trajectory.getTime(index) != 0)) {

        float ratio = (float) (time - trajectory.getTime(index))
                      /
                      (float) (trajectory.getTime(index + 1) - trajectory.getTime(index));

        ILcdPoint firstPoint = trajectory.getPoint(index);
        ILcdPoint nextPoint = trajectory.getPoint(index + 1);

        // True geodesic interpolation, needed at edges of map
        // ASTERIX always uses WGS84, so we can use the default ellipsoid which corresponds to WGS84
        TLcdEllipsoid ellipsoid = TLcdEllipsoid.DEFAULT;
        ellipsoid.geodesicPointSFCT(firstPoint, nextPoint, ratio, track);

        // Interpolate altitude
        double altitude = (1 - ratio) * firstPoint.getZ() + ratio * nextPoint.getZ();
        track.move3D(track.getX(), track.getY(), altitude);

        //Update the state of the track, see TLcdASTERIXTrack.
        track.setTrajectoryPointIndex(index);

        return true;
      } else {
        //Update the track using the default mechanism
        track.updateForIndex(trajectory.getIndexForTimeStamp(time));
        return true;
      }
    }

    private static class TrackModelUpdater implements ILcdModelListener {

      private final WeakReference<TimeIndexedTrackSimulatorModel> fTrackSimulatorModel;
      private final WeakReference<TimeIndexedTrackSimulatorModel> fSimulatorModel;
      private final WeakReference<TLcdTrackModel> fTrackModel;

      public TrackModelUpdater(TimeIndexedTrackSimulatorModel aTrackSimulatorModel,
                               TimeIndexedTrackSimulatorModel aSimulatorModel,
                               TLcdTrackModel aTrackModel) {
        fTrackSimulatorModel = new WeakReference<TimeIndexedTrackSimulatorModel>(aTrackSimulatorModel);
        fSimulatorModel = new WeakReference<TimeIndexedTrackSimulatorModel>(aSimulatorModel);
        fTrackModel = new WeakReference<TLcdTrackModel>(aTrackModel);
      }

      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        TimeIndexedTrackSimulatorModel trackSimulatorModel = fTrackSimulatorModel.get();
        TimeIndexedTrackSimulatorModel simulatorModel = fSimulatorModel.get();
        TLcdTrackModel trackModel = fTrackModel.get();
        if (trackSimulatorModel == null || simulatorModel == null || trackModel == null) {
          aEvent.getModel().removeModelListener(this);
        } else {
          updateTrackModel(trackSimulatorModel, simulatorModel, trackModel, aEvent);
        }
      }

      private void updateTrackModel(TimeIndexedTrackSimulatorModel aTrackSimulatorModel,
                                    TimeIndexedTrackSimulatorModel aSimulatorModel,
                                    TLcdTrackModel aTrackModel,
                                    TLcdModelChangedEvent aEvent) {
        if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_REMOVED) != 0) {
          aSimulatorModel.fTrackMap.clear();
          aTrackModel.removeAllElements(ILcdModel.FIRE_NOW);
        } else {
          if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_CHANGED) != 0) {
            aTrackModel.allElementsChanged(ILcdModel.FIRE_LATER);
          }

          //ignore added objects.
          Set<Object> addedTracks = new HashSet<Object>();
          Set<Object> removedTracks = new HashSet<Object>();
          Enumeration elements = aEvent.elements();
          while (elements.hasMoreElements()) {
            Object element = elements.nextElement();
            if (element instanceof TLcdASTERIXTrajectory) {
              TLcdASTERIXTrajectory trajectory = (TLcdASTERIXTrajectory) element;
              if (aEvent.retrieveChange(trajectory) == TLcdModelChangedEvent.OBJECT_ADDED) {
                TLcdASTERIXTrack track = aTrackSimulatorModel.createTrack(trajectory);
                TLcdASTERIXTrack existingTrack = aSimulatorModel.fTrackMap.put(trajectory, track);
                if (existingTrack != null) {
                  throw new RuntimeException("Error, it should not have an existing track for a new added trajectory");
                }
                addedTracks.add(track);
              } else if (aEvent.retrieveChange(trajectory) == TLcdModelChangedEvent.OBJECT_REMOVED) {
                TLcdASTERIXTrack track = aSimulatorModel.fTrackMap.remove(trajectory);
                if (track != null) {
                  removedTracks.add(track);
                }
              } else if (aEvent.retrieveChange(trajectory) == TLcdModelChangedEvent.OBJECT_CHANGED) {
                if (isActive(trajectory, aSimulatorModel)) {
                  //Only mark track as changed if it is active (track model contains track).
                  TLcdASTERIXTrack track = aSimulatorModel.fTrackMap.get(trajectory);
                  if (track != null) {
                    aTrackModel.elementChanged(track, ILcdModel.FIRE_LATER);
                  }
                }
              }
            }
          }
          if (!addedTracks.isEmpty()) {
            //add tracks to the simulator model
            aSimulatorModel.addTracks(addedTracks, ILcdModel.FIRE_LATER);
          }
          if (!removedTracks.isEmpty()) {
            //remove tracks the simulator model
            aSimulatorModel.removeTracks(removedTracks, ILcdModel.FIRE_LATER);
          }
          aTrackModel.fireCollectedModelChanges();
        }
      }

      private boolean isActive(TLcdASTERIXTrajectory aTrajectory, TimeIndexedTrackSimulatorModel aSimulatorModel) {
        return aSimulatorModel.getDate().getTime() >= aTrajectory.getBeginTime() &&
               aSimulatorModel.getDate().getTime() <= aTrajectory.getEndTime();
      }
    }
  }

  /**
   * Simulator model for weather data.
   */
  public static class TimeIndexedWeatherSimulatorModel extends ALcdTimeIndexedSimulatorModel {

    private TLcdASTERIXFilteredModel fStaticModel;

    public static boolean isSupported(ILcdModel aModel) {
      if (aModel.getModelDescriptor() instanceof TLcdASTERIXWeatherModelDescriptor &&
          aModel instanceof TLcdASTERIXFilteredModel &&
          ((TLcdASTERIXFilteredModel) aModel).getUnfilteredModel().elements().hasMoreElements()) {

        Enumeration elements = ((TLcdASTERIXFilteredModel) aModel).getUnfilteredModel().elements();
        while (elements.hasMoreElements()) {
          TLcdASTERIXWeatherPicture picture = (TLcdASTERIXWeatherPicture) elements.nextElement();
          if (picture.getTimeBounds().getBeginTimeBoundedness() != ILcdTimeBounds.Boundedness.BOUNDED &&
              picture.getTimeBounds().getEndTimeBoundedness() != ILcdTimeBounds.Boundedness.BOUNDED) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    public TimeIndexedWeatherSimulatorModel(ILcdModel aWeatherModel) {
      fStaticModel = (TLcdASTERIXFilteredModel) aWeatherModel;
      /**
       * Instead of creating a new layer for the simulator model, we will filter the static model.
       * Even if it will not be used by us, ALcdTimeIndexedSimulatorModel requires a track model.
       */
      init(createTrackModel(aWeatherModel), createTracks(fStaticModel.getUnfilteredModel()));
    }

    private ILcdModel createTrackModel(ILcdModel aWeatherModel) {
      ILcdBounds bounds = (aWeatherModel instanceof ILcdBounded) ? ((ILcdBounded) aWeatherModel).getBounds() : new TLcdLonLatBounds(-180, -90, 360, 180);
      TLcdTrackModel trackModel = new TLcdTrackModel(bounds);
      trackModel.setModelReference(aWeatherModel.getModelReference());
      trackModel.setModelDescriptor(createSimulatorModelDescriptor(aWeatherModel.getModelDescriptor()));
      return trackModel;
    }

    protected ILcdModelDescriptor createSimulatorModelDescriptor(ILcdModelDescriptor aModelDescriptor) {
      TLcdASTERIXWeatherModelDescriptor modelDescriptor = (TLcdASTERIXWeatherModelDescriptor) aModelDescriptor;
      return new WeatherSimulationModelDescriptor(modelDescriptor.getSourceName(),
                                                  "ASTERIX Weather Pictures",
                                                  "Pictures " + modelDescriptor.getDisplayName(),
                                                  TLcdASTERIXDataTypes.Category8WeatherPictureType);
    }

    protected Collection<Object> createTracks(ILcdModel aWeatherModel) {
      ArrayList<Object> tracks = new ArrayList<>();
      Enumeration elements = aWeatherModel.elements();
      while (elements.hasMoreElements()) {
        tracks.add(elements.nextElement());
      }
      return tracks;
    }

    @Override
    protected long getBeginTime(Object aTrack) {
      return ((TLcdASTERIXWeatherPicture) aTrack).getTimeBounds().getBeginTime();
    }

    @Override
    protected long getEndTime(Object aTrack) {
      return ((TLcdASTERIXWeatherPicture) aTrack).getTimeBounds().getEndTime();
    }

    @Override
    protected boolean updateTrackForDateSFCT(ILcdModel aTrackModel, Object aTrackSFCT, Date aDate) {
      return false;
    }

    public ILcdModel getStaticModel() {
      return fStaticModel;
    }

    @Override
    public void setDate(Date aDate) {
      super.setDate(aDate);
      try (TLcdLockUtil.Lock autoClosed = TLcdLockUtil.writeLock(fStaticModel)) {
        fStaticModel.setDate(aDate, ILcdModel.FIRE_LATER);
      }
      fStaticModel.fireCollectedModelChanges();
    }

    @Override
    public ILcdModel[] getTrackModels() {
      // We return a empty array to ensure that no layer can be created for the track model.
      return new ILcdModel[0];
    }
  }
}
