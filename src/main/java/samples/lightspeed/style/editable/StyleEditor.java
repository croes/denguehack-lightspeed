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
package samples.lightspeed.style.editable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.TLcdAWTUtil;
import samples.common.gui.ColorChooser;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

/**
 * A GUI widget for editing styles.
 * <p/>
 * Workings of the dialog can be summarized as follows:
 * <ol>
 * <li> User launches the dialog: the system initializes its GUI to match the values that are common between all selected objects.</li>
 * <li>When the user presses the ok button, any feature that was adjusted in the dialog will be
 * applied to ALL the selected objects. For the features that were not adjusted, the value of
 * the object's old style will be used.</li>
 * </ol>
 */
public class StyleEditor extends JDialog {

  private StyleEditorModel fModel;

  public static void editStyle(ILspAWTView aView, TLspEditableStyler aStyler) {
    StyleEditor editor = new StyleEditor(aView, new StyleEditorModel(aView, aStyler));
    editor.setVisible(true);
  }

  private StyleEditor(ILspAWTView aView, StyleEditorModel aModel) throws HeadlessException {
    super(TLcdAWTUtil.findParentFrame(aView.getHostComponent()), "Style Editor", true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    fModel = aModel;
    buildGUI();
  }

  private void buildGUI() {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

    contentPanel.add(buildContentPanel());
    contentPanel.add(buildButtonsPanel());

    setContentPane(contentPanel);
    pack();
  }

  /**
   * Create panel with ok and cancel buttons.
   */
  private JPanel buildButtonsPanel() {
    JPanel panel = new JPanel();

    JButton applyButton = new ApplyButton(fModel);
    applyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fModel.apply();
      }
    });

    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fModel.apply();
        StyleEditor.this.dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
                                     @Override
                                     public void actionPerformed(ActionEvent e) {
                                       StyleEditor.this.dispose();
                                     }
                                   }
    );

    panel.add(okButton);
    panel.add(cancelButton);
    panel.add(applyButton);
    return panel;
  }

  /**
   * Create content panel.
   */
  private JPanel buildContentPanel() {
    GridPanelBuilder builder = new GridPanelBuilder();
    builder.
               insets(5, 5, 5, 5).
               anchor(GridBagConstraints.LINE_START).
               fill(GridBagConstraints.BOTH);

    // -----------------
    // Line Width widget

    final JSpinner ws = new JSpinner(new MySpinnerModel(fModel.getLineWidth()));
    ws.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (ws.getValue() != null && !ws.getValue().equals("")) {
          fModel.setLineWidth(Integer.parseInt((String) ws.getValue()));
        }
      }
    });
    builder.
               add(new JLabel("Line Width:")).
               add(ws).
               wrap();

    // -----------------
    // Line Color widget

    ColorChooser lineColorChooser = new ColorChooser(fModel.getLineColor(), 25, 25);
    lineColorChooser.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("color".equals(evt.getPropertyName())) {
          fModel.setLineColor((Color) evt.getNewValue());
        }
      }
    });
    builder.
               add(new JLabel("Line Color:")).
               fill(GridBagConstraints.NONE).
               add(lineColorChooser).
               fill(GridBagConstraints.BOTH).  // Restore default fill property
        wrap();

    // -------------------
    // Line Pattern widget

    final JComboBox patternBox = new JComboBox(prepend(new LinePatternIcon(fModel
                                                                               .getLinePattern()), LinePatternIcon
                                                           .defaults()));
    patternBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        LinePatternIcon icon = (LinePatternIcon) patternBox.getSelectedItem();
        fModel.setLinePattern(icon.getPattern());
      }
    });
    builder.add(new JLabel("Line Pattern:")).gridWidth(2).add(patternBox).gridWidth(1)
           .wrap();

    // -----------------
    // Fill Color widget

    ColorChooser fillColorChooser = new ColorChooser(fModel.getFillColor(), 25, 25);
    fillColorChooser.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("color".equals(evt.getPropertyName())) {
          fModel.setFillColor((Color) evt.getNewValue());
        }
      }
    });
    builder.
               add(new JLabel("Fill Color:")).
               fill(GridBagConstraints.NONE).
               add(fillColorChooser).
               fill(GridBagConstraints.BOTH).   // Restore default fill property
        wrap();

    // -------------------
    // Fill Pattern widget

    final JComboBox fillPatternBox = new JComboBox(prepend(new FillPatternIcon(fModel
                                                                                   .getFillPattern()), FillPatternIcon
                                                               .defaults()));
    fillPatternBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        FillPatternIcon icon = (FillPatternIcon) fillPatternBox.getSelectedItem();
        fModel.setFillPattern(icon.getPattern());
      }
    });
    builder.
               add(new JLabel("Fill Pattern:")).
               add(fillPatternBox).
               wrap();

    return builder.getPanel();
  }

  /**
   * Prepends the given element to the given array.
   */
  private <E> E[] prepend(E aElement, E[] aArray) {
    if (aElement == null || aArray == null) {
      throw new IllegalArgumentException("Unable to prepend element [" + aElement + "] to array [" + aArray + "]");
    }
    E[] result = (E[]) Array.newInstance(aElement.getClass(), aArray.length + 1);
    result[0] = aElement;
    System.arraycopy(aArray, 0, result, 1, aArray.length);
    return result;
  }

