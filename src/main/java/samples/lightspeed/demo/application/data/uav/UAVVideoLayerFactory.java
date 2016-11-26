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

import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.luciad.model.ILcdModel;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjector;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjectionLayerBuilder;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjectionStyle;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjector;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.util.opengl.texture.ALspTextureObject;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

/**
 * Layer factory for the UAV video painted on the terrain.
 */
public class UAVVideoLayerFactory extends AbstractLayerFactory {

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.<ILspLayer>singletonList(createRegionLayer(aModel));
  }

  public ILspImageProjectionLayer createRegionLayer(ILcdModel aModel) {
    final GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework
        .getInstance().getSharedValue("videoStream");

    ILspImageProjectionLayer layer = TLspImageProjectionLayerBuilder.newBuilder().
        model(aModel).
                                                                        label("UAV Feed").
                                                                        build();
    TLspImageProjector projector = new TLspImageProjector();

    UAVVideoStyler imageProvider = new UAVVideoStyler(
        projector,
        layer,
        videoStream
    );
    layer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, imageProvider);

    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName() != null &&
           aModel.getModelDescriptor().getTypeName().equals("UAVRegion");
  }

  public static class UAVVideoStyler
      extends ALspStyler implements GStreamerVideoStream.VideoStreamListener {

    private final TLspImageProjector fProjector;
    private final ILspImageProjectionLayer fLayer;
    private final GStreamerVideoStream fVideoStream;

    private Map<ILspView, IntBufferTextureObject> fTextures = new TLcdWeakIdentityHashMap<ILspView, IntBufferTextureObject>();

    public UAVVideoStyler(TLspImageProjector aProjector, ILspImageProjectionLayer aLayer, GStreamerVideoStream aVideoStream) {
      fProjector = aProjector;
      fLayer = aLayer;
      fVideoStream = aVideoStream;
      fVideoStream.addVideoStreamListener(this, true);
    }

    @Override
    public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
      if (aCamera == null) {
        return;
      }

      for (IntBufferTextureObject texture : fTextures.values()) {
        texture.setBuffer(aImage);
      }

      fProjector.setEyePoint(aCamera.getEye());
      fProjector.setReferencePoint(aCamera.getRef());
      fProjector.setUpVector(aCamera.getUp());
      fProjector.setFieldOfView(aCamera.getFov());
      fProjector.setAspectRatio(aCamera.getAspectRatio());
      fProjector.setRange(1e5);

      fireStyleChangeEvent();
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        aStyleCollector
            .object(o)
            .style(TLspImageProjectionStyle.newBuilder()
                                           .projector(fProjector)
                                           .image(getProjectedImage(fProjector, aContext))
                                           .build()
            )
            .submit();
      }
    }

    private ALspTextureObject getProjectedImage(ILspImageProjector aProjector, TLspContext aContext) {
      IntBufferTextureObject texture = fTextures.get(aContext.getView());
      if (texture == null) {
        texture = new IntBufferTextureObject(fVideoStream.getWidth(), fVideoStream.getHeight());
        texture.setBuffer(fVideoStream.getCurrentBuffer());
        fTextures.put(aContext.getView(), texture);
      }
      return texture;
    }

    public TLspImageProjector getProjector() {
      return fProjector;
    }
  }
}
