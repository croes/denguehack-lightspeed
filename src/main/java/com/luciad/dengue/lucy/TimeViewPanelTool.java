package com.luciad.dengue.lucy;

import com.luciad.lucy.gui.ALcyApplicationPaneTool;
import com.luciad.lucy.util.properties.ALcyProperties;

import javax.swing.*;
import java.awt.*;

import samples.lightspeed.timeview.TimeSlider;

/**
 * Created by tomc on 23/05/2016.
 */
public class TimeViewPanelTool extends ALcyApplicationPaneTool {

  private JPanel viewPanel;
  private TimeSlider timeSlider;

  public TimeViewPanelTool(ALcyProperties aProperties,
                           String aLongPrefix,
                           String aShortPrefix) {
    super(aProperties, aLongPrefix + "time.", aShortPrefix + "time.");

    timeSlider = new TimeSlider();
    viewPanel = new JPanel();
    viewPanel.setLayout(new BoxLayout(viewPanel,BoxLayout.X_AXIS ));
    viewPanel.add(timeSlider);
  }

  @Override
  protected Component createContent() {
    return viewPanel;
  }
}
