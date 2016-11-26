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
package samples.lightspeed.demo.application.data.uav;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZLine;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.services.terrain.ILspTerrainChangeListener;
import com.luciad.view.lightspeed.services.terrain.TLspTerrainChangeEvent;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

public class UAVFrustumLayerFactory extends AbstractLayerFactory {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(UAVFrustumLayerFactory.class);

  private static final String LINECOLOR = "uav.frustum.lineColor";
  private static final String LINEWIDTH = "uav.frustum.lineWidth";

  private final ILspImageProjectionLayer fImageProjectionLayer;

  private float fLineWidth;
  private Color fLineColor;

  public UAVFrustumLayerFactory(ILspImageProjectionLayer aImageProjectionLayer) {
    fImageProjectionLayer = aImageProjectionLayer;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createFrustumLayer(aModel));
  }

  public ILspLayer createFrustumLayer(final ILcdModel aModel) {
    final GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance().getSharedValue("videoStream");
    if (videoStream == null) {
      sLogger.error("UAV video stream not available!");
      return null;
    }

    FrustumStyler styler = new FrustumStyler(
        aModel,
        fImageProjectionLayer,
        videoStream,
        TLspLineStyle.newBuilder()
                     .width(fLineWidth)
                     .color(fLineColor)
                     .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                     .build()
    );

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, styler)
                                .culling(false)
                                .selectable(false)
                                .label("UAV Frustum")
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName() != null &&
           aModel.getModelDescriptor().getTypeName().equals("UAVFrustum");
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fLineWidth = Float.parseFloat(aProperties.getProperty(LINEWIDTH, "1.0"));
    fLineColor = new Color(Integer.parseInt(aProperties.getProperty(LINECOLOR, "FFFFFF"), 16));
  }

  /**
   * Styler that uses the frustum edges as geometry.
   */
  static class FrustumStyler extends AbstractProjectedPointStyler implements ILspTerrainChangeListener {

    private final ALspStyle fStyle;
    private ILspView fView = null;

    private FrustumStyler(ILcdModel aModel, ILspImageProjectionLayer aImageProjectionLayer, GStreamerVideoStream aVideoStream, ALspStyle aStyle) {
      super(aModel, aImageProjectionLayer, aVideoStream);
      fStyle = aStyle;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      if (fView != aContext.getView()) {
        if (fView != null) {
          fView.getServices().getTerrainSupport().removeTerrainChangeListener(this);
        }
        fView = aContext.getView();
        fView.getServices().getTerrainSupport().addTerrainChangeListener(this);
      }

      super.style(aObjects, aStyleCollector, aContext);
    }

    @Override
    public void terrainChanged(TLspTerrainChangeEvent aEvent) {
      fireStyleChangeEvent();
    }

    @Override
    protected void styleImpl(Object aObject, UAVVideoPoint aUAVVideoPoint, ALspStyleCollector aStyleCollector, ILcd3DEditablePoint aProjectedModelPoint) {
      VirtualCamera camera = getVideoStream().getCurrentCamera();
      if (camera == null) {
        return;
      }
      TLcdXYZLine line = new TLcdXYZLine(camera.getEye().cloneAs3DEditablePoint(),
                                         aProjectedModelPoint);
      aStyleCollector
          .object(aObject)
          .geometry(line)
          .style(fStyle)
          .submit();
    }
  }
}
