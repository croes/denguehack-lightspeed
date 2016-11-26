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
package samples.lightspeed.demo.application.data.lighting;

import static samples.lightspeed.demo.application.data.lighting.LightingModel.Mode;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.swing.TLcdSWAction;

import samples.common.HaloLabel;
import samples.lightspeed.common.AngleControlComponent;
import samples.lightspeed.demo.framework.gui.DemoUIColors;

/**
 * Panel to control the lighting in a LuciadLightspeed view. The code to actually change the
 * lighting of the view is in {@link LightingModel}.
 */
public class LightingPanelBuilder {
  private final List<LightingModel> fLightingModels;

  private JRadioButton fAutoLightButton;
  private AngleControlComponent fAutoLightYawControl;
  private AngleControlComponent fAutoLightPitchControl;
  private JRadioButton fTimeOfDayLightButton;
  private JSlider fTimeOfDaySlider;
  private JRadioButton fNoLightButton;
  private HaloLabel fTimeLabel;

  public LightingPanelBuilder(List<LightingModel> aLightingModels) {
    fLightingModels = aLightingModels;

    // Create GUI
    ButtonGroup group = new ButtonGroup();
    fAutoLightButton = new JRadioButton(new TLcdSWAction(new LightingModeAction("", Mode.AUTO)));
    fAutoLightButton.setToolTipText("A light that follows the camera");
    fAutoLightButton.getModel().setGroup(group);
    fAutoLightYawControl = new AngleControlComponent(AngleControlComponent.Type.SEGMENT, -180.0, 0.0);
    fAutoLightYawControl.setPreferredSize(new Dimension(100, 50));
    ChangeListener autoLightChangeListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        for (LightingModel lightingModel : fLightingModels) {
          lightingModel.setAutoLightOrientation(270.0 - fAutoLightYawControl.getAngle(), -fAutoLightPitchControl.getAngle());
        }
      }
    };
    fAutoLightYawControl.addChangeListener(autoLightChangeListener);
    fAutoLightPitchControl = new AngleControlComponent(AngleControlComponent.Type.SEGMENT, 0.0, 90.0);
    fAutoLightPitchControl.setPreferredSize(new Dimension(50, 50));
    fAutoLightPitchControl.addChangeListener(autoLightChangeListener);
    fTimeOfDayLightButton = new JRadioButton(new TLcdSWAction(new LightingModeAction("", Mode.TIME_OF_DAY)));
    fTimeOfDayLightButton.setToolTipText("A sun light");
    fTimeOfDayLightButton.getModel().setGroup(group);

    fTimeLabel = new HaloLabel("");

    fTimeOfDaySlider = new JSlider(new DefaultBoundedRangeModel(0, 0, 0, 24));
    fTimeOfDaySlider.setToolTipText("The PDT time of the day");
    fTimeOfDaySlider.setPaintLabels(true);
    fTimeOfDaySlider.setPaintTicks(true);
    fTimeOfDaySlider.setSnapToTicks(true);

    fTimeOfDaySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int timeOfDay = fTimeOfDaySlider.getValue();
        String amOrPm = "a.m.";
        //We use PDT instead of GTM. PDT = GTM -7
        for (LightingModel lightingModel : fLightingModels) {
          lightingModel.setTimeOfDayLightOrientation(timeOfDay - 7);
        }

        if (timeOfDay >= 12 && timeOfDay != 24) {
          amOrPm = "p.m.";
        }

        // 12a.m. is midnight, 12 p.m. is noon.
        timeOfDay %= 12;
        if (timeOfDay == 0) {
          timeOfDay = 12;
        }

        fTimeLabel.setText(timeOfDay + " " + amOrPm);
      }
    });
    fNoLightButton = new JRadioButton(new TLcdSWAction(new LightingModeAction("", Mode.OFF)));
    fNoLightButton.setToolTipText("No light");
    fNoLightButton.getModel().setGroup(group);

    fAutoLightYawControl.setAngle(-90.0);
    fAutoLightPitchControl.setAngle(45.0);
    fTimeOfDaySlider.setValue(12);
    fTimeLabel.setText("12 p.m.");
    fAutoLightButton.setSelected(true);
    setLightMode(fLightingModels.get(0).getMode());

  }

  public JPanel createContentPanel(String aTitle, boolean aIsTouchUI) {
    DefaultFormBuilder builder;
    builder = new DefaultFormBuilder(new FormLayout("l:p"));

    if (aIsTouchUI) {
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel(aTitle, 15, true) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(240, 25);
      }
    };
    builder.append(titleLabel);
    builder.nextLine();

    combineAndAdd(builder, fAutoLightButton, new HaloLabel("Auto"));
    builder.nextLine();

    combineAndAdd(builder, fAutoLightYawControl, fAutoLightPitchControl);
    builder.nextLine();

    combineAndAdd(builder, fTimeOfDayLightButton, new HaloLabel("Time of day (PDT)"));
    builder.nextLine();

    combineAndAdd(builder, fTimeOfDaySlider, fTimeLabel);
    builder.nextLine();

    combineAndAdd(builder, fNoLightButton, new HaloLabel("Off"));
    builder.nextLine();

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    return contentPanel;
  }

  private static void combineAndAdd(DefaultFormBuilder aBuilder, Component aComponent1, Component aComponent2) {
    JPanel panel = new JPanel();
    panel.setBackground(DemoUIColors.TRANSPARENT);
    panel.add(aComponent1);
    panel.add(aComponent2);
    aBuilder.append(panel);
  }

  public void setLightMode(Mode aMode) {
    for (LightingModel lightingModel : fLightingModels) {
      lightingModel.setMode(aMode);
    }
    fAutoLightYawControl.setEnabled(aMode == Mode.AUTO);
    fAutoLightPitchControl.setEnabled(aMode == Mode.AUTO);
    fTimeOfDaySlider.setEnabled(aMode == Mode.TIME_OF_DAY);
  }

  private class LightingModeAction extends ALcdAction {

    private final Mode fMode;

    public LightingModeAction(String aName, Mode aMode) {
      super(aName);
      fMode = aMode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setLightMode(fMode);
    }
  }
}
