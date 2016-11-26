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

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;

/**
 * Generates random trajectories between a set of cities.
 */
public class TrajectoryGenerator {

  private TrajectoryGenerator() {
  }

  public static ILcdModel generateTrajectories(Date aBeginDate, Date aEndDate, ILcdModel aCitiesModel, int aNumberOfTrajectories) {
    ILcdModel trajectoryLinesModel = new TLcdVectorModel(
        new TLcdGeodeticReference(new TLcdGeodeticDatum()),
        new TLcdModelDescriptor("", "TrajectoryLines", "Trajectory lines")
    );
    generateTrajectories(aBeginDate, aEndDate, aCitiesModel, aNumberOfTrajectories, trajectoryLinesModel);
    return trajectoryLinesModel;
  }

  public static void generateTrajectories(Date aBeginDate, Date aEndDate, ILcdModel aCitiesModel, int aNumberOfTrajectories, ILcdModel aTrajectoryLinesModel) {
    ILcdEllipsoid ellipsoid = ((ILcdGeodeticReference) aTrajectoryLinesModel.getModelReference()).getGeodeticDatum().getEllipsoid();

    // Get all the shapes.
    ArrayList<ILcdPoint> shapes = new ArrayList<ILcdPoint>();
    for (Enumeration<?> e = aCitiesModel.elements(); e.hasMoreElements(); ) {
      shapes.add((ILcdPoint) e.nextElement());
    }

    // Populate the trajectories list with trajectories between random cities
    Random random = new Random(0xACCE55);
    for (int i = 0; i < aNumberOfTrajectories; i++) {
      // Get a random start and end point for the trajectory but make sure the start point is different
      // than the end point
      ILcdPoint departureCity = shapes.get(random.nextInt(shapes.size()));
      ILcdPoint destinationCity = shapes.get(random.nextInt(shapes.size()));
      if (departureCity == destinationCity) {
        i--;
        continue;
      }
      //Generate random begin and end times, based on a random flight speed and distance of trajectory.
      double distanceInMeters = ((ILcdGeoReference) aCitiesModel.getModelReference()).getGeodeticDatum().getEllipsoid().geodesicDistance(departureCity, destinationCity);
      if (distanceInMeters > 10000000) {
        i--;
        continue;
      }
      double trajectorySpeedInMetersPerSecond = random.nextDouble() * 100 + 200;
      double durationInSeconds = distanceInMeters / trajectorySpeedInMetersPerSecond;
      long durationInMilliSeconds = Math.round(durationInSeconds) * 1000;

      //make sure begin time fits in our timeframe
      long lastPossibleLeaveTime = aEndDate.getTime() - durationInMilliSeconds;
      long randomBeginTime = nextLong(random, aBeginDate.getTime(), lastPossibleLeaveTime);
      long calculatedEndTime = randomBeginTime + durationInMilliSeconds;

      // Create a new parabolic trajectory based on the retrieved start and end points and start and end times
      aTrajectoryLinesModel.addElement(new ParabolicTrajectory(departureCity, destinationCity, randomBeginTime, calculatedEndTime, ellipsoid, nextID(i)), ILcdFireEventMode.NO_EVENT);
    }
  }

  /**
   * Generates a semi-random number between the given min and max.
   *
   * @param aRandom a random
   * @param aMinValue a minimum value (inclusive)
   * @param aMaxValue a maximum value (exclusive)
   * @return a random long between the given minimum and maximum
   */
  private static long nextLong(Random aRandom, long aMinValue, long aMaxValue) {
    double r = aRandom.nextDouble();
    r = Math.abs(r * Math.sin(r));  // This formula creates a non-uniform distribution.
    return aMinValue + (long)((double)(aMaxValue - aMinValue) * r);
  }

  public static void generateTrackPlots(ILcdModel aTrajectoryLinesModel, int aTrackPlotTimeStep, TimeUnit aTimeUnit, ILcdModel aTrackPlotsModel) {
    long dt = aTimeUnit.toMillis(aTrackPlotTimeStep);
    Enumeration en = aTrajectoryLinesModel.elements();
    TrackPlot prevPlot = null;
    TLcdLonLatHeightPoint tmpPt = new TLcdLonLatHeightPoint();
    while (en.hasMoreElements()) {
      ITrajectory trajectory = (ITrajectory) en.nextElement();
      for (long t = trajectory.getBeginTime(); t < trajectory.getEndTime(); t += dt) {
        trajectory.getPositionAtTimeSFCT(t, tmpPt);
        double orientation;
        if (prevPlot != null) {
          orientation = Math.toDegrees(trajectory.getEllipsoid().forwardAzimuth2D(prevPlot, tmpPt));
        } else {
          orientation = trajectory.getOrientationAtTimeSFCT(t);
        }
        aTrackPlotsModel.addElement(new TrackPlot(trajectory, t, tmpPt.getX(), tmpPt.getY(), tmpPt.getZ(), orientation), ILcdModel.NO_EVENT);
      }
    }
  }

  public static void generateTracks(ILcdModel aTrajectoryLinesModel, ILcdModel aModel) {
    Enumeration en = aTrajectoryLinesModel.elements();
    while (en.hasMoreElements()) {
      ITrajectory trajectory = (ITrajectory) en.nextElement();
      aModel.addElement(new TimeStampedTrack(trajectory), ILcdFireEventMode.NO_EVENT);
    }
  }

  private static String nextID(int aId) {
    return Integer.toHexString(aId).toUpperCase();
  }
}
