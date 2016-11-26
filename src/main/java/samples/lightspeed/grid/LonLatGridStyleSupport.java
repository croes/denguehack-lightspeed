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
package samples.lightspeed.grid;

import static java.awt.Color.black;
import static java.awt.Color.white;

import static com.luciad.view.lightspeed.painter.grid.ILspLonLatGridLine.Category.MERIDIAN;
import static com.luciad.view.lightspeed.painter.grid.ILspLonLatGridLine.Category.PARALLEL;
import static com.luciad.view.lightspeed.painter.grid.TLspLonLatGridStyler.Orientation;

import static samples.lightspeed.grid.GridStyleCustomizerPanelUtil.createStyleCombobox;
import static samples.lightspeed.grid.GridStyleCustomizerPanelUtil.transparent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridStyler;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridStyler.LabelPosition;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridStyler.Position;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.gxy.common.TitledPanel;

/**
 * Support class to style a lon-lat grid. It can create an {@code ILspStyler} and a UI to customize
 * the styles returned by that {@code ILspStyler}
 */
final class LonLatGridStyleSupport implements GridStyleSupport {

  private enum CoordinateFormat {
    DEFAULT,
    DECIMAL,
    MINUTES
  }

  private final GridPanel fGridPanel;
  private final TLspLonLatGridStyler fStyler;
  private LonLatGridLayerFactory.Spacing fSpacing = LonLatGridLayerFactory.Spacing.DEFAULT;

  public LonLatGridStyleSupport(GridPanel aGridPanel) {
    fGridPanel = aGridPanel;
    fStyler = createStyler();
  }

  /**
   * Creates a layer with the current style settings.
   */
  @Override
  public void createLayer() {
    ILcdModel lonLatGridModel = LonLatGridLayerFactory.createLonLatGridModel();
    Collection<ILspLayer> layers = new LonLatGridLayerFactory(fSpacing, fStyler).createLayers(lonLatGridModel);
    fGridPanel.setGridLayer(GridType.LON_LAT, layers.iterator().next());
  }

  private static TLspLonLatGridStyler createStyler() {
    TLspLineStyle lineStyle = TLspLineStyle.newBuilder().color(transparent(white)).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    TLspTextStyle textStyle = TLspTextStyle.newBuilder().textColor(transparent(black)).haloColor(white).font("Default-BOLD-12").build();

    TLspLonLatGridStyler styler = new TLspLonLatGridStyler();
    styler.setDefaultStyles(lineStyle, textStyle);
    styler.setStyles(PARALLEL, lineStyle, textStyle);
    styler.setStyles(MERIDIAN, lineStyle, textStyle);
    styler.setStyles(PARALLEL, 0.0, lineStyle, textStyle);
    styler.setStyles(MERIDIAN, 0.0, lineStyle, textStyle);
    styler.setPosition(Position.ABOVE, 3);
    return styler;
  }

