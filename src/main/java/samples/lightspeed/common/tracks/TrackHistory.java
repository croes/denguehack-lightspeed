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

import java.util.LinkedList;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;

/**
 * A point list that contains the previous positions of a track.
 */
public class TrackHistory implements ILcdPointList {

  private int fHistoryPointCount;

  private LinkedList<ILcdPoint> fHistoryPoints = new LinkedList<ILcdPoint>();

  private final EnrouteAirwayTrack fTrack;

  private final double fHistoryPointInterval;
  private int fHistoryIntervalCounter = 0;

  /**
   * Construct a new position that retains up to {@code aHistoryPointCount}
   * points.
   *
   * @param aHistoryPointCount The number of points to retain in the history.
   * @param aHistoryPointInterval
   */
  public TrackHistory(EnrouteAirwayTrack aTrack, int aHistoryPointCount, double aHistoryPointInterval) {
    fTrack = aTrack;
    fHistoryPointInterval = aHistoryPointInterval;
    fHistoryPointCount = aHistoryPointCount;
    aTrack.setTrackHistory(this);
  }

  /**
   * Adds a new position to this point list, if the number of positions is
   * limited, the oldest position will be removed.
   *
   * @param aPoint A position to add.
   */
  public synchronized void addLatestPosition(ILcdPoint aPoint) {
    if (++fHistoryIntervalCounter % fHistoryPointInterval == 0) {
      fHistoryPoints.addFirst(aPoint);
      while (fHistoryPoints.size() > fHistoryPointCount) {
        fHistoryPoints.pollLast();
      }
    }
  }

  /**
   * Returns the track for which this object contains the history.
   *
   * @return A valid track.
   */
  public EnrouteAirwayTrack getTrack() {
    return fTrack;
  }

  @Override
  public synchronized int getPointCount() {
    return fHistoryPoints.size();
  }

  public synchronized ILcdPoint safeGetPoint(int aIndex) {
    if (aIndex < fHistoryPoints.size()) {
      return fHistoryPoints.get(aIndex);
    } else {
      return null;
    }
  }

  @Override
  public synchronized ILcdPoint getPoint(int aIndex) {
    return fHistoryPoints.get(aIndex);
  }
}
