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
package samples.lightspeed.labels.placement;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.*;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * Layer factory for cities. This layer factory demonstrates how to configure label dependencies.
 * Each city has on icon label. Each icon label has a text label. Because the text label depends
 * on the icon label, it always moves along. Also when an icon label is omitted, the text label
 * disappears as well. A custom styler is used for this.
 */
public class CitiesLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    //Configure label stylers
    TLspTextStyle regularLabelStyle = TLspTextStyle.newBuilder().font(Font.decode("Default-BOLD-12")).textColor(new Color(255, 255, 255)).haloColor(Color.black).build();
    TLspTextStyle selectionLabelStyle = regularLabelStyle.asBuilder().textColor(Color.red).build();
    TLspTextStyle editedLabelStyle = regularLabelStyle.asBuilder().textColor(Color.orange).build();
    TLspIconStyle iconStyle = TLspIconStyle.newBuilder().icon(TLcdIconFactory.create(TLcdIconFactory.LAYER_CONTROL_ICON)).build();

    CityLabelStyler labelStyler = new CityLabelStyler(regularLabelStyle, iconStyle);
    CityLabelStyler selectedLabelStyler = new CityLabelStyler(selectionLabelStyle, iconStyle);
    CityLabelStyler editedLabelStyler = new CityLabelStyler(editedLabelStyle, iconStyle);

    ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
        .labelStyler(TLspPaintState.REGULAR, labelStyler)
            // other layer builder calls ...
        .model(aModel)
        .selectable(true)
        .labelEditable(true)
        .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, new Color(240, 215, 140))).build())
        .bodyStyler(TLspPaintState.SELECTED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.red)).build())
        .bodyStyler(TLspPaintState.EDITED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.orange)).build())
        .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
        .labelStyler(TLspPaintState.EDITED, editedLabelStyler)
        .synchronizePainters(false)
        .build();
    return layer;
  }

  private static class CityLabelStyler extends ALspLabelStyler implements ILspCustomizableStyler {

    private final PropertyChangeListener fCustomizableStyleListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        fireStyleChangeEvent(); // When the customizable style changes, a style change event should be fired
      }
    };

    private static final String PARENT_SUBLABEL_ID = "parent";
    private static final String CHILD_SUBLABEL_ID = "child";

    private final TLspCustomizableStyle fTextStyle;
    private final TLspCustomizableStyle fLabelOpacityStyle;
    private final TLspIconStyle fIconStyle;
    private final TLspPinLineStyle fPinStyle;
    private final TLspLabelBoxStyle fLabelBoxStyle;

    private final ILspLabelingAlgorithm fParentAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(4, EAST, NORTH, SOUTH));
    private final ILspLabelingAlgorithm fChildAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(14, EAST));

    public CityLabelStyler(TLspTextStyle aTextStyle, TLspIconStyle aIconStyle) {
      fTextStyle = createCustomizableStyle(aTextStyle);
      fLabelOpacityStyle = createCustomizableStyle(TLspLabelOpacityStyle.newBuilder().build());
      fIconStyle = aIconStyle;
      fPinStyle = TLspPinLineStyle.newBuilder().color(Color.white).build();
      fLabelBoxStyle = TLspLabelBoxStyle.newBuilder().padding(0).frameThickness(1.0f).frameColor(Color.black).filled(true).build();
    }

    private TLspCustomizableStyle createCustomizableStyle(ALspStyle aStyle) {
      TLspCustomizableStyle customizableStyle = new TLspCustomizableStyle(aStyle, true);
      customizableStyle.addPropertyChangeListener(fCustomizableStyleListener);
      return customizableStyle;
    }

    @Override
    public Collection<TLspCustomizableStyle> getStyles() {
      return Arrays.asList(fTextStyle, fLabelOpacityStyle);
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      List<ALspStyle> parentStyles = getParentStyles();
      List<ALspStyle> childStyles = getChildStyles();
      for (Object object : aObjects) {
        aStyleCollector.object(object).label(PARENT_SUBLABEL_ID).algorithm(fParentAlgorithm).styles(parentStyles).submit();
        aStyleCollector.object(object).label(CHILD_SUBLABEL_ID).anchorLabel(PARENT_SUBLABEL_ID).algorithm(fChildAlgorithm).styles(childStyles).submit();
      }
    }

    private List<ALspStyle> getParentStyles() {
      List<ALspStyle> styles = new ArrayList<ALspStyle>();
      styles.add(fPinStyle);
      styles.add(fLabelBoxStyle);
      styles.add(fIconStyle);
      if (fLabelOpacityStyle.isEnabled()) {
        styles.add(fLabelOpacityStyle.getStyle());
      }
      return styles;
    }

    private List<ALspStyle> getChildStyles() {
      List<ALspStyle> styles = new ArrayList<ALspStyle>();
      styles.add(fPinStyle);
      if (fTextStyle.isEnabled()) {
        styles.add(fTextStyle.getStyle());
      }
      if (fLabelOpacityStyle.isEnabled()) {
        styles.add(fLabelOpacityStyle.getStyle());
      }
      return styles;
    }
  }
}
