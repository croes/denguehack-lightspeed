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

import com.luciad.view.gxy.TLcdGXYCurvedPathLabelPainter;

import samples.gxy.contour.ContourPaintUtil;

/**
 * Label painter for the polyline contours, using a string array to determine
 * the label texts for each contour value.
 */
public class PolylineContourLabelPainter extends TLcdGXYCurvedPathLabelPainter {
  private ContourPaintUtil fContourViewUtil;
  private Color[] fLevelColors;
  private Color[] fSpecialColors;
  private String[] fLevelLabels;
  private String[] fSpecialLabels;

  /**
   * Creates a new PolylineContourLabelPainter
   * @param aLevelValues Values for the level contours.
   * @param aLevelColors Labels for the level contours. There must be the same amount of level
   * values and level labels.
   * @param aLevelLabels Colors for the level contours. There must be the same amount of level
   * values and level colors.
   * @param aSpecialValues Values for the special contours.
   * @param aSpecialColors Colors for the special contours. There must be the same amount of special
   * values and special colors.
   * @param aSpecialLabels Labels for the special contours. There must be the same amount of special
   * values and special labels.
   */
  public PolylineContourLabelPainter(double[] aLevelValues,
                                     Color[] aLevelColors,
                                     String[] aLevelLabels,
                                     double[] aSpecialValues,
                                     Color[] aSpecialColors,
                                     String[] aSpecialLabels) {
    fLevelColors = aLevelColors;
    fLevelLabels = aLevelLabels;
    fSpecialColors = aSpecialColors;
    fSpecialLabels = aSpecialLabels;

    fContourViewUtil = new ContourPaintUtil(aLevelValues, aSpecialValues);
  }

  public void setObject(Object aObject) {

    //setObject is overridden to customize the label painter style based on the height value in the contour object.

    if (!fContourViewUtil.isValidObject(aObject)) {
      throw new IllegalArgumentException("The PolylineContourLabelPainter only accepts TLcdValuedContour or TLcdIntervalContour objects");
    }
    super.setObject(aObject);

    boolean special = fContourViewUtil.isSpecial(aObject);
    int index = fContourViewUtil.getIndex(aObject);

    if (index >= 0) {
      Color color = special ? fSpecialColors[index] : fLevelColors[index];
      setForeground(color);
    }
  }

  /**
   * Returns the text should be used as a label.
   *
   * @param aObject        the object
   * @param aLabelIndex    the label index
   * @param aSubLabelIndex the sublabel index
   *
   * @return the value of the TLcdValuedPolygon or TLcdValuedContour set to this painter.
   *
   * @see #setObject
   */
  @Override
  public String retrieveLabels(Object aObject, int aLabelIndex, int aSubLabelIndex) {
    boolean special = fContourViewUtil.isSpecial(getObject());
    int index = fContourViewUtil.getIndex(getObject());

    if (index >= 0) {
      return special ? fSpecialLabels[index] : fLevelLabels[index];
    }

    return super.retrieveLabels(aObject, aLabelIndex, aSubLabelIndex);
  }
}
