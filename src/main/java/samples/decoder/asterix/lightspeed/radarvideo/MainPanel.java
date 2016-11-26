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
package samples.decoder.asterix.lightspeed.radarvideo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.luciad.format.asterix.TLcdASTERIXModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.TLcdSimulatorModelList;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.common.SampleData;
import samples.decoder.asterix.ASTERIXTrackSimulatorModelFactory;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarFactory;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarStyleProperties;
import samples.decoder.asterix.lightspeed.radarvideo.radar.RadarStylingConfigurationPanel;
import samples.decoder.asterix.lightspeed.radarvideo.radar.SimulatedRadarProperties;
import samples.decoder.asterix.lightspeed.radarvideo.tracks.TrackLayerFactory;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ProjectionSupport;
import samples.lightspeed.decoder.UnstyledLayerFactory;

/**
 * This sample shows how to visualize a simulated ASTERIX Category 240 radar data stream.
 * It is generated by creating a simulator with ASTERIX Category 21 data. The dynamic tracks generated from this data are
 * detected on our simulated radars from which an ASTERIX Category 240 stream is generated.
 */
public class MainPanel extends LightspeedSample {

  //Default style properties for the radars.
  protected static final RadarStyleProperties sRadarStyleProperties = new RadarStyleProperties(
      new Color(255, 168, 56, 255),
      new Color(0, 168, 56, 255),
      new Color(32, 32, 32, 128),
      new Color(192, 192, 192, 64),
      new Color(192, 192, 192, 128),
      15,
      1.0,
      0.01
  );

  private List<ILcdDisposable> fTimersToDispose;

  public static void main(String[] args) {
    startSample(MainPanel.class, "Static radar sample");
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    JPanel rightPanelComponent = new JPanel();
    rightPanelComponent.setLayout(new GridLayout(1, 1));

    RadarStylingConfigurationPanel configurationPanel = new RadarStylingConfigurationPanel(sRadarStyleProperties);
    rightPanelComponent.add(TitledPanel.createTitledPanel("Radar Style", configurationPanel));
    addComponentToRightPanel(rightPanelComponent, BorderLayout.SOUTH);
  }

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView();
    view.setBackground(Color.black);
    view.getServices().getTerrainSupport().setBackgroundStyler(
        TLspRasterStyle
            .newBuilder()
            .brightness(0.25f)
            .contrast(0.25f)
            .build()
    );
    return view;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    ProjectionSupport support = new ProjectionSupport(getView());
    support.setProjection("Mercator");

    UnstyledLayerFactory factory = new UnstyledLayerFactory();
    factory.setFillStyle(null);
    factory.setLineStyle(TLspLineStyle.newBuilder().color(new Color(140, 25, 25, 160)).build());
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer(factory).label("Countries").labeled(false).addToView(getView());

    // Add ASTERIX category 240 data
    addRadarVideo();
  }

  @Override
  protected void tearDown() {
    for (ILcdDisposable disposable : fTimersToDispose) {
      disposable.dispose();
    }
    super.tearDown();
  }

  protected void addRadarVideo() throws IOException {
    //Decode ASTERIX Category 21 data and create a simulator for it to visualize the dynamic tracks.
    String source = "Data/ASTERIX/atx_cat21_zurich.asterix";
    ILcdModelDecoder decoder = new TLcdASTERIXModelDecoder();
    ILcdModel model = decoder.decode(source);
    ASTERIXTrackSimulatorModelFactory modelFactory = new ASTERIXTrackSimulatorModelFactory();
    TLcdSimulatorModelList simulatorModelList = (TLcdSimulatorModelList) modelFactory.createSimulatorModel(model);
    TLcdSimulator simulator = createSimulator(simulatorModelList);
    simulator.addView(getView());

    //Add the simulated radars
    fTimersToDispose = new ArrayList<ILcdDisposable>();
    SimulatedRadarProperties radarSwitzerland = new SimulatedRadarProperties("Radar - Switzerland", new TLcdLonLatPoint(9.022743, 47.291471), 400000, 500, 1.0, 2);
    SimulatedRadarProperties radarZurich = new SimulatedRadarProperties("Radar - Zurich", new TLcdLonLatPoint(8.555476, 47.458390), 20000, 20, 0.4, 1);
    RadarFactory.addRadarToView(getView(), radarSwitzerland, simulatorModelList, sRadarStyleProperties, fTimersToDispose, MainPanel.this);
    RadarFactory.addRadarToView(getView(), radarZurich, simulatorModelList, sRadarStyleProperties, fTimersToDispose, MainPanel.this);

    //Fit on Switzerland.
    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(8, 47, 1, 1), new TLcdGeodeticReference(new TLcdGeodeticDatum()));
  }

  private TLcdSimulator createSimulator(ILcdSimulatorModel aSimulatorModel) throws IOException {
    TrackLayerFactory layerFactory = new TrackLayerFactory();

    //Create a new TLcdSimulator and set it up
    TLcdSimulator simulator = new TLcdSimulator();
    simulator.setSimulatorModel(aSimulatorModel);
    simulator.setPauseOnMousePressed(false);
    simulator.setLspLayerFactory(layerFactory);
    simulator.setDate(simulator.getBeginDate());
    simulator.setDelayBetweenUpdateMs(25);
    simulator.setMaxCPUUsage(60);
    simulator.setPlayInLoop(true);
    simulator.run();

    return simulator;
  }
}
