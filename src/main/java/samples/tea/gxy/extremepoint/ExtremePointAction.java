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
package samples.tea.gxy.extremepoint;

import java.awt.Component;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdShape;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdReferencedMatrixView;
import com.luciad.tea.TLcdAltitudeDescriptor;
import com.luciad.tea.TLcdAltitudeMatrixViewFactory;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.tea.AbstractExtremePointAction;

/**
 * A sample action which computes the highest or lowest N points that lie inside a polygon.
 */
class ExtremePointAction extends AbstractExtremePointAction {

  private ILcdRaster           fRaster;
  private ILcdGeoReference     fRasterReference;

  /**
   * @param aParentComponent  Is required only for parenting the progress dialog.
   * @param aShape            The shape within which to search for highest points.
   * @param aShapeReference   The reference of the shape.
   * @param aAltitudeProvider The altitude provider for the given shape.
   */
  public ExtremePointAction( Component            aParentComponent,
                             ILcdShape            aShape,
                             ILcdGeoReference     aShapeReference,
                             ILcdAltitudeProvider aAltitudeProvider ) {
    super(aParentComponent, aShape, aShapeReference, aAltitudeProvider);
  }

  @Override
  protected ILcdReferencedMatrixView createAreaAltitudeMatrixView(TLcdAltitudeMatrixViewFactory aFactory) throws TLcdOutOfBoundsException, TLcdNoBoundsException {
    return aFactory.createAreaAltitudeMatrixView(
        getShape(),
        getShapeReference(),
        getAltitudeProvider(),
        fRaster,
        fRasterReference,
        TLcdAltitudeDescriptor.getDefaultInstance(),
        fRasterReference
    );
  }

  public ILcdRaster getRaster() {
    return fRaster;
  }

  public void setRaster( ILcdRaster aRaster ) {
    fRaster = aRaster;
    fRecomputeMatrix = true;
  }

  public ILcdGeoReference getRasterReference() {
    return fRasterReference;
  }

  public void setRasterReference( ILcdGeoReference aRasterReference ) {
    fRasterReference = aRasterReference;
    fRecomputeMatrix = true;
  }

}
