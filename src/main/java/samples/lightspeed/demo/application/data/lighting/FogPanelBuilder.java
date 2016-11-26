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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.gui.DemoUIColors;

public class FogPanelBuilder {
  private final List<FogModel> fFogModels;

  private final JCheckBox fFogCheckBox;
  final private JSlider fFogVisibilityAtMinAltitude;
  private final HaloLabel fVisibilityDistanceLabel;

  public FogPanelBuilder(List<FogModel> aFogModels) {
    fFogModels = aFogModels;

    fFogCheckBox = new JCheckBox();
    fFogCheckBox.setOpaque(false);
    fFogCheckBox.getModel().addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        for (FogModel fogModel : fFogModels) {
          fogModel.setFogEnabled(fFogCheckBox.isSelected());
        }

      }
    });

    fFogVisibilityAtMinAltitude = new JSlider();
    fFogVisibilityAtMinAltitude.setOpaque(false);
    fFogVisibilityAtMinAltitude.setMinimum(2000);
    fFogVisibilityAtMinAltitude.setMaximum(100000);
    fFogVisibilityAtMinAltitude.setValue((int) fFogModels.get(0).getFogVisibilityAtMinAltitude());
    fFogVisibilityAtMinAltitude.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent aChangeEvent) {
        for (FogModel fogModel : fFogModels) {
          fogModel.setFogVisibilityAtMinAltitude(fFogVisibilityAtMinAltitude.getValue());
        }
        fVisibilityDistanceLabel.setText(fFogVisibilityAtMinAltitude.getValue() / 1000 + "km");
      }
    });
    fVisibilityDistanceLabel = new HaloLabel(fFogVisibilityAtMinAltitude.getValue() / 1000 + "km");

    fFogCheckBox.setSelected(fFogModels.get(0).isFogEnabled());
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
        return new Dimension(200, 25);
      }
    };
    builder.append(titleLabel);
    builder.nextLine();

    builder.append(new HaloLabel("Visibility"));
    builder.nextLine();

    combineAndAdd(builder, fFogVisibilityAtMinAltitude, fVisibilityDistanceLabel);
    builder.nextLine();

    JPanel panel = new JPanel();
    panel.setBackground(DemoUIColors.TRANSPARENT);
    panel.add(fFogCheckBox);
    panel.add(new HaloLabel("Enabled"));
    builder.append(panel);
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

}
