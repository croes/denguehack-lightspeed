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
package samples.decoder.ecdis.common;

import static samples.decoder.ecdis.common.S52Tooltips.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.format.s52.ILcdS52ColorProvider;
import com.luciad.format.s52.TLcdS52DisplaySettings;
import samples.common.UIColors;

/**
 * UI panel for configuration of the S-52 depth contour values.
 */
class S52DepthsPanel extends JPanel {

  // Layout settings
  private static final int DEPTH_WIDTH = 25;
  private static final int DEPTH_HALF_HEIGHT = 8;
  private static final int GAP = 6;
  private static final int HALF_ARROW_WIDTH = 7;

  private JPanel fTwoShadesPanel;
  private JPanel fFourShadesPanel;

  private ILcdS52ColorProvider fS52ColorProvider;

  private TLcdS52DisplaySettings fDisplaySettings;

  private JFormattedTextField fShallowContour;
  private JFormattedTextField fSafetyContour2;
  private JFormattedTextField fSafetyContour4;
  private JFormattedTextField fDeepContour;

  private boolean fUIIsBeingUpdated;

  private boolean fEnabled = true;

  private final PropertyChangeListener fTextFieldListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("value".equals(evt.getPropertyName()) && !fUIIsBeingUpdated) {
        applyUISettingsToDisplaySettings();
      }
    }
  };

  public S52DepthsPanel(ILcdS52ColorProvider aS52ColorProvider) {
    fS52ColorProvider = aS52ColorProvider;
    fTwoShadesPanel = createTwoShadesPanel();
    fFourShadesPanel = createFourShadesPanel();
  }

  public void setS52DisplaySettings(TLcdS52DisplaySettings aDisplaySettings) {
    fDisplaySettings = aDisplaySettings;
    updateFromDataObject();
  }

  public void setEnabled(boolean aEnabled) {
    fEnabled = aEnabled;
    updateEnabled();
  }

  public void updateFromDataObject() {
    fUIIsBeingUpdated = true;
    if (fDisplaySettings != null) {
      fShallowContour.setValue(fDisplaySettings.getShallowContour());
      fSafetyContour2.setValue(fDisplaySettings.getSafetyContour());
      fSafetyContour4.setValue(fDisplaySettings.getSafetyContour());
      fDeepContour.setValue(fDisplaySettings.getDeepContour());
      setTwoShades(fDisplaySettings.isUseTwoShades());
    }
    updateEnabled();
    fUIIsBeingUpdated = false;
  }

  private void updateEnabled() {
    boolean enabled = fEnabled && fDisplaySettings != null;
    super.setEnabled(enabled);
    fShallowContour.setEnabled(enabled);
    fSafetyContour2.setEnabled(enabled);
    fSafetyContour4.setEnabled(enabled);
    fDeepContour.setEnabled(enabled);
  }

  public void setTwoShades(boolean aTwoShades) {
    if (aTwoShades) {
      remove(fFourShadesPanel);
      add(fTwoShadesPanel);
      if (fDisplaySettings != null) {
        fSafetyContour2.setValue(fDisplaySettings.getSafetyContour());
      }
    } else {
      remove(fTwoShadesPanel);
      add(fFourShadesPanel);
      if (fDisplaySettings != null) {
        fSafetyContour4.setValue(fDisplaySettings.getSafetyContour());
      }
    }
    revalidate();
  }

  private void applyUISettingsToDisplaySettings() {
    if (fDisplaySettings != null) {
      fDisplaySettings.setShallowContour(((Number) fShallowContour.getValue()).doubleValue());
      double safetyContour = fDisplaySettings.isUseTwoShades() ?
                             ((Number) fSafetyContour2.getValue()).doubleValue() : ((Number) fSafetyContour4.getValue()).doubleValue();
      fDisplaySettings.setSafetyContour(safetyContour);
      // We keep the safety depth and safety contour synchronized, as recommended by recent IHO studies.
      fDisplaySettings.setSafetyDepth(safetyContour);
      fDisplaySettings.setDeepContour(((Number) fDeepContour.getValue()).doubleValue());
    }
  }

  private JFormattedTextField createDoubleTextField() {
    JFormattedTextField field = new JFormattedTextField(NumberFormat.getInstance());
    field.setValue(0.0);
    field.setColumns(3);
    field.addPropertyChangeListener(fTextFieldListener);
    return field;
  }

  private JPanel createTwoShadesPanel() {
    FormLayout formLayout = new FormLayout("left:pref,3dlu,left:pref,left:pref", "p,p,p");
    DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout);
    CellConstraints cc = new CellConstraints();
    formBuilder.addLabel("Safety contour (m)", cc.xy(1, 2));
    fSafetyContour2 = createDoubleTextField();
    fSafetyContour2.setToolTipText(SAFETY_CONTOUR_TOOLTIP);
    formBuilder.add(fSafetyContour2, cc.xy(3, 2));
    formBuilder.add(new JLabel(new DepthIcon(DepthAreaType.VERY_SHALLOW, true)), cc.xy(4, 1));
    formBuilder.add(new JLabel(new ContourAndDepthIcon(true, DepthAreaType.VERY_SHALLOW, DepthAreaType.DEEP)), cc.xy(4, 2));
    formBuilder.add(new JLabel(new DepthIcon(DepthAreaType.DEEP, false)), cc.xy(4, 3));
    return formBuilder.getPanel();
  }

  private JPanel createFourShadesPanel() {
    FormLayout formLayout = new FormLayout("left:pref,3dlu,left:pref,left:pref", "p,p,p,p,p");
    DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout);
    CellConstraints cc = new CellConstraints();
    formBuilder.addLabel("Shallow contour (m)", cc.xy(1, 2));
    formBuilder.addLabel("Safety contour (m)", cc.xy(1, 3));
    formBuilder.addLabel("Deep contour (m)", cc.xy(1, 4));
    fShallowContour = createDoubleTextField();
    fShallowContour.setToolTipText(SHALLOW_CONTOUR_TOOLTIP);
    fSafetyContour4 = createDoubleTextField();
    fSafetyContour4.setToolTipText(SAFETY_CONTOUR_TOOLTIP);
    fDeepContour = createDoubleTextField();
    fDeepContour.setToolTipText(DEEP_CONTOUR_TOOLTIP);
    formBuilder.add(fShallowContour, cc.xy(3, 2));
    formBuilder.add(fSafetyContour4, cc.xy(3, 3));
    formBuilder.add(fDeepContour, cc.xy(3, 4));
    formBuilder.add(new JLabel(new DepthIcon(DepthAreaType.VERY_SHALLOW, true)), cc.xy(4, 1));
    formBuilder.add(new JLabel(new ContourAndDepthIcon(false, DepthAreaType.VERY_SHALLOW, DepthAreaType.MEDIUM_SHALLOW)), cc.xy(4, 2));
    formBuilder.add(new JLabel(new ContourAndDepthIcon(true, DepthAreaType.MEDIUM_SHALLOW, DepthAreaType.MEDIUM_DEEP)), cc.xy(4, 3));
    formBuilder.add(new JLabel(new ContourAndDepthIcon(false, DepthAreaType.MEDIUM_DEEP, DepthAreaType.DEEP)), cc.xy(4, 4));
    formBuilder.add(new JLabel(new DepthIcon(DepthAreaType.DEEP, false)), cc.xy(4, 5));
    return formBuilder.getPanel();
  }

  private enum DepthAreaType {

    VERY_SHALLOW("DEPVS"), MEDIUM_SHALLOW("DEPMS"), MEDIUM_DEEP("DEPMD"), DEEP("DEPDW");

    private String fCode;

    DepthAreaType(String aCode) {
      fCode = aCode;
    }

    private String getCode() {
      return fCode;
    }
  }

  private class ContourAndDepthIcon implements Icon {

    private boolean fSafetyContour;

    private DepthAreaType fDepthAreaType1;
    private DepthAreaType fDepthAreaType2;

    private ContourAndDepthIcon(boolean aIsSafetyContour, DepthAreaType aDepthAreaType1, DepthAreaType aDepthAreaType2) {
      fSafetyContour = aIsSafetyContour;
      fDepthAreaType1 = aDepthAreaType1;
      fDepthAreaType2 = aDepthAreaType2;
    }

    @Override
    public int getIconWidth() {
      return 100;
    }

    @Override
    public int getIconHeight() {
      return (2 * DEPTH_HALF_HEIGHT) + (2 * GAP) + 1;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g = g.create();
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Depth area 1
      g.translate(x, y);
      drawHalfColorPad(g, fDepthAreaType1, false);

      // Arrow with safety contour
      g.translate(0, GAP + 1 + DEPTH_HALF_HEIGHT);
      int thickness = fSafetyContour ? 1 : 0;
      int[] xCoords = new int[]{0, 10, 100, 100, 10, 0, 0};
      int[] yCoords = new int[]{-HALF_ARROW_WIDTH, -thickness - 1, -thickness - 1, thickness, thickness, HALF_ARROW_WIDTH, 0};
      g.setColor(Color.BLACK);
      g.fillPolygon(xCoords, yCoords, xCoords.length);

      // Labels
      if (fSafetyContour) {
        g.setColor(UIColors.fgHint());
        g.setFont(g.getFont().deriveFont(Font.ITALIC, 11));
        g.drawString("Unsafe", 60, -4);
        g.drawString("Safe", 60, 1 + g.getFontMetrics().getAscent());
      }

      // Depth area 2
      g.translate(0, GAP);
      drawHalfColorPad(g, fDepthAreaType2, true);
    }

  }

  private void drawHalfColorPad(Graphics g, DepthAreaType aDepthAreaType, boolean aTopHalf) {
    // The outline
    g.setColor(Color.DARK_GRAY);
    g.drawRect(20, 0, DEPTH_WIDTH - 1, DEPTH_HALF_HEIGHT - 1);

    // The fill
    g.setColor(fS52ColorProvider.getS52Color(aDepthAreaType.getCode()));
    g.fillRect(21, aTopHalf ? 1 : 0, DEPTH_WIDTH - 2, DEPTH_HALF_HEIGHT - 1);
  }

  private class DepthIcon implements Icon {

    private DepthAreaType fDepthAreaType;
    private boolean fTop;

    private DepthIcon(DepthAreaType aDepthAreaType, boolean aTop) {
      fDepthAreaType = aDepthAreaType;
      fTop = aTop;
    }

    // Implementations for Icon

    @Override
    public int getIconWidth() {
      return 100;
    }

    @Override
    public int getIconHeight() {
      return DEPTH_HALF_HEIGHT;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g = g.create();
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Depth areas
      g.translate(x, y);
      drawHalfColorPad(g, fDepthAreaType, fTop);
    }

  }

}
