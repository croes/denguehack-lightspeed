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
package samples.gxy.density;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.IndexColorModel;

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGridReference;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYDensityLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;

import samples.gxy.decoder.MapSupport;

/**
 * This ILcdGXYLayerFactory creates layers for models that contain polylines.
 * The densities of the polylines are displayed by means of color coding,
 * ranging from blue to red.
 */
public class DensityLayerFactory
    implements ILcdGXYLayerFactory {

  // Implementation for ILcdGXYLayerFactory.

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    // Create a layer for the model containing polylines.
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(false);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setLineStyle(new MyDensityLineStyle());
    painter.setAntiAliased(false);

    layer.setGXYPainterProvider(painter);

    // Set up a color model factory for creating a range of jet colors.
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBits(8);
    factory.setSize(256);
    factory.setBasicColor(0, new Color(0, 0, 0, 0));
    factory.setBasicColor(1, Color.blue);
    factory.setBasicColor(10, Color.cyan);
    factory.setBasicColor(20, Color.yellow);
    factory.setBasicColor(30, Color.orange);
    factory.setBasicColor(255, Color.red);

    // Create the color model.
    IndexColorModel colorModel = (IndexColorModel) factory.createColorModel();

    // Create the density layer that wraps the above layer.
    ILcdGXYLayer densityLayer = new TLcdGXYDensityLayer(layer, colorModel);

    return densityLayer;
  }

  /**
   * This ILcdGXYPainterStyle sets a line stroke whose width is proportional
   * to the scale of the view.
   */
  private static class MyDensityLineStyle
      implements ILcdGXYPainterStyle {

    // Implementation for ILcdGXYPainterStyle.

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D g2d = (Graphics2D) aGraphics;

      // Set the stroke.
      double scale = scaleInMeters(aGXYContext.getGXYView());
      g2d.setStroke(new BasicStroke((float) (50000.0 * scale),
                                    BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND));

      // Note that we don't have to set a color, since the colors are determined
      // by the density layer.
    }

    private double scaleInMeters(ILcdGXYView aView) {
      double scale = aView.getScale();
      if (aView.getXYWorldReference() instanceof ILcdGridReference) {
        ILcdGridReference gridReference = (ILcdGridReference) aView.getXYWorldReference();
        scale /= gridReference.getUnitOfMeasure();
      }
      return scale;
    }
  }
}
