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
package samples.lightspeed.demo.application.data.dynamictracks;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer.LayerType;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.common.tracks.AirbornTrackProvider;
import samples.lightspeed.common.tracks.TrackHistoryPointProvider;
import samples.lightspeed.demo.application.data.support.EnrouteAirwaySimulatorModel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Layer factory for Enroute Track models.
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>label.decluttering</td> <td>boolean</td><td>false</td> <td>Sets whether label
 * decluttering is activated (true) or not (false)</td></tr>
 * <tr> <td>track.icon.size</td> <td>int</td><td>10</td> <td>Sets the size of the track
 * icon</td></tr>
 * <tr> <td>track.icon.outline</td> <td>int,int,int</td><td>255,255,0</td> <td>Specifies the
 * outline
 * color of the track symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.icon.fill</td> <td>int,int,int</td><td>0,0,0</td> <td>Specifies the fill of the
 * track symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.history.size</td> <td>int</td><td>7</td> <td>Sets the size of the history icons
 * corresponding to the track</td></tr>
 * <tr> <td>track.history.outline</td> <td>int,int,int</td><td>0,0,255</td> <td>Specifies the
 * outline color of the history symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.history.fill</td> <td>int,int,int</td><td>255,192,0</td> <td>Specifies the fill
 * color of the history symbols (format: R,G,B)</td></tr>
 * <tr> <td>history.point.count</td> <td>int</td> <td>0</td> <td>Specifies the number of history
 * points that are to be drawn for each track</td></tr>
 * <tr> <td>history.point.interval</td> <td>double</td> <td>0</td> <td>Specifies the spacing
 * between
 * the history points of a track</td></tr>
 * </table>
 */
public class EnrouteTrackLayerFactory extends AbstractLayerFactory implements ILspLayerFactory {

  private boolean fLabelDecluttering;
  private int fIconSize;
  private Color fIconOutline;
  private Color fIconFill;
  private int fHistoryPointCount;
  private double fHistoryPointInterval;
  private int fHistorySize;
  private Color fHistoryOutline;
  private Color fHistoryFill;

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null &&
           Framework.getInstance().getThemeByClass(DynamicTracksTheme.class) != null;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    ILcdModel[] trackModels = getTracksModel(aModel);

    final TLspStyler historyTrailStyler = new TLspStyler();

    TLspIconStyle trackStyle = createTrackStyle();
    historyTrailStyler.addStyles(AirbornTrackProvider.getProvider(),
                                 Collections.singletonList(trackStyle));

    for (int i = 0; i < fHistoryPointCount; i++) {
      TLspIconStyle historyStyle = createTrackHistoryStyle(fHistorySize - i);
      historyTrailStyler.addStyles(new TrackHistoryPointProvider(i),
                                   Collections.singletonList(historyStyle));
    }

    // Create a layer using the shape layer builder
    return TLspShapeLayerBuilder.newBuilder()
                                .model(trackModels[0])
                                .layerType(LayerType.REALTIME)
                                .selectable(false)
                                .culling(false)
                                .minimumObjectSizeForPainting(0)
                                .bodyEditable(false)
                                .bodyStyler(TLspPaintState.REGULAR, new BodyTrackStyler(trackStyle, historyTrailStyler))
                                .labelPainter(createTrackLabelPainter(fLabelDecluttering))
                                .labelEditable(true)
                                .synchronizePainters(true)
                                .build();
  }

  private TLspIconStyle createTrackStyle() {
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_RECT);
    icon.setSize(fIconSize);
    icon.setBorderColor(fIconOutline);
    icon.setFillColor(fIconFill);
    return TLspIconStyle.newBuilder()
                        .icon(icon)
                        .zOrder(1)
                        .build();
  }

  private TLspIconStyle createTrackHistoryStyle(int aSize) {
    TLcdSymbol historyIcon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE,
                                            aSize,
                                            fHistoryOutline,
                                            fHistoryFill);
    return TLspIconStyle.newBuilder()
                        .icon(historyIcon)
                        .zOrder(0)
                        .opacity(0.6f)
                        .build();
  }

  private ILcdModel[] getTracksModel(ILcdModel aModel) {
    // Write lock because route segments are not thread safe for reading
    try (Lock autoUnlock = writeLock(aModel)) {
      AbstractTheme dynamicTracksTheme = getTheme();

      String key = EnrouteAirwaySimulatorModel.getModelKey(aModel);
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) SimulationSupport.getInstance().getSharedSimulatorModel(key);
      if (simulatorModel == null) {
        simulatorModel = new EnrouteAirwaySimulatorModel(aModel, fHistoryPointCount, fHistoryPointInterval);
        SimulationSupport.getInstance().setSharedSimulatorModel(key, simulatorModel);
      }
      SimulationSupport.getInstance().addSimulatorModelForTheme(simulatorModel, dynamicTracksTheme);

      return simulatorModel.getTrackModels();
    }
  }

  protected AbstractTheme getTheme() {
    return Framework.getInstance().getThemeByClass(DynamicTracksTheme.class);
  }

  private ILspLabelPainter createTrackLabelPainter(boolean aLabelDecluttering) {
    TLspLabelPainter painter = new TLspLabelPainter();
    painter.setOverlayLabels(true);

    painter.setStyler(TLspPaintState.REGULAR, new LabelTrackStyler(aLabelDecluttering, false));
    painter.setStyler(TLspPaintState.SELECTED, new LabelTrackStyler(aLabelDecluttering, true));

    return painter;
  }

  @Override
  public void configure(Properties aProperties) {
    fLabelDecluttering = Boolean.valueOf(aProperties.getProperty("label.decluttering", "false"));
    fIconSize = Integer.parseInt(aProperties.getProperty("track.icon.size", "10"));
    fIconOutline = getColor(aProperties, "track.icon.outline", "FFFFFF00");
    fIconFill = getColor(aProperties, "track.icon.fill", "FF000000");

    fHistorySize = Integer.parseInt(aProperties.getProperty("track.history.size", "7"));
    fHistoryOutline = getColor(aProperties, "track.history.outline", "FF0000FF");
    fHistoryFill = getColor(aProperties, "track.history.fill", "FFFFC000");

    fHistoryPointCount = Integer.valueOf(aProperties.getProperty("history.point.count", "0"));
    fHistoryPointInterval = Double.valueOf(aProperties.getProperty("history.point.interval", "0"));
  }
}
