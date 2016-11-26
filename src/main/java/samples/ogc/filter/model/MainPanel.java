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
package samples.ogc.filter.model;

import static com.luciad.ogc.filter.model.TLcdOGCFilterFactory.*;

import java.awt.Color;

import com.luciad.format.xml.TLcdXMLName;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.filter.evaluator.ILcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.ILcdPropertyRetrieverProvider;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterContext;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.TLcdPropertyRetrieverUtil;
import com.luciad.ogc.filter.model.TLcdOGCFilter;
import com.luciad.ogc.filter.model.TLcdOGCFilterFactory;
import com.luciad.ogc.filter.model.TLcdOGCPropertyName;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * This sample demonstrates the ability to filter data using the OGC Filter API.
 * <p/>
 * It displays three layers that all show the same model (the major cities of the United States) with three different filters applied.
 * <p/>
 */
public class MainPanel extends GXYSample {

  private ILcdGXYLayerFactory fLayerFactory;

  // Create the default filter evaluator.
  private final ILcdOGCFilterEvaluator fFilterEvaluator = new TLcdOGCFilterEvaluator();

  public MainPanel() {
    super();
    fLayerFactory = new GXYUnstyledLayerFactory();
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-125, 25, 60.0, 30.0);
  }

  public void addData() {
    // Add some background data
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").selectable(false).addToView(getView());

    // Create a layer for the cities model
    ILcdModel model = GXYDataUtil.instance().model(SampleData.US_CITIES).getModel();
      TLcdOGCPropertyName propertyName = new TLcdOGCPropertyName(TLcdXMLName.getInstance(null, "TOT_POP"));
      GXYLayerUtil.addGXYLayer(getView(), createFilteredLayer(
          model,
          "< 200000",
          Color.blue,
          // TOT_POP < 200000
          new TLcdOGCFilter(TLcdOGCFilterFactory.lt(propertyName, literal((long) 200000)))
      ));

      // Create a new layer, adding the model to it.
    GXYLayerUtil.addGXYLayer(getView(), createFilteredLayer(
        model,
        "BETWEEN 200000 AND 1000000",
        Color.orange,
        // TOT_POP BETWEEN 200000 AND 1000000
        new TLcdOGCFilter(between(propertyName, literal((long) 200000), literal((long) 1000000)))
      ));

      // Create a new layer, adding the model to it.
      ILcdGXYLayer layer = createFilteredLayer(
          model,
          "> 1000000",
          Color.green,
          // TOT_POP > 1000000
          new TLcdOGCFilter(gt(propertyName, literal((long) 1000000)))
      );
      GXYLayerUtil.addGXYLayer(getView(), layer);
  }

  /**
   * Creates a layer for a given model, label and color and a filter that will be evaluated and applied.
   *
   * @param aModel     the given model.
   * @param aLabel     the label of the layer.
   * @param aColor     the color of the icons that will be used for displaying the layer.
   * @param aOGCFilter the filter that will be evaluated and applied.
   *
   * @return the resulting layer.
   */
  private ILcdGXYLayer createFilteredLayer(ILcdModel aModel, String aLabel, Color aColor, TLcdOGCFilter aOGCFilter) {

    // get a default property retriever provider for the given model.
    ILcdPropertyRetrieverProvider propertyRetrieverProvider =
        TLcdPropertyRetrieverUtil.getDefaultPropertyRetrieverProvider(aModel);

    // build the filter evaluation context for the given model.
    TLcdOGCFilterContext filterContext =
        new TLcdOGCFilterContext((ILcdGeoReference) aModel.getModelReference(), null, propertyRetrieverProvider);

    // build the object filter from the filter expression and the filter evaluation context.
    ILcdDynamicFilter filter = fFilterEvaluator.buildFilter(aOGCFilter, filterContext);

    ILcdGXYLayer layer = fLayerFactory.createGXYLayer(aModel);
    layer.setLabel(aLabel);
    layer.setLabeled(true);

    if (layer instanceof TLcdGXYLayer) {
      TLcdGXYLayer gxy_layer = (TLcdGXYLayer) layer;
      ILcdGXYPainterProvider gxy_painter = gxy_layer.getGXYPainterProvider();
      TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 7, aColor);
      gxy_layer.setIcon(icon);
      if (gxy_painter instanceof TLcdGXYShapePainter) {
        TLcdGXYShapePainter gxy_shape_painter = (TLcdGXYShapePainter) gxy_painter;
        gxy_shape_painter.setIcon(icon);
        gxy_shape_painter.setSelectedIcon(new TLcdSymbol(TLcdSymbol.RECT, 7, Color.red));
      } else if (gxy_painter instanceof TLcdGXYIconPainter) {
        TLcdGXYIconPainter gxy_icon_painter = (TLcdGXYIconPainter) gxy_painter;
        gxy_icon_painter.setIcon(icon);
        gxy_icon_painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.RECT, 7, Color.red));
      }
      // apply the object filter to the layer.
      gxy_layer.setFilter(filter);
    }

    return layer;
  }

  public static void main(final String[] args) {
    startSample(MainPanel.class, "Filtering SHP model");
  }
}
