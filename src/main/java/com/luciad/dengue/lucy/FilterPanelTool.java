package com.luciad.dengue.lucy;

import com.luciad.dengue.ui.ColorPalette;
import com.luciad.dengue.view.DengueFilter;
import com.luciad.gui.swing.TLcdRangeSlider;
import com.luciad.lucy.gui.ALcyApplicationPaneTool;
import com.luciad.lucy.util.properties.ALcyProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class FilterPanelTool extends ALcyApplicationPaneTool {

  private final JPanel sidePanel;

  public FilterPanelTool(ALcyProperties aProperties,
                         String aLongPrefix,
                         String aShortPrefix,
                         DengueFilter aDengueFilter) {

    super(aProperties, aLongPrefix, aShortPrefix);
    sidePanel = createSidePanel(aDengueFilter);
  }

  @Override
  protected Component createContent() {
    return sidePanel;
  }

  private static JPanel createParameterFilter(final String aParameterName, final DengueFilter aDengueFilter) {
    TLcdRangeSlider slider = new TLcdRangeSlider(0.0, 100.0);

//    JCheckBox checkBox = new JCheckBox();
//    checkBox.setText(aParameterName);
//    checkBox.setSelected(true);
//    checkBox.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        boolean selected = checkBox.isSelected();
//        double min = slider.getMinimum();
//        double max = slider.getMaximum();
//        aDengueFilter.updateFilter(aParameterName, selected, min, max);
//      }
//    });

    JLabel checkBox = new JLabel();
    checkBox.setText(aParameterName);


    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
//        boolean selected = checkBox.isSelected();
        boolean selected = true;
        double min = slider.getMinimum();
        double max = slider.getMaximum();
        aDengueFilter.updateFilter(aParameterName, selected, min, max);
      }
    });

    TextField minField = new TextField("0", 1);
    minField.setBackground(ColorPalette.background);
    minField.setText("0");
    TextField maxField = new TextField("100", 1);
    maxField.setBackground(ColorPalette.background);
    maxField.setText("100");

    JPanel sliderPanel = new JPanel();
    sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
    sliderPanel.add(minField);
    sliderPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    sliderPanel.add(slider);
    sliderPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    sliderPanel.add(maxField);

    JPanel titlePanel = new JPanel();
//    titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//    titlePanel.add(Box.createRigidArea(new Dimension(100, 0)));
    titlePanel.add(checkBox);

    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
    filterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    filterPanel.add(titlePanel);
    filterPanel.add(sliderPanel);

    return filterPanel;
  }

  public static JPanel createSidePanel(DengueFilter aDengueFilter) {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    List<String> parameterNames = aDengueFilter.getParameterNames();
    for (String parameterName : parameterNames) {
      mainPanel.add(createParameterFilter(parameterName, aDengueFilter));
    }

    return mainPanel;
  }
}
