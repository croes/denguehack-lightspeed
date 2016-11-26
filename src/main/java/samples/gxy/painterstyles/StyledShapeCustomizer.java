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
package samples.gxy.painterstyles;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYHatchedFillStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdStrokeLineStyle;
import samples.gxy.common.TitledPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Customizer panel that allows to individually style StyledShape objects.
 */
class StyledShapeCustomizer extends JPanel implements Customizer {

  // Pattern choice list;
  private static final PatternLibrary.Pattern[] fPatternChoiceList =
      new PatternLibrary.Pattern[]{PatternLibrary.sCIRCLE_PATTERN, PatternLibrary.sSQUARE_PATTERN,
                                   PatternLibrary.sRECTANGLE_1_PATTERN, PatternLibrary.sRECTANGLE_2_PATTERN,
                                   PatternLibrary.sT_PATTERN, PatternLibrary.sCHEVRON_PATTERN,
                                   PatternLibrary.sTEXT_PATTERN_1, PatternLibrary.sTEXT_PATTERN_2};

  // Pattern 1 repetition & gap configuration: default, minimum, maximum.
  private static final int[] PATTERN_1_REPETITION = {8, 1, 10};
  private static final int[] PATTERN_1_GAP_WIDTH = {3, 0, 10};

  // Pattern 2 repetition & gap configuration: default, minimum, maximum.
  private static final int[] PATTERN_2_REPETITION = {1, 1, 5};
  private static final int[] PATTERN_2_GAP_WIDTH = {3, 0, 10};

  // Tolerance configuration: default, minimum, maximum.
  private static final int[] TOLERANCE = {5, 1, 15};

  // Halo thickness configuration: default, minimum, maximum.
  private static final int[] HALO_THICKNESS = {1, 0, 3};

  // Stroke/Stroke selection/Halo initial colors.
  private static final Color STROKE_COLOR = Color.white;
  private static final Color STROKE_SELECTION_COLOR = Color.red;
  private static final Color HALO_COLOR = Color.black;

  // GUI components.
  private JTextField fRoundnessTextField;
  private JSlider fRoundnessSlider;
  private JSpinner fPattern1RepetitionSpinner, fPattern1WidthSpinner,
      fPattern2RepetitionSpinner, fPattern2WidthSpinner,
      fToleranceSpinner, fHaloSpinner;
  private JRadioButton fSimpleFillButton, fHatchedFillButton;
  private JRadioButton fSimpleStrokeButton, fComplexStrokeButton;
  private JComboBox fPattern1ComboBox;
  private JComboBox fPattern2ComboBox;
  private JCheckBox fAllowSplitCheckBox;
  private ColorPickPanel fStrokeColorPickPanel, fStrokeSelectionColorPickPanel, fHaloColorPickPanel;

  // The active object to be customized.
  private TLcdDomainObjectContext fDomainObjectContext;
  private boolean fIsUpdatingCustomizerFromObject = false;

  /**
   * Creates a new StyledShapeCustomizer object.
   */
  public StyledShapeCustomizer() {
    buildGUI();
  }

  public void setObject(Object aObject) {
    if (aObject instanceof TLcdDomainObjectContext &&
        ((TLcdDomainObjectContext) aObject).getDomainObject() instanceof StyledShape) {
      fDomainObjectContext = (TLcdDomainObjectContext) aObject;

      setStyleConfigurationEnabled(true);
      updateCustomizerFromDomainObject();
    } else {
      setStyleConfigurationEnabled(false);
    }
  }

  /**
   * Initializes the given StyledShape object with the current line style settings of this
   * customizer.
   *
   * @param aStyledShape the StyledShape object to be configured.
   */
  public void initializeObject(StyledShape aStyledShape) {
    // Update painter style.
    ShapeStyle shapeStyle = aStyledShape.getShapeStyle();
    shapeStyle.setLineStyle(fSimpleStrokeButton.isSelected() ? createSimpleLineStyle() : createComplexLineStyle());
    if (shapeStyle.getFillStyle() != null) {
      shapeStyle.setFillStyle(fSimpleFillButton.isSelected() ? createSimpleFillStyle() : createHatchedFillStyle());
    }
    shapeStyle.setRoundness(fRoundnessSlider.getValue() / 100d);
    shapeStyle.setHaloThickness((Integer) fHaloSpinner.getValue());
    shapeStyle.setHaloColor(fHaloColorPickPanel.getColor());
  }

