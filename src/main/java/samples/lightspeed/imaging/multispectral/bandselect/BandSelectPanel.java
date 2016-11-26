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
package samples.lightspeed.imaging.multispectral.bandselect;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;

import samples.lightspeed.imaging.multispectral.OperatorModel;

/**
 * Panel that allows you to select which bands of an image you want to visualize.
 * It's also possible to switch to gray scale, such that only one band is visualized.
 */
public class BandSelectPanel extends JPanel {

  private static final String RGB = "rgb";
  private static final String GRAY_SCALE = "grayScale";
  private final JCheckBox fNormalizeCheckBox;
  private OperatorModel fOperatorModel;
  private final JToggleButton fToggleRGB;
  private final JToggleButton fToggleGrayScale;
  private JComboBox fCbxRed, fCbxGreen, fCbxBlue, fCbxGrayScale;
  private final JPanel fCardsPanel;

  /**
   * Creates a new band select panel.
   *
   * @param aOperatorModel the filter model to use
   */
  public BandSelectPanel(OperatorModel aOperatorModel) {
    fOperatorModel = aOperatorModel;
    setLayout(new BorderLayout(4, 4));
    ButtonGroup toggleGroup = new ButtonGroup();
    fToggleRGB = new JToggleButton(new TLcdSWIcon(new TLcdImageIcon("samples/lightspeed/icons/rgb.png")));
    fToggleGrayScale = new JToggleButton(new TLcdSWIcon(new TLcdImageIcon("samples/lightspeed/icons/grayscale.png")));

    fCardsPanel = new JPanel(new CardLayout());
    initializeCardsPanel();

    fToggleRGB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fToggleRGB.isSelected()) {
          CardLayout cardLayout = (CardLayout) fCardsPanel.getLayout();
          cardLayout.show(fCardsPanel, RGB);
          //notify filter model of changes in the band selection
          fOperatorModel.setSelectedBands(getSelectedIndices());
        }
      }
    });

    fToggleGrayScale.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fToggleGrayScale.isSelected()) {
          CardLayout cardLayout = (CardLayout) fCardsPanel.getLayout();
          cardLayout.show(fCardsPanel, GRAY_SCALE);
          //notify filter model of changes in the band selection
          fOperatorModel.setSelectedBands(getSelectedIndices());
        }
      }
    });

    toggleGroup.add(fToggleRGB);
    toggleGroup.add(fToggleGrayScale);

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(fToggleRGB);
    toolBar.add(fToggleGrayScale);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(toolBar, BorderLayout.WEST);
    fNormalizeCheckBox = createNormalizeCheckBox();
    topPanel.add(fNormalizeCheckBox, BorderLayout.EAST);

    add(topPanel, BorderLayout.NORTH);
    add(fCardsPanel, BorderLayout.CENTER);
  }

  private void initializeCardsPanel() {
    fCardsPanel.removeAll();

    int[] selectedBands = fOperatorModel.getSelectedBands();
    if (selectedBands.length == 1) {
      if (fOperatorModel.getNbBands() >= 3) {
        fCardsPanel.add(createRGBPanel(fOperatorModel.getNbBands(), new int[]{0, 1, 2}), RGB);
      }
    } else {
      fCardsPanel.add(createRGBPanel(fOperatorModel.getNbBands(), selectedBands), RGB);
    }
    fCardsPanel.add(createGrayScalePanel(fOperatorModel.getNbBands(), selectedBands[0]), GRAY_SCALE);
    fToggleRGB.setEnabled(true);

    if (selectedBands.length == 1) {
      fToggleGrayScale.setSelected(true);
      if (fOperatorModel.getNbBands() < 3) {
        fToggleRGB.setEnabled(false);
      }
      CardLayout cardLayout = (CardLayout) fCardsPanel.getLayout();
      cardLayout.show(fCardsPanel, GRAY_SCALE);
    } else {
      fToggleRGB.setSelected(true);
    }
  }

  public void setOperatorModel(OperatorModel aOperatorModel) {
    fOperatorModel = aOperatorModel;
    fNormalizeCheckBox.setSelected(fOperatorModel.isNormalized());
    initializeCardsPanel();

  }

  /**
   * Create a rgb band selection panel.
   *
   * @param aNbBands the number of bands that must be shown in the comboboxes
   * @return the panel for rgb selection
   */
  private JPanel createRGBPanel(final int aNbBands, int[] aSelectedBands) {

    JLabel lblRed = new JLabel("Red");
    JLabel lblGreen = new JLabel("Green");
    JLabel lblBlue = new JLabel("Blue");

    fCbxRed = getBandSelectComboBox(aNbBands);
    fCbxGreen = getBandSelectComboBox(aNbBands);
    fCbxBlue = getBandSelectComboBox(aNbBands);

    fCbxRed.setSelectedIndex(0);
    fCbxGreen.setSelectedIndex(Math.min(aNbBands - 1, 1));
    fCbxBlue.setSelectedIndex(Math.min(aNbBands - 1, 2));

    fCbxRed.setSelectedIndex(aSelectedBands[0]);
    fCbxGreen.setSelectedIndex(aSelectedBands[1]);
    fCbxBlue.setSelectedIndex(aSelectedBands[2]);

    fCbxRed.addItemListener(new BandSelectionListener());
    fCbxGreen.addItemListener(new BandSelectionListener());
    fCbxBlue.addItemListener(new BandSelectionListener());

    FormLayout layout = new FormLayout("left:pref, 100px, fill:pref:grow", "pref, 4px, pref, 4px, pref");
    CellConstraints cc = new CellConstraints();

    JPanel rgbSelectionPanel = new JPanel(layout);
    rgbSelectionPanel.add(lblRed, cc.xy(1, 1));
    rgbSelectionPanel.add(fCbxRed, cc.xy(3, 1));

    rgbSelectionPanel.add(lblGreen, cc.xy(1, 3));
    rgbSelectionPanel.add(fCbxGreen, cc.xy(3, 3));

    rgbSelectionPanel.add(lblBlue, cc.xy(1, 5));
    rgbSelectionPanel.add(fCbxBlue, cc.xy(3, 5));

    return rgbSelectionPanel;
  }

  /**
   * Create a panel for gray scale band selection.
   *
   * @param aNbBands the number of bands that must be shown in the combobox
   * @return the panel for gray scale selection
   */
  private JPanel createGrayScalePanel(final int aNbBands, int aSelectedBand) {
    JLabel lblGrayScale = new JLabel("Band");
    fCbxGrayScale = getBandSelectComboBox(aNbBands);
    fCbxGrayScale.setSelectedIndex(aSelectedBand);

    fCbxGrayScale.addItemListener(new BandSelectionListener());

    FormLayout layout = new FormLayout("left:pref, 100px, fill:pref:grow", "pref, 4px, pref, 4px, pref");
    CellConstraints cc = new CellConstraints();

    JPanel grayScaleSelectionPanel = new JPanel(layout);
    grayScaleSelectionPanel.add(lblGrayScale, cc.xy(1, 1));
    grayScaleSelectionPanel.add(fCbxGrayScale, cc.xy(3, 1));

    return grayScaleSelectionPanel;
  }

  //create a combobox with the specified bands
  private JComboBox getBandSelectComboBox(final int aNbBands) {
    JComboBox cbxSelection = new JComboBox();
    ((JLabel) cbxSelection.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
    for (int i = 0; i < aNbBands; i++) {
      cbxSelection.addItem("Band " + Integer.toString(i + 1));
    }

    return cbxSelection;
  }

  private JCheckBox createNormalizeCheckBox() {
    final JCheckBox normalize = new JCheckBox("Normalize");
    normalize.setSelected(fOperatorModel.isNormalized());
    normalize.setHorizontalTextPosition(SwingConstants.LEFT);
    normalize.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (normalize.isSelected()) {
          fOperatorModel.normalize();
        } else {
          fOperatorModel.resetNormalization();
        }
      }
    });

    return normalize;
  }

  public int[] getSelectedIndices() {
    if (fToggleRGB.isSelected()) {
      return new int[]{fCbxRed.getSelectedIndex(),
                       fCbxGreen.getSelectedIndex(),
                       fCbxBlue.getSelectedIndex()};
    } else {
      return new int[]{fCbxGrayScale.getSelectedIndex()};
    }
  }

  //listener to notify the filter model about band selection changes
  private class BandSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        fOperatorModel.setSelectedBands(getSelectedIndices());
      }
    }
  }

}
