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

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.view.ILcdView;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

/**
 * Collection of lon lat polylines and points on those polylines.
 * Each polyline represents a trajectory. Each point represents
 * the position of a dynamic track on the corresponding trajectory.
 */
public class LonLatTrajectories {
  public static final double HEIGHT_STEPS = 10;

  private final ALcdModel fTrajectoriesModel;

  private TrackSimulatorModel fTrackSimulatorModel;

  LonLatTrajectories(Builder aBuilder) {
    fTrajectoriesModel = createEmptyModel(aBuilder.fModelReference, new TLcdModelDescriptor("Trajectories", "Trajectories", "Trajectories"), true);
    for (TLcdLonLatHeightPolyline p : aBuilder.fTrajectories) {
      fTrajectoriesModel.addElement(p, ILcdModel.NO_EVENT);
    }
    fTrackSimulatorModel = new TrackSimulatorModel(fTrajectoriesModel, 3) {

      @Override
      protected int getHistoryPointCount(ILcdShape aRoute) {
        final double flightLength = EnrouteAirwayTrack.shapeToCurve(aRoute).getLength2D(0., 1.);

        int carbonCategory = 0;
        if (flightLength < 1000000) {
          carbonCategory = 1;
        } else if (flightLength < 4000000) {
          carbonCategory = 2;
        } else if (flightLength < 10000000) {
          carbonCategory = 3;
        } else {
          carbonCategory = 4;
        }
        return carbonCategory;
      }

    };

    // Update all points to time 0.
    update(0);
  }

  /**
   * Returns a model that contains the trajectories that will be followed.
   *
   * @return a model
   */
  public ILcdModel getTrajectoriesModel() {
    return fTrajectoriesModel;
  }

  /**
   * Returns a model containing the current position along the trajectory.
   *
   * @return a model.
   */
  public ILcdModel getPositionsModel() {
    return (ALcdModel) fTrackSimulatorModel.getTrackModels()[0];
  }

  public ILcdModel getTrailsModel() {
    final ILcdModel[] trackModels = fTrackSimulatorModel.getTrackModels();
    if (trackModels.length >= 2) {
      return (ALcdModel) trackModels[1];
    } else {
      return null;
    }
  }

  /**
   * Updates the position of the track
   *
   * @param aT The time of the animation in seconds
   */
  public void update(double aT) {
    fTrackSimulatorModel.setDate(new Date((long) (fTrackSimulatorModel.getBeginDate().getTime() + aT * 1000.)));
  }

  public static ILcdPoint getPointOnLine(ILcdPoint aPoint1, ILcdPoint aPoint2, double aT) {
    return new TLcdXYPoint(aT * (aPoint1.getX() - aPoint2.getX()) + aPoint2.getX(),
                           aT * (aPoint1.getY() - aPoint2.getY()) + aPoint2.getY());

  }

  /**
   * Returns the duration in seconds.
   *
   * @return
   */
  public double getDuration() {
    return (fTrackSimulatorModel.getEndDate().getTime() - fTrackSimulatorModel.getBeginDate().getTime()) / 1000;
  }

  public static void startTrackAnimation(final ILcdView aView, final LonLatTrajectories trajectories) {
    ALcdAnimation animation = new ALcdAnimation() {

      private final static double SPEEDUP_FACTOR = 50;

      // We execute the update of the positions of the tracks on a separate
      // thread using a single thread task executor. We hold on to the returned
      // Future to avoid invoking a new update task when the old one is still
      // running.
      private Future fUpdateFuture;
      private ILcdView fView = aView;
      private ExecutorService fExecutor = Executors.newSingleThreadExecutor();

      @Override
      protected void setTimeImpl(final double aTime) {
        // Perform the update on a separate thread to avoid blocking
        // the event dispatch thread (and hence the repaints).
        if (fUpdateFuture == null || fUpdateFuture.isDone()) {
          Runnable runnable = new Runnable() {
            @Override
            public void run() {
              trajectories.update(aTime * SPEEDUP_FACTOR);
            }
          };
          fUpdateFuture = fExecutor.submit(runnable);
        }
      }

      @Override
      public double getDuration() {
        return trajectories.getDuration() / SPEEDUP_FACTOR;
      }

    };

    ALcdAnimationManager.getInstance()
                        .putAnimation(trajectories, new LoopedAnimation(animation, LoopedAnimation.LoopMode.PINGPONG));
  }

  public static double getHeightOnLine(double aT) {
    return -1100000 * Math.pow(aT, 2) + (1100000 * aT);
  }

  private static ALcdModel createEmptyModel(ILcdModelReference aModelReference, ILcdModelDescriptor aModelDescriptor, boolean aBounded) {
    if (aBounded) {
      TLcd2DBoundsIndexedModel result = new TLcd2DBoundsIndexedModel(new TLcdLonLatBounds(-180, -90, 360, 180));
      result.setModelReference(aModelReference);
      result.setModelDescriptor(aModelDescriptor);
      return result;
    } else {
      return new TLcdVectorModel(aModelReference, aModelDescriptor);
    }
  }

  /**
   * Construction of the trajectories is performed through the builder pattern.
   */
  public static class Builder {
    private ArrayList<TLcdLonLatHeightPolyline> fTrajectories = new ArrayList<TLcdLonLatHeightPolyline>();

    private ILcdModelReference fModelReference;

    public Builder(ILcdModelReference aModelReference) {
      fModelReference = aModelReference;
    }

    public Builder add(TLcdLonLatHeightPolyline aTrajectory) {
      fTrajectories.add(aTrajectory);
      return this;
    }

    public LonLatTrajectories build() {
      return new LonLatTrajectories(this);
    }
  }

}
