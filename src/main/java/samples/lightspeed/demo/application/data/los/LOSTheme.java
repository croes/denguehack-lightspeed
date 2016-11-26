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
package samples.lightspeed.demo.application.data.los;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.terrain.TLcdEarthTileSetElevationProvider;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdLOSCoverageMatrix;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.tea.lightspeed.los.EarthTerrainElevationAdapter;
import samples.tea.lightspeed.los.model.LOSCoverageInputShape;
import samples.tea.lightspeed.los.view.LOSCoverageInputShapeEditor;
import samples.tea.lightspeed.los.view.LOSCoverageStyler;
import samples.tea.lightspeed.los.view.LOSLayerFactory;

/**
 * A custom theme for Line-Of-Sight calculations.
 * <p>
 * This theme combines two layers: a LOS coverage layer and an icons layer. The LOS coverage layer
 * shows the results of the LOS calculations as a color-coded 3D surface. Points that are located
 * underneath this 3D surface are not visible to the observer (who is located in the center of the
 * LOS coverage) and points that are located above the surface are visible.
 * <p>
 * The icon layer contains a set of points for which the visibility is determined against the LOS
 * coverage of the first layer. The model which supplies the points is loaded based on the
 * <code>los.theme.points.file</code> property which has to be set in the index.xml file (note that
 * the file should be specified relative to the one of the directories specified in the  <code>data.paths</code>
 * property set in the demo.properties file).
 * <p>
 * The points model should be a <b>MIF model</b> which has objects that each have two data properties
 * set: <code>icon.visible</code> and <code>icon.invisible</code>. These properties are interpreted
 * as file names of icon images that are used resp. for when the point is visible or not w.r.t. the
 * LOS coverage.
 */
public class LOSTheme extends AbstractTheme {

  private static String sEyePointSource;

  private ILcdModel fInputModel;
  private ILcdAltitudeProvider fAltitudeProvider;
  private HashMap<ILspView, ILspController> fOldControllerMap;
  private LOSStylePanelBuilder fLOSStylePanelBuilder;
  private LOSCoverageStyler fLOSCoverageStyler;
  private String fIconsFile;
  private final TLspLOSCalculator fLOSCalculator;
  private boolean fIsTouchEnabled;

