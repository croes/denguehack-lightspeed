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
package samples.symbology.common.gui.customizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.common.TwoColumnPanel;
import samples.common.gui.ColorChooser;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.StyleMediator;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

/**
 * GUI panel for changing some style properties of a symbol.
 */
class SymbolStyleCustomizer extends AbstractSymbolCustomizer {
  private final StyleMediator fStyleMediator;
  private final JPanel fContent;

  private final JSlider fRoundedCornerSlider, fLineSizeSlider, fSizeSlider, fLabelHaloThicknessSlider, fBorderWidthSlider;
  private final JTextField fRoundedCornerField, fLineSizeField, fSizeField, fLabelHaloThicknessField, fBorderWidthField;
  private final JSlider fFontSizeSlider, fHaloThicknessSlider;
  private final JTextField fFontSizeField, fHaloThicknessField;
  private final ColorChooser fHaloColorChooser, fLabelHaloColorChooser;
  private final JCheckBox fFrameCheckBox, fFillCheckBox, fIconCheckBox;

  private boolean fInternalChange = false;

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public SymbolStyleCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fStyleMediator = new SymbologyStyleMediator();
    final StyleValueChangeListener styleValueChangeListener = new StyleValueChangeListener();

    // Slider panel for rounded corners.
    fRoundedCornerField = new JTextField(3);
    fRoundedCornerField.setEnabled(false);
    fRoundedCornerSlider = createSlider(0, 100, 50, 10, 50);
    fRoundedCornerSlider.setEnabled(false);
    fRoundedCornerSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setCornerSmoothness((Integer) aValue / 100d);
      }
    });
    fRoundedCornerSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fRoundedCornerField, fRoundedCornerSlider);

    // Slider panel for font size.
    fFontSizeField = new JTextField(3);
    fFontSizeField.setEnabled(false);
    fFontSizeSlider = createSlider(1, 64, 12, 0, 63);
    fFontSizeSlider.setEnabled(false);
    fFontSizeSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setFontSize((Integer) aValue);
      }
    });
    fFontSizeSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fFontSizeField, fFontSizeSlider);

    // Slider panel for size.
    fSizeField = new JTextField(3);
    fSizeField.setEnabled(false);
    fSizeSlider = createSlider(25, 250, 96, 25, 50);
    fSizeSlider.setEnabled(false);
    fSizeSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setSizeSymbol((Integer) aValue);
      }
    });
    fSizeSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fSizeField, fSizeSlider);

    // Slider panel for line size.
    fLineSizeField = new JTextField(3);
    fLineSizeField.setEnabled(false);
    fLineSizeSlider = createSlider(1, 10, 2, 0, 2);
    fLineSizeSlider.setEnabled(false);
    fLineSizeSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setLineWidth((Integer) aValue);
      }
    });
    fLineSizeSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fLineSizeField, fLineSizeSlider);

    //Symbol border width slider
    fBorderWidthField = new JTextField(3);
    fBorderWidthField.setEnabled(false);
    fBorderWidthSlider = createSlider(1, 30, 10, 0, 5);
    fBorderWidthSlider.setEnabled(false);
    fBorderWidthSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setSymbolFrameLineWidth((Integer) aValue);
      }
    });
    fBorderWidthSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fBorderWidthField, fBorderWidthSlider);

    // Slider panel for halo thickness.
    fHaloThicknessField = new JTextField(3);
    fHaloThicknessField.setEnabled(false);
    fHaloThicknessSlider = createSlider(0, 3, 0, 0, 1);
    fHaloThicknessSlider.setEnabled(false);
    fHaloThicknessSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setHaloThickness((Integer) aValue);
      }
    });
    fHaloThicknessSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fHaloThicknessField, fHaloThicknessSlider);
    fHaloColorChooser = new ColorChooser(Color.white, 25, 25);
    fHaloColorChooser.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setHaloColor((Color) aValue);
      }
    });
    fHaloColorChooser.addPropertyChangeListener("color",styleValueChangeListener);

    // Slider panel for label halo thickness.
    fLabelHaloThicknessField = new JTextField(3);
    fLabelHaloThicknessField.setEnabled(false);
    fLabelHaloThicknessSlider = createSlider(0, 3, 0, 0, 1);
    fLabelHaloThicknessSlider.setEnabled(false);
    fLabelHaloThicknessSlider.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setLabelHaloThickness((Integer) aValue);
      }
    });
    fLabelHaloThicknessSlider.addChangeListener(styleValueChangeListener);
    SliderTextFieldMediator.link(fLabelHaloThicknessField, fLabelHaloThicknessSlider);
    fLabelHaloColorChooser = new ColorChooser(Color.white, 25, 25);
    fLabelHaloColorChooser.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setLabelHaloColor((Color) aValue);
      }
    });
    fLabelHaloColorChooser.addPropertyChangeListener("color",styleValueChangeListener);

    fFrameCheckBox = new JCheckBox(translate("Frame", aStringTranslator), false) {
      @Override
      public void setEnabled(boolean b) {
        removeChangeListener(styleValueChangeListener);
        super.setEnabled(b);
        addChangeListener(styleValueChangeListener);
      }
    };
    fFrameCheckBox.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setSymbolFrameEnabled((Boolean) aValue);
      }
    });
    fFrameCheckBox.addChangeListener(styleValueChangeListener);

    fFillCheckBox = new JCheckBox(translate("Fill", aStringTranslator), false) {
      @Override
      public void setEnabled(boolean b) {
        removeChangeListener(styleValueChangeListener);
        super.setEnabled(b);
        addChangeListener(styleValueChangeListener);
      }
    };
    fFillCheckBox.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setSymbolFillEnabled((Boolean) aValue);
      }
    });
    fFillCheckBox.addChangeListener(styleValueChangeListener);

    fIconCheckBox = new JCheckBox(translate("Icon", aStringTranslator), false) {
      @Override
      public void setEnabled(boolean b) {
        removeChangeListener(styleValueChangeListener);
        super.setEnabled(b);
        addChangeListener(styleValueChangeListener);
      }
    };
    fIconCheckBox.putClientProperty(SinglePropertyStyler.class, new SinglePropertyStyler() {
      @Override
      public void setValue(StyleMediator aStyleMediator, Object aValue) {
        aStyleMediator.setSymbolIconEnabled((Boolean) aValue);
      }
    });
    fIconCheckBox.addChangeListener(styleValueChangeListener);

    fContent = new TwoColumnPanel() {
      @Override
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.setSize(250, d.getHeight());
        return d;
      }
    }.contentBuilder()
     .row(translate("Roundness", aStringTranslator), createSliderPanel(fRoundedCornerField, fRoundedCornerSlider))
     .row(translate("Font size", aStringTranslator), createSliderPanel(fFontSizeField, fFontSizeSlider))
     .row(translate("Symbol size", aStringTranslator), createSliderPanel(fSizeField, fSizeSlider))
     .row(translate("Line width", aStringTranslator), createSliderPanel(fLineSizeField, fLineSizeSlider))
     .row(translate("Frame width", aStringTranslator), createSliderPanel(fBorderWidthField, fBorderWidthSlider))
     .row(translate("Halo", aStringTranslator), createHaloSliderPanel(fHaloColorChooser, fHaloThicknessField, fHaloThicknessSlider))
     .row(translate("Label halo", aStringTranslator), createHaloSliderPanel(fLabelHaloColorChooser, fLabelHaloThicknessField, fLabelHaloThicknessSlider))
     .row(translate("Draw", aStringTranslator), createIconOptionsPanel(fFrameCheckBox, fFillCheckBox, fIconCheckBox))
     .build();
  }

  private static String translate(String aString, ILcdStringTranslator aStringTranslator) {
    return aStringTranslator == null ? aString : aStringTranslator.translate(aString);
  }

  private static JPanel createSliderPanel(JTextField aRoundedCornerField, JSlider aRoundedCornerSlider) {
    JPanel areaFieldPanel = new JPanel(new BorderLayout());
    areaFieldPanel.add(BorderLayout.NORTH, aRoundedCornerField);
    JPanel areaPanel = new JPanel(new BorderLayout(5, 5));
    areaPanel.add(BorderLayout.CENTER, aRoundedCornerSlider);
    areaPanel.add(BorderLayout.EAST, areaFieldPanel);
    return areaPanel;
  }

  private static JPanel createHaloSliderPanel(JPanel aColorChooser, JTextField aRoundedCornerField, JSlider aRoundedCornerSlider) {
    JPanel areaFieldPanel = new JPanel(new BorderLayout());
    areaFieldPanel.add(BorderLayout.NORTH, aRoundedCornerField);
    JPanel areaPanel = new JPanel(new BorderLayout(5, 5));
    areaPanel.add(BorderLayout.WEST, aColorChooser);
    areaPanel.add(BorderLayout.CENTER, aRoundedCornerSlider);
    areaPanel.add(BorderLayout.EAST, areaFieldPanel);
    return areaPanel;
  }

  private static JPanel createIconOptionsPanel(JCheckBox aFrameCheckBox, JCheckBox aFillCheckBox, JCheckBox aIconCheckBox) {
    JPanel iconOptionsPanel = new JPanel();
    iconOptionsPanel.setLayout(new GridLayout(1, 3));
    iconOptionsPanel.add(aFrameCheckBox);
    iconOptionsPanel.add(aFillCheckBox);
    iconOptionsPanel.add(aIconCheckBox);
    return iconOptionsPanel;
  }

  @Override
  public JComponent getComponent() {
    return fContent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fRoundedCornerSlider.setEnabled(fStyleMediator.isLine() && aEnabled);
    fRoundedCornerField.setEnabled(fStyleMediator.isLine() && aEnabled);
    fLineSizeSlider.setEnabled(fStyleMediator.isLine() && aEnabled);
    fLineSizeField.setEnabled(fStyleMediator.isLine() && aEnabled);
    fBorderWidthField.setEnabled(!fStyleMediator.isLine() && aEnabled);
    fBorderWidthSlider.setEnabled(!fStyleMediator.isLine() && aEnabled);
    fSizeSlider.setEnabled(aEnabled);
    fSizeField.setEnabled(aEnabled);
    fFontSizeSlider.setEnabled(aEnabled);
    fFontSizeField.setEnabled(aEnabled);
    fHaloThicknessSlider.setEnabled(aEnabled);
    fHaloThicknessField.setEnabled(aEnabled);
    fHaloColorChooser.setEnabled(aEnabled);
    fLabelHaloThicknessSlider.setEnabled(aEnabled);
    fLabelHaloThicknessField.setEnabled(aEnabled);
    fLabelHaloColorChooser.setEnabled(aEnabled);
    fFrameCheckBox.setEnabled(!fStyleMediator.isLine() && aEnabled);
    fFillCheckBox.setEnabled(!fStyleMediator.isLine() && aEnabled);
    fIconCheckBox.setEnabled(!fStyleMediator.isLine() && aEnabled);
  }

  /**
   * Sets the symbol that should be customized.
   *
   * @param aModel             the model containing the symbol or {@code null}
   * @param aSymbol            the symbol or {@code null}
   */
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (isNewSymbol(aSymbol)) {
      fInternalChange = true;
      if (aModel != null && aSymbol != null && fStyleMediator.canSetObject(aSymbol)) {
        boolean isNewSymbol = fStyleMediator.getObject() != aSymbol;
        fStyleMediator.setObject(aSymbol);

        if (fStyleMediator.isLine()) {
          fRoundedCornerSlider.setEnabled(true);
          fRoundedCornerField.setEnabled(true);
          fRoundedCornerSlider.setValue((int) (fStyleMediator.getCornerSmoothness() * 100.0d));
          fRoundedCornerSlider.invalidate();

          fFrameCheckBox.setEnabled(false);
          fFillCheckBox.setEnabled(false);
          fIconCheckBox.setEnabled(false);
        } else {
          fRoundedCornerSlider.setEnabled(false);
          fRoundedCornerField.setEnabled(false);

          fFrameCheckBox.setSelected(fStyleMediator.isSymbolFrameEnabled());
          fFrameCheckBox.setEnabled(true);
          fFrameCheckBox.invalidate();

          fFillCheckBox.setSelected(fStyleMediator.isSymbolFillEnabled());
          fFillCheckBox.setEnabled(true);
          fFillCheckBox.invalidate();

          fIconCheckBox.setSelected(fStyleMediator.isSymbolIconEnabled());
          fIconCheckBox.setEnabled(true);
          fIconCheckBox.invalidate();
        }
        fFontSizeSlider.setEnabled(true);
        fFontSizeField.setEnabled(true);
        fFontSizeSlider.setValue(fStyleMediator.getFontSize());
        fFontSizeSlider.invalidate();

        fSizeSlider.setEnabled(true);
        fSizeField.setEnabled(true);
        fSizeSlider.setValue(fStyleMediator.getSizeSymbol());
        fSizeSlider.invalidate();

        fLineSizeSlider.setEnabled(true);
        fLineSizeField.setEnabled(true);
        fLineSizeSlider.setValue(fStyleMediator.getLineWidth());
        fLineSizeSlider.invalidate();

        fBorderWidthSlider.setEnabled(true);
        fBorderWidthField.setEnabled(true);
        fBorderWidthSlider.setValue(fStyleMediator.getSymbolFrameLineWidth());
        fBorderWidthSlider.invalidate();

        fHaloThicknessSlider.setEnabled(true);
        fHaloThicknessField.setEnabled(true);
        fHaloThicknessSlider.setValue(fStyleMediator.getHaloThickness());
        fHaloThicknessSlider.invalidate();
        fHaloColorChooser.setEnabled(true);
        fHaloColorChooser.setColor(fStyleMediator.getHaloColor());

        fLabelHaloThicknessSlider.setEnabled(true);
        fLabelHaloThicknessField.setEnabled(true);
        fLabelHaloThicknessSlider.setValue(fStyleMediator.getLabelHaloThickness());
        fLabelHaloThicknessSlider.invalidate();
        fLabelHaloColorChooser.setEnabled(true);
        fLabelHaloColorChooser.setColor(fStyleMediator.getLabelHaloColor());

        if (isNewSymbol) {
          if (fRoundedCornerSlider.isEnabled()) {
            fRoundedCornerSlider.putClientProperty("OLD_VALUE", fRoundedCornerSlider.getValue());
          }
          fFontSizeSlider.putClientProperty("OLD_VALUE", fFontSizeSlider.getValue());
          fSizeSlider.putClientProperty("OLD_VALUE", fSizeSlider.getValue());
          fLineSizeSlider.putClientProperty("OLD_VALUE", fLineSizeSlider.getValue());
          fBorderWidthSlider.putClientProperty("OLD_VALUE", fBorderWidthSlider.getValue());
          fHaloThicknessSlider.putClientProperty("OLD_VALUE", fHaloThicknessSlider.getValue());
          fLabelHaloThicknessSlider.putClientProperty("OLD_VALUE", fLabelHaloThicknessSlider.getValue());
          if (fFrameCheckBox.isEnabled()) {
            fFrameCheckBox.putClientProperty("OLD_VALUE", fFrameCheckBox.isSelected());
          }
          if (fFillCheckBox.isEnabled()) {
            fFillCheckBox.putClientProperty("OLD_VALUE", fFillCheckBox.isSelected());
          }
          if (fIconCheckBox.isEnabled()) {
            fIconCheckBox.putClientProperty("OLD_VALUE", fIconCheckBox.isSelected());
          }
        }
      } else {
        fRoundedCornerSlider.setEnabled(false);
        fRoundedCornerField.setEnabled(false);
        fSizeSlider.setEnabled(false);
        fSizeField.setEnabled(false);
        fLineSizeSlider.setEnabled(false);
        fLineSizeField.setEnabled(false);
        fBorderWidthSlider.setEnabled(false);
        fBorderWidthField.setEnabled(false);
        fFontSizeSlider.setEnabled(false);
        fFontSizeField.setEnabled(false);
        fHaloThicknessSlider.setEnabled(false);
        fHaloThicknessField.setEnabled(false);
        fHaloColorChooser.setEnabled(false);
        fLabelHaloThicknessSlider.setEnabled(false);
        fLabelHaloThicknessField.setEnabled(false);
        fLabelHaloColorChooser.setEnabled(false);
        fFrameCheckBox.setEnabled(false);
        fFillCheckBox.setEnabled(false);
        fIconCheckBox.setEnabled(false);
      }
      fInternalChange = false;
    }
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

  private static class SliderTextFieldMediator implements ChangeListener, ActionListener, FocusListener {
    private final JTextField fTextField;
    private final JSlider fSlider;
    private final Format fFormat;

    private boolean fUpdating = false;

    public SliderTextFieldMediator(JTextField aTextField, JSlider aSlider) {
      fTextField = aTextField;
      fSlider = aSlider;
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMaximumFractionDigits(0);
      format.setMinimumFractionDigits(0);
      fFormat = format;
    }

    public static void link(JTextField aTextField, JSlider aSlider) {
      SliderTextFieldMediator mediator = new SliderTextFieldMediator(aTextField, aSlider);
      aSlider.addChangeListener(mediator);
      aTextField.addFocusListener(mediator);
      aTextField.addActionListener(mediator);
      mediator.update(aTextField);
    }

    public void stateChanged(ChangeEvent aEvent) {
      update(fTextField);
    }

    public void actionPerformed(ActionEvent aEvent) {
      update(fSlider);
    }

    public void focusLost(FocusEvent aEvent) {
      update(fSlider);
    }

    public void focusGained(FocusEvent aEvent) {
    }

    private void update(JSlider aSlider) {
      if (!fUpdating) {
        fUpdating = true;
        try {
          aSlider.setValue(((Number) fFormat.parseObject(fTextField.getText())).intValue());
        } catch (ParseException ignored) {
        }
        fUpdating = false;
      }
    }

    private void update(JTextField aTextField) {
      if (!fUpdating) {
        fUpdating = true;
        aTextField.setText(fFormat.format((double) fSlider.getValue()));
        fUpdating = false;
      }
    }
  }

  private interface SinglePropertyStyler {
    public void setValue(StyleMediator aStyleMediator, Object aValue);
  }

  private class StyleValueChangeListener implements ChangeListener, PropertyChangeListener {
    @Override
    public void stateChanged(ChangeEvent e) {
      if (!fInternalChange) {
        final JComponent component = (JComponent) e.getSource();
        final SinglePropertyStyler singlePropertyStyler = (SinglePropertyStyler) component.getClientProperty(SinglePropertyStyler.class);
        final Object currentObject = fStyleMediator.getObject();
        if (currentObject != null) {
          final Object newValue = getValue(component);
          Runnable changeRunnable = new Runnable() {
            @Override
            public void run() {
              Object o = fStyleMediator.getObject();
              fStyleMediator.setObject(currentObject);
              singlePropertyStyler.setValue(fStyleMediator, newValue);
              fStyleMediator.setObject(o);
            }
          };
          Runnable undoRunnable = null;
          if (!isAdjusting(component)) {
            final Object oldValue = component.getClientProperty("OLD_VALUE");
            if (oldValue != null) {
              undoRunnable = new Runnable() {
                @Override
                public void run() {
                  Object o = fStyleMediator.getObject();
                  fStyleMediator.setObject(currentObject);
                  singlePropertyStyler.setValue(fStyleMediator, oldValue);
                  fStyleMediator.setObject(o);
                }
              };
            }
            component.putClientProperty("OLD_VALUE", newValue);
          }
          applyChange(changeRunnable, undoRunnable);
        }
      }
    }

    private Object getValue(Object aComponent) {
      if (aComponent instanceof JSlider) {
        return ((JSlider) aComponent).getValue();
      } else if (aComponent instanceof JCheckBox) {
        return ((JCheckBox) aComponent).isSelected();
      } else {
        throw new IllegalArgumentException("Unknown component");
      }
    }

    private boolean isAdjusting(Object aComponent) {
      if (aComponent instanceof JSlider) {
        return ((JSlider) aComponent).getValueIsAdjusting();
      }
      return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (!fInternalChange) {
        final JComponent source = (JComponent) evt.getSource();
        final SinglePropertyStyler singlePropertyStyler = (SinglePropertyStyler) source.getClientProperty(SinglePropertyStyler.class);
        final Object currentObject = fStyleMediator.getObject();
        final Object newValue = evt.getNewValue();
        Runnable changeRunnable = new Runnable() {
          @Override
          public void run() {
            Object o = fStyleMediator.getObject();
            fStyleMediator.setObject(currentObject);
            singlePropertyStyler.setValue(fStyleMediator, newValue);
            fStyleMediator.setObject(o);
          }
        };
        Runnable undoRunnable = null;
        final Object oldValue = evt.getOldValue();
        if (oldValue != null) {
          undoRunnable = new Runnable() {
            @Override
            public void run() {
              Object o = fStyleMediator.getObject();
              fStyleMediator.setObject(currentObject);
              singlePropertyStyler.setValue(fStyleMediator, oldValue);
              fStyleMediator.setObject(o);
            }
          };
        }
        applyChange(changeRunnable, undoRunnable);
      }
    }
  }
}
