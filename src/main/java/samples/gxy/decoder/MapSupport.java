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
package samples.gxy.decoder;

import java.awt.Color;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.ILcdTopocentricReference;
import com.luciad.reference.TLcdXYModelReference;
import com.luciad.transformation.TLcdGeocentric2Grid;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.transformation.TLcdGrid2Grid;
import com.luciad.transformation.TLcdIdentityModelXYWorldTransformation;
import com.luciad.transformation.TLcdTopocentric2Grid;
import com.luciad.view.gxy.ALcdGXYPen;
import com.luciad.view.gxy.TLcdGXYPen;
import com.luciad.view.map.TLcdGeodeticPen;
import com.luciad.view.map.TLcdGridPen;

/**
 * Utility class that can provide a <code>ILcdModelXYWorldTransformation</code> and
 * a <code>ILcdGXYPen</code> for a layer based on a given <code>ILcdModelReference</code>.
 */
public class MapSupport {

  /** A handle icon that's visible on most backgrounds **/
  public static final TLcdSymbol sHotPointIcon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 8, Color.darkGray, Color.white);

  /**
   * Returns a transformation class that suits the given model reference.
   */
  public static Class getModelXYWorldTransformationClass(ILcdModelReference aModelReference) {

    return
        aModelReference instanceof ILcdGeodeticReference ? TLcdGeodetic2Grid.class :
        aModelReference instanceof ILcdGridReference ? TLcdGrid2Grid.class :
        aModelReference instanceof ILcdTopocentricReference ? TLcdTopocentric2Grid.class :
        aModelReference instanceof ILcdGeocentricReference ? TLcdGeocentric2Grid.class :
        aModelReference instanceof TLcdXYModelReference ? TLcdIdentityModelXYWorldTransformation.class :
        null;
  }

  /**
   * Creates a pen that suits the given model reference and the given painting mode.
   * The pen is used by the painters as a painting tool, e.g. to discretize lines, or to draw hot points.
   */
  public static ALcdGXYPen createPen(ILcdModelReference aModelReference, boolean aStraightLineMode) {

    ALcdGXYPen pen = aModelReference instanceof ILcdGeodeticReference ? new TLcdGeodeticPen() :
                     aModelReference instanceof ILcdGridReference ? new TLcdGridPen() :
                     aModelReference instanceof ILcdTopocentricReference ? new TLcdGXYPen() :
                     aModelReference instanceof ILcdGeocentricReference ? new TLcdGXYPen() :
                     new TLcdGXYPen();
    if (pen instanceof TLcdGeodeticPen) {
      ((TLcdGeodeticPen) pen).setStraightLineMode(aStraightLineMode);
    }
    if (pen instanceof TLcdGridPen) {
      ((TLcdGridPen) pen).setStraightLineMode(aStraightLineMode);
    }
    if (!aStraightLineMode) {
      // enables higher quality discretization
      pen.setMinRecursionDepth(0);
      pen.setMaxRecursionDepth(10);
      pen.setAngleThreshold(0.5);
      pen.setViewDistanceThreshold(6.0);
      pen.setWorldDistanceThreshold(10.0);
    }
    pen.setHotPointIcon(sHotPointIcon);
    return pen;
  }

  /**
   * Creates a pen that suits the given model reference.
   * The pen is used by the painters as a painting tool, e.g. to discretize lines, or to draw hot points.
   */
  public static ALcdGXYPen createPen(ILcdModelReference aModelReference) {
    return createPen(aModelReference, true);
  }

}
