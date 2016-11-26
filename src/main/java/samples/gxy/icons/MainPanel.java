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
package samples.gxy.icons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.IOException;
import java.util.Random;

import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAnchoredIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.common.SampleData;
import samples.gxy.common.AntiAliasedIcon;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;
import samples.gxy.projections.ProjectionComboBox;

/**
 * This sample demonstrates the use of icons and TLcdGXYIconPainter. It demonstrates icons with
 * an anchor point ( ILcdAnchoredIcon ), rotated icons, and icon scaling. 
 */
public class MainPanel extends GXYSample {

  private static final ILcdIcon PIN_ICON = new TLcdAnchoredIcon(new AntiAliasedIcon(new TLcdImageIcon("samples/images/i32_pin_red.png")), new Point(10, 31));
  private static final ILcdIcon PIN_ICON_SELECTED = new TLcdAnchoredIcon(new AntiAliasedIcon(new TLcdImageIcon("samples/images/i32_pin_orange.png")), new Point(10, 31));
  private static final ILcdIcon AIRPLANE_ICON = new AntiAliasedIcon(new TLcdImageIcon("samples/images/airplane.png"));
  private static final ILcdIcon AIRPLANE_ICON_SELECTED = new AntiAliasedIcon(new TLcdImageIcon("samples/images/airplane_orange.png"));

  private TLcdGXYIconPainter fPinIconPainter = new TLcdGXYIconPainter();
  private TLcdGXYIconPainter fAirplaneIconPainter = new TLcdGXYIconPainter();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new IconScalePanel(new TLcdGXYIconPainter[]{fAirplaneIconPainter, fPinIconPainter}, getView()));
    return panel;
  }

  @Override
  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());

    // Create a layer containing cities. Draw them using anchored icons.
    ILcdGXYLayer anchoredIconLayer = GXYDataUtil.instance().model(SampleData.US_CITIES).layer(new GXYUnstyledLayerFactory()).getLayer();
    if (anchoredIconLayer instanceof TLcdGXYLayer) {
      TLcdGXYLayer layer = (TLcdGXYLayer) anchoredIconLayer;
      layer.setLabel("Anchored Icons");
      layer.setSelectable(true);

      // Set up an icon painter that draws pins (anchored icons).
      fPinIconPainter.setIcon(PIN_ICON);
      fPinIconPainter.setSelectionIcon(PIN_ICON_SELECTED);
      layer.setGXYPainterProvider(fPinIconPainter);
      layer.setGXYEditorProvider(fPinIconPainter);

      // Make sure the labels are drawn underneath the icons.
      TLcdGXYDataObjectLabelPainter label_painter = (TLcdGXYDataObjectLabelPainter) layer.getGXYLabelPainterProvider();
      label_painter.setSelectionColor(Color.ORANGE);
      label_painter.setPositionList(new int[]{TLcdGXYLabelPainter.SOUTH});
    }

    // Create a layer containing oriented objects. The icon painter will draw them rotated.
    ILcdGXYLayer rotatedIconLayer = GXYDataUtil.instance().model(SampleData.US_CITIES).layer(new GXYUnstyledLayerFactory()).getLayer();
    if (rotatedIconLayer instanceof TLcdGXYLayer) {
      TLcdGXYLayer layer = (TLcdGXYLayer) rotatedIconLayer;
      layer.setLabel("Rotated Icons");
      layer.setSelectable(true);

      // Removes all current elements from the layer model, and adds new, randomly placed points to it.
      // These points implement the ILcdOriented interface and are given a random orientation. When
      // a TLcdGXYIconPainter paints an oriented point, it dynamically retrieves its rotation and
      // draws it rotated.
      createOrientedElements(layer);

      // Set up an icon painter that draws airplanes. Also enable the use of orientation to draw
      // icons rotated when their domain object implements ILcdOriented.
      fAirplaneIconPainter.setIcon(AIRPLANE_ICON);
      fAirplaneIconPainter.setSelectionIcon(AIRPLANE_ICON_SELECTED);
      fAirplaneIconPainter.setUseOrientation(true);
      layer.setGXYPainterProvider(fAirplaneIconPainter);
      layer.setGXYEditorProvider(fAirplaneIconPainter);

      TLcdGXYDataObjectLabelPainter label_painter = (TLcdGXYDataObjectLabelPainter) layer.getGXYLabelPainterProvider();
      label_painter.setExpressions(RotatedPoint.Orientation.getName());
      label_painter.setSelectionColor(Color.ORANGE);
    }
    GXYLayerUtil.addGXYLayer(getView(), anchoredIconLayer, false, false);
    GXYLayerUtil.addGXYLayer(getView(), rotatedIconLayer, true, false);
  }

  private void createOrientedElements(ILcdGXYLayer aLayer) {
    aLayer.getModel().removeAllElements(ILcdFireEventMode.FIRE_NOW);
    Random random = new Random(54321);
    for (int i = 0; i < 50; i++) {
      double lon = random.nextDouble() * 70.0 - 130.0;
      double lat = random.nextDouble() * 40.0 + 20.0;
      double rot = Math.floor(random.nextDouble() * 360.0);
      aLayer.getModel().addElement(new RotatedPoint(lon, lat, "Rotated point", rot), ILcdFireEventMode.FIRE_NOW);
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Icons");
  }

}
