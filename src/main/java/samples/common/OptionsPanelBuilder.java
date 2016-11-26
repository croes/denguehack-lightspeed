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
package samples.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import samples.common.search.TextFieldComboBox;

/**
 * Builder for creating configuration panels that consist of multiple options, typically checkboxes, radio buttons
 * and/or toggle buttons.
 */
public class OptionsPanelBuilder {

  private static final int SPACE = 5;

  private List<PanelBuilder> fPanelBuilders = new ArrayList<PanelBuilder>();
  private JComponent fComponent;
  private List<AbstractButton> fButtons = new ArrayList<AbstractButton>();

  /**
   * Creates a new options panel builder.
   *
   * @return the builder instance.
   */
  public static OptionsPanelBuilder newInstance() {
    return new OptionsPanelBuilder();
  }

  public class Handle {

    private Handle() {
    }

    public void setEnabled(boolean aEnabled) {
      for (AbstractButton button : fButtons) {
        button.setEnabled(aEnabled);
      }
    }
  }

  OptionsPanelBuilder() {
  }

  /**
   * Final call on this builder, which populates the given component with all the panels created by this builder.
   *
   * @param aComponent the component to which the panel(s) of this builder should be added.
   */
  public Handle populate(JComponent aComponent) {
    TwoColumnLayoutBuilder layoutBuilder = TwoColumnLayoutBuilder.newBuilder();
    for (PanelBuilder panelBuilder : fPanelBuilders) {
      layoutBuilder.row().spanBothColumns(panelBuilder.build()).build();
    }
    layoutBuilder.populate(aComponent);
    fComponent = aComponent;
    return new Handle();
  }

  /**
   * Creates a new fixed panel (not collapsible).
   *
   * @param aTitle a title for the new panel.
   *
   * @return this builder.
   */
  public PanelBuilder fixedPanel(String aTitle) {
    PanelBuilder panelBuilder = new PanelBuilder(aTitle, false);
    fPanelBuilders.add(panelBuilder);
    return panelBuilder;
  }

  /**
   * Creates a new collapsible panel.
   *
   * @param aTitle a title for the new panel.
   *
   * @return this builder.
   */
  public PanelBuilder collapsiblePanel(String aTitle) {
    PanelBuilder panelBuilder = new PanelBuilder(aTitle, true);
    fPanelBuilders.add(panelBuilder);
    return panelBuilder;
  }

  public class PanelBuilder {

    private JPanel fPanel = new JPanel();
    private String fTitle;
    private boolean fCollapsible;
    private boolean fFirstRow = true;

    private PanelBuilder(String aTitle, boolean aCollapsible) {
      fPanel.setLayout(new BoxLayout(fPanel, BoxLayout.PAGE_AXIS));
      fPanel.setBorder(BorderFactory.createEmptyBorder(0, SPACE * 2, SPACE * 2, SPACE * 2));
      fTitle = aTitle;
      fCollapsible = aCollapsible;
    }

    private JPanel build() {
      TitledCollapsiblePane pane = new TitledCollapsiblePane(fTitle, fPanel, fCollapsible);
      pane.setCollapsed(fCollapsible);
      return pane;
    }

