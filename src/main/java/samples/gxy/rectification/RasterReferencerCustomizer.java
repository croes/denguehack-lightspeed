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
package samples.gxy.rectification;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.luciad.format.raster.reference.ILcdRasterReferencer;
import com.luciad.format.raster.reference.TLcdPolynomialRasterReferencer;
import com.luciad.format.raster.reference.TLcdProjectiveRasterReferencer;
import com.luciad.format.raster.reference.TLcdRationalRasterReferencer;

/**
 * This JPanel can be used to display and adjust the properties of an ILcdRasterReferencer.
 */
public class RasterReferencerCustomizer extends JPanel {

  private static final String PROJECTIVE = "Projective";
  private static final String POLYNOMIAL = "Polynomial";
  private static final String RATIONAL = "Rational";

  private static final String[] fProjectionDisplayNames = new String[]{
      PROJECTIVE,
      POLYNOMIAL,
      RATIONAL,
  };

  private JLabel fNumeratorDegreeLabel = new JLabel("Numerator degree:");
  private JLabel fDenominatorDegreeLabel = new JLabel("Denominator degree:");
  private JLabel fPolynomialDegreeLabel = new JLabel("Polynomial degree:");
  private JTextField fTextField1 = new JTextField("2", 10); // numerator / polynomial degree
  private JTextField fTextField2 = new JTextField("1", 10); // denominator degree
  private JComboBox fProjectionCombo;

  private ILcdRasterReferencer fRasterReferencer;

  public RasterReferencerCustomizer() {

    JPanel panel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    panel.setLayout(layout);
    GridBagConstraints constraints;
    JLabel label;

    int line = 0;
    // Upper-left: Projection type combo
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = line;
    constraints.insets.right = 5;
    constraints.fill = GridBagConstraints.NONE;
    label = new JLabel("Projection");
    label.setHorizontalAlignment(JTextField.RIGHT);
    panel.add(label, constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = line;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fProjectionCombo = new JComboBox(fProjectionDisplayNames);
    panel.add(fProjectionCombo, constraints);

    line++;
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = line;
    fNumeratorDegreeLabel.setHorizontalAlignment(JTextField.RIGHT);
    fPolynomialDegreeLabel.setHorizontalAlignment(JTextField.RIGHT);
    panel.add(fNumeratorDegreeLabel, constraints);
    panel.add(fPolynomialDegreeLabel, constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = line;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    panel.add(fTextField1, constraints);

    line++;
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = line;
    fDenominatorDegreeLabel.setHorizontalAlignment(JTextField.RIGHT);
    panel.add(fDenominatorDegreeLabel, constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = line;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    panel.add(fTextField2, constraints);

    line++;
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = line;
    constraints.insets.right = 5;

    panel.setPreferredSize(new Dimension(250, 80));
    add(new JScrollPane(panel));

    fProjectionCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateGUI();
      }
    });
  }

  public void setRasterReferencer(ILcdRasterReferencer aRasterReferencer) {
    fRasterReferencer = aRasterReferencer;

    data2GUI();
  }

  public ILcdRasterReferencer getRasterReferencer() {
    GUI2Data();

    return fRasterReferencer;
  }

  private void GUI2Data() {

    if (PROJECTIVE.equals(fProjectionCombo.getSelectedItem())) {
      fRasterReferencer = new TLcdProjectiveRasterReferencer();

    } else if (POLYNOMIAL.equals(fProjectionCombo.getSelectedItem())) {
      int degree;
      try {
        degree = Integer.parseInt(fTextField1.getText());
        if (degree > 4) {
          System.err.println("The degree of the polynomial should be less than 4.");
          degree = 4;
        }
      } catch (NumberFormatException ex) {
        degree = 0;
      }

      fRasterReferencer = new TLcdPolynomialRasterReferencer(degree);

    } else if (RATIONAL.equals(fProjectionCombo.getSelectedItem())) {
      int numerator;
      int denominator;
      try {
        numerator = Integer.parseInt(fTextField1.getText());
        if (numerator > 4) {
          System.err.println("The degree of the numerator should be less than 4.");
          numerator = 4;
        }
      } catch (NumberFormatException ex) {
        numerator = 2;
      }
      try {
        denominator = Integer.parseInt(fTextField2.getText());
        if (denominator > 4) {
          System.err.println("The degree of the denominator should be less than 4.");
          denominator = 4;
        }
      } catch (NumberFormatException ex) {
        denominator = 1;
      }

      fRasterReferencer = new TLcdRationalRasterReferencer(numerator, denominator);

    }
  }

  private void data2GUI() {

    if (fRasterReferencer instanceof TLcdProjectiveRasterReferencer) {
      fProjectionCombo.setSelectedItem(PROJECTIVE);
      fTextField1.setVisible(false);
      fTextField2.setVisible(false);
      fDenominatorDegreeLabel.setVisible(false);
      fNumeratorDegreeLabel.setVisible(false);
      fPolynomialDegreeLabel.setVisible(false);

    } else if (fRasterReferencer instanceof TLcdPolynomialRasterReferencer) {
      fProjectionCombo.setSelectedItem(POLYNOMIAL);
      fTextField1.setVisible(true);
      fTextField2.setVisible(false);
      fDenominatorDegreeLabel.setVisible(false);
      fNumeratorDegreeLabel.setVisible(false);
      fPolynomialDegreeLabel.setVisible(true);

    } else if (fRasterReferencer instanceof TLcdRationalRasterReferencer) {
      fProjectionCombo.setSelectedItem(RATIONAL);
      fTextField1.setVisible(true);
      fTextField2.setVisible(true);
      fDenominatorDegreeLabel.setVisible(true);
      fNumeratorDegreeLabel.setVisible(true);
      fPolynomialDegreeLabel.setVisible(false);

    }
    updateGUI();
  }

  private void updateGUI() {

    if (PROJECTIVE.equals(fProjectionCombo.getSelectedItem())) {
      fTextField1.setVisible(false);
      fTextField2.setVisible(false);
      fDenominatorDegreeLabel.setVisible(false);
      fNumeratorDegreeLabel.setVisible(false);
      fPolynomialDegreeLabel.setVisible(false);

    } else if (POLYNOMIAL.equals(fProjectionCombo.getSelectedItem())) {
      fTextField1.setVisible(true);
      fTextField2.setVisible(false);
      fDenominatorDegreeLabel.setVisible(false);
      fNumeratorDegreeLabel.setVisible(false);
      fPolynomialDegreeLabel.setVisible(true);

    } else if (RATIONAL.equals(fProjectionCombo.getSelectedItem())) {
      fTextField1.setVisible(true);
      fTextField2.setVisible(true);
      fDenominatorDegreeLabel.setVisible(true);
      fNumeratorDegreeLabel.setVisible(true);
      fPolynomialDegreeLabel.setVisible(false);

    }
  }
}
