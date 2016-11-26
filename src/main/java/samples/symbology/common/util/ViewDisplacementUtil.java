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
package samples.symbology.common.util;

import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.*;

import java.awt.Point;

import com.luciad.gui.ILcdAnchoredIcon;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdShape;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;

/**
 * Calculates the necessary displacement for 3D visualization to avoid terrain intersections.
 */
//@ThreadSafe
public class ViewDisplacementUtil {

  private final ILcdObjectIconProvider fIconProvider;

  /**
   * Creates an instance to be able to calculate icon displacements depending on the created icons by
   * given {@link ILcdObjectIconProvider}
   * @param aIconProvider displacements will be calculated based on icons created by this icon provider
   */
  public ViewDisplacementUtil(ILcdObjectIconProvider aIconProvider) {
    fIconProvider = aIconProvider;
  }

  /**
   * Calculates the view displacement for the given object and creates a style for it
   * @param aObject object from which an icon will be created and displacement will be calculated for the icon
   * @return a {@link TLspViewDisplacementStyle} for calculated displacements
   */
  public TLspViewDisplacementStyle getDisplacement(Object aObject) {
    ILcdIcon icon = null;
    if (fIconProvider.canGetIcon(aObject)) {
      icon = fIconProvider.getIcon(aObject);
    }
    return getViewDisplacement(icon);
  }

  /**
   * Calculates the view displacement for the given icon and creates a style for it
   * @param aIcon icon whose displacement will be calculated
   * @return a {@link TLspViewDisplacementStyle} for calculated displacements
   */
  public static TLspViewDisplacementStyle getViewDisplacement(ILcdIcon aIcon) {
    int offsetX = 0;
    int offsetY = 0;
    if (aIcon != null) {
      offsetX = (int) (aIcon.getIconWidth() / 2.0);
      offsetY = (int) (aIcon.getIconHeight() / 2.0);

      if (aIcon instanceof ILcdAnchoredIcon) {
        Point anchor = new Point();
        ((ILcdAnchoredIcon) aIcon).anchorPointSFCT(anchor);
        offsetX += aIcon.getIconWidth() / 2.0 - anchor.getX();
        offsetY += aIcon.getIconHeight() / 2.0 - anchor.getY();
      }
    }

    return TLspViewDisplacementStyle.newBuilder().viewDisplacement(offsetX, offsetY).build();
  }

  /**
   * Decides whether to apply a displacement or not.
   * @param a3D is view 3D or not
   * @param aGeometry geometry of the symbol
   * @param aElevationMode elevation mode of the symbol
   * @return true If a symbol has 0 altitude level and the view is 3D and elevation mode is ABOVE_TERRAIN or ABOVE_ELLIPSOID, false otherwise
   */
  public static boolean shouldUseViewDisplacement(boolean a3D, Object aGeometry, ILspWorldElevationStyle.ElevationMode aElevationMode) {
    return a3D && (aElevationMode == ABOVE_TERRAIN || aElevationMode == ABOVE_ELLIPSOID) &&
           isAtZeroAltitude(aGeometry);
  }

  /**
   * Returns if the given geometry has an altitude value of 0 or not
   * @param aGeometry geometry to be tested
   * @return true if geometry has 0 altitude value. false otherwise
   */
  public static boolean isAtZeroAltitude(Object aGeometry) {
    if (!(aGeometry instanceof ILcdBounded)) {
      throw new IllegalArgumentException("Expected ILcdBounded");
    }
    ILcdBounded shape = (ILcdBounded) aGeometry;
    return shape.getBounds().getLocation().getZ() == 0 && shape.getBounds().getDepth() == 0;
  }

  /**
   * Returns elevationmode for the given geometry
   * @param aGeometry geometry
   * @param aElevationMode default value
   * @return
   */
  public static ILspWorldElevationStyle.ElevationMode getIconElevationModeForIconSymbol(ILcdShape aGeometry, ILspWorldElevationStyle.ElevationMode aElevationMode) {
    if (aElevationMode == OBJECT_DEPENDENT) {
      return isAtZeroAltitude(aGeometry) ? ABOVE_TERRAIN : ABOVE_ELLIPSOID; // use the same mode as for a regular icon style
    }
    return aElevationMode;
  }



}
