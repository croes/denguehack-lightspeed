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
package samples.lightspeed.imaging.multispectral.curves;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdVectorModel;
import com.luciad.util.ILcdDisposable;

import samples.common.OptionsPanelBuilder;
import samples.lightspeed.imaging.multispectral.OperatorModel;
import samples.lightspeed.imaging.multispectral.histogram.Histogram;
import samples.lightspeed.imaging.multispectral.histogram.HistogramUtil;

/**
 * Panel that shows the curves that allow to remap the brightness levels in an image.
 *
 */
public class CurvesPanel extends JPanel implements ILcdDisposable {

  private static final String RGB = "rgb";
  private static final String GRAY_SCALE = "grayScale";

  private final FilterModelChangeListener fFilterChangeListener;
  private final JPanel fCurvesPanel;

  private OperatorModel fOperatorModel;

  private final int[] fColors = new int[]{CatmullRomEditLine.R_CHANNEL, CatmullRomEditLine.G_CHANNEL, CatmullRomEditLine.B_CHANNEL};
  private final JLabel[] fLabels = new JLabel[]{new JLabel(), new JLabel(), new JLabel(), new JLabel()};
  private HistogramPanel fRedEditingView;
  private HistogramPanel fGreenEditingView;
  private HistogramPanel fBlueEditingView;
  private HistogramPanel fLuminanceEditingView;
  private HistogramPanel fGrayScaleEditingView;

  private HistogramPanel fCurrentEditingView;
  private JTabbedPane fTabbedPane;