//////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Simple button that disables itself when style editor model is dirty.
   */
  private static class ApplyButton extends JButton implements PropertyChangeListener {

    private ApplyButton(StyleEditorModel aModel) {
      super("Apply");
      aModel.addPropertyChangeListener(this);
      setEnabled(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("dirty")) {
        setEnabled((Boolean) evt.getNewValue());
      }
    }
  }

  /**
   * Helper class to convert a <code>TLspLineStyle.DashPattern</code> to an icon.
   */
  private static class LinePatternIcon implements Icon {

    TLspLineStyle.DashPattern fPattern;

    private static LinePatternIcon[] defaults() {
      return new LinePatternIcon[]{
          new LinePatternIcon(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.SOLID, 10)),
          new LinePatternIcon(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.DOT, 10)),
          new LinePatternIcon(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.LONG_DASH, 10)),
          new LinePatternIcon(new TLspLineStyle.DashPattern((short) 0xABCD, 10)),
          new LinePatternIcon(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.DOT_DASH, 10)),
      };
    }

    private LinePatternIcon(TLspLineStyle.DashPattern aPattern) {
      fPattern = aPattern;
    }

    public TLspLineStyle.DashPattern getPattern() {
      return fPattern;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (fPattern == null) {
        return;
      }

      int ix = 5;
      int iy = 5;
      int h = 2;
      int w = 90;
      int dw = (w / 16);

      g.setColor(Color.black);
      int i = 0;
      int posX = ix;
      while (i < 16 && posX < ix + w) {
        int val = 1 << i;

        if ((val & (int) fPattern.getStipplePattern()) != 0) {
          g.fillRect(x + posX, y + iy, dw, h);
        }

        i++;
        posX = ix + dw * i;
      }
    }

    @Override
    public int getIconWidth() {
      return 100;
    }

    @Override
    public int getIconHeight() {
      return 12;
    }
  }

  /**
   * Helper class to convert a <code>TLspFillStyle.StipplePattern</code> to an icon.
   */
  private static class FillPatternIcon implements Icon {

    private TLspFillStyle.StipplePattern fPattern;

    private static FillPatternIcon[] defaults() {
      return new FillPatternIcon[]{
          new FillPatternIcon(TLspFillStyle.StipplePattern.EMPTY_TONE),
          new FillPatternIcon(TLspFillStyle.StipplePattern.FULL_TONE),
          new FillPatternIcon(TLspFillStyle.StipplePattern.HALF_TONE),
          new FillPatternIcon(TLspFillStyle.StipplePattern.HALF_TONE_2x2),
          new FillPatternIcon(TLspFillStyle.StipplePattern.HALF_TONE_8x8),
          new FillPatternIcon(TLspFillStyle.StipplePattern.ONE_DOT),
          new FillPatternIcon(TLspFillStyle.StipplePattern.SIXTEEN_DOTS),
          new FillPatternIcon(TLspFillStyle.StipplePattern.HATCHED)
      };
    }

    private FillPatternIcon(TLspFillStyle.StipplePattern aPattern) {
      fPattern = aPattern;
    }

    public TLspFillStyle.StipplePattern getPattern() {
      return fPattern;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (fPattern == null) {
        return;
      }

      byte[] pattern = fPattern.getStipplePattern();
      boolean[] flags = new boolean[1024];

      for (int yi = 0; yi < 32; ++yi) {
        for (int i = 0; i < 4; ++i) {
          byte b = pattern[yi * 4 + i];
          int v = 0x0080;
          for (int j = 0; j < 8; ++j) {
            flags[yi * 32 + i * 8 + j] = (b & v) != 0;
            v = v >> 1;
          }
        }
      }

      for (int yi = 0; yi < 32; ++yi) {
        for (int xi = 0; xi < 32; ++xi) {
          if (flags[yi * 32 + xi]) {
            g.setColor(Color.black);
          } else {
            g.setColor(new Color(0, 0, 0, 0));
          }
          g.fillRect(xi * 2, (31 - yi) * 2, 2, 2);
        }
      }

    }

    @Override
    public int getIconWidth() {
      return 64;
    }

    @Override
    public int getIconHeight() {
      return 64;
    }
  }

  /**
   * Convenience class for creating a panel with a grid layout.
   */
  private static class GridPanelBuilder {

    private int fColumn;
    private int fRow;
    private int fIncrementX;
    private int fIncrementY;
    private GridBagConstraints fConstraints;
    private JPanel fPanel;

    private GridPanelBuilder() {
      fPanel = new JPanel(new GridBagLayout());
      fColumn = 0;
      fRow = 0;
      fIncrementX = 1;
      fIncrementY = 0;
      fConstraints = new GridBagConstraints();
    }

    private GridPanelBuilder insets(int aTop, int aLeft, int aBottom, int aRight) {
      fConstraints.insets = new Insets(aTop, aLeft, aBottom, aRight);
      return this;
    }

    private GridPanelBuilder gridWidth(int aWidth) {
      fConstraints.gridwidth = aWidth;
      fIncrementX = aWidth;
      return this;
    }

    private GridPanelBuilder gridHeight(int aHeight) {
      fConstraints.gridheight = aHeight;
      fIncrementY = aHeight;
      return this;
    }

    private GridPanelBuilder fill(int aFill) {
      fConstraints.fill = aFill;
      return this;
    }

    private GridPanelBuilder padX(int aPadX) {
      fConstraints.ipadx = aPadX;
      return this;
    }

    private GridPanelBuilder padY(int aPadY) {
      fConstraints.ipady = aPadY;
      return this;
    }

    private GridPanelBuilder anchor(int aAnchor) {
      fConstraints.anchor = aAnchor;
      return this;
    }

    private GridPanelBuilder weightX(double aWeight) {
      fConstraints.weightx = aWeight;
      return this;
    }

    private GridPanelBuilder weightY(double aWeight) {
      fConstraints.weighty = aWeight;
      return this;
    }

    private GridPanelBuilder wrap() {
      fColumn = 0;
      fRow++;
      return this;
    }

    private GridPanelBuilder add(JComponent aComponent) {
      fConstraints.gridx = fColumn;
      fConstraints.gridy = fRow;
      fColumn += fIncrementX;
      fRow += fIncrementY;
      fPanel.add(aComponent, fConstraints);
      return this;
    }

    private GridPanelBuilder title(String aTitle) {
      fPanel.setBorder(BorderFactory.createTitledBorder(aTitle));
      return this;
    }

    public JPanel getPanel() {
      return fPanel;
    }
  }

  /**
   * Spinner model that restricts its values to the range [1,8].
   */
  public static class MySpinnerModel extends AbstractSpinnerModel {

    private String fValue = "";

    public MySpinnerModel(Object aInitialValue) {
      if (aInitialValue instanceof Double) {
        setValue(((Double) aInitialValue).intValue());
      }
    }

    public void setValue(Object o) {
      fValue = o == null ? "" : o.toString();
      fireStateChanged();
    }

    public Object getValue() {
      return fValue;
    }

    public Object getPreviousValue() {
      Integer i = toInteger();
      if (i == null || i == 1) {
        return "1";
      } else {
        return "" + (i - 1);
      }
    }

    public Object getNextValue() {
      Integer i = toInteger();
      if (i == null) {
        return "1";
      } else {
        return "" + Math.min(i + 1, 8);
      }
    }

    private Integer toInteger() {
      if (fValue.equals("")) {
        return null;
      } else {
        return new Integer(fValue);
      }
    }
  }
}
