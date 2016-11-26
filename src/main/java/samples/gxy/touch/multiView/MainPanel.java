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
package samples.gxy.touch.multiView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNavigateController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.model.GeodeticModelFactory;
import samples.gxy.common.OverlayPanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.common.touch.TouchUtil;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.NewShapeControllerModel;
import samples.gxy.touch.editing.TouchNewController;

/**
 * Main class for the multi view sample. This sample shows how to setup 2 views,
 * that can be used by multiple users at the same time.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel1 = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00));
  private TLcdMapJPanel fMapJPanel2 = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00));
  private ILcdGXYLayer fLonLatShapeGXYLayer1;
  private ILcdGXYLayer fLonLatShapeGXYLayer2;
  private OverlayPanel fOverlayPanel1;
  private OverlayPanel fOverlayPanel2;

  @Override
  protected void createGUI() {
    fOverlayPanel1 = new OverlayPanel(fMapJPanel1);
    fOverlayPanel2 = new OverlayPanel(fMapJPanel2);

    boolean touch_supported = TouchUtil.checkTouchDevice(this);

    // Create a toolbar for each of the 2 views
    TouchToolBar tool_bar1 = new TouchToolBar(fMapJPanel1, true, touch_supported, this, fOverlayPanel1);
    TouchToolBar tool_bar2 = new TouchToolBar(fMapJPanel2, false, touch_supported, this, fOverlayPanel2);

    tool_bar1.addGXYController(createGXYNewController(new TLcdImageIcon("images/shape/i32_geodesic.png"), "Create a geodetic polyline", true));
    tool_bar2.addGXYController(createGXYNewController(new TLcdImageIcon("images/shape/i32_geodesic.png"), "Create a geodetic polyline", false));

    // Create a panel for each of the 2 views
    JPanel west_map_panel = new JPanel(new BorderLayout());
    west_map_panel.add(TitledPanel.createTitledPanel("Map User 1", fOverlayPanel1, TitledPanel.NORTH | TitledPanel.EAST), BorderLayout.CENTER);
    west_map_panel.add(tool_bar1, BorderLayout.NORTH);

    JPanel east_map_panel = new JPanel(new BorderLayout());
    east_map_panel.add(TitledPanel.createTitledPanel("Map User 2", fOverlayPanel2, TitledPanel.NORTH | TitledPanel.EAST), BorderLayout.CENTER);
    east_map_panel.add(tool_bar2, BorderLayout.NORTH);

    // Add the 2 views in a split glass pane
    JPanel map_panel = new MultiViewSplitGlassPane(west_map_panel, east_map_panel);

    // Add the components to the sample.
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, map_panel);

    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  private ILcdGXYController createGXYNewController(ILcdIcon aShapeIcon, String aDescription, final boolean aUser1) {
    ControllerSettingsNotifier notifier = new ControllerSettingsNotifier(aUser1 ? fMapJPanel1 : fMapJPanel2);
    NewShapeControllerModel model = new NewShapeControllerModel(NewShapeControllerModel.ShapeType.POLYLINE, notifier) {
      @Override
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return aUser1 ? fLonLatShapeGXYLayer1 : fLonLatShapeGXYLayer2;
      }
    };
    TLcdGXYTouchNewController new_controller = new TouchNewController(model, aUser1 ? fOverlayPanel1 : fOverlayPanel2, TLcdOverlayLayout.Location.NORTH_WEST);
    new_controller.setShortDescription(aDescription);
    new_controller.setName(aUser1 ? "new1" : "new2");
    new_controller.setNextGXYController(new TLcdGXYTouchNavigateController());
    new_controller.setIcon(aShapeIcon);
    return new_controller;
  }

  @Override
  protected void addData() {
    fLonLatShapeGXYLayer1 = loadData(true);
    fLonLatShapeGXYLayer2 = loadData(false);
  }

  private ILcdGXYLayer loadData(boolean aUser1) {
    ILcdGXYView view = aUser1 ? fMapJPanel1 : fMapJPanel2;

    // Add the states, rivers and cities layers.
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(view);
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(view);
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(view);
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(view);

    ShapeGXYLayerFactory factory = new ShapeGXYLayerFactory();
    GeodeticModelFactory modelFactory = new GeodeticModelFactory();
    ILcdGXYLayer layer = factory.createGXYLayer(modelFactory.createSimpleModel());

    TLcdGXYPainterColorStyle line_style = new TLcdGXYPainterColorStyle(aUser1 ? Color.black : Color.blue, aUser1 ? Color.red : Color.yellow);
    ShapeGXYLayerFactory.retrieveGXYPainterEditor(layer).setLineStyle(line_style);

    layer.getModel().removeAllElements(ILcdFireEventMode.NO_EVENT);
    layer.setLabel(aUser1 ? "User 1 shapes" : "User 2 shapes");
    GXYLayerUtil.addGXYLayer(view, layer, true, false);

    return layer;
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
        new LuciadFrame(new MainPanel(), "Multi User", 1024, 600);
      }
    });
  }
}
