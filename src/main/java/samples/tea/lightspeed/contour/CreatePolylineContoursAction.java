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
package samples.tea.lightspeed.contour;

import com.luciad.contour.ILcdContourBuilder;
import com.luciad.contour.TLcdLonLatPolylineContourBuilder;
import com.luciad.contour.TLcdPolylineContourFinder;
import com.luciad.contour.TLcdXYPolylineContourBuilder;
import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.view.lightspeed.layer.ILspLayer;

class CreatePolylineContoursAction extends AbstractCreateContoursAction {

  private final TLcdPolylineContourFinder fContourFinder;
  private final PolylineContourLayerFactory fLayerFactory = new PolylineContourLayerFactory();

  public CreatePolylineContoursAction() {
    fContourFinder = new TLcdPolylineContourFinder() {
      @Override
      protected boolean isSpecialValue(double aValue) {
        //Use values smaller than or equal to return aValue <= TLcdDTEDTileDecoder.UNKNOWN_ELEVATION + 10 as special values, such as "unknown"
        return aValue <= TLcdDTEDTileDecoder.UNKNOWN_ELEVATION + 10;
      }
    };
    setStatusSource(fContourFinder);
  }

  ILspLayer createContourLayer() {
    if (isInitialized()) {
      // make the bounds somewhat bigger.
      TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel(f2DEditableBounds);
      model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
      model.setModelDescriptor(new TLcdModelDescriptor("", "", "Polyline contours"));

      ILcdContourBuilder contourBuilder;
      if (fBounds instanceof TLcdLonLatBounds || fBounds instanceof TLcdLonLatHeightBounds) {
        contourBuilder = new TLcdLonLatPolylineContourBuilder(new BuilderFunction(model));
      } else {
        contourBuilder = new TLcdXYPolylineContourBuilder(new BuilderFunction(model));
      }
      fContourFinder.findContours(contourBuilder, fImageMatrixView, ContourLevels.getContourLevelsPolyline(), ContourLevels.getContourLevelsSpecial());

      return fLayerFactory.createLayer(model);
    }
    return null;
  }

}