  /**
   * Updates this customizer with the line style settings of the StyledShape object that is
   * currently set.
   */
  private void updateCustomizerFromDomainObject() {
    fIsUpdatingCustomizerFromObject = true;

    StyledShape shape = getStyledShape();

    setStyleConfigurationEnabled(shape != null);

    if (shape != null) {

      // Roundness configuration.

      fRoundnessSlider.setValue((int) (shape.getShapeStyle().getRoundness() * 100d));

      // Fill configuration.

      ILcdGXYPainterStyle fillStyle = shape.getShapeStyle().getFillStyle();
      fHatchedFillButton.setEnabled(fillStyle != null);
      fSimpleFillButton.setEnabled(fillStyle != null);
      if (fillStyle instanceof TLcdGXYHatchedFillStyle) {
        fHatchedFillButton.setSelected(true);
      } else {
        fSimpleFillButton.setSelected(true);
      }

      // Stroke configuration.

      ILcdGXYPainterStyle lineStyle = shape.getShapeStyle().getLineStyle();
      boolean isComplexStroke = lineStyle instanceof ComplexStrokePainterStyle;
      setComplexStrokeConfigurationEnabled(isComplexStroke);

      if (isComplexStroke) {
        ComplexStrokePainterStyle complexStrokeStyle = (ComplexStrokePainterStyle) lineStyle;

        fStrokeColorPickPanel.setColor(complexStrokeStyle.getDefaultColor());
        fStrokeSelectionColorPickPanel.setColor(complexStrokeStyle.getSelectionColor());
        fComplexStrokeButton.setSelected(true);
        fPattern1ComboBox.setSelectedItem(complexStrokeStyle.getPatterns()[0]);
        fPattern1RepetitionSpinner.setValue(complexStrokeStyle.getPatternRepetitions()[0]);
        fPattern1WidthSpinner.setValue(complexStrokeStyle.getGapWidths()[0]);
        fPattern2ComboBox.setSelectedItem(complexStrokeStyle.getPatterns()[1]);
        fPattern2RepetitionSpinner.setValue(complexStrokeStyle.getPatternRepetitions()[1]);
        fPattern2WidthSpinner.setValue(complexStrokeStyle.getGapWidths()[1]);
        fToleranceSpinner.setValue((int) complexStrokeStyle.getTolerance());
        fAllowSplitCheckBox.setSelected(complexStrokeStyle.isAllowSplit());
      } else if (lineStyle instanceof TLcdStrokeLineStyle) {
        TLcdStrokeLineStyle simpleStrokeStyle = (TLcdStrokeLineStyle) lineStyle;
        fStrokeColorPickPanel.setColor(simpleStrokeStyle.getColor());
        fStrokeSelectionColorPickPanel.setColor(simpleStrokeStyle.getSelectionColor());
        fSimpleStrokeButton.setSelected(true);
      }

      // Halo configuration.

      fHaloSpinner.setValue(shape.getShapeStyle().getHaloThickness());
      fHaloColorPickPanel.setColor(shape.getShapeStyle().getHaloColor());
    }

    fIsUpdatingCustomizerFromObject = false;
  }

  private StyledShape getStyledShape() {
    if (fDomainObjectContext != null) {
      return (StyledShape) fDomainObjectContext.getDomainObject();
    } else {
      return null;
    }
  }

