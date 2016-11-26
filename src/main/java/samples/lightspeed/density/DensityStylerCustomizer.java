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
package samples.lightspeed.density;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.gxy.common.TitledPanel;

/**
 * Creates a GUI component for customizing the properties of a density layer. The component allows
 * changing the density plot mode between "world-sized" and "pixel-sized", as well as modifying the
 * hardness and width of the density plots.
 */
public class DensityStylerCustomizer extends JPanel {

  private static final int PREFERRED_SPINNER_WIDTH = 80;
  /**
   * The density styler to be customized.
   */
  private final DensityStyler fStyler;
  /**
   * Combobox to choose the current density style.
   */
  private JComboBox fComboBox;
  /**
   * Spinner controlling the hardness of the current density style
   */
  private JSpinner fHardnessSpinner;
  /**
   * Spinner controlling the width of the current density style
   */
  private JSpinner fWidthSpinner;
  /**
   * SpinnerNumberModel for fWidthSpinner
   */
  private SpinnerNumberModel fNumberModel;
  /**
   * Object listening to actions from the combobox or state changes from the spinners, to update the
   * customizer accordingly.
   */
  private final Updater fUpdater;

  /**
   * Creates a new customizer for the given layer.
   *
   * @param aLabel                    Title of this customizer's GUI component.
   * @param aDensityStyler            The density styler to be customized
   * @param aSupportedStyleTypes      A vector of supported density style types.
   */
  public DensityStylerCustomizer(
      String aLabel,
      DensityStyler aDensityStyler,
      Vector<DensityStyleType> aSupportedStyleTypes) {
    setLayout(new BorderLayout());
    fStyler = aDensityStyler;
    // Create components for the customizer.
    fComboBox = new JComboBox(aSupportedStyleTypes);
    fComboBox.setSelectedItem(fStyler.getDensityStyleType());
    initSpinners();
    //Adds a titled panel with all GUI components to this customizer
    JPanel panel = createTitledPanel(aLabel);
    add(panel, BorderLayout.CENTER);
    //Creates updater object and attaches
    //it to combobox and spinners.
    fUpdater = new Updater(this);
    fComboBox.addActionListener(fUpdater);
    addListenerToSpinners();
  }

  /**
   * Creates a titled JPanel with all GUI components of this customizer
   *
   * @param aName the title
   *
   * @return a titled JPanel with all GUI components of this customizer
   */
  private JPanel createTitledPanel(String aName) {
    JPanel panel = new JPanel();
    panel.setName(aName);
    panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    panel.add(new JLabel("Width"));
    panel.add(fWidthSpinner);
    panel.add(new JLabel("Hardness"));
    panel.add(fHardnessSpinner);

    panel.add(fComboBox);
    panel = TitledPanel.createTitledPanel(aName, panel);
    panel.doLayout();
    return panel;
  }

  /**
   * Initializes the spinners of this customizer
   */
  private void initSpinners() {
    // Spinner for the hardness parameter.
    SpinnerNumberModel hardnessNumberModel = new SpinnerNumberModel();
    hardnessNumberModel.setMaximum(1.0);
    hardnessNumberModel.setMinimum(0.0);
    hardnessNumberModel.setStepSize(0.05);
    hardnessNumberModel.setValue(fStyler.getCurrentHardness());
    fHardnessSpinner = new JSpinner(hardnessNumberModel) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(PREFERRED_SPINNER_WIDTH, size.width);
        return size;
      }
    };
    // Spinner for the width parameter.
    fNumberModel = new SpinnerNumberModel(fStyler.getCurrentWidth(), fStyler.getMinimumWidth(), fStyler.getMaximumWidth(), getStepSize(fStyler.getMinimumWidth(), fStyler.getMaximumWidth()));
    fWidthSpinner = new JSpinner(fNumberModel) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(PREFERRED_SPINNER_WIDTH, size.width);
        return size;
      }
    };
  }

  /**
   * Returns the customized DensityStyler
   *
   * @return the customized DensityStyler
   */
  private DensityStyler getStyler() {
    return fStyler;
  }

  /**
   * Switches style type and update gui components.
   */
  private void switchStyleType() {
    fStyler.setDensityStyleType((DensityStyleType) fComboBox.getSelectedItem());
    updateGUIComponents();
  }

  /**
   * Updates the customizer components to reflect the given styler.
   */
  private void updateGUIComponents() {
    removeListenerFromSpinners();
    //updates the spinner values based on the provided styler
    fHardnessSpinner.setValue(fStyler.getCurrentHardness());
    fNumberModel.setMaximum(fStyler.getMaximumWidth());
    fNumberModel.setMinimum(fStyler.getMinimumWidth());
    fNumberModel.setStepSize(getStepSize(fStyler.getMinimumWidth(), fStyler.getMaximumWidth()));
    fWidthSpinner.setValue(fStyler.getCurrentWidth());
    addListenerToSpinners();
  }

  /**
   * Adds the updater as listener to the spinners.
   */
  private void addListenerToSpinners() {
    fHardnessSpinner.addChangeListener(fUpdater);
    fWidthSpinner.addChangeListener(fUpdater);
  }

  /**
   * Removes the updater as listener from the spinners.
   */
  private void removeListenerFromSpinners() {
    fHardnessSpinner.removeChangeListener(fUpdater);
    fWidthSpinner.removeChangeListener(fUpdater);
  }

  private double getStepSize(double aMinValue, double aMaxValue) {
    double diff = aMaxValue - aMinValue;
    int exponent = (int) Math.ceil(Math.log10(diff));
    double upper = Math.pow(10, exponent);
    return upper / (diff <= upper / 2.0 ? 50.0 : 20.0);
  }

  /**
   * private inner class to which listens to the customizer's GUI components to update the current
   * style whenever needed.
   */
  private static class Updater implements ActionListener, ChangeListener {
    //Weak reference to customizer
    private final WeakReference<DensityStylerCustomizer> fCustomizerRef;

    /**
     * Creates a new updater for the given customizer
     * @param aCustomizer the given customizer
     */
    public Updater(DensityStylerCustomizer aCustomizer) {
      fCustomizerRef = new WeakReference<DensityStylerCustomizer>(aCustomizer);
    }

    /**
     * Retrieves the customizer for this updater
     * @return the customizer for this updater
     */
    private DensityStylerCustomizer getCustomizer() {
      DensityStylerCustomizer result = fCustomizerRef.get();
      if (result == null) {
        throw new IllegalStateException("Reference to DensityLayerCustomizer no longer valid");
      }
      return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      DensityStylerCustomizer customizer = getCustomizer();
      if (e.getSource().equals(customizer.fComboBox)) {
        // If the mode combo box is changed, update the density styler
        customizer.switchStyleType();
      }
    }

    /**
     * Responds to a change of the hardness and width parameters.
     *
     * @param e event fired by the hardness or width spinner
     */
    @Override
    public void stateChanged(ChangeEvent e) {
      DensityStylerCustomizer customizer = getCustomizer();
      DensityStyler currentStyler = customizer.getStyler();
      if (e.getSource().equals(customizer.fHardnessSpinner)) {
        double hardness = (Double) customizer.fHardnessSpinner.getValue();
        currentStyler.setHardness(hardness);
      } else if (e.getSource().equals(customizer.fWidthSpinner)) {
        double width = (Double) customizer.fWidthSpinner.getValue();
        currentStyler.setWidth(width);
      }
    }
  }
}
