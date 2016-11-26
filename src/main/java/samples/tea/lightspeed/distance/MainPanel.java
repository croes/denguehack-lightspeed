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
package samples.tea.lightspeed.distance;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.tea.TLcdHeightProviderAdapter;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController.MeasureMode;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.tea.lightspeed.HeightProviderUtil;
import samples.tea.lightspeed.HeightProviderUtil.DTEDLevel;

/**
 * This sample demonstrates how to calculate distance over terrain.
 */
public class MainPanel extends LightspeedSample {

  private static final TLcdLonLatPoint FIRST_POINT = new TLcdLonLatPoint(10, 45);
  private static final TLcdLonLatPoint SECOND_POINT = new TLcdLonLatPoint(11, 46);
  private static final TLcdLonLatPoint THIRD_POINT = new TLcdLonLatPoint(11, 45);

  private TerrainRulerController fTerrainRulerController = new TerrainRulerController();

  protected void createGUI() {
    super.createGUI();
    addComponentToRightPanel(buildMeasureModePanel());

    getView().setController(fTerrainRulerController);
  }

  /**
   * Replaces the standard ruler with the terrain ruler.
   *
   * @param aView       the view to which the toolbar will be associated
   *
   * @return the default toolbar with a replaced ruler
   */
  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    return new JToolBar[]{new ToolBar(aView, this, true, true) {

      @Override
      protected TLspRulerController createRulerController() {
        return fTerrainRulerController;
      }

      @Override
      protected ILcdFilter<ILspLayer> createStickyLabelsLayerFilter() {
        return MainPanel.this.createStickyLabelsLayerFilter();
      }

    }};
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    LspDataUtil.instance().grid().addToView(getView());
    ILspLayer dtedLayer = LspDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(getView()).fit().getLayer();

    if (dtedLayer != null) {
      fTerrainRulerController.setTerrainElevationProvider(createAltitudeProvider(dtedLayer.getModel()));
    }

    // Create a point model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing some geodetic points",   // source name (is used as tooltip text)
        "Point",          // type name
        "Points"          // display name
    ));

    // Add some points to the model, to allow comparison between two measurements.
    model.addElement(FIRST_POINT, ILcdFireEventMode.NO_EVENT );
    model.addElement(SECOND_POINT, ILcdFireEventMode.NO_EVENT );
    model.addElement(THIRD_POINT, ILcdFireEventMode.NO_EVENT );

    // Create a layer for the point model.
    ILspLayer pointLayer = TLspShapeLayerBuilder.newBuilder()
                                                .model(model)
                                                .selectable(false)
                                                .bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 10, Color.WHITE, Color.BLUE)).build())
                                                .build();

    // Add the layer to the map.
    getView().addLayer(pointLayer);
  }

  private TLcdHeightProviderAdapter createAltitudeProvider(ILcdModel aModel) {
    return new TLcdHeightProviderAdapter(createHeightProvider(aModel), (ILcdGeoReference) aModel.getModelReference());
  }

  private ILcdHeightProvider createHeightProvider(ILcdModel aModel) {
    return HeightProviderUtil.getHeightProvider(aModel, (ILcdGeoReference) aModel.getModelReference(), FIRST_POINT, DTEDLevel.LEVEL_1);
  }

  private JPanel buildMeasureModePanel() {
    JRadioButton geodeticMeasureModeRadioButton = new JRadioButton( "Geodetic" );
    geodeticMeasureModeRadioButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fTerrainRulerController.setMeasureMode(MeasureMode.MEASURE_GEODETIC);
      }
    });

    JRadioButton rhumbLineMeasureModeRadioButton = new JRadioButton( "Constant azimuth" );
    rhumbLineMeasureModeRadioButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fTerrainRulerController.setMeasureMode(MeasureMode.MEASURE_RHUMB);
      }
    });
    geodeticMeasureModeRadioButton.setSelected(true);

    JCheckBox terrainModeCheckbox = new JCheckBox( "Over terrain" );
    terrainModeCheckbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean useTerrain = e.getStateChange() == ItemEvent.SELECTED;
        fTerrainRulerController.setUseTerrain(useTerrain);
      }
    });
    terrainModeCheckbox.setSelected(true);

    ButtonGroup measureModesGroup = new ButtonGroup();
    measureModesGroup.add(geodeticMeasureModeRadioButton);
    measureModesGroup.add(rhumbLineMeasureModeRadioButton);

    JPanel measureModePanel = new JPanel( new GridLayout( 3, 1 ) );
    measureModePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    measureModePanel.add(geodeticMeasureModeRadioButton);
    measureModePanel.add(rhumbLineMeasureModeRadioButton);
    measureModePanel.add(terrainModeCheckbox);

    return TitledPanel.createTitledPanel( "Measure mode", measureModePanel );
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        startSample(MainPanel.class, "Measuring distance over terrain");
      }
    } );
  }
}
