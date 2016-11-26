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
package samples.ais.gxy;

import com.luciad.ais.model.aerodrome.TLcdAerodromeModelDescriptor;
import com.luciad.ais.model.aerodrome.TLcdRunwayModelDescriptor;
import com.luciad.ais.model.airspace.TLcdAirspaceModelDescriptor;
import com.luciad.ais.model.navaid.TLcdWayPointModelDescriptor;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectoryModelDescriptor;
import com.luciad.ais.model.route.TLcdATSRouteModelDescriptor;
import com.luciad.ais.symbology.icao.TLcdICAOAirspaceDetailedLabelPainter;
import com.luciad.ais.symbology.icao.TLcdICAOAirspacePainter;
import com.luciad.ais.symbology.icao.TLcdICAODefaultSymbolProvider;
import com.luciad.ais.symbology.icao.TLcdICAORunwayPainter;
import com.luciad.ais.view.gxy.painter.TLcdGXYAISIconProviderPainter;
import com.luciad.ais.view.gxy.painter.TLcdGXYProcedureTrajectoryPainter;
import com.luciad.ais.view.gxy.painter.TLcdGXYRoutePainter;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.ais.model.MyAirspaceDataProperties;
import samples.ais.model.MyRunwayDataProperties;
import samples.ais.view.gxy.TLcdGXYProcedureTrajectoryLegLabelPainter;

import java.awt.Color;
import java.awt.Font;

/**
 * Layer factory for AIS models.
 */
class LayerFactory implements ILcdGXYLayerFactory {

  //method of ILcdGXYLayerFactory
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    // Check whether aModel is an AIS model and create a layer accordingly.
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    if (descriptor instanceof TLcdAerodromeModelDescriptor) {
      return createAerodromeLayer(aModel);
    } else if (descriptor instanceof TLcdWayPointModelDescriptor) {
      return createWayPointLayer(aModel);
    } else if (descriptor instanceof TLcdATSRouteModelDescriptor) {
      return createRouteLayer(aModel);
    } else if (descriptor instanceof TLcdProcedureTrajectoryModelDescriptor) {
      return createProcedureTrajectoryLayer(aModel);
    } else if (descriptor instanceof TLcdAirspaceModelDescriptor) {
      return createAirspaceLayer(aModel);
    } else if (descriptor instanceof TLcdRunwayModelDescriptor) {
      return createRunwayLayer(aModel);
    }

