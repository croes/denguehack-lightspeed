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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYCurvedPathLabelLocation;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.contour.ContourLevels;

/**
 * Layer factory to create the layer for a contour model.
 */
public class ContourLayerFactory implements ILcdGXYLayerFactory {
  private ContourLevels fContourLevels;
  private boolean fStrokeStyle;

  /**
   * Create a new contour layer factory
   * @param aContourLevels The contour levels to use.
   * @param aStrokeStyle   whether or not to use the complex stroke style painter
   */
  public ContourLayerFactory(ContourLevels aContourLevels, boolean aStrokeStyle) {
    fContourLevels = aContourLevels;
    fStrokeStyle = aStrokeStyle;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    layer.setLabel("Contours");

    //create an array of darker colors than those from the ContourLevels utility, to be able to distinguish the contour lines from the background
    Color[] level_colors_darker = new Color[fContourLevels.getLevelColors(false).length];
    for (int i = 0; i < level_colors_darker.length; i++) {
      level_colors_darker[i] = fContourLevels.getLevelColors(false)[i].darker().darker().darker();
    }

    if (fStrokeStyle) {
      //create the painter with complex stroke style
      ILcdGXYPainterProvider painter = new StrokePolylineContourPainter(fContourLevels.getLevelValues(false),
                                                                        level_colors_darker,
                                                                        fContourLevels.getLevelLabels(false),
                                                                        fContourLevels.getSpecialValues(),
                                                                        fContourLevels.getSpecialColors(),
                                                                        fContourLevels.getSpecialLabels());
      layer.setGXYPainterProvider(painter);

    } else {
      //create the polyline contour painter
      ILcdGXYPainterProvider painter = new PolylineContourPainter(fContourLevels.getLevelValues(false),
                                                                  level_colors_darker,
                                                                  fContourLevels.getSpecialValues(),
                                                                  fContourLevels.getSpecialColors());
      layer.setGXYPainterProvider(painter);

      layer.setLabelLocations(new TLcdLabelLocations(layer, new TLcdGXYCurvedPathLabelLocation()));
      //create the label painter for polyline contours
      PolylineContourLabelPainter labelPainter = new PolylineContourLabelPainter(fContourLevels.getLevelValues(false),
                                                                                 fContourLevels.getLevelColors(false),
                                                                                 fContourLevels.getLevelLabels(false),
                                                                                 fContourLevels.getSpecialValues(),
                                                                                 fContourLevels.getSpecialColors(),
                                                                                 fContourLevels.getSpecialLabels()) {
        public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
          ((Graphics2D) aGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          super.paintLabel(aGraphics, aMode, aContext);
        }
      };
      layer.setGXYLabelPainterProvider(labelPainter);
      labelPainter.setHaloEnabled(true);
      labelPainter.setHaloColor(Color.black);
      layer.setLabelScaleRange(new TLcdInterval(0.0007, Double.POSITIVE_INFINITY));
      layer.setLabeled(true);
    }

    return layer;
  }
}
