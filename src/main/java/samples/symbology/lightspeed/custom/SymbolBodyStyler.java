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
package samples.symbology.lightspeed.custom;

import java.awt.Color;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.ALcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditableShape;
import com.luciad.shape.shape2D.TLcdLonLatGeoBuffer;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;

import samples.gxy.common.AntiAliasedIcon;

/**
 * Styler that implements the following customizations for the body of the symbols:
 * <ul>
 * <li>Use {@link Symbol} domain objects instead of {@link com.luciad.symbology.milstd2525b.model.ILcdMS2525bShape}</li>
 * <li>Replace the symbols with a simple dot when zoomed out</li>
 * <li>Support custom (i.e. non-MS2525b) symbol codes</li>
 * </ul>
 */
class SymbolBodyStyler extends SymbolStylerBase {

  private static final FocusPointStyleTargetProvider FOCUS_POINT_STYLE_TARGET_PROVIDER = new FocusPointStyleTargetProvider();
  private static final CustomCodeStyleTargetProvider CUSTOM_BUFFER_STYLE_TARGET_PROVIDER = new CustomCodeStyleTargetProvider();

  /**
   * Creates a new symbol styler. The boolean parameter indicates whether the
   * styler will be used for selected or non-selected objects.
   *
   * @param aSelected true if the styler should show objects in selected state
   */
  SymbolBodyStyler(boolean aSelected) {
    super(aSelected);
  }

  @Override
  protected void styleStandardSymbol(
      Symbol aSymbol,
      boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  ) {
    ILcdShape geometry = aSymbol.getGeometry();
    if (aInScaleRange) {
      // Use the standard symbology visualization
      ALspStyle symbologyStyle = createSymbologyStyle(aSymbol);

      aStyleCollector
          .object(aSymbol)
          .geometry(geometry)
          .style(symbologyStyle)
          .submit();
    } else {
      // Visualize the symbol as a dot when zoomed out
      TLcdEditableMS2525bObject coded = new TLcdEditableMS2525bObject(aSymbol.getSymbolCode());
      TLcdDefaultMS2525bStyle style = getStyle();
      Color affiliationColor = style.isAffiliationColorEnabled() ?
                               style.getAffiliationColor(ELcdMS2525Standard.MIL_STD_2525b, coded.getAffiliationValue()) :
                               style.getColor();
      styleSimplifiedSymbol(aSymbol, aStyleCollector, affiliationColor);
    }
  }

  @Override
  protected void styleCustomSymbol(
      Symbol aSymbol,
      CustomSymbolCodeDescriptor aDescriptor, boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  ) {
    // Determine the affiliation color.
    int affiliation = aDescriptor.getAffiliation();
    TLcdDefaultMS2525bStyle style = getStyle();
    Color affiliationColor = style.isAffiliationColorEnabled() ?
                             style.getAffiliationColor(affiliation) :
                             style.getColor();

    if (aInScaleRange) {
      // Create a stroke style for the symbol
      TLspComplexStrokedLineStyle stroke = getDashDotArrowStyle(affiliationColor);

      // Draw the symbol as a geobuffer
      aStyleCollector
          .object(aSymbol)
          .geometry(CUSTOM_BUFFER_STYLE_TARGET_PROVIDER)
          .style(stroke)
          .submit();
    } else {
      // Visualize the symbol as a dot when zoomed out
      styleSimplifiedSymbol(aSymbol, aStyleCollector, affiliationColor);
    }
  }

