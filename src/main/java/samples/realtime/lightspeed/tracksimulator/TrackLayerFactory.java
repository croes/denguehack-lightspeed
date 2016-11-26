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

import static com.luciad.realtime.lightspeed.labeling.TLspContinuousLabelingAlgorithm.LabelMovementBehavior.REDUCED_MOVEMENT;
import static com.luciad.view.lightspeed.style.TLspTextStyle.Alignment.CENTER;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.lightspeed.labeling.TLspContinuousLabelingAlgorithm;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.label.TLspLabelEditor;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle.ScalingMode;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.realtime.common.TimeStampedTrack;

/**
 * Layer factory for the air tracks.
 * Shows small plane icons, with trail icons.
 */
public class TrackLayerFactory extends ALspSingleLayerFactory {

  private static final TLcdImageIcon AIRPLANE_ICON = new TLcdImageIcon("samples/images/airplane.png");
  private static final Color SELECTION_COLOR = Color.orange;

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor().getTypeName().equals("Tracks"));
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    // Create label styles
    TLspPinLineStyle pinStyle = TLspPinLineStyle.newBuilder()
                                                .color(new Color(0.2f, 0.2f, 1f, 1.0f))
                                                .width(1)
                                                .build();
    TLspLabelBoxStyle boxStyle = TLspLabelBoxStyle.newBuilder()
                                                  .filled(true)
                                                  .fillColor(new Color(1f, 1f, 1f, 0.3f))
                                                  .frameThickness(1)
                                                  .frameColor(new Color(0.2f, 0.2f, 1f, 0.7f))
                                                  .haloThickness(0)
                                                  .padding(2)
                                                  .build();
    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font(Font.decode("SansSerif-BOLD-10"))
                                           .textColor(new Color(10, 10, 55))
                                           .haloColor(new Color(255, 255, 255))
                                           .haloThickness(1)
                                           .alignment(CENTER)
                                           .build();
    TLspTextStyle selectedStyle = textStyle.asBuilder().haloColor(SELECTION_COLOR).build();

    ALspLabelTextProviderStyle textProvider = new ALspLabelTextProviderStyle() {
      @Override
      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
        if (aDomainObject instanceof TimeStampedTrack) {
          TimeStampedTrack track = (TimeStampedTrack) aDomainObject;
          return track.isGrounded() ?
                 new String[]{track.getID(), "Grounded"} :
                 new String[]{track.getID()};
        } else {
          return super.getText(aDomainObject, aSubLabelID, aContext);
        }
      }
    };

    ILspStyler defaultStyler = new TrailedIconStyler(false);
    ILspStyler selectedStyler = new TrailedIconStyler(true);

    //Configure labeling
    TLspContinuousLabelingAlgorithm labelingAlgorithm = new TLspContinuousLabelingAlgorithm();
    labelingAlgorithm.setReuseLocationsScaleRatioInterval(new TLcdInterval(0, Double.MAX_VALUE));
    labelingAlgorithm.setClampOnScreenEdges(false);
    labelingAlgorithm.setMinDistance(10);
    labelingAlgorithm.setMaxDistance(50);
    labelingAlgorithm.setLabelMovementBehavior(REDUCED_MOVEMENT);
    labelingAlgorithm.setPadding(2);
    labelingAlgorithm.setDesiredRelativeLocation(new Point(0, -15));
    labelingAlgorithm.setMaxCoverage(0.30);

    TLspLabelStyler labelStyler = TLspLabelStyler.newBuilder()
                                                 .algorithm(labelingAlgorithm)
                                                 .group(TLspLabelPlacer.DEFAULT_REALTIME_GROUP)
                                                 .styles(textStyle, textProvider, pinStyle, boxStyle)
                                                 .build();
    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .selectable(true)
                                .bodyStyler(TLspPaintState.REGULAR, defaultStyler)
                                .bodyStyler(TLspPaintState.SELECTED, selectedStyler)
                                .bodyStyler(TLspPaintState.EDITED, selectedStyler)
                                .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                .labelStyler(TLspPaintState.SELECTED, labelStyler.asBuilder().styles(selectedStyle, textProvider, pinStyle, boxStyle).build())
                                .labelStyler(TLspPaintState.EDITED, labelStyler.asBuilder().styles(selectedStyle, textProvider, pinStyle, boxStyle).build())
                                .labelEditable(true)
                                .labelEditor(new TLspLabelEditor())
                                .labelScaleRange(new TLcdInterval(2 * 1e-4, Double.POSITIVE_INFINITY))
                                .layerType(ILspLayer.LayerType.REALTIME)
                                .culling(false)
                                .build();
  }

  /**
   * Provides for each track one of the points that was recently visited, based on an index
   */
  private final class TrailHistoryProvider extends ALspStyleTargetProvider {
    private final int fIndex;

    /**
     * Creates a new trail history provider with the given index
     *
     * @param aIndex index of the recently visited point this provider should return
     */
    public TrailHistoryProvider(int aIndex) {
      fIndex = aIndex;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (aObject instanceof TimeStampedTrack) {
        ILcdPoint point = ((TimeStampedTrack) aObject).getPreviousLocation(fIndex);
        if (point != null) {
          aResultSFCT.add(point);
        }
      }
    }
  }

  /**
   * Icon styler that submits an airplane icon as well as grey trail dots.
   */
  private class TrailedIconStyler extends ALspStyler {

    private final TLspVerticalLineStyle fVerticalLineStyle;
    private final TLspIconStyle fAircraftStyle;
    private final TLspIconStyle fGroundedStyle;
    private final TLspIconStyle fGroundedAircraftStyle;
    private final ALspStyleTargetProvider[] fTrailProviders = new ALspStyleTargetProvider[4];
    private final TLspIconStyle[] fTrailStyles = new TLspIconStyle[4];

    public TrailedIconStyler(boolean aSelectedStyler) {
      fVerticalLineStyle = TLspVerticalLineStyle.newBuilder().color(Color.lightGray).width(1.5f).build();

      fAircraftStyle = TLspIconStyle.newBuilder()
                                    .worldSize(200000)
                                    .scalingMode(ScalingMode.WORLD_SCALING_CLAMPED)
                                    .icon(AIRPLANE_ICON)
                                    .modulationColor(aSelectedStyler ? SELECTION_COLOR : Color.white)
                                    .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                                    .useOrientation(true)
                                    .zOrder(5)
                                    .build();
      fGroundedStyle = fAircraftStyle.asBuilder()
                                     .icon(new TLcdSymbol(TLcdSymbol.CIRCLE, AIRPLANE_ICON.getIconWidth(), Color.white))
                                     .modulationColor(aSelectedStyler ? SELECTION_COLOR : Color.white)
                                     .elevationMode(ElevationMode.ON_TERRAIN)
                                     .build();
      fGroundedAircraftStyle = fAircraftStyle.asBuilder()
                                             .elevationMode(ElevationMode.ON_TERRAIN)
                                             .build();

      for (int i = 0; i < fTrailProviders.length; i++) {
        fTrailProviders[i] = new TrailHistoryProvider(i);
        fTrailStyles[i] = TLspIconStyle.newBuilder()
                                       .worldSize((i + 1) * 30000)
                                       .scalingMode(ScalingMode.WORLD_SCALING_CLAMPED)
                                       .scale(1.)
                                       .icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, (i + 1) * 2, null, Color.gray))
                                       .zOrder(4)
                                       .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                                       .build();
      }
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      Collection<Object> groundedObjects = new ArrayList<Object>(aObjects.size());
      Collection<Object> airborneObjects = new ArrayList<Object>(aObjects.size());

      for (Object object : aObjects) {
        if ((object instanceof TimeStampedTrack) && (((TimeStampedTrack) object).isGrounded())) {
          groundedObjects.add(object);
        } else {
          airborneObjects.add(object);
        }
      }

      aStyleCollector.objects(groundedObjects).styles(fGroundedStyle, fGroundedAircraftStyle).submit();
      aStyleCollector.objects(airborneObjects).styles(fAircraftStyle, fVerticalLineStyle).submit();

      for (int i = 0; i < fTrailProviders.length; i++) {
        aStyleCollector.objects(airborneObjects).geometry(fTrailProviders[i]).style(fTrailStyles[i]).submit();
      }
    }
  }
}
