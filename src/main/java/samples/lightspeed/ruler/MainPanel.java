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
package samples.lightspeed.ruler;

import static java.awt.Color.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerDistanceFormatStyle;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerLabelStyler;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerSegmentLabelContentStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyler;

import samples.common.model.GeodeticModelFactory;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.touch.TouchToolBar;

/**
 * This sample is intended as a step by step guide to set up a <code>TLspRulerController</code> and
 * configure its measurement modes, distance units and different styles.
 */
public class MainPanel extends LightspeedSample {

  //Set this to true to be able to use touch input for the ruler controller.
  private static final boolean TOUCH = false;

  private static final String[] FONT_STYLE_OPTIONS = {"Dialog-bold-12", "Dialog-bold-14", "Serif-bold-14", "Serif-bold-18"};
  private static final String[] COLOR_STYLE_OPTIONS = {"Black - White", "Blue - White", "Red - Yellow", "Red - White"};

  private static final String METERS = "Meters";
  private static final String KILOMETERS = "Kilometers";
  private static final String FEET = "Feet";
  private static final String MILES = "Miles";

  private static final String[] DISTANCE_UNITS_OPTIONS = {METERS, KILOMETERS, FEET, MILES};

  private TLspRulerController fRulerController;

  private TLspCustomizableStyle fLineStyle1;
  private TLspCustomizableStyle fLineStyle2;
  private TLspCustomizableStyle fCircleStyle;

  private TLspCustomizableStyle fSegmentLabelStyle;
  private TLspCustomizableStyle fSegmentTextStyle;
  private TLspCustomizableStyle fSegmentLabelContentStyle;

  private TLspCustomizableStyle fTotalLabelStyle;
  private TLspCustomizableStyle fTotalTextStyle;
  private TLspCustomizableStyle fTotalLabelContentStyle;

