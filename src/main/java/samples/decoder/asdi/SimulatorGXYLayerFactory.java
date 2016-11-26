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
package samples.decoder.asdi;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.format.asdi.TLcdASDIFlightPlanModelDescriptor;
import com.luciad.format.asdi.TLcdASDITrackModelDescriptor;
import samples.common.gxy.GXYScaleSupport;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.realtime.common.LockedGXYLayer;

/**
 * ILcdGXYLayerFactory that can create ILcdGXYLayers for the real-time track models.
 */
public class SimulatorGXYLayerFactory implements ILcdGXYLayerFactory, ILcdFilter {
  public static final double SWITCH_SCALE_MAP = 1d / 2000000d; //Map scale, meter/meter
  public static final double SWITCH_SCALE_INTERNAL = GXYScaleSupport.mapScale2InternalScale(SWITCH_SCALE_MAP, -1, null);

  public boolean accept(Object aObject) {
    if (aObject instanceof ILcdModel) {
      ILcdModel model = (ILcdModel) aObject;
      return model.getModelDescriptor() instanceof TrackSimulationModelDescriptor ||
             model.getModelDescriptor() instanceof TLcdASDITrackModelDescriptor ||
             model.getModelDescriptor() instanceof FlightPlanSimulationModelDescriptor ||
             model.getModelDescriptor() instanceof TLcdASDIFlightPlanModelDescriptor;
    }
    return false;
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TrackSimulationModelDescriptor ||
        aModel.getModelDescriptor() instanceof TLcdASDITrackModelDescriptor) {
      return createTrackLayer(aModel);
    } else if (aModel.getModelDescriptor() instanceof FlightPlanSimulationModelDescriptor ||
               aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanModelDescriptor) {
      return createFlightPlanLayer(aModel);
    }
    return null;
  }

  private ILcdGXYLayer createTrackLayer(final ILcdModel aModel) {
    //Add locking to the layer. This is needed because the live decoder adds
    //new data in another thread (see above).
    TLcdGXYLayer layer = new LockedGXYLayer();
    layer.setModel(aModel);
    layer.setLabel(aModel.getModelDescriptor().getDisplayName());
    layer.setGXYPen(new TLcdGeodeticPen(true));
    layer.setGXYPainterProvider(new ChooseForScalePainter(
        new TrackGXYPainterZoomedOut(), new TrackGXYPainterZoomedIn(), SWITCH_SCALE_INTERNAL));
    layer.setLabelsEditable(true);

    TLcdGXYDataObjectLabelPainter labeler = new TLcdGXYDataObjectLabelPainter() {
      protected void paintPin(Graphics aGraphics, int aMode, int aStartX, int aStartY, int aEndX, int aEndY) {
        aGraphics.setColor(Color.lightGray); //always paint the pin in gray
        aGraphics.drawLine(aStartX, aStartY, aEndX, aEndY);
      }
    };
    if (TrackSelectionMediator.isTOModel(aModel)) {
      //Aircraft ID
      labeler.setExpressions("ACID");
    } else {
      //flight id
      labeler.setExpressions("FlightId");
    }
    labeler.setHaloEnabled(true);
    labeler.setForeground(Color.white);
    labeler.setHaloColor(Color.black);
    labeler.setSelectionColor(Color.orange);
    labeler.setWithPin(true);
    labeler.setUseImageCache(true);
    layer.setGXYLabelPainterProvider(labeler);
    layer.setGXYLabelEditorProvider(labeler);

    //Only display labels if zoomed in
    layer.setLabelScaleRange(new TLcdInterval(SWITCH_SCALE_INTERNAL, Double.MAX_VALUE));
    layer.setLabeled(true);

    return layer;
  }

  private ILcdGXYLayer createFlightPlanLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new PaintSelectionOnlyLayer(aModel);
    layer.setGXYPen(new TLcdGeodeticPen(true));
    TLcdGXYPointListPainter flight_plan_painter = new TLcdGXYPointListPainter();
    flight_plan_painter.setLineStyle(new SelectionDashedLineStyle(new Color(68, 102, 68), Color.black));
    layer.setGXYPainterProvider(flight_plan_painter);
    return layer;
  }

}
