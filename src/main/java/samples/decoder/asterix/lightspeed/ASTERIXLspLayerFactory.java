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
package samples.decoder.asterix.lightspeed;

import static samples.decoder.asterix.ASTERIXLayerFactory.WEATHER_COLORS;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.format.asterix.TLcdASTERIXPlotModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXPrecipitationZone;
import com.luciad.format.asterix.TLcdASTERIXTrajectoryModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXWeatherModelDescriptor;
import com.luciad.format.asterix.TLcdASTERIXWeatherPicture;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelList;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.snapping.ILspSnappable;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.decoder.asterix.ASTERIXLayerFactory;

/**
 *  Layer factory for the trajectory models produced by the ASTERIX model decoder.
 */
@LcdService(service = ILspLayerFactory.class)
public class ASTERIXLspLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdASTERIXTrajectoryModelDescriptor ||
        aModel.getModelDescriptor() instanceof TLcdASTERIXPlotModelDescriptor ||
        aModel.getModelDescriptor() instanceof TLcdASTERIXWeatherModelDescriptor) {
      return true;
    }
    if (aModel instanceof TLcdModelList && ((TLcdModelList) aModel).getModelCount() > 0) {
      for (int i = 0; i < ((TLcdModelList) aModel).getModelCount(); i++) {
        if (!canCreateLayers(((TLcdModelList) aModel).getModel(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel instanceof TLcdModelList) {
      TLspLayerTreeNode node = new TLspLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      for (int i = 0; i < ((TLcdModelList) aModel).getModelCount(); i++) {
        node.addLayer(createLayer(((TLcdModelList) aModel).getModel(i)));
      }
      return node;
    }
    return createLeafLayer(aModel);
  }

  public ILspLayer createLeafLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdASTERIXTrajectoryModelDescriptor) {
      return createTrajectoryLayer(aModel);
    }
    if (aModel.getModelDescriptor() instanceof TLcdASTERIXPlotModelDescriptor) {
      return createPlotLayer(aModel);
    }
    if (aModel.getModelDescriptor() instanceof TLcdASTERIXWeatherModelDescriptor) {
      return createWeatherLayer(aModel);
    }
    return null;
  }

  private ILspLayer createPlotLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder.newBuilder().model(aModel).build();
  }

  private ILspLayer createTrajectoryLayer(ILcdModel aModel) {
    TLspLineStyle lineStyle = TLspLineStyle.newBuilder().
        color(ASTERIXLayerFactory.TRAJECTORY_COLOR).
                                               elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID).build();
    return TLspShapeLayerBuilder.newBuilder().model(aModel).bodyStyles(TLspPaintState.REGULAR, lineStyle).build();
  }

  private ILspLayer createWeatherLayer(ILcdModel aModel) {
    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder().bodyStyler(TLspPaintState.REGULAR, new WeatherPictureStyler()).model(aModel).selectable(true).build();
    if (layer instanceof ILspSnappable) {
      ((ILspSnappable) layer).setSnapTarget(false);
    }
    return layer;
  }

  private class WeatherPictureStyler extends ALspStyler {

    private final Map<Integer, TLspLineStyle> fLineStyles = new HashMap<>();
    private final Map<Integer, ALspStyleTargetProvider> fTargetProviders = new HashMap<>();

    public WeatherPictureStyler() {
      for (Integer intensity : WEATHER_COLORS.keySet()) {
        TLspLineStyle lineStyle = TLspLineStyle.newBuilder()
                                               .color(WEATHER_COLORS.get(intensity))
                                               .build();
        fLineStyles.put(intensity, lineStyle);
        fTargetProviders.put(intensity, new IntensityTargetProvider(intensity));
      }
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Integer intensity : fLineStyles.keySet()) {
        aStyleCollector.objects(aObjects).geometry(fTargetProviders.get(intensity)).style(fLineStyles.get(intensity)).submit();
      }
    }
  }

  private static class IntensityTargetProvider extends ALspStyleTargetProvider {

    private final int fIntensity;

    public IntensityTargetProvider(int aIntensity) {
      fIntensity = aIntensity;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      TLcdASTERIXWeatherPicture weatherPicture = (TLcdASTERIXWeatherPicture) aObject;
      for (TLcdASTERIXPrecipitationZone zone : weatherPicture.getPrecipitationZones()) {
        if (zone.getIntensity() == fIntensity) {
          aResultSFCT.add(zone);
        }
      }
    }
  }
}
