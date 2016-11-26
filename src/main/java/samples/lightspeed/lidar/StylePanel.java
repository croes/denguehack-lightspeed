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
package samples.lightspeed.lidar;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.format.las.TLcdLASModelDescriptor;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.TwoColumnLayoutBuilder;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;

/**
 * Panel that allows to change the styling of LIDAR layers.
 * <p>
 * This panel uses {@link StyleModel} to apply styling to all layers at once.
 *
 * @since 2014.0
 */
public class StylePanel extends JPanel {

  protected static final Map<String, String> PROPERTY_DESCRIPTIONS = new HashMap<String, String>();
  protected static final Map<String, String> PROPERTY_NAMES = new HashMap<String, String>();
  protected static String WARNING_TOOLTIPS = "The selected styling could not be applied to all layers. Apply the Height style instead.";

  static {
    PROPERTY_DESCRIPTIONS.put(TLcdLASModelDescriptor.COLOR, "Use the colors from the LAS file");
    PROPERTY_DESCRIPTIONS.put(TLcdLASModelDescriptor.HEIGHT, "A color gradient is fitted to the height range of all the LAS layers");
    PROPERTY_DESCRIPTIONS.put(TLcdLASModelDescriptor.CLASSIFICATION, "Color according to the classification (water, vegetation ...)");
    PROPERTY_DESCRIPTIONS.put(TLcdLASModelDescriptor.INTENSITY, "Gray-scale that matches the strength of the laser pulse return. Brighter means stronger returns.");
    PROPERTY_DESCRIPTIONS.put(TLcdLASModelDescriptor.INFRARED, "Color gradient for the infrared intensity (dark blue, purple, orange, white). Brighter means more intense.");

    PROPERTY_NAMES.put(TLcdLASModelDescriptor.COLOR, "Color");
    PROPERTY_NAMES.put(TLcdLASModelDescriptor.HEIGHT, "Height");
    PROPERTY_NAMES.put(TLcdLASModelDescriptor.CLASSIFICATION, "Classification");
    PROPERTY_NAMES.put(TLcdLASModelDescriptor.INTENSITY, "Intensity");
    PROPERTY_NAMES.put(TLcdLASModelDescriptor.INFRARED, "Infrared");
  }

  public static final TLcdImageIcon WARNING_ICON = new TLcdImageIcon("images/icons/warning_16.png");

  private final StyleModel fStyleModel;

  private Map<String, JRadioButton> fRadioButtons = new HashMap<String, JRadioButton>();

  private JLabel fWarningLabel;

  public StylePanel(StyleModel aStyleModel) {
    fStyleModel = aStyleModel;

    fStyleModel.addChangeListener(new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aChangeEvent) {
        enableRadioButtonsAndWarningLabel();
      }
    });

    TwoColumnLayoutBuilder builder = TwoColumnLayoutBuilder.newBuilder();

    ButtonGroup buttonGroup = new ButtonGroup();
    for (String type : LASStyler.SUPPORTED_PROPERTIES) {
      JRadioButton radioButton = createRadioButton(type);
      buttonGroup.add(radioButton);
      fRadioButtons.put(type, radioButton);
      builder = addRadioButtonToBuilder(builder, radioButton);
    }

    fWarningLabel = new JLabel("");
    fWarningLabel.setIcon(new TLcdSWIcon(WARNING_ICON));
    fWarningLabel.setToolTipText(WARNING_TOOLTIPS);
    fWarningLabel.setBorder(BorderFactory.createEmptyBorder(10, 2, 0, 0));
    fWarningLabel.setIconTextGap(3);
    fWarningLabel.setVisible(false);

    TwoColumnLayoutBuilder.RowBuilder rowBuilder = builder.row();
    rowBuilder.spanBothColumns(fWarningLabel);
    builder = rowBuilder.build();

    builder.populate(this);

    setBorder(BorderFactory.createEmptyBorder());
    enableRadioButtonsAndWarningLabel();
  }

  private TwoColumnLayoutBuilder addRadioButtonToBuilder(TwoColumnLayoutBuilder builder, JRadioButton aRadioButton) {
    TwoColumnLayoutBuilder.RowBuilder rowBuilder = null;
    rowBuilder = builder.row();
    rowBuilder.columnOne(aRadioButton);
    builder = rowBuilder.build();
    return builder;
  }

  private JRadioButton createRadioButton(String aType) {
    JRadioButton radioButton = new JRadioButton(new StyleAction(PROPERTY_NAMES.get(aType), aType));
    radioButton.setToolTipText(PROPERTY_DESCRIPTIONS.get(aType));
    return radioButton;
  }

  protected String warningText(int aNumberOfLayers) {
    return "Could not be applied to " + aNumberOfLayers + " " + (aNumberOfLayers == 1 ? "layer" : "layers");
  }

  private void enableRadioButtonsAndWarningLabel() {
    //to update the UI if the model was modified programmatically
    fRadioButtons.get(fStyleModel.getStyleProperty()).setSelected(true);

    for (String property : LASStyler.SUPPORTED_PROPERTIES) {
      int numberOfLayers = fStyleModel.getLASLayersThatCanUseStyleProperty(property).size();
      fRadioButtons.get(property).setEnabled(numberOfLayers > 0);
    }

    int numberOfLASLayers = fStyleModel.getLASLayers().size();
    int numberOfLASLayersThatCanUseStyleProperty = fStyleModel.getLASLayersThatCanUseStyleProperty(fStyleModel.getStyleProperty()).size();
    fWarningLabel.setText(warningText(numberOfLASLayers - numberOfLASLayersThatCanUseStyleProperty));
    fWarningLabel.setVisible(numberOfLASLayers > 0 && numberOfLASLayersThatCanUseStyleProperty != numberOfLASLayers);
  }

  private class StyleAction extends AbstractAction {
    private final String fProperty;

    public StyleAction(String aName, String aProperty) {
      super(aName);
      fProperty = aProperty;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String oldProperty = fStyleModel.getStyleProperty();
      if (!fProperty.equals(oldProperty)) {
        fStyleModel.setStyleProperty(fProperty);
      }
    }
  }
}
