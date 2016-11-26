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
package samples.realtime.common;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.TLcdSimulatorCPUUsageSlider;
import com.luciad.realtime.TLcdSimulatorStatusLabel;
import com.luciad.realtime.TLcdSimulatorTimeLabel;
import com.luciad.realtime.TLcdSimulatorTimeSlider;

import samples.gxy.common.TitledPanel;

/**
 * This is a JPanel that contains all the controls for the simulation. How
 * these controls can be used is explained in the MainPanel.
 */
public class SimulatorControlPanel extends JPanel implements PropertyChangeListener {

  private TLcdSimulator fSimulator;

  // The sliders used in the panel
  private TLcdSimulatorTimeSlider fSimulatorTimeSlider = new TLcdSimulatorTimeSlider();
  private JSlider fSpeedSlider = new JSlider();
  private TLcdSimulatorCPUUsageSlider fCPUUsageSlider = new TLcdSimulatorCPUUsageSlider();

  // The labels used in the panel
  private TLcdSimulatorTimeLabel fSimulatorLabel = new TLcdSimulatorTimeLabel();
  private JLabel fSpeedLabel = new JLabel();
  private JLabel fCPUUsageLabel = new JLabel();

  // The buttons used in the panel
  private JButton fRunPauseButton = new JButton();
  private JButton fStopButton = new JButton();
  private TLcdSimulatorStatusLabel fSimulatorStatusLabel
      = new TLcdSimulatorStatusLabel();

  /**
   * Constructs a new SimulatorControlPanel.
   */
  public SimulatorControlPanel() {
    initGUI();
  }

  /**
   * Sets the TLcdSimulator that is controlled by this panel.
   *
   * @param aSimulator The TLcdSimulator that will be controlled by this panel.
   */
  public void setSimulator(TLcdSimulator aSimulator) {
    fSimulator = aSimulator;
    fSimulator.addPropertyChangeListener(this);
    fSimulator.setMaxCPUUsage(60);
    fSimulatorTimeSlider.setSimulator(fSimulator);
    fSimulatorLabel.setSimulator(fSimulator);
    fSimulatorStatusLabel.setSimulator(fSimulator);
    fCPUUsageSlider.setSimulator(fSimulator);

    initAfterSimulator();
  }

  public void setTimeFactor(double aTimeFactor) {
    fSpeedSlider.setValue((int) aTimeFactor);
  }

  /**
   * Sets the values for those items that are not set by the simulator.
   */
  private void initAfterSimulator() {
    setTimeFactor(100);
  }

