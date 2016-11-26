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
package samples.wms.client.lightspeed;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdResizeableIcon;
import samples.common.MapColors;
import samples.gxy.common.PaddedIcon;
import samples.gxy.common.labels.GXYLabelPainterFactory;
import samples.lightspeed.common.LabelStylerWrapper;
import samples.lightspeed.common.LspStyleUtil;
import samples.wms.client.common.WMSGetFeatureInfoMouseListener;
import samples.wms.client.gxy.GXYWMSGetFeatureInfoMouseListener;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspTouchInfo;
import com.luciad.view.lightspeed.layer.ALspWorldTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.wms.client.lightspeed.TLspWMSGetFeatureInfoContextQuery;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSGetFeatureInfoContext;

/**
 * Lightspeed specific implementation of WMSGetFeatureInfoMouseListener.
 */
public abstract class LspWMSGetFeatureInfoMouseListener extends WMSGetFeatureInfoMouseListener<ILspView, ILspLayer> {

  private TLspFillStyle fFillStyle = TLspFillStyle.newBuilder().color(MapColors.BACKGROUND_FILL).build();
  private TLspLineStyle fLineStyle = TLspLineStyle.newBuilder().width(2).color(MapColors.BACKGROUND_OUTLINE).build();
  private TLspIconStyle fIconStyle = TLspIconStyle.newBuilder().icon(createIcon(false)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
  private TLspIconStyle fSelectedIconStyle = TLspIconStyle.newBuilder().icon(createIcon(true)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();

  private static ILcdIcon createIcon(boolean aSelected) {
    ILcdIcon unselectedIcon = GXYWMSGetFeatureInfoMouseListener.createIcon(false);
    ILcdIcon selectedIcon = GXYWMSGetFeatureInfoMouseListener.createIcon(true);
    if (!aSelected) {
      return unselectedIcon;
    } else {
      TLcdCompositeIcon compositeIcon = new TLcdCompositeIcon();
      compositeIcon.addIcon(new TLcdResizeableIcon(new PaddedIcon(unselectedIcon, 1)));
      compositeIcon.addIcon(new TLcdResizeableIcon(selectedIcon));
      return compositeIcon;
    }
  }

  /**
   * Create a new mouse listener that performs GetFeatureInfo requests for the given view.
   * @param aView             the view.
   */
  public LspWMSGetFeatureInfoMouseListener(ILspAWTView aView) {
    super(aView);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    handleEvent(e);
  }

  @Override
  protected TLcdWMSGetFeatureInfoContext getFeatureInfoContext(ALcdWMSProxy aProxy, ILspLayer aLayer, ILspView aView, int aX, int aY) {
    if (!(aLayer instanceof ILspInteractivePaintableLayer)) {
      throw new IllegalArgumentException("Expected ILspInteractivePaintableLayer, but got " + aLayer);
    }
    ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
    TLspContext context = new TLspContext(aLayer, aView);
    TLspWMSGetFeatureInfoContextQuery query = new TLspWMSGetFeatureInfoContextQuery(aX, aY);
    return layer.query(query, context);
  }

  @Override
  protected ILcdPoint getQueryLocationInModelCoordinates(ILspLayer aLayer, ILspView aView, int aX, int aY) {
    if (!(aLayer instanceof ILspInteractivePaintableLayer)) {
      throw new IllegalArgumentException("Expected ILspInteractivePaintableLayer, but got " + aLayer);
    }
    ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
    ILcdPoint viewPoint = new TLcdXYPoint(aX, aY);
    TLspPaintedObjectsTouchQuery query = new TLspPaintedObjectsTouchQuery(TLspPaintRepresentationState.REGULAR_BODY, viewPoint, 1);
    TLspContext context = new TLspContext(aLayer, aView);
    Collection<ALspTouchInfo> result = layer.query(query, context);
    if (result != null) {
      for (ALspTouchInfo touchInfo : result) {
        if (touchInfo instanceof ALspWorldTouchInfo) {
          ALspWorldTouchInfo worldTouchInfo = (ALspWorldTouchInfo) touchInfo;
          ILcdModelXYZWorldTransformation m2w = context.getModelXYZWorldTransformation();
          ILcd3DEditablePoint modelPoint = m2w.getModelReference().makeModelPoint().cloneAs3DEditablePoint();
          try {
            m2w.worldPoint2modelSFCT(worldTouchInfo.getTouchedWorldPoint(), modelPoint);
            return modelPoint;
          } catch (TLcdOutOfBoundsException ignored) {
            // Try again, or return null
          }
        }
      }
    }
    return null;
  }

  @Override
  protected void addFeatureInfoLayer(ILspView aView, ILcdModel aModel) {
    // Create a label styler
    TLcdDataProperty labelProperty = GXYLabelPainterFactory.getDataModelLabelProperty(aModel.getModelDescriptor());
    ALspStyle labelStyle = labelProperty == null ?
                           TLspDataObjectLabelTextProviderStyle.newBuilder().build() :
                           TLspDataObjectLabelTextProviderStyle.newBuilder().expressions(labelProperty.getName()).build();
    TLspTextStyle textStyle = TLspTextStyle.newBuilder().elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    ALspLabelStyler labelStyler = TLspLabelStyler.newBuilder().styles(labelStyle, textStyle).build();
    // Make sure that no labels are painter for text/plain or text/html, since the labels don't provide useful information
    labelStyler = new LabelStylerWrapper(labelStyler) {
      @Override
      public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
        for (Object object : aObjects) {
          if (shouldPaintLabel(object)) {
            super.style(Collections.singletonList(object), aStyleCollector, aContext);
          }
        }
      }
    };

    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                               .model(aModel)
                                                               .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                                               .editableSupported(false)
                                                               .bodyStyler(TLspPaintState.REGULAR, createStyler(false))
                                                               .bodyStyler(TLspPaintState.SELECTED, createStyler(true))
                                                               .build();
    layer.setVisible(TLspPaintRepresentation.LABEL,
                     labelProperty != null &&
                     (labelProperty.getName().toLowerCase().contains("name") || TLcdCoreDataTypes.STRING_TYPE.equals(labelProperty.getType()))
    );
    aView.addLayer(layer);
  }

  private ALspStyler createStyler(boolean aSelected) {
    TLspIconStyle iconStyle = aSelected ? fSelectedIconStyle : fIconStyle;
    return LspStyleUtil.combinePointLineAndFill(iconStyle, fFillStyle == null ? Collections.singletonList(fLineStyle) : Arrays.asList(fFillStyle, fLineStyle));
  }
}
