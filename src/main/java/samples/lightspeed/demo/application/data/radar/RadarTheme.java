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
package samples.lightspeed.demo.application.data.radar;

import java.awt.Color;
import java.awt.Dimension;
import java.io.EOFException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.format.asterix.TLcdASTERIXLiveDecoder;
import com.luciad.format.asterix.TLcdASTERIXReferenceProvider;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainerListener;
import com.luciad.model.TLcdModelContainerEvent;
import com.luciad.model.TLcdModelList;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.lightspeed.radarvideo.TLspRadarVideoLayerBuilder;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.common.HaloLabel;
import samples.decoder.asterix.ASTERIXTrackSimulatorModelFactory;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarStyleProperties;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarStyler;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarStylingConfigurationPanel;
import samples.decoder.asterix.lightspeed.radarvideo.radar.SimulatedRadarProperties;
import samples.decoder.asterix.lightspeed.radarvideo.radar.SimulatedRadarStream;
import samples.decoder.asterix.lightspeed.radarvideo.tracks.TrackLayerFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.lightspeed.demo.framework.gui.RoundedBorder;
import samples.lightspeed.demo.simulation.SimulationSupport;

public class RadarTheme extends AbstractTheme {

  //Default style properties for the radars.
  private static final RadarStyleProperties sRadarStyleProperties = new RadarStyleProperties(
      new Color(0, 168, 56, 255),
      new Color(0, 168, 56, 255),
      new Color(32, 32, 32, 128),
      new Color(192, 192, 192, 64),
      new Color(192, 192, 192, 128),
      15,
      1.0,
      0.01
  );

  private final Map<ILspLayer, ILspView> fRadarLayers = new HashMap<ILspLayer, ILspView>();
  private final List<SimulatedRadarStream> fSimulatedStreams = new ArrayList<SimulatedRadarStream>();
  private final List<ScheduledExecutorService> fTimers = new ArrayList<ScheduledExecutorService>();

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(RadarTheme.class);

  private float fCurrentBrightness = 1;
  private float fCurrentContrast = 1;
  private float fInitialBrightness = 0.8f;
  private float fInitialContrast = 1.0f;
  private Collection<ILspLayer> fTrackLayers = new ArrayList<ILspLayer>();
  private TLcdSimulator fSimulator;

  public RadarTheme() {
    setName("SimulatedRadarProperties");
    setCategory("Tracks");
  }

  @Override
  public List<JPanel> getThemePanels() {

    RadarStylingConfigurationPanel radarConfigurationPanel = new RadarStylingConfigurationPanel(sRadarStyleProperties);
    DefaultFormBuilder themePanelBuilder = new DefaultFormBuilder(new FormLayout("p:grow"));
    themePanelBuilder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    themePanelBuilder.append(new HaloLabel("Radar Style", 15, true));

    DefaultFormBuilder subPanelBuilder = createSubPanelBuilder(1, 1);
    subPanelBuilder.append(radarConfigurationPanel);
    subPanelBuilder.opaque(false);
    themePanelBuilder.append(subPanelBuilder.getPanel());

    themePanelBuilder.append(new HaloLabel("Background", 15, true));
    subPanelBuilder = createSubPanelBuilder(1, 1);
    subPanelBuilder.opaque(false);
    subPanelBuilder.append(createBackgroundConfigurationPanel());
    themePanelBuilder.append(subPanelBuilder.getPanel());

    JPanel result = themePanelBuilder.build();
    result.setSize(result.getLayout().preferredLayoutSize(result));

    return Collections.singletonList(result);
  }

  private DefaultFormBuilder createSubPanelBuilder(int aNumberOfRows, int aNumberOfColumns) {
    String layoutString = "";
    for (int i = 0; i < aNumberOfColumns; i++) {
      layoutString += "p, 5dlu, ";
    }
    layoutString += "p:grow";
    DefaultFormBuilder result = new DefaultFormBuilder(new FormLayout(layoutString));
    final RoundedBorder roundedBorder = new RoundedBorder(15, DemoUIColors.SUB_PANEL_COLOR, DemoUIColors.SUB_PANEL_COLOR);
    roundedBorder.setTotalItems(aNumberOfRows);
    result.border(roundedBorder);
    return result;
  }