    /**
     * Adds a new toggle button group to the current panel, with the given list of options.
     *
     * @param aTitle             the title of the button group, used as display name in the UI.
     * @param aValues            the list of available values (one button will be created per value). These values will be passed
     *                           to the property change listener(s).
     * @param aValueDescriptions the list of descriptions to be used in the UI as button name.
     * @param aDefaultValue      the default value
     * @param aTooltip           a tooltip for the button group. Will be displayed when the cursor lingers over the component.
     * @param aUIListeners       zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder toggleButtonGroup(String aTitle,
                                          Object[] aValues,
                                          String[] aValueDescriptions,
                                          Object aDefaultValue,
                                          String aTooltip,
                                          String aHint,
                                          PropertyChangeListener... aUIListeners) {
      addButtonGroup(ButtonType.TOGGLE_BUTTON, aTitle, aTitle, aValues, Collections.<AbstractButton>emptyList(), aValueDescriptions, aDefaultValue, aTooltip, aHint, aUIListeners);
      return this;
    }

    /**
     * Adds a new radio button group to the current panel, with the given list of options.
     *
     * @param aTitle             the title of the button group, used as display name in the UI.
     * @param aValues            the list of available values (one button will be created per value). These values will be passed
     *                           to the property change listener(s).
     * @param aValueDescriptions the list of descriptions to be used in the UI as button name.
     * @param aDefaultValue      the default value
     * @param aTooltip           a tooltip for the button group. Will be displayed when the cursor lingers over the component.
     * @param aUIListeners       zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder radioButtonGroup(String aTitle,
                                         Object[] aValues,
                                         String[] aValueDescriptions,
                                         Object aDefaultValue,
                                         String aTooltip,
                                         String aHint,
                                         PropertyChangeListener... aUIListeners) {
      addButtonGroup(ButtonType.RADIO_BUTTON, aTitle, aTitle, aValues, Collections.<AbstractButton>emptyList(), aValueDescriptions, aDefaultValue, aTooltip, aHint, aUIListeners);
      return this;
    }

    /**
     * Creates a new checkbox for the given property.
     *
     * @param aPropertyName the property name, used to send property change events.
     * @param aTitle        the title of the checkbox, used as display name in the UI.
     * @param aDefaultValue the default value
     * @param aTooltip      a tooltip for the checkbox. Will be displayed when the cursor lingers over the component.
     * @param aHint         a hint providing more details about the checkbox. The hint will be added next to or under the checkbox.
     * @param aUIListeners  zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder checkBox(final String aPropertyName,
                                 String aTitle,
                                 boolean aDefaultValue,
                                 String aTooltip,
                                 String aHint,
                                 final PropertyChangeListener... aUIListeners) {
      addCheckBox(aPropertyName, aTitle, aDefaultValue, aTooltip, aHint, aUIListeners);
      return this;
    }

    /**
     * Creates a new text field for the given property.
     *
     * @param aPropertyName the property name, used to send property change events.
     * @param aTitle        the title of the field, used as display name in the UI.
     * @param aDefaultValue the default value
     * @param aTooltip      a tooltip for the field. Will be displayed when the cursor lingers over the component.
     * @param aUIListeners  zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder textField(final String aPropertyName,
                                  String aTitle,
                                  String aDefaultValue,
                                  String aTooltip,
                                  final PropertyChangeListener... aUIListeners) {
      addTextField(aPropertyName, aTitle, aDefaultValue, aTooltip, aUIListeners);
      return this;
    }

    /**
     * Adds a new list to the current panel, with the given list of options.
     *
     * @param aPropertyName the property name, used to send property change events.
     * @param aTitle             the title of the button group, used as display name in the UI.
     * @param aValues            the list of available values (one button will be created per value). These values will be passed
     *                           to the property change listener(s).
     * @param aValueDescriptions the list of descriptions to be used in the UI as button name.
     * @param aDefaultValues     the default selection
     * @param aTooltip           a tooltip for the button group. Will be displayed when the cursor lingers over the component.
     * @param aUIListeners       zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder list(String aPropertyName,
                             String aTitle,
                             Object[] aValues,
                             String[] aValueDescriptions,
                             Object[] aDefaultValues,
                             int aVisibleRowCount,
                             String aTooltip,
                             String aHint,
                             PropertyChangeListener... aUIListeners) {
      addList(aPropertyName, aTitle, aValues, aValueDescriptions, aDefaultValues, aVisibleRowCount, aTooltip, aHint, aUIListeners);
      return this;
    }

    /**
     * Adds a box with a drop-down list to the current panel, with the given list of options.
     *
     * @param aPropertyName      the property name, used to send property change events.
     * @param aTitle             the title of the list, used as display name in the UI.
     * @param aValues            the list of available values (one row will be created per value). These values will be passed
     *                           to the property change listener(s).
     * @param aValueDescriptions the list of descriptions to be used in the UI as button name.
     * @param aDefaultValue      the default selection
     * @param aTooltip           a tooltip for the button group. Will be displayed when the cursor lingers over the component.
     * @param aUIListeners       zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    public PanelBuilder listBox(String aPropertyName,
                                String aTitle,
                                Object[] aValues,
                                String[] aValueDescriptions,
                                Object aDefaultValue,
                                String aTooltip,
                                String aHint,
                                PropertyChangeListener... aUIListeners) {
      addListBox(aPropertyName, aTitle, aDefaultValue, aTooltip, aValues, aValueDescriptions, aHint, aUIListeners);
      return this;
    }

    /**
     * Adds the given component to this panel.
     *
     * @param aComponent a component to add to this panel.
     *
     * @return this builder
     */
    public PanelBuilder component(JComponent aComponent) {
      row().span(aComponent).build();
      return this;
    }

