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
package samples.gxy.editing;

import static samples.gxy.editing.controllers.NewShapeControllerModel.ShapeType;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.action.ShowPopupAction;
import samples.common.model.GeodeticModelFactory;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.editing.actions.CreateBufferAction;
import samples.gxy.editing.actions.CurveTypeListener;
import samples.gxy.editing.actions.FilledListener;
import samples.gxy.editing.actions.GeodeticShapeListener;
import samples.gxy.editing.actions.InsertCurveAction;
import samples.gxy.editing.actions.PreviewEditListener;
import samples.gxy.editing.actions.RemoveCurveAction;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.NewCompositeCurveControllerModel;
import samples.gxy.editing.controllers.NewShapeControllerModel;

/**
 * This sample demonstrates how to create new ILcdShape objects and edit
 * existing ILcdShape objects in an ILcdGXYView.
 * <p/>
 * Each button added in the toolbar sets a different instance of
 * TLcdGXYNewController2 active. Each TLcdGXYNewController2 has
 * a shape-specific new controller model (see NewShapeControllerModel)
 * that tells which shape instance to create, in which ILcdGXYLayer to add it and how.
 * <p/>
 * A special case is the composite curve, for which we need a separate controller
 * model (NewCompositeCurveControllerModel).
 * <p/>
 */
public class MainPanel extends GXYSample {

  private ControllerSettingsNotifier fControllerSettingsNotifier = new ControllerSettingsNotifier();