  private void setStyleConfigurationEnabled(boolean aEnabled) {
    fRoundnessTextField.setEnabled(aEnabled);
    fRoundnessSlider.setEnabled(aEnabled);
    fSimpleStrokeButton.setEnabled(aEnabled);
    fComplexStrokeButton.setEnabled(aEnabled);
    fSimpleFillButton.setEnabled(aEnabled);
    fHatchedFillButton.setEnabled(aEnabled);
    fStrokeColorPickPanel.setEnabled(aEnabled);
    fStrokeSelectionColorPickPanel.setEnabled(aEnabled);
    fHaloSpinner.setEnabled(aEnabled);
    fHaloColorPickPanel.setEnabled(aEnabled);
    setComplexStrokeConfigurationEnabled(aEnabled);
  }

  private void setComplexStrokeConfigurationEnabled(boolean aEnabled) {
    fPattern1ComboBox.setEnabled(aEnabled);
    fPattern1RepetitionSpinner.setEnabled(aEnabled);
    fPattern1WidthSpinner.setEnabled(aEnabled);
    fPattern2ComboBox.setEnabled(aEnabled);
    fPattern2RepetitionSpinner.setEnabled(aEnabled);
    fPattern2WidthSpinner.setEnabled(aEnabled);
    fToleranceSpinner.setEnabled(aEnabled);
    fAllowSplitCheckBox.setEnabled(aEnabled);
  }

  private void elementChanged(Object aObject) {
    if (fDomainObjectContext != null) {
      ILcdModel model = fDomainObjectContext.getModel();
      model.elementChanged(aObject, ILcdFireEventMode.FIRE_NOW);
    }
  }

