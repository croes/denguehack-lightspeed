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
package samples.gxy.magneticnorth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.format.magneticnorth.ALcdMagneticNorthModelDescriptor;
import com.luciad.format.magneticnorth.gxy.TLcdMagneticNorthGXYController;
import com.luciad.format.magneticnorth.gxy.TLcdMagneticNorthGXYLabelPainter;
import com.luciad.format.magneticnorth.gxy.TLcdMagneticNorthGXYPainter;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.navigationcontrols.ALcdCompassNavigationControl;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdStereographic;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.swing.navigationcontrols.TLcdGXYCompassNavigationControl;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.HaloLabel;
import samples.gxy.common.AntiAliasedLabelPainter;
import samples.gxy.common.AntiAliasedPainter;
import samples.gxy.common.GXYLayerSelectionPanel;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.decoder.MapSupport;

/**
 * This sample shows you the LuciadLightspeed capabilities in respect with the magnetic
 * north.   The magnetic north does not coincide with the true north.  The
 * difference between them is dependent on your position on the earth and the
 * current time (date).
 *
 * First of all, there is a controller that allows you to
 * rotate the map so that the direction of the magnetic north (for the point
 * where you clicked) is at the top of your screen.  Also, the map will be
 * recentered on the point where you clicked.
 *
 * When you point on the map, the map also displays the
 * declination of your current mouse position.  The declination is the angle (in
 * degrees) between the true north and the magnetic north.  You can think of it
 * as the error a compass would 'make' on a certain position as your compass
 * points to the magnetic north and not to the true north.  The declination is
 * calculated for the current date (today).
 *
 * Second, there is a feature to calculate iso charts of the declination of the
 * magnetic north.  For every point on such a line, the declination is constant.
 * Several properties can be set: the model to use, an accommodation between
 * speed and precision, the density of the lines and the time (date).
 * There are different mathematical models to calculate the magnetic north, you
 * can choose between the two most popular models.
 * The density of the lines means how many lines should be calculated.  A value
 * of 2 for instance, means a line is calculated for zero degrees, 2 degrees, 4
 * degrees and so on.
 * The declination is also dependant on the date, which is configurable.  Note
 * that this date value is limited by the model.  A model is only valid between
 * certain dates and can only predict for a couple of years at most.
 */
public class MainPanel extends GXYSample {

  // a controller that rotates the map such that the direction to the magnetic north (what a compass would indicate)
  // is equal to the (vertical) direction to the top of your screen.
  private TLcdMagneticNorthGXYController fController = new TLcdMagneticNorthGXYController();
  // a custom compass widget pointing to the magnetic north
  private TLcdGXYCompassNavigationControl fNavigationControl;

  @Override
  protected void createGUI() {
    super.createGUI();
    addMagneticNorthControllerToToolbar(getView(), getToolBars()[0]);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.add(new HaloLabel("Magnetic north"), BorderLayout.SOUTH);
    panel.add(fNavigationControl, BorderLayout.CENTER);
    getOverlayPanel().add(panel, TLcdOverlayLayout.Location.NORTH_EAST);
  }

  @Override
  protected JPanel createSettingsPanel() {
    try {
      fNavigationControl = new TLcdGXYCompassNavigationControl("images/gui/navigationcontrols/small/" +
                                                               ALcdCompassNavigationControl.COMPASSPAN_COMPONENT_DIR, getView());
    } catch (IOException e) {
      e.printStackTrace();
    }
    GXYLayerSelectionPanel panel = new GXYLayerSelectionPanel(
        getView(), getOverlayPanel()) {
      @Override
      protected void changeLayer(ILcdGXYLayer aOldLayer, ILcdGXYLayer aNewLayer) {
        super.changeLayer(aOldLayer, aNewLayer);
        ALcdMagneticNorthModelDescriptor modelDescriptor =
            (ALcdMagneticNorthModelDescriptor) aNewLayer.getModel().getModelDescriptor();
        fController.setMagneticNorthMap(modelDescriptor.getMagneticNorthMap());
        fNavigationControl.setMagneticNorthMap(modelDescriptor.getMagneticNorthMap());
      }
    };
    panel.addLayer("WMM", createWMMMagneticNorthLayer(), null, TLcdOverlayLayout.Location.SOUTH);
    panel.addLayer("IGRF", createIGRFMagneticNorthLayer(), null, TLcdOverlayLayout.Location.SOUTH);
    return TitledPanel.createTitledPanel("Model type", panel);
  }

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = super.createMap();
    map.setXYWorldReference(new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdStereographic()));
    // Make the grid layer invisible for better visibility of magnetic north layer.
    map.getGridLayer().setVisible(false);
    return map;
  }

  private void addMagneticNorthControllerToToolbar(ILcdGXYView aGXYView, ToolBar aToolBar) {
    // the controller can also display the declination in a tooltip. We'll be using the
    // TLcdMagneticNorthMeasureProviderFactory instead.
    fController.setDisplayLabel(false);
    aToolBar.addGXYController(fController);
    // Set the controller as the current controller
    aGXYView.setGXYController(aToolBar.getGXYController(fController));
  }

  private ILcdGXYLayer createWMMMagneticNorthLayer() {
    // create a layer factory for the magnetic north model
    MagneticNorthGXYLayerFactory layerFactory = new MagneticNorthGXYLayerFactory();
    // let the factory create a layer for the model of the chart
    return layerFactory.createGXYLayer(MagneticNorthModelFactory.createWMMMagneticNorthModel("", this, getStatusBar()));
  }

  private ILcdGXYLayer createIGRFMagneticNorthLayer() {

    ILcdModel model = MagneticNorthModelFactory.createIGRFMagneticNorthModel("", this, getStatusBar());

    //--------- Init the painters ----------

    //create a painter with custom colors
    TLcdMagneticNorthGXYPainter painter = new TLcdMagneticNorthGXYPainter();
    painter.setZeroStyle(new TLcdG2DLineStyle(Color.black, Color.gray));
    painter.setPositiveStyle(new TLcdG2DLineStyle(Color.red, Color.magenta));
    painter.setNegativeStyle(new TLcdG2DLineStyle(Color.blue, Color.magenta));
    painter.setEmphasizedPositiveStyle(new TLcdG2DLineStyle(Color.darkGray, Color.lightGray));
    painter.setEmphasizedNegativeStyle(new TLcdG2DLineStyle(Color.darkGray, Color.lightGray));

    TLcdMagneticNorthGXYLabelPainter labelPainter = new TLcdMagneticNorthGXYLabelPainter();
    labelPainter.setHaloEnabled(true);
    labelPainter.setForeground(Color.white);
    labelPainter.setHaloColor(Color.darkGray);

    //--------- Init a TLcdGXYLayer to add the model to ---------

    //here, we don't use the layer factory, but instead create and initialise our own layer
    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setSelectable(false);
    layer.setEditable(false);
    layer.setGXYPen(MapSupport.createPen(model.getModelReference()));
    layer.setLabeled(true);

    //set our custom configured painters to the layer
    layer.setGXYPainterProvider(new AntiAliasedPainter((ILcdGXYPainterProvider) painter));
    layer.setGXYLabelPainterProvider(new AntiAliasedLabelPainter((ILcdGXYLabelPainterProvider) labelPainter));

    return layer;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Magnetic north");
  }
}
