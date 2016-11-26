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
package samples.lightspeed.internal.havelsan.radar;

import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Map;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjector;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjectionLayerBuilder;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjectionStyle;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjector;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.util.opengl.texture.ALspTextureObject;

import samples.lightspeed.demo.application.data.uav.IntBufferTextureObject;

/**
 * @author tomn
 * @since 2012.0
 */
public class RadarLayerFactory extends ALspSingleLayerFactory {
  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    VideoStream stream = (VideoStream) aModel.elements().nextElement();

    RadarStyler styler = new RadarStyler(stream);

    return TLspImageProjectionLayerBuilder.newBuilder()
                                          .model(aModel)
                                          .label("Radar video")
                                          .styler(styler)
                                          .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof RadarModelDescriptor;
  }

  private class RadarStyler extends ALspStyler implements VideoStream.Listener {

    private final TLspImageProjector fProjector;
    private final VideoStream fVideoStream;

    private Map<ILspView, IntBufferTextureObject> fTextures = new TLcdWeakIdentityHashMap<ILspView, IntBufferTextureObject>();

    public RadarStyler(VideoStream aVideoStream) {
      TLcdLonLatPoint ll = new TLcdLonLatPoint(-117.5, 39.5);
      TLcdXYZPoint xyz = new TLcdXYZPoint();
      TLcdEllipsoid.DEFAULT.llh2geocSFCT(ll, xyz);

      fProjector = new TLspImageProjector();
      fProjector.lookAt(xyz, 0.9e6, 30, -75, 0, new TLcdGeocentricReference());
      fProjector.setRange(3e6);
      fVideoStream = aVideoStream;
      fVideoStream.addVideoStreamListener(this);
    }

    @Override
    public void frame(int aWidth, int aHeight, IntBuffer aImage) {
      for (IntBufferTextureObject texture : fTextures.values()) {
        texture.setBuffer(aImage);
      }

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
  }
}
