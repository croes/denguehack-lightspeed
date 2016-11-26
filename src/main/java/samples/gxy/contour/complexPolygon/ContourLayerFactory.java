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

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.contour.ContourLevels;

/**
 * Layer factory to create the layer for a contour model.
 */
public class ContourLayerFactory implements ILcdGXYLayerFactory {
  private boolean fDisjoint;
  private ContourLevels fContourLevels;

  /**
   * Create a new contour layer factory
   * @param aContourLevels The contour levels to use.
   * @param aDisjoint      Whether overlapping or disjoint complex polygons were created. Disjoint
   *                       complex polygons are slower to draw because there are up to twice as much
   *                       segments, but can be drawn translucent because there are no parts of
   *                       other complex polygons behind them.
   */
  public ContourLayerFactory(ContourLevels aContourLevels, boolean aDisjoint) {
    fContourLevels = aContourLevels;
    fDisjoint = aDisjoint;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);

    //get the levels and colors that the painter should use
    double[] levelValues = fContourLevels.getLevelValues(true);
    Color[] levelColors = fContourLevels.getLevelColors(true);
    double[] specialValues = fContourLevels.getSpecialValues();
    Color[] specialColors = fContourLevels.getSpecialColors();

    //To demonstrate the disjoint complex polygons, draw them translucent in this sample
    if (fDisjoint) {
      levelColors = getTransparentColorArray(levelColors, 128);
      specialColors = getTransparentColorArray(specialColors, 128);
    }

    //create the painter for complex polygon contours
    TLcdGXYPointListPainter painter = new ComplexPolygonContourPainter(levelValues,
                                                                       levelColors,
                                                                       specialValues,
                                                                       specialColors);
    layer.setGXYPainterProvider(painter);

    layer.setLabel("Contours");

    return layer;
  }

  /**
   * Utility method to create translucent colors from the given colors.
   * @param aColors The input colors.
   * @param aAlpha The alpha value for all output colors.
   * @return An array with the same colors as the input colors, except the alpha of each color is changed to aAlpha.
   */
  private Color[] getTransparentColorArray(Color[] aColors, int aAlpha) {
    Color[] result = new Color[aColors.length];
    for (int i = 0; i < aColors.length; i++) {
      result[i] = new Color(aColors[i].getRed(), aColors[i].getGreen(), aColors[i].getBlue(), aAlpha);
    }
    return result;
  }
}
