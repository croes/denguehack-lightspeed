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
package samples.ogc.sld.gxy;

import com.luciad.model.ILcdModel;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.model.TLcdSLDRule;
import com.luciad.ogc.sld.model.TLcdSLDTextSymbolizer;
import com.luciad.ogc.sld.view.gxy.ALcdSLDFeatureTypeStylePainter;
import com.luciad.ogc.sld.view.gxy.ALcdSLDGXYPainterFactory;
import com.luciad.ogc.sld.view.gxy.TLcdSLDContext;
import com.luciad.ogc.sld.view.gxy.TLcdSLDGXYPainterFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;
import samples.ogc.sld.SLDFeatureTypeStyleStore;

/**
 * A utility class that enables:
 * <ul>
 * <li>creation of an SLD styled layer for a given model and style</li>
 * <li>adaptation of a given layer to apply a given style</li>
 * </ul>
 */
public class SLDGXYLayerUtil {

  private static SLDGXYLayerUtil fInstance = new SLDGXYLayerUtil();

  private SLDGXYLayerUtil() {
  }

  public static SLDGXYLayerUtil getInstance() {
    return fInstance;
  }

  public ILcdGXYLayer createSLDGXYLayer(ILcdModel aModel, TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle) {
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    // ...
    if (aModel.getModelDescriptor() != null) {
      layer.setLabel(aModel.getModelDescriptor().getDisplayName());
    } else {
      layer.setLabel("New SLD layer");
    }
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    layer.setLabeled(true);
    layer.setVisible(true);
    layer.setEditable(false);
    applyStyleToLayerSFCT(aSLDFeatureTypeStyle, layer);

    return layer;
  }

  public void applyStyleToLayerSFCT(TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle,
                                    TLcdGXYLayer aGXYLayerSFCT) {
    // create an SLD context based on the model
    TLcdSLDContext sld_context = SLDFeatureTypeStyleStore.createSLDContext(aGXYLayerSFCT.getModel());

    // create a painter based on the feature type style and an SLD context
    ALcdSLDFeatureTypeStylePainter painter =
        createFeatureTypeStylePainter(aSLDFeatureTypeStyle, sld_context);

    // set the painter as painter provider on the layer
    aGXYLayerSFCT.setGXYPainterProvider(painter);
    // this is required for the text symbolizers, as they may be implemented
    // as ILcdGXYLabelPainter instances.
    aGXYLayerSFCT.setGXYLabelPainterProvider(painter);

    // check if we have to set the layer labeled or not 
    int rule_count = aSLDFeatureTypeStyle.getRuleCount();
    boolean contains_text_symbolizer = false;
    for (int rule_index = 0; rule_index < rule_count && !contains_text_symbolizer; rule_index++) {
      TLcdSLDRule rule = aSLDFeatureTypeStyle.getRule(rule_index);
      int symbolizer_count = rule.getSymbolizerCount();
      for (int symbolizer_index = 0; symbolizer_index < symbolizer_count && !contains_text_symbolizer; symbolizer_index++) {
        contains_text_symbolizer = (rule.getSymbolizer(symbolizer_index) instanceof TLcdSLDTextSymbolizer);
      }
    }
    if (!aGXYLayerSFCT.isLabeled()) {
      aGXYLayerSFCT.setLabeled(contains_text_symbolizer);
    }
    // ...
  }


  private ALcdSLDFeatureTypeStylePainter
  createFeatureTypeStylePainter(TLcdSLDFeatureTypeStyle aSLDFeatureTypeStyle,
                                TLcdSLDContext aSLDContext) {

    ALcdSLDGXYPainterFactory sld_painter_factory = new TLcdSLDGXYPainterFactory();
    return sld_painter_factory.createFeatureTypeStylePainter(aSLDFeatureTypeStyle, aSLDContext);
  }

}
