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
package samples.tea.lightspeed.extremepoint;

import static samples.tea.lightspeed.HeightProviderUtil.DTEDLevel;
import static samples.tea.lightspeed.HeightProviderUtil.getHeightProvider;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.swing.*;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.tea.TLcdHeightProviderAdapter;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * In this sample, we demonstrate the functionality of the extreme point finder.
 */
public class MainPanel extends LightspeedSample {

  private ILspLayer fHighestPointsLayer = ExtremePointLayerFactory.createExtremeMaximaLayer();
  private ILspLayer fLowestPointsLayer = ExtremePointLayerFactory.createExtremeMinimaLayer();
  private ILspLayer fPolygonLayer = ExtremePointLayerFactory.createPolygonLayer();

  private ILcdPolygon fPolygon = (ILcdPolygon) fPolygonLayer.getModel().elements().nextElement();
  private ILcdGeoReference fPolygonReference = (ILcdGeoReference) fPolygonLayer.getModel().getModelReference();
  private ExtremePointAction fExtremePointAction;
  private ILcdModel fHeightModel;

  protected void createGUI() {
    super.createGUI();
    fHeightModel = LspDataUtil.instance().model(SampleData.ALPS_ELEVATION).getModel();
    addComponentToRightPanel(createExtremePointPanel());
  }

  /**
   * Loads the sample data.
   */
  protected void addData() throws IOException {
    LspDataUtil.instance().grid().addToView(getView());
    LspDataUtil.instance().model(fHeightModel).layer().label("Alps").addToView(getView());

    getView().addLayer(fPolygonLayer);
    getView().addLayer(fHighestPointsLayer);
    getView().addLayer(fLowestPointsLayer);

    TLspViewNavigationUtil navigationUtil = new TLspViewNavigationUtil(getView());
    navigationUtil.setFitMargin(0.1);
    try {
      navigationUtil.fit(fPolygonLayer);
    } catch (TLcdNoBoundsException | TLcdOutOfBoundsException e) {
      throw new RuntimeException(e);
    }

    if (fExtremePointAction.isEnabled()) {
      fExtremePointAction.actionPerformed(null);
    }

  }

