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
package samples.lucy.cop.addons.missioncontroltheme;

import static com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke.*;

import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ELcdHorizontalAlignment;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * {@link com.luciad.view.lightspeed.style.styler.ILspStyler ILspStyler} implementation for the annotation layer
 */
final class AnnotationsStyler extends ALspStyler {

  private final TLspLineStyle fLineStyle = TLspLineStyle.newBuilder().build();
  private final TLspFillStyle fFillStyle = TLspFillStyle.newBuilder().build();
  private final TLspComplexStrokedLineStyle fArrowStyle = TLspComplexStrokedLineStyle.newBuilder()
                                                                                     .fallback(line().lengthRelative(1).lineColor(fLineStyle.getColor()).build())
                                                                                     .decoration(
                                                                                         1f,
                                                                                         ELcdHorizontalAlignment.RIGHT,
                                                                                         combineWithFallbackStroking(arrow().size(20f).lineColor(fLineStyle.getColor()).fillColor(fLineStyle.getColor()).type(ArrowBuilder.ArrowType.REGULAR_FILLED).build())
                                                                                                )
                                                                                     .build();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object domainObject : aObjects) {
      if (domainObject instanceof GeoJsonRestModelElement) {
        styleSingleShape(domainObject, ((GeoJsonRestModelElement) domainObject).getShape(0), aStyleCollector, aContext);
      }
    }
  }

  private void styleSingleShape(Object aDomainObject, ILcdShape aShape, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (aShape instanceof ILcdPolygon) {
      stylePolygon(aDomainObject, aShape, aStyleCollector, aContext);
    } else if (aShape instanceof ILcdPolyline) {
      stylePolyline(aDomainObject, aShape, aStyleCollector, aContext);
    }
  }

  private void stylePolygon(Object aDomainObject, ILcdShape aPolygon, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.object(aDomainObject).styles(fLineStyle, fFillStyle).submit();
  }

  private void stylePolyline(Object aDomainObject, ILcdShape aPolyline, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (aDomainObject instanceof ILcdDataObject &&
        AnnotationModel.ARROW_LINE_CAP_INDICATION.equals(((ILcdDataObject) aDomainObject).getValue(AnnotationModel.LINE_CAP_DECORATION_PROPERTY))) {
      aStyleCollector.object(aDomainObject).styles(fArrowStyle).submit();
    } else {
      aStyleCollector.object(aDomainObject).style(fLineStyle).submit();
    }
  }
}