  public LOSTheme() {
    setName("Line of Sight");
    setCategory("Terrain");
    fOldControllerMap = new HashMap<ILspView, ILspController>();

    try {
      fLOSCalculator = new TLspLOSCalculator();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    List<ILspLayer> themeLayers = new ArrayList<ILspLayer>();
    Framework framework = Framework.getInstance();

    for (ILspView view : aViews) {
      fLOSCoverageStyler.getLOSCoverageStyle().getColorMap().setMasterOpacity(64);

      LOSLayerFactory losLayerFactory = new CustomLOSLayerFactory(fLOSCalculator, fIsTouchEnabled);
      losLayerFactory.setLOSCoverageStyler(fLOSCoverageStyler);
      losLayerFactory.setAltitudeProvider(getAltitudeProvider(view));

      if (losLayerFactory.canCreateLayers(fInputModel)) {
        Collection<ILspLayer> layers = new ArrayList<ILspLayer>();

        Collection<ILspLayer> coverageLayers = losLayerFactory.createLayers(fInputModel);
        layers.addAll(coverageLayers);

        ILspLayer losInputLayer = findLOSLayer(layers, LOSLayerFactory.LOS_INPUT_LAYER_LABEL);
        ILspLayer losOutputLayer = findLOSLayer(layers, LOSLayerFactory.LOS_OUTPUT_LAYER_LABEL);
        ILspLayer visibilityIconsLayer = createVisibilityIconsLayer(losOutputLayer.getModel());
        layers.add(visibilityIconsLayer);

        // We register the layers with the framework so that they will
        // also show up in the layer control panel (if we don't, they
        // will only be implicitly available by activating the theme)
        framework.registerLayers("layer.id.los.input", view, Collections.singletonList(losInputLayer));
        framework.registerLayers("layer.id.los.coverage", view, Collections.singletonList(losOutputLayer));
        framework.registerLayers("layer.id.los.icons", view, Collections.singletonList(visibilityIconsLayer));

        for (ILspLayer layer : layers) {
          view.addLayer(layer);
          themeLayers.add(layer);
        }
      }
    }

    return themeLayers;
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    AbstractModelFactory losModelFactory = Framework.getInstance().getRegisteredModelFactory("los");
    try {
      //The los layer factory does not require a source.
      fInputModel = losModelFactory.createModel(null);
    } catch (IOException e) {
      throw new UnsupportedOperationException(e);
    }
    fLOSCoverageStyler = new LOSCoverageStyler();

    initializeStylePanelBuilder(aProps);

    fIconsFile = Framework.getInstance().getDataPath(aProps.getProperty("los.theme.points.file"));
    sEyePointSource = aProps.getProperty("los.theme.center.eyepoint.file");

    fIsTouchEnabled = Boolean.parseBoolean(Framework.getInstance().getProperty("controllers.touch.enabled", "false"));

    super.initialize(aViews, aProps);
  }

  private void initializeStylePanelBuilder(Properties aProps) {
    String defaults = aProps.getProperty("los.default.colors");
    String visibilities = aProps.getProperty("los.visibility.colors");

    if (defaults == null || visibilities == null) {
      fLOSStylePanelBuilder = new LOSStylePanelBuilder(fLOSCoverageStyler,
                                                       fLOSCalculator,
                                                       fInputModel);
    } else {
      String[] split = defaults.split(",");
      double[] defaultLevels = new double[split.length / 2];
      Color[] defaultColors = new Color[split.length / 2];
      for (int i = 0; i < split.length / 2; ++i) {
        defaultLevels[i] = Double.parseDouble(split[2 * i].trim());
        defaultColors[i] = parseAlphaHexColor(split[2 * i + 1].trim());
      }

      split = visibilities.split(",");
      double[] visibilityLevels = new double[split.length / 2];
      Color[] visibilityColors = new Color[split.length / 2];
      for (int i = 0; i < split.length / 2; ++i) {
        visibilityLevels[i] = Double.parseDouble(split[2 * i].trim());
        visibilityColors[i] = parseAlphaHexColor(split[2 * i + 1].trim());
      }

      fLOSStylePanelBuilder = new LOSStylePanelBuilder(fLOSCoverageStyler,
                                                       fLOSCalculator,
                                                       fInputModel,
                                                       defaultLevels,
                                                       defaultColors,
                                                       visibilityLevels,
                                                       visibilityColors);
    }
  }

  /**
   * Parses the given string as an ARGB color, where each channel is expected to be
   * encoded with two hexadecimal numbers: e.g. FF00FF00 will yield an opaque green
   * color and 880000FF will yield a semi-transparent blue color.
   *
   * @param aStr the string to be parsed
   *
   * @return a color
   */
  private static Color parseAlphaHexColor(String aStr) {
    if (aStr.startsWith("0x")) {
      aStr = aStr.substring(2);
    }

    int alpha = Integer.parseInt(aStr.substring(0, 2), 16);
    int red = Integer.parseInt(aStr.substring(2, 4), 16);
    int green = Integer.parseInt(aStr.substring(4, 6), 16);
    int blue = Integer.parseInt(aStr.substring(6), 16);

    return new Color(red, green, blue, alpha);
  }

  @Override
  public void activate() {
    if (!fIsTouchEnabled) {
      Runnable runnable = new Runnable() {
        public void run() {
          for (ILspView view : getViews()) {
            // Save old controllers and set new LOS controller while LOS theme is active
            fOldControllerMap.put(view, view.getController());
            view.setController(createLOSController(view));
          }
        }
      };
      TLcdAWTUtil.invokeLater(runnable);
    }

    super.activate();
  }

  private ILcdAltitudeProvider getAltitudeProvider(ILspView aView) {
    if (fAltitudeProvider == null) {
      fAltitudeProvider = createAltitudeProvider(aView);
    }
    return fAltitudeProvider;
  }

  private ILcdAltitudeProvider createAltitudeProvider(ILspView aView) {
    ILcdEarthTileSet elevationTileSet = aView.getServices().getTerrainSupport().getElevationTileSet();
    TLcdEarthTileSetElevationProvider elevationProvider = new TLcdEarthTileSetElevationProvider(elevationTileSet, 1, 0, 64);
    elevationProvider.setForceAsynchronousTileRequests(false);
    int tileLevel = 10;
    elevationProvider.setMaxSynchronousLevel(tileLevel);
    elevationProvider.setMaxTileLevel(tileLevel);
    return new EarthTerrainElevationAdapter(elevationProvider);
  }

  @Override
  public void deactivate() {
    if (!fIsTouchEnabled) {
      //Restore old controllers
      for (Map.Entry<ILspView, ILspController> controllerEntry : fOldControllerMap.entrySet()) {
        controllerEntry.getKey().setController(controllerEntry.getValue());
      }
      fOldControllerMap.clear();
    }

    super.deactivate();
  }

  /**
   * Gets a list of all views registered in the framework.
   *
   * @return a list of all currently registered views
   */
  @SuppressWarnings("unchecked")
  private List<ILspView> getViews() {
    return Framework.getInstance().getFrameworkContext().getViews();
  }

  @Override
  public void destroy() {
    TLcdIdentityHashSet<ILcdModel> models = new TLcdIdentityHashSet<ILcdModel>();

    for (ILspLayer themeLayer : getLayers()) {
      models.add(themeLayer.getModel());
    }

    for (ILcdModel model : models) {
      model.dispose();
    }
  }

  @Override
  public String getName() {
    return "Line Of Sight";
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fLOSStylePanelBuilder.createThemePanels(this);
  }

  private ILspLayer findLOSLayer(Collection<ILspLayer> aLayers, String aLabel) {
    for (ILspLayer layer : aLayers) {
      if (layer.getLabel().equals(aLabel)) {
        return layer;
      }
    }
    return null;
  }

  private ILspLayer findLOSLayer(Collection<ILspLayer> aLayers, ILspView aView, String aLabel) {
    for (ILspLayer layer : aLayers) {
      if (layer.getLabel().equals(aLabel) && layer.getCurrentViews().contains(aView)) {
        return layer;
      }
    }
    return null;
  }

  private ILspController createLOSController(ILspView aView) {
    ALspController chainableController;
    ILspLayer losLayer = findLOSLayer(getLayers(), aView, LOSLayerFactory.LOS_INPUT_LAYER_LABEL);
    if (losLayer instanceof ILspInteractivePaintableLayer) {
      chainableController = new LOSEditController((ILspInteractivePaintableLayer) losLayer);
      chainableController.appendController(ControllerFactory.createDefaultSelectController());
    } else {
      chainableController = ControllerFactory.createDefaultSelectController();
    }
    chainableController.appendController(ControllerFactory.createNavigationController());
    return chainableController;
  }

  /**
   * Creates a layer with some points that get styled based on their visibility calculations.
   *
   * @param aLOSCoverageModel the los coverage model that contains one LOS coverage matrix element
   *                          on which the visualization of these icons is based
   *
   * @return a layer that styles its points based on visibility w.r.t. the given LOS coverage model
   */
  private ILspLayer createVisibilityIconsLayer(final ILcdModel aLOSCoverageModel) {
    if (!aLOSCoverageModel.elements().hasMoreElements()) {
      throw new IllegalArgumentException("The given LOS coverage layer is empty. Can not create visibility icons layer.");
    }

    ILcdModel pointModel;
    TLcdMIFModelDecoder decoder = new TLcdMIFModelDecoder();
    try {
      pointModel = decoder.decode(fIconsFile);
    } catch (IOException e) {
      throw new RuntimeException("Could not create visibility point model for LOS theme");
    }

    //trigger initial visibility calculation
    aLOSCoverageModel.elementChanged(aLOSCoverageModel.elements().nextElement(), ILcdModel.FIRE_NOW);

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    return layerBuilder
        .model(pointModel)
        .label("LOS Points")
        .bodyStyler(TLspPaintState.REGULAR,
                    new VisibilityPointStyler(aLOSCoverageModel, getAltitudeProvider(null), fLOSCalculator))
        .selectable(false)
        .build();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Convenience base class to share code between BottomPointStyler and VisibilityPointStyler.
   */
  private static abstract class AbstractPointStyler extends ALspStyler implements ILcdModelListener {

    private ILcdLOSCoverageMatrix fLOSCoverage;
    private ILcdModelReference fModelReference;
    private ILcdAltitudeProvider fAltitudeProvider;
    private TLspLOSCalculator fLOSCalculator;

    protected AbstractPointStyler(ILcdModel aCoverageModel,
                                  ILcdAltitudeProvider aAltitudeProvider,
                                  TLspLOSCalculator aLOSCalculator) {
      Enumeration elements = aCoverageModel.elements();
      if (!elements.hasMoreElements()) {
        throw new IllegalArgumentException("Cannot make visibility icon styler, " +
                                           "no elements in LOS coverage model");
      }
      fLOSCoverage = (ILcdLOSCoverageMatrix) elements.nextElement();
      fModelReference = aCoverageModel.getModelReference();
      fAltitudeProvider = aAltitudeProvider;
      fLOSCalculator = aLOSCalculator;

      aCoverageModel.addModelListener(this);
    }

    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      fireStyleChangeEvent();
    }

    protected boolean isPointVisible(ILcdPoint aPoint) {
      try {
        return LOSUtil.isPointVisible(getLOSCoverage(),
                                      getAltitudeProvider(),
                                      fLOSCalculator.getCoverageAltitudeMode(),
                                      5.0,
                                      aPoint,
                                      (ILcdGeoReference) getModelReference());
      } catch (TLcdOutOfBoundsException e) {
        return false;
      }
    }

    protected ILcdLOSCoverageMatrix getLOSCoverage() {
      return fLOSCoverage;
    }

    protected ILcdAltitudeProvider getAltitudeProvider() {
      return fAltitudeProvider;
    }

    protected ILcdModelReference getModelReference() {
      return fModelReference;
    }
  }

  /**
   * Styler implementation that checks whether the focus point of
   * incoming objects is visible w.r.t. the associated LOS Coverage.
   */
  private static class VisibilityPointStyler extends AbstractPointStyler {

    private VisibilityPointStyler(ILcdModel aCoverageModel, ILcdAltitudeProvider aAltitudeProvider, TLspLOSCalculator aLOSCalculator) {
      super(aCoverageModel, aAltitudeProvider, aLOSCalculator);
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (!(object instanceof ILcdDataObject)) {
          continue;
        }

        if (object instanceof ILcdShape) {
          ILcdPoint focusPoint = ((ILcdShape) object).getFocusPoint();
          final double altitude = getAltitudeProvider().retrieveAltitudeAt(focusPoint, (ILcdGeoReference) getModelReference());

          ILcd3DEditablePoint bottomPoint = focusPoint.cloneAs3DEditablePoint();
          bottomPoint.move3D(bottomPoint.getX(), bottomPoint.getY(), altitude);

          ALspStyle topStyle = getStyle(focusPoint, (ILcdDataObject) object);
          aStyleCollector
              .object(object)
              .geometry(bottomPoint)
              .styles(topStyle)
              .submit();
        }
      }
    }

    private ALspStyle getStyle(ILcdPoint aPoint, ILcdDataObject aDataObject) {
      String imageFile;
      if (isPointVisible(aPoint)) {
        imageFile = Framework.getInstance().getDataPath(aDataObject.getValue("icon_visible").toString());
      } else {
        imageFile = Framework.getInstance().getDataPath(aDataObject.getValue("icon_invisible").toString());
      }

      TLcdImageIcon icon = new TLcdImageIcon(imageFile);
      return new TLspIconStyle.Builder()
          .icon(icon)
          .offset(0, -icon.getIconHeight() / 2)
          .build();
    }

  }

