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
package samples.lightspeed.internal.editing;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspDrapingToggleStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 */
public class LayerFactory extends ALspSingleLayerFactory {

  private static final float DEFAULT_FILL_ALPHA = 0.75f;
  private static final float DEFAULT_LINE_ALPHA = 0.95f;

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder builder = TLspShapeLayerBuilder.newBuilder().model(aModel).bodyEditable(true);
    boolean hasAShape = aModel.getModelDescriptor().getTypeName().equals(ModelFactory.HAS_A_SHAPES_TYPE);
    setShapeStyler(builder, hasAShape);
    return builder.build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName().equals(ModelFactory.SHAPES_TYPE) ||
           aModel.getModelDescriptor().getTypeName().equals(ModelFactory.HAS_A_SHAPES_TYPE);
  }

  // Sets the painter's styler.
  private static void setShapeStyler(TLspShapeLayerBuilder aBuilder, final boolean aHasAShape) {
    aBuilder.bodyStyler(
        TLspPaintState.REGULAR,
        new MyHasAShapeDrapingToggleStyler(aHasAShape, false)
    );
    aBuilder.bodyStyler(
        TLspPaintState.SELECTED,
        new MyHasAShapeDrapingToggleStyler(aHasAShape, true)
    );
    aBuilder.bodyStyler(
        TLspPaintState.EDITED,
        new MyHasAShapeDrapingToggleStyler(aHasAShape, true)
    );
  }

  private static ILspStyler getStyler(boolean aSelected, ILspWorldElevationStyle.ElevationMode aElevationMode, boolean aHasAShape) {

    TLcdImageIcon regularIcon = new TLcdImageIcon("images/mif/mif20_airplane.gif");

    TLcdCompositeIcon selectedIcon = new TLcdCompositeIcon();
    selectedIcon.addIcon(new TLcdResizeableIcon(new TLcdSymbol(TLcdSymbol.CIRCLE, 20, Color.red)));
    selectedIcon.addIcon(new TLcdResizeableIcon(regularIcon));
    TLspStyler lineAndFillStyler;
    TLspStyler iconStyler;

    TLspFillStyle fillStyle;
    TLspLineStyle lineStyle;
    TLspTextStyle textStyle;
    TLspIconStyle iconStyle;
    TLspVerticalLineStyle verticalLineStyle;
    if (!aSelected) {
      // Green fill with 1-pixel wide outline
      fillStyle = TLspFillStyle.newBuilder().elevationMode(aElevationMode).color(new Color(0.1f, 0.7f, 0.1f, DEFAULT_FILL_ALPHA)).build();
      lineStyle = TLspLineStyle.newBuilder().elevationMode(aElevationMode).color(new Color(1f, 1f, 1f, DEFAULT_LINE_ALPHA)).build();
      textStyle = TLspTextStyle.newBuilder().font(new Font("Monospaced", Font.BOLD, 1)).build();
      iconStyle = TLspIconStyle.newBuilder().icon(regularIcon).elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN).build();
      verticalLineStyle = TLspVerticalLineStyle.newBuilder().build();
    } else {
      // Red fill with 1.5 pixel wide outline
      fillStyle = TLspFillStyle.newBuilder().elevationMode(aElevationMode).color(new Color(0.7f, 0.1f, 0.1f, DEFAULT_FILL_ALPHA)).build();
      lineStyle = TLspLineStyle.newBuilder().elevationMode(aElevationMode).color(new Color(1f, 1f, 1f, DEFAULT_LINE_ALPHA)).width(1.5f).build();
      textStyle = TLspTextStyle.newBuilder().font(new Font("Monospaced", Font.BOLD, 1)).build();
      iconStyle = TLspIconStyle.newBuilder().icon(selectedIcon).elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN).build();
      verticalLineStyle = TLspVerticalLineStyle.newBuilder().build();
    }
    if (aHasAShape) {
      HashMap<ALspStyleTargetProvider, List<? extends ALspStyle>> iconStylesMap = new HashMap<ALspStyleTargetProvider, List<? extends ALspStyle>>();
      HashMap<ALspStyleTargetProvider, List<? extends ALspStyle>> lineAndFillStylesMap = new HashMap<ALspStyleTargetProvider, List<? extends ALspStyle>>();
      iconStylesMap.put(new HasAShapeUnpacker(), Arrays.<ALspStyle>asList(iconStyle, verticalLineStyle));
      lineAndFillStylesMap.put(new HasAShapeUnpacker(), Arrays.<ALspStyle>asList(fillStyle, lineStyle, textStyle));
      iconStyler = new TLspStyler(iconStylesMap);
      lineAndFillStyler = new TLspStyler(lineAndFillStylesMap);
    } else {
      iconStyler = new TLspStyler(iconStyle, verticalLineStyle);
      lineAndFillStyler = new TLspStyler(fillStyle, lineStyle, textStyle);
    }
    return new ShapeStyler(iconStyler, lineAndFillStyler);
  }

  /**
   * A styler that separates points from other shapes.
   */
  public static class ShapeStyler extends ALspStyler {
    private final ILspStyler fPointStyler;
    private final ILspStyler fLineAndFillStyler;

    public ShapeStyler(ILspStyler aPointStyler, ILspStyler aLineAndFillStyler) {
      super();
      fPointStyler = aPointStyler;
      fLineAndFillStyler = aLineAndFillStyler;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      ArrayList<Object> points = new ArrayList<Object>();
      ArrayList<Object> other = new ArrayList<Object>();
      for (Object o : aObjects) {
        if (o instanceof ILcdPoint) {
          points.add(o);
        } else if (o instanceof HasAShape && ((HasAShape) o).getShape() instanceof ILcdPoint) {
          points.add(o);
        } else {
          other.add(o);
        }
      }
      if (!other.isEmpty()) {
        fLineAndFillStyler.style(other, aStyleCollector, aContext);
      }
      if (!points.isEmpty()) {
        fPointStyler.style(points, aStyleCollector, aContext);
      }
    }

  }

  public static class HasAShapeUnpacker extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (aObject instanceof HasAShape) {
        aResultSFCT.add(((HasAShape) aObject).getShape());
      }
    }
  }

  private static class MyHasAShapeDrapingToggleStyler extends TLspDrapingToggleStyler {

    public MyHasAShapeDrapingToggleStyler(boolean aHasAShape, boolean aSelected) {
      super(LayerFactory.getStyler(aSelected, ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID, aHasAShape),
            LayerFactory.getStyler(aSelected, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, aHasAShape));
    }

    @Override
    protected boolean isUseSpecialStyler(Object aObject, TLspContext aContext) {
      if (aObject instanceof HasAShape) {
        return super.isUseSpecialStyler(((HasAShape) aObject).getShape(), aContext);
      } else {
        return super.isUseSpecialStyler(aObject, aContext);
      }
    }
  }
}
