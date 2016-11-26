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
package samples.gxy.contour.complexPolygon;

import com.luciad.contour.ILcdContourBuilder;
import com.luciad.contour.TLcdComplexPolygonContourFinder;
import com.luciad.contour.TLcdLonLatComplexPolygonContourBuilder;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.gxy.contour.ContourLevels;
import samples.gxy.contour.RasterMatrixView;

/**
 * Factory to create contours and place them in a model.
 */
public class ContourModelFactory {
  private boolean fDisjoint;
  private ContourLevels fContourLevels;

  /**
   * Create a new contour model factory
   * @param aContourLevels The contour levels to use.
   * @param aDisjoint      Whether overlapping or disjoint complex polygons are desired. Disjoint
   *                       complex polygons are slower to draw because there are up to twice as much
   *                       segments, but can be drawn translucent because there are no parts of
   *                       other complex polygons behind them.
   */
  public ContourModelFactory(ContourLevels aContourLevels, boolean aDisjoint) {
    fContourLevels = aContourLevels;
    fDisjoint = aDisjoint;
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

    ALcdModel model = fDisjoint ?
                      new TLcd2DBoundsIndexedModel() :
                      new TLcdVectorModel(); //when the complex polygons aren't disjoint, it's important that they're drawn in the correct order, or the larger ones will overlap the smaller ones. Therefore a TLcd2DBoundsIndexedModel, which doesn't always preserve order, can't be used.
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    calculateContours(matrixView, fContourLevels.getLevelValues(true), fContourLevels.getSpecialValues(), fDisjoint, aStatusListener, model);

    return model;
  }

  /**
   * Calculate the contours based on the matrix view.
   *
   * @param aMatrixView     The raster.
   * @param aLevelValues    The level values to base the contours on.
   * @param aSpecialValues  The special values which are no height level.
   * @param aDisjoint       Whether overlapping or disjoint complex polygons are desired.
   * @param aStatusListener Progress bar.
   * @param aModelSFCT Model to which the contour shapes are added.
   */
  private static void calculateContours(ILcdMatrixView aMatrixView, double[] aLevelValues, final double[] aSpecialValues, boolean aDisjoint, ILcdStatusListener aStatusListener, ILcdModel aModelSFCT) {

    TLcdComplexPolygonContourFinder contourFinder = new TLcdComplexPolygonContourFinder() {
      @Override
      protected boolean isSpecialValue(double aValue) {
        /*
        The method isSpecialValue must be overridden to be able to use special values. Anything inside the
        range of the special values array is considered a special value in this sample.
        */
        return aSpecialValues.length > 0 && aValue >= aSpecialValues[0] && aValue <= aSpecialValues[aSpecialValues.length - 1];
      }
    };
    TLcdComplexPolygonContourFinder.IntervalMode mode = aDisjoint ?
                                                        TLcdComplexPolygonContourFinder.IntervalMode.INTERVAL : //each complex polygon is the area in which the height is inside an interval, so the result will be disjoint complex polygons
                                                        TLcdComplexPolygonContourFinder.IntervalMode.HIGHER; //each complex polygon is the area in which the height is higher than the value, so the result will be overlapping complex polygons

    //add the status listener to the contour finder to get progress updates
    contourFinder.addStatusListener(aStatusListener);

    /*
    The parameter aSplitComplexPolygon of the TLcdLonLatComplexPolygonContourBuilder is set to true here. This takes
    a large amount of calculation time, larger than the work of the TLcdComplexPolygonContourFinder itself. However,
    it makes painting the complex polygons go faster, because painting multiple smaller complex polygon goes faster
    than painting one large complex polygon.
    */
    ILcdContourBuilder contourBuilder = new TLcdLonLatComplexPolygonContourBuilder(new MyContourBuilderFunction(aModelSFCT), true);
    contourFinder.findContours(contourBuilder, aMatrixView, mode, aLevelValues, aSpecialValues);

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
