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
package samples.lightspeed.grid;

import java.util.Collection;
import java.util.Collections;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle.Orientation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

class XYGridLayerFactory implements ILspLayerFactory {

  private static final String TYPE_NAME = TLspXYGridLayerBuilder.createModel("XY Grid").getModelDescriptor().getTypeName();

  private final boolean fCoarse;
  private final Orientation fOrientation;
  private final double fGridOffset;
  private final double fEdgeOffset;

  // Whenever a grid layer is created again, we want to use the same styling as was used before. For example, if we
  // modified the grid to use red lines instead of white ones, and then recreate the grid because we change the
  // coarseness, we still want red lines.
  private final Collection<TLspCustomizableStyle> fPreviousStyles;

  public XYGridLayerFactory(boolean aCoarse, Orientation aOrientation, double aGridOffset, double aEdgeOffset, Collection<TLspCustomizableStyle> aPreviousStyles) {
    fCoarse = aCoarse;
    fOrientation = aOrientation;
    fGridOffset = aGridOffset;
    fEdgeOffset = aEdgeOffset;
    fPreviousStyles = aPreviousStyles;
  }

  public static ILcdModel createXYGridModel() {
    return TLspXYGridLayerBuilder.createModel("XY Grid");
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel != null && TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    TLspXYGridLayerBuilder builder = TLspXYGridLayerBuilder.newBuilder().model(aModel);
    TLspXYGridStyle gridStyle = XYGridStyleFactory.createGridStyle(fCoarse, fOrientation, fGridOffset, fEdgeOffset);
    builder.style(gridStyle);

    ILspStyledLayer layer = builder.build();
    if (fPreviousStyles != null) {
      transferStyles(layer, fPreviousStyles);
    }
    new XYGridLayerWarningMediator(layer);
    return Collections.<ILspLayer>singletonList(layer);
  }

  private void transferStyles(ILspStyledLayer aStyledLayer, Collection<TLspCustomizableStyle> aStyles) {
    ILspCustomizableStyler bodyStyler = (ILspCustomizableStyler) aStyledLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    Collection<TLspCustomizableStyle> bodyStyles = bodyStyler.getStyles();

    ILspCustomizableStyler labelStyler = (ILspCustomizableStyler) aStyledLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL);
    Collection<TLspCustomizableStyle> labelStyles = labelStyler.getStyles();

    for (TLspCustomizableStyle customizableStyle : aStyles) {
      ALspStyle style = customizableStyle.getStyle();
      if (style instanceof TLspLineStyle) {
        // Find a line style in bodyStyles, with the same display name
        for (TLspCustomizableStyle bodyStyle : bodyStyles) {
          if (bodyStyle.getStyle() instanceof TLspLineStyle && bodyStyle.getDisplayName().equals(customizableStyle.getDisplayName())) {
            bodyStyle.setStyle(customizableStyle.getStyle());
          }
        }
      } else if (style instanceof TLspTextStyle) {
        // Find a text style in labelStyles, with the same display name
        for (TLspCustomizableStyle labelStyle : labelStyles) {
          if (labelStyle.getStyle() instanceof TLspLineStyle && labelStyle.getDisplayName().equals(customizableStyle.getDisplayName())) {
            labelStyle.setStyle(customizableStyle.getStyle());
          }
        }
      }
    }
  }
}
