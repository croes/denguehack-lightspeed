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
package samples.gxy.editmodes;

import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPolygon;

/**
 * A {@link TLcdXYPolygon TLcdXYPolygon} that stores the number of
 * degrees it is rotated.
 */
public class XYPolygonWithRotation extends TLcdXYPolygon {

  private double fRotation = 0d;

  /**
   * Creates an unrotated polygon.
   *
   * @param aPointList a list of consecutive lon lat points that make up this polygon
   */
  public XYPolygonWithRotation(ILcd2DEditablePointList aPointList) {
    super(aPointList);
  }

  /**
   * Returns the rotation of this polygon in degrees
   */
  public double getRotation() {
    return fRotation;
  }

  /**
   * Sets the rotation of this polygon in degrees
   *
   * @param aRotation the rotation of this polygon in degrees
   */
  public void setRotation(double aRotation) {
    fRotation = aRotation;
  }
}
