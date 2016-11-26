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
package samples.wms.client.gxy;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import samples.common.MapColors;
import samples.gxy.common.labels.GXYLabelPainterFactory;
import samples.gxy.decoder.MapSupport;
import samples.wms.client.common.WMSGetFeatureInfoMouseListener;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.wms.client.gxy.TLcdWMSProxyGXYPainter;
import com.luciad.wms.client.gxy.tiled.TLcdGXYTiledWMSProxyPainter;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSGetFeatureInfoContext;

/**
 * Lightspeed specific implementation of WMSGetFeatureInfoMouseListener.
 */
public abstract class GXYWMSGetFeatureInfoMouseListener extends WMSGetFeatureInfoMouseListener<ILcdGXYView, ILcdGXYLayer> {

  private ILcdGXYPainterStyle fFillStyle = new TLcdGXYPainterColorStyle(MapColors.BACKGROUND_FILL);
  private ILcdGXYPainterStyle fLineStyle = TLcdStrokeLineStyle.newBuilder().lineWidth(2).color(MapColors.BACKGROUND_OUTLINE).selectionColor(MapColors.SELECTION_OUTLINE).build();

  /**
   * Create a new mouse listener that performs GetFeatureInfo requests for the given view.
   * @param aView the view
   */
  public GXYWMSGetFeatureInfoMouseListener(ILcdGXYView aView) {
    super(aView);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    // Use mouseClicked because of EditPanController, which delays mousePressed events before sending them to the
    // select controller.
    handleEvent(e);
  }

  @Override
  protected TLcdWMSGetFeatureInfoContext getFeatureInfoContext(ALcdWMSProxy aProxy, ILcdGXYLayer aLayer, ILcdGXYView aView, int aX, int aY) {
    ILcdGXYPainter gxyPainter = aLayer.getGXYPainter(aProxy);
    TLcdGXYContext context = new TLcdGXYContext(aView, aLayer);
    if (gxyPainter instanceof TLcdWMSProxyGXYPainter) {
      TLcdWMSProxyGXYPainter proxyPainter = (TLcdWMSProxyGXYPainter) gxyPainter;
      return proxyPainter.getFeatureInfoContext(aX, aY, context);
    } else if (gxyPainter instanceof TLcdGXYTiledWMSProxyPainter) {
      TLcdGXYTiledWMSProxyPainter proxyPainter = (TLcdGXYTiledWMSProxyPainter) gxyPainter;
      return proxyPainter.getFeatureInfoContext(aX, aY, context);
    }
    return null;
  }

  @Override
  protected ILcdPoint getQueryLocationInModelCoordinates(ILcdGXYLayer aLayer, ILcdGXYView aView, int aX, int aY) {
    ILcd3DEditablePoint worldPoint = new TLcdXYZPoint();
    aView.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(new Point(aX, aY), worldPoint);
    try {
      ILcdModelReference modelReference = aLayer.getModel().getModelReference();
      ILcdModelXYWorldTransformation m2w = (ILcdModelXYWorldTransformation) aLayer.getModelXYWorldTransfoClass().newInstance();
      m2w.setModelReference(modelReference);
      m2w.setXYWorldReference(aView.getXYWorldReference());

      ILcd3DEditablePoint modelPoint = modelReference.makeModelPoint().cloneAs3DEditablePoint();
      m2w.worldPoint2modelSFCT(worldPoint, modelPoint);
      return modelPoint;
    } catch (Exception e) {
      // Should not happen
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void addFeatureInfoLayer(ILcdGXYView aView, ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setLabel(aModel.getModelDescriptor().getDisplayName());

    layer.setModel(aModel);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(), false));
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setLineStyle(fLineStyle);
    painter.setFillStyle(fFillStyle);
    painter.setMode(fFillStyle == null ? ALcdGXYAreaPainter.OUTLINED : ALcdGXYAreaPainter.OUTLINED_FILLED);
    painter.setIcon(createIcon(false));
    painter.setSelectedIcon(createIcon(true));
    layer.setGXYPainterProvider(painter);
    layer.setEditable(false);

    TLcdGXYLabelPainter labelPainter = GXYLabelPainterFactory.createGXYLabelPainter(aModel, false);
    layer.setGXYLabelPainterProvider(new HideLabelLabelPainterProvider(labelPainter));
    layer.setLabelsEditable(false);
    layer.setLabeled(true);

    aView.addGXYLayer(layer);
  }

  public static ILcdIcon createIcon(boolean aSelected) {
    int type = TLcdSymbol.RECT;
    int size = aSelected ? 8 : 6;
    Color borderColor = aSelected ? new Color(255, 0, 0, 128) : new Color(255, 255, 255, 128);
    TLcdSymbol symbol = new TLcdSymbol(type, size, borderColor, MapColors.ICON_FILL);
    symbol.setAntiAliasing(true);
    return symbol;
  }

  private static class HideLabelLabelPainterProvider implements ILcdGXYLabelPainterProvider {

    private ILcdGXYLabelPainterProvider fDelegate;

    private HideLabelLabelPainterProvider(ILcdGXYLabelPainterProvider aDelegate) {
      fDelegate = aDelegate;
    }

    @Override
    public ILcdGXYLabelPainter getGXYLabelPainter(Object aObject) {
      if (!WMSGetFeatureInfoMouseListener.shouldPaintLabel(aObject)) {
        return null;
      }
      return fDelegate.getGXYLabelPainter(aObject);
    }

    @Override
    public Object clone() {
      try {
        HideLabelLabelPainterProvider clone = (HideLabelLabelPainterProvider) super.clone();
        clone.fDelegate = (ILcdGXYLabelPainterProvider) fDelegate.clone();
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
