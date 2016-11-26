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
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

/**
 * Layer factory that can create layers for earth tileset models.
 */
public class UAVScreenOverlayLayerFactory extends AbstractLayerFactory {

  private static final String LINECOLOR = "uav.overlay.lineColor";
  private static final String LINEWIDTH = "uav.overlay.lineWidth";
  private static final String SELECTED_LINECOLOR = "uav.overlay.selectedLineColor";
  private static final String SELECTED_LINEWIDTH = "uav.overlay.selectedLineWidth";
  public static final TLspPaintRepresentation OVERLAY = TLspPaintRepresentation.getInstance("Overlay", TLspPaintRepresentation.HANDLE.getSortOrder() - 1);
  public static final TLspPaintRepresentationState OVERLAY_EDITED = TLspPaintRepresentationState.getInstance(OVERLAY, TLspPaintState.EDITED);

  private double fSensitivity;

  private Color fLineColor;
  private float fLineWidth;
  private Color fSelectedLineColor;
  private float fSelectedLineWidth;

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createOverlayLayer(aModel));
  }

  public ILspLayer createOverlayLayer(ILcdModel aModel) {
    final GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance().getSharedValue("videoStream");

    TLspLayer layer = new TLspLayer(aModel, aModel.getModelDescriptor().getDisplayName());
    layer.setCulling(false);
    final ScreenSpaceTexturingPainter painter = new ScreenSpaceTexturingPainter();

    TLspLineStyle def = TLspLineStyle.newBuilder()
                                     .color(fLineColor)
                                     .width(fLineWidth)
                                     .build();
    TLspLineStyle sel = TLspLineStyle.newBuilder()
                                     .color(fSelectedLineColor)
                                     .width(fSelectedLineWidth)
                                     .build();
    painter.setStyler(TLspPaintState.REGULAR, def);
    painter.setStyler(TLspPaintState.SELECTED, sel);

    videoStream.addVideoStreamListener(new GStreamerVideoStream.VideoStreamListener() {
      @Override
      public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
        painter.setVideoStreamDirty(aWidth, aHeight, aImage);
      }
    });

    painter.setVideoStream(videoStream);
    painter.setVideoStreamDirty(videoStream.getWidth(), videoStream.getHeight(), videoStream
        .getCurrentBuffer());

    layer.addPaintRepresentation(OVERLAY);
    layer.setPainter(OVERLAY, painter);

    layer.setEditable(true);
    layer.setSelectable(true);
    layer.setCulling(false);

    ScreenSpaceBoundsEditor editor = new ScreenSpaceBoundsEditor();
    layer.setEditor(OVERLAY, editor);
    layer.setVisible(OVERLAY_EDITED, true);
    layer.setLabel("UAV Video Overlay");
    return layer;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fSensitivity = Double.parseDouble(aProperties.getProperty("handle.sensitivity", "10.0"));
    fLineWidth = Float.parseFloat(aProperties.getProperty(LINEWIDTH, "1.0"));
    fLineColor = new Color(Integer
                               .parseInt(aProperties.getProperty(LINECOLOR, "FFFFFF"), 16));
    fSelectedLineWidth = Float.parseFloat(aProperties.getProperty(SELECTED_LINEWIDTH, "1.0"));
    fSelectedLineColor = new Color(Integer.parseInt(aProperties.getProperty(SELECTED_LINECOLOR, "00AAFF"), 16));
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName() != null &&
           aModel.getModelDescriptor().getTypeName().equals("UAVScreenOverlay");
  }
}
