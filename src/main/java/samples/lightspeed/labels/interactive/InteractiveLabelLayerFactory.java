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
package samples.lightspeed.labels.interactive;

import static java.util.Arrays.asList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JComponent;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.manipulation.ALspInteractiveLabelProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.ALspSwingLabelStyler;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Layer factory that is capable of creating a layer with interactive labels for
 * the city_125 SHP model.
 */
public class InteractiveLabelLayerFactory extends ALspSingleLayerFactory {

  private final ALspInteractiveLabelProvider fInteractiveLabelProvider;
  private final RegularSwingLabelComponent fRegularComponent;
  private final InteractiveSwingLabelComponent fInteractiveComponent;
  private final Map<Object, String> fComments;

  public InteractiveLabelLayerFactory(ALspInteractiveLabelProvider aInteractiveLabelProvider,
                                      RegularSwingLabelComponent aRegularComponent,
                                      InteractiveSwingLabelComponent aInteractiveComponent,
                                      Map<Object, String> aComments) {
    fInteractiveLabelProvider = aInteractiveLabelProvider;
    fRegularComponent = aRegularComponent;
    fInteractiveComponent = aInteractiveComponent;
    fComments = aComments;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return "city_125".equals(aModel.getModelDescriptor().getDisplayName());
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if ("city_125".equals(aModel.getModelDescriptor().getDisplayName())) {
      return createInteractiveLayer(aModel, fInteractiveLabelProvider, fRegularComponent, fInteractiveComponent, fComments);
    }
    return null;
  }

  private static ILspLayer createInteractiveLayer(ILcdModel aModel,
                                                  ALspInteractiveLabelProvider aInteractiveLabelProvider,
                                                  RegularSwingLabelComponent regularComponent,
                                                  InteractiveSwingLabelComponent interactiveComponent,
                                                  Map<Object, String> aComments) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    TLspPinLineStyle pinStyle = TLspPinLineStyle.newBuilder().width(1.0f).build();
    TLspLabelOpacityStyle colorStyle = TLspLabelOpacityStyle.newBuilder().color(new Color(255, 150, 150)).build();

    // Create stylers for interactive labels. These make sure that the labels added by
    // TLspInteractiveLabelsController aren't painted twice.
    ILspStyler regularComponentStyler = new ComponentStyler(aInteractiveLabelProvider, TLspPaintRepresentation.LABEL, TLspPaintState.REGULAR, regularComponent, interactiveComponent, aComments, pinStyle);
    ILspStyler selectedComponentStyler = new ComponentStyler(aInteractiveLabelProvider, TLspPaintRepresentation.LABEL, TLspPaintState.SELECTED, regularComponent, interactiveComponent, aComments, pinStyle, colorStyle);
    ILspStyler editedComponentStyler = new ComponentStyler(aInteractiveLabelProvider, TLspPaintRepresentation.LABEL, TLspPaintState.EDITED, regularComponent, interactiveComponent, aComments, pinStyle, colorStyle);

    return layerBuilder.model(aModel)
                       .label("Cities")
                       .selectable(true)
                       .bodyEditable(false)
                       .labelEditable(true)
                       .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, new Color(240, 215, 140))).build())
                       .bodyStyler(TLspPaintState.SELECTED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.red)).build())
                       .bodyStyler(TLspPaintState.EDITED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.orange)).build())
                       .labelStyler(TLspPaintState.REGULAR, regularComponentStyler)
                       .labelStyler(TLspPaintState.SELECTED, selectedComponentStyler)
                       .labelStyler(TLspPaintState.EDITED, editedComponentStyler)
                       .build();
  }

  /**
   * This styler has two modes: currently editing or not. This is because we want to display a
   * simple component when the object is not being edited by the interactive controller, but is
   * still being moved by an editor.
   */
  private static class ComponentStyler extends ALspSwingLabelStyler {

    private static final Object SUBLABEL_ID = 0;

    private final LabelComponentProvider fRegularComponentProvider;
    private final LabelComponentProvider fInteractiveComponentProvider;
    private final Collection<ALspStyle> fAdditionalStyles;

    // Used to track changes in the comments map.
    private final Map<Object, String> fComments;
    private final Map<Object, String> fCurrentComments = new TLcdWeakIdentityHashMap<>();

    public ComponentStyler(ALspInteractiveLabelProvider aInteractiveLabelProvider,
                           TLspPaintRepresentation aPaintRepresentation,
                           TLspPaintState aPaintState,
                           LabelComponentProvider aRegularComponentProvider,
                           LabelComponentProvider aInteractiveComponentProvider,
                           Map<Object, String> aComments,
                           ALspStyle... aAdditionalStyles) {
      super(aInteractiveLabelProvider,
            TLspPaintRepresentationState.getInstance(aPaintRepresentation, aPaintState),
            true);
      fRegularComponentProvider = aRegularComponentProvider;
      fInteractiveComponentProvider = aInteractiveComponentProvider;
      fComments = aComments;
      fAdditionalStyles = asList(aAdditionalStyles);
    }

    @Override
    protected boolean shouldInvalidateLabel(Object aObject, Object aSubLabelID, TLspContext aContext) {
      // Only invalidate labels when the content has actually changed.
      String oldComment = fCurrentComments.get(aObject);
      String newComment = fComments.get(aObject);
      if (newComment == null) {
        fCurrentComments.remove(aObject);
      } else {
        fCurrentComments.put(aObject, newComment);
      }
      return !Objects.equals(oldComment, newComment);
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        ALspStyle componentStyle = getComponentStyle(object, SUBLABEL_ID, aContext);
        if (componentStyle != null) {
          // Make sure interactive labels are never dropped by giving them a higher priority
          int priority = isInteractiveEditedLabel(object, SUBLABEL_ID, aContext) ? 0 : 10;
          List<ALspStyle> styles = new ArrayList<>(fAdditionalStyles);
          styles.add(componentStyle);
          aStyleCollector.object(object).label(SUBLABEL_ID).priority(priority).styles(styles).submit();
        }
      }
    }

    @Override
    protected JComponent getComponent(Object aObject, Object aSublabelId, TLspContext aContext) {
      if (isInteractiveEditedLabel(aObject, aSublabelId, aContext)) {
        // Make the label painter think that the label has the size of the interactive component
        return fInteractiveComponentProvider.getComponent(aObject, aSublabelId, aContext);
      } else {
        // Paint the regular (non-interactive) component
        return fRegularComponentProvider.getComponent(aObject, aSublabelId, aContext);
      }
    }
  }
}