  /**
   * Create a new curves panel.
   *
   * @param aOperatorModel the filter model to be used
   * @param aViewSize the size of the view in which the curves are displayed.
   */
  public CurvesPanel(OperatorModel aOperatorModel, int aViewSize) {
    fOperatorModel = aOperatorModel;
    setLayout(new BorderLayout());

    ILcdModelListener curveModelListener = new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        fOperatorModel.curveChange();
      }
    };
    fRedEditingView = new HistogramPanel(aViewSize);
    fRedEditingView.getCurvesModel().addModelListener(curveModelListener);
    fGreenEditingView = new HistogramPanel(aViewSize);
    fGreenEditingView.getCurvesModel().addModelListener(curveModelListener);
    fBlueEditingView = new HistogramPanel(aViewSize);
    fBlueEditingView.getCurvesModel().addModelListener(curveModelListener);
    fLuminanceEditingView = new HistogramPanel(aViewSize);
    fLuminanceEditingView.getCurvesModel().addModelListener(curveModelListener);
    fGrayScaleEditingView = new HistogramPanel(aViewSize);
    fGrayScaleEditingView.getCurvesModel().addModelListener(curveModelListener);

    fCurrentEditingView = fRedEditingView;

    fCurvesPanel = new JPanel(new CardLayout());
    fCurvesPanel.setOpaque(false);
    fCurvesPanel.add(createCurvesPanel(), RGB);
    fCurvesPanel.add(createGrayScaleCurvesPanel(), GRAY_SCALE);
    CardLayout cardLayout = (CardLayout) fCurvesPanel.getLayout();
    cardLayout.show(fCurvesPanel, RGB);

    add(fCurvesPanel, BorderLayout.CENTER);

    if (fOperatorModel.getSelectedBands().length == 1) {
      cardLayout.show(fCurvesPanel, GRAY_SCALE);
      fCurrentEditingView = fGrayScaleEditingView;
    } else {
      cardLayout.show(fCurvesPanel, RGB);
    }

    updateEditingViews();

    //add a listener to the filter model to be notified of changes.
    fFilterChangeListener = new FilterModelChangeListener();
    fOperatorModel.addChangeListener(fFilterChangeListener);

    setLabels();
  }

  //create the main panel containing the curve controls
  private JPanel createCurvesPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setOpaque(false);
    fTabbedPane = new JTabbedPane();

    fTabbedPane.addTab("Red", createColorPanel(fRedEditingView, fLabels[0]));
    fTabbedPane.addTab("Green", createColorPanel(fGreenEditingView, fLabels[1]));
    fTabbedPane.addTab("Blue", createColorPanel(fBlueEditingView, fLabels[2]));
    fTabbedPane.addTab("Lum", createLuminancePanel());

    fTabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (fTabbedPane.getSelectedIndex() == 0) {
          fCurrentEditingView = fRedEditingView;
          fOperatorModel.setCurvesOnLuminance(false);
        } else if (fTabbedPane.getSelectedIndex() == 1) {
          fCurrentEditingView = fGreenEditingView;
          fOperatorModel.setCurvesOnLuminance(false);
        } else if (fTabbedPane.getSelectedIndex() == 2) {
          fCurrentEditingView = fBlueEditingView;
          fOperatorModel.setCurvesOnLuminance(false);
        } else {
          fCurrentEditingView = fLuminanceEditingView;
          fOperatorModel.setCurvesOnLuminance(true);
        }
        updateEditingViews();
      }
    });

    fTabbedPane.setOpaque(false);
    panel.add(fTabbedPane);
    return panel;
  }

  //create the panel for using the curves with the luminance of an image
  private JPanel createLuminancePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setOpaque(false);

    JPanel viewPanel = new JPanel();
    viewPanel.setOpaque(false);
    viewPanel.add(fLuminanceEditingView.getComponent());
    panel.add(viewPanel, BorderLayout.CENTER);

    JToolBar controlPanel = createToolBar();

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setOpaque(false);

    //equalize button calculates curves for histogram equalization.
    final AbstractButton equalize = OptionsPanelBuilder.createUnderlinedButton("Equalize");
    equalize.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        equalize.setSelected(false);
        fLuminanceEditingView.setCurvesEditable(false);
        fOperatorModel.equalizeLuminance(new HistogramUtil.Callback() {
          @Override
          public void histogramsAvailable(Histogram[] aHistograms, Object aSource) {
            updateEditingViews();
          }

          @Override
          public void histogramNotAvailable() {

          }
        });
      }
    });
    final AbstractButton reset = createResetButton(fLuminanceEditingView);

    rightPanel.add(equalize);
    rightPanel.add(reset);

    controlPanel.add(new JLabel("Relative Luminance"));
    controlPanel.add(rightPanel);

    panel.add(controlPanel, BorderLayout.NORTH);

    return panel;
  }

  //create the panel for using the curves with the RGB channels
  private JPanel createColorPanel(HistogramPanel aHistogramPanel, JLabel aLabel) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setOpaque(false);

    JToolBar controlPanel = createToolBar();
    controlPanel.setOpaque(false);

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setOpaque(false);

    rightPanel.add(createResetButton(aHistogramPanel));
    controlPanel.add(aLabel);
    controlPanel.add(rightPanel);

    panel.add(controlPanel, BorderLayout.NORTH);
    JPanel viewPanel = new JPanel();
    viewPanel.setOpaque(false);
    viewPanel.add(aHistogramPanel.getComponent());
    panel.add(viewPanel, BorderLayout.CENTER);

    return panel;
  }

  //create a panel for using curves with gray scale
  private JPanel createGrayScaleCurvesPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setOpaque(false);

    JToolBar controlPanel = createToolBar();
    controlPanel.setOpaque(false);

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setOpaque(false);

    //equalize button calculates curves for histogram equalization.
    final AbstractButton equalize = OptionsPanelBuilder.createUnderlinedButton("Equalize");
    equalize.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        equalize.setSelected(false);
        fGrayScaleEditingView.setCurvesEditable(false);
        int band = 0;
        fOperatorModel.equalize();
        updateEditingViews();
        fGrayScaleEditingView.setCurveOnCurveLayer(fOperatorModel.getCurrentCurves()[band]);
      }
    });
    final AbstractButton reset = createResetButton(fGrayScaleEditingView);

    rightPanel.add(equalize);
    rightPanel.add(reset);

    controlPanel.add(fLabels[3]);
    controlPanel.add(rightPanel);
    panel.add(controlPanel, BorderLayout.NORTH);

    JPanel viewPanel = new JPanel();
    viewPanel.setOpaque(false);
    viewPanel.add(fGrayScaleEditingView.getComponent());
    panel.add(viewPanel, BorderLayout.CENTER);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Gray", panel);
    tabbedPane.setOpaque(false);

    JPanel result = new JPanel();
    result.setLayout(new BorderLayout());
    result.setOpaque(false);
    result.add(tabbedPane);
    return result;
  }

  private AbstractButton createResetButton(final HistogramPanel aEditingView) {
    // Add a "reset" button that restores the curves to their neutral values
    final AbstractButton reset = OptionsPanelBuilder.createUnderlinedButton("Reset");
    reset.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        reset.setSelected(false);
        aEditingView.setCurvesEditable(true);
        TLcdVectorModel curvesModel = aEditingView.getCurvesModel();
        CatmullRomEditLine curve = ((CatmullRomEditLine) curvesModel.elementAt(0));
        fOperatorModel.resetCurves();
        updateEditingViews();
        curvesModel.removeAllElements(ILcdModel.FIRE_LATER);
        curvesModel.addElement(curve, ILcdModel.FIRE_NOW);
      }
    });
    return reset;
  }

  private JToolBar createToolBar() {
    JToolBar controlPanel = new JToolBar();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setFloatable(false);
    controlPanel.setOpaque(false);
    return controlPanel;
  }

  /**
   * Set the labels of the channel select combo box.
   *
   */
  private void setLabels() {
    String[] bandNames = fOperatorModel.getSelectedBandNames();
    if (bandNames.length == 1) {
      fLabels[3].setText(bandNames[0]);
    } else {
      for (int i = 0; i < bandNames.length; i++) {
        fLabels[i].setText(bandNames[i]);
      }
    }
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    if (isEnabled() == aEnabled) {
      return;
    }
    super.setEnabled(aEnabled);

    fLuminanceEditingView.setEnabled(aEnabled);
    fGrayScaleEditingView.setEnabled(aEnabled);
    fRedEditingView.setEnabled(aEnabled);
    fGreenEditingView.setEnabled(aEnabled);
    fBlueEditingView.setEnabled(aEnabled);
  }

  public void setOperatorModel(OperatorModel aOperatorModel) {
    fOperatorModel.removeChangeListener(fFilterChangeListener);
    fOperatorModel = aOperatorModel;
    fOperatorModel.addChangeListener(fFilterChangeListener);
    CardLayout cardLayout = (CardLayout) fCurvesPanel.getLayout();
    if (fOperatorModel.getSelectedBands().length == 1) {
      cardLayout.show(fCurvesPanel, GRAY_SCALE);
      fCurrentEditingView = fGrayScaleEditingView;
    } else {
      cardLayout.show(fCurvesPanel, RGB);
      fTabbedPane.setSelectedIndex(0);
      fCurrentEditingView = fRedEditingView;
    }
    updateEditingViews();
    fLuminanceEditingView.setCurvesEditable(true);
    fGrayScaleEditingView.setCurvesEditable(true);
  }

  /**
   * Update the curves and histograms.
   */
  public void updateEditingViews() {
    final int[] selectedBands = fOperatorModel.getSelectedBands();

    //do not wait until histogram is calculated, use a callback
    HistogramUtil.Callback histogramCallback = new HistogramUtil.Callback() {
      @Override
      public void histogramsAvailable(Histogram[] aHistograms, Object aSource) {
        TLcdVectorModel histogramModel = fCurrentEditingView.getHistogramModel();
        histogramModel.removeAllElements(ILcdModel.FIRE_LATER);
        histogramModel.addElement(aHistograms[0].asShape(), ILcdModel.FIRE_LATER);
        histogramModel.fireCollectedModelChanges();
      }

      @Override
      public void histogramNotAvailable() {

      }
    };

    if (fCurrentEditingView == fLuminanceEditingView) {
      fOperatorModel.getLuminanceHistogram(histogramCallback);
    } else {
      int[] bands = fOperatorModel.getSelectedBands();
      if (bands.length == 1) {
        fOperatorModel.getHistogram(0, histogramCallback);
      } else {
        int index = fCurrentEditingView == fRedEditingView ? 0 : (fCurrentEditingView == fGreenEditingView ? 1 : 2);
        fOperatorModel.getHistogram(index, histogramCallback);
      }
    }

    //update the curves in this panel
    CatmullRomEditLine currentCurve;

    if (fCurrentEditingView == fLuminanceEditingView) {
      currentCurve = fOperatorModel.getLuminanceCurve();
      currentCurve.setChannel(CatmullRomEditLine.GRAY_CHANNEL);
    } else {
      int[] bands = fOperatorModel.getSelectedBands();
      int selectedIndex;
      if (bands.length == 1) {
        selectedIndex = 0;
      } else {
        selectedIndex = fCurrentEditingView == fRedEditingView ? 0 : (fCurrentEditingView == fGreenEditingView ? 1 : 2);
      }
      currentCurve = fOperatorModel.getCurrentCurves()[selectedIndex];
      currentCurve.setChannel((selectedBands.length == 1 || fOperatorModel.getNbBands() == 1) ? CatmullRomEditLine.GRAY_CHANNEL : fColors[selectedIndex]);
    }

    ILcdModel curvesModel = fCurrentEditingView.getCurvesModel();
    curvesModel.removeAllElements(ILcdModel.FIRE_NOW);
    curvesModel.addElement(currentCurve, ILcdModel.FIRE_NOW);
    fCurrentEditingView.fitOnCurves();
  }

  @Override
  public void dispose() {
    removeAll();

    fRedEditingView.dispose();
    fGreenEditingView.dispose();
    fBlueEditingView.dispose();
    fLuminanceEditingView.dispose();
    fGrayScaleEditingView.dispose();

    fRedEditingView = null;
    fGreenEditingView = null;
    fBlueEditingView = null;
    fLuminanceEditingView = null;
    fGrayScaleEditingView = null;
    fCurrentEditingView = null;
  }

  /**
   * Listen to changes in the filter model. If the band selection has changed,
   * the curves and histograms need to be updated.
   */
  private class FilterModelChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (OperatorModel.BAND_CHANGE_EVENT.equals(evt.getPropertyName())) {
        if (evt.getSource() instanceof OperatorModel) {
          fOperatorModel.resetCurves();
          int[] selectedBands = fOperatorModel.getSelectedBands();
          CardLayout cardLayout = (CardLayout) fCurvesPanel.getLayout();
          if (selectedBands.length == 1) {
            cardLayout.show(fCurvesPanel, GRAY_SCALE);
            fCurrentEditingView = fGrayScaleEditingView;
            fOperatorModel.setCurvesOnLuminance(false);
          } else {
            if (fTabbedPane.getSelectedIndex() == 0) {
              fCurrentEditingView = fRedEditingView;
              fOperatorModel.setCurvesOnLuminance(false);
            } else if (fTabbedPane.getSelectedIndex() == 1) {
              fCurrentEditingView = fGreenEditingView;
              fOperatorModel.setCurvesOnLuminance(false);
            } else if (fTabbedPane.getSelectedIndex() == 2) {
              fCurrentEditingView = fBlueEditingView;
              fOperatorModel.setCurvesOnLuminance(false);
            } else {
              fCurrentEditingView = fLuminanceEditingView;
              fOperatorModel.setCurvesOnLuminance(true);
            }
            cardLayout.show(fCurvesPanel, RGB);
          }
          setLabels();
          updateEditingViews();
        }
      }
    }
  }
}
