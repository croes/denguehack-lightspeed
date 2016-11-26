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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.util.ILcdFunction;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * Class to generate artificial ASTERIX category 240 data.
 */
public class SimulatedRadarStream extends PipedInputStream {

  private static final double SPEED_OF_LIGHT = 299792458;
  private final List<DetectedObject> fObjects;

  private static final int CELL_PER_BLOCK = 64;

  private final double fTimePerBlock;
  private final double fAzimuthResolution;
  private final ILcdPoint fPosition;
  private final int fBlockCount;
  private final int fCellCount;
  private final double fRange;
  private final int fCellDuration;
  private final Object fClosedLock = new Object();
  private boolean fClosed = false;
  private final ILcdBounds fBounds;

  /**
   * New simulated radar data stream based on the position, range and resolution of a radar.
   *
   *
   * @param aPosition the position of the radar.
   * @param aAzimuthResolution the azimuth range one radar sweep covers.
   * @param aRange the range of the radar in metres.
   * @param aCellRange the range of one cell in metres.
   * @param aSecondsPerRotation the time for one rotation in milliseconds.
   * @throws IOException
   */
  public SimulatedRadarStream(
      ILcdPoint aPosition,
      double aAzimuthResolution,
      double aRange,
      double aCellRange,
      double aSecondsPerRotation
  ) throws IOException {
    super(500 * 1024);
    fObjects = new ArrayList<DetectedObject>();
    fTimePerBlock = aSecondsPerRotation * 1000 * aAzimuthResolution / 360.0;
    fPosition = aPosition;
    fAzimuthResolution = aAzimuthResolution;
    fBlockCount = (int) (aRange / (aCellRange * CELL_PER_BLOCK));
    fCellCount = fBlockCount * CELL_PER_BLOCK;

    /* Calculate the cell duration based on the formula (D=CELL_DUR*(START_RG + NU_CELL-1)*c/2) in the ASTERIX 240
    specification. The Video Header used in this stream is defined in nanoseconds, therefore the result is multiplied
    with 1e9.*/
    fCellDuration = (int) (aCellRange * (2.0 / SPEED_OF_LIGHT) * 1e9);

    /* The cell duration in ASTERIX Category 240 is encoded as an integer (in nanoseconds),
       hence compute the actual range based on the the integer duration. */
    fRange = fCellCount * fCellDuration * SPEED_OF_LIGHT / 2.0 / 1.0e9;

    fBounds = new TLcdLonLatCircle(
        aPosition, fRange, TLcdEllipsoid.DEFAULT
    ).getBounds().cloneAs2DEditableBounds();
    RadarPipedOut pipedOut = new RadarPipedOut();
    connect(pipedOut);
    pipedOut.start();
  }

  @Override
  public void close() throws IOException {
    synchronized (fClosedLock) {
      fClosed = true;
      super.close();
    }
  }

  private boolean isClosed() {
    synchronized (fClosedLock) {
      return fClosed;
    }
  }

