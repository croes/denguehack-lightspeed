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

import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdShape;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A utility class that simulates a point moving along a track. The simulation
 * is time based and uses {@code Date} to represent the begin, end and current
 * time. It is also possible to create a model that contains the history of the
 * point, up to a given number of previous positions.
 *
 */
public class TrackSimulatorModel {
  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(TrackSimulatorModel.class);

  // Interval between flights on a track in seconds
  private static final double FLIGHT_INTERVAL = 2 * 60 * 60;
  // 222 m/sec ~= 800 km/h
  private static final double FLIGHT_SPEED = 222;
  // Total duration of simulation in seconds
  private static final double GLOBAL_DURATION = 24 * 60 * 60;

  private List<EnrouteAirwayTrack> fTracks;
  private TLcdVectorModel fTrackModel;
  private ILcdModel[] fModelArray;
  private Date fDate = new Date(), fBeginDate, fEndDate;

  private final ExecutorService fExecutorService = Executors.newSingleThreadExecutor();

  /**
   * Constructs a new simulator model.
   *
   * @param aATSRouteModel A model containing the routes along which the points
   *          should move. A route is a continuous line.
   * @param aHistoryPointInterval An integer that denotes the number of points
   *          between each point of the history trail that is shown
   */
  public TrackSimulatorModel(ILcdModel aATSRouteModel, double aHistoryPointInterval) {
    this(aATSRouteModel, aHistoryPointInterval, new Random());
  }

  /**
   * Constructs a new simulator model.
   *
   * @param aATSRouteModel A model containing the routes along which the points
   *          should move. A route is a continuous line.
   * @param aHistoryPointInterval An integer that denotes the number of points
   *          between each point of the history trail that is shown
   * @param aRandom The random number generator
   */
  public TrackSimulatorModel(ILcdModel aATSRouteModel, double aHistoryPointInterval, Random aRandom) {
    final ILcdModelDescriptor originalDescriptor = aATSRouteModel.getModelDescriptor();
    createTrackModel(aATSRouteModel, originalDescriptor);

    int numFlights = Math.max(1, (int) (GLOBAL_DURATION / FLIGHT_INTERVAL));
    fTracks = new ArrayList<EnrouteAirwayTrack>(numFlights * ((ILcdIntegerIndexedModel) aATSRouteModel).size());
    Enumeration<?> routes = aATSRouteModel.elements();
    int routeCount = 0;
    while (routes.hasMoreElements()) {
      ILcdShape route = (ILcdShape) routes.nextElement();

      // Exemplar track
      EnrouteAirwayTrack track = new EnrouteAirwayTrack(
          route,
          FLIGHT_SPEED // Speed in m/sec
      );

      double duration = track.getDuration();
      double phase = aRandom.nextDouble() * FLIGHT_INTERVAL;
      Date globalBeginDate = new Date();
      Date globalEndDate = new Date(globalBeginDate.getTime() + (long) (GLOBAL_DURATION * 1000));
      int historyPointCount = getHistoryPointCount(route);
      for (int i = 0; i < numFlights; i++) {
        long beginDate = (long) (globalBeginDate.getTime() + phase * 1000 + i * FLIGHT_INTERVAL * 1000);
        if (beginDate + duration * 1000 <= globalEndDate.getTime()) {
          track = new EnrouteAirwayTrack(route, FLIGHT_SPEED, null, ("R" + routeCount + "-" + i));
          TrackHistory trackHistory;
          if (historyPointCount > 0) {
            trackHistory = new TrackHistory(track, historyPointCount, aHistoryPointInterval + 1);
            track.setTrackHistory(trackHistory);
          }
          track.setBeginDate(new Date(beginDate));
          fTracks.add(track);

          if (fBeginDate == null) {
            fBeginDate = new Date(track.getBeginDate().getTime());
          } else {
            fBeginDate.setTime(Math.min(fBeginDate.getTime(), track.getBeginDate().getTime()));
          }
          if (fEndDate == null) {
            fEndDate = new Date(track.getEndDate().getTime());
          } else {
            fEndDate.setTime(Math.max(fEndDate.getTime(), track.getEndDate().getTime()));
          }
        }

      }
      routeCount++;
    }

    fBeginDate.setTime(fBeginDate.getTime() + 3 * 60 * 60 * 1000);

    fModelArray = new ILcdModel[]{fTrackModel};
    setDate(fBeginDate);
  }

  private void createTrackModel(ILcdModel aRouteModel, final ILcdModelDescriptor aOriginalDescriptor) {
    fTrackModel = new TLcdVectorModel();
    fTrackModel.setModelReference(aRouteModel.getModelReference());
    fTrackModel.setModelDescriptor(new TLcdModelDescriptor(aOriginalDescriptor.getSourceName(), aOriginalDescriptor.getTypeName(), "Tracks"));
  }

