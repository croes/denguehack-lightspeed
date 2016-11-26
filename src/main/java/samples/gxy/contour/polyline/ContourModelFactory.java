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
package samples.gxy.contour.polyline;

import com.luciad.contour.ILcdContourBuilder;
import com.luciad.contour.TLcdLonLatPolylineContourBuilder;
import com.luciad.contour.TLcdPolylineContourFinder;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.contour.ContourLevels;
import samples.gxy.contour.RasterMatrixView;

/**
 * Factory to create contours and place them in a model.
 */
public class ContourModelFactory {
  private ContourLevels fContourLevels;

  /**
   * Create a new contour model factory
   * @param aContourLevels The contour levels to use.
   */
  public ContourModelFactory(ContourLevels aContourLevels) {
    fContourLevels = aContourLevels;
  }

  /**
   * Create the contours and place them in a model, using the given DMED layer as raster height data.
   * @param aDMEDLayer The DMED layer with height data.
   * @param aStatusListener Status listener to get progress updates from the contour finder.
   * @return The model with contours.
   */
  public ILcdModel createModel(ILcdGXYLayer aDMEDLayer, ILcdStatusListener aStatusListener) {

    // the DTED model contains the altitude information.
    ILcdMultilevelRaster dted_multilevel_raster = (ILcdMultilevelRaster) aDMEDLayer.getModel().elements().nextElement();
    // DTED level 0 is raster at index 1
    ILcdRaster raster = dted_multilevel_raster.getRaster(1);
    ILcdMatrixView matrixView = new RasterMatrixView(raster, raster.getBounds());

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    calculateContours(matrixView, fContourLevels.getLevelValues(false), fContourLevels.getSpecialValues(), aStatusListener, model);

    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(model);

    return model;
  }

  /**
   * Calculate the contours based on the matrix view.
   *
   * @param aMatrixView     The raster.
   * @param aLevelValues    The level values to base the contours on.
   * @param aSpecialValues  The special values which are no height level.
   * @param aStatusListener Progress bar.
   * @param aModelSFCT Model to which the contour shapes are added.
   */
  private static void calculateContours(ILcdMatrixView aMatrixView, double[] aLevelValues, final double[] aSpecialValues, ILcdStatusListener aStatusListener, ILcdModel aModelSFCT) {

    TLcdPolylineContourFinder contourFinder = new TLcdPolylineContourFinder() {
      @Override
      protected boolean isSpecialValue(double aValue) {
        /*
        The method isSpecialValue must be overridden to be able to use special values. Anything inside the
        range of the special values array is considered a special value in this sample.
        */
        return aSpecialValues.length > 0 && aValue >= aSpecialValues[0] && aValue <= aSpecialValues[aSpecialValues.length - 1];
      }
    };

    //add the status listener to the contour finder to get progress updates
    contourFinder.addStatusListener(aStatusListener);

    ILcdContourBuilder function = new TLcdLonLatPolylineContourBuilder(new MyContourBuilderFunction(aModelSFCT));
    contourFinder.findContours(function, aMatrixView, aLevelValues, aSpecialValues);

    //remove the status listener from the contour finder again as it is no longer needed
    contourFinder.removeStatusListener(aStatusListener);
  }

  /**
   * Function to use with the contour builder, the contour builder provides all the shapes it creates
   * to this function.
   */
  private static class MyContourBuilderFunction implements ILcdFunction {
    private ILcdModel fModel;

    public MyContourBuilderFunction(ILcdModel aModel) {
      fModel = aModel;
    }

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      fModel.addElement(aObject, ILcdFireEventMode.FIRE_NOW);
      return true;
    }
  }

}
