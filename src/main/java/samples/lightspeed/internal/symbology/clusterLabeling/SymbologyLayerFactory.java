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
package samples.lightspeed.internal.symbology.clusterLabeling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.symbology.app6a.model.ILcdAPP6AShape;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspToggleStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.gxy.common.AntiAliasedIcon;
import samples.realtime.lightspeed.clusterLabeling.AnimatedDeclutterLabelingAlgorithmProvider;

/**
 * Layer factory that makes sure that the bodies of symbology objects are painted as a label.
 */
public class SymbologyLayerFactory extends ALspSingleLayerFactory {

  private static int sLayerCount = 0;

  private static final String ICON_SUBLABEL_ID = "icon";

  private final AnimatedDeclutterLabelingAlgorithmProvider fLabelingAlgorithmProvider;

  public SymbologyLayerFactory(AnimatedDeclutterLabelingAlgorithmProvider aLabelingAlgorithmProvider) {
    fLabelingAlgorithmProvider = aLabelingAlgorithmProvider;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return SymbologyMainPanel.SYMBOLOGY_MODEL_TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    String label = aModel.getModelDescriptor().getDisplayName() + " " + sLayerCount++;
    ILspInteractivePaintableLayer layer = TLspAPP6ALayerBuilder.newBuilder()
                                                               .label(label)
                                                               .model(aModel)
                                                               .build();
    wrapStylers(layer, new PointSymbologyFilter());

    return layer;
  }

  protected final void wrapStylers(ILspInteractivePaintableLayer aLayer, ILcdFilter<Object> aSpecialStylerFilter) {
    // If possible, modify the layer by wrapping its styler
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer styledLayer = (ILspEditableStyledLayer) aLayer;

      ILspStyler oldRegularBodyStyler = aLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      ILspStyler oldRegularLabelStyler = aLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL);

      ILspStyler oldSelectedBodyStyler = aLayer.getStyler(TLspPaintRepresentationState.SELECTED_BODY);
      ILspStyler oldSelectedLabelStyler = aLayer.getStyler(TLspPaintRepresentationState.SELECTED_LABEL);

      ILspStyler oldEditedBodyStyler = aLayer.getStyler(TLspPaintRepresentationState.EDITED_BODY);
      ILspStyler oldEditedLabelStyler = aLayer.getStyler(TLspPaintRepresentationState.EDITED_LABEL);

