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
package samples.lightspeed.customization.style.highlighting;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.CENTER;
import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.SOUTH;

import java.awt.geom.Dimension2D;
import java.util.Collection;
import java.util.Collections;

import com.luciad.geodesy.TLcdEllipsoid;
import samples.lightspeed.labels.util.FixedTextProviderStyle;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatArc;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.ALspStampLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm.LabelContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

/**
 * A styler that labels the 3D pie charts for countries in the highlighting sample. The labels shows
 * the population distribution per age group.
 */
public class AnimatedHighlightAreaLabelStyler extends AnimatedHighlightStyler {

  private final ILspLabelingAlgorithm fCenterLabelAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(CENTER));

  /**
   * The default constructor for this AnimatedHighlightAreaLabelStyler
   */
  public AnimatedHighlightAreaLabelStyler() {
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (aStyleCollector instanceof ALspLabelStyleCollector) {
      style(aObjects, (ALspLabelStyleCollector) aStyleCollector, aContext);
    } else {
      throw new IllegalArgumentException("ALspLabelStyleCollector expected, not " + aStyleCollector);
    }
  }

  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      Integer alpha = getCurrentAlpha(object);
      ILspStyler styler = fStylerIndex.get(alpha);
      if (styler == null) {
        styler = createStyler(((double) alpha) / 255.0f);
        fStylerIndex.put(alpha, styler);
      }
      styler.style(Collections.singleton(object), aStyleCollector, aContext);
    }
  }

  /**
   * Creates a styler for a given alpha value.
   *
   * @param aAlpha alpha is a number in [0,1] where 0 corresponds to not highlighted and 1
   *               corresponds to fully highlighted.
   *
   * @return a style corresponding to the given alpha
   */
  protected ILspStyler createStyler(final double aAlpha) {
    if (aAlpha < 0.01) {
      return TLspLabelStyler.newBuilder().build();
    }

    final TLspLabelOpacityStyle opacityStyle = TLspLabelOpacityStyle.newBuilder().opacity((float) aAlpha).build();

    return new ALspLabelStyler() {
      @Override
      public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
        for (Object object : aObjects) {
          ILcdShape shape = (ILcdShape) object;
          //Retrieve the center point and radius of the shape
          ILcdBounds b = shape.getBounds();
          double radius = Math.sqrt(b.getWidth() * b.getWidth() + b.getHeight() * b.getHeight()) * 8e3;

          ILcdPoint center = AnimatedHighlightStyler.getFocusPoint(shape);

          //Retrieve and store the statistical information for each population group
          int[] slices = getSliceStatistics(object);

          int max = 0;
          int total = 0;
          boolean validPieChart = false;
          for (int slice : slices) {
            total += slice;
            max = Math.max(max, slice);
            validPieChart |= (slice != 0);
          }

          double alpha = 0;
          float normalizedT = (float) aAlpha;
          float tRadius = Math.min(1f, normalizedT * 2);
          float tHeight = Math.max(0f, normalizedT - 0.5f) * 2f;

          int priorityModifier = (int) (1000 * normalizedT);

          double sAltitude = max / 200.0;
          ILcd3DEditablePoint centerPoint = center.cloneAs3DEditablePoint();
          centerPoint.translate3D(0, 0, ELEVATION_OFFSET + tHeight * sAltitude);

          aStyleCollector.object(object)
                         .label("country")
                         .priority(priorityModifier)
                         .geometry(centerPoint)
                         .algorithm(fCenterLabelAlgorithm)
                         .styles(COUNTRY_TEXT_STYLE, opacityStyle)
                         .submit();

          if (!validPieChart) {
            aStyleCollector
                .object(object)
                .label("noData")
                .algorithm(new TLspLabelingAlgorithm(new TLspLabelLocationProvider(20, SOUTH)))
                .styles(FixedTextProviderStyle.newBuilder().text("No demographic data available").build(), opacityStyle, CHART_MALE_TEXT_STYLE)
                .submit();
            continue;
          }
          // This is the complete circle around the pie chart
          for (int i = 0; i < slices.length; i++) {
            double percentage = (double) slices[i] / (double) total;
            int roundedPercentage = (int) (100.0 * percentage);
            double angle = 360.0 * percentage;

            TLcdLonLatArc arc = new TLcdLonLatArc(center, radius * tRadius / 1.5, radius * tRadius / 1.5, 0, alpha + 90, angle, new TLcdEllipsoid());
            TLcdLonLatHeightPoint anchorPoint = new TLcdLonLatHeightPoint();
            arc.computePointSFCT(0.5, anchorPoint);
            anchorPoint.translate3D(0, 0, ELEVATION_OFFSET + tHeight * sAltitude);

            centerPoint = center.cloneAs3DEditablePoint();
            centerPoint.translate3D(0, 0, ELEVATION_OFFSET + tHeight * sAltitude);

            TLspTextStyle textStyle = (i < 3) ? CHART_MALE_TEXT_STYLE : CHART_FEMALE_TEXT_STYLE;

            aStyleCollector.object(object)
                           .label("dot-" + i)
                           .priority(priorityModifier + (200 - roundedPercentage))
                           .geometry(anchorPoint)
                           .algorithm(fCenterLabelAlgorithm)
                           .styles(CHART_ANCHOR_ICON_STYLE)
                           .submit();
            try {
              ILcd3DEditablePoint worldCenterPoint = new TLcdXYZPoint();
              aContext.getModelXYZWorldTransformation().modelPoint2worldSFCT(centerPoint, worldCenterPoint);

              FixedTextProviderStyle textProviderStyle = FixedTextProviderStyle.newBuilder().text(CHART_CAT_LABELS[i] + ": " + String.valueOf(roundedPercentage) + "%").build();
              aStyleCollector.object(object)
                             .label("pct-" + i)
                             .priority(priorityModifier + (100 - roundedPercentage))
                             .geometry(anchorPoint)
                             .algorithm(new TLspLabelingAlgorithm(new PieChartLocationProvider(worldCenterPoint)))
                             .styles(textStyle, CHART_PIN_STYLE, opacityStyle, textProviderStyle)
                             .submit();
            } catch (TLcdOutOfBoundsException e) {
              // Skip label if the anchor point lies outside the world bounds
            }

            alpha += angle;
          }
        }
      }
    };
  }

  /**
   * Class providing label locations for the pie charts
   */
  private static class PieChartLocationProvider extends ALspStampLabelLocationProvider {

    private final ILcdPoint fCenterPoint;

    private PieChartLocationProvider(ILcdPoint aCenterPoint) {
      super(true);
      fCenterPoint = aCenterPoint;
    }

    @Override
    public int getMaxLocationCount(TLspLabelID aLabel, TLspPaintState aPaintState, LabelContext aLabelContext, ILspView aView) {
      return 5;
    }

    @Override
    public double calculateLocation(int aLocationIndex, TLspLabelID aLabel, ILcdPoint aObjectAnchorPoint, Dimension2D aDimension, TLspContext aContext, ILcd3DEditablePoint aUpperLeftPointSFCT) throws TLcdNoBoundsException {
      TLcdXYZPoint centerPoint = new TLcdXYZPoint();

      aContext.getViewXYZWorldTransformation().worldPoint2ViewSFCT(fCenterPoint, centerPoint);

      double dx = aObjectAnchorPoint.getX() - centerPoint.getX();
      double dy = aObjectAnchorPoint.getY() - centerPoint.getY();
      double length = Math.sqrt((dx * dx) + (dy * dy));

      if (length < 1e-9) {
        length = 1;
      }
      double unitX = dx / length;
      double unitY = dy / length;

      aUpperLeftPointSFCT.move2D(aObjectAnchorPoint);
      int increment = 10;
      aUpperLeftPointSFCT.translate2D(unitX * increment * (3 + aLocationIndex), unitY * increment * (3 + aLocationIndex));
      aUpperLeftPointSFCT.translate2D(-aDimension.getWidth() / 2, -aDimension.getHeight() / 2);

      return 0;
    }

    @Override
    protected boolean isValidLocation(TLspLabelID aLabelID, ILcdPoint aObjectAnchorPoint, Dimension2D aDimension, TLspContext aContext, ILcdPoint aUpperLeftPoint, double aRotation) {
      // The locations do not directly depend on fCenterPoint
      return super.isValidLocation(aLabelID, aObjectAnchorPoint, aDimension, aContext, aUpperLeftPoint, aRotation);
    }

    @Override
    public boolean equals(Object aObject) {
      // The locations do not directly depend on fCenterPoint
      return super.equals(aObject);
    }

    @Override
    public int hashCode() {
      // The locations do not directly depend on fCenterPoint
      return super.hashCode();
    }
  }
}