  @Override
  protected void createGUI() {
    super.createGUI();

    // Initialize the ruler controller styles
    initRulerControllerStyles();

    // Create a checkbox to enable/disable displaying of azimuths
    final JCheckBox azimuthCheckbox = new JCheckBox("Display Azimuth");
    azimuthCheckbox.setSelected(fRulerController.isDisplayAzimuth());
    azimuthCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRulerController.setDisplayAzimuth(azimuthCheckbox.isSelected());
      }
    });

    // Create a checkbox to enable/disable circle display
    final JCheckBox equalDistanceCirclesCheckbox = new JCheckBox("Display Equal Distance Circles");
    equalDistanceCirclesCheckbox.setSelected(fRulerController.isDisplayEqualDistanceCircles());
    equalDistanceCirclesCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRulerController.setDisplayEqualDistanceCircles(equalDistanceCirclesCheckbox.isSelected());
      }
    });

    // Create a checkbox to enable/disable preservation of the measurement layer
    final JCheckBox keepLayerCheckbox = new JCheckBox("Keep Layer");
    keepLayerCheckbox.setSelected(fRulerController.isKeepLayer());
    keepLayerCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRulerController.setKeepLayer(keepLayerCheckbox.isSelected());
      }
    });

    // Create a checkbox to enable/disable preservation of measurements when starting a new one.
    final JCheckBox keepMeasurementCheckbox = new JCheckBox("Keep Measurement");
    keepMeasurementCheckbox.setSelected(fRulerController.isKeepMeasurements());
    keepMeasurementCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fRulerController.setKeepMeasurements(keepMeasurementCheckbox.isSelected());
      }
    });

    // Add a combobox used to modify the colors of the measurements
    JLabel colorStyleLabel = new JLabel("Color Style");
    JComboBox colorStyleComboBox = createStyleComboBox(
        new StylePropertyModifier() {
          @Override
          public void modifyStyleProperty(String aModifiedStyleProperty) {
            Colors colors = new Colors(aModifiedStyleProperty);
            changeLineStyleColor(colors);
            changeCircleStyleColor(colors);
            changeLabelStyleColor(colors);
          }

          @Override
          public String getStyleProperty() {
            TLspLineStyle primaryLineStyle = (TLspLineStyle) fLineStyle1.getStyle();
            TLspLineStyle secondaryLineStyle = (TLspLineStyle) fLineStyle2.getStyle();
            return new Colors(primaryLineStyle.getColor(), secondaryLineStyle.getColor()).toString();
          }
        }, COLOR_STYLE_OPTIONS
    );

    // Add a combobox to modify the measurement label font
    JLabel fontStyleLabel = new JLabel("Font Style");
    JComboBox fontStyleComboBox = createStyleComboBox(
        new StylePropertyModifier() {

          @Override
          public void modifyStyleProperty(String aModifiedStyleProperty) {
            TLspTextStyle textStyle = (TLspTextStyle) fSegmentTextStyle.getStyle();
            fSegmentTextStyle.setStyle(textStyle.asBuilder().font(aModifiedStyleProperty).build());
            textStyle = (TLspTextStyle) fTotalTextStyle.getStyle();
            fTotalTextStyle.setStyle(textStyle.asBuilder().font(aModifiedStyleProperty).build());
          }

          @Override
          public String getStyleProperty() {
            // this code assumes that segment and total text file always have identical font families
            return FontConversionUtil.toString(((TLspTextStyle) fSegmentTextStyle.getStyle()).getFont());
          }
        }, FONT_STYLE_OPTIONS
    );

    // Add a combobox to specify the distance unit
    JLabel distanceUnitLabel = new JLabel("Distance unit");
    JComboBox distanceUnitComboBox = createStyleComboBox(
        new StylePropertyModifier() {
          @Override
          public void modifyStyleProperty(String aModifiedStyleProperty) {
            TLcdDistanceFormat distanceFormat = getDistanceFormat(aModifiedStyleProperty);

            TLspRulerSegmentLabelContentStyle segmentContentStyle = (TLspRulerSegmentLabelContentStyle) fSegmentLabelContentStyle.getStyle();
            fSegmentLabelContentStyle.setStyle(segmentContentStyle.asBuilder().distanceFormat(distanceFormat).build());

            TLspRulerDistanceFormatStyle totalContentStyle = (TLspRulerDistanceFormatStyle) fTotalLabelContentStyle.getStyle();
            fTotalLabelContentStyle.setStyle(totalContentStyle.asBuilder().distanceFormat(distanceFormat).build());
          }

          @Override
          public String getStyleProperty() {
            // this code assumes that segment and total content style use the same distance format
            TLcdDistanceFormat format = (TLcdDistanceFormat) ((TLspRulerDistanceFormatStyle) fTotalLabelContentStyle.getStyle()).getDistanceFormat();
            return format.getUserUnit().getUnitName();
          }
        }, DISTANCE_UNITS_OPTIONS
    );

    // Add all GUI components to a panel
    JPanel customPanel = new JPanel();

    customPanel.setLayout(new GridLayout(0, 1));
    customPanel.add(colorStyleLabel);
    customPanel.add(colorStyleComboBox);
    customPanel.add(fontStyleLabel);
    customPanel.add(fontStyleComboBox);
    customPanel.add(distanceUnitLabel);
    customPanel.add(distanceUnitComboBox);
    customPanel.add(azimuthCheckbox);
    customPanel.add(equalDistanceCirclesCheckbox);
    customPanel.add(keepMeasurementCheckbox);

    addComponentToRightPanel(TitledPanel.createTitledPanel("Ruler style", customPanel));
  }

  private void initRulerControllerStyles() {
    TLspLineStyle primaryLineStyle = TLspLineStyle.newBuilder()
                                                  .color(Color.blue)
                                                  .width(4.0f)
                                                  .build();

    TLspLineStyle secondaryLineStyle = TLspLineStyle.newBuilder()
                                                    .color(Color.white)
                                                    .width(2.0f)
                                                    .build();

    TLspLineStyle circleLineStyle = TLspLineStyle.newBuilder()
                                                 .elevationMode(ElevationMode.ON_TERRAIN)
                                                 .color(Color.blue)
                                                 .width(1.0f)
                                                 .build();

    TLspFillStyle circleFillStyle = TLspFillStyle.newBuilder()
                                                 .elevationMode(ElevationMode.ON_TERRAIN)
                                                 .color(Color.lightGray)
                                                 .opacity(0.3f)
                                                 .build();

    //We create customizable stylers to enable us to easily modify the styles at runtime.
    TLspCustomizableStyler lineStyler = new TLspCustomizableStyler(primaryLineStyle, secondaryLineStyle);
    TLspCustomizableStyler circleStyler = new TLspCustomizableStyler(circleLineStyle, circleFillStyle);
    TLspRulerLabelStyler labelStyler = new TLspRulerLabelStyler();

    //We store the resulting customizable styles in fields to to be able to easily change them.
    for (TLspCustomizableStyle style : lineStyler.getStyles()) {
      if (style.getStyle() == primaryLineStyle) {
        fLineStyle1 = style;
      } else if (style.getStyle() == secondaryLineStyle) {
        fLineStyle2 = style;
      }
    }
    for (TLspCustomizableStyle style : circleStyler.getStyles()) {
      if (style.getStyle() == circleLineStyle) {
        fCircleStyle = style;
      }
    }
    for (TLspCustomizableStyle style : labelStyler.getStyles()) {
      if (TLspRulerLabelStyler.SEGMENT.equals(style.getIdentifier())) {
        if (style.getStyle() instanceof TLspTextStyle) {
          fSegmentTextStyle = style;
        } else if (style.getStyle() instanceof TLspLabelBoxStyle) {
          fSegmentLabelStyle = style;
        } else if (style.getStyle() instanceof TLspRulerSegmentLabelContentStyle) {
          fSegmentLabelContentStyle = style;
        }
      } else if (TLspRulerLabelStyler.TOTAL.equals(style.getIdentifier())) {
        if (style.getStyle() instanceof TLspTextStyle) {
          fTotalTextStyle = style;
        } else if (style.getStyle() instanceof TLspLabelBoxStyle) {
          fTotalLabelStyle = style;
        } else if (style.getStyle() instanceof TLspRulerDistanceFormatStyle) {
          fTotalLabelContentStyle = style;
        }
      }
    }

    fRulerController.setLineStyler(lineStyler);
    fRulerController.setCircleStyler(circleStyler);
    fRulerController.setLabelStyler(labelStyler);
  }

  private void changeLineStyleColor(Colors aColors) {
    // Change the color of the primary and secondary line style.
    TLspLineStyle oldPrimaryLineStyle = (TLspLineStyle) fLineStyle1.getStyle();
    TLspLineStyle newPrimaryLineStyle = oldPrimaryLineStyle.asBuilder().color(aColors.getPrimaryColor()).build();
    fLineStyle1.setStyle(newPrimaryLineStyle);

    TLspLineStyle oldSecondaryLineStyle = (TLspLineStyle) fLineStyle2.getStyle();
    TLspLineStyle newSecondaryLineStyle = oldSecondaryLineStyle.asBuilder().color(aColors.getSecondaryColor()).build();
    fLineStyle2.setStyle(newSecondaryLineStyle);
  }

  private void changeCircleStyleColor(Colors aColors) {
    // Change the color of the circle to the chosen secondary line style color.
    TLspLineStyle oldCircleLineStyle = (TLspLineStyle) fCircleStyle.getStyle();
    TLspLineStyle newCircleLineStyle = oldCircleLineStyle.asBuilder().color(aColors.getSecondaryColor()).build();
    fCircleStyle.setStyle(newCircleLineStyle);
  }

  private void changeLabelStyleColor(Colors aColors) {
    TLspTextStyle oldTextStyle = (TLspTextStyle) fSegmentTextStyle.getStyle();
    TLspTextStyle newTextStyle = oldTextStyle.asBuilder()
                                             .haloColor(aColors.getPrimaryColor())
                                             .textColor(aColors.getSecondaryColor())
                                             .build();
    fSegmentTextStyle.setStyle(newTextStyle);

    TLspLabelBoxStyle oldLabelBoxStyle = (TLspLabelBoxStyle) fSegmentLabelStyle.getStyle();
    TLspLabelBoxStyle newLabelBoxStyle = oldLabelBoxStyle.asBuilder()
                                                         .haloColor(aColors.getPrimaryColor())
                                                         .frameColor(aColors.getSecondaryColor())
                                                         .build();
    fSegmentLabelStyle.setStyle(newLabelBoxStyle);

    oldTextStyle = (TLspTextStyle) fTotalTextStyle.getStyle();
    newTextStyle = oldTextStyle.asBuilder()
                               .haloColor(aColors.getPrimaryColor())
                               .textColor(aColors.getSecondaryColor())
                               .build();
    fTotalTextStyle.setStyle(newTextStyle);

    oldLabelBoxStyle = (TLspLabelBoxStyle) fTotalLabelStyle.getStyle();
    newLabelBoxStyle = oldLabelBoxStyle.asBuilder()
                                       .haloColor(aColors.getPrimaryColor())
                                       .frameColor(aColors.getSecondaryColor())
                                       .build();
    fTotalLabelStyle.setStyle(newLabelBoxStyle);
  }

  private TLcdDistanceFormat getDistanceFormat(String aModifiedStyleProperty) {
    if (METERS.equals(aModifiedStyleProperty)) {
      return new TLcdDistanceFormat(TLcdDistanceUnit.METRE_UNIT);
    } else if (KILOMETERS.equals(aModifiedStyleProperty)) {
      return new TLcdDistanceFormat(TLcdDistanceUnit.KM_UNIT);
    } else if (FEET.equals(aModifiedStyleProperty)) {
      return new TLcdDistanceFormat(TLcdDistanceUnit.FT_UNIT);
    } else if (MILES.equals(aModifiedStyleProperty)) {
      return new TLcdDistanceFormat(TLcdDistanceUnit.MILE_US_UNIT);
    } else {
      throw new IllegalArgumentException();
    }
  }

  private JComboBox createStyleComboBox(final StylePropertyModifier aStylePropertyModifier, String[] items) {
    final JComboBox comboBox = new JComboBox(items);

    comboBox.setSelectedItem(aStylePropertyModifier.getStyleProperty());
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        aStylePropertyModifier.modifyStyleProperty((String) comboBox.getSelectedItem());
      }
    });

    return comboBox;
  }

  protected void addData() throws IOException {
    super.addData();

    // Add some interesting shapes to measure.
    LspDataUtil.instance().model(GeodeticModelFactory.createModel()).layer().label("Shapes").addToView(getView()).fit();
  }

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = TOUCH ? new TouchToolBar(aView, this, true, true) : new ToolBar(aView, this, true, true);
    fRulerController = toolBar.getRulerController();
    toolBar.addAction(new TLcdUndoAction(toolBar.getUndoManager()), ToolBar.FILE_GROUP);
    toolBar.addAction(new TLcdRedoAction(toolBar.getUndoManager()), ToolBar.FILE_GROUP);
    getView().setController(fRulerController);
    return new JToolBar[]{toolBar};
  }

  /**
   * Encapsulates a strategy for changing a certain style property such as a color or a font.
   */
  private abstract class StylePropertyModifier {

    /**
     * Modify some aspect of the provided style property.
     *
     * @param aModifiedStyleProperty the name of the style property.
     */
    public abstract void modifyStyleProperty(String aModifiedStyleProperty);

    /**
     * Returns the name of the style property of this style property modifier.
     *
     * @return the name of the style property.
     */
    public abstract String getStyleProperty();
  }

  private class Colors {
    private Color primaryColor;
    private Color secondaryColor;

    public Colors(String aColors) {
      primaryColor = toColor(aColors.substring(0, aColors.indexOf("-")));
      secondaryColor = toColor(aColors.substring(aColors.indexOf("-") + 1));
    }

    public Colors(Color aPrimaryColor, Color aSecondaryColor) {
      primaryColor = aPrimaryColor;
      secondaryColor = aSecondaryColor;
    }

    private Color toColor(String aColor) {
      try {
        Field field = Class.forName("java.awt.Color").getField(aColor.toLowerCase().trim());

        return (Color) field.get(null);
      } catch (Exception exc) {
        throw new IllegalArgumentException(aColor + " is not a valid Color");
      }
    }

    public Color getPrimaryColor() {
      return primaryColor;
    }

    public Color getSecondaryColor() {
      return secondaryColor;
    }

    @Override
    public String toString() {
      return toString(primaryColor) + " - " + toString(secondaryColor);
    }

    private String toString(Color aColor) {
      String color;

      if (aColor.getRGB() == BLACK.getRGB()) {
        color = "Black";
      } else if (aColor.getRGB() == BLUE.getRGB()) {
        color = "Blue";
      } else if (aColor.getRGB() == YELLOW.getRGB()) {
        color = "Yellow";
      } else if (aColor.getRGB() == RED.getRGB()) {
        color = "Red";
      } else if (aColor.getRGB() == WHITE.getRGB()) {
        color = "White";
      } else {
        throw new IllegalArgumentException(aColor + " is not a color supported by this sample");
      }

      return color;
    }
  }

  private static class FontConversionUtil {
    /**
     * Converts a Font to a readable String.
     *
     * @param aFont the font that needs to be converted to a readable String.
     *
     * @return the String representing the font.
     */
    private static String toString(Font aFont) {
      return aFont.getFontName().replace('.', '-') + "-" + aFont.getSize();
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Measurement controller");
  }

}