  /**
   * Add a model whose elements need to be checked that they are seen by the radar. If they are in the range of the radar,
   * they will be inserted in the data stream.
   *
   * @param aModel the model whose elements will be checked if they are in the radars range.
   */
  public void addModel(final ILcd2DBoundsIndexedModel aModel) {
    ILcdGeodeticReference reference = (ILcdGeodeticReference) aModel.getModelReference();
    final ILcdEllipsoid e = reference.getGeodeticDatum().getEllipsoid();

    aModel.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        synchronized (fObjects) {
          fObjects.clear();
          try {
            TLcdLockUtil.readLock(aModel);
            //If an element is in the range of the radar, add it to the detected objects list.
            aModel.applyOnInteract2DBounds(fBounds, false, new ILcdFunction() {
              @Override
              public boolean applyOn(Object aObject) throws IllegalArgumentException {
                if (aObject instanceof ILcdPoint) {
                  //Calculate the distance and azimuth from the radar's origin, this is later used to determine the amplitude
                  //in the cells of the radar.
                  ILcdPoint timedStampTrack = (ILcdPoint) aObject;
                  double distance = e.geodesicDistance(fPosition, timedStampTrack);
                  double azimuth = Math.toDegrees(e.forwardAzimuth2D(fPosition, timedStampTrack));

                  int planeSize = 100;
                  double diffAz = Math.toDegrees(Math.atan2(planeSize / 2, distance));

                  fObjects.add(
                      new DetectedObject(
                          azimuth - diffAz,
                          azimuth + diffAz,
                          distance - planeSize / 2,
                          distance + planeSize / 2,
                          Math.max(0.5, (System.identityHashCode(timedStampTrack) & 0xFF) / 255.0)
                      ));
                }
                return true;
              }
            }, 0, 0);
          } finally {
            TLcdLockUtil.readUnlock(aModel);
          }
        }
      }
    });
  }

  class DetectedObject {
    public final double fStartAzimuth;
    public final double fEndAzimuth;
    public final double fMinDistance;
    public final double fMaxDistance;
    public final double fAmplitude;

    public DetectedObject(
        double aStartAzimuth,
        double aEndAzimuth,
        double aMinDistance,
        double aMaxDistance,
        double aAmplitude
    ) {
      fStartAzimuth = aStartAzimuth;
      fMinDistance = aMinDistance;
      fMaxDistance = aMaxDistance;
      fEndAzimuth = aEndAzimuth;
      fAmplitude = aAmplitude;
    }
  }

  /**
   * A PipedOutputStream to which Asterix240 data is written.
   */
  class RadarPipedOut extends PipedOutputStream {

    private int fCategory = 240;
    private int fId = 0xAAAA;

    public void start() throws IOException {

      final Timer timer = new Timer();

      TimerTask timerTask = new TimerTask() {

        private double fTime = System.nanoTime() * 1e-6;
        private double angle = 0;
        private int index = 0;

        @Override
        public void run() {
          try {

            if (isClosed()) {
              timer.cancel();
              RadarPipedOut.this.close();
              return;
            }

            int blocksToWrite = 0;

            double t = System.nanoTime() * 1e-6;
            double delta = t - fTime;

            if (delta > 0) {
              blocksToWrite = (int) (delta / fTimePerBlock);
              if (blocksToWrite > 0) {
                fTime = t;
              }
            }

            //Generate ASTERIX Category 240 data blocks.
            for (int i = 0; i < blocksToWrite; i++) {
              double endAngle = angle + fAzimuthResolution;
              endAngle = (endAngle > 360) ? endAngle - 360 : endAngle;

              //category
              write(fCategory);
              //block length
              writeShort(32 + fCellCount);
              //FSPEC flags
              writeShort(0xEBA0);
              //id
              writeShort(fId);
              //messagetype
              write(2);
              //video record header
              writeInt(index++);
              //videoheader nano
              int startAzimuth = (int) (angle * (Math.pow(2, 16) / 360));
              int endAzimuth = (int) (endAngle * (Math.pow(2, 16) / 360));
              writeShort(startAzimuth);
              writeShort(endAzimuth);
              writeInt(0);
              writeInt(fCellDuration);

              //cell resolution and compression
              write(0);
              //8 bits per cell
              write(4);

              //block and cell counters
              writeShort(fBlockCount);
              write3Bytes(fCellCount);

              //videoblocks
              write(fBlockCount);

              for (int j = 0; j < fCellCount; j++) {
                write(getAmplitudeValue(angle, endAngle, j));
              }

              flush();
              angle = endAngle;
              angle = (angle > 360) ? angle - 360 : angle;
            }
          } catch (IOException e) {
          }
        }
      };

      timer.scheduleAtFixedRate(timerTask, 0, (long) Math.max(10, 10 * fTimePerBlock));
    }

    private boolean isClosed() {
      synchronized (fClosedLock) {
        return fClosed;
      }
    }

    /**
     * Calculate an amplitude for a cell in a sweep. If the cell is close to one of the points of the models, a high amplitude is
     * generated.
     *
     * @param aStartAngle the start angle of the current sweep.
     * @param aEndAngle the end angle of the current sweep.
     * @param aCellIndex the cells index in the radar sweep
     * @return the amplitude for the cell
     */
    public int getAmplitudeValue(double aStartAngle, double aEndAngle, int aCellIndex) {
      double cellDistanceMin = aCellIndex * (fRange / fCellCount);
      double cellDistanceMax = (aCellIndex + 1) * (fRange / fCellCount);
      double noise = Math.random() < 0.05 ? Math.random() * 0.15 : 0.0;
      synchronized (fObjects) {
        for (int i = 0; i < fObjects.size(); i++) {
          DetectedObject detectedObject = fObjects.get(i);

          double minDist = detectedObject.fMinDistance;
          double maxDist = detectedObject.fMaxDistance;
          double minAz = detectedObject.fStartAzimuth;
          double maxAz = detectedObject.fEndAzimuth;

          boolean distanceCheck = ((minDist > cellDistanceMin && minDist < cellDistanceMax) || (maxDist > cellDistanceMin && maxDist < cellDistanceMax))
                                  || ((cellDistanceMin > minDist && cellDistanceMin < maxDist) || (cellDistanceMax > minDist && cellDistanceMax < maxDist));

          boolean azCheck = ((minAz > aStartAngle && minAz < aEndAngle) || (maxAz > aStartAngle && maxAz < aEndAngle))
                            || ((aStartAngle > minAz && aStartAngle < maxAz) || (aEndAngle > minAz && aEndAngle < maxAz));

          if (distanceCheck && azCheck) {
            return (int) (255 * Math.min(1.0, detectedObject.fAmplitude + noise));
          }
        }
      }
      return (int) (noise * 255);
    }

    public final void writeShort(int v) throws IOException {
      write((v >>> 8) & 0xFF);
      write((v >>> 0) & 0xFF);
    }

    public final void write3Bytes(int v) throws IOException {
      write((v >>> 16) & 0xFF);
      write((v >>> 8) & 0xFF);
      write((v >>> 0) & 0xFF);
    }

    public final void writeInt(int v) throws IOException {
      write((v >>> 24) & 0xFF);
      write((v >>> 16) & 0xFF);
      write((v >>> 8) & 0xFF);
      write((v >>> 0) & 0xFF);
    }
  }
}