  /**
   * Draws a dot at the symbol's focus point in the given affiliation color.
   *
   * @param aSymbol           the symbol to be drawn
   * @param aStyleCollector   the style collector
   * @param aAffiliationColor the affiliation color
   */
  private void styleSimplifiedSymbol(
      Symbol aSymbol,
      ALspStyleCollector aStyleCollector,
      Color aAffiliationColor
  ) {
    TLspIconStyle iconStyle = TLspIconStyle.newBuilder()
                                           .icon(getDotIcon(aAffiliationColor))
                                           .build();

    aStyleCollector
        .object(aSymbol)
        .geometry(FOCUS_POINT_STYLE_TARGET_PROVIDER)
        .styles(iconStyle, TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, iconStyle.getIcon().getIconHeight() / 2).build())
        .submit();
  }

  /**
   * Creates a line style that shows a dash-dot pattern and an arrow.
   *
   * @param aColor the color of the pattern
   *
   * @return the style
   */
  private TLspComplexStrokedLineStyle getDashDotArrowStyle(Color aColor) {
    ALspComplexStroke arrow = ALspComplexStroke.arrow()
                                               .size(12)
                                               .lineColor(aColor)
                                               .lineWidth(2)
                                               .build();
    ALspComplexStroke dashdot = ALspComplexStroke.append(
        ALspComplexStroke.line()
                         .length(10)
                         .lineColor(aColor)
                         .lineWidth(2)
                         .build(),
        ALspComplexStroke.gap(3),
        ALspComplexStroke.filledArc()
                         .length(5)
                         .minorRadius(2.5)
                         .fillColor(aColor)
                         .build(),
        ALspComplexStroke.gap(3)
    );
    ALspComplexStroke plain = ALspComplexStroke.line()
                                               .lengthRelative(1)
                                               .lineColor(aColor)
                                               .lineWidth(2)
                                               .build();
    return TLspComplexStrokedLineStyle.newBuilder()
                                      .regular(dashdot)
                                      .fallback(plain)
                                      .decoration(0.25, arrow)
                                      .decoration(0.5, arrow)
                                      .decoration(0.75, arrow)
                                      .decoration(1.0, arrow)
                                      .haloColor(SELECTION_COLOR)
                                      .haloThickness(isSelected() ? 2 : 0)
                                      .build();
  }

  private ILcdIcon getDotIcon(Color aColor) {
    return new AntiAliasedIcon(
        new TLcdSymbol(
            TLcdSymbol.FILLED_CIRCLE,
            15,
            null,
            isSelected() ? SELECTION_COLOR : aColor
        ));
  }

  /**
   * Uses the focus point of the shape as geometry.
   */
  private static class FocusPointStyleTargetProvider extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      aResultSFCT.add(((Symbol) aObject).getGeometry().getFocusPoint());
    }

    @Override
    public void getEditTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      ILcdShape geometry = ((Symbol) aObject).getGeometry();
      if (geometry instanceof ILcd2DEditableShape) {
        aResultSFCT.add(new EditableFocusPoint((ILcd2DEditableShape) geometry));
      }
    }
  }

  /**
   * An editable point that represents the focus point of a shape.
   */
  private static class EditableFocusPoint extends ALcd2DEditablePoint {
    private final ILcd2DEditableShape fShape;

    public EditableFocusPoint(ILcd2DEditableShape aShape) {
      fShape = aShape;
    }

    @Override
    public void move2D(double aX, double aY) {
      fShape.move2D(aX, aY);
    }

    @Override
    public double getX() {
      return fShape.getFocusPoint().getX();
    }

    @Override
    public double getY() {
      return fShape.getFocusPoint().getY();
    }

    @Override
    public double getZ() {
      return fShape.getFocusPoint().getZ();
    }

    @Override
    public ILcd2DEditablePoint cloneAs2DEditablePoint() {
      return fShape.getFocusPoint().cloneAs2DEditablePoint();
    }

    @Override
    public ILcd3DEditablePoint cloneAs3DEditablePoint() {
      return fShape.getFocusPoint().cloneAs3DEditablePoint();
    }

    @Override
    public ILcd2DEditableBounds cloneAs2DEditableBounds() {
      return fShape.getFocusPoint().getBounds().cloneAs2DEditableBounds();
    }

    @Override
    public ILcd3DEditableBounds cloneAs3DEditableBounds() {
      return fShape.getFocusPoint().getBounds().cloneAs3DEditableBounds();
    }
  }

  /**
   * Style target provider for custom symbols which produces a geobuffer.
   */
  private static class CustomCodeStyleTargetProvider extends ALspStyleTargetProvider {
    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      Symbol symbol = (Symbol) aObject;
      double width = parseCustomSymbol(symbol.getSymbolCode()).getWidth();
      TLcdLonLatGeoBuffer buffer = new TLcdLonLatGeoBuffer(symbol.getGeometry(), width);
      aResultSFCT.add(buffer);
    }

    @Override
    public void getEditTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      aResultSFCT.add(((Symbol) aObject).getGeometry());
    }
  }
}
