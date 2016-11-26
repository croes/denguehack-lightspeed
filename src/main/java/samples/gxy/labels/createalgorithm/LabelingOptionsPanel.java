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
package samples.gxy.labels.createalgorithm;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYMultiLabelPriorityProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;

import samples.gxy.common.TitledPanel;

/**
 * Switches between and configures the default labeling algorithm and a sample algorithm.
 */
class LabelingOptionsPanel extends JPanel {

  private ILcdGXYView fView;
  private TLcdGXYLabelPlacer fPlacer;

  private JRadioButton fRadioButton1;
  private JRadioButton fRadioButton2;
  private JCheckBox fCheckBox2;
  private JCheckBox fCheckBox3;
  private JSlider fSlider;

  public LabelingOptionsPanel(ILcdGXYView aView, TLcdGXYLabelPlacer aPlacer) {
    fView = aView;
    fPlacer = aPlacer;
    aPlacer.setAlgorithm(createSampleLabelingAlgorithm());

    setLayout(new GridLayout(3, 1));

    // Create a panel for the algorithm
    JPanel algorithmPanel = new JPanel(new GridLayout(2, 1));
    algorithmPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    algorithmPanel.add(fRadioButton1 = new JRadioButton("Sample Labeling", true));
    fRadioButton1.addItemListener(new MyItemListener());
    fRadioButton1.setToolTipText("Places labels on the domain object's anchor point.");

    algorithmPanel.add(fRadioButton2 = new JRadioButton("Default Labeling", false));
    fRadioButton2.addItemListener(new MyItemListener());
    fRadioButton2.setToolTipText("Places labels on various positions around the domain object's anchor point.");

    ButtonGroup group = new ButtonGroup();
    group.add(fRadioButton1);
    group.add(fRadioButton2);

    add(TitledPanel.createTitledPanel("Label Algorithm", algorithmPanel));

    // Create a panel for the wrappers
    JPanel wrapperPanel = new JPanel(new GridLayout(2, 1));

    wrapperPanel.add(fCheckBox2 = new JCheckBox("More Positions", false));
    fCheckBox2.addItemListener(new MyItemListener());
    fCheckBox2.setToolTipText("Activates an algorithm wrapper that adds extra locations to place labels on.");

    wrapperPanel.add(fCheckBox3 = new JCheckBox("Modify Label Detail", false));
    fCheckBox3.addItemListener(new MyItemListener());
    fCheckBox3.setToolTipText("Activates an algorithm wrapper that changes the number of displayed properties in the label and the font size to allow placing more labels.");

    add(TitledPanel.createTitledPanel("Label Options", wrapperPanel));

    // Create a panel for the rotation wrapper
    JPanel rotationPanel = new JPanel(new GridLayout(1, 1));

    rotationPanel.add(fSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 0));
    fSlider.setLabelTable(fSlider.createStandardLabels(90));
    fSlider.setMajorTickSpacing(90);
    fSlider.setMinorTickSpacing(45);
    fSlider.setPaintLabels(true);
    fSlider.setPaintTicks(true);
    fSlider.setToolTipText("Drag to change the rotation of all labels.");
    fSlider.addChangeListener(new MyChangeListener());

    add(TitledPanel.createTitledPanel("Label Rotation", rotationPanel));
  }

  private class MyItemListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      clearLabelLocations();
      setAlgorithm();
    }
  }

  private class MyChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      setAlgorithm();
    }
  }

  private void setAlgorithm() {
    ALcdGXYDiscretePlacementsLabelingAlgorithm algorithm;
    if (fRadioButton1.isSelected()) {
      algorithm = createSampleLabelingAlgorithm();
    } else if (fRadioButton2.isSelected()) {
      algorithm = createDefaultLabelingAlgorithm();
    } else {
      return;
    }

    if (fCheckBox3.isSelected()) {
      algorithm = new LabelDetailAlgorithmWrapper(algorithm);
    }

    if (fCheckBox2.isSelected()) {
      algorithm = new MorePositionsAlgorithmWrapper(algorithm);
    }

    double rotation = (double) fSlider.getValue();
    algorithm = new RotationAlgorithmWrapper(algorithm, rotation);

    fPlacer.setAlgorithm(algorithm);
    fView.invalidate(true, this, "Changed the labeling algorithm.");
  }

  private void clearLabelLocations() {
    Enumeration layers = fView.layers();
    while (layers.hasMoreElements()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();
      if (layer instanceof TLcdGXYLayer) {
        TLcdGXYLayer labelsLayer = (TLcdGXYLayer) layer;
        // When the labeling algorithm is changed, remove the previous label locations.
        labelsLayer.setLabelLocations(new TLcdLabelLocations(layer, new LabelDetailLabelLocation()));
      }
    }
  }

  private ALcdGXYDiscretePlacementsLabelingAlgorithm createSampleLabelingAlgorithm() {
    SampleLabelingAlgorithm algorithm = new SampleLabelingAlgorithm();
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider());
    return algorithm;
  }

  private ALcdGXYDiscretePlacementsLabelingAlgorithm createDefaultLabelingAlgorithm() {
    TLcdGXYLocationListLabelingAlgorithm algorithm = new TLcdGXYLocationListLabelingAlgorithm();
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider());
    return algorithm;
  }

  private ILcdGXYMultiLabelPriorityProvider createLabelPriorityProvider() {
    // This label priority provider creates priorities based on the population of a city.
    return new ILcdGXYMultiLabelPriorityProvider() {
      public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
        if (aObject instanceof ILcdDataObject) {
          ILcdDataObject data_object = (ILcdDataObject) aObject;
          try {
            Object value = data_object.getValue("TOT_POP");
            if (value != null) {
              Integer population = (Integer) value;
              return Integer.MAX_VALUE - population;
            }
          } catch (IllegalArgumentException e) {
            return Integer.MAX_VALUE;
          }
        }
        return Integer.MAX_VALUE;
      }
    };
  }
}
