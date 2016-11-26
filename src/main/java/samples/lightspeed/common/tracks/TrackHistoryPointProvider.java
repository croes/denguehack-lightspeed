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

import java.util.List;

import com.luciad.shape.ILcdPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;

/**
 * A style target provider that returns a point of a track history at a
 * predefined index.
 *
 */
public final class TrackHistoryPointProvider extends ALspStyleTargetProvider {

  private final int fIndex;

  /**
   * Creates a new provider that returns the point with the given index.
   *
   * @param aPointIndex A point index, positive or 0.
   */
  public TrackHistoryPointProvider(int aPointIndex) {
    fIndex = aPointIndex;
  }

  @Override
  public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
    final EnrouteAirwayTrack track = (EnrouteAirwayTrack) aObject;
    if (track.isAirborne()) {
      final ILcdPoint point = track.getTrackHistory().safeGetPoint(fIndex);
      if (point != null) {
        aResultSFCT.add(point);
      }
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + fIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TrackHistoryPointProvider other = (TrackHistoryPointProvider) obj;
    if (fIndex != other.fIndex) {
      return false;
    }
    return true;
  }

}
