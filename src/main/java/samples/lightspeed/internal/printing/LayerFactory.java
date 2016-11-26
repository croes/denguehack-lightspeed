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
package samples.lightspeed.internal.printing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;
import com.luciad.view.gxy.ILcdGXYMultiLabelPriorityProvider;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLabelPainterLocationLabelingAlgorithm;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.integration.gxy.TLspGXYLayerAdapter;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 *
 */
public class LayerFactory {
  private LayerFactory() {
  }

  public static TLspGXYLayerAdapter createGXYLayerAdapter(ILspStyledLayer aLayer) {
    Object referenceElem = aLayer.getModel().elements().nextElement();
    TLcdGXYLayer gxyLayer = new TLcdGXYLayer(aLayer.getModel(), aLayer.getLabel() + " (GXY)");
    ILcdGXYPainterProvider bodyPainter = createBodyPainter(aLayer, referenceElem);
    if (bodyPainter != null) {
      gxyLayer.setGXYPainterProvider(bodyPainter);
    }
    ILcdGXYLabelPainterProvider labelPainter = createLabelPainter(aLayer, referenceElem);
    if (labelPainter != null) {
      gxyLayer.setLabeled(true);
      gxyLayer.setGXYLabelPainterProvider(labelPainter);
    }
    TLspGXYLayerAdapter gxyLayerAdapter = new TLspGXYLayerAdapter(gxyLayer);
    TLcdGXYLabelPainterLocationLabelingAlgorithm labelingAlgorithm = new TLcdGXYLabelPainterLocationLabelingAlgorithm();
    labelingAlgorithm.setLabelPriorityProvider(new ILcdGXYMultiLabelPriorityProvider() {
      @Override
      public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
        return ((ILcdDataObject) aObject).getValue("COUNTRY").hashCode();
      }
    });
    gxyLayerAdapter.setGXYLayerLabelingAlgorithm(labelingAlgorithm);
    return gxyLayerAdapter;
  }

  private static ILcdGXYPainterProvider createBodyPainter(ILspStyledLayer aLayer, Object aObject) {
    if (aObject instanceof ILcdShapeList) {
      ILcdShape shape = ((ILcdShapeList) aObject).getShape(0);
      ILcdGXYPainterProvider bodyPainter = createBodyPainter(aLayer, shape);
      return new TLcdGXYShapeListPainter(bodyPainter);
    }

    if (!(aObject instanceof ILcdPointList)) {
      throw new IllegalArgumentException("Expected point list but got " + aObject.getClass());
    }
    List<ALspStyle> styles = getStyles(aLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY), aObject);
    TLcdGXYPointListPainter bodyPainter = new TLcdGXYPointListPainter();
    TLspLineStyle lineStyle = getStyle(styles, TLspLineStyle.class);
    TLspFillStyle fillStyle = getStyle(styles, TLspFillStyle.class);
    bodyPainter.setMode(lineStyle != null && fillStyle != null ? ALcdGXYAreaPainter.OUTLINED_FILLED :
                        lineStyle != null ? ALcdGXYAreaPainter.OUTLINED :
                        ALcdGXYAreaPainter.FILLED);
    if (lineStyle != null) {
      bodyPainter.setLineStyle(new TLcdGXYPainterColorStyle(lineStyle.getColor()));
    }
    if (fillStyle != null) {
      bodyPainter.setFillStyle(new TLcdGXYPainterColorStyle(fillStyle.getColor()));
    }
    return bodyPainter;
  }

  private static ILcdGXYLabelPainterProvider createLabelPainter(ILspStyledLayer aLayer, final Object aObject) {
    List<ALspStyle> styles = getStyles(aLayer.getStyler(TLspPaintRepresentationState.REGULAR_LABEL), aObject);
    TLspTextStyle textStyle = getStyle(styles, TLspTextStyle.class);
    final ALspLabelTextProviderStyle textProviderStyle = getStyle(styles, ALspLabelTextProviderStyle.class);
    TLspLabelBoxStyle boxStyle = getStyle(styles, TLspLabelBoxStyle.class);
    TLspPinLineStyle pinStyle = getStyle(styles, TLspPinLineStyle.class);
    if (textProviderStyle == null) {
      throw new IllegalArgumentException("Missing ALspLabelTextProviderStyle");
    }
    TLcdGXYLabelPainter labelPainter = new TLcdGXYLabelPainter() {
      @Override
      protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {
        return textProviderStyle.getText(getObject(), 0, null);
      }
    };
    labelPainter.setFont(textStyle.getFont());
    labelPainter.setForeground(textStyle.getTextColor());
    labelPainter.setHaloColor(textStyle.getHaloColor());
    labelPainter.setHaloThickness((int) textStyle.getHaloThickness());
    if (boxStyle != null) {
      labelPainter.setFrame(true);
      labelPainter.setFilled(boxStyle.isFilled());
      labelPainter.setBackground(boxStyle.getFillColor());
    }
    if (pinStyle != null) {
      labelPainter.setWithPin(true);
      labelPainter.setPinColor(pinStyle.getColor());
      labelPainter.setHaloPinEnabled(false);
    }
    return labelPainter;
  }

  private static List<ALspStyle> getStyles(ILspStyler aStyler, Object aObject) {
    final List<ALspStyle> styles = new ArrayList<ALspStyle>();
    List<Object> objects = Collections.singletonList(aObject);
    aStyler.style(objects, new ALspLabelStyleCollector(objects) {
      @Override
      protected void submitImpl() {
        styles.addAll(getStyles());
      }
    }, new TLspContext());
    return styles;
  }

  private static <T extends ALspStyle> T getStyle(Collection<ALspStyle> aStyles, Class<T> aStyleClass) {
    for (ALspStyle style : aStyles) {
      if (aStyleClass.isAssignableFrom(style.getClass())) {
        return (T) style;
      }
    }
    return null;
  }
}
