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
package samples.lightspeed.demo.application.data.maritime;

import static com.luciad.format.s52.ILcdS52Symbology.*;

import static samples.lightspeed.demo.application.data.maritime.ExactAISModelDescriptor.*;
import static samples.lightspeed.demo.application.data.maritime.countrycodeutil.CountryCodeUtil.getCountryFlagIconLocation;
import static samples.lightspeed.demo.application.data.maritime.countrycodeutil.CountryCodeUtil.getCountryName;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.swing.TLspBalloonManager;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ILcdBalloonContentProvider;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

import samples.lightspeed.demo.application.data.maritime.util.ExactAISDepthUtil;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.TooltipMouseListener;

/**
 * @author tomn
 * @since 2012.1
 */
public class MaritimeTheme extends AbstractTheme {

  static final DateFormatter sDateFormatter = new DateFormatter(new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm"));

  private final ExactAISLayerFactory fLayerFactory = new ExactAISLayerFactory();
  private final MaritimePanelFactory fPanelFactory = new MaritimePanelFactory();

  private List<ILspView> fViews;
  private Map<ILspView, TooltipMouseListener> fTooltipListeners = new HashMap<ILspView, TooltipMouseListener>();
  private final List<TooltipMouseListener.TooltipLogic> fTooltipLogics = new ArrayList<TooltipMouseListener.TooltipLogic>();
  private Map<ILspView, JLabel> fExactEarthLabels = new HashMap<ILspView, JLabel>();
  private ExactAISModelDescriptor fModelDescriptor;

  private float fDayThemeBrightness, fDuskThemeBrightness, fNightThemeBrightness;
  private float fDayThemeContrast, fDuskThemeContrast, fNightThemeContrast;

  private float fCurrentBrightness = 1;
  private float fCurrentContrast = 1;

  private final float fOriginalBrightness = 1;
  private final float fOriginalContrast = 1;

  private List<ILspLayer> fAISLayers = new ArrayList<ILspLayer>();
  private List<ILspLayer> fNOAALayers = new ArrayList<ILspLayer>();

  private final PropertyChangeListener fColorThemeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("Color type".equals(evt.getPropertyName())) {
        putAnimationForCurrentEcdisSettings();
      }
    }
  };

  /**
   * Turn this to true if a new depth map needs to be created. This may take up to 5 minutes. (Less
   * if you use S-ENC)
   */
  public static final boolean CREATE_OCEAN_DEPTH_MAP = false;
  private List<ILspLayer> fAllLayers;

  public MaritimeTheme() {
    setCategory("Tracks");
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    fLayerFactory.configure(aProps);
    super.initialize(aViews, aProps);
    fViews = new ArrayList<ILspView>(aViews);

    fDayThemeBrightness = Float.valueOf(aProps.getProperty("background.brightness.day", "1"));
    fDayThemeContrast = Float.valueOf(aProps.getProperty("background.contrast.day", "1"));
    fDuskThemeBrightness = Float.valueOf(aProps.getProperty("background.brightness.dusk", "1"));
    fDuskThemeContrast = Float.valueOf(aProps.getProperty("background.contrast.dusk", "1"));
    fNightThemeBrightness = Float.valueOf(aProps.getProperty("background.brightness.night", "1"));
    fNightThemeContrast = Float.valueOf(aProps.getProperty("background.contrast.night", "1"));

  }

  @Override
  public void activate() {
    super.activate();

    putAnimationForCurrentEcdisSettings();
    ECDISConfigurationProvider.getS52DisplaySettings().addPropertyChangeListener(fColorThemeListener);

    addTooltipListener();
    addExactEarthLogo(fViews);

    // Only make the ExactAIS, NOAA and Coastline data visible at startup
    for (ILspLayer layer : getLayers()) {
      layer.setVisible(fAISLayers.contains(layer) || fNOAALayers.contains(layer) || layer.getLabel().contains("Natural Earth Coastline"));
    }

    setDisplayLandAreas(true);
  }

  private void putAnimationForCurrentEcdisSettings() {
    ILspView[] views = fViews.toArray(new ILspView[fViews.size()]);
    int colorScheme = ECDISConfigurationProvider.getS52DisplaySettings().getColorType();
    ALcdAnimation animation;
    switch (colorScheme) {
    case NIGHT_COLORS:
      animation = new BCAnimation(fCurrentBrightness, fNightThemeBrightness, fCurrentContrast, fNightThemeContrast, views);
      break;
    case DUSK_COLORS:
      animation = new BCAnimation(fCurrentBrightness, fDuskThemeBrightness, fCurrentContrast, fDuskThemeContrast, views);
      break;
    case DAY_BRIGHT_COLORS:
      animation = new BCAnimation(fCurrentBrightness, fDayThemeBrightness, fCurrentContrast, fDayThemeContrast, views);
      break;
    default:
      animation = null;
      break;
    }
    if (animation != null) {
      ALcdAnimationManager.getInstance().putAnimation(this, animation);
    }
  }

  @Override
  public void deactivate() {
    ILspView[] views = fViews.toArray(new ILspView[fViews.size()]);
    ECDISConfigurationProvider.getS52DisplaySettings().removePropertyChangeListener(fColorThemeListener);

    ALcdAnimationManager.getInstance().putAnimation(
        this,
        new BCAnimation(
            fOriginalBrightness, 1,
            fOriginalContrast, 1,
            views
        )
    );

    removeExactEarthLogo(fViews);
    removeTooltipListener();

    setDisplayLandAreas(false);

    super.deactivate();
  }

  /**
   * See LSP131-493:
   * - The NOAA layer should hide land areas in all themes except Vessel theme
   * - The NOAA layer should display land areas by default in Vessel theme (but can be toggled)
   */
  public void setDisplayLandAreas(boolean aDisplay) {
    for (ILspLayer layer : fNOAALayers) {
      // TODO: TLspS52Layer.setOpacity(float) is not in the API, we use reflection but this should be changed
      for (Method method : layer.getClass().getMethods()) {
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 1 && types[0].equals(float.class)) {
          try {
            method.invoke(layer, aDisplay ? 1.0f : 0.9f);
          } catch (InvocationTargetException ignore) {
            ignore.printStackTrace();
          } catch (IllegalAccessException ignore) {
            ignore.printStackTrace();
          }
        }
      }
    }

    ECDISConfigurationProvider.getS52DisplaySettings().setDisplayLandAreas(aDisplay);
  }

  private void addExactEarthLogo(List<ILspView> aViews) {
    if (fAISLayers.isEmpty()) {
      return;
    }
    for (ILspView view_ : aViews) {
      ILspAWTView view = (ILspAWTView) view_;
      TLcdSWIcon logo = new TLcdSWIcon(new TLcdImageIcon("samples/lightspeed/demo/application/data/maritime/exactearth_small.png"));
      JLabel logoLabel = new JLabel(logo);
      view.getOverlayComponent().add(logoLabel, TLcdOverlayLayout.Location.SOUTH_WEST);
      view.getOverlayComponent().validate();
      fExactEarthLabels.put(view, logoLabel);
    }
  }

  private void removeExactEarthLogo(List<ILspView> aViews) {
    for (ILspView view_ : aViews) {
      final ILspAWTView view = (ILspAWTView) view_;
      JLabel logoLabel = fExactEarthLabels.get(view);
      if (logoLabel != null) {
        view.getOverlayComponent().remove(logoLabel);
      }
    }
  }

  private void addTooltipListener() {
    for (final ILspView view : fViews) {
      if (view instanceof ILspAWTView) {
        final ILspAWTView awtView = (ILspAWTView) view;

        TooltipMouseListener tooltipMouseListener = fTooltipListeners.get(view);
        if (tooltipMouseListener == null) {
          List<ILspLayer> layers = new ArrayList<ILspLayer>();
          layers.addAll(fAISLayers);
          layers.addAll(fNOAALayers);
          tooltipMouseListener = new TooltipMouseListener(awtView, layers, fTooltipLogics);
          fTooltipListeners.put(view, tooltipMouseListener);
        }
        tooltipMouseListener.start();
        awtView.getHostComponent().addMouseListener(tooltipMouseListener);
        awtView.getHostComponent().addMouseMotionListener(tooltipMouseListener);
      }
    }
  }

  private void removeTooltipListener() {
    for (final ILspView view : fViews) {
      if (view instanceof ILspAWTView) {
        final ILspAWTView awtView = (ILspAWTView) view;
        TooltipMouseListener tooltipMouseListener = fTooltipListeners.get(view);
        if (tooltipMouseListener != null) {
          tooltipMouseListener.showTooltip(null, null, -1, -1);
          awtView.getHostComponent().removeMouseListener(tooltipMouseListener);
          awtView.getHostComponent().removeMouseMotionListener(tooltipMouseListener);
          tooltipMouseListener.stop();
        }
      }
    }
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    fAllLayers = new ArrayList<ILspLayer>();

    Framework framework = Framework.getInstance();
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.bing"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.fusion.osm.roads"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.aixm5.airport"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.buildings"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.clouds"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.shp.naturalearth.coastline.lessdetailed"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.shp.naturalearth.coastline.detailed"));
    fAllLayers.addAll(getLayersWithID(framework, "layer.id.fusion.osm.coastline2"));

    try {
      ILcdModel exactAISModel = framework.getModelWithID("model.id.exactais.plots");

      if (exactAISModel != null) {

        fModelDescriptor = (ExactAISModelDescriptor) exactAISModel.getModelDescriptor();

        for (ILspView view : aViews) {
          Collection<ILspLayer> exactAISLayers = fLayerFactory.createLayers(exactAISModel);
          Framework.getInstance().registerLayers("layer.id.exactais.plots", view, exactAISLayers);

          for (ILspLayer exactAISLayer : exactAISLayers) {
            BalloonOnSelectionListener selectionListener = new BalloonOnSelectionListener((ILspAWTView) view);
            selectionListener.registerFor(exactAISLayer);
            view.addLayer(exactAISLayer);
            exactAISLayer.addSelectionListener(new UpdateS52DisplaySettingsOnVesselSelection());
          }
          fAllLayers.addAll(exactAISLayers);
          fAISLayers.addAll(exactAISLayers);
          fTooltipLogics.add(new ExactAISTooltipLogic(fAISLayers, fLayerFactory, fModelDescriptor));
        }
      }
    } catch (IllegalArgumentException ignored) {
      // model not loaded in index.xml
    }

    fNOAALayers = getLayersWithID(framework, "layer.id.ecdis.noaa");
    fAllLayers.addAll(fNOAALayers);

    if (!fNOAALayers.isEmpty()) {
      fTooltipLogics.add(new EcdisTooltipLogic(fNOAALayers));
    }

    if (!fAISLayers.isEmpty() && !fNOAALayers.isEmpty()) {
      ExactAISDepthUtil.insertDepthsInAISPlots(fNOAALayers.get(0),
                                               fAISLayers.get(0).getModel(),
                                               "samples/lightspeed/demo/application/data/maritime/oceandepth.dat",
                                               CREATE_OCEAN_DEPTH_MAP);
    }

    setDisplayLandAreas(false);

    return fAllLayers;
  }

  private List<ILspLayer> getLayersWithID(Framework aFramework, String aId) {
    try {
      return aFramework.getLayersWithID(aId);
    } catch (IllegalArgumentException e) {
      return Collections.emptyList(); // layer does not exist
    }
  }

  @Override
  public List<ILspLayer> getLayers() {
    return fAllLayers;
  }

  public List<ILspLayer> getAISLayers() {
    return fAISLayers;
  }

  public List<ILspLayer> getNOAALayers() {
    return fNOAALayers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fPanelFactory.createThemePanels(this);
  }

  public ExactAISLayerFactory getLayerFactory() {
    return fLayerFactory;
  }

  /**
   * Selection listener which updates the S52 display settings based on the selected vessel
   */
  private class UpdateS52DisplaySettingsOnVesselSelection implements ILcdSelectionListener {

    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      Enumeration selectedElements = aSelectionEvent.selectedElements();
      if (selectedElements.hasMoreElements()) {
        AISPlot plot = (AISPlot) selectedElements.nextElement();
        ShipDescriptor shipDescriptor = fModelDescriptor.getShipDescriptor(plot.getID());
        if (shipDescriptor != null) {
          updateS52DisplaySettingsForDraught(shipDescriptor.getDraught());
        }
      }
    }

    private void updateS52DisplaySettingsForDraught(double aDraught) {
      if (aDraught <= 0) {
        //do not alter the display settings when the draught is not specified
        return;
      }

      updateSafetyDepth(aDraught);
    }
  }

  /**
   * More or less based on http://www.warsashacademy.co.uk/news-events/resources/article-ecdis-display-safety-settings-and-alarm-mgt.pdf
   * (figure 1 on page 4)
   */
  public static void updateSafetyDepth(double aDraught) {
    TLcdS52DisplaySettings settings = new TLcdS52DisplaySettings(ECDISConfigurationProvider.getS52DisplaySettings());
    settings.setSafetyDepth(aDraught);
    settings.setSafetyContour(aDraught * 1.2);
    settings.setDeepContour(aDraught * 2.0);
    settings.setShallowContour(Math.min(aDraught, Math.max(settings.getSafetyContour() - 5, 2)));
    ECDISConfigurationProvider.getS52DisplaySettings().setAll(settings);
  }

  /**
   * Show balloon based on selection events.
   */
  private class BalloonOnSelectionListener implements ILcdSelectionListener, PropertyChangeListener {

    private final TLspBalloonManager fBalloonManager;

    public BalloonOnSelectionListener(ILspAWTView aView) {
      fBalloonManager = new TLspBalloonManager(aView,
                                               aView.getOverlayComponent(),
                                               TLcdOverlayLayout.Location.NO_LAYOUT,
                                               new PlotBalloonContentProvider());
    }

    /*
//     experimental less intrusive balloon manager
       public BalloonOnSelectionListener( ILspAWTView aView ) {
      fBalloonManager = new TLspBalloonManager( aView,
                                                aView.getOverlayComponent(),
                                                TLcdOverlayLayout.Location.NO_LAYOUT,
                                                new PlotBalloonContentProvider() ){
        @Override
        protected Rectangle computeBalloonBounds( ALcdBalloonDescriptor aBalloonDescriptor, Rectangle aViewBounds, boolean aIsManualRepositioned, Point aEnforcedPosition, boolean aIsManualResized, Dimension aEnforcedSize ) {
          Rectangle rectangle = super.computeBalloonBounds( aBalloonDescriptor, aViewBounds, aIsManualRepositioned, aEnforcedPosition, aIsManualResized, aEnforcedSize );
          int viewWidth = aViewBounds.width;
          int viewHeight = aViewBounds.height;
          int width = rectangle.width;
          int height = rectangle.height;
          int margin = 10;
          return new Rectangle( viewWidth-width-margin, viewHeight-height-margin, width, height );
        }
      };
    }
     */

    public void registerFor(ILspLayer aViewLayer) {
      aViewLayer.addSelectionListener(this);
      aViewLayer.addPropertyChangeListener(this);
    }

    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      ILcdLayer layer = (ILcdLayer) aSelectionEvent.getSource();
      ILcdModel model = layer.getModel();

      hideDeselectedBalloons(aSelectionEvent.deselectedElements());
      showSelectedBalloon(aSelectionEvent.selectedElements(), model, layer);
    }

    private void hideDeselectedBalloons(Enumeration aDeselectedElements) {
      fLayerFactory.getStyler().setSelectedMMSI(-1);
      while (aDeselectedElements.hasMoreElements()) {
        Object element = aDeselectedElements.nextElement();
        ALcdBalloonDescriptor balloonDescriptor = fBalloonManager.getBalloonDescriptor();

        if (balloonDescriptor != null && balloonDescriptor.getObject() == element) {
          fBalloonManager.setBalloonDescriptor(null);
        }
      }
    }

    private void showSelectedBalloon(Enumeration aSelectedElements, ILcdModel aModel, ILcdLayer aLayer) {
      if (fBalloonManager.getBalloonDescriptor() == null && aSelectedElements.hasMoreElements()) {
        AISPlot element = (AISPlot) aSelectedElements.nextElement();
        fBalloonManager.setBalloonDescriptor(new TLcdModelElementBalloonDescriptor(element, aModel, aLayer));
        fLayerFactory.getStyler().setSelectedMMSI(element.getID());
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("visible")) {
        if (!((Boolean) evt.getNewValue())) {
          ((ILcdLayer) evt.getSource()).clearSelection(ILcdFireEventMode.FIRE_NOW);
        }
      }
    }
  }

  /**
   * Basic balloon content provider.
   */
  private class PlotBalloonContentProvider implements ILcdBalloonContentProvider {

    @Override
    public boolean canGetContent(ALcdBalloonDescriptor aBalloonDescriptor) {
      AISPlot plot = (AISPlot) aBalloonDescriptor.getObject();
      ShipDescriptor ship = fModelDescriptor.getShipDescriptor(plot.getID());
      return ship != null;
    }

    @Override
    public JComponent getContent(final ALcdBalloonDescriptor aBalloonDescriptor) {
      try {
        AISPlot plot = (AISPlot) aBalloonDescriptor.getObject();
        ShipDescriptor ship = fModelDescriptor.getShipDescriptor(plot.getID());
        if (ship == null) {
          return null;
        }
        String isoCode = ExactAISTooltipLogic.getIsoCode(ship);
        String draughtAsString = ship.getDraught() > 0 ? ship.getDraught() + " meters" : "Not available";
        double oceanFloorDepth = plot.getOceanFloorDepth();
        String depthString;
        if (!Double.isNaN(oceanFloorDepth)) {
          DecimalFormat df = new DecimalFormat("#.##");
          depthString = df.format(oceanFloorDepth) + " meters";
        } else {
          depthString = "Unknown";
        }

        String html = "<html><table>";
        html += "<tr>";
        html += "<th colspan=\"3\"><b>" + ship.getVesselName().trim() + "</th>";
        html += "</tr><tr>";
        html += "<td><i>Origin: </td><td colspan=\"2\">" + getCountryName(isoCode) + " <img src=\"" + this.getClass().getResource("/" + getCountryFlagIconLocation(isoCode)) + "\"/></td>";
        html += "</tr><tr>";
        html += "<td><i>Call sign: </td><td colspan=\"2\">" + ship.getCallSign().trim() + "</td>";
        html += "</tr><tr>";
        html += "<td><i>MMSI: </td><td colspan=\"2\">" + ship.getMMSI() + "</td>";
        html += "</tr><tr>";
        html += "<td><i>Ship type: </td><td colspan=\"2\">" + ShipType.getString(ship.getShipType()) + "</td>";
        html += "</tr><tr>";
        html += "<td><i>Destination: </td><td colspan=\"2\">" + escapeSpecialChars(ship.getDestination()) + "</td>";
        html += "</tr><tr valign=\"middle\">";
        html += "<td><i>Navigational status: </td><td>" + NavigationalStatus.getString(plot.getNavigationalStatus()) + "</td><td><img src=\"" + IOUtil.getFile(fLayerFactory.getIconDir() + "/" + plot.getNavigationalStatus() + ".png").toURI() + "\"/></td>";
        html += "</tr><tr>";
        html += "<td><i>Date: </td><td colspan=\"2\">" + sDateFormatter.valueToString(new Date(plot.getTimeStamp())) + "</td>";
        html += "</tr><tr>";
        html += "<td><i>Draught: </i></td><td colspan=\"2\">" + draughtAsString + "</td>";
        html += "</tr>";
        html += "</tr><tr>";
        html += "<td><i>Ocean Surface Depth:: </i></td><td colspan=\"2\">" + depthString + "</td>";
        html += "</tr>";
        html += "</table></html>";

        return new JLabel(html);
      } catch (ParseException e) {
        throw new IllegalStateException(e);
      }
    }

    private String escapeSpecialChars(String aString) {
      return aString
          .trim()
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;");
    }
  }

  private class BCAnimation extends ALcdAnimation {

    private float fB0;
    private float fB1;
    private float fC0;
    private float fC1;
    private ILspView[] fViews;

    private BCAnimation(
        float aB0, float aB1,
        float aC0, float aC1,
        ILspView... aView
    ) {
      super(1.0, aView);
      fB0 = aB0;
      fB1 = aB1;
      fC0 = aC0;
      fC1 = aC1;
      fViews = aView;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      fCurrentBrightness = (float) interpolate(fB0, fB1, fraction);
      fCurrentContrast = (float) interpolate(fC0, fC1, fraction);
      TLspRasterStyle style = TLspRasterStyle.newBuilder()
                                             .brightness(fCurrentBrightness)
                                             .contrast(fCurrentContrast)
                                             .levelSwitchFactor(0.5)
                                             .build();
      for (ILspView view : fViews) {
        view.getServices().getTerrainSupport().setBackgroundStyler(style);
      }
    }

    @Override
    public void start() {
      setTimeImpl(0);
    }

    @Override
    public void stop() {
      setTimeImpl(getDuration());
    }
  }
}
