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
package samples.gxy.vertical;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdInterval;
import com.luciad.view.vertical.ILcdVVXAxisRenderer;
import com.luciad.view.vertical.TLcdDefaultVVGridLineOrdinateProvider;
import com.luciad.view.vertical.TLcdVVJPanel;

/**
 * Paints distance labels at regular intervals between the leftmost and rightmost visible profile points.
 */
public class VVXAxisDistanceRenderer implements ILcdVVXAxisRenderer {

  private TLcdDistanceFormat fDistanceFormat;
  private TLcdAltitudeUnit fAltitudeUnit;

  public VVXAxisDistanceRenderer() {
    setDistanceFormat(new TLcdDistanceFormat(TLcdDistanceUnit.KM_UNIT));
    fDistanceFormat.setFractionDigits(0);
  }

  public void setDistanceFormat(TLcdDistanceFormat aFormat) {
    fDistanceFormat = aFormat;
    TLcdDistanceUnit distanceUnit = fDistanceFormat.getUserUnit();
    fDistanceFormat.setProgramUnit(distanceUnit);
    // derive an altitude unit to pass to TLcdDefaultVVGridLineOrdinateProvider
    fAltitudeUnit = new TLcdAltitudeUnit(distanceUnit.getUnitName(), distanceUnit.getUnitShortName(), distanceUnit.getToMetreFactor());
  }

  @Override
  public int getHeight(Graphics aGraphics, TLcdVVJPanel aVV) {
    return (int) getMaxStringBounds(aGraphics).getHeight();
  }

  @Override
  public void paintOnXAxis(int[] aVisibleProfilePointXPositions, int aStartPointIndex, int aVisibleProfilePoints, Rectangle aBounds, Graphics aGraphics, TLcdVVJPanel aVV) {
    if (aVisibleProfilePoints >= 2) {
      ((Graphics2D) aGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      double distanceToFirstVisiblePoint = fDistanceFormat.getUserUnit().convertFromStandard(aVV.getVVModel().getDistance(aStartPointIndex, 0));
      double distanceToLastVisiblePoint = fDistanceFormat.getUserUnit().convertFromStandard(aVV.getVVModel().getDistance(aStartPointIndex + aVisibleProfilePoints - 1, 0));

      // reuse TLcdDefaultVVGridLineOrdinateProvider to come up with a list of distances to label
      TLcdDefaultVVGridLineOrdinateProvider provider = new TLcdDefaultVVGridLineOrdinateProvider();
      double viewRangeToBeLabeled = aVisibleProfilePointXPositions[aVisibleProfilePoints - 1] - aVisibleProfilePointXPositions[0];
      double pixelsPerDistanceUnit = viewRangeToBeLabeled / (distanceToLastVisiblePoint - distanceToFirstVisiblePoint);
      double[] distancesToBeLabeled = provider.getGridLineOrdinates(pixelsPerDistanceUnit, new TLcdInterval(distanceToFirstVisiblePoint, distanceToLastVisiblePoint), fAltitudeUnit);

      int labelDistance = (int) getMaxStringBounds(aGraphics).getWidth();
      int previousLabelX = Integer.MIN_VALUE;

      for (double distance : distancesToBeLabeled) {
        int labelX = (int) (aVisibleProfilePointXPositions[0] + ((distance - distanceToFirstVisiblePoint) * pixelsPerDistanceUnit));
        // only paint a label if we have enough space for it
        if (labelX >= previousLabelX + labelDistance) {
         String label = fDistanceFormat.formatDistance(distance);
          int labelWidth = (int) aGraphics.getFontMetrics().getStringBounds(label, aGraphics).getBounds().getWidth();
          if (labelX + labelWidth <= aVV.getWidth()) {
            aGraphics.setColor(Color.black);
            aGraphics.drawString(label, labelX, aBounds.y + aBounds.height);
            previousLabelX = labelX;
          }
        }
      }
    }
  }

  private Rectangle2D getMaxStringBounds(Graphics aGraphics) {
    return aGraphics.getFontMetrics().getStringBounds(fDistanceFormat.formatDistance(1234567890), aGraphics);
  }
}
