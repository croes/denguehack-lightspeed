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
package samples.lightspeed.plots;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;

import samples.lightspeed.plots.datamodelstyling.DataTypeStyler;
import samples.lightspeed.plots.datamodelstyling.EnumAnnotation;
import samples.lightspeed.plots.datamodelstyling.RangeAnnotation;

/**
 * Panel that displays a legend for the icons and colors used by a {@link samples.lightspeed.plots.datamodelstyling.DataTypeStyler}.
 */
public class LegendPanel extends JPanel {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM HH:mm");
  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.00");
  private static final int ICON_SIZE = 12;
  private static final Font FONT = Font.getFont("Arial");

  private DataTypeStyler fStyler;

  private JLabel fGradientLabel;
  private JLabel fColorLabel;
  private JLabel fIconLabel;

  public LegendPanel() {
    super(new GridBagLayout());
    setOpaque(false);
    initializePanel();
  }

  public void initialize(DataTypeStyler aDataTypeStyler) {
    fStyler = aDataTypeStyler;
  }

  public void updateLegend() {
    Image colorLegend = getColorLegend();
    Image gradientLegend = getGradientLegend();
    Image iconTypeLegend = getIconLegend();

    fColorLabel.setIcon(colorLegend != null ? new ImageIcon(colorLegend) : null);
    fGradientLabel.setIcon(gradientLegend != null ? new ImageIcon(gradientLegend) : null);
    fIconLabel.setIcon(iconTypeLegend != null ? new ImageIcon(iconTypeLegend) : null);

    revalidate(); // visible Swing component changed its size
  }

  private void initializePanel() {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 5, 2, 5);
    c.anchor = GridBagConstraints.NORTH;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.25;
    c.ipadx = 2;
    c.ipady = 2;

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    fGradientLabel = new JLabel();
    fGradientLabel.setOpaque(false);
    add(fGradientLabel, c);

    c.gridwidth = 1;

    c.gridx = 0;
    c.gridy = 1;
    fIconLabel = new JLabel();
    fIconLabel.setOpaque(false);
    add(fIconLabel, c);

