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
package samples.realtime.lightspeed.tracksimulator;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.labels.util.FixedTextProviderStyle;
import samples.lightspeed.timeview.model.TimeReference;
import samples.realtime.common.ITrajectory;

/**
 * Creates the layer used in the time view:
 * - The trajectory line, in terms of time vs. height.
 * - The departure and destination city icons and labels.
 */
public class TrajectoryTimeHeightLayerFactory extends ALspSingleLayerFactory {

  private static final TLspLineStyle LINE_STYLE = TLspLineStyle.newBuilder()
                                                               .color(Color.WHITE)
                                                               .width(2.)
                                                               .build();

  private static final TLspIconStyle DOT_STYLE = TLspIconStyle.newBuilder()
                                                              .icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 6, Color.WHITE, Color.WHITE))
                                                              .build();

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                .layerType(ILspLayer.LayerType.INTERACTIVE)
                                .culling(false)
                                .selectable(true)
                                .bodyStyler(TLspPaintState.REGULAR, null)
                                .labelStyler(TLspPaintState.REGULAR, null)
                                .bodyStyler(TLspPaintState.SELECTED, new TrajectoryAsHeightStyler())
                                .labelStyler(TLspPaintState.SELECTED, new CityLabelStyler())
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  /**
   * Style target provider that passes the input shape but associates it with the {@link TimeReference time reference}.
   */
  private static class TimeReferenceProvider extends ALspStyleTargetProvider {
    private final ILcdShape fShape;

    public TimeReferenceProvider(ILcdShape aShape) {
      fShape = aShape;
    }

    @Override
    public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
      return TimeReference.INSTANCE;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      aResultSFCT.add(fShape);
    }
  }

  private static class TrajectoryAsHeightStyler extends ALspStyler {
    private ILcdPolyline getTimeHeightTrajectory(ITrajectory aTrajectory) {
      TLcdXYZPoint tempPoint = new TLcdXYZPoint();
      TLcdXYPolyline polyline = new TLcdXYPolyline();

      for (int i = 0; i <= 100; i++) {
        long time = (long) (aTrajectory.getBeginTime() + (((double) i/100.0) * (aTrajectory.getEndTime() - aTrajectory.getBeginTime())));
        aTrajectory.getPositionAtTimeSFCT(time, tempPoint);
        polyline.insert2DPoint(polyline.getPointCount(), time, tempPoint.getZ());
      }

      return polyline;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        ITrajectory trajectory = (ITrajectory) object;
        aStyleCollector.objects(aObjects)
                       .geometry(new TimeReferenceProvider(getTimeHeightTrajectory(trajectory)))
                       .style(LINE_STYLE)
                       .submit();
        aStyleCollector.object(trajectory)
                       .geometry(new TimeReferenceProvider(new TLcdXYPoint(trajectory.getBeginTime(), 0)))
                       .style(DOT_STYLE)
                       .submit();
        aStyleCollector.object(trajectory)
                       .geometry(new TimeReferenceProvider(new TLcdXYPoint(trajectory.getEndTime(), 0)))
                       .style(DOT_STYLE)
                       .submit();
      }
    }
  }

  private static class CityLabelStyler extends ALspLabelStyler {

    private static final TLspTextStyle TEXT_STYLE = TLspTextStyle.newBuilder().textColor(Color.WHITE).haloColor(Color.DARK_GRAY).build();

    public void style(Collection<?> aCollection, ALspLabelStyleCollector aStyleCollector, TLspContext aTLspContext) {
      for (Object object : aCollection) {
        ITrajectory trajectory = (ITrajectory) object;
        aStyleCollector.object(trajectory)
                       .geometry(new TimeReferenceProvider(new TLcdXYPoint(trajectory.getBeginTime(), 0)))
                       .label("departure")
                       .locations(0, TLspLabelLocationProvider.Location.NORTH_WEST)
                       .styles(TEXT_STYLE, FixedTextProviderStyle.newBuilder().text(((ILcdDataObject) trajectory.getDestination()).getValue("City").toString()).build())
                       .submit();
        aStyleCollector.object(trajectory)
                       .geometry(new TimeReferenceProvider(new TLcdXYPoint(trajectory.getEndTime(), 0)))
                       .label("destination")
                       .locations(0, TLspLabelLocationProvider.Location.NORTH_EAST)
                       .styles(TEXT_STYLE, FixedTextProviderStyle.newBuilder().text(((ILcdDataObject) trajectory.getDeparture()).getValue("City").toString()).build())
                       .submit();
      }
    }
  }
}