  private JPanel createBackgroundConfigurationPanel() {
    final DecimalFormat labelFormat = new DecimalFormat("0.00");

    final JLabel labelBrightnessSlider = new JLabel();
    labelBrightnessSlider.setText(labelFormat.format(fInitialBrightness));

    final JLabel labelContrastSlider = new JLabel();
    labelContrastSlider.setText(labelFormat.format(fInitialContrast));

    final JSlider brightnessSlider = createSlider(0, 200, 150);
    brightnessSlider.setToolTipText("Adjust the brightness of the background.");
    brightnessSlider.setValue((int) (fCurrentBrightness * 100));
    brightnessSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        double value = brightnessSlider.getValue() * 0.01;
        fCurrentBrightness = (float) value;
        setBackgroundBrightnessAndContrast(fCurrentBrightness, fCurrentContrast);
        labelBrightnessSlider.setText(labelFormat.format(fCurrentBrightness));
      }
    });

    final JSlider contrastSlider = createSlider(0, 200, 150);
    contrastSlider.setToolTipText("Adjust the contrast of the background.");
    contrastSlider.setValue((int) (fCurrentContrast * 100));
    contrastSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        double value = contrastSlider.getValue() * 0.01;
        fCurrentContrast = (float) value;
        setBackgroundBrightnessAndContrast(fCurrentBrightness, fCurrentContrast);
        labelContrastSlider.setText(labelFormat.format(fCurrentContrast));
      }
    });

    FormLayout layout = new FormLayout("left:pref, 5px, fill:pref, 5px, left:pref", "pref, 2px, pref");
    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(layout);
    panel.add(new JLabel("Brigthness"), cc.xy(1, 1));
    panel.add(brightnessSlider, cc.xy(3, 1));
    panel.add(labelBrightnessSlider, cc.xy(5, 1));

    panel.add(new JLabel("Contrast"), cc.xy(1, 3));
    panel.add(contrastSlider, cc.xy(3, 3));
    panel.add(labelContrastSlider, cc.xy(5, 3));

    panel.setOpaque(false);

    return panel;
  }

  private JSlider createSlider(int aMin, int aMax, final int aWidth) {
    return new JSlider(aMin, aMax) {
      public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = Math.min(aWidth, pref.width);
        return pref;
      }
    };
  }

  @Override
  protected List<ILspLayer> createLayers(final List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> allLayers = new ArrayList<ILspLayer>();
    List<ILspLayer> layers = framework.getLayersWithID("layer.id.world");
    allLayers.addAll(layers);
    layers = framework.getLayersWithID("layer.id.fusion.osm.places");
    allLayers.addAll(layers);

    ILcdModel asterixModel = framework.getModelWithID("model.id.radar.tracks");
    final ILcdSimulatorModel simulatorModel = createSimulator(asterixModel);

    TrackLayerFactory trackLayerfactory = new TrackLayerFactory();

    fSimulator = new TLcdSimulator();
    fSimulator.setSimulatorModel(simulatorModel);
    fSimulator.setLspLayerFactory(trackLayerfactory);
    fSimulator.setPauseOnMousePressed(false);
    fSimulator.setDate(fSimulator.getBeginDate());
    fSimulator.setDelayBetweenUpdateMs(25);
    fSimulator.setMaxCPUUsage(60);
    fSimulator.setPlayInLoop(true);

    for (int i = 0; i < aViews.size(); i++) {
      ILspView view = aViews.get(i);
      fSimulator.addView(view);
      ILspLayer[][] simulationLspLayers = fSimulator.getSimulationLspLayers();
      List<ILspLayer> tracks = Arrays.asList(simulationLspLayers[i]);
      fTrackLayers.addAll(tracks);
      framework.registerLayers("layer.id.radar.tracks", view, tracks);
      allLayers.addAll(tracks);
    }

    return allLayers;
  }

  public Map<ILspLayer, ILspView> getRadarLayers() {
    return fRadarLayers;
  }

  @Override
  public void activate() {
    super.activate();
    ALcdAnimation dimAnimation = new DimBGAnimation(fCurrentBrightness, fInitialBrightness, fCurrentContrast, fInitialContrast);
    ALcdAnimationManager.getInstance().putAnimation(this, dimAnimation);
    initializeRadars();
    fSimulator.run();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    ALcdAnimation dimAnimation = new DimBGAnimation(fCurrentBrightness, 1, fCurrentContrast, 1);
    ALcdAnimationManager.getInstance().putAnimation(this, dimAnimation);
    removeRadars();
    fSimulator.stop();
  }

  private ILspView[] getViews() {
    Framework framework = Framework.getInstance();
    List<ILspView> views = framework.getFrameworkContext().getViews();
    return views.toArray(new ILspView[views.size()]);
  }

  private void removeRadars() {
    // Stop the stream decoding
    try {
      for (int i = 0; i < fSimulatedStreams.size(); i++) {
        fSimulatedStreams.get(i).close();
      }
      fSimulatedStreams.clear();
    } catch (IOException e) {
      sLogger.error(e.getMessage());
    }

    // Block until we are sure the timers are not working anymore
    for (ScheduledExecutorService t : fTimers) {
      t.shutdown();
      try {
        t.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
      }
    }
    fTimers.clear();

    Set<ILspLayer> keySet = fRadarLayers.keySet();
    for (ILspLayer layer : keySet) {
      ILspView view = fRadarLayers.get(layer);
      view.removeLayer(layer);
    }
    fRadarLayers.clear();
  }

  private void initializeRadars() {
    Framework framework = Framework.getInstance();

    ILcdModel asterixModel = framework.getModelWithID("model.id.radar.tracks");
    ILcdSimulatorModel simulatorModel = createSimulator(asterixModel);

    List<SimulatedRadarProperties> simulatedRadars = createSimulatedRadarProperties();
    for (int i = 0; i < simulatedRadars.size(); i++) {
      try {
        addSimulatedRadar(simulatorModel.getTrackModels(), simulatedRadars.get(i));
      } catch (IOException e) {
        sLogger.error("Could not create radar.");
      }
    }
  }

  private void addSimulatedRadar(ILcdModel[] aModels, final SimulatedRadarProperties aSimulatedRadarProperties) throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);

    final TLcdModelList modelList = new TLcdModelList();
    modelList.addModelContainerListener(new ILcdModelContainerListener() {
      @Override
      public void modelContainerStateChanged(final TLcdModelContainerEvent aModelContainerEvent) {
        Runnable runnable = new Runnable() {
          public void run() {
            Framework framework = Framework.getInstance();
            ILcdModel model = aModelContainerEvent.getModel();
            List<ILspView> views = framework.getFrameworkContext().getViews();
            for (int i = 0; i < views.size(); i++) {
              ILspLayer radarLayer = createRadarLayer(model, aSimulatedRadarProperties);
              ILspView view = views.get(i);
              fRadarLayers.put(radarLayer, view);
              int index = -1;
              for (ILcdLayer l : fTrackLayers) {
                if (view.containsLayer(l)) {
                  index = view.indexOf(l);
                  break;
                }
              }
              view.addLayer(radarLayer);
              if (index != -1) {
                view.moveLayerAt(Math.max(0, index - 1), radarLayer);
              }
            }
          }
        };
        latch.countDown();
        TLcdAWTUtil.invokeAndWait(runnable);
      }
    });

    SimulatedRadarStream radarStream = new SimulatedRadarStream(aSimulatedRadarProperties.getPosition(),
                                                                aSimulatedRadarProperties.getAngularResolution(),
                                                                aSimulatedRadarProperties.getRange(),
                                                                aSimulatedRadarProperties.getCellRange(),
                                                                aSimulatedRadarProperties.getSecondsPerRotation());
    fSimulatedStreams.add(radarStream);
    fSimulatedStreams.add(radarStream);

    //add the models to track on the radar
    for (int i = 0; i < aModels.length; i++) {
      if (aModels[i] instanceof ILcd2DBoundsIndexedModel) {
        radarStream.addModel((ILcd2DBoundsIndexedModel) aModels[i]);
      }
    }

    final TLcdASTERIXLiveDecoder liveDecoder = new TLcdASTERIXLiveDecoder();
    liveDecoder.setReferenceProvider(new TLcdASTERIXReferenceProvider(aSimulatedRadarProperties.getPosition()));
    liveDecoder.initDecode(radarStream);

    //read data blocks from the data stream at fixed time intervals.
    final ScheduledThreadPoolExecutor t = new ScheduledThreadPoolExecutor(1);
    t.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              int decodedMessages = 0;
              while (liveDecoder.blockAvailable()) {
                liveDecoder.decodeBlockSFCT(ILcdModel.FIRE_LATER, modelList);
                decodedMessages++;
              }
              if (decodedMessages > 0) {
                modelList.fireCollectedModelChanges();
              }
            } catch (EOFException e1) {
            } catch (IOException e1) {
            }
          }
        },
        0, 1, TimeUnit.MILLISECONDS
    );
    fTimers.add(t);

    try {
      latch.await();
    } catch (InterruptedException ignored) {
    }
  }

  private ILspLayer createRadarLayer(final ILcdModel aRadarModel, SimulatedRadarProperties aSimulatedRadarProperties) {
    RadarStyler radarStyler1 = new RadarStyler(sRadarStyleProperties, false);
    RadarStyler radarStyler2 = new RadarStyler(sRadarStyleProperties, true);

    final ILspLayer radarLayer = TLspRadarVideoLayerBuilder.newBuilder()
                                                           .model(aRadarModel)
                                                           .selectable(false)
                                                           .label(aSimulatedRadarProperties.getName())
                                                           .bodyStyler(TLspPaintState.REGULAR, radarStyler1)
                                                           .bodyStyler(TLspPaintState.SELECTED, radarStyler2)
                                                           .build();

    return radarLayer;
  }

  private ILcdSimulatorModel createSimulator(ILcdModel aModel) {
    TLcdLockUtil.writeLock(aModel);
    try {
      String key = RadarTheme.class.getName() + "-" + aModel.getModelDescriptor().getSourceName();
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) SimulationSupport.getInstance().getSharedSimulatorModel(key);

      RadarTheme radarTheme = Framework.getInstance().getThemeByClass(RadarTheme.class);

      if (simulatorModel == null) {
        simulatorModel = new ASTERIXTrackSimulatorModelFactory().createSimulatorModel(aModel);
        SimulationSupport.getInstance().setSharedSimulatorModel(key, simulatorModel);
      }

      SimulationSupport.getInstance().addSimulatorModelForTheme(simulatorModel, radarTheme);

      return simulatorModel;
    } finally {
      TLcdLockUtil.writeUnlock(aModel);
    }
  }

  private List<SimulatedRadarProperties> createSimulatedRadarProperties() {
    List<SimulatedRadarProperties> simulatedRadarProperties = new ArrayList<SimulatedRadarProperties>();
    simulatedRadarProperties.add(new SimulatedRadarProperties("Radar - Switzerland", new TLcdLonLatPoint(9.022743, 47.291471), 400000, 500, 1.0, 2));
    simulatedRadarProperties.add(new SimulatedRadarProperties("Radar - Zurich", new TLcdLonLatPoint(8.555476, 47.458390), 20000, 20, 0.4, 1));
    return simulatedRadarProperties;
  }

  private void setBackgroundBrightnessAndContrast(float aBrightness, float aContrast) {
    TLspRasterStyle style = TLspRasterStyle.newBuilder()
                                           .brightness(aBrightness)
                                           .contrast(aContrast)
                                           .levelSwitchFactor(0.5)
                                           .build();
    for (ILspView view : getViews()) {
      view.getServices().getTerrainSupport().setBackgroundStyler(style);
    }
  }

  private class DimBGAnimation extends ALcdAnimation {

    private float fB0;
    private float fB1;
    private float fC0;
    private float fC1;

    private DimBGAnimation(
        float aB0, float aB1,
        float aC0, float aC1
    ) {
      super(1.0);
      fB0 = aB0;
      fB1 = aB1;
      fC0 = aC0;
      fC1 = aC1;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      fCurrentBrightness = (float) interpolate(fB0, fB1, fraction);
      fCurrentContrast = (float) interpolate(fC0, fC1, fraction);
      setBackgroundBrightnessAndContrast(fCurrentBrightness, fCurrentContrast);
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