  private static class CustomLOSLayerFactory extends LOSLayerFactory {

    private final boolean fIsTouchEnabled;

    private CustomLOSLayerFactory(TLspLOSCalculator aLOSCalculator, boolean aIsTouchEnabled) {
      super(aLOSCalculator);
      fIsTouchEnabled = aIsTouchEnabled;
    }

    @Override
    public Collection<ILspLayer> createLayers(ILcdModel aModel) {
      Collection<ILspLayer> layers = super.createLayers(aModel);
      List<ILspLayer> removedLayers = new ArrayList<ILspLayer>();
      List<ILspLayer> newLayers = new ArrayList<ILspLayer>();
      for (final ILspLayer layer : layers) {
        if (layer.getLabel().equals(LOS_INPUT_LAYER_LABEL)) {
          TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder(ILspLayer.LayerType.EDITABLE);

          ILspStyler transparentEyeIconStyler = new MyEyeStyler(layer, false);
          ILspStyler opaqueEyeIconStyler = new MyEyeStyler(layer, true);
          LOSCoverageInputShapeEditor losEditor = new LOSCoverageInputShapeEditor(false);
          layerBuilder.model(aModel)
                      .label(LOS_INPUT_LAYER_LABEL)
                      .bodyStyler(TLspPaintState.REGULAR, fIsTouchEnabled ? transparentEyeIconStyler : opaqueEyeIconStyler)
                      .bodyStyler(TLspPaintState.SELECTED, opaqueEyeIconStyler)
                      .bodyStyler(TLspPaintState.EDITED, opaqueEyeIconStyler)
                      .bodyEditor(losEditor)
                      .bodyEditable(true);
          newLayers.add(layerBuilder.build());
          removedLayers.add(layer);
        }

      }
      layers.removeAll(removedLayers);
      layers.addAll(newLayers);
      return layers;
    }