  /**
   * Creates a panel which provides a UI to customize the styles used by the styler
   *
   * @return a panel which provides a UI to customize the styles used by the styler
   */
  @Override
  public JPanel createStylePanel(ILspLayer aLayer) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.add(new JLabel("Parallels"));
    panel.add(createStyleCombobox(retrieveStyleFromStyler(fStyler, PARALLEL.toString())));
    panel.add(new JLabel("Equator"));
    panel.add(createStyleCombobox(retrieveStyleFromStyler(fStyler, PARALLEL.toString() + "_0.0")));
    panel.add(new JLabel("Meridians"));
    panel.add(createStyleCombobox(retrieveStyleFromStyler(fStyler, MERIDIAN.toString())));
    panel.add(new JLabel("Prime Meridian"));
    panel.add(createStyleCombobox(retrieveStyleFromStyler(fStyler, MERIDIAN.toString() + "_0.0")));
    panel.add(new JLabel("Coordinate format"));
    panel.add(createCoordinateFormatCombobox(fStyler));
    panel.add(new JLabel("Positioning"));
    panel.add(createPositioningCombobox(fStyler));
    panel.add(new JLabel("Spacing"));
    panel.add(createSpacingCombobox());
    panel.add(new JLabel("Orientation"));
    panel.add(createOrientationCombobox(fStyler));
    panel.add(new JLabel("Label Position"));
    panel.add(createViewPositionCombobox(fStyler));
    JPanel outerPanel = new JPanel(new BorderLayout());
    outerPanel.add(panel, BorderLayout.NORTH);//wrap the panel for better vertical resizing behavior
    return TitledPanel.createTitledPanel("Grid style", outerPanel);
  }

  private static Component createOrientationCombobox(final TLspLonLatGridStyler aStyler) {
    final JComboBox<Orientation> combobox = new JComboBox<>(Orientation.values());
    combobox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Orientation orientation = (Orientation) combobox.getSelectedItem();
        aStyler.setOrientation(orientation);
      }
    });
    return combobox;
  }

  private static Component createViewPositionCombobox(final TLspLonLatGridStyler aStyler) {
    final JComboBox<LabelPosition> combobox = new JComboBox<>(LabelPosition.values());
    combobox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        LabelPosition labelPosition = (LabelPosition) combobox.getSelectedItem();
        aStyler.setLabelPosition(labelPosition);
      }
    });
    return combobox;
  }

  private static TLspCustomizableStyle retrieveStyleFromStyler(TLspLonLatGridStyler aStyler, String aIdentifier) {
    Collection<TLspCustomizableStyle> styles = aStyler.getStyles();
    for (TLspCustomizableStyle style : styles) {
      if (style.getIdentifier().equals(aIdentifier)) {
        return style;
      }
    }
    throw new IllegalArgumentException(aIdentifier + " is an unknown identifier");
  }

  private static Component createCoordinateFormatCombobox(final TLspLonLatGridStyler aStyler) {
    final JComboBox<CoordinateFormat> comboBox = new JComboBox<>(CoordinateFormat.values());
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setCoordinateFormat(aStyler, (CoordinateFormat) comboBox.getSelectedItem());
      }
    });
    comboBox.setSelectedItem(CoordinateFormat.DEFAULT);

    return comboBox;
  }

  private static void setCoordinateFormat(TLspLonLatGridStyler aStyler, CoordinateFormat aFormat) {
    if (aFormat == CoordinateFormat.DEFAULT) {
      aStyler.setCoordinateFormatter(0, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_DEG_0));
      aStyler.setCoordinateFormatter(1, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_MIN_0));
      aStyler.setCoordinateFormatter(2, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_MIN_0));
    } else if (aFormat == CoordinateFormat.DECIMAL) {
      aStyler.setCoordinateFormatter(0, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_DEG_3));
      aStyler.setCoordinateFormatter(1, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_DEG_3));
      aStyler.setCoordinateFormatter(2, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_DEG_3));
    } else if (aFormat == CoordinateFormat.MINUTES) {
      aStyler.setCoordinateFormatter(0, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_MIN_2));
      aStyler.setCoordinateFormatter(1, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_MIN_2));
      aStyler.setCoordinateFormatter(2, new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_MIN_2));
    } else {
      throw new IllegalArgumentException("Unknown format: " + aFormat);
    }
  }

  private static Component createPositioningCombobox(final TLspLonLatGridStyler aStyler) {
    final JComboBox<Position> comboBox = new JComboBox<>(Position.values());
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aStyler.setPosition((Position) comboBox.getSelectedItem(), 4);
      }
    });

    return comboBox;
  }

  private Component createSpacingCombobox() {
    final JComboBox<String> comboBox = new JComboBox<>(new String[]{LonLatGridLayerFactory.Spacing.DEFAULT.toString(),
                                                                    LonLatGridLayerFactory.Spacing.FINE.toString(),
                                                                    LonLatGridLayerFactory.Spacing.COARSE.toString()});
    comboBox.setSelectedItem(fSpacing.toString());
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fSpacing = LonLatGridLayerFactory.Spacing.DEFAULT;
        if ("Fine".equals(comboBox.getSelectedItem())) {
          fSpacing = LonLatGridLayerFactory.Spacing.FINE;
        }
        if ("Coarse".equals(comboBox.getSelectedItem())) {
          fSpacing = LonLatGridLayerFactory.Spacing.COARSE;
        }
        createLayer();
      }
    });

    return comboBox;
  }
}
