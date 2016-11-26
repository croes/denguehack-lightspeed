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
package samples.lightspeed.style.textured;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.lightspeed.style.raster.RasterLayerFactory;

public class TexturedLayerFactory extends ALspSingleLayerFactory {

  private static final String IMAGE_TEXTURE_FILENAME = "samples/lightspeed/style/texture/bluemarble.png";

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .selectable(true);

    // Texture that is applied to the model elements.
    Image textureImage = TLcdImageIcon.getImage(IMAGE_TEXTURE_FILENAME);
    BufferedImage texture = toBufferedImage(textureImage);

    // Set a styler that submits a texture for the fill and a line
    // style for the outline.
    // We use a selection toggle styler to modulate the texture with
    // a red color when the object is selected. We also change the line color from
    // black to red when a shape is selected.
    TLspFillStyle regularFillStyle = TLspFillStyle.newBuilder()
                                                  .texture(texture)
                                                  .translateTexture(180, 90, 0)
                                                  .scaleTexture(1.0 / 360.0, 1.0 / 180.0, 1.0)
                                                  .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                  .build();
    TLspLineStyle regularLineStyle = TLspLineStyle.newBuilder()
                                                  .color(Color.black)
                                                  .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    layerBuilder.bodyStyles(TLspPaintState.REGULAR, regularFillStyle, regularLineStyle);

    TLspFillStyle selectedFillStyle = TLspFillStyle.newBuilder()
                                                   .color(new Color(1.0f, 0.5f, 0.5f, 1.0f))
                                                   .texture(texture).translateTexture(180, 90, 0)
                                                   .scaleTexture(1.0 / 360.0, 1.0 / 180.0, 1.0)
                                                   .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    TLspLineStyle selectedLineStyle = TLspLineStyle.newBuilder()
                                                   .color(Color.red)
                                                   .width(2)
                                                   .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    layerBuilder.bodyStyles(TLspPaintState.SELECTED, selectedFillStyle, selectedLineStyle);

    return layerBuilder.build();
  }

  private BufferedImage toBufferedImage(Image aImage) {
    BufferedImage bufferedImage = new BufferedImage(aImage.getWidth(null),
                                                    aImage.getHeight(null),
                                                    BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics g = bufferedImage.createGraphics();
    g.drawImage(aImage, 0, 0, null);
    g.dispose();
    return bufferedImage;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return !RasterLayerFactory.canCreateLayersForModel(aModel);
  }
}
