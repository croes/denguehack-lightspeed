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
package samples.gxy.statisticalPainter;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdSingleGXYPainterProvider;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;

/**
 * This class implements an ILcdGXYPainter to paint polygons filled with a grey
 * color gradient depending on the population density computed by
 * PopulationUtil.
 */
class PopulationDensityPainter extends TLcdGXYShapeListPainter implements ILcdGXYPainter {
  /*
  * We want the PopulationDensityPainter to behave exactly like TLcdGXYShapeListPainter
  * with TLcdGXYPointListPainter for individual shapes, except that we want
  * the area to be filled according to the density of one data object over another data object.
  * We want bounds etc. to remain the default behaviour.
  * Therefore we set the ILcdGXYPainterProvider of this PopulationDensityPainter to
  * return a TLcdGXYPointListPainter, but we define the paint method of
  * this PopulationDensityPainter ourselves.
  * An alternative is to extend a TLcdGXYAreaShapeListPainter, and to
  * override the paint method by first setting the color, and then
  * calling the original paint method (see also PopulationChangePainter)
  */

  TLcdGXYPointListPainter fAreaPainter;
  TLcdGXYPainterColorStyle fFillStyle;

  public PopulationDensityPainter() {

    // Each TLcdGXYShapeListPainter must have an ILcdGXYPainterProvider
    fAreaPainter =
        new TLcdGXYPointListPainter(TLcdGXYPointListPainter.OUTLINED_FILLED);
    fFillStyle = new TLcdGXYPainterColorStyle();
    fAreaPainter.setFillStyle(fFillStyle);

    ILcdGXYPainterProvider shape_list_painter_provider =
        new TLcdSingleGXYPainterProvider(fAreaPainter);

    this.setShapeGXYPainterProvider(shape_list_painter_provider);
  }

  // the paint method is is the principal implementation for this painter:
  // it paints all objects of the featured shape in the color corresponding
  // to the density of the object (calculated in densityColorForObject)
  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdDataObject dataObjectShapeList =
        (ILcdDataObject) getObject();
    Color object_color =
        colorForFactor(PopulationUtil.getDensity(
            dataObjectShapeList));
    fFillStyle.setDefaultColor(object_color);
    fFillStyle.setSelectionColor(object_color);
    super.paint(aGraphics, aMode, aGXYContext);
  }

  public static Color colorForFactor(double aFactor) {
    int gray = 55 + (200 - (int) (200 * (aFactor * aFactor)));

    int rgb = 0xff000000 |
              ((gray << 16) & 0x00ff0000) |
              ((gray << 8) & 0x0000ff00) |
              (gray & 0x000000ff);
    return new Color(rgb);
  }

}

