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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.demo.application.data.milsym.ClusterAwareMilSymLabelStylerWrapper;
import samples.lightspeed.demo.application.data.milsym.MilSymLabelStyler;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.symbology.common.MilitarySymbologyModelDescriptor;
import samples.symbology.common.app6.StyledEditableAPP6Object;
import samples.symbology.lightspeed.ClusterAwareAPP6SymbolStyler;
import samples.symbology.lightspeed.MilitarySymbolClassifier;

/**
 * Layer factory for the military symbology theme.
 */
public class MilSymLayerFactory extends AbstractLayerFactory {

  private static final TLcdDefaultAPP6AStyle fRegularStyle;
  private static final TLcdDefaultAPP6AStyle fSelectedStyle;
  private static final ClusterAwareAPP6SymbolStyler fRegularStyler;
  private static final ClusterAwareAPP6SymbolStyler fSelectedStyler;
  private static final ClusterAwareMilSymLabelStylerWrapper fRegularLabelStyler;
  private static final ClusterAwareMilSymLabelStylerWrapper fSelectedLabelStyler;

  static {
    fRegularStyle = StyledEditableAPP6Object.getDefaultStyle();
    fRegularStyler = new ClusterAwareAPP6SymbolStyler(fRegularStyle, TLspPaintState.REGULAR);

    fSelectedStyle = StyledEditableAPP6Object.getDefaultStyle();
    fSelectedStyler = new ClusterAwareAPP6SymbolStyler(fSelectedStyle, TLspPaintState.SELECTED);

    fRegularLabelStyler = new ClusterAwareMilSymLabelStylerWrapper(new MilSymLabelStyler(fRegularStyle), TLspPaintState.REGULAR);
    fSelectedLabelStyler = new ClusterAwareMilSymLabelStylerWrapper(new MilSymLabelStyler(fSelectedStyle),  TLspPaintState.SELECTED);
  }

  public MilSymLayerFactory() {
  }

  public static ClusterAwareAPP6SymbolStyler getRegularStyler() {
    return fRegularStyler;
  }

  public static ClusterAwareAPP6SymbolStyler getSelectedStyler() {
    return fSelectedStyler;
  }

  @Override
  public void configure(Properties aProperties) {
    int lineThickness = Integer.parseInt(aProperties.getProperty("line.thickness", "4"));
    int haloThickness = Integer.parseInt(aProperties.getProperty("halo.thickness", "0"));
    Color haloColor = getColor(aProperties, "halo.color", "FF444444");

    fRegularStyle.setHaloEnabled(haloThickness > 0);
    fRegularStyle.setHaloColor(haloColor);
    fRegularStyle.setHaloThickness(haloThickness);
    fRegularStyle.setLineWidth(lineThickness);

    fSelectedStyle.setHaloEnabled(haloThickness > 0);
    fSelectedStyle.setHaloColor(fRegularStyle.getSelectionColor());
    fSelectedStyle.setHaloThickness(haloThickness);
    fSelectedStyle.setLineWidth(lineThickness);
    fSelectedStyle.setSelectionColor(haloThickness == 0 ? fRegularStyle.getSelectionColor() : null);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof MilitarySymbologyModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor);
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ILspLayer layer;
    if (aModel.getModelDescriptor() instanceof MilitarySymbologyModelDescriptor) {
      TLcdClusteringTransformer transformer = TLcdClusteringTransformer.newBuilder()
                                                                       .classifier(new MilitarySymbolClassifier())
                                                                       .defaultParameters()
                                                                         .clusterSize(100)
                                                                         .minimumPoints(2)
                                                                         .build()
                                                                       .forClass("SEA")
                                                                         .noClustering()
                                                                         .build()
                                                                       .build();
      ILcdModel model = TLcdTransformingModelFactory.createTransformingModel(aModel, transformer);
      layer = TLspAPP6ALayerBuilder.newBuilder()
                                   .model(model)
                                   .defaultStyle(fRegularStyle)
                                   .bodyStyler(TLspPaintState.REGULAR, fRegularStyler)
                                   .bodyStyler(TLspPaintState.SELECTED, fSelectedStyler)
                                   .labelStyler(TLspPaintState.REGULAR, fRegularLabelStyler)
                                   .labelStyler(TLspPaintState.SELECTED, fSelectedLabelStyler)
                                   .objectWorldMargin(100e3)
                                   .build();
    } else {
      layer = TLspShapeLayerBuilder.newBuilder()
                                   .model(aModel)
                                   .selectable(false)
                                   .bodyStyler(
                                       TLspPaintState.REGULAR,
                                       new TLspStyler(
                                           TLspFillStyle.newBuilder()
                                                        .color(new Color(0.5f, 0.75f, 1f, 0.6f))
                                                        .stipplePattern(TLspFillStyle.StipplePattern.HATCHED)
                                                        .build(),
                                           TLspLineStyle.newBuilder().build()
                                       )
                                   )
                                   .objectWorldMargin(100e3)
                                   .build();
    }

    return Collections.singletonList(layer);
  }

}
