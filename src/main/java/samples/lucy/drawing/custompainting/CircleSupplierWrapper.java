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
package samples.lucy.drawing.custompainting;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.format.ALcyShapeSupplier;
import com.luciad.lucy.addons.drawing.format.ALcyShapeSupplierWrapper;
import com.luciad.lucy.addons.drawing.format.TLcyShapePainterProviderContainer;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.gxy.painter.TLcdGXYCirclePainter;

/**
 * Wraps a shape supplier to replace the painter provider container of the circle painter.
 * It does this to display the radius of a circle while creating it.
 */
class CircleSupplierWrapper extends ALcyShapeSupplierWrapper {
  private final ILcyLucyEnv fLucyEnv;

  public CircleSupplierWrapper(ALcyShapeSupplier aShapeSupplier, ILcyLucyEnv aLucyEnv) {
    super(aShapeSupplier);
    fLucyEnv = aLucyEnv;
  }

  @Override
  public TLcyShapePainterProviderContainer createShapePainterProviderContainer() {
    // General setup of the circle painter
    TLcdGXYCirclePainter strokePainter = createCirclePainter();
    return createCirclePainterProviderContainer(strokePainter);

  }

  /**
   * This method creates all required painter providers and editor providers for the given circle
   * painter.
   * @param aPainter The painter to create a TLcyShapePainterProviderContainer for.
   * @return The created TLcyShapePainterProviderContainer.
   */
  private TLcyShapePainterProviderContainer createCirclePainterProviderContainer(TLcdGXYCirclePainter aPainter) {
    // Define the painter for the fill. Make sure it doesn't have any fill/line styles, as SLD takes
    // care of that.
    aPainter.setMode(ALcdGXYAreaPainter.FILLED);
    aPainter.setFillStyle(TLcyShapePainterProviderContainer.EMPTY_STYLE);
    aPainter.setLineStyle(TLcyShapePainterProviderContainer.EMPTY_STYLE);

    // Define the painter for the outlines. Make sure it doesn't have any fill/line styles, as SLD
    // takes care of that (except for the selection style).
    TLcdGXYCirclePainter outlinePainter = (TLcdGXYCirclePainter) aPainter.clone();
    outlinePainter.setMode(ALcdGXYAreaPainter.OUTLINED);
    ILcdGXYPainterStyle selectionLineStyle = TLcyShapePainterProviderContainer.createSelectionLineStyle(getProperties());
    outlinePainter.setLineStyle(selectionLineStyle);
    outlinePainter.setFillStyle(TLcyShapePainterProviderContainer.EMPTY_STYLE);

    // Using null for point, text and raster as that isn't applicable here.
    return new TLcyShapePainterProviderContainer(outlinePainter, aPainter, null, null, null,
                                                 outlinePainter, aPainter, null, null, null);
  }

  /**
   * Create the circle painter, with the desired customizations.
   * @return The created circle painter.
   */
  private TLcdGXYCirclePainter createCirclePainter() {
    // General setup of the circle painter
    TLcdGXYCirclePainter circlePainter = new TLcdGXYCirclePainter();

    //Set-up the painter to our likings
    circlePainter.setSnapToInvisiblePoints(true);
    circlePainter.setEditingLabel(true);
    circlePainter.setPointFormat(fLucyEnv.getDefaultLonLatPointFormat());
    return circlePainter;
  }
}