  /**
   * Constructs the actual panel.
   */
  private void initGUI() {
    JPanel panel_button = new JPanel(new GridLayout(1, 3));
    fSimulatorStatusLabel.setHorizontalAlignment(JLabel.CENTER);
    fRunPauseButton.setFont(new Font("Dialog", Font.PLAIN, 9));
    fRunPauseButton.setText("Pause");
    fRunPauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fRunPauseButton_actionPerformed(e);
      }
    });
    fRunPauseButton.setMaximumSize(fRunPauseButton.getPreferredSize());
    fRunPauseButton.setMinimumSize(fRunPauseButton.getPreferredSize());
    fRunPauseButton.setPreferredSize(fRunPauseButton.getPreferredSize());
    fRunPauseButton.setText("Run");
    panel_button.add(fRunPauseButton);

    fStopButton.setFont(new Font("Dialog", 0, 9));
    fStopButton.setText("Stop");
    fStopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fStopButton_actionPerformed(e);
      }
    });
    panel_button.add(fStopButton);
    panel_button.add(fSimulatorStatusLabel);

    JPanel panel_time = new JPanel(new BorderLayout());
    panel_time.add(BorderLayout.WEST, panel_button);
    panel_time.add(BorderLayout.CENTER, fSimulatorTimeSlider);
    panel_time.add(BorderLayout.EAST, fSimulatorLabel);

    JPanel panel_cpu = new JPanel(new BorderLayout());
    fCPUUsageSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fCPUUsageSlider_stateChanged(e);
      }
    });
    fCPUUsageSlider.setPaintTicks(true);
    fCPUUsageSlider.setMajorTickSpacing(10);
    fCPUUsageSlider.setMinorTickSpacing(5);
    panel_cpu.add(BorderLayout.CENTER, fCPUUsageSlider);
    panel_cpu.add(BorderLayout.EAST, fCPUUsageLabel);

    JPanel panel_speedup = new JPanel(new BorderLayout());
    fSpeedSlider.setMinimum(0);
    fSpeedSlider.setMaximum(1000);
    fSpeedSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fSpeedSlider_stateChanged(e);
      }
    });
    fSpeedSlider.setPaintTicks(true);
    fSpeedSlider.setMajorTickSpacing(100);
    fSpeedSlider.setMinorTickSpacing(50);
    panel_speedup.add(BorderLayout.CENTER, fSpeedSlider);
    panel_speedup.add(BorderLayout.EAST, fSpeedLabel);

    JPanel panel_speedup_cpu = new JPanel(new GridLayout(1, 2));
    panel_speedup_cpu.add(TitledPanel.createTitledPanel("% CPU", panel_cpu, TitledPanel.NORTH | TitledPanel.EAST));
    panel_speedup_cpu.add(TitledPanel.createTitledPanel("Speed-Up", panel_speedup));

    this.setLayout(new GridLayout(2, 1));
    this.add(TitledPanel.createTitledPanel("Time", panel_time));
    this.add(panel_speedup_cpu);
  }

  /**
   * Performs the right action on the simulator depending on whether the
   * button was in run or pause mode.
   */
  private void fRunPauseButton_actionPerformed(ActionEvent ev) {
    switch (fSimulator.getStatus()) {
    case TLcdSimulator.STOPPED:
    case TLcdSimulator.PAUSING:
    case TLcdSimulator.ENDED:
      fSimulator.run();
      break;
    case TLcdSimulator.RUNNING:
      fSimulator.pause();
      break;
    }
    updateRunPauseButton();
  }

  /**
   * Updates the text on the Run/Pause button depending on the current status
   * of the TLcdSimulator.
   */
  private void updateRunPauseButton() {
    switch (fSimulator.getStatus()) {
    case TLcdSimulator.STOPPED:
      fRunPauseButton.setText("Run");
      break;
    case TLcdSimulator.PAUSING:
      fRunPauseButton.setText("Run");
      break;
    case TLcdSimulator.ENDED:
      fRunPauseButton.setText("Run");
      break;
    case TLcdSimulator.RUNNING:
      fRunPauseButton.setText("Pause");
      break;
    }
  }

  /**
   * Stops the simulator and updates the Run/Pause button.
   */
  void fStopButton_actionPerformed(ActionEvent e) {
    this.fSimulator.stop();
    updateRunPauseButton();
  }

  /**
   * Tries to set the new time factor to the simulator and updates the label giving
   * the value of the slider.
   */
  private void fSpeedSlider_stateChanged(ChangeEvent e) {
    try {
      double new_time_factor = (double) fSpeedSlider.getValue();
      fSimulator.setTimeFactor(new_time_factor);
    } catch (Exception ex) {
      double current_time_factor = fSimulator.getTimeFactor();
      fSpeedSlider.setValue((int) current_time_factor);
    }
    fSpeedLabel.setText(Integer.toString(fSpeedSlider.getValue()));
  }

  /**
   * Updates the label giving the value of the slider (setting the maxCPUUsage
   * of the simulator is already done by TLcdSimulatorCPUUsageSlider itself).
   */
  private void fCPUUsageSlider_stateChanged(ChangeEvent e) {
    fCPUUsageLabel.setText(Integer.toString(fCPUUsageSlider.getValue()));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    updateRunPauseButton();
  }
}
