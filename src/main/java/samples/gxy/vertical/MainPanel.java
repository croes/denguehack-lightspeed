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
package samples.gxy.vertical;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelection;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.TLcdStrokeLineStyle;
import com.luciad.view.vertical.ILcdVVModel;
import com.luciad.view.vertical.ILcdVVRenderer;
import com.luciad.view.vertical.ILcdVVXAxisRenderer;
import com.luciad.view.vertical.TLcdAltitudeRangeSliderAdapter;
import com.luciad.view.vertical.TLcdVVJPanel;
import com.luciad.view.vertical.TLcdVVWithControllersJPanel;

import samples.common.TwoColumnLayoutBuilder;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.decoder.MapSupport;

/**
 * This sample shows how to use the vertical view package.
 * The view in the upper part contains two layers: one layer with a map of
 * Europe and a layer that contains two flights. Each flight is a list of 3D
 * points. The view in the upper part only shows the first two coordinates(X,Y)
 * of each of the points.
 * <p/>
 * The vertical view in the lower part is used to show the vertical profile of
 * the flight that is selected in the upper part. Apart from this main profile,
 * a number of sub-profiles associated to it can be showed in a vertical view.
 * In this case, only one sub-profile is associated to the view: the air route.
 * A sub-profile is divided into sub-profile steps. In this case, for each
 * segment of the main profile, the sub-profile has only one step. The ratio of
 * the sub-profile step is the percentage on the x-axis it represents,
 * relatively to the main-profile. Because there is just one sub-profile step
 * for each segment, the ratio of each of the sub-profile steps is 1.
 * FlightVVModel, an implementation of ILcdVVModel holds the main-profile points
 * and the information about the sub-profile.
 */
public class MainPanel extends GXYSample {

  public static final Color AIRSPACE_COLOR = new Color(25, 102, 0, 128);

