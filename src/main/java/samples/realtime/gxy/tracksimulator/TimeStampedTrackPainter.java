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
package samples.realtime.gxy.tracksimulator;

import java.awt.Graphics;

import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.realtime.common.TimeStampedTrack;

/**
 * A <code>TimeStampedTrackPainter</code> is a painter for instances of
 * <code>TimeStampedTrack</code>.
 * If the track is on the ground, a rectangle will be drawn around its normal symbol.
 */
class TimeStampedTrackPainter extends TLcdGXYIconPainter {
  public TimeStampedTrackPainter() {
  }

  /**
   * Paints the <code>TimeStampedTrack</code> with a rectangle around it if the
   * track is on the ground.
   */
  public void paint(Graphics aGraphics, int aState, ILcdGXYContext aGXYContext) {
    super.paint(aGraphics, aState, aGXYContext);
    TimeStampedTrack track = (TimeStampedTrack) getObject();
    if (track.isGrounded()) {
      ILcdGXYPen gxy_pen = aGXYContext.getGXYPen();
      try {
        gxy_pen.moveTo(track, aGXYContext.getModelXYWorldTransformation(),
                       aGXYContext.getGXYViewXYWorldTransformation());
        aGraphics.drawRect(gxy_pen.getX() - 6, gxy_pen.getY() - 6, 12, 12);
      } catch (TLcdOutOfBoundsException ignore) {
        // The track location (in model coordinates) is outside the valid area of
        // the transformation. This means the track is not visible in the current
        // map display, so this exception can be safely ignored.
      }
    }
  }
}
