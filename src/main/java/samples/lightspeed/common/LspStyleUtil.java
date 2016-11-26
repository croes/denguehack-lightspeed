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
package samples.lightspeed.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdPolypoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.ILcdSurface;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

public class LspStyleUtil {

  public static <S extends ALspStyle> S getCustomizableStyle(ILspStyler aStyler, Class<S> aClass) {
    if (aStyler instanceof ILspCustomizableStyler) {
      for (TLspCustomizableStyle style : ((ILspCustomizableStyler) aStyler).getStyles()) {
        if (aClass.isInstance(style.getStyle())) {
          return (S) style.getStyle();
        }
      }
    }
    throw new IllegalArgumentException("Could not find style of class " + aClass + " in styler " + aStyler);
  }

  public static <S extends ALspStyle> void setCustomizableStyle(ILspStyler aStyler, S aStyle) {
    if (aStyler instanceof ILspCustomizableStyler) {
      for (TLspCustomizableStyle style : ((ILspCustomizableStyler) aStyler).getStyles()) {
        if (style.getStyle().getClass().isInstance(aStyle)) {
          style.setStyle(aStyle);
          return;
        }
      }
    }
    throw new IllegalArgumentException("Could not find style of class " + aStyle.getClass() + " in styler " + aStyler);
  }

  public static ALspStyler combinePointLineAndFill(ALspStyle aPointStyle, List<? extends ALspStyle> aLineAndFillStyled) {
    Map<ALspStyleTargetProvider, List<? extends ALspStyle>> map = new HashMap<>();
    // use the point style for point and multi-point objects
    map.put(new PointStyleTargetProvider(), Collections.singletonList(aPointStyle));
    // use the line and fill styles for all other objects
    map.put(null, aLineAndFillStyled);
    return new TLspStyler(map);
  }

  /**
   * Tests whether a shape is equivalent to a multipoint (a disconnected point-cloud).
   * Note that some shape implementations which are not point-clouds (such as TLcdLonLatPoint), also implement ILcdPolypoint.
   * @param aShape the shape to test
   * @return whether the shape is a "multi-point"
   */
  public static boolean isSimpleFeaturePolypoint(ILcdShape aShape) {
    return aShape instanceof ILcdPolypoint && !(aShape instanceof ILcdPolygon ||
                                                aShape instanceof ILcdPolyline ||
                                                aShape instanceof ILcdCurve ||
                                                aShape instanceof ILcdSurface);
  }

  public static final class PointStyleTargetProvider extends ALspStyleTargetProvider {

    private final ALspStyleTargetProvider fStyleTargetProvider;

    public PointStyleTargetProvider() {
      this(null);
    }

    public PointStyleTargetProvider(ALspStyleTargetProvider aStyleTargetProvider) {
      fStyleTargetProvider = aStyleTargetProvider;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (fStyleTargetProvider != null) {
        List<Object> objects = new ArrayList<>();
        fStyleTargetProvider.getStyleTargetsSFCT(aObject, aContext, objects);
        for (Object object : objects) {
          extractPoints(ALcdShape.fromDomainObject(object), aResultSFCT);
        }
        objects.clear();
      } else {
        ILcdShape shape = ALcdShape.fromDomainObject(aObject);
        extractPoints(shape, aResultSFCT);
      }
    }

    private void extractPoints(Object aObject, List<Object> aResultSFCT) {
      if (aObject instanceof ILcdPoint) {
        aResultSFCT.add(aObject);
      } else if (aObject instanceof ILcdShapeList) {
        ILcdShapeList shapeList = (ILcdShapeList) aObject;
        for (int i = 0; i < shapeList.getShapeCount(); i++) {
          extractPoints(shapeList.getShape(i), aResultSFCT);
        }
      } else if (aObject instanceof ILcdShape && isSimpleFeaturePolypoint((ILcdShape) aObject)) {
        //points and multipoints - by default - are styled as icons.
        aResultSFCT.add(aObject);
      }
    }

    @Override
    public void getEditTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      // The provider is used to make sure we don't apply icon styling to non-point objects.
      // Editing should still happen on the parent shape object.
      aResultSFCT.add(ALcdShape.fromDomainObject(aObject));
    }
  }
}