  /**
   * Returns the number of positions that are to be stored in the history model.
   *
   * @param aRoute The route for which the number of history points is to be
   *          returned.
   * @return A positive integer or zero to indicate that no history should be
   *         retained. Zero by default.
   */
  protected int getHistoryPointCount(ILcdShape aRoute) {
    return 0;
  }

  /**
   * Returns an array containing one or two models: the first model contains
   * points that represent the tracks, the second model contains the history of
   * the tracks, this model is optional.
   *
   * @return An array of models.
   */
  public ILcdModel[] getTrackModels() {
    return fModelArray;
  }

  private final AtomicBoolean fRunning = new AtomicBoolean(false);
  /**
   * Token that can be taken (e.g. set to true). The owner is allowed to run updates asynchronously.
   */
  private final AtomicBoolean fRunAsynchronouslyToken = new AtomicBoolean(false);
  // @GuardedBy("this")
  private boolean fAsynchronous = true;
  private volatile Future<?> fFuture;

  /**
   * Sets the current date of the simulation. Triggers an update of the track
   * and the history model.
   *
   * @param aDate The new date.
   */
  public void setDate(final Date aDate) {
    if (!fRunning.getAndSet(true)) {
      final boolean asynchronous = TLcdAWTUtil.isDispatchThread() && fRunAsynchronouslyToken.compareAndSet(false, true);
      /**
       * We update the model in 2 phases:
       * <ol>
       *   <li>Prepare the changes: perform some computations (ex. compute new track locations) but do not expose these yet</li>
       *   <li>Apply the changes: update the tracks (ex. update the track locations) using the results of the preparation</li>
       * </ol>
       * This approach reduces contention with model readers (ex. a painter) because we only need a
       * write lock on the model in the apply phase. The goal is to perform all expensive operations
       * in the prepare phase and only swap some data in the apply phase.
       */
      Runnable runnable = new Runnable() {
        public void run() {
          // Prepare the changes
          try {
            for (EnrouteAirwayTrack track : fTracks) {
              track.prepareDate(aDate);
            }
            // Apply the changes with a write lock on the model
            try (TLcdLockUtil.Lock autoUnlock = writeLock(fTrackModel)) {
              for (EnrouteAirwayTrack track : fTracks) {
                boolean wasAirborne = track.isAirborne();
                boolean isAirborne = track.applyDate();
                if (!wasAirborne && isAirborne) {
                  fTrackModel.addElement(track, ILcdModel.FIRE_LATER);
                } else if (wasAirborne && !isAirborne) {
                  fTrackModel.removeElement(track, ILcdModel.FIRE_LATER);
                }
              }
              fTrackModel.allElementsChanged(ILcdModel.FIRE_LATER);
            }
            fTrackModel.fireCollectedModelChanges();

          } catch (Throwable t) {
            sLogger.error(t.getMessage(), t);
            throw new RuntimeException(t);
          } finally {
            if (asynchronous) {
              fRunAsynchronouslyToken.set(false);
            }
            fRunning.set(false);
          }
        }
      };
      if (asynchronous) {
        fFuture = fExecutorService.submit(runnable);
      } else {
        runnable.run();
      }
      fDate.setTime(aDate.getTime());
    }
  }

  /**
   * Returns the current date of the simulation.
   *
   * @return The current simulation date.
   */
  public Date getDate() {
    return fDate;
  }

  /**
   * Returns the begin date of the simulation.
   *
   * @return The begin date.
   */
  public Date getBeginDate() {
    return fBeginDate;
  }

  /**
   * Returns the end date of the simulation.
   *
   * @return The end Date,
   */
  public Date getEndDate() {
    return fEndDate;
  }

  /**
   * Returns whether this model is updated asynchronously.
   *
   * @return {@code true} if this model is updated asynchronously
   */
  public synchronized boolean isAsynchronous() {
    return fAsynchronous;
  }

  /**
   * Changes whether the model is updated asynchronously or not.
   * <p/>
   * When {@code aAsynchronous} if {@code false}, no asynchronous model changes will be performed
   * any more after this method returns until the model is set to update asynchronously again.
   *
   * @param aAsynchronous {@code true} if the model should be updated asynchronously
   */
  public synchronized void setAsynchronous(boolean aAsynchronous) {
    if (fAsynchronous == aAsynchronous) {
      return;
    }

    if (aAsynchronous) {
      fRunAsynchronouslyToken.compareAndSet(true, false); // release the token
    } else {
      while (!fRunAsynchronouslyToken.compareAndSet(false, true)) { // grab the token to avoid asynchronous updates
        Future<?> future = fFuture;
        if (future != null) {
          try {
            future.get();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          } catch (ExecutionException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    fAsynchronous = aAsynchronous;
  }
}