      // Use a simple black circle instead of the symbol as body
      ILcdIcon bodyIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 4, Color.black));
      TLspIconStyle bodyStyler = TLspIconStyle.newBuilder().icon(bodyIcon).build();
      SwitchStyler regularBodyStyler = new SwitchStyler(oldRegularBodyStyler, bodyStyler, aSpecialStylerFilter);
      SwitchStyler selectedBodyStyler = new SwitchStyler(oldSelectedBodyStyler, bodyStyler, aSpecialStylerFilter);
      SwitchStyler editedBodyStyler = new SwitchStyler(oldEditedBodyStyler, bodyStyler, aSpecialStylerFilter);
      styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, regularBodyStyler);
      styledLayer.setStyler(TLspPaintRepresentationState.SELECTED_BODY, selectedBodyStyler);
      styledLayer.setStyler(TLspPaintRepresentationState.EDITED_BODY, editedBodyStyler);

      // Paint the symbol as a label to make sure it can be decluttered
      LabelStyler regularLabelStyler = new LabelStyler(fLabelingAlgorithmProvider, oldRegularBodyStyler, oldRegularLabelStyler);
      LabelStyler selectedLabelStyler = new LabelStyler(fLabelingAlgorithmProvider, oldSelectedBodyStyler, oldSelectedLabelStyler);
      LabelStyler editedLabelStyler = new LabelStyler(fLabelingAlgorithmProvider, oldEditedBodyStyler, oldEditedLabelStyler);
      styledLayer.setStyler(TLspPaintRepresentationState.REGULAR_LABEL, new SwitchStyler(oldRegularLabelStyler, regularLabelStyler, aSpecialStylerFilter));
      styledLayer.setStyler(TLspPaintRepresentationState.SELECTED_LABEL, new SwitchStyler(oldSelectedLabelStyler, selectedLabelStyler, aSpecialStylerFilter));
      styledLayer.setStyler(TLspPaintRepresentationState.EDITED_LABEL, new SwitchStyler(oldEditedLabelStyler, editedLabelStyler, aSpecialStylerFilter));
    }
  }

  /**
   * Styler that makes sure that the special styling is only performed for point symbols, and not for
   * tactical graphics.
   */
  private static class SwitchStyler extends ALspToggleStyler {

    private final ILcdFilter<Object> fSpecialStylerFilter;

    private SwitchStyler(ILspStyler aRegularStyler, ILspStyler aSpecialStyler, ILcdFilter<Object> aSpecialStylerFilter) {
      super(aRegularStyler, aSpecialStyler);
      fSpecialStylerFilter = aSpecialStylerFilter;
    }

    @Override
    protected boolean isUseSpecialStyler(Object aObject, TLspContext aContext) {
      return fSpecialStylerFilter.accept(aObject);
    }
  }

  private static class LabelStyler extends ALspLabelStyler {

    private static final TLspPinLineStyle PIN_LINE_STYLE = TLspPinLineStyle.newBuilder().build();

    private final ILspLabelPriorityProvider fPriorityProvider;
    private final AnimatedDeclutterLabelingAlgorithmProvider fLabelingAlgorithmProvider;
    private final ILspStyler fDelegateBodyStyler;
    private final ILspStyler fDelegateLabelStyler;

    public LabelStyler(AnimatedDeclutterLabelingAlgorithmProvider aLabelingAlgorithmProvider,
                       ILspStyler aDelegateBodyStyler,
                       ILspStyler aDelegateLabelStyler) {
      fPriorityProvider = aLabelingAlgorithmProvider;
      fLabelingAlgorithmProvider = aLabelingAlgorithmProvider;
      fDelegateBodyStyler = aDelegateBodyStyler;
      fDelegateLabelStyler = aDelegateLabelStyler;
    }

    @Override
    public void style(Collection<?> aObjects, final ALspLabelStyleCollector aStyleCollector, final TLspContext aContext) {
      // Use the delegate body styler to extract the icon symbol, and paint it as a label
      fDelegateBodyStyler.style(aObjects, new ALspStyleCollector(aObjects) {
        @Override
        protected void submitImpl() {
          List<ALspStyle> styles = new ArrayList<ALspStyle>(getStyles());
          styles.add(PIN_LINE_STYLE);

          // Use the icon of the symbol, and paint it as a label
          aStyleCollector.objects(getObjects());
          aStyleCollector.geometry(getStyleTargetProvider());
          aStyleCollector.algorithm(fLabelingAlgorithmProvider);
          aStyleCollector.priority(fPriorityProvider);
          aStyleCollector.label(ICON_SUBLABEL_ID);
          aStyleCollector.styles(styles);
          aStyleCollector.group(TLspLabelPlacer.DEFAULT_NO_DECLUTTER_GROUP);
          aStyleCollector.submit();
        }
      }, aContext);

      // Use the delegate label styler to extract the label information, and make sure they are anchored to the
      // label that is used to visualize the symbol.
      fDelegateLabelStyler.style(aObjects, new ALspLabelStyleCollector(aObjects) {
        @Override
        protected void submitImpl() {
          Collection<Object> labels = getLabels();
          for (Object label : labels) {
            aStyleCollector.objects(getObjects());
            // Make sure the labels of the symbol are positioned relative to the symbol icon
            aStyleCollector.anchorLabel(ICON_SUBLABEL_ID);
            aStyleCollector.priority(fPriorityProvider);
            aStyleCollector.label(label);
            aStyleCollector.styles(getStyles());
            aStyleCollector.group(TLspLabelPlacer.DEFAULT_NO_DECLUTTER_GROUP);
            aStyleCollector.algorithm(getAlgorithmProvider());
            aStyleCollector.submit();
          }
        }
      }, aContext);
    }
  }

  protected static class PointSymbologyFilter implements ILcdFilter<Object> {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdAPP6AShape) {
        ILcdAPP6AShape app6AShape = (ILcdAPP6AShape) aObject;
        return !app6AShape.isLine();
      }
      return false;
    }
  }

}
