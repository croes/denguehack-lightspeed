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
package samples.lightspeed.imaging.multispectral.curves;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;

/**
 * Extra line that is drawn next to a Catmull rom curve, to allow for easier editing.
 */
public class CatmullRomEditLine extends TLcdXYPolyline {
  private static final double EPSILON = 0.01;

  public static final int R_CHANNEL = 0;
  public static final int G_CHANNEL = 1;
  public static final int B_CHANNEL = 2;
  public static final int GRAY_CHANNEL = 3;

  private int fChannel;

  public int getChannel() {
    return fChannel;
  }

  public void setChannel(int aChannel) {
    fChannel = aChannel;
  }

  public CatmullRomEditLine() {
    reset();
  }

  public CatmullRomEditLine(ILcdPoint[] aControlPoints) {
    for (int i = 0; i < aControlPoints.length; ++i) {
      insert2DPoint(i, aControlPoints[i].getX(), aControlPoints[i].getY());
    }
  }

  public void reset() {
    while (getPointCount() > 0) {
      super.removePointAt(getPointCount() - 1);
    }
    insert2DPoint(0, 0, 0);
    insert2DPoint(1, 0.5, 0.5);
    insert2DPoint(2, 1, 1);
  }

  @Override
  public void insert2DPoint(int aIndex, double aX, double aY) {
    if (aIndex < getPointCount() && aIndex > 1) {
      if (getPoint(aIndex - 1).getX() + EPSILON > aX) {
        return;
      }

      if (getPoint(aIndex).getX() - EPSILON < aX) {
        return;
      }
    }

    super.insert2DPoint(aIndex, aX, aY);
  }

  @Override
  public void translate2D(double aDeltaX, double aDeltaY) {
    // Only allow translation in y direction, but make sure all points are still within bounds
    double maxY = 0;
    double minY = 1;
    for (int i = 0; i < getPointCount(); i++) {
      double y = getPoint(i).getY();
      maxY = Math.max(maxY, y);
      minY = Math.min(minY, y);
    }
    if (aDeltaY > 0) {
      aDeltaY = Math.min(aDeltaY, 1 - maxY);
    } else if (aDeltaY < 0) {
      aDeltaY = Math.max(aDeltaY, -minY);
    }
    super.translate2D(0, aDeltaY);
  }

  @Override
  public void move2DPoint(int aIndex, double aX, double aY) {
    // Cannot move first and last point
    if (aIndex == 0 || aIndex == getPointCount() - 1) {
      // Only allow movement up right
      super.move2DPoint(aIndex, getPoint(aIndex).getX(), constrain(aY, 0, 1));
    } else {
      double leftX = getPoint(aIndex - 1).getX() + EPSILON;
      double rightX = getPoint(aIndex + 1).getX() - EPSILON;
      super.move2DPoint(aIndex, constrain(aX, leftX, rightX), constrain(aY, 0, 1));
    }
  }

  @Override
  public void removePointAt(int aIndex) {
    if (aIndex == 0 || aIndex == getPointCount() - 1) {
      // don't remove first and last point
      return;
    }
    super.removePointAt(aIndex);
  }

  private double constrain(double aValue, double aLeft, double aRight) {
    if (aValue < aLeft) {
      aValue = aLeft;
    }
    if (aValue > aRight) {
      aValue = aRight;
    }
    return aValue;
  }
}
