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
package samples.lightspeed.style.icon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle.ScalingMode;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Simple layer factory
 */
public class LayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getDisplayName().equals("World sized icons") ||
           aModel.getModelDescriptor().getDisplayName().equals("View sized icons") ||
           aModel.getModelDescriptor().getDisplayName().equals("Rotatable icons");
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor().getDisplayName().equals("World sized icons")) {
      return createWorldIconPointLayer(aModel);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("View sized icons")) {
      return createViewIconPointLayer(aModel);
    } else if (aModel.getModelDescriptor().getDisplayName().equals("Rotatable icons")) {
      return createRotatableIconPointLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model not recognized");
    }
  }

  /**
   * Creates and returns a layer for visualising points with fixed world sized icons
   * @param aModel the model for which to create a layer
   * @return a newly created layer for the given model
   */
  private ILspLayer createWorldIconPointLayer(ILcdModel aModel) {
    TLcdImageIcon icon = new TLcdImageIcon(new TLcdImageIcon("samples/lightspeed/icons/mountains.png"));

    TLspIconStyle.Builder iconStyleBuilder = TLspIconStyle.newBuilder()
        .icon(icon)
            //Set icons to have a fixed world size
        .scalingMode(ScalingMode.WORLD_SCALING).worldSize(250)
            //Set the icons' opacity value
        .opacity(1.0f);

    TLspVerticalLineStyle.Builder<?> lineBuilder = TLspVerticalLineStyle.newBuilder()
                                                                        .color(Color.orange)
                                                                        .width(1.0f);

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .label("World sized icons")
                .selectable(true)
                .bodyStyler(TLspPaintState.REGULAR,
                            new TLspStyler(iconStyleBuilder.build(),
                                           lineBuilder.build())
                );
    return layerBuilder.build();
  }

  /**
   * Creates and returns a layer for visualising points with fixed view sized icons
   * @param aModel the model for which to create a layer
   * @return a newly created layer for the given model
   */
  private ILspLayer createViewIconPointLayer(ILcdModel aModel) {
    Color arrowColor = new Color(168, 230, 29);
    final TLspIconStyle iconStyle = TLspIconStyle.newBuilder()
        .icon(createArrowIcon(arrowColor.darker(), arrowColor))
            //Set icons to have fixed view coordinates
        .scalingMode(ScalingMode.VIEW_SCALING)
        .useOrientation(true)
        .scale(1.0)
            //Set the icons' alpha value
        .opacity(1.0f).build();

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
        .label("View sized icons")
            //Only show the icons from a certain scale, because they would otherwise clutter the view
        .bodyScaleRange(new TLcdInterval(5e-3, Double.MAX_VALUE))
        .bodyStyler(TLspPaintState.REGULAR, iconStyle);

    return layerBuilder.build();
  }

  /**
   * Creates and returns a layer for visualising points with rotable icons with a fixed world size
   * @param aModel the model for which to create a layer
   * @return a newly created layer for the given model
   */
  private ILspLayer createRotatableIconPointLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .label("Rotatable icons")
                .bodyStyler(TLspPaintState.REGULAR, new RotatableIconStyler());
    return layerBuilder.build();
  }

  /**
   * Returns a TLcdImageIcon that depicts an arrow pointing upward with the given border and fill color.
   * @param aBorderColor the border color for the arrow
   * @param aFillColor the fill color for the arrow
   * @return a TLcdImageIcon that depicts an arrow pointing upward with the given border and fill color.
   */
  private TLcdImageIcon createArrowIcon(Color aBorderColor, Color aFillColor) {
    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = image.createGraphics();
    graphics.setColor(aBorderColor);
    graphics.fillRect(25, 22, 14, 35);
    graphics.fillPolygon(new int[]{16, 31, 32, 47}, new int[]{22, 7, 7, 22}, 4);

    graphics.setColor(aFillColor);
    graphics.fillRect(26, 21, 12, 35);
    graphics.fillPolygon(new int[]{18, 31, 32, 45}, new int[]{21, 8, 8, 21}, 4);
    graphics.dispose();
    TLcdImageIcon icon = new TLcdImageIcon(image);
    return icon;
  }

}