    private static class MyEyeStyler extends ALspStyler {

      private final ILspLayer fLayer;
      private final boolean fSelection;

      public MyEyeStyler(ILspLayer aLayer, boolean aSelection) {
        fLayer = aLayer;
        fSelection = aSelection;
      }

      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        for (Object object : aObjects) {
          if (object instanceof LOSCoverageInputShape) {
            LOSCoverageInputShape circle = (LOSCoverageInputShape) object;
            ILcdPoint centerPoint = circle.getCenter();
            ILcd3DEditablePoint editablePoint = fLayer.getModel().getModelReference().makeModelPoint().cloneAs3DEditablePoint();
            editablePoint.move3D(centerPoint.getX(), centerPoint.getY(), 0);
            TLspIconStyle iconStyle = TLspIconStyle.newBuilder().elevationMode(ElevationMode.ABOVE_TERRAIN).icon(new TLcdImageIcon(sEyePointSource)).modulationColor(fSelection ? Color.WHITE : new Color(255, 255, 255, 192)).scale(2).build();

            aStyleCollector.object(object)
                           .geometry(editablePoint)
                           .styles(iconStyle, TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, 8).build())
                           .submit();
            aStyleCollector.object(object)
                           .style(TLspFillStyle.newBuilder().color(DemoUIColors.TRANSPARENT).elevationMode(ElevationMode.ON_TERRAIN).build())
                           .submit();

          }
        }
      }

    }
  }

}
