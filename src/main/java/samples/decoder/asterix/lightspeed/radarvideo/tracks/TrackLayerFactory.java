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
package samples.decoder.asterix.lightspeed.radarvideo.tracks;

import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.format.asterix.TLcdASTERIXTrackModelDescriptor;
import com.luciad.gui.TLcdSymbol;
import samples.decoder.asterix.SimulationModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.label.TLspLabelEditor;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import java.awt.Color;
import java.awt.Font;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.NORTH;

/**
 * Layer factory to style the tracks.
 */
public class TrackLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof SimulationModelDescriptor ||
        aModel.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor) {
      return createAirplanesLayer(aModel);
    }

    throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                       "reason: model not recognized");

  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof SimulationModelDescriptor ||
           aModel.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor;
  }

  /**
   * Creates the layer that displays the dynamic tracks as small plane icons. The tracks are
   * transformed from model to world coordinates on the GPU yielding fast update rates allowing for
   * painting thousands of tracks at fast update rates.
   *
   * @param aModel the tracks model
   *
   * @return the layer
   */
  private ILspLayer createAirplanesLayer(ILcdModel aModel) {

    ALspStyle verticalLineStyle = TLspVerticalLineStyle.newBuilder().color(Color.lightGray).width(1.5f).build();
    ILspStyler defaultStyler = new TLspStyler(createIconStyle(false), verticalLineStyle);
    ILspStyler selectedStyler = new TLspStyler(createIconStyle(true), verticalLineStyle);

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .selectable(true)
                                .bodyStyler(TLspPaintState.REGULAR, defaultStyler)
                                .bodyStyler(TLspPaintState.SELECTED, selectedStyler)
                                .bodyStyler(TLspPaintState.EDITED, selectedStyler)
                                .labelPainter(createTrackLabelPainter())
                                .labelEditable(true)
                                .labelEditor(new TLspLabelEditor())
                                .labelScaleRange(new TLcdInterval(2 * 1e-4, Double.POSITIVE_INFINITY))
                                .culling(false)
                                .build();
  }

  /**
   * Creates a label painter that will set the call signs in the labels.
   */
  private ILspLabelPainter createTrackLabelPainter() {
    TLspLabelPainter painter = new TLspLabelPainter();
    painter.setOverlayLabels(true);

    ALspLabelTextProviderStyle textProvider = new ALspLabelTextProviderStyle() {
      @Override
      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
        if (aDomainObject instanceof TLcdASTERIXTrack) {
          TLcdASTERIXTrack track = (TLcdASTERIXTrack) aDomainObject;
          String targetIdentification = (String) track.getTrajectory().getValue("TargetIdentification");
          if (targetIdentification != null) {
            return new String[]{targetIdentification};
          }
        }
        return null;
      }
    };

    TLspPinLineStyle pinStyle = TLspPinLineStyle.newBuilder()
                                                .color(new Color(50, 50, 200))
                                                .width(1.5f)
                                                .build();

    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font(Font.decode("Default-BOLD-10"))
                                           .textColor(new Color(50, 50, 200))
                                           .haloColor(new Color(255, 255, 255))
                                           .haloThickness(1)
                                           .build();

    TLspTextStyle selectedStyle = textStyle.asBuilder()
                                           .haloColor(new Color(50, 255, 255))
                                           .build();

    ILspLabelingAlgorithm labelingAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(20, NORTH));

    painter.setStyler(TLspPaintState.REGULAR,
                      TLspLabelStyler.newBuilder()
                                     .group(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP)
                                     .algorithm(labelingAlgorithm)
                                     .styles(textStyle, pinStyle, textProvider)
                                     .build()
    );
    painter.setStyler(TLspPaintState.SELECTED,
                      TLspLabelStyler.newBuilder()
                                     .group(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP)
                                     .algorithm(labelingAlgorithm)
                                     .styles(selectedStyle, pinStyle, textProvider)
                                     .build()
    );

    return painter;
  }

  private ALspStyle createIconStyle(boolean aSelectedStyler) {
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_RECT);
    icon.setSize(5);
    icon.setBorderColor(new Color(0xFF888888));
    icon.setFillColor(new Color(0xFFFFF00));
    return TLspIconStyle.newBuilder()
                        .icon(icon)
                        .modulationColor(aSelectedStyler ? Color.yellow : Color.white)
                        .zOrder(1)
                        .build();
  }

}
