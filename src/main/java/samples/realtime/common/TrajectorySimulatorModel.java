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
package samples.realtime.common;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.Date;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.realtime.ALcdSimulatorModel;
import com.luciad.realtime.TLcdTrackModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * A simulator model used to simulate moving tracks along parabolic trajectories between cities
 */
public class TrajectorySimulatorModel extends ALcdSimulatorModel {

  private static final ILcdBounds TOTAL_DATA_BOUNDS = new TLcdLonLatBounds(-180, -90, 360, 180);
  private final ILcdModel fCitiesModel;

  private final ILcdModel fTrajectoryLinesModel;
  private final int fNumberOfTrajectories;

  /**
   * Creates a new trajectory simulator model with the given model as the model describing the cities between
   * which the tracks should lie.
   * @param aCitiesModel the model describing the cities between which the tracks should lie
   * @param aNumberOfTrajectories the number of trajectories this simulator model should describe
   */
  public TrajectorySimulatorModel(ILcdModel aCitiesModel, int aNumberOfTrajectories) {

    fCitiesModel = aCitiesModel;
    fNumberOfTrajectories = aNumberOfTrajectories;
    //We create a new model in which to store the lines that outline the trajectories between the cities
    fTrajectoryLinesModel = new TLcdVectorModel(new TLcdGeodeticReference(new TLcdGeodeticDatum()),
                                                new TLcdModelDescriptor("", "TrajectoryLines", "Trajectory lines"));
    Calendar calendar = Calendar.getInstance();

    //We set the simulation so that it lasts for 6 hours
    calendar.set(2012, Calendar.JANUARY, 1, 10, 0, 0);
    Date beginDate = calendar.getTime();
    setBeginDate(beginDate);
    calendar.set(2012, Calendar.JANUARY, 1, 16, 0, 0);
    Date endDate = calendar.getTime();
    setEndDate(endDate);

    //We create a single track model and set this simulator to use that model
    ILcdModel[] trackModel = new ILcdModel[]{createEmptyTrackModel()};
    setTrackModels(trackModel);

    // Generate the trajectories
    TrajectoryGenerator.generateTrajectories(
        beginDate, endDate, fCitiesModel, fNumberOfTrajectories, fTrajectoryLinesModel
                                            );
    // Generate the tracks
    TrajectoryGenerator.generateTracks(fTrajectoryLinesModel, trackModel[0]);
  }

  public TrajectorySimulatorModel(ILcdModel aCitiesModel, int aNumberOfTrajectories, Date aBeginDate, Date aEndDate) {

    fCitiesModel = aCitiesModel;
    fNumberOfTrajectories = aNumberOfTrajectories;
    //We create a new model in which to store the lines that outline the trajectories between the cities
    fTrajectoryLinesModel = new TLcdVectorModel(new TLcdGeodeticReference(new TLcdGeodeticDatum()),
                                                new TLcdModelDescriptor("", "TrajectoryLines", "Trajectory lines"));

    setBeginDate(aBeginDate);
    setEndDate(aEndDate);

    ILcdModel[] trackModel = new ILcdModel[]{createEmptyTrackModel()};
    setTrackModels(trackModel);
    TrajectoryGenerator.generateTrajectories(
        aBeginDate, aEndDate, fCitiesModel, fNumberOfTrajectories, fTrajectoryLinesModel);

    TrajectoryGenerator.generateTracks(fTrajectoryLinesModel, trackModel[0]);

    setDate(getBeginDate());
  }

  /**
   * Returns the model containing the lines outlining the parabolic trajectories of this trajectory
   * simulator model
   * @return the model containing the lines outlining the parabolic trajectories of this trajectory simulator model.
   */
  public ILcdModel getTrajectoryLinesModel() {
    return fTrajectoryLinesModel;
  }

  private TLcdTrackModel createEmptyTrackModel() {
    TLcdTrackModel trackModel = new TLcdTrackModel(TOTAL_DATA_BOUNDS);
    trackModel.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    trackModel.setModelDescriptor(new TimeStampedTrackModelDescriptor("", "Tracks", "Airplane tracks"));
    return trackModel;
  }

  @Override
  protected void updateTrackModels(Date aOldDate, Date aNewDate) {
    if (aNewDate.equals(aOldDate)) {
      return;
    }

    ILcdModel[] trackModels = getTrackModels();
    for (int model_index = 0; model_index < trackModels.length; model_index++) {
      TLcdTrackModel model = (TLcdTrackModel) trackModels[model_index];
      // Prepare the changes
      try (Lock autoUnlock = TLcdLockUtil.readLock(model)) {
        int model_size = model.size();
        for (int track_index = 0; track_index < model_size; track_index++) {
          ((TimeStampedTrack) model.elementAt(track_index)).prepareTimeStamp(aNewDate.getTime());
        }
      }
      // Apply the changes
      try (Lock autoUnlock = writeLock(model)) {
        int model_size = model.size();
        for (int track_index = 0; track_index < model_size; track_index++) {
          ((TimeStampedTrack) model.elementAt(track_index)).applyChanges();
        }
      }

      // notify listeners to the model that all elements in the model have changed.
      model.allElementsChanged(ILcdFireEventMode.FIRE_NOW);
      firePropertyChangeEvent(new PropertyChangeEvent(this, "Tracks", this, null));
    }
  }
}
