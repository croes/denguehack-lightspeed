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

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;

/**
 * Creates the layers for indicating a specific point of the 2D video on the 3D terrain.
 */
public class UAVProjectedPointLayerFactory extends AbstractLayerFactory {

  private String fIconImageSource;
  private double fIconScale;
  private double fIconOffsetX;
  private double fIconOffsetY;

  private final ILspImageProjectionLayer fImageProjectionLayer;

  public UAVProjectedPointLayerFactory(ILspImageProjectionLayer aImageProjectionLayer) {
    fImageProjectionLayer = aImageProjectionLayer;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance()
                                                                       .getSharedValue("videoStream");
    if (videoStream == null) {
      throw new RuntimeException("Could not find a video stream");
    }

    TLspLayer layer = new TLspLayer(aModel, aModel.getModelDescriptor().getDisplayName());
    layer.setCulling(false);
    layer.setSelectable(false);
    layer.setPainter(TLspPaintRepresentation.BODY, new TLspShapePainter());
    Image iconImage = TLcdImageIcon.getImage(fIconImageSource);
    layer.setStyler(
        TLspPaintRepresentationState.REGULAR_BODY,
        new ProjectedPointStyler(
            aModel,
            fImageProjectionLayer,
            videoStream,
            TLspIconStyle.newBuilder().
                icon(new TLcdImageIcon(iconImage)).
                             scale(fIconScale).
                             offset(fIconOffsetX, fIconOffsetY).
                             build()
        )
    );
    layer.setLabel("UAV Projected Point");
    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return "UAVProjectedPoint".equals(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fIconImageSource = aProperties.getProperty("uav.projected.icon.image", "samples/lightspeed/demo/icons/down.png");
    fIconScale = Double.parseDouble(aProperties.getProperty("uav.projected.icon.scale", "0.5"));
    fIconOffsetX = Double.parseDouble(aProperties.getProperty("uav.projected.icon.offsetX", "0"));
    fIconOffsetY = Double.parseDouble(aProperties.getProperty("uav.projected.icon.offsetY", "-16"));
  }

  /**
   * Styler that uses the projection of a point on the terrain as geometry.
   */
  private static class ProjectedPointStyler extends AbstractProjectedPointStyler {

    private final ALspStyle fStyle;

    public ProjectedPointStyler(ILcdModel aModel, ILspImageProjectionLayer aImageProjectionLayer, GStreamerVideoStream aVideoStream, ALspStyle aStyle) {
      super(aModel, aImageProjectionLayer, aVideoStream);
      fStyle = aStyle;
    }

    protected void styleImpl(Object aObject, UAVVideoPoint aUAVVideoPoint, ALspStyleCollector aStyleCollector, ILcd3DEditablePoint aProjectedModelPoint) {
      // Use the projected point as geometry
      aStyleCollector.object(aObject).geometry(aProjectedModelPoint).style(fStyle).submit();
    }
  }
}
