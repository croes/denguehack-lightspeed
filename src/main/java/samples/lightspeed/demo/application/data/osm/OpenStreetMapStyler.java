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
package samples.lightspeed.demo.application.data.osm;

import static com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider.*;

import static samples.lightspeed.demo.application.data.osm.OpenStreetMapLayerFactory.GeometryType.*;

import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * @since 2012.1
 */
public class OpenStreetMapStyler extends ALspStyler {

  private static final double FIXED_WIDTH_SCALE = 0.1;

  private final OpenStreetMapStyleProvider fStyleProvider;
  private final OpenStreetMapLayerFactory.GeometryType fGeometryType;

  public OpenStreetMapStyler(OpenStreetMapStyleProvider aStyleProvider, OpenStreetMapLayerFactory.GeometryType aGeometryType) {
    fStyleProvider = aStyleProvider;
    fGeometryType = aGeometryType;
  }

  // implementations for ILspStyler

  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    boolean useFixedWidths = useFixedWidths(aContext);
    for (Object object : aObjects) {
      if (object instanceof ILcdDataObject) {
        ILcdDataObject dataObject = (ILcdDataObject) object;
        if (fGeometryType == point) {
          pointStyle(aStyleCollector, dataObject);
        } else if (fGeometryType == line) {
          lineStyle(aStyleCollector, dataObject, useFixedWidths);
        } else if (fGeometryType == area) {
          areaStyle(aStyleCollector, dataObject, useFixedWidths);
        }
      }
    }
  }

  private void pointStyle(ALspStyleCollector aStyleCollector, ILcdDataObject aDataObject) {
    ALspStyle[] styles = fStyleProvider.getIcon(aDataObject);
    if (styles != null && styles.length > 0) {
      submitStyle(aStyleCollector, aDataObject, POINT, styles);
    }
  }

  private void lineStyle(ALspStyleCollector aStyleCollector, ILcdDataObject aDataObject, boolean aUseFixedWidths) {
    ALspStyle[] styles = fStyleProvider.getLineStyle(aDataObject);
    if (aUseFixedWidths) {
      styles = fStyleProvider.convertToFixedWidthLineStyle(styles);
    }
    submitStyle(aStyleCollector, aDataObject, LINE, styles);
  }

  private void areaStyle(ALspStyleCollector aStyleCollector, ILcdDataObject aDataObject, boolean aUseFixedWidths) {
    ALspStyle[] outlineStyle = fStyleProvider.getOutlineStyle(aDataObject);
    if (aUseFixedWidths) {
      outlineStyle = fStyleProvider.convertToFixedWidthLineStyle(outlineStyle);
    }
    submitStyle(aStyleCollector, aDataObject, AREA_OUTLINE, outlineStyle);
    ALspStyle fillStyle = fStyleProvider.getFillStyle(aDataObject);
    submitStyle(aStyleCollector, aDataObject, AREA_FILL, fillStyle);
  }

  private boolean useFixedWidths(TLspContext aContext) {
    ALspViewXYZWorldTransformation v2w = aContext.getViewXYZWorldTransformation();
    double scale = v2w.getScale() / v2w.getFeatureScale();
    return scale < FIXED_WIDTH_SCALE;
  }

  private void submitStyle(ALspStyleCollector aStyleCollector, ILcdDataObject aDataObject,
                           ALspStyleTargetProvider aStyleTargetProvider, ALspStyle... aStyles) {
    aStyleCollector.object(aDataObject);
    aStyleCollector.geometry(aStyleTargetProvider);
    aStyleCollector.styles(aStyles);
    aStyleCollector.submit();
  }
}
