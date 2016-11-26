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
package samples.common.gxy;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.map.TLcdMapLonLatGridLayer;
import com.luciad.view.map.painter.TLcdLonLatGridPainter;

/**
 * Refines the grid according to the view's scale.
 */
public class DynamicLonLatGridLayer extends TLcdMapLonLatGridLayer {

  private GridSpacer fGridSpacer = new GridSpacer();

  @Override
  public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
    if (getGXYPainter() instanceof TLcdLonLatGridPainter) {
      try {
        GridSpacer.GridSpacing gridSpacing = fGridSpacer.retrieveSpacing(new TLcdGXYContext(aGXYView, this));
        adaptGrid(gridSpacing);
        adaptGridLabels(gridSpacing);
      } catch (TLcdOutOfBoundsException ignored) {
        return; // the grid should not be visible, so don't try to paint it
      }
    }
    super.paint(aGraphics, aMode, aGXYView);
  }

  private void adaptGrid(GridSpacer.GridSpacing aGridSpacing) {
    getLonLatGrid().setDeltaLat(aGridSpacing.getDeltaLat());
    getLonLatGrid().setDeltaLon(aGridSpacing.getDeltaLon());
  }

  private void adaptGridLabels(GridSpacer.GridSpacing aGridSpacing) {
    double deltaLon = aGridSpacing.getDeltaLon();
    double deltaLat = aGridSpacing.getDeltaLat();

    String mode = TLcdLonLatFormatter.DEFAULT2;
    if (roundValue(deltaLon) && roundValue(deltaLat)) { //round degrees
      mode = TLcdLonLatFormatter.DEC_DEG_0;
    } else if (roundValue(deltaLon * 60) && roundValue(deltaLat * 60)) { //round minutes
      mode = TLcdLonLatFormatter.DEC_MIN_0;
    } else if (roundValue(deltaLon * 60 * 60) && roundValue(deltaLat * 60 * 60)) { //round seconds
      mode = TLcdLonLatFormatter.DEFAULT;
    }
    ((TLcdLonLatGridPainter) getGXYPainter()).setLonLatFormatter(new TLcdLonLatFormatter(mode));
  }

  private boolean roundValue(double aValue) {
    return (Math.abs(aValue - (int) aValue) < 1E-9);
  }

  /**
   * Calculates the grid spacing.
   */
  public static class GridSpacer {

    private final Collection<GridSpacing> fGridSpacings;
    private int fMaxLineCount;

    public GridSpacer() {
      this(new ArrayList<GridSpacing>(), 40);
      // the list is ordered from low zoom level (i.e. the world) to high zoom level
      fGridSpacings.add(new GridSpacing(0, 30, 30));
      fGridSpacings.add(new GridSpacing(0.0000672, 10, 10));
      fGridSpacings.add(new GridSpacing(0.000336, 5, 5));
      fGridSpacings.add(new GridSpacing(0.0012, 1, 1));
      fGridSpacings.add(new GridSpacing(0.006, 0.5, 0.5));
      fGridSpacings.add(new GridSpacing(0.012, 0.1666666666666666666666, 0.1666666666666666666666));
      fGridSpacings.add(new GridSpacing(0.024, 0.0833333333333333333333, 0.0833333333333333333333));
      fGridSpacings.add(new GridSpacing(0.048, 0.01666666666666666666666, 0.01666666666666666666666));
      fGridSpacings.add(new GridSpacing(0.192, 0.00833333333333333333333, 0.00833333333333333333333));
      fGridSpacings.add(new GridSpacing(0.384, 0.00277777777777777777777, 0.00277777777777777777777));
      fGridSpacings.add(new GridSpacing(0.768, 0.0013888888888888888888, 0.0013888888888888888888));
      fGridSpacings.add(new GridSpacing(3.072, 0.0002777777777777777777, 0.0002777777777777777777));
    }

    public GridSpacer(Collection<GridSpacing> aGridSpacings, int aMaxLineCount) {
      fGridSpacings = aGridSpacings;
      fMaxLineCount = aMaxLineCount;
    }

    public GridSpacing retrieveSpacing(ILcdGXYContext aContext) throws TLcdOutOfBoundsException {
      ILcdGXYView view = aContext.getGXYView();

      // calculate the scale in meters
      double unitOfMeasure = ((ILcdGridReference) view.getXYWorldReference()).getUnitOfMeasure();
      double scaleInMeters = view.getScale() / unitOfMeasure;

      // calculate the view bounds in lon lat coordinates
      Rectangle viewPixelBounds = new Rectangle(0, 0, view.getWidth(), view.getHeight());
      ILcd2DEditableBounds viewWorldBounds = new TLcdXYBounds();
      view.getGXYViewXYWorldTransformation().viewAWTBounds2worldSFCT(viewPixelBounds, viewWorldBounds);
      TLcdLonLatHeightBounds viewModelBounds = new TLcdLonLatHeightBounds();
      aContext.getModelXYWorldTransformation().worldBounds2modelSFCT(viewWorldBounds, viewModelBounds);

      return getScaleRange(scaleInMeters, viewModelBounds);
    }

    private GridSpacing getScaleRange(double aScale, ILcdBounds aBounds) {
      GridSpacing appropriateSection = null;
      for (GridSpacing section : fGridSpacings) {
        if (aScale < section.getScale() ||
            aBounds.getWidth() / section.getDeltaLon() > fMaxLineCount ||
            aBounds.getHeight() / section.getDeltaLat() > fMaxLineCount
            ) {
          break;
        } else {
          appropriateSection = section;
        }
      }
      return appropriateSection;
    }

    public static class GridSpacing {
      private final double fMinScale;
      private final double fDeltaLon;
      private final double fDeltaLat;

      public GridSpacing(double aMinScale, double aDeltaLon, double aDeltaLat) {
        fMinScale = aMinScale;
        fDeltaLon = aDeltaLon;
        fDeltaLat = aDeltaLat;
      }

      public double getScale() {
        return fMinScale;
      }

      public double getDeltaLon() {
        return fDeltaLon;
      }

      public double getDeltaLat() {
        return fDeltaLat;
      }
    }

  }

}
