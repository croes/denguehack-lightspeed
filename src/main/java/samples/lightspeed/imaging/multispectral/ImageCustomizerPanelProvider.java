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
package samples.lightspeed.imaging.multispectral;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.util.ILcdDisposable;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.imaging.multispectral.bandselect.BandSelectPanel;
import samples.lightspeed.imaging.multispectral.curves.CurvesPanel;
import samples.lightspeed.imaging.multispectral.general.GeneralOperationPanel;

/**
 * Utility class to obtain GUI component for configuring the display of a multispectral raster model.
 * This component allows the user to choose which color bands are displayed
 * and in what order. It also allows manipulation of the raster's dynamic range
 * via tone mapping.
 */
public class ImageCustomizerPanelProvider implements ILcdDisposable {

  private JPanel fNotAnImagePanel;
  private JPanel fCustomizerPanel;
  private GeneralOperationPanel fGeneralOperationPanel;
  private BandSelectPanel fBandSelectPanel;
  private CurvesPanel fCurvesPanel;

  public JPanel getPanelForOperatorModel(OperatorModel aOperatorModel) {
    if (aOperatorModel == null) {
      if (fNotAnImagePanel == null) {
        fNotAnImagePanel = createNotAnImagePanel();
      }
      return fNotAnImagePanel;
    } else {
      if (fCustomizerPanel == null) {
        fCustomizerPanel = createCustomizerPanel(aOperatorModel);
      }
      fCurvesPanel.setOperatorModel(aOperatorModel);
      fGeneralOperationPanel.setOperatorModel(aOperatorModel);
      fBandSelectPanel.setOperatorModel(aOperatorModel);
      return fCustomizerPanel;
    }
  }

  private JPanel createParentPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setPreferredSize(new Dimension(265, 600));
    return panel;
  }

  private JPanel createNotAnImagePanel() {
    JPanel bandSelect = new JPanel(new BorderLayout());
    JLabel notEnabled = new JLabel("<html>Select a visible raster layer <br>" +
                                   "(Not containing elevation data)<html>");
    notEnabled.setHorizontalAlignment(SwingConstants.CENTER);
    notEnabled.setVerticalAlignment(SwingConstants.CENTER);
    notEnabled.setEnabled(false);
    bandSelect.add(notEnabled, BorderLayout.CENTER);
    JPanel parentPanel = createParentPanel();
    parentPanel.add(bandSelect);

    return parentPanel;
  }

  /**
   *
   *
   * @param aOperatorModel the operator model for which to adapt the panels
   * @return the customizer panel
   */
  private JPanel createCustomizerPanel(OperatorModel aOperatorModel) {
    //add panel that allows to change contrast, brightness, opacity and sharpness
    JPanel parentPanel = createParentPanel();
    fGeneralOperationPanel = new GeneralOperationPanel(aOperatorModel);
    TitledPanel generalPanel = TitledPanel.createTitledPanel("General", fGeneralOperationPanel);
    parentPanel.add(generalPanel);

    //add panel for selecting bands
    fBandSelectPanel = new BandSelectPanel(aOperatorModel);
    TitledPanel bandSelectPanel = TitledPanel.createTitledPanel("Visible bands", fBandSelectPanel);
    parentPanel.add(bandSelectPanel);

    //add the curves panel, which allows to perform histogram equalization etc.
    fCurvesPanel = new CurvesPanel(aOperatorModel, 250);
    TitledPanel panel = TitledPanel.createTitledPanel("Curves", fCurvesPanel);
    parentPanel.add(panel);

    return parentPanel;
  }

  @Override
  public void dispose() {
    if (fCurvesPanel != null) {
      fCurvesPanel.dispose();
    }
  }
}
