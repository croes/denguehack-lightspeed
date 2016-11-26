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
package samples.lucy.cop.addons.airpicturetheme;

import java.awt.Color;
import java.awt.Font;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.lightspeed.ALcyLspStyleRepository;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lucy.theme.Theme;

/**
 * Air picture theme
 */
final class AirPictureTheme implements Theme {

  private final TracksModel fTracksModel;
  private final ILspLayer fTracksLayer;
  private final ILcyLucyEnv fLucyEnv;

  AirPictureTheme(String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fTracksModel = new TracksModel(aPropertyPrefix, aProperties, aLucyEnv);
    fTracksLayer = createTracksLayer(fTracksModel);
  }

  private ILspLayer createTracksLayer(ILcdModel aTracksModel) {
    ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(fLucyEnv);

    TLspLabelStyler labelStyler = TLspLabelStyler.newBuilder().styles(
        TLspDataObjectLabelTextProviderStyle.newBuilder().expressions(TracksModel.CALLSIGN_PROPERTY.getName()).build(),
        TLspTextStyle.newBuilder().font(new Font("Arial", Font.PLAIN, 11)).textColor(Color.white).haloColor(Color.black).haloThickness(1).build()
                                                                     ).build();
    ILspStyler selectionLabelStyler = styleRepository.createSelectionStyler(labelStyler);

    ILspStyler bodyStyler = new AirtrackStyler();
    ILspStyler selectedBodyStyler = styleRepository.createSelectionStyler(bodyStyler);

    ILspInteractivePaintableLayer layer =
        TLspShapeLayerBuilder.newBuilder(ILspLayer.LayerType.REALTIME)
                             .model(aTracksModel)
                             .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                             .bodyStyler(TLspPaintState.SELECTED, selectedBodyStyler)
                             .labelStyler(TLspPaintState.SELECTED, selectionLabelStyler)
                             .labelStyler(TLspPaintState.REGULAR, labelStyler)
                             .build();

    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, false);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);
    return layer;
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    getActiveMapComponent(aLucyEnv).getMainView().addLayer(fTracksLayer);
    fTracksModel.activate();
  }

  private ILcyGenericMapComponent<ILspView, ILspLayer> getActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    return mapManager.getActiveMapComponent();
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    getActiveMapComponent(aLucyEnv).getMainView().removeLayer(fTracksLayer);
    fTracksModel.deactivate();
  }

  @Override
  public String getDisplayName() {
    return "Air Picture";
  }
}
