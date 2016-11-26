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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.enumeration.ILcdMorphingFunction;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * {@code ILcdSimulatorModel} based on a model containing
 * {@code Trajectory} instances
 */
final class TrajectoryTrackSimulatorModel implements ILcdSimulatorModel {
  static final String TRAJECTORY_TRACK_SIMULATOR_MODEL_TYPE = "Trajectory Preview";

  private static final double TIME_FACTOR = Double.parseDouble(Framework.getInstance().getProperty("simulator.timeFactor", "50"));

  /**
   * Time between two subsequent tracks in milliseconds
   */
  private static final int MINIMUM_TIME_BETWEEN_TRACKS = (int) (60000 * TIME_FACTOR);
  private static final int MAXIMUM_TIME_BETWEEN_TRACKS = (int) (120000 * TIME_FACTOR);

  private static final int MINIMUM_DEPARTURES_PER_TRACK = 10;
  private static final int MAXIMUM_DEPARTURES_PER_TRACK = 30;

  private final ILcdModel fTrajectoryModel;
  private final ILcdMorphingFunction<Object, ILcd3DEditablePoint[]> fTrajectoryModelMorphingFunction;
  private final Random fRandom = new Random(0x12345678);

  private List<Trajectory> fTrajectories = new ArrayList<Trajectory>();
  private List<TrajectoryTrack> fTracks = new ArrayList<TrajectoryTrack>();

  private TLcdVectorModel[] fTrackModels;

  private Date fBeginDate = new Date(System.currentTimeMillis());
  private Date fDate = new Date(System.currentTimeMillis());
  private Date fEndDate = new Date(System.currentTimeMillis());
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * @param aTrajectoryModel Model containing the static data
   * @param aTrajectoryModelMorphingFunction Morphing function which will be applied on the elements of {@code aTrajectoryModel}.
   *                                         A {@code Trajectory} and corresponding {@code TrajectoryTrack}s will be created when
   *                                         the function returns an {@code ILcd3DEditablePoint[]}. Function may return {@code null}
   *                                         when a model element should be skipped.
   *
   */
  public TrajectoryTrackSimulatorModel(ILcdModel aTrajectoryModel, ILcdMorphingFunction<Object, ILcd3DEditablePoint[]> aTrajectoryModelMorphingFunction) {
    fTrajectoryModel = aTrajectoryModel;
    fTrajectoryModelMorphingFunction = aTrajectoryModelMorphingFunction;
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdModelDescriptor("dummy source", TRAJECTORY_TRACK_SIMULATOR_MODEL_TYPE, "Trajectory Preview"));
    fTrackModels = new TLcdVectorModel[]{model};
    update();
  }

  @Override
  public ILcdModel[] getTrackModels() {
    return fTrackModels;
  }

  @Override
  public void setDate(Date aDate) {
    fDate = aDate;
    ILcdModel model = fTrackModels[0];

    try (TLcdLockUtil.Lock autoLock = TLcdLockUtil.writeLock(model)) {
      for (int i = 0; i < fTrajectories.size(); i++) {
        Trajectory trajectory = fTrajectories.get(i);
        TrajectoryTrack track = fTracks.get(i);
        model.removeElement(track, ILcdModel.NO_EVENT);
        if (shouldIncludeTrajectory(trajectory)) {
          model.addElement(track, ILcdModel.NO_EVENT);
        }
      }

      Enumeration<TrajectoryTrack> objects = model.elements();
      while (objects.hasMoreElements()) {
        objects.nextElement().updateToTimeStamp(aDate);
      }
    }

    fTrackModels[0].allElementsChanged(ILcdModel.FIRE_NOW);
  }

  @Override
  public Date getDate() {
    if (fDate == null) {
      return new Date();
    }
    return fDate;
  }

  @Override
  public Date getBeginDate() {
    if (fBeginDate == null) {
      return new Date();
    }
    return fBeginDate;
  }

  @Override
  public Date getEndDate() {
    if (fEndDate == null) {
      return new Date();
    }
    return fEndDate;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aListener);
  }

  private void update() {
    TLcdVectorModel trackModel = fTrackModels[0];
    fTrajectories.clear();
    fTracks.clear();

    trackModel.removeAllElements(ILcdFireEventMode.FIRE_LATER);

    int departuresPerTrack = MINIMUM_DEPARTURES_PER_TRACK + (int) (fRandom.nextDouble() * (MAXIMUM_DEPARTURES_PER_TRACK - MINIMUM_DEPARTURES_PER_TRACK));

    Enumeration<Object> trajectories = fTrajectoryModel.elements();
    long currentTime = System.currentTimeMillis();
    while (trajectories.hasMoreElements()) {
      Object trajectoryModelElement = trajectories.nextElement();
      int i = 0;
      long time = currentTime;
      for (int j = 0; j < departuresPerTrack; j++) {
        ILcd3DEditablePoint[] trajectoryData = fTrajectoryModelMorphingFunction.morph(trajectoryModelElement);
        if (trajectoryData != null && trajectoryData.length > 0) {
          float speed = (700f) / 3.6f;
          speed = (float) (speed / TIME_FACTOR);
          int timeBetweenTwoTracks = MINIMUM_TIME_BETWEEN_TRACKS + (int) (fRandom.nextDouble() * (MAXIMUM_TIME_BETWEEN_TRACKS - MINIMUM_TIME_BETWEEN_TRACKS));
          time += timeBetweenTwoTracks;
          Trajectory trajectory = new Trajectory(trajectoryData, speed,
                                                 new Date(time));
          TrajectoryTrack track = new TrajectoryTrack(trajectory);
          if (shouldIncludeTrajectory(trajectory)) {
            trackModel.addElement(track, ILcdFireEventMode.FIRE_LATER);
          }
          fTrajectories.add(trajectory);
          fTracks.add(track);
        }
      }
    }
    trackModel.fireCollectedModelChanges();
    updateBeginAndEndDate();
  }

  private boolean shouldIncludeTrajectory(Trajectory aTrajectory) {
    return fDate.getTime() >= aTrajectory.getBeginDate().getTime() &&
           fDate.getTime() <= aTrajectory.getEndDate().getTime();
  }

  private void updateBeginAndEndDate() {
    long begin = Long.MAX_VALUE;
    long end = 0;
    if (fTrajectories.size() > 0) {
      for (Trajectory trajectory : fTrajectories) {
        if (trajectory.getBeginDate().getTime() < begin) {
          begin = trajectory.getBeginDate().getTime();
        }
        if (trajectory.getEndDate().getTime() > end) {
          end = trajectory.getEndDate().getTime();
        }
      }
    } else {
      begin = end = System.currentTimeMillis();
    }

    fBeginDate = new Date(begin);
    fEndDate = new Date(end);

    fPropertyChangeSupport.firePropertyChange("beginDate", null, null);
    fPropertyChangeSupport.firePropertyChange("endDate", null, null);
  }
}