    c.gridx = 1;
    c.gridy = 1;
    fColorLabel = new JLabel();
    fColorLabel.setOpaque(false);
    add(fColorLabel, c);
  }

  public Image getGradientLegend() {
    BufferedImage legend = null;

    if (fStyler.isUseDensity()) {
      legend = new BufferedImage(256, 32, BufferedImage.TYPE_INT_ARGB);
      paintColorGradient2(legend, Color.blue.brighter(), Color.yellow, Color.red);
      paintRangeLabels(legend, "Dense", "Sparse", "");
      return legend;
    }

    TLcdDataProperty property = fStyler.getColorProperty();
    if (property != null) {
      if (property.isAnnotationPresent(RangeAnnotation.class)) {
        legend = new BufferedImage(256, 32, BufferedImage.TYPE_INT_ARGB);
        paintColorGradient(legend, Color.orange, Color.cyan);
        if ("Time".equals(property.getName())) {
          paintDateLabels(legend, property);      //Time-Based --> special formatting
        } else {
          paintToStringLabels(legend, property);
        }
      }
    }

    return legend;
  }

  public Image getColorLegend() {
    BufferedImage legend = new BufferedImage(128, 1, BufferedImage.TYPE_INT_ARGB);

    if (fStyler.isUseDensity()) {
      return legend;
    }

    TLcdDataProperty property = fStyler.getColorProperty();
    if (property != null) {
      if (property.isAnnotationPresent(EnumAnnotation.class)) {
        EnumAnnotation enumAnnotation = property.getAnnotation(EnumAnnotation.class);
        Color[] colors = fStyler.getColors(enumAnnotation.size());
        ILcdIcon[] icons = new ILcdIcon[colors.length];
        for (int i = 0; i < colors.length; i++) {
          icons[i] = new TLcdSymbol(TLcdSymbol.FILLED_RECT, ICON_SIZE, colors[i]);
        }
        legend = drawLegend(enumAnnotation, icons);
      }
    }

    return legend;
  }

  public Image getIconLegend() {
    BufferedImage legend = new BufferedImage(128, 1, BufferedImage.TYPE_INT_ARGB);

    if (fStyler.isUseDensity()) {
      return legend;
    }

    TLcdDataProperty property = fStyler.getIconProperty();
    if (property != null && property.isAnnotationPresent(EnumAnnotation.class)) {
      EnumAnnotation enumAnnotation = property.getAnnotation(EnumAnnotation.class);
      ILcdIcon[] icons = fStyler.getIcons(enumAnnotation.size());
      legend = drawLegend(enumAnnotation, icons);
    }

    return legend;
  }

  ///////////////////////////////////////////
  //Helper methods
  ///////////////////////////////////////////

  private BufferedImage drawLegend(EnumAnnotation aEnumAnnotation, ILcdIcon[] aIcons) {
    BufferedImage legend = new BufferedImage(128, (ICON_SIZE + 4) * aIcons.length, BufferedImage.TYPE_INT_ARGB);

    for (int i = 0; i < aIcons.length; i++) {
      Object value = aEnumAnnotation.get(i);
      ILcdIcon icon = new TLcdResizeableIcon(aIcons[i], ICON_SIZE, ICON_SIZE);
      int locY = (ICON_SIZE + 4) * i + 2;
      Graphics2D graphics = (Graphics2D) legend.getGraphics();
      enableAntiAliasing(graphics);
      graphics.setColor(Color.darkGray);
      graphics.fillRect(7, locY - 1, ICON_SIZE + 2, ICON_SIZE + 2);
      icon.paintIcon(null, graphics, 8, locY);
      graphics.setPaint(Color.black);
      graphics.setFont(FONT);
      graphics.drawString(value.toString(), ICON_SIZE + 10, locY + 4 + graphics.getFontMetrics().getHeight() / 2);
    }

    return legend;
  }

  private void paintColorGradient(BufferedImage aImage, Color aFrom, Color aTo) {
    Graphics2D graphics = (Graphics2D) aImage.getGraphics();
    graphics.setPaint(new GradientPaint(0, 0, aFrom, aImage.getWidth(), 0, aTo));
    graphics.fill(new Rectangle(0, aImage.getHeight() / 2, aImage.getWidth(), aImage.getHeight() / 2));
  }

  private void paintColorGradient2(BufferedImage aImage, Color aFrom, Color aOver, Color aTo) {
    Graphics2D graphics = (Graphics2D) aImage.getGraphics();
    graphics.setPaint(new GradientPaint(0, 0, aFrom, aImage.getWidth() / 2, 0, aOver));
    graphics.fill(new Rectangle(0, aImage.getHeight() / 2, aImage.getWidth() / 2, aImage.getHeight() / 2));
    graphics.setPaint(new GradientPaint(aImage.getWidth() / 2, 0, aOver, aImage.getWidth(), 0, aTo));
    graphics.fill(new Rectangle(aImage.getWidth() / 2, aImage.getHeight() / 2, aImage.getWidth(), aImage.getHeight() / 2));
  }

  private void paintToStringLabels(BufferedImage aImage, TLcdDataProperty aProperty) {
    double min = fStyler.getRangeMinParameter(aProperty);
    double max = fStyler.getRangeMaxParameter(aProperty);
    String minLabel = NUMBER_FORMAT.format(min);
    String maxLabel = NUMBER_FORMAT.format(max);
    String propertyLabel = "- " + aProperty.getDisplayName() + " -";

    paintRangeLabels(aImage, maxLabel, minLabel, propertyLabel);
  }

  private void paintDateLabels(BufferedImage aImage, TLcdDataProperty aProperty) {
    long min = (long) fStyler.getRangeMinParameter(aProperty);
    long max = (long) fStyler.getRangeMaxParameter(aProperty);
    String minLabel = DATE_FORMAT.format(new Date(min));
    String maxLabel = DATE_FORMAT.format(new Date(max));
    paintRangeLabels(aImage, minLabel, maxLabel, "");
  }

  private void paintRangeLabels(BufferedImage aImage, String aMaxLabel, String aMinLabel, String aPropertyLabel) {
    Graphics2D graphics = (Graphics2D) aImage.getGraphics();
    enableAntiAliasing(graphics);
    FontMetrics fontMetrics = graphics.getFontMetrics();
    int maxLabelWidth = fontMetrics.stringWidth(aMaxLabel);
    int propertyLabelWidth = fontMetrics.stringWidth(aPropertyLabel);

    graphics.setPaint(Color.black);
    graphics.setFont(FONT);
    graphics.drawString(aMinLabel, 0, aImage.getHeight() / 2 - 2);
    graphics.drawString(aPropertyLabel, aImage.getWidth() / 2 - propertyLabelWidth / 2, aImage.getHeight() / 2 - 2);
    graphics.drawString(aMaxLabel, aImage.getWidth() - maxLabelWidth, aImage.getHeight() / 2 - 2);
  }

  private void enableAntiAliasing(Graphics2D aGraphics) {
    aGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }
}