    return null;
  }

  /**
   * Create an ILcdGXYLayer which shows aerodromes with their ICAO symbol and labels them
   * with the ICAO code.
   *
   * @param aModel A model containing aerodromes.
   * @return an ILcdGXYLayer which shows aerodromes.
   */
  private ILcdGXYLayer createAerodromeLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Airports");

    // Initialize a painter with icon providers that use ICAO symbology.
    TLcdICAODefaultSymbolProvider icon_provider = new TLcdICAODefaultSymbolProvider();
    TLcdICAODefaultSymbolProvider selection_icon_provider = new TLcdICAODefaultSymbolProvider();
    icon_provider.setColor(Color.white);
    selection_icon_provider.setColor(Color.red);
    TLcdGXYAISIconProviderPainter painter_provider = new TLcdGXYAISIconProviderPainter();
    painter_provider.setIconProvider(icon_provider);
    painter_provider.setSelectionIconProvider(selection_icon_provider);

    // Set the painter on the layer.
    gxy_layer.setGXYPainterProvider(painter_provider);

    // Initialize a label painter: standard dataobject label painter.
    TLcdGXYDataObjectLabelPainter label_painter = new TLcdGXYDataObjectLabelPainter();
    Color label_default_color = Color.white;
    Color label_selection_color = Color.red;
    label_painter.setForeground(label_default_color);
    label_painter.setSelectionColor(label_selection_color);
    // Draw a pin from the aerodrome to the label.
    label_painter.setWithPin(true);
    // Specify the features to display in the label.
    label_painter.setExpressions("Identification");

    // Set the label painter on the layer.
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    return gxy_layer;
  }

  /**
   * Create an ILcdGXYLayer which shows waypoints with their ICAO symbol and labels them
   * with their identifier.
   *
   * @param aModel A model containing waypoints.
   * @return an ILcdGXYLayer which shows waypoints.
   */
  private ILcdGXYLayer createWayPointLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Waypoints");

    // Initialize a painter with icon providers that use ICAO symbology.
    TLcdICAODefaultSymbolProvider icon_provider = new TLcdICAODefaultSymbolProvider();
    TLcdICAODefaultSymbolProvider selection_icon_provider = new TLcdICAODefaultSymbolProvider();
    icon_provider.setColor(new Color(252, 191, 0));
    selection_icon_provider.setColor(Color.red);
    TLcdGXYAISIconProviderPainter painter_provider = new TLcdGXYAISIconProviderPainter();
    painter_provider.setIconProvider(icon_provider);
    painter_provider.setSelectionIconProvider(selection_icon_provider);

    // Set the painter on the layer.
    gxy_layer.setGXYPainterProvider(painter_provider);

    // Initialize a label painter: standard dataobject label painter.
    TLcdGXYDataObjectLabelPainter label_painter = new TLcdGXYDataObjectLabelPainter();
    // Label color settings.
    Color label_default_color = new Color(252, 191, 0);
    Color label_selection_color = Color.red;
    label_painter.setForeground(label_default_color);
    label_painter.setSelectionColor(label_selection_color);
    // Draw a pin from the aerodrome to the label.
    label_painter.setWithPin(false);
    // Specify the features to display in the label.
    label_painter.setExpressions("Identifier");

    // Set the label painter on the layer.
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    return gxy_layer;
  }

  /**
   * Create an ILcdGXYLayer which draws routes and labels them
   * with their identifier.
   *
   * @param aModel A model containing routes.
   * @return an ILcdGXYLayer which draws routes.
   */
  private ILcdGXYLayer createRouteLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Routes");

    // Lines are drawn in the route painter, so an ILcdGXYPen must be set on the layer.
    gxy_layer.setGXYPen(new TLcdGeodeticPen());

    // Initialize a specific route painter (since routes can consist of several polylines).
    TLcdGXYRoutePainter painter_provider = new TLcdGXYRoutePainter();

    Color route_color = Color.green;

    // Initialize line style.
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setAntiAliasing(false);
    line_style.setLineWidth(1);
    line_style.setSelectionLineWidth(2);
    line_style.setColor(route_color);
    line_style.setSelectionColor(Color.red);

    TLcdGXYIconPainter icon_painter = new TLcdGXYIconPainter();
    icon_painter.setIcon(null);
    icon_painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 3, Color.red));
    painter_provider.setLineStyle(line_style);
    painter_provider.setPointPainter(icon_painter);
    gxy_layer.setGXYPainterProvider(painter_provider);

    // Initialize a label painter: standard dataobject label painter.
    TLcdGXYDataObjectLabelPainter label_painter = new TLcdGXYDataObjectLabelPainter();
    // Label color settings.
    label_painter.setForeground(route_color);
    label_painter.setSelectionColor(Color.red);
    // Specify the features to display in the label.
    label_painter.setExpressions("Identifier");
    // Draw the label with a frame.
    label_painter.setFrame(true);
    // Draw a pin with anchor point from the aerodrome to the label.
    label_painter.setWithAnchorPoint(false);
    label_painter.setWithPin(true);
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    return gxy_layer;
  }

  /**
   * Returns an <code>ILcdGXYLayer</code>
   * that is able to display a given procedure model on the view.
   *
   * @param aModel a procedure model
   * @return an instance of an implementation of the <code>ILcdGXYLayer</code> interface
   *         to display the given procedure model on the view
   */
  private ILcdGXYLayer createProcedureTrajectoryLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Procedures");

    TLcdGXYProcedureTrajectoryPainter procedure_painter = new TLcdGXYProcedureTrajectoryPainter();
    procedure_painter.setConnectorPainterStyle(new TLcdGXYPainterColorStyle(new Color(0, 255, 192), new Color(255, 100, 100)));
    procedure_painter.setDecorationPainterStyle(new TLcdGXYPainterColorStyle(new Color(255, 192, 0), new Color(255, 192, 0)));
    procedure_painter.setErrorPainterStyle(new TLcdGXYPainterColorStyle(Color.red, Color.red));
    procedure_painter.setMissedApproachPainterStyle(new TLcdGXYPainterColorStyle(new Color(0, 64, 255), new Color(255, 100, 100)));
    procedure_painter.setNormalPainterStyle(new TLcdGXYPainterColorStyle(new Color(0, 192, 255), new Color(255, 100, 100)));

    /* icon style
    TLcdICAODefaultSymbolProvider icon_provider = new TLcdICAODefaultSymbolProvider();
    TLcdICAODefaultSymbolProvider selection_icon_provider = new TLcdICAODefaultSymbolProvider();
    icon_provider.setColor( Color.black );
    selection_icon_provider.setColor( Color.red );

    TLcdGXYAISIconProviderPainter painter_provider = new TLcdGXYAISIconProviderPainter();
    painter_provider.setIconProvider(icon_provider);
    painter_provider.setSelectionIconProvider(selection_icon_provider);
    gxy_layer.setGXYPainterProvider( painter_provider );*/

    procedure_painter.setPaintMode(TLcdGXYProcedureTrajectoryPainter.PROCEDURE_ONLY);

    gxy_layer.setGXYPainterProvider(procedure_painter);

    // Add a label painter that paints a label for each leg. This painter is supplied as part
    // of the AIS samples.
    TLcdGXYProcedureTrajectoryLegLabelPainter label_painter = new TLcdGXYProcedureTrajectoryLegLabelPainter();
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    return gxy_layer;
  }

  /**
   * Returns an <code>ILcdGXYLayer</code>
   * that is able to display a given airspace model on the view.
   *
   * @param aModel a airspace model
   * @return an instance of an implementation of the <code>ILcdGXYLayer</code> interface
   *         to display the given airspace model on the view
   */
  private ILcdGXYLayer createAirspaceLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Airspaces");

    // Create and configure an ICAO airspace painter.
    TLcdICAOAirspacePainter airspace_painter = new TLcdICAOAirspacePainter();

    // Set the line settings on the painter.
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setAntiAliasing(true);
    line_style.setLineWidth(1);
    line_style.setSelectionLineWidth(2);

    // Separate settings for the different airspace classifications.
    airspace_painter.setClassExpression(MyAirspaceDataProperties.CLASS.getName());
    airspace_painter.setColor('A', new Color(176, 58, 80));
    airspace_painter.setSelectionColor('A', new Color(255, 0, 0));
    airspace_painter.setBandWidth('A', 400); //in meters
    airspace_painter.setColor('B', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('B', new Color(255, 0, 0));
    airspace_painter.setBandWidth('B', 400);
    airspace_painter.setColor('C', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('C', new Color(255, 0, 0));
    airspace_painter.setBandWidth('C', 400);
    airspace_painter.setColor('D', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('D', new Color(255, 0, 0));
    airspace_painter.setBandWidth('D', 400);
    airspace_painter.setColor('E', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('E', new Color(255, 0, 0));
    airspace_painter.setBandWidth('E', 800);
    airspace_painter.setColor('F', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('F', new Color(255, 0, 0));
    airspace_painter.setBandWidth('F', 0);
    airspace_painter.setColor('G', new Color(88, 115, 165));
    airspace_painter.setSelectionColor('G', new Color(255, 0, 0));
    airspace_painter.setBandWidth('G', 200);

    // Create an ICAO airspace label painter; you can choose between an
    // outlined label painter (TLcdICAOAirspaceLabelPainter)
    // or a detailed one:
    TLcdICAOAirspaceDetailedLabelPainter label_painter = new TLcdICAOAirspaceDetailedLabelPainter();

    // Label painter settings specific for the model.
    label_painter.setClassExpression(MyAirspaceDataProperties.CLASS.getName());
    label_painter.setNameExpression(MyAirspaceDataProperties.NAME.getName());
    label_painter.setTypeExpression(MyAirspaceDataProperties.TYPE.getName());
    label_painter.setLowerLimitValueExpression(MyAirspaceDataProperties.LOWER_LIMIT.getName());
    label_painter.setLowerLimitUnitExpression(MyAirspaceDataProperties.LOWER_LIMIT_UNIT.getName());
    label_painter.setLowerLimitReferenceExpression(MyAirspaceDataProperties.LOWER_LIMIT_REFERENCE.getName());
    label_painter.setUpperLimitValueExpression(MyAirspaceDataProperties.UPPER_LIMIT.getName());
    label_painter.setUpperLimitUnitExpression(MyAirspaceDataProperties.UPPER_LIMIT_UNIT.getName());
    label_painter.setUpperLimitReferenceExpression(MyAirspaceDataProperties.UPPER_LIMIT_REFERENCE.getName());

    // General label painter settings.
    label_painter.setFont(new Font("SansSerif", Font.PLAIN, 10));
    label_painter.setForeground(Color.white);
    label_painter.setBackground(new Color(255, 255, 255, 125));
    label_painter.setSelectionColor(Color.red);
    label_painter.setPinColor(Color.white);
    label_painter.setSelectedPinColor(Color.red);

    // Specific settings for the detailed label painter.
    label_painter.setWithAnchorPoint(true);
    label_painter.setWithPin(true);
    label_painter.setShiftLabelPosition(40);

    // When we would use the outlined label painter, we set the following specific settings.
    //label_painter.setAlignment(TLcdICAOAirspaceLabelPainter.ABOVE);
    //label_painter.setVGap(5);

    // Set the painter and label painter on the layer.
    gxy_layer.setGXYPainterProvider(airspace_painter);
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    // Set a pen on the layer.
    gxy_layer.setGXYPen(new TLcdGeodeticPen());

    return gxy_layer;
  }

  /**
   * Returns an <code>ILcdGXYLayer</code>
   * that is able to display a given runway model on the view.
   *
   * @param aModel a runway model
   * @return an instance of an implementation of the <code>ILcdGXYLayer</code> interface
   *         to display the given runway model on the view
   */
  private ILcdGXYLayer createRunwayLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel, "Runways");

    // Create and configure an ICAO runway painter.
    TLcdICAORunwayPainter runway_painter = new TLcdICAORunwayPainter();

    runway_painter.setSurfaceTypeExpression(MyRunwayDataProperties.SURFACE_TYPE.getName());

    // Initialize line style.
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setAntiAliasing(true);
    line_style.setLineWidth(1);
    line_style.setSelectionLineWidth(1);
    line_style.setColor(Color.white);
    line_style.setSelectionColor(Color.red);
    runway_painter.setLineStyle(line_style);

    // Enable the paint cache.
    runway_painter.setPaintCache(true);

    // Initialize a label painter: standard dataobject label painter.
    TLcdGXYDataObjectLabelPainter label_painter = new TLcdGXYDataObjectLabelPainter();
    label_painter.setFont(new Font("SansSerif", Font.PLAIN, 10));
    label_painter.setForeground(Color.white);
    label_painter.setSelectionColor(Color.red);
    label_painter.setFrame(false);

    label_painter.setWithAnchorPoint(true);
    label_painter.setWithPin(true);
    label_painter.setShiftLabelPosition(10);
    // Specify the features to display in the label.
    label_painter.setExpressions(MyRunwayDataProperties.WIDTH.getName(), MyRunwayDataProperties.SURFACE_TYPE.getName());

    // Set the painter and label painter on the layer.
    gxy_layer.setGXYPainterProvider(runway_painter);
    gxy_layer.setGXYLabelPainterProvider(label_painter);

    return gxy_layer;
  }
}
