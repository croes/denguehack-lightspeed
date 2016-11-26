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
package samples.gxy.highlight;

import static samples.gxy.common.labels.GXYLabelPainterFactory.createGXYLabelPainter;

import java.io.IOException;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdSymbol;
import samples.common.MapColors;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * Shows how to add highlighting to a GXY map.
 * The highlighting consists of 2 parts:
 * - a controller that determines which object to highlight
 * - a layer that paints highlighted objects
 */
public class MainPanel extends GXYSample {

  private static final TLcdGXYPainterColorStyle REGULAR_FILL_STYLE = new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_FILL);
  private static final TLcdG2DLineStyle HIGHLIGHT_LINE_STYLE = new TLcdG2DLineStyle(MapColors.INTERACTIVE_OUTLINE, MapColors.SELECTION);
  private static final TLcdG2DLineStyle REGULAR_LINE_STYLE = new TLcdG2DLineStyle(MapColors.BACKGROUND_OUTLINE, MapColors.SELECTION);

  private static final ILcdIcon REGULAR_CITY_ICON = makeAntiAliased(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, MapColors.ICON_OUTLINE, MapColors.ICON_FILL));
  private static final ILcdIcon SELECTED_CITY_ICON = makeAntiAliased(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, MapColors.SELECTION, MapColors.ICON_FILL));
  private static final ILcdIcon HIGHLIGHTED_CITY_ICON = makeAntiAliased(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, MapColors.ICON_OUTLINE, MapColors.INTERACTIVE_FILL));

  private HighlightController fHighlightController;

  @Override
  protected void createGUI() {
    super.createGUI();

    // deactivate the controller, add the highlight controller, and reactivate it
    getView().setGXYController(null);
    TLcdGXYCompositeController editController = ((ToolBar) getToolBars()[0]).getGXYCompositeEditController();
    fHighlightController = new HighlightController();
    editController.addGXYController(fHighlightController);
    getView().setGXYController(editController);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    final HighlightLayer statesLayer = createHighlightedStatesLayer();
    GXYLayerUtil.addGXYLayer(getView(), statesLayer);

    final HighlightLayer citiesLayer = createHighlightedCitiesLayer();
    GXYLayerUtil.addGXYLayer(getView(), citiesLayer);
    GXYLayerUtil.fitGXYLayer(getView(), citiesLayer);

    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        fHighlightController.registerLayer(statesLayer);
        fHighlightController.registerLayer(citiesLayer);
      }
    });
  }

  protected HighlightLayer createHighlightedCitiesLayer() {
    ILcdModel model = GXYDataUtil.instance().model(SampleData.US_CITIES).getModel();

    TLcdGXYShapePainter highlightPainter = new TLcdGXYShapePainter();
    highlightPainter.setIcon(HIGHLIGHTED_CITY_ICON);

    TLcdGXYShapePainter regularPainter = new TLcdGXYShapePainter();
    regularPainter.setIcon(REGULAR_CITY_ICON);
    regularPainter.setSelectedIcon(SELECTED_CITY_ICON);

    HighlightLayer layer = new HighlightLayer(model, "Cities", highlightPainter);
    layer.setGXYPainterProvider(regularPainter);
    layer.setGXYLabelPainterProvider(createGXYLabelPainter(model, false));
    return layer;
  }

  protected HighlightLayer createHighlightedStatesLayer() {
    ILcdModel model = GXYDataUtil.instance().model(SampleData.US_STATES).getModel();

    TLcdGXYShapePainter highlightPainter = new TLcdGXYShapePainter();
    highlightPainter.setMode(ALcdGXYAreaPainter.OUTLINED);
    highlightPainter.setLineStyle(HIGHLIGHT_LINE_STYLE);

    TLcdGXYShapePainter regularPainter = new TLcdGXYShapePainter();
    regularPainter.setMode(ALcdGXYAreaPainter.OUTLINED_FILLED);
    regularPainter.setLineStyle(REGULAR_LINE_STYLE);
    regularPainter.setFillStyle(REGULAR_FILL_STYLE);

    HighlightLayer layer = new HighlightLayer(model, "States", highlightPainter);
    layer.setGXYPainterProvider(regularPainter);
    layer.setGXYLabelPainterProvider(createGXYLabelPainter(model, false));
    return layer;
  }

  private static ILcdIcon makeAntiAliased(TLcdSymbol aSymbol) {
    aSymbol.setAntiAliasing(true);
    return aSymbol;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Highlighting");
  }
}