  @Override
  protected Component[] createToolBars() {
    Component[] toolBars = super.createToolBars();
    if (toolBars[0] instanceof ToolBar) {
      ToolBar toolBar = (ToolBar) toolBars[0];
      addNewShapeControllers(toolBar);
      addRightClickMenu(toolBar);
    }
    return toolBars;
  }

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = super.createMap();
    fControllerSettingsNotifier.setView(map);
    return map;
  }

  @Override
  protected JPanel createSettingsPanel() {
    JPanel renderingOptionsPanel = createRenderingOptionsPanel();
    JPanel lineModePanel = createShapeTypePanel();
    JPanel subCurveTypePanel = createCompositeCurveTypePanel(fControllerSettingsNotifier);

    JPanel settingsPanel = new JPanel(new GridLayout(0, 1));
    settingsPanel.add(renderingOptionsPanel);
    settingsPanel.add(lineModePanel);
    settingsPanel.add(subCurveTypePanel);

    return settingsPanel;
  }

  public static JPanel createCompositeCurveTypePanel(ControllerSettingsNotifier aControllerSettingsNotifier) {
    // Sets up a combo box with some curve types for the composite curves

    String[] strings = {"Polyline", "Arc-by-3-points", "Arc-by-bulge"};
    ShapeType[] types = {
        ShapeType.POLYLINE,
        ShapeType.ARCBY3POINTS,
        ShapeType.ARCBYBULGE,
    };
    JComboBox curveTypeBox = new JComboBox(strings);
    curveTypeBox.setToolTipText("Determines the shape type when creating composite curves.");
    curveTypeBox.addActionListener(new CurveTypeListener(types, aControllerSettingsNotifier));
    curveTypeBox.setSelectedIndex(0);

    JPanel subCurveTypePanel = new JPanel(new BorderLayout());
    subCurveTypePanel.add(curveTypeBox, BorderLayout.NORTH);
    return TitledPanel.createTitledPanel("Composite curve type", subCurveTypePanel);
  }

  private JPanel createShapeTypePanel() {
    JRadioButton geodeticRadio = new JRadioButton("Geodetic");
    JRadioButton gridRadio = new JRadioButton("Grid");
    gridRadio.setToolTipText("When selected, shapes will be created in a grid model reference.");
    ButtonGroup group = new ButtonGroup();
    group.add(geodeticRadio);
    group.add(gridRadio);
    group.setSelected(geodeticRadio.getModel(), true);
    geodeticRadio.setToolTipText("When selected, shapes will be created in a geodetic model reference.");
    geodeticRadio.addItemListener(new GeodeticShapeListener(fControllerSettingsNotifier));
    JPanel shapeTypePanel = new JPanel(new GridLayout(0, 1));
    shapeTypePanel.add(geodeticRadio);
    shapeTypePanel.add(gridRadio);
    return TitledPanel.createTitledPanel("Shape type", shapeTypePanel);
  }

  private JPanel createRenderingOptionsPanel() {
    // controls instant editing
    JCheckBox instantEditing = new JCheckBox("Preview editing");
    instantEditing.setToolTipText("When checked, a fast preview is shown until you release the mouse button. When unchecked, edit operations are immediately applied.");
    instantEditing.addItemListener(new PreviewEditListener(getToolBars()[0].getGXYControllerEdit()));
    instantEditing.setSelected(true);

    // paints the area objects filled and/or outlined
    String[] fillStrings = {"Filled", "Outlined", "Outlined-filled"};
    int[] fillTypes = {
        ALcdGXYAreaPainter.FILLED,
        ALcdGXYAreaPainter.OUTLINED,
        ALcdGXYAreaPainter.OUTLINED_FILLED
    };
    JComboBox fillType = new JComboBox(fillStrings);
    fillType.setToolTipText("Determines the outline and fill of the shape.");
    fillType.addActionListener(new FilledListener(getView(), fillTypes));
    fillType.setSelectedIndex(2);

    JPanel renderingOptionsPanel = new JPanel(new GridLayout(0, 1));
    renderingOptionsPanel.add(instantEditing);
    renderingOptionsPanel.add(fillType);
    return TitledPanel.createTitledPanel("Rendering", renderingOptionsPanel);
  }

  private void addRightClickMenu(ToolBar aToolBar) {
    ILcdAction[] rightClickActions = {
        new TLcdDeleteSelectionAction(getView()),
        new CreateBufferAction(getView()),
        new InsertCurveAction(aToolBar.getGXYCompositeEditController(),
                              fControllerSettingsNotifier, aToolBar.getSnappables(), getView()),
        new RemoveCurveAction(getView())
    };
    ShowPopupAction showPopupAction
        = new ShowPopupAction(rightClickActions, getView());
    aToolBar.getGXYControllerEdit().setRightClickAction(showPopupAction);
  }

  private void addNewShapeControllers(ToolBar aToolBar) {
    addNewShapeController(ShapeType.TEXT, TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON), "Create a text object", aToolBar);
    addNewShapeController(ShapeType.POINT, TLcdIconFactory.create(TLcdIconFactory.DRAW_POINT_ICON), "Create a point", aToolBar);
    addNewShapeController(ShapeType.BOUNDS, TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON), "Create a bounds object", aToolBar);
    aToolBar.addSpace();
    addNewShapeController(ShapeType.POLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), "Create a polyline", aToolBar);
    addNewShapeController(ShapeType.RHUMBPOLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_RHUMBLINE_POLYLINE_ICON), "Create a rhumb polyline", aToolBar);
    addNewShapeController(ShapeType.POLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), "Create a polygon", aToolBar);
    addNewShapeController(ShapeType.RHUMBPOLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_RHUMBLINE_POLYGON_ICON), "Create a rhumb polygon", aToolBar);
    aToolBar.addSpace();
    addNewShapeController(ShapeType.CIRCLE, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON), "Create a circle", aToolBar);
    addNewShapeController(ShapeType.CIRCLE_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_3_POINTS_ICON), "Create a circle-by-3-points", aToolBar);
    addNewShapeController(ShapeType.ELLIPSE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON), "Create an ellipse", aToolBar);
    aToolBar.addSpace();
    addNewShapeController(ShapeType.ARC, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON), "Create an elliptical arc", aToolBar);
    addNewShapeController(ShapeType.ARCBY3POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON), "Create an arc-by-3-points", aToolBar);
    addNewShapeController(ShapeType.ARCBYBULGE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON), "Create an arc-by-bulge", aToolBar);
    addNewShapeController(ShapeType.ARCBYCENTERPOINT, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_CENTERPOINT_ICON), "Create an arc-by-center-point", aToolBar);
    addNewShapeController(ShapeType.ARCBAND, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON), "Create an arcband", aToolBar);
    aToolBar.addSpace();
    addNewShapeController(ShapeType.GEO_BUFFER_POLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON), "Create a polyline geo buffer", aToolBar);
    addNewShapeController(ShapeType.GEO_BUFFER_POLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_BUFFER_ICON), "Create a polygon geo buffer", aToolBar);
    addNewShapeController(ShapeType.VARIABLE_GEO_BUFFER, TLcdIconFactory.create(TLcdIconFactory.DRAW_VARIABLE_BUFFER_ICON), "Create a buffer with varying width", aToolBar);
    aToolBar.addSpace();
    addNewShapeController(ShapeType.COMPOSITE_CURVE, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_NON_CLOSED_ICON), "Create a composite curve", aToolBar);
    addNewShapeController(ShapeType.COMPOSITE_RING, TLcdIconFactory.create(TLcdIconFactory.COMPOSITE_SHAPE_CLOSED_ICON), "Create a composite ring", aToolBar);
  }

  protected void addNewShapeController(ShapeType aShapeType, ILcdIcon aIcon, String aTooltip, ToolBar aToolBarSFCT) {
    NewShapeControllerModel model;
    if (aShapeType == ShapeType.COMPOSITE_CURVE || aShapeType == ShapeType.COMPOSITE_RING) {
      model = new NewCompositeCurveControllerModel(aShapeType, fControllerSettingsNotifier);
    } else {
      model = new NewShapeControllerModel(aShapeType, fControllerSettingsNotifier);
    }
    ControllerUtil.addNewShapeAction(model, aIcon, aTooltip, aToolBarSFCT);
  }

  protected void addData() throws IOException {
    super.addData();

    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();

    ILcdGXYLayer lonLatLayer = factory.createGXYLayer(new GeodeticModelFactory().createSimpleModel());
    ILcdGXYLayer xyLayer = factory.createGXYLayer(new GridModelFactory().createModel());

    getToolBars()[0].getSnappables().getSnappableLayers().add(lonLatLayer);
    getToolBars()[0].getSnappables().getSnappableLayers().add(xyLayer);

    // Adds the layers to the map, just beneath the grid layer.
    GXYLayerUtil.addGXYLayer(getView(), lonLatLayer, true, false);
    GXYLayerUtil.addGXYLayer(getView(), xyLayer, true, false);

    GXYLayerUtil.fitGXYLayers(getView(), new ILcdGXYLayer[]{lonLatLayer, xyLayer});
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Editing shapes");
  }

}