  /**
   * Creates a panel to allow the user to configure the extreme point computation.
   * @return a panel to allow the user to configure the extreme point computation.
   */
  private JPanel createExtremePointPanel() {
    JLabel requestedPointsLabel = new JLabel("Number of points:    ");
    JLabel separationDistanceLabel = new JLabel("Separation distance: ");
    JLabel separationHeightLabel = new JLabel("Separation height:   ");
    JLabel dtedLabel = new JLabel("DTED Level:   ");
    JCheckBox separationDistanceInfinity = new JCheckBox("+Infinity");
    JCheckBox separationHeightInfinity   = new JCheckBox("+Infinity");
    final JComboBox<DTEDLevel> dtedLevel = new JComboBox<>(DTEDLevel.values());
    JButton computeButton = new JButton("Compute");

    final MyTextField requestedPoints = new MyTextField(30, 5);
    final MyTextField separationDistance = new MyTextField(2500, 5);
    final MyTextField separationHeight = new MyTextField(100, 5);

    requestedPoints.setHorizontalAlignment(JTextField.RIGHT);
    separationDistance.setHorizontalAlignment(JTextField.RIGHT);
    separationHeight.setHorizontalAlignment(JTextField.RIGHT);

    GridBagConstraints constraints = new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0,0,0,0 ), 0, 0
    );

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    constraints.gridx     = 0;
    constraints.gridy     = 0;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( requestedPointsLabel, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( requestedPoints, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 1;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( separationDistanceLabel, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( separationDistance, constraints );
    constraints.gridx++;
    constraints.weightx   = 0;
    panel.add( Box.createHorizontalStrut( 5 ), constraints );
    constraints.gridx++;
    panel.add( separationDistanceInfinity, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 2;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add( separationHeightLabel, constraints );
    constraints.gridx++;
    constraints.weightx   = 1;
    panel.add( separationHeight, constraints );
    constraints.gridx++;
    constraints.weightx   = 0;
    panel.add( Box.createHorizontalStrut(5), constraints );
    constraints.gridx++;
    panel.add( separationHeightInfinity, constraints );

    constraints.gridx     = 0;
    constraints.gridy     = 3;
    constraints.weightx   = 0;
    constraints.anchor    = GridBagConstraints.WEST;
    panel.add(dtedLabel, constraints );
    constraints.gridx++;
    constraints.weightx = 1;
    panel.add(dtedLevel, constraints);

    constraints.gridx     = 0;
    constraints.gridy     = 4;
    constraints.weightx   = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add( Box.createVerticalStrut( 5 ), constraints );

    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    panel.add( computeButton, constraints );

    DTEDLevel level = (DTEDLevel) dtedLevel.getSelectedItem();

    // Setup the action
    fExtremePointAction = new ExtremePointAction(
            this,
            fPolygon,
            fPolygonReference,
            createAltitudeProvider(level)
    );
    fExtremePointAction.setMaximumPointModel(fHighestPointsLayer.getModel());
    fExtremePointAction.setMinimumPointModel(fLowestPointsLayer.getModel());
    fExtremePointAction.setRequestedPoints(((Number) requestedPoints.getValue()).intValue());
    fExtremePointAction.setSeparationDistance(((Number) separationDistance.getValue()).doubleValue());
    fExtremePointAction.setSeparationHeight(((Number) separationHeight.getValue()).doubleValue());
    fExtremePointAction.setDTEDLevel(level);

    // Listen to changes in the polygon.
    fPolygonLayer.getModel().addModelListener(new ILcdModelListener() {
      public void modelChanged( TLcdModelChangedEvent aModelChangedEvent ) {
        ILcdModel model = fPolygonLayer.getModel();
        try (TLcdLockUtil.Lock readLock = TLcdLockUtil.readLock(model)) {
          Enumeration element = model.elements();
          if (element.hasMoreElements()) {
            ILcdShape firstShape = (ILcdShape) element.nextElement();
            fExtremePointAction.setShape(firstShape);
          }
        }
      }
    });

    separationDistanceInfinity.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent aEvent) {
        separationDistance.setEnabled(aEvent.getStateChange() == ItemEvent.DESELECTED);
      }
    });

    separationHeightInfinity.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent aEvent) {
        separationHeight.setEnabled(aEvent.getStateChange() == ItemEvent.DESELECTED);
      }
    });

    dtedLevel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DTEDLevel level = (DTEDLevel) dtedLevel.getSelectedItem();
        fExtremePointAction.setDTEDLevel(level);
        fExtremePointAction.setAltitudeProvider(createAltitudeProvider(level));
      }
    });

    computeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent aEvent) {
        fExtremePointAction.setRequestedPoints(((Number) requestedPoints.getValue()).intValue());
        fExtremePointAction.setSeparationDistance(separationDistance.isEnabled() ? ((Number) separationDistance.getValue()).doubleValue() : Double.POSITIVE_INFINITY);
        fExtremePointAction.setSeparationHeight(separationHeight.isEnabled() ? ((Number) separationHeight.getValue()).doubleValue() : Double.POSITIVE_INFINITY);

        if (fExtremePointAction.getRequestedPoints() < 0) {
          JOptionPane.showMessageDialog(MainPanel.this, "The number of points should be strict positive", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (fExtremePointAction.getSeparationDistance() < 0) {
          JOptionPane.showMessageDialog(MainPanel.this, "The separation distance should be strict positive", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (fExtremePointAction.getSeparationHeight() < 0) {
          JOptionPane.showMessageDialog(MainPanel.this, "The separation height should be strict positive", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          if (fExtremePointAction.isEnabled()) {
            fExtremePointAction.actionPerformed(aEvent);
          }
        }
      }
    });

    return TitledPanel.createTitledPanel("Find extreme points", panel);
  }

  private TLcdHeightProviderAdapter createAltitudeProvider(DTEDLevel aLevel) {
    return new TLcdHeightProviderAdapter(createHeightProvider(aLevel), fPolygonReference);
  }

  private ILcdHeightProvider createHeightProvider(DTEDLevel aLevel) {
    return getHeightProvider(fHeightModel, fPolygonReference, fPolygon.getFocusPoint(), aLevel);
  }

  private static class MyTextField extends JFormattedTextField {
    public MyTextField( int aValue, int aColumns ) {
      super( NumberFormat.getIntegerInstance() );
      setValue( new Integer( aValue ) );
      setColumns( aColumns );
    }
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        startSample(MainPanel.class, "Extreme points");
      }
    });
  }



}
