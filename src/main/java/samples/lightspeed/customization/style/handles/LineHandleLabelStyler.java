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
package samples.lightspeed.customization.style.handles;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.editor.handle.ALspEditHandle;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;

/**
 * <p>A custom styler for <code>ALspEditHandle</code> instances that contain
 * line-based visual components. This styler will generate a label containing the
 * length of the line, and place it in orientation of the line.</p>
 */
public class LineHandleLabelStyler extends ALspLabelStyler {

  private Color fColor = Color.white;
  private Color fHaloColor = Color.black;
  private Font fFont = Font.decode("Default-BOLD-12");

  public LineHandleLabelStyler(Color aColor, Color aHaloColor) {
    fColor = aColor;
    fHaloColor = aHaloColor;
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      if (object instanceof ALspEditHandle) {
        ALspEditHandle handle = (ALspEditHandle) object;
        List<ALspStyleTargetProvider> targetProviders = handle.getStyleTargetProviders(TLspHandleGeometryType.REGULAR_LINE);
        if (targetProviders.isEmpty()) {
          continue;
        }
        int index = 0;
        for (ALspStyleTargetProvider geometryProvider : targetProviders) {
          ArrayList<Object> elements = new ArrayList<Object>();
          geometryProvider.getStyleTargetsSFCT(handle.getGeometry(), aContext, elements);
          TLspTextStyle textStyle = TLspTextStyle.newBuilder().font(fFont).textColor(fColor).haloColor(fHaloColor).build();
          for (Object geometry : elements) {
            if (geometry instanceof ILcdPolyline) {
              ILcdPolyline line = (ILcdPolyline) geometry;
              aStyleCollector.object(handle)
                  .label(index++)
                      // Use the line shape as the geometry to place the label along the line.
                  .geometry(line)
                  .styles(textStyle, new PolylineDistanceTextProviderStyle(line))
                  .submit();
            }
          }
        }
      }
    }
  }

  private static class PolylineDistanceTextProviderStyle extends ALspLabelTextProviderStyle {
    private final Object fGeometry;

    public PolylineDistanceTextProviderStyle(ILcdPolyline aGeometry) {
      fGeometry = aGeometry;
    }

    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      ILcdPolyline polyline = (ILcdPolyline) fGeometry;
      ILcdPoint firstPoint = polyline.getPoint(0);
      ILcdPoint lastPoint = polyline.getPoint(polyline.getPointCount() - 1);
      if (aContext.getModelReference() instanceof TLcdGeodeticReference) {
        double distance = ((TLcdGeodeticReference) aContext.getModelReference()).getGeodeticDatum().getEllipsoid().geodesicDistance(firstPoint, lastPoint);
        DecimalFormat df = new DecimalFormat("#.##");
        return new String[]{df.format(distance) + " m"};
      } else {
        throw new IllegalArgumentException("This style provider only supports TLcdGeodeticReference");
      }
    }
  }
}