  private final ILcdGXYLayer fFlightLayer = createGXYFlightLayer(FlightModelFactory.createFlightModel());
  private final FlightVVModel fFlightVVModel = new FlightVVModel();
  private final TLcdVVJPanel fVerticalView = createVerticalView(fFlightVVModel, new Color[]{AIRSPACE_COLOR});

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-14, 33, 43, 28);
  }

  @Override
  protected JPanel createBottomPanel() {
    // Creates a panel combining the vertical view with extra controllers
    TLcdVVWithControllersJPanel vvWithControllers = createVerticalViewWithControllers(fVerticalView);

    // Replaces the edit controller with a selection controller that also updates the vertical view's cursor.
    VerticalCursorController verticalCursorController = new VerticalCursorController(fVerticalView, fFlightLayer);
    getToolBars()[0].removeGXYController(getToolBars()[0].getGXYCompositeEditController());
    getToolBars()[0].addGXYController(verticalCursorController, 0);
    getView().setGXYController(getToolBars()[0].getGXYController(verticalCursorController));

    return TitledPanel.createTitledPanel("Vertical View", vvWithControllers);
  }

  @Override
  protected JPanel createSettingsPanel() {
    return createUnitPanel(fVerticalView);
  }

  public static JPanel createUnitPanel(TLcdVVJPanel aVerticalView) {
    TitledPanel altitudePanel = TitledPanel.createTitledPanel("Altitude Unit", createAltitudeUnitPanel(aVerticalView));
    TitledPanel distancePanel = TitledPanel.createTitledPanel("Distance Unit", createDistanceUnitPanel(aVerticalView));
    TwoColumnLayoutBuilder builder = TwoColumnLayoutBuilder.newBuilder().row().spanBothColumns(altitudePanel).build().row().spanBothColumns(distancePanel).build();
    JPanel container = new JPanel();
    builder.populate(container);
    return container;
  }

  public static JPanel createDistanceUnitPanel(final TLcdVVJPanel aVerticalView) {
    // create a panel to change the unit on the vertical view.
    TLcdDistanceUnit[] units = new TLcdDistanceUnit[]{
        TLcdDistanceUnit.KM_UNIT,
        TLcdDistanceUnit.MILE_US_UNIT,
        TLcdDistanceUnit.NM_UNIT
    };
    JPanel unitPanel = new JPanel(new GridLayout(units.length, 1));
    ButtonGroup unitButtonGroup = new ButtonGroup();
    for (int index = 0; index < units.length; index++) {
      final TLcdDistanceUnit unit = units[index];
      JRadioButton unitRadioButton = new JRadioButton();
      if (unit != null) {
        String text = unit.toString();
        // Nicer names
        text = "Kilometre".equals(text) ? "Kilometer" : text;
        text = "MileUS".equals(text) ? "Miles" : text;
        text = "NauticalMile".equals(text) ? "Nautical miles" : text;
        unitRadioButton.setText(text);
      } else {
        unitRadioButton.setText("No units");
      }
      unitPanel.add(unitRadioButton);
      unitButtonGroup.add(unitRadioButton);
      unitRadioButton.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          ILcdVVXAxisRenderer vvxAxisRenderer = aVerticalView.getVVXAxisRenderer();
          if ( vvxAxisRenderer instanceof VVXAxisDistanceRenderer) {
            TLcdDistanceFormat format = new TLcdDistanceFormat(unit);
            format.setFractionDigits(0);
            ((VVXAxisDistanceRenderer) vvxAxisRenderer).setDistanceFormat(format);
            aVerticalView.invalidate();
            aVerticalView.repaint();
          }
        }
      });
      if (index == 0) {
        unitRadioButton.setSelected(true);
      }
    }
    return unitPanel;
  }

  public static JPanel createAltitudeUnitPanel(final TLcdVVJPanel aVerticalView) {
    // create a panel to change the unit on the vertical view.
    TLcdAltitudeUnit[] units = new TLcdAltitudeUnit[]{
        TLcdAltitudeUnit.METRE,
        TLcdAltitudeUnit.KM,
        TLcdAltitudeUnit.FLIGHT_LEVEL,
        TLcdAltitudeUnit.FEET,
        // add a custom altitude unit
        new TLcdAltitudeUnit("10 meter", "*10m", 10),
        null
    };

    JPanel unitPanel = new JPanel(new GridLayout(units.length, 1));

    ButtonGroup unitButtonGroup = new ButtonGroup();
    for (int index = 0; index < units.length; index++) {
      final TLcdAltitudeUnit unit = units[index];
      JRadioButton unitRadioButton = new JRadioButton();
      if (unit != null) {
        String text = unit.toString();
        // Use US English.
        text = "Metre".equals(text) ? "Meter" : text;
        text = "Kilometre".equals(text) ? "Kilometer" : text;
        text = "FlightLevel".equals(text) ? "Flight level" : text;
        unitRadioButton.setText(text);
      } else {
        unitRadioButton.setText("No units");
      }
      unitPanel.add(unitRadioButton);
      unitButtonGroup.add(unitRadioButton);
      unitRadioButton.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          aVerticalView.setAltitudeUnit(unit);
          aVerticalView.invalidate();
          aVerticalView.repaint();
        }
      });
      if (index == 0) {
        unitRadioButton.setSelected(true);
      }
    }
    return unitPanel;
  }

  public static TLcdVVJPanel createVerticalView(ILcdVVModel aVVModel, Color[] aColors) {
    // ---- Prepare the vertical view -------------
    final TLcdVVJPanel verticalView = new TLcdVVJPanel();
    // Sets the ILcdVVModel that contains all the information for displaying
    // the profile on the TLcdVVJPanel.
    verticalView.setVVModel(aVVModel);
    // Indicates that labels should be painted on the X-axis.
    verticalView.setXAxisLabeled(true);
    verticalView.setVVXAxisRenderer(new VVXAxisDistanceRenderer());
    // Indicates whether or not all the point-icons should be labeled
    verticalView.setPaintAllLabels(false);
    // Determines how the grid is subdivided
    verticalView.setVVGridLineOrdinateProvider(new VVGridLineOrdinateProvider());
    AltitudeVVRenderer viewRenderer = new AltitudeVVRenderer(verticalView);
    // The rendering mode for the main profile is set to top_line: a line is
    // drawn connecting the points.
    viewRenderer.setMainProfileRenderingMode(ILcdVVRenderer.TOP_LINE);
    // The rendering mode for the sub-profiles is set to filled: filled polygons
    // are drawn at each subProfileStepIndex.
    viewRenderer.setSubProfileRenderingMode(ILcdVVRenderer.FILLED);
    // Color configuration.
    viewRenderer.setProfilePaint(Color.black);
    viewRenderer.setSubProfilePaintArray(aColors);
    viewRenderer.setMainProfileLabelPaint(Color.darkGray);
    // Sets the constructed TLcdDefaultVVRenderer to be used as ILcdVVRenderer
    // to draw issues of the vertical view.
    verticalView.setVVRenderer(viewRenderer);
    return verticalView;
  }

  public static TLcdVVWithControllersJPanel createVerticalViewWithControllers(TLcdVVJPanel aVerticalView) {
    // Constructs a TLcdVVWithControllersJPanel that contains the
    // TLcdVVJPanel and some controllers used to change the left and right
    // offset, the altitude range and the start and end index.
    TLcdVVWithControllersJPanel vvWithControllers =
        new TLcdVVWithControllersJPanel(aVerticalView);
    vvWithControllers.setMinimumSize(new Dimension(0, 200));
    vvWithControllers.setPreferredSize(new Dimension(0, 200));
    // Auto-adjusts the altitude range slider when the model of the vertical view changes.
    new TLcdAltitudeRangeSliderAdapter(aVerticalView, vvWithControllers.getAltitudeRangeSliderPanel());
    return vvWithControllers;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    GXYLayerUtil.addGXYLayer(getView(), fFlightLayer);
    // constructs the listener that will listen for changes in
    // the selection of the flight layer and will set the selected flight on the FlightVVModel
    fFlightLayer.addSelectionListener(new FlightSelectionListener(fFlightVVModel));
    fFlightLayer.selectObject(fFlightLayer.getModel().elements().nextElement(), true, ILcdFireEventMode.FIRE_NOW);
    GXYLayerUtil.addGXYLayer(getView(), fFlightLayer);
  }

  private ILcdGXYLayer createGXYFlightLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel, "Flight");
    layer.setEditable(false);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(), false));
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    layer.setGXYPainterProvider(painter);
    painter.setLineStyle(TLcdStrokeLineStyle.newBuilder()
                                            .color(Color.black)
                                            .lineWidth(1.5f)
                                            .antiAliasing(true)
                                            .selectionColor(new Color(255, 200, 0, 200)).build());
    return layer;
  }

  public static class FlightSelectionListener implements ILcdSelectionListener {
    private final FlightVVModel fVVModel;

    public FlightSelectionListener(FlightVVModel aVVModel) {
      fVVModel = aVVModel;
    }

    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      ILcdSelection selection = aSelectionEvent.getSelection();
      if (selection.getSelectionCount() < 1) {
        fVVModel.setFlight(null);
      } else {
        Enumeration enumeration = selection.selectedObjects();
        Flight flight = (Flight) enumeration.nextElement();
        fVVModel.setFlight(flight);
      }
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Vertical view");
  }

}