    /**
     * Creates a new checkbox for the given property.
     *
     * @param aPropertyName the property name, used to send property change events.
     * @param aTitle        the title of the checkbox, used as display name in the UI.
     * @param aDefaultValue the default value
     * @param aTooltip      a tooltip for the checkbox. Will be displayed when the cursor lingers over the component.
     * @param aHint         a hint providing more details about the checkbox. The hint will be added next to or under the checkbox.
     * @param aUIListeners  zero or more listeners, which will be notified whenever the selected value changes.
     *
     * @return this builder
     */
    JCheckBox addCheckBox(final String aPropertyName,
                          String aTitle,
                          boolean aDefaultValue,
                          String aTooltip,
                          String aHint,
                          final PropertyChangeListener... aUIListeners) {

      if (!fFirstRow) {
        separator();
      }
      fFirstRow = false;
      final JCheckBox checkBox = new JCheckBox();
      checkBox.setSelected(aDefaultValue);
      checkBox.setToolTipText(aTooltip);
      checkBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean selected = e.getStateChange() == ItemEvent.SELECTED;
          for (PropertyChangeListener listener : aUIListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, aPropertyName, !selected, selected));
          }
          if (fComponent != null) {
            fComponent.repaint();
          }
        }
      });
      JLabel label = new JLabel(aTitle);
      label.setToolTipText(aTooltip);
      MouseAdapter toggleOnClick = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          // Using released iso clicked to avoid ignoring a 1px mouse move between button down and up
          checkBox.setSelected(!checkBox.isSelected());
        }
      };
      label.addMouseListener(toggleOnClick);
      label.setBorder(BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0));
      row().left(label).right(checkBox).mouseListener(toggleOnClick).build();
      hint(aHint);
      fButtons.add(checkBox);
      return checkBox;
    }

    TextFieldComboBox addListBox(final String aPropertyName,
                                 String aTitle,
                                 Object aDefaultValue,
                                 String aTooltip,
                                 final Object[] aValues,
                                 final String[] aValueDescriptions,
                                 String aHint,
                                 final PropertyChangeListener... aUIListeners) {
      if (!fFirstRow) {
        separator();
      }
      fFirstRow = false;
      final TextFieldComboBox box = new TextFieldComboBox(12) {
        @Override
        protected void valueSelected(String aOldValue, String aValue) {
          int newIndex = Arrays.asList(aValueDescriptions).indexOf(aValue);
          int oldIndex = Arrays.asList(aValueDescriptions).indexOf(aOldValue);
          Object oldValue = oldIndex >= 0 ? aValues[oldIndex] : null;
          Object newValue = newIndex >= 0 ? aValues[newIndex] : null;
          for (PropertyChangeListener listener : aUIListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, aPropertyName, oldValue, newValue));
          }
          setText(aValue);
        }
      };
      box.setSearchContent(aValueDescriptions);
      box.setToolTipText(aTooltip);
      if (aDefaultValue != null) {
        List<Object> valueList = Arrays.asList(aValues);
        if (!valueList.contains(aDefaultValue)) {
          throw new IllegalArgumentException("Value not allowed: " + aDefaultValue);
        }
        box.setText(aValueDescriptions[valueList.indexOf(aDefaultValue)]);
      }
      JLabel label = new JLabel(aTitle);
      label.setBorder(BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0));
      row().left(label).right(box).build();
      hint(aHint);
      return box;
    }

    JList addList(final String aPropertyName,
                  String aTitle,
                  Object[] aValues,
                  String[] aValueDescriptions,
                  Object[] aDefaultValues,
                  int aVisibleRowCount,
                  String aTooltip,
                  String aHint,
                  final PropertyChangeListener... aUIListeners) {
      if (!fFirstRow) {
        separator();
      }
      fFirstRow = false;
      final JList list = new JList(aValues);
      list.setVisibleRowCount(aVisibleRowCount);
      if (aValueDescriptions != null) {
        list.setCellRenderer(new ListCellRenderer(aValues, aValueDescriptions));
      }
      if (aDefaultValues != null) {
        List defaultValues = Arrays.asList(aDefaultValues);
        for (int i = 0; i < aValues.length; i++) {
          if (defaultValues.contains(aValues[i])) {
            list.getSelectionModel().addSelectionInterval(i, i);
          }
        }
      }
      list.addListSelectionListener(new ListSelectionListener() {

        List fSelectedValues = list.getSelectedValuesList();

        @Override
        public void valueChanged(ListSelectionEvent e) {

          for (PropertyChangeListener listener : aUIListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, aPropertyName, fSelectedValues, list.getSelectedValuesList()));
          }
          fSelectedValues = list.getSelectedValuesList();
        }
      });
      list.setToolTipText(aTooltip);

      JLabel label = new JLabel(aTitle);
      label.setVerticalAlignment(JLabel.TOP);
      label.setBorder(BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0));
      JScrollPane listPane = new JScrollPane(list);
      listPane.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0),
          listPane.getBorder()));
      row().left(label).right(listPane).build();
      hint(aHint);

      return list;
    }

    JTextField addTextField(final String aPropertyName,
                            String aTitle,
                            String aDefaultValue,
                            String aTooltip,
                            final PropertyChangeListener... aUIListeners) {
      if (!fFirstRow) {
        separator();
      }

      final AtomicReference<String> oldValue = new AtomicReference<String>(aDefaultValue);
      final JTextField textField = new JTextField() {
        @Override
        public void setText(String t) {
          oldValue.set(getText());
          super.setText(t);
        }
      };
      textField.setColumns(12);
      textField.setToolTipText(aTooltip);

      // sets the value if we press enter
      final ActionListener valueSetter = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          for (PropertyChangeListener listener : aUIListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, aPropertyName, oldValue.get(), textField.getText()));
          }
          oldValue.set(textField.getText());
        }
      };
      textField.addActionListener(valueSetter);

      // sets the value if we jump away from the field
      textField.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
          valueSetter.actionPerformed(null);
        }
      });

      JLabel label = new JLabel(aTitle);
      label.setVerticalAlignment(JLabel.TOP);
      label.setBorder(BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0));
      row().left(label).right(textField).build();

      return textField;
    }

    class ListCellRenderer extends DefaultListCellRenderer {
      private Object[] fValues;
      private String[] fValueDescriptions;

      ListCellRenderer(Object[] aValues, String[] aValueDescriptions) {
        fValues = aValues;
        fValueDescriptions = aValueDescriptions;
      }

      public Component getListCellRendererComponent(JList aList, Object aVAlue, int aIndex, boolean aSelected, boolean aCellHasFocus) {
        Component c = super.getListCellRendererComponent(aList, aVAlue, aIndex, aSelected, aCellHasFocus);
        setText(getText(aVAlue));
        return c;
      }

      private String getText(Object aValue) {
        for (int i = 0; i < fValues.length; i++) {
          if (fValues[i].equals(aValue)) {
            return fValueDescriptions[i];
          }
        }
        return "";
      }

    }

    /**
     * Adds a new button group to the current panel, with the given list of options.
     *
     * @param aButtonType        the type of buttons to be created.
     * @param aPropertyName      the property name, used to send property change events.
     * @param aTitle             the title of the button group, used as display name in the UI.
     * @param aValues            the list of available values (one button will be created per value). These values will be passed
     *                           to the property change listener(s).
     * @param aButtons           extra buttons that should be appended after the buttons created in scope of {@code aValues}
     * @param aValueDescriptions the list of descriptions to be used in the UI as button name.
     * @param aDefaultValue      the default value
     * @param aTooltip           a tooltip for the button group. Will be displayed when the cursor lingers over the component.
     * @param aHint              a hint providing more details about the button group. The hint will be added next to or under the button group.
     * @param aUIListeners       zero or more listeners, which will be notified whenever the selected value changes.
     *      @return the array of buttons
     */
    AbstractButton[] addButtonGroup(ButtonType aButtonType,
                                    String aPropertyName,
                                    String aTitle,
                                    Object[] aValues,
                                    List<AbstractButton> aButtons,
                                    String[] aValueDescriptions,
                                    Object aDefaultValue,
                                    String aTooltip,
                                    String aHint,
                                    PropertyChangeListener... aUIListeners) {
      if (!fFirstRow) {
        separator();
      }
      fFirstRow = false;

      AbstractButton[] buttons = createButtonGroup(aButtonType, aPropertyName, aValues, aValueDescriptions, aDefaultValue, aTooltip, aUIListeners);
      JLabel label = new JLabel(aTitle);
      label.setToolTipText(aTooltip);
      label.setBorder(BorderFactory.createEmptyBorder(SPACE, 0, SPACE, 0));

      JPanel panel = new JPanel();
      panel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.fill = GridBagConstraints.VERTICAL;
      c.anchor = GridBagConstraints.LINE_START;
      for (AbstractButton button : buttons) {
        panel.add(button, c);
        c.gridx += 1;
      }

      if (aButtons != null) {
        for (AbstractButton button : aButtons) {
          panel.add(button, c);
          c.gridx += 1;
        }
      }

      if (aButtonType == ButtonType.RADIO_BUTTON) {
        row().left(label).right(panel).build();
      } else {
        row().span(label).build();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, SPACE, 0));
        row().span(left(panel)).build();
      }
      if (aHint != null) {
        hint(aHint);
      }
      return buttons;
    }

    private Component left(Component aPanel) {
      JPanel buttonBar = new JPanel(new BorderLayout());
      buttonBar.add(aPanel, BorderLayout.WEST);
      return buttonBar;
    }

    private AbstractButton[] createButtonGroup(ButtonType aButtonType,
                                               final String aPropertyName,
                                               final Object[] aValues,
                                               final String[] aValueDescriptions,
                                               final Object aDefaultValue,
                                               String aTooltip,
                                               final PropertyChangeListener... aUIListener) {
      final AbstractButton[] buttons = new AbstractButton[aValueDescriptions.length];
      ButtonGroup buttonGroup = new ButtonGroup();
      final AtomicReference<Object> oldValue = new AtomicReference<Object>();
      for (int i = 0; i < aValueDescriptions.length; i++) {
        final String option = aValueDescriptions[i];
        final Object value = aValues[i];
        AbstractButton button = aButtonType == ButtonType.RADIO_BUTTON ? createUnderlinedButton(option) : new JToggleButton(option);
        button.setSelected(value.equals(aDefaultValue));
        button.setActionCommand(option);
        button.setToolTipText(aTooltip);
        button.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              if (aUIListener != null) {
                for (PropertyChangeListener uiListener : aUIListener) {
                  uiListener.propertyChange(new PropertyChangeEvent(this, aPropertyName, oldValue.get(), value));
                }
              }
              oldValue.set(value);
              if (fComponent != null) {
                fComponent.repaint();
              }
            }
          }
        });
        buttonGroup.add(button);
        buttons[i] = button;
        fButtons.add(button);
      }
      return buttons;
    }

    private PanelBuilder hint(String aHint) {
      JLabel hintLabel = new JLabel(aHint);
      Font font = hintLabel.getFont();
      hintLabel.setFont(font.deriveFont(Font.ITALIC, font.getSize() - 1));
      hintLabel.setForeground(UIColors.fgHint());
      row().span(hintLabel).build();
      return this;
    }

    private void separator() {
      JComponent component = new JPanel();
      component.setPreferredSize(new Dimension(0, 1));
      component.setBorder(BorderFactory.createLineBorder(UIColors.mid(component.getBackground(), Color.BLACK, 0.1), 1));
      fPanel.add(component);
    }

    private RowBuilder row() {
      return new RowBuilder();
    }

    private class RowBuilder {

      private JPanel fRowPanel = new JPanel(new BorderLayout());

      public RowBuilder span(Component aComponent) {
        fRowPanel.add(aComponent, BorderLayout.CENTER);
        return this;
      }

      public RowBuilder left(Component aComponent) {
        fRowPanel.add(aComponent, BorderLayout.WEST);
        return this;
      }

      public RowBuilder right(Component aComponent) {
        fRowPanel.add(aComponent, BorderLayout.EAST);
        return this;
      }

      public RowBuilder mouseListener(MouseListener aMouseListener) {
        fRowPanel.addMouseListener(aMouseListener);
        return this;
      }

      public void build() {

        fPanel.add(fRowPanel);
      }
    }

  }

  /**
   * Similar in behavior to a toggle button, but visually different:
   * - It does not paint the regular border or fill
   * - It underlines the selected item
   */
  public static AbstractButton createUnderlinedButton(String aName) {
    JToggleButton radio = new JToggleButton(aName);
    UnderlineSelectedBorder border = new UnderlineSelectedBorder();
    radio.setBorder(border);
    radio.addMouseListener(border);
    radio.setContentAreaFilled(false);
    // some Look&Feels use background color as foreground when disabled&selected, and use a darker
    // background. As we've disabled filling the content area, we need a different color to make
    // sure the text is readable.
    radio.setBackground(UIColors.getUIColor("ToggleButton.disabledText", UIColors.fgHint()));
    radio.setFocusPainted(false);
    Font font = radio.getFont();
    radio.setFont(font.deriveFont((float) font.getSize() - 1));
    return radio;
  }

  /**
   * Border that draws a line at the bottom when the associated button is selected.
   */
  private static class UnderlineSelectedBorder extends MouseAdapter implements Border {
    private static final int PAD = 5; //px
    private static final int LINE_SIZE = 2; //px, must be < PAD

    private boolean fHoover = false;

    @Override
    public void mouseEntered(MouseEvent e) {
      fHoover = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
      fHoover = false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      AbstractButton b = (AbstractButton) c;
      if (b.isSelected() || fHoover) {
        // The insets provide space for the border. Draw the line below the text, but at the
        // top of the available space
        Insets insets = b.getInsets();
        int lineLength = width - insets.left - insets.right;
        Rectangle line = new Rectangle(x + insets.left, y + height - insets.bottom, lineLength, LINE_SIZE);

        // Draw line in an accent color
        Graphics2D g2d = (Graphics2D) g;
        Color color = UIColors.fgAccent();
        if (!b.isSelected()) { // create transparent color when hoovering but not selected
          color = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
        }
        g2d.setColor(color);
        g2d.fill(line);
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(PAD, PAD, PAD, PAD);
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }
  }

  static enum ButtonType {
    RADIO_BUTTON, TOGGLE_BUTTON
  }

}