  private void buildGUI() {
    // Create listeners.
    RoundnessChangeListener roundness_listener = new RoundnessChangeListener();
    StrokeColorListener stroke_color_listener = new StrokeColorListener();
    StrokeSelectionColorListener stroke_selection_color_listener = new StrokeSelectionColorListener();
    HaloColorListener halo_color_listener = new HaloColorListener();
    StrokeChoiceListener stroke_choice_listener = new StrokeChoiceListener();
    FillChoiceListener fill_choice_listener = new FillChoiceListener();
    ComplexStrokeConfigurationListener pattern_configuration_listener = new ComplexStrokeConfigurationListener();
    HaloThicknessListener halo_listener = new HaloThicknessListener();

    // Panel to control the spline roundness.
    fRoundnessTextField = new JTextField(3);
    fRoundnessSlider = createSlider(0, 100, 50, 10, 20);
    fRoundnessSlider.setToolTipText("Configure roundness");
    SliderTextFieldMediator roundness_mediator = new SliderTextFieldMediator(fRoundnessTextField, fRoundnessSlider);
    fRoundnessSlider.addChangeListener(roundness_mediator);
    fRoundnessTextField.addFocusListener(roundness_mediator);
    fRoundnessTextField.addActionListener(roundness_mediator);
    fRoundnessSlider.addChangeListener(roundness_listener);

    JPanel roundness_panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
    roundness_panel.add(fRoundnessSlider, gbc);
    gbc.weightx = 0;
    gbc.gridx = 1;
    roundness_panel.add(fRoundnessTextField, gbc);

    // Panel to choose between a simple or hatched fill style.
    JPanel fill_choice_panel = new JPanel(new GridLayout(1, 2));
    fSimpleFillButton = new JRadioButton("Simple fill", true);
    fHatchedFillButton = new JRadioButton("Hatched fill");
    fSimpleFillButton.addActionListener(fill_choice_listener);
    fHatchedFillButton.addActionListener(fill_choice_listener);
    ButtonGroup button_group = new ButtonGroup();
    button_group.add(fSimpleFillButton);
    button_group.add(fHatchedFillButton);
    fill_choice_panel.add(fSimpleFillButton);
    fill_choice_panel.add(fHatchedFillButton);

    // General stroke panel.

    // 1. Panel to control stroke color.
    JPanel stroke_color_panel = new JPanel(new GridLayout(1, 2));
    JLabel stroke_color_label = new JLabel("Stroke color");
    fStrokeColorPickPanel = new ColorPickPanel();
    fStrokeColorPickPanel.setToolTipText("Configure stroke color");
    fStrokeColorPickPanel.setColor(STROKE_COLOR);
    fStrokeColorPickPanel.addPropertyChangeListener(ColorPickPanel.COLOR_PROPERTY, stroke_color_listener);
    stroke_color_panel.add(stroke_color_label);
    stroke_color_panel.add(fStrokeColorPickPanel);
    stroke_color_panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // 2. Panel to control stroke color.
    JPanel stroke_selection_color_panel = new JPanel(new GridLayout(1, 2));
    JLabel stroke_selection_color_label = new JLabel("Stroke selection color");
    fStrokeSelectionColorPickPanel = new ColorPickPanel();
    fStrokeSelectionColorPickPanel.setToolTipText("Configure stroke selection color");
    fStrokeSelectionColorPickPanel.setColor(STROKE_SELECTION_COLOR);
    fStrokeSelectionColorPickPanel.addPropertyChangeListener(ColorPickPanel.COLOR_PROPERTY, stroke_selection_color_listener);
    stroke_selection_color_panel.add(stroke_selection_color_label);
    stroke_selection_color_panel.add(fStrokeSelectionColorPickPanel);
    stroke_selection_color_panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // 3. Panel to choose between a simple or complex stroke.
    JPanel stroke_choice_panel = new JPanel(new GridLayout(1, 2));
    fSimpleStrokeButton = new JRadioButton("Simple stroke", true);
    fComplexStrokeButton = new JRadioButton("Complex stroke");
    fSimpleStrokeButton.addActionListener(stroke_choice_listener);
    fComplexStrokeButton.addActionListener(stroke_choice_listener);
    button_group = new ButtonGroup();
    button_group.add(fSimpleStrokeButton);
    button_group.add(fComplexStrokeButton);
    stroke_choice_panel.add(fSimpleStrokeButton);
    stroke_choice_panel.add(fComplexStrokeButton);

    JPanel stroke_panel = new JPanel(new GridLayout(3, 1));
    stroke_panel.add(stroke_color_panel);
    stroke_panel.add(stroke_selection_color_panel);
    stroke_panel.add(stroke_choice_panel);

    // Combobox renderer for patterns.
    PatternRenderer pattern_renderer = new PatternRenderer();

    // Panel to control pattern 1.
    JPanel pattern_1_panel = new JPanel(new GridBagLayout());
    gbc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    fPattern1ComboBox = new JComboBox(fPatternChoiceList);
    fPattern1ComboBox.setToolTipText("Choose first pattern");
    fPattern1ComboBox.setRenderer(pattern_renderer);
    fPattern1ComboBox.addActionListener(pattern_configuration_listener);
    JLabel pattern_1_repetition_label = new JLabel("Pattern repetition");
    fPattern1RepetitionSpinner = new MySpinner(new SpinnerNumberModel(PATTERN_1_REPETITION[0], PATTERN_1_REPETITION[1], PATTERN_1_REPETITION[2], 1), "Number of times the pattern should be repeated.");
    fPattern1RepetitionSpinner.addChangeListener(pattern_configuration_listener);
    JPanel pattern_repetition_panel_1 = new JPanel(new GridLayout(1, 2));
    pattern_repetition_panel_1.add(pattern_1_repetition_label);
    pattern_repetition_panel_1.add(fPattern1RepetitionSpinner);
    JLabel pattern_1_width_label = new JLabel("Gap width");
    fPattern1WidthSpinner = new MySpinner(new SpinnerNumberModel(PATTERN_1_GAP_WIDTH[0], PATTERN_1_GAP_WIDTH[1], PATTERN_1_GAP_WIDTH[2], 1), "Amount of space between repetitions of the pattern.");
    fPattern1WidthSpinner.addChangeListener(pattern_configuration_listener);
    JPanel pattern_width_panel_1 = new JPanel(new GridLayout(1, 2));
    pattern_width_panel_1.add(pattern_1_width_label);
    pattern_width_panel_1.add(fPattern1WidthSpinner);
    gbc.gridy = 0;
    pattern_1_panel.add(fPattern1ComboBox, gbc);
    gbc.gridy = 1;
    pattern_1_panel.add(pattern_repetition_panel_1, gbc);
    gbc.gridy = 2;
    pattern_1_panel.add(pattern_width_panel_1, gbc);
    pattern_1_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                                                 BorderFactory.createTitledBorder("Pattern 1")));

    // Panel to control pattern 2.
    JPanel pattern_2_panel = new JPanel(new GridBagLayout());
    gbc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    fPattern2ComboBox = new JComboBox(fPatternChoiceList);
    fPattern2ComboBox.setToolTipText("Choose second pattern");
    fPattern2ComboBox.setRenderer(pattern_renderer);
    fPattern2ComboBox.addActionListener(pattern_configuration_listener);
    JLabel pattern_2_repetition_label = new JLabel("Pattern repetition");
    fPattern2RepetitionSpinner = new MySpinner(new SpinnerNumberModel(PATTERN_2_REPETITION[0], PATTERN_2_REPETITION[1], PATTERN_2_REPETITION[2], 1), "Number of times the pattern should be repeated.");
    fPattern2RepetitionSpinner.addChangeListener(pattern_configuration_listener);
    JPanel pattern_repetition_panel_2 = new JPanel(new GridLayout(1, 2));
    pattern_repetition_panel_2.add(pattern_2_repetition_label);
    pattern_repetition_panel_2.add(fPattern2RepetitionSpinner);
    JLabel pattern_2_width_label = new JLabel("Gap width");
    fPattern2WidthSpinner = new MySpinner(new SpinnerNumberModel(PATTERN_2_GAP_WIDTH[0], PATTERN_2_GAP_WIDTH[1], PATTERN_2_GAP_WIDTH[2], 1), "Amount of space between repetitions of the pattern.");
    fPattern2WidthSpinner.addChangeListener(pattern_configuration_listener);
    JPanel pattern_width_panel_2 = new JPanel(new GridLayout(1, 2));
    pattern_width_panel_2.add(pattern_2_width_label);
    pattern_width_panel_2.add(fPattern2WidthSpinner);
    gbc.gridy = 0;
    pattern_2_panel.add(fPattern2ComboBox, gbc);
    gbc.gridy = 1;
    pattern_2_panel.add(pattern_repetition_panel_2, gbc);
    gbc.gridy = 2;
    pattern_2_panel.add(pattern_width_panel_2, gbc);
    pattern_2_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                                                 BorderFactory.createTitledBorder("Pattern 2")));

    // Panel to control general settings of complex stroke.

    // 1. Panel to control tolerance.
    JPanel tolerance_panel = new JPanel(new GridLayout(1, 2));
    JLabel tolerance_label = new JLabel("Tolerance");
    fToleranceSpinner = new MySpinner(new SpinnerNumberModel(TOLERANCE[0], TOLERANCE[1], TOLERANCE[2], 1), "The allowed difference between the combined pattern and the real path, in pixels.");
    fToleranceSpinner.addChangeListener(pattern_configuration_listener);
    tolerance_panel.add(tolerance_label);
    tolerance_panel.add(fToleranceSpinner);
    tolerance_panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // 2. Panel to allow split.
    JPanel allow_split_panel = new JPanel();
    allow_split_panel.setLayout(new GridBagLayout());
    fAllowSplitCheckBox = new JCheckBox("Allow split", true);
    fAllowSplitCheckBox.setToolTipText("<html>If checked, the pattern can be split up if needed (e.g. to respect the above tolerance). <br/>If unchecked, the combined pattern is always displayed in its entirety.</html>");
    fAllowSplitCheckBox.addActionListener(pattern_configuration_listener);
    gbc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 2, 2), 0, 0);
    allow_split_panel.add(fAllowSplitCheckBox, gbc);

    JPanel general_settings_panel = new JPanel();
    general_settings_panel.setLayout(new BoxLayout(general_settings_panel, BoxLayout.Y_AXIS));
    general_settings_panel.add(tolerance_panel);
    general_settings_panel.add(allow_split_panel);
    general_settings_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                                                        BorderFactory.createTitledBorder("General settings")));

    // Panel to control halo.

    // 1. Panel to control halo thickness.
    JPanel halo_thickness_panel = new JPanel(new GridLayout(1, 2));
    JLabel halo_thickness_label = new JLabel("Halo thickness");
    fHaloSpinner = new MySpinner(new SpinnerNumberModel(HALO_THICKNESS[0], HALO_THICKNESS[1], HALO_THICKNESS[2], 1), "Halo thickness in pixels.");
    fHaloSpinner.addChangeListener(halo_listener);
    halo_thickness_panel.add(halo_thickness_label);
    halo_thickness_panel.add(fHaloSpinner);
    halo_thickness_panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    // 2. Panel to control halo color.
    JPanel halo_color_panel = new JPanel(new GridLayout(1, 2));
    JLabel halo_color_label = new JLabel("Halo color");
    fHaloColorPickPanel = new ColorPickPanel();
    fHaloColorPickPanel.setToolTipText("Configure halo color");
    fHaloColorPickPanel.setColor(HALO_COLOR);
    fHaloColorPickPanel.addPropertyChangeListener(ColorPickPanel.COLOR_PROPERTY, halo_color_listener);
    halo_color_panel.add(halo_color_label);
    halo_color_panel.add(fHaloColorPickPanel);
    halo_color_panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel halo_panel = new JPanel(new GridLayout(2, 1));
    halo_panel.add(halo_thickness_panel);
    halo_panel.add(halo_color_panel);

    // Global layout.
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(TitledPanel.createTitledPanel("Roundness", roundness_panel));
    add(TitledPanel.createTitledPanel("Fill", fill_choice_panel));
    add(TitledPanel.createTitledPanel("Stroke", stroke_panel));
    add(pattern_1_panel);
    add(pattern_2_panel);
    add(general_settings_panel);
    add(TitledPanel.createTitledPanel("Halo", halo_panel));

    updateCustomizerFromDomainObject();
  }

  private JSlider createSlider(int aMin, int aMax, int aValue, int aMinorTickSpacing, int aMajorTickSpacing) {
    JSlider slider = new JSlider(aMin, aMax, aValue);
    slider.setMinorTickSpacing(aMinorTickSpacing);
    slider.setMajorTickSpacing(aMajorTickSpacing);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.setPaintTrack(true);
    return slider;
  }

  private ILcdGXYPainterStyle createSimpleFillStyle() {
    return new TLcdGXYPainterColorStyle(new Color(184, 184, 184, 32));
  }

  private ILcdGXYPainterStyle createHatchedFillStyle() {
    return new TLcdGXYHatchedFillStyle();
  }

  private ILcdGXYPainterStyle createSimpleLineStyle() {
    return TLcdStrokeLineStyle.newBuilder()
                              .dashedLineStyle()
                              .antiAliasing(true)
                              .color(fStrokeColorPickPanel.getColor())
                              .selectionColor(fStrokeSelectionColorPickPanel.getColor())
                              .lineWidth(3).build();
  }

  private ILcdGXYPainterStyle createComplexLineStyle() {
    ComplexStrokePainterStyle painter_style = new ComplexStrokePainterStyle();
    painter_style.setDefaultColor(fStrokeColorPickPanel.getColor());
    painter_style.setSelectionColor(fStrokeSelectionColorPickPanel.getColor());
    PatternLibrary.Pattern[] patterns = new PatternLibrary.Pattern[2];
    patterns[0] = (PatternLibrary.Pattern) fPattern1ComboBox.getSelectedItem();
    patterns[1] = (PatternLibrary.Pattern) fPattern2ComboBox.getSelectedItem();
    int[] repetitions = new int[]{(Integer) fPattern1RepetitionSpinner.getValue(),
                                  (Integer) fPattern2RepetitionSpinner.getValue()};
    int[] gap_widths = new int[]{(Integer) fPattern1WidthSpinner.getValue(),
                                 (Integer) fPattern2WidthSpinner.getValue()};
    painter_style.setPatterns(patterns, repetitions, gap_widths);
    painter_style.setAllowSplit(fAllowSplitCheckBox.isSelected());
    painter_style.setTolerance((Integer) fToleranceSpinner.getValue());
    return painter_style;
  }

  private class RoundnessChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent aChangeEvent) {
      JSlider slider = (JSlider) aChangeEvent.getSource();
      StyledShape shape = getStyledShape();
      if (shape != null) {
        // Update object.
        shape.getShapeStyle().setRoundness(slider.getValue() / 100d);

        // Update model.
        elementChanged(shape);
      }
    }
  }

  private class HaloThicknessListener implements ChangeListener {

    public void stateChanged(ChangeEvent aChangeEvent) {
      JSpinner spinner = (JSpinner) aChangeEvent.getSource();
      StyledShape polyline = getStyledShape();
      if (polyline != null && !fIsUpdatingCustomizerFromObject) {
        // Update object.
        polyline.getShapeStyle().setHaloThickness((Integer) spinner.getValue());

        // Update model.
        elementChanged(polyline);
      }
    }
  }

  private class StrokeColorListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent aEvent) {
      StyledShape polyline = getStyledShape();
      if (polyline != null && !fIsUpdatingCustomizerFromObject) {
        // Retrieve new color.
        Color new_color = (Color) aEvent.getNewValue();

        if (new_color != null) {
          // Update object.
          ILcdGXYPainterStyle style = polyline.getShapeStyle().getLineStyle();
          if (style instanceof TLcdGXYPainterColorStyle) {
            ((TLcdGXYPainterColorStyle) style).setDefaultColor(new_color);
          } else if (style instanceof TLcdStrokeLineStyle) {
            ((TLcdStrokeLineStyle) style).setColor(new_color);
          }

          // Update model.
          elementChanged(polyline);
        }
      }
    }
  }

  private class StrokeSelectionColorListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent aEvent) {
      StyledShape shape = getStyledShape();
      if (shape != null && !fIsUpdatingCustomizerFromObject) {
        // Retrieve new color.
        Color new_color = (Color) aEvent.getNewValue();

        if (new_color != null) {
          // Update object.
          ILcdGXYPainterStyle style = shape.getShapeStyle().getLineStyle();
          if (style instanceof TLcdGXYPainterColorStyle) {
            ((TLcdGXYPainterColorStyle) style).setSelectionColor(new_color);
          } else if (style instanceof TLcdStrokeLineStyle) {
            ((TLcdStrokeLineStyle) style).setSelectionColor(new_color);
          }

          // Update model.
          elementChanged(shape);
        }
      }
    }
  }

  private class HaloColorListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent aEvent) {
      StyledShape shape = getStyledShape();
      if (shape != null && !fIsUpdatingCustomizerFromObject) {
        // Retrieve new color.
        Color new_color = (Color) aEvent.getNewValue();

        if (new_color != null) {
          // Update object.
          shape.getShapeStyle().setHaloColor(new_color);

          // Update model.
          elementChanged(shape);
        }
      }
    }
  }

  private class FillChoiceListener implements ActionListener {
    public void actionPerformed(ActionEvent aActionEvent) {
      StyledShape shape = getStyledShape();
      if (shape != null && !fIsUpdatingCustomizerFromObject) {
        if (fSimpleFillButton.isSelected()) {
          // Create new line style.
          ILcdGXYPainterStyle painter_style = createSimpleFillStyle();

          // Update object.
          shape.getShapeStyle().setFillStyle(painter_style);
        } else {
          // Create new line style.
          ILcdGXYPainterStyle painter_style = createHatchedFillStyle();

          // Update object.
          shape.getShapeStyle().setFillStyle(painter_style);
        }

        // Update model.
        elementChanged(shape);
      }
    }
  }

  private class StrokeChoiceListener implements ActionListener {
    public void actionPerformed(ActionEvent aActionEvent) {
      StyledShape shape = getStyledShape();
      if (shape != null && !fIsUpdatingCustomizerFromObject) {
        if (fSimpleStrokeButton.isSelected()) {
          // Create new line style.
          ILcdGXYPainterStyle painter_style = createSimpleLineStyle();

          // Update object.
          shape.getShapeStyle().setLineStyle(painter_style);

          // Update GUI.
          setComplexStrokeConfigurationEnabled(false);
        } else {
          // Create new line style.
          ILcdGXYPainterStyle painter_style = createComplexLineStyle();

          // Update object.
          shape.getShapeStyle().setLineStyle(painter_style);

          // Update GUI.
          setComplexStrokeConfigurationEnabled(true);
        }

        // Update model.
        elementChanged(shape);
      }
    }
  }

  private class ComplexStrokeConfigurationListener implements ChangeListener, ActionListener {

    public void stateChanged(ChangeEvent e) {
      updateObject();
    }

    public void actionPerformed(ActionEvent e) {
      updateObject();
    }

    private void updateObject() {
      StyledShape polyline = getStyledShape();
      if (polyline != null && !fIsUpdatingCustomizerFromObject) {
        // Create new line style.
        ILcdGXYPainterStyle painter_style = createComplexLineStyle();

        // Update object.
        polyline.getShapeStyle().setLineStyle(painter_style);

        // Update model.
        elementChanged(polyline);
      }
    }
  }

  private static class SliderTextFieldMediator implements ChangeListener, ActionListener, FocusListener {

    private JTextField fTextField;
    private JSlider fSlider;
    private Format fFormat;

    public SliderTextFieldMediator(JTextField aTextField, JSlider aSlider) {
      fTextField = aTextField;
      fSlider = aSlider;
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMaximumFractionDigits(0);
      format.setMinimumFractionDigits(0);
      fFormat = format;
      slider2textfield();
    }

    public void stateChanged(ChangeEvent aEvent) {
      slider2textfield();
    }

    public void actionPerformed(ActionEvent aEvent) {
      textfield2slider();
    }

    public void focusLost(FocusEvent aEvent) {
      textfield2slider();
    }

    public void focusGained(FocusEvent aEvent) {
    }

    private void textfield2slider() {
      try {
        fSlider.setValue(((Number) fFormat.parseObject(fTextField.getText())).intValue());
      } catch (ParseException aException) {
        // parse error invalid value ...
      }
    }

    private void slider2textfield() {
      fTextField.setText(fFormat.format((double) fSlider.getValue()));
    }
  }

  private static class PatternRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (isSelected) {
        setBackground(Color.lightGray);
      }

      if (value instanceof PatternLibrary.Pattern) {
        setIcon(((PatternLibrary.Pattern) value).getIcon());
        setText(null);
      }
      return this;
    }
  }

  private class MySpinner extends JSpinner {

    public MySpinner(SpinnerNumberModel aModel, String aTooltip) {
      super(aModel);

      // Install tooltip.
      setToolTipText("<html>" + aTooltip + "<br/>" + "Choose a value between " + aModel.getMinimum() + " and " + aModel.getMaximum() + ".</html>");

      // Restrict text field input.
      JFormattedTextField field = getTextField(this);
      JFormattedTextField.AbstractFormatter formatter = field.getFormatter();
      if (formatter instanceof DefaultFormatter) {
        ((DefaultFormatter) formatter).setAllowsInvalid(false);
        ((DefaultFormatter) formatter).setOverwriteMode(true);
      }
    }

    /**
     * Return the formatted text field used by the editor, or null if the editor doesn't descend
     * from JSpinner.DefaultEditor.
     *
     * @param aSpinner A JSpinner object.
     *
     * @return the formatted text field used by the editor, or null if the editor doesn't descend
     *         from JSpinner.DefaultEditor.
     */
    private JFormattedTextField getTextField(JSpinner aSpinner) {
      JComponent editor = aSpinner.getEditor();
      if (editor instanceof DefaultEditor) {
        return ((DefaultEditor) editor).getTextField();
      } else {
        System.err.println("Unexpected editor type: " + aSpinner.getEditor().getClass() + " isn't a descendant of DefaultEditor");
        return null;
      }
    }
  }
}
