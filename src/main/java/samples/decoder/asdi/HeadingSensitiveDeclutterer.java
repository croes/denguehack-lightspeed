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
package samples.decoder.asdi;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.luciad.format.asdi.TLcdASDITrack;
import com.luciad.realtime.gxy.labeling.TLcdGXYContinuousLabelingAlgorithm;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;

/**
 * Extension of TLcdContinuousDeclutteringLabelPainter that puts the labels
 * at 4 o'clock relative to the heading of each track.
 */
public class HeadingSensitiveDeclutterer extends TLcdGXYContinuousLabelingAlgorithm {
  private static final int ANGLE_DELTA = 120; //4 o'clock
  private static final int LABEL_DISTANCE = 10; //px
  private Rectangle fLabelBounds = new Rectangle();

  public HeadingSensitiveDeclutterer() {
  }

  /**
   * Overridden to have the labels at 4 o'clock relative to the heading of the track.
   */
  protected void retrieveDesiredLabelLocation(Graphics aGraphics,
                                              ILcdGXYContext aGXYContext,
                                              Object aObject,
                                              int aLabelIndex,
                                              int aSubLabelIndex,
                                              Point aRelativeLocationSFCT) {

    //We know it is a TLcdASDITrack as the layer we set this painter to
    //only contains objects of that type.
    TLcdASDITrack track = (TLcdASDITrack) aObject;

    double orientation = TrackGXYPainterZoomedIn.getTrackOrientation(
        track, aGXYContext.getGXYLayer().getModel().getModelReference());

    if (!Double.isNaN(orientation)) {
      ILcdGXYLabelPainter label_painter = aGXYContext.getGXYLayer().getGXYLabelPainter(aObject);
      if (label_painter instanceof ILcdGXYLabelPainter2) {
        try {
          ILcdGXYLabelPainter2 label_painter2 = (ILcdGXYLabelPainter2) label_painter;

          if (!(aGXYContext.getGXYLayer() instanceof ILcdGXYEditableLabelsLayer)) {
            super.retrieveDesiredLabelLocation(aGraphics, aGXYContext, aObject, aLabelIndex, aSubLabelIndex, aRelativeLocationSFCT);
            return;
          }
          ILcdGXYEditableLabelsLayer layer = (ILcdGXYEditableLabelsLayer) aGXYContext.getGXYLayer();
          TLcdLabelLocation location = layer.getLabelLocations().createLabelLocation();
          layer.getLabelLocations().getLabelLocationSFCT(aObject, aLabelIndex, aSubLabelIndex, aGXYContext.getGXYView(), location);
          location.setLocationIndex(-1);

          //Retrieve label size
          label_painter2.setLabelIndex(aLabelIndex);
          label_painter2.setSubLabelIndex(aSubLabelIndex);
          label_painter2.setLabelLocation(location);
          label_painter2.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.BODY, aGXYContext, fLabelBounds);

          //Calculate desired location, taking heading into account
          double label_orientation = orientation + ANGLE_DELTA;
          double label_angle_rad = Math.toRadians(label_orientation - 90); //convert heading to mathematical angle
          aRelativeLocationSFCT.x = (int) (Math.cos(label_angle_rad) * LABEL_DISTANCE);
          aRelativeLocationSFCT.y = (int) (Math.sin(label_angle_rad) * LABEL_DISTANCE);

          //Shift location with the label size
          if (aRelativeLocationSFCT.x <= 0) {
            aRelativeLocationSFCT.x -= fLabelBounds.width;
          }
          if (aRelativeLocationSFCT.y <= 0) {
            aRelativeLocationSFCT.y -= fLabelBounds.height;
          }
        } catch (TLcdNoBoundsException ignore) {
          //no harm, label is just not visible
          super.retrieveDesiredLabelLocation(aGraphics, aGXYContext, aObject, aLabelIndex, aSubLabelIndex, aRelativeLocationSFCT);
        }
      } else {
        super.retrieveDesiredLabelLocation(aGraphics, aGXYContext, aObject, aLabelIndex, aSubLabelIndex, aRelativeLocationSFCT);
      }
    } else {
      super.retrieveDesiredLabelLocation(aGraphics, aGXYContext, aObject, aLabelIndex, aSubLabelIndex, aRelativeLocationSFCT);
    }
  }
}
