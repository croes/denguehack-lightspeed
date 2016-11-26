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
package samples.lightspeed.style.fillstyle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import samples.common.MapColors;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspTexturedStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

public class LayerFactory extends ALspSingleLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LayerFactory.class.getName());

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getDisplayName().equals("Textured shapes") ||
           aModel.getModelDescriptor().getDisplayName().equals("Extruded textured shapes") ||
           aModel.getModelDescriptor().getDisplayName().equals("Stipple pattern shapes") ||
           aModel.getModelDescriptor().getDisplayName().equals("Extruded stipple shapes") ||
           aModel.getModelDescriptor().getDisplayName().equals("Solid fill shapes") ||
           aModel.getModelDescriptor().getDisplayName().equals("Extruded solid shapes");
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor().getDisplayName().equals("Textured shapes")) {
      return createTexturedLayer(aModel, false);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Extruded textured shapes")) {
      return createTexturedLayer(aModel, true);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Stipple pattern shapes")) {
      return createStipplePatternLayer(aModel, false);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Extruded stipple shapes")) {
      return createStipplePatternLayer(aModel, true);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Solid fill shapes")) {
      return createSolidFillLayer(aModel, false);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Extruded solid shapes")) {
      return createSolidFillLayer(aModel, true);
    }

    return null;
  }

  /**
   * Returns a layer using a texture for filling the shapes of the given model
   *
   * @param aModel the model for which to create the layer
   * @param aExtruded
   * @return a layer using a texture for filling the shapes of the given model
   */
  private ILspLayer createTexturedLayer(ILcdModel aModel, boolean aExtruded) {

    //Read in an image to be used as a texture
    BufferedImage image = null;
    try {
      image = ImageIO.read(getClass().getResourceAsStream("/images/luciad_logo.png"));
    } catch (IOException e) {
      sLogger.error("Unable to read texture image file for textured layer creation");
    }

    //Create the fill style with the loaded image as a texture
    TLspFillStyle.Builder fillStyleBuilder = TLspFillStyle.newBuilder()
                                                          .texture(image)
                                                          .textureCoordinatesMode(ILspTexturedStyle.TextureCoordinatesMode.OBJECT_RELATIVE)
                                                          .elevationMode(aExtruded ? ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID :
                                                                         ILspWorldElevationStyle.ElevationMode.ON_TERRAIN);
    TLspFillStyle fillStyle = fillStyleBuilder.build();
    TLspFillStyle selectedStyle = fillStyleBuilder.color(MapColors.SELECTION).build();

    //Return the layer on which we set the created fill styles
    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                .bodyEditable(true)
                                .bodyStyler(TLspPaintState.REGULAR, fillStyle)
                                .bodyStyler(TLspPaintState.SELECTED, selectedStyle)
                                .bodyStyler(TLspPaintState.EDITED, selectedStyle)
                                .build();
  }

  /**
   * Returns a layer using a custom stipple pattern for filling the shapes of the given model
   *
   * @param aModel the model for which to create the layer
   * @param aExtruded
   * @return a layer using a custom stipple pattern for filling the shapes of the given model
   */
  private ILspLayer createStipplePatternLayer(ILcdModel aModel, boolean aExtruded) {
    //Create a custom stipple pattern
    TLspFillStyle.StipplePattern stipplePattern =
        TLspFillStyle.StipplePattern.newBuilder()
                                    .fillRect(1, 1, 13, 13)
                                    .fillRect(18, 18, 13, 13)
                                    .fillPolygon(new int[]{18, 30, 30}, new int[]{1, 1, 13}, 3)
                                    .fillPolygon(new int[]{1, 1, 13}, new int[]{30, 18, 30}, 3)
                                    .build();

    //Create the fill styles
    TLspFillStyle.Builder fillStyleBuilder = TLspFillStyle.newBuilder()
                                                          .stipplePattern(stipplePattern)
                                                          .elevationMode(aExtruded ? ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID :
                                                                         ILspWorldElevationStyle.ElevationMode.ON_TERRAIN);
    TLspFillStyle fillStyle = fillStyleBuilder.color(new Color(0, 200, 255)).build();
    TLspFillStyle selectedStyle = fillStyleBuilder.color(MapColors.SELECTION).build();

    //Create a line style for improving the visibility of the shapes' outlines
    TLspLineStyle lineStyle = TLspLineStyle.newBuilder().color(Color.white)
                                           .width(2.0)
                                           .elevationMode(aExtruded ? ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID :
                                                          ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                           .build();
    //Return the layer on which we set the created fill styles
    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                .selectable(true)
                                .bodyEditable(true)
                                .bodyStyler(TLspPaintState.REGULAR, new TLspStyler(fillStyle,
                                                                                   lineStyle))
                                .bodyStyler(TLspPaintState.SELECTED, new TLspStyler(selectedStyle,
                                                                                    lineStyle))
                                .bodyStyler(TLspPaintState.EDITED, new TLspStyler(selectedStyle,
                                                                                  lineStyle))
                                .build();
  }

  /**
   * Returns a layer using a solid color for filling the shapes of the given model
   *
   * @param aModel the model for which to create the layer
   * @param aExtruded
   * @return a layer using a solid color for filling the shapes of the given model
   */
  private ILspLayer createSolidFillLayer(ILcdModel aModel, boolean aExtruded) {

    //Create the fill styles
    TLspFillStyle.Builder fillStylebuilder = TLspFillStyle.newBuilder()
                                                          .elevationMode(aExtruded ? ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID :
                                                                         ILspWorldElevationStyle.ElevationMode.ON_TERRAIN);
    TLspFillStyle fillStyle = fillStylebuilder.color(new Color(0, 200, 255)).opacity(0.7f).build();
    TLspFillStyle selectedStyle = fillStylebuilder.color(MapColors.SELECTION).opacity(0.7f).build();
    TLspLineStyle lineStyle = TLspLineStyle.newBuilder().color(Color.white)
                                           .width(2.0)
                                           .elevationMode(aExtruded ? ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID :
                                                          ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                           .build();

    //Return the layer on which we set the created fill styles
    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                .selectable(true)
                                .bodyEditable(true)
                                .bodyStyler(TLspPaintState.REGULAR, new TLspStyler(fillStyle,
                                                                                   lineStyle))
                                .bodyStyler(TLspPaintState.SELECTED, new TLspStyler(selectedStyle,
                                                                                    lineStyle))
                                .bodyStyler(TLspPaintState.EDITED, new TLspStyler(selectedStyle,
                                                                                  lineStyle))
                                .build();
  }
}
