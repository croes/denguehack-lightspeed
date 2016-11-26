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
package samples.wms.server.config.xml;

import java.io.IOException;
import java.util.List;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.wms.server.ILcdModelProvider;
import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * Utility class for calculating the bounds of WMS layers/models.
 */
public final class WMSBoundsUtility {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(WMSBoundsUtility.class.getName());

  private static final ILcdBounds DEFAULT_WGS84_BOUNDS = new TLcdLonLatBounds(-180, -90, 360, 180);

  private WMSBoundsUtility() {
    // No need to instantiate this class, as all utility methods are static.
  }

  /**
   * Calculate the bounding box of a WMS layer with children.
   */
  public static ILcdBounds calculateWGS84Bounds(List aWMSLayers) {
    ILcd2DEditableBounds wgs84Bounds = null;
    for (Object wmsLayerObject : aWMSLayers) {
      ALcdWMSLayer wmsLayer = (ALcdWMSLayer) wmsLayerObject;
      ILcdBounds childWgs84LonLatBounds = wmsLayer.getWGS84LonLatBounds();
      if (childWgs84LonLatBounds != null) {
        if (wgs84Bounds == null) {
          wgs84Bounds = childWgs84LonLatBounds.cloneAs2DEditableBounds();
        } else {
          wgs84Bounds.setTo2DUnion(childWgs84LonLatBounds);
        }
      }
    }
    return wgs84Bounds;
  }

  /**
   * Calculates the bounding box of the source in WGS84.
   * If they can not be determined, the default is returned (the whole earth).
   */
  public static ILcdBounds calculateWGS84Bounds(String aFullSourceFilename, ILcdModelProvider aModelProvider) {
    try {
      if (aModelProvider != null) {
        ILcdModel model = aModelProvider.getModel(aFullSourceFilename, null);
        if (model instanceof ILcd2DBoundsIndexedModel) {
          ILcdBounds modelBounds = ((ILcd2DBoundsIndexedModel) model).getBounds();
          if (modelBounds != null) {
            if (model.getModelReference() instanceof ILcdGeodeticReference) {
              return modelBounds.cloneAs2DEditableBounds();
            }
            if (model.getModelReference() instanceof ILcdGridReference) {
              try {
                TLcdGeodetic2Grid geodetic2grid = new TLcdGeodetic2Grid();
                geodetic2grid.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
                geodetic2grid.setXYWorldReference((ILcdXYWorldReference) model.getModelReference());
                TLcdLonLatHeightBounds wgs84Bounds = new TLcdLonLatHeightBounds();
                geodetic2grid.worldBounds2modelSFCT(modelBounds, wgs84Bounds);
                return wgs84Bounds.cloneAs2DEditableBounds();
              } catch (TLcdOutOfBoundsException e) {
                sLogger.trace(e.getMessage());
              }
            }
          }
        }
      } else {
        sLogger.trace("ILcdModelProvider in WMSCapabilitiesXMLDecoder is null. Bounding box of " + aFullSourceFilename + " could not be retrieved from corresponding model.");
      }
    } catch (IOException e) {
      sLogger.trace(e.getMessage());
    }
    return DEFAULT_WGS84_BOUNDS;
  }

}
