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
package samples.realtime.lightspeed.clusterLabeling;

import static samples.realtime.gxy.clusterLabeling.MainPanel.*;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import samples.common.AScaleSupport;
import samples.lightspeed.labels.util.FixedTextProviderStyle;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.editor.label.TLspLabelEditor;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

import samples.gxy.common.AntiAliasedIcon;

/**
 * Layer factory for track models
 */
class TrackLayerFactory extends ALspSingleLayerFactory {

  private static int sLayerCount = 0;

  private static final TLspPaintRepresentation ICON = TLspPaintRepresentation.getInstance("ICON", TLspPaintRepresentation.BODY.getSortOrder() + 1);

  private static final String ICON_SUBLABEL_ID = "icon";
  private static final String LABEL_SUBLABEL_ID = "label";

  private final AnimatedDeclutterLabelingAlgorithmProvider fLabelingAlgorithmProvider;

  public TrackLayerFactory(AnimatedDeclutterLabelingAlgorithmProvider aLabelingAlgorithmProvider) {
    fLabelingAlgorithmProvider = aLabelingAlgorithmProvider;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return new TrackModelFilter().accept(aModel);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILcdIcon bodyIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 4, Color.black));
    TLspIconStyle bodyStyler = TLspIconStyle.newBuilder().icon(bodyIcon).build();

    TLspLayer layer = new TLspLayer(aModel);
    layer.setLabel(aModel.getModelDescriptor().getDisplayName() + " " + sLayerCount++);

    TLspShapePainter bodyPainter = new TLspShapePainter();
    bodyPainter.setStyler(TLspPaintState.REGULAR, bodyStyler);
    layer.setPainter(TLspPaintRepresentation.BODY, bodyPainter);

    TLspLabelPainter labelPainter = new TLspLabelPainter();
    labelPainter.setStyler(TLspPaintState.REGULAR, new LabelStyler(fLabelingAlgorithmProvider, false));
    layer.setPainter(TLspPaintRepresentation.LABEL, labelPainter);

    // Icons are painted using a label painter for the purpose of decluttering, but they represent the
    // domain object itself. Because of that we use a custom painter representation which is painted before
    // the labels are painted. This also makes it possible to only make the text labels invisible, without
    // hiding the icon labels.
    layer.addPaintRepresentation(ICON);
    TLspLabelPainter iconPainter = new TLspLabelPainter();
    iconPainter.setStyler(TLspPaintState.REGULAR, new LabelStyler(fLabelingAlgorithmProvider, true));
    layer.setPainter(ICON, iconPainter);

    layer.setEditor(TLspPaintRepresentation.BODY, new TLspShapeEditor());
    layer.setEditor(TLspPaintRepresentation.LABEL, new TLspLabelEditor());
    layer.setEditor(ICON, new TLspLabelEditor());
    layer.setEditable(true);

    layer.setScaleRange(TLspPaintRepresentation.LABEL, new TLcdInterval(AScaleSupport.mapScale2InternalScale(1.0 / 50000000.0, -1.0, null), Double.MAX_VALUE));

    return layer;
  }

  private static class LabelStyler extends ALspLabelStyler {

    private static final Font FONT = new Font("Dialog", Font.PLAIN, 10);

    private final boolean fIcon;
    private final ILspLabelPriorityProvider fPriorityProvider;
    private final AnimatedDeclutterLabelingAlgorithmProvider fLabelingAlgorithmProvider;
    private final ALspStyle[] fIconStyles;
    private final ALspStyle[] fLabelStyles;

    public LabelStyler(AnimatedDeclutterLabelingAlgorithmProvider aLabelingAlgorithmProvider, boolean aIcon) {
      fIcon = aIcon;
      fPriorityProvider = aLabelingAlgorithmProvider;
      fLabelingAlgorithmProvider = aLabelingAlgorithmProvider;
      ILcdIcon labelIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 17, Color.black, new Color(240, 215, 140)));
      fIconStyles = new ALspStyle[]{
          TLspIconStyle.newBuilder().icon(labelIcon).build(),
          TLspPinLineStyle.newBuilder().pinEndPosition(TLspPinLineStyle.PinEndPosition.MIDDLE_OF_BOUNDS).build()
      };
      fLabelStyles = new ALspStyle[]{
          TLspTextStyle.newBuilder().font(FONT).haloThickness(1.0f).haloColor(Color.black).textColor(Color.white).build(),
          TLspLabelBoxStyle.newBuilder().haloThickness(0.0f).frameThickness(1.0f).frameColor(Color.black).build(),
          FixedTextProviderStyle.newBuilder().text("Label").build(),
          TLspPinLineStyle.newBuilder().pinEndPosition(TLspPinLineStyle.PinEndPosition.MIDDLE_OF_BOUNDS_ON_EDGE).build()
      };
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        aStyleCollector.object(object).algorithm(fLabelingAlgorithmProvider).priority(fPriorityProvider);
        if (fIcon) {
          aStyleCollector.label(ICON_SUBLABEL_ID).styles(fIconStyles);
        } else {
          aStyleCollector.label(LABEL_SUBLABEL_ID).anchorLabel(ICON, ICON_SUBLABEL_ID).styles(fLabelStyles);
        }
        aStyleCollector.submit();
      }
    }
  }
}
