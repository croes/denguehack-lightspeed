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
package samples.gxy.contour.polyline;

import java.awt.Color;

import com.luciad.contour.TLcdValuedContour;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.contour.ContourPaintUtil;

/**
 * Painter for polyline contours, each polyline is drawn with a different color depending on its
 * value.
 */
public class PolylineContourPainter extends TLcdGXYPointListPainter {
  private ContourPaintUtil fContourViewUtil;
  private Color[] fLevelColors;
  private Color[] fSpecialColors;

  /**
   * Creates a new PolylineContourPainter
   *
   * @param aLevelValues   Values for the level contours.
   * @param aLevelColors   Labels for the level contours. There must be the same amount of level
   *                       values and level labels.
   * @param aSpecialValues Values for the special contours.
   * @param aSpecialColors Colors for the special contours. There must be the same amount of special
   *                       values and special colors.
   */
  public PolylineContourPainter(double[] aLevelValues,
                                Color[] aLevelColors,
                                double[] aSpecialValues,
                                Color[] aSpecialColors) {
    fLevelColors = aLevelColors;
    fSpecialColors = aSpecialColors;

    fContourViewUtil = new ContourPaintUtil(aLevelValues, aSpecialValues);

    setMode(POLYLINE);
  }

  @Override
  public void setObject(Object aObject) {

    /*
    setObject is overridden to customize the painter style based on the height value in the contour object.
    In addition, the base shape of the contour object needs to be set to the parent painter.
    */

    super.setObject(((TLcdValuedContour) aObject).getShape());

    boolean special = fContourViewUtil.isSpecial(aObject);
    int index = fContourViewUtil.getIndex(aObject);

    if (index >= 0) {
      Color color = special ? fSpecialColors[index] : fLevelColors[index];
      //darken the color so that it's visible on the background
      int darken = 2;
      color = new Color(color.getRed() / darken, color.getGreen() / darken, color.getBlue() / darken, color.getAlpha());

      setLineStyle(new TLcdG2DLineStyle(color, Color.RED));
    }
  }
}
