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
package samples.tea.gxy.visibility;

import java.awt.Component;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPolygon;
import com.luciad.tea.ALcdTerrainElevationProvider;
import com.luciad.tea.ILcdAltitudeMatrixView;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.TLcdAltitudeDescriptor;
import com.luciad.tea.TLcdAltitudeMatrixViewFactory;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;

import samples.tea.AbstractVisibilityAction;

class GXYVisibilityAction extends AbstractVisibilityAction {

  private ILcdRaster fDTEDRaster;
  private ILcdGeoReference fDTEDRasterReference;

  public GXYVisibilityAction(Component aComponent, ALcdTerrainElevationProvider aTerrainElevationProvider, ILcdLayer aPointLayer, ILcdLayer aPolylineLayer, ILcdLayer aPolygonLayer, ILcdLayer aToPolylineLayer, ILcdLayer aToPolygonLayer) {
    super(aComponent, aTerrainElevationProvider, aPointLayer, aPolylineLayer, aPolygonLayer, aToPolylineLayer, aToPolygonLayer);
  }

  public void setDTEDRaster(ILcdRaster aDTEDRaster) {
    fDTEDRaster = aDTEDRaster;
  }

  public void setDTEDRasterReference(ILcdGeoReference aDTEDRasterReference) {
    fDTEDRasterReference = aDTEDRasterReference;
  }

  @Override
  protected ILcdAltitudeMatrixView createAreaAltitudeMatrixView(TLcdAltitudeMatrixViewFactory aAltitudeMatrixViewFactory, ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, ILcdAltitudeProvider aAltitudeProvider, TLcdAltitudeDescriptor aAltitudeDescriptor) throws TLcdOutOfBoundsException, TLcdNoBoundsException {

    ILcdAltitudeMatrixView matrixViewShape = aAltitudeMatrixViewFactory.createAreaAltitudeMatrixView(
        aPolygon,
        aPolygonReference,
        aAltitudeProvider,        // fixed height above ground
        fDTEDRaster,
        fDTEDRasterReference,
        aAltitudeDescriptor,      // default descriptor
        aPolygonReference         // same as source reference
    );
    return matrixViewShape;
  }

}
