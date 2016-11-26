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
package samples.lucy.cop.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponentFactory;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lucy.frontend.mapcentric.map.MapCentricMapComponentFactory;

/**
 * Extension of the default {@link TLcyLspMapComponentFactory} which
 * decorates the view with a classification
 */
public class ClassificationMapComponentFactory extends MapCentricMapComponentFactory {
  /**
   * Key which should be used to put a {@link Classification} into the
   * {@link ILcyLspMapComponent#getProperties() properties} of
   * a map component. The corresponding value is the {@link Classification#name() name}
   * of the classification
   */
  public static final String CLASSIFICATION_KEY = "Lcd_classification";

  /**
   * The different types of classifications
   */
  public static enum Classification {
    UNCLASSIFIED,
    DEMO_SECRET,
    DEFAULT
  }

  public ClassificationMapComponentFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  protected ILcyLspMapComponent createGUIContent(ALcyProperties aProperties) {
    TLcyLspMapComponent mapComponent = getMapComponent();
    if (mapComponent != null) {
      mapComponent.setLayout(new BorderLayout());

      //insert all the panels into the GUI of the map component
      Component northPanel = getPanel(NORTH_PANEL);
      Component eastPanel = getPanel(EAST_PANEL);
      Component westPanel = getPanel(WEST_PANEL);
      Component southPanel = getPanel(SOUTH_PANEL);
      Component mapCenterOverlayPanel = getPanel(CENTER_OVERLAY_PANEL);

      if (northPanel != null) {
        mapComponent.add(northPanel, BorderLayout.NORTH);
      }
      if (eastPanel != null) {
        mapComponent.add(eastPanel, BorderLayout.EAST);
      }
      if (westPanel != null) {
        mapComponent.add(westPanel, BorderLayout.WEST);
      }
      if (southPanel != null) {
        mapComponent.add(southPanel, BorderLayout.SOUTH);
      }
      if (mapCenterOverlayPanel != null) {
        mapComponent.add(new ClassificationPanel(mapComponent, "TLcyLspMapAddOn.", aProperties, mapCenterOverlayPanel), BorderLayout.CENTER);
      }
      addLogoComponent(aProperties);
    }
    return mapComponent;
  }

  private void addLogoComponent(ALcyProperties aProperties) {
    //instead of adding the logo on the map, add it in the right toolbar
    ILcyToolBar rightToolBar = getToolBar(RIGHT_TOOL_BAR);
    if (rightToolBar != null) {
      String logoPath = aProperties.getString("TLcyLspMapAddOn.logo.imageFileName", null);
      if (logoPath != null) {
        TLcdImageIcon imageIcon = new TLcdImageIcon(logoPath);
        JLabel label = new JLabel(new TLcdSWIcon(imageIcon));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        rightToolBar.insertComponent(label, new TLcyGroupDescriptor("LogoGroup"));
      }
    }
  }

  private static class ClassificationPanel extends JPanel {

    private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ILcyLspMapComponent.class);

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String TEXT_COLOR = "textColor";
    private static final String CLASSIFICATION = "text";

    private static final String FONT_PREFIX = "font.";
    private static final String FONT_NAME = FONT_PREFIX + "name";
    private static final String FONT_STYLE = FONT_PREFIX + "style";
    private static final String FONT_SIZE = FONT_PREFIX + "size";

    private final ILcyLspMapComponent fMapComponent;
    private final ALcyProperties fConfigurationSettings;
    private final String fConfigurationPrefix;
    private JLabel fUpperClassification = new JLabel();
    private JLabel fLowerClassification = new JLabel();

    private ClassificationPanel(ILcyLspMapComponent aMapComponent, String aConfigurationPrefix, ALcyProperties aConfigurationSettings, Component aContentToDecorate) {
      fMapComponent = aMapComponent;
      fConfigurationPrefix = aConfigurationPrefix;
      fConfigurationSettings = aConfigurationSettings;
      Classification defaultClassification = Classification.DEFAULT;
      updateLabelForClassification(fLowerClassification, defaultClassification);
      updateLabelForClassification(fUpperClassification, defaultClassification);

      setLayout(new BorderLayout());
      add(aContentToDecorate, BorderLayout.CENTER);
      add(fUpperClassification, BorderLayout.NORTH);
      add(fLowerClassification, BorderLayout.SOUTH);

      updateFromMapComponentProperties();
      fMapComponent.getProperties().addPropertyChangeListener(new ClassificationSynchronizationListener());
    }

    private String getClassificationPrefix(Classification classification) {
      return fConfigurationPrefix + "classification." + classification.name() + ".";
    }

    private void updateLabelForClassification(JLabel aLabelSFCT, Classification aClassification) {
      String prefix = getClassificationPrefix(aClassification);
      Color backgroundColor = fConfigurationSettings.getColor(prefix + BACKGROUND_COLOR, new Color(255, 255, 255));
      Color textColor = fConfigurationSettings.getColor(prefix + TEXT_COLOR, new Color(255, 255, 255));
      String text = fConfigurationSettings.getString(prefix + CLASSIFICATION, "UNCLASSIFIED");
      Font font = retrieveFont(prefix, fConfigurationSettings);

      aLabelSFCT.setForeground(textColor);
      aLabelSFCT.setBackground(backgroundColor);
      aLabelSFCT.setFont(font);
      aLabelSFCT.setText(text);
      aLabelSFCT.setHorizontalAlignment(SwingConstants.CENTER);
      aLabelSFCT.setOpaque(true);
    }

    private Font retrieveFont(String aPropertyPrefix, ALcyProperties aProperties) {
      Font defaultFont = new Font("serif", Font.PLAIN, 13);
      String name = aProperties.getString(aPropertyPrefix + FONT_NAME, null);
      int style = aProperties.getInt(aPropertyPrefix + FONT_STYLE, defaultFont.getStyle());
      int size = aProperties.getInt(aPropertyPrefix + FONT_SIZE, defaultFont.getSize());
      if (name != null) {
        return new Font(name, style, size);
      } else {
        return defaultFont;
      }
    }

    private void updateFromMapComponentProperties() {
      String name = fMapComponent.getProperties().getString(CLASSIFICATION_KEY, null);
      if (name == null) {
        return;
      }
      try {
        Classification classification = Classification.valueOf(name);
        updateLabelForClassification(fUpperClassification, classification);
        updateLabelForClassification(fLowerClassification, classification);

        repaint();
      } catch (IllegalArgumentException e) {
        LOGGER.error("The map component properties contains an invalid classification name [" + name + "]. Setting will be ignored");
      }
    }

    private class ClassificationSynchronizationListener implements PropertyChangeListener {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (CLASSIFICATION_KEY.equals(evt.getPropertyName())) {
          updateFromMapComponentProperties();
        }
      }
    }
  }
}
