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
package samples.tea.lightspeed.visibility;

import java.awt.Color;
import java.util.Collection;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.shape2D.TLcdLonLatLine;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.tea.ILcdVisibilityMatrixView;
import com.luciad.tea.TLcdVisibilityDescriptor;
import com.luciad.tea.TLcdVisibilityInterpretation;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * The styler used to style to-polyline intervisibility results.
 *
 * The discretized polyline (an Mx1 ILcdVisibilityMatrixView) is visualized
 * by visualizing a series of connected (vector) polylines.
 *
 * Between two consecutive points of the discretized polyline
 * 1 or 2 colored line segments are shown.
 *
 * If the points have the same visibility value, a single line segment in the corresponding color is visualized.
 * If the points have different visibility values, the line segment is split into two line segments.
 * Each segment is visualized with the corresponding color.
 */
class PolylineVisibilityStyler extends ALspStyler {

  private static final TLspLineStyle LINE_STYLE_INVISIBLE = TLspLineStyle.newBuilder().color(Color.RED).width(2).build();
  private static final TLspLineStyle LINE_STYLE_UNCERTAIN = TLspLineStyle.newBuilder().color(Color.ORANGE).width(2).build();
  private static final TLspLineStyle LINE_STYLE_VISIBLE = TLspLineStyle.newBuilder().color(Color.GREEN).width(2).build();

  private transient TLcdLonLatPoint fTempLonLatPoint1 = new TLcdLonLatPoint();
  private transient TLcdLonLatPoint fTempLonLatPoint2 = new TLcdLonLatPoint();
  private transient TLcdLonLatPoint fTempLonLatPoint3 = new TLcdLonLatPoint();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      if (object instanceof ILcdVisibilityMatrixView) {
        ILcdVisibilityMatrixView visibilityMatrixView = (ILcdVisibilityMatrixView) object;

        if (!(visibilityMatrixView.getRowCount() < 2)) {
          double value1 = visibilityMatrixView.getValue(0, 0);
          double pointX = visibilityMatrixView.retrieveAssociatedPointX(0, 0);
          double pointY = visibilityMatrixView.retrieveAssociatedPointY(0, 0);
          fTempLonLatPoint1.move2D(pointX, pointY);

          for (int i = 1; i < visibilityMatrixView.getRowCount(); i++) {
            pointX = visibilityMatrixView.retrieveAssociatedPointX(0, i);
            pointY = visibilityMatrixView.retrieveAssociatedPointY(0, i);
            fTempLonLatPoint2.move2D(pointX, pointY);

            double value2 = visibilityMatrixView.getValue(0, i);
            if (value2 == value1) {
              // Visibility value not changed.
              // Paint line with line style of the value of the second point.
              TLcdLonLatLine line = new TLcdLonLatLine(fTempLonLatPoint1.cloneAs2DEditablePoint(), fTempLonLatPoint2.cloneAs2DEditablePoint());
              aStyleCollector.object(line)
                             .geometry(line)
                             .style(findLineStyle(visibilityMatrixView, value2))
                             .submit();
            } else {
              // Visibility value changed, therefore compute the point in the
              // middle of the line between the first and the second point.
              final ILcdEllipsoid ellipsoid = visibilityMatrixView.getReference().getGeodeticDatum().getEllipsoid();
              ellipsoid.geodesicPointSFCT(fTempLonLatPoint1, fTempLonLatPoint2, 0.5, fTempLonLatPoint3);

              // Paint line fTempLonLatPoint1 and fTempLonLatPoint3 with line style of the value of the first point.
              TLcdLonLatLine firstLineHalf = new TLcdLonLatLine(fTempLonLatPoint1.cloneAs2DEditablePoint(), fTempLonLatPoint3.cloneAs2DEditablePoint());
              aStyleCollector.object(firstLineHalf)
                             .geometry(firstLineHalf)
                             .style(findLineStyle(visibilityMatrixView, value1))
                             .submit();

              // Paint line fTempLonLatPoint1 and fTempLonLatPoint3 with line style of the value of the second point.
              TLcdLonLatLine secondLineHalf = new TLcdLonLatLine(fTempLonLatPoint3.cloneAs2DEditablePoint(), fTempLonLatPoint2.cloneAs2DEditablePoint());
              aStyleCollector.object(secondLineHalf)
                             .geometry(secondLineHalf)
                             .style(findLineStyle(visibilityMatrixView, value2))
                             .submit();
            }

            // Cache current values.
            fTempLonLatPoint1.move2D(fTempLonLatPoint2);
            value1 = value2;
          }
        }
      }
    }
  }

  /**
   * Finds the line style for the given visibility value.
   * @param aVisibilityValue The visibility value to query.
   * @return the line style for the given visibility value.
   */
  private TLspLineStyle findLineStyle(ILcdVisibilityMatrixView aVisibilityMatrixView, double aVisibilityValue) {
    TLcdVisibilityDescriptor descriptor = aVisibilityMatrixView.getVisibilityDescriptor();
    if (descriptor.isSpecialValue(aVisibilityValue)) {
      TLcdVisibilityInterpretation interpretation = descriptor.getSpecialValueInterpretation(aVisibilityValue);
      if (interpretation == TLcdVisibilityInterpretation.INVISIBLE) {
        return LINE_STYLE_INVISIBLE;
      }
      if (interpretation == TLcdVisibilityInterpretation.UNCERTAIN) {
        return LINE_STYLE_UNCERTAIN;
      }
      if (interpretation == TLcdVisibilityInterpretation.VISIBLE) {
        return LINE_STYLE_VISIBLE;
      }
    }
    throw new IllegalArgumentException("Invalid visibility value found!");
  }
}
