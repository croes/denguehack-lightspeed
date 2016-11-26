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
package samples.gxy.labels.offset;

import java.awt.Font;
import java.util.List;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.ILcdLabelDependencyProvider;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.TLcdGXYCompositeLabelPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYLabelPainterAdapter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.MapColors;

/**
 * This layer factory creates a layer displaying city data as labeled offset icons.
 * The spatial dependency of the text label on the icon is set in the layer's ALcdLabelLocations.
 */
public class OffsetLabelLayerFactory implements ILcdGXYLayerFactory {

  private ILcdGXYView fGXYView;

  private ILcdInterval fLabelScaleRange = new TLcdInterval(0.0, Double.MAX_VALUE);

  public OffsetLabelLayerFactory(ILcdGXYView aGXYView) {
    fGXYView = aGXYView;
  }

  public void setLabelScaleRange(ILcdInterval aLabelScaleRange) {
    fLabelScaleRange = aLabelScaleRange;
  }

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);

    layer.setGXYPen(new TLcdGeodeticPen());

    setupPainter(layer);
    setupLabelPainter(layer);

    layer.setIcon(new TLcdImageIcon("samples/images/layerIcons/land.png"));

    layer.setVisible(true);
    layer.setLabeled(true);
    layer.setSelectable(true);
    layer.setSelectionLabeled(true);
    layer.setLabelsEditable(true);
    layer.setEditable(false);

    layer.setLabelScaleRange(fLabelScaleRange);

    TLcdLabelLocations label_locations = createLabelLocations(layer);
    return BodyLabelsLayerWrapper.createLayerWrapper(layer, label_locations, fGXYView);
  }

  /*
   * Create a new ALcdLabelLocations which defines
   * - the parent label dependency : every label has label [0, 0] as parent.
   *   This dependency is used by the label painters and label algorithms to position the text label
   *   relative to the icon label.
   * - the body labels : label [0, 0] is used as body representation
   *   This is used to paint the icons below the map's labels (see BodyLabelsLayer)
   *   and to treat the icons as domain object representations during editing.
   *   (see TLcdGXYEditControllerModel2)
   */
  private TLcdLabelLocations createLabelLocations(ILcdGXYLayer aGXYLayer) {
    TLcdLabelLocation default_label_location = new TLcdLabelLocation(0, 0, 0.0d, 0);
    ILcdLabelDependencyProvider offset_dependency_provider = new ILcdLabelDependencyProvider() {
      @Override
      public void getDependingLabelsSFCT(TLcdLabelIdentifier aLabel, List<TLcdLabelIdentifier> aDependingLabelsSFCT) {
        if (aLabel.getLabelIndex() != 0 || aLabel.getSubLabelIndex() != 0) {
          aDependingLabelsSFCT.add(new TLcdLabelIdentifier(aLabel.getLayer(), aLabel.getDomainObject(), 0, 0));
        }
      }
    };
    return new TLcdLabelLocations(aGXYLayer, default_label_location, offset_dependency_provider) {
      @Override
      public void getDefaultLabelLocationSFCT(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocationSFCT) {
        super.getDefaultLabelLocationSFCT(aObject, aLabelIndex, aSubLabelIndex, aView, aLocationSFCT);
        if (aLabelIndex == 0 && aSubLabelIndex == 0) {
          aLocationSFCT.setBodyLabel(true);
        }
      }
    };
  }

  private void setupPainter(TLcdGXYLayer aLayer) {
    ILcdGXYPainterProvider painter_provider = createGXYPainterProvider();
    aLayer.setGXYPainterProvider(painter_provider);
    if (painter_provider instanceof ILcdGXYEditorProvider) {
      aLayer.setGXYEditorProvider((ILcdGXYEditorProvider) painter_provider);
    }
  }

  private void setupLabelPainter(TLcdGXYLayer aLayer) {
    // This label painter is used for the bodies.
    ILcdGXYLabelPainter body_label_painter = createBodyLabelPainter();

    // This label painter is used for the text labels.
    ILcdGXYLabelPainter text_label_painter = createTextLabelPainter();

    // Merge these label painters together in a composite label painter
    TLcdGXYCompositeLabelPainter composite_label_painter = new TLcdGXYCompositeLabelPainter(TLcdGXYCompositeLabelPainter.IndexRemappingMode.SHIFT_SUBLABEL_INDEX);

    if (body_label_painter instanceof ILcdGXYLabelEditor) {
      composite_label_painter.addLabelPainter(body_label_painter, (ILcdGXYLabelEditor) body_label_painter);
    } else {
      composite_label_painter.addLabelPainter(body_label_painter);
    }

    if (text_label_painter instanceof ILcdGXYLabelEditor) {
      composite_label_painter.addLabelPainter(text_label_painter, (ILcdGXYLabelEditor) text_label_painter);
    } else {
      composite_label_painter.addLabelPainter(text_label_painter);
    }

    // Set them on the layer
    aLayer.setGXYLabelPainterProvider(composite_label_painter);
    aLayer.setGXYLabelEditorProvider(composite_label_painter);
  }

  protected ILcdGXYPainterProvider createGXYPainterProvider() {
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 6, MapColors.BACKGROUND_OUTLINE));
    painter.setSelectedIcon(new TLcdSymbol(TLcdSymbol.RECT, 10, MapColors.SELECTION));
    return painter;
  }

  protected ILcdGXYLabelPainter createBodyLabelPainter() {
    TLcdGXYIconPainter icon_painter = createIconPainter();
    TLcdGXYLabelPainterAdapter body_label_painter = new TLcdGXYLabelPainterAdapter(icon_painter, icon_painter);
    body_label_painter.setPositionList(new int[]{
        TLcdGXYLabelPainterAdapter.SOUTH_EAST,
        TLcdGXYLabelPainterAdapter.SOUTH_WEST,
        TLcdGXYLabelPainterAdapter.NORTH_EAST,
        TLcdGXYLabelPainterAdapter.NORTH_WEST
    });
    body_label_painter.setPinStyle(new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_OUTLINE, MapColors.SELECTION));
    return body_label_painter;
  }

  private TLcdGXYIconPainter createIconPainter() {
    TLcdGXYIconPainter icon_painter = new TLcdGXYIconPainter();
    icon_painter.setIcon(MapColors.createIcon(false));
    icon_painter.setSelectionIcon(MapColors.createIcon(true));
    return icon_painter;
  }

  protected ILcdGXYLabelPainter createTextLabelPainter() {
    TLcdGXYDataObjectLabelPainter text_label_painter = new TLcdGXYDataObjectLabelPainter();
    text_label_painter.setExpressions("CITY");
    text_label_painter.setPositionList(new int[]{
        TLcdGXYDataObjectLabelPainter.EAST,
        TLcdGXYDataObjectLabelPainter.WEST,
        TLcdGXYDataObjectLabelPainter.NORTH,
        TLcdGXYDataObjectLabelPainter.SOUTH
    });
    text_label_painter.setForeground(MapColors.LABEL);
    text_label_painter.setSelectionColor(MapColors.SELECTION);
    text_label_painter.setFrame(false);
    text_label_painter.setFilled(false);
    text_label_painter.setWithPin(true);
    text_label_painter.setPinColor(MapColors.BACKGROUND_OUTLINE);
    text_label_painter.setSelectedPinColor(MapColors.SELECTION);
    text_label_painter.setShiftLabelPosition(18);
    text_label_painter.setFont(new Font("Dialog", Font.ITALIC, 12));
    text_label_painter.setHaloThickness(1);
    text_label_painter.setHaloColor(MapColors.LABEL_HALO);
    text_label_painter.setHaloEnabled(true);
    return text_label_painter;
  }

}
